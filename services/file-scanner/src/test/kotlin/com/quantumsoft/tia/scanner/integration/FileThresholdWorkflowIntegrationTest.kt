package com.quantumsoft.tia.scanner.integration

import com.quantumsoft.tia.scanner.components.DirectoryScanner
import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.entities.ScanJob
import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import com.quantumsoft.tia.scanner.scheduler.JobScheduler
import com.quantumsoft.tia.scanner.services.SettingsService
import com.quantumsoft.tia.scanner.services.ScanJobService
import com.quantumsoft.tia.scanner.validators.FileThresholdValidator
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.UUID

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration", "test")
@DisplayName("File Threshold Workflow Integration Tests")
class FileThresholdWorkflowIntegrationTest {

    @Autowired
    private lateinit var directoryScanner: DirectoryScanner
    
    @Autowired
    private lateinit var queueManager: QueueManager
    
    @Autowired
    private lateinit var settingsService: SettingsService
    
    @Autowired
    private lateinit var fileThresholdValidator: FileThresholdValidator
    
    @Autowired
    private lateinit var scanJobService: ScanJobService
    
    @Autowired
    private lateinit var scanJobRepository: ScanJobRepository
    
    @Autowired
    private lateinit var jobScheduler: JobScheduler
    
    @Autowired
    private lateinit var metricsCollector: MetricsCollector
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>
    
    @TempDir
    lateinit var tempDir: Path
    
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")
            .apply {
                withDatabaseName("test_db")
                withUsername("test")
                withPassword("test")
            }
        
        @Container
        @JvmStatic
        val redis = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine"))
            .apply {
                withExposedPorts(6379)
            }
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
        }
    }
    
    @BeforeEach
    fun setUp() {
        // Clear Redis
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        
        // Clear database
        scanJobRepository.deleteAll()
        
        // Reset settings
        runBlocking {
            settingsService.setSetting("scanner.file.threshold", 20, SettingType.INTEGER)
            settingsService.setSetting("scanner.file.threshold.enabled", true, SettingType.BOOLEAN)
        }
        
        // Reset metrics
        metricsCollector.reset()
    }
    
    @Test
    fun `should complete full threshold workflow from scan to queue`() = runTest {
        // Given - Create test files
        val fileCount = 30
        val testFiles = createTestFiles(fileCount)
        
        // Set threshold lower than file count
        val threshold = 15
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Create scan job
        val scanJob = ScanJob(
            id = UUID.randomUUID(),
            name = "Threshold Test Job",
            sourceDirectory = tempDir.toString(),
            filePattern = "*.csv",
            parserId = "test-parser",
            isActive = true,
            scanIntervalType = ScanIntervalType.CRON,
            scanIntervalValue = "0 */5 * * * ?"
        )
        
        val savedJob = scanJobRepository.save(scanJob)
        
        // When - Execute scan
        val scanResult = directoryScanner.scan(
            ScanConfiguration(
                sourceDirectory = tempDir.toString(),
                filePattern = "*.csv",
                recursiveScan = false,
                maxDepth = 1
            )
        )
        
        // Queue scanned files
        var successCount = 0
        var failedCount = 0
        
        scanResult.files.forEach { file ->
            val queueResult = queueManager.queueFile(
                QueueRequest(
                    jobId = savedJob.id.toString(),
                    file = file,
                    priority = Priority.NORMAL
                )
            )
            
            if (queueResult.success) successCount++ else failedCount++
        }
        
        // Then - Verify threshold enforcement
        assertThat(scanResult.files).hasSize(fileCount)
        assertThat(successCount).isLessThanOrEqualTo(threshold)
        assertThat(failedCount).isEqualTo(fileCount - successCount)
        
        // Verify queue statistics
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isLessThanOrEqualTo(threshold.toLong())
        
        // Verify metrics
        val metricsSummary = metricsCollector.getMetricsSummary()
        assertThat(metricsSummary.totalFilesScanned).isEqualTo(fileCount.toLong())
        assertThat(metricsSummary.totalFilesQueued).isEqualTo(successCount.toLong())
        
        // Verify threshold validator state
        val utilization = fileThresholdValidator.getThresholdUtilization(stats.currentDepth.toInt())
        assertThat(utilization).isLessThanOrEqualTo(100.0)
        
        val remainingCapacity = fileThresholdValidator.getRemainingCapacity(stats.currentDepth.toInt())
        assertThat(remainingCapacity).isGreaterThanOrEqualTo(0)
    }
    
    @Test
    fun `should handle dynamic threshold changes during scanning`() = runTest {
        // Given - Initial high threshold
        val initialThreshold = 50
        settingsService.setSetting("scanner.file.threshold", initialThreshold, SettingType.INTEGER)
        
        // Create files
        val fileCount = 40
        createTestFiles(fileCount)
        
        // Start scanning and queueing
        val scanResult = directoryScanner.scan(
            ScanConfiguration(
                sourceDirectory = tempDir.toString(),
                filePattern = "*.csv",
                recursiveScan = false
            )
        )
        
        // Queue first batch
        var queuedCount = 0
        for (i in 0 until 20) {
            val result = queueManager.queueFile(
                QueueRequest(
                    jobId = "dynamic-test",
                    file = scanResult.files[i],
                    priority = Priority.NORMAL
                )
            )
            if (result.success) queuedCount++
        }
        
        assertThat(queuedCount).isEqualTo(20)
        
        // When - Reduce threshold
        val newThreshold = 25
        settingsService.setSetting("scanner.file.threshold", newThreshold, SettingType.INTEGER)
        
        // Wait for setting propagation
        delay(500)
        
        // Try to queue more files
        for (i in 20 until 30) {
            val result = queueManager.queueFile(
                QueueRequest(
                    jobId = "dynamic-test",
                    file = scanResult.files[i],
                    priority = Priority.NORMAL
                )
            )
            if (result.success) queuedCount++
        }
        
        // Then - Should respect new threshold
        assertThat(queuedCount).isLessThanOrEqualTo(newThreshold)
        
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isLessThanOrEqualTo(newThreshold.toLong())
    }
    
    @Test
    fun `should process workflow with scheduled job and threshold`() = runTest {
        // Given - Create scheduled job with threshold
        val threshold = 10
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        createTestFiles(20)
        
        val jobConfig = ScanJob(
            id = UUID.randomUUID(),
            name = "Scheduled Threshold Job",
            sourceDirectory = tempDir.toString(),
            filePattern = "*.csv",
            parserId = "test-parser",
            scanIntervalType = ScanIntervalType.FIXED,
            scanIntervalValue = "PT5S",
            isActive = true
        )
        
        // When - Schedule and execute job
        val savedJob = scanJobRepository.save(jobConfig)
        jobScheduler.scheduleJob(savedJob)
        
        // Wait for job execution
        delay(2000)
        
        // Then - Verify threshold was respected
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isLessThanOrEqualTo(threshold.toLong())
        
        // Verify job status
        val executedJob = scanJobRepository.findAll().find { it.name == jobConfig.name }
        assertThat(executedJob).isNotNull()
        
        // Clean up
        jobScheduler.unscheduleJob(savedJob.id)
    }
    
    @Test
    fun `should handle threshold with different file priorities`() = runTest {
        // Given - Set threshold
        val threshold = 15
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Create files with different priorities
        val highPriorityFiles = createTestFiles(5, "high")
        val normalPriorityFiles = createTestFiles(10, "normal")
        val lowPriorityFiles = createTestFiles(10, "low")
        
        // When - Queue with priorities
        val highResults = highPriorityFiles.map { path ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "priority-test",
                    file = pathToScannedFile(path),
                    priority = Priority.HIGH
                )
            )
        }
        
        val normalResults = normalPriorityFiles.map { path ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "priority-test",
                    file = pathToScannedFile(path),
                    priority = Priority.NORMAL
                )
            )
        }
        
        val lowResults = lowPriorityFiles.map { path ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "priority-test",
                    file = pathToScannedFile(path),
                    priority = Priority.LOW
                )
            )
        }
        
        // Then - High priority should succeed, others limited by threshold
        assertThat(highResults.all { it.success }).isTrue()
        assertThat(normalResults.count { it.success }).isEqualTo(10) // Fills up to threshold
        assertThat(lowResults.all { !it.success }).isTrue() // All rejected
        
        val stats = queueManager.getQueueStatistics()
        assertThat(stats.currentDepth).isEqualTo(threshold.toLong())
        assertThat(stats.depthByPriority[Priority.HIGH]).isEqualTo(5L)
        assertThat(stats.depthByPriority[Priority.NORMAL]).isEqualTo(10L)
        assertThat(stats.depthByPriority[Priority.LOW]).isEqualTo(0L)
    }
    
    @Test
    fun `should recover from threshold breach with retry mechanism`() = runTest {
        // Given - Low threshold
        val threshold = 5
        settingsService.setSetting("scanner.file.threshold", threshold, SettingType.INTEGER)
        
        // Create files
        val files = createTestFiles(10)
        
        // When - Try to queue all (some will fail)
        val initialResults = files.map { path ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "retry-test",
                    file = pathToScannedFile(path),
                    priority = Priority.NORMAL
                )
            )
        }
        
        val initialSuccess = initialResults.count { it.success }
        assertThat(initialSuccess).isEqualTo(threshold)
        
        // Process some files (simulate consumption)
        repeat(3) {
            // Simulate file processing by clearing from queue
            redisTemplate.opsForList().leftPop("tia:scanner:queue:normal")
        }
        
        // Retry failed files
        val failedFiles = files.filterIndexed { index, _ -> 
            !initialResults[index].success 
        }
        
        val retryResults = failedFiles.map { path ->
            queueManager.queueFile(
                QueueRequest(
                    jobId = "retry-test",
                    file = pathToScannedFile(path),
                    priority = Priority.NORMAL,
                    retryCount = 1
                )
            )
        }
        
        // Then - Some retries should succeed
        val retrySuccess = retryResults.count { it.success }
        assertThat(retrySuccess).isGreaterThanOrEqualTo(3)
        
        val finalStats = queueManager.getQueueStatistics()
        assertThat(finalStats.currentDepth).isLessThanOrEqualTo(threshold.toLong())
    }
    
    private fun createTestFiles(count: Int, prefix: String = "test"): List<Path> {
        return (1..count).map { i ->
            val file = tempDir.resolve("$prefix-file-$i.csv")
            Files.writeString(file, "header1,header2,header3\nvalue1,value2,value3\n")
            file
        }
    }
    
    private fun pathToScannedFile(path: Path): ScannedFile {
        return ScannedFile(
            filePath = path.toString(),
            fileName = path.fileName.toString(),
            fileSizeBytes = Files.size(path),
            lastModified = Instant.now(),
            fileHash = path.fileName.toString().hashCode().toString()
        )
    }
}