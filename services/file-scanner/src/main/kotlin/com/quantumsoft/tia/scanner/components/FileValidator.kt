package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.models.FileFormat
import com.quantumsoft.tia.scanner.models.ValidationResult
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * File validator component that performs format detection, accessibility checks,
 * hash calculation, and content validation.
 */

@Component
class FileValidator {
    
    /**
     * Validates a file including format detection, accessibility, and optional hash calculation
     */
    suspend fun validateFile(
        file: Path,
        calculateHash: Boolean = true,
        maxSizeMb: Int = Int.MAX_VALUE
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check file existence and accessibility
        if (!file.exists()) {
            return ValidationResult(
                isValid = false,
                isAccessible = false,
                detectedFormat = FileFormat.UNKNOWN,
                errors = listOf("File does not exist")
            )
        }
        
        if (!file.isRegularFile()) {
            errors.add("Path is not a regular file")
        }
        
        if (!file.isReadable()) {
            return ValidationResult(
                isValid = false,
                isAccessible = false,
                detectedFormat = FileFormat.UNKNOWN,
                errors = listOf("File is not readable")
            )
        }
        
        // Get file size
        val fileSizeBytes = try {
            file.fileSize()
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                isAccessible = false,
                detectedFormat = FileFormat.UNKNOWN,
                errors = listOf("Cannot read file size: ${e.message}")
            )
        }
        
        // Check if file is empty
        if (fileSizeBytes == 0L) {
            return ValidationResult(
                isValid = false,
                isAccessible = true,
                detectedFormat = FileFormat.UNKNOWN,
                fileSizeBytes = fileSizeBytes,
                errors = listOf("File is empty")
            )
        }
        
        // Check size limits
        val maxSizeBytes = maxSizeMb * 1024L * 1024L
        if (fileSizeBytes > maxSizeBytes) {
            return ValidationResult(
                isValid = false,
                isAccessible = true,
                detectedFormat = FileFormat.UNKNOWN,
                fileSizeBytes = fileSizeBytes,
                errors = listOf("File size exceeds limit")
            )
        }
        
        // Detect file format
        val detectedFormat = detectFileFormat(file)
        
        // Validate file content based on format
        when (detectedFormat) {
            FileFormat.ASN1 -> validateAsn1Content(file, errors)
            FileFormat.CSV -> validateCsvContent(file, errors)
            FileFormat.UNKNOWN -> errors.add("Unsupported file format detected")
        }
        
        // Calculate hash if requested
        val fileHash = if (calculateHash) {
            calculateFileHash(file)
        } else {
            null
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            isAccessible = true,
            detectedFormat = detectedFormat,
            fileHash = fileHash,
            fileSizeBytes = fileSizeBytes,
            errors = errors
        )
    }
    
    /**
     * Detects file format based on file extension and content analysis
     */
    private fun detectFileFormat(file: Path): FileFormat {
        val fileName = file.fileName.toString().lowercase()
        val extension = fileName.substringAfterLast('.', "")
        
        return when (extension) {
            "asn1", "asn" -> {
                if (isValidAsn1File(file)) FileFormat.ASN1 else FileFormat.UNKNOWN
            }
            "csv" -> {
                if (isValidCsvFile(file)) FileFormat.CSV else FileFormat.UNKNOWN
            }
            else -> {
                // Try to detect by content
                when {
                    isValidAsn1File(file) -> FileFormat.ASN1
                    isValidCsvFile(file) -> FileFormat.CSV
                    else -> FileFormat.UNKNOWN
                }
            }
        }
    }
    
    /**
     * Checks if file has valid ASN.1 structure by examining BER/DER header
     */
    private fun isValidAsn1File(file: Path): Boolean {
        return try {
            val bytes = file.readBytes().take(4).toByteArray()
            if (bytes.isEmpty()) return false
            
            // Check for ASN.1 SEQUENCE tag (0x30)
            bytes[0] == 0x30.toByte()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if file has valid CSV structure
     */
    private fun isValidCsvFile(file: Path): Boolean {
        return try {
            val content = file.readText()
            if (content.isBlank()) return false
            
            // Basic CSV detection - look for comma-separated values
            content.contains(',') && content.contains('\n')
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validates ASN.1 file content structure
     */
    private fun validateAsn1Content(file: Path, errors: MutableList<String>) {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 2) {
                errors.add("Invalid ASN.1 structure")
                return
            }
            
            // Basic ASN.1 structure validation
            if (bytes[0] != 0x30.toByte()) {
                errors.add("Invalid ASN.1 structure")
                return
            }
            
            // Check length encoding
            val lengthByte = bytes[1].toInt() and 0xFF
            if (lengthByte == 0xFF) {
                errors.add("Invalid ASN.1 structure")
                return
            }
            
        } catch (e: Exception) {
            errors.add("Error reading ASN.1 file: ${e.message}")
        }
    }
    
    /**
     * Validates CSV file content structure
     */
    private fun validateCsvContent(file: Path, errors: MutableList<String>) {
        try {
            val lines = file.readLines().filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                errors.add("CSV file has no content")
                return
            }
            
            // Check column consistency
            if (lines.size > 1) {
                val expectedColumns = lines[0].split(',').size
                for ((index, line) in lines.drop(1).withIndex()) {
                    val actualColumns = line.split(',').size
                    if (actualColumns != expectedColumns) {
                        errors.add("Inconsistent CSV column count")
                        break
                    }
                }
            }
            
        } catch (e: Exception) {
            errors.add("Error reading CSV file: ${e.message}")
        }
    }
    
    /**
     * Calculates SHA-256 hash of the file
     */
    private fun calculateFileHash(file: Path): String {
        return try {
            val bytes = file.readBytes()
            val digest = MessageDigest.getInstance("SHA-256")
            digest.digest(bytes).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
