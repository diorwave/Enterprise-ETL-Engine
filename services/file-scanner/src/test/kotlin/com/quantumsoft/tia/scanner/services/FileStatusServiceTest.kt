package com.quantumsoft.tia.scanner.services

import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.dto.FileStatusFilter
import com.quantumsoft.tia.scanner.entities.*
import com.quantumsoft.tia.scanner.models.QueueResult
import com.quantumsoft.tia.scanner.repositories.ScannedFileRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class FileStatusServiceTest {

    private lateinit var scannedFileRepository: ScannedFileRepository
    private lateinit var queueManager: QueueManager
    private lateinit var fileStatusService: FileStatusService
    
    @BeforeEach
    fun setUp() {
        scannedFileRepository = mockk(relaxed = true)
        queueManager = mockk(relaxed = true)
        fileStatusService = FileStatusService(scannedFileRepository, queueManager)
    }
    
    @Test
    fun `should query files with filters`() {
        // Given
        val filter = FileStatusFilter(
            jobId = UUID.randomUUID(),
            statuses = listOf(FileStatus.COMPLETED),
            filePattern = "*.asn1"
        )
        val pageable = PageRequest.of(0, 20)
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob)
        
        every {
            scannedFileRepository.findWithFilters(
                filter.jobId, listOf(FileStatus.COMPLETED), null, null,
                filter.filePattern, null, null, pageable
            )
        } returns PageImpl(listOf(scannedFile))
        
        // When
        val result = fileStatusService.queryFiles(filter, pageable)
        
        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().fileName).isEqualTo(scannedFile.fileName)
    }
    
    @Test
    fun `should get file by id`() {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob)
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        
        // When
        val result = fileStatusService.getFile(scannedFile.id)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.fileName).isEqualTo(scannedFile.fileName)
    }
    
    @Test
    fun `should return null when file not found`() {
        // Given
        val fileId = UUID.randomUUID()
        every { scannedFileRepository.findById(fileId) } returns Optional.empty()
        
        // When
        val result = fileStatusService.getFile(fileId)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `should retry failed file`() = runTest {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(
            status = FileStatus.FAILED,
            errorMessage = "Previous error"
        )
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        coEvery { queueManager.queueFile(any()) } returns QueueResult(
            success = true,
            queueId = "new-queue-id",
            message = "Queued successfully"
        )
        every { scannedFileRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = fileStatusService.retryFile(scannedFile.id)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.status).isEqualTo(FileStatus.QUEUED)
        assertThat(result?.queueId).isEqualTo("new-queue-id")
        assertThat(result?.retryCount).isEqualTo(1)
        
        coVerify { queueManager.queueFile(any()) }
        verify { scannedFileRepository.save(match { it.status == FileStatus.QUEUED }) }
    }
    
    @Test
    fun `should retry skipped file`() = runTest {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(
            status = FileStatus.SKIPPED,
            retryCount = 1
        )
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        coEvery { queueManager.queueFile(any()) } returns QueueResult(
            success = true,
            queueId = "retry-queue-id",
            message = "Queued successfully"
        )
        every { scannedFileRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = fileStatusService.retryFile(scannedFile.id)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.retryCount).isEqualTo(2)
    }
    
    @Test
    fun `should not retry file with invalid status`() {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(status = FileStatus.PROCESSING)
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        
        // When/Then
        assertThrows<IllegalStateException> {
            fileStatusService.retryFile(scannedFile.id)
        }
    }
    
    @Test
    fun `should throw exception when retry fails`() = runTest {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(status = FileStatus.FAILED)
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        coEvery { queueManager.queueFile(any()) } returns QueueResult(
            success = false,
            queueId = null,
            message = "Queue full"
        )
        
        // When/Then
        assertThrows<RuntimeException> {
            fileStatusService.retryFile(scannedFile.id)
        }
    }
    
    @Test
    fun `should delete file`() {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(status = FileStatus.COMPLETED)
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        every { scannedFileRepository.delete(scannedFile) } just runs
        
        // When
        val result = fileStatusService.deleteFile(scannedFile.id)
        
        // Then
        assertThat(result).isTrue()
        verify { scannedFileRepository.delete(scannedFile) }
    }
    
    @Test
    fun `should not delete processing file`() {
        // Given
        val scanJob = createScanJob()
        val scannedFile = createScannedFile(scanJob).copy(status = FileStatus.PROCESSING)
        
        every { scannedFileRepository.findById(scannedFile.id) } returns Optional.of(scannedFile)
        
        // When/Then
        assertThrows<IllegalStateException> {
            fileStatusService.deleteFile(scannedFile.id)
        }
    }
    
    @Test
    fun `should get statistics for all files`() {
        // Given
        val files = listOf(
            createScannedFile(createScanJob()).copy(
                status = FileStatus.COMPLETED,
                fileSizeBytes = 1000
            ),
            createScannedFile(createScanJob()).copy(
                status = FileStatus.FAILED,
                fileSizeBytes = 2000
            ),
            createScannedFile(createScanJob()).copy(
                status = FileStatus.COMPLETED,
                fileSizeBytes = 3000
            )
        )
        
        every { scannedFileRepository.findAll() } returns files
        
        // When
        val result = fileStatusService.getStatistics(null)
        
        // Then
        assertThat(result.totalFiles).isEqualTo(3)
        assertThat(result.statusDistribution[FileStatus.COMPLETED]).isEqualTo(2)
        assertThat(result.statusDistribution[FileStatus.FAILED]).isEqualTo(1)
        assertThat(result.averageFileSize).isEqualTo(2000.0)
        assertThat(result.totalSizeBytes).isEqualTo(6000)
    }
    
    @Test
    fun `should get statistics for specific job`() {
        // Given
        val jobId = UUID.randomUUID()
        val files = listOf(
            createScannedFile(createScanJob()).copy(
                status = FileStatus.COMPLETED,
                fileSizeBytes = 5000
            )
        )
        
        every { scannedFileRepository.findAllByScanJobId(jobId) } returns files
        
        // When
        val result = fileStatusService.getStatistics(jobId)
        
        // Then
        assertThat(result.totalFiles).isEqualTo(1)
        assertThat(result.averageFileSize).isEqualTo(5000.0)
    }
    
    @Test
    fun `should cleanup old files`() {
        // Given
        val daysToKeep = 30
        val cutoffDate = Instant.now().minus(daysToKeep.toLong(), ChronoUnit.DAYS)
        
        every { scannedFileRepository.count() } returnsMany listOf(1000L, 950L)
        every { scannedFileRepository.deleteOldProcessedFiles(any()) } returns 50
        
        // When
        val result = fileStatusService.cleanupOldFiles(daysToKeep)
        
        // Then
        assertThat(result.deletedCount).isEqualTo(50)
        assertThat(result.beforeCount).isEqualTo(1000)
        assertThat(result.afterCount).isEqualTo(950)
        assertThat(result.message).contains("50 files older than 30 days")
        
        verify { scannedFileRepository.deleteOldProcessedFiles(match { 
            it.isAfter(cutoffDate.minus(1, ChronoUnit.SECONDS)) &&
            it.isBefore(cutoffDate.plus(1, ChronoUnit.SECONDS))
        }) }
    }
    
    private fun createScanJob() = ScanJob(
        id = UUID.randomUUID(),
        name = "test-job",
        sourceDirectory = "/test",
        filePattern = "*.txt",
        scanIntervalType = ScanIntervalType.CRON,
        scanIntervalValue = "0 * * * * *",
        parserId = "test-parser"
    )
    
    private fun createScannedFile(scanJob: ScanJob) = ScannedFileEntity(
        id = UUID.randomUUID(),
        scanJob = scanJob,
        filePath = "/test/file.txt",
        fileName = "file.txt",
        fileSizeBytes = 1024,
        fileHash = "hash123",
        fileModifiedAt = Instant.now(),
        status = FileStatus.COMPLETED,
        discoveredAt = Instant.now()
    )
}
