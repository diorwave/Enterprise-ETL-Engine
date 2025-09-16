package com.quantumsoft.tia.scanner.validators

import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import com.quantumsoft.tia.scanner.services.SettingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class FileThresholdValidator(
    private val settingsService: SettingsService
) {
    private val logger = LoggerFactory.getLogger(FileThresholdValidator::class.java)
    
    companion object {
        const val DEFAULT_THRESHOLD = 10000
        const val THRESHOLD_KEY = "scanner.file.threshold"
        const val THRESHOLD_ENABLED_KEY = "scanner.file.threshold.enabled"
    }
    
    suspend fun canEnqueueFile(currentQueueSize: Int): Boolean = withContext(Dispatchers.IO) {
        val threshold = getThreshold()
        currentQueueSize < threshold
    }
    
    suspend fun canEnqueueBatch(currentQueueSize: Int, batchSize: Int): Boolean = withContext(Dispatchers.IO) {
        val threshold = getThreshold()
        (currentQueueSize + batchSize) <= threshold
    }
    
    suspend fun getThreshold(): Int = withContext(Dispatchers.IO) {
        settingsService.getSettingAs(THRESHOLD_KEY, Int::class.java) ?: DEFAULT_THRESHOLD
    }
    
    suspend fun setThreshold(threshold: Int) = withContext(Dispatchers.IO) {
        settingsService.setSetting(THRESHOLD_KEY, threshold, SettingType.INTEGER)
        logger.info("File threshold updated to: $threshold")
    }
    
    suspend fun validateThreshold(currentQueueSize: Int) = withContext(Dispatchers.IO) {
        val threshold = getThreshold()
        if (currentQueueSize >= threshold) {
            throw ThresholdExceededException(
                "Queue size ($currentQueueSize) exceeds threshold ($threshold)",
                currentQueueSize,
                threshold
            )
        }
    }
    
    suspend fun getThresholdUtilization(currentQueueSize: Int): Double = withContext(Dispatchers.IO) {
        val threshold = getThreshold()
        if (threshold <= 0) {
            100.0
        } else {
            (currentQueueSize.toDouble() / threshold) * 100
        }
    }
    
    suspend fun isThresholdEnabled(): Boolean = withContext(Dispatchers.IO) {
        settingsService.getSettingAs(THRESHOLD_ENABLED_KEY, Boolean::class.java) ?: true
    }
    
    suspend fun getRemainingCapacity(currentQueueSize: Int): Int = withContext(Dispatchers.IO) {
        val threshold = getThreshold()
        max(0, threshold - currentQueueSize)
    }
    
    suspend fun isNearThreshold(currentQueueSize: Int, percentageThreshold: Double): Boolean = withContext(Dispatchers.IO) {
        val utilization = getThresholdUtilization(currentQueueSize)
        utilization >= percentageThreshold
    }
}