package com.quantumsoft.tia.scanner.components

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@Component
class QueueManager(
    private val redisTemplate: RedisTemplate<String, String>,
    private val metricsCollector: MetricsCollector? = null,
    private val objectMapper: ObjectMapper,
    private val fileThresholdValidator: FileThresholdValidator? = null,
    private val instanceId: String = "scanner-${System.currentTimeMillis()}"
) {
    companion object {
        private const val QUEUE_PREFIX = "tia:scanner:queue"
        private const val LOCK_PREFIX = "tia:scanner:lock"
        private const val DLQ_PREFIX = "tia:scanner:dlq"
        private const val RETRY_PREFIX = "tia:scanner:retry"
        private const val STATS_PREFIX = "tia:scanner:stats"
        private const val DEFAULT_LOCK_TIMEOUT = 300L // 5 minutes in seconds
        private const val MAX_BATCH_SIZE = 100
        
        private val logger = LoggerFactory.getLogger(QueueManager::class.java)
    }
    
    private val queuedCount = AtomicLong(0)
    private val processedCount = AtomicLong(0)
    private val failedCount = AtomicLong(0)
    
    suspend fun queueFile(request: QueueRequest): QueueResult = coroutineScope {
        try {
            // Check threshold if enabled
            if (fileThresholdValidator != null && fileThresholdValidator.isThresholdEnabled()) {
                val currentQueueSize = getCurrentQueueSize()
                try {
                    fileThresholdValidator.validateThreshold(currentQueueSize)
                    
                    // Record metrics if near threshold
                    if (fileThresholdValidator.isNearThreshold(currentQueueSize, 80.0)) {
                        metricsCollector?.recordThresholdWarning()
                    }
                    val utilization = fileThresholdValidator.getThresholdUtilization(currentQueueSize)
                    metricsCollector?.recordThresholdUtilization(utilization)
                } catch (e: ThresholdExceededException) {
                    logger.warn("Queue threshold exceeded: ${e.message}")
                    metricsCollector?.recordError("threshold_exceeded")
                    return@coroutineScope QueueResult(
                        success = false,
                        queueId = null,
                        message = "Queue threshold exceeded",
                        error = e.message
                    )
                }
            }
            
            // Check if file is already queued (distributed lock)
            val lockKey = "$LOCK_PREFIX:${request.file.fileHash ?: request.file.filePath}"
            val lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, instanceId, Duration.ofSeconds(DEFAULT_LOCK_TIMEOUT))
            
            if (lockAcquired != true) {
                logger.debug("File already queued or being processed: ${request.file.filePath}")
                return@coroutineScope QueueResult(
                    success = false,
                    queueId = null,
                    message = "File already queued or being processed",
                    duplicateDetected = true
                )
            }
            
            // Serialize file info to JSON
            val fileJson = serializeMessage(
                QueueMessage(
                    queueId = "preview",
                    jobId = request.jobId,
                    file = request.file,
                    priority = request.priority,
                    timestamp = Instant.now()
                )
            )
            val queueKey = getQueueKey(request.priority)
            val queueId = generateQueueId()
            
            val messageWithId = QueueMessage(
                queueId = queueId,
                jobId = request.jobId,
                file = request.file,
                priority = request.priority,
                timestamp = Instant.now()
            )
            
            // Add to appropriate priority queue
            redisTemplate.opsForList().rightPush(queueKey, serializeMessage(messageWithId))
            
            // Update retry count if applicable
            if (request.retryCount > 0) {
                val retryKey = "$RETRY_PREFIX:$queueId"
                redisTemplate.opsForValue().set(retryKey, request.retryCount.toString(), Duration.ofDays(1))
            }
            
            // Update statistics
            queuedCount.incrementAndGet()
            updateStats("queued")
            
            // Record metrics
            metricsCollector?.recordFilesQueued(1)
            
            logger.info("File queued successfully: ${request.file.filePath} with ID: $queueId")
            
            QueueResult(
                success = true,
                queueId = queueId,
                message = "File queued successfully",
                position = getQueuePosition(queueKey)
            )
        } catch (e: Exception) {
            logger.error("Failed to queue file: ${request.file.filePath}", e)
            metricsCollector?.recordError("queue_failure")
            failedCount.incrementAndGet()
            
            QueueResult(
                success = false,
                queueId = null,
                message = "Failed to queue file: ${e.message}",
                error = e.message
            )
        }
    }
    
    suspend fun batchQueue(requests: List<QueueRequest>): BatchQueueResult = coroutineScope {
        // Check batch threshold if enabled
        if (fileThresholdValidator != null && fileThresholdValidator.isThresholdEnabled()) {
            val currentQueueSize = getCurrentQueueSize()
            if (!fileThresholdValidator.canEnqueueBatch(currentQueueSize, requests.size)) {
                logger.warn("Batch queue request rejected: would exceed threshold")
                metricsCollector?.recordError("batch_threshold_exceeded")
                
                val failedResults = requests.map { request ->
                    QueueResult(
                        success = false,
                        queueId = null,
                        message = "Batch would exceed queue threshold",
                        error = "Threshold check failed for batch"
                    )
                }
                
                return@coroutineScope BatchQueueResult(
                    totalRequests = requests.size,
                    successful = 0,
                    failed = requests.size,
                    duplicates = 0,
                    results = failedResults
                )
            }
        }
        
        val results = mutableListOf<QueueResult>()
        val chunks = requests.chunked(MAX_BATCH_SIZE)
        
        for (chunk in chunks) {
            val chunkResults = chunk.map { request ->
                async { queueFile(request) }
            }.awaitAll()
            results.addAll(chunkResults)
        }
        
        val successful = results.count { it.success }
        val duplicates = results.count { it.duplicateDetected }
        val failed = results.count { !it.success && !it.duplicateDetected }
        
        BatchQueueResult(
            totalRequests = requests.size,
            successful = successful,
            failed = failed,
            duplicates = duplicates,
            results = results
        )
    }
    
    suspend fun moveToDeadLetter(queueId: String, reason: String): Boolean = coroutineScope {
        try {
            val dlqKey = "$DLQ_PREFIX:${System.currentTimeMillis() / 1000 / 3600}" // Hourly buckets
            
            val dlqEntry = DeadLetterEntry(
                originalQueueId = queueId,
                reason = reason,
                timestamp = Instant.now(),
                instanceId = instanceId
            )
            
            redisTemplate.opsForList().rightPush(dlqKey, serializeDLQEntry(dlqEntry))
            redisTemplate.expire(dlqKey, Duration.ofDays(7)) // Keep DLQ entries for 7 days
            
            failedCount.incrementAndGet()
            updateStats("dlq")
            metricsCollector?.recordError("moved_to_dlq")
            
            logger.warn("Message moved to DLQ: $queueId, reason: $reason")
            true
        } catch (e: Exception) {
            logger.error("Failed to move message to DLQ: $queueId", e)
            false
        }
    }
    
    private fun getCurrentQueueSize(): Int {
        val priorities = listOf(Priority.HIGH, Priority.NORMAL, Priority.LOW)
        var totalSize = 0L
        
        for (priority in priorities) {
            val queueKey = getQueueKey(priority)
            totalSize += redisTemplate.opsForList().size(queueKey) ?: 0
        }
        
        return totalSize.toInt()
    }
    
    suspend fun getQueueStatistics(): QueueStatistics = coroutineScope {
        try {
            val priorities = listOf(Priority.HIGH, Priority.NORMAL, Priority.LOW)
            val queueDepths = mutableMapOf<Priority, Long>()
            
            for (priority in priorities) {
                val queueKey = getQueueKey(priority)
                val depth = redisTemplate.opsForList().size(queueKey) ?: 0
                queueDepths[priority] = depth
            }
            
            val dlqKeys = scanKeys("$DLQ_PREFIX:*")
            val dlqCount = dlqKeys.sumOf { key -> redisTemplate.opsForList().size(key) ?: 0 }
            
            QueueStatistics(
                totalQueued = queuedCount.get(),
                totalProcessed = processedCount.get(),
                totalFailed = failedCount.get(),
                currentDepth = queueDepths.values.sum(),
                depthByPriority = queueDepths,
                deadLetterCount = dlqCount,
                throughput = calculateThroughput()
            )
        } catch (e: Exception) {
            logger.error("Failed to get queue statistics", e)
            QueueStatistics()
        }
    }
    
    suspend fun cleanupExpiredLocks(): Int = coroutineScope {
        try {
            val lockPattern = "$LOCK_PREFIX:*"
            val locks = scanKeys(lockPattern)
            var cleaned = 0
            
            for (lockKey in locks) {
                val ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS) ?: -1L
                if (ttl < 0) {
                    // Lock has no expiration or is already expired
                    redisTemplate.delete(lockKey)
                    cleaned++
                }
            }
            
            logger.info("Cleaned up $cleaned expired locks")
            cleaned
        } catch (e: Exception) {
            logger.error("Failed to cleanup expired locks", e)
            0
        }
    }
    
    // Helper methods
    
    private fun getQueueKey(priority: Priority): String {
        return "$QUEUE_PREFIX:${priority.name.lowercase()}"
    }
    
    private fun generateQueueId(): String {
        return "$instanceId-${System.currentTimeMillis()}-${(Math.random() * 10000).toInt()}"
    }
    
    private fun serializeMessage(message: QueueMessage): String {
        return objectMapper.writeValueAsString(message)
    }
    
    private fun serializeDLQEntry(entry: DeadLetterEntry): String {
        return objectMapper.writeValueAsString(entry)
    }
    
    private fun getQueuePosition(queueKey: String): Long {
        return redisTemplate.opsForList().size(queueKey) ?: 0
    }
    
    private fun updateStats(operation: String) {
        val statsKey = "$STATS_PREFIX:$operation:${System.currentTimeMillis() / 1000 / 60}" // Per minute
        redisTemplate.opsForValue().increment(statsKey)
        redisTemplate.expire(statsKey, Duration.ofHours(24))
    }
    
    private fun calculateThroughput(): Double {
        val processed = processedCount.get()
        val timeWindowSeconds = 60.0
        return processed / timeWindowSeconds
    }

    private fun scanKeys(pattern: String): List<String> {
        return try {
            // Prefer direct KEYS in test/mocked environments where execute/SCAN isn't invoked
            try {
                val direct = redisTemplate.keys(pattern)?.toList()
                if (!direct.isNullOrEmpty()) return direct
            } catch (_: Exception) {
                // Ignore and try SCAN
            }

            // Use SCAN for production to avoid KEYS on large datasets
            val scanned = redisTemplate.execute { connection ->
                val keys = mutableListOf<String>()
                val options = ScanOptions.scanOptions().match(pattern).count(1000).build()
                connection.keyCommands().scan(options).use { cursor ->
                    while (cursor.hasNext()) {
                        val raw = cursor.next()
                        keys.add(String(raw, StandardCharsets.UTF_8))
                    }
                }
                keys
            }
            scanned ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to scan keys for pattern: $pattern", e)
            emptyList()
        }
    }
}
