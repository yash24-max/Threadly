-- V10__service_isolation.sql
-- Atomic migration to create all 9 microservice schemas
-- Each service maintains its own database schema for full isolation

-- Identity Service Schema
CREATE SCHEMA IF NOT EXISTS identity_service;

COMMENT ON SCHEMA identity_service IS 'Identity Service - Users, organizations, authentication, API keys';

CREATE TABLE IF NOT EXISTS identity_service.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    oauth_provider VARCHAR(50),
    oauth_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS identity_service.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    owner_id UUID NOT NULL REFERENCES identity_service.users(id),
    subscription_plan VARCHAR(50) DEFAULT 'free',
    billing_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS identity_service.memberships (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES identity_service.organizations(id),
    user_id UUID NOT NULL REFERENCES identity_service.users(id),
    role VARCHAR(50) DEFAULT 'member',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(org_id, user_id)
);

CREATE TABLE IF NOT EXISTS identity_service.api_keys (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES identity_service.organizations(id),
    name VARCHAR(255) NOT NULL,
    key_hash VARCHAR(255) UNIQUE NOT NULL,
    scopes TEXT[] DEFAULT '{}',
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_identity_users_email ON identity_service.users(email);
CREATE INDEX IF NOT EXISTS idx_identity_organizations_owner ON identity_service.organizations(owner_id);
CREATE INDEX IF NOT EXISTS idx_identity_memberships_org ON identity_service.memberships(org_id);
CREATE INDEX IF NOT EXISTS idx_identity_memberships_user ON identity_service.memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_identity_api_keys_org ON identity_service.api_keys(org_id);

-- Workspace Service Schema
CREATE SCHEMA IF NOT EXISTS workspace_service;

COMMENT ON SCHEMA workspace_service IS 'Workspace Service - Bots, team management, workspace settings';

CREATE TABLE IF NOT EXISTS workspace_service.bots (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(10) DEFAULT 'en',
    accent_color VARCHAR(7) DEFAULT '#4F46E5',
    avatar_url VARCHAR(500),
    welcome_message TEXT,
    kb_search_enabled BOOLEAN DEFAULT TRUE,
    integrations JSONB DEFAULT '{}',
    webhook_signing_key VARCHAR(255),
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspace_service.workspace_settings (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL UNIQUE,
    custom_domain VARCHAR(255),
    custom_branding JSONB,
    sso_config JSONB,
    rate_limits JSONB DEFAULT '{"conversations_per_day": 100000, "api_calls_per_min": 1000}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspace_service.bot_metrics_snapshot (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL REFERENCES workspace_service.bots(id),
    date DATE NOT NULL,
    conversation_count INT DEFAULT 0,
    unique_visitors INT DEFAULT 0,
    api_calls INT DEFAULT 0,
    kb_queries INT DEFAULT 0,
    avg_response_time_ms FLOAT
);

CREATE INDEX IF NOT EXISTS idx_workspace_bots_org ON workspace_service.bots(org_id);
CREATE INDEX IF NOT EXISTS idx_workspace_bots_status ON workspace_service.bots(status);
CREATE INDEX IF NOT EXISTS idx_workspace_metrics ON workspace_service.bot_metrics_snapshot(bot_id, date);

-- Flow Service Schema
CREATE SCHEMA IF NOT EXISTS flow_service;

COMMENT ON SCHEMA flow_service IS 'Flow Service - Bot flow definitions, versioning, publishing';

CREATE TABLE IF NOT EXISTS flow_service.flows (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL,
    org_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    flow_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flow_service.flow_versions (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id),
    version_number INT NOT NULL,
    flow_json JSONB NOT NULL,
    published_by_id UUID,
    published_at TIMESTAMP,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flow_service.flow_triggers (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id),
    trigger_type VARCHAR(50),
    trigger_config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flow_service.flow_templates (
    id UUID PRIMARY KEY,
    org_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    flow_json JSONB NOT NULL,
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_flow_flows_org_bot ON flow_service.flows(org_id, bot_id);
CREATE INDEX IF NOT EXISTS idx_flow_versions ON flow_service.flow_versions(flow_id, version_number);
CREATE INDEX IF NOT EXISTS idx_flow_triggers ON flow_service.flow_triggers(flow_id, trigger_type);
CREATE INDEX IF NOT EXISTS idx_flow_templates_category ON flow_service.flow_templates(category);

-- Runtime Service Schema
CREATE SCHEMA IF NOT EXISTS runtime_service;

COMMENT ON SCHEMA runtime_service IS 'Runtime Service - Flow execution, session state, node execution';

CREATE TABLE IF NOT EXISTS runtime_service.sessions (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL,
    org_id UUID NOT NULL,
    visitor_id VARCHAR(255) NOT NULL,
    flow_id UUID NOT NULL,
    current_node_id VARCHAR(255),
    variables JSONB DEFAULT '{}',
    status VARCHAR(50) DEFAULT 'active',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS runtime_service.session_snapshots (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES runtime_service.sessions(id),
    node_id VARCHAR(255) NOT NULL,
    variables JSONB,
    execution_context JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS runtime_service.node_executions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES runtime_service.sessions(id),
    node_id VARCHAR(255) NOT NULL,
    node_type VARCHAR(50),
    execution_input JSONB,
    execution_output JSONB,
    status VARCHAR(50),
    error_message TEXT,
    duration_ms INT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_runtime_sessions_org_bot ON runtime_service.sessions(org_id, bot_id, visitor_id);
CREATE INDEX IF NOT EXISTS idx_runtime_sessions_status ON runtime_service.sessions(status);
CREATE INDEX IF NOT EXISTS idx_runtime_snapshots ON runtime_service.session_snapshots(session_id);
CREATE INDEX IF NOT EXISTS idx_runtime_executions ON runtime_service.node_executions(session_id, node_id);

-- Conversation Service Schema
CREATE SCHEMA IF NOT EXISTS conversation_service;

COMMENT ON SCHEMA conversation_service IS 'Conversation Service - Transcripts, messages, leads, handoffs';

CREATE TABLE IF NOT EXISTS conversation_service.conversations (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL,
    org_id UUID NOT NULL,
    visitor_id VARCHAR(255) NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'open',
    assigned_agent_id UUID,
    is_lead BOOLEAN DEFAULT FALSE,
    lead_id UUID
);

CREATE TABLE IF NOT EXISTS conversation_service.messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversation_service.conversations(id),
    sender_type VARCHAR(50),
    sender_id VARCHAR(255),
    sender_name VARCHAR(255),
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'text',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversation_service.leads (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    bot_id UUID NOT NULL,
    conversation_id UUID REFERENCES conversation_service.conversations(id),
    email VARCHAR(255),
    phone VARCHAR(20),
    name VARCHAR(255),
    custom_fields JSONB,
    tags TEXT[] DEFAULT '{}',
    status VARCHAR(50) DEFAULT 'new',
    pipeline_stage VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversation_service.lead_activities (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES conversation_service.leads(id),
    activity_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_id UUID
);

CREATE INDEX IF NOT EXISTS idx_conversation_conversations_org ON conversation_service.conversations(org_id, bot_id);
CREATE INDEX IF NOT EXISTS idx_conversation_conversations_status ON conversation_service.conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversation_conversations_visitor ON conversation_service.conversations(visitor_id);
CREATE INDEX IF NOT EXISTS idx_conversation_messages ON conversation_service.messages(conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_conversation_leads_org ON conversation_service.leads(org_id, email);
CREATE INDEX IF NOT EXISTS idx_conversation_leads_status ON conversation_service.leads(status);
CREATE INDEX IF NOT EXISTS idx_conversation_activities ON conversation_service.lead_activities(lead_id, created_at);

-- Knowledge Service Schema
CREATE SCHEMA IF NOT EXISTS knowledge_service;

COMMENT ON SCHEMA knowledge_service IS 'Knowledge Service - Documents, chunks, embeddings, RAG';

CREATE TABLE IF NOT EXISTS knowledge_service.kb_documents (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL,
    org_id UUID NOT NULL,
    document_name VARCHAR(500) NOT NULL,
    document_type VARCHAR(50),
    source_url VARCHAR(1000),
    file_path VARCHAR(1000),
    file_size_bytes INT,
    page_count INT,
    status VARCHAR(50) DEFAULT 'pending',
    error_message TEXT,
    chunk_count INT DEFAULT 0,
    indexed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_service.kb_chunks (
    id UUID PRIMARY KEY,
    kb_document_id UUID NOT NULL REFERENCES knowledge_service.kb_documents(id),
    chunk_number INT NOT NULL,
    content TEXT NOT NULL,
    token_count INT,
    metadata JSONB,
    qdrant_point_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_service.kb_ingestion_jobs (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    kb_document_id UUID REFERENCES knowledge_service.kb_documents(id),
    job_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'pending',
    progress_percent INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_documents_org ON knowledge_service.kb_documents(org_id, bot_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_status ON knowledge_service.kb_documents(status);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks ON knowledge_service.kb_chunks(kb_document_id, chunk_number);
CREATE INDEX IF NOT EXISTS idx_knowledge_jobs ON knowledge_service.kb_ingestion_jobs(org_id, status);

-- Analytics Service Schema
CREATE SCHEMA IF NOT EXISTS analytics_service;

COMMENT ON SCHEMA analytics_service IS 'Analytics Service - Event aggregation, metrics, reporting';

CREATE TABLE IF NOT EXISTS analytics_service.analytics_events (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    bot_id UUID,
    event_type VARCHAR(100),
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analytics_service.daily_metrics (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    bot_id UUID NOT NULL,
    date DATE NOT NULL,
    conversation_count INT DEFAULT 0,
    unique_visitors INT DEFAULT 0,
    message_count INT DEFAULT 0,
    api_calls INT DEFAULT 0,
    kb_queries INT DEFAULT 0,
    avg_response_time_ms FLOAT,
    handoff_count INT DEFAULT 0,
    lead_count INT DEFAULT 0,
    cost_usd FLOAT DEFAULT 0,
    satisfaction_score FLOAT
);

CREATE TABLE IF NOT EXISTS analytics_service.conversation_costs (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    bot_id UUID NOT NULL,
    org_id UUID NOT NULL,
    llm_tokens INT DEFAULT 0,
    llm_cost_usd FLOAT DEFAULT 0,
    embedding_calls INT DEFAULT 0,
    embedding_cost_usd FLOAT DEFAULT 0,
    total_cost_usd FLOAT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analytics_events ON analytics_service.analytics_events(org_id, bot_id, created_at);
CREATE INDEX IF NOT EXISTS idx_analytics_daily_metrics ON analytics_service.daily_metrics(org_id, bot_id, date);
CREATE INDEX IF NOT EXISTS idx_analytics_costs ON analytics_service.conversation_costs(org_id, bot_id, created_at);

-- Billing Service Schema
CREATE SCHEMA IF NOT EXISTS billing_service;

COMMENT ON SCHEMA billing_service IS 'Billing Service - Subscriptions, payments, invoices';

CREATE TABLE IF NOT EXISTS billing_service.plans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price_usd FLOAT DEFAULT 0,
    billing_period VARCHAR(50) DEFAULT 'monthly',
    features JSONB,
    stripe_product_id VARCHAR(255),
    stripe_price_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_service.subscriptions (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL UNIQUE,
    plan_id UUID NOT NULL REFERENCES billing_service.plans(id),
    stripe_subscription_id VARCHAR(255),
    stripe_customer_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'active',
    current_period_start DATE,
    current_period_end DATE,
    trial_end DATE,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    auto_renew BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_service.usage_records (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES billing_service.subscriptions(id),
    org_id UUID NOT NULL,
    metric_name VARCHAR(100),
    value FLOAT NOT NULL,
    period_start DATE,
    period_end DATE,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_service.invoices (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES billing_service.subscriptions(id),
    stripe_invoice_id VARCHAR(255),
    amount_usd FLOAT,
    status VARCHAR(50),
    issued_at TIMESTAMP,
    due_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_billing_subscriptions_org ON billing_service.subscriptions(org_id);
CREATE INDEX IF NOT EXISTS idx_billing_usage ON billing_service.usage_records(org_id, metric_name, recorded_at);
CREATE INDEX IF NOT EXISTS idx_billing_invoices ON billing_service.invoices(subscription_id);

-- Integration Service Schema
CREATE SCHEMA IF NOT EXISTS integration_service;

COMMENT ON SCHEMA integration_service IS 'Integration Service - Connectors, OAuth, credentials, actions';

CREATE TABLE IF NOT EXISTS integration_service.integrations (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    display_config JSONB,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS integration_service.integration_credentials (
    id UUID PRIMARY KEY,
    integration_id UUID NOT NULL REFERENCES integration_service.integrations(id),
    credential_type VARCHAR(50),
    encrypted_value TEXT NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS integration_service.integration_actions_log (
    id UUID PRIMARY KEY,
    integration_id UUID NOT NULL REFERENCES integration_service.integrations(id),
    action_name VARCHAR(100),
    action_params JSONB,
    result JSONB,
    status VARCHAR(50),
    error_message TEXT,
    duration_ms INT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS integration_service.integration_oauth_state (
    id UUID PRIMARY KEY,
    state_token VARCHAR(255) UNIQUE NOT NULL,
    org_id UUID NOT NULL,
    integration_type VARCHAR(100) NOT NULL,
    return_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP + INTERVAL '10 minutes'
);

CREATE INDEX IF NOT EXISTS idx_integration_integrations_org ON integration_service.integrations(org_id, type);
CREATE INDEX IF NOT EXISTS idx_integration_credentials ON integration_service.integration_credentials(integration_id);
CREATE INDEX IF NOT EXISTS idx_integration_actions ON integration_service.integration_actions_log(integration_id, executed_at);

-- Grant schema access to application user
GRANT USAGE ON SCHEMA identity_service, workspace_service, flow_service, runtime_service,
              conversation_service, knowledge_service, analytics_service, billing_service,
              integration_service TO threadly;

GRANT CREATE ON SCHEMA identity_service, workspace_service, flow_service, runtime_service,
              conversation_service, knowledge_service, analytics_service, billing_service,
              integration_service TO threadly;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA identity_service, workspace_service, flow_service,
                                       runtime_service, conversation_service, knowledge_service,
                                       analytics_service, billing_service, integration_service
TO threadly;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA identity_service, workspace_service, flow_service,
                                         runtime_service, conversation_service, knowledge_service,
                                         analytics_service, billing_service, integration_service
TO threadly;
