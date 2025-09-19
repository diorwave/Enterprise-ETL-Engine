package com.quantumsoft.tia.scanner.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.dto.files.CleanupRequest
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
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@AutoConfigureMockMvc
class FileOperationsE2ETest : BaseE2ETest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var scannedFileRepository: ScannedFileRepository
    
    @Autowired
    private lateinit var scanJobRepository: ScanJobRepository
    
    private lateinit var testJob: ScanJob
    
    @BeforeEach
    fun setup() {
        scannedFileRepository.deleteAll()
        scanJobRepository.deleteAll()
        
        // Create a test job for files
        testJob = scanJobRepository.save(
            ScanJob(
                id = UUID.randomUUID(),
                name = "Test Job",
                sourceDirectory = "/test",
                filePattern = "*.txt",
                scanIntervalType = ScanIntervalType.FIXED,
                scanIntervalValue = "PT1H",
                parserId = "test-parser",
                isActive = true,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
    }
    
    @AfterEach
    fun cleanup() {
        scannedFileRepository.deleteAll()
        scanJobRepository.deleteAll()
    }
    
    @Test
    fun `should get all scanned files with pagination`() {
        // Given - Create multiple scanned files
        val files = (1..5).map { index ->
            ScannedFileEntity(
                id = UUID.randomUUID(),
                filePath = "/test/file$index.asn1",
                fileName = "file$index.asn1",
                fileSizeBytes = (index * 1000).toLong(),
                fileHash = "hash$index",
                fileModifiedAt = Instant.now(),
                status = if (index % 2 == 0) FileStatus.COMPLETED else FileStatus.DISCOVERED,
                scanJob = testJob,
                discoveredAt = Instant.now(),
                processingCompletedAt = if (index % 2 == 0) Instant.now() else null,
                errorMessage = if (index == 3) "Test error" else null,
                retryCount = 0
            )
        }
        scannedFileRepository.saveAll(files)
        
        // When & Then
        mockMvc.perform(
            get("/api/v1/files")
                .param("page", "0")
                .param("size", "3")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content").isArray)
        .andExpect(jsonPath("$.content", hasSize<Any>(3)))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(2))
    }
    
    @Test
    fun `should get file by id`() {
        // Given - Create a scanned file
        val file = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/specific.asn1",
            fileName = "specific.asn1",
            fileSizeBytes = 5000,
            fileHash = "abc123",
            fileModifiedAt = Instant.now(),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            processingCompletedAt = Instant.now(),
            errorMessage = null,
            retryCount = 0
        )
        val saved = scannedFileRepository.save(file)
        
        // When & Then
        mockMvc.perform(get("/api/v1/files/${saved.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(saved.id.toString()))
            .andExpect(jsonPath("$.fileName").value("specific.asn1"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }
    
    @Test
    fun `should get files by status`() {
        // Given - Create files with different statuses
        val discoveredFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/discovered.txt",
            fileName = "discovered.txt",
            fileSizeBytes = 1000,
            fileHash = "hash1",
            fileModifiedAt = Instant.now(),
            status = FileStatus.DISCOVERED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            retryCount = 0
        )
        
        val completedFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/completed.txt",
            fileName = "completed.txt",
            fileSizeBytes = 2000,
            fileHash = "hash2",
            fileModifiedAt = Instant.now(),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            processingCompletedAt = Instant.now(),
            retryCount = 0
        )
        
        val failedFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/failed.txt",
            fileName = "failed.txt",
            fileSizeBytes = 3000,
            fileHash = "hash3",
            fileModifiedAt = Instant.now(),
            status = FileStatus.FAILED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            errorMessage = "Processing failed",
            retryCount = 3
        )
        
        scannedFileRepository.saveAll(listOf(discoveredFile, completedFile, failedFile))
        
        // When & Then - Get only DISCOVERED files
        mockMvc.perform(
            get("/api/v1/files/status/DISCOVERED")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$", hasSize<Any>(1)))
        .andExpect(jsonPath("$[0].fileName").value("discovered.txt"))
        .andExpect(jsonPath("$[0].status").value("DISCOVERED"))
        
        // When & Then - Get only FAILED files
        mockMvc.perform(
            get("/api/v1/files/status/FAILED")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$", hasSize<Any>(1)))
        .andExpect(jsonPath("$[0].fileName").value("failed.txt"))
        .andExpect(jsonPath("$[0].errorMessage").value("Processing failed"))
    }
    
    @Test
    fun `should retry failed file`() {
        // Given - Create a failed file
        val failedFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/retry.txt",
            fileName = "retry.txt",
            fileSizeBytes = 1500,
            fileHash = "retry123",
            fileModifiedAt = Instant.now(),
            status = FileStatus.FAILED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            errorMessage = "Initial processing failed",
            retryCount = 1
        )
        val saved = scannedFileRepository.save(failedFile)
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/files/${saved.id}/retry")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(saved.id.toString()))
        .andExpect(jsonPath("$.status").value("QUEUED"))
        .andExpect(jsonPath("$.retryCount").value(2))
        
        // Verify in database
        val updated = scannedFileRepository.findById(saved.id).get()
        assert(updated.status == FileStatus.QUEUED)
        assert(updated.retryCount == 2)
    }
    
    @Test
    fun `should reprocess completed file`() {
        // Given - Create a completed file
        val completedFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/reprocess.csv",
            fileName = "reprocess.csv",
            fileSizeBytes = 2500,
            fileHash = "completed456",
            fileModifiedAt = Instant.now(),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            processingCompletedAt = Instant.now(),
            retryCount = 0
        )
        val saved = scannedFileRepository.save(completedFile)
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/files/${saved.id}/reprocess")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(saved.id.toString()))
        .andExpect(jsonPath("$.status").value("QUEUED"))
        
        // Verify in database
        val updated = scannedFileRepository.findById(saved.id).get()
        assert(updated.status == FileStatus.QUEUED)
        assert(updated.processingCompletedAt == null)
    }
    
    @Test
    fun `should cleanup old files`() {
        // Given - Create old and new files
        val oldFile1 = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/old1.txt",
            fileName = "old1.txt",
            fileSizeBytes = 1000,
            fileHash = "old1",
            fileModifiedAt = Instant.now().minus(10, ChronoUnit.DAYS),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now().minus(10, ChronoUnit.DAYS),
            processingCompletedAt = Instant.now().minus(10, ChronoUnit.DAYS),
            retryCount = 0
        )
        
        val oldFile2 = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/old2.txt",
            fileName = "old2.txt",
            fileSizeBytes = 1000,
            fileHash = "old2",
            fileModifiedAt = Instant.now().minus(8, ChronoUnit.DAYS),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now().minus(8, ChronoUnit.DAYS),
            processingCompletedAt = Instant.now().minus(8, ChronoUnit.DAYS),
            retryCount = 0
        )
        
        val recentFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/recent.txt",
            fileName = "recent.txt",
            fileSizeBytes = 1000,
            fileHash = "recent",
            fileModifiedAt = Instant.now().minus(3, ChronoUnit.DAYS),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now().minus(3, ChronoUnit.DAYS),
            processingCompletedAt = Instant.now().minus(3, ChronoUnit.DAYS),
            retryCount = 0
        )
        
        scannedFileRepository.saveAll(listOf(oldFile1, oldFile2, recentFile))
        
        // When - Cleanup files older than 7 days
        val request = CleanupRequest(
            daysToKeep = 7
        )
        
        mockMvc.perform(
            post("/api/v1/files/cleanup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.filesDeleted").value(2))
        .andExpect(jsonPath("$.success").value(true))
        
        // Then - Verify only recent file remains
        val remaining = scannedFileRepository.findAll()
        assert(remaining.size == 1)
        assert(remaining[0].fileName == "recent.txt")
    }
    
    @Test
    fun `should return 404 for non-existent file`() {
        val nonExistentId = UUID.randomUUID()
        
        mockMvc.perform(get("/api/v1/files/$nonExistentId"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should get files by job id`() {
        // Given - Create files for specific job
        val otherJob = scanJobRepository.save(
            ScanJob(
                id = UUID.randomUUID(),
                name = "Other Job",
                sourceDirectory = "/other",
                filePattern = "*.log",
                scanIntervalType = ScanIntervalType.FIXED,
                scanIntervalValue = "PT2H",
                parserId = "other-parser",
                isActive = true,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        
        val file1 = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/job1_file1.txt",
            fileName = "job1_file1.txt",
            fileSizeBytes = 1000,
            fileHash = "hash1",
            fileModifiedAt = Instant.now(),
            status = FileStatus.COMPLETED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            retryCount = 0
        )
        
        val file2 = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/job1_file2.txt",
            fileName = "job1_file2.txt",
            fileSizeBytes = 2000,
            fileHash = "hash2",
            fileModifiedAt = Instant.now(),
            status = FileStatus.DISCOVERED,
            scanJob = testJob,
            discoveredAt = Instant.now(),
            retryCount = 0
        )
        
        val otherFile = ScannedFileEntity(
            id = UUID.randomUUID(),
            filePath = "/test/other_job.txt",
            fileName = "other_job.txt",
            fileSizeBytes = 3000,
            fileHash = "hash3",
            fileModifiedAt = Instant.now(),
            status = FileStatus.COMPLETED,
            scanJob = otherJob,
            discoveredAt = Instant.now(),
            retryCount = 0
        )
        
        scannedFileRepository.saveAll(listOf(file1, file2, otherFile))
        
        // When & Then
        mockMvc.perform(get("/api/v1/files/job/${testJob.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("$[*].jobId", everyItem(equalTo(testJob.id.toString()))))
    }
    
    @Test
    fun `should handle invalid status parameter`() {
        mockMvc.perform(get("/api/v1/files/status/INVALID_STATUS"))
            .andExpect(status().isBadRequest)
    }
}