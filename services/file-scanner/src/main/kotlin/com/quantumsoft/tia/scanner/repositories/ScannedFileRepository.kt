package com.quantumsoft.tia.scanner.repositories

import com.quantumsoft.tia.scanner.entities.FileStatus
import com.quantumsoft.tia.scanner.entities.ScannedFileEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import java.util.Optional

@Repository
interface ScannedFileRepository : JpaRepository<ScannedFileEntity, UUID> {
    
    fun findByFilePathAndFileHash(filePath: String, fileHash: String): Optional<ScannedFileEntity>
    
    fun findAllByStatus(status: FileStatus, pageable: Pageable): Page<ScannedFileEntity>
    
    fun findAllByScanJobIdAndStatus(jobId: UUID, status: FileStatus): List<ScannedFileEntity>
    
    @Query("""
        SELECT f FROM ScannedFileEntity f
        WHERE f.scanJob.id = :jobId
        AND f.discoveredAt BETWEEN :startDate AND :endDate
        ORDER BY f.discoveredAt DESC
    """)
    fun findByJobAndDateRange(
        @Param("jobId") jobId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant,
        pageable: Pageable
    ): Page<ScannedFileEntity>
    
    @Query("""
        SELECT f FROM ScannedFileEntity f
        WHERE (:jobId IS NULL OR f.scanJob.id = :jobId)
        AND (:statuses IS NULL OR f.status IN :statuses)
        AND (:fromDate IS NULL OR f.discoveredAt >= :fromDate)
        AND (:toDate IS NULL OR f.discoveredAt <= :toDate)
        AND (:filePattern IS NULL OR f.fileName LIKE %:filePattern%)
        AND (:minSize IS NULL OR f.fileSizeBytes >= :minSize)
        AND (:maxSize IS NULL OR f.fileSizeBytes <= :maxSize)
        ORDER BY f.discoveredAt DESC
    """)
    fun findWithFilters(
        @Param("jobId") jobId: UUID?,
        @Param("statuses") statuses: List<FileStatus>?,
        @Param("fromDate") fromDate: Instant?,
        @Param("toDate") toDate: Instant?,
        @Param("filePattern") filePattern: String?,
        @Param("minSize") minSize: Long?,
        @Param("maxSize") maxSize: Long?,
        pageable: Pageable
    ): Page<ScannedFileEntity>
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE ScannedFileEntity f
        SET f.status = :newStatus,
            f.processingStartedAt = :now,
            f.processingInstanceId = :instanceId
        WHERE f.id = :fileId
        AND f.status = :expectedStatus
    """)
    fun updateStatusIfCurrent(
        @Param("fileId") fileId: UUID,
        @Param("newStatus") newStatus: FileStatus,
        @Param("expectedStatus") expectedStatus: FileStatus,
        @Param("now") now: Instant,
        @Param("instanceId") instanceId: String
    ): Int
    
    @Query("""
        SELECT COUNT(f) FROM ScannedFileEntity f
        WHERE f.scanJob.id = :jobId
        AND f.status = :status
    """)
    fun countByJobAndStatus(
        @Param("jobId") jobId: UUID,
        @Param("status") status: FileStatus
    ): Long
    
    @Query("""
        SELECT f.status, COUNT(f)
        FROM ScannedFileEntity f
        WHERE f.scanJob.id = :jobId
        GROUP BY f.status
    """)
    fun getStatusCountsByJob(@Param("jobId") jobId: UUID): List<Array<Any>>
    
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ScannedFileEntity f
        WHERE f.discoveredAt < :cutoffDate
        AND f.status IN ('COMPLETED', 'SKIPPED')
    """)
    fun deleteOldProcessedFiles(@Param("cutoffDate") cutoffDate: Instant): Int

    // Fetch all files for a job (all statuses)
    fun findAllByScanJobId(jobId: UUID): List<ScannedFileEntity>
}
