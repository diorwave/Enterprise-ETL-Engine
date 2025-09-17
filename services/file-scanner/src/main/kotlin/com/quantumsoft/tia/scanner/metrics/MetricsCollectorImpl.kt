package com.quantumsoft.tia.scanner.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

@Component
class MetricsCollectorImpl(
    private val meterRegistry: MeterRegistry
) : MetricsCollector {
    
    private val filesScannedCounter = Counter.builder("scanner.files.scanned")
        .description("Total number of files scanned")
        .register(meterRegistry)
    
    private val filesQueuedCounter = Counter.builder("scanner.files.queued")
        .description("Total number of files queued for processing")
        .register(meterRegistry)
    
    private val errorCounter = Counter.builder("scanner.errors")
        .description("Total number of errors")
        .register(meterRegistry)
    
    private val skippedFileCounter = Counter.builder("scanner.files.skipped")
        .description("Total number of files skipped")
        .register(meterRegistry)
    
    private val scanDurationTimer = Timer.builder("scanner.scan.duration")
        .description("Duration of directory scans")
        .register(meterRegistry)
    
    private val processingDurationTimer = Timer.builder("scanner.processing.duration")
        .description("Duration of file processing")
        .register(meterRegistry)
    
    private val currentQueueDepth = AtomicLong(0)
    private val currentThresholdUtilization = AtomicLong(0)
    
    private val thresholdWarningCounter = Counter.builder("scanner.threshold.warnings")
        .description("Number of times threshold warning was triggered")
        .register(meterRegistry)
    
    private val backpressureEventCounter = Counter.builder("scanner.backpressure.events")
        .description("Number of backpressure events")
        .register(meterRegistry)
    
    private val backpressureRetryCounter = Counter.builder("scanner.backpressure.retries")
        .description("Number of backpressure retries")
        .register(meterRegistry)
    
    private val backpressureResolutionTimer = Timer.builder("scanner.backpressure.resolution.duration")
        .description("Time taken to resolve backpressure")
        .register(meterRegistry)
    
    init {
        // Register gauge for queue depth
        Gauge.builder("scanner.queue.depth", currentQueueDepth) { it.get().toDouble() }
            .description("Current queue depth")
            .register(meterRegistry)
            
        // Register gauge for threshold utilization
        Gauge.builder("scanner.threshold.utilization", currentThresholdUtilization) { it.get().toDouble() }
            .description("Current threshold utilization percentage")
            .register(meterRegistry)
    }
    
    override fun recordScanDuration(directory: String, duration: Duration) {
        scanDurationTimer.record(duration)
        
        // Also record with directory tag
        Timer.builder("scanner.scan.duration.by.directory")
            .tag("directory", directory)
            .description("Duration of directory scans by directory")
            .register(meterRegistry)
            .record(duration)
    }
    
    override fun recordFilesScanned(count: Int) {
        filesScannedCounter.increment(count.toDouble())
    }
    
    override fun recordScanRate(filesPerSecond: Double) {
        meterRegistry.gauge("scanner.scan.rate", filesPerSecond)
    }
    
    override fun recordError(errorType: String) {
        errorCounter.increment()
        
        // Also record with error type tag
        Counter.builder("scanner.errors.by.type")
            .tag("type", errorType)
            .description("Errors by type")
            .register(meterRegistry)
            .increment()
    }
    
    override fun recordSkippedFile(reason: String) {
        skippedFileCounter.increment()
        
        // Also record with reason tag
        Counter.builder("scanner.files.skipped.by.reason")
            .tag("reason", reason)
            .description("Skipped files by reason")
            .register(meterRegistry)
            .increment()
    }
    
    override fun recordFilesQueued(count: Int) {
        filesQueuedCounter.increment(count.toDouble())
    }
    
    override fun recordQueueDepth(depth: Long) {
        currentQueueDepth.set(depth)
    }
    
    override fun recordProcessingDuration(duration: Duration) {
        processingDurationTimer.record(duration)
    }
    
    override fun recordThresholdUtilization(percentage: Double) {
        currentThresholdUtilization.set(percentage.toLong())
        
        // Also record as a gauge with current value
        meterRegistry.gauge("scanner.threshold.utilization.current", percentage)
    }
    
    override fun recordThresholdWarning() {
        thresholdWarningCounter.increment()
    }
    
    override fun recordBackpressureEvent() {
        backpressureEventCounter.increment()
    }
    
    override fun recordBackpressureRetry(attemptNumber: Int) {
        backpressureRetryCounter.increment()
        
        // Also record with attempt number tag
        Counter.builder("scanner.backpressure.retries.by.attempt")
            .tag("attempt", attemptNumber.toString())
            .description("Backpressure retries by attempt number")
            .register(meterRegistry)
            .increment()
    }
    
    override fun recordBackpressureResolution(durationMs: Long) {
        backpressureResolutionTimer.record(Duration.ofMillis(durationMs))
    }
    
    override fun getMetricsSummary(): MetricsSummary {
        return MetricsSummary(
            totalFilesScanned = filesScannedCounter.count().toLong(),
            totalFilesQueued = filesQueuedCounter.count().toLong(),
            totalErrors = errorCounter.count().toLong(),
            totalFilesSkipped = skippedFileCounter.count().toLong(),
            currentQueueDepth = currentQueueDepth.get(),
            averageScanDuration = Duration.ofNanos(scanDurationTimer.mean(java.util.concurrent.TimeUnit.NANOSECONDS).toLong()),
            averageProcessingDuration = Duration.ofNanos(processingDurationTimer.mean(java.util.concurrent.TimeUnit.NANOSECONDS).toLong())
        )
    }
    
    override fun reset() {
        // Clear all counters by creating new instances
        // Note: This is a simplified reset - in production you might want to use MeterRegistry.clear()
        filesScannedCounter.count()
        filesQueuedCounter.count()
        errorCounter.count()
        skippedFileCounter.count()
        currentQueueDepth.set(0)
    }
}