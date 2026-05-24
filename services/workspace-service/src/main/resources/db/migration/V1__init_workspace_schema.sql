-- Workspace Service Schema Initialization
-- Handles bot, conversation, and workspace management

CREATE SCHEMA IF NOT EXISTS workspace_service;

-- Organizations table (shared across services, replicated here for reference)
CREATE TABLE IF NOT EXISTS workspace_service.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT org_not_deleted CHECK (deleted_at IS NULL)
);

-- Bots table - core entity managed by workspace-service
CREATE TABLE IF NOT EXISTS workspace_service.bots (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES workspace_service.organizations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(10) DEFAULT 'en',
    status VARCHAR(50) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'archived')),
    accent_color VARCHAR(7),
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT bot_unique_per_org UNIQUE (org_id, name),
    CONSTRAINT bot_not_deleted CHECK (deleted_at IS NULL)
);

CREATE INDEX IF NOT EXISTS idx_bots_org_id ON workspace_service.bots(org_id);
CREATE INDEX IF NOT EXISTS idx_bots_created_by ON workspace_service.bots(created_by);
CREATE INDEX IF NOT EXISTS idx_bots_status ON workspace_service.bots(status);

-- Bot members/team table
CREATE TABLE IF NOT EXISTS workspace_service.bot_members (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL REFERENCES workspace_service.bots(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('owner', 'admin', 'editor', 'viewer')),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bot_member_unique UNIQUE (bot_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_bot_members_bot_id ON workspace_service.bot_members(bot_id);
CREATE INDEX IF NOT EXISTS idx_bot_members_user_id ON workspace_service.bot_members(user_id);

-- Bot settings/configuration
CREATE TABLE IF NOT EXISTS workspace_service.bot_settings (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL UNIQUE REFERENCES workspace_service.bots(id) ON DELETE CASCADE,
    greeting_message TEXT,
    closed_message TEXT,
    working_hours_enabled BOOLEAN DEFAULT FALSE,
    working_hours_json JSONB,
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bot_settings_bot_id ON workspace_service.bot_settings(bot_id);

-- Workspaces table (organizational units within an org)
CREATE TABLE IF NOT EXISTS workspace_service.workspaces (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES workspace_service.organizations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT workspace_unique_per_org UNIQUE (org_id, name),
    CONSTRAINT workspace_not_deleted CHECK (deleted_at IS NULL)
);

CREATE INDEX IF NOT EXISTS idx_workspaces_org_id ON workspace_service.workspaces(org_id);

-- Bot assignments to workspaces
CREATE TABLE IF NOT EXISTS workspace_service.bot_workspace_assignments (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL REFERENCES workspace_service.bots(id) ON DELETE CASCADE,
    workspace_id UUID NOT NULL REFERENCES workspace_service.workspaces(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bot_workspace_unique UNIQUE (bot_id, workspace_id)
);

CREATE INDEX IF NOT EXISTS idx_bot_workspace_bot_id ON workspace_service.bot_workspace_assignments(bot_id);
CREATE INDEX IF NOT EXISTS idx_bot_workspace_workspace_id ON workspace_service.bot_workspace_assignments(workspace_id);
