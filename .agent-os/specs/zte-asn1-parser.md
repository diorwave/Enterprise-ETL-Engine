# ZTE ASN.1 CDR Parser Specification

## Overview

**Parser ID**: `zte-asn1-parser`  
**Version**: 1.0.0  
**Author**: TIA ETL System  
**Target**: ZTE ZXUN CG ASN.1 CDR files processing  

This specification defines a Kotlin-based parser implementation for processing ZTE ASN.1 encoded Call Detail Records (CDRs) from ZXUN CG telecommunications equipment. The parser will be dynamically loadable and integrate with the TIA ETL system architecture.

## 1. Architecture Requirements

### 1.1 Interface Implementation

The parser must implement the `DataParser` interface from the TIA ETL system:

```kotlin
class ZteAsn1Parser : DataParser {
    override fun getMetadata(): ParserMetadata
    override fun process(context: ProcessingContext): ProcessingResult
}
```

### 1.2 Key Dependencies

- **jasn1-compiler**: `org.openmuc:jasn1-compiler:1.10.0` for ASN.1 BER encoding/decoding
- **Kotlin**: 2.0.21 with coroutines support
- **Spring Boot**: 3.3.5 for dependency injection and configuration
- **ClickHouse JDBC**: 0.7.1 for batch database operations
- **SLF4J**: For logging integration

## 2. File Format Support

### 2.1 Supported CDR Types

The parser must support the following CDR record types as defined in ZXUN CG v7.2.1:

| CDR Type | Record Name | Tag | Description |
|----------|-------------|-----|-------------|
| S-CDR | SGSNPDPRecord | [20] | SGSN PDP Context CDR |
| G-CDR | GGSNPDPRecord | [21] | GGSN PDP Context CDR |
| M-CDR | SGSNMMRecord | [22] | SGSN Mobility Management CDR |
| S-SMO-CDR | SGSNSMORecord | [23] | SGSN SMS Mobile Originated CDR |
| S-SMT-CDR | SGSNSMTRecord | [24] | SGSN SMS Mobile Terminated CDR |
| LCS-MO-CDR | SGSNMOLCSRecord | [26] | SGSN LCS Mobile Originated CDR |
| LCS-MT-CDR | SGSNMTLCSRecord | [25] | SGSN LCS Mobile Terminated CDR |
| LCS-NI-CDR | SGSNNILCSRecord | [27] | SGSN LCS Network Initiated CDR |
| S-MB-CDR | SGSNMBMSRecord | [29] | SGSN MBMS CDR |
| G-MB-CDR | GGSNMBMSRecord | [30] | GGSN MBMS CDR |
| SGW-CDR | SGWRecord | [84] | Serving Gateway CDR |
| PGW-CDR | PGWRecord | [85] | PDN Gateway CDR |
| W-CDR | WLANRecord | [86] | WLAN CDR |
| HSGW-CDR | HSGWRecord | [87] | HRPD Serving Gateway CDR |
| MBMS-GW-CDR | GWMBMSRecord | [88] | MBMS Gateway CDR |
| ePDG-CDR | EPDGRecord | [89] | Evolved Packet Data Gateway CDR |

### 2.2 File Structure

#### Block-Based Organization
- **Block sizes**: 2048, 4096, or 8192 bytes (configurable)
- **Block structure**: Contains multiple variable-length CDRs
- **Filler bytes**: 0xFF bytes pad blocks to fixed size
- **CDR boundaries**: No CDR spans across blocks

#### ASN.1 BER Encoding
- **Encoding**: Basic Encoding Rules (BER) as per ITU-T X.690
- **Structure**: TLV (Tag-Length-Value) format
- **Root element**: `CallEventRecord` CHOICE containing specific CDR types

## 3. Parser Metadata

### 3.1 Parser Identification

```kotlin
ParserMetadata(
    parserId = "zte-asn1-parser",
    name = "ZTE ASN.1 CDR Parser",
    version = "1.0.0",
    description = "Processes ZTE ZXUN CG ASN.1 encoded CDR files with support for multiple record types",
    supportedFormats = listOf("asn1", "ber", "cdr"),
    targetTable = "zte_cdr_records",
    schemaDefinition = zteSchemaDefinition,
    maxFileSize = 1024 * 1024 * 1024L, // 1GB
    batchSize = 1000,
    requiresValidation = true
)
```

### 3.2 Target Table Schema

The parser targets a ClickHouse table with the following structure:

```sql
CREATE TABLE zte_cdr_records (
    -- Record identification
    record_type UInt8,
    record_sequence_number String,
    processing_id UUID,
    file_name String,
    block_number UInt32,
    record_number_in_block UInt16,
    
    -- Common CDR fields
    served_imsi Nullable(String),
    served_imei Nullable(String),
    served_msisdn Nullable(String),
    charging_id Nullable(UInt32),
    record_opening_time Nullable(DateTime('UTC')),
    record_closing_time Nullable(DateTime('UTC')),
    duration Nullable(UInt32),
    
    -- Network information
    sgsn_address Nullable(String),
    ggsn_address Nullable(String),
    sgw_address Nullable(String),
    pgw_address Nullable(String),
    
    -- Location information
    location_area_code Nullable(UInt16),
    routing_area_code Nullable(String),
    cell_identifier Nullable(UInt32),
    user_location_information Nullable(String),
    
    -- Traffic information
    uplink_volume Nullable(UInt64),
    downlink_volume Nullable(UInt64),
    total_volume Nullable(UInt64),
    
    -- Service information
    access_point_name Nullable(String),
    pdp_type Nullable(String),
    served_pdp_address Nullable(String),
    qos_information Nullable(String),
    
    -- Additional metadata
    raw_record String, -- Base64 encoded original ASN.1 data
    parsed_timestamp DateTime('UTC'),
    parser_version String
    
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(record_opening_time)
ORDER BY (record_type, record_opening_time, charging_id)
```

## 4. Processing Implementation

### 4.1 File Processing Workflow

```kotlin
class ZteAsn1Parser : DataParser {
    
    override fun process(context: ProcessingContext): ProcessingResult {
        return try {
            val startTime = System.currentTimeMillis()
            var totalRecords = 0L
            var successfulRecords = 0L
            var failedRecords = 0L
            val errors = mutableListOf<ProcessingError>()
            
            // 1. Determine file organization (block-based or CDR-based)
            val fileOrganization = detectFileOrganization(context.file)
            
            // 2. Process based on organization type
            when (fileOrganization) {
                FileOrganization.BLOCK_BASED -> processBlockBasedFile(context)
                FileOrganization.CDR_BASED -> processCdrBasedFile(context)
            }
            
            // 3. Build processing result
            ProcessingResult(
                totalRecords = totalRecords,
                successfulRecords = successfulRecords,
                failedRecords = failedRecords,
                processingTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                errors = errors,
                bytesProcessed = context.file.length()
            )
        } catch (e: Exception) {
            throw ParserException("Critical error processing file: ${e.message}", e)
        }
    }
}
```

### 4.2 Block-Based Processing

```kotlin
private fun processBlockBasedFile(context: ProcessingContext): ProcessingStats {
    val blockSize = detectBlockSize(context.file)
    val totalBlocks = (context.file.length() + blockSize - 1) / blockSize
    
    context.file.inputStream().buffered().use { input ->
        for (blockNumber in 0 until totalBlocks) {
            processBlock(input, blockNumber, blockSize, context)
        }
    }
}

private fun processBlock(
    input: InputStream, 
    blockNumber: Long, 
    blockSize: Int, 
    context: ProcessingContext
) {
    val blockData = ByteArray(blockSize)
    val bytesRead = input.read(blockData)
    
    if (bytesRead <= 0) return
    
    var offset = 0
    var recordCount = 0
    
    while (offset < bytesRead && blockData[offset] != 0xFF.toByte()) {
        try {
            val (cdr, nextOffset) = parseCdrRecord(blockData, offset)
            if (cdr != null) {
                insertCdrToDatabase(cdr, blockNumber, recordCount, context)
                recordCount++
            }
            offset = nextOffset
        } catch (e: Exception) {
            context.log(LogLevel.ERROR, "Error parsing CDR at block $blockNumber, offset $offset", e)
            break // Skip rest of block on parse error
        }
    }
}
```

### 4.3 ASN.1 Parsing

```kotlin
private fun parseCdrRecord(data: ByteArray, offset: Int): Pair<CdrRecord?, Int> {
    try {
        val decoder = BerInputStream(data.inputStream().skip(offset))
        
        // Parse the root CallEventRecord CHOICE
        val callEventRecord = CallEventRecord()
        callEventRecord.decode(decoder)
        
        // Determine CDR type and extract data
        val cdr = when {
            callEventRecord.sgsnPDPRecord != null -> 
                extractSgsnPdpRecord(callEventRecord.sgsnPDPRecord)
            callEventRecord.ggsnPDPRecord != null -> 
                extractGgsnPdpRecord(callEventRecord.ggsnPDPRecord)
            callEventRecord.sgwRecord != null -> 
                extractSgwRecord(callEventRecord.sgwRecord)
            callEventRecord.pgwRecord != null -> 
                extractPgwRecord(callEventRecord.pgwRecord)
            // ... handle other CDR types
            else -> {
                context.log(LogLevel.WARN, "Unknown CDR type at offset $offset")
                null
            }
        }
        
        val nextOffset = offset + decoder.bytesRead
        return Pair(cdr, nextOffset)
        
    } catch (e: IOException) {
        throw ParserException("Failed to parse ASN.1 data at offset $offset", e)
    }
}
```

### 4.4 CDR Data Extraction

```kotlin
private fun extractSgsnPdpRecord(record: SGSNPDPRecord): CdrRecord {
    return CdrRecord(
        recordType = CallEventRecordType.sgsnPDPRecord.value,
        servedImsi = record.servedIMSI?.toString(),
        servedImei = record.servedIMEI?.toString(),
        chargingId = record.chargingID?.value,
        recordOpeningTime = parseTimeStamp(record.recordOpeningTime),
        duration = record.duration?.value,
        sgsnAddress = record.sgsnAddressList?.firstOrNull()?.toString(),
        locationAreaCode = record.locationAreaCode?.value,
        routingAreaCode = record.routingArea?.toString(),
        cellIdentifier = record.cellIdentifier?.value,
        accessPointName = record.accessPointNameNI?.toString(),
        pdpType = record.pdpType?.toString(),
        servedPdpAddress = record.servedPDPAddress?.toString(),
        uplinkVolume = record.listOfTrafficVolumes?.sumOf { it.uplinkVolume?.value ?: 0L },
        downlinkVolume = record.listOfTrafficVolumes?.sumOf { it.downlinkVolume?.value ?: 0L },
        // ... extract other fields
        rawRecord = Base64.getEncoder().encodeToString(record.encode())
    )
}
```

### 4.5 Database Operations

```kotlin
private fun insertCdrToDatabase(
    cdr: CdrRecord, 
    blockNumber: Long, 
    recordNumber: Int, 
    context: ProcessingContext
) {
    val sql = """
        INSERT INTO zte_cdr_records (
            record_type, record_sequence_number, processing_id, file_name,
            block_number, record_number_in_block, served_imsi, served_imei,
            charging_id, record_opening_time, duration, sgsn_address,
            location_area_code, routing_area_code, cell_identifier,
            access_point_name, pdp_type, served_pdp_address,
            uplink_volume, downlink_volume, raw_record,
            parsed_timestamp, parser_version
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """
    
    context.clickHouseConnection.prepareStatement(sql).use { stmt ->
        stmt.setInt(1, cdr.recordType)
        stmt.setString(2, cdr.recordSequenceNumber)
        stmt.setString(3, context.processingId.toString())
        stmt.setString(4, context.file.name)
        stmt.setLong(5, blockNumber)
        stmt.setInt(6, recordNumber)
        stmt.setString(7, cdr.servedImsi)
        // ... set other parameters
        stmt.executeUpdate()
    }
}
```

## 5. Error Handling

### 5.1 Error Categories

- **File Format Errors**: Invalid block structure, corrupted ASN.1 data
- **Parse Errors**: Invalid CDR structure, unknown record types
- **Database Errors**: Connection failures, constraint violations
- **Memory Errors**: Exceeding memory limits during processing

### 5.2 Recovery Strategies

```kotlin
private fun handleParseError(error: Exception, context: ProcessingContext): ProcessingError {
    return when (error) {
        is IOException -> ProcessingError(
            message = "Failed to read file data: ${error.message}",
            severity = ErrorSeverity.CRITICAL,
            exception = error
        )
        is BerDecodeException -> ProcessingError(
            message = "Invalid ASN.1 BER encoding: ${error.message}",
            severity = ErrorSeverity.ERROR,
            exception = error
        )
        else -> ProcessingError(
            message = "Unexpected error: ${error.message}",
            severity = ErrorSeverity.ERROR,
            exception = error
        )
    }
}
```

## 6. Performance Requirements

### 6.1 Processing Targets

- **Throughput**: Process minimum 100 files/hour
- **Memory usage**: Stay within configured limits (default 1GB)
- **Batch processing**: Insert records in batches of 1000 for optimal performance
- **Error tolerance**: Continue processing after individual record failures

### 6.2 Optimization Strategies

```kotlin
class BatchProcessor(private val batchSize: Int = 1000) {
    private val batch = mutableListOf<CdrRecord>()
    
    fun addRecord(record: CdrRecord, context: ProcessingContext) {
        batch.add(record)
        if (batch.size >= batchSize) {
            flushBatch(context)
        }
    }
    
    private fun flushBatch(context: ProcessingContext) {
        if (batch.isEmpty()) return
        
        val sql = buildBatchInsertSql()
        context.clickHouseConnection.prepareStatement(sql).use { stmt ->
            batch.forEach { record ->
                // Set parameters for batch insert
            }
            stmt.executeBatch()
        }
        batch.clear()
    }
}
```

## 7. Testing Requirements

### 7.1 Unit Tests

- ASN.1 parsing logic with sample CDR data
- Block detection and processing
- CDR extraction for each record type
- Error handling scenarios
- Database operation mocking

### 7.2 Integration Tests

- Processing real ZTE CDR files with Testcontainers
- ClickHouse integration with sample data
- Performance testing with large files
- Memory usage validation

### 7.3 Test Data Requirements

- Sample CDR files for each supported record type
- Corrupted files for error handling tests
- Large files for performance testing
- Files with different block sizes

## 8. Deployment

### 8.1 JAR Structure

```
zte-asn1-parser-1.0.0.jar
├── META-INF/
│   ├── MANIFEST.MF
│   └── services/
│       └── com.tia.etl.parser.api.DataParser
├── com/
│   └── tia/
│       └── etl/
│           └── parser/
│               └── zte/
│                   ├── ZteAsn1Parser.kt
│                   ├── models/
│                   ├── asn1/ (generated classes)
│                   └── utils/
└── asn1-definitions/
    └── ZTECdrDefinitions.asn
```

### 8.2 Configuration

```yaml
# application.yml
zte-parser:
  block-sizes: [2048, 4096, 8192]
  batch-size: 1000
  max-memory-mb: 1024
  supported-versions: ["v7.2.1", "v7.1.0"]
  
clickhouse:
  batch-size: 1000
  connection-timeout: 30s
```

## 9. Monitoring and Logging

### 9.1 Metrics

- Records processed per second
- Parse success/failure rates
- Memory usage during processing
- Database insertion performance

### 9.2 Logging

```kotlin
context.log(LogLevel.INFO, "Started processing file with ${totalBlocks} blocks")
context.log(LogLevel.DEBUG, "Parsed ${recordType} CDR with charging ID ${chargingId}")
context.log(LogLevel.WARN, "Skipped unknown CDR type at block ${blockNumber}")
context.log(LogLevel.ERROR, "Failed to insert batch: ${error.message}", error)
```

## 10. Versioning and Maintenance

### 10.1 Version Compatibility

- Support multiple ZTE CDR format versions
- Graceful handling of new CDR types
- Backward compatibility maintenance

### 10.2 ASN.1 Schema Updates

- Regenerate ASN.1 classes when format changes
- Version detection based on record structure
- Migration path for schema changes

This specification provides a comprehensive foundation for implementing the ZTE ASN.1 CDR parser that integrates seamlessly with the TIA ETL system architecture while meeting all performance and reliability requirements.