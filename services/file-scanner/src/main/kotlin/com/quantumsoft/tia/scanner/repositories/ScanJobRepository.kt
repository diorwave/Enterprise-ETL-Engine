package com.quantumsoft.tia.scanner.repositories

import com.quantumsoft.tia.scanner.entities.ScanJob
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface ScanJobRepository : JpaRepository<ScanJob, UUID> {
    
    fun findByName(name: String): Optional<ScanJob>
    
    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<ScanJob>
    
    fun findAllByIsActive(isActive: Boolean): List<ScanJob>
    
    fun existsByName(name: String): Boolean
    
    @Query("""
        SELECT j FROM ScanJob j 
        WHERE (:active IS NULL OR j.isActive = :active)
        AND (:parserId IS NULL OR j.parserId = :parserId)
        ORDER BY j.priority DESC, j.createdAt DESC
    """)
    fun findJobsWithFilters(
        @Param("active") active: Boolean?,
        @Param("parserId") parserId: String?,
        pageable: Pageable
    ): Page<ScanJob>
    
    @Query("""
        SELECT j FROM ScanJob j
        LEFT JOIN FETCH j.executions e
        WHERE j.id = :id
        AND e.startedAt = (
            SELECT MAX(e2.startedAt) 
            FROM ScanJobExecution e2 
            WHERE e2.scanJob.id = :id
        )
    """)
    fun findByIdWithLatestExecution(@Param("id") id: UUID): Optional<ScanJob>
    
    @Query("""
        SELECT COUNT(j) FROM ScanJob j
        WHERE j.isActive = true
        AND j.parserId = :parserId
    """)
    fun countActiveJobsByParser(@Param("parserId") parserId: String): Long
}