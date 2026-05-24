-- Conversation Service Schema Initialization
-- Manages conversations, messages, and chat history

CREATE SCHEMA IF NOT EXISTS conversation_service;

-- Organizations table (reference)
CREATE TABLE IF NOT EXISTS conversation_service.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table - represents individual chat sessions
CREATE TABLE IF NOT EXISTS conversation_service.sessions (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES conversation_service.organizations(id),
    bot_id UUID NOT NULL,
    customer_id UUID,
    customer_name VARCHAR(255),
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),
    status VARCHAR(50) DEFAULT 'active' CHECK (status IN ('active', 'closed', 'archived')),
    channel VARCHAR(50) DEFAULT 'web',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_org_id ON conversation_service.sessions(org_id);
CREATE INDEX IF NOT EXISTS idx_sessions_bot_id ON conversation_service.sessions(bot_id);
CREATE INDEX IF NOT EXISTS idx_sessions_customer_id ON conversation_service.sessions(customer_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON conversation_service.sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_created_at ON conversation_service.sessions(created_at);

-- Messages table - individual messages in a conversation
CREATE TABLE IF NOT EXISTS conversation_service.messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES conversation_service.sessions(id) ON DELETE CASCADE,
    org_id UUID NOT NULL REFERENCES conversation_service.organizations(id),
    sender_type VARCHAR(50) NOT NULL CHECK (sender_type IN ('customer', 'bot', 'agent')),
    sender_id UUID,
    message_text TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'file', 'rich')),
    attachments JSONB,
    rich_content JSONB,
    status VARCHAR(50) DEFAULT 'sent' CHECK (status IN ('sent', 'delivered', 'read', 'failed')),
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_session_id ON conversation_service.messages(session_id);
CREATE INDEX IF NOT EXISTS idx_messages_org_id ON conversation_service.messages(org_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON conversation_service.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON conversation_service.messages(created_at);

-- Conversation metadata (thread-level data)
CREATE TABLE IF NOT EXISTS conversation_service.conversations (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE REFERENCES conversation_service.sessions(id) ON DELETE CASCADE,
    org_id UUID NOT NULL REFERENCES conversation_service.organizations(id),
    flow_execution_id UUID,
    assigned_to UUID,
    priority VARCHAR(50) DEFAULT 'normal' CHECK (priority IN ('low', 'normal', 'high', 'urgent')),
    tags JSONB,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversations_org_id ON conversation_service.conversations(org_id);
CREATE INDEX IF NOT EXISTS idx_conversations_assigned_to ON conversation_service.conversations(assigned_to);

-- Message reactions/sentiment
CREATE TABLE IF NOT EXISTS conversation_service.message_reactions (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES conversation_service.messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reaction_unique UNIQUE (message_id, user_id, reaction_type)
);

CREATE INDEX IF NOT EXISTS idx_reactions_message_id ON conversation_service.message_reactions(message_id);

-- Typing indicators (temporary, for real-time features)
CREATE TABLE IF NOT EXISTS conversation_service.typing_indicators (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES conversation_service.sessions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    user_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_typing_session_id ON conversation_service.typing_indicators(session_id);
