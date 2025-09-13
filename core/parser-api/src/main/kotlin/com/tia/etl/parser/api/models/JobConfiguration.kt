package com.tia.etl.parser.api.models

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * Configuration for a parsing job in the TIA ETL system.
 * 
 * This class contains all the necessary configuration parameters for running
 * a parsing job, including file handling, scheduling, and processing options.
 * 
 * @property id Unique identifier for this job configuration
 * @property name Human-readable name for the job
 * @property sourceDirectory Directory path to scan for files
 * @property filePattern Regular expression pattern to match files
 * @property scanInterval How often to scan for new files
 * @property parserId Identifier of the parser to use for processing files
 * @property afterProcessing Action to take after successful processing
 * @property isActive Whether this job configuration is currently active
 * @property maxRetries Maximum number of retry attempts for failed files
 * @property retryDelay Delay between retry attempts
 * @property createdAt When this configuration was created
 * @property updatedAt When this configuration was last updated
 * @property createdBy User who created this configuration
 * @property tags Optional tags for categorizing jobs
 * @property customSettings Additional parser-specific configuration parameters
 */
data class JobConfiguration(
    val id: UUID,
    val name: String,
    val sourceDirectory: String,
    val filePattern: String,
    val scanInterval: ScanInterval,
    val parserId: String,
    val afterProcessing: ProcessingAction,
    val isActive: Boolean,
    val maxRetries: Int = 3,
    val retryDelay: Duration = Duration.ofMinutes(5),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String,
    val tags: List<String> = emptyList(),
    val customSettings: Map<String, Any> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Job name cannot be blank" }
        require(sourceDirectory.isNotBlank()) { "Source directory cannot be blank" }
        require(filePattern.isNotBlank()) { "File pattern cannot be blank" }
        require(parserId.isNotBlank()) { "Parser ID cannot be blank" }
        require(maxRetries >= 0) { "Max retries cannot be negative" }
        require(!retryDelay.isNegative) { "Retry delay cannot be negative" }
        require(createdBy.isNotBlank()) { "Created by cannot be blank" }
        
        // Validate regex pattern
        try {
            Regex(filePattern)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid file pattern regex: $filePattern", e)
        }
    }
    
    /**
     * Creates a copy of this configuration with updated timestamp.
     * 
     * @param updatedBy User making the update
     * @return New JobConfiguration with current timestamp
     */
    fun withUpdate(updatedBy: String): JobConfiguration {
        return copy(
            updatedAt = LocalDateTime.now(),
            createdBy = updatedBy // This represents the last updater
        )
    }
    
    /**
     * Checks if a filename matches this job's file pattern.
     * 
     * @param fileName The filename to check
     * @return true if the filename matches the pattern
     */
    fun matchesFilePattern(fileName: String): Boolean {
        return try {
            Regex(filePattern).matches(fileName)
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Defines how often to scan for new files.
 */
sealed class ScanInterval {
    /**
     * Scan at fixed intervals.
     * 
     * @property interval The duration between scans
     */
    data class Fixed(val interval: Duration) : ScanInterval() {
        init {
            require(!interval.isNegative && !interval.isZero) {
                "Fixed scan interval must be positive"
            }
        }
    }
    
    /**
     * Scan based on a cron expression.
     * 
     * @property expression Valid cron expression (e.g., "0 0/15 * * * *" for every 15 minutes)
     */
    data class Cron(val expression: String) : ScanInterval() {
        init {
            require(expression.isNotBlank()) { "Cron expression cannot be blank" }
            // Basic validation - should have 6 parts (second minute hour day month dayOfWeek)
            require(expression.split(" ").size == 6) {
                "Cron expression must have 6 parts: second minute hour day month dayOfWeek"
            }
        }
    }
    
    /**
     * Single execution - run once and stop.
     */
    object Once : ScanInterval()
}

/**
 * Defines what action to take after successfully processing a file.
 */
enum class ProcessingAction {
    /** Leave the file in the original location */
    KEEP,
    
    /** Move the file to an archive directory */
    ARCHIVE,
    
    /** Delete the file after successful processing */
    DELETE,
    
    /** Move the file to a processed directory */
    MOVE_TO_PROCESSED
}