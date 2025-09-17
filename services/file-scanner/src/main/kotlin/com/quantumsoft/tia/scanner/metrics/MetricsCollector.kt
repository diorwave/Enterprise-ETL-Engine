package com.quantumsoft.tia.scanner.metrics

import java.time.Duration

interface MetricsCollector {
    fun recordScanDuration(directory: String, duration: Duration)
    fun recordFilesScanned(count: Int)
    fun recordScanRate(filesPerSecond: Double)
    fun recordError(errorType: String)
    fun recordSkippedFile(reason: String)
    fun recordFilesQueued(count: Int)
    fun recordQueueDepth(depth: Long)
    fun recordProcessingDuration(duration: Duration)
    fun recordThresholdUtilization(percentage: Double)
    fun recordThresholdWarning()
    fun recordBackpressureEvent()
    fun recordBackpressureRetry(attemptNumber: Int)
    fun recordBackpressureResolution(durationMs: Long)
    fun getMetricsSummary(): MetricsSummary
    fun reset()
}