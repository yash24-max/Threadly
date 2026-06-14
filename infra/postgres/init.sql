-- Create keycloak database for Keycloak's internal storage
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO threadly;

-- Create service schemas for microservices
\c threadly;
CREATE SCHEMA IF NOT EXISTS identity_service;
CREATE SCHEMA IF NOT EXISTS workspace_service;
CREATE SCHEMA IF NOT EXISTS flow_service;
CREATE SCHEMA IF NOT EXISTS runtime_service;
CREATE SCHEMA IF NOT EXISTS conversation_service;
CREATE SCHEMA IF NOT EXISTS knowledge_service;
CREATE SCHEMA IF NOT EXISTS analytics_service;
CREATE SCHEMA IF NOT EXISTS billing_service;
CREATE SCHEMA IF NOT EXISTS integration_service;
GRANT ALL ON SCHEMA identity_service TO threadly;
GRANT ALL ON SCHEMA workspace_service TO threadly;
GRANT ALL ON SCHEMA flow_service TO threadly;
GRANT ALL ON SCHEMA runtime_service TO threadly;
GRANT ALL ON SCHEMA conversation_service TO threadly;
GRANT ALL ON SCHEMA knowledge_service TO threadly;
GRANT ALL ON SCHEMA analytics_service TO threadly;
GRANT ALL ON SCHEMA billing_service TO threadly;
GRANT ALL ON SCHEMA integration_service TO threadly;
