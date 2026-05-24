-- Flow execution logging and audit trail

-- Execution logs for debugging and monitoring
CREATE TABLE flow_execution_log (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    node_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    execution_time_ms BIGINT,
    error_message TEXT,
    input_data_json TEXT,
    output_data_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE
);

CREATE INDEX idx_flow_exec_flow_id ON flow_execution_log(flow_id);
CREATE INDEX idx_flow_exec_session_id ON flow_execution_log(session_id);
CREATE INDEX idx_flow_exec_status ON flow_execution_log(status);
CREATE INDEX idx_flow_exec_created ON flow_execution_log(created_at);

-- Publish history and audit trail
CREATE TABLE flow_publish_log (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    published_by VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    previous_version_id VARCHAR(36),
    current_version_id VARCHAR(36) NOT NULL,
    rollback_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE,
    FOREIGN KEY (previous_version_id) REFERENCES flow_version(id) ON DELETE SET NULL,
    FOREIGN KEY (current_version_id) REFERENCES flow_version(id) ON DELETE RESTRICT
);

CREATE INDEX idx_flow_pub_flow_id ON flow_publish_log(flow_id);
CREATE INDEX idx_flow_pub_published_by ON flow_publish_log(published_by);
CREATE INDEX idx_flow_pub_event_type ON flow_publish_log(event_type);
CREATE INDEX idx_flow_pub_created ON flow_publish_log(created_at);
