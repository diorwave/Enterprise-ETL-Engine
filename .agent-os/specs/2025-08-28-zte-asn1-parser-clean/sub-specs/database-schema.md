# Database Schema

This is the database schema implementation for the spec detailed in @.agent-os/specs/2025-08-28-zte-asn1-parser-clean/spec.md

> Created: 2025-08-28
> Version: 1.0.0

## Schema Changes

### ClickHouse Table Design

#### Core Requirements
- **No nullable fields**: All columns must have NOT NULL constraints with appropriate default values
- **No raw_record field**: Eliminates Base64 encoded ASN.1 storage to reduce storage overhead
- **Monthly partitioning**: Partition by call_start_time for optimal query performance
- **Optimized data types**: Use most efficient ClickHouse types for each field

#### Primary Table: zte_cdr_records

```sql
CREATE TABLE zte_cdr_records
(
    -- Record Identification
    record_id           String NOT NULL,
    file_name           String NOT NULL,
    record_sequence     UInt32 NOT NULL,
    processing_time     DateTime('UTC') NOT NULL,
    
    -- Call Identification
    call_id             String NOT NULL DEFAULT '',
    imsi               String NOT NULL DEFAULT '',
    msisdn             String NOT NULL DEFAULT '',
    imei               String NOT NULL DEFAULT '',
    
    -- Network Information
    serving_cell_id     String NOT NULL DEFAULT '',
    lac                UInt32 NOT NULL DEFAULT 0,
    rac                UInt32 NOT NULL DEFAULT 0,
    mcc                String NOT NULL DEFAULT '',
    mnc                String NOT NULL DEFAULT '',
    
    -- Call Details
    call_type          Enum8('voice' = 1, 'sms' = 2, 'data' = 3, 'unknown' = 0) NOT NULL DEFAULT 0,
    call_direction     Enum8('mo' = 1, 'mt' = 2, 'unknown' = 0) NOT NULL DEFAULT 0,
    service_type       String NOT NULL DEFAULT '',
    
    -- Timing Information  
    call_start_time    DateTime('UTC') NOT NULL,
    call_end_time      DateTime('UTC') NOT NULL,
    call_duration      UInt32 NOT NULL DEFAULT 0,
    setup_duration     UInt32 NOT NULL DEFAULT 0,
    
    -- Usage Metrics
    data_volume_uplink    UInt64 NOT NULL DEFAULT 0,
    data_volume_downlink  UInt64 NOT NULL DEFAULT 0,
    data_volume_total     UInt64 NOT NULL DEFAULT 0,
    
    -- Quality Metrics
    signal_strength    Int8 NOT NULL DEFAULT -128,
    quality_indicator  UInt8 NOT NULL DEFAULT 0,
    error_code        UInt32 NOT NULL DEFAULT 0,
    
    -- Charging Information
    charging_id       String NOT NULL DEFAULT '',
    tariff_class     UInt16 NOT NULL DEFAULT 0,
    charge_amount    Decimal64(4) NOT NULL DEFAULT 0,
    currency_code    String NOT NULL DEFAULT '',
    
    -- Location Information
    location_area     String NOT NULL DEFAULT '',
    routing_area      String NOT NULL DEFAULT '',
    user_location     String NOT NULL DEFAULT '',
    
    -- Additional Context
    equipment_type    String NOT NULL DEFAULT '',
    software_version  String NOT NULL DEFAULT '',
    protocol_version  String NOT NULL DEFAULT '',
    
    -- Audit Fields
    created_at        DateTime('UTC') NOT NULL DEFAULT now(),
    created_by        String NOT NULL DEFAULT 'zte-asn1-parser'
)
ENGINE = MergeTree()
PARTITION BY toYYYYMM(call_start_time)
ORDER BY (call_start_time, record_id)
SETTINGS index_granularity = 8192;
```

#### Indexes for Query Optimization

```sql
-- Primary search indexes
ALTER TABLE zte_cdr_records ADD INDEX idx_msisdn (msisdn) TYPE bloom_filter GRANULARITY 1;
ALTER TABLE zte_cdr_records ADD INDEX idx_imsi (imsi) TYPE bloom_filter GRANULARITY 1;
ALTER TABLE zte_cdr_records ADD INDEX idx_call_id (call_id) TYPE bloom_filter GRANULARITY 1;

-- Network analysis indexes
ALTER TABLE zte_cdr_records ADD INDEX idx_serving_cell (serving_cell_id) TYPE bloom_filter GRANULARITY 1;
ALTER TABLE zte_cdr_records ADD INDEX idx_location (location_area) TYPE bloom_filter GRANULARITY 1;

-- Performance monitoring indexes  
ALTER TABLE zte_cdr_records ADD INDEX idx_error_code (error_code) TYPE bloom_filter GRANULARITY 1;
ALTER TABLE zte_cdr_records ADD INDEX idx_quality (quality_indicator) TYPE minmax GRANULARITY 1;
```

#### Materialized Views for Analytics

```sql
-- Hourly aggregation view
CREATE MATERIALIZED VIEW zte_cdr_hourly_stats
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(call_hour)
ORDER BY (call_hour, call_type, call_direction, serving_cell_id)
AS SELECT
    toStartOfHour(call_start_time) as call_hour,
    call_type,
    call_direction,
    serving_cell_id,
    count() as call_count,
    sum(call_duration) as total_duration,
    sum(data_volume_total) as total_data_volume,
    avg(signal_strength) as avg_signal_strength,
    countIf(error_code != 0) as error_count
FROM zte_cdr_records
GROUP BY call_hour, call_type, call_direction, serving_cell_id;

-- Daily summary view
CREATE MATERIALIZED VIEW zte_cdr_daily_summary
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(call_date)
ORDER BY (call_date, serving_cell_id)
AS SELECT
    toDate(call_start_time) as call_date,
    serving_cell_id,
    count() as daily_call_count,
    uniq(msisdn) as unique_subscribers,
    sum(data_volume_total) as daily_data_volume,
    avgIf(signal_strength, signal_strength > -128) as avg_signal_strength,
    countIf(error_code != 0) as daily_error_count
FROM zte_cdr_records
GROUP BY call_date, serving_cell_id;
```

### Data Type Mapping

#### ASN.1 to ClickHouse Type Mapping

| ASN.1 Type | ZTE Field | ClickHouse Type | Default Value | Validation |
|------------|-----------|-----------------|---------------|------------|
| OCTET STRING | MSISDN | String | '' | Length 1-15, numeric |
| OCTET STRING | IMSI | String | '' | Length 15, numeric |
| OCTET STRING | IMEI | String | '' | Length 15, numeric |
| INTEGER | Call Duration | UInt32 | 0 | >= 0, <= 86400 |
| INTEGER | Data Volume | UInt64 | 0 | >= 0 |
| ENUMERATED | Call Type | Enum8 | 0 | Valid enum values |
| UTCTime | Timestamps | DateTime('UTC') | 1970-01-01 00:00:00 | Valid UTC datetime |
| OCTET STRING | Cell ID | String | '' | Hex format validation |
| INTEGER | Signal Strength | Int8 | -128 | -128 to 0 range |

#### Default Value Strategy

1. **Numeric Fields**: Default to 0 or appropriate minimum value
2. **String Fields**: Default to empty string ('')
3. **Enum Fields**: Default to 'unknown' or appropriate default
4. **DateTime Fields**: Default to Unix epoch or processing time
5. **Boolean-like Fields**: Default to false/0 equivalent

### Storage Optimization

#### Compression Strategy
```sql
-- Optimize compression for string fields
ALTER TABLE zte_cdr_records MODIFY COLUMN msisdn String CODEC(LZ4);
ALTER TABLE zte_cdr_records MODIFY COLUMN imsi String CODEC(LZ4);
ALTER TABLE zte_cdr_records MODIFY COLUMN serving_cell_id String CODEC(LZ4);

-- Optimize compression for time series data
ALTER TABLE zte_cdr_records MODIFY COLUMN call_start_time DateTime('UTC') CODEC(DoubleDelta, LZ4);
ALTER TABLE zte_cdr_records MODIFY COLUMN call_duration UInt32 CODEC(T64, LZ4);
```

#### Partitioning Strategy
- **Monthly Partitions**: Based on call_start_time for optimal query performance
- **Automatic Cleanup**: TTL policy for old partitions (configurable retention)
- **Partition Pruning**: Queries filtered by date range leverage partition elimination

## Migrations

### Migration Scripts

#### Initial Schema Creation
```sql
-- File: V1.0.0__Create_ZTE_CDR_Table.sql
-- Create the main ZTE CDR table with all required fields
-- and proper constraints

CREATE TABLE IF NOT EXISTS zte_cdr_records
(
    -- [Full table definition as shown above]
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(call_start_time)
ORDER BY (call_start_time, record_id)
SETTINGS index_granularity = 8192;
```

#### Index Creation
```sql
-- File: V1.0.1__Create_ZTE_CDR_Indexes.sql
-- Add performance indexes for common query patterns

ALTER TABLE zte_cdr_records ADD INDEX IF NOT EXISTS idx_msisdn (msisdn) TYPE bloom_filter GRANULARITY 1;
ALTER TABLE zte_cdr_records ADD INDEX IF NOT EXISTS idx_imsi (imsi) TYPE bloom_filter GRANULARITY 1;
-- [Additional indexes as defined above]
```

#### Materialized Views Creation
```sql
-- File: V1.0.2__Create_ZTE_CDR_Views.sql
-- Create materialized views for analytics

CREATE MATERIALIZED VIEW IF NOT EXISTS zte_cdr_hourly_stats
-- [Full view definition as shown above]

CREATE MATERIALIZED VIEW IF NOT EXISTS zte_cdr_daily_summary  
-- [Full view definition as shown above]
```

### Data Validation Rules

#### Pre-insertion Validation
```kotlin
data class ValidationRules(
    val msisdnPattern: Regex = "^\\d{1,15}$".toRegex(),
    val imsiPattern: Regex = "^\\d{15}$".toRegex(),
    val imeiPattern: Regex = "^\\d{15}$".toRegex(),
    val maxCallDuration: Int = 86400, // 24 hours in seconds
    val validCallTypes: Set<String> = setOf("voice", "sms", "data"),
    val validDirections: Set<String> = setOf("mo", "mt"),
    val signalStrengthRange: IntRange = -128..0
)
```

#### Schema Validation
```kotlin
interface SchemaValidator {
    fun validateRecord(record: ZteCdrRecord): ValidationResult
    fun validateFieldTypes(record: ZteCdrRecord): TypeValidationResult
    fun validateConstraints(record: ZteCdrRecord): ConstraintValidationResult
}
```

### Performance Characteristics

#### Expected Storage
- **Average Record Size**: ~500 bytes (without raw_record field)
- **Monthly Data Volume**: ~50GB for 100M records
- **Compression Ratio**: 3:1 with LZ4 compression
- **Index Overhead**: ~5% of total storage

#### Query Performance
- **Point Lookups**: <10ms for MSISDN/IMSI searches
- **Range Queries**: <100ms for daily aggregations
- **Analytics Queries**: <1s for monthly summaries
- **Partition Pruning**: 90%+ reduction in scanned data for time-based queries