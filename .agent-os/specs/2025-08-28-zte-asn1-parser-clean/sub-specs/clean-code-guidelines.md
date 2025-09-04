# Clean Code Guidelines

This is the clean code implementation guidelines for the spec detailed in @.agent-os/specs/2025-08-28-zte-asn1-parser-clean/spec.md

> Created: 2025-08-28
> Version: 1.0.0

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)

#### Class Responsibility Definition
Each class should have one and only one reason to change. The ZTE ASN.1 parser implementation follows strict SRP:

```kotlin
// GOOD: Single responsibility - ASN.1 decoding only
class Asn1Decoder {
    fun decode(asn1Data: ByteArray): Asn1Structure {
        // Only handles ASN.1 BER/DER decoding logic
    }
}

// GOOD: Single responsibility - Data validation only
class ZteCdrValidator {
    fun validate(record: ZteCdrRecord): ValidationResult {
        // Only handles data validation logic
    }
}

// GOOD: Single responsibility - Database operations only
class ClickHouseWriter {
    fun insertBatch(records: List<ZteCdrRecord>): InsertResult {
        // Only handles ClickHouse insertion logic
    }
}

// BAD: Multiple responsibilities
class ZteParser {
    fun parseAndValidateAndSave(asn1Data: ByteArray) {
        // Violates SRP - parsing, validation, and saving in one class
    }
}
```

#### Function Responsibility
Functions should do one thing and do it well:

```kotlin
// GOOD: Single responsibility function
fun extractMsisdn(asn1Structure: Asn1Structure): String {
    return asn1Structure.getOctetString(MSISDN_TAG)
        .toString(StandardCharsets.UTF_8)
}

// GOOD: Single responsibility function  
fun validateMsisdnFormat(msisdn: String): ValidationResult {
    return if (msisdn.matches("^\\d{1,15}$".toRegex())) {
        ValidationResult.success()
    } else {
        ValidationResult.error("Invalid MSISDN format")
    }
}

// BAD: Multiple responsibilities
fun extractAndValidateMsisdn(asn1Structure: Asn1Structure): ValidationResult {
    val msisdn = asn1Structure.getOctetString(MSISDN_TAG).toString(StandardCharsets.UTF_8)
    return if (msisdn.matches("^\\d{1,15}$".toRegex())) {
        ValidationResult.success(msisdn)
    } else {
        ValidationResult.error("Invalid MSISDN format")
    }
}
```

### Open/Closed Principle (OCP)

#### Extensible Design Patterns
Classes should be open for extension but closed for modification:

```kotlin
// GOOD: Extensible validation system
interface ValidationRule<T> {
    fun validate(value: T): ValidationResult
    fun getFieldName(): String
}

class MsisdnLengthRule : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.length in 1..15) {
            ValidationResult.success()
        } else {
            ValidationResult.error("MSISDN length must be 1-15 digits")
        }
    }
    
    override fun getFieldName() = "msisdn"
}

class MsisdnFormatRule : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.matches("^\\d+$".toRegex())) {
            ValidationResult.success()
        } else {
            ValidationResult.error("MSISDN must contain only digits")
        }
    }
    
    override fun getFieldName() = "msisdn"
}

// Validator can accept new rules without modification
class FieldValidator(private val rules: List<ValidationRule<String>>) {
    fun validate(value: String): ValidationResult {
        return rules.fold(ValidationResult.success()) { acc, rule ->
            if (acc.isValid) rule.validate(value) else acc
        }
    }
}
```

#### Strategy Pattern for Extensibility
```kotlin
// GOOD: Strategy pattern for different field extraction strategies
interface FieldExtractor<T> {
    fun extract(asn1Structure: Asn1Structure): T
    fun getFieldName(): String
}

class MsisdnExtractor : FieldExtractor<String> {
    override fun extract(asn1Structure: Asn1Structure): String {
        return asn1Structure.getOctetString(MSISDN_TAG)
            .toString(StandardCharsets.UTF_8)
    }
    
    override fun getFieldName() = "msisdn"
}

class CallDurationExtractor : FieldExtractor<Int> {
    override fun extract(asn1Structure: Asn1Structure): Int {
        return asn1Structure.getInteger(CALL_DURATION_TAG).intValue()
    }
    
    override fun getFieldName() = "callDuration"
}

// Parser registry can be extended without modifying existing code
class FieldExtractorRegistry {
    private val extractors = mutableMapOf<String, FieldExtractor<*>>()
    
    fun <T> register(extractor: FieldExtractor<T>) {
        extractors[extractor.getFieldName()] = extractor
    }
    
    fun <T> getExtractor(fieldName: String): FieldExtractor<T>? {
        @Suppress("UNCHECKED_CAST")
        return extractors[fieldName] as? FieldExtractor<T>
    }
}
```

### Liskov Substitution Principle (LSP)

#### Interface Contract Compliance
Derived classes must be substitutable for their base classes:

```kotlin
// GOOD: All implementations honor the contract
interface DataParser {
    /**
     * Parse ASN.1 data and return structured record
     * @param asn1Data Valid ASN.1 BER/DER encoded data
     * @return ParseResult with success flag and parsed data or errors
     * @throws IllegalArgumentException if asn1Data is null or empty
     */
    fun parseRecord(asn1Data: ByteArray): ParseResult
}

class ZteAsn1Parser : DataParser {
    override fun parseRecord(asn1Data: ByteArray): ParseResult {
        require(asn1Data.isNotEmpty()) { "ASN.1 data cannot be empty" }
        
        return try {
            val decoded = asn1Decoder.decode(asn1Data)
            val record = extractFields(decoded)
            val validation = validator.validate(record)
            
            if (validation.isValid) {
                ParseResult.success(record)
            } else {
                ParseResult.failure(validation.errors)
            }
        } catch (ex: Exception) {
            ParseResult.failure(listOf(ParsingError.from(ex)))
        }
    }
}

class MockZteAsn1Parser : DataParser {
    override fun parseRecord(asn1Data: ByteArray): ParseResult {
        require(asn1Data.isNotEmpty()) { "ASN.1 data cannot be empty" }
        
        // Mock implementation still honors the contract
        return ParseResult.success(createMockRecord())
    }
}
```

#### Behavioral Consistency
```kotlin
// GOOD: Consistent behavior across implementations
abstract class BaseValidator {
    abstract fun validateField(fieldName: String, value: Any): ValidationResult
    
    fun validate(record: ZteCdrRecord): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Template method - consistent validation flow
        errors.addAll(validateRequiredFields(record))
        errors.addAll(validateFieldFormats(record))
        errors.addAll(validateBusinessRules(record))
        
        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }
    
    protected abstract fun validateRequiredFields(record: ZteCdrRecord): List<ValidationError>
    protected abstract fun validateFieldFormats(record: ZteCdrRecord): List<ValidationError>
    protected abstract fun validateBusinessRules(record: ZteCdrRecord): List<ValidationError>
}
```

### Interface Segregation Principle (ISP)

#### Focused Interfaces
Clients should not depend on interfaces they don't use:

```kotlin
// GOOD: Focused interfaces
interface Asn1Decoder {
    fun decode(asn1Data: ByteArray): Asn1Structure
}

interface DataValidator {
    fun validate(record: ZteCdrRecord): ValidationResult
    fun validateField(fieldName: String, value: Any): ValidationResult
}

interface DatabaseWriter {
    fun insertBatch(records: List<ZteCdrRecord>): InsertResult
    fun validateSchema(record: ZteCdrRecord): SchemaValidationResult
}

interface MetricsCollector {
    fun recordParsingTime(duration: Duration)
    fun recordValidationResult(result: ValidationResult)
    fun recordInsertionResult(result: InsertResult)
}

// BAD: Fat interface
interface ZteParserService {
    // Decoding methods
    fun decode(asn1Data: ByteArray): Asn1Structure
    
    // Validation methods
    fun validate(record: ZteCdrRecord): ValidationResult
    fun validateField(fieldName: String, value: Any): ValidationResult
    
    // Database methods
    fun insertBatch(records: List<ZteCdrRecord>): InsertResult
    fun validateSchema(record: ZteCdrRecord): SchemaValidationResult
    
    // Metrics methods
    fun recordParsingTime(duration: Duration)
    fun getStatistics(): ParserStatistics
    
    // Configuration methods
    fun updateConfiguration(config: ParserConfiguration)
    fun getConfiguration(): ParserConfiguration
}
```

#### Role-Based Interface Design
```kotlin
// GOOD: Interfaces based on client needs
interface Parseable {
    fun parseRecord(asn1Data: ByteArray): ParseResult
}

interface Validatable {
    fun validate(record: ZteCdrRecord): ValidationResult
}

interface Insertable {
    fun insertBatch(records: List<ZteCdrRecord>): InsertResult
}

interface Monitorable {
    fun getHealthStatus(): HealthStatus
    fun getMetrics(): Map<String, Any>
}

// Clients only depend on what they need
class ParsingService(private val parser: Parseable) {
    fun processFile(file: File): List<ParseResult> {
        return file.readAsn1Records().map { parser.parseRecord(it) }
    }
}

class ValidationService(private val validator: Validatable) {
    fun validateRecords(records: List<ZteCdrRecord>): List<ValidationResult> {
        return records.map { validator.validate(it) }
    }
}
```

### Dependency Inversion Principle (DIP)

#### Depend on Abstractions
High-level modules should not depend on low-level modules:

```kotlin
// GOOD: Depends on abstractions
class ZteAsn1ParserService(
    private val decoder: Asn1Decoder,
    private val validator: DataValidator,
    private val writer: DatabaseWriter,
    private val metrics: MetricsCollector
) {
    fun processFile(file: File): ProcessingResult {
        val results = mutableListOf<ParseResult>()
        val records = mutableListOf<ZteCdrRecord>()
        
        file.readAsn1Records().forEach { asn1Data ->
            measureTime { decoder.decode(asn1Data) }.let { (decoded, duration) ->
                metrics.recordParsingTime(duration)
                
                val record = extractFields(decoded)
                val validation = validator.validate(record)
                
                if (validation.isValid) {
                    records.add(record)
                } else {
                    results.add(ParseResult.failure(validation.errors))
                }
            }
        }
        
        val insertResult = writer.insertBatch(records)
        metrics.recordInsertionResult(insertResult)
        
        return ProcessingResult.from(results, insertResult)
    }
}

// BAD: Depends on concrete implementations
class ZteAsn1ParserService {
    private val decoder = BouncyCastleAsn1Decoder() // Concrete dependency
    private val validator = ZteCdrValidatorImpl()    // Concrete dependency
    private val writer = ClickHouseWriterImpl()     // Concrete dependency
    
    // Implementation tightly coupled to concrete classes
}
```

#### Dependency Injection Configuration
```kotlin
@Configuration
class ParserConfiguration {
    
    @Bean
    fun asn1Decoder(): Asn1Decoder {
        return BouncyCastleAsn1Decoder()
    }
    
    @Bean
    fun dataValidator(): DataValidator {
        return ZteCdrValidatorImpl()
    }
    
    @Bean
    fun databaseWriter(
        @Qualifier("clickHouseDataSource") dataSource: DataSource
    ): DatabaseWriter {
        return ClickHouseWriterImpl(dataSource)
    }
    
    @Bean
    fun zteAsn1Parser(
        decoder: Asn1Decoder,
        validator: DataValidator,
        writer: DatabaseWriter,
        metrics: MetricsCollector
    ): ZteAsn1ParserService {
        return ZteAsn1ParserService(decoder, validator, writer, metrics)
    }
}
```

## Clean Code Naming Conventions

### Class Naming
```kotlin
// GOOD: Clear, intention-revealing names
class ZteAsn1RecordParser
class MsisdnValidator
class ClickHouseBatchWriter
class ValidationResultBuilder
class Asn1DecodingException

// BAD: Vague, abbreviated names
class ZteParser // Too vague
class MsisdnVal // Abbreviated
class CHWriter  // Abbreviated
class VRB      // Meaningless abbreviation
class ADE      // Acronym without context
```

### Method Naming
```kotlin
// GOOD: Intention-revealing method names
fun extractMsisdnFromAsn1Structure(structure: Asn1Structure): String
fun validateCallDurationRange(duration: Int): ValidationResult
fun insertRecordsInBatches(records: List<ZteCdrRecord>): InsertResult
fun createClickHouseInsertStatement(tableName: String): PreparedStatement

// BAD: Unclear method names
fun getMsisdn(s: Asn1Structure): String // 's' is not descriptive
fun check(duration: Int): Boolean       // 'check' is too vague
fun insert(records: List<ZteCdrRecord>) // Missing return type info
fun stmt(table: String): PreparedStatement // Abbreviated
```

### Variable Naming
```kotlin
// GOOD: Descriptive variable names
val parsedMsisdn: String = extractMsisdn(asn1Structure)
val validationErrors: List<ValidationError> = mutableListOf()
val clickHouseConnection: Connection = dataSource.connection
val processingStartTime: Instant = Instant.now()
val batchInsertResults: List<InsertResult> = mutableListOf()

// BAD: Unclear variable names
val m: String = extractMsisdn(asn1Structure)  // Too short
val errors: List<ValidationError> = mutableListOf() // Too generic
val conn: Connection = dataSource.connection   // Abbreviated
val time: Instant = Instant.now()             // Too generic
val results: List<InsertResult> = mutableListOf() // Too generic
```

### Constant Naming
```kotlin
// GOOD: Descriptive constant names
object ZteAsn1Tags {
    const val MSISDN_TAG: Int = 0x04
    const val IMSI_TAG: Int = 0x05
    const val CALL_DURATION_TAG: Int = 0x10
    const val SIGNAL_STRENGTH_TAG: Int = 0x15
}

object ValidationConstraints {
    const val MAX_MSISDN_LENGTH: Int = 15
    const val MIN_MSISDN_LENGTH: Int = 1
    const val MAX_CALL_DURATION_SECONDS: Int = 86400
    const val MIN_SIGNAL_STRENGTH_DBM: Int = -128
    const val MAX_SIGNAL_STRENGTH_DBM: Int = 0
}

// BAD: Unclear constant names
object Tags {
    const val T1: Int = 0x04    // Meaningless name
    const val T2: Int = 0x05    // Meaningless name
}

object Limits {
    const val MAX_LEN: Int = 15  // Abbreviated
    const val MAX_DUR: Int = 86400 // Abbreviated
}
```

## Function Design Principles

### Small Functions
Functions should be small and do one thing:

```kotlin
// GOOD: Small, focused functions
fun extractMsisdn(asn1Structure: Asn1Structure): String {
    return asn1Structure.getOctetString(MSISDN_TAG)
        .toString(StandardCharsets.UTF_8)
}

fun validateMsisdnLength(msisdn: String): Boolean {
    return msisdn.length in MIN_MSISDN_LENGTH..MAX_MSISDN_LENGTH
}

fun validateMsisdnFormat(msisdn: String): Boolean {
    return msisdn.matches("^\\d+$".toRegex())
}

fun validateMsisdn(msisdn: String): ValidationResult {
    return when {
        !validateMsisdnLength(msisdn) -> 
            ValidationResult.error("MSISDN length must be $MIN_MSISDN_LENGTH-$MAX_MSISDN_LENGTH digits")
        !validateMsisdnFormat(msisdn) -> 
            ValidationResult.error("MSISDN must contain only digits")
        else -> 
            ValidationResult.success()
    }
}

// BAD: Large function doing multiple things
fun processAsn1Record(asn1Data: ByteArray): ProcessingResult {
    // 50+ lines of mixed parsing, validation, and database logic
    // Violates single responsibility and is hard to test/maintain
}
```

### Pure Functions When Possible
Prefer pure functions that don't have side effects:

```kotlin
// GOOD: Pure function
fun calculateCallDuration(startTime: Instant, endTime: Instant): Duration {
    return Duration.between(startTime, endTime)
}

fun formatMsisdnForDisplay(msisdn: String): String {
    return if (msisdn.startsWith("+")) {
        msisdn
    } else {
        "+231$msisdn"
    }
}

// GOOD: Pure function with clear inputs and outputs
fun validateRecordFields(record: ZteCdrRecord): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()
    
    if (record.msisdn.isEmpty()) {
        errors.add(ValidationError("msisdn", "MSISDN is required"))
    }
    
    if (record.callDuration < 0) {
        errors.add(ValidationError("callDuration", "Call duration cannot be negative"))
    }
    
    return errors
}

// AVOID: Functions with side effects when not necessary
fun validateAndLogRecord(record: ZteCdrRecord): ValidationResult {
    val result = validate(record)
    logger.info("Validation result: ${result.isValid}") // Side effect
    return result
}
```

### Function Parameters
Keep parameter lists small and use data classes for complex parameters:

```kotlin
// GOOD: Few, well-named parameters
fun createInsertStatement(
    connection: Connection,
    tableName: String,
    columnCount: Int
): PreparedStatement

// GOOD: Data class for complex parameters
data class ParsingContext(
    val asn1Data: ByteArray,
    val recordIndex: Int,
    val fileName: String,
    val validationLevel: ValidationLevel,
    val errorHandler: ErrorHandler
)

fun parseRecord(context: ParsingContext): ParseResult {
    // Implementation using context
}

// BAD: Too many parameters
fun parseRecord(
    asn1Data: ByteArray,
    recordIndex: Int,
    fileName: String,
    validationLevel: ValidationLevel,
    errorHandler: ErrorHandler,
    metricsCollector: MetricsCollector,
    configuration: ParserConfiguration,
    logger: Logger
): ParseResult
```

## Error Handling Principles

### Explicit Error Types
Use sealed classes or enums for explicit error handling:

```kotlin
// GOOD: Explicit error types
sealed class ParseError(val message: String) {
    data class Asn1DecodingError(val cause: String) : ParseError("ASN.1 decoding failed: $cause")
    data class FieldExtractionError(val fieldName: String, val cause: String) : 
        ParseError("Failed to extract field '$fieldName': $cause")
    data class ValidationError(val fieldName: String, val violation: String) : 
        ParseError("Validation failed for field '$fieldName': $violation")
    data class DatabaseError(val operation: String, val cause: String) : 
        ParseError("Database operation '$operation' failed: $cause")
}

// GOOD: Result type for error handling
sealed class ParseResult {
    data class Success(val record: ZteCdrRecord) : ParseResult()
    data class Failure(val errors: List<ParseError>) : ParseResult()
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

// Usage
fun parseRecord(asn1Data: ByteArray): ParseResult {
    return try {
        val decoded = asn1Decoder.decode(asn1Data)
        val record = extractFields(decoded)
        ParseResult.Success(record)
    } catch (ex: Asn1DecodingException) {
        ParseResult.Failure(listOf(ParseError.Asn1DecodingError(ex.message ?: "Unknown error")))
    }
}
```

### Fail Fast Principle
Validate inputs early and fail fast:

```kotlin
// GOOD: Early validation
fun parseRecord(asn1Data: ByteArray): ParseResult {
    require(asn1Data.isNotEmpty()) { "ASN.1 data cannot be empty" }
    require(asn1Data.size <= MAX_ASN1_RECORD_SIZE) { "ASN.1 data too large" }
    
    return try {
        val decoded = asn1Decoder.decode(asn1Data)
        extractAndValidateRecord(decoded)
    } catch (ex: Exception) {
        ParseResult.Failure(listOf(ParseError.from(ex)))
    }
}

// GOOD: Guard clauses for early returns
fun validateMsisdn(msisdn: String): ValidationResult {
    if (msisdn.isEmpty()) {
        return ValidationResult.error("MSISDN cannot be empty")
    }
    
    if (msisdn.length > MAX_MSISDN_LENGTH) {
        return ValidationResult.error("MSISDN too long")
    }
    
    if (!msisdn.matches("^\\d+$".toRegex())) {
        return ValidationResult.error("MSISDN must contain only digits")
    }
    
    return ValidationResult.success()
}

// BAD: Late validation with nested conditions
fun validateMsisdn(msisdn: String): ValidationResult {
    if (msisdn.isNotEmpty()) {
        if (msisdn.length <= MAX_MSISDN_LENGTH) {
            if (msisdn.matches("^\\d+$".toRegex())) {
                return ValidationResult.success()
            } else {
                return ValidationResult.error("MSISDN must contain only digits")
            }
        } else {
            return ValidationResult.error("MSISDN too long")
        }
    } else {
        return ValidationResult.error("MSISDN cannot be empty")
    }
}
```

## Documentation Standards

### KDoc Comments
Use KDoc for all public APIs:

```kotlin
/**
 * Parses ZTE ASN.1 encoded CDR records and extracts structured data.
 *
 * This parser handles ZTE-specific ASN.1 BER/DER encoded Call Detail Records,
 * extracting telecommunications data fields and validating them according
 * to ZTE CDR specifications.
 *
 * @param asn1Data The ASN.1 BER/DER encoded CDR record data
 * @return ParseResult containing either the successfully parsed ZteCdrRecord or errors
 * @throws IllegalArgumentException if asn1Data is null or empty
 * @throws Asn1DecodingException if ASN.1 data is malformed and cannot be decoded
 *
 * @sample parseValidZteCdrRecord
 * @see ZteCdrRecord for the structure of parsed data
 * @see ValidationResult for validation error details
 *
 * @since 1.0.0
 * @author TIA ETL Team
 */
fun parseRecord(asn1Data: ByteArray): ParseResult {
    // Implementation
}

/**
 * Validates a parsed ZTE CDR record against business rules and format constraints.
 *
 * Performs comprehensive validation including:
 * - Field presence validation
 * - Format validation (MSISDN, IMSI patterns)
 * - Range validation (signal strength, duration)
 * - Business rule validation (start time before end time)
 *
 * @param record The ZteCdrRecord to validate
 * @return ValidationResult indicating success or containing detailed error information
 * @throws IllegalArgumentException if record is null
 *
 * @see ValidationRule for individual validation rule implementations
 * @see ValidationError for error detail structure
 */
fun validateRecord(record: ZteCdrRecord): ValidationResult {
    // Implementation
}
```

### Code Comments
Comments should explain "why" not "what":

```kotlin
// GOOD: Explains business context
fun calculateTaxAmount(chargeAmount: Decimal, tariffClass: Int): Decimal {
    // Government of Liberia telecom tax is 15% for all call types
    // as per Telecommunications Act 2024, Section 12.3
    val taxRate = when (tariffClass) {
        PREMIUM_TARIFF -> 0.20 // Higher rate for premium services
        STANDARD_TARIFF -> 0.15 // Standard rate per legislation
        else -> 0.15 // Default to standard rate for unknown tariffs
    }
    
    return chargeAmount * taxRate.toBigDecimal()
}

// GOOD: Explains complex algorithm
fun optimizeBatchSize(recordCount: Int, memoryLimit: Int): Int {
    // Dynamic batch sizing based on available memory to prevent OOM
    // Each ZteCdrRecord averages ~500 bytes in memory
    val estimatedRecordSize = 500
    val maxRecordsInMemory = memoryLimit / estimatedRecordSize
    
    // Use smaller batches for better transaction granularity
    return minOf(recordCount, maxRecordsInMemory, DEFAULT_BATCH_SIZE)
}

// BAD: Comments that restate the code
fun validateMsisdn(msisdn: String): Boolean {
    // Check if msisdn length is between 1 and 15
    if (msisdn.length < 1 || msisdn.length > 15) {
        return false
    }
    
    // Check if msisdn contains only digits
    return msisdn.matches("^\\d+$".toRegex())
}
```

### README and Documentation
Maintain comprehensive documentation:

```markdown
# ZTE ASN.1 CDR Parser

High-quality implementation of ZTE ASN.1 CDR parser following Clean Code principles and TDD methodology.

## Features

- ✅ Clean Architecture with SOLID principles
- ✅ Comprehensive test coverage (>90%)
- ✅ Non-nullable ClickHouse schema
- ✅ Memory-efficient processing
- ✅ Robust error handling

## Quick Start

```kotlin
val parser = ZteAsn1ParserService(decoder, validator, writer, metrics)
val result = parser.processFile(File("cdr-data.asn1"))

if (result.success) {
    println("Processed ${result.recordsInserted} records successfully")
} else {
    println("Processing failed: ${result.errorSummary}")
}
```

## Architecture

### Core Components

- **Asn1Decoder**: Handles ASN.1 BER/DER decoding
- **DataValidator**: Validates extracted field data
- **DatabaseWriter**: Manages ClickHouse batch insertion
- **MetricsCollector**: Tracks performance metrics

### Design Principles

This implementation strictly follows Clean Code principles:

1. **Single Responsibility**: Each class has one reason to change
2. **Open/Closed**: Extensible without modification
3. **Liskov Substitution**: Implementations are interchangeable
4. **Interface Segregation**: Focused, client-specific interfaces
5. **Dependency Inversion**: Depends on abstractions, not concretions
```