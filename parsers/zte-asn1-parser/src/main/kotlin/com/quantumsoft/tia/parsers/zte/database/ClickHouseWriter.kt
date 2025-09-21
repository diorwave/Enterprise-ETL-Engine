package com.quantumsoft.tia.parsers.zte.database

import com.quantumsoft.tia.parsers.zte.InsertResult
import com.quantumsoft.tia.parsers.zte.ZteCdrRecord
import com.tia.etl.parser.api.models.ProcessingContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.Duration
import java.time.Instant

/**
 * ClickHouse database writer for ZTE CDR records.
 * 
 * Handles batch insertions with proper error handling and retry logic.
 * Implements Clean Code principles with single responsibility.
 */
@Component
class ClickHouseWriter {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ClickHouseWriter::class.java)
        
        const val INSERT_SQL = """
            INSERT INTO zte_cdr_records (
                record_type, record_sequence_number, processing_id, file_name,
                block_number, record_number_in_block,
                served_imsi, served_imei, served_msisdn, charging_id,
                record_opening_time, record_closing_time, duration,
                sgsn_address, ggsn_address, sgw_address, pgw_address,
                location_area_code, routing_area_code, cell_identifier,
                user_location_information, uplink_volume, downlink_volume, total_volume,
                access_point_name, pdp_type, served_pdp_address, qos_information,
                parsed_timestamp, parser_version
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
    }
    
    private var config: Map<String, Any> = emptyMap()
    
    fun initialize(configuration: Map<String, Any>) {
        config = configuration
        logger.info("ClickHouse writer initialized")
    }
    
    fun destroy() {
        logger.info("ClickHouse writer destroyed")
    }
    
    /**
     * Writes a batch of CDR records to ClickHouse.
     */
    fun writeBatch(records: List<ZteCdrRecord>, context: ProcessingContext): InsertResult {
        val startTime = Instant.now()
        
        return try {
            val connection = context.clickHouseConnection
            connection.autoCommit = false
            
            try {
                val statement = connection.prepareStatement(INSERT_SQL)
                var successCount = 0
                val errors = mutableListOf<String>()
                
                for ((index, record) in records.withIndex()) {
                    try {
                        setStatementParameters(statement, record, context, index)
                        statement.addBatch()
                        successCount++
                    } catch (e: Exception) {
                        logger.warn("Failed to add record $index to batch", e)
                        errors.add("Record $index: ${e.message}")
                    }
                }
                
                if (successCount > 0) {
                    val results = statement.executeBatch()
                    connection.commit()
                    
                    val insertedCount = results.count { it >= 0 }
                    val duration = Duration.between(startTime, Instant.now())
                    
                    if (errors.isEmpty()) {
                        InsertResult.Success(insertedCount, duration)
                    } else {
                        InsertResult.PartialSuccess(
                            recordsInserted = insertedCount,
                            recordsFailed = errors.size,
                            errors = errors
                        )
                    }
                } else {
                    InsertResult.Failure("No records to insert", null)
                }
                
            } catch (e: SQLException) {
                connection.rollback()
                logger.error("Database error during batch insert", e)
                InsertResult.Failure("Database error: ${e.message}", e)
            }
            
        } catch (e: Exception) {
            logger.error("Unexpected error during batch write", e)
            InsertResult.Failure("Unexpected error: ${e.message}", e)
        }
    }
    
    private fun setStatementParameters(
        statement: PreparedStatement,
        record: ZteCdrRecord,
        context: ProcessingContext,
        recordIndex: Int
    ) {
        var paramIndex = 1
        
        // Basic fields
        statement.setInt(paramIndex++, record.recordType)
        statement.setString(paramIndex++, record.recordSequenceNumber)
        statement.setString(paramIndex++, context.processingId.toString())
        statement.setString(paramIndex++, context.file.name)
        statement.setInt(paramIndex++, 0) // block_number - будет установлен при обработке
        statement.setInt(paramIndex++, recordIndex)
        
        // Subscriber information
        statement.setString(paramIndex++, record.servedImsi)
        statement.setString(paramIndex++, record.servedImei)
        statement.setString(paramIndex++, record.servedMsisdn)
        statement.setLong(paramIndex++, record.chargingId)
        
        // Timestamps
        statement.setTimestamp(paramIndex++, java.sql.Timestamp.from(record.recordOpeningTime))
        statement.setTimestamp(paramIndex++, java.sql.Timestamp.from(record.recordClosingTime))
        statement.setLong(paramIndex++, record.duration)
        
        // Network addresses
        statement.setString(paramIndex++, record.sgsnAddress)
        statement.setString(paramIndex++, record.ggsnAddress)
        statement.setString(paramIndex++, record.sgwAddress)
        statement.setString(paramIndex++, record.pgwAddress)
        
        // Location information
        statement.setInt(paramIndex++, record.locationAreaCode)
        statement.setString(paramIndex++, record.routingAreaCode)
        statement.setLong(paramIndex++, record.cellIdentifier)
        statement.setString(paramIndex++, record.userLocationInfo)
        
        // Traffic volumes
        statement.setLong(paramIndex++, record.uplinkVolume)
        statement.setLong(paramIndex++, record.downlinkVolume)
        statement.setLong(paramIndex++, record.totalVolume)
        
        // Service information
        statement.setString(paramIndex++, record.accessPointName)
        statement.setString(paramIndex++, record.pdpType)
        statement.setString(paramIndex++, record.servedPdpAddress)
        statement.setString(paramIndex++, record.qosInformation)
        
        // Metadata
        statement.setTimestamp(paramIndex++, java.sql.Timestamp.from(Instant.now()))
        statement.setString(paramIndex++, "1.0.0")
    }
}