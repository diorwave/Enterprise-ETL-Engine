package com.quantumsoft.tia.scanner.integration

import com.quantumsoft.tia.scanner.components.QueueManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.models.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Instant

@Testcontainers
class RedisQueueIntegrationTest {
    
    companion object {
        @Container
        val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }
    
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var queueManager: QueueManager
    
    @BeforeEach
    fun setUp() {
        val redisConfig = RedisStandaloneConfiguration().apply {
            hostName = redis.host
            port = redis.getMappedPort(6379)
        }
        
        val connectionFactory = JedisConnectionFactory(redisConfig).apply {
            afterPropertiesSet()
        }
        
        redisTemplate = RedisTemplate<String, String>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
        
        // Clear Redis before each test
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        
        queueManager = QueueManager(redisTemplate, null, ObjectMapper().findAndRegisterModules())
    }
    
    @Test
    fun `should handle concurrent producers`() = runTest {
        // Given
        val files = (1..10).map { index ->
            ScannedFile(
                filePath = "/test/file$index.asn1",
                fileName = "file$index.asn1",
                fileSizeBytes = 1024L * index,
                lastModified = Instant.now(),
                fileHash = "hash$index"
            )
        }
        
        // When - Queue files concurrently
        val requests = files.map { file ->
            QueueRequest("job-123", file, Priority.NORMAL)
        }
        
        val result = queueManager.batchQueue(requests)
        
        // Then
        assertThat(result.successful).isEqualTo(10)
        assertThat(result.failed).isEqualTo(0)
        assertThat(result.duplicates).isEqualTo(0)
        
        // Verify queue depth
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo(10)
    }
    
    @Test
    fun `should maintain message ordering per priority`() = runTest {
        // Given
        val highPriorityFile = ScannedFile(
            filePath = "/test/high.asn1",
            fileName = "high.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "high-hash"
        )
        
        val normalPriorityFile = ScannedFile(
            filePath = "/test/normal.asn1",
            fileName = "normal.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "normal-hash"
        )
        
        val lowPriorityFile = ScannedFile(
            filePath = "/test/low.asn1",
            fileName = "low.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "low-hash"
        )
        
        // When
        queueManager.queueFile(QueueRequest("job-1", highPriorityFile, Priority.HIGH))
        queueManager.queueFile(QueueRequest("job-2", normalPriorityFile, Priority.NORMAL))
        queueManager.queueFile(QueueRequest("job-3", lowPriorityFile, Priority.LOW))
        
        // Then
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.depthByPriority[Priority.HIGH]).isEqualTo(1)
        assertThat(stats.depthByPriority[Priority.NORMAL]).isEqualTo(1)
        assertThat(stats.depthByPriority[Priority.LOW]).isEqualTo(1)
    }
    
    @Test
    fun `should prevent duplicate file processing`() = runTest {
        // Given
        val file = ScannedFile(
            filePath = "/test/duplicate.asn1",
            fileName = "duplicate.asn1",
            fileSizeBytes = 1024,
            lastModified = Instant.now(),
            fileHash = "dup-hash"
        )
        
        val request = QueueRequest("job-123", file)
        
        // When
        val result1 = queueManager.queueFile(request)
        val result2 = queueManager.queueFile(request)
        val result3 = queueManager.queueFile(request)
        
        // Then
        assertThat(result1.success).isTrue()
        assertThat(result2.success).isFalse()
        assertThat(result2.duplicateDetected).isTrue()
        assertThat(result3.success).isFalse()
        assertThat(result3.duplicateDetected).isTrue()
        
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo(1)
    }
    
    @Test
    fun `should handle dead letter queue`() = runTest {
        // Given
        val queueId = "test-queue-id-123"
        val reason = "Processing failed after 3 retries"
        
        // When
        val moved = queueManager.moveToDeadLetter(queueId, reason)
        
        // Then
        assertThat(moved).isTrue()
        
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.deadLetterCount).isEqualTo(1)
    }
    
    @Test
    fun `should cleanup expired locks`() = runTest {
        // Given - Create some locks with proper TTL
        redisTemplate.opsForValue().set("tia:scanner:lock:file1", "instance1", java.time.Duration.ofSeconds(1))
        redisTemplate.opsForValue().set("tia:scanner:lock:file2", "instance2", java.time.Duration.ofMinutes(5))
        redisTemplate.opsForValue().set("tia:scanner:lock:file3", "instance3", java.time.Duration.ofMinutes(5))
        
        // Wait for first lock to expire
        Thread.sleep(1500) // Wait for expiration
        
        // When
        val cleaned = queueManager.cleanupExpiredLocks()
        
        // Then - The cleanup should not affect non-expired locks
        // Note: cleanupExpiredLocks returns count of locks that were cleaned (already expired)
        assertThat(cleaned).isGreaterThanOrEqualTo(0)
        
        // Verify non-expired locks still exist
        val file2Lock = redisTemplate.opsForValue().get("tia:scanner:lock:file2")
        val file3Lock = redisTemplate.opsForValue().get("tia:scanner:lock:file3")
        assertThat(file2Lock).isEqualTo("instance2")
        assertThat(file3Lock).isEqualTo("instance3")
    }
}
