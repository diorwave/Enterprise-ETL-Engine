package com.quantumsoft.tia.scanner.repositories

import com.quantumsoft.tia.scanner.entities.ExecutionStatus
import com.quantumsoft.tia.scanner.entities.ScanJobExecution
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID
import java.util.Optional

@Repository
interface ScanJobExecutionRepository : JpaRepository<ScanJobExecution, UUID> {
    
    fun findByScanJobIdOrderByStartedAtDesc(jobId: UUID, pageable: Pageable): Page<ScanJobExecution>
    
    fun findTopByScanJobIdOrderByStartedAtDesc(jobId: UUID): Optional<ScanJobExecution>
    
    fun findAllByStatus(status: ExecutionStatus): List<ScanJobExecution>
    
    fun findAllByStatusAndStartedAtBefore(
        status: ExecutionStatus,
        cutoff: Instant
    ): List<ScanJobExecution>
    
    @Query("""
        SELECT e FROM ScanJobExecution e
        WHERE e.scanJob.id = :jobId
        AND e.status = 'RUNNING'
    """)
    fun findRunningExecutionByJob(@Param("jobId") jobId: UUID): Optional<ScanJobExecution>
    
    @Query("""
        SELECT COUNT(e) > 0 FROM ScanJobExecution e
        WHERE e.scanJob.id = :jobId
        AND e.status = 'RUNNING'
    """)
    fun hasRunningExecution(@Param("jobId") jobId: UUID): Boolean
    
    @Query("""
        SELECT AVG(e.durationMs) FROM ScanJobExecution e
        WHERE e.scanJob.id = :jobId
        AND e.status = 'COMPLETED'
        AND e.completedAt >= :since
    """)
    fun getAverageExecutionDuration(
        @Param("jobId") jobId: UUID,
        @Param("since") since: Instant
    ): Double?
    
    @Query("""
        SELECT e FROM ScanJobExecution e
        WHERE e.startedAt BETWEEN :startDate AND :endDate
        ORDER BY e.startedAt DESC
    """)
    fun findExecutionsInTimeRange(
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant,
        pageable: Pageable
    ): Page<ScanJobExecution>
    
    @Query("""
        SELECT 
            e.status,
            COUNT(e),
            AVG(e.filesDiscovered),
            AVG(e.filesQueued),
            AVG(e.durationMs)
        FROM ScanJobExecution e
        WHERE e.scanJob.id = :jobId
        GROUP BY e.status
    """)
    fun getExecutionStatisticsByJob(@Param("jobId") jobId: UUID): List<Array<Any>>
}