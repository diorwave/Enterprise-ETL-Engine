package com.quantumsoft.tia.scanner.scheduler

import com.quantumsoft.tia.scanner.components.DirectoryScanner
import com.quantumsoft.tia.scanner.components.QueueManager
import com.quantumsoft.tia.scanner.entities.ExecutionStatus
import com.quantumsoft.tia.scanner.entities.ScanJob
import com.quantumsoft.tia.scanner.entities.ScanJobExecution
import com.quantumsoft.tia.scanner.entities.ScanIntervalType
import com.quantumsoft.tia.scanner.models.*
import com.quantumsoft.tia.scanner.repositories.ScanJobExecutionRepository
import com.quantumsoft.tia.scanner.repositories.ScanJobRepository
import com.quantumsoft.tia.scanner.repositories.ScannedFileRepository
import kotlinx.coroutines.runBlocking
import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Component
class JobScheduler(
    private val scheduler: Scheduler,
    private val scanJobRepository: ScanJobRepository
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(JobScheduler::class.java)
        const val JOB_GROUP = "scanner-jobs"
        const val TRIGGER_GROUP = "scanner-triggers"
        const val JOB_ID_KEY = "jobId"
    }
    
    fun scheduleJob(scanJob: ScanJob) {
        try {
            val jobDetail = buildJobDetail(scanJob)
            val trigger = buildTrigger(scanJob)
            
            if (scheduler.checkExists(jobDetail.key)) {
                scheduler.deleteJob(jobDetail.key)
            }
            
            scheduler.scheduleJob(jobDetail, trigger)
            logger.info("Scheduled job: ${scanJob.name} with trigger: ${trigger.key}")
        } catch (e: Exception) {
            logger.error("Failed to schedule job: ${scanJob.name}", e)
            throw RuntimeException("Failed to schedule job", e)
        }
    }
    
    fun unscheduleJob(jobId: UUID) {
        try {
            val jobKey = JobKey(jobId.toString(), JOB_GROUP)
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey)
                logger.info("Unscheduled job: $jobId")
            }
        } catch (e: Exception) {
            logger.error("Failed to unschedule job: $jobId", e)
        }
    }
    
    fun pauseJob(jobId: UUID) {
        try {
            val jobKey = JobKey(jobId.toString(), JOB_GROUP)
            scheduler.pauseJob(jobKey)
            logger.info("Paused job: $jobId")
        } catch (e: Exception) {
            logger.error("Failed to pause job: $jobId", e)
        }
    }
    
    fun resumeJob(jobId: UUID) {
        try {
            val jobKey = JobKey(jobId.toString(), JOB_GROUP)
            scheduler.resumeJob(jobKey)
            logger.info("Resumed job: $jobId")
        } catch (e: Exception) {
            logger.error("Failed to resume job: $jobId", e)
        }
    }
    
    fun triggerJobNow(jobId: UUID) {
        try {
            val jobKey = JobKey(jobId.toString(), JOB_GROUP)
            scheduler.triggerJob(jobKey)
            logger.info("Triggered job immediately: $jobId")
        } catch (e: Exception) {
            logger.error("Failed to trigger job: $jobId", e)
            throw RuntimeException("Failed to trigger job", e)
        }
    }
    
    fun isJobRunning(jobId: UUID): Boolean {
        return try {
            val jobKey = JobKey(jobId.toString(), JOB_GROUP)
            val executingJobs = scheduler.getCurrentlyExecutingJobs()
            executingJobs.any { it.jobDetail.key == jobKey }
        } catch (e: Exception) {
            logger.error("Failed to check job status: $jobId", e)
            false
        }
    }
    
    fun rescheduleAllActiveJobs() {
        try {
            val activeJobs = scanJobRepository.findAllByIsActive(true)
            activeJobs.forEach { job ->
                scheduleJob(job)
            }
            logger.info("Rescheduled ${activeJobs.size} active jobs")
        } catch (e: Exception) {
            logger.error("Failed to reschedule jobs", e)
        }
    }
    
    private fun buildJobDetail(scanJob: ScanJob): JobDetail {
        return JobBuilder.newJob(ScannerJob::class.java)
            .withIdentity(scanJob.id.toString(), JOB_GROUP)
            .withDescription(scanJob.description ?: scanJob.name)
            .usingJobData(JOB_ID_KEY, scanJob.id.toString())
            .storeDurably()
            .build()
    }
    
    private fun buildTrigger(scanJob: ScanJob): Trigger {
        val triggerBuilder = TriggerBuilder.newTrigger()
            .withIdentity(scanJob.id.toString(), TRIGGER_GROUP)
            .withDescription("Trigger for ${scanJob.name}")
            .startNow()
        
        return when (scanJob.scanIntervalType) {
            ScanIntervalType.CRON -> {
                triggerBuilder
                    .withSchedule(
                        CronScheduleBuilder.cronSchedule(scanJob.scanIntervalValue)
                            .withMisfireHandlingInstructionFireAndProceed()
                    )
                    .build()
            }
            ScanIntervalType.FIXED -> {
                val intervalMillis = Duration.parse(scanJob.scanIntervalValue).toMillis()
                triggerBuilder
                    .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(intervalMillis)
                            .repeatForever()
                            .withMisfireHandlingInstructionNextWithRemainingCount()
                    )
                    .build()
            }
        }
    }
}

@Component
class ScannerJob(
    private val scanJobRepository: ScanJobRepository,
    private val executionRepository: ScanJobExecutionRepository,
    private val scannedFileRepository: ScannedFileRepository,
    private val directoryScanner: DirectoryScanner,
    private val queueManager: QueueManager
) : QuartzJobBean() {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ScannerJob::class.java)
    }
    
    override fun executeInternal(context: JobExecutionContext) {
        val jobIdStr = context.mergedJobDataMap.getString(JobScheduler.JOB_ID_KEY)
        val jobId = UUID.fromString(jobIdStr)
        
        logger.info("Starting scan job execution: $jobId")
        
        // Load job configuration
        val scanJob = scanJobRepository.findById(jobId).orElse(null)
        if (scanJob == null) {
            logger.error("Scan job not found: $jobId")
            return
        }
        
        if (!scanJob.isActive) {
            logger.info("Scan job is not active, skipping: ${scanJob.name}")
            return
        }
        
        // Check for running execution
        if (executionRepository.hasRunningExecution(jobId)) {
            logger.warn("Scan job already has running execution: ${scanJob.name}")
            return
        }
        
        // Create execution record
        var execution = ScanJobExecution(
            scanJob = scanJob,
            instanceId = "scanner-${System.currentTimeMillis()}",
            status = ExecutionStatus.RUNNING,
            startedAt = Instant.now()
        )
        execution = executionRepository.save(execution)
        
        try {
            // Perform the scan
            val scanResult = runBlocking {
                performScan(scanJob)
            }
            
            // Update execution record
            execution = execution.copy(
                status = ExecutionStatus.COMPLETED,
                completedAt = Instant.now(),
                filesDiscovered = scanResult.filesDiscovered,
                filesQueued = scanResult.filesQueued,
                filesSkipped = scanResult.filesSkipped,
                durationMs = Duration.between(execution.startedAt, Instant.now()).toMillis()
            )
            executionRepository.save(execution)
            
            logger.info("Completed scan job execution: ${scanJob.name}, discovered: ${scanResult.filesDiscovered}, queued: ${scanResult.filesQueued}")
            
        } catch (e: Exception) {
            logger.error("Failed to execute scan job: ${scanJob.name}", e)
            
            // Update execution record with failure
            execution = execution.copy(
                status = ExecutionStatus.FAILED,
                completedAt = Instant.now(),
                errorMessage = e.message,
                durationMs = Duration.between(execution.startedAt, Instant.now()).toMillis()
            )
            executionRepository.save(execution)
        }
    }
    
    private suspend fun performScan(scanJob: ScanJob): ScanExecutionResult {
        // Configure and perform directory scan
        val scanConfig = ScanConfiguration(
            sourceDirectory = scanJob.sourceDirectory,
            filePattern = scanJob.filePattern,
            recursiveScan = scanJob.recursiveScan,
            maxDepth = scanJob.maxDepth,
            maxFileSizeMb = scanJob.maxFileSizeMb,
            scanTimeout = Duration.ofMinutes(30)
        )
        
        val scanResult = directoryScanner.scan(scanConfig)
        
        // Queue discovered files
        var filesQueued = 0
        var filesSkipped = 0
        
        for (file in scanResult.files) {
            // Check if file already exists in database
            val existingFile = scannedFileRepository.findByFilePathAndFileHash(
                file.filePath,
                file.fileHash ?: ""
            )
            
            if (existingFile.isPresent) {
                filesSkipped++
                continue
            }
            
            // Queue the file
            val queueRequest = QueueRequest(
                jobId = scanJob.id.toString(),
                file = file,
                priority = when(scanJob.priority) {
                    in 0..3 -> Priority.LOW
                    in 4..6 -> Priority.NORMAL
                    else -> Priority.HIGH
                }
            )
            
            val queueResult = queueManager.queueFile(queueRequest)
            if (queueResult.success) {
                filesQueued++
                
                // Save to database
                val scannedFile = com.quantumsoft.tia.scanner.entities.ScannedFileEntity(
                    scanJob = scanJob,
                    filePath = file.filePath,
                    fileName = file.fileName,
                    fileSizeBytes = file.fileSizeBytes,
                    fileHash = file.fileHash ?: "",
                    fileModifiedAt = file.lastModified,
                    status = com.quantumsoft.tia.scanner.entities.FileStatus.QUEUED,
                    queueId = queueResult.queueId
                )
                scannedFileRepository.save(scannedFile)
            } else if (queueResult.duplicateDetected) {
                filesSkipped++
            }
        }
        
        return ScanExecutionResult(
            filesDiscovered = scanResult.filesDiscovered,
            filesQueued = filesQueued,
            filesSkipped = filesSkipped + scanResult.skippedFiles.size
        )
    }
}

data class ScanExecutionResult(
    val filesDiscovered: Int,
    val filesQueued: Int,
    val filesSkipped: Int
)