-- V7: Integrations table for third-party integrations (Slack, HubSpot, Salesforce, etc.)

CREATE TABLE integrations (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  name            VARCHAR(200) NOT NULL,
  type            VARCHAR(50)  NOT NULL, -- slack | hubspot | salesforce | pipedrive | etc.
  config          JSONB        NOT NULL DEFAULT '{}', -- Integration-specific configuration
  credentials_id  UUID         REFERENCES bot_credentials(id) ON DELETE SET NULL,
  status          VARCHAR(30)  NOT NULL DEFAULT 'active', -- active | inactive | error
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_integrations_org ON integrations (org_id);
