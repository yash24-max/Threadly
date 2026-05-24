-- Conversation Service Database Schema
-- This migration creates tables for conversations, messages, leads, tags, and notes

-- Create conversations table
CREATE TABLE conversations (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    flow_id VARCHAR(36),
    visitor_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'HANDED_OFF')),
    assigned_agent_id VARCHAR(36),
    message_count INT NOT NULL DEFAULT 0,
    tokens_used BIGINT NOT NULL DEFAULT 0,
    metadata_json TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_conversation_id_valid CHECK (LENGTH(id) = 36),
    CONSTRAINT chk_org_id_valid CHECK (LENGTH(org_id) = 36)
);

-- Create indexes on conversations
CREATE INDEX idx_org_id ON conversations(org_id);
CREATE INDEX idx_bot_id ON conversations(bot_id);
CREATE INDEX idx_visitor_id ON conversations(visitor_id);
CREATE INDEX idx_status ON conversations(status);
CREATE INDEX idx_started_at ON conversations(started_at);
CREATE INDEX idx_org_status ON conversations(org_id, status);
CREATE INDEX idx_org_visitor ON conversations(org_id, visitor_id);

-- Create messages table
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender VARCHAR(20) NOT NULL CHECK (sender IN ('VISITOR', 'AI', 'HUMAN')),
    sender_id VARCHAR(36),
    content TEXT NOT NULL,
    metadata_json TEXT,
    tokens_used BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_id_valid CHECK (LENGTH(id) = 36),
    CONSTRAINT chk_conversation_id_msg_valid CHECK (LENGTH(conversation_id) = 36)
);

-- Create indexes on messages
CREATE INDEX idx_conversation_id ON messages(conversation_id);
CREATE INDEX idx_sender ON messages(sender);
CREATE INDEX idx_created_at ON messages(created_at);
CREATE INDEX idx_conversation_created ON messages(conversation_id, created_at);

-- Create leads table
CREATE TABLE leads (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    conversation_id VARCHAR(36) NOT NULL,
    visitor_id VARCHAR(36),
    email VARCHAR(255),
    phone VARCHAR(20),
    name VARCHAR(255),
    company VARCHAR(255),
    custom_fields_json TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'CONTACTED', 'CONVERTED', 'LOST', 'DUPLICATE')),
    quality_score INT CHECK (quality_score >= 0 AND quality_score <= 100),
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_lead_id_valid CHECK (LENGTH(id) = 36),
    CONSTRAINT chk_org_id_lead_valid CHECK (LENGTH(org_id) = 36),
    CONSTRAINT chk_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

-- Create indexes on leads
CREATE INDEX idx_org_id_leads ON leads(org_id);
CREATE INDEX idx_conversation_id_leads ON leads(conversation_id);
CREATE INDEX idx_visitor_id_leads ON leads(visitor_id);
CREATE INDEX idx_email ON leads(email);
CREATE INDEX idx_phone ON leads(phone);
CREATE INDEX idx_status_leads ON leads(status);
CREATE INDEX idx_org_status_leads ON leads(org_id, status);
CREATE INDEX idx_captured_at ON leads(captured_at);
CREATE UNIQUE INDEX idx_email_org ON leads(org_id, email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX idx_phone_org ON leads(org_id, phone) WHERE phone IS NOT NULL;

-- Create conversation_tags table
CREATE TABLE conversation_tags (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_tag FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_tag_id_valid CHECK (LENGTH(id) = 36),
    CONSTRAINT chk_conversation_id_tag_valid CHECK (LENGTH(conversation_id) = 36)
);

-- Create indexes on conversation_tags
CREATE INDEX idx_conversation_id_tags ON conversation_tags(conversation_id);
CREATE INDEX idx_tag_name ON conversation_tags(tag_name);
CREATE INDEX idx_tag_value ON conversation_tags(tag_value);
CREATE INDEX idx_tag_name_value ON conversation_tags(tag_name, tag_value);

-- Create conversation_notes table
CREATE TABLE conversation_notes (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_conversation_note FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_note_id_valid CHECK (LENGTH(id) = 36),
    CONSTRAINT chk_conversation_id_note_valid CHECK (LENGTH(conversation_id) = 36),
    CONSTRAINT chk_agent_id_valid CHECK (LENGTH(agent_id) = 36)
);

-- Create indexes on conversation_notes
CREATE INDEX idx_conversation_id_notes ON conversation_notes(conversation_id);
CREATE INDEX idx_agent_id ON conversation_notes(agent_id);
CREATE INDEX idx_created_at_notes ON conversation_notes(created_at);

-- Create audit table for message deletions
CREATE TABLE message_audit_log (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(36) NOT NULL,
    conversation_id VARCHAR(36) NOT NULL,
    action VARCHAR(20) NOT NULL,
    deleted_by VARCHAR(36),
    deleted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    CONSTRAINT chk_action_valid CHECK (action IN ('DELETED', 'RESTORED'))
);

CREATE INDEX idx_message_audit_log ON message_audit_log(message_id, conversation_id);
