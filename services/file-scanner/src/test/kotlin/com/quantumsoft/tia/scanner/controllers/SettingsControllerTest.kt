package com.quantumsoft.tia.scanner.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.dto.settings.CreateSettingRequest
import com.quantumsoft.tia.scanner.dto.settings.SettingDto
import com.quantumsoft.tia.scanner.dto.settings.UpdateSettingRequest
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.services.SettingsService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
@DisplayName("SettingsController Tests")
class SettingsControllerTest {

    @MockK
    private lateinit var settingsService: SettingsService

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper
    private lateinit var controller: SettingsController

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        controller = SettingsController(settingsService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `should get all settings`() {
        // Given
        val settings = mapOf(
            "setting1" to "value1",
            "setting2" to "value2"
        )
        coEvery { settingsService.getAllSettings() } returns settings

        // When & Then
        mockMvc.perform(get("/api/v1/settings"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.setting1").value("value1"))
            .andExpect(jsonPath("$.setting2").value("value2"))
    }

    @Test
    fun `should get setting by key`() {
        // Given
        val key = "test.setting"
        val value = "test-value"
        coEvery { settingsService.getSetting(key) } returns value

        // When & Then
        mockMvc.perform(get("/api/v1/settings/$key"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(key))
            .andExpect(jsonPath("$.value").value(value))
    }

    @Test
    fun `should return 404 when setting not found`() {
        // Given
        val key = "non.existent"
        coEvery { settingsService.getSetting(key) } returns null

        // When & Then
        mockMvc.perform(get("/api/v1/settings/$key"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should create new setting`() {
        // Given
        val request = CreateSettingRequest(
            key = "new.setting",
            value = "new-value",
            type = SettingType.STRING,
            description = "Test setting"
        )
        
        coEvery { settingsService.setSetting(any(), any(), any()) } just Runs
        coEvery { settingsService.getSetting(request.key) } returns request.value

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.key").value(request.key))
            .andExpect(jsonPath("$.value").value(request.value))
            .andExpect(jsonPath("$.type").value(request.type.toString()))
    }

    @Test
    fun `should update existing setting`() {
        // Given
        val key = "existing.setting"
        val request = UpdateSettingRequest(
            value = "updated-value"
        )
        
        coEvery { settingsService.getSetting(key) } returns "old-value"
        coEvery { settingsService.setSetting(key, request.value, SettingType.STRING) } just Runs

        // When & Then
        mockMvc.perform(
            put("/api/v1/settings/$key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(key))
            .andExpect(jsonPath("$.value").value(request.value))
    }

    @Test
    fun `should return 404 when updating non-existent setting`() {
        // Given
        val key = "non.existent"
        val request = UpdateSettingRequest(value = "value")
        
        coEvery { settingsService.getSetting(key) } returns null

        // When & Then
        mockMvc.perform(
            put("/api/v1/settings/$key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should delete setting`() {
        // Given
        val key = "delete.me"
        coEvery { settingsService.getSetting(key) } returns "value"
        coEvery { settingsService.deleteSetting(key) } just Runs

        // When & Then
        mockMvc.perform(delete("/api/v1/settings/$key"))
            .andExpect(status().isNoContent)
        
        coVerify { settingsService.deleteSetting(key) }
    }

    @Test
    fun `should return 404 when deleting non-existent setting`() {
        // Given
        val key = "non.existent"
        coEvery { settingsService.getSetting(key) } returns null

        // When & Then
        mockMvc.perform(delete("/api/v1/settings/$key"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should get settings by pattern`() {
        // Given
        val pattern = "scanner.*"
        val allSettings = mapOf(
            "scanner.file.threshold" to "5000",
            "scanner.enabled" to "true",
            "other.setting" to "value"
        )
        coEvery { settingsService.getAllSettings() } returns allSettings

        // When & Then
        mockMvc.perform(get("/api/v1/settings/search")
                .param("pattern", pattern))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.['scanner.file.threshold']").value("5000"))
            .andExpect(jsonPath("$.['scanner.enabled']").value("true"))
    }

    @Test
    fun `should validate setting value type`() {
        // Given
        val request = CreateSettingRequest(
            key = "invalid.integer",
            value = "not-a-number",
            type = SettingType.INTEGER
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle integer setting`() {
        // Given
        val request = CreateSettingRequest(
            key = "max.retries",
            value = "5",
            type = SettingType.INTEGER
        )
        
        coEvery { settingsService.setSetting(request.key, 5, SettingType.INTEGER) } just Runs
        coEvery { settingsService.getSetting(request.key) } returns "5"

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.value").value("5"))
            .andExpect(jsonPath("$.type").value("INTEGER"))
    }

    @Test
    fun `should handle boolean setting`() {
        // Given
        val request = CreateSettingRequest(
            key = "feature.enabled",
            value = "true",
            type = SettingType.BOOLEAN
        )
        
        coEvery { settingsService.setSetting(request.key, true, SettingType.BOOLEAN) } just Runs
        coEvery { settingsService.getSetting(request.key) } returns "true"

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.value").value("true"))
            .andExpect(jsonPath("$.type").value("BOOLEAN"))
    }

    @Test
    fun `should handle JSON setting`() {
        // Given
        val jsonValue = """{"threshold": 5000, "enabled": true}"""
        val request = CreateSettingRequest(
            key = "config.json",
            value = jsonValue,
            type = SettingType.JSON
        )
        
        coEvery { settingsService.setSetting(request.key, any<Map<String, Any>>(), SettingType.JSON) } just Runs
        coEvery { settingsService.getSetting(request.key) } returns jsonValue

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("JSON"))
    }

    @Test
    fun `should export settings`() {
        // Given
        val settings = mapOf(
            "export1" to "value1",
            "export2" to "value2"
        )
        coEvery { settingsService.getAllSettings() } returns settings

        // When & Then
        mockMvc.perform(get("/api/v1/settings/export"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Disposition", "attachment; filename=settings-export.json"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `should import settings`() {
        // Given
        val importData = mapOf(
            "import1" to "value1",
            "import2" to "value2"
        )
        
        coEvery { settingsService.setSetting(any(), any(), any()) } just Runs

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(importData))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.imported").value(2))
        
        coVerify(exactly = 2) { settingsService.setSetting(any(), any(), any()) }
    }
}