package com.quantumsoft.tia.scanner.integration

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
abstract class BaseTestcontainersTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:15-alpine")).apply {
            withDatabaseName("tia_etl_test")
            withUsername("test_user")
            withPassword("test_password")
            withInitScript("init-test-db.sql")
        }
        
        @Container
        @JvmStatic
        val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine")).apply {
            withExposedPorts(6379)
            withCommand("redis-server", "--maxmemory", "256mb", "--maxmemory-policy", "allkeys-lru")
        }
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            // PostgreSQL properties
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            
            // Redis properties
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
            
            // Liquibase properties
            registry.add("spring.liquibase.enabled") { "true" }
            registry.add("spring.liquibase.change-log") { "classpath:db/changelog/db.changelog-master.yaml" }
            
            // JPA properties for tests
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
            registry.add("spring.jpa.show-sql") { "true" }
            
            // Quartz properties for tests
            registry.add("spring.quartz.job-store-type") { "jdbc" }
            registry.add("spring.quartz.properties.org.quartz.jobStore.isClustered") { "false" }
        }
        
        @BeforeAll
        @JvmStatic
        fun checkContainers() {
            check(postgresContainer.isRunning) { "PostgreSQL container should be running" }
            check(redisContainer.isRunning) { "Redis container should be running" }
        }
    }
    
    protected fun clearRedisData() {
        redisContainer.execInContainer("redis-cli", "FLUSHALL")
    }
    
    protected fun getRedisValue(key: String): String? {
        val result = redisContainer.execInContainer("redis-cli", "GET", key)
        return if (result.exitCode == 0) result.stdout.trim() else null
    }
    
    protected fun setRedisValue(key: String, value: String, ttlSeconds: Int? = null) {
        if (ttlSeconds != null) {
            redisContainer.execInContainer("redis-cli", "SETEX", key, ttlSeconds.toString(), value)
        } else {
            redisContainer.execInContainer("redis-cli", "SET", key, value)
        }
    }
}