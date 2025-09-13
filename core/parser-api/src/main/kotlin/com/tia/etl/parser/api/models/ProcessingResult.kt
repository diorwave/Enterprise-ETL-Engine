package com.tia.etl.parser.api.models

import java.time.Duration
import java.time.LocalDateTime

/**
 * Result of processing a file through a data parser.
 * 
 * This class contains detailed information about the outcome of processing
 * a file, including statistics, timing information, and any errors encountered.
 * 
 * @property totalRecords Total number of records found in the file
 * @property successfulRecords Number of records successfully processed and inserted
 * @property failedRecords Number of records that failed processing
 * @property skippedRecords Number of records that were skipped (e.g., duplicates)
 * @property processingTime Total time taken to process the file
 * @property errors List of errors encountered during processing
 * @property warnings List of warnings generated during processing
 * @property metadata Additional metadata about the processing result
 * @property bytesProcessed Number of bytes read from the file
 * @property peakMemoryUsageMB Peak memory usage during processing in megabytes
 * @property databaseInsertTime Time spent inserting data into the database
 * @property validationTime Time spent validating data
 * @property completedAt When processing completed
 */
data class ProcessingResult(
    val totalRecords: Long,
    val successfulRecords: Long,
    val failedRecords: Long,
    val skippedRecords: Long = 0,
    val processingTime: Duration,
    val errors: List<ProcessingError> = emptyList(),
    val warnings: List<ProcessingWarning> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    val bytesProcessed: Long = 0,
    val peakMemoryUsageMB: Long = 0,
    val databaseInsertTime: Duration = Duration.ZERO,
    val validationTime: Duration = Duration.ZERO,
    val completedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(totalRecords >= 0) { "Total records cannot be negative" }
        require(successfulRecords >= 0) { "Successful records cannot be negative" }
        require(failedRecords >= 0) { "Failed records cannot be negative" }
        require(skippedRecords >= 0) { "Skipped records cannot be negative" }
        require(successfulRecords + failedRecords + skippedRecords == totalRecords) {
            "Sum of successful, failed, and skipped records must equal total records"
        }
        require(!processingTime.isNegative) { "Processing time cannot be negative" }
        require(bytesProcessed >= 0) { "Bytes processed cannot be negative" }
        require(peakMemoryUsageMB >= 0) { "Peak memory usage cannot be negative" }
        require(!databaseInsertTime.isNegative) { "Database insert time cannot be negative" }
        require(!validationTime.isNegative) { "Validation time cannot be negative" }
    }
    
    /**
     * Calculates the success rate as a percentage.
     * 
     * @return Success rate between 0.0 and 1.0, or 1.0 if no records were processed
     */
    fun getSuccessRate(): Double {
        return if (totalRecords == 0L) 1.0 else successfulRecords.toDouble() / totalRecords
    }
    
    /**
     * Calculates the error rate as a percentage.
     * 
     * @return Error rate between 0.0 and 1.0, or 0.0 if no records were processed
     */
    fun getErrorRate(): Double {
        return if (totalRecords == 0L) 0.0 else failedRecords.toDouble() / totalRecords
    }
    
    /**
     * Calculates records processed per second.
     * 
     * @return Records per second, or 0.0 if processing time is zero
     */
    fun getRecordsPerSecond(): Double {
        val seconds = processingTime.toMillis() / 1000.0
        return if (seconds == 0.0) 0.0 else totalRecords / seconds
    }
    
    /**
     * Calculates megabytes processed per second.
     * 
     * @return MB/sec processing rate, or 0.0 if processing time is zero
     */
    fun getMegabytesPerSecond(): Double {
        val seconds = processingTime.toMillis() / 1000.0
        val megabytes = bytesProcessed / (1024.0 * 1024.0)
        return if (seconds == 0.0) 0.0 else megabytes / seconds
    }
    
    /**
     * Checks if processing was completely successful (no failures or errors).
     * 
     * @return true if no records failed and no errors occurred
     */
    fun isCompletelySuccessful(): Boolean {
        return failedRecords == 0L && errors.isEmpty()
    }
    
    /**
     * Checks if processing had any failures.
     * 
     * @return true if any records failed or errors occurred
     */
    fun hasFailures(): Boolean {
        return failedRecords > 0L || errors.isNotEmpty()
    }
    
    /**
     * Gets a summary string suitable for logging.
     * 
     * @return Formatted summary of processing results
     */
    fun getSummary(): String {
        return "ProcessingResult(total=$totalRecords, successful=$successfulRecords, " +
                "failed=$failedRecords, skipped=$skippedRecords, " +
                "time=${processingTime.toMillis()}ms, errors=${errors.size})"
    }
    
    /**
     * Gets metadata value by key with type casting.
     * 
     * @param key The metadata key
     * @param defaultValue Default value if key not found
     * @return The metadata value cast to the expected type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getMetadata(key: String, defaultValue: T): T {
        return metadata[key] as? T ?: defaultValue
    }
}

/**
 * Represents an error that occurred during processing.
 * 
 * @property message Description of the error
 * @property recordNumber Record number where the error occurred (if applicable)
 * @property lineNumber Line number in the file where the error occurred (if applicable)
 * @property errorCode Optional error code for categorization
 * @property severity Severity level of the error
 * @property exception The underlying exception that caused this error (if any)
 * @property context Additional context about where/why the error occurred
 */
data class ProcessingError(
    val message: String,
    val recordNumber: Long? = null,
    val lineNumber: Long? = null,
    val errorCode: String? = null,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val exception: Throwable? = null,
    val context: Map<String, Any> = emptyMap()
) {
    init {
        require(message.isNotBlank()) { "Error message cannot be blank" }
        recordNumber?.let { require(it > 0) { "Record number must be positive" } }
        lineNumber?.let { require(it > 0) { "Line number must be positive" } }
    }
    
    /**
     * Returns a formatted string representation of this error.
     */
    override fun toString(): String {
        val location = when {
            recordNumber != null && lineNumber != null -> " (record $recordNumber, line $lineNumber)"
            recordNumber != null -> " (record $recordNumber)"
            lineNumber != null -> " (line $lineNumber)"
            else -> ""
        }
        val code = errorCode?.let { " [$it]" } ?: ""
        return "$severity$code: $message$location"
    }
}

/**
 * Represents a warning that occurred during processing.
 * 
 * @property message Description of the warning
 * @property recordNumber Record number where the warning occurred (if applicable)
 * @property lineNumber Line number in the file where the warning occurred (if applicable)
 * @property warningCode Optional warning code for categorization
 * @property context Additional context about the warning
 */
data class ProcessingWarning(
    val message: String,
    val recordNumber: Long? = null,
    val lineNumber: Long? = null,
    val warningCode: String? = null,
    val context: Map<String, Any> = emptyMap()
) {
    init {
        require(message.isNotBlank()) { "Warning message cannot be blank" }
        recordNumber?.let { require(it > 0) { "Record number must be positive" } }
        lineNumber?.let { require(it > 0) { "Line number must be positive" } }
    }
    
    /**
     * Returns a formatted string representation of this warning.
     */
    override fun toString(): String {
        val location = when {
            recordNumber != null && lineNumber != null -> " (record $recordNumber, line $lineNumber)"
            recordNumber != null -> " (record $recordNumber)"
            lineNumber != null -> " (line $lineNumber)"
            else -> ""
        }
        val code = warningCode?.let { " [$it]" } ?: ""
        return "WARNING$code: $message$location"
    }
}

/**
 * Severity levels for processing errors.
 */
enum class ErrorSeverity {
    /** Critical error that stops processing */
    CRITICAL,
    
    /** Error that affects data quality but allows processing to continue */
    ERROR,
    
    /** Minor issue that doesn't significantly impact processing */
    WARNING,
    
    /** Informational message about processing decisions */
    INFO
}