package com.quantumsoft.tia.scanner.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "scan_job_executions",
    indexes = [
        Index(name = "idx_executions_job", columnList = "job_id"),
        Index(name = "idx_executions_started", columnList = "started_at")
    ]
)
data class ScanJobExecution(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    val scanJob: ScanJob,
    
    @Column(name = "instance_id", nullable = false, length = 100)
    val instanceId: String,
    
    @Column(name = "started_at", nullable = false)
    val startedAt: Instant = Instant.now(),
    
    @Column(name = "completed_at")
    val completedAt: Instant? = null,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: ExecutionStatus,
    
    @Column(name = "files_discovered")
    val filesDiscovered: Int = 0,
    
    @Column(name = "files_queued")
    val filesQueued: Int = 0,
    
    @Column(name = "files_skipped")
    val filesSkipped: Int = 0,
    
    @Column(name = "duration_ms")
    val durationMs: Long? = null,
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null
)

enum class ExecutionStatus {
    RUNNING,
    COMPLETED,
    FAILED
}