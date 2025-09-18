package com.quantumsoft.tia.scanner.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class MetricsCollectorImplTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metricsCollector: MetricsCollectorImpl
    
    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metricsCollector = MetricsCollectorImpl(meterRegistry)
    }
    
    @Test
    fun `should record scan duration`() {
        // Given
        val directory = "/test/dir"
        val duration = Duration.ofSeconds(5)
        
        // When
        metricsCollector.recordScanDuration(directory, duration)
        
        // Then
        val timer = meterRegistry.find("scanner.scan.duration").timer()
        assertThat(timer).isNotNull()
        assertThat(timer?.count()).isEqualTo(1)
        assertThat(timer?.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(5.0)
        
        val timerByDir = meterRegistry.find("scanner.scan.duration.by.directory")
            .tag("directory", directory)
            .timer()
        assertThat(timerByDir).isNotNull()
    }
    
    @Test
    fun `should record files scanned`() {
        // Given
        val count = 10
        
        // When
        metricsCollector.recordFilesScanned(count)
        
        // Then
        val counter = meterRegistry.find("scanner.files.scanned").counter()
        assertThat(counter).isNotNull()
        assertThat(counter?.count()).isEqualTo(10.0)
    }
    
    @Test
    fun `should record scan rate`() {
        // Given
        val filesPerSecond = 100.5
        
        // When
        metricsCollector.recordScanRate(filesPerSecond)
        
        // Then
        val gauge = meterRegistry.find("scanner.scan.rate").gauge()
        assertThat(gauge).isNotNull()
        assertThat(gauge?.value()).isEqualTo(filesPerSecond)
    }
    
    @Test
    fun `should record errors by type`() {
        // Given
        val errorType = "file_not_found"
        
        // When
        metricsCollector.recordError(errorType)
        metricsCollector.recordError(errorType)
        metricsCollector.recordError("permission_denied")
        
        // Then
        val totalErrors = meterRegistry.find("scanner.errors").counter()
        assertThat(totalErrors?.count()).isEqualTo(3.0)
        
        val fileNotFoundErrors = meterRegistry.find("scanner.errors.by.type")
            .tag("type", errorType)
            .counter()
        assertThat(fileNotFoundErrors?.count()).isEqualTo(2.0)
        
        val permissionErrors = meterRegistry.find("scanner.errors.by.type")
            .tag("type", "permission_denied")
            .counter()
        assertThat(permissionErrors?.count()).isEqualTo(1.0)
    }
    
    @Test
    fun `should record skipped files by reason`() {
        // Given
        val reason1 = "too_large"
        val reason2 = "invalid_format"
        
        // When
        metricsCollector.recordSkippedFile(reason1)
        metricsCollector.recordSkippedFile(reason1)
        metricsCollector.recordSkippedFile(reason2)
        
        // Then
        val totalSkipped = meterRegistry.find("scanner.files.skipped").counter()
        assertThat(totalSkipped?.count()).isEqualTo(3.0)
        
        val tooLargeSkipped = meterRegistry.find("scanner.files.skipped.by.reason")
            .tag("reason", reason1)
            .counter()
        assertThat(tooLargeSkipped?.count()).isEqualTo(2.0)
    }
    
    @Test
    fun `should record files queued`() {
        // Given
        val count = 25
        
        // When
        metricsCollector.recordFilesQueued(count)
        
        // Then
        val counter = meterRegistry.find("scanner.files.queued").counter()
        assertThat(counter?.count()).isEqualTo(25.0)
    }
    
    @Test
    fun `should record queue depth`() {
        // Given
        val depth = 150L
        
        // When
        metricsCollector.recordQueueDepth(depth)
        
        // Then
        val gauge = meterRegistry.find("scanner.queue.depth").gauge()
        assertThat(gauge).isNotNull()
        assertThat(gauge?.value()).isEqualTo(150.0)
    }
    
    @Test
    fun `should record processing duration`() {
        // Given
        val duration = Duration.ofMillis(250)
        
        // When
        metricsCollector.recordProcessingDuration(duration)
        metricsCollector.recordProcessingDuration(Duration.ofMillis(350))
        
        // Then
        val timer = meterRegistry.find("scanner.processing.duration").timer()
        assertThat(timer).isNotNull()
        assertThat(timer?.count()).isEqualTo(2)
        assertThat(timer?.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(600.0)
    }
    
    @Test
    fun `should get metrics summary`() {
        // Given
        metricsCollector.recordFilesScanned(100)
        metricsCollector.recordFilesQueued(80)
        metricsCollector.recordError("test_error")
        metricsCollector.recordError("another_error")
        metricsCollector.recordSkippedFile("too_large")
        metricsCollector.recordQueueDepth(50)
        metricsCollector.recordScanDuration("/test", Duration.ofSeconds(10))
        metricsCollector.recordProcessingDuration(Duration.ofMillis(500))
        
        // When
        val summary = metricsCollector.getMetricsSummary()
        
        // Then
        assertThat(summary.totalFilesScanned).isEqualTo(100L)
        assertThat(summary.totalFilesQueued).isEqualTo(80L)
        assertThat(summary.totalErrors).isEqualTo(2L)
        assertThat(summary.totalFilesSkipped).isEqualTo(1L)
        assertThat(summary.currentQueueDepth).isEqualTo(50L)
        assertThat(summary.averageScanDuration.seconds).isEqualTo(10)
        assertThat(summary.averageProcessingDuration.toMillis()).isEqualTo(500)
    }
    
    @Test
    fun `should handle multiple recordings correctly`() {
        // Given/When
        repeat(5) {
            metricsCollector.recordFilesScanned(10)
            metricsCollector.recordFilesQueued(8)
            metricsCollector.recordScanDuration("/dir$it", Duration.ofSeconds(it.toLong() + 1))
        }
        
        // Then
        val summary = metricsCollector.getMetricsSummary()
        assertThat(summary.totalFilesScanned).isEqualTo(50L)
        assertThat(summary.totalFilesQueued).isEqualTo(40L)
        
        val timer = meterRegistry.find("scanner.scan.duration").timer()
        assertThat(timer?.count()).isEqualTo(5)
    }
}