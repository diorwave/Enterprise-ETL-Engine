package com.quantumsoft.tia.scanner.dto.files

import com.quantumsoft.tia.scanner.entities.FileStatus
import java.time.Instant

data class FileStatisticsDto(
    val totalFiles: Long,
    val statusDistribution: Map<FileStatus, Long>,
    val averageFileSize: Double,
    val totalSizeBytes: Long,
    val oldestFile: Instant?,
    val newestFile: Instant?
)