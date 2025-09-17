package com.quantumsoft.tia.scanner.metrics

import java.time.Duration

data class MetricsSummary(
    val totalFilesScanned: Long,
    val totalFilesQueued: Long,
    val totalErrors: Long,
    val totalFilesSkipped: Long,
    val currentQueueDepth: Long,
    val averageScanDuration: Duration,
    val averageProcessingDuration: Duration
)