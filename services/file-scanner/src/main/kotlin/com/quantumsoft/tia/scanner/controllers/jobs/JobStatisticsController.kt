package com.quantumsoft.tia.scanner.controllers.jobs

import com.quantumsoft.tia.scanner.dto.JobStatisticsDto
import com.quantumsoft.tia.scanner.services.ScanJobService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/scanner/jobs/{jobId}/statistics")
@Tag(name = "Job Statistics", description = "Retrieve statistical information about scan jobs")
class JobStatisticsController(
    private val scanJobService: ScanJobService
) {
    
    @GetMapping
    @Operation(
        summary = "Get job statistics",
        description = "Retrieve statistical information about scan job performance"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun getJobStatistics(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID
    ): ResponseEntity<JobStatisticsDto> {
        val stats = scanJobService.getJobStatistics(jobId)
        return if (stats != null) {
            ResponseEntity.ok(stats)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}