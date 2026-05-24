-- Runtime Service Database Schema

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
  id VARCHAR(36) PRIMARY KEY,
  bot_id VARCHAR(36) NOT NULL,
  flow_id VARCHAR(36) NOT NULL,
  visitor_id VARCHAR(36) NOT NULL,
  session_variables_json TEXT,
  state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP,
  token_usage_count INTEGER NOT NULL DEFAULT 0,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_session_bot FOREIGN KEY (bot_id) REFERENCES bots(id) ON DELETE CASCADE
);

CREATE INDEX idx_session_bot_id ON sessions(bot_id);
CREATE INDEX idx_session_flow_id ON sessions(flow_id);
CREATE INDEX idx_session_visitor_id ON sessions(visitor_id);
CREATE INDEX idx_session_state ON sessions(state);

-- Session Variables table
CREATE TABLE IF NOT EXISTS session_variables (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36) NOT NULL,
  variable_name VARCHAR(255) NOT NULL,
  variable_value TEXT,
  data_type VARCHAR(50),
  last_updated TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_session_var FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
  CONSTRAINT uk_session_var_name UNIQUE (session_id, variable_name)
);

CREATE INDEX idx_session_var_session_id ON session_variables(session_id);
CREATE INDEX idx_session_var_name ON session_variables(variable_name);

-- Execution State table
CREATE TABLE IF NOT EXISTS execution_states (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36) NOT NULL UNIQUE,
  current_node_id VARCHAR(36),
  execution_stack_json TEXT,
  status VARCHAR(20) NOT NULL,
  error_message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_exec_state_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_exec_state_session_id ON execution_states(session_id);

-- Execution Logs table
CREATE TABLE IF NOT EXISTS execution_logs (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36) NOT NULL,
  node_id VARCHAR(36) NOT NULL,
  node_type VARCHAR(100) NOT NULL,
  input_json TEXT,
  output_json TEXT,
  execution_time_ms BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  error_details TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_exec_log_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_exec_log_session_id ON execution_logs(session_id);
CREATE INDEX idx_exec_log_node_id ON execution_logs(node_id);
CREATE INDEX idx_exec_log_created_at ON execution_logs(created_at);

-- Visitor Profiles table
CREATE TABLE IF NOT EXISTS visitor_profiles (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36) NOT NULL UNIQUE,
  email VARCHAR(255),
  name VARCHAR(255),
  phone VARCHAR(20),
  custom_fields_json TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_visitor_profile_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_visitor_profile_session_id ON visitor_profiles(session_id);
CREATE INDEX idx_visitor_profile_email ON visitor_profiles(email);

-- Conversation Memory table
CREATE TABLE IF NOT EXISTS conversation_memories (
  id VARCHAR(36) PRIMARY KEY,
  session_id VARCHAR(36) NOT NULL UNIQUE,
  summary TEXT,
  recent_turns_json TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_conv_memory_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_conv_memory_session_id ON conversation_memories(session_id);
