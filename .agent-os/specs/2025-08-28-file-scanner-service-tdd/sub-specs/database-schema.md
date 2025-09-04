# Database Schema

This is the database schema implementation for the spec detailed in @.agent-os/specs/2025-08-28-file-scanner-service-tdd/spec.md

## New Tables

### scan_jobs

```sql
CREATE TABLE scan_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    source_directory VARCHAR(1024) NOT NULL,
    file_pattern VARCHAR(255) NOT NULL,
    scan_interval_type VARCHAR(20) NOT NULL CHECK (scan_interval_type IN ('CRON', 'FIXED')),
    scan_interval_value VARCHAR(100) NOT NULL,
    max_file_size_mb INTEGER DEFAULT 1024,
    recursive_scan BOOLEAN DEFAULT true,
    max_depth INTEGER DEFAULT 10,
    priority INTEGER DEFAULT 0,
    parser_id VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_scan_jobs_active ON scan_jobs(is_active);
CREATE INDEX idx_scan_jobs_parser ON scan_jobs(parser_id);
```

### scanned_files

```sql
CREATE TABLE scanned_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES scan_jobs(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    file_name VARCHAR(512) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    file_modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DISCOVERED', 'QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED', 'SKIPPED')),
    queue_id VARCHAR(255),
    processing_started_at TIMESTAMP WITH TIME ZONE,
    processing_completed_at TIMESTAMP WITH TIME ZONE,
    processing_instance_id VARCHAR(100),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    discovered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(file_path, file_hash)
);

CREATE INDEX idx_scanned_files_job ON scanned_files(job_id);
CREATE INDEX idx_scanned_files_status ON scanned_files(status);
CREATE INDEX idx_scanned_files_discovered ON scanned_files(discovered_at);
CREATE INDEX idx_scanned_files_hash ON scanned_files(file_hash);
```

### scan_job_executions

```sql
CREATE TABLE scan_job_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES scan_jobs(id) ON DELETE CASCADE,
    instance_id VARCHAR(100) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED')),
    files_discovered INTEGER DEFAULT 0,
    files_queued INTEGER DEFAULT 0,
    files_skipped INTEGER DEFAULT 0,
    duration_ms BIGINT,
    error_message TEXT
);

CREATE INDEX idx_executions_job ON scan_job_executions(job_id);
CREATE INDEX idx_executions_started ON scan_job_executions(started_at);
```

## Test Data Seeds

### Test Fixtures for Database Tests

```sql
-- Test data for integration tests
INSERT INTO scan_jobs (id, name, source_directory, file_pattern, scan_interval_type, scan_interval_value, parser_id)
VALUES 
    ('123e4567-e89b-12d3-a456-426614174000', 'test-asn1-job', '/test/asn1', '*.asn1', 'CRON', '*/5 * * * *', 'zte-asn1-parser'),
    ('123e4567-e89b-12d3-a456-426614174001', 'test-csv-job', '/test/csv', '*.csv', 'FIXED', 'PT5M', 'csv-parser');

-- Sample scanned files for testing
INSERT INTO scanned_files (job_id, file_path, file_name, file_size_bytes, file_hash, file_modified_at, status)
VALUES 
    ('123e4567-e89b-12d3-a456-426614174000', '/test/asn1/sample1.asn1', 'sample1.asn1', 1024, 'abc123', NOW(), 'DISCOVERED'),
    ('123e4567-e89b-12d3-a456-426614174000', '/test/asn1/sample2.asn1', 'sample2.asn1', 2048, 'def456', NOW(), 'QUEUED');
```

## Migration Testing

```sql
-- Rollback script for testing
DROP TABLE IF EXISTS scan_job_executions CASCADE;
DROP TABLE IF EXISTS scanned_files CASCADE;
DROP TABLE IF EXISTS scan_jobs CASCADE;
```

## Performance Considerations

- **Partitioning**: Consider partitioning `scanned_files` by `discovered_at` for large datasets
- **Archival**: Implement archival strategy for old execution records
- **Vacuum**: Regular vacuum on high-churn tables
- **Statistics**: Update table statistics regularly for query optimization

## Data Integrity Tests

```kotlin
@Test
fun `should enforce unique job names`()
@Test
fun `should cascade delete executions when job deleted`()
@Test
fun `should prevent invalid status transitions`()
@Test
fun `should maintain referential integrity`()
```