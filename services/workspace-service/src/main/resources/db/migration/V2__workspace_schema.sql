-- Workspace Service Bot Schema (PostgreSQL)
-- Tables used by JPA entities in workspace_service schema

CREATE TABLE IF NOT EXISTS bot (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_bot_org_id ON bot(org_id);
CREATE INDEX IF NOT EXISTS idx_bot_deleted_at ON bot(deleted_at);

CREATE TABLE IF NOT EXISTS bot_settings (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL UNIQUE,
    theme_color VARCHAR(7) DEFAULT '#3B82F6',
    avatar TEXT,
    welcome_message TEXT,
    max_token_budget INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bot_settings_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bot_settings_bot_id ON bot_settings(bot_id);

CREATE TABLE IF NOT EXISTS bot_version (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    config_snapshot TEXT,
    published_at TIMESTAMP NOT NULL,
    published_by VARCHAR(36) NOT NULL,
    release_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (bot_id, version_number),
    CONSTRAINT fk_bot_version_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bot_version_bot_id ON bot_version(bot_id);

CREATE TABLE IF NOT EXISTS team_member (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (bot_id, user_id),
    CONSTRAINT fk_team_member_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_team_member_bot_id ON team_member(bot_id);
CREATE INDEX IF NOT EXISTS idx_team_member_user_id ON team_member(user_id);

CREATE TABLE IF NOT EXISTS bot_api_key (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    key_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    created_by VARCHAR(36) NOT NULL,
    last_used_at TIMESTAMP,
    CONSTRAINT fk_bot_api_key_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bot_api_key_bot_id ON bot_api_key(bot_id);
CREATE INDEX IF NOT EXISTS idx_bot_api_key_key_hash ON bot_api_key(key_hash);

CREATE TABLE IF NOT EXISTS bot_webhook (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    url TEXT NOT NULL,
    events TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    secret VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_delivered_at TIMESTAMP,
    CONSTRAINT fk_bot_webhook_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bot_webhook_bot_id ON bot_webhook(bot_id);
