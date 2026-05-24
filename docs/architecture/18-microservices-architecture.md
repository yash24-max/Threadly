# Threadly Microservices Architecture Plan

**Status**: Phase 1 — Refactoring from modular monolith to distributed microservices  
**Timeline**: 2–3 weeks with parallel agent execution  
**Deployment**: Kong/Nginx API Gateway + Kafka async + REST sync + Docker Compose (dev) + Consul/Eureka (prod)  
**Data Strategy**: Database-per-service with event-driven consistency  
**Observability**: Unified OpenTelemetry traces across all services

---

## 1. Service Boundary Map (9 Java Services + 3 Supporting)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            THREADLY MICROSERVICES                            │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                         API Gateway (Nginx)                          │  │
│  │  Port 8080 (public), routes → service mesh, rate limiting, CORS     │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌─────────────────┬─────────────────┬──────────────┬────────────────────┐  │
│  │  identity       │  workspace      │  flow        │  runtime           │  │
│  │  Service        │  Service        │  Service     │  Service           │  │
│  │  :3001          │  :3002          │  :3003       │  :3004             │  │
│  │                 │                 │              │                    │  │
│  │ • Auth (OAuth)  │ • Orgs/Members  │ • Flow CRUD  │ • Session state    │  │
│  │ • JWT tokens    │ • Bots          │ • Versions   │ • Execution        │  │
│  │ • Users         │ • Settings      │ • Publish    │ • Node execution   │  │
│  │ • Refresh flow  │ • API keys      │ • Draft      │ • Variable store   │  │
│  └─────────────────┴─────────────────┴──────────────┴────────────────────┘  │
│                                                                              │
│  ┌──────────────────┬──────────────────┬─────────────┬────────────────────┐  │
│  │  conversation    │  knowledge       │  analytics  │  billing           │  │
│  │  Service         │  Service         │  Service    │  Service           │  │
│  │  :3005           │  :3006           │  :3007      │  :3008             │  │
│  │                  │                  │             │                    │  │
│  │ • Transcripts    │ • Doc upload     │ • Events    │ • Plans            │  │
│  │ • Handoff        │ • RAG query      │ • Metrics   │ • Subscriptions    │  │
│  │ • Leads (CRM)    │ • Ingestion jobs │ • Dashboard │ • Stripe webhook   │  │
│  │ • Tags           │ • Qdrant vector  │ • Exports   │ • Usage metering   │  │
│  │ • Timeline       │ • KB scraping    │             │ • Invoice (later)  │  │
│  └──────────────────┴──────────────────┴─────────────┴────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────┬──────────────────────────────┐  │
│  │  integration-service                   │  (3 supporting services)     │  │
│  │  :3009                                 │                              │  │
│  │                                        │ • threadly-ai (FastAPI 8001) │  │
│  │ • Connectors (20 types)                │ • threadly-web (Next.js 3000)│  │
│  │ • Marketplace                          │ • threadly-widget (CDN)      │  │
│  │ • OAuth callback handling              │                              │  │
│  │ • Action execution                     │                              │  │
│  └────────────────────────────────────────┴──────────────────────────────┘  │
│                                                                              │
│  ┌───────────────┬──────────────────┬──────────────┬────────────────────┐   │
│  │  PostgreSQL   │  Kafka Cluster   │  Consul/     │  Redis Cluster     │   │
│  │               │  (async events)  │  Eureka      │  (sessions,        │   │
│  │ Database-per- │  Topics:         │  (service    │  cache, rate       │   │
│  │ service schema│  • conv.msg      │  discovery)  │  limits)           │   │
│  │               │  • flow.exec     │  Port 8500   │                    │   │
│  │               │  • kb.indexed    │  Port 8761   │                    │   │
│  │               │  • lead.created  │              │                    │   │
│  │               │  • flow.version  │              │                    │   │
│  │               │  • billing.*     │              │                    │   │
│  └───────────────┴──────────────────┴──────────────┴────────────────────┘   │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Observability (Unified across all services)                          │  │
│  │  • OpenTelemetry SDK in every service (Java SDK + FastAPI integration)│  │
│  │  • One trace ID spans all hops (Nginx → service → Kafka → service)   │  │
│  │  • Metrics → Prometheus (each service `/metrics`)                     │  │
│  │  • Traces → Honeycomb or Tempo (via OTLP exporter)                   │  │
│  │  • Logs → Loki (structured JSON, service label)                      │  │
│  │  • LLM traces → Langfuse (cost tracking, prompt versioning)           │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Service Specifications (Domain, Responsibility, Database)

### 2.1 Identity Service (:3001)
**Domain**: Authentication, user management, organization/tenancy boundaries  
**Responsibility**:
- User signup/login (local, OAuth 2.0 via GitHub/Google)
- JWT token issuance (RS256, access + refresh)
- Password reset, email verification
- Organization creation, membership management
- API key generation + validation (scoped to org/bot)
- Tenancy context injection (validates org membership per request)

**Database Schema** (`identity_service` schema in shared Postgres):
```sql
-- Users
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255),
  email_verified BOOLEAN DEFAULT FALSE,
  oauth_provider VARCHAR(50),
  oauth_id VARCHAR(255),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Organizations
CREATE TABLE organizations (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(100) UNIQUE NOT NULL,
  owner_id UUID NOT NULL REFERENCES users(id),
  subscription_plan VARCHAR(50) DEFAULT 'free',
  billing_email VARCHAR(255),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Memberships (org_id + user_id + role)
CREATE TABLE memberships (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL REFERENCES organizations(id),
  user_id UUID NOT NULL REFERENCES users(id),
  role VARCHAR(50) DEFAULT 'member', -- owner, admin, member
  created_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(org_id, user_id)
);

-- API Keys
CREATE TABLE api_keys (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL REFERENCES organizations(id),
  name VARCHAR(255) NOT NULL,
  key_hash VARCHAR(255) UNIQUE NOT NULL,
  scopes TEXT[] DEFAULT '{}',
  last_used_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id)
);
```

**API Endpoints**:
- `POST /auth/signup` — create user, org, membership
- `POST /auth/login` — local or OAuth
- `POST /auth/refresh` — rotate tokens
- `POST /auth/logout` — invalidate refresh token
- `GET /me` — current user + org context
- `GET /orgs/{orgId}/members` — list members
- `POST /orgs/{orgId}/members` — invite user
- `DELETE /orgs/{orgId}/members/{userId}` — remove member
- `POST /apikeys` — generate new key
- `DELETE /apikeys/{keyId}` — revoke key
- `POST /tenancy/validate` — verify org membership (internal use)

**Events Published to Kafka**:
- `user.created` → {userId, email, orgId}
- `org.created` → {orgId, name, ownerId}
- `membership.updated` → {orgId, userId, role}

**Inter-Service Calls**:
- ← All services: Validate JWT, extract org_id (via Feign `IdentityServiceClient`)
- → (none; terminal service)

---

### 2.2 Workspace Service (:3002)
**Domain**: Organizations, bots, workspace settings, API key management  
**Responsibility**:
- Bot CRUD (create, read, update, delete)
- Bot settings (language, accent color, knowledge base settings, integrations)
- Team management (owner → admin → member roles)
- Workspace settings (branding, custom domain, webhook signing key)
- Usage dashboard (conversation count, API calls, KB ingestion jobs)

**Database Schema** (`workspace_service` schema):
```sql
CREATE TABLE bots (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  language VARCHAR(10) DEFAULT 'en',
  accent_color VARCHAR(7) DEFAULT '#4F46E5',
  avatar_url VARCHAR(500),
  welcome_message TEXT,
  kb_search_enabled BOOLEAN DEFAULT TRUE,
  integrations JSONB DEFAULT '{}',
  webhook_signing_key VARCHAR(255),
  status VARCHAR(50) DEFAULT 'active', -- active, paused, archived
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id),
  INDEX(status)
);

CREATE TABLE workspace_settings (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL UNIQUE REFERENCES organizations(id),
  custom_domain VARCHAR(255),
  custom_branding JSONB,
  sso_config JSONB,
  rate_limits JSONB DEFAULT '{"conversations_per_day": 100000, "api_calls_per_min": 1000}',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE bot_metrics_snapshot (
  id UUID PRIMARY KEY,
  bot_id UUID NOT NULL REFERENCES bots(id),
  date DATE NOT NULL,
  conversation_count INT DEFAULT 0,
  unique_visitors INT DEFAULT 0,
  api_calls INT DEFAULT 0,
  kb_queries INT DEFAULT 0,
  avg_response_time_ms FLOAT,
  INDEX(bot_id, date)
);
```

**API Endpoints**:
- `GET /bots` — list bots in org
- `POST /bots` — create bot
- `GET /bots/{botId}` — bot details
- `PATCH /bots/{botId}` — update bot settings
- `DELETE /bots/{botId}` — archive/delete bot
- `GET /bots/{botId}/metrics` — usage stats
- `GET /workspace/settings` — org settings
- `PATCH /workspace/settings` — update settings
- `POST /workspace/invite` — invite team member
- `GET /workspace/integrations` — integration status

**Events Published**:
- `bot.created` → {botId, orgId, name}
- `bot.updated` → {botId, fieldChanges}
- `bot.deleted` → {botId, orgId}
- `workspace.settings.updated` → {orgId, settings}

**Inter-Service Calls**:
- → Identity Service (validate JWT)
- ← Flow Service: fetch bot flows on update
- ← Runtime Service: fetch session counts for metrics
- ← Analytics Service: pull conversation counts

---

### 2.3 Flow Service (:3003)
**Domain**: Bot flow definitions (JSON), versioning, publishing  
**Responsibility**:
- Flow CRUD (create, read, update)
- Flow validation (schema, node references, edges)
- Version management (draft vs. published, rollback)
- Flow publish workflow (validation → publish → emit event)
- Flow cloning from templates
- Flow export/import

**Database Schema** (`flow_service` schema):
```sql
CREATE TABLE flows (
  id UUID PRIMARY KEY,
  bot_id UUID NOT NULL,
  org_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  flow_json JSONB NOT NULL, -- full node + edge definition
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, bot_id)
);

CREATE TABLE flow_versions (
  id UUID PRIMARY KEY,
  flow_id UUID NOT NULL REFERENCES flows(id),
  version_number INT NOT NULL,
  flow_json JSONB NOT NULL,
  published_by_id UUID,
  published_at TIMESTAMP,
  is_published BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(flow_id, version_number)
);

CREATE TABLE flow_triggers (
  id UUID PRIMARY KEY,
  flow_id UUID NOT NULL REFERENCES flows(id),
  trigger_type VARCHAR(50), -- webhook, cron, manual
  trigger_config JSONB,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(flow_id, trigger_type)
);

CREATE TABLE flow_templates (
  id UUID PRIMARY KEY,
  org_id UUID,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(50),
  flow_json JSONB NOT NULL,
  thumbnail_url VARCHAR(500),
  is_public BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(category)
);
```

**API Endpoints**:
- `GET /flows/{botId}` — list flows for bot (draft + versions)
- `POST /flows/{botId}` — create new flow
- `GET /flows/{flowId}` — flow definition
- `PATCH /flows/{flowId}` — update flow draft
- `POST /flows/{flowId}/publish` — validate & publish
- `GET /flows/{flowId}/versions` — version history
- `POST /flows/{flowId}/versions/{versionNum}/rollback` — revert to version
- `POST /flows/{flowId}/clone` — clone flow
- `GET /templates` — list templates
- `POST /flows/{botId}/from-template/{templateId}` — create from template

**Events Published**:
- `flow.published` → {flowId, botId, versionNum, publishedBy}
- `flow.updated` → {flowId, changes}
- `flow.deleted` → {flowId, botId}

**Inter-Service Calls**:
- → Identity Service (validate JWT)
- → Workspace Service (verify bot exists in org)
- ← Runtime Service: Fetch active flow to detect breaking changes on publish

---

### 2.4 Runtime Service (:3004)
**Domain**: Flow execution, session state, node execution  
**Responsibility**:
- Session management (visitor sessions, state persistence)
- Flow interpretation (read flow JSON, execute nodes in sequence)
- Node execution orchestration (route to specific executors)
- Variable management (store user variables, loop context, flow outputs)
- State snapshots (save state after each node for resume/debug)
- Streaming AI token publishing to Centrifugo
- Break/pause/resume logic

**Database Schema** (`runtime_service` schema):
```sql
CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  bot_id UUID NOT NULL,
  org_id UUID NOT NULL,
  visitor_id VARCHAR(255) NOT NULL,
  flow_id UUID NOT NULL,
  current_node_id VARCHAR(255),
  variables JSONB DEFAULT '{}',
  status VARCHAR(50) DEFAULT 'active', -- active, paused, completed, error
  started_at TIMESTAMP DEFAULT NOW(),
  last_activity_at TIMESTAMP DEFAULT NOW(),
  ended_at TIMESTAMP,
  INDEX(org_id, bot_id, visitor_id),
  INDEX(status)
);

CREATE TABLE session_snapshots (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id),
  node_id VARCHAR(255) NOT NULL,
  variables JSONB,
  execution_context JSONB,
  timestamp TIMESTAMP DEFAULT NOW(),
  INDEX(session_id)
);

CREATE TABLE node_executions (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id),
  node_id VARCHAR(255) NOT NULL,
  node_type VARCHAR(50),
  execution_input JSONB,
  execution_output JSONB,
  status VARCHAR(50), -- success, error, skipped
  error_message TEXT,
  duration_ms INT,
  executed_at TIMESTAMP DEFAULT NOW(),
  INDEX(session_id, node_id)
);
```

**API Endpoints**:
- `POST /sessions/{botId}/start` — begin new session (visitor_id in body)
- `POST /sessions/{sessionId}/message` — send user message
- `GET /sessions/{sessionId}` — session state (for debugging)
- `POST /sessions/{sessionId}/pause` — pause execution
- `POST /sessions/{sessionId}/resume` — resume execution
- `POST /sessions/{sessionId}/reset` — reset to start node
- `GET /sessions/{botId}` — list active sessions (auth: bot owner only)

**Events Published**:
- `session.started` → {sessionId, botId, visitorId, orgId}
- `session.message.received` → {sessionId, message, visitorId}
- `session.node.executed` → {sessionId, nodeId, nodeType, result}
- `session.completed` → {sessionId, endedAt, finalVariables}
- `session.error` → {sessionId, nodeId, error}

**Inter-Service Calls**:
- → Flow Service (fetch flow definition on session start)
- → Conversation Service (persist message, query conversation context)
- → Knowledge Service (query KB for RAG context)
- → Integration Service (execute integration node actions)
- → AI Sidecar (call LLM for AI Reply nodes)
- Publishes to Centrifugo (streaming tokens, typing indicator)

---

### 2.5 Conversation Service (:3005)
**Domain**: Conversation transcripts, message persistence, handoff, CRM leads  
**Responsibility**:
- Message persistence (visitor ↔ bot chat, human takeover messages)
- Conversation lifecycle (open → closed/handed off/resolved)
- Leads/CRM (capture lead info, tags, custom fields, pipeline status)
- Conversation search & export
- Lead activity timeline (created, contacted, qualified, etc.)
- Human takeover inbox (agent view, assignment)

**Database Schema** (`conversation_service` schema):
```sql
CREATE TABLE conversations (
  id UUID PRIMARY KEY,
  bot_id UUID NOT NULL,
  org_id UUID NOT NULL,
  visitor_id VARCHAR(255) NOT NULL,
  started_at TIMESTAMP DEFAULT NOW(),
  ended_at TIMESTAMP,
  status VARCHAR(50) DEFAULT 'open', -- open, closed, handed_off, resolved
  assigned_agent_id UUID,
  is_lead BOOLEAN DEFAULT FALSE,
  lead_id UUID,
  INDEX(org_id, bot_id),
  INDEX(status),
  INDEX(visitor_id)
);

CREATE TABLE messages (
  id UUID PRIMARY KEY,
  conversation_id UUID NOT NULL REFERENCES conversations(id),
  sender_type VARCHAR(50), -- visitor, bot, human_agent, system
  sender_id VARCHAR(255),
  sender_name VARCHAR(255),
  content TEXT NOT NULL,
  message_type VARCHAR(50) DEFAULT 'text', -- text, image, file, form_submission
  metadata JSONB, -- citations, referenced node, etc.
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(conversation_id, created_at)
);

CREATE TABLE leads (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  bot_id UUID NOT NULL,
  conversation_id UUID REFERENCES conversations(id),
  email VARCHAR(255),
  phone VARCHAR(20),
  name VARCHAR(255),
  custom_fields JSONB,
  tags TEXT[] DEFAULT '{}',
  status VARCHAR(50) DEFAULT 'new', -- new, contacted, qualified, converted, lost
  pipeline_stage VARCHAR(100),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, email),
  INDEX(status)
);

CREATE TABLE lead_activities (
  id UUID PRIMARY KEY,
  lead_id UUID NOT NULL REFERENCES leads(id),
  activity_type VARCHAR(50), -- contacted, email_sent, call, note, status_changed
  description TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  created_by_id UUID,
  INDEX(lead_id, created_at)
);
```

**API Endpoints**:
- `GET /conversations/{botId}` — list conversations (pagination, filter by status)
- `GET /conversations/{conversationId}` — full transcript
- `GET /conversations/{conversationId}/messages` — paginated messages
- `POST /conversations/{conversationId}/takeover` — agent takeover
- `POST /conversations/{conversationId}/message` — human reply (agent endpoint)
- `POST /conversations/{conversationId}/close` — mark resolved
- `GET /leads` — list leads (filter by status, tag, custom field)
- `POST /leads` — create lead (manual or from conversation)
- `PATCH /leads/{leadId}` — update lead (status, tags, custom fields)
- `GET /leads/{leadId}/activities` — activity timeline
- `POST /leads/{leadId}/activities` — add note/activity
- `GET /conversations/{botId}/export` — export to CSV

**Events Published**:
- `conversation.started` → {conversationId, botId, visitorId}
- `conversation.message.added` → {conversationId, message, senderType}
- `conversation.handoff.requested` → {conversationId, botId}
- `conversation.agent.assigned` → {conversationId, agentId}
- `lead.created` → {leadId, email, name, botId, orgId}
- `lead.updated` → {leadId, changes}
- `lead.status.changed` → {leadId, oldStatus, newStatus}

**Inter-Service Calls**:
- → Identity Service (validate JWT, get agent info)
- ← Runtime Service: Receive session messages
- ← Knowledge Service: Fetch KB citations for messages
- Publishes to Centrifugo (new messages, agent join, assignment)

---

### 2.6 Knowledge Service (:3006)
**Domain**: Knowledge base documents, RAG (retrieval + augmented generation)  
**Responsibility**:
- Document upload (PDF, TXT, MD, HTML, URLs)
- Document parsing & chunking (token-aware, semantic boundaries)
- Embedding (Voyage/OpenAI to Qdrant)
- RAG query (retrieve top-K passages, optional reranking, citations)
- KB ingestion jobs (async via Kafka, progress tracking)
- URL/sitemap scraping
- Hybrid search (dense + sparse + RRF fusion)

**Database Schema** (`knowledge_service` schema):
```sql
CREATE TABLE kb_documents (
  id UUID PRIMARY KEY,
  bot_id UUID NOT NULL,
  org_id UUID NOT NULL,
  document_name VARCHAR(500) NOT NULL,
  document_type VARCHAR(50), -- pdf, txt, url, html
  source_url VARCHAR(1000),
  file_path VARCHAR(1000), -- MinIO/R2 path
  file_size_bytes INT,
  page_count INT,
  status VARCHAR(50) DEFAULT 'pending', -- pending, processing, active, error
  error_message TEXT,
  chunk_count INT DEFAULT 0,
  indexed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, bot_id),
  INDEX(status)
);

CREATE TABLE kb_chunks (
  id UUID PRIMARY KEY,
  kb_document_id UUID NOT NULL REFERENCES kb_documents(id),
  chunk_number INT NOT NULL,
  content TEXT NOT NULL,
  token_count INT,
  metadata JSONB, -- page number, section title, etc.
  qdrant_point_id BIGINT, -- reference to Qdrant point ID
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(kb_document_id, chunk_number)
);

CREATE TABLE kb_ingestion_jobs (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  kb_document_id UUID REFERENCES kb_documents(id),
  job_type VARCHAR(50), -- parse, embed, reindex
  status VARCHAR(50) DEFAULT 'pending', -- pending, processing, completed, failed
  progress_percent INT DEFAULT 0,
  error_message TEXT,
  started_at TIMESTAMP,
  completed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, status)
);
```

**API Endpoints**:
- `GET /bots/{botId}/kb/documents` — list KB documents
- `POST /bots/{botId}/kb/upload` — upload file
- `POST /bots/{botId}/kb/scrape-url` — scrape single URL
- `POST /bots/{botId}/kb/scrape-sitemap` — scrape sitemap.xml
- `DELETE /bots/{botId}/kb/documents/{docId}` — delete document
- `GET /bots/{botId}/kb/jobs` — ingestion job status
- `POST /bots/{botId}/kb/query` — RAG query (retrieve + rerank)
- `POST /bots/{botId}/kb/reindex` — reindex all documents

**Qdrant Integration**:
- Collection per bot: `qdrant_bot_{botId}`
- Payload: `{doc_id, chunk_number, content, metadata}`
- Vector dimensions: 1536 (Voyage 3-lite or OpenAI)
- Hybrid search: dense vector + BM25 sparse + RRF fusion

**Events Published**:
- `kb.document.uploaded` → {docId, botId, orgId, fileName}
- `kb.ingestion.started` → {jobId, docId, botId}
- `kb.ingestion.completed` → {jobId, docId, chunkCount}
- `kb.ingestion.failed` → {jobId, docId, error}
- `kb.reindexed` → {botId, totalChunks}

**Inter-Service Calls**:
- → Identity Service (validate JWT)
- ← Runtime Service: Accept RAG query from AI Reply nodes
- → AI Sidecar (embedding, reranking)
- Publishes to Centrifugo (ingestion progress for dashboard live updates)

---

### 2.7 Analytics Service (:3007)
**Domain**: Event aggregation, metrics, reporting, dashboards  
**Responsibility**:
- Event ingestion from Kafka (all services)
- Metrics aggregation (conversations/day, response time, cost, satisfaction)
- Dashboard queries (real-time + historical)
- CSV/PDF exports
- Funnel & cohort analysis (optional, Phase 2)
- Cost tracking (per-bot, per-conversation, LLM tokens)

**Database Schema** (`analytics_service` schema):
```sql
CREATE TABLE analytics_events (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  bot_id UUID,
  event_type VARCHAR(100), -- conversation.created, message.sent, api.call, etc.
  event_data JSONB,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, bot_id, created_at)
);

CREATE TABLE daily_metrics (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  bot_id UUID NOT NULL,
  date DATE NOT NULL,
  conversation_count INT DEFAULT 0,
  unique_visitors INT DEFAULT 0,
  message_count INT DEFAULT 0,
  api_calls INT DEFAULT 0,
  kb_queries INT DEFAULT 0,
  avg_response_time_ms FLOAT,
  handoff_count INT DEFAULT 0,
  lead_count INT DEFAULT 0,
  cost_usd FLOAT DEFAULT 0,
  satisfaction_score FLOAT,
  INDEX(org_id, bot_id, date)
);

CREATE TABLE conversation_costs (
  id UUID PRIMARY KEY,
  conversation_id UUID NOT NULL,
  bot_id UUID NOT NULL,
  org_id UUID NOT NULL,
  llm_tokens INT DEFAULT 0,
  llm_cost_usd FLOAT DEFAULT 0,
  embedding_calls INT DEFAULT 0,
  embedding_cost_usd FLOAT DEFAULT 0,
  total_cost_usd FLOAT DEFAULT 0,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, bot_id, created_at)
);
```

**API Endpoints**:
- `GET /dashboard/summary` — high-level metrics (today, this week, this month)
- `GET /dashboard/conversations` — conversation count trend
- `GET /dashboard/cost` — cost breakdown (LLM, embedding)
- `GET /dashboard/funnel` — message count, handoff rate, lead conversion
- `GET /dashboard/top-intents` — most common user intents (from transcript analysis)
- `GET /export/conversations` — CSV export
- `GET /export/costs` — cost report

**Events Consumed from Kafka**:
- `conversation.started`, `conversation.completed`, `conversation.message.added`
- `session.node.executed`, `session.error`
- `kb.query`, `kb.ingestion.completed`
- `billing.charge`, `billing.overage`
- All service events with `created_at` timestamp

**Inter-Service Calls**:
- ← Kafka (consume all events)
- → Langfuse (cost tracking for LLM calls)

---

### 2.8 Billing Service (:3008)
**Domain**: Subscriptions, Stripe integration, usage metering, invoicing  
**Responsibility**:
- Subscription management (create, update, cancel)
- Stripe webhook handling (payment success, failure, invoice)
- Usage metering (conversation count, API calls, KB ingestion)
- Overage charging (if applicable)
- Invoice generation & archival
- Trial period management
- Plan feature enforcement (rate limits, bot count, etc.)

**Database Schema** (`billing_service` schema):
```sql
CREATE TABLE plans (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL, -- free, pro, business, enterprise
  price_usd FLOAT DEFAULT 0,
  billing_period VARCHAR(50) DEFAULT 'monthly', -- monthly, annual
  features JSONB, -- max_bots, max_conversations, api_calls, etc.
  stripe_product_id VARCHAR(255),
  stripe_price_id VARCHAR(255),
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE subscriptions (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL UNIQUE,
  plan_id UUID NOT NULL REFERENCES plans(id),
  stripe_subscription_id VARCHAR(255),
  stripe_customer_id VARCHAR(255),
  status VARCHAR(50) DEFAULT 'active', -- active, past_due, canceled, trialing
  current_period_start DATE,
  current_period_end DATE,
  trial_end DATE,
  cancel_at_period_end BOOLEAN DEFAULT FALSE,
  auto_renew BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id)
);

CREATE TABLE usage_records (
  id UUID PRIMARY KEY,
  subscription_id UUID NOT NULL REFERENCES subscriptions(id),
  org_id UUID NOT NULL,
  metric_name VARCHAR(100), -- conversation_count, api_calls, kb_ingestion
  value FLOAT NOT NULL,
  period_start DATE,
  period_end DATE,
  recorded_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, metric_name, recorded_at)
);

CREATE TABLE invoices (
  id UUID PRIMARY KEY,
  subscription_id UUID NOT NULL REFERENCES subscriptions(id),
  stripe_invoice_id VARCHAR(255),
  amount_usd FLOAT,
  status VARCHAR(50), -- draft, open, paid, uncollectible, void
  issued_at TIMESTAMP,
  due_at TIMESTAMP,
  paid_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(subscription_id)
);
```

**API Endpoints**:
- `GET /subscription` — current subscription
- `POST /subscription/upgrade/{planId}` — change plan
- `POST /subscription/cancel` — cancel subscription
- `GET /billing/usage` — current month usage
- `GET /billing/invoices` — invoice list
- `POST /billing/payment-method` — add/update payment method
- `POST /webhook/stripe` — Stripe event handler (private)

**Events Published**:
- `billing.subscription.created` → {subscriptionId, orgId, planId}
- `billing.subscription.updated` → {subscriptionId, planId}
- `billing.subscription.canceled` → {subscriptionId, orgId}
- `billing.invoice.issued` → {invoiceId, amount}
- `billing.overage.charged` → {subscriptionId, metricName, amount}

**Inter-Service Calls**:
- → Identity Service (validate JWT, get org context)
- → Workspace Service (enforce plan limits on bot creation, API calls)
- ← Analytics Service: Consume usage events
- Stripe API (external): Create/update/cancel subscriptions, handle webhooks

---

### 2.9 Integration Service (:3009)
**Domain**: Integration connectors (20 types), marketplace, OAuth callbacks  
**Responsibility**:
- IntegrationConnector plugin discovery & registration
- OAuth callback handling (redirect from providers)
- Credential encryption & storage (asymmetric in DB)
- Action execution (call connector methods)
- Marketplace metadata (display integrations, auth requirements)
- Test connection validation

**Database Schema** (`integration_service` schema):
```sql
CREATE TABLE integrations (
  id UUID PRIMARY KEY,
  org_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(100) NOT NULL, -- slack, gmail, hubspot, etc.
  display_config JSONB, -- color, icon, category
  status VARCHAR(50) DEFAULT 'active',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX(org_id, type)
);

CREATE TABLE integration_credentials (
  id UUID PRIMARY KEY,
  integration_id UUID NOT NULL REFERENCES integrations(id),
  credential_type VARCHAR(50), -- api_key, oauth_token, webhook_secret
  encrypted_value TEXT NOT NULL, -- AES-256 encrypted
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX(integration_id)
);

CREATE TABLE integration_actions_log (
  id UUID PRIMARY KEY,
  integration_id UUID NOT NULL REFERENCES integrations(id),
  action_name VARCHAR(100),
  action_params JSONB,
  result JSONB,
  status VARCHAR(50), -- success, error
  error_message TEXT,
  duration_ms INT,
  executed_at TIMESTAMP DEFAULT NOW(),
  INDEX(integration_id, executed_at)
);

CREATE TABLE integration_oauth_state (
  id UUID PRIMARY KEY,
  state_token VARCHAR(255) UNIQUE NOT NULL,
  org_id UUID NOT NULL,
  integration_type VARCHAR(100) NOT NULL,
  return_url VARCHAR(1000),
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP DEFAULT NOW() + INTERVAL '10 minutes'
);
```

**API Endpoints**:
- `GET /integrations/marketplace` — list all available integrations
- `GET /integrations/{orgId}` — list connected integrations
- `POST /integrations/{orgId}/{type}/auth` — initiate OAuth or API key entry
- `GET /integrations/oauth/callback` — OAuth callback (public)
- `POST /integrations/{integrationId}/test` — test connection
- `DELETE /integrations/{integrationId}` — disconnect integration
- `POST /integrations/{integrationId}/action/{actionName}` — execute action (internal)

**20 Connectors Included**:
1. Slack (send messages, create threads)
2. Gmail (send email, create draft)
3. HubSpot (create contact, update deal)
4. Salesforce (create lead, update opportunity)
5. Stripe (create customer, charge card, fetch invoice)
6. Shopify (check order status, create product)
7. Twilio (send SMS)
8. SendGrid (send email)
9. Discord (send message to channel)
10. Microsoft Teams (send message)
11. Notion (create page, append block)
12. Google Sheets (append row, update cell)
13. GitHub (create issue, add comment)
14. Linear (create issue, update status)
15. Jira (create ticket, update status)
16. Airtable (create record, update record)
17. Mailchimp (add subscriber to list)
18. Mixpanel (track event)
19. Segment (track event)
20. Make.com / Zapier (webhook POST to scenario)

**Events Published**:
- `integration.connected` → {integrationId, orgId, type}
- `integration.disconnected` → {integrationId, orgId}
- `integration.action.executed` → {integrationId, action, result}
- `integration.error` → {integrationId, error}

**Inter-Service Calls**:
- → Identity Service (validate JWT, get org)
- ← Runtime Service: Accept integration node execution requests
- External APIs: Slack, Gmail, HubSpot, etc. (via connector implementations)

---

### 2.10–2.12 Supporting Services (No Changes)

**threadly-ai** (FastAPI, Python 8001):
- Stays external to Spring services
- Called via HTTP POST from Runtime Service (AI Reply node)
- Embeds context from Knowledge Service
- Langfuse integration for LLM tracing

**threadly-web** (Next.js, port 3000):
- Updated to call Nginx gateway instead of Spring Boot directly
- All API calls routed via Kong/Nginx to appropriate microservice

**threadly-widget** (Preact, CDN):
- Unchanged; still connects to Centrifugo via JWT from Identity Service
- Runtime Service publishes session events to Centrifugo

---

## 3. Kafka Topics & Event Schema

**Topic: `user-events`**
```json
{
  "event_type": "user.created | user.email_verified",
  "user_id": "uuid",
  "email": "user@example.com",
  "org_id": "uuid",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `org-events`**
```json
{
  "event_type": "org.created | membership.updated",
  "org_id": "uuid",
  "user_id": "uuid",
  "role": "owner | admin | member",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `flow-events`**
```json
{
  "event_type": "flow.published | flow.updated | flow.deleted",
  "flow_id": "uuid",
  "bot_id": "uuid",
  "org_id": "uuid",
  "version": 3,
  "changes": {"nodeAdded": "ai-reply-5", "edgeRemoved": "edge-12"},
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `session-events`**
```json
{
  "event_type": "session.started | session.message.received | session.node.executed | session.completed | session.error",
  "session_id": "uuid",
  "bot_id": "uuid",
  "org_id": "uuid",
  "visitor_id": "string",
  "node_id": "string (if applicable)",
  "message": "user text (if applicable)",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `conversation-events`**
```json
{
  "event_type": "conversation.started | conversation.message.added | conversation.handoff.requested | conversation.closed",
  "conversation_id": "uuid",
  "bot_id": "uuid",
  "org_id": "uuid",
  "visitor_id": "string",
  "message_id": "uuid (if applicable)",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `kb-events`**
```json
{
  "event_type": "kb.document.uploaded | kb.ingestion.started | kb.ingestion.completed | kb.ingestion.failed",
  "bot_id": "uuid",
  "org_id": "uuid",
  "document_id": "uuid",
  "chunk_count": 150,
  "error": "string (if failed)",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `analytics-events`**
```json
{
  "event_type": "metric.recorded",
  "org_id": "uuid",
  "bot_id": "uuid (optional)",
  "metric_name": "conversation_count | response_time_ms | llm_tokens",
  "value": 42,
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `billing-events`**
```json
{
  "event_type": "billing.subscription.created | billing.invoice.issued | billing.overage.charged",
  "subscription_id": "uuid",
  "org_id": "uuid",
  "amount_usd": 29.00,
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Topic: `integration-events`**
```json
{
  "event_type": "integration.connected | integration.action.executed | integration.error",
  "integration_id": "uuid",
  "org_id": "uuid",
  "action_name": "send_message",
  "status": "success | error",
  "timestamp": "2025-05-24T10:30:00Z"
}
```

---

## 4. Inter-Service Communication Patterns

### 4.1 Synchronous (REST + Feign Client)

**Pattern**: Feign declarative HTTP client for Spring services  
**When to use**: Immediate response needed, strong consistency required

**Example: Runtime Service calls Flow Service to fetch flow definition**

```java
// flow-service-client.jar (shared JAR in threadly-common-spring)
@FeignClient(name = "flow-service", url = "http://flow-service:3003")
public interface FlowServiceClient {
  @GetMapping("/flows/{flowId}")
  FlowDTO getFlow(@PathVariable UUID flowId, @RequestHeader("Authorization") String token);
}

// In runtime-service
@Service
class RuntimeService {
  @Autowired FlowServiceClient flowClient;
  
  public void executeFlow(UUID flowId, Session session) {
    FlowDTO flow = flowClient.getFlow(flowId, getToken()); // Feign adds auth header
    // Execute flow...
  }
}
```

**Services using REST Feign clients**:
- Runtime → Flow Service (fetch flow)
- Runtime → Conversation Service (persist message)
- Runtime → Knowledge Service (RAG query)
- Runtime → Integration Service (execute action)
- Conversation → Knowledge Service (fetch citations)
- Billing → Workspace Service (check plan limits)
- Analytics → Conversation Service (query data)

### 4.2 Asynchronous (Kafka Events)

**Pattern**: Transactional outbox + event publishing  
**When to use**: Loose coupling, eventual consistency acceptable, fan-out needed

**Example: Session completes, publish event to Kafka**

```java
// In runtime-service
@Transactional
public void completeSession(Session session) {
  session.setStatus("completed");
  sessionRepository.save(session);
  
  // Outbox entry (same transaction)
  outboxService.publish(new OutboxEvent()
    .setEventType("session.completed")
    .setAggregateId(session.getId())
    .setPayload(Map.of(
      "sessionId", session.getId(),
      "botId", session.getBotId(),
      "orgId", session.getOrgId()
    ))
  );
}

// Poller (Spring @Scheduled or Kafka Connect Debezium)
@Scheduled(fixedDelay = 5000) // every 5 seconds
public void publishOutboxEvents() {
  List<OutboxEvent> unpublished = outboxRepository.findByPublishedAtIsNull();
  for (OutboxEvent event : unpublished) {
    kafkaTemplate.send("session-events", event.getPayload());
    event.setPublishedAt(now());
    outboxRepository.save(event);
  }
}
```

**Kafka consumers (subscribers)**:
- Analytics Service: consume all events (for daily_metrics aggregation)
- Billing Service: consume `conversation-events` (usage metering)
- Conversation Service: consume `session-events` (update transcript with final status)

### 4.3 Service Discovery (Consul/Eureka)

**Development (Docker Compose)**:
```yaml
services:
  consul:
    image: consul:latest
    ports:
      - "8500:8500"
      - "8600:8600/udp"
    environment:
      - CONSUL_BIND_INTERFACE=eth0

  identity-service:
    image: threadly/identity-service:latest
    environment:
      - SPRING_CLOUD_CONSUL_HOST=consul
      - SPRING_CLOUD_CONSUL_PORT=8500
      - SPRING_APPLICATION_NAME=identity-service
```

**Feign client discovery**:
```java
@FeignClient(name = "flow-service") // Automatically resolved via Consul
public interface FlowServiceClient {
  @GetMapping("/flows/{flowId}")
  FlowDTO getFlow(@PathVariable UUID flowId);
}
```

**Production (Kubernetes)**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: flow-service
spec:
  selector:
    app: flow-service
  ports:
    - port: 80
      targetPort: 3003
```

Feign auto-resolves to `http://flow-service` (Kubernetes DNS).

---

## 5. API Gateway (Nginx)

**Purpose**: Single entry point, rate limiting, auth validation, routing, CORS, SSL termination

**Nginx Configuration** (`infra/nginx/nginx.conf`):

```nginx
upstream identity_service {
  server identity-service:3001;
}

upstream workspace_service {
  server workspace-service:3002;
}

upstream flow_service {
  server flow-service:3003;
}

upstream runtime_service {
  server runtime-service:3004;
}

upstream conversation_service {
  server conversation-service:3005;
}

upstream knowledge_service {
  server knowledge-service:3006;
}

upstream analytics_service {
  server analytics-service:3007;
}

upstream billing_service {
  server billing-service:3008;
}

upstream integration_service {
  server integration-service:3009;
}

upstream threadly_ai {
  server threadly-ai:8001;
}

upstream threadly_web {
  server threadly-web:3000;
}

server {
  listen 8080;
  server_name _;

  # Rate limiting
  limit_req_zone $binary_remote_addr zone=general:10m rate=100r/s;
  limit_req_zone $binary_remote_addr zone=auth:10m rate=5r/m;
  limit_req_zone $binary_remote_addr zone=widget:10m rate=1000r/s;

  # CORS headers
  add_header 'Access-Control-Allow-Origin' '$http_origin' always;
  add_header 'Access-Control-Allow-Credentials' 'true' always;
  add_header 'Access-Control-Allow-Methods' 'GET, POST, PATCH, DELETE, OPTIONS' always;
  add_header 'Access-Control-Allow-Headers' 'Content-Type, Authorization' always;

  # Auth endpoints
  location ~ ^/auth/ {
    limit_req zone=auth burst=10 nodelay;
    proxy_pass http://identity_service;
  }

  # Workspace endpoints
  location ~ ^/(orgs|bots|workspace)/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://workspace_service;
  }

  # Flow endpoints
  location ~ ^/flows/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://flow_service;
  }

  # Runtime endpoints (widget, session management)
  location ~ ^/(sessions|realtime)/ {
    limit_req zone=widget burst=50 nodelay;
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://runtime_service;
  }

  # Conversation endpoints
  location ~ ^/conversations/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://conversation_service;
  }

  # Knowledge endpoints
  location ~ ^/kb/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://knowledge_service;
  }

  # Analytics endpoints
  location ~ ^/dashboard/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://analytics_service;
  }

  # Billing endpoints
  location ~ ^/billing/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://billing_service;
  }

  # Integrations endpoints
  location ~ ^/integrations/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://integration_service;
  }

  # AI sidecar endpoints (internal)
  location ~ ^/ai/ {
    proxy_set_header Authorization $http_authorization;
    proxy_pass http://threadly_ai;
  }

  # Frontend
  location ~ ^/(app|builder|dashboard) {
    proxy_pass http://threadly_web;
  }

  # Root redirect
  location / {
    proxy_pass http://threadly_web;
  }

  # Health check
  location /health {
    access_log off;
    return 200 "OK\n";
  }
}
```

---

## 6. Shared Libraries (threadly-common-spring)

**Minimal shared starter** to avoid tight coupling:

```java
// pom.xml (threadly-common-spring)
<dependency>
  <groupId>dev.threadly</groupId>
  <artifactId>threadly-common-spring</artifactId>
  <version>1.0.0</version>
</dependency>

// Included in threadly-common-spring:
// 1. TenantContext + TenantFilter (org_id injection)
// 2. ErrorModel (RFC 7807 Problem+JSON)
// 3. AuthenticationPrincipal extraction utilities
// 4. Feign + Resilience4j configuration
// 5. OpenTelemetry auto-configuration
// 6. Idempotency-Key handling
```

---

## 7. Data Consistency & Migration Strategy

### 7.1 Data Migration (Monolith → Microservices)

**Phase 1** (week 1): Deploy services alongside monolith in "shadow mode" (read-only, no writes)
```sql
-- Copy data from monolith to new service schemas
INSERT INTO identity_service.users 
  SELECT * FROM public.users;
INSERT INTO workspace_service.bots 
  SELECT * FROM public.bots;
-- ... etc for all entities
```

**Phase 2** (week 2): Enable dual-writes (new services write alongside monolith)
- Spring Boot interceptor: every write hits monolith + new service in parallel
- If new service write fails, rollback and log (eventual consistency)
- Analytics Service monitors "write lag" metric

**Phase 3** (week 3): Cutover (all reads/writes → microservices)
- Update Nginx routing to skip monolith
- Monolith enters "maintenance mode" (read-only, fallback only)
- Keep monolith running for 2 weeks for safe rollback

### 7.2 Cross-Service Transactions

**Pattern: Saga (orchestrated via Kafka events)**

Example: Conversation handoff triggers billing overage check + subscription update

```
1. Conversation Service: emit "conversation.handoff.requested"
2. Billing Service subscribes, checks plan limits
   - If within limit: emit "billing.overage.allowed"
   - If exceeded: emit "billing.overage.blocked"
3. Conversation Service subscribes to both outcomes
   - On "allowed": proceed with handoff
   - On "blocked": return error to user (eventual consistency)
```

---

## 8. Deployment Topology

### 8.1 Local Development (Docker Compose)

```yaml
version: '3.9'
services:
  consul:
    image: consul:latest
    ports:
      - "8500:8500"

  nginx:
    image: nginx:alpine
    ports:
      - "8080:8080"
    volumes:
      - ./infra/nginx/nginx.conf:/etc/nginx/nginx.conf:ro

  postgres:
    image: postgres:16
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=threadly
      - POSTGRES_USER=threadly
      - POSTGRES_PASSWORD=dev

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092

  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"

  identity-service:
    build: ./threadly-core
    ports:
      - "3001:3001"
    environment:
      - SPRING_CLOUD_CONSUL_HOST=consul
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/threadly
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092

  # ... (repeat for all 8 Java services)

  threadly-ai:
    build: ./threadly-ai
    ports:
      - "8001:8001"
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092

  threadly-web:
    build: ./threadly-web
    ports:
      - "3000:3000"

volumes:
  postgres_data:
```

**Start stack**: `make up` (uses docker-compose + mprocs for local dev)

### 8.2 Production (Kubernetes)

```yaml
---
apiVersion: v1
kind: Namespace
metadata:
  name: threadly

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-init
  namespace: threadly
data:
  init.sql: |
    CREATE SCHEMA IF NOT EXISTS identity_service;
    CREATE SCHEMA IF NOT EXISTS workspace_service;
    -- ... (all 9 schemas)

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: identity-service
  namespace: threadly
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: identity-service
        image: threadly/identity-service:latest
        ports:
        - containerPort: 3001
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres-service:5432/threadly
        - name: SPRING_CLOUD_CONSUL_HOST
          value: consul-service
        livenessProbe:
          httpGet:
            path: /health
            port: 3001
          initialDelaySeconds: 30

---
apiVersion: v1
kind: Service
metadata:
  name: identity-service
  namespace: threadly
spec:
  selector:
    app: identity-service
  ports:
  - port: 80
    targetPort: 3001
```

---

## 9. Migration Checklist (2-Week Monolith Deprecation)

- [ ] **Week 1**: Deploy all 9 services (shadow mode, read-only)
- [ ] **Week 1**: Run integration tests (monolith + microservices in parallel)
- [ ] **Week 1**: Verify data consistency (monolith ↔ services)
- [ ] **Week 2**: Enable dual-writes (monolith + service)
- [ ] **Week 2**: Monitor write lag & consistency errors
- [ ] **Week 2**: Switch routing (Nginx → services, skip monolith)
- [ ] **Week 3**: Keep monolith in maintenance mode (read-only fallback)
- [ ] **Week 4**: Decommission monolith (after 2-week observation)

---

## 10. Observability & Tracing

**OpenTelemetry Setup** (all services):

```java
// pom.xml (every service)
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

// application.yml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    metrics:
      export:
        enabled: true
        endpoint: http://otel-collector:4317
    tracing:
      endpoint: http://otel-collector:4317
```

**One Trace ID Across All Hops**:
1. Widget sends message to Runtime Service (header: `traceparent: 00-{traceId}-{spanId}-01`)
2. Runtime Service adds span, calls Knowledge Service (same `traceparent`)
3. Knowledge Service calls AI sidecar (same `traceparent`)
4. AI sidecar returns, Knowledge Service publishes to Kafka with `traceId`
5. Analytics Service consumes Kafka event, continues same trace

Result: Single trace ID visible in Honeycomb/Tempo spanning all 5 hops.

---

## 11. Success Criteria

✅ **By end of week 3**:
- All 9 Java services live and receiving traffic
- Kong/Nginx routes 100% of requests
- Monolith in maintenance mode (read-only)
- 0 data loss during migration
- OpenTelemetry traces flowing to Honeycomb (one ID per user request)
- Kafka topics healthy, consumers lag < 30s
- Load test: 500 concurrent sessions sustained on services (same CPU as monolith)
- CI/CD deploys individual services (not monolith)
- Runbook documented for emergency rollback to monolith
