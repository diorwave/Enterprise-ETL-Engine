package com.tia.etl.parser.api.models

/**
 * Metadata information about a data parser implementation.
 * 
 * This class contains essential information about a parser, including its identity,
 * capabilities, and target table structure. This metadata is used by the parser
 * orchestrator to load and manage parser instances.
 * 
 * @property parserId Unique identifier for the parser (e.g., "orange-asn1-parser")
 * @property name Human-readable name of the parser
 * @property version Version of the parser implementation (semantic versioning recommended)
 * @property description Brief description of what this parser does
 * @property supportedFormats List of file formats this parser can handle (e.g., ["asn1", "ber"])
 * @property targetTable The name of the ClickHouse table where data will be stored
 * @property schemaDefinition Complete schema definition for the target table
 * @property author Author or maintainer of the parser (optional)
 * @property maxFileSize Maximum file size this parser can handle in bytes (optional)
 * @property batchSize Preferred batch size for processing records (optional)
 * @property requiresValidation Whether files should be validated before processing
 */
data class ParserMetadata(
    val parserId: String,
    val name: String,
    val version: String,
    val description: String,
    val supportedFormats: List<String>,
    val targetTable: String,
    val schemaDefinition: TableSchema,
    val author: String? = null,
    val maxFileSize: Long? = null,
    val batchSize: Int? = null,
    val requiresValidation: Boolean = true
) {
    init {
        require(parserId.isNotBlank()) { "Parser ID cannot be blank" }
        require(name.isNotBlank()) { "Parser name cannot be blank" }
        require(version.isNotBlank()) { "Parser version cannot be blank" }
        require(description.isNotBlank()) { "Parser description cannot be blank" }
        require(supportedFormats.isNotEmpty()) { "Parser must support at least one format" }
        require(supportedFormats.all { it.isNotBlank() }) { "Supported formats cannot be blank" }
        require(targetTable.isNotBlank()) { "Target table cannot be blank" }
        require(parserId.matches(Regex("^[a-z0-9][a-z0-9-]*[a-z0-9]$"))) {
            "Parser ID must be lowercase alphanumeric with hyphens, starting and ending with alphanumeric"
        }
        require(version.matches(Regex("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$"))) {
            "Version must follow semantic versioning format (e.g., 1.0.0 or 1.0.0-alpha)"
        }
        maxFileSize?.let { size ->
            require(size > 0) { "Max file size must be positive" }
        }
        batchSize?.let { batch ->
            require(batch > 0) { "Batch size must be positive" }
        }
    }
    
    /**
     * Checks if this parser supports the given file format.
     * 
     * @param format The file format to check (case-insensitive)
     * @return true if the format is supported, false otherwise
     */
    fun supportsFormat(format: String): Boolean {
        return supportedFormats.any { it.equals(format, ignoreCase = true) }
    }
    
    /**
     * Returns a string representation suitable for logging and debugging.
     */
    override fun toString(): String {
        return "ParserMetadata(parserId='$parserId', name='$name', version='$version', " +
                "formats=${supportedFormats.joinToString()}, targetTable='$targetTable')"
    }
}