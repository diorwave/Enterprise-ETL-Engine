package com.quantumsoft.tia.scanner.dto.metrics

data class ClusterMetrics(
    val instanceCount: Int,
    val healthyInstances: Int,
    val totalCapacity: Double
)