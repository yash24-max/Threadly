-- V3: Webhook subscriptions

CREATE TABLE webhooks (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id     UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id     UUID         NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  url        TEXT         NOT NULL,
  events     JSONB        NOT NULL DEFAULT '[]',
  secret     VARCHAR(64)  NOT NULL,
  active     BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ON webhooks (bot_id, org_id) WHERE active = TRUE;
CREATE INDEX ON webhooks (org_id);
