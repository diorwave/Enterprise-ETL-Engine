package com.quantumsoft.tia.scanner.controllers.jobs

import com.quantumsoft.tia.scanner.dto.ScanExecutionDto
import com.quantumsoft.tia.scanner.dto.TriggerScanRequest
import com.quantumsoft.tia.scanner.services.ScanJobService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/scanner/jobs/{jobId}")
@Tag(name = "Job Executions", description = "Manage and monitor scan job executions")
class JobExecutionsController(
    private val scanJobService: ScanJobService
) {
    
    @PostMapping("/scan")
    @Operation(
        summary = "Trigger manual scan",
        description = "Manually trigger a scan for the specified job"
    )
    @ApiResponses(
        ApiResponse(responseCode = "202", description = "Scan triggered successfully"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun triggerScan(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID,
        @Parameter(description = "Scan trigger options")
        @RequestBody(required = false) request: TriggerScanRequest?
    ): ResponseEntity<ScanExecutionDto> {
        val force = request?.force ?: false
        val execution = scanJobService.triggerScan(jobId, force)
        
        return if (execution != null) {
            ResponseEntity.accepted().body(execution)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/executions")
    @Operation(
        summary = "Get job execution history",
        description = "Retrieve paginated execution history for a scan job"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved execution history"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun getJobExecutions(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID,
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = ["startedAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ScanExecutionDto>> {
        val executions = scanJobService.getExecutions(jobId, pageable)
        return ResponseEntity.ok(executions)
    }
}