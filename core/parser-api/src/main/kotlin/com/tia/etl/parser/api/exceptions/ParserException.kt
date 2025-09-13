package com.tia.etl.parser.api.exceptions

/**
 * Base exception for all parser-related errors in the TIA ETL system.
 * 
 * This exception should be used for any error that occurs during parser operations,
 * including initialization failures, processing errors, and validation issues.
 * 
 * @param message The detail message explaining the cause of the exception
 * @param cause The underlying cause of this exception (optional)
 * @param parserId The identifier of the parser that threw this exception (optional)
 */
open class ParserException(
    message: String,
    cause: Throwable? = null,
    val parserId: String? = null
) : Exception(message, cause) {
    
    /**
     * Creates a parser exception with just a message.
     * 
     * @param message The detail message explaining the cause of the exception
     */
    constructor(message: String) : this(message, null, null)
    
    /**
     * Creates a parser exception with a message and parser ID.
     * 
     * @param message The detail message explaining the cause of the exception
     * @param parserId The identifier of the parser that threw this exception
     */
    constructor(message: String, parserId: String) : this(message, null, parserId)
}