-- Threadly initial schema
-- V1: identity + workspace + flow + conversations + KB + analytics + outbox

-- ── Extensions ───────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Orgs ─────────────────────────────────────────────────────────────
CREATE TABLE orgs (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name        VARCHAR(200) NOT NULL,
  slug        VARCHAR(100) NOT NULL UNIQUE,
  plan        VARCHAR(50)  NOT NULL DEFAULT 'free',
  settings    JSONB        NOT NULL DEFAULT '{}',
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Users ─────────────────────────────────────────────────────────────
CREATE TABLE users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id        UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  email         VARCHAR(320) NOT NULL,
  password_hash VARCHAR(72)  NOT NULL,
  name          VARCHAR(200) NOT NULL,
  avatar_url    TEXT,
  role          VARCHAR(50)  NOT NULL DEFAULT 'agent', -- admin | agent
  active        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (org_id, email)
);
CREATE INDEX ON users (email);
CREATE INDEX ON users (org_id);

-- ── Memberships ───────────────────────────────────────────────────────
CREATE TABLE memberships (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  org_id       UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  role         VARCHAR(50) NOT NULL DEFAULT 'agent',
  invited_by   UUID        REFERENCES users(id),
  accepted_at  TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, org_id)
);

-- ── Refresh tokens ────────────────────────────────────────────────────
CREATE TABLE refresh_tokens (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(64) NOT NULL UNIQUE,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ON refresh_tokens (token_hash);
CREATE INDEX ON refresh_tokens (user_id);

-- ── API keys ─────────────────────────────────────────────────────────
CREATE TABLE api_keys (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id       UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  name         VARCHAR(200) NOT NULL,
  key_hash     VARCHAR(64)  NOT NULL UNIQUE,
  last_used_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ON api_keys (key_hash);
CREATE INDEX ON api_keys (org_id);

-- ── Bots ──────────────────────────────────────────────────────────────
CREATE TABLE bots (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id      UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  name        VARCHAR(200) NOT NULL,
  description TEXT,
  language    VARCHAR(10)  NOT NULL DEFAULT 'en',
  theme       JSONB        NOT NULL DEFAULT '{"color":"#4F46E5","position":"bottom-right"}',
  active      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ON bots (org_id);

-- ── Flows ─────────────────────────────────────────────────────────────
CREATE TABLE flows (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id        UUID        NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  org_id        UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  draft_json    JSONB       NOT NULL DEFAULT '{"version":1,"nodes":[],"edges":[]}',
  published_json JSONB,
  published_at  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (bot_id)
);
CREATE INDEX ON flows (bot_id);
CREATE INDEX ON flows (org_id);

CREATE TABLE flow_versions (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  flow_id      UUID        NOT NULL REFERENCES flows(id) ON DELETE CASCADE,
  org_id       UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  version_num  INTEGER     NOT NULL,
  snapshot_json JSONB      NOT NULL,
  published_by UUID        REFERENCES users(id),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (flow_id, version_num)
);
CREATE INDEX ON flow_versions (flow_id);

-- ── Conversations ─────────────────────────────────────────────────────
CREATE TABLE conversations (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id      UUID         NOT NULL REFERENCES bots(id),
  org_id      UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  visitor_id  VARCHAR(200) NOT NULL,
  status      VARCHAR(50)  NOT NULL DEFAULT 'open', -- open | closed | handed_off
  channel     VARCHAR(50)  NOT NULL DEFAULT 'website',
  metadata    JSONB        NOT NULL DEFAULT '{}',
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ON conversations (org_id, bot_id, status, created_at DESC);
CREATE INDEX ON conversations (org_id, visitor_id);
CREATE INDEX ON conversations (bot_id, visitor_id);

CREATE TABLE messages (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID         NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  org_id          UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  role            VARCHAR(20)  NOT NULL, -- user | ai | agent | system
  content         TEXT         NOT NULL,
  tokens_used     INTEGER,
  latency_ms      INTEGER,
  node_id         VARCHAR(100),
  metadata        JSONB        NOT NULL DEFAULT '{}',
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ON messages (conversation_id, created_at);
CREATE INDEX ON messages (org_id, created_at DESC);

-- ── Sessions (runtime state stored in Redis, but track in DB for recovery) ──
CREATE TABLE sessions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id          UUID         NOT NULL REFERENCES bots(id),
  visitor_id      VARCHAR(200) NOT NULL,
  org_id          UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  conversation_id UUID         REFERENCES conversations(id),
  current_node_id VARCHAR(100),
  variables       JSONB        NOT NULL DEFAULT '{}',
  status          VARCHAR(50)  NOT NULL DEFAULT 'active', -- active | waiting | completed | handed_off
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (bot_id, visitor_id)
);
CREATE INDEX ON sessions (bot_id, visitor_id);

-- ── Knowledge base ────────────────────────────────────────────────────
CREATE TABLE kb_documents (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bot_id       UUID         NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  org_id       UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  name         VARCHAR(500) NOT NULL,
  type         VARCHAR(20)  NOT NULL, -- pdf | txt | url | html | docx
  storage_key  TEXT,
  source_url   TEXT,
  status       VARCHAR(20)  NOT NULL DEFAULT 'pending', -- pending | indexing | ready | error
  chunk_count  INTEGER,
  error_msg    TEXT,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ON kb_documents (bot_id);
CREATE INDEX ON kb_documents (org_id);

CREATE TABLE kb_jobs (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  document_id  UUID        NOT NULL REFERENCES kb_documents(id) ON DELETE CASCADE,
  org_id       UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  status       VARCHAR(20) NOT NULL DEFAULT 'pending',
  error_msg    TEXT,
  started_at   TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Agent handoff ─────────────────────────────────────────────────────
CREATE TABLE agent_assignments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID        NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  org_id          UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  agent_id        UUID        REFERENCES users(id),
  status          VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending | active | resolved
  assigned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  resolved_at     TIMESTAMPTZ
);
CREATE INDEX ON agent_assignments (org_id, status);
CREATE INDEX ON agent_assignments (conversation_id);

-- ── Analytics ─────────────────────────────────────────────────────────
CREATE TABLE events (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id          UUID        REFERENCES bots(id),
  conversation_id UUID        REFERENCES conversations(id),
  event_type      VARCHAR(100) NOT NULL,
  metadata        JSONB       NOT NULL DEFAULT '{}',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ON events (org_id, bot_id, event_type, created_at);

CREATE TABLE daily_rollups (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id           UUID    NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id           UUID    REFERENCES bots(id),
  date             DATE    NOT NULL,
  conversations    INTEGER NOT NULL DEFAULT 0,
  messages         INTEGER NOT NULL DEFAULT 0,
  ai_messages      INTEGER NOT NULL DEFAULT 0,
  avg_latency_ms   INTEGER,
  tokens_used      BIGINT  NOT NULL DEFAULT 0,
  cost_usd         NUMERIC(10,6) NOT NULL DEFAULT 0,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (org_id, bot_id, date)
);
CREATE INDEX ON daily_rollups (org_id, bot_id, date);

-- ── Outbox ────────────────────────────────────────────────────────────
CREATE TABLE outbox (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_type VARCHAR(100) NOT NULL,
  aggregate_id   VARCHAR(200) NOT NULL,
  event_type     VARCHAR(100) NOT NULL,
  payload        JSONB        NOT NULL,
  status         VARCHAR(20)  NOT NULL DEFAULT 'pending', -- pending | sent | failed
  retry_count    INTEGER      NOT NULL DEFAULT 0,
  error_msg      TEXT,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  sent_at        TIMESTAMPTZ
);
CREATE INDEX ON outbox (status, created_at) WHERE status = 'pending';

-- ── Plans / subscriptions (stub) ──────────────────────────────────────
CREATE TABLE plans (
  id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name   VARCHAR(100) NOT NULL UNIQUE,
  limits JSONB NOT NULL DEFAULT '{}'
);

INSERT INTO plans (name, limits) VALUES
  ('free',       '{"bots":1,"conversations_per_month":100,"kb_documents":3,"team_members":1}'),
  ('starter',    '{"bots":3,"conversations_per_month":1000,"kb_documents":20,"team_members":3}'),
  ('growth',     '{"bots":10,"conversations_per_month":10000,"kb_documents":100,"team_members":10}'),
  ('enterprise', '{"bots":-1,"conversations_per_month":-1,"kb_documents":-1,"team_members":-1}');

CREATE TABLE subscriptions (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id              UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE UNIQUE,
  plan_id             UUID        NOT NULL REFERENCES plans(id),
  status              VARCHAR(50) NOT NULL DEFAULT 'active',
  current_period_end  TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
