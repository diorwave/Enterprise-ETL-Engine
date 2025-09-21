package com.quantumsoft.tia.parsers.zte

import com.tia.etl.parser.api.DataParser
import com.tia.etl.parser.api.ParserLifecycle
import com.tia.etl.parser.api.exceptions.ParserException
import com.tia.etl.parser.api.models.*
import com.quantumsoft.tia.parsers.zte.asn1.Asn1DecoderImpl
import com.quantumsoft.tia.parsers.zte.database.ClickHouseWriter
import com.quantumsoft.tia.parsers.zte.validation.ZteDataValidator
import com.quantumsoft.tia.parsers.zte.utils.MetricsCollectorImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * ZTE ASN.1 CDR Parser implementation following Clean Code and TDD principles.
 */
@Component
class ZteAsn1ParserImpl(
    private val decoder: Asn1DecoderImpl,
    private val validator: ZteDataValidator,
    private val writer: ClickHouseWriter,
    private val metrics: MetricsCollectorImpl
) : DataParser, ParserLifecycle {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ZteAsn1ParserImpl::class.java)
        
        const val PARSER_ID = "zte-asn1-parser"
        const val PARSER_VERSION = "1.0.0"
        const val DEFAULT_BATCH_SIZE = 5000
        const val DEFAULT_BLOCK_SIZE = 8192
    }
    
    private var isInitialized = false
    private var configuration: Map<String, Any> = emptyMap()
    
    override fun getMetadata(): ParserMetadata {
        return ParserMetadata(
            parserId = PARSER_ID,
            name = "ZTE ASN.1 CDR Parser",
            version = PARSER_VERSION,
            description = "Processes ZTE ZXUN CG ASN.1 encoded CDR files",
            supportedFormats = listOf("asn1", "ber", "cdr"),
            targetTable = "zte_cdr_records",
            schemaDefinition = createTableSchema(),
            author = "Quantum Soft TIA Team"
        )
    }
    
    override fun process(context: ProcessingContext): ProcessingResult {
        val startTime = Instant.now()
        
        try {
            // Initialize if needed
            if (!isInitialized) {
                initialize(emptyMap())
            }
            
            // Process file
            val stats = processFileWithBatching(context)
            
            // Build result
            return ProcessingResult(
                totalRecords = stats.recordsProcessed,
                successfulRecords = stats.recordsInserted,
                failedRecords = stats.recordsFailed,
                skippedRecords = 0,
                processingTime = Duration.between(startTime, Instant.now()),
                errors = stats.errors.map { it.toApiError() },
                warnings = emptyList(),
                metadata = mapOf(
                    "blockSize" to stats.blockSize,
                    "batchSize" to stats.batchSize,
                    "fileSize" to context.file.length()
                ),
                bytesProcessed = context.file.length()
            )
            
        } catch (e: Exception) {
            logger.error("Critical error processing file ${context.file.name}", e)
            
            return ProcessingResult(
                totalRecords = 0,
                successfulRecords = 0,
                failedRecords = 0,
                skippedRecords = 0,
                processingTime = Duration.between(startTime, Instant.now()),
                errors = listOf(
                    ProcessingError(
                        message = "Critical processing error: ${e.message}",
                        severity = ErrorSeverity.CRITICAL,
                        recordNumber = null,
                        exception = e
                    )
                ),
                warnings = emptyList(),
                metadata = emptyMap(),
                bytesProcessed = context.file.length()
            )
        }
    }
    
    override fun initialize(config: Map<String, Any>) {
        logger.info("Initializing ZTE ASN.1 parser")
        
        configuration = config
        
        // Initialize components
        decoder.initialize(config)
        validator.initialize(config)
        writer.initialize(config)
        metrics.initialize(config)
        
        isInitialized = true
        logger.info("ZTE ASN.1 parser initialized successfully")
    }
    
    override fun validate(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult(
                isValid = false,
                errors = listOf("FILE_NOT_FOUND: File does not exist: ${file.absolutePath}"),
                warnings = emptyList()
            )
            !file.canRead() -> ValidationResult(
                isValid = false,
                errors = listOf("FILE_NOT_READABLE: File is not readable: ${file.absolutePath}"),
                warnings = emptyList()
            )
            file.length() == 0L -> ValidationResult(
                isValid = false,
                errors = listOf("FILE_EMPTY: File is empty: ${file.absolutePath}"),
                warnings = emptyList()
            )
            else -> ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = emptyList()
            )
        }
    }
    
    override fun shutdown() {
        logger.info("Shutting down ZTE ASN.1 parser")
        
        decoder.destroy()
        validator.destroy()
        writer.destroy()
        metrics.destroy()
        
        isInitialized = false
        logger.info("ZTE ASN.1 parser shutdown complete")
    }
    
    private fun processFileWithBatching(context: ProcessingContext): ProcessingStats {
        val stats = ProcessingStats()
        val batchSize = DEFAULT_BATCH_SIZE
        val blockSize = DEFAULT_BLOCK_SIZE
        
        stats.batchSize = batchSize
        stats.blockSize = blockSize
        
        context.file.inputStream().buffered().use { stream ->
            val buffer = ByteArray(blockSize)
            var blockNumber = 0
            var batch = mutableListOf<ZteCdrRecord>()
            
            while (true) {
                val bytesRead = stream.read(buffer)
                if (bytesRead == -1) break
                
                // Process block
                val records = processBlock(buffer, bytesRead, blockNumber, context, stats)
                batch.addAll(records)
                
                // Write batch if full
                if (batch.size >= batchSize) {
                    writeBatch(batch, context, stats)
                    batch = mutableListOf()
                }
                
                blockNumber++
            }
            
            // Write remaining records
            if (batch.isNotEmpty()) {
                writeBatch(batch, context, stats)
            }
        }
        
        return stats
    }
    
    private fun processBlock(
        buffer: ByteArray,
        bytesRead: Int,
        blockNumber: Int,
        context: ProcessingContext,
        stats: ProcessingStats
    ): List<ZteCdrRecord> {
        val records = mutableListOf<ZteCdrRecord>()
        var offset = 0
        
        while (offset < bytesRead && buffer[offset] != 0xFF.toByte()) {
            try {
                val decodeResult = decoder.decode(buffer, offset, bytesRead - offset)
                
                when (decodeResult) {
                    is DecodeResult.Success -> {
                        val record = decodeResult.value
                        val validationResult = validator.validateRecord(record)
                        
                        if (validationResult.isValid) {
                            records.add(record)
                            stats.recordsProcessed++
                        } else {
                            stats.recordsFailed++
                            stats.errors.add(
                                InternalProcessingError(
                                    message = "Validation failed: ${validationResult.errors.firstOrNull()}",
                                    severity = InternalErrorSeverity.WARNING,
                                    recordNumber = stats.recordsProcessed.toInt()
                                )
                            )
                        }
                        
                        offset = decodeResult.nextOffset
                    }
                    
                    is DecodeResult.Failure -> {
                        logger.warn("Failed to decode CDR at block $blockNumber, offset $offset")
                        stats.recordsFailed++
                        break
                    }
                }
                
            } catch (e: Exception) {
                logger.error("Error processing CDR at block $blockNumber", e)
                stats.recordsFailed++
                break
            }
        }
        
        return records
    }
    
    private fun writeBatch(
        batch: List<ZteCdrRecord>,
        context: ProcessingContext,
        stats: ProcessingStats
    ) {
        try {
            val insertResult = writer.writeBatch(batch, context)
            
            when (insertResult) {
                is InsertResult.Success -> {
                    stats.recordsInserted += insertResult.recordsInserted
                    metrics.recordBatchWrite(insertResult.recordsInserted, insertResult.duration)
                }
                
                is InsertResult.PartialSuccess -> {
                    stats.recordsInserted += insertResult.recordsInserted
                    stats.recordsFailed += insertResult.recordsFailed
                }
                
                is InsertResult.Failure -> {
                    stats.recordsFailed += batch.size
                    stats.errors.add(
                        InternalProcessingError(
                            message = "Batch insert failed: ${insertResult.error}",
                            severity = InternalErrorSeverity.ERROR
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error writing batch", e)
            stats.recordsFailed += batch.size
        }
    }
    
    private fun createTableSchema(): TableSchema {
        return TableSchema(
            tableName = "zte_cdr_records",
            columns = listOf(
                ColumnDefinition("record_type", "UInt8"),
                ColumnDefinition("record_sequence_number", "String"),
                ColumnDefinition("processing_id", "UUID"),
                ColumnDefinition("served_imsi", "String"),
                ColumnDefinition("served_imei", "String"),
                ColumnDefinition("served_msisdn", "String"),
                ColumnDefinition("charging_id", "UInt32"),
                ColumnDefinition("record_opening_time", "DateTime('UTC')"),
                ColumnDefinition("duration", "UInt32"),
                ColumnDefinition("uplink_volume", "UInt64"),
                ColumnDefinition("downlink_volume", "UInt64"),
                ColumnDefinition("total_volume", "UInt64")
            )
        )
    }
    
    private data class ProcessingStats(
        var recordsProcessed: Long = 0,
        var recordsInserted: Long = 0,
        var recordsFailed: Long = 0,
        val errors: MutableList<InternalProcessingError> = mutableListOf(),
        var blockSize: Int = 0,
        var batchSize: Int = 0
    )
}

// Internal error model for processing
private data class InternalProcessingError(
    val message: String,
    val severity: InternalErrorSeverity,
    val recordNumber: Int? = null
) {
    fun toApiError(): ProcessingError = ProcessingError(
        message = message,
        severity = when (severity) {
            InternalErrorSeverity.WARNING -> ErrorSeverity.WARNING
            InternalErrorSeverity.ERROR -> ErrorSeverity.ERROR
            InternalErrorSeverity.CRITICAL -> ErrorSeverity.CRITICAL
        },
        recordNumber = recordNumber?.toLong()
    )
}

private enum class InternalErrorSeverity {
    WARNING,
    ERROR,
    CRITICAL
}

// Data models used by the parser
data class ZteCdrRecord(
    val recordType: Int,
    val recordSequenceNumber: String = "",
    val servedImsi: String = "",
    val servedImei: String = "",
    val servedMsisdn: String = "",
    val chargingId: Long = 0,
    val recordOpeningTime: Instant = Instant.EPOCH,
    val recordClosingTime: Instant = Instant.EPOCH,
    val duration: Long = 0,
    val sgsnAddress: String = "",
    val ggsnAddress: String = "",
    val sgwAddress: String = "",
    val pgwAddress: String = "",
    val locationAreaCode: Int = 0,
    val routingAreaCode: String = "",
    val cellIdentifier: Long = 0,
    val userLocationInfo: String = "",
    val uplinkVolume: Long = 0,
    val downlinkVolume: Long = 0,
    val totalVolume: Long = 0,
    val accessPointName: String = "",
    val pdpType: String = "",
    val servedPdpAddress: String = "",
    val qosInformation: String = ""
)

sealed class DecodeResult<out T> {
    data class Success<T>(val value: T, val nextOffset: Int) : DecodeResult<T>()
    data class Failure(val error: String, val cause: Exception? = null) : DecodeResult<Nothing>()
}

sealed class InsertResult {
    data class Success(val recordsInserted: Int, val duration: Duration) : InsertResult()
    data class PartialSuccess(
        val recordsInserted: Int,
        val recordsFailed: Int,
        val errors: List<String>
    ) : InsertResult()
    data class Failure(val error: String, val cause: Exception? = null) : InsertResult()
}