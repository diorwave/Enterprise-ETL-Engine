package com.quantumsoft.tia.scanner.services

import com.quantumsoft.tia.scanner.dto.*
import com.quantumsoft.tia.scanner.entities.*
import com.quantumsoft.tia.scanner.repositories.*
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.*

class ScanJobServiceTest {

    private lateinit var scanJobRepository: ScanJobRepository
    private lateinit var executionRepository: ScanJobExecutionRepository
    private lateinit var scannedFileRepository: ScannedFileRepository
    private lateinit var scanJobService: ScanJobService
    private lateinit var jobScheduler: com.quantumsoft.tia.scanner.scheduler.JobScheduler
    
    @BeforeEach
    fun setUp() {
        scanJobRepository = mockk(relaxed = true)
        executionRepository = mockk(relaxed = true)
        scannedFileRepository = mockk(relaxed = true)
        jobScheduler = mockk(relaxed = true)
        scanJobService = ScanJobService(scanJobRepository, executionRepository, scannedFileRepository, jobScheduler)
    }
    
    @Test
    fun `should find jobs with filters`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val scanJob = createScanJob()
        val page = PageImpl(listOf(scanJob))
        
        every { scanJobRepository.findJobsWithFilters(true, "test-parser", pageable) } returns page
        // Mock calculateSimpleStats dependencies (for toDto call)
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.COMPLETED) } returns 0L
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.FAILED) } returns 0L
        every { executionRepository.getAverageExecutionDuration(any(), any()) } returns null
        every { executionRepository.findTopByScanJobIdOrderByStartedAtDesc(any()) } returns Optional.empty()
        
        // When
        val result = scanJobService.findJobs(true, "test-parser", pageable)
        
        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().name).isEqualTo(scanJob.name)
    }
    
    @Test
    fun `should find job by id`() {
        // Given
        val scanJob = createScanJob()
        val execution = ScanJobExecution(
            scanJob = scanJob,
            instanceId = "test-instance",
            status = ExecutionStatus.COMPLETED,
            completedAt = Instant.now()
        )
        val jobWithExecution = scanJob.copy(executions = listOf(execution))
        
        every { scanJobRepository.findByIdWithLatestExecution(scanJob.id) } returns Optional.of(jobWithExecution)
        // Mock calculateSimpleStats dependencies
        every { scannedFileRepository.countByJobAndStatus(scanJob.id, FileStatus.COMPLETED) } returns 100L
        every { scannedFileRepository.countByJobAndStatus(scanJob.id, FileStatus.FAILED) } returns 5L
        every { executionRepository.getAverageExecutionDuration(scanJob.id, any()) } returns 30000.0
        every { executionRepository.findTopByScanJobIdOrderByStartedAtDesc(scanJob.id) } returns Optional.of(execution)
        
        // When
        val result = scanJobService.findById(scanJob.id)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo(scanJob.name)
        assertThat(result?.lastExecution).isNotNull()
    }
    
    @Test
    fun `should return null when job not found`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scanJobRepository.findByIdWithLatestExecution(jobId) } returns Optional.empty()
        
        // When
        val result = scanJobService.findById(jobId)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `should create job successfully`() {
        // Given
        val request = CreateScanJobRequest(
            name = "new-job",
            sourceDirectory = "/new/dir",
            filePattern = "*.log",
            scanInterval = ScanIntervalDto(ScanIntervalType.CRON, "0 * * * * *"),
            parserId = "log-parser"
        )
        
        every { scanJobRepository.existsByName(request.name) } returns false
        every { scanJobRepository.save(any()) } answers { firstArg() }
        // Mock calculateSimpleStats dependencies (for toDto call)
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.COMPLETED) } returns 0L
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.FAILED) } returns 0L
        every { executionRepository.getAverageExecutionDuration(any(), any()) } returns null
        every { executionRepository.findTopByScanJobIdOrderByStartedAtDesc(any()) } returns Optional.empty()
        
        // When
        val result = scanJobService.createJob(request)
        
        // Then
        assertThat(result.name).isEqualTo(request.name)
        assertThat(result.sourceDirectory).isEqualTo(request.sourceDirectory)
        verify { scanJobRepository.save(any()) }
    }
    
    @Test
    fun `should throw exception when creating job with duplicate name`() {
        // Given
        val request = CreateScanJobRequest(
            name = "duplicate-job",
            sourceDirectory = "/dir",
            filePattern = "*.txt",
            scanInterval = ScanIntervalDto(ScanIntervalType.FIXED, "PT5M"),
            parserId = "parser"
        )
        
        every { scanJobRepository.existsByName(request.name) } returns true
        
        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            scanJobService.createJob(request)
        }
        assertThat(exception.message).contains("already exists")
    }
    
    @Test
    fun `should update job successfully`() {
        // Given
        val scanJob = createScanJob()
        val request = UpdateScanJobRequest(
            name = "updated-name",
            filePattern = "*.updated",
            isActive = false
        )
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        every { scanJobRepository.existsByName("updated-name") } returns false
        every { scanJobRepository.save(any()) } answers { firstArg() }
        // Mock calculateSimpleStats dependencies (for toDto call)
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.COMPLETED) } returns 0L
        every { scannedFileRepository.countByJobAndStatus(any(), FileStatus.FAILED) } returns 0L
        every { executionRepository.getAverageExecutionDuration(any(), any()) } returns null
        every { executionRepository.findTopByScanJobIdOrderByStartedAtDesc(any()) } returns Optional.empty()
        
        // When
        val result = scanJobService.updateJob(scanJob.id, request)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("updated-name")
        assertThat(result?.filePattern).isEqualTo("*.updated")
        assertThat(result?.isActive).isFalse()
    }
    
    @Test
    fun `should return null when updating non-existent job`() {
        // Given
        val jobId = UUID.randomUUID()
        val request = UpdateScanJobRequest(name = "new-name")
        
        every { scanJobRepository.findById(jobId) } returns Optional.empty()
        
        // When
        val result = scanJobService.updateJob(jobId, request)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `should delete job successfully`() {
        // Given
        val scanJob = createScanJob()
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        every { executionRepository.hasRunningExecution(scanJob.id) } returns false
        every { scanJobRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = scanJobService.deleteJob(scanJob.id)
        
        // Then
        assertThat(result).isTrue()
        verify { scanJobRepository.save(match { !it.isActive }) }
    }
    
    @Test
    fun `should not delete job with running execution`() {
        // Given
        val scanJob = createScanJob()
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        every { executionRepository.hasRunningExecution(scanJob.id) } returns true
        
        // When/Then
        assertThrows<IllegalStateException> {
            scanJobService.deleteJob(scanJob.id)
        }
    }
    
    @Test
    fun `should trigger scan successfully`() {
        // Given
        val scanJob = createScanJob()
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        every { executionRepository.hasRunningExecution(scanJob.id) } returns false
        every { executionRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = scanJobService.triggerScan(scanJob.id, false)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.status).isEqualTo(ExecutionStatus.RUNNING)
        assertThat(result?.message).isEqualTo("Scan initiated")
    }
    
    @Test
    fun `should not trigger scan for inactive job without force`() {
        // Given
        val scanJob = createScanJob().copy(isActive = false)
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        
        // When/Then
        assertThrows<IllegalStateException> {
            scanJobService.triggerScan(scanJob.id, false)
        }
    }
    
    @Test
    fun `should trigger scan for inactive job with force`() {
        // Given
        val scanJob = createScanJob().copy(isActive = false)
        
        every { scanJobRepository.findById(scanJob.id) } returns Optional.of(scanJob)
        every { executionRepository.hasRunningExecution(scanJob.id) } returns false
        every { executionRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = scanJobService.triggerScan(scanJob.id, true)
        
        // Then
        assertThat(result).isNotNull()
    }
    
    @Test
    fun `should get job executions`() {
        // Given
        val jobId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)
        val execution = ScanJobExecution(
            scanJob = createScanJob(),
            instanceId = "test",
            status = ExecutionStatus.COMPLETED,
            filesDiscovered = 100,
            filesQueued = 90,
            durationMs = 5000
        )
        
        every { executionRepository.findByScanJobIdOrderByStartedAtDesc(jobId, pageable) } returns PageImpl(listOf(execution))
        
        // When
        val result = scanJobService.getExecutions(jobId, pageable)
        
        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().status).isEqualTo(ExecutionStatus.COMPLETED)
    }
    
    @Test
    fun `should get job statistics`() {
        // Given
        val jobId = UUID.randomUUID()
        
        every { scanJobRepository.existsById(jobId) } returns true
        every { executionRepository.getExecutionStatisticsByJob(jobId) } returns listOf(
            arrayOf(ExecutionStatus.COMPLETED, 10L, 100.0, 90.0, 5000.0),
            arrayOf(ExecutionStatus.FAILED, 2L, 50.0, 0.0, 3000.0)
        )
        every { scannedFileRepository.getStatusCountsByJob(jobId) } returns listOf(
            arrayOf(FileStatus.COMPLETED, 900L),
            arrayOf(FileStatus.FAILED, 100L)
        )
        
        // When
        val result = scanJobService.getJobStatistics(jobId)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.totalExecutions).isEqualTo(12)
        assertThat(result?.successfulExecutions).isEqualTo(10)
        assertThat(result?.failedExecutions).isEqualTo(2)
        assertThat(result?.fileStatusDistribution).containsEntry(FileStatus.COMPLETED, 900L)
    }
    
    @Test
    fun `should return null statistics for non-existent job`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scanJobRepository.existsById(jobId) } returns false
        
        // When
        val result = scanJobService.getJobStatistics(jobId)
        
        // Then
        assertThat(result).isNull()
    }
    
    private fun createScanJob() = ScanJob(
        id = UUID.randomUUID(),
        name = "test-job",
        sourceDirectory = "/test/dir",
        filePattern = "*.txt",
        scanIntervalType = ScanIntervalType.CRON,
        scanIntervalValue = "0 * * * * *",
        parserId = "test-parser",
        isActive = true
    )
}
