package com.quantumsoft.tia.scanner.dto

import java.time.Duration
import java.time.Instant

data class ScannerMetricsDto(
    val totalFilesScanned: Int,
    val totalFilesQueued: Int,
    val totalErrors: Int,
    val totalFilesSkipped: Int,
    val currentQueueDepth: Long,
    val averageScanDuration: Duration,
    val averageProcessingDuration: Duration,
    val lastUpdated: Instant
)

data class MetricsSummaryDto(
    val totalFilesScanned: Int,
    val totalFilesQueued: Int,
    val queueSuccessRate: Double,
    val totalErrors: Int,
    val errorRate: Double,
    val totalFilesSkipped: Int,
    val currentQueueDepth: Long,
    val averageScanDurationSeconds: Double,
    val averageProcessingDurationMs: Double,
    val lastUpdated: Instant
)