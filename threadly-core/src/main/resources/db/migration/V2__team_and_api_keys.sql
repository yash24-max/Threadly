-- V2: Team memberships enhancements + API keys per-bot

-- ── Memberships table is already created in V1, add accepted_at if missing ──
ALTER TABLE memberships
  ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMPTZ;

-- ── Drop and recreate api_keys to add bot_id + per-bot scoping ────────────
-- V1 had a simpler api_keys table (org-scoped, no bot_id).
-- We extend it to support per-bot keys with BCrypt + SHA-256 dual hash.
DROP TABLE IF EXISTS api_keys CASCADE;

CREATE TABLE api_keys (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id           UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id           UUID         NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  name             VARCHAR(200) NOT NULL,
  key_hash         VARCHAR(72)  NOT NULL,
  key_lookup_hash  VARCHAR(64)  NOT NULL UNIQUE,
  key_prefix       VARCHAR(16)  NOT NULL,
  last_used_at     TIMESTAMPTZ,
  revoked_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ON api_keys (key_lookup_hash) WHERE revoked_at IS NULL;
CREATE INDEX ON api_keys (bot_id, org_id) WHERE revoked_at IS NULL;
CREATE INDEX ON api_keys (org_id);
