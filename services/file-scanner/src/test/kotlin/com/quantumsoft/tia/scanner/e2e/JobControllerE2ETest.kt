package com.quantumsoft.tia.scanner.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.dto.jobs.CreateScanJobRequest
import com.quantumsoft.tia.scanner.dto.jobs.UpdateScanJobRequest
import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
class JobControllerE2ETest : BaseE2ETest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var scanJobRepository: ScanJobRepository
    
    @AfterEach
    fun cleanup() {
        scanJobRepository.deleteAll()
    }
    
    @Test
    fun `should create new scan job`() {
        // Given
        val request = CreateScanJobRequest(
            name = "E2E Test Job",
            description = "Test job for E2E testing",
            sourceDirectory = "/test/e2e/path",
            filePattern = "*.asn1",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */5 * * * *",
            parserId = "test-parser",
            maxFileSizeMb = 50,
            recursiveScan = true,
            maxDepth = 3,
            priority = 1,
            isActive = true
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("E2E Test Job"))
        .andExpect(jsonPath("$.description").value("Test job for E2E testing"))
        .andExpect(jsonPath("$.sourceDirectory").value("/test/e2e/path"))
        .andExpect(jsonPath("$.filePattern").value("*.asn1"))
        .andExpect(jsonPath("$.parserId").value("test-parser"))
        .andExpect(jsonPath("$.priority").value(1))
        .andExpect(jsonPath("$.isActive").value(true))
    }
    
    @Test
    fun `should get all scan jobs with pagination`() {
        // Given - Create multiple jobs
        repeat(3) { index ->
            val request = CreateScanJobRequest(
                name = "Test Job $index",
                description = "Job $index",
                sourceDirectory = "/test/path$index",
                filePattern = "*.txt",
                scanIntervalType = ScanIntervalType.FIXED,
                scanIntervalValue = "PT${index + 1}H",
                parserId = "parser-$index",
                isActive = index % 2 == 0
            )
            
            mockMvc.perform(
                post("/api/v1/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
        }
        
        // When & Then
        mockMvc.perform(
            get("/api/v1/jobs")
                .param("page", "0")
                .param("size", "10")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content").isArray)
        .andExpect(jsonPath("$.content", hasSize<Any>(3)))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.number").value(0))
    }
    
    @Test
    fun `should get scan job by id`() {
        // Given - Create a job
        val createRequest = CreateScanJobRequest(
            name = "Get By ID Test",
            description = "Test finding by ID",
            sourceDirectory = "/test/get",
            filePattern = "*.csv",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 0 * * * *",
            parserId = "csv-parser",
            isActive = true
        )
        
        val createResponse = mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)
        .andReturn()
        
        val jobId = objectMapper.readTree(createResponse.response.contentAsString)
            .get("id").asText()
        
        // When & Then
        mockMvc.perform(get("/api/v1/jobs/$jobId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(jobId))
            .andExpect(jsonPath("$.name").value("Get By ID Test"))
            .andExpect(jsonPath("$.parserId").value("csv-parser"))
    }
    
    @Test
    fun `should update scan job`() {
        // Given - Create a job
        val createRequest = CreateScanJobRequest(
            name = "Update Test Job",
            description = "Original description",
            sourceDirectory = "/original/path",
            filePattern = "*.log",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT1H",
            parserId = "log-parser",
            priority = 1,
            isActive = true
        )
        
        val createResponse = mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)
        .andReturn()
        
        val jobId = objectMapper.readTree(createResponse.response.contentAsString)
            .get("id").asText()
        
        // When - Update the job
        val updateRequest = UpdateScanJobRequest(
            description = "Updated description",
            priority = 5,
            isActive = false
        )
        
        // Then
        mockMvc.perform(
            put("/api/v1/jobs/$jobId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(jobId))
        .andExpect(jsonPath("$.name").value("Update Test Job")) // Name unchanged
        .andExpect(jsonPath("$.description").value("Updated description"))
        .andExpect(jsonPath("$.priority").value(5))
        .andExpect(jsonPath("$.isActive").value(false))
    }
    
    @Test
    fun `should delete scan job`() {
        // Given - Create a job
        val createRequest = CreateScanJobRequest(
            name = "Delete Test Job",
            description = "To be deleted",
            sourceDirectory = "/delete/test",
            filePattern = "*.json",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */10 * * * *",
            parserId = "json-parser",
            isActive = true
        )
        
        val createResponse = mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)
        .andReturn()
        
        val jobId = objectMapper.readTree(createResponse.response.contentAsString)
            .get("id").asText()
        
        // When - Delete the job
        mockMvc.perform(delete("/api/v1/jobs/$jobId"))
            .andExpect(status().isNoContent)
        
        // Then - Verify it's deleted
        mockMvc.perform(get("/api/v1/jobs/$jobId"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should get only active jobs`() {
        // Given - Create active and inactive jobs
        val activeJob = CreateScanJobRequest(
            name = "Active Job",
            description = "This is active",
            sourceDirectory = "/active",
            filePattern = "*.xml",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT30M",
            parserId = "xml-parser",
            isActive = true
        )
        
        val inactiveJob = CreateScanJobRequest(
            name = "Inactive Job",
            description = "This is inactive",
            sourceDirectory = "/inactive",
            filePattern = "*.yaml",
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 0 0 * * *",
            parserId = "yaml-parser",
            isActive = false
        )
        
        mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activeJob))
        ).andExpect(status().isCreated)
        
        mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inactiveJob))
        ).andExpect(status().isCreated)
        
        // When & Then
        mockMvc.perform(get("/api/v1/jobs/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].name").value("Active Job"))
            .andExpect(jsonPath("$[0].isActive").value(true))
    }
    
    @Test
    fun `should return 404 for non-existent job`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"
        
        mockMvc.perform(get("/api/v1/jobs/$nonExistentId"))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `should validate required fields on job creation`() {
        // Given - Invalid request with missing required fields
        val invalidRequest = """
            {
                "description": "Missing required fields"
            }
        """.trimIndent()
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
        .andExpect(status().isBadRequest)
    }
}