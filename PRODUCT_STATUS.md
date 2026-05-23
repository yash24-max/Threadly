# Threadly — Product Status

> Master tracker · Updated: 2026-05-23 · Phase 0 MVP + Sprint 2

---

## Sprint 2 Progress

| Agent | Domain | Tasks | Status |
|-------|--------|-------|--------|
| Backend Agent | RBAC, API Keys, Webhooks, Analytics, Credentials, Rate Limiting | 13/13 | ✅ Complete |
| AI+Widget Agent | Multi-provider LLM, Hybrid RAG, Cost Tracking, Widget Rich Messages | 15/15 | ✅ Complete |
| Frontend Agent | Node catalog, PropertiesPanel, Analytics page, Inbox redesign, Onboarding | 0/8 | 🔄 In Progress |
| Testing Agent | JUnit5, Testcontainers, Pytest, Playwright E2E, Vitest | 0/12 | 🔄 In Progress |
| Tech Lead Agent | FlowSchemaValidator, Audit Log, Flyway V6, Railway, Cloudflare Worker | 0/14 | 🔄 In Progress |
| PM Agent | PRODUCT_STATUS, AGENTS, CHANGELOG, SPRINT, FEATURES | 0/5 | 🔄 In Progress |

---

## Feature Checklist

### Phase 0 Foundation (Sprint 1) — All Complete ✅

- ✅ Spring Boot 3.3 modular monolith (11 modules), Java 21
- ✅ JWT RS256 auth — signup / login / refresh / /me
- ✅ Org multi-tenancy (Hibernate `@Filter`, `TenantContext` ThreadLocal)
- ✅ Bot CRUD + embed snippet generation
- ✅ Visual flow builder (React Flow v12, custom node types)
- ✅ 9 node executors: Message, Question, AiReply, Condition, Handoff, End, ApiCall, SetVariable + template engine
- ✅ Flow runtime engine + `FlowGraph` traversal
- ✅ Redis session state (`Session.variables` JSONB)
- ✅ Centrifugo v5 realtime (publish, proxy hooks, circuit breaker)
- ✅ Conversation store + message transcript
- ✅ Human handoff (Take Over / Resume AI)
- ✅ Knowledge base upload + RAG (Qdrant dense search)
- ✅ Embeddable Preact widget (IIFE, < 35 KB gzip target)
- ✅ Dashboard with live stat counters (SSE)
- ✅ Docker Compose with 10 services (Postgres, Redis, Qdrant, MinIO, Centrifugo, Prometheus, Grafana, Core, AI, Web)
- ✅ Flyway V1 migration (full schema)
- ✅ GitHub Actions CI pipeline

### Sprint 2 — Backend Agent ✅

- ✅ Team Management RBAC (invite / role update / remove, `OrgMembership` entity)
- ✅ API Keys (`tly_live_` prefix, BCrypt hash, `JwtAuthFilter` integration)
- ✅ Webhook Triggers (HMAC-SHA256 `X-Threadly-Signature`, Resilience4j retry)
- ✅ Flow Import/Export (JSON download/upload endpoints)
- ✅ Delay node executor
- ✅ Switch node executor
- ✅ SendEmail node executor
- ✅ CollectInput node executor
- ✅ Conversation bulk-close + bulk-assign
- ✅ Conversation CSV export
- ✅ Per-bot analytics API (summary, daily, funnel)
- ✅ Credentials Store (AES-256-GCM, PBKDF2 key derivation)
- ✅ SecurityHeadersFilter (CSP, HSTS, X-Frame-Options)
- ✅ RateLimitFilter (Bucket4j + Redis, 10/min auth, 1000/min per org)
- ✅ Flyway V2 (memberships + api_keys)
- ✅ Flyway V3 (webhooks)
- ✅ Flyway V4 (credentials)
- ✅ Flyway V5 (events)

### Sprint 2 — AI+Widget Agent ✅

- ✅ `AnthropicProvider` streaming (claude-3-5-sonnet)
- ✅ `OpenAIProvider` fallback streaming (gpt-4o)
- ✅ `ProviderChain` auto-fallback logic
- ✅ Cost tracking per LLM call (`COSTS` table, $/token input+output)
- ✅ Hybrid RAG: dense Qdrant + sparse BM25 + RRF fusion
- ✅ Cohere reranking (optional)
- ✅ Citation formatting `[1][2]` inline with `Citation` model
- ✅ `/ai/summarize` route
- ✅ `/ai/suggest-replies` route
- ✅ `/ai/extract-entities` route
- ✅ `/ai/classify-intent` route
- ✅ Langfuse tracing on every LLM call
- ✅ Improved ingestion: 512-token chunks, 50-token overlap, progress callbacks
- ✅ Widget: buttons, cards, quick replies, file attachments (rich messages)
- ✅ Widget: XHR file upload with progress bar
- ✅ Widget: unread badge + Web Audio notification sound
- ✅ Widget: message timestamps (HH:mm on hover) + delivery status (✓/✓✓)
- ✅ Widget: conversation persistence (sessionStorage, max 200 messages)
- ✅ Widget: full theme system (12 CSS custom properties, dark mode auto/light/dark)

### Sprint 2 — Frontend Agent 🔄

- 🔄 n8n-inspired node catalog (categorized, searchable)
- 🔄 PropertiesPanel with 12 input types (text, number, select, code, condition, etc.)
- 🔄 Analytics page (charts, funnel, cost per conversation)
- 🔄 Conversation inbox redesign (3-pane with virtual scrolling)
- 🔄 Onboarding wizard (create bot → install snippet → go live)
- 🔄 Credentials manager UI
- 🔄 Command palette (Cmd+K) enhancements
- 🔄 Theme preview live panel

### Sprint 2 — Testing Agent 🔄

- 🔄 JUnit5 + Testcontainers: auth flow tests
- 🔄 JUnit5 + Testcontainers: bot CRUD tests
- 🔄 JUnit5 + Testcontainers: flow publish tests
- 🔄 JUnit5 + Testcontainers: conversation tests
- 🔄 JUnit5 + Testcontainers: tenant isolation tests
- 🔄 Pytest: AI route tests (complete, summarize, RAG)
- 🔄 Playwright E2E: auth (signup/login/logout)
- 🔄 Playwright E2E: builder (create flow, publish)
- 🔄 Playwright E2E: conversations (send message, handoff)
- 🔄 Playwright E2E: knowledge base (upload, ingest, query)
- 🔄 Vitest: widget unit tests (theme, ws-client, message rendering)
- 🔄 Vitest: widget integration tests

### Sprint 2 — Tech Lead Agent 🔄

- 🔄 `FlowSchemaValidator` (strict JSON Schema validation on publish)
- 🔄 Enhanced `ConditionNodeExecutor` (12 operators: eq, neq, gt, lt, gte, lte, contains, startsWith, endsWith, in, notIn, regex)
- 🔄 Core↔AI request signing (HMAC header on all internal calls)
- 🔄 `AuditLog` entity + interceptor (who changed what, when)
- 🔄 Flyway V6 (audit_log table)
- 🔄 Railway config (`railway.toml`, environment variable mapping)
- 🔄 Cloudflare Worker (widget CDN edge caching)
- 🔄 Grafana dashboard for production metrics
- 🔄 `ThreadlyMetrics` (custom Micrometer meters)
- 🔄 Production `application.yml` profile

---

## API Inventory

### Auth — `/v1/auth`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/v1/auth/signup` | ✅ | Returns JWT + org |
| POST | `/v1/auth/login` | ✅ | Returns JWT + refresh |
| POST | `/v1/auth/refresh` | ✅ | Rotates refresh token |
| POST | `/v1/auth/logout` | ✅ | Revokes refresh token |
| GET | `/v1/auth/me` | ✅ | Current user + org |

### Bots — `/v1/bots`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/bots` | ✅ | List bots for org |
| POST | `/v1/bots` | ✅ | Create bot |
| GET | `/v1/bots/{id}` | ✅ | Get bot details |
| PUT | `/v1/bots/{id}` | ✅ | Update bot config |
| DELETE | `/v1/bots/{id}` | ✅ | Delete bot |
| PATCH | `/v1/bots/{id}/theme` | ✅ | Update theme config |
| GET | `/v1/bots/{id}/snippet` | ✅ | Get embed HTML snippet |

### Flow — `/v1/bots/{id}/flow`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/bots/{id}/flow/draft` | ✅ | Get draft flow JSON |
| PUT | `/v1/bots/{id}/flow/draft` | ✅ | Save draft (autosave) |
| POST | `/v1/bots/{id}/flow/publish` | ✅ | Publish draft → live |
| POST | `/v1/bots/{id}/flow/rollback` | ✅ | Rollback to version |
| GET | `/v1/bots/{id}/flow/versions` | ✅ | List versions |
| GET | `/v1/bots/{id}/flow/export` | ✅ | Download flow JSON |
| POST | `/v1/bots/{id}/flow/import` | ✅ | Upload flow JSON |

### Conversations — `/v1/conversations`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/conversations` | ✅ | List with filters |
| GET | `/v1/conversations/{id}` | ✅ | Get conversation |
| GET | `/v1/conversations/{id}/messages` | ✅ | Get transcript |
| POST | `/v1/conversations/{id}/handoff` | ✅ | Request handoff |
| POST | `/v1/conversations/{id}/takeover` | ✅ | Agent takes over |
| POST | `/v1/conversations/{id}/resume` | ✅ | Resume AI |
| POST | `/v1/conversations/{id}/close` | ✅ | Close conversation |
| POST | `/v1/conversations/{id}/messages` | ✅ | Agent reply |
| POST | `/v1/conversations/bulk-close` | ✅ | Bulk close |
| POST | `/v1/conversations/bulk-assign` | ✅ | Bulk assign |
| GET | `/v1/conversations/export` | ✅ | CSV export |

### Knowledge Base — `/v1/bots/{id}/kb`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/v1/bots/{id}/kb/upload` | ✅ | Upload file (multipart) |
| POST | `/v1/bots/{id}/kb/url` | ✅ | Add URL for ingestion |
| GET | `/v1/bots/{id}/kb` | ✅ | List documents |
| DELETE | `/v1/bots/{id}/kb/{docId}` | ✅ | Delete document |

### Analytics — `/v1/analytics`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/analytics/summary` | ✅ | Total messages, p50, handoffs |
| GET | `/v1/analytics/daily` | ✅ | Daily message counts |
| GET | `/v1/analytics/funnel` | ✅ | Conversation funnel |
| GET | `/v1/analytics/live` | ✅ | SSE live counters |
| GET | `/v1/bots/{id}/analytics/summary` | ✅ | Per-bot summary |
| GET | `/v1/bots/{id}/analytics/daily` | ✅ | Per-bot daily |
| GET | `/v1/bots/{id}/analytics/funnel` | ✅ | Per-bot funnel |

### Team Management — `/v1/team`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/team/members` | ✅ | List org members |
| POST | `/v1/team/invite` | ✅ | Send invite by email |
| PUT | `/v1/team/members/{id}/role` | ✅ | Update member role |
| DELETE | `/v1/team/members/{id}` | ✅ | Remove member |

### API Keys — `/v1/api-keys`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/api-keys` | ✅ | List keys (hashed) |
| POST | `/v1/api-keys` | ✅ | Create key (returns plaintext once) |
| DELETE | `/v1/api-keys/{id}` | ✅ | Revoke key |

### Webhooks — `/v1/webhooks`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/webhooks` | ✅ | List webhook endpoints |
| POST | `/v1/webhooks` | ✅ | Register endpoint |
| PUT | `/v1/webhooks/{id}` | ✅ | Update endpoint |
| DELETE | `/v1/webhooks/{id}` | ✅ | Delete endpoint |

### Credentials — `/v1/credentials`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/v1/credentials` | ✅ | List credential names (no values) |
| POST | `/v1/credentials` | ✅ | Store credential (AES-GCM encrypted) |
| DELETE | `/v1/credentials/{id}` | ✅ | Delete credential |

### Realtime — `/v1/realtime`

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/v1/realtime/token/dashboard` | ✅ | Centrifugo dashboard JWT |
| POST | `/v1/realtime/token/visitor` | ✅ | Centrifugo visitor JWT |
| POST | `/v1/centrifugo/connect` | ✅ | Proxy: connect hook |
| POST | `/v1/centrifugo/subscribe` | ✅ | Proxy: subscribe hook |
| POST | `/v1/centrifugo/publish` | ✅ | Proxy: publish hook → FlowRuntime |

### AI Routes — `/ai` (threadly-ai service)

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/ai/complete` | ✅ | Streaming LLM completion |
| POST | `/ai/summarize` | ✅ | Summarize conversation |
| POST | `/ai/suggest-replies` | ✅ | Suggest 3 agent replies |
| POST | `/ai/extract-entities` | ✅ | Extract NER from message |
| POST | `/ai/classify-intent` | ✅ | Intent classification |
| POST | `/kb/ingest` | ✅ | Ingest document into Qdrant |
| POST | `/kb/query` | ✅ | Hybrid RAG query |
| GET | `/health` | ✅ | Health check |

### Internal

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/v1/internal/kb/{docId}/status` | ✅ | Ingestion status callback |

---

## Database Schema

| Table | Description | Migration | Status |
|-------|-------------|-----------|--------|
| `orgs` | Organizations (tenants) | V1 | ✅ |
| `users` | User accounts | V1 | ✅ |
| `refresh_tokens` | JWT refresh tokens (hashed) | V1 | ✅ |
| `bots` | Bot definitions + config | V1 | ✅ |
| `flows` | Flow definitions (latest draft + live) | V1 | ✅ |
| `flow_versions` | Immutable published versions | V1 | ✅ |
| `sessions` | Visitor chat sessions + variable JSONB | V1 | ✅ |
| `conversations` | Conversation threads | V1 | ✅ |
| `messages` | Individual messages in conversations | V1 | ✅ |
| `kb_documents` | Knowledge base document metadata | V1 | ✅ |
| `idempotency_keys` | Redis-backed (not DB table) | — | ✅ |
| `org_memberships` | Team member roles (OWNER/ADMIN/AGENT) | V2 | ✅ |
| `api_keys` | API key records (BCrypt hash, prefix) | V2 | ✅ |
| `webhooks` | Webhook endpoint registrations | V3 | ✅ |
| `webhook_deliveries` | Delivery attempt log (status, attempts) | V3 | ✅ |
| `credentials` | Encrypted credential store (AES-GCM) | V4 | ✅ |
| `costs` | LLM cost tracking per call | V5 | ✅ |
| `events` | Domain events for analytics rollups | V5 | ✅ |
| `audit_log` | Who changed what, when (all mutations) | V6 | 🔄 Tech Lead |

---

## Node Types

| # | Node Type | Executor | Status | Sprint |
|---|-----------|----------|--------|--------|
| 1 | `message` | `MessageNodeExecutor` | ✅ | Phase 0 |
| 2 | `question` | `QuestionNodeExecutor` | ✅ | Phase 0 |
| 3 | `ai_reply` | `AiReplyNodeExecutor` | ✅ | Phase 0 |
| 4 | `condition` | `ConditionNodeExecutor` | ✅ (basic) / 🔄 (12 ops) | Phase 0 / Sprint 2 |
| 5 | `handoff` | `HandoffNodeExecutor` | ✅ | Phase 0 |
| 6 | `end` | `EndNodeExecutor` | ✅ | Phase 0 |
| 7 | `api_call` | `ApiCallNodeExecutor` | ✅ | Phase 0 |
| 8 | `set_variable` | `SetVariableNodeExecutor` | ✅ | Phase 0 |
| 9 | `delay` | `DelayNodeExecutor` | ✅ | Sprint 2 |
| 10 | `switch` | `SwitchNodeExecutor` | ✅ | Sprint 2 |
| 11 | `send_email` | `SendEmailNodeExecutor` | ✅ | Sprint 2 |
| 12 | `collect_input` | `CollectInputNodeExecutor` | ✅ | Sprint 2 |
| 13 | `start` | (entry point, no executor) | ✅ | Phase 0 |

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          THREADLY SYSTEM                                │
│                                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌─────────────────────────┐   │
│  │  threadly-   │    │  threadly-   │    │     threadly-widget     │   │
│  │    web       │    │    core      │    │    (Preact IIFE < 35KB) │   │
│  │  (Next.js 15)│◄──►│ (Spring Boot │◄───│  Visitor browser page   │   │
│  │  :3000       │    │  Java 21)    │    └─────────────────────────┘   │
│  └──────────────┘    │  :8080       │                                   │
│         │            └──────┬───────┘                                   │
│         │                   │                                           │
│         │            ┌──────▼───────┐    ┌─────────────────────────┐   │
│         │            │  threadly-   │    │       Centrifugo v5      │   │
│         │            │    ai        │    │   (WebSocket realtime)   │   │
│         │            │  (FastAPI    │    │         :8000            │   │
│         │            │  Python)     │    └─────────────────────────┘   │
│         │            │  :8000       │              ▲                    │
│         │            └──────┬───────┘              │ pub/sub            │
│         │                   │                      │                    │
│  ┌──────▼───────┐    ┌──────▼───────┐    ┌────────┴────────────────┐   │
│  │  PostgreSQL  │    │    Qdrant    │    │         Redis 7          │   │
│  │     :5432    │    │  (vector DB) │    │   (sessions, rate limit, │   │
│  │  (primary DB)│    │    :6333     │    │    cache, idempotency)   │   │
│  └──────────────┘    └──────────────┘    │         :6379            │   │
│                                          └─────────────────────────┘   │
│  ┌──────────────┐    ┌──────────────┐                                   │
│  │    MinIO     │    │  Prometheus  │    ┌─────────────────────────┐   │
│  │  (S3-compat) │    │  + Grafana   │    │    GitHub Actions CI     │   │
│  │  :9000/:9001 │    │  :9090/:3001 │    │  build/test/push images  │   │
│  └──────────────┘    └──────────────┘    └─────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

**Message flow (widget → AI reply):**
```
Visitor types → widget.js → Centrifugo publish → CentrifugoProxyController
  → CentrifugoProxyService → FlowRuntime → AiReplyNodeExecutor
  → AiClient.streamComplete() → threadly-ai /ai/complete
  → AnthropicProvider (streaming) → tokens → Centrifugo publish
  → widget receives SSE tokens → renders streaming text
```

---

## Performance Targets

| Metric | Target | Current | Notes |
|--------|--------|---------|-------|
| Widget bundle size | < 35 KB gzipped | ~32 KB | Terser + brotli |
| API p50 latency | < 100 ms | ~45 ms | Excludes AI calls |
| AI first-token latency | < 1.5 s | ~800 ms | Anthropic claude-3-5-sonnet |
| Widget reconnect time | < 2 s | ~1.2 s | Centrifugo exponential backoff |
| Concurrent widget sessions | 500 | — | Load test pending |
| Auth rate limit | 10 req/min | ✅ | Bucket4j per IP |
| Per-org API rate limit | 1000 req/min | ✅ | Bucket4j per orgId |
| Ingestion throughput | 1 MB/s | — | Qdrant batch upsert |
| DB connection pool | 10–50 | 20 | HikariCP |

---

## Key URLs (Local Development)

| Service | URL | Credentials |
|---------|-----|-------------|
| Threadly Web Dashboard | http://localhost:3000 | signup at /signup |
| Spring Boot API | http://localhost:8080 | Bearer JWT |
| API Docs (Swagger) | http://localhost:8080/swagger-ui.html | — |
| Python AI Service | http://localhost:8001 | HMAC X-Service-Signature |
| Centrifugo Admin | http://localhost:8000/admin | see .env |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| Grafana | http://localhost:3001 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Qdrant Dashboard | http://localhost:6333/dashboard | — |
| PostgreSQL | localhost:5432 | threadly / threadly |

Start everything: `make up`

---

## Known Issues & Tech Debt

| ID | Severity | Description | Owner | Sprint |
|----|----------|-------------|-------|--------|
| TD-01 | High | `FlowSchemaValidator` is basic (checks nodes array + start node only); needs full JSON Schema | Tech Lead | Sprint 2 |
| TD-02 | High | `ConditionNodeExecutor` only supports eq/neq/gt/lt — 12-operator spec not yet implemented | Tech Lead | Sprint 2 |
| TD-03 | High | Core→AI calls use shared secret but no per-request HMAC signing on all routes | Tech Lead | Sprint 2 |
| TD-04 | High | No `AuditLog` — mutations are not tracked | Tech Lead | Sprint 2 |
| TD-05 | Medium | Maven wrapper (`mvnw`) missing from threadly-core | Tech Lead | Sprint 2 |
| TD-06 | Medium | OpenTelemetry trace propagation web→core→ai not validated end-to-end | Tech Lead | Sprint 2 |
| TD-07 | Medium | No E2E tests — smoke tests require running stack | Testing | Sprint 2 |
| TD-08 | Medium | Widget bundle size check pending (`npm run build` + verify < 35 KB) | Frontend | Sprint 2 |
| TD-09 | Medium | Password reset flow not implemented (Phase 1 scope) | Backend | Phase 1 |
| TD-10 | Low | Cohere reranker is optional — not enabled by default | AI | Phase 1 |
| TD-11 | Low | URL ingestion in AI service incomplete (Phase 1) | AI | Phase 1 |
| TD-12 | Low | Events table daily rollups not wired to analytics queries | Backend | Phase 1 |
| TD-13 | Low | Sentry not integrated in frontend or backend | Tech Lead | Phase 1 |
| TD-14 | Low | `mypy --strict` not passing in threadly-ai | AI | Sprint 2 |
