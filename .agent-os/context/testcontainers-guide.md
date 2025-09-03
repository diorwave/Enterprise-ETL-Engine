# Testcontainers Guide for TIA ETL System

## Overview
Testcontainers is a Java library that provides lightweight, throwaway instances of common databases, message brokers, and other services that can run in Docker containers. This guide covers best practices for using Testcontainers in the TIA ETL System.

## Dependencies

### Gradle Configuration
```kotlin
dependencies {
    // Testcontainers BOM
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.3"))
    
    // Core Testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    
    // Database containers
    testImplementation("org.testcontainers:postgresql")
    
    // Redis container (use generic container)
    testImplementation("org.testcontainers:testcontainers")
    
    // Kafka container (if needed)
    testImplementation("org.testcontainers:kafka")
}
```

## Container Configurations

### PostgreSQL Container
```kotlin
companion object {
    @Container
    @JvmStatic
    val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("init-test.sql") // Optional
}
```

### Redis Container
```kotlin
companion object {
    @Container
    @JvmStatic
    val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withCommand("redis-server", "--requirepass", "test")
}
```

## Test Configuration Patterns

### 1. Base Integration Test Class
```kotlin
package com.quantumsoft.tia.scanner.integration

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseIntegrationTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.liquibase.enabled") { "false" }
        }
    }
}
```

### 2. Base E2E Test Class
```kotlin
package com.quantumsoft.tia.scanner.e2e

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "logging.level.com.quantumsoft.tia=DEBUG"
    ]
)
@Testcontainers
@ActiveProfiles("test")
abstract class BaseE2ETest {

    companion object {
        
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("tia_test")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort())
        
        @Container
        @JvmStatic
        val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL configuration
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            
            // Redis configuration  
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
            registry.add("spring.data.redis.timeout") { "5000ms" }
            
            // Disable components that might cause issues in tests
            registry.add("spring.quartz.auto-startup") { "false" }
            registry.add("spring.task.scheduling.pool.size") { "1" }
        }
    }
}
```

### 3. Disabling Problematic Components for Tests

Create a test configuration to disable Quartz and other components:

```kotlin
package com.quantumsoft.tia.scanner.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.quartz.Scheduler
import org.mockito.Mockito

@TestConfiguration
@Profile("test")
class TestConfig {
    
    @Bean
    @Primary
    fun mockScheduler(): Scheduler = Mockito.mock(Scheduler::class.java)
    
    @Bean
    @Primary
    fun testJobScheduler(): JobScheduler = Mockito.mock(JobScheduler::class.java)
}
```

## Best Practices

### 1. Container Lifecycle Management
- Use `@Container` annotation for automatic lifecycle management
- Use `@JvmStatic` for companion object containers to share across tests
- Always specify container versions explicitly (e.g., `postgres:15-alpine`)

### 2. Waiting Strategies
```kotlin
// Wait for port
.waitingFor(Wait.forListeningPort())

// Wait for log message
.waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))

// Wait for HTTP endpoint
.waitingFor(Wait.forHttp("/health").forStatusCode(200))
```

### 3. Network Configuration
```kotlin
// Create a network for containers to communicate
val network = Network.newNetwork()

val postgres = PostgreSQLContainer("postgres:15-alpine")
    .withNetwork(network)
    .withNetworkAliases("postgres")

val app = GenericContainer("my-app:latest")
    .withNetwork(network)
    .withEnv("DB_HOST", "postgres")
```

### 4. Resource Cleanup
```kotlin
@AfterEach
fun cleanup() {
    // Clean up test data
    repository.deleteAll()
}

@AfterAll
companion object {
    @JvmStatic
    fun tearDown() {
        // Containers are automatically stopped by Testcontainers
        // Add any additional cleanup if needed
    }
}
```

### 5. Parallel Test Execution
```kotlin
// In build.gradle.kts
tasks.test {
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}
```

## Common Issues and Solutions

### Issue 1: Quartz Configuration in Tests
**Problem**: Quartz tries to create database tables in test environment
**Solution**: Disable Quartz auto-startup or provide mock implementation

```kotlin
// In application-test.yml
spring:
  quartz:
    auto-startup: false
    job-store-type: memory
```

### Issue 2: Port Conflicts
**Problem**: Fixed ports cause conflicts when running tests in parallel
**Solution**: Use random ports with getMappedPort()

```kotlin
// DON'T DO THIS
val redis = GenericContainer("redis:7-alpine")
    .withExposedPorts(6379)
    .withFixedExposedPort(6379, 6379) // BAD!

// DO THIS
val redis = GenericContainer("redis:7-alpine")
    .withExposedPorts(6379)
// Access with: redis.getMappedPort(6379)
```

### Issue 3: Container Startup Timeouts
**Problem**: Containers take too long to start
**Solution**: Increase timeout and use appropriate wait strategies

```kotlin
val postgres = PostgreSQLContainer("postgres:15-alpine")
    .withStartupTimeout(Duration.ofMinutes(2))
    .waitingFor(
        Wait.forLogMessage(".*database system is ready.*", 1)
            .withStartupTimeout(Duration.ofMinutes(2))
    )
```

### Issue 4: Docker Not Available
**Problem**: Docker daemon not running or not accessible
**Solution**: Check Docker installation and permissions

```kotlin
@BeforeAll
@JvmStatic
fun checkDocker() {
    try {
        DockerClientFactory.instance().client()
    } catch (e: Exception) {
        throw IllegalStateException("Docker is not available. Please start Docker daemon.", e)
    }
}
```

## Testing Patterns

### 1. Repository Integration Test
```kotlin
@DataJpaTest
class ScanJobRepositoryIntegrationTest : BaseIntegrationTest() {
    
    @Autowired
    private lateinit var repository: ScanJobRepository
    
    @Test
    fun `should save and retrieve entity`() {
        // Given
        val entity = createTestEntity()
        
        // When
        val saved = repository.save(entity)
        val retrieved = repository.findById(saved.id)
        
        // Then
        assertThat(retrieved).isPresent
        assertThat(retrieved.get()).isEqualTo(saved)
    }
}
```

### 2. Service Integration Test
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class FileServiceIntegrationTest : BaseE2ETest() {
    
    @Autowired
    private lateinit var service: FileService
    
    @Test
    fun `should process file end-to-end`() {
        // Test complete workflow with real database and Redis
    }
}
```

### 3. REST API E2E Test
```kotlin
@AutoConfigureMockMvc
class JobControllerE2ETest : BaseE2ETest() {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should create job via REST API`() {
        mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Test Job"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").exists())
    }
}
```

## Performance Optimization

### 1. Reuse Containers
```kotlin
// In application-test.yml
testcontainers:
  reuse:
    enable: true

// In .testcontainers.properties
testcontainers.reuse.enable=true
```

### 2. Use Lightweight Images
- `postgres:15-alpine` instead of `postgres:15`
- `redis:7-alpine` instead of `redis:7`
- `eclipse-temurin:17-jre-alpine` for Java apps

### 3. Parallel Container Startup
```kotlin
companion object {
    @JvmStatic
    val containers = listOf(
        PostgreSQLContainer("postgres:15-alpine"),
        GenericContainer("redis:7-alpine")
    ).apply {
        forEach { it.start() } // Start all containers in parallel
    }
}
```

## Debugging

### 1. Enable Container Logs
```kotlin
val container = PostgreSQLContainer("postgres:15-alpine")
    .withLogConsumer { frame ->
        println(frame.utf8String)
    }
```

### 2. Keep Containers Running After Test
```kotlin
// Set breakpoint after test
// Containers will stay running until debugger continues
```

### 3. Access Container Shell
```kotlin
@Test
fun debugContainer() {
    val result = container.execInContainer("sh", "-c", "ps aux")
    println(result.stdout)
}
```

## CI/CD Considerations

### GitHub Actions
```yaml
- name: Start Docker
  run: |
    docker info
    
- name: Run Tests
  run: ./gradlew test -Pprofile=integration
```

### GitLab CI
```yaml
services:
  - docker:dind

variables:
  DOCKER_HOST: tcp://docker:2375
  DOCKER_DRIVER: overlay2
```

## Conclusion
Testcontainers provides reliable, isolated test environments for integration and E2E testing. Follow these patterns and best practices to ensure stable and maintainable tests in the TIA ETL System.