package com.quantumsoft.tia.scanner.dto.metrics

import java.time.Duration

data class ScanningMetrics(
    val totalFilesScanned: Long,
    val totalFilesQueued: Long,
    val totalFilesSkipped: Long,
    val scanRate: Double,
    val averageScanDuration: Duration
)