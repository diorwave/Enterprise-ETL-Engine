package com.quantumsoft.tia.scanner.models

import java.time.Duration
import java.time.Instant

data class ScanConfiguration(
    val sourceDirectory: String,
    val filePattern: String,
    val recursiveScan: Boolean = true,
    val maxDepth: Int = 10,
    val maxFileSizeMb: Int = 1024,
    val followSymlinks: Boolean = false,
    val scanTimeout: Duration = Duration.ofMinutes(5)
)

data class ScanResult(
    val filesDiscovered: Int = 0,
    val files: List<ScannedFile> = emptyList(),
    val errors: List<String> = emptyList(),
    val skippedDirectories: List<String> = emptyList(),
    val skippedFiles: List<SkippedFile> = emptyList(),
    val timedOut: Boolean = false,
    val scanDuration: Duration = Duration.ZERO,
    val timestamp: Instant = Instant.now()
)

data class ScannedFile(
    val filePath: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val lastModified: Instant,
    val fileHash: String? = null
)

data class SkippedFile(
    val filePath: String,
    val reason: String
)

/**
 * File format enumeration for supported file types
 */
enum class FileFormat {
    ASN1,
    CSV,
    UNKNOWN
}

/**
 * Result of file validation including format detection and accessibility
 */
data class ValidationResult(
    val isValid: Boolean,
    val isAccessible: Boolean = true,
    val detectedFormat: FileFormat,
    val fileHash: String? = null,
    val fileSizeBytes: Long = 0L,
    val errors: List<String> = emptyList()
)

