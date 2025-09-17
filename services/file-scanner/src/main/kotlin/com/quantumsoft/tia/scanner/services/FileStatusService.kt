package com.quantumsoft.tia.scanner.services

import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.dto.files.CleanupResultDto
import com.quantumsoft.tia.scanner.dto.files.FileStatisticsDto
import com.quantumsoft.tia.scanner.dto.FileStatusDto
import com.quantumsoft.tia.scanner.dto.FileStatusFilter
import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.entities.ScannedFileEntity
import com.quantumsoft.tia.scanner.models.Priority
import com.quantumsoft.tia.scanner.models.QueueRequest
import com.quantumsoft.tia.scanner.models.ScannedFile
import com.quantumsoft.tia.scanner.repositories.ScannedFileRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional
class FileStatusService(
    private val scannedFileRepository: ScannedFileRepository,
    private val queueManager: QueueManager
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FileStatusService::class.java)
    }
    
    fun queryFiles(filter: FileStatusFilter, pageable: Pageable): Page<FileStatusDto> {
        val statuses = filter.statuses?.takeIf { it.isNotEmpty() }
        val files = scannedFileRepository.findWithFilters(
            jobId = filter.jobId,
            statuses = statuses,
            fromDate = filter.fromDate,
            toDate = filter.toDate,
            filePattern = filter.filePattern,
            minSize = filter.minSize,
            maxSize = filter.maxSize,
            pageable = pageable
        )
        
        return files.map { toDto(it) }
    }
    
    fun getFile(fileId: UUID): FileStatusDto? {
        val file = scannedFileRepository.findById(fileId).orElse(null)
        return file?.let { toDto(it) }
    }
    
    fun retryFile(fileId: UUID): FileStatusDto? {
        val file = scannedFileRepository.findById(fileId).orElse(null) ?: return null
        
        if (file.status !in listOf(FileStatus.FAILED, FileStatus.SKIPPED)) {
            throw IllegalStateException("Can only retry failed or skipped files")
        }
        
        // Re-queue the file
        val scannedFile = ScannedFile(
            filePath = file.filePath,
            fileName = file.fileName,
            fileSizeBytes = file.fileSizeBytes,
            lastModified = file.fileModifiedAt,
            fileHash = file.fileHash
        )
        
        val queueRequest = QueueRequest(
            jobId = file.scanJob.id.toString(),
            file = scannedFile,
            priority = Priority.HIGH,
            retryCount = file.retryCount + 1
        )
        
        val queueResult = runBlocking {
            queueManager.queueFile(queueRequest)
        }
        
        if (queueResult.success) {
            // Update file status
            val updatedFile = file.copy(
                status = FileStatus.QUEUED,
                queueId = queueResult.queueId,
                retryCount = file.retryCount + 1,
                errorMessage = null
            )
            val saved = scannedFileRepository.save(updatedFile)
            logger.info("Retried file: ${file.fileName}, new queue ID: ${queueResult.queueId}")
            return toDto(saved)
        }
        
        throw RuntimeException("Failed to re-queue file: ${queueResult.message}")
    }
    
    fun deleteFile(fileId: UUID): Boolean {
        val file = scannedFileRepository.findById(fileId).orElse(null) ?: return false
        
        if (file.status in listOf(FileStatus.PROCESSING, FileStatus.QUEUED)) {
            throw IllegalStateException("Cannot delete file that is processing or queued")
        }
        
        scannedFileRepository.delete(file)
        logger.info("Deleted file: ${file.fileName}")
        return true
    }
    
    fun getStatistics(jobId: UUID?): FileStatisticsDto {
        val files = if (jobId != null) {
            scannedFileRepository.findAllByScanJobId(jobId)
        } else {
            scannedFileRepository.findAll()
        }
        
        val statusDistribution = files.groupBy { it.status }
            .mapValues { it.value.size.toLong() }
        
        val totalSize = files.sumOf { it.fileSizeBytes }
        val averageSize = if (files.isNotEmpty()) {
            totalSize.toDouble() / files.size
        } else 0.0
        
        val oldestFile = files.minByOrNull { it.discoveredAt }?.discoveredAt
        val newestFile = files.maxByOrNull { it.discoveredAt }?.discoveredAt
        
        return FileStatisticsDto(
            totalFiles = files.size.toLong(),
            statusDistribution = statusDistribution,
            averageFileSize = averageSize,
            totalSizeBytes = totalSize,
            oldestFile = oldestFile,
            newestFile = newestFile
        )
    }
    
    fun cleanupOldFiles(daysToKeep: Int): CleanupResultDto {
        val cutoffDate = Instant.now().minus(daysToKeep.toLong(), ChronoUnit.DAYS)
        val beforeCount = scannedFileRepository.count()
        
        val deletedCount = scannedFileRepository.deleteOldProcessedFiles(cutoffDate)
        
        val afterCount = scannedFileRepository.count()
        
        logger.info("Cleaned up $deletedCount old files, cutoff date: $cutoffDate")
        
        return CleanupResultDto(
            deletedCount = deletedCount,
            beforeCount = beforeCount,
            afterCount = afterCount,
            message = "Deleted $deletedCount files older than $daysToKeep days"
        )
    }
    
    private fun toDto(entity: ScannedFileEntity): FileStatusDto {
        return FileStatusDto(
            id = entity.id,
            jobId = entity.scanJob.id,
            jobName = entity.scanJob.name,
            filePath = entity.filePath,
            fileName = entity.fileName,
            fileSizeBytes = entity.fileSizeBytes,
            fileHash = entity.fileHash,
            fileModifiedAt = entity.fileModifiedAt,
            status = entity.status,
            queueId = entity.queueId,
            processingStartedAt = entity.processingStartedAt,
            processingCompletedAt = entity.processingCompletedAt,
            processingInstanceId = entity.processingInstanceId,
            errorMessage = entity.errorMessage,
            retryCount = entity.retryCount,
            discoveredAt = entity.discoveredAt
        )
    }
}
