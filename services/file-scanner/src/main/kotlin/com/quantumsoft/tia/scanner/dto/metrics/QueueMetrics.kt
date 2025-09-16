package com.quantumsoft.tia.scanner.dto.metrics

data class QueueMetrics(
    val depth: Long,
    val throughput: Double,
    val deadLetterCount: Long,
    val depthByPriority: Map<String, Long>
)