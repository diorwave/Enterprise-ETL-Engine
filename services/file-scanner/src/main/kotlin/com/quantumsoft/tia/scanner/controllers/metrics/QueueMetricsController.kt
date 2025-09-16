package com.quantumsoft.tia.scanner.controllers.metrics

import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.dto.metrics.CleanupResult
import com.quantumsoft.tia.scanner.models.QueueStatistics
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/scanner/metrics/queue")
@Tag(name = "Queue Metrics", description = "Monitor and manage queue metrics")
class QueueMetricsController(
    private val queueManager: QueueManager
) {
    
    @GetMapping
    @Operation(
        summary = "Get queue metrics",
        description = "Retrieve current queue statistics and metrics"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Queue metrics retrieved successfully")
    )
    fun getQueueMetrics(): ResponseEntity<QueueStatistics> {
        val stats = runBlocking {
            queueManager.getQueueStatistics()
        }
        return ResponseEntity.ok(stats)
    }
    
    @PostMapping("/cleanup")
    @Operation(
        summary = "Cleanup expired locks",
        description = "Remove expired locks from the queue system"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Cleanup completed successfully")
    )
    fun cleanupExpiredLocks(): ResponseEntity<CleanupResult> {
        val cleaned = runBlocking {
            queueManager.cleanupExpiredLocks()
        }
        
        return ResponseEntity.ok(
            CleanupResult(
                cleanedLocks = cleaned,
                message = "Cleaned up $cleaned expired locks"
            )
        )
    }
}