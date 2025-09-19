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
import java.time.Duration

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "spring.quartz.auto-startup=false",
        "spring.task.scheduling.pool.size=1",
        "logging.level.com.quantumsoft.tia=DEBUG",
        "logging.level.org.springframework.web=INFO"
    ]
)
//@SpringBootTest(
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//    properties = [
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.liquibase.enabled=false",
//        "spring.quartz.auto-startup=false",
//        "spring.task.scheduling.pool.size=1",
//        "logging.level.com.quantumsoft.tia=DEBUG",
//        "logging.level.org.springframework.web=INFO"
//    ]
//)
@Testcontainers
@ActiveProfiles("e2e", "test")
abstract class BaseE2ETest {

    companion object {
        
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("tia_e2e_test")
            .withUsername("test")
            .withPassword("test123")
            .withStartupTimeout(Duration.ofMinutes(2))
            .waitingFor(
                Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2)
                    .withStartupTimeout(Duration.ofMinutes(2))
            )
        
        @Container
        @JvmStatic
        val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(Duration.ofMinutes(1))
            .waitingFor(Wait.forListeningPort())

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL configuration
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.properties.hibernate.dialect") { "org.hibernate.dialect.PostgreSQLDialect" }
            
            // Redis configuration  
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
            registry.add("spring.data.redis.database") { "0" }
            registry.add("spring.data.redis.timeout") { "5000ms" }
            
            // Disable Quartz for tests
            registry.add("spring.quartz.job-store-type") { "memory" }
            registry.add("spring.quartz.properties.org.quartz.scheduler.instanceName") { "TestScheduler" }
            registry.add("spring.quartz.properties.org.quartz.scheduler.instanceId") { "AUTO" }
            registry.add("spring.quartz.properties.org.quartz.threadPool.threadCount") { "1" }
        }
    }
}
