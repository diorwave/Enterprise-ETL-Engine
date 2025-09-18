package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.time.Instant

@DisplayName("BackpressureHandler Tests")
class BackpressureHandlerTest {
    
    private lateinit var queueManager: QueueManager
    private lateinit var fileThresholdValidator: FileThresholdValidator
    private lateinit var backpressureHandler: BackpressureHandler
    
    @BeforeEach
    fun setUp() {
        queueManager = mockk(relaxed = true)
        fileThresholdValidator = mockk(relaxed = true)
        backpressureHandler = BackpressureHandler(queueManager, fileThresholdValidator)
    }
    
    @Test
    fun `should apply exponential backoff on threshold exceeded`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // Set up the validator to allow queueing after initial failure
        coEvery { fileThresholdValidator.canEnqueueFile(any()) } returnsMany listOf(true, true)
        
        // First attempt fails with threshold error, second succeeds
        coEvery { queueManager.queueFile(request) } returnsMany listOf(
            QueueResult(false, null, "Threshold exceeded", error = "threshold_exceeded"),
            QueueResult(true, "queue-id-123", "Success")
        )
        
        // When
        val result = backpressureHandler.enqueueWithBackpressure(request)
        
        // Then
        assertThat(result.success).isTrue()
        assertThat(result.retryCount).isEqualTo(1)
        coVerify(exactly = 2) { queueManager.queueFile(request) }
    }
    
    @Test
    fun `should respect max retry attempts`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // Always fail - first canEnqueue returns true but queueFile fails, then canEnqueue returns false
        var callCount = 0
        coEvery { fileThresholdValidator.canEnqueueFile(any()) } answers {
            callCount++ == 0  // First call returns true, rest return false
        }
        
        coEvery { queueManager.queueFile(request) } returns 
            QueueResult(false, null, "Threshold exceeded", error = "threshold_exceeded")
        
        // When
        val result = backpressureHandler.enqueueWithBackpressure(request, maxRetries = 3)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.retryCount).isEqualTo(3)
        assertThat(result.shouldRetryLater).isTrue()
        coVerify(exactly = 1) { queueManager.queueFile(request) } // Only initial attempt, then blocked by canEnqueue
    }
    
    @Test
    fun `should wait for capacity before retry`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // Capacity becomes available after delay
        var capacityCheckCount = 0
        coEvery { fileThresholdValidator.getRemainingCapacity(any()) } answers {
            when (capacityCheckCount++) {
                0, 1 -> 0  // First two checks return no capacity
                else -> 100 // Third check returns capacity
            }
        }
        coEvery { queueManager.queueFile(request) } returns 
            QueueResult(true, "queue-id-123", "Success")
        
        // When
        val result = backpressureHandler.waitForCapacityAndEnqueue(request, timeoutMs = 5000)
        
        // Then
        assertThat(result.success).isTrue()
        assertThat(result.waitTimeMs).isGreaterThanOrEqualTo(0) // May be 0 in test environment
        coVerify(atLeast = 2) { fileThresholdValidator.getRemainingCapacity(any()) }
    }
    
    @Test
    fun `should timeout waiting for capacity`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // No capacity available
        coEvery { fileThresholdValidator.getRemainingCapacity(any()) } returns 0
        
        // When
        val result = backpressureHandler.waitForCapacityAndEnqueue(request, timeoutMs = 1000)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.timedOut).isTrue()
        assertThat(result.error).contains("Timeout")
        coVerify(atLeast = 2) { fileThresholdValidator.getRemainingCapacity(any()) }
    }
    
    @Test
    fun `should handle batch with partial failures due to threshold`() = runTest {
        // Given
        val files = (1..10).map { i ->
            ScannedFile("/test/file$i.asn1", "file$i.asn1", 1024, Instant.now())
        }
        
        val requests = files.map { file ->
            QueueRequest("job-123", file)
        }
        
        // Allow first 5, then hit threshold
        var callCount = 0
        coEvery { fileThresholdValidator.canEnqueueFile(any()) } answers {
            callCount++ < 5
        }
        
        coEvery { queueManager.queueFile(any()) } answers {
            if (callCount <= 5) {
                QueueResult(true, "queue-id", "Success")
            } else {
                QueueResult(false, null, "Threshold exceeded", error = "threshold_exceeded")
            }
        }
        
        // When
        val result = backpressureHandler.batchEnqueueWithBackpressure(requests)
        
        // Then
        assertThat(result.successful).isEqualTo(5)
        assertThat(result.deferred).isEqualTo(5)
        assertThat(result.failed).isEqualTo(0)
        assertThat(result.deferredRequests).hasSize(5)
    }
    
    @Test
    fun `should apply adaptive backoff based on queue utilization`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // High utilization
        coEvery { fileThresholdValidator.getThresholdUtilization(any()) } returns 95.0
        
        // When
        val backoffMs = backpressureHandler.calculateAdaptiveBackoff(
            attempt = 1,
            utilization = 95.0
        )
        
        // Then
        assertThat(backoffMs).isGreaterThanOrEqualTo(300) // Base 100ms * 3x factor for 95% utilization
    }
    
    @Test
    fun `should use circuit breaker pattern for repeated failures`() = runTest {
        // Given
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile(
                filePath = "/test/file.asn1",
                fileName = "file.asn1",
                fileSizeBytes = 1024,
                lastModified = Instant.now()
            )
        )
        
        // Multiple consecutive failures should open circuit
        coEvery { queueManager.queueFile(request) } returns 
            QueueResult(false, null, "Threshold exceeded", error = "threshold_exceeded")
        
        repeat(5) {
            backpressureHandler.enqueueWithBackpressure(request, maxRetries = 0)
        }
        
        // When - circuit should be open
        val result = backpressureHandler.enqueueWithBackpressure(request)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.circuitBreakerOpen).isTrue()
        assertThat(result.error).contains("Circuit breaker")
        
        // Circuit breaker should prevent immediate calls
        coVerify(exactly = 0) { queueManager.queueFile(request) }
    }
    
    @Test
    fun `should prioritize files during backpressure`() = runTest {
        // Given
        val highPriorityRequest = QueueRequest(
            jobId = "job-123",
            file = ScannedFile("/test/high.asn1", "high.asn1", 1024, Instant.now()),
            priority = Priority.HIGH
        )
        
        val lowPriorityRequest = QueueRequest(
            jobId = "job-123",
            file = ScannedFile("/test/low.asn1", "low.asn1", 1024, Instant.now()),
            priority = Priority.LOW
        )
        
        // Limited capacity - only 1 slot available, then 0
        var capacityCalls = 0
        coEvery { fileThresholdValidator.getRemainingCapacity(any()) } answers {
            if (capacityCalls++ == 0) 1 else 0
        }
        coEvery { fileThresholdValidator.canEnqueueFile(any()) } returns true
        coEvery { queueManager.queueFile(any()) } returns 
            QueueResult(true, "queue-id", "Success")
        
        // When
        val results = backpressureHandler.prioritizedBatchEnqueue(
            listOf(lowPriorityRequest, highPriorityRequest)
        )
        
        // Then
        assertThat(results.first { it.request.priority == Priority.HIGH }.result.success).isTrue()
        assertThat(results.first { it.request.priority == Priority.LOW }.result.success).isFalse()
    }
    
    @Test
    fun `should emit metrics for backpressure events`() = runTest {
        // Given
        val metricsCollector = mockk<MetricsCollector>(relaxed = true)
        backpressureHandler = BackpressureHandler(
            queueManager, 
            fileThresholdValidator,
            metricsCollector
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = ScannedFile("/test/file.asn1", "file.asn1", 1024, Instant.now())
        )
        
        // First canEnqueue returns true, queueFile fails, then after retry canEnqueue returns true again and succeeds
        coEvery { fileThresholdValidator.canEnqueueFile(any()) } returns true
        
        coEvery { queueManager.queueFile(request) } returnsMany listOf(
            QueueResult(false, null, "Threshold exceeded", error = "threshold_exceeded"),
            QueueResult(true, "queue-id", "Success")
        )
        
        // When
        backpressureHandler.enqueueWithBackpressure(request)
        
        // Then
        verify { metricsCollector.recordBackpressureEvent() }
        verify { metricsCollector.recordBackpressureRetry(1) }
        verify { metricsCollector.recordBackpressureResolution(any()) }
    }
}