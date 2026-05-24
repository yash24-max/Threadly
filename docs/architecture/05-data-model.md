# Data Model

## Tenancy strategy
Row-level isolation. Every tenant-scoped table has `org_id UUID NOT NULL`. A Hibernate `@Filter` auto-applies `WHERE org_id = :currentOrg` from the JWT. Repositories never accept raw `org_id` — it's injected from `TenantContext`.

## Core tables

### Identity
```sql
orgs (id, name, slug, plan, settings_jsonb, created_at)
users (id, org_id, email, password_hash, name, avatar_url, role, active, created_at)
memberships (id, user_id, org_id, role, invited_by, accepted_at)
refresh_tokens (id, user_id, token_hash, expires_at, revoked)
api_keys (id, org_id, name, key_hash, last_used_at, created_at)
```

### Workspace
```sql
bots (id, org_id, name, description, language, theme_jsonb, active, created_at, updated_at)
```

### Flow
```sql
flows (id, bot_id, org_id, draft_json, published_json, published_at, created_at, updated_at)
flow_versions (id, flow_id, org_id, version_num, snapshot_json, published_by, created_at)
```

Flow JSON shape:
```json
{
  "nodes": [
    { "id": "start", "type": "start", "data": {} },
    { "id": "n1", "type": "message", "data": { "text": "Hello!" } },
    { "id": "n2", "type": "ai_reply", "data": { "prompt": "You are a support agent.", "use_kb": true } }
  ],
  "edges": [
    { "id": "e1", "source": "start", "target": "n1" },
    { "id": "e2", "source": "n1", "target": "n2" }
  ]
}
```

### Runtime / Conversations
```sql
sessions (id, bot_id, visitor_id, org_id, state_jsonb, current_node_id, created_at, updated_at)
conversations (id, bot_id, org_id, visitor_id, status, channel, metadata_jsonb, created_at, updated_at)
messages (id, conversation_id, org_id, role [user|ai|agent|system], content, tokens_used, latency_ms, node_id, created_at)
```

### Knowledge Base
```sql
kb_documents (id, bot_id, org_id, name, type [pdf|txt|url], storage_key, status [pending|indexing|ready|error], chunk_count, created_at)
kb_jobs (id, document_id, org_id, status, error_msg, started_at, completed_at)
```

### Agent / Handoff
```sql
agent_assignments (id, conversation_id, org_id, agent_id, status [pending|active|resolved], assigned_at, resolved_at)
```

### Analytics
```sql
events (id, org_id, bot_id, conversation_id, event_type, metadata_jsonb, created_at)
daily_rollups (id, org_id, bot_id, date, conversations, messages, ai_messages, avg_latency_ms, tokens_used, cost_usd)
```

### Outbox
```sql
outbox (id, aggregate_type, aggregate_id, event_type, payload_jsonb, status [pending|sent|failed], created_at, sent_at, retry_count)
```

### Billing (stub)
```sql
plans (id, name, limits_jsonb)
subscriptions (id, org_id, plan_id, status, current_period_end)
```

## Indexes (critical ones)
```sql
-- Tenancy fast-paths
CREATE INDEX ON messages (conversation_id, created_at);
CREATE INDEX ON conversations (org_id, bot_id, status, created_at DESC);
CREATE INDEX ON sessions (bot_id, visitor_id);

-- Outbox poller
CREATE INDEX ON outbox (status, created_at) WHERE status = 'pending';

-- Analytics
CREATE INDEX ON events (org_id, bot_id, event_type, created_at);
CREATE INDEX ON daily_rollups (org_id, bot_id, date);
```
