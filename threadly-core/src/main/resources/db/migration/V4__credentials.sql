-- V4: Encrypted bot credentials store (AES-256-GCM)

CREATE TABLE bot_credentials (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id          UUID         NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  name            VARCHAR(200) NOT NULL,
  encrypted_value TEXT         NOT NULL,
  type            VARCHAR(50)  NOT NULL DEFAULT 'generic',
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (bot_id, name)
);

CREATE INDEX ON bot_credentials (bot_id, org_id);
CREATE INDEX ON bot_credentials (org_id);
