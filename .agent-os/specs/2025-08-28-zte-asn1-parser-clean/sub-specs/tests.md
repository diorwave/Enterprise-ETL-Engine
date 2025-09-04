# Tests Specification

This is the tests coverage details for the spec detailed in @.agent-os/specs/2025-08-28-zte-asn1-parser-clean/spec.md

> Created: 2025-08-28
> Version: 1.0.0

## Test Coverage

### Coverage Requirements

#### Overall Coverage Targets
- **Unit Tests**: >90% code coverage across all modules
- **Integration Tests**: 100% coverage of external system interactions  
- **End-to-End Tests**: 100% coverage of critical user flows
- **Performance Tests**: All components under load conditions

#### Coverage by Module

| Module | Unit Tests | Integration Tests | Performance Tests | Coverage Target |
|--------|------------|-------------------|-------------------|-----------------|
| Core Parser | >95% | ✓ | ✓ | Critical |
| ASN.1 Decoder | >90% | ✓ | ✓ | High |
| Data Validation | >95% | ✓ | - | Critical |
| ClickHouse Writer | >85% | ✓ | ✓ | High |
| Error Handling | >90% | ✓ | - | High |
| Configuration | >80% | ✓ | - | Medium |

### Test-Driven Development Strategy

#### TDD Implementation Phases

**Phase 1: Core Parsing Logic (Red-Green-Refactor)**
```kotlin
// Example TDD cycle for ASN.1 field parsing
class ZteAsn1FieldParsingTest {
    
    @Test
    fun `should fail when parsing invalid ASN1 MSISDN field`() {
        // RED: Write failing test first
        val invalidAsn1Data = byteArrayOf(0x04, 0x10, 0xFF) // Invalid MSISDN
        
        assertThatThrownBy {
            fieldParser.parseMsisdn(invalidAsn1Data)
        }.isInstanceOf(Asn1ParsingException::class.java)
            .hasMessageContaining("Invalid MSISDN format")
    }
    
    @Test
    fun `should parse valid ASN1 MSISDN field correctly`() {
        // GREEN: Write minimal implementation to pass
        val validAsn1Data = createValidMsisdnAsn1Data("1234567890")
        
        val result = fieldParser.parseMsisdn(validAsn1Data)
        
        assertThat(result).isEqualTo("1234567890")
    }
    
    @Test
    fun `should handle MSISDN with country code prefix`() {
        // REFACTOR: Add more test cases and refine implementation
        val msisdnWithCountryCode = createValidMsisdnAsn1Data("+23176543210")
        
        val result = fieldParser.parseMsisdn(msisdnWithCountryCode)
        
        assertThat(result).isEqualTo("+23176543210")
        assertThat(result).hasSize(12)
    }
}
```

**Phase 2: Data Validation Logic**
```kotlin
class ZteCdrRecordValidationTest {
    
    @Test
    fun `should reject record with invalid call duration`() {
        // RED: Test validation rules
        val invalidRecord = ZteCdrRecord(
            callDuration = -1, // Invalid negative duration
            // ... other valid fields
        )
        
        val result = validator.validateRecord(invalidRecord)
        
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).hasSize(1)
        assertThat(result.errors.first().field).isEqualTo("callDuration")
    }
    
    @Test
    fun `should accept record with valid call duration`() {
        // GREEN: Implement validation logic
        val validRecord = createValidZteCdrRecord(callDuration = 300)
        
        val result = validator.validateRecord(validRecord)
        
        assertThat(result.isValid).isTrue()
        assertThat(result.errors).isEmpty()
    }
}
```

**Phase 3: Database Integration**
```kotlin
class ClickHouseWriterTest {
    
    @Test
    fun `should reject batch with null values`() {
        // RED: Test non-nullable constraint enforcement
        val recordWithNulls = ZteCdrRecord(
            msisdn = null, // Should not be allowed
            // ... other fields
        )
        
        assertThatThrownBy {
            clickHouseWriter.insertBatch(listOf(recordWithNulls))
        }.isInstanceOf(SchemaViolationException::class.java)
            .hasMessageContaining("null values not allowed")
    }
    
    @Test
    fun `should successfully insert valid batch`() {
        // GREEN: Implement proper validation and insertion
        val validRecords = createValidZteCdrRecords(count = 1000)
        
        val result = clickHouseWriter.insertBatch(validRecords)
        
        assertThat(result.insertedCount).isEqualTo(1000)
        assertThat(result.errors).isEmpty()
    }
}
```

### Unit Test Specifications

#### Core Parser Module Tests
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZteAsn1ParserTest {
    
    private lateinit var parser: ZteAsn1Parser
    private lateinit var mockDecoder: Asn1Decoder
    private lateinit var mockValidator: DataValidator
    
    @BeforeAll
    fun setup() {
        mockDecoder = mockk<Asn1Decoder>()
        mockValidator = mockk<DataValidator>()
        parser = ZteAsn1ParserImpl(mockDecoder, mockValidator)
    }
    
    @Nested
    @DisplayName("Record Parsing Tests")
    inner class RecordParsingTests {
        
        @Test
        fun `should parse complete ZTE CDR record successfully`() {
            // Given
            val sampleAsn1Data = loadSampleAsn1File("valid-zte-cdr.asn1")
            val expectedRecord = createExpectedZteCdrRecord()
            
            every { mockDecoder.decode(any()) } returns createAsn1Structure()
            every { mockValidator.validate(any()) } returns ValidationResult.success()
            
            // When
            val result = parser.parseRecord(sampleAsn1Data)
            
            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.record).isEqualTo(expectedRecord)
            verify { mockDecoder.decode(sampleAsn1Data) }
            verify { mockValidator.validate(any()) }
        }
        
        @Test
        fun `should handle malformed ASN1 data gracefully`() {
            // Given
            val malformedData = byteArrayOf(0xFF, 0xFF, 0xFF)
            
            every { mockDecoder.decode(any()) } throws Asn1DecodingException("Invalid BER encoding")
            
            // When
            val result = parser.parseRecord(malformedData)
            
            // Then
            assertThat(result.isSuccess).isFalse()
            assertThat(result.errors).hasSize(1)
            assertThat(result.errors.first().type).isEqualTo(ErrorType.ASN1_PARSING_ERROR)
        }
        
        @ParameterizedTest
        @ValueSource(strings = ["", "1", "12345678901234567890"])
        fun `should validate MSISDN length constraints`(msisdn: String) {
            // Test various MSISDN lengths including edge cases
            val isValid = msisdn.length in 1..15
            
            val record = createZteCdrRecord(msisdn = msisdn)
            val result = parser.validateRecord(record)
            
            assertThat(result.isValid).isEqualTo(isValid)
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    inner class ErrorHandlingTests {
        
        @Test
        fun `should collect multiple validation errors`() {
            // Given
            val recordWithMultipleErrors = ZteCdrRecord(
                msisdn = "", // Too short
                callDuration = -1, // Negative
                signalStrength = 100 // Out of range
            )
            
            // When
            val result = parser.validateRecord(recordWithMultipleErrors)
            
            // Then
            assertThat(result.isValid).isFalse()
            assertThat(result.errors).hasSize(3)
            assertThat(result.errors.map { it.field })
                .containsExactlyInAnyOrder("msisdn", "callDuration", "signalStrength")
        }
        
        @Test
        fun `should handle parser exceptions without crashing`() {
            // Given
            val problematicData = createProblematicAsn1Data()
            
            every { mockDecoder.decode(any()) } throws OutOfMemoryError("Simulated OOM")
            
            // When
            val result = parser.parseRecord(problematicData)
            
            // Then
            assertThat(result.isSuccess).isFalse()
            assertThat(result.errors.first().type).isEqualTo(ErrorType.SYSTEM_ERROR)
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    inner class PerformanceTests {
        
        @Test
        fun `should parse 1000 records within 1 second`() {
            // Given
            val records = generateAsn1Records(count = 1000)
            val startTime = System.currentTimeMillis()
            
            // When
            records.forEach { data ->
                parser.parseRecord(data)
            }
            
            // Then
            val duration = System.currentTimeMillis() - startTime
            assertThat(duration).isLessThan(1000) // Less than 1 second
        }
        
        @Test
        fun `should maintain memory usage under 100MB for large file`() {
            // Given
            val runtime = Runtime.getRuntime()
            val initialMemory = runtime.totalMemory() - runtime.freeMemory()
            val largeAsn1File = generateLargeAsn1File(sizeMb = 50)
            
            // When
            parser.parseFile(largeAsn1File)
            
            // Then
            val finalMemory = runtime.totalMemory() - runtime.freeMemory()
            val memoryUsed = (finalMemory - initialMemory) / (1024 * 1024) // Convert to MB
            assertThat(memoryUsed).isLessThan(100)
        }
    }
}
```

#### Data Validation Tests
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataValidatorTest {
    
    private lateinit var validator: DataValidator
    
    @BeforeAll
    fun setup() {
        validator = DataValidatorImpl()
    }
    
    @Nested
    @DisplayName("Field Validation Tests")
    inner class FieldValidationTests {
        
        @ParameterizedTest
        @CsvSource(
            "'1234567890', true",
            "'123456789012345', true",
            "'', false",
            "'12345678901234567890', false",
            "'123abc789', false"
        )
        fun `should validate MSISDN format correctly`(msisdn: String, expected: Boolean) {
            val result = validator.validateMsisdn(msisdn)
            assertThat(result.isValid).isEqualTo(expected)
        }
        
        @ParameterizedTest
        @ValueSource(ints = [-1, 86401, Int.MAX_VALUE])
        fun `should reject invalid call duration values`(duration: Int) {
            val result = validator.validateCallDuration(duration)
            assertThat(result.isValid).isFalse()
            assertThat(result.errorMessage).contains("duration")
        }
        
        @ParameterizedTest
        @ValueSource(ints = [0, 1, 3600, 86400])
        fun `should accept valid call duration values`(duration: Int) {
            val result = validator.validateCallDuration(duration)
            assertThat(result.isValid).isTrue()
        }
    }
    
    @Nested
    @DisplayName("Record Validation Tests") 
    inner class RecordValidationTests {
        
        @Test
        fun `should validate complete record successfully`() {
            // Given
            val validRecord = createValidZteCdrRecord()
            
            // When
            val result = validator.validateRecord(validRecord)
            
            // Then
            assertThat(result.isValid).isTrue()
            assertThat(result.errors).isEmpty()
            assertThat(result.warnings).isEmpty()
        }
        
        @Test
        fun `should collect all validation errors for invalid record`() {
            // Given
            val invalidRecord = ZteCdrRecord(
                msisdn = "", // Invalid
                imsi = "12345", // Too short
                callDuration = -1, // Invalid
                signalStrength = 100 // Out of range
            )
            
            // When
            val result = validator.validateRecord(invalidRecord)
            
            // Then
            assertThat(result.isValid).isFalse()
            assertThat(result.errors).hasSize(4)
        }
    }
}
```

### Integration Test Specifications

#### Database Integration Tests
```kotlin
@Testcontainers
@SpringBootTest
class ClickHouseIntegrationTest {
    
    companion object {
        @Container
        val clickhouseContainer = ClickHouseContainer("yandex/clickhouse-server:21.3")
            .withDatabaseName("test_tia")
            .withUsername("test")
            .withPassword("test")
    }
    
    @Autowired
    private lateinit var clickHouseWriter: ClickHouseWriter
    
    @Test
    fun `should create table with non-nullable schema`() {
        // Given
        val connection = createClickHouseConnection()
        
        // When
        clickHouseWriter.createTable(connection)
        
        // Then
        val tableSchema = getTableSchema(connection, "zte_cdr_records")
        assertThat(tableSchema.columns).allMatch { !it.isNullable }
    }
    
    @Test
    fun `should insert batch of valid records successfully`() {
        // Given
        val validRecords = createValidZteCdrRecords(count = 1000)
        
        // When
        val result = clickHouseWriter.insertBatch(validRecords)
        
        // Then
        assertThat(result.insertedCount).isEqualTo(1000)
        assertThat(result.errors).isEmpty()
        
        val actualCount = getRecordCount()
        assertThat(actualCount).isEqualTo(1000)
    }
    
    @Test
    fun `should reject batch with schema violations`() {
        // Given - Create records with null values (should be prevented by validation)
        val recordsWithNulls = createRecordsWithNullValues(count = 10)
        
        // When/Then
        assertThatThrownBy {
            clickHouseWriter.insertBatch(recordsWithNulls)
        }.isInstanceOf(SchemaViolationException::class.java)
    }
    
    @Test
    fun `should handle large batch insertion efficiently`() {
        // Given
        val largeRecordSet = createValidZteCdrRecords(count = 100_000)
        val startTime = System.currentTimeMillis()
        
        // When
        val result = clickHouseWriter.insertBatch(largeRecordSet)
        
        // Then
        val duration = System.currentTimeMillis() - startTime
        assertThat(duration).isLessThan(30_000) // Less than 30 seconds
        assertThat(result.insertedCount).isEqualTo(100_000)
    }
}
```

#### File Processing Integration Tests
```kotlin
@SpringBootTest
@TestPropertySource(properties = ["spring.profiles.active=test"])
class FileProcessingIntegrationTest {
    
    @Autowired
    private lateinit var zteAsn1Parser: ZteAsn1ParserPlugin
    
    @TempDir
    lateinit var tempDir: Path
    
    @Test
    fun `should process complete ZTE ASN1 file end-to-end`() {
        // Given
        val sampleFile = createSampleAsn1File(tempDir, recordCount = 10_000)
        val context = createProcessingContext(sampleFile)
        
        // When
        val result = zteAsn1Parser.process(context)
        
        // Then
        assertThat(result.success).isTrue()
        assertThat(result.recordsProcessed).isEqualTo(10_000)
        assertThat(result.recordsInserted).isEqualTo(10_000)
        assertThat(result.recordsWithErrors).isEqualTo(0)
    }
    
    @Test
    fun `should handle file with mixed valid and invalid records`() {
        // Given
        val mixedFile = createMixedQualityAsn1File(
            tempDir,
            validRecords = 8_000,
            invalidRecords = 2_000
        )
        val context = createProcessingContext(mixedFile)
        
        // When
        val result = zteAsn1Parser.process(context)
        
        // Then
        assertThat(result.success).isTrue()
        assertThat(result.recordsProcessed).isEqualTo(10_000)
        assertThat(result.recordsInserted).isEqualTo(8_000)
        assertThat(result.recordsWithErrors).isEqualTo(2_000)
        assertThat(result.errorDetails).hasSize(2_000)
    }
    
    @Test
    fun `should fail gracefully on completely corrupted file`() {
        // Given
        val corruptedFile = createCorruptedAsn1File(tempDir)
        val context = createProcessingContext(corruptedFile)
        
        // When
        val result = zteAsn1Parser.process(context)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.recordsProcessed).isEqualTo(0)
        assertThat(result.errorDetails).isNotEmpty()
    }
}
```

### Performance Test Specifications

#### Load Testing with JMH
```kotlin
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
class ZteAsn1ParserBenchmark {
    
    private lateinit var parser: ZteAsn1Parser
    private lateinit var sampleRecords: List<ByteArray>
    
    @Setup
    fun setup() {
        parser = ZteAsn1ParserImpl(
            Asn1DecoderImpl(),
            DataValidatorImpl()
        )
        sampleRecords = generateSampleAsn1Records(count = 1000)
    }
    
    @Benchmark
    fun benchmarkSingleRecordParsing(): ParseResult {
        return parser.parseRecord(sampleRecords.first())
    }
    
    @Benchmark
    fun benchmarkBatchProcessing(): List<ParseResult> {
        return sampleRecords.map { parser.parseRecord(it) }
    }
    
    @Benchmark
    fun benchmarkMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        val beforeMemory = runtime.totalMemory() - runtime.freeMemory()
        
        parser.parseRecords(sampleRecords)
        
        val afterMemory = runtime.totalMemory() - runtime.freeMemory()
        return afterMemory - beforeMemory
    }
}
```

#### Stress Testing
```kotlin
@SpringBootTest
class StressTest {
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    fun `should handle continuous processing without memory leaks`() {
        // Given
        val parser = createZteAsn1Parser()
        val runtime = Runtime.getRuntime()
        var maxMemoryUsage = 0L
        
        // When - Process files continuously for 5 minutes
        val endTime = System.currentTimeMillis() + 300_000 // 5 minutes
        var filesProcessed = 0
        
        while (System.currentTimeMillis() < endTime) {
            val file = generateRandomAsn1File(recordCount = 1000)
            parser.processFile(file)
            
            val currentMemory = runtime.totalMemory() - runtime.freeMemory()
            maxMemoryUsage = maxOf(maxMemoryUsage, currentMemory)
            
            filesProcessed++
            
            // Force garbage collection every 10 files
            if (filesProcessed % 10 == 0) {
                System.gc()
            }
        }
        
        // Then
        val finalMemoryMb = maxMemoryUsage / (1024 * 1024)
        assertThat(finalMemoryMb).isLessThan(512) // Stay under 512MB
        assertThat(filesProcessed).isGreaterThan(100) // Process at least 100 files
    }
}
```

## Mocking Requirements

### Mock Strategy

#### External Dependencies Mocking
```kotlin
// Database Connection Mocking
@MockBean
private lateinit var mockClickHouseConnection: Connection

@MockBean
private lateinit var mockPostgresConnection: Connection

// ASN.1 Library Mocking
private lateinit var mockAsn1Decoder: Asn1Decoder

// Configuration Mocking
@MockBean
private lateinit var mockJobConfiguration: JobConfiguration

// Logger Mocking
private lateinit var mockLogger: Logger
```

#### Test Data Factories
```kotlin
object TestDataFactory {
    
    fun createValidZteCdrRecord(
        msisdn: String = "1234567890",
        imsi: String = "123456789012345",
        callDuration: Int = 300,
        signalStrength: Int = -85
    ): ZteCdrRecord {
        return ZteCdrRecord(
            recordId = UUID.randomUUID().toString(),
            msisdn = msisdn,
            imsi = imsi,
            callStartTime = Instant.now(),
            callDuration = callDuration,
            signalStrength = signalStrength,
            // ... other required fields with defaults
        )
    }
    
    fun createValidAsn1Data(record: ZteCdrRecord): ByteArray {
        // Generate valid ASN.1 BER encoding for the record
        return Asn1TestEncoder.encode(record)
    }
    
    fun createProcessingContext(
        file: File,
        batchSize: Int = 10_000
    ): ProcessingContext {
        return ProcessingContext(
            file = file,
            clickHouseConnection = mockClickHouseConnection,
            postgresConnection = mockPostgresConnection,
            jobConfig = JobConfiguration(
                id = UUID.randomUUID(),
                batchSize = batchSize,
                // ... other config
            ),
            logger = mockLogger
        )
    }
}
```

#### Mock Behavior Setup
```kotlin
class MockSetupHelper {
    
    fun setupSuccessfulDatabaseMocks() {
        every { 
            mockClickHouseConnection.prepareStatement(any()) 
        } returns mockk<PreparedStatement> {
            every { executeBatch() } returns intArrayOf(1)
            every { close() } just Runs
        }
    }
    
    fun setupFailingDatabaseMocks() {
        every { 
            mockClickHouseConnection.prepareStatement(any()) 
        } throws SQLException("Connection failed")
    }
    
    fun setupAsn1DecoderMocks(records: List<ZteCdrRecord>) {
        records.forEachIndexed { index, record ->
            every { 
                mockAsn1Decoder.decode(any()) 
            } returns createAsn1Structure(record)
        }
    }
}
```

### Testing Best Practices

#### Test Organization
1. **Arrange-Act-Assert** pattern for all tests
2. **One assertion per test** method when possible
3. **Descriptive test names** using backticks
4. **Test data builders** for complex objects
5. **Parameterized tests** for multiple input scenarios

#### Test Isolation
1. **Independent tests** that don't depend on each other
2. **Clean state** before each test execution
3. **Resource cleanup** after test completion
4. **Separate test databases** for integration tests

#### Error Testing Strategy
1. **Negative test cases** for all validation rules
2. **Exception path testing** with proper assertions
3. **Resource cleanup** testing in error scenarios
4. **Boundary value testing** for all numeric fields