package com.quantumsoft.tia.scanner.dto.settings

import com.quantumsoft.tia.scanner.entities.SettingType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Schema(description = "Setting data transfer object")
data class SettingDto(
    @Schema(description = "Setting key", example = "scanner.file.threshold")
    val key: String,
    
    @Schema(description = "Setting value", example = "5000")
    val value: String,
    
    @Schema(description = "Value type", example = "INTEGER")
    val type: SettingType? = null,
    
    @Schema(description = "Setting description", example = "Maximum files in queue")
    val description: String? = null
)

@Schema(description = "Create setting request")
data class CreateSettingRequest(
    @field:NotBlank(message = "Key is required")
    @field:Pattern(regexp = "^[a-z0-9.]+$", message = "Key must contain only lowercase letters, numbers, and dots")
    @Schema(description = "Setting key", example = "scanner.file.threshold", required = true)
    val key: String,
    
    @field:NotBlank(message = "Value is required")
    @Schema(description = "Setting value", example = "5000", required = true)
    val value: String,
    
    @Schema(description = "Value type", example = "INTEGER", required = true)
    val type: SettingType,
    
    @Schema(description = "Setting description", example = "Maximum files in queue")
    val description: String? = null
)

@Schema(description = "Update setting request")
data class UpdateSettingRequest(
    @field:NotBlank(message = "Value is required")
    @Schema(description = "New setting value", example = "10000", required = true)
    val value: String
)

@Schema(description = "Settings import result")
data class ImportResult(
    @Schema(description = "Number of settings imported", example = "5")
    val imported: Int,
    
    @Schema(description = "List of failed imports")
    val failed: List<String> = emptyList()
)