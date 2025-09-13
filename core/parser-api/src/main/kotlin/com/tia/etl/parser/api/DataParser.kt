package com.tia.etl.parser.api

import com.tia.etl.parser.api.exceptions.ParserException
import com.tia.etl.parser.api.models.ParserMetadata
import com.tia.etl.parser.api.models.ProcessingContext
import com.tia.etl.parser.api.models.ProcessingResult

/**
 * Main interface for all data parsers in the TIA ETL system.
 * 
 * This interface defines the core contract that all parser implementations must follow.
 * Parsers are responsible for reading files, extracting data, and inserting it into
 * the target database using the provided connections and configuration.
 * 
 * ## Thread Safety
 * 
 * Implementations of this interface must be thread-safe as multiple instances
 * may be used concurrently to process different files. However, each instance
 * will only process one file at a time.
 * 
 * ## Error Handling
 * 
 * Implementations should:
 * - Never throw unchecked exceptions during processing
 * - Use the provided logger for all logging operations
 * - Report errors through the ProcessingResult rather than throwing exceptions
 * - Only throw ParserException for critical initialization or configuration errors
 * 
 * ## Resource Management
 * 
 * Parsers should not close the provided database connections as they are managed
 * by the parser orchestrator. Temporary resources created during processing should
 * be properly cleaned up before returning.
 * 
 * ## Transaction Handling
 * 
 * Parsers are responsible for managing database transactions appropriately:
 * - Use transactions for batch inserts to ClickHouse
 * - Handle rollback on errors
 * - Update processing status in PostgreSQL
 * 
 * @see ParserLifecycle for additional lifecycle management capabilities
 * @see ProcessingContext for available resources and configuration
 * @see ProcessingResult for result reporting requirements
 */
interface DataParser {
    
    /**
     * Gets metadata information about this parser.
     * 
     * This method should return static metadata about the parser's capabilities,
     * target table schema, supported formats, and other identifying information.
     * The metadata should not change during the lifetime of the parser instance.
     * 
     * This method must be thread-safe and should execute quickly as it may be
     * called frequently for parser discovery and routing decisions.
     * 
     * @return ParserMetadata containing complete information about this parser
     * @throws ParserException if metadata cannot be determined or is invalid
     */
    @Throws(ParserException::class)
    fun getMetadata(): ParserMetadata
    
    /**
     * Processes a file using the provided context and resources.
     * 
     * This is the main processing method where the parser should:
     * 1. Read and parse the input file
     * 2. Validate the data according to the target schema
     * 3. Insert valid records into ClickHouse using the provided connection
     * 4. Update processing status in PostgreSQL
     * 5. Handle errors gracefully and report them in the result
     * 6. Clean up any temporary resources
     * 
     * The method should use the provided logger for all logging operations and
     * include the processing ID from the context for correlation.
     * 
     * Database connections are managed externally and should not be closed by
     * the parser. The parser should handle database transactions appropriately
     * and ensure data consistency.
     * 
     * ## Performance Considerations
     * 
     * - Process records in batches rather than one by one
     * - Monitor memory usage and respect the maxMemoryMB limit from context
     * - Use prepared statements for database operations when possible
     * - Provide progress updates through logging for long-running operations
     * 
     * ## Error Recovery
     * 
     * - Continue processing remaining records when individual records fail
     * - Collect detailed error information for debugging
     * - Ensure partial data is handled correctly (transaction rollback if needed)
     * - Don't throw exceptions unless processing cannot continue at all
     * 
     * @param context Processing context containing file, connections, configuration, and logging
     * @return ProcessingResult with detailed statistics, timing, and error information
     * @throws ParserException only for critical errors that prevent any processing
     *         (e.g., corrupted file that cannot be read, database connection failures)
     */
    @Throws(ParserException::class)
    fun process(context: ProcessingContext): ProcessingResult
}