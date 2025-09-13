package com.tia.etl.parser.api.models

import org.slf4j.Logger
import java.io.File
import java.sql.Connection
import java.time.LocalDateTime
import java.util.UUID

/**
 * Processing context provided to parsers during file processing.
 * 
 * This class contains all the resources and configuration needed for a parser
 * to process a file, including database connections, job configuration, and
 * logging facilities. All resources are managed by the parser orchestrator.
 * 
 * @property file The file to be processed
 * @property clickHouseConnection Active connection to ClickHouse database for data insertion
 * @property postgresConnection Active connection to PostgreSQL database for metadata/audit
 * @property jobConfig Configuration for the current parsing job
 * @property logger Logger instance for the parser to use
 * @property processingId Unique identifier for this processing attempt
 * @property startTime When processing started for this file
 * @property maxMemoryMB Maximum memory the parser should use in megabytes
 * @property tempDirectory Directory for temporary files during processing
 * @property metadata Additional metadata about the processing context
 */
data class ProcessingContext(
    val file: File,
    val clickHouseConnection: Connection,
    val postgresConnection: Connection,
    val jobConfig: JobConfiguration,
    val logger: Logger,
    val processingId: UUID = UUID.randomUUID(),
    val startTime: LocalDateTime = LocalDateTime.now(),
    val maxMemoryMB: Long = 1024, // Default 1GB
    val tempDirectory: File? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    init {
        require(file.exists()) { "File must exist: ${file.absolutePath}" }
        require(file.canRead()) { "File must be readable: ${file.absolutePath}" }
        require(!clickHouseConnection.isClosed) { "ClickHouse connection must be open" }
        require(!postgresConnection.isClosed) { "PostgreSQL connection must be open" }
        require(maxMemoryMB > 0) { "Max memory must be positive" }
        
        tempDirectory?.let { tempDir ->
            require(tempDir.exists() && tempDir.isDirectory) {
                "Temp directory must exist and be a directory: ${tempDir.absolutePath}"
            }
            require(tempDir.canWrite()) {
                "Temp directory must be writable: ${tempDir.absolutePath}"
            }
        }
    }
    
    /**
     * Gets the file size in bytes.
     * 
     * @return File size in bytes
     */
    fun getFileSize(): Long = file.length()
    
    /**
     * Gets the file extension (without the dot).
     * 
     * @return File extension or empty string if no extension
     */
    fun getFileExtension(): String {
        val name = file.name
        val lastDotIndex = name.lastIndexOf('.')
        return if (lastDotIndex != -1 && lastDotIndex < name.length - 1) {
            name.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }
    
    /**
     * Gets a temporary file in the temp directory with the given name.
     * 
     * @param fileName Name for the temporary file
     * @return File object pointing to the temporary file location
     * @throws IllegalStateException if no temp directory is configured
     */
    fun getTempFile(fileName: String): File {
        val tempDir = tempDirectory ?: throw IllegalStateException("No temp directory configured")
        require(fileName.isNotBlank()) { "Temp file name cannot be blank" }
        return File(tempDir, fileName)
    }
    
    /**
     * Logs a message with the processing ID for context.
     * 
     * @param level Log level
     * @param message Message to log
     * @param throwable Optional throwable to log
     */
    fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        val contextMessage = "[$processingId] ${file.name}: $message"
        when (level) {
            LogLevel.TRACE -> logger.trace(contextMessage, throwable)
            LogLevel.DEBUG -> logger.debug(contextMessage, throwable)
            LogLevel.INFO -> logger.info(contextMessage, throwable)
            LogLevel.WARN -> logger.warn(contextMessage, throwable)
            LogLevel.ERROR -> logger.error(contextMessage, throwable)
        }
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
 * Log levels for context logging.
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}