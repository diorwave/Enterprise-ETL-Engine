# Kotlin Code Conventions for TIA ETL System

## Language Features

### Coroutines
- Use `suspend` functions for async operations
- Prefer `coroutineScope` over `GlobalScope`
- Use `withContext(Dispatchers.IO)` for blocking I/O operations
- Handle cancellation properly with `ensureActive()`

```kotlin
// Good
suspend fun processFile(file: File) = coroutineScope {
    withContext(Dispatchers.IO) {
        // I/O operations
    }
}

// Bad
fun processFile(file: File) = GlobalScope.launch {
    // Avoid GlobalScope
}
```

### Data Classes
- Use data classes for DTOs and value objects
- Include `@field:` annotations for validation

```kotlin
data class JobConfigurationDto(
    val id: UUID,
    @field:NotBlank
    val name: String,
    @field:Valid
    val schedule: ScheduleDto,
    val createdAt: Instant = Instant.now()
)
```

### Sealed Classes
- Use sealed classes for state representation
- Prefer sealed classes over enums when data is needed

```kotlin
sealed class ProcessingStatus {
    object Pending : ProcessingStatus()
    data class Processing(val startTime: Instant) : ProcessingStatus()
    data class Completed(val result: ProcessingResult) : ProcessingStatus()
    data class Failed(val error: String, val retryCount: Int) : ProcessingStatus()
}
```

### Extension Functions
- Create extension functions for common operations
- Keep extensions close to their usage

```kotlin
fun String.toUUID(): UUID? = try {
    UUID.fromString(this)
} catch (e: IllegalArgumentException) {
    null
}

fun <T> RedisTemplate<String, String>.getObject(key: String, type: Class<T>): T? {
    val json = opsForValue().get(key)
    return json?.let { objectMapper.readValue(it, type) }
}
```

## Spring Boot Integration

### Dependency Injection
- Use constructor injection exclusively
- Avoid field injection

```kotlin
// Good
@Service
class FileScanner(
    private val repository: JobRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {
    // Implementation
}

// Bad
@Service
class FileScanner {
    @Autowired
    lateinit var repository: JobRepository // Avoid
}
```

### Configuration Properties
- Use `@ConfigurationProperties` with data classes
- Validate configuration at startup

```kotlin
@ConfigurationProperties(prefix = "file-scanner")
@Validated
data class FileScannerProperties(
    @field:NotNull
    val threadPoolSize: Int = 10,
    @field:NotNull
    val maxConcurrentScans: Int = 5,
    @field:Valid
    val retry: RetryProperties = RetryProperties()
) {
    data class RetryProperties(
        val maxAttempts: Int = 3,
        val initialDelay: Duration = Duration.ofSeconds(30)
    )
}
```

### REST Controllers
- Use suspend functions for async endpoints
- Return proper HTTP status codes
- Use `@Valid` for request validation

```kotlin
@RestController
@RequestMapping("/api/v1/jobs")
class JobController(private val service: JobService) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@Valid @RequestBody request: CreateJobRequest): JobDto {
        return service.create(request)
    }
    
    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: UUID): JobDto {
        return service.findById(id) 
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found")
    }
}
```

## Error Handling

### Custom Exceptions
- Create domain-specific exceptions
- Include relevant context in exceptions

```kotlin
class FileProcessingException(
    message: String,
    val fileName: String,
    val jobId: UUID,
    cause: Throwable? = null
) : RuntimeException(message, cause)
```

### Global Exception Handler
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(FileProcessingException::class)
    fun handleFileProcessing(e: FileProcessingException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(
                error = "FILE_PROCESSING_ERROR",
                message = e.message,
                details = mapOf(
                    "fileName" to e.fileName,
                    "jobId" to e.jobId.toString()
                )
            ))
    }
}
```

## Testing

### Unit Tests
- Use MockK for mocking
- Test coroutines with `runTest`
- Use descriptive test names with backticks

```kotlin
@ExtendWith(MockKExtension::class)
class FileServiceTest {
    
    @MockK
    private lateinit var repository: FileRepository
    
    @Test
    fun `should process file successfully when valid`() = runTest {
        // Given
        every { repository.save(any()) } returns mockFile
        
        // When
        val result = service.processFile(testFile)
        
        // Then
        assertThat(result).isNotNull()
        verify { repository.save(any()) }
    }
}
```

### Integration Tests
- Use Testcontainers for external dependencies
- Test complete workflows

```kotlin
@SpringBootTest
@Testcontainers
class IntegrationTest {
    
    @Container
    val postgres = PostgreSQLContainer("postgres:16")
    
    @Container
    val redis = GenericContainer("redis:7-alpine")
        .withExposedPorts(6379)
    
    @Test
    fun `should complete file processing workflow`() {
        // Test complete workflow
    }
}
```

## Code Organization

### Package Structure
```
com.tia.etl.{service-name}/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic
│   └── impl/        # Service implementations
├── repository/       # Data access
├── domain/          # Domain models
│   ├── entity/      # JPA entities
│   └── dto/         # Data transfer objects
├── mapper/          # MapStruct mappers
├── exception/       # Custom exceptions
├── event/           # Event classes
└── util/            # Utility classes
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `FileScanner`)
- **Functions**: camelCase (e.g., `scanDirectory`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_FILE_SIZE`)
- **Properties**: camelCase (e.g., `fileName`)
- **Packages**: lowercase (e.g., `com.tia.etl.scanner`)

## Performance

### Lazy Initialization
```kotlin
class ExpensiveService {
    private val cachedData by lazy { loadExpensiveData() }
}
```

### Collection Operations
- Prefer sequences for large collections
- Use appropriate collection types

```kotlin
// For large collections
files.asSequence()
    .filter { it.size > threshold }
    .map { processFile(it) }
    .take(100)
    .toList()
```

### Database Operations
- Use batch operations for bulk inserts
- Implement proper pagination

```kotlin
@Repository
interface FileRepository : JpaRepository<FileEntity, UUID> {
    
    @Modifying
    @Query("UPDATE FileEntity f SET f.status = :status WHERE f.id IN :ids")
    fun updateStatusBatch(@Param("ids") ids: List<UUID>, @Param("status") status: String)
    
    fun findByStatusOrderByCreatedAtDesc(
        status: String, 
        pageable: Pageable
    ): Page<FileEntity>
}
```

## Security

### Input Validation
- Validate all external inputs
- Use `@Valid` annotation
- Sanitize file paths

```kotlin
fun validateFilePath(path: String): String {
    require(!path.contains("..")) { "Path traversal detected" }
    require(path.startsWith("/data/")) { "Invalid path prefix" }
    return path.normalize()
}
```

### Sensitive Data
- Never log sensitive information
- Use `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)` for passwords

```kotlin
data class UserDto(
    val username: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val password: String
)
```

## Documentation

### KDoc Comments
```kotlin
/**
 * Processes files from the configured directory.
 * 
 * @param job The job configuration containing scan parameters
 * @return List of discovered files
 * @throws FileProcessingException if scanning fails
 */
suspend fun scanDirectory(job: JobConfiguration): List<FileDiscovery> {
    // Implementation
}
```

### API Documentation
- Use OpenAPI annotations
- Document all endpoints

```kotlin
@Operation(summary = "Create a new job", description = "Creates a new file scanning job")
@ApiResponses(
    ApiResponse(responseCode = "201", description = "Job created successfully"),
    ApiResponse(responseCode = "400", description = "Invalid request"),
    ApiResponse(responseCode = "409", description = "Job with same name exists")
)
@PostMapping
suspend fun createJob(@RequestBody request: CreateJobRequest): JobDto
```

## Logging

### Structured Logging
```kotlin
private val logger = LoggerFactory.getLogger(FileScanner::class.java)

logger.info("Processing file", kv("fileName", file.name), kv("size", file.size))
logger.error("File processing failed", e, kv("fileName", file.name), kv("jobId", jobId))
```

### Log Levels
- **ERROR**: System errors requiring attention
- **WARN**: Recoverable issues
- **INFO**: Important business events
- **DEBUG**: Detailed diagnostic information

## Build Configuration

### Gradle Kotlin DSL
```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}
```