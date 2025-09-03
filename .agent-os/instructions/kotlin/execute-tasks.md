# Execute Tasks for Kotlin Spring Boot Services

## Overview
Execute implementation tasks for TIA ETL System using Kotlin, Spring Boot, and associated technologies.

## Execution Process

### Pre-Execution Checklist
Before executing any task:
1. ✅ Verify spec exists in `.agent-os/specs/`
2. ✅ Review `.agent-os/product/tech-stack.md` for technology requirements
3. ✅ Check `.agent-os/standards/kotlin-conventions.md` for coding standards
4. ✅ Ensure development environment has required dependencies

### Task Execution Flow

## Step 1: Service Implementation

### For Backend Services (Kotlin/Spring Boot)

#### 1.1 Create Service Structure
```bash
# Create service directory
mkdir -p services/{service-name}/src/main/kotlin/com/tia/etl/{service-name}
mkdir -p services/{service-name}/src/main/resources
mkdir -p services/{service-name}/src/test/kotlin/com/tia/etl/{service-name}
```

#### 1.2 Generate Build Configuration
Create `build.gradle.kts`:
```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.tia.etl"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
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
    implementation("com.clickhouse:clickhouse-jdbc:0.7.1")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

#### 1.3 Implement Core Components

##### Application Entry Point
```kotlin
package com.tia.etl.{service_name}

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAsync
@EnableScheduling
class {ServiceName}Application

fun main(args: Array<String>) {
    runApplication<{ServiceName}Application>(*args)
}
```

##### Configuration Classes
```kotlin
package com.tia.etl.{service_name}.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }
}
```

## Step 2: Database Implementation

### 2.1 Create Liquibase Migrations
Location: `src/main/resources/db/changelog/`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.30.xsd">
    
    <changeSet id="001-create-{table}" author="tia-system">
        <createTable tableName="{table_name}">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
        
        <createIndex tableName="{table_name}" indexName="idx_{table}_name">
            <column name="name"/>
        </createIndex>
    </changeSet>
</databaseChangeLog>
```

### 2.2 Create JPA Entities
```kotlin
package com.tia.etl.{service_name}.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "{table_name}")
class {Entity}(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    
    @Version
    var version: Long = 0
)
```

## Step 3: API Implementation

### 3.1 Create DTOs
```kotlin
package com.tia.etl.{service_name}.domain.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class {Resource}Dto(
    val id: UUID,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Create{Resource}Request(
    @field:NotBlank(message = "Name is required")
    val name: String
)

data class Update{Resource}Request(
    @field:NotBlank(message = "Name is required")
    val name: String
)
```

### 3.2 Implement Repository
```kotlin
package com.tia.etl.{service_name}.repository

import com.tia.etl.{service_name}.domain.entity.{Entity}
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface {ServiceName}Repository : JpaRepository<{Entity}, UUID> {
    fun findByNameIgnoreCase(name: String): Optional<{Entity}>
}
```

### 3.3 Implement Service
Use template from `.agent-os/templates/kotlin/service-impl.kt.template`

### 3.4 Implement Controller
Use template from `.agent-os/templates/kotlin/controller.kt.template`

## Step 4: Testing Implementation

### 4.1 Unit Tests
```kotlin
package com.tia.etl.{service_name}.service

import com.tia.etl.{service_name}.repository.{ServiceName}Repository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class {ServiceName}ServiceTest {
    
    private val repository = mockk<{ServiceName}Repository>()
    private val service = {ServiceName}ServiceImpl(repository, mapper, redisTemplate)
    
    @Test
    fun `should create entity successfully`() = runTest {
        // Given
        val request = Create{Resource}Request(name = "Test")
        every { repository.save(any()) } returns testEntity
        
        // When
        val result = service.create(request)
        
        // Then
        assertNotNull(result)
        assertEquals("Test", result.name)
        verify(exactly = 1) { repository.save(any()) }
    }
}
```

### 4.2 Integration Tests
```kotlin
package com.tia.etl.{service_name}

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class {ServiceName}IntegrationTest {
    
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should create resource via API`() {
        mockMvc.post("/api/v1/{resource}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Test Resource"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Test Resource") }
        }
    }
}
```

## Step 5: Configuration & Deployment

### 5.1 Application Configuration
```yaml
spring:
  application:
    name: {service-name}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/tia_etl}
    username: ${DATABASE_USER:tia_user}
    password: ${DATABASE_PASSWORD:tia_pass}
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

server:
  port: ${SERVER_PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
```

### 5.2 Docker Configuration
```dockerfile
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata
ENV TZ=UTC

WORKDIR /app

COPY build/libs/{service-name}-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Step 6: Documentation

### 6.1 OpenAPI Annotations
```kotlin
@Operation(
    summary = "Create a new {resource}",
    description = "Creates a new {resource} in the system"
)
@ApiResponses(
    ApiResponse(responseCode = "201", description = "Successfully created"),
    ApiResponse(responseCode = "400", description = "Invalid request"),
    ApiResponse(responseCode = "409", description = "Resource already exists")
)
@PostMapping
suspend fun create(@Valid @RequestBody request: Create{Resource}Request): {Resource}Dto
```

### 6.2 README Documentation
Create comprehensive README.md with:
- Service description
- API endpoints
- Configuration options
- Development setup
- Testing instructions
- Deployment guide

## Execution Checklist

### Pre-Implementation
- [ ] Review spec document
- [ ] Verify tech stack compatibility
- [ ] Check existing code patterns
- [ ] Plan database schema

### Implementation
- [ ] Create service structure
- [ ] Implement domain models
- [ ] Create database migrations
- [ ] Implement repository layer
- [ ] Implement service layer
- [ ] Create REST controllers
- [ ] Add validation
- [ ] Implement error handling

### Testing
- [ ] Write unit tests (80% coverage)
- [ ] Write integration tests
- [ ] Test with Testcontainers
- [ ] Performance testing if needed

### Documentation
- [ ] Add OpenAPI annotations
- [ ] Create README
- [ ] Document configuration
- [ ] Add code comments where needed

### Quality Assurance
- [ ] Run linting (detekt)
- [ ] Format code (ktlint)
- [ ] Check for security issues
- [ ] Review error messages
- [ ] Verify logging

### Deployment
- [ ] Create Dockerfile
- [ ] Configure environment variables
- [ ] Setup health checks
- [ ] Create deployment scripts

## Common Patterns

### Error Handling
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                error = "NOT_FOUND",
                message = e.message ?: "Entity not found"
            ))
    }
}
```

### Distributed Locking
```kotlin
fun acquireLock(key: String, ttl: Duration): Boolean {
    return redisTemplate.opsForValue()
        .setIfAbsent("lock:$key", instanceId, ttl) ?: false
}
```

### Event Publishing
```kotlin
fun publishEvent(event: DomainEvent) {
    val json = objectMapper.writeValueAsString(event)
    redisTemplate.convertAndSend("tia.events", json)
}
```

## Troubleshooting

### Common Issues
1. **Dependency conflicts**: Check version compatibility in build.gradle.kts
2. **Database connection**: Verify PostgreSQL/ClickHouse configuration
3. **Redis connection**: Check Redis host and port
4. **Test failures**: Ensure Testcontainers Docker is running
5. **Build failures**: Verify Kotlin and Java versions

### Debug Commands
```bash
# Run with debug logging
./gradlew bootRun --debug

# Run specific test
./gradlew test --tests "*ServiceTest"

# Check dependencies
./gradlew dependencies

# Clean and rebuild
./gradlew clean build
```