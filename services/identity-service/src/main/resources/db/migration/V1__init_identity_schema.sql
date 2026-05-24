-- Threadly Identity Service Database Schema
-- Manages users, organizations, teams, memberships, API keys, and refresh tokens

-- Users table
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  org_id VARCHAR(36) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(72) NOT NULL,
  full_name VARCHAR(255),
  profile_picture_url VARCHAR(500),
  job_title VARCHAR(255),
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  email_verified_at TIMESTAMP,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_at TIMESTAMP
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_org_id ON users(org_id);
CREATE INDEX idx_created_at ON users(created_at);

-- Organizations table
CREATE TABLE organizations (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  owner_id VARCHAR(36) NOT NULL,
  plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
  stripe_customer_id VARCHAR(255),
  description TEXT,
  website VARCHAR(500),
  logo_url VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_owner_id ON organizations(owner_id);
CREATE INDEX idx_created_at ON organizations(created_at);
CREATE UNIQUE INDEX idx_stripe_customer_id ON organizations(stripe_customer_id) WHERE stripe_customer_id IS NOT NULL;

-- Teams table
CREATE TABLE teams (
  id VARCHAR(36) PRIMARY KEY,
  org_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_org_id_teams ON teams(org_id);
CREATE INDEX idx_created_at_teams ON teams(created_at);

-- Memberships table (User -> Organization -> Team relationship)
CREATE TABLE memberships (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  org_id VARCHAR(36) NOT NULL,
  role VARCHAR(50) NOT NULL,
  team_ids TEXT DEFAULT '',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, org_id)
);

CREATE INDEX idx_user_id_org_id ON memberships(user_id, org_id);
CREATE INDEX idx_org_id_memberships ON memberships(org_id);
CREATE INDEX idx_role ON memberships(role);

-- API Keys table
CREATE TABLE api_keys (
  id VARCHAR(36) PRIMARY KEY,
  org_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  key_hash VARCHAR(255) NOT NULL UNIQUE,
  key_prefix VARCHAR(50),
  scopes TEXT DEFAULT 'read,write',
  last_used_at TIMESTAMP,
  expires_at TIMESTAMP,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  revoked_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_org_id_api_keys ON api_keys(org_id);
CREATE INDEX idx_key_hash ON api_keys(key_hash);
CREATE INDEX idx_created_at_api_keys ON api_keys(created_at);

-- Refresh Tokens table
CREATE TABLE refresh_tokens (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  issued_from_ip VARCHAR(45),
  user_agent TEXT,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_id_refresh ON refresh_tokens(user_id);
CREATE INDEX idx_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);

-- Add foreign key constraints
ALTER TABLE users
ADD CONSTRAINT fk_users_org_id
FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE;

ALTER TABLE memberships
ADD CONSTRAINT fk_memberships_user_id
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE memberships
ADD CONSTRAINT fk_memberships_org_id
FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE;

ALTER TABLE teams
ADD CONSTRAINT fk_teams_org_id
FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE;

ALTER TABLE api_keys
ADD CONSTRAINT fk_api_keys_org_id
FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE;

ALTER TABLE refresh_tokens
ADD CONSTRAINT fk_refresh_tokens_user_id
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
