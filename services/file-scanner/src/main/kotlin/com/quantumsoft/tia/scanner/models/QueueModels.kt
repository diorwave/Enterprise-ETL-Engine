package com.quantumsoft.tia.scanner.models

import java.time.Instant

enum class Priority {
    HIGH,
    NORMAL,
    LOW
}

data class QueueRequest(
    val jobId: String,
    val file: ScannedFile,
    val priority: Priority = Priority.NORMAL,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)

data class QueueResult(
    val success: Boolean,
    val queueId: String?,
    val message: String,
    val duplicateDetected: Boolean = false,
    val position: Long? = null,
    val error: String? = null
)

data class BatchQueueResult(
    val totalRequests: Int,
    val successful: Int,
    val failed: Int,
    val duplicates: Int,
    val results: List<QueueResult>
)

data class QueueMessage(
    val queueId: String,
    val jobId: String,
    val file: ScannedFile,
    val priority: Priority,
    val timestamp: Instant
)

data class DeadLetterEntry(
    val originalQueueId: String,
    val reason: String,
    val timestamp: Instant,
    val instanceId: String
)

data class QueueStatistics(
    val totalQueued: Long = 0,
    val totalProcessed: Long = 0,
    val totalFailed: Long = 0,
    val currentDepth: Long = 0,
    val depthByPriority: Map<Priority, Long> = emptyMap(),
    val deadLetterCount: Long = 0,
    val throughput: Double = 0.0
)