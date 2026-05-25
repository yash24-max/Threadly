# Threadly Database Schemas

**Version:** 1.0  
**Database:** PostgreSQL 16  
**Last Updated:** May 25, 2026

---

## Overview

Threadly uses 9 PostgreSQL databases (one per microservice) for complete data isolation and independent scaling.

```
identity_db       (users, orgs, auth)
workspace_db      (bots, settings)
flow_db           (flows, definitions)
runtime_db        (sessions, logs)
conversation_db   (messages, leads)
knowledge_db      (documents, chunks)
analytics_db      (events, metrics)
billing_db        (plans, invoices)
integration_db    (integrations, creds)
```

---

## identity_db - Authentication & Organizations

### users table

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  email_verified BOOLEAN DEFAULT false,
  email_verified_at TIMESTAMP,
  status VARCHAR(50) DEFAULT 'active', -- active, suspended, deleted
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  
  INDEX idx_email (email),
  INDEX idx_created_at (created_at)
);
```

### organizations table

```sql
CREATE TABLE organizations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  logo_url VARCHAR(500),
  plan VARCHAR(50) DEFAULT 'free', -- free, professional, business, enterprise
  status VARCHAR(50) DEFAULT 'active',
  owner_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_owner_id (owner_id),
  INDEX idx_plan (plan)
);
```

### memberships table

```sql
CREATE TABLE memberships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  org_id UUID NOT NULL REFERENCES organizations(id),
  role VARCHAR(50) DEFAULT 'user', -- admin, editor, viewer
  invited_at TIMESTAMP,
  accepted_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(user_id, org_id),
  INDEX idx_user_id (user_id),
  INDEX idx_org_id (org_id)
);
```

### api_keys table

```sql
CREATE TABLE api_keys (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL REFERENCES organizations(id),
  key_hash VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(100),
  scopes VARCHAR(500), -- comma-separated
  last_used_at TIMESTAMP,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_org_id (org_id),
  INDEX idx_expires_at (expires_at)
);
```

### refresh_tokens table

```sql
CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  revoked BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_user_id (user_id),
  INDEX idx_expires_at (expires_at)
);
```

---

## workspace_db - Bots & Settings

### bots table

```sql
CREATE TABLE bots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(50) DEFAULT 'draft', -- draft, published, archived
  template_id VARCHAR(100),
  avatar_url VARCHAR(500),
  greeting_message TEXT,
  created_by UUID NOT NULL,
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_org_id (org_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
```

### bot_settings table

```sql
CREATE TABLE bot_settings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  llm_provider VARCHAR(50) DEFAULT 'anthropic',
  llm_model VARCHAR(100),
  temperature DECIMAL(3,2) DEFAULT 0.7,
  max_tokens INTEGER DEFAULT 1000,
  system_prompt TEXT,
  use_kb BOOLEAN DEFAULT true,
  enable_handoff BOOLEAN DEFAULT true,
  enable_feedback BOOLEAN DEFAULT true,
  custom_css TEXT,
  widget_color VARCHAR(7),
  widget_position VARCHAR(20) DEFAULT 'bottom-right',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(bot_id)
);
```

### api_keys_workspace table

```sql
CREATE TABLE api_keys_workspace (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL REFERENCES bots(id),
  name VARCHAR(100),
  key_hash VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id)
);
```

---

## flow_db - Flow Definitions

### flows table

```sql
CREATE TABLE flows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  current_version_id UUID,
  status VARCHAR(50) DEFAULT 'draft',
  created_by UUID NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id)
);
```

### flow_versions table

```sql
CREATE TABLE flow_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  flow_id UUID NOT NULL REFERENCES flows(id) ON DELETE CASCADE,
  version_number INTEGER NOT NULL,
  definition JSONB NOT NULL, -- nodes, edges, metadata
  created_by UUID NOT NULL,
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(flow_id, version_number),
  INDEX idx_flow_id (flow_id)
);
```

### flow_nodes table

```sql
CREATE TABLE flow_nodes (
  id VARCHAR(100) PRIMARY KEY,
  flow_id UUID NOT NULL,
  node_type VARCHAR(50) NOT NULL, -- start, message, ai_reply, etc.
  config JSONB, -- node-specific configuration
  position_x INTEGER,
  position_y INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_flow_id (flow_id)
);
```

### flow_edges table

```sql
CREATE TABLE flow_edges (
  id VARCHAR(100) PRIMARY KEY,
  flow_id UUID NOT NULL,
  from_node_id VARCHAR(100) NOT NULL,
  to_node_id VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_flow_id (flow_id)
);
```

---

## runtime_db - Execution Sessions

### sessions table

```sql
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  visitor_id UUID NOT NULL,
  flow_id UUID NOT NULL,
  current_node_id VARCHAR(100),
  status VARCHAR(50) DEFAULT 'active', -- active, completed, failed
  started_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_visitor_id (visitor_id),
  INDEX idx_status (status)
);
```

### session_variables table

```sql
CREATE TABLE session_variables (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  key VARCHAR(100) NOT NULL,
  value TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_session_id (session_id)
);
```

### execution_logs table

```sql
CREATE TABLE execution_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID NOT NULL,
  node_id VARCHAR(100),
  status VARCHAR(50),
  input JSONB,
  output JSONB,
  error_message TEXT,
  duration_ms INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_session_id (session_id),
  INDEX idx_created_at (created_at)
);
```

---

## conversation_db - Messages & Leads

### conversations table

```sql
CREATE TABLE conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  visitor_id UUID NOT NULL,
  visitor_email VARCHAR(255),
  visitor_name VARCHAR(255),
  visitor_phone VARCHAR(20),
  message_count INTEGER DEFAULT 0,
  status VARCHAR(50) DEFAULT 'active', -- active, closed, waiting_for_human
  assigned_to UUID,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_visitor_email (visitor_email),
  INDEX idx_status (status)
);
```

### messages table

```sql
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender VARCHAR(50) NOT NULL, -- visitor, bot, human
  content TEXT NOT NULL,
  metadata JSONB, -- tokens, cost, model, etc.
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_conversation_id (conversation_id),
  INDEX idx_created_at (created_at)
);
```

### leads table

```sql
CREATE TABLE leads (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  conversation_id UUID REFERENCES conversations(id),
  email VARCHAR(255),
  name VARCHAR(255),
  phone VARCHAR(20),
  metadata JSONB,
  status VARCHAR(50) DEFAULT 'new', -- new, contacted, converted, lost
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_email (email)
);
```

---

## knowledge_db - Documents & Embeddings

### kb_documents table

```sql
CREATE TABLE kb_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  filename VARCHAR(255) NOT NULL,
  file_type VARCHAR(20), -- pdf, docx, txt, html
  file_size_bytes INTEGER,
  status VARCHAR(50) DEFAULT 'ingesting', -- ingesting, ingested, failed
  chunks_count INTEGER,
  tokens_count INTEGER,
  error_message TEXT,
  created_by UUID NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_status (status)
);
```

### kb_chunks table

```sql
CREATE TABLE kb_chunks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  document_id UUID NOT NULL REFERENCES kb_documents(id),
  chunk_index INTEGER NOT NULL,
  content TEXT NOT NULL,
  tokens_count INTEGER,
  metadata JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_document_id (document_id)
);
```

### kb_embeddings table

```sql
CREATE TABLE kb_embeddings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chunk_id UUID NOT NULL REFERENCES kb_chunks(id),
  qdrant_id VARCHAR(100), -- Reference to Qdrant collection
  model VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(chunk_id),
  INDEX idx_qdrant_id (qdrant_id)
);
```

---

## analytics_db - Events & Metrics

### events table

```sql
CREATE TABLE events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL,
  bot_id UUID NOT NULL,
  visitor_id UUID,
  event_type VARCHAR(50),
  event_data JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_event_type (event_type),
  INDEX idx_created_at (created_at)
);
```

### metrics table

```sql
CREATE TABLE metrics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  metric_date DATE NOT NULL,
  total_conversations INTEGER DEFAULT 0,
  active_conversations INTEGER DEFAULT 0,
  total_messages INTEGER DEFAULT 0,
  unique_visitors INTEGER DEFAULT 0,
  avg_message_length DECIMAL(10,2),
  avg_response_time_ms INTEGER,
  fallback_count INTEGER DEFAULT 0,
  handoff_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(bot_id, metric_date),
  INDEX idx_bot_id (bot_id)
);
```

---

## billing_db - Payments & Subscriptions

### plans table

```sql
CREATE TABLE plans (
  id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price_monthly DECIMAL(10,2),
  price_annual DECIMAL(10,2),
  max_bots INTEGER,
  max_conversations_monthly INTEGER,
  max_kb_documents INTEGER,
  features JSONB,
  status VARCHAR(50) DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_status (status)
);
```

### subscriptions table

```sql
CREATE TABLE subscriptions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL,
  plan_id VARCHAR(50) NOT NULL REFERENCES plans(id),
  stripe_subscription_id VARCHAR(100),
  status VARCHAR(50) DEFAULT 'active', -- active, canceled, past_due
  current_period_start DATE,
  current_period_end DATE,
  cancel_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_org_id (org_id),
  INDEX idx_status (status)
);
```

### invoices table

```sql
CREATE TABLE invoices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL,
  subscription_id UUID REFERENCES subscriptions(id),
  stripe_invoice_id VARCHAR(100),
  amount_cents INTEGER NOT NULL,
  status VARCHAR(50) DEFAULT 'draft', -- draft, sent, paid, void
  due_date DATE,
  paid_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_org_id (org_id),
  INDEX idx_status (status)
);
```

### usage_events table

```sql
CREATE TABLE usage_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID NOT NULL,
  bot_id UUID,
  metric_name VARCHAR(100),
  quantity INTEGER,
  cost_cents INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_org_id (org_id),
  INDEX idx_created_at (created_at)
);
```

---

## integration_db - External Integrations

### integrations table

```sql
CREATE TABLE integrations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id UUID NOT NULL,
  provider VARCHAR(50) NOT NULL, -- slack, zapier, make, etc.
  status VARCHAR(50) DEFAULT 'active',
  created_by UUID NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_bot_id (bot_id),
  INDEX idx_provider (provider)
);
```

### integration_configs table

```sql
CREATE TABLE integration_configs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  integration_id UUID NOT NULL REFERENCES integrations(id),
  config_key VARCHAR(100) NOT NULL,
  config_value_encrypted TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_integration_id (integration_id)
);
```

### oauth_tokens table

```sql
CREATE TABLE oauth_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  integration_id UUID NOT NULL,
  access_token_encrypted TEXT NOT NULL,
  refresh_token_encrypted TEXT,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_integration_id (integration_id),
  INDEX idx_expires_at (expires_at)
);
```

---

## Global Indexes

All tables have indexes on commonly queried columns:

- org_id (multi-tenant isolation)
- created_at (time-series queries)
- status (filtering)
- Foreign keys (relationships)

---

## Data Retention

- Conversations: 90 days (configurable)
- Events: 365 days
- Execution logs: 30 days
- API keys: Until revoked or expired
- Refresh tokens: Until expired

---

## Backup & Recovery

- Daily automated snapshots
- 30-day retention
- Point-in-time recovery
- Tested restore procedures

---

**Last Updated:** May 25, 2026
