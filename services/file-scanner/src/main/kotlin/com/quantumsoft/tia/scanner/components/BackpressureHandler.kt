package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min
import kotlin.math.pow

@Component
class BackpressureHandler(
    private val queueManager: QueueManager,
    private val fileThresholdValidator: FileThresholdValidator,
    private val metricsCollector: MetricsCollector? = null
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BackpressureHandler::class.java)
        private const val DEFAULT_MAX_RETRIES = 5
        private const val BASE_BACKOFF_MS = 100L
        private const val MAX_BACKOFF_MS = 30000L
        private const val CIRCUIT_BREAKER_THRESHOLD = 5
        private const val CIRCUIT_BREAKER_RESET_MS = 60000L
        private const val CAPACITY_CHECK_INTERVAL_MS = 100L
    }
    
    private val circuitBreakerFailureCount = AtomicInteger(0)
    private val circuitBreakerOpenTime = AtomicLong(0)
    
    data class BackpressureResult(
        val success: Boolean,
        val queueId: String? = null,
        val retryCount: Int = 0,
        val waitTimeMs: Long = 0,
        val error: String? = null,
        val shouldRetryLater: Boolean = false,
        val timedOut: Boolean = false,
        val circuitBreakerOpen: Boolean = false
    )
    
    data class BatchBackpressureResult(
        val successful: Int,
        val failed: Int,
        val deferred: Int,
        val deferredRequests: List<QueueRequest>,
        val results: List<QueueResult>
    )
    
    data class PrioritizedResult(
        val request: QueueRequest,
        val result: QueueResult
    )
    
    suspend fun enqueueWithBackpressure(
        request: QueueRequest,
        maxRetries: Int = DEFAULT_MAX_RETRIES
    ): BackpressureResult = coroutineScope {
        // Check circuit breaker
        if (isCircuitBreakerOpen()) {
            logger.warn("Circuit breaker is open, rejecting request")
            return@coroutineScope BackpressureResult(
                success = false,
                error = "Circuit breaker is open due to repeated failures",
                circuitBreakerOpen = true,
                shouldRetryLater = true
            )
        }
        
        var retryCount = 0
        var lastError: String? = null
        val startTime = System.currentTimeMillis()
        
        while (retryCount <= maxRetries) {
            try {
                // Check if we can enqueue
                if (fileThresholdValidator.canEnqueueFile(getCurrentQueueSize())) {
                    val result = queueManager.queueFile(request)
                    
                    if (result.success) {
                        // Reset circuit breaker on success
                        circuitBreakerFailureCount.set(0)
                        
                        metricsCollector?.recordBackpressureResolution(
                            System.currentTimeMillis() - startTime
                        )
                        
                        return@coroutineScope BackpressureResult(
                            success = true,
                            queueId = result.queueId,
                            retryCount = retryCount,
                            waitTimeMs = System.currentTimeMillis() - startTime
                        )
                    } else if (result.error?.contains("threshold") == true) {
                        // Threshold exceeded, apply backpressure
                        lastError = result.error
                        
                        if (retryCount < maxRetries) {
                            val backoffMs = calculateAdaptiveBackoff(
                                retryCount,
                                fileThresholdValidator.getThresholdUtilization(getCurrentQueueSize())
                            )
                            
                            logger.debug("Applying backpressure: waiting ${backoffMs}ms before retry ${retryCount + 1}")
                            metricsCollector?.recordBackpressureEvent()
                            metricsCollector?.recordBackpressureRetry(retryCount + 1)
                            
                            delay(backoffMs)
                            retryCount++
                        } else {
                            break
                        }
                    } else {
                        // Non-threshold error, don't retry
                        return@coroutineScope BackpressureResult(
                            success = false,
                            error = result.error,
                            retryCount = retryCount
                        )
                    }
                } else {
                    // Can't enqueue yet, wait
                    if (retryCount < maxRetries) {
                        val backoffMs = calculateAdaptiveBackoff(
                            retryCount,
                            fileThresholdValidator.getThresholdUtilization(getCurrentQueueSize())
                        )
                        delay(backoffMs)
                        retryCount++
                    } else {
                        break
                    }
                }
            } catch (e: ThresholdExceededException) {
                lastError = e.message
                if (retryCount < maxRetries) {
                    val backoffMs = calculateAdaptiveBackoff(retryCount, 100.0)
                    delay(backoffMs)
                    retryCount++
                } else {
                    break
                }
            } catch (e: Exception) {
                logger.error("Unexpected error during backpressure handling", e)
                return@coroutineScope BackpressureResult(
                    success = false,
                    error = e.message,
                    retryCount = retryCount
                )
            }
        }
        
        // Failed after all retries
        incrementCircuitBreakerFailure()
        
        BackpressureResult(
            success = false,
            error = lastError ?: "Max retries exceeded",
            retryCount = retryCount,
            shouldRetryLater = true,
            waitTimeMs = System.currentTimeMillis() - startTime
        )
    }
    
    suspend fun waitForCapacityAndEnqueue(
        request: QueueRequest,
        timeoutMs: Long = 30000L
    ): BackpressureResult = coroutineScope {
        val startTime = System.currentTimeMillis()
        val deadline = startTime + timeoutMs
        
        while (System.currentTimeMillis() < deadline) {
            val remainingCapacity = fileThresholdValidator.getRemainingCapacity(getCurrentQueueSize())
            
            if (remainingCapacity > 0) {
                try {
                    val result = queueManager.queueFile(request)
                    if (result.success) {
                        return@coroutineScope BackpressureResult(
                            success = true,
                            queueId = result.queueId,
                            waitTimeMs = System.currentTimeMillis() - startTime
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error while waiting for capacity", e)
                }
            }
            
            delay(CAPACITY_CHECK_INTERVAL_MS)
        }
        
        // Timeout reached
        BackpressureResult(
            success = false,
            error = "Timeout waiting for queue capacity",
            timedOut = true,
            waitTimeMs = timeoutMs
        )
    }
    
    suspend fun batchEnqueueWithBackpressure(
        requests: List<QueueRequest>
    ): BatchBackpressureResult = coroutineScope {
        val results = mutableListOf<QueueResult>()
        val deferredRequests = mutableListOf<QueueRequest>()
        var successful = 0
        var failed = 0
        var deferred = 0
        
        for (request in requests) {
            if (fileThresholdValidator.canEnqueueFile(getCurrentQueueSize())) {
                try {
                    val result = queueManager.queueFile(request)
                    results.add(result)
                    
                    if (result.success) {
                        successful++
                    } else if (result.error?.contains("threshold") == true) {
                        deferredRequests.add(request)
                        deferred++
                    } else {
                        failed++
                    }
                } catch (e: Exception) {
                    failed++
                    results.add(QueueResult(false, null, e.message ?: "Unknown error"))
                }
            } else {
                // Can't enqueue more, defer remaining
                deferredRequests.add(request)
                deferred++
                results.add(QueueResult(false, null, "Deferred due to threshold"))
            }
        }
        
        BatchBackpressureResult(
            successful = successful,
            failed = failed,
            deferred = deferred,
            deferredRequests = deferredRequests,
            results = results
        )
    }
    
    suspend fun prioritizedBatchEnqueue(
        requests: List<QueueRequest>
    ): List<PrioritizedResult> = coroutineScope {
        // Sort by priority (HIGH > NORMAL > LOW)
        val sortedRequests = requests.sortedBy { it.priority.ordinal }
        val results = mutableListOf<PrioritizedResult>()
        
        for (request in sortedRequests) {
            val remainingCapacity = fileThresholdValidator.getRemainingCapacity(getCurrentQueueSize())
            
            if (remainingCapacity > 0) {
                try {
                    val result = queueManager.queueFile(request)
                    results.add(PrioritizedResult(request, result))
                } catch (e: Exception) {
                    results.add(
                        PrioritizedResult(
                            request, 
                            QueueResult(false, null, e.message ?: "Unknown error")
                        )
                    )
                }
            } else {
                // No more capacity
                results.add(
                    PrioritizedResult(
                        request,
                        QueueResult(false, null, "No capacity available")
                    )
                )
            }
        }
        
        results
    }
    
    fun calculateAdaptiveBackoff(attempt: Int, utilization: Double): Long {
        // Base exponential backoff
        val exponentialBackoff = BASE_BACKOFF_MS * (2.0.pow(attempt.toDouble())).toLong()
        
        // Adjust based on utilization (higher utilization = longer backoff)
        val utilizationFactor = when {
            utilization >= 95 -> 3.0
            utilization >= 90 -> 2.0
            utilization >= 80 -> 1.5
            else -> 1.0
        }
        
        val adaptiveBackoff = (exponentialBackoff * utilizationFactor).toLong()
        
        return min(adaptiveBackoff, MAX_BACKOFF_MS)
    }
    
    private fun isCircuitBreakerOpen(): Boolean {
        val openTime = circuitBreakerOpenTime.get()
        if (openTime > 0) {
            // Check if reset time has passed
            if (System.currentTimeMillis() - openTime > CIRCUIT_BREAKER_RESET_MS) {
                // Reset circuit breaker
                circuitBreakerFailureCount.set(0)
                circuitBreakerOpenTime.set(0)
                logger.info("Circuit breaker reset after timeout")
                return false
            }
            return true
        }
        return false
    }
    
    private fun incrementCircuitBreakerFailure() {
        val failures = circuitBreakerFailureCount.incrementAndGet()
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitBreakerOpenTime.set(System.currentTimeMillis())
            logger.warn("Circuit breaker opened after $failures consecutive failures")
            metricsCollector?.recordError("circuit_breaker_opened")
        }
    }
    
    private suspend fun getCurrentQueueSize(): Int {
        // This would typically call the queue manager or Redis directly
        // For now, returning a placeholder
        return 0
    }
}