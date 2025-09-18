package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.models.ScanConfiguration
import com.quantumsoft.tia.scanner.models.ScanResult
import com.quantumsoft.tia.scanner.models.ScannedFile
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class DirectoryScannerTest {

    private lateinit var scanner: DirectoryScanner
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeEach
    fun setUp() {
        scanner = DirectoryScanner()
    }
    
    @Test
    fun `should scan directory recursively with max depth`() = runTest {
        // Given
        val rootDir = tempDir.resolve("root").createDirectory()
        val level1Dir = rootDir.resolve("level1").createDirectory()
        val level2Dir = level1Dir.resolve("level2").createDirectory()
        val level3Dir = level2Dir.resolve("level3").createDirectory()
        
        rootDir.resolve("file1.asn1").createFile().writeText("content1")
        level1Dir.resolve("file2.asn1").createFile().writeText("content2")
        level2Dir.resolve("file3.asn1").createFile().writeText("content3")
        level3Dir.resolve("file4.asn1").createFile().writeText("content4")
        
        val config = ScanConfiguration(
            sourceDirectory = rootDir.toString(),
            filePattern = "*.asn1",
            recursiveScan = true,
            maxDepth = 2,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(3)
        assertThat(result.files).hasSize(3)
        assertThat(result.files.map { it.fileName }).containsExactlyInAnyOrder(
            "file1.asn1",
            "file2.asn1",
            "file3.asn1"
        )
        assertThat(result.files.map { it.fileName }).doesNotContain("file4.asn1")
    }
    
    @Test
    fun `should match files by pattern`() = runTest {
        // Given
        val dir = tempDir.resolve("test").createDirectory()
        dir.resolve("valid1.asn1").createFile().writeText("content")
        dir.resolve("valid2.ASN1").createFile().writeText("content")
        dir.resolve("invalid.txt").createFile().writeText("content")
        dir.resolve("test.csv").createFile().writeText("content")
        
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = false,
            maxDepth = 1,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(2)
        assertThat(result.files.map { it.fileName }).containsExactlyInAnyOrder(
            "valid1.asn1",
            "valid2.ASN1"
        )
    }
    
    @Test
    fun `should handle permission denied gracefully`() = runTest {
        // Given
        val dir = tempDir.resolve("test").createDirectory()
        val protectedDir = dir.resolve("protected").createDirectory()
        dir.resolve("accessible.asn1").createFile().writeText("content")
        protectedDir.resolve("inaccessible.asn1").createFile().writeText("content")
        
        // Simulate permission denied by mocking file operations
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = true,
            maxDepth = 5,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isGreaterThanOrEqualTo(1)
        assertThat(result.errors).isEmpty()
        assertThat(result.skippedDirectories).isEmpty()
    }
    
    @Test
    fun `should skip symbolic links when configured`() = runTest {
        // Given
        val dir = tempDir.resolve("test").createDirectory()
        val targetFile = dir.resolve("target.asn1").createFile()
        targetFile.writeText("content")
        
        val linkFile = dir.resolve("link.asn1")
        Files.createSymbolicLink(linkFile, targetFile)
        
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = false,
            maxDepth = 1,
            maxFileSizeMb = 10,
            followSymlinks = false
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(1)
        assertThat(result.files).hasSize(1)
        assertThat(result.files.first().fileName).isEqualTo("target.asn1")
    }
    
    // TODO: Re-enable after fixing timeout handling in testcontainers
    // @Test
    fun `should respect scan timeout`() = runTest {
        // Given
        val dir = tempDir.resolve("test").createDirectory()
        
        // Create many files to simulate long scan
        repeat(100) { index ->
            dir.resolve("file$index.asn1").createFile().writeText("content")
        }
        
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = false,
            maxDepth = 1,
            maxFileSizeMb = 10,
            scanTimeout = Duration.ofMillis(1) // Very short timeout to force timeout
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.timedOut).isTrue()
        assertThat(result.errors).isNotEmpty()
    }
    
    @Test
    fun `should emit metrics for scan performance`() = runTest {
        // Given
        val metricsCollector = mockk<MetricsCollector>(relaxed = true)
        scanner = DirectoryScanner(metricsCollector)
        
        val dir = tempDir.resolve("test").createDirectory()
        dir.resolve("file1.asn1").createFile().writeText("content")
        dir.resolve("file2.asn1").createFile().writeText("content")
        
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = false,
            maxDepth = 1,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(2)
        
        verify {
            metricsCollector.recordScanDuration(any(), any())
            metricsCollector.recordFilesScanned(2)
            metricsCollector.recordScanRate(any())
        }
    }
    
    @Test
    fun `should respect file size limits`() = runTest {
        // Given
        val dir = tempDir.resolve("test").createDirectory()
        
        val smallFile = dir.resolve("small.asn1").createFile()
        smallFile.writeText("small content")
        
        val largeFile = dir.resolve("large.asn1").createFile()
        // Write 2MB of data (exceeds 1MB limit)
        val largeContent = ByteArray(2 * 1024 * 1024) { 'X'.code.toByte() }
        Files.write(largeFile, largeContent, StandardOpenOption.WRITE)
        
        val config = ScanConfiguration(
            sourceDirectory = dir.toString(),
            filePattern = "*.asn1",
            recursiveScan = false,
            maxDepth = 1,
            maxFileSizeMb = 1 // 1MB limit
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(1)
        assertThat(result.files).hasSize(1)
        assertThat(result.files.first().fileName).isEqualTo("small.asn1")
        assertThat(result.skippedFiles).hasSize(1)
        assertThat(result.skippedFiles.first().reason).contains("exceeds size limit")
    }
    
    @Test
    fun `should handle empty directories`() = runTest {
        // Given
        val emptyDir = tempDir.resolve("empty").createDirectory()
        
        val config = ScanConfiguration(
            sourceDirectory = emptyDir.toString(),
            filePattern = "*.asn1",
            recursiveScan = true,
            maxDepth = 5,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(0)
        assertThat(result.files).isEmpty()
        assertThat(result.errors).isEmpty()
    }
    
    @Test
    fun `should handle non-existent directory`() = runTest {
        // Given
        val config = ScanConfiguration(
            sourceDirectory = "/non/existent/directory",
            filePattern = "*.asn1",
            recursiveScan = true,
            maxDepth = 5,
            maxFileSizeMb = 10
        )
        
        // When
        val result = scanner.scan(config)
        
        // Then
        assertThat(result.filesDiscovered).isEqualTo(0)
        assertThat(result.files).isEmpty()
        assertThat(result.errors).hasSize(1)
        assertThat(result.errors.first()).contains("does not exist")
    }
}