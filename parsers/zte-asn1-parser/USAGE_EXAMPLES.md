# ZTE ASN.1 Parser Library - Usage Examples

This document provides practical examples of how to use the ZTE ASN.1 Parser Library in different scenarios.

## 1. Basic Usage as Library Dependency

### Adding to your project

```kotlin
// build.gradle.kts
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/quantum-soft-dev/tia-etl-system")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("com.quantumsoft.tia.parsers:zte-asn1-parser:1.0.0")
    implementation("com.quantumsoft.tia.parsers:parser-api:1.0.0")
}
```

### Basic parsing example

```kotlin
import com.quantumsoft.tia.parsers.api.*
import com.quantumsoft.tia.parsers.zte.ZteAsn1ParserImpl
import java.io.File
import java.sql.Connection

class CdrProcessingService(
    private val clickHouseConnection: Connection,
    private val postgresConnection: Connection
) {
    
    fun processCdrFile(cdrFile: File): ProcessingResult {
        val parser = ZteAsn1ParserImpl()
        
        val context = ProcessingContext(
            file = cdrFile,
            clickHouseConnection = clickHouseConnection,
            postgresConnection = postgresConnection,
            jobConfig = createJobConfig(),
            logger = LoggerFactory.getLogger(this::class.java)
        )
        
        return parser.process(context)
    }
    
    private fun createJobConfig(): JobConfiguration {
        return JobConfiguration(
            id = UUID.randomUUID(),
            name = "zte-cdr-processing",
            parserId = "zte-asn1-parser",
            batchSize = 1000,
            maxFileSize = 1073741824L, // 1GB
            targetTable = "zte_cdr_records"
        )
    }
}
```

## 2. Spring Boot Integration

### Configuration

```kotlin
@Configuration
class ParserConfiguration {
    
    @Bean
    fun zteAsn1Parser(): DataParser {
        return ZteAsn1ParserImpl()
    }
    
    @Bean
    @ConfigurationProperties("parser.zte-asn1")
    fun zteParserProperties(): ZteParserProperties {
        return ZteParserProperties()
    }
}

@ConfigurationProperties("parser.zte-asn1")
data class ZteParserProperties(
    var maxFileSize: Long = 1073741824L,
    var batchSize: Int = 1000,
    var validationEnabled: Boolean = true,
    var targetTable: String = "zte_cdr_records"
)
```

### Service implementation

```kotlin
@Service
class CdrProcessingService(
    private val zteAsn1Parser: DataParser,
    private val zteParserProperties: ZteParserProperties,
    @Qualifier("clickHouseConnection") private val clickHouseConnection: Connection,
    @Qualifier("postgresConnection") private val postgresConnection: Connection,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(CdrProcessingService::class.java)
    private val processingTimer = Timer.builder("cdr.processing.duration")
        .description("Time taken to process CDR files")
        .register(meterRegistry)
    
    suspend fun processFile(file: File): ProcessingResult = withContext(Dispatchers.IO) {
        val timer = Timer.Sample.start(meterRegistry)
        
        try {
            val context = ProcessingContext(
                file = file,
                clickHouseConnection = clickHouseConnection,
                postgresConnection = postgresConnection,
                jobConfig = createJobConfig(file),
                logger = logger
            )
            
            val result = zteAsn1Parser.process(context)
            
            // Update metrics
            meterRegistry.counter("cdr.files.processed", 
                "status", result.status.name.lowercase(),
                "parser", "zte-asn1"
            ).increment()
            
            if (result.status == ProcessingStatus.SUCCESS) {
                meterRegistry.counter("cdr.records.processed").increment(result.recordsProcessed.toDouble())
            }
            
            result
        } catch (e: Exception) {
            logger.error("Error processing CDR file: ${file.name}", e)
            meterRegistry.counter("cdr.processing.errors").increment()
            throw e
        } finally {
            timer.stop(processingTimer)
        }
    }
    
    private fun createJobConfig(file: File) = JobConfiguration(
        id = UUID.randomUUID(),
        name = "zte-cdr-${file.nameWithoutExtension}",
        parserId = "zte-asn1-parser",
        batchSize = zteParserProperties.batchSize,
        maxFileSize = zteParserProperties.maxFileSize,
        targetTable = zteParserProperties.targetTable
    )
}
```

## 3. Batch Processing with Coroutines

```kotlin
@Service
class BatchCdrProcessor(
    private val cdrProcessingService: CdrProcessingService
) {
    
    private val logger = LoggerFactory.getLogger(BatchCdrProcessor::class.java)
    
    suspend fun processDirectory(directory: File, maxConcurrency: Int = 5): List<ProcessingResult> {
        val cdrFiles = directory.listFiles { file -> 
            file.isFile && (file.extension.lowercase() in listOf("cdr", "asn1", "ber"))
        } ?: emptyArray()
        
        logger.info("Found ${cdrFiles.size} CDR files in directory: ${directory.path}")
        
        return cdrFiles.asFlow()
            .map { file ->
                async {
                    try {
                        cdrProcessingService.processFile(file)
                    } catch (e: Exception) {
                        logger.error("Failed to process file: ${file.name}", e)
                        ProcessingResult(
                            status = ProcessingStatus.FAILED,
                            recordsProcessed = 0,
                            errorMessage = "Processing failed: ${e.message}",
                            processingTimeMs = 0,
                            metadata = mapOf("fileName" to file.name)
                        )
                    }
                }
            }
            .buffer(maxConcurrency)
            .map { it.await() }
            .toList()
    }
}
```

## 4. Custom Validation and Error Handling

```kotlin
@Component
class CdrFileValidator {
    
    private val logger = LoggerFactory.getLogger(CdrFileValidator::class.java)
    
    fun validateFile(file: File): ValidationResult {
        val errors = mutableListOf<String>()
        
        // File size validation
        if (file.length() > 1073741824L) { // 1GB
            errors.add("File size exceeds maximum limit of 1GB")
        }
        
        if (file.length() == 0L) {
            errors.add("File is empty")
        }
        
        // File format validation
        if (!isValidAsn1File(file)) {
            errors.add("File does not appear to be a valid ASN.1 file")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun isValidAsn1File(file: File): Boolean {
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(10)
                val bytesRead = input.read(header)
                
                // Basic ASN.1 BER/DER format check
                bytesRead > 0 && (header[0].toInt() and 0x1F) != 0x1F
            }
        } catch (e: Exception) {
            logger.warn("Error validating ASN.1 file format: ${file.name}", e)
            false
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
```

## 5. Monitoring and Metrics Integration

```kotlin
@Component
class CdrProcessingMetrics(private val meterRegistry: MeterRegistry) {
    
    private val fileProcessingTimer = Timer.builder("cdr.file.processing.time")
        .description("Time spent processing individual CDR files")
        .register(meterRegistry)
    
    private val recordProcessingRate = Gauge.builder("cdr.record.processing.rate")
        .description("Records processed per second")
        .register(meterRegistry) { recordsPerSecond }
    
    private var recordsPerSecond: Double = 0.0
    
    fun recordFileProcessed(file: File, result: ProcessingResult) {
        // File processing metrics
        meterRegistry.counter("cdr.files.total", 
            "status", result.status.name.lowercase(),
            "parser", "zte-asn1",
            "file_extension", file.extension.lowercase()
        ).increment()
        
        if (result.status == ProcessingStatus.SUCCESS) {
            // Records processed
            meterRegistry.counter("cdr.records.total", "parser", "zte-asn1")
                .increment(result.recordsProcessed.toDouble())
            
            // Processing rate calculation
            if (result.processingTimeMs > 0) {
                recordsPerSecond = (result.recordsProcessed * 1000.0) / result.processingTimeMs
            }
            
            // File size histogram
            meterRegistry.summary("cdr.file.size.bytes", "parser", "zte-asn1")
                .record(file.length().toDouble())
        }
        
        // Error tracking
        if (result.errorMessage != null) {
            meterRegistry.counter("cdr.processing.errors", 
                "parser", "zte-asn1",
                "error_type", categorizeError(result.errorMessage!!)
            ).increment()
        }
    }
    
    private fun categorizeError(errorMessage: String): String {
        return when {
            "validation" in errorMessage.lowercase() -> "validation"
            "connection" in errorMessage.lowercase() -> "database"
            "parsing" in errorMessage.lowercase() -> "parsing"
            "memory" in errorMessage.lowercase() -> "memory"
            else -> "other"
        }
    }
}
```

## 6. Testing Examples

### Unit Test

```kotlin
@ExtendWith(MockKExtension::class)
class ZteAsn1ParserTest {
    
    @MockK
    private lateinit var clickHouseConnection: Connection
    
    @MockK
    private lateinit var postgresConnection: Connection
    
    @MockK
    private lateinit var logger: Logger
    
    private lateinit var parser: ZteAsn1ParserImpl
    
    @BeforeEach
    fun setup() {
        parser = ZteAsn1ParserImpl()
    }
    
    @Test
    fun `should process valid CDR file successfully`() {
        // Given
        val testFile = createTestCdrFile()
        val context = ProcessingContext(
            file = testFile,
            clickHouseConnection = clickHouseConnection,
            postgresConnection = postgresConnection,
            jobConfig = createTestJobConfig(),
            logger = logger
        )
        
        every { clickHouseConnection.prepareStatement(any()) } returns mockk()
        
        // When
        val result = parser.process(context)
        
        // Then
        assertEquals(ProcessingStatus.SUCCESS, result.status)
        assertTrue(result.recordsProcessed > 0)
        assertNull(result.errorMessage)
    }
    
    private fun createTestCdrFile(): File {
        // Create a test CDR file with sample ASN.1 data
        val testFile = File.createTempFile("test_cdr", ".cdr")
        // Add test ASN.1 data...
        return testFile
    }
    
    private fun createTestJobConfig() = JobConfiguration(
        id = UUID.randomUUID(),
        name = "test-job",
        parserId = "zte-asn1-parser",
        batchSize = 100,
        maxFileSize = 1048576L,
        targetTable = "test_zte_cdr_records"
    )
}
```

### Integration Test

```kotlin
@SpringBootTest
@Testcontainers
class ZteAsn1ParserIntegrationTest {
    
    @Container
    companion object {
        @JvmStatic
        val clickhouse = ClickHouseContainer("clickhouse/clickhouse-server:latest")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
        
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
    }
    
    @Autowired
    private lateinit var cdrProcessingService: CdrProcessingService
    
    @Test
    fun `should process real CDR file end-to-end`() {
        // Given
        val testCdrFile = loadTestResourceFile("sample_zte_cdr.asn1")
        
        // When
        val result = runBlocking {
            cdrProcessingService.processFile(testCdrFile)
        }
        
        // Then
        assertEquals(ProcessingStatus.SUCCESS, result.status)
        assertTrue(result.recordsProcessed > 0)
        
        // Verify data was inserted into ClickHouse
        val recordCount = queryClickHouseRecordCount()
        assertEquals(result.recordsProcessed, recordCount)
    }
    
    private fun loadTestResourceFile(fileName: String): File {
        val resource = this::class.java.classLoader.getResource(fileName)
        requireNotNull(resource) { "Test resource file not found: $fileName" }
        return File(resource.toURI())
    }
    
    private fun queryClickHouseRecordCount(): Long {
        // Query ClickHouse to verify records were inserted
        // Implementation depends on your ClickHouse client setup
        return 0L // Placeholder
    }
}
```

## 7. Performance Tuning Configuration

```yaml
# application.yml
parser:
  zte-asn1:
    max-file-size: 2147483648  # 2GB
    batch-size: 5000          # Increase for better performance
    validation-enabled: false # Disable for production if not needed
    target-table: zte_cdr_records
    
clickhouse:
  url: jdbc:clickhouse://localhost:8123/tia
  username: default
  password: ""
  batch-size: 10000          # Large batches for ClickHouse
  connection-timeout: 60000  # 1 minute
  socket-timeout: 300000     # 5 minutes
  max-connections: 10
  
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 8. Error Recovery and Retry Logic

```kotlin
@Component
class ResilientCdrProcessor(
    private val cdrProcessingService: CdrProcessingService,
    private val retryTemplate: RetryTemplate
) {
    
    private val logger = LoggerFactory.getLogger(ResilientCdrProcessor::class.java)
    
    fun processWithRetry(file: File): ProcessingResult {
        return retryTemplate.execute<ProcessingResult, Exception> { context ->
            logger.info("Processing attempt ${context.retryCount + 1} for file: ${file.name}")
            
            try {
                runBlocking {
                    cdrProcessingService.processFile(file)
                }
            } catch (e: Exception) {
                logger.warn("Processing failed for ${file.name}, attempt ${context.retryCount + 1}", e)
                throw e
            }
        }
    }
}

@Configuration
class RetryConfiguration {
    
    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(SQLException::class.java)
            .retryOn(IOException::class.java)
            .build()
    }
}
```

These examples demonstrate the flexibility and power of the ZTE ASN.1 Parser Library in various scenarios, from simple file processing to complex enterprise integrations with monitoring, error handling, and performance optimization.