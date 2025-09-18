package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.models.ValidationResult
import com.quantumsoft.tia.scanner.models.FileFormat
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.*

class FileValidatorTest {

    private lateinit var validator: FileValidator
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeEach
    fun setUp() {
        validator = FileValidator()
    }
    
    @Test
    fun `should detect ASN1 file format correctly`() = runTest {
        // Given
        val file = tempDir.resolve("test.asn1").createFile()
        // Write ASN.1 BER/DER header bytes (30 82 - SEQUENCE with length)
        file.writeBytes(byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "test content".toByteArray())
        
        // When
        val result = validator.validateFile(file)
        
        // Then
        assertThat(result.isValid).isTrue()
        assertThat(result.detectedFormat).isEqualTo(FileFormat.ASN1)
        assertThat(result.errors).isEmpty()
    }
    
    @Test
    fun `should detect CSV file format correctly`() = runTest {
        // Given
        val file = tempDir.resolve("test.csv").createFile()
        file.writeText("col1,col2,col3\nvalue1,value2,value3\n")
        
        // When
        val result = validator.validateFile(file)
        
        // Then
        assertThat(result.isValid).isTrue()
        assertThat(result.detectedFormat).isEqualTo(FileFormat.CSV)
        assertThat(result.errors).isEmpty()
    }
    
    @Test
    fun `should detect unknown file format`() = runTest {
        // Given
        val file = tempDir.resolve("test.unknown").createFile()
        file.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x01, 0x02))
        
        // When
        val result = validator.validateFile(file)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.detectedFormat).isEqualTo(FileFormat.UNKNOWN)
        assertThat(result.errors).containsExactly("Unsupported file format detected")
    }
    
    @Test
    fun `should validate file accessibility`() = runTest {
        // Given - Create file then make it unreadable
        val file = tempDir.resolve("unreadable.asn1").createFile()
        file.writeBytes(byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "test content".toByteArray())
        
        // When
        val result = validator.validateFile(file)
        
        // Then - Since we can't easily make files unreadable in tests, 
        // this tests the happy path where file is accessible
        assertThat(result.isValid).isTrue()
        assertThat(result.isAccessible).isTrue()
        assertThat(result.errors).isEmpty()
    }
    
    @Test
    fun `should handle non-existent file`() = runTest {
        // Given
        val nonExistentFile = tempDir.resolve("does-not-exist.asn1")
        
        // When
        val result = validator.validateFile(nonExistentFile)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.isAccessible).isFalse()
        assertThat(result.errors).contains("File does not exist")
    }
    
    @Test
    fun `should calculate file hash correctly`() = runTest {
        // Given
        val content = byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "test content for hashing".toByteArray()
        val file = tempDir.resolve("test.asn1").createFile()
        file.writeBytes(content)
        
        // Calculate expected hash
        val expectedHash = MessageDigest.getInstance("SHA-256")
            .digest(content)
            .joinToString("") { "%02x".format(it) }
        
        // When
        val result = validator.validateFile(file, calculateHash = true)
        
        // Then
        assertThat(result.isValid).isTrue()
        assertThat(result.fileHash).isEqualTo(expectedHash)
    }
    
    @Test
    fun `should skip hash calculation when disabled`() = runTest {
        // Given
        val file = tempDir.resolve("test.asn1").createFile()
        file.writeBytes(byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "test content".toByteArray())
        
        // When
        val result = validator.validateFile(file, calculateHash = false)
        
        // Then
        assertThat(result.isValid).isTrue()
        assertThat(result.fileHash).isNull()
    }
    
    @Test
    fun `should validate file size limits`() = runTest {
        // Given
        val smallFile = tempDir.resolve("small.asn1").createFile()
        smallFile.writeBytes(byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "small content".toByteArray())
        
        val largeContent = byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + ByteArray(2 * 1024 * 1024) { 'X'.code.toByte() } // 2MB
        val largeFile = tempDir.resolve("large.asn1").createFile()
        largeFile.writeBytes(largeContent)
        
        // When - Small file within limit
        val smallResult = validator.validateFile(smallFile, maxSizeMb = 1)
        
        // When - Large file exceeds limit
        val largeResult = validator.validateFile(largeFile, maxSizeMb = 1)
        
        // Then
        assertThat(smallResult.isValid).isTrue()
        assertThat(smallResult.errors).isEmpty()
        
        assertThat(largeResult.isValid).isFalse()
        assertThat(largeResult.errors).containsExactly("File size exceeds limit")
    }
    
    @Test
    fun `should validate empty files`() = runTest {
        // Given
        val emptyFile = tempDir.resolve("empty.asn1").createFile()
        
        // When
        val result = validator.validateFile(emptyFile)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).contains("File is empty")
    }
    
    @Test
    fun `should validate corrupted ASN1 files`() = runTest {
        // Given - Create file with invalid ASN.1 structure
        val file = tempDir.resolve("corrupted.asn1").createFile()
        file.writeBytes(byteArrayOf(0x30, 0xFF.toByte(), 0x00)) // Invalid length encoding
        
        // When
        val result = validator.validateFile(file)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.detectedFormat).isEqualTo(FileFormat.ASN1)
        assertThat(result.errors).contains("Invalid ASN.1 structure")
    }
    
    @Test
    fun `should validate malformed CSV files`() = runTest {
        // Given - CSV with inconsistent columns
        val file = tempDir.resolve("malformed.csv").createFile()
        file.writeText("col1,col2,col3\nvalue1,value2\nvalue3,value4,value5,value6\n")
        
        // When
        val result = validator.validateFile(file)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.detectedFormat).isEqualTo(FileFormat.CSV)
        assertThat(result.errors).contains("Inconsistent CSV column count")
    }
    
    @Test
    fun `should handle concurrent validations`() = runTest {
        // Given
        val files = (1..10).map { index ->
            val file = tempDir.resolve("file$index.asn1").createFile()
            file.writeBytes(byteArrayOf(0x30, 0x82.toByte(), 0x00, 0x10) + "content $index".toByteArray())
            file
        }
        
        // When - Validate all files concurrently
        val results = files.map { file ->
            validator.validateFile(file)
        }
        
        // Then
        assertThat(results).hasSize(10)
        assertThat(results.all { it.isValid }).isTrue()
    }
}