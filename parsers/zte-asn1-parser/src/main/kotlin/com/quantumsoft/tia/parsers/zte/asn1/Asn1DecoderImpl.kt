package com.quantumsoft.tia.parsers.zte.asn1

// Import generated ASN.1 classes from pgw_r8_new
import com.bd.asn.zte.data.pgw_r8_new.PGWRecord
import com.bd.asn.zte.data.pgw_r8_new.SGWRecord
import com.quantumsoft.tia.parsers.zte.DecodeResult
import com.quantumsoft.tia.parsers.zte.ZteCdrRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * ASN.1 decoder implementation using asn1bean library.
 * 
 * Decodes ZTE ZXUN CG CDR records from BER-encoded ASN.1 structures.
 * Supports multiple CDR types including S-CDR, G-CDR, SGW-CDR, PGW-CDR, etc.
 * 
 * ## Clean Code Principles
 * - Small focused methods for each decoding operation
 * - Clear error handling with detailed messages
 * - No side effects in decoding methods
 * - Immutable data structures
 */
@Component
class Asn1DecoderImpl {
    
    companion object {
        private val logger = LoggerFactory.getLogger(Asn1DecoderImpl::class.java)
        
        // ASN.1 Tag definitions for ZTE CDR types
        const val TAG_SGSN_PDP_RECORD = 20  // S-CDR
        const val TAG_GGSN_PDP_RECORD = 21  // G-CDR
        const val TAG_SGSN_MM_RECORD = 22   // M-CDR
        const val TAG_SGSN_SMO_RECORD = 23  // S-SMO-CDR
        const val TAG_SGSN_SMT_RECORD = 24  // S-SMT-CDR
        const val TAG_SGW_RECORD = 84       // SGW-CDR
        const val TAG_PGW_RECORD = 85       // PGW-CDR
        
        // Field tags within CDR records
        const val TAG_SERVED_IMSI = 3
        const val TAG_SERVED_IMEI = 4
        const val TAG_SERVED_MSISDN = 5
        const val TAG_CHARGING_ID = 6
        const val TAG_RECORD_OPENING_TIME = 13
        const val TAG_DURATION = 14
        const val TAG_SGSN_ADDRESS = 15
        const val TAG_ACCESS_POINT_NAME = 17
        const val TAG_PDP_TYPE = 18
        const val TAG_SERVED_PDP_ADDRESS = 19
        const val TAG_VOLUME_UPLINK = 30
        const val TAG_VOLUME_DOWNLINK = 31
        
        // Timestamp format (YYMMDDhhmmss+ZZZZ)
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss")
    }
    
    private var isInitialized = false
    private var config: Map<String, Any> = emptyMap()
    
    fun initialize(configuration: Map<String, Any>) {
        config = configuration
        isInitialized = true
        logger.info("ASN.1 decoder initialized with config: $configuration")
    }
    
    fun destroy() {
        isInitialized = false
        logger.info("ASN.1 decoder destroyed")
    }
    
    /**
     * Decodes a CDR record from the buffer at the specified offset.
     * 
     * @param buffer Byte array containing ASN.1 encoded data
     * @param offset Starting position in the buffer
     * @param maxLength Maximum number of bytes to read
     * @return DecodeResult with the decoded record or failure
     */
    fun decode(buffer: ByteArray, offset: Int, maxLength: Int): DecodeResult<ZteCdrRecord> {
        return try {
            // Create input stream from buffer slice
            val inputStream = ByteArrayInputStream(buffer, offset, maxLength)
            
            // Read the tag to determine CDR type
            val tag = readTag(inputStream)
            if (tag == -1) {
                return DecodeResult.Failure("End of stream reached")
            }
            
            // Read the length
            val length = readLength(inputStream)
            if (length < 0) {
                return DecodeResult.Failure("Invalid length: $length")
            }
            
            // Ensure we have enough data
            if (length > maxLength - inputStream.available()) {
                return DecodeResult.Failure("Insufficient data: need $length bytes")
            }
            
            // Decode based on CDR type
            val record = when (tag) {
                TAG_SGSN_PDP_RECORD -> decodeSgsnPdpRecord(inputStream, length)
                TAG_GGSN_PDP_RECORD -> decodeGgsnPdpRecord(inputStream, length)
                TAG_SGW_RECORD -> decodeSgwRecord(inputStream, length)
                TAG_PGW_RECORD -> decodePgwRecord(inputStream, length)
                TAG_SGSN_MM_RECORD -> decodeSgsnMmRecord(inputStream, length)
                else -> {
                    logger.warn("Unknown CDR type tag: $tag")
                    // Skip unknown record
                    inputStream.skip(length.toLong())
                    return DecodeResult.Failure("Unknown CDR type: $tag")
                }
            }
            
            // Calculate next offset
            val bytesConsumed = maxLength - inputStream.available()
            val nextOffset = offset + bytesConsumed
            
            DecodeResult.Success(record, nextOffset)
            
        } catch (e: Exception) {
            logger.error("Error decoding CDR at offset $offset", e)
            DecodeResult.Failure("Decoding error: ${e.message}", e)
        }
    }
    
    private fun readTag(stream: ByteArrayInputStream): Int {
        val firstByte = stream.read()
        if (firstByte == -1) return -1
        
        // Check if it's a single-byte tag
        if ((firstByte and 0x1F) != 0x1F) {
            return firstByte and 0x1F
        }
        
        // Multi-byte tag
        var tag = 0
        var b: Int
        do {
            b = stream.read()
            if (b == -1) return -1
            tag = (tag shl 7) or (b and 0x7F)
        } while ((b and 0x80) != 0)
        
        return tag
    }
    
    private fun readLength(stream: ByteArrayInputStream): Int {
        val firstByte = stream.read()
        if (firstByte == -1) return -1
        
        // Short form (bit 8 = 0)
        if ((firstByte and 0x80) == 0) {
            return firstByte
        }
        
        // Long form
        val numOctets = firstByte and 0x7F
        if (numOctets > 4) {
            throw IllegalArgumentException("Length too large: $numOctets octets")
        }
        
        var length = 0
        for (i in 0 until numOctets) {
            val b = stream.read()
            if (b == -1) return -1
            length = (length shl 8) or b
        }
        
        return length
    }
    
    private fun decodeSgsnPdpRecord(stream: ByteArrayInputStream, length: Int): ZteCdrRecord {
        val fields = decodeSequenceFields(stream, length)
        
        return ZteCdrRecord(
            recordType = TAG_SGSN_PDP_RECORD,
            recordSequenceNumber = fields[2]?.let { decodeOctetString(it) } ?: "",
            servedImsi = fields[TAG_SERVED_IMSI]?.let { decodeImsi(it) } ?: "",
            servedImei = fields[TAG_SERVED_IMEI]?.let { decodeImei(it) } ?: "",
            servedMsisdn = fields[TAG_SERVED_MSISDN]?.let { decodeMsisdn(it) } ?: "",
            chargingId = fields[TAG_CHARGING_ID]?.let { decodeInteger(it) } ?: 0L,
            recordOpeningTime = fields[TAG_RECORD_OPENING_TIME]?.let { decodeTimestamp(it) } ?: Instant.EPOCH,
            duration = fields[TAG_DURATION]?.let { decodeInteger(it) } ?: 0L,
            sgsnAddress = fields[TAG_SGSN_ADDRESS]?.let { decodeIpAddress(it) } ?: "",
            accessPointName = fields[TAG_ACCESS_POINT_NAME]?.let { decodeOctetString(it) } ?: "",
            pdpType = fields[TAG_PDP_TYPE]?.let { decodePdpType(it) } ?: "",
            servedPdpAddress = fields[TAG_SERVED_PDP_ADDRESS]?.let { decodeIpAddress(it) } ?: "",
            uplinkVolume = fields[TAG_VOLUME_UPLINK]?.let { decodeInteger(it) } ?: 0L,
            downlinkVolume = fields[TAG_VOLUME_DOWNLINK]?.let { decodeInteger(it) } ?: 0L
        ).let { record ->
            record.copy(totalVolume = record.uplinkVolume + record.downlinkVolume)
        }
    }
    
    private fun decodeGgsnPdpRecord(stream: ByteArrayInputStream, length: Int): ZteCdrRecord {
        val fields = decodeSequenceFields(stream, length)
        
        return ZteCdrRecord(
            recordType = TAG_GGSN_PDP_RECORD,
            recordSequenceNumber = fields[2]?.let { decodeOctetString(it) } ?: "",
            servedImsi = fields[TAG_SERVED_IMSI]?.let { decodeImsi(it) } ?: "",
            servedMsisdn = fields[TAG_SERVED_MSISDN]?.let { decodeMsisdn(it) } ?: "",
            chargingId = fields[TAG_CHARGING_ID]?.let { decodeInteger(it) } ?: 0L,
            recordOpeningTime = fields[TAG_RECORD_OPENING_TIME]?.let { decodeTimestamp(it) } ?: Instant.EPOCH,
            duration = fields[TAG_DURATION]?.let { decodeInteger(it) } ?: 0L,
            ggsnAddress = fields[16]?.let { decodeIpAddress(it) } ?: "",
            accessPointName = fields[TAG_ACCESS_POINT_NAME]?.let { decodeOctetString(it) } ?: "",
            pdpType = fields[TAG_PDP_TYPE]?.let { decodePdpType(it) } ?: "",
            servedPdpAddress = fields[TAG_SERVED_PDP_ADDRESS]?.let { decodeIpAddress(it) } ?: "",
            uplinkVolume = fields[TAG_VOLUME_UPLINK]?.let { decodeInteger(it) } ?: 0L,
            downlinkVolume = fields[TAG_VOLUME_DOWNLINK]?.let { decodeInteger(it) } ?: 0L
        ).let { record ->
            record.copy(totalVolume = record.uplinkVolume + record.downlinkVolume)
        }
    }
    
    private fun decodeSgwRecord(stream: ByteArrayInputStream, length: Int): ZteCdrRecord {
        val fields = decodeSequenceFields(stream, length)
        
        return ZteCdrRecord(
            recordType = TAG_SGW_RECORD,
            recordSequenceNumber = fields[2]?.let { decodeOctetString(it) } ?: "",
            servedImsi = fields[TAG_SERVED_IMSI]?.let { decodeImsi(it) } ?: "",
            servedImei = fields[TAG_SERVED_IMEI]?.let { decodeImei(it) } ?: "",
            servedMsisdn = fields[TAG_SERVED_MSISDN]?.let { decodeMsisdn(it) } ?: "",
            chargingId = fields[TAG_CHARGING_ID]?.let { decodeInteger(it) } ?: 0L,
            recordOpeningTime = fields[TAG_RECORD_OPENING_TIME]?.let { decodeTimestamp(it) } ?: Instant.EPOCH,
            duration = fields[TAG_DURATION]?.let { decodeInteger(it) } ?: 0L,
            sgwAddress = fields[32]?.let { decodeIpAddress(it) } ?: "",
            accessPointName = fields[TAG_ACCESS_POINT_NAME]?.let { decodeOctetString(it) } ?: "",
            uplinkVolume = fields[TAG_VOLUME_UPLINK]?.let { decodeInteger(it) } ?: 0L,
            downlinkVolume = fields[TAG_VOLUME_DOWNLINK]?.let { decodeInteger(it) } ?: 0L
        ).let { record ->
            record.copy(totalVolume = record.uplinkVolume + record.downlinkVolume)
        }
    }
    
    private fun decodePgwRecord(stream: ByteArrayInputStream, length: Int): ZteCdrRecord {
        val fields = decodeSequenceFields(stream, length)
        
        return ZteCdrRecord(
            recordType = TAG_PGW_RECORD,
            recordSequenceNumber = fields[2]?.let { decodeOctetString(it) } ?: "",
            servedImsi = fields[TAG_SERVED_IMSI]?.let { decodeImsi(it) } ?: "",
            servedImei = fields[TAG_SERVED_IMEI]?.let { decodeImei(it) } ?: "",
            servedMsisdn = fields[TAG_SERVED_MSISDN]?.let { decodeMsisdn(it) } ?: "",
            chargingId = fields[TAG_CHARGING_ID]?.let { decodeInteger(it) } ?: 0L,
            recordOpeningTime = fields[TAG_RECORD_OPENING_TIME]?.let { decodeTimestamp(it) } ?: Instant.EPOCH,
            duration = fields[TAG_DURATION]?.let { decodeInteger(it) } ?: 0L,
            pgwAddress = fields[33]?.let { decodeIpAddress(it) } ?: "",
            accessPointName = fields[TAG_ACCESS_POINT_NAME]?.let { decodeOctetString(it) } ?: "",
            pdpType = fields[TAG_PDP_TYPE]?.let { decodePdpType(it) } ?: "",
            servedPdpAddress = fields[TAG_SERVED_PDP_ADDRESS]?.let { decodeIpAddress(it) } ?: "",
            uplinkVolume = fields[TAG_VOLUME_UPLINK]?.let { decodeInteger(it) } ?: 0L,
            downlinkVolume = fields[TAG_VOLUME_DOWNLINK]?.let { decodeInteger(it) } ?: 0L
        ).let { record ->
            record.copy(totalVolume = record.uplinkVolume + record.downlinkVolume)
        }
    }
    
    private fun decodeSgsnMmRecord(stream: ByteArrayInputStream, length: Int): ZteCdrRecord {
        val fields = decodeSequenceFields(stream, length)
        
        return ZteCdrRecord(
            recordType = TAG_SGSN_MM_RECORD,
            recordSequenceNumber = fields[2]?.let { decodeOctetString(it) } ?: "",
            servedImsi = fields[TAG_SERVED_IMSI]?.let { decodeImsi(it) } ?: "",
            servedImei = fields[TAG_SERVED_IMEI]?.let { decodeImei(it) } ?: "",
            servedMsisdn = fields[TAG_SERVED_MSISDN]?.let { decodeMsisdn(it) } ?: "",
            recordOpeningTime = fields[TAG_RECORD_OPENING_TIME]?.let { decodeTimestamp(it) } ?: Instant.EPOCH,
            duration = fields[TAG_DURATION]?.let { decodeInteger(it) } ?: 0L,
            sgsnAddress = fields[TAG_SGSN_ADDRESS]?.let { decodeIpAddress(it) } ?: "",
            locationAreaCode = fields[20]?.let { decodeInteger(it)?.toInt() } ?: 0,
            routingAreaCode = fields[21]?.let { decodeOctetString(it) } ?: "",
            cellIdentifier = fields[22]?.let { decodeInteger(it) } ?: 0L
        )
    }
    
    private fun decodeSequenceFields(stream: ByteArrayInputStream, length: Int): Map<Int, ByteArray> {
        val fields = mutableMapOf<Int, ByteArray>()
        val endPosition = stream.available() - length
        
        while (stream.available() > endPosition) {
            val tag = readTag(stream)
            if (tag == -1) break
            
            val fieldLength = readLength(stream)
            if (fieldLength < 0) break
            
            val fieldData = ByteArray(fieldLength)
            val bytesRead = stream.read(fieldData)
            if (bytesRead != fieldLength) break
            
            fields[tag] = fieldData
        }
        
        return fields
    }
    
    private fun decodeImsi(data: ByteArray): String {
        return data.joinToString("") { byte ->
            val high = (byte.toInt() shr 4) and 0x0F
            val low = byte.toInt() and 0x0F
            if (high == 0x0F) {
                low.toString()
            } else {
                "$low$high"
            }
        }
    }
    
    private fun decodeImei(data: ByteArray): String {
        return decodeImsi(data) // Same encoding as IMSI
    }
    
    private fun decodeMsisdn(data: ByteArray): String {
        if (data.isEmpty()) return ""
        
        // Skip first byte (type of number)
        return data.drop(1).joinToString("") { byte ->
            val high = (byte.toInt() shr 4) and 0x0F
            val low = byte.toInt() and 0x0F
            if (high == 0x0F) {
                low.toString()
            } else {
                "$low$high"
            }
        }
    }
    
    private fun decodeInteger(data: ByteArray): Long {
        return data.fold(0L) { acc, byte ->
            (acc shl 8) or (byte.toInt() and 0xFF).toLong()
        }
    }
    
    private fun decodeTimestamp(data: ByteArray): Instant {
        return try {
            // ZTE format: YYMMDDhhmmss+ZZZZ (BCD encoded)
            val bcdString = data.take(7).joinToString("") { byte ->
                val high = (byte.toInt() shr 4) and 0x0F
                val low = byte.toInt() and 0x0F
                "$high$low"
            }
            
            // Parse as LocalDateTime and convert to Instant
            val dateTime = LocalDateTime.parse(bcdString.take(12), TIMESTAMP_FORMATTER)
            dateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            logger.warn("Failed to parse timestamp", e)
            Instant.EPOCH
        }
    }
    
    private fun decodeIpAddress(data: ByteArray): String {
        return when (data.size) {
            4 -> // IPv4
                data.joinToString(".") { (it.toInt() and 0xFF).toString() }
            16 -> // IPv6
                data.toList().chunked(2).joinToString(":") { bytes ->
                    bytes.joinToString("") { "%02x".format(it) }
                }
            else -> ""
        }
    }
    
    private fun decodePdpType(data: ByteArray): String {
        return when (data.firstOrNull()?.toInt()) {
            0x21 -> "IPv4"
            0x57 -> "IPv6"
            0x8D -> "IPv4v6"
            else -> "Unknown"
        }
    }
    
    private fun decodeOctetString(data: ByteArray): String {
        return data.toString(Charsets.UTF_8).trim('\u0000')
    }
}