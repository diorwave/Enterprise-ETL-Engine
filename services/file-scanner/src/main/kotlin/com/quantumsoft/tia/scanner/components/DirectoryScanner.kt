package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.models.ScanConfiguration
import com.quantumsoft.tia.scanner.models.ScanResult
import com.quantumsoft.tia.scanner.models.ScannedFile
import com.quantumsoft.tia.scanner.models.SkippedFile
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Component
import java.nio.file.*
import java.time.Duration
import java.time.Instant
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * Directory scanner component that recursively scans directories for files matching patterns.
 * Implements file discovery, filtering, validation, and metrics collection.
 */
@Component
class DirectoryScanner(
    private val metricsCollector: MetricsCollector? = null
) {
    
    /**
     * Scans the configured directory for files matching the specified pattern and constraints.
     */
    suspend fun scan(config: ScanConfiguration): ScanResult {
        val startTime = Instant.now()
        
        return try {
            withTimeout(config.scanTimeout.toMillis()) {
                performScan(config, startTime)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            ScanResult(
                timedOut = true,
                errors = listOf("Scan timeout exceeded: ${config.scanTimeout}"),
                scanDuration = Duration.between(startTime, Instant.now()),
                timestamp = startTime
            )
        } catch (e: Exception) {
            ScanResult(
                errors = listOf("Scan failed: ${e.message}"),
                scanDuration = Duration.between(startTime, Instant.now()),
                timestamp = startTime
            )
        }
    }
    
    private fun performScan(config: ScanConfiguration, startTime: Instant): ScanResult {
        val sourceDir = Paths.get(config.sourceDirectory)
        
        // Check if directory exists
        if (!sourceDir.exists()) {
            return ScanResult(
                errors = listOf("Directory does not exist: ${config.sourceDirectory}"),
                scanDuration = Duration.between(startTime, Instant.now()),
                timestamp = startTime
            )
        }
        
        if (!sourceDir.isDirectory()) {
            return ScanResult(
                errors = listOf("Path is not a directory: ${config.sourceDirectory}"),
                scanDuration = Duration.between(startTime, Instant.now()),
                timestamp = startTime
            )
        }
        
        val files = mutableListOf<ScannedFile>()
        val skippedFiles = mutableListOf<SkippedFile>()
        val errors = mutableListOf<String>()
        val skippedDirectories = mutableListOf<String>()
        
        try {
            // Precompile case-insensitive regex from glob pattern
            val patternRegex = globToRegex(config.filePattern)
            val maxDepth = if (config.recursiveScan) config.maxDepth + 1 else 1
            
            val visitOptions = if (config.followSymlinks) arrayOf(FileVisitOption.FOLLOW_LINKS) else arrayOf()
            Files.walk(sourceDir, maxDepth, *visitOptions)
                .use { paths ->
                    paths.forEach { path ->
                        try {
                            when {
                                path == sourceDir -> return@forEach // Skip root directory
                                path.isDirectory() -> return@forEach // Skip directories
                                !config.followSymlinks && path.isSymbolicLink() -> return@forEach // Skip symlinks if not following
                                !matchesPattern(path.fileName.toString(), patternRegex) -> return@forEach // Skip files that don't match pattern
                                else -> {
                                    val fileSize = path.fileSize()
                                    val maxSizeBytes = config.maxFileSizeMb * 1024L * 1024L
                                    
                                    if (fileSize > maxSizeBytes) {
                                        skippedFiles.add(
                                            SkippedFile(
                                                filePath = path.toString(),
                                                reason = "File size ${fileSize / (1024 * 1024)}MB exceeds size limit ${config.maxFileSizeMb}MB"
                                            )
                                        )
                                        return@forEach
                                    }
                                    
                                    files.add(
                                        ScannedFile(
                                            filePath = path.toString(),
                                            fileName = path.fileName.toString(),
                                            fileSizeBytes = fileSize,
                                            lastModified = path.getLastModifiedTime().toInstant(),
                                            fileHash = calculateFileHash(path)
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Log error but continue scanning
                            errors.add("Error scanning ${path}: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            errors.add("Error during directory walk: ${e.message}")
        }
        
        val endTime = Instant.now()
        val scanDuration = Duration.between(startTime, endTime)
        
        // Emit metrics
        metricsCollector?.let { collector ->
            collector.recordScanDuration(config.sourceDirectory, scanDuration)
            collector.recordFilesScanned(files.size)
            collector.recordScanRate(files.size.toDouble() / scanDuration.toSeconds().coerceAtLeast(1))
        }
        
        return ScanResult(
            filesDiscovered = files.size,
            files = files,
            errors = errors,
            skippedDirectories = skippedDirectories,
            skippedFiles = skippedFiles,
            timedOut = false,
            scanDuration = scanDuration,
            timestamp = startTime
        )
    }
    
    /**
     * Case-insensitive glob pattern matching compiled to Regex.
     */
    private fun matchesPattern(fileName: String, compiled: Regex): Boolean {
        return compiled.matches(fileName)
    }

    /**
     * Convert a glob pattern to a Regex with IGNORE_CASE.
     * Supports '*' and '?' wildcards; escapes other regex metacharacters.
     */
    private fun globToRegex(glob: String): Regex {
        val sb = StringBuilder()
        sb.append('^')
        var i = 0
        while (i < glob.length) {
            when (val ch = glob[i]) {
                '*' -> sb.append(".*")
                '?' -> sb.append('.')
                '.', '(', ')', '+', '|', '^', '$', '{', '}', '[', ']', '\\' -> {
                    sb.append('\\').append(ch)
                }
                else -> sb.append(ch)
            }
            i++
        }
        sb.append('$')
        return Regex(sb.toString(), setOf(RegexOption.IGNORE_CASE))
    }

    /**
     * Calculates SHA-256 hash of the file contents. Returns empty string on failure.
     */
    private fun calculateFileHash(path: Path): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            path.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int
                while (true) {
                    read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
