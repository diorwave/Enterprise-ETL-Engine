package com.quantumsoft.tia.scanner.controllers.metrics

import com.quantumsoft.tia.scanner.metrics.MetricsCollector
import com.quantumsoft.tia.scanner.metrics.MetricsSummary
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/v1/scanner/metrics")
@Tag(name = "Scanner Metrics", description = "Monitor scanner performance and health metrics")
class ScannerMetricsController(
    private val metricsCollector: MetricsCollector
) {
    
    @GetMapping
    @Operation(
        summary = "Get basic metrics",
        description = "Retrieve basic scanner metrics"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    )
    fun getMetrics(): ResponseEntity<Map<String, Any>> {
        val metricsSummary = metricsCollector.getMetricsSummary()
        
        val metricsMap = mapOf(
            "totalFilesScanned" to metricsSummary.totalFilesScanned,
            "totalFilesQueued" to metricsSummary.totalFilesQueued,
            "totalErrors" to metricsSummary.totalErrors,
            "totalFilesSkipped" to metricsSummary.totalFilesSkipped,
            "currentQueueDepth" to metricsSummary.currentQueueDepth,
            "averageScanDuration" to metricsSummary.averageScanDuration.toMillis(),
            "averageProcessingDuration" to metricsSummary.averageProcessingDuration.toMillis(),
            "timestamp" to Instant.now()
        )
        
        return ResponseEntity.ok(metricsMap)
    }
    
    @GetMapping("/summary")
    @Operation(
        summary = "Get metrics summary",
        description = "Retrieve comprehensive metrics summary"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Metrics summary retrieved successfully")
    )
    fun getMetricsSummary(): ResponseEntity<MetricsSummary> {
        val summary = metricsCollector.getMetricsSummary()
        return ResponseEntity.ok(summary)
    }
    
    @PostMapping("/reset")
    @Operation(
        summary = "Reset metrics",
        description = "Reset all collected metrics to zero"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Metrics reset successfully")
    )
    fun resetMetrics(): ResponseEntity<Void> {
        metricsCollector.reset()
        return ResponseEntity.noContent().build()
    }
}