package com.tia.etl.parser.api.models

/**
 * Result of validating a file before processing.
 * 
 * This class contains the outcome of file validation, including whether
 * the file is valid for processing and any issues discovered.
 * 
 * @property isValid Whether the file passed validation
 * @property errors List of validation errors found
 * @property warnings List of validation warnings found
 * @property fileSize Size of the validated file in bytes
 * @property estimatedRecords Estimated number of records in the file (if determinable)
 * @property validationTime Time taken to perform validation
 * @property metadata Additional validation metadata
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val fileSize: Long = 0,
    val estimatedRecords: Long? = null,
    val validationTime: Long = 0, // milliseconds
    val metadata: Map<String, Any> = emptyMap()
) {
    init {
        require(fileSize >= 0) { "File size cannot be negative" }
        estimatedRecords?.let { require(it >= 0) { "Estimated records cannot be negative" } }
        require(validationTime >= 0) { "Validation time cannot be negative" }
        
        // If validation failed, there should be at least one error
        if (!isValid) {
            require(errors.isNotEmpty()) { "Invalid files must have at least one error" }
        }
    }
    
    /**
     * Creates a successful validation result.
     * 
     * @param fileSize Size of the file in bytes
     * @param estimatedRecords Estimated number of records (optional)
     * @param validationTime Time taken for validation in milliseconds
     * @param warnings Any warnings found during validation
     * @param metadata Additional metadata
     * @return ValidationResult indicating success
     */
    companion object {
        fun success(
            fileSize: Long,
            estimatedRecords: Long? = null,
            validationTime: Long = 0,
            warnings: List<String> = emptyList(),
            metadata: Map<String, Any> = emptyMap()
        ): ValidationResult {
            return ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = warnings,
                fileSize = fileSize,
                estimatedRecords = estimatedRecords,
                validationTime = validationTime,
                metadata = metadata
            )
        }
        
        /**
         * Creates a failed validation result.
         * 
         * @param errors List of validation errors
         * @param fileSize Size of the file in bytes
         * @param validationTime Time taken for validation in milliseconds
         * @param warnings Any warnings found during validation
         * @param metadata Additional metadata
         * @return ValidationResult indicating failure
         */
        fun failure(
            errors: List<String>,
            fileSize: Long = 0,
            validationTime: Long = 0,
            warnings: List<String> = emptyList(),
            metadata: Map<String, Any> = emptyMap()
        ): ValidationResult {
            require(errors.isNotEmpty()) { "Failure must have at least one error" }
            return ValidationResult(
                isValid = false,
                errors = errors,
                warnings = warnings,
                fileSize = fileSize,
                estimatedRecords = null,
                validationTime = validationTime,
                metadata = metadata
            )
        }
        
        /**
         * Creates a failed validation result with a single error.
         * 
         * @param error The validation error message
         * @param fileSize Size of the file in bytes
         * @param validationTime Time taken for validation in milliseconds
         * @return ValidationResult indicating failure
         */
        fun failure(
            error: String,
            fileSize: Long = 0,
            validationTime: Long = 0
        ): ValidationResult {
            return failure(listOf(error), fileSize, validationTime)
        }
    }
    
    /**
     * Gets the total number of issues (errors + warnings).
     * 
     * @return Total count of issues found
     */
    fun getTotalIssues(): Int = errors.size + warnings.size
    
    /**
     * Checks if validation found any issues (errors or warnings).
     * 
     * @return true if any issues were found
     */
    fun hasIssues(): Boolean = errors.isNotEmpty() || warnings.isNotEmpty()
    
    /**
     * Gets a summary string of the validation result.
     * 
     * @return Formatted summary
     */
    fun getSummary(): String {
        return if (isValid) {
            "VALID (${warnings.size} warnings, ${fileSize} bytes${estimatedRecords?.let { ", ~$it records" } ?: ""})"
        } else {
            "INVALID (${errors.size} errors, ${warnings.size} warnings)"
        }
    }
}