-- Create test database schema
CREATE SCHEMA IF NOT EXISTS public;

-- Grant permissions
GRANT ALL ON SCHEMA public TO test_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO test_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO test_user;

-- Set search path
SET search_path TO public;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";