-- Flow management schema
-- Supports versioning, validation, and audit trails

-- Main flow table
CREATE TABLE flow (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    org_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    current_version_id VARCHAR(36),
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flow_bot_id ON flow(bot_id);
CREATE INDEX idx_flow_org_id ON flow(org_id);
CREATE INDEX idx_flow_status ON flow(status);
CREATE UNIQUE INDEX idx_flow_org_bot ON flow(org_id, bot_id, id);

-- Flow version history
CREATE TABLE flow_version (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    version_number INTEGER NOT NULL,
    definition_json TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    published_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE,
    UNIQUE KEY uk_flow_version (flow_id, version_number)
);

CREATE INDEX idx_flow_version_flow_id ON flow_version(flow_id);
CREATE INDEX idx_flow_version_active ON flow_version(is_active);
CREATE INDEX idx_flow_version_published ON flow_version(published_at);

-- Flow nodes (vertices in the DAG)
CREATE TABLE flow_node (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    node_id VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    position_x DOUBLE NOT NULL,
    position_y DOUBLE NOT NULL,
    data_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE
);

CREATE INDEX idx_flow_node_flow_id ON flow_node(flow_id);
CREATE INDEX idx_flow_node_type ON flow_node(type);
CREATE UNIQUE INDEX uk_flow_node_id ON flow_node(flow_id, node_id);

-- Flow edges (connections between nodes)
CREATE TABLE flow_edge (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    edge_id VARCHAR(255) NOT NULL,
    source_node_id VARCHAR(255) NOT NULL,
    target_node_id VARCHAR(255) NOT NULL,
    source_handle VARCHAR(100),
    target_handle VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE
);

CREATE INDEX idx_flow_edge_flow_id ON flow_edge(flow_id);
CREATE INDEX idx_flow_edge_source ON flow_edge(source_node_id);
CREATE INDEX idx_flow_edge_target ON flow_edge(target_node_id);
CREATE UNIQUE INDEX uk_flow_edge_id ON flow_edge(flow_id, edge_id);

-- Flow validation status
CREATE TABLE flow_validation (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL UNIQUE,
    is_valid BOOLEAN NOT NULL DEFAULT FALSE,
    validation_errors_json TEXT,
    last_validated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE
);

CREATE INDEX idx_flow_validation_flow_id ON flow_validation(flow_id);
CREATE INDEX idx_flow_validation_valid ON flow_validation(is_valid);
