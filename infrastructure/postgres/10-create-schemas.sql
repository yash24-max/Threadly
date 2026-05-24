-- V10: Create microservices database schemas
-- Each service has its own isolated schema for database-per-service pattern

-- Create all service schemas
CREATE SCHEMA IF NOT EXISTS identity_service;
CREATE SCHEMA IF NOT EXISTS workspace_service;
CREATE SCHEMA IF NOT EXISTS flow_service;
CREATE SCHEMA IF NOT EXISTS runtime_service;
CREATE SCHEMA IF NOT EXISTS conversation_service;
CREATE SCHEMA IF NOT EXISTS knowledge_service;
CREATE SCHEMA IF NOT EXISTS analytics_service;
CREATE SCHEMA IF NOT EXISTS billing_service;
CREATE SCHEMA IF NOT EXISTS integration_service;

-- Grant all privileges to threadly user on all schemas
GRANT ALL PRIVILEGES ON SCHEMA identity_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA workspace_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA flow_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA runtime_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA conversation_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA knowledge_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA analytics_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA billing_service TO threadly;
GRANT ALL PRIVILEGES ON SCHEMA integration_service TO threadly;

-- Set default privileges for future tables/sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA identity_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA workspace_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA flow_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA runtime_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA conversation_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA knowledge_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA billing_service GRANT ALL ON TABLES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA integration_service GRANT ALL ON TABLES TO threadly;

ALTER DEFAULT PRIVILEGES IN SCHEMA identity_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA workspace_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA flow_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA runtime_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA conversation_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA knowledge_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA billing_service GRANT ALL ON SEQUENCES TO threadly;
ALTER DEFAULT PRIVILEGES IN SCHEMA integration_service GRANT ALL ON SEQUENCES TO threadly;

-- Verify schema creation
SELECT schema_name FROM information_schema.schemata
WHERE schema_name IN (
  'identity_service',
  'workspace_service',
  'flow_service',
  'runtime_service',
  'conversation_service',
  'knowledge_service',
  'analytics_service',
  'billing_service',
  'integration_service'
) ORDER BY schema_name;
