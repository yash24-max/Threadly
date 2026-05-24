-- Bot table: stores bot workspaces with multi-tenancy and soft deletes
CREATE TABLE bot (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at TIMESTAMP(3),
    INDEX idx_bot_org_id (org_id),
    INDEX idx_bot_deleted_at (deleted_at),
    CONSTRAINT fk_bot_org FOREIGN KEY (org_id) REFERENCES organization(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bot settings: customizable configuration per bot
CREATE TABLE bot_settings (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL UNIQUE,
    theme_color VARCHAR(7) DEFAULT '#3B82F6',
    avatar LONGTEXT,
    welcome_message TEXT,
    max_token_budget INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_bot_settings_bot_id (bot_id),
    CONSTRAINT fk_bot_settings_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bot version: version history with configuration snapshots
CREATE TABLE bot_version (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    config_snapshot LONGTEXT,
    published_at TIMESTAMP(3) NOT NULL,
    published_by VARCHAR(36) NOT NULL,
    release_notes TEXT,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_bot_version_bot_id (bot_id),
    INDEX idx_bot_version_version_number (bot_id, version_number),
    UNIQUE KEY uk_bot_version_number (bot_id, version_number),
    CONSTRAINT fk_bot_version_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Team member: role-based access control
CREATE TABLE team_member (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_team_member_bot_id (bot_id),
    INDEX idx_team_member_user_id (user_id),
    INDEX idx_team_member_bot_user (bot_id, user_id),
    UNIQUE KEY uk_team_member_bot_user (bot_id, user_id),
    CONSTRAINT fk_team_member_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bot API key: hashed credentials for programmatic access
CREATE TABLE bot_api_key (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    key_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    revoked_at TIMESTAMP(3),
    created_by VARCHAR(36) NOT NULL,
    last_used_at TIMESTAMP(3),
    INDEX idx_bot_api_key_bot_id (bot_id),
    INDEX idx_bot_api_key_key_hash (key_hash),
    CONSTRAINT fk_bot_api_key_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bot webhook: event subscriptions
CREATE TABLE bot_webhook (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    url LONGTEXT NOT NULL,
    events TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    secret VARCHAR(255),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    last_delivered_at TIMESTAMP(3),
    INDEX idx_bot_webhook_bot_id (bot_id),
    CONSTRAINT fk_bot_webhook_bot FOREIGN KEY (bot_id) REFERENCES bot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
