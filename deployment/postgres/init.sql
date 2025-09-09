-- Create TIA ETL database and user
CREATE USER tia_user WITH PASSWORD 'tia_password';
CREATE DATABASE tia_etl WITH OWNER = tia_user ENCODING = 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE tia_etl TO tia_user;

-- Create Keycloak database and user
CREATE USER keycloak WITH PASSWORD 'keycloak_password';
CREATE DATABASE keycloak WITH OWNER = keycloak ENCODING = 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to tia_etl database
\c tia_etl;

-- Grant schema permissions to tia_user
GRANT ALL ON SCHEMA public TO tia_user;

-- Create initial schema for ETL system
CREATE SCHEMA IF NOT EXISTS etl AUTHORIZATION tia_user;

-- -- Create job_configurations table
-- CREATE TABLE IF NOT EXISTS etl.job_configurations (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     name VARCHAR(255) NOT NULL UNIQUE,
--     source_directory VARCHAR(500) NOT NULL,
--     file_pattern VARCHAR(255) NOT NULL,
--     scan_interval_type VARCHAR(50) NOT NULL, -- 'CRON' or 'FIXED'
--     scan_interval_value VARCHAR(255) NOT NULL,
--     parser_id VARCHAR(100) NOT NULL,
--     after_processing VARCHAR(50) NOT NULL, -- 'DELETE' or 'MOVE_TO_DONE'
--     is_active BOOLEAN DEFAULT true,
--     created_by VARCHAR(100) NOT NULL,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     metadata JSONB
-- );
--
-- -- Create file_processing_history table
-- CREATE TABLE IF NOT EXISTS etl.file_processing_history (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     job_id UUID REFERENCES etl.job_configurations(id),
--     file_name VARCHAR(500) NOT NULL,
--     file_path VARCHAR(1000) NOT NULL,
--     file_size BIGINT,
--     status VARCHAR(50) NOT NULL, -- 'QUEUED', 'PROCESSING', 'SUCCESS', 'FAILED'
--     parser_id VARCHAR(100),
--     total_records BIGINT,
--     successful_records BIGINT,
--     failed_records BIGINT,
--     processing_time_ms BIGINT,
--     error_message TEXT,
--     started_at TIMESTAMP WITH TIME ZONE,
--     completed_at TIMESTAMP WITH TIME ZONE,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );
--
-- -- Create audit_logs table for critical logs
-- CREATE TABLE IF NOT EXISTS etl.audit_logs (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     event_type VARCHAR(100) NOT NULL,
--     event_source VARCHAR(100) NOT NULL,
--     user_id VARCHAR(100),
--     description TEXT,
--     metadata JSONB,
--     severity VARCHAR(20), -- 'INFO', 'WARNING', 'ERROR', 'CRITICAL'
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );
--
-- -- Create indexes
-- CREATE INDEX idx_job_configurations_active ON etl.job_configurations(is_active);
-- CREATE INDEX idx_file_processing_history_job_id ON etl.file_processing_history(job_id);
-- CREATE INDEX idx_file_processing_history_status ON etl.file_processing_history(status);
-- CREATE INDEX idx_file_processing_history_created_at ON etl.file_processing_history(created_at);
-- CREATE INDEX idx_audit_logs_severity ON etl.audit_logs(severity);
-- CREATE INDEX idx_audit_logs_created_at ON etl.audit_logs(created_at);

-- Grant permissions on etl schema
GRANT ALL ON SCHEMA etl TO tia_user;
GRANT ALL ON ALL TABLES IN SCHEMA etl TO tia_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA etl TO tia_user;

-- Set default search path for tia_user
ALTER USER tia_user SET search_path TO etl, public;
