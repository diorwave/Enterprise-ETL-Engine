package com.quantumsoft.tia.scanner.controllers.files

import com.quantumsoft.tia.scanner.dto.FileStatusDto
import com.quantumsoft.tia.scanner.dto.FileStatusFilter
import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.services.FileStatusService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/scanner/files")
@Tag(name = "File Management", description = "Query and manage scanned files")
class FilesController(
    private val fileStatusService: FileStatusService
) {
    
    @GetMapping
    @Operation(
        summary = "Query files",
        description = "Search and filter scanned files with pagination support"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Files retrieved successfully")
    )
    fun queryFiles(
        @Parameter(description = "Filter by job ID")
        @RequestParam(required = false) jobId: UUID?,
        @Parameter(description = "Filter by file status")
        @RequestParam(required = false) status: List<FileStatus>?,
        @Parameter(description = "Filter files from this date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @Parameter(description = "Filter files to this date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @Parameter(description = "File name pattern (supports wildcards)")
        @RequestParam(required = false) filePattern: String?,
        @Parameter(description = "Minimum file size in bytes")
        @RequestParam(required = false) minSize: Long?,
        @Parameter(description = "Maximum file size in bytes")
        @RequestParam(required = false) maxSize: Long?,
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = ["discoveredAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<FileStatusDto>> {
        
        val filter = FileStatusFilter(
            jobId = jobId,
            statuses = status,
            fromDate = from,
            toDate = to,
            filePattern = filePattern,
            minSize = minSize,
            maxSize = maxSize
        )
        
        val files = fileStatusService.queryFiles(filter, pageable)
        return ResponseEntity.ok(files)
    }
    
    @GetMapping("/{fileId}")
    @Operation(
        summary = "Get file details",
        description = "Retrieve detailed information about a specific file"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "File found"),
        ApiResponse(responseCode = "404", description = "File not found")
    )
    fun getFile(
        @Parameter(description = "Unique identifier of the file", required = true)
        @PathVariable fileId: UUID
    ): ResponseEntity<FileStatusDto> {
        val file = fileStatusService.getFile(fileId)
        return if (file != null) {
            ResponseEntity.ok(file)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{fileId}/retry")
    @Operation(
        summary = "Retry file processing",
        description = "Requeue a failed file for processing"
    )
    @ApiResponses(
        ApiResponse(responseCode = "202", description = "File requeued successfully"),
        ApiResponse(responseCode = "404", description = "File not found"),
        ApiResponse(responseCode = "409", description = "File cannot be retried in current state")
    )
    fun retryFile(
        @Parameter(description = "Unique identifier of the file", required = true)
        @PathVariable fileId: UUID
    ): ResponseEntity<FileStatusDto> {
        return try {
            val file = fileStatusService.retryFile(fileId)
            if (file != null) {
                ResponseEntity.status(HttpStatus.ACCEPTED).body(file)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(
        summary = "Delete file record",
        description = "Remove a file record from the system"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "File deleted successfully"),
        ApiResponse(responseCode = "404", description = "File not found"),
        ApiResponse(responseCode = "409", description = "File cannot be deleted in current state")
    )
    fun deleteFile(
        @Parameter(description = "Unique identifier of the file", required = true)
        @PathVariable fileId: UUID
    ): ResponseEntity<Void> {
        return try {
            val deleted = fileStatusService.deleteFile(fileId)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}