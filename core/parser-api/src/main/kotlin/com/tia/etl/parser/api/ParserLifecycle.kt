package com.tia.etl.parser.api

import com.tia.etl.parser.api.exceptions.ParserException
import com.tia.etl.parser.api.exceptions.ValidationException
import com.tia.etl.parser.api.models.ValidationResult
import java.io.File

/**
 * Interface for managing parser lifecycle operations.
 * 
 * This interface provides additional capabilities for parser initialization,
 * file validation, and cleanup operations. Parsers that implement this interface
 * gain better integration with the parser orchestrator and can perform
 * more sophisticated validation and setup operations.
 * 
 * ## Implementation Guidelines
 * 
 * This interface is optional but recommended for production parsers. It enables:
 * - Custom initialization logic with configuration parameters
 * - Pre-processing file validation to fail fast on invalid files
 * - Proper resource cleanup when parsers are unloaded
 * - Better error reporting and diagnostics
 * 
 * ## Thread Safety
 * 
 * - `initialize()` is called once during parser loading and must be thread-safe
 * - `validate()` may be called concurrently for different files
 * - `shutdown()` is called once during parser unloading
 * 
 * ## Lifecycle Order
 * 
 * 1. Parser instance is created
 * 2. `initialize()` is called with configuration
 * 3. `validate()` may be called for files before processing
 * 4. `DataParser.process()` is called for validated files
 * 5. `shutdown()` is called before parser is unloaded
 * 
 * @see DataParser for the main processing interface
 */
interface ParserLifecycle {
    
    /**
     * Initializes the parser with configuration parameters.
     * 
     * This method is called once when the parser is first loaded by the orchestrator.
     * It provides an opportunity to:
     * - Validate and store configuration parameters
     * - Initialize internal resources (connection pools, caches, etc.)
     * - Perform any setup required before processing files
     * - Validate parser dependencies and requirements
     * 
     * The configuration map contains parser-specific settings from the job
     * configuration's `customSettings` field, plus any system-level parser
     * configuration.
     * 
     * Common configuration parameters might include:
     * - `batchSize`: Number of records to process in each batch
     * - `maxMemoryMB`: Memory limit for processing operations
     * - `tempDirectory`: Directory for temporary files
     * - `encoding`: Character encoding for text files
     * - `dateFormat`: Date format for parsing timestamps
     * - `skipHeaders`: Whether to skip header rows in CSV files
     * 
     * @param config Configuration parameters for this parser instance
     * @throws ParserException if initialization fails due to invalid configuration
     *         or missing dependencies
     */
    @Throws(ParserException::class)
    fun initialize(config: Map<String, Any>)
    
    /**
     * Validates a file before processing to ensure it can be handled correctly.
     * 
     * This method provides an opportunity to check file validity before attempting
     * to process it, enabling fast failure for obviously invalid files. Validation
     * should be relatively quick and should not perform full file parsing.
     * 
     * Typical validation checks include:
     * - File format validation (magic bytes, headers, structure)
     * - File size checks (too large or too small)
     * - Basic content validation (encoding, required fields)
     * - File accessibility and permissions
     * - Schema compatibility checks
     * 
     * The validation result should include:
     * - Whether the file is valid for processing
     * - Specific errors that would prevent processing
     * - Warnings about potential issues that won't prevent processing
     * - Estimated record count if determinable
     * - File size and validation timing information
     * 
     * ## Performance Considerations
     * 
     * - Validation should be fast (typically < 1 second for most files)
     * - Avoid reading entire files during validation
     * - Use file headers, metadata, or sampling for validation
     * - Cache validation results if files don't change frequently
     * 
     * ## Error Handling
     * 
     * - Return validation failures rather than throwing exceptions
     * - Only throw ValidationException for critical validation errors
     * - Include detailed error messages for debugging
     * - Distinguish between recoverable and non-recoverable issues
     * 
     * @param file The file to validate for processing
     * @return ValidationResult indicating whether the file is valid and any issues found
     * @throws ValidationException only for critical validation errors that prevent
     *         any validation from occurring (e.g., file not found, permission denied)
     */
    @Throws(ValidationException::class)
    fun validate(file: File): ValidationResult
    
    /**
     * Shuts down the parser and releases any resources.
     * 
     * This method is called when the parser is being unloaded by the orchestrator.
     * It provides an opportunity to:
     * - Close any open resources (files, connections, etc.)
     * - Clean up temporary files or caches
     * - Persist any important state information
     * - Log shutdown statistics or diagnostics
     * 
     * The shutdown process should be:
     * - **Graceful**: Allow current operations to complete if possible
     * - **Fast**: Complete within a reasonable time (typically < 30 seconds)
     * - **Safe**: Handle errors during shutdown gracefully
     * - **Complete**: Release all allocated resources
     * 
     * ## Implementation Notes
     * 
     * - This method should not throw exceptions
     * - Log any errors during shutdown but continue cleanup
     * - Use try-catch blocks around individual cleanup operations
     * - Consider using a timeout for cleanup operations
     * - Make this method idempotent (safe to call multiple times)
     * 
     * ## Resource Cleanup Examples
     * 
     * ```kotlin
     * override fun shutdown() {
     *     try {
     *         // Close connection pools
     *         connectionPool?.close()
     *     } catch (e: Exception) {
     *         logger.warn("Error closing connection pool", e)
     *     }
     *     
     *     try {
     *         // Clean up temporary files
     *         tempFiles.forEach { it.deleteRecursively() }
     *     } catch (e: Exception) {
     *         logger.warn("Error cleaning up temp files", e)
     *     }
     *     
     *     // Reset state
     *     initialized = false
     * }
     * ```
     */
    fun shutdown()
}