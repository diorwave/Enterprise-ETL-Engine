package com.quantumsoft.tia.scanner.dto.jobs

import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateScanJobRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    val description: String? = null,
    
    @field:NotBlank(message = "Source directory is required")
    val sourceDirectory: String,
    
    @field:NotBlank(message = "File pattern is required")
    val filePattern: String,
    
    @field:NotNull(message = "Scan interval type is required")
    val scanIntervalType: ScanIntervalType,
    
    @field:NotBlank(message = "Scan interval value is required")
    val scanIntervalValue: String,
    
    @field:NotBlank(message = "Parser ID is required")
    val parserId: String,
    
    @field:Min(1, message = "Max file size must be at least 1 MB")
    val maxFileSizeMb: Int = 100,
    
    val recursiveScan: Boolean = true,
    
    @field:Min(1, message = "Max depth must be at least 1")
    val maxDepth: Int = 10,
    
    @field:Min(0, message = "Priority cannot be negative")
    val priority: Int = 0,
    
    val isActive: Boolean = true
)