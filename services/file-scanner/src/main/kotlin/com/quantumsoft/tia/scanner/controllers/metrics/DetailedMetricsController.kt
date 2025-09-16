package com.quantumsoft.tia.scanner.controllers.metrics

import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.dto.metrics.*
import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.metrics.MetricsSummary
import com.quantumsoft.tia.scanner.models.QueueStatistics
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/scanner/metrics")
@Tag(name = "Detailed Metrics", description = "Detailed performance and system metrics")
class DetailedMetricsController(
    private val metricsCollector: MetricsCollector,
    private val queueManager: QueueManager
) {
    
    @GetMapping("/full")
    @Operation(
        summary = "Get full metrics",
        description = "Retrieve comprehensive metrics including queue, performance, and cluster statistics"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Full metrics retrieved successfully")
    )
    fun getFullMetrics(): ResponseEntity<ScannerMetricsDto> {
        val metricsSummary = metricsCollector.getMetricsSummary()
        val queueStatistics = runBlocking {
            queueManager.getQueueStatistics()
        }
        
        val metrics = ScannerMetricsDto(
            scanning = ScanningMetrics(
                totalFilesScanned = metricsSummary.totalFilesScanned,
                totalFilesQueued = metricsSummary.totalFilesQueued,
                totalFilesSkipped = metricsSummary.totalFilesSkipped,
                scanRate = calculateScanRate(metricsSummary),
                averageScanDuration = metricsSummary.averageScanDuration
            ),
            queue = QueueMetrics(
                depth = queueStatistics.currentDepth,
                throughput = queueStatistics.throughput,
                deadLetterCount = queueStatistics.deadLetterCount,
                depthByPriority = queueStatistics.depthByPriority.mapKeys { it.key.name }
            ),
            performance = PerformanceMetrics(
                averageScanDuration = metricsSummary.averageScanDuration,
                averageProcessingDuration = metricsSummary.averageProcessingDuration,
                failureRate = calculateFailureRate(metricsSummary, queueStatistics),
                totalErrors = metricsSummary.totalErrors
            ),
            cluster = ClusterMetrics(
                instanceCount = 1, // TODO: Get from cluster coordinator
                healthyInstances = 1,
                totalCapacity = calculateCapacity(metricsSummary)
            )
        )
        
        return ResponseEntity.ok(metrics)
    }
    
    private fun calculateScanRate(summary: MetricsSummary): Double {
        return if (summary.averageScanDuration.toMillis() > 0) {
            summary.totalFilesScanned.toDouble() / (summary.averageScanDuration.toMillis() / 1000.0)
        } else 0.0
    }
    
    private fun calculateFailureRate(summary: MetricsSummary, queueStats: QueueStatistics): Double {
        val total = queueStats.totalProcessed + queueStats.totalFailed
        return if (total > 0) {
            (queueStats.totalFailed.toDouble() / total) * 100
        } else 0.0
    }
    
    private fun calculateCapacity(summary: MetricsSummary): Double {
        return if (summary.averageProcessingDuration.toMillis() > 0) {
            1000.0 / summary.averageProcessingDuration.toMillis() * 60
        } else 0.0
    }
}