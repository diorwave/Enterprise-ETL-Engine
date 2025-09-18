package com.quantumsoft.tia.scanner.components

import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.*
import java.time.Duration
import java.time.Instant

class QueueManagerTest {

    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var valueOps: ValueOperations<String, String>
    private lateinit var listOps: ListOperations<String, String>
    private lateinit var metricsCollector: MetricsCollector
    private lateinit var fileThresholdValidator: FileThresholdValidator
    private lateinit var objectMapper: com.fasterxml.jackson.databind.ObjectMapper
    private lateinit var queueManager: QueueManager
    
    @BeforeEach
    fun setUp() {
        redisTemplate = mockk(relaxed = true)
        valueOps = mockk(relaxed = true)
        listOps = mockk(relaxed = true)
        metricsCollector = mockk(relaxed = true)
        fileThresholdValidator = mockk(relaxed = true)
        objectMapper = com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
        
        every { redisTemplate.opsForValue() } returns valueOps
        every { redisTemplate.opsForList() } returns listOps
        every { redisTemplate.keys(any()) } returns emptySet()
        
        queueManager = QueueManager(redisTemplate, metricsCollector, objectMapper, fileThresholdValidator)
    }
    
    @Test
    fun `should queue file successfully`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file,
            priority = Priority.NORMAL
        )
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isTrue()
        assertThat(result.queueId).isNotNull()
        assertThat(result.message).contains("successfully")
        
        verify { listOps.rightPush(any(), any()) }
        verify { metricsCollector.recordFilesQueued(1) }
    }
    
    @Test
    fun `should detect duplicate files`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file
        )
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns false
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.duplicateDetected).isTrue()
        assertThat(result.message).contains("already queued")
        
        verify(exactly = 0) { listOps.rightPush(any(), any()) }
    }
    
    @Test
    fun `should handle different priorities`() = runTest {
        // Given
        val highPriorityFile = ScannedFile(
            filePath = "/test/high.asn1",
            fileName = "high.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now()
        )
        
        val lowPriorityFile = ScannedFile(
            filePath = "/test/low.asn1",
            fileName = "low.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now()
        )
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val highResult = queueManager.queueFile(
            QueueRequest("job-1", highPriorityFile, Priority.HIGH)
        )
        val lowResult = queueManager.queueFile(
            QueueRequest("job-2", lowPriorityFile, Priority.LOW)
        )
        
        // Then
        assertThat(highResult.success).isTrue()
        assertThat(lowResult.success).isTrue()
        
        verify { listOps.rightPush(match { it.contains("high") }, any()) }
        verify { listOps.rightPush(match { it.contains("low") }, any()) }
    }
    
    @Test
    fun `should batch queue multiple files`() = runTest {
        // Given
        val files = listOf(
            ScannedFile("/test/file1.asn1", "file1.asn1", 1024, Instant.now()),
            ScannedFile("/test/file2.asn1", "file2.asn1", 2048, Instant.now()),
            ScannedFile("/test/file3.asn1", "file3.asn1", 3072, Instant.now())
        )
        
        val requests = files.map { file ->
            QueueRequest("job-123", file, Priority.NORMAL)
        }
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.batchQueue(requests)
        
        // Then
        assertThat(result.totalRequests).isEqualTo(3)
        assertThat(result.successful).isEqualTo(3)
        assertThat(result.failed).isEqualTo(0)
        assertThat(result.results).hasSize(3)
        
        verify(exactly = 3) { listOps.rightPush(any(), any()) }
    }
    
    @Test
    fun `should move failed messages to dead letter queue`() = runTest {
        // Given
        val queueId = "test-queue-id"
        val reason = "Processing failed"
        
        // When
        val result = queueManager.moveToDeadLetter(queueId, reason)
        
        // Then
        assertThat(result).isTrue()
        
        verify { listOps.rightPush(match { it.contains("dlq") }, any()) }
        verify { redisTemplate.expire(any(), any()) }
        verify { metricsCollector.recordError("moved_to_dlq") }
    }
    
    @Test
    fun `should handle retry count`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/retry.asn1",
            fileName = "retry.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now()
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file,
            retryCount = 2,
            maxRetries = 3
        )
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isTrue()
        
        verify { valueOps.set(match { it.contains("retry") }, "2", any<Duration>()) }
    }
    
    @Test
    fun `should get queue statistics`() = runTest {
        // Given
        every { listOps.size("tia:scanner:queue:high") } returns 5L
        every { listOps.size("tia:scanner:queue:normal") } returns 10L
        every { listOps.size("tia:scanner:queue:low") } returns 3L
        every { redisTemplate.keys("tia:scanner:dlq:*") } returns setOf("tia:scanner:dlq:1")
        every { listOps.size("tia:scanner:dlq:1") } returns 2L
        
        // When
        val stats = queueManager.getQueueStatistics()
        
        // Then
        assertThat(stats.currentDepth).isEqualTo(18L)
        assertThat(stats.depthByPriority[Priority.HIGH]).isEqualTo(5L)
        assertThat(stats.depthByPriority[Priority.NORMAL]).isEqualTo(10L)
        assertThat(stats.depthByPriority[Priority.LOW]).isEqualTo(3L)
        assertThat(stats.deadLetterCount).isEqualTo(2L)
    }
    
    @Test
    fun `should cleanup expired locks`() = runTest {
        // Given
        val lockKeys = setOf(
            "tia:scanner:lock:file1",
            "tia:scanner:lock:file2",
            "tia:scanner:lock:file3"
        )
        
        every { redisTemplate.keys("tia:scanner:lock:*") } returns lockKeys
        every { redisTemplate.getExpire("tia:scanner:lock:file1", any()) } returns -1L
        every { redisTemplate.getExpire("tia:scanner:lock:file2", any()) } returns 100L
        every { redisTemplate.getExpire("tia:scanner:lock:file3", any()) } returns -2L
        every { redisTemplate.delete(any<String>()) } returns true
        
        // When
        val cleaned = queueManager.cleanupExpiredLocks()
        
        // Then
        assertThat(cleaned).isEqualTo(2)
        
        verify { redisTemplate.delete("tia:scanner:lock:file1") }
        verify { redisTemplate.delete("tia:scanner:lock:file3") }
        verify(exactly = 0) { redisTemplate.delete("tia:scanner:lock:file2") }
    }
    
    @Test
    fun `should handle Redis connection failure`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now()
        )
        
        val request = QueueRequest("job-123", file)
        
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } throws RuntimeException("Redis connection failed")
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.error).contains("Redis connection failed")
        
        verify { metricsCollector.recordError("queue_failure") }
    }
    
    @Test
    fun `should handle batch queue with mixed results`() = runTest {
        // Given
        val files = listOf(
            ScannedFile("/test/file1.asn1", "file1.asn1", 1024, Instant.now(), "hash1"),
            ScannedFile("/test/file2.asn1", "file2.asn1", 2048, Instant.now(), "hash2"),
            ScannedFile("/test/file3.asn1", "file3.asn1", 3072, Instant.now(), "hash3")
        )
        
        val requests = files.map { file ->
            QueueRequest("job-123", file)
        }
        
        // First file succeeds, second is duplicate, third succeeds
        every { valueOps.setIfAbsent(match { it.contains("hash1") }, any(), any<Duration>()) } returns true
        every { valueOps.setIfAbsent(match { it.contains("hash2") }, any(), any<Duration>()) } returns false
        every { valueOps.setIfAbsent(match { it.contains("hash3") }, any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.batchQueue(requests)
        
        // Then
        assertThat(result.totalRequests).isEqualTo(3)
        assertThat(result.successful).isEqualTo(2)
        assertThat(result.failed).isEqualTo(0)
        assertThat(result.duplicates).isEqualTo(1)
        
        verify(exactly = 2) { listOps.rightPush(any(), any()) }
    }
    
    @Test
    fun `should check threshold before queueing file`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file,
            priority = Priority.NORMAL
        )
        
        every { listOps.size(any()) } returns 10L
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns true
        coEvery { fileThresholdValidator.validateThreshold(any()) } just Runs
        coEvery { fileThresholdValidator.isNearThreshold(10, 80.0) } returns false
        coEvery { fileThresholdValidator.getThresholdUtilization(10) } returns 10.0
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isTrue()
        coVerify { fileThresholdValidator.validateThreshold(any()) }
    }
    
    @Test
    fun `should reject file when threshold exceeded`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file,
            priority = Priority.NORMAL
        )
        
        every { listOps.size(any()) } returns 100L
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns true
        coEvery { fileThresholdValidator.validateThreshold(any()) } throws 
            ThresholdExceededException("Queue size exceeds threshold", 100, 50)
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isFalse()
        assertThat(result.error).isNotNull()
        assertThat(result.error).contains("threshold")
        verify { metricsCollector.recordError("threshold_exceeded") }
        verify(exactly = 0) { listOps.rightPush(any(), any()) }
    }
    
    @Test
    fun `should skip threshold check when disabled`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest(
            jobId = "job-123",
            file = file,
            priority = Priority.NORMAL
        )
        
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns false
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isTrue()
        coVerify(exactly = 0) { fileThresholdValidator.validateThreshold(any()) }
    }
    
    @Test
    fun `should check batch threshold before queueing`() = runTest {
        // Given
        val files = listOf(
            ScannedFile("/test/file1.asn1", "file1.asn1", 1024, Instant.now(), "hash1"),
            ScannedFile("/test/file2.asn1", "file2.asn1", 2048, Instant.now(), "hash2"),
            ScannedFile("/test/file3.asn1", "file3.asn1", 3072, Instant.now(), "hash3")
        )
        
        val requests = files.map { file ->
            QueueRequest("job-123", file)
        }
        
        every { listOps.size(any()) } returns 40L
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns true
        coEvery { fileThresholdValidator.canEnqueueBatch(any(), any()) } returns true
        coEvery { fileThresholdValidator.validateThreshold(any()) } just Runs
        coEvery { fileThresholdValidator.isNearThreshold(any(), any()) } returns false
        coEvery { fileThresholdValidator.getThresholdUtilization(any()) } returns 40.0
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.batchQueue(requests)
        
        // Then
        assertThat(result.successful).isEqualTo(3)
        coVerify { fileThresholdValidator.canEnqueueBatch(any(), any()) }
    }
    
    @Test
    fun `should reject batch when would exceed threshold`() = runTest {
        // Given
        val files = (1..5).map { i ->
            ScannedFile("/test/file$i.asn1", "file$i.asn1", 1024, Instant.now(), "hash$i")
        }
        
        val requests = files.map { file ->
            QueueRequest("job-123", file)
        }
        
        every { listOps.size(any()) } returns 97L
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns true
        coEvery { fileThresholdValidator.canEnqueueBatch(any(), any()) } returns false
        
        // When
        val result = queueManager.batchQueue(requests)
        
        // Then
        assertThat(result.successful).isEqualTo(0)
        assertThat(result.failed).isEqualTo(5)
        assertThat(result.results.all { !it.success }).isTrue()
        verify { metricsCollector.recordError("batch_threshold_exceeded") }
    }
    
    @Test
    fun `should record threshold metrics`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/file.asn1",
            fileName = "file.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "abc123"
        )
        
        val request = QueueRequest("job-123", file)
        
        every { listOps.size(any()) } returns 75L
        coEvery { fileThresholdValidator.isThresholdEnabled() } returns true
        coEvery { fileThresholdValidator.isNearThreshold(any(), any()) } returns true
        coEvery { fileThresholdValidator.getThresholdUtilization(any()) } returns 75.0
        coEvery { fileThresholdValidator.validateThreshold(any()) } just Runs
        every { valueOps.setIfAbsent(any(), any(), any<Duration>()) } returns true
        
        // When
        val result = queueManager.queueFile(request)
        
        // Then
        assertThat(result.success).isTrue()
        verify { metricsCollector.recordThresholdUtilization(any()) }
        verify { metricsCollector.recordThresholdWarning() }
    }
}
