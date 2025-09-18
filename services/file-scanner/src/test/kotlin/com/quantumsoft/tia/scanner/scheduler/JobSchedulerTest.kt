package com.quantumsoft.tia.scanner.scheduler

import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.entities.ScanJob
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import java.util.*

class JobSchedulerTest {

    private lateinit var scheduler: Scheduler
    private lateinit var scanJobRepository: ScanJobRepository
    private lateinit var jobScheduler: JobScheduler
    
    @BeforeEach
    fun setUp() {
        scheduler = mockk(relaxed = true)
        scanJobRepository = mockk(relaxed = true)
        jobScheduler = JobScheduler(scheduler, scanJobRepository)
    }
    
    @Test
    fun `should schedule job with cron trigger`() {
        // Given
        val scanJob = ScanJob(
            id = UUID.randomUUID(),
            name = "test-job",
            sourceDirectory = "/test",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */5 * * * ?",
            parserId = "test-parser"
        )
        
        every { scheduler.checkExists(any<JobKey>()) } returns false
        
        // When
        jobScheduler.scheduleJob(scanJob)
        
        // Then
        verify { scheduler.scheduleJob(any(), any<CronTrigger>()) }
    }
    
    @Test
    fun `should schedule job with fixed interval trigger`() {
        // Given
        val scanJob = ScanJob(
            id = UUID.randomUUID(),
            name = "test-job",
            sourceDirectory = "/test",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT5M",
            parserId = "test-parser"
        )
        
        every { scheduler.checkExists(any<JobKey>()) } returns false
        
        // When
        jobScheduler.scheduleJob(scanJob)
        
        // Then
        verify { scheduler.scheduleJob(any(), any<SimpleTrigger>()) }
    }
    
    @Test
    fun `should replace existing job when scheduling`() {
        // Given
        val scanJob = ScanJob(
            id = UUID.randomUUID(),
            name = "test-job",
            sourceDirectory = "/test",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 0 * * * ?",
            parserId = "test-parser"
        )
        
        every { scheduler.checkExists(any<JobKey>()) } returns true
        
        // When
        jobScheduler.scheduleJob(scanJob)
        
        // Then
        verify { scheduler.deleteJob(any()) }
        verify { scheduler.scheduleJob(any(), any()) }
    }
    
    @Test
    fun `should handle scheduling failure`() {
        // Given
        val scanJob = ScanJob(
            id = UUID.randomUUID(),
            name = "test-job",
            sourceDirectory = "/test",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "invalid cron",
            parserId = "test-parser"
        )
        
        every { scheduler.scheduleJob(any(), any()) } throws SchedulerException("Invalid cron expression")
        
        // When/Then
        assertThrows<RuntimeException> {
            jobScheduler.scheduleJob(scanJob)
        }
    }
    
    @Test
    fun `should unschedule job`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scheduler.checkExists(any<JobKey>()) } returns true
        
        // When
        jobScheduler.unscheduleJob(jobId)
        
        // Then
        verify { scheduler.deleteJob(JobKey(jobId.toString(), JobScheduler.JOB_GROUP)) }
    }
    
    @Test
    fun `should handle unschedule of non-existent job`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scheduler.checkExists(any<JobKey>()) } returns false
        
        // When
        jobScheduler.unscheduleJob(jobId)
        
        // Then
        verify(exactly = 0) { scheduler.deleteJob(any()) }
    }
    
    @Test
    fun `should pause job`() {
        // Given
        val jobId = UUID.randomUUID()
        
        // When
        jobScheduler.pauseJob(jobId)
        
        // Then
        verify { scheduler.pauseJob(JobKey(jobId.toString(), JobScheduler.JOB_GROUP)) }
    }
    
    @Test
    fun `should resume job`() {
        // Given
        val jobId = UUID.randomUUID()
        
        // When
        jobScheduler.resumeJob(jobId)
        
        // Then
        verify { scheduler.resumeJob(JobKey(jobId.toString(), JobScheduler.JOB_GROUP)) }
    }
    
    @Test
    fun `should trigger job immediately`() {
        // Given
        val jobId = UUID.randomUUID()
        
        // When
        jobScheduler.triggerJobNow(jobId)
        
        // Then
        verify { scheduler.triggerJob(JobKey(jobId.toString(), JobScheduler.JOB_GROUP)) }
    }
    
    @Test
    fun `should handle trigger failure`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scheduler.triggerJob(any()) } throws SchedulerException("Job not found")
        
        // When/Then
        assertThrows<RuntimeException> {
            jobScheduler.triggerJobNow(jobId)
        }
    }
    
    @Test
    fun `should check if job is running`() {
        // Given
        val jobId = UUID.randomUUID()
        val jobKey = JobKey(jobId.toString(), JobScheduler.JOB_GROUP)
        
        val executingContext = mockk<JobExecutionContext>(relaxed = true)
        val jobDetail = mockk<JobDetail>(relaxed = true)
        
        every { jobDetail.key } returns jobKey
        every { executingContext.jobDetail } returns jobDetail
        every { scheduler.getCurrentlyExecutingJobs() } returns listOf(executingContext)
        
        // When
        val isRunning = jobScheduler.isJobRunning(jobId)
        
        // Then
        assertThat(isRunning).isTrue()
    }
    
    @Test
    fun `should return false when job is not running`() {
        // Given
        val jobId = UUID.randomUUID()
        every { scheduler.getCurrentlyExecutingJobs() } returns emptyList()
        
        // When
        val isRunning = jobScheduler.isJobRunning(jobId)
        
        // Then
        assertThat(isRunning).isFalse()
    }
    
    @Test
    fun `should reschedule all active jobs`() {
        // Given
        val activeJobs = listOf(
            ScanJob(
                id = UUID.randomUUID(),
                name = "job1",
                sourceDirectory = "/dir1",
                filePattern = "*.txt",
                scanIntervalType = ScanIntervalType.CRON,
                scanIntervalValue = "0 * * * * ?",
                parserId = "parser1",
                isActive = true
            ),
            ScanJob(
                id = UUID.randomUUID(),
                name = "job2",
                sourceDirectory = "/dir2",
                filePattern = "*.csv",
                scanIntervalType = ScanIntervalType.FIXED,
                scanIntervalValue = "PT10M",
                parserId = "parser2",
                isActive = true
            )
        )
        
        every { scanJobRepository.findAllByIsActive(true) } returns activeJobs
        every { scheduler.checkExists(any<JobKey>()) } returns false
        
        // When
        jobScheduler.rescheduleAllActiveJobs()
        
        // Then
        verify(exactly = 2) { scheduler.scheduleJob(any(), any()) }
    }
    
    @Test
    fun `should handle reschedule failure gracefully`() {
        // Given
        every { scanJobRepository.findAllByIsActive(true) } throws RuntimeException("DB error")
        
        // When - should not throw
        jobScheduler.rescheduleAllActiveJobs()
        
        // Then
        verify(exactly = 0) { scheduler.scheduleJob(any(), any()) }
    }
}