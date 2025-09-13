package com.tia.etl.parser.api.exceptions

/**
 * Exception thrown when file or data validation fails before or during processing.
 * 
 * This exception is used specifically for validation errors such as:
 * - Invalid file format
 * - Unsupported file type
 * - Corrupted file data
 * - Schema validation failures
 * - Missing required fields or headers
 * 
 * @param message The detail message explaining the validation failure
 * @param cause The underlying cause of this validation failure (optional)
 * @param parserId The identifier of the parser that performed the validation (optional)
 * @param fileName The name of the file that failed validation (optional)
 * @param validationErrors List of specific validation error details (optional)
 */
class ValidationException(
    message: String,
    cause: Throwable? = null,
    parserId: String? = null,
    val fileName: String? = null,
    val validationErrors: List<String> = emptyList()
) : ParserException(message, cause, parserId) {
    
    /**
     * Creates a validation exception with just a message.
     * 
     * @param message The detail message explaining the validation failure
     */
    constructor(message: String) : this(message, null, null, null, emptyList())
    
    /**
     * Creates a validation exception with a message and file name.
     * 
     * @param message The detail message explaining the validation failure
     * @param fileName The name of the file that failed validation
     */
    constructor(message: String, fileName: String) : this(message, null, null, fileName, emptyList())
    
    /**
     * Creates a validation exception with detailed validation errors.
     * 
     * @param message The detail message explaining the validation failure
     * @param fileName The name of the file that failed validation
     * @param validationErrors List of specific validation error details
     */
    constructor(
        message: String, 
        fileName: String, 
        validationErrors: List<String>
    ) : this(message, null, null, fileName, validationErrors)
}