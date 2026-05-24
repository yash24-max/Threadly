-- Flow Service Schema Initialization
-- Manages flow definitions, executions, and node configurations

CREATE SCHEMA IF NOT EXISTS flow_service;

-- Organizations table (reference)
CREATE TABLE IF NOT EXISTS flow_service.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- Flows table - core entity managed by flow-service
CREATE TABLE IF NOT EXISTS flow_service.flows (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES flow_service.organizations(id),
    bot_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    definition JSONB NOT NULL,
    version INT DEFAULT 1,
    status VARCHAR(50) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'archived')),
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT flow_unique_per_bot UNIQUE (bot_id, name),
    CONSTRAINT flow_not_deleted CHECK (deleted_at IS NULL)
);

CREATE INDEX IF NOT EXISTS idx_flows_org_id ON flow_service.flows(org_id);
CREATE INDEX IF NOT EXISTS idx_flows_bot_id ON flow_service.flows(bot_id);
CREATE INDEX IF NOT EXISTS idx_flows_status ON flow_service.flows(status);
CREATE INDEX IF NOT EXISTS idx_flows_created_by ON flow_service.flows(created_by);

-- Flow versions table for audit trail
CREATE TABLE IF NOT EXISTS flow_service.flow_versions (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    definition JSONB NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT flow_version_unique UNIQUE (flow_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_flow_versions_flow_id ON flow_service.flow_versions(flow_id);

-- Flow nodes (individual nodes within a flow)
CREATE TABLE IF NOT EXISTS flow_service.flow_nodes (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id) ON DELETE CASCADE,
    node_type VARCHAR(100) NOT NULL,
    node_label VARCHAR(255) NOT NULL,
    configuration JSONB NOT NULL,
    position_x INT,
    position_y INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_flow_nodes_flow_id ON flow_service.flow_nodes(flow_id);
CREATE INDEX IF NOT EXISTS idx_flow_nodes_type ON flow_service.flow_nodes(node_type);

-- Flow edges (connections between nodes)
CREATE TABLE IF NOT EXISTS flow_service.flow_edges (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id) ON DELETE CASCADE,
    source_node_id UUID NOT NULL REFERENCES flow_service.flow_nodes(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES flow_service.flow_nodes(id) ON DELETE CASCADE,
    label VARCHAR(255),
    CONSTRAINT flow_edge_unique UNIQUE (source_node_id, target_node_id)
);

CREATE INDEX IF NOT EXISTS idx_flow_edges_flow_id ON flow_service.flow_edges(flow_id);
CREATE INDEX IF NOT EXISTS idx_flow_edges_source ON flow_service.flow_edges(source_node_id);
CREATE INDEX IF NOT EXISTS idx_flow_edges_target ON flow_service.flow_edges(target_node_id);

-- Flow executions/runs
CREATE TABLE IF NOT EXISTS flow_service.flow_executions (
    id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES flow_service.flows(id),
    org_id UUID NOT NULL REFERENCES flow_service.organizations(id),
    bot_id UUID NOT NULL,
    session_id UUID,
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'running', 'completed', 'failed', 'paused')),
    input_data JSONB,
    output_data JSONB,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_flow_executions_flow_id ON flow_service.flow_executions(flow_id);
CREATE INDEX IF NOT EXISTS idx_flow_executions_org_id ON flow_service.flow_executions(org_id);
CREATE INDEX IF NOT EXISTS idx_flow_executions_status ON flow_service.flow_executions(status);
CREATE INDEX IF NOT EXISTS idx_flow_executions_session_id ON flow_service.flow_executions(session_id);

-- Flow execution steps (audit trail of each node execution)
CREATE TABLE IF NOT EXISTS flow_service.flow_execution_steps (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL REFERENCES flow_service.flow_executions(id) ON DELETE CASCADE,
    node_id UUID NOT NULL REFERENCES flow_service.flow_nodes(id),
    step_index INT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'running', 'completed', 'failed', 'skipped')),
    input_data JSONB,
    output_data JSONB,
    error_message TEXT,
    duration_ms BIGINT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_execution_steps_execution_id ON flow_service.flow_execution_steps(execution_id);
CREATE INDEX IF NOT EXISTS idx_execution_steps_node_id ON flow_service.flow_execution_steps(node_id);

-- Flow templates
CREATE TABLE IF NOT EXISTS flow_service.flow_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    definition JSONB NOT NULL,
    category VARCHAR(100),
    is_public BOOLEAN DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_flow_templates_category ON flow_service.flow_templates(category);
CREATE INDEX IF NOT EXISTS idx_flow_templates_public ON flow_service.flow_templates(is_public);
