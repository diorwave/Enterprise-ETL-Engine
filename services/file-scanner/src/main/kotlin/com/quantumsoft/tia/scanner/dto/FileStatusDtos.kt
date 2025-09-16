package com.quantumsoft.tia.scanner.dto

import com.quantumsoft.tia.scanner.entities.FileStatus
import java.time.Instant
import java.util.UUID

data class FileStatusDto(
    val id: UUID,
    val jobId: UUID,
    val jobName: String,
    val filePath: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val fileHash: String,
    val fileModifiedAt: Instant,
    val status: FileStatus,
    val queueId: String?,
    val processingStartedAt: Instant?,
    val processingCompletedAt: Instant?,
    val processingInstanceId: String?,
    val errorMessage: String?,
    val retryCount: Int,
    val discoveredAt: Instant
)

data class FileStatusFilter(
    val jobId: UUID? = null,
    val statuses: List<FileStatus>? = null,
    val fromDate: Instant? = null,
    val toDate: Instant? = null,
    val filePattern: String? = null,
    val minSize: Long? = null,
    val maxSize: Long? = null
)

data class FileDetailDto(
    val id: UUID,
    val jobId: UUID,
    val jobName: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val fileHash: String,
    val fileModifiedAt: Instant,
    val status: FileStatus,
    val discoveredAt: Instant,
    val queuedAt: Instant?,
    val processedAt: Instant?,
    val completedAt: Instant?,
    val queueId: String?,
    val instanceId: String?,
    val errorMessage: String?,
    val retryCount: Int,
    val processingDurationMs: Long?
)

data class FileRetryResult(
    val fileId: UUID,
    val status: FileStatus,
    val queueId: String?,
    val message: String,
    val retryCount: Int
)