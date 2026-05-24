-- V9: Plans, subscriptions, and usage tracking for billing

CREATE TABLE plans (
  id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                      VARCHAR(100) NOT NULL UNIQUE,
  price_monthly             DECIMAL(10, 2) NOT NULL DEFAULT 0,
  price_yearly              DECIMAL(10, 2) NOT NULL DEFAULT 0,
  stripe_product_id         VARCHAR(100),
  max_bots                  INTEGER,
  max_conversations_monthly INTEGER,
  max_kb_documents          INTEGER,
  max_team_members          INTEGER,
  features                  JSONB        NOT NULL DEFAULT '{}',
  created_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO plans (
  name, price_monthly, price_yearly, max_bots, max_conversations_monthly, max_kb_documents, max_team_members,
  features
) VALUES
  ('FREE', 0, 0, 1, 500, 5, 1, '{"integrations": 0, "custom_domain": false, "priority_support": false, "ai_powered": true, "webhooks": false}'),
  ('PRO', 29, 290, 5, 5000, 50, 5, '{"integrations": 5, "custom_domain": true, "priority_support": true, "ai_powered": true, "webhooks": true}'),
  ('BUSINESS', 99, 990, NULL, NULL, 500, 25, '{"integrations": 20, "custom_domain": true, "priority_support": true, "ai_powered": true, "webhooks": true, "advanced_analytics": true, "sso": true}'),
  ('ENTERPRISE', 0, 0, NULL, NULL, NULL, NULL, '{"integrations": "unlimited", "custom_domain": true, "priority_support": true, "ai_powered": true, "webhooks": true, "advanced_analytics": true, "sso": true, "dedicated_account_manager": true}');

CREATE TABLE subscriptions (
  id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id                    UUID        NOT NULL UNIQUE REFERENCES orgs(id) ON DELETE CASCADE,
  plan_id                   UUID        NOT NULL REFERENCES plans(id),
  stripe_subscription_id    VARCHAR(100),
  stripe_customer_id        VARCHAR(100),
  status                    VARCHAR(30) NOT NULL DEFAULT 'active', -- active | past_due | canceled | unpaid
  current_period_start      TIMESTAMPTZ,
  current_period_end        TIMESTAMPTZ,
  cancel_at_period_end      BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at                TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE usage_records (
  id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id                    UUID        NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
  period_start              DATE        NOT NULL,
  period_end                DATE        NOT NULL,
  conversations_count       BIGINT      NOT NULL DEFAULT 0,
  messages_count            BIGINT      NOT NULL DEFAULT 0,
  ai_tokens_used            BIGINT      NOT NULL DEFAULT 0,
  ai_cost_usd               DECIMAL(12, 4) NOT NULL DEFAULT 0,
  created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (org_id, period_start)
);

CREATE INDEX ON usage_records (org_id, period_start DESC);
