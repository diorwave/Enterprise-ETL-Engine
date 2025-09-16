package com.quantumsoft.tia.scanner.dto.jobs

import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import jakarta.validation.constraints.Min

data class UpdateScanJobRequest(
    val description: String? = null,
    val sourceDirectory: String? = null,
    val filePattern: String? = null,
    val scanIntervalType: ScanIntervalType? = null,
    val scanIntervalValue: String? = null,
    val parserId: String? = null,
    
    @field:Min(1, message = "Max file size must be at least 1 MB")
    val maxFileSizeMb: Int? = null,
    
    val recursiveScan: Boolean? = null,
    
    @field:Min(1, message = "Max depth must be at least 1")
    val maxDepth: Int? = null,
    
    @field:Min(0, message = "Priority cannot be negative")
    val priority: Int? = null,
    
    val isActive: Boolean? = null
)