package com.quantumsoft.tia.scanner.dto

import com.quantumsoft.tia.scanner.entities.ExecutionStatus
import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import jakarta.validation.constraints.*
import java.time.Duration
import java.time.Instant
import java.util.UUID

data class ScanJobDto(
    val id: UUID,
    val name: String,
    val sourceDirectory: String,
    val filePattern: String,
    val scanInterval: ScanIntervalDto,
    val isActive: Boolean,
    val lastExecution: Instant?,
    val nextExecution: Instant?,
    val statistics: ScanJobStatisticsDto?
)

data class ScanJobDetailDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val sourceDirectory: String,
    val filePattern: String,
    val scanInterval: ScanIntervalDto,
    val maxFileSizeMb: Int,
    val recursiveScan: Boolean,
    val maxDepth: Int,
    val priority: Int,
    val parserId: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: String?,
    val updatedBy: String?,
    val lastExecution: ScanExecutionDto?,
    val statistics: ScanJobStatisticsDto?
)

data class ScanIntervalDto(
    val type: ScanIntervalType,
    val value: String
)

data class ScanJobStatisticsDto(
    val totalFiles: Long,
    val successRate: Double,
    val averageScanDuration: Duration?,
    val lastSuccessfulScan: Instant?,
    val failureCount: Long
)

data class CreateScanJobRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,
    
    @field:NotBlank(message = "Source directory is required")
    @field:Size(max = 1024, message = "Source directory must not exceed 1024 characters")
    val sourceDirectory: String,
    
    @field:NotBlank(message = "File pattern is required")
    @field:Pattern(regexp = "^[\\w\\*\\.\\?\\[\\]\\{\\}]+$", message = "Invalid file pattern")
    val filePattern: String,
    
    @field:NotNull(message = "Scan interval is required")
    val scanInterval: ScanIntervalDto,
    
    @field:Min(value = 1, message = "Max file size must be at least 1 MB")
    @field:Max(value = 10240, message = "Max file size cannot exceed 10240 MB")
    val maxFileSizeMb: Int = 1024,
    
    val recursiveScan: Boolean = true,
    
    @field:Min(value = 1, message = "Max depth must be at least 1")
    @field:Max(value = 100, message = "Max depth cannot exceed 100")
    val maxDepth: Int = 10,
    
    @field:Min(value = 0, message = "Priority must be at least 0")
    @field:Max(value = 10, message = "Priority cannot exceed 10")
    val priority: Int = 0,
    
    @field:NotBlank(message = "Parser ID is required")
    val parserId: String
)

data class UpdateScanJobRequest(
    val name: String? = null,
    val description: String? = null,
    val sourceDirectory: String? = null,
    val filePattern: String? = null,
    val scanInterval: ScanIntervalDto? = null,
    val maxFileSizeMb: Int? = null,
    val recursiveScan: Boolean? = null,
    val maxDepth: Int? = null,
    val priority: Int? = null,
    val parserId: String? = null,
    val isActive: Boolean? = null
)

data class TriggerScanRequest(
    val force: Boolean = false
)

data class ScanExecutionDto(
    val executionId: UUID,
    val jobId: UUID,
    val status: ExecutionStatus,
    val message: String,
    val estimatedDuration: Duration?,
    val startedAt: Instant
)

data class JobStatisticsDto(
    val jobId: UUID,
    val totalExecutions: Long,
    val successfulExecutions: Long,
    val failedExecutions: Long,
    val runningExecutions: Long,
    val averageFilesDiscovered: Double,
    val averageFilesQueued: Double,
    val averageDuration: Duration?,
    val fileStatusDistribution: Map<FileStatus, Long>
)