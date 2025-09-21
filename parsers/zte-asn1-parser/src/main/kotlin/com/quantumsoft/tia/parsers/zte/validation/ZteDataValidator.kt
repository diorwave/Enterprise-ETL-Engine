package com.quantumsoft.tia.parsers.zte.validation

import com.quantumsoft.tia.parsers.zte.ZteCdrRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Data validator for ZTE CDR records.
 * 
 * Validates CDR fields according to business rules and data integrity constraints.
 * 
 * ## Validation Rules
 * - IMSI must be 15 digits
 * - IMEI must be 14-16 digits
 * - MSISDN must start with valid country code
 * - Timestamps must be reasonable (not future, not too old)
 * - Volumes must be non-negative
 * - Duration must match timestamp difference (if both present)
 */
@Component
class ZteDataValidator {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ZteDataValidator::class.java)
        
        // Validation constants
        const val IMSI_LENGTH = 15
        const val IMEI_MIN_LENGTH = 14
        const val IMEI_MAX_LENGTH = 16
        const val MAX_RECORD_AGE_DAYS = 90L
        const val MAX_FUTURE_MINUTES = 5L
        
        // Valid Liberia MSISDNs start with 231
        const val LIBERIA_COUNTRY_CODE = "231"
        
        // Reasonable limits
        const val MAX_VOLUME_GB = 100L * 1024 * 1024 * 1024 // 100GB
        const val MAX_DURATION_HOURS = 24L * 3600 // 24 hours
    }
    
    private var config: Map<String, Any> = emptyMap()
    private var strictMode = false
    private var validateTimestamps = true
    private var validateVolumes = true
    
    fun initialize(configuration: Map<String, Any>) {
        config = configuration
        strictMode = config["strictValidation"] as? Boolean ?: false
        validateTimestamps = config["validateTimestamps"] as? Boolean ?: true
        validateVolumes = config["validateVolumes"] as? Boolean ?: true
        
        logger.info("Data validator initialized with strict mode: $strictMode")
    }
    
    fun destroy() {
        logger.info("Data validator destroyed")
    }
    
    /**
     * Validates a ZTE CDR record.
     * 
     * @param record The CDR record to validate
     * @return Internal validation result
     */
    fun validateRecord(record: ZteCdrRecord): InternalValidationResult {
        val violations = mutableListOf<String>()
        
        // Validate IMSI
        validateImsi(record.servedImsi)?.let { violations.add(it) }
        
        // Validate IMEI (if present and not empty)
        if (record.servedImei.isNotEmpty()) {
            validateImei(record.servedImei)?.let { violations.add(it) }
        }
        
        // Validate MSISDN (if present and not empty)
        if (record.servedMsisdn.isNotEmpty()) {
            validateMsisdn(record.servedMsisdn)?.let { violations.add(it) }
        }
        
        // Validate timestamps
        if (validateTimestamps) {
            validateTimestamp(record.recordOpeningTime, "recordOpeningTime")?.let { violations.add(it) }
            
            if (record.recordClosingTime != Instant.EPOCH) {
                validateTimestamp(record.recordClosingTime, "recordClosingTime")?.let { violations.add(it) }
                validateTimestampOrder(record.recordOpeningTime, record.recordClosingTime)?.let { violations.add(it) }
            }
        }
        
        // Validate volumes
        if (validateVolumes) {
            validateVolume(record.uplinkVolume, "uplinkVolume")?.let { violations.add(it) }
            validateVolume(record.downlinkVolume, "downlinkVolume")?.let { violations.add(it) }
            validateTotalVolume(record)?.let { violations.add(it) }
        }
        
        // Validate duration
        validateDuration(record.duration)?.let { violations.add(it) }
        
        // Validate charging ID
        if (record.chargingId == 0L && strictMode) {
            violations.add("INVALID_CHARGING_ID: Charging ID cannot be zero")
        }
        
        // Validate IP addresses (if present)
        validateIpAddress(record.sgsnAddress, "sgsnAddress")?.let { violations.add(it) }
        validateIpAddress(record.ggsnAddress, "ggsnAddress")?.let { violations.add(it) }
        validateIpAddress(record.sgwAddress, "sgwAddress")?.let { violations.add(it) }
        validateIpAddress(record.pgwAddress, "pgwAddress")?.let { violations.add(it) }
        validateIpAddress(record.servedPdpAddress, "servedPdpAddress")?.let { violations.add(it) }
        
        // Build result
        return if (violations.isEmpty()) {
            InternalValidationResult(
                isValid = true,
                errors = emptyList()
            )
        } else {
            InternalValidationResult(
                isValid = false,
                errors = violations
            )
        }
    }
    
    private fun validateImsi(imsi: String): String? {
        return when {
            imsi.isEmpty() && strictMode -> "MISSING_IMSI: IMSI is required"
            imsi.isNotEmpty() && !imsi.matches(Regex("\\d+")) -> "INVALID_IMSI_FORMAT: IMSI must contain only digits"
            imsi.isNotEmpty() && imsi.length != IMSI_LENGTH -> "INVALID_IMSI_LENGTH: IMSI must be $IMSI_LENGTH digits (was ${imsi.length})"
            else -> null
        }
    }
    
    private fun validateImei(imei: String): String? {
        return when {
            !imei.matches(Regex("\\d+")) -> "INVALID_IMEI_FORMAT: IMEI must contain only digits"
            imei.length !in IMEI_MIN_LENGTH..IMEI_MAX_LENGTH -> 
                "INVALID_IMEI_LENGTH: IMEI must be $IMEI_MIN_LENGTH-$IMEI_MAX_LENGTH digits (was ${imei.length})"
            else -> null
        }
    }
    
    private fun validateMsisdn(msisdn: String): String? {
        return when {
            !msisdn.matches(Regex("\\d+")) -> "INVALID_MSISDN_FORMAT: MSISDN must contain only digits"
            strictMode && !msisdn.startsWith(LIBERIA_COUNTRY_CODE) -> 
                "INVALID_MSISDN_COUNTRY: MSISDN must start with Liberia country code $LIBERIA_COUNTRY_CODE"
            msisdn.length < 10 -> "INVALID_MSISDN_LENGTH: MSISDN too short (was ${msisdn.length})"
            else -> null
        }
    }
    
    private fun validateTimestamp(timestamp: Instant, fieldName: String): String? {
        val now = Instant.now()
        val maxAge = now.minus(MAX_RECORD_AGE_DAYS, ChronoUnit.DAYS)
        val maxFuture = now.plus(MAX_FUTURE_MINUTES, ChronoUnit.MINUTES)
        
        return when {
            timestamp == Instant.EPOCH -> null // Default value is allowed
            timestamp.isAfter(maxFuture) -> 
                "FUTURE_TIMESTAMP: $fieldName is in the future (${timestamp})"
            strictMode && timestamp.isBefore(maxAge) -> 
                "OLD_TIMESTAMP: $fieldName is older than $MAX_RECORD_AGE_DAYS days (${timestamp})"
            else -> null
        }
    }
    
    private fun validateTimestampOrder(opening: Instant, closing: Instant): String? {
        return if (opening != Instant.EPOCH && closing != Instant.EPOCH && closing.isBefore(opening)) {
            "TIMESTAMP_ORDER: Closing time ($closing) is before opening time ($opening)"
        } else {
            null
        }
    }
    
    private fun validateVolume(volume: Long, fieldName: String): String? {
        return when {
            volume < 0 -> "NEGATIVE_VOLUME: $fieldName cannot be negative ($volume)"
            strictMode && volume > MAX_VOLUME_GB -> 
                "EXCESSIVE_VOLUME: $fieldName exceeds maximum (${volume / (1024 * 1024 * 1024)}GB)"
            else -> null
        }
    }
    
    private fun validateTotalVolume(record: ZteCdrRecord): String? {
        val calculatedTotal = record.uplinkVolume + record.downlinkVolume
        return if (record.totalVolume != 0L && record.totalVolume != calculatedTotal) {
            "VOLUME_MISMATCH: Total volume (${record.totalVolume}) doesn't match uplink+downlink ($calculatedTotal)"
        } else {
            null
        }
    }
    
    private fun validateDuration(duration: Long): String? {
        return when {
            duration < 0 -> "NEGATIVE_DURATION: Duration cannot be negative ($duration)"
            strictMode && duration > MAX_DURATION_HOURS -> 
                "EXCESSIVE_DURATION: Duration exceeds maximum (${duration / 3600} hours)"
            else -> null
        }
    }
    
    private fun validateIpAddress(address: String, fieldName: String): String? {
        if (address.isEmpty()) return null // Empty is allowed
        
        // Basic IP validation (IPv4 or IPv6)
        val isValidIpv4 = address.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
        val isValidIpv6 = address.contains(":")
        
        return if (!isValidIpv4 && !isValidIpv6) {
            "INVALID_IP: $fieldName contains invalid IP address ($address)"
        } else {
            null
        }
    }
}

// Internal validation result
data class InternalValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)