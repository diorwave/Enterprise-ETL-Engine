package com.quantumsoft.tia.scanner.controllers.jobs

import com.quantumsoft.tia.scanner.dto.*
import com.quantumsoft.tia.scanner.services.ScanJobService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/scanner/jobs")
@Tag(name = "Scan Jobs", description = "Operations for managing file scanning jobs")
class JobsController(
    private val scanJobService: ScanJobService
) {
    
    @GetMapping
    @Operation(
        summary = "List scan jobs",
        description = "Retrieve a paginated list of scan jobs with optional filtering"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved list of scan jobs")
    )
    fun listJobs(
        @Parameter(description = "Filter by active status")
        @RequestParam(required = false) active: Boolean?,
        @Parameter(description = "Filter by parser ID")
        @RequestParam(required = false) parserId: String?,
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = ["priority"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ScanJobDto>> {
        val jobs = scanJobService.findJobs(active, parserId, pageable)
        return ResponseEntity.ok(jobs)
    }
    
    @GetMapping("/{jobId}")
    @Operation(
        summary = "Get scan job details",
        description = "Retrieve detailed information about a specific scan job"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Scan job found"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun getJob(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID
    ): ResponseEntity<ScanJobDetailDto> {
        val job = scanJobService.findById(jobId)
        return if (job != null) {
            ResponseEntity.ok(job)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    @Operation(
        summary = "Create new scan job",
        description = "Create a new file scanning job with specified configuration"
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Scan job created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data")
    )
    fun createJob(
        @Parameter(description = "Scan job configuration", required = true)
        @Valid @RequestBody request: CreateScanJobRequest
    ): ResponseEntity<ScanJobDto> {
        val job = scanJobService.createJob(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(job)
    }
    
    @PutMapping("/{jobId}")
    @Operation(
        summary = "Update scan job",
        description = "Update configuration of an existing scan job"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Scan job updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun updateJob(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID,
        @Parameter(description = "Updated scan job configuration", required = true)
        @Valid @RequestBody request: UpdateScanJobRequest
    ): ResponseEntity<ScanJobDto> {
        val job = scanJobService.updateJob(jobId, request)
        return if (job != null) {
            ResponseEntity.ok(job)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{jobId}")
    @Operation(
        summary = "Delete scan job",
        description = "Remove a scan job from the system"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Scan job deleted successfully"),
        ApiResponse(responseCode = "404", description = "Scan job not found")
    )
    fun deleteJob(
        @Parameter(description = "Unique identifier of the scan job", required = true)
        @PathVariable jobId: UUID
    ): ResponseEntity<Void> {
        val deleted = scanJobService.deleteJob(jobId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}