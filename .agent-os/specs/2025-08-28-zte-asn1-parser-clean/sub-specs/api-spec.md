# API Specification

This is the API specification for the spec detailed in @.agent-os/specs/2025-08-28-zte-asn1-parser-clean/spec.md

> Created: 2025-08-28
> Version: 1.0.0

## Endpoints

### Parser Plugin Interface

The ZTE ASN.1 parser implements the standard DataParser interface for integration with the TIA ETL system.

#### DataParser Interface
```kotlin
interface DataParser {
    /**
     * Returns metadata about this parser implementation
     */
    fun getMetadata(): ParserMetadata
    
    /**
     * Process a file and load results to ClickHouse
     */
    fun process(context: ProcessingContext): ProcessingResult
}
```

#### Parser Metadata Endpoint
```kotlin
/**
 * Parser identification and capabilities
 */
data class ParserMetadata(
    val parserId: String = "zte-asn1-parser-clean",
    val version: String = "1.0.0",
    val name: String = "ZTE ASN.1 CDR Parser (Clean Code)",
    val description: String = "High-quality ZTE ASN.1 CDR parser following Clean Code and TDD principles",
    val supportedFormats: List<String> = listOf("asn1", "ber", "der"),
    val supportedExtensions: List<String> = listOf(".asn1", ".ber", ".der", ".cdr"),
    val vendor: String = "ZTE",
    val capabilities: ParserCapabilities = ParserCapabilities(
        batchProcessing = true,
        streamProcessing = true,
        validationSupport = true,
        errorRecovery = true,
        compressionSupport = false
    )
)
```

#### Processing Context Interface
```kotlin
/**
 * Processing context provided by the orchestrator
 */
data class ProcessingContext(
    val file: File,
    val clickHouseConnection: Connection,
    val postgresConnection: Connection,
    val jobConfig: JobConfiguration,
    val logger: Logger,
    val metrics: MetricsCollector? = null,
    val processingId: UUID = UUID.randomUUID()
)
```

#### Processing Result Interface
```kotlin
/**
 * Result of file processing operation
 */
data class ProcessingResult(
    val success: Boolean,
    val recordsProcessed: Int,
    val recordsInserted: Int,
    val recordsSkipped: Int,
    val recordsWithErrors: Int,
    val processingDurationMs: Long,
    val errorDetails: List<ProcessingError> = emptyList(),
    val metrics: ProcessingMetrics? = null
)

data class ProcessingError(
    val recordIndex: Int?,
    val errorType: ErrorType,
    val message: String,
    val cause: Throwable? = null,
    val context: Map<String, Any> = emptyMap()
)

enum class ErrorType {
    ASN1_PARSING_ERROR,
    DATA_VALIDATION_ERROR,
    DATABASE_ERROR,
    IO_ERROR,
    CONFIGURATION_ERROR
}
```

## Controllers

### Internal Parser APIs

The parser exposes internal APIs for testing, monitoring, and debugging purposes.

#### Core Parser Controller
```kotlin
@RestController
@RequestMapping("/api/v1/parser/zte-asn1")
class ZteAsn1ParserController(
    private val parser: ZteAsn1Parser,
    private val validator: DataValidator,
    private val metricsCollector: MetricsCollector
) {
    
    /**
     * Get parser metadata and capabilities
     */
    @GetMapping("/metadata")
    fun getMetadata(): ResponseEntity<ParserMetadata> {
        return ResponseEntity.ok(parser.getMetadata())
    }
    
    /**
     * Validate ASN.1 record without processing
     */
    @PostMapping("/validate")
    fun validateRecord(
        @RequestBody request: ValidationRequest
    ): ResponseEntity<ValidationResponse> {
        val result = validator.validateAsn1Data(request.asn1Data)
        return ResponseEntity.ok(ValidationResponse.from(result))
    }
    
    /**
     * Parse single ASN.1 record for testing
     */
    @PostMapping("/parse")
    fun parseRecord(
        @RequestBody request: ParseRequest
    ): ResponseEntity<ParseResponse> {
        val result = parser.parseRecord(request.asn1Data)
        return ResponseEntity.ok(ParseResponse.from(result))
    }
    
    /**
     * Get current processing statistics
     */
    @GetMapping("/stats")
    fun getStatistics(): ResponseEntity<ParserStatistics> {
        val stats = metricsCollector.getParserStatistics("zte-asn1-parser-clean")
        return ResponseEntity.ok(stats)
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<HealthStatus> {
        val health = parser.checkHealth()
        return if (health.isHealthy) {
            ResponseEntity.ok(health)
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health)
        }
    }
}
```

#### Request/Response DTOs

```kotlin
// Validation Request/Response
data class ValidationRequest(
    val asn1Data: ByteArray,
    val validationLevel: ValidationLevel = ValidationLevel.FULL
)

data class ValidationResponse(
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>,
    val validatedFields: Map<String, Any>
) {
    companion object {
        fun from(result: ValidationResult): ValidationResponse {
            return ValidationResponse(
                isValid = result.isValid,
                errors = result.errors.map { ValidationError.from(it) },
                warnings = result.warnings.map { ValidationWarning.from(it) },
                validatedFields = result.validatedFields
            )
        }
    }
}

// Parse Request/Response
data class ParseRequest(
    val asn1Data: ByteArray,
    val includeRawFields: Boolean = false
)

data class ParseResponse(
    val success: Boolean,
    val parsedRecord: ZteCdrRecordDto?,
    val parsingErrors: List<ParsingError>,
    val parsingDurationMs: Long
) {
    companion object {
        fun from(result: ParseResult): ParseResponse {
            return ParseResponse(
                success = result.isSuccess,
                parsedRecord = result.record?.toDto(),
                parsingErrors = result.errors.map { ParsingError.from(it) },
                parsingDurationMs = result.processingTime.toMillis()
            )
        }
    }
}

// Statistics Response
data class ParserStatistics(
    val totalRecordsProcessed: Long,
    val totalRecordsInserted: Long,
    val totalRecordsWithErrors: Long,
    val averageProcessingTimeMs: Double,
    val peakProcessingTimeMs: Long,
    val errorRatePercent: Double,
    val lastProcessedFile: String?,
    val lastProcessingTime: Instant?,
    val uptime: Duration
)

// Health Status Response
data class HealthStatus(
    val isHealthy: Boolean,
    val status: String,
    val checks: Map<String, HealthCheck>,
    val timestamp: Instant = Instant.now()
)

data class HealthCheck(
    val status: HealthCheckStatus,
    val message: String?,
    val responseTimeMs: Long? = null,
    val details: Map<String, Any> = emptyMap()
)

enum class HealthCheckStatus {
    UP, DOWN, DEGRADED, UNKNOWN
}
```

#### Configuration Controller
```kotlin
@RestController
@RequestMapping("/api/v1/parser/zte-asn1/config")
class ZteAsn1ConfigController(
    private val configManager: ParserConfigManager
) {
    
    /**
     * Get current parser configuration
     */
    @GetMapping
    fun getConfiguration(): ResponseEntity<ZteParserConfiguration> {
        val config = configManager.getCurrentConfiguration()
        return ResponseEntity.ok(config)
    }
    
    /**
     * Update parser configuration
     */
    @PutMapping
    fun updateConfiguration(
        @RequestBody @Valid config: ZteParserConfiguration
    ): ResponseEntity<ConfigurationUpdateResponse> {
        val result = configManager.updateConfiguration(config)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Get field mapping configuration
     */
    @GetMapping("/field-mappings")
    fun getFieldMappings(): ResponseEntity<List<FieldMapping>> {
        val mappings = configManager.getFieldMappings()
        return ResponseEntity.ok(mappings)
    }
    
    /**
     * Update field mapping configuration
     */
    @PutMapping("/field-mappings")
    fun updateFieldMappings(
        @RequestBody @Valid mappings: List<FieldMapping>
    ): ResponseEntity<FieldMappingUpdateResponse> {
        val result = configManager.updateFieldMappings(mappings)
        return ResponseEntity.ok(result)
    }
}
```

#### Configuration DTOs
```kotlin
data class ZteParserConfiguration(
    val batchSize: Int = 10000,
    val maxMemoryUsageMb: Int = 512,
    val validationEnabled: Boolean = true,
    val strictValidation: Boolean = false,
    val errorThresholdPercent: Double = 5.0,
    val retryAttempts: Int = 3,
    val timeoutSeconds: Int = 300,
    val compressionEnabled: Boolean = true,
    val debugLogging: Boolean = false
)

data class FieldMapping(
    val asn1Tag: String,
    val asn1Type: String,
    val clickHouseColumn: String,
    val clickHouseType: String,
    val required: Boolean,
    val defaultValue: Any?,
    val validationRules: List<ValidationRule>
)

data class ValidationRule(
    val type: ValidationType,
    val parameters: Map<String, Any>
)

enum class ValidationType {
    LENGTH, PATTERN, RANGE, ENUM, CUSTOM
}
```

### Error Handling

#### Global Exception Handler
```kotlin
@RestControllerAdvice
class ParserExceptionHandler {
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "VALIDATION_ERROR",
            message = ex.message ?: "Validation failed",
            timestamp = Instant.now(),
            details = ex.validationErrors
        )
        return ResponseEntity.badRequest().body(error)
    }
    
    @ExceptionHandler(ParsingException::class)
    fun handleParsingException(ex: ParsingException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "PARSING_ERROR",
            message = ex.message ?: "ASN.1 parsing failed",
            timestamp = Instant.now(),
            details = mapOf("recordIndex" to ex.recordIndex)
        )
        return ResponseEntity.badRequest().body(error)
    }
    
    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(ex: DatabaseException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "DATABASE_ERROR",
            message = "Database operation failed",
            timestamp = Instant.now(),
            details = mapOf("operation" to ex.operation)
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Instant,
    val details: Map<String, Any> = emptyMap()
)
```

### OpenAPI Documentation

#### API Documentation Configuration
```kotlin
@OpenAPIDefinition(
    info = Info(
        title = "ZTE ASN.1 CDR Parser API",
        version = "1.0.0",
        description = "Clean Code implementation of ZTE ASN.1 CDR parser with comprehensive testing",
        contact = Contact(
            name = "TIA ETL Team",
            email = "tia-etl@quantumsoft.dev"
        )
    ),
    servers = [
        Server(url = "http://localhost:8080", description = "Development server"),
        Server(url = "https://api.tia-etl.gov.lr", description = "Production server")
    ]
)
@Configuration
class OpenApiConfig
```

#### Endpoint Documentation Examples
```kotlin
@Operation(
    summary = "Parse ASN.1 record",
    description = "Parse a single ASN.1 encoded CDR record and return structured data",
    responses = [
        ApiResponse(
            responseCode = "200",
            description = "Record parsed successfully",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ParseResponse::class)
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid ASN.1 data or parsing error",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class)
            )]
        )
    ]
)
@PostMapping("/parse")
fun parseRecord(@RequestBody request: ParseRequest): ResponseEntity<ParseResponse>
```