package com.quantumsoft.tia.parsers.zte.utils

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Metrics collector implementation for monitoring parser performance.
 * 
 * Collects and reports metrics such as:
 * - Records processed per second
 * - Batch write performance
 * - Error rates
 * - Memory usage
 */
@Component
class MetricsCollectorImpl(
    private val meterRegistry: MeterRegistry? = null
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(MetricsCollectorImpl::class.java)
        
        const val METRIC_PREFIX = "zte.parser"
        const val RECORDS_PROCESSED = "$METRIC_PREFIX.records.processed"
        const val RECORDS_FAILED = "$METRIC_PREFIX.records.failed"
        const val BATCH_WRITE_TIME = "$METRIC_PREFIX.batch.write.time"
        const val BATCH_SIZE = "$METRIC_PREFIX.batch.size"
        const val FILE_PROCESS_TIME = "$METRIC_PREFIX.file.process.time"
    }
    
    private var totalRecordsProcessed = 0L
    private var totalRecordsFailed = 0L
    private var config: Map<String, Any> = emptyMap()
    
    fun initialize(configuration: Map<String, Any>) {
        config = configuration
        logger.info("Metrics collector initialized")
    }
    
    fun destroy() {
        logger.info("Metrics collector destroyed. Total records processed: $totalRecordsProcessed, failed: $totalRecordsFailed")
    }
    
    /**
     * Records a batch write operation.
     */
    fun recordBatchWrite(recordCount: Int, duration: Duration) {
        totalRecordsProcessed += recordCount
        
        meterRegistry?.let { registry ->
            registry.counter(RECORDS_PROCESSED).increment(recordCount.toDouble())
            
            Timer.builder(BATCH_WRITE_TIME)
                .register(registry)
                .record(duration)
            
            registry.gauge(BATCH_SIZE, recordCount)
        }
        
        val recordsPerSecond = if (duration.toMillis() > 0) {
            recordCount * 1000.0 / duration.toMillis()
        } else {
            0.0
        }
        
        logger.debug("Batch write completed: {} records in {}ms ({:.2f} records/sec)",
            recordCount, duration.toMillis(), recordsPerSecond)
    }
    
    /**
     * Records a failed record.
     */
    fun recordFailure() {
        totalRecordsFailed++
        meterRegistry?.counter(RECORDS_FAILED)?.increment()
    }
    
    /**
     * Records file processing completion.
     */
    fun recordFileProcessing(fileName: String, duration: Duration, recordCount: Long) {
        meterRegistry?.let { registry ->
            Timer.builder(FILE_PROCESS_TIME)
                .tag("file", fileName)
                .register(registry)
                .record(duration)
        }
        
        logger.info("File processed: {} - {} records in {}s",
            fileName, recordCount, duration.seconds)
    }
    
    /**
     * Gets current metrics summary.
     */
    fun getMetricsSummary(): Map<String, Any> {
        return mapOf(
            "totalRecordsProcessed" to totalRecordsProcessed,
            "totalRecordsFailed" to totalRecordsFailed,
            "successRate" to if (totalRecordsProcessed > 0) {
                (totalRecordsProcessed - totalRecordsFailed) * 100.0 / totalRecordsProcessed
            } else {
                0.0
            }
        )
    }
}