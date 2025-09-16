package com.quantumsoft.tia.scanner.dto.metrics

import java.time.Duration

data class PerformanceMetrics(
    val averageScanDuration: Duration,
    val averageProcessingDuration: Duration,
    val failureRate: Double,
    val totalErrors: Long
)