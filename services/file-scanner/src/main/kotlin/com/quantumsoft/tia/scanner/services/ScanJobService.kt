package com.quantumsoft.tia.scanner.services

import com.quantumsoft.tia.scanner.dto.*
import com.quantumsoft.tia.scanner.entities.*
import com.quantumsoft.tia.scanner.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID
import com.quantumsoft.tia.scanner.scheduler.JobScheduler

@Service
@Transactional
class ScanJobService(
    private val scanJobRepository: ScanJobRepository,
    private val executionRepository: ScanJobExecutionRepository,
    private val scannedFileRepository: ScannedFileRepository,
    private val jobScheduler: JobScheduler
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ScanJobService::class.java)
    }
    
    fun findJobs(active: Boolean?, parserId: String?, pageable: Pageable): Page<ScanJobDto> {
        val jobs = scanJobRepository.findJobsWithFilters(active, parserId, pageable)
        return jobs.map { job -> toDto(job) }
    }
    
    fun findById(id: UUID): ScanJobDetailDto? {
        val job = scanJobRepository.findByIdWithLatestExecution(id)
        return job.orElse(null)?.let { toDetailDto(it) }
    }
    
    fun createJob(request: CreateScanJobRequest): ScanJobDto {
        // Check for duplicate name
        if (scanJobRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Job with name '${request.name}' already exists")
        }
        
        val job = ScanJob(
            name = request.name,
            description = request.description,
            sourceDirectory = request.sourceDirectory,
            filePattern = request.filePattern,
            scanIntervalType = request.scanInterval.type,
            scanIntervalValue = request.scanInterval.value,
            maxFileSizeMb = request.maxFileSizeMb,
            recursiveScan = request.recursiveScan,
            maxDepth = request.maxDepth,
            priority = request.priority,
            parserId = request.parserId,
            isActive = true
        )
        
        val savedJob = scanJobRepository.save(job)
        logger.info("Created scan job: ${savedJob.name} with ID: ${savedJob.id}")
        
        return toDto(savedJob)
    }
    
    fun updateJob(id: UUID, request: UpdateScanJobRequest): ScanJobDto? {
        val job = scanJobRepository.findById(id).orElse(null) ?: return null
        
        // Check for duplicate name if name is being changed
        request.name?.let { newName ->
            if (newName != job.name && scanJobRepository.existsByName(newName)) {
                throw IllegalArgumentException("Job with name '$newName' already exists")
            }
        }
        
        val updatedJob = job.copy(
            name = request.name ?: job.name,
            description = request.description ?: job.description,
            sourceDirectory = request.sourceDirectory ?: job.sourceDirectory,
            filePattern = request.filePattern ?: job.filePattern,
            scanIntervalType = request.scanInterval?.type ?: job.scanIntervalType,
            scanIntervalValue = request.scanInterval?.value ?: job.scanIntervalValue,
            maxFileSizeMb = request.maxFileSizeMb ?: job.maxFileSizeMb,
            recursiveScan = request.recursiveScan ?: job.recursiveScan,
            maxDepth = request.maxDepth ?: job.maxDepth,
            priority = request.priority ?: job.priority,
            parserId = request.parserId ?: job.parserId,
            isActive = request.isActive ?: job.isActive,
            updatedAt = Instant.now()
        )
        
        val savedJob = scanJobRepository.save(updatedJob)
        logger.info("Updated scan job: ${savedJob.name}")
        
        return toDto(savedJob)
    }
    
    fun deleteJob(id: UUID): Boolean {
        val job = scanJobRepository.findById(id).orElse(null) ?: return false
        
        // Check for running executions
        if (executionRepository.hasRunningExecution(id)) {
            throw IllegalStateException("Cannot delete job with running execution")
        }
        
        // Soft delete - mark as inactive
        val deletedJob = job.copy(isActive = false, updatedAt = Instant.now())
        scanJobRepository.save(deletedJob)
        
        logger.info("Soft deleted scan job: ${job.name}")
        return true
    }
    
    fun triggerScan(jobId: UUID, force: Boolean): ScanExecutionDto? {
        val job = scanJobRepository.findById(jobId).orElse(null) ?: return null
        
        if (!job.isActive && !force) {
            throw IllegalStateException("Job is not active")
        }
        
        if (executionRepository.hasRunningExecution(jobId)) {
            throw IllegalStateException("Job already has a running execution")
        }
        
        val execution = ScanJobExecution(
            scanJob = job,
            instanceId = "scanner-${System.currentTimeMillis()}",
            status = ExecutionStatus.RUNNING,
            startedAt = Instant.now()
        )
        
        val savedExecution = executionRepository.save(execution)
        logger.info("Triggered scan for job: ${job.name}, execution ID: ${savedExecution.id}")
        
        // Trigger the Quartz job for immediate execution
        try {
            jobScheduler.triggerJobNow(jobId)
        } catch (e: Exception) {
            logger.warn("Failed to trigger Quartz job immediately for jobId=$jobId: ${e.message}")
        }
        
        return ScanExecutionDto(
            executionId = savedExecution.id,
            jobId = jobId,
            status = savedExecution.status,
            message = "Scan initiated",
            estimatedDuration = getEstimatedDuration(jobId),
            startedAt = savedExecution.startedAt
        )
    }
    
    fun getExecutions(jobId: UUID, pageable: Pageable): Page<ScanExecutionDto> {
        val executions = executionRepository.findByScanJobIdOrderByStartedAtDesc(jobId, pageable)
        return executions.map { execution ->
            ScanExecutionDto(
                executionId = execution.id,
                jobId = jobId,
                status = execution.status,
                message = execution.errorMessage ?: "Execution ${execution.status.name.lowercase()}",
                estimatedDuration = execution.durationMs?.let { Duration.ofMillis(it) },
                startedAt = execution.startedAt
            )
        }
    }
    
    fun getJobStatistics(jobId: UUID): JobStatisticsDto? {
        if (!scanJobRepository.existsById(jobId)) {
            return null
        }
        
        val executionStats = executionRepository.getExecutionStatisticsByJob(jobId)
        val fileStatusCounts = scannedFileRepository.getStatusCountsByJob(jobId)
        
        var totalExecutions = 0L
        var successfulExecutions = 0L
        var failedExecutions = 0L
        var runningExecutions = 0L
        var totalFilesDiscovered = 0.0
        var totalFilesQueued = 0.0
        var totalDuration = 0.0
        
        executionStats.forEach { row ->
            val status = row[0] as ExecutionStatus
            val count = (row[1] as Number).toLong()
            val avgDiscovered = (row[2] as Number?)?.toDouble() ?: 0.0
            val avgQueued = (row[3] as Number?)?.toDouble() ?: 0.0
            val avgDuration = (row[4] as Number?)?.toDouble() ?: 0.0
            
            when (status) {
                ExecutionStatus.COMPLETED -> {
                    successfulExecutions = count
                    totalFilesDiscovered += avgDiscovered * count
                    totalFilesQueued += avgQueued * count
                    totalDuration += avgDuration * count
                }
                ExecutionStatus.FAILED -> failedExecutions = count
                ExecutionStatus.RUNNING -> runningExecutions = count
            }
            totalExecutions += count
        }
        
        val fileStatusDistribution = fileStatusCounts.associate {
            (it[0] as FileStatus) to (it[1] as Number).toLong()
        }
        
        return JobStatisticsDto(
            jobId = jobId,
            totalExecutions = totalExecutions,
            successfulExecutions = successfulExecutions,
            failedExecutions = failedExecutions,
            runningExecutions = runningExecutions,
            averageFilesDiscovered = if (totalExecutions > 0) totalFilesDiscovered / totalExecutions else 0.0,
            averageFilesQueued = if (totalExecutions > 0) totalFilesQueued / totalExecutions else 0.0,
            averageDuration = if (successfulExecutions > 0) Duration.ofMillis((totalDuration / successfulExecutions).toLong()) else null,
            fileStatusDistribution = fileStatusDistribution
        )
    }
    
    private fun toDto(job: ScanJob): ScanJobDto {
        val lastExecution = executionRepository.findTopByScanJobIdOrderByStartedAtDesc(job.id).orElse(null)
        val stats = calculateSimpleStats(job.id)
        
        return ScanJobDto(
            id = job.id,
            name = job.name,
            sourceDirectory = job.sourceDirectory,
            filePattern = job.filePattern,
            scanInterval = ScanIntervalDto(job.scanIntervalType, job.scanIntervalValue),
            isActive = job.isActive,
            lastExecution = lastExecution?.startedAt,
            nextExecution = calculateNextExecution(job, lastExecution),
            statistics = stats
        )
    }
    
    private fun toDetailDto(job: ScanJob): ScanJobDetailDto {
        val lastExecution = job.executions.maxByOrNull { it.startedAt }
        val stats = calculateSimpleStats(job.id)
        
        return ScanJobDetailDto(
            id = job.id,
            name = job.name,
            description = job.description,
            sourceDirectory = job.sourceDirectory,
            filePattern = job.filePattern,
            scanInterval = ScanIntervalDto(job.scanIntervalType, job.scanIntervalValue),
            maxFileSizeMb = job.maxFileSizeMb,
            recursiveScan = job.recursiveScan,
            maxDepth = job.maxDepth,
            priority = job.priority,
            parserId = job.parserId,
            isActive = job.isActive,
            createdAt = job.createdAt,
            updatedAt = job.updatedAt,
            createdBy = job.createdBy,
            updatedBy = job.updatedBy,
            lastExecution = lastExecution?.let {
                ScanExecutionDto(
                    executionId = it.id,
                    jobId = job.id,
                    status = it.status,
                    message = it.errorMessage ?: "Execution ${it.status.name.lowercase()}",
                    estimatedDuration = it.durationMs?.let { ms -> Duration.ofMillis(ms) },
                    startedAt = it.startedAt
                )
            },
            statistics = stats
        )
    }
    
    private fun calculateSimpleStats(jobId: UUID): ScanJobStatisticsDto {
        val totalFiles = scannedFileRepository.countByJobAndStatus(jobId, FileStatus.COMPLETED)
        val failedFiles = scannedFileRepository.countByJobAndStatus(jobId, FileStatus.FAILED)
        val successRate = if ((totalFiles + failedFiles) > 0) {
            (totalFiles.toDouble() / (totalFiles + failedFiles)) * 100
        } else 0.0
        
        val avgDuration = executionRepository.getAverageExecutionDuration(
            jobId, 
            Instant.now().minus(Duration.ofDays(30))
        )
        
        return ScanJobStatisticsDto(
            totalFiles = totalFiles,
            successRate = successRate,
            averageScanDuration = avgDuration?.let { Duration.ofMillis(it.toLong()) },
            lastSuccessfulScan = executionRepository.findTopByScanJobIdOrderByStartedAtDesc(jobId)
                .filter { it.status == ExecutionStatus.COMPLETED }
                .map { it.completedAt }
                .orElse(null),
            failureCount = failedFiles
        )
    }
    
    private fun calculateNextExecution(job: ScanJob, lastExecution: ScanJobExecution?): Instant? {
        // TODO: Implement based on cron or fixed interval
        return null
    }
    
    private fun getEstimatedDuration(jobId: UUID): Duration? {
        val avgMs = executionRepository.getAverageExecutionDuration(
            jobId,
            Instant.now().minus(Duration.ofDays(7))
        )
        return avgMs?.let { Duration.ofMillis(it.toLong()) }
    }
}
