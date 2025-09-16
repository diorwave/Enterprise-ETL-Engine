package com.quantumsoft.tia.scanner.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "scanned_files",
    indexes = [
        Index(name = "idx_scanned_files_job", columnList = "job_id"),
        Index(name = "idx_scanned_files_status", columnList = "status"),
        Index(name = "idx_scanned_files_discovered", columnList = "discovered_at"),
        Index(name = "idx_scanned_files_hash", columnList = "file_hash")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_file_path_hash", columnNames = ["file_path", "file_hash"])
    ]
)
data class ScannedFileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    val scanJob: ScanJob,
    
    @Column(name = "file_path", nullable = false, length = 2048)
    val filePath: String,
    
    @Column(name = "file_name", nullable = false, length = 512)
    val fileName: String,
    
    @Column(name = "file_size_bytes", nullable = false)
    val fileSizeBytes: Long,
    
    @Column(name = "file_hash", nullable = false, length = 64)
    val fileHash: String,
    
    @Column(name = "file_modified_at", nullable = false)
    val fileModifiedAt: Instant,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: FileStatus,
    
    @Column(name = "queue_id", length = 255)
    val queueId: String? = null,
    
    @Column(name = "processing_started_at")
    val processingStartedAt: Instant? = null,
    
    @Column(name = "processing_completed_at")
    val processingCompletedAt: Instant? = null,
    
    @Column(name = "processing_instance_id", length = 100)
    val processingInstanceId: String? = null,
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,
    
    @Column(name = "retry_count")
    val retryCount: Int = 0,
    
    @Column(name = "discovered_at", nullable = false)
    val discoveredAt: Instant = Instant.now()
)

enum class FileStatus {
    DISCOVERED,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    SKIPPED
}