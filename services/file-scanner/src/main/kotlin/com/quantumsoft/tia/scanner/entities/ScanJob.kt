package com.quantumsoft.tia.scanner.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "scan_jobs")
data class ScanJob(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false, unique = true, length = 255)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "source_directory", nullable = false, length = 1024)
    val sourceDirectory: String,
    
    @Column(name = "file_pattern", nullable = false, length = 255)
    val filePattern: String,
    
    @Column(name = "scan_interval_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val scanIntervalType: ScanIntervalType,
    
    @Column(name = "scan_interval_value", nullable = false, length = 100)
    val scanIntervalValue: String,
    
    @Column(name = "max_file_size_mb")
    val maxFileSizeMb: Int = 1024,
    
    @Column(name = "recursive_scan")
    val recursiveScan: Boolean = true,
    
    @Column(name = "max_depth")
    val maxDepth: Int = 10,
    
    @Column
    val priority: Int = 0,
    
    @Column(name = "parser_id", nullable = false, length = 100)
    val parserId: String,
    
    @Column(name = "is_active")
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
    
    @Column(name = "created_by", length = 100)
    val createdBy: String? = null,
    
    @Column(name = "updated_by", length = 100)
    val updatedBy: String? = null,
    
    @OneToMany(mappedBy = "scanJob", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val executions: List<ScanJobExecution> = emptyList(),
    
    @OneToMany(mappedBy = "scanJob", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val scannedFiles: List<ScannedFileEntity> = emptyList()
)

enum class ScanIntervalType {
    CRON,
    FIXED
}