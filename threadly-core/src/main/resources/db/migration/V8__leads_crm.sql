-- V8: Leads and CRM functionality tables

CREATE TABLE leads (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id            UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  bot_id            UUID         NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
  conversation_id   UUID         REFERENCES conversations(id) ON DELETE SET NULL,
  email             VARCHAR(320),
  phone             VARCHAR(20),
  name              VARCHAR(200),
  custom_fields     JSONB        NOT NULL DEFAULT '{}',
  tags              TEXT[],
  source            VARCHAR(100), -- widget | api | import | etc.
  status            VARCHAR(30)  NOT NULL DEFAULT 'NEW', -- NEW | CONTACTED | QUALIFIED | CONVERTED | LOST
  notes             TEXT,
  created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_org ON leads (org_id, created_at DESC);
CREATE INDEX idx_leads_email ON leads (org_id, email);
CREATE INDEX idx_leads_status ON leads (org_id, status);

CREATE TABLE lead_activities (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lead_id           UUID         NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  org_id            UUID         NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  type              VARCHAR(50)  NOT NULL, -- call | email | message | note | etc.
  metadata          JSONB        NOT NULL DEFAULT '{}',
  created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lead_activities_lead ON lead_activities (lead_id, created_at DESC);
