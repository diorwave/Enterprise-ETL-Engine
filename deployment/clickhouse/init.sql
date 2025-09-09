-- Create database for TIA ETL
CREATE DATABASE IF NOT EXISTS tia_etl;

-- Use the database
USE tia_etl;

-- Example table for CDR (Call Detail Records)
-- This is a template, actual tables will be created by parsers
CREATE TABLE IF NOT EXISTS cdr_template
(
    record_id UUID DEFAULT generateUUIDv4(),
    call_id String,
    caller_number String,
    called_number String,
    call_start_time DateTime64(3),
    call_end_time DateTime64(3),
    duration_seconds UInt32,
    call_type Enum8('voice' = 1, 'sms' = 2, 'data' = 3),
    operator String,
    region String,
    tower_id String,
    signal_strength Float32,
    dropped_call Bool,
    data_volume_mb Float64,
    charge_amount Decimal(10, 2),
    currency String DEFAULT 'USD',
    processing_date Date DEFAULT today(),
    processing_timestamp DateTime64(3) DEFAULT now64(3),
    file_name String,
    job_id String
)
ENGINE = MergeTree()
PARTITION BY toYYYYMM(processing_date)
ORDER BY (processing_date, call_start_time, call_id)
TTL processing_date + INTERVAL 10 YEAR
SETTINGS index_granularity = 8192;

-- Create table for quality metrics
CREATE TABLE IF NOT EXISTS quality_metrics
(
    metric_id UUID DEFAULT generateUUIDv4(),
    timestamp DateTime64(3),
    operator String,
    region String,
    tower_id String,
    metric_type String,
    metric_value Float64,
    unit String,
    threshold_exceeded Bool,
    processing_date Date DEFAULT today(),
    file_name String,
    job_id String
)
ENGINE = MergeTree()
PARTITION BY toYYYYMM(processing_date)
ORDER BY (processing_date, timestamp, operator, region)
TTL processing_date + INTERVAL 10 YEAR
SETTINGS index_granularity = 8192;

-- Create materialized view for daily aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS daily_operator_stats
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (date, operator, call_type)
AS
SELECT
    toDate(call_start_time) as date,
    operator,
    call_type,
    count() as total_calls,
    sum(duration_seconds) as total_duration,
    sum(charge_amount) as total_revenue,
    countIf(dropped_call = 1) as dropped_calls
FROM cdr_template
GROUP BY date, operator, call_type;

-- Create dictionary for region mapping (example)
-- This would be populated from PostgreSQL
CREATE DICTIONARY IF NOT EXISTS regions_dict
(
    region_code String,
    region_name String,
    population UInt32
)
PRIMARY KEY region_code
SOURCE(CLICKHOUSE(
    HOST 'localhost'
    PORT 9000
    USER 'default'
    TABLE 'regions'
    DB 'tia_etl'
))
LIFETIME(MIN 300 MAX 360)
LAYOUT(HASHED());