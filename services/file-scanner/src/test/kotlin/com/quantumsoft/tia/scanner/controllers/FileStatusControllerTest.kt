package com.quantumsoft.tia.scanner.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.config.TestConfiguration
import com.quantumsoft.tia.scanner.controllers.files.FilesController
import com.quantumsoft.tia.scanner.controllers.files.FileStatisticsController
import com.quantumsoft.tia.scanner.dto.*
import com.quantumsoft.tia.scanner.dto.files.CleanupRequest
import com.quantumsoft.tia.scanner.dto.files.CleanupResultDto
import com.quantumsoft.tia.scanner.dto.files.FileStatisticsDto
import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.services.FileStatusService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.UUID

@WebMvcTest(controllers = [FilesController::class, FileStatisticsController::class])
@Import(TestConfiguration::class)
class FileStatusControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockBean
    private lateinit var fileStatusService: FileStatusService
    
    @Test
    fun `should query files with filters`() {
        // Given
        val fileDto = createFileStatusDto()
        val page = PageImpl(listOf(fileDto))
        
        whenever(fileStatusService.queryFiles(any(), any()))
            .thenReturn(page)
        
        // When & Then
        mockMvc.perform(
            get("/api/v1/scanner/files")
                .param("jobId", UUID.randomUUID().toString())
                .param("status", "COMPLETED")
                .param("filePattern", "*.asn1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].fileName").value(fileDto.fileName))
            .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
    }
    
    @Test
    fun `should get file by id`() {
        // Given
        val fileId = UUID.randomUUID()
        val fileDetail = createFileStatusDto()
        
        whenever(fileStatusService.getFile(fileId)).thenReturn(fileDetail)
        
        // When & Then
        mockMvc.perform(get("/api/v1/scanner/files/$fileId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(fileDetail.id.toString()))
            .andExpect(jsonPath("$.fileName").value(fileDetail.fileName))
    }
    
    @Test
    fun `should return 404 when file not found`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.getFile(fileId)).thenReturn(null)
        
        // When & Then
        mockMvc.perform(get("/api/v1/scanner/files/$fileId"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should retry file successfully`() {
        // Given
        val fileId = UUID.randomUUID()
        val retryResult = createFileStatusDto().copy(
            id = fileId,
            status = FileStatus.QUEUED,
            queueId = "new-queue-id",
            retryCount = 1
        )
        
        whenever(fileStatusService.retryFile(fileId)).thenReturn(retryResult)
        
        // When & Then
        mockMvc.perform(post("/api/v1/scanner/files/$fileId/retry"))
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.status").value("QUEUED"))
            .andExpect(jsonPath("$.queueId").value("new-queue-id"))
    }
    
    @Test
    fun `should return 404 when retrying non-existent file`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.retryFile(fileId)).thenReturn(null)
        
        // When & Then
        mockMvc.perform(post("/api/v1/scanner/files/$fileId/retry"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should return 409 when retry fails due to state conflict`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.retryFile(fileId))
            .thenThrow(IllegalStateException("File is currently processing"))
        
        // When & Then
        mockMvc.perform(post("/api/v1/scanner/files/$fileId/retry"))
            .andExpect(status().isConflict)
    }
    
    @Test
    fun `should delete file successfully`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.deleteFile(fileId)).thenReturn(true)
        
        // When & Then
        mockMvc.perform(delete("/api/v1/scanner/files/$fileId"))
            .andExpect(status().isNoContent)
    }
    
    @Test
    fun `should return 404 when deleting non-existent file`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.deleteFile(fileId)).thenReturn(false)
        
        // When & Then
        mockMvc.perform(delete("/api/v1/scanner/files/$fileId"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should return 409 when delete fails due to state conflict`() {
        // Given
        val fileId = UUID.randomUUID()
        whenever(fileStatusService.deleteFile(fileId))
            .thenThrow(IllegalStateException("Cannot delete processing file"))
        
        // When & Then
        mockMvc.perform(delete("/api/v1/scanner/files/$fileId"))
            .andExpect(status().isConflict)
    }
    
    @Test
    fun `should get statistics for all files`() {
        // Given
        val stats = FileStatisticsDto(
            totalFiles = 1000,
            statusDistribution = mapOf(
                FileStatus.COMPLETED to 800,
                FileStatus.FAILED to 100,
                FileStatus.PROCESSING to 50,
                FileStatus.QUEUED to 50
            ),
            averageFileSize = 1024000.0,
            totalSizeBytes = 1024000000L,
            oldestFile = Instant.now().minusSeconds(86400),
            newestFile = Instant.now()
        )
        
        whenever(fileStatusService.getStatistics(null)).thenReturn(stats)
        
        // When & Then
        mockMvc.perform(get("/api/v1/scanner/files/statistics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalFiles").value(1000))
            .andExpect(jsonPath("$.statusDistribution.COMPLETED").value(800))
    }
    
    @Test
    fun `should get statistics for specific job`() {
        // Given
        val jobId = UUID.randomUUID()
        val stats = FileStatisticsDto(
            totalFiles = 100,
            statusDistribution = mapOf(FileStatus.COMPLETED to 100),
            averageFileSize = 500000.0,
            totalSizeBytes = 50000000L,
            oldestFile = Instant.now().minusSeconds(3600),
            newestFile = Instant.now()
        )
        
        whenever(fileStatusService.getStatistics(jobId)).thenReturn(stats)
        
        // When & Then
        mockMvc.perform(get("/api/v1/scanner/files/statistics")
            .param("jobId", jobId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalFiles").value(100))
            .andExpect(jsonPath("$.statusDistribution.COMPLETED").value(100))
    }
    
    @Test
    fun `should cleanup old files`() {
        // Given
        val request = CleanupRequest(daysToKeep = 30)
        val result = CleanupResultDto(
            deletedCount = 500,
            beforeCount = 10000L,
            afterCount = 9500L,
            message = "Successfully deleted 500 files older than 30 days"
        )
        
        whenever(fileStatusService.cleanupOldFiles(30)).thenReturn(result)
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/scanner/files/cleanup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.deletedCount").value(500))
            .andExpect(jsonPath("$.message").value(result.message))
    }
    
    @Test
    fun `should validate cleanup request`() {
        // Given - Testing invalid request (daysToKeep = 0 violates @Min(1))
        val invalidRequest = CleanupRequest(daysToKeep = 0)
        
        // When & Then - Should return 400 Bad Request for invalid input
        mockMvc.perform(
            post("/api/v1/scanner/files/cleanup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }
    
    private fun createFileStatusDto() = FileStatusDto(
        id = UUID.randomUUID(),
        jobId = UUID.randomUUID(),
        jobName = "test-job",
        fileName = "test-file.asn1",
        filePath = "/test/path/test-file.asn1",
        fileSizeBytes = 1024,
        fileHash = "hash123",
        fileModifiedAt = Instant.now(),
        status = FileStatus.COMPLETED,
        queueId = "queue-123",
        processingStartedAt = Instant.now(),
        processingCompletedAt = Instant.now(),
        processingInstanceId = "instance-1",
        errorMessage = null,
        retryCount = 0,
        discoveredAt = Instant.now()
    )
    
}