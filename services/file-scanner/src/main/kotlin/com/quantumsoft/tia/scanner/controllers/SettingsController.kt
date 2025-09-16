package com.quantumsoft.tia.scanner.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.dto.settings.*
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.services.SettingsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Settings", description = "System settings management endpoints")
class SettingsController(
    private val settingsService: SettingsService,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {
    
    private val logger = LoggerFactory.getLogger(SettingsController::class.java)

    @GetMapping
    @Operation(summary = "Get all settings", description = "Retrieve all system settings")
    @ApiResponse(responseCode = "200", description = "Settings retrieved successfully")
    fun getAllSettings(): Map<String, String> = runBlocking {
        settingsService.getAllSettings()
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get setting by key", description = "Retrieve a specific setting by its key")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Setting found"),
        ApiResponse(responseCode = "404", description = "Setting not found")
    )
    fun getSettingByKey(
        @Parameter(description = "Setting key", required = true)
        @PathVariable key: String
    ): ResponseEntity<SettingDto> = runBlocking {
        val value = settingsService.getSetting(key)
        if (value != null) {
            ResponseEntity.ok(SettingDto(key = key, value = value))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @Operation(summary = "Create setting", description = "Create a new system setting")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Setting created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request")
    )
    fun createSetting(
        @Valid @RequestBody request: CreateSettingRequest
    ): ResponseEntity<SettingDto> = runBlocking {
        // Validate value type
        val parsedValue = try {
            when (request.type) {
                SettingType.INTEGER -> request.value.toInt()
                SettingType.LONG -> request.value.toLong()
                SettingType.BOOLEAN -> request.value.toBoolean()
                SettingType.JSON -> objectMapper.readValue(request.value, Map::class.java)
                SettingType.STRING -> request.value
            }
        } catch (e: Exception) {
            logger.error("Invalid value for type ${request.type}: ${request.value}", e)
            return@runBlocking ResponseEntity.badRequest().build<SettingDto>()
        }
        
        settingsService.setSetting(request.key, parsedValue, request.type)
        
        ResponseEntity.status(HttpStatus.CREATED).body(
            SettingDto(
                key = request.key,
                value = request.value,
                type = request.type,
                description = request.description
            )
        )
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update setting", description = "Update an existing setting value")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Setting updated successfully"),
        ApiResponse(responseCode = "404", description = "Setting not found"),
        ApiResponse(responseCode = "400", description = "Invalid value")
    )
    fun updateSetting(
        @Parameter(description = "Setting key", required = true)
        @PathVariable key: String,
        @Valid @RequestBody request: UpdateSettingRequest
    ): ResponseEntity<SettingDto> = runBlocking {
        val existing = settingsService.getSetting(key)
        if (existing == null) {
            return@runBlocking ResponseEntity.notFound().build<SettingDto>()
        }
        
        // For updates, assume STRING type if not specified
        settingsService.setSetting(key, request.value, SettingType.STRING)
        
        ResponseEntity.ok(SettingDto(key = key, value = request.value))
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Delete setting", description = "Delete a system setting")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Setting deleted successfully"),
        ApiResponse(responseCode = "404", description = "Setting not found")
    )
    fun deleteSetting(
        @Parameter(description = "Setting key", required = true)
        @PathVariable key: String
    ): ResponseEntity<Void> = runBlocking {
        val existing = settingsService.getSetting(key)
        if (existing == null) {
            return@runBlocking ResponseEntity.notFound().build()
        }
        
        settingsService.deleteSetting(key)
        ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    @Operation(summary = "Search settings", description = "Search settings by key pattern")
    @ApiResponse(responseCode = "200", description = "Settings found")
    fun searchSettings(
        @Parameter(description = "Key pattern (e.g., scanner.*)", required = true)
        @RequestParam pattern: String
    ): Map<String, String> = runBlocking {
        val allSettings = settingsService.getAllSettings()
        val regex = pattern.replace("*", ".*").toRegex()
        allSettings.filterKeys { it.matches(regex) }
    }

    @GetMapping("/export")
    @Operation(summary = "Export settings", description = "Export all settings as JSON")
    @ApiResponse(
        responseCode = "200",
        description = "Settings exported",
        content = [Content(mediaType = "application/json")]
    )
    fun exportSettings(): ResponseEntity<Map<String, String>> = runBlocking {
        val settings = settingsService.getAllSettings()
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=settings-export.json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(settings)
    }

    @PostMapping("/import")
    @Operation(summary = "Import settings", description = "Import settings from JSON")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Settings imported successfully"),
        ApiResponse(responseCode = "400", description = "Invalid import data")
    )
    fun importSettings(
        @RequestBody settings: Map<String, String>
    ): ResponseEntity<ImportResult> = runBlocking {
        var imported = 0
        val failed = mutableListOf<String>()
        
        settings.forEach { (key, value) ->
            try {
                settingsService.setSetting(key, value, SettingType.STRING)
                imported++
            } catch (e: Exception) {
                logger.error("Failed to import setting: $key", e)
                failed.add(key)
            }
        }
        
        ResponseEntity.ok(ImportResult(imported = imported, failed = failed))
    }

    @GetMapping("/threshold")
    @Operation(summary = "Get threshold settings", description = "Get all file threshold related settings")
    @ApiResponse(responseCode = "200", description = "Threshold settings retrieved")
    fun getThresholdSettings(): Map<String, String> = runBlocking {
        val allSettings = settingsService.getAllSettings()
        allSettings.filterKeys { it.startsWith("scanner.file.threshold") }
    }
}