package com.quantumsoft.tia.scanner.controllers.files

import com.quantumsoft.tia.scanner.dto.files.CleanupRequest
import com.quantumsoft.tia.scanner.dto.files.CleanupResultDto
import com.quantumsoft.tia.scanner.dto.files.FileStatisticsDto
import com.quantumsoft.tia.scanner.services.FileStatusService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/scanner/files")
@Tag(name = "File Statistics", description = "File processing statistics and maintenance")
class FileStatisticsController(
    private val fileStatusService: FileStatusService
) {
    
    @GetMapping("/statistics")
    @Operation(
        summary = "Get file statistics",
        description = "Retrieve aggregated statistics about file processing"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    )
    fun getStatistics(
        @Parameter(description = "Filter statistics by job ID")
        @RequestParam(required = false) jobId: UUID?
    ): ResponseEntity<FileStatisticsDto> {
        val stats = fileStatusService.getStatistics(jobId)
        return ResponseEntity.ok(stats)
    }
    
    @PostMapping("/cleanup")
    @Operation(
        summary = "Cleanup old files",
        description = "Remove old file records from the database"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Cleanup completed successfully"),
        ApiResponse(responseCode = "400", description = "Invalid cleanup parameters")
    )
    fun cleanupOldFiles(
        @Parameter(description = "Cleanup configuration", required = true)
        @RequestBody @Valid request: CleanupRequest
    ): ResponseEntity<CleanupResultDto> {
        val result = fileStatusService.cleanupOldFiles(request.daysToKeep)
        return ResponseEntity.ok(result)
    }
}