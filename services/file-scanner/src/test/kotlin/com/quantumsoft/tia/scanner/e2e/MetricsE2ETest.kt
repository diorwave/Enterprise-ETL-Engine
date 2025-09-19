package com.quantumsoft.tia.scanner.e2e

import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.entities.ScannedFileEntity
import com.quantumsoft.tia.scanner.entities.ScanJob
import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.repositories.ScannedFileRepository
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@AutoConfigureMockMvc
class MetricsE2ETest : BaseE2ETest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var scanJobRepository: ScanJobRepository
    
    @Autowired
    private lateinit var scannedFileRepository: ScannedFileRepository
    
    @BeforeEach
    fun setup() {
        scannedFileRepository.deleteAll()
        scanJobRepository.deleteAll()
    }
    
    @AfterEach
    fun cleanup() {
        scannedFileRepository.deleteAll()
        scanJobRepository.deleteAll()
    }
    
    @Test
    fun `should get overall metrics`() {
        // Given - Create test data
        createTestData()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalFilesScanned").value(5))
            .andExpect(jsonPath("$.filesProcessed").value(2))
            .andExpect(jsonPath("$.filesPending").value(2))
            .andExpect(jsonPath("$.filesFailed").value(1))
            .andExpect(jsonPath("$.totalBytesProcessed").isNumber)
            .andExpect(jsonPath("$.averageProcessingTime").isNumber)
            .andExpect(jsonPath("$.activeJobs").value(2))
            .andExpect(jsonPath("$.lastScanTime").exists())
    }
    
    @Test
    fun `should get metrics summary`() {
        // Given - Create test data
        createTestData()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalFiles").value(5))
            .andExpect(jsonPath("$.processedFiles").value(2))
            .andExpect(jsonPath("$.pendingFiles").value(2))
            .andExpect(jsonPath("$.failedFiles").value(1))
            .andExpect(jsonPath("$.successRate").isNumber)
            .andExpect(jsonPath("$.averageFileSize").isNumber)
            .andExpect(jsonPath("$.totalDataProcessed").isNumber)
    }
    
    @Test
    fun `should get job metrics`() {
        // Given - Create job with files
        val job = createJobWithFiles()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/jobs/${job.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.jobId").value(job.id.toString()))
            .andExpect(jsonPath("$.jobName").value(job.name))
            .andExpect(jsonPath("$.totalFiles").value(3))
            .andExpect(jsonPath("$.processedFiles").value(1))
            .andExpect(jsonPath("$.pendingFiles").value(1))
            .andExpect(jsonPath("$.failedFiles").value(1))
            .andExpect(jsonPath("$.successRate").isNumber)
            .andExpect(jsonPath("$.lastScanTime").exists())
            .andExpect(jsonPath("$.averageProcessingTime").isNumber)
    }
    
    @Test
    fun `should get processing statistics`() {
        // Given - Create test data with various processing times
        createTestDataWithProcessingTimes()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/processing-stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageProcessingTime").isNumber)
            .andExpect(jsonPath("$.minProcessingTime").isNumber)
            .andExpect(jsonPath("$.maxProcessingTime").isNumber)
            .andExpect(jsonPath("$.medianProcessingTime").isNumber)
            .andExpect(jsonPath("$.totalProcessingTime").isNumber)
            .andExpect(jsonPath("$.filesPerHour").isNumber)
            .andExpect(jsonPath("$.bytesPerSecond").isNumber)
    }
    
    @Test
    fun `should get hourly metrics`() {
        // Given - Create files processed at different hours
        createFilesForHourlyMetrics()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/hourly"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].hour").exists())
            .andExpect(jsonPath("$[0].filesProcessed").isNumber)
            .andExpect(jsonPath("$[0].bytesProcessed").isNumber)
            .andExpect(jsonPath("$[0].averageProcessingTime").isNumber)
    }
    
    @Test
    fun `should get daily metrics`() {
        // Given - Create files over multiple days
        createFilesForDailyMetrics()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/daily"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].date").exists())
            .andExpect(jsonPath("$[0].totalFiles").isNumber)
            .andExpect(jsonPath("$[0].processedFiles").isNumber)
            .andExpect(jsonPath("$[0].failedFiles").isNumber)
            .andExpect(jsonPath("$[0].totalBytes").isNumber)
            .andExpect(jsonPath("$[0].successRate").isNumber)
    }
    
    @Test
    fun `should get error statistics`() {
        // Given - Create failed files with various errors
        createFailedFiles()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/errors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalErrors").value(3))
            .andExpect(jsonPath("$.errorsByType").exists())
            .andExpect(jsonPath("$.errorsByType['ParseError']").value(1))
            .andExpect(jsonPath("$.errorsByType['ConnectionError']").value(1))
            .andExpect(jsonPath("$.errorsByType['ValidationError']").value(1))
            .andExpect(jsonPath("$.mostCommonErrors").isArray)
            .andExpect(jsonPath("$.errorRate").isNumber)
    }
    
    @Test
    fun `should get performance metrics`() {
        // Given - Create test data
        createTestData()
        
        // When & Then
        mockMvc.perform(get("/api/v1/metrics/performance"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cpuUsage").isNumber)
            .andExpect(jsonPath("$.memoryUsage").isNumber)
            .andExpect(jsonPath("$.diskUsage").isNumber)
            .andExpect(jsonPath("$.activeThreads").isNumber)
            .andExpect(jsonPath("$.queueSize").isNumber)
            .andExpect(jsonPath("$.uptime").isNumber)
    }
    
    @Test
    fun `should return 404 for non-existent job metrics`() {
        val nonExistentJobId = UUID.randomUUID()
        
        mockMvc.perform(get("/api/v1/metrics/jobs/$nonExistentJobId"))
            .andExpect(status().isNotFound)
    }
    
    // Helper methods to create test data
    
    private fun createTestData() {
        // Create jobs
        val activeJob1 = ScanJob(
            id = UUID.randomUUID(),
            name = "Active Job 1",
            sourceDirectory = "/data/source1",
            filePattern = "*.asn1",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT1H",
            parserId = "parser1",
            isActive = true,
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        
        val activeJob2 = ScanJob(
            id = UUID.randomUUID(),
            name = "Active Job 2",
            sourceDirectory = "/data/source2",
            filePattern = "*.csv",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */30 * * * *",
            parserId = "parser2",
            isActive = true,
            createdAt = Instant.now().minus(3, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        
        val inactiveJob = ScanJob(
            id = UUID.randomUUID(),
            name = "Inactive Job",
            sourceDirectory = "/data/source3",
            filePattern = "*.xml",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT2H",
            parserId = "parser3",
            isActive = false,
            createdAt = Instant.now().minus(10, ChronoUnit.DAYS),
            updatedAt = Instant.now().minus(2, ChronoUnit.DAYS)
        )
        
        scanJobRepository.saveAll(listOf(activeJob1, activeJob2, inactiveJob))
        
        // Create scanned files
        val files = listOf(
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/data/file1.asn1",
                fileName = "file1.asn1",
                fileSizeBytes = 1024 * 1024, // 1MB
                fileHash = "hash1",
                fileModifiedAt = Instant.now().minus(2, ChronoUnit.HOURS),
                status = FileStatus.COMPLETED,
                scanJob = activeJob1,
                discoveredAt = Instant.now().minus(2, ChronoUnit.HOURS),
                processingCompletedAt = Instant.now().minus(1, ChronoUnit.HOURS),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/data/file2.asn1",
                fileName = "file2.asn1",
                fileSizeBytes = 2 * 1024 * 1024, // 2MB
                fileHash = "hash2",
                fileModifiedAt = Instant.now().minus(3, ChronoUnit.HOURS),
                status = FileStatus.COMPLETED,
                scanJob = activeJob1,
                discoveredAt = Instant.now().minus(3, ChronoUnit.HOURS),
                processingCompletedAt = Instant.now().minus(2, ChronoUnit.HOURS),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/data/file3.csv",
                fileName = "file3.csv",
                fileSizeBytes = 512 * 1024, // 512KB
                fileHash = "hash3",
                fileModifiedAt = Instant.now().minus(30, ChronoUnit.MINUTES),
                status = FileStatus.DISCOVERED,
                scanJob = activeJob2,
                discoveredAt = Instant.now().minus(30, ChronoUnit.MINUTES),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/data/file4.csv",
                fileName = "file4.csv",
                fileSizeBytes = 768 * 1024, // 768KB
                fileHash = "hash4",
                fileModifiedAt = Instant.now().minus(45, ChronoUnit.MINUTES),
                status = FileStatus.QUEUED,
                scanJob = activeJob2,
                discoveredAt = Instant.now().minus(45, ChronoUnit.MINUTES),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/data/file5.xml",
                fileName = "file5.xml",
                fileSizeBytes = 1536 * 1024, // 1.5MB
                fileHash = "hash5",
                fileModifiedAt = Instant.now().minus(4, ChronoUnit.HOURS),
                status = FileStatus.FAILED,
                scanJob = inactiveJob,
                discoveredAt = Instant.now().minus(4, ChronoUnit.HOURS),
                errorMessage = "Parse error at line 42",
                retryCount = 3
            )
        )
        
        scannedFileRepository.saveAll(files)
    }
    
    private fun createJobWithFiles(): ScanJob {
        val job = ScanJob(
            id = UUID.randomUUID(),
            name = "Test Job with Files",
            sourceDirectory = "/test/data",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT30M",
            parserId = "test-parser",
            isActive = true,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        scanJobRepository.save(job)
        
        val files = listOf(
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/test/completed.txt",
                fileName = "completed.txt",
                fileSizeBytes = 1024,
                fileHash = "c1",
                fileModifiedAt = Instant.now().minus(1, ChronoUnit.HOURS),
                status = FileStatus.COMPLETED,
                scanJob = job,
                discoveredAt = Instant.now().minus(1, ChronoUnit.HOURS),
                processingCompletedAt = Instant.now().minus(30, ChronoUnit.MINUTES),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/test/pending.txt",
                fileName = "pending.txt",
                fileSizeBytes = 2048,
                fileHash = "p1",
                fileModifiedAt = Instant.now().minus(15, ChronoUnit.MINUTES),
                status = FileStatus.DISCOVERED,
                scanJob = job,
                discoveredAt = Instant.now().minus(15, ChronoUnit.MINUTES),
                retryCount = 0
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/test/failed.txt",
                fileName = "failed.txt",
                fileSizeBytes = 512,
                fileHash = "f1",
                fileModifiedAt = Instant.now().minus(45, ChronoUnit.MINUTES),
                status = FileStatus.FAILED,
                scanJob = job,
                discoveredAt = Instant.now().minus(45, ChronoUnit.MINUTES),
                errorMessage = "Connection timeout",
                retryCount = 2
            )
        )
        scannedFileRepository.saveAll(files)
        
        return job
    }
    
    private fun createTestDataWithProcessingTimes() {
        val job = ScanJob(
            id = UUID.randomUUID(),
            name = "Performance Test Job",
            sourceDirectory = "/perf",
            filePattern = "*.*",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT1H",
            parserId = "perf-parser",
            isActive = true,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        scanJobRepository.save(job)
        
        val baseTime = Instant.now()
        
        val files = listOf(
            // Fast processing
            createProcessedFile("fast1.txt", job, baseTime.minus(5, ChronoUnit.HOURS), 10),
            createProcessedFile("fast2.txt", job, baseTime.minus(4, ChronoUnit.HOURS), 15),
            // Medium processing  
            createProcessedFile("medium1.txt", job, baseTime.minus(3, ChronoUnit.HOURS), 60),
            createProcessedFile("medium2.txt", job, baseTime.minus(2, ChronoUnit.HOURS), 90),
            // Slow processing
            createProcessedFile("slow1.txt", job, baseTime.minus(1, ChronoUnit.HOURS), 300)
        )
        
        scannedFileRepository.saveAll(files)
    }
    
    private fun createProcessedFile(
        fileName: String,
        job: ScanJob,
        discoveredAt: Instant,
        processingSeconds: Long
    ): ScannedFileEntity {
        return ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/$fileName",
            fileName = fileName,
            fileSizeBytes = (1024 * 1024 * Math.random() * 10).toLong(),
            fileHash = UUID.randomUUID().toString(),
            fileModifiedAt = discoveredAt,
            status = FileStatus.COMPLETED,
            scanJob = job,
            discoveredAt = discoveredAt,
            processingStartedAt = discoveredAt,
            processingCompletedAt = discoveredAt.plusSeconds(processingSeconds),
            retryCount = 0
        )
    }
    
    private fun createFilesForHourlyMetrics() {
        val job = ScanJob(
            id = UUID.randomUUID(),
            name = "Hourly Metrics Job",
            sourceDirectory = "/hourly",
            filePattern = "*.txt",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT1H",
            parserId = "hourly-parser",
            isActive = true,
            createdAt = Instant.now().minus(2, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        scanJobRepository.save(job)
        
        val now = Instant.now()
        
        val files = (0..23).flatMap { hour ->
            (1..3).map { index ->
                ScannedFileEntity(
                    id = UUID.randomUUID(),
                    filePath = "/hourly/file_${hour}_$index.txt",
                    fileName = "file_${hour}_$index.txt",
                    fileSizeBytes = (1024 * (100 + Math.random() * 900)).toLong(),
                    fileHash = "h${hour}_$index",
                    fileModifiedAt = now.minus(hour.toLong(), ChronoUnit.HOURS),
                    status = if (Math.random() > 0.1) FileStatus.COMPLETED else FileStatus.FAILED,
                    scanJob = job,
                    discoveredAt = now.minus(hour.toLong(), ChronoUnit.HOURS),
                    processingCompletedAt = if (Math.random() > 0.1) 
                        now.minus(hour.toLong(), ChronoUnit.HOURS).plus(5, ChronoUnit.MINUTES) 
                        else null,
                    errorMessage = if (Math.random() <= 0.1) "Random error" else null,
                    retryCount = 0
                )
            }
        }
        
        scannedFileRepository.saveAll(files)
    }
    
    private fun createFilesForDailyMetrics() {
        val job = ScanJob(
            id = UUID.randomUUID(),
            name = "Daily Metrics Job",
            sourceDirectory = "/daily",
            filePattern = "*.dat",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 0 * * * *",
            parserId = "daily-parser",
            isActive = true,
            createdAt = Instant.now().minus(7, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        scanJobRepository.save(job)
        
        val now = Instant.now()
        
        val files = (0..6).flatMap { day ->
            (1..10).map { index ->
                val status = when {
                    Math.random() < 0.7 -> FileStatus.COMPLETED
                    Math.random() < 0.9 -> FileStatus.DISCOVERED
                    else -> FileStatus.FAILED
                }
                
                ScannedFileEntity(
                    id = UUID.randomUUID(),
                    filePath = "/daily/file_${day}_$index.txt",
                    fileName = "file_${day}_$index.txt",
                    fileSizeBytes = (1024 * 1024 * (1 + Math.random() * 9)).toLong(),
                    fileHash = "d${day}_$index",
                    fileModifiedAt = now.minus(day.toLong(), ChronoUnit.DAYS),
                    status = status,
                    scanJob = job,
                    discoveredAt = now.minus(day.toLong(), ChronoUnit.DAYS),
                    processingCompletedAt = if (status == FileStatus.COMPLETED) 
                        now.minus(day.toLong(), ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS) 
                        else null,
                    errorMessage = if (status == FileStatus.FAILED) "Processing error" else null,
                    retryCount = if (status == FileStatus.FAILED) (Math.random() * 3).toInt() else 0
                )
            }
        }
        
        scannedFileRepository.saveAll(files)
    }
    
    private fun createFailedFiles() {
        val job = ScanJob(
            id = UUID.randomUUID(),
            name = "Error Test Job",
            sourceDirectory = "/errors",
            filePattern = "*.*",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT30M",
            parserId = "error-parser",
            isActive = true,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            updatedAt = Instant.now()
        )
        scanJobRepository.save(job)
        
        val now = Instant.now()
        
        val files = listOf(
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/errors/parse_error.txt",
                fileName = "parse_error.txt",
                fileSizeBytes = 1024,
                fileHash = "err1",
                fileModifiedAt = now.minus(1, ChronoUnit.HOURS),
                status = FileStatus.FAILED,
                scanJob = job,
                discoveredAt = now.minus(1, ChronoUnit.HOURS),
                errorMessage = "ParseError: Invalid ASN.1 structure at line 42",
                retryCount = 3
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/errors/connection_error.txt",
                fileName = "connection_error.txt",
                fileSizeBytes = 2048,
                fileHash = "err2",
                fileModifiedAt = now.minus(2, ChronoUnit.HOURS),
                status = FileStatus.FAILED,
                scanJob = job,
                discoveredAt = now.minus(2, ChronoUnit.HOURS),
                errorMessage = "ConnectionError: Database connection timeout",
                retryCount = 5
            ),
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/errors/validation_error.txt",
                fileName = "validation_error.txt",
                fileSizeBytes = 512,
                fileHash = "err3",
                fileModifiedAt = now.minus(3, ChronoUnit.HOURS),
                status = FileStatus.FAILED,
                scanJob = job,
                discoveredAt = now.minus(3, ChronoUnit.HOURS),
                errorMessage = "ValidationError: Required field 'timestamp' is missing",
                retryCount = 1
            )
        )
        
        scannedFileRepository.saveAll(files)
    }
}