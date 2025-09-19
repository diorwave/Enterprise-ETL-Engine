package com.quantumsoft.tia.scanner.integration

import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.entities.ScanJob
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import com.quantumsoft.tia.scanner.config.TestConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfiguration::class)
class ScanJobRepositoryIntegrationTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
        
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.properties.hibernate.dialect") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.liquibase.enabled") { "false" }
        }
    }
    
    @Autowired
    private lateinit var scanJobRepository: ScanJobRepository
    
    @BeforeEach
    fun setUp() {
        scanJobRepository.deleteAll()
    }
    
    @Test
    fun `should persist and retrieve scan job`() {
        // Given
        val scanJob = ScanJob(
            name = "test-job-${UUID.randomUUID()}",
            description = "Test job description",
            sourceDirectory = "/test/directory",
            filePattern = "*.asn1",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */5 * * * *",
            parserId = "test-parser",
            isActive = true
        )
        
        // When
        val saved = scanJobRepository.save(scanJob)
        val retrieved = scanJobRepository.findById(saved.id).orElse(null)
        
        // Then
        assertThat(retrieved).isNotNull
        assertThat(retrieved.name).isEqualTo(scanJob.name)
        assertThat(retrieved.sourceDirectory).isEqualTo(scanJob.sourceDirectory)
    }
    
    @Test
    fun `should find jobs with filters`() {
        // Given
        val activeJob = ScanJob(
            name = "active-job-${UUID.randomUUID()}",
            description = "Active job",
            sourceDirectory = "/active",
            filePattern = "*.csv",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT5M",
            parserId = "csv-parser",
            isActive = true
        )
        
        val inactiveJob = ScanJob(
            name = "inactive-job-${UUID.randomUUID()}",
            description = "Inactive job",
            sourceDirectory = "/inactive",
            filePattern = "*.asn1",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 0 * * * *",
            parserId = "asn1-parser",
            isActive = false
        )
        
        scanJobRepository.save(activeJob)
        scanJobRepository.save(inactiveJob)
        
        // When
        val activeJobs = scanJobRepository.findByIsActive(true, PageRequest.of(0, 10))
        val csvParserJobs = scanJobRepository.findJobsWithFilters(null, "csv-parser", PageRequest.of(0, 10))
        
        // Then
        assertThat(activeJobs.content).hasSize(1)
        assertThat(activeJobs.content[0].name).isEqualTo(activeJob.name)
        assertThat(csvParserJobs.content).hasSize(1)
        assertThat(csvParserJobs.content[0].parserId).isEqualTo("csv-parser")
    }
    
    @Test
    fun `should enforce unique job names`() {
        // Given
        val jobName = "unique-job-${UUID.randomUUID()}"
        val job1 = ScanJob(
            name = jobName,
            description = "First job",
            sourceDirectory = "/dir1",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT10M",
            parserId = "parser1",
            isActive = true
        )
        
        // When
        scanJobRepository.save(job1)
        val existsByName = scanJobRepository.existsByName(jobName)
        
        // Then
        assertThat(existsByName).isTrue()
    }
    
    @Test
    fun `should count active jobs by parser`() {
        // Given
        val parser1Jobs = (1..3).map {
            ScanJob(
                name = "parser1-job-$it-${UUID.randomUUID()}",
                description = "Job $it",
                sourceDirectory = "/dir$it",
                filePattern = "*.txt",
                scanIntervalType = ScanIntervalType.FIXED,
                scanIntervalValue = "PT${it}M",
                parserId = "parser1",
                isActive = true
            )
        }
        
        val parser2Jobs = (1..2).map {
            ScanJob(
                name = "parser2-job-$it-${UUID.randomUUID()}",
                description = "Job $it",
                sourceDirectory = "/dir$it",
                filePattern = "*.csv",
                scanIntervalType = ScanIntervalType.CRON,
                scanIntervalValue = "0 * * * * *",
                parserId = "parser2",
                isActive = true
            )
        }
        
        scanJobRepository.saveAll(parser1Jobs + parser2Jobs)
        
        // When
        val parser1Count = scanJobRepository.countActiveJobsByParser("parser1")
        val parser2Count = scanJobRepository.countActiveJobsByParser("parser2")
        
        // Then
        assertThat(parser1Count).isEqualTo(3)
        assertThat(parser2Count).isEqualTo(2)
    }
    
    @Test
    fun `should query active jobs efficiently`() {
        // Given
        val jobs = (1..20).map { i ->
            ScanJob(
                name = "job-$i-${UUID.randomUUID()}",
                description = "Job $i",
                sourceDirectory = "/dir$i",
                filePattern = if (i % 2 == 0) "*.csv" else "*.asn1",
                scanIntervalType = if (i % 3 == 0) ScanIntervalType.CRON else ScanIntervalType.FIXED,
                scanIntervalValue = if (i % 3 == 0) "0 * * * * *" else "PT${i}M",
                parserId = "parser-${i % 3}",
                isActive = i % 2 == 0,
                priority = i % 5
            )
        }
        
        scanJobRepository.saveAll(jobs)
        
        // When
        val activeJobs = scanJobRepository.findByIsActive(true, PageRequest.of(0, 5))
        
        // Then
        assertThat(activeJobs.content).hasSize(5)
        assertThat(activeJobs.totalElements).isEqualTo(10) // Half of 20 are active
        assertThat(activeJobs.content).allMatch { it.isActive }
    }
}