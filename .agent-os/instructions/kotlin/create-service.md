# Create Kotlin Spring Boot Service

## Overview
Generate a complete Spring Boot microservice with Kotlin following TIA ETL System architecture patterns.

## Service Generation Process

### Step 1: Service Structure
When creating a new service, generate the following structure:
```
services/{service-name}/
├── build.gradle.kts
├── src/main/kotlin/com/tia/etl/{service-name}/
│   ├── Application.kt
│   ├── config/
│   │   ├── RedisConfig.kt
│   │   ├── DatabaseConfig.kt
│   │   └── SecurityConfig.kt
│   ├── controller/
│   │   └── {ServiceName}Controller.kt
│   ├── service/
│   │   ├── {ServiceName}Service.kt
│   │   └── impl/{ServiceName}ServiceImpl.kt
│   ├── repository/
│   │   └── {ServiceName}Repository.kt
│   ├── domain/
│   │   ├── entity/
│   │   └── dto/
│   ├── mapper/
│   │   └── {ServiceName}Mapper.kt
│   └── exception/
│       └── {ServiceName}Exception.kt
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/
└── src/test/kotlin/com/tia/etl/{service-name}/
```

### Step 2: Core Components

#### Application Entry Point
```kotlin
@SpringBootApplication
@EnableAsync
@EnableScheduling
class {ServiceName}Application

fun main(args: Array<String>) {
    runApplication<{ServiceName}Application>(*args)
}
```

#### REST Controller Template
```kotlin
@RestController
@RequestMapping("/api/v1/{resource}")
class {ServiceName}Controller(
    private val service: {ServiceName}Service
) {
    @GetMapping
    suspend fun getAll(
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<{ResourceDto}> = service.findAll(pageable)
    
    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: UUID): {ResourceDto} = 
        service.findById(id) ?: throw NotFoundException("Resource not found")
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@Valid @RequestBody request: Create{Resource}Request): {ResourceDto} = 
        service.create(request)
    
    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: Update{Resource}Request
    ): {ResourceDto} = service.update(id, request)
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: UUID) = service.delete(id)
}
```

#### Service Implementation
```kotlin
@Service
class {ServiceName}ServiceImpl(
    private val repository: {ServiceName}Repository,
    private val mapper: {ServiceName}Mapper,
    private val redisTemplate: RedisTemplate<String, String>
) : {ServiceName}Service {
    
    @Transactional(readOnly = true)
    override suspend fun findAll(pageable: Pageable): Page<{ResourceDto}> = 
        withContext(Dispatchers.IO) {
            repository.findAll(pageable).map(mapper::toDto)
        }
    
    @Transactional
    override suspend fun create(request: Create{Resource}Request): {ResourceDto} = 
        withContext(Dispatchers.IO) {
            val entity = mapper.toEntity(request)
            val saved = repository.save(entity)
            publishEvent({Resource}CreatedEvent(saved.id))
            mapper.toDto(saved)
        }
    
    private suspend fun publishEvent(event: Any) {
        // Publish to Redis or Spring Events
    }
}
```

#### JPA Repository
```kotlin
@Repository
interface {ServiceName}Repository : JpaRepository<{Entity}, UUID> {
    fun findByNameIgnoreCase(name: String): Optional<{Entity}>
    
    @Query("SELECT e FROM {Entity} e WHERE e.status = :status")
    fun findByStatus(@Param("status") status: Status): List<{Entity}>
    
    @Modifying
    @Query("UPDATE {Entity} e SET e.status = :status WHERE e.id = :id")
    fun updateStatus(@Param("id") id: UUID, @Param("status") status: Status)
}
```

#### MapStruct Mapper
```kotlin
@Mapper(componentModel = "spring")
interface {ServiceName}Mapper {
    fun toDto(entity: {Entity}): {ResourceDto}
    fun toEntity(dto: {ResourceDto}): {Entity}
    fun toEntity(request: Create{Resource}Request): {Entity}
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateEntity(request: Update{Resource}Request, @MappingTarget entity: {Entity})
}
```

### Step 3: Configuration

#### Application Configuration
```yaml
spring:
  application:
    name: {service-name}
  
  datasource:
    url: jdbc:postgresql://localhost:5432/tia_etl
    username: ${DB_USERNAME:tia_user}
    password: ${DB_PASSWORD:tia_pass}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8

server:
  port: ${SERVICE_PORT:8080}
  
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Gradle Build Configuration
```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core:4.30.0")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}
```

### Step 4: Testing

#### Unit Test Template
```kotlin
@ExtendWith(MockKExtension::class)
class {ServiceName}ServiceTest {
    
    @MockK
    private lateinit var repository: {ServiceName}Repository
    
    @MockK
    private lateinit var mapper: {ServiceName}Mapper
    
    @InjectMockKs
    private lateinit var service: {ServiceName}ServiceImpl
    
    @Test
    fun `should create resource successfully`() = runTest {
        // Given
        val request = Create{Resource}Request(name = "Test")
        val entity = {Entity}(name = "Test")
        val dto = {ResourceDto}(id = UUID.randomUUID(), name = "Test")
        
        every { mapper.toEntity(request) } returns entity
        every { repository.save(entity) } returns entity
        every { mapper.toDto(entity) } returns dto
        
        // When
        val result = service.create(request)
        
        // Then
        assertEquals(dto.name, result.name)
        verify { repository.save(entity) }
    }
}
```

#### Integration Test Template
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class {ServiceName}IntegrationTest {
    
    @Container
    val postgres = PostgreSQLContainer("postgres:16-alpine")
    
    @Container
    val redis = GenericContainer("redis:7-alpine").withExposedPorts(6379)
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should handle complete workflow`() {
        mockMvc.post("/api/v1/{resource}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Test Resource"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Test Resource") }
        }
    }
    
    @DynamicPropertySource
    companion object {
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.redis.host", redis::getHost)
            registry.add("spring.redis.port", redis::getFirstMappedPort)
        }
    }
}
```

### Step 5: Health Check & Metrics

#### Health Indicator
```kotlin
@Component
class {ServiceName}HealthIndicator(
    private val redisTemplate: RedisTemplate<String, String>,
    private val dataSource: DataSource
) : HealthIndicator {
    
    override fun health(): Health = try {
        val redisHealth = checkRedis()
        val dbHealth = checkDatabase()
        
        if (redisHealth && dbHealth) {
            Health.up()
                .withDetail("redis", "Connected")
                .withDetail("database", "Connected")
                .build()
        } else {
            Health.down()
                .withDetail("redis", if (redisHealth) "Connected" else "Disconnected")
                .withDetail("database", if (dbHealth) "Connected" else "Disconnected")
                .build()
        }
    } catch (e: Exception) {
        Health.down(e).build()
    }
    
    private fun checkRedis(): Boolean = try {
        redisTemplate.connectionFactory?.connection?.ping() != null
    } catch (e: Exception) {
        false
    }
    
    private fun checkDatabase(): Boolean = try {
        dataSource.connection.use { it.isValid(2) }
    } catch (e: Exception) {
        false
    }
}
```

## Usage

To create a new service, run:
```bash
/create-service service-name
```

This will generate:
1. Complete service structure
2. All necessary Kotlin files
3. Configuration files
4. Test templates
5. Build configuration

## Checklist

When creating a service, ensure:
- [ ] Service follows TIA ETL architecture
- [ ] Uses Kotlin coroutines for async operations
- [ ] Implements proper error handling
- [ ] Includes health checks
- [ ] Has comprehensive tests
- [ ] Follows MapStruct for DTO mapping
- [ ] Uses Redis for distributed coordination
- [ ] Implements proper logging
- [ ] Includes OpenAPI documentation
- [ ] Follows security best practices