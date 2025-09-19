package com.quantumsoft.tia.scanner.e2e

import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.services.SettingsService
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createTempDirectory

@DisplayName("File Threshold Enforcement E2E Tests")
class FileThresholdEnforcementE2ETest : BaseE2ETest() {

    @Autowired
    private lateinit var queueManager: QueueManager
    
    @Autowired
    private lateinit var settingsService: SettingsService
    
    @Autowired
    private lateinit var fileThresholdValidator: FileThresholdValidator
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @LocalServerPort
    private var port: Int = 0
    
    private lateinit var testDirectory: Path
    
    @BeforeEach
    fun setUp() {
        // Clear Redis queues
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        
        // Create test directory
        testDirectory = createTempDirectory("e2e-test-")
        
        // Reset threshold settings
        runBlocking {
            settingsService.setSetting("scanner.file.threshold", 10, SettingType.INTEGER)
            settingsService.setSetting("scanner.file.threshold.enabled", true, SettingType.BOOLEAN)
        }
    }
    
    @Test
    fun `should enforce file threshold during scanning`() = runTest {
        // Given - Set a low threshold
        val threshold = 5
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Create more files than threshold
        val fileCount = 10
        val files = (1..fileCount).map { i ->
            val file = testDirectory.resolve("test-file-$i.csv")
            Files.writeString(file, "header1,header2\nvalue1,value2")
            ScannedFile(
                filePath = file.toString(),
                fileName = file.fileName.toString(),
                fileSizeBytes = Files.size(file),
                lastModified = Instant.now()
            )
        }
        
        // When - Try to queue all files
        val results = files.map { file ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "test-job-1",
                    file = file,
                    priority = Priority.NORMAL
                )
            )
        }
        
        // Then - Only files up to threshold should be queued
        val successfulCount = results.count { it.success }
        val failedCount = results.count { !it.success }
        
        assertThat(successfulCount).isLessThanOrEqualTo(threshold)
        assertThat(failedCount).isGreaterThan(0)
        
        // Verify queue statistics
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isLessThanOrEqualTo(threshold.toLong())
    }
    
    @Test
    fun `should allow queueing when threshold is disabled`() = runTest {
        // Given - Disable threshold
        settingsService.setSetting("scanner.file.threshold.enabled", false, SettingType.BOOLEAN)
        
        // Create files
        val fileCount = 20
        val files = (1..fileCount).map { i ->
            val file = testDirectory.resolve("test-file-$i.csv")
            Files.writeString(file, "test content")
            ScannedFile(
                filePath = file.toString(),
                fileName = file.fileName.toString(),
                fileSizeBytes = Files.size(file),
                lastModified = Instant.now()
            )
        }
        
        // When - Queue all files
        val results = files.map { file ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "test-job-2",
                    file = file,
                    priority = Priority.NORMAL
                )
            )
        }
        
        // Then - All files should be queued
        val successfulCount = results.count { it.success }
        assertThat(successfulCount).isEqualTo(fileCount)
        
        // Verify queue statistics
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo(fileCount.toLong())
    }
    
    @Test
    fun `should respect threshold changes in real-time`() = runTest {
        // Given - Initial threshold
        val initialThreshold = 5
        settingsService.setSetting("scanner.file.threshold", initialThreshold, SettingType.INTEGER)
        
        // Queue files up to initial threshold
        val firstBatch = (1..initialThreshold).map { i ->
            val file = testDirectory.resolve("batch1-file-$i.csv")
            Files.writeString(file, "test")
            ScannedFile(
                filePath = file.toString(),
                fileName = file.fileName.toString(),
                fileSizeBytes = 10,
                lastModified = Instant.now()
            )
        }
        
        firstBatch.forEach { file ->
            val result = queueManager.queueFile(
                QueueRequest("job-3", file, Priority.NORMAL)
            )
            assertThat(result.success).isTrue()
        }
        
        // Verify queue is at threshold
        var stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo(initialThreshold.toLong())
        
        // When - Increase threshold via REST API
        val newThreshold = 15
        val url = "http://localhost:$port/api/v1/settings/scanner.file.threshold"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(mapOf(
            "value" to newThreshold.toString(),
            "type" to "INTEGER"
        ), headers)
        
        val response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            request,
            String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        
        // Wait for synchronization
        delay(500)
        
        // Then - Should be able to queue more files
        val additionalFiles = 5
        val secondBatch = (1..additionalFiles).map { i ->
            val file = testDirectory.resolve("batch2-file-$i.csv")
            Files.writeString(file, "test")
            ScannedFile(
                filePath = file.toString(),
                fileName = file.fileName.toString(),
                fileSizeBytes = 10,
                lastModified = Instant.now()
            )
        }
        
        secondBatch.forEach { file ->
            val result = queueManager.queueFile(
                QueueRequest("job-3", file, Priority.NORMAL)
            )
            assertThat(result.success).isTrue()
        }
        
        // Verify new queue depth
        stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo((initialThreshold + additionalFiles).toLong())
    }
    
    @Test
    fun `should handle batch queue operations with threshold`() = runTest {
        // Given - Set threshold
        val threshold = 10
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Create batch of files
        val batchSize = 15
        val files = (1..batchSize).map { i ->
            ScannedFile(
                filePath = "/test/batch-$i.csv",
                fileName = "batch-$i.csv",
                fileSizeBytes = 100,
                lastModified = Instant.now()
            )
        }
        
        val requests = files.map { file ->
            QueueRequest("job-batch", file, Priority.NORMAL)
        }
        
        // When - Try batch queue
        val result = queueManager.batchQueue(requests)
        
        // Then - Should reject entire batch if it exceeds threshold
        assertThat(result.successful).isEqualTo(0)
        assertThat(result.failed).isEqualTo(batchSize)
    }
    
    @Test
    fun `should track threshold metrics via REST API`() = runTest {
        // Given - Set threshold and queue some files
        val threshold = 100
        val queuedCount = 75
        
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Queue files
        (1..queuedCount).forEach { i ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "metrics-job",
                    file = ScannedFile(
                        filePath = "/test/file-$i.csv",
                        fileName = "file-$i.csv",
                        fileSizeBytes = 100,
                        lastModified = Instant.now()
                    ),
                    priority = Priority.NORMAL
                )
            )
        }
        
        // When - Get metrics via REST API
        val metricsUrl = "http://localhost:$port/api/v1/metrics"
        val response = restTemplate.exchange(
            metricsUrl,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<Map<String, Any>>() {}
        )
        
        // Then - Metrics should include threshold information
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        
        val metrics = response.body
        assertThat(metrics).isNotNull()
        
        // Check queue statistics endpoint
        val statsUrl = "http://localhost:$port/api/v1/queue/stats"
        val statsResponse = restTemplate.getForEntity(
            statsUrl,
            QueueStatistics::class.java
        )
        
        assertThat(statsResponse.statusCode).isEqualTo(HttpStatus.OK)
        val stats = statsResponse.body
        assertThat(stats).isNotNull()
        assertThat(stats?.currentDepth).isEqualTo(queuedCount.toLong())
        
        // Check threshold utilization
        val utilization = fileThresholdValidator.getThresholdUtilization(queuedCount)
        assertThat(utilization).isEqualTo(75.0)
    }
    
    @Test
    fun `should handle concurrent file additions with threshold`() = runTest {
        // Given - Set threshold
        val threshold = 50
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // When - Concurrent queue attempts
        val concurrentRequests = 100
        val results = (1..concurrentRequests).map { i ->
            async {
                queueManager.queueFile(
                    QueueRequest(
                        jobId = "concurrent-job",
                        file = ScannedFile(
                            filePath = "/test/concurrent-$i.csv",
                            fileName = "concurrent-$i.csv",
                            fileSizeBytes = 100,
                            lastModified = Instant.now(),
                            fileHash = "hash-$i" // Unique hash to avoid duplicates
                        ),
                        priority = Priority.NORMAL
                    )
                )
            }
        }.awaitAll()
        
        // Then - Should respect threshold
        val successCount = results.count { it.success }
        assertThat(successCount).isLessThanOrEqualTo(threshold)
        
        // Verify queue depth doesn't exceed threshold
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isLessThanOrEqualTo(threshold.toLong())
    }
    
    @Test
    fun `should handle priority queues with threshold`() = runTest {
        // Given - Set threshold
        val threshold = 10
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Queue high priority files first
        val highPriorityCount = 5
        (1..highPriorityCount).forEach { i ->
            val result = queueManager.queueFile(
                QueueRequest(
                    jobId = "priority-job",
                    file = ScannedFile(
                        filePath = "/test/high-$i.csv",
                        fileName = "high-$i.csv",
                        fileSizeBytes = 100,
                        lastModified = Instant.now()
                    ),
                    priority = Priority.HIGH
                )
            )
            assertThat(result.success).isTrue()
        }
        
        // Queue normal priority files
        val normalPriorityCount = 5
        (1..normalPriorityCount).forEach { i ->
            val result = queueManager.queueFile(
                QueueRequest(
                    jobId = "priority-job",
                    file = ScannedFile(
                        filePath = "/test/normal-$i.csv",
                        fileName = "normal-$i.csv",
                        fileSizeBytes = 100,
                        lastModified = Instant.now()
                    ),
                    priority = Priority.NORMAL
                )
            )
            assertThat(result.success).isTrue()
        }
        
        // Try to queue low priority file (should fail due to threshold)
        val lowPriorityResult = queueManager.queueFile(
            QueueRequest(
                jobId = "priority-job",
                file = ScannedFile(
                    filePath = "/test/low-1.csv",
                    fileName = "low-1.csv",
                    fileSizeBytes = 100,
                    lastModified = Instant.now()
                ),
                priority = Priority.LOW
            )
        )
        
        // Then - Low priority should be rejected
        assertThat(lowPriorityResult.success).isFalse()
        assertThat(lowPriorityResult.error).contains("threshold")
        
        // Verify queue statistics by priority
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.depthByPriority[Priority.HIGH]).isEqualTo(highPriorityCount.toLong())
        assertThat(stats.depthByPriority[Priority.NORMAL]).isEqualTo(normalPriorityCount.toLong())
        assertThat(stats.depthByPriority[Priority.LOW]).isEqualTo(0L)
    }
}