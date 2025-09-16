package com.quantumsoft.tia.scanner.dto.metrics

import java.time.Duration

data class ScannerMetricsDto(
    val scanning: ScanningMetrics,
    val queue: QueueMetrics,
    val performance: PerformanceMetrics,
    val cluster: ClusterMetrics
)