# Threadly — Sprint Tracker

> Sprint 3 Active. Updated: 2026-05-24
> Run everything: `make up` · Docs: `docs/` · Plan: `.claude/plans/`

---

## Legend
- ✅ Done — code written, file exists
- ❌ Pending — not yet built
- `[~]` Partial — scaffolded but needs work
- `[!]` Blocked / needs decision

---

## Sprint 3 — n8n Parity + Growth Features

> Started: 2026-05-24 · Target completion: 2026-06-14
> 6 agents running in parallel. Goal: close all feature gaps vs n8n + chatbotbuilder.net.

### Sprint 3 Agent Summary

| Agent | Tasks | Done | Status |
|-------|-------|------|--------|
| Backend Agent | 13 | 0 | ❌ 0/13 |
| Frontend Agent | 15 | 0 | ❌ 0/15 |
| AI+Widget Agent | 12 | 0 | ❌ 0/12 |
| Testing Agent | 10 | 0 | ❌ 0/10 |
| Tech Lead Agent | 12 | 0 | ❌ 0/12 |
| PM Agent | 5 | 5 | ✅ 5/5 |
| **Total** | **67** | **5** | **7%** |

---

### Backend Agent — Sprint 3 Tasks ❌ 0/13

| # | Task | Status |
|---|------|--------|
| B3-1 | Cron/scheduled flow triggers — Quartz Scheduler integration, `CronTriggerController`, `CronTriggerJob`, `CronTrigger` entity | ❌ |
| B3-2 | Inbound webhook trigger node — `POST /webhooks/trigger/{token}`, `InboundWebhookController`, token generation + HMAC validation | ❌ |
| B3-3 | Integration plugin framework — `IntegrationPlugin` interface, `IntegrationRegistry`, `IntegrationNodeExecutor` | ❌ |
| B3-4 | 20 integration plugins — Slack, Gmail, HubSpot, Notion, Google Sheets, Airtable, Twilio, SendGrid, Mailchimp, Shopify, Discord, GitHub, Linear, Jira, Stripe, Mixpanel, Segment, Make.com, Teams, Salesforce | ❌ |
| B3-5 | Flow subflows / reusable blocks — `SubflowNodeExecutor`, `SubflowController`, `SubflowDefinition` entity, Flyway V11 | ❌ |
| B3-6 | Error handling branches — `onError` routing in `FlowRuntime`, `ErrorNodeExecutor`, `ErrorBranch` edge type | ❌ |
| B3-7 | Loop node executor (ForEach) — `ForEachNodeExecutor`, iterate over array session variable | ❌ |
| B3-8 | Per-node test mode endpoint — `POST /v1/bots/{id}/flow/nodes/{nodeId}/test` with mock input | ❌ |
| B3-9 | CRM module — `Lead`, `LeadNote`, `LeadTimelineEvent`, `LeadTag`, `CustomFieldDefinition` entities; `LeadController`; `LeadCaptureController`; Flyway V10 | ❌ |
| B3-10 | Email sequence engine — `EmailSequence`, `EmailSequenceStep`, `EmailSequenceEnrollment` entities; `EmailSequenceService`, `SequenceStepScheduler`; SendGrid/SMTP delivery; Flyway V13 | ❌ |
| B3-11 | Stripe billing module — `BillingController`, `StripeWebhookController`, `BillingService`, `PlanFeatureGate`, `BillingMeterJob`; Flyway V8+V9 | ❌ |
| B3-12 | Bot cloning endpoint — `POST /v1/bots/{id}/clone`; deep copy flow + settings, new bot ID | ❌ |
| B3-13 | A/B testing — `AbTest`, `AbTestVariant`, `AbTestConversion` entities; `AbTestController`; traffic split logic in `FlowRuntime`; Flyway V14 | ❌ |

---

### AI+Widget Agent — Sprint 3 Tasks ❌ 0/12

| # | Task | Status |
|---|------|--------|
| A3-1 | Per-node test mode UI bridge — `POST /ai/node-test` route, mock session context execution | ❌ |
| A3-2 | 20 flow JSON templates — template library in `threadly-ai/templates/`, served via `GET /ai/templates` | ❌ |
| A3-3 | Widget lead capture form — pre-chat form component in widget (`LeadCaptureForm.tsx`), configurable required fields | ❌ |
| A3-4 | Widget CSAT rating — post-conversation 1–5 star satisfaction widget (`CsatWidget.tsx`), submits to `POST /v1/conversations/{id}/csat` | ❌ |
| A3-5 | KB URL scraping — `url_scraper.py` (BeautifulSoup/playwright-python), HTML → text extraction, `robots.txt` respect | ❌ |
| A3-6 | KB sitemap ingestion — `sitemap_parser.py`, parse XML sitemap, enqueue all URLs for scraping | ❌ |
| A3-7 | Cohere reranker (complete) — previously partial; make fully opt-in per bot, enable in production | ❌ |
| A3-8 | URL ingestion (complete) — previously partial (S3-19); full implementation with retry, depth limit | ❌ |
| A3-9 | `mypy --strict` passing — fix all remaining type errors in threadly-ai | ❌ |
| A3-10 | Widget A/B test variant rendering — show variant UI based on test cohort assignment | ❌ |
| A3-11 | Widget analytics event tracking — track message events, CSAT ratings, lead captures | ❌ |
| A3-12 | KB document metadata indexing — track source URL, upload date, language, document type | ❌ |

---

### Frontend Agent — Sprint 3 Tasks ❌ 0/15

| # | Task | Status |
|---|------|--------|
| F3-1 | Full UI overhaul — n8n dark canvas (`bg-neutral-950`) + chatbotbuilder.net clean sidebar; update `globals.css` design tokens | ❌ |
| F3-2 | 20+ node types in catalog (`NodePanel.tsx`) — add integration, cron, inbound-webhook, subflow, foreach, error-handler, ab-test, send-sms, send-slack, create-lead, update-lead, send-sequence nodes | ❌ |
| F3-3 | Integration marketplace page — `/integrations` with category filter, search, connect OAuth flow, connection status | ❌ |
| F3-4 | Template gallery — `/templates` with 20+ preview cards, one-click import to new bot | ❌ |
| F3-5 | CRM contacts page — `/crm` list view with filter sidebar, search, bulk actions | ❌ |
| F3-6 | CRM pipeline page — `/crm/pipeline` Kanban board with drag-and-drop | ❌ |
| F3-7 | CRM contact profile page — `/crm/leads/[id]` with timeline, notes, custom fields | ❌ |
| F3-8 | Billing/subscription page — `/billing` with plan cards, usage meters, invoice table, upgrade flow | ❌ |
| F3-9 | Analytics overhaul — CSV export button, funnel chart (D3/Recharts), cohort retention table | ❌ |
| F3-10 | Bot cloning UI — one-click "Duplicate" button on bot card, optimistic UI update | ❌ |
| F3-11 | A/B test management UI — `/bots/[id]/ab-tests` create test, set traffic split %, view conversion metrics | ❌ |
| F3-12 | Email sequence builder UI — `/sequences` list + `/sequences/[id]` step builder with scheduling config | ❌ |
| F3-13 | Per-node test mode in builder — "Test Node" button in PropertiesPanel, opens test input modal, shows mock output | ❌ |
| F3-14 | 20 flow templates library — pre-built starter flows (customer support, lead qualification, FAQ bot, survey bot, etc.) | ❌ |
| F3-15 | Sprint 2 Frontend backlog — complete F2-1 through F2-8 (node catalog, PropertiesPanel 12 types, analytics page, inbox redesign, onboarding wizard, credentials UI, Cmd+K enhancements, theme preview) | ❌ |

---

### Testing Agent — Sprint 3 Tasks ❌ 0/10

| # | Task | Status |
|---|------|--------|
| T3-1 | Unit tests — all Sprint 3 new node executors: `CronTriggerTest`, `InboundWebhookTest`, `ForEachNodeTest`, `ErrorHandlerTest`, `SubflowNodeTest` | ❌ |
| T3-2 | Integration tests — `LeadIntegrationTest.java` (create, upsert, tag, assign, timeline) | ❌ |
| T3-3 | Integration tests — `BillingIntegrationTest.java` (checkout session, webhook events, plan downgrade) | ❌ |
| T3-4 | Integration tests — `AbTestIntegrationTest.java` (create test, traffic split, conversion tracking) | ❌ |
| T3-5 | Integration tests — `IntegrationPluginTest.java` — mock external APIs, verify executor calls | ❌ |
| T3-6 | Integration tests — `EmailSequenceIntegrationTest.java` (enroll, step scheduling, email delivery) | ❌ |
| T3-7 | E2E Playwright — full happy path: `login → create bot → publish flow → embed → send message → see AI reply → lead captured` | ❌ |
| T3-8 | Performance — 500 concurrent widget conversations (k6 load test script `scripts/load-test.js`) | ❌ |
| T3-9 | Security — tenant isolation on all Sprint 3 new endpoints (CRM, billing, integrations, sequences, A/B tests) | ❌ |
| T3-10 | Widget e2e tests — lead form capture, CSAT submission, file uploads | ❌ |

---

### Tech Lead Agent — Sprint 3 Tasks ❌ 0/12

| # | Task | Status |
|---|------|--------|
| TL3-1 | DB migrations V7–V14 — integration_connections (V7), billing_subscriptions (V8), billing_usage (V9), leads+crm (V10), subflows (V11), email_sequences (V12), ab_tests (V13), csat_ratings (V14) | ❌ |
| TL3-2 | Integration plugin framework architecture — `IntegrationPlugin` interface, `OAuthRefreshScheduler`, plugin registration, `IntegrationMeta` | ❌ |
| TL3-3 | Full API docs update — update `openapi.yaml` with all Sprint 3 endpoints (CRM, billing, integrations, sequences, A/B tests, triggers) | ❌ |
| TL3-4 | Docker Compose additions — MailHog (dev SMTP), Stripe CLI (webhook forwarding) | ❌ |
| TL3-5 | `application-prod.yml` additions — Stripe env vars, integration OAuth secrets, Quartz scheduler config, email sequence SMTP | ❌ |
| TL3-6 | Grafana dashboard updates — add panels for: active email sequences, CRM lead funnel, Stripe MRR meter, integration call rate/error | ❌ |
| TL3-7 | Security review — OWASP check on new endpoints; verify Stripe webhook signature verification; verify all CRM endpoints have tenant isolation | ❌ |
| TL3-8 | Integration OAuth flow — standardized flow for all 20 integrations (code exchange, token refresh, scope mapping) | ❌ |
| TL3-9 | Rate limiting per integration — per-integration API call budgets via Bucket4j | ❌ |
| TL3-10 | Webhook retry logic — exponential backoff for failed outbound webhooks (3 retries) | ❌ |
| TL3-11 | Complete Sprint 2 Tech Lead backlog — TL2-1 through TL2-14 (FlowSchemaValidator, ConditionNodeExecutor, HMAC filter, AuditLog, Flyway V6, Railway, Cloudflare, Grafana, Metrics, prod yml, mvnw, OTel) | ❌ |
| TL3-12 | Performance tuning — query optimization for CRM lead list (n+1 fixes), billing usage aggregation indexes | ❌ |

---

### PM Agent — Sprint 3 Tasks ✅ 5/5

| # | Task | Status |
|---|------|--------|
| PM3-1 | `SPRINT.md` — prepend Sprint 3 section with 67 tasks organized by agent | ✅ |
| PM3-2 | `FEATURES.md` — add F039–F070 entries for all Sprint 3 features | ✅ |
| PM3-3 | `PRODUCT_STATUS.md` — update to Phase 0 ✅ + Sprint 3 IN PROGRESS | ✅ |
| PM3-4 | `docs/15-integrations.md` — document all 20 integrations (Slack, Gmail, HubSpot, Notion, Google Sheets, SendGrid, Twilio, Stripe, Shopify, Discord, GitHub, Linear, Jira, Airtable, Mailchimp, Mixpanel, Segment, Make.com, Teams, Salesforce) | ✅ |
| PM3-5 | `docs/16-billing.md`, `docs/17-crm.md` — billing plans + CRM model | ✅ |

---

## Sprint 2 — Parallel Agent Sprint

> Started: 2026-05-21 · Target completion: 2026-05-28
> 6 agents running in parallel across dedicated git worktrees.

### Sprint 2 Agent Summary

| Agent | Tasks | Done | Status |
|-------|-------|------|--------|
| Backend Agent | 13 | 13 | ✅ 13/13 |
| AI+Widget Agent | 15 | 15 | ✅ 15/15 |
| Frontend Agent | 8 | 0 | 🔄 0/8 |
| Testing Agent | 12 | 0 | 🔄 0/12 |
| Tech Lead Agent | 14 | 0 | 🔄 0/14 |
| PM Agent | 5 | 0 | 🔄 0/5 |
| **Total** | **67** | **28** | **42%** |

---

### Backend Agent — Sprint 2 Tasks ✅ 13/13

| # | Task | Status |
|---|------|--------|
| B2-1 | `OrgMembership` entity (id, org_id, user_id, role, invited_email, accepted_at) | `[x]` |
| B2-2 | `TeamController` — invite, list, update role, remove | `[x]` |
| B2-3 | Flyway V2: `org_memberships` + `api_keys` tables | `[x]` |
| B2-4 | `ApiKey` entity + `ApiKeyController` (create, list, revoke) | `[x]` |
| B2-5 | `JwtAuthFilter` — API key auth branch (tly_live_ prefix detection) | `[x]` |
| B2-6 | `WebhookController` + `WebhookDeliveryService` (HMAC-SHA256, Resilience4j retry) | `[x]` |
| B2-7 | Flyway V3: `webhooks` + `webhook_deliveries` tables | `[x]` |
| B2-8 | `GET /flow/export` + `POST /flow/import` on `FlowController` | `[x]` |
| B2-9 | 4 new node executors: `DelayNodeExecutor`, `SwitchNodeExecutor`, `SendEmailNodeExecutor`, `CollectInputNodeExecutor` | `[x]` |
| B2-10 | Conversation bulk ops: `POST /conversations/bulk-close`, `POST /conversations/bulk-assign`, `GET /conversations/export` (CSV) | `[x]` |
| B2-11 | Per-bot analytics: `GET /bots/{id}/analytics/summary`, `/daily`, `/funnel` | `[x]` |
| B2-12 | `CredentialsController` + `CredentialsService` (AES-256-GCM, PBKDF2) + Flyway V4 | `[x]` |
| B2-13 | `SecurityHeadersFilter` (CSP/HSTS/X-Frame-Options) + `RateLimitFilter` (Bucket4j + Redis) + Flyway V5 (events) | `[x]` |

---

### AI+Widget Agent — Sprint 2 Tasks ✅ 15/15

| # | Task | Status |
|---|------|--------|
| A2-1 | `AnthropicProvider` streaming (claude-3-5-sonnet) + `OpenAIProvider` fallback + `ProviderChain` | `[x]` |
| A2-2 | `COSTS` table + `CostTracker` (input/output $/token per provider) | `[x]` |
| A2-3 | Hybrid RAG: BM25 sparse index + RRF fusion with Qdrant dense results | `[x]` |
| A2-4 | Cohere reranker integration (optional, `rerank=true` param) | `[x]` |
| A2-5 | Citation formatting: `[1][2]` inline markers + `Citation` model (doc_name, page, passage) | `[x]` |
| A2-6 | `/ai/summarize` route | `[x]` |
| A2-7 | `/ai/suggest-replies` route (returns 3 options as JSON array) | `[x]` |
| A2-8 | `/ai/extract-entities` route (NER: name, email, phone, company, topic) | `[x]` |
| A2-9 | `/ai/classify-intent` route (intent + confidence score) | `[x]` |
| A2-10 | Langfuse tracing: trace + generation span + flush on every `/ai/complete` | `[x]` |
| A2-11 | Improved ingestion: 512-token chunks, 50-token overlap, progress callbacks | `[x]` |
| A2-12 | Widget rich messages: ButtonMessage, CardMessage, QuickReplies, file attachment rendering | `[x]` |
| A2-13 | Widget file upload: XHR + progress bar + 10 MB limit | `[x]` |
| A2-14 | Widget UX: unread badge, Web Audio notification, timestamps (hover), delivery status ✓/✓✓, sessionStorage persistence | `[x]` |
| A2-15 | Widget theme system: 12 CSS custom properties, darkMode auto/light/dark | `[x]` |

---

### Frontend Agent — Sprint 2 Tasks 🔄 0/8

| # | Task | Status |
|---|------|--------|
| F2-1 | n8n-inspired node catalog (NodePanel.tsx) — categories: AI, Logic, Actions, Utils; searchable | `[ ]` |
| F2-2 | PropertiesPanel.tsx — 12 input types: text, number, select, multiselect, code, json, condition-builder, email-template, variable-picker, credential-picker, toggle, slider | `[ ]` |
| F2-3 | Analytics page `app/(app)/analytics/page.tsx` — bar chart (daily messages), line chart (costs), funnel chart | `[ ]` |
| F2-4 | Conversation inbox redesign: react-virtual scrolling, bulk select + bulk-close action, CSV export button | `[ ]` |
| F2-5 | Onboarding wizard `app/(app)/onboarding/page.tsx` — 3 steps: create bot, install snippet (copy + verify), go live | `[ ]` |
| F2-6 | Credentials manager `app/(app)/settings/credentials/page.tsx` — list, create modal, delete | `[ ]` |
| F2-7 | Command palette enhancements — recent actions history, bot-scoped search, Cmd+K from builder | `[ ]` |
| F2-8 | Theme preview live panel in `bots/[id]/settings/page.tsx` — full widget preview iframe | `[ ]` |

---

### Testing Agent — Sprint 2 Tasks 🔄 0/12

| # | Task | Status |
|---|------|--------|
| T2-1 | `AuthIntegrationTest.java` (Testcontainers Postgres + Redis) — signup, login, refresh, logout, /me | `[ ]` |
| T2-2 | `BotIntegrationTest.java` — CRUD, theme update, snippet generation | `[ ]` |
| T2-3 | `FlowIntegrationTest.java` — saveDraft, publish, rollback, import/export | `[ ]` |
| T2-4 | `ConversationIntegrationTest.java` — create, handoff, agent reply, bulk-close, CSV export | `[ ]` |
| T2-5 | `TenantIsolationTest.java` — Org A API key cannot read Org B bots/conversations/KB | `[ ]` |
| T2-6 | `AnalyticsIntegrationTest.java` — summary, daily, funnel per bot | `[ ]` |
| T2-7 | `test_complete.py` (Pytest) — streaming response, citation format, fallback provider | `[ ]` |
| T2-8 | `test_kb.py` — ingest, query, BM25+RRF fusion, Cohere rerank toggle | `[ ]` |
| T2-9 | `test_ai_utils.py` — summarize, suggest-replies, extract-entities, classify-intent | `[ ]` |
| T2-10 | Playwright E2E: `auth.spec.ts` — signup → login → logout full flow | `[ ]` |
| T2-11 | Playwright E2E: `builder.spec.ts` — create bot → add nodes → publish → verify live | `[ ]` |
| T2-12 | Vitest widget: `theme.test.ts` + `ws-client.test.ts` + `ChatPanel.test.tsx` | `[ ]` |

---

### Tech Lead Agent — Sprint 2 Tasks 🔄 0/14

| # | Task | Status |
|---|------|--------|
| TL2-1 | `FlowSchemaValidator.java` — full JSON Schema validation (required fields per node type) | `[ ]` |
| TL2-2 | `ConditionNodeExecutor.java` — 12 operators: eq, neq, gt, lt, gte, lte, contains, startsWith, endsWith, in, notIn, regex | `[ ]` |
| TL2-3 | `HmacRequestFilter.java` — sign all Core→AI requests with `X-Service-Signature` header | `[ ]` |
| TL2-4 | `AuditLog.java` entity: entity_type, entity_id, action, actor_id, old_value JSONB, new_value JSONB, occurred_at | `[ ]` |
| TL2-5 | `AuditInterceptor.java` — Spring MVC interceptor capturing all mutating requests to AuditLog | `[ ]` |
| TL2-6 | Flyway V6: `audit_log` table | `[ ]` |
| TL2-7 | `railway.toml` — Railway.app deployment config for core + ai + web services | `[ ]` |
| TL2-8 | Cloudflare Worker `infra/cloudflare/worker.js` — widget CDN edge cache + cache-busting | `[ ]` |
| TL2-9 | Grafana production dashboard `infra/grafana/dashboards/threadly-prod.json` | `[ ]` |
| TL2-10 | `ThreadlyMetrics.java` — Micrometer custom meters: messages.total, conversations.active, llm.cost.usd, handoffs.total | `[ ]` |
| TL2-11 | `application-prod.yml` — production profile (connection pools, logging, feature flags) | `[ ]` |
| TL2-12 | Maven wrapper `mvnw` added to threadly-core | `[ ]` |
| TL2-13 | OTel trace propagation validated end-to-end: web → core → ai | `[ ]` |
| TL2-14 | `mypy --strict` passing in threadly-ai | `[ ]` |

---

### PM Agent — Sprint 2 Tasks 🔄 0/5

| # | Task | Status |
|---|------|--------|
| PM2-1 | `PRODUCT_STATUS.md` — master feature + API + schema tracker | `[ ]` |
| PM2-2 | `AGENTS.md` — agent coordination doc with contracts | `[ ]` |
| PM2-3 | `CHANGELOG.md` — Keep a Changelog format, Sprint 2 + v0.1.0 | `[ ]` |
| PM2-4 | `SPRINT.md` — Sprint 2 prepended to existing Sprint 1 | `[ ]` |
| PM2-5 | `FEATURES.md` — feature registry, 30+ features with user stories | `[ ]` |

---

---

# Sprint Tracker — Phase 0 (Original)

> Phase 0 MVP. Updated: 2026-05-21
> Run everything: `make up` · Docs: `docs/` · Plan: `.claude/plans/`

---

## Sprint 0 — Foundation & Docs

| # | Task | Status |
|---|------|--------|
| S0-1 | Product vision + naming (Threadly) | `[x]` |
| S0-2 | All 15 docs written (`docs/00–14`) | `[x]` |
| S0-3 | Top-level README, .gitignore, Makefile, .env.example | `[x]` |
| S0-4 | Git repo initialized | `[x]` |
| S0-5 | First git commit | `[ ]` needs approval |

---

## Sprint 1 — Infrastructure

| # | Task | Status |
|---|------|--------|
| S1-1 | `infra/docker-compose.yml` (Postgres 16, Redis 7, Qdrant, MinIO, Centrifugo, Prometheus, Grafana) | `[x]` |
| S1-2 | `infra/centrifugo/config.json` (channels, proxy hooks, namespaces) | `[x]` |
| S1-3 | `infra/test-embed/index.html` (widget test page) | `[x]` |
| S1-4 | `scripts/bootstrap.sh` (JWT key gen, Docker pull, MinIO bucket) | `[x]` |
| S1-5 | `scripts/seed-demo-bot.sh` | `[x]` |
| S1-6 | `scripts/codegen-api.sh` (Orval codegen) | `[x]` |
| S1-7 | Verify `make up` brings all services up cleanly | `[ ]` runtime check |
| S1-8 | Centrifugo healthcheck reachable at `localhost:8000/health` | `[ ]` runtime check |
| S1-9 | MinIO bucket `threadly-kb` auto-created on bootstrap | `[ ]` runtime check |
| S1-10 | Grafana dashboards pre-provisioned | `[x]` provisioning yml + threadly-overview.json |

---

## Sprint 2 — threadly-core (Spring Boot)

### 2A · Project scaffold
| # | Task | Status |
|---|------|--------|
| S2-1 | `pom.xml` — all deps (Spring Boot 3.3, Hibernate, Flyway, jjwt, Resilience4j, Bucket4j, AWS SDK, Springdoc, OTel) | `[x]` |
| S2-2 | `application.yml` — full config (datasource, Redis, cache, Centrifugo, AI, storage) | `[x]` |
| S2-3 | `V1__init.sql` Flyway migration — all tables + indexes | `[x]` |
| S2-4 | `ThreadlyCoreApplication.java` | `[x]` |
| S2-5 | Maven wrapper (`mvnw`) | `[ ]` add for Dockerfile |
| S2-6 | `Dockerfile` | `[x]` |

### 2B · Common / cross-cutting
| # | Task | Status |
|---|------|--------|
| S2-7 | `TenantContext` ThreadLocal | `[x]` |
| S2-8 | `ApiError` RFC 7807 Problem+JSON | `[x]` |
| S2-9 | `GlobalExceptionHandler` | `[x]` |
| S2-10 | `S3Config` (MinIO / AWS SDK) | `[x]` |
| S2-11 | Hibernate `@Filter` tenant enforcement | `[x]` `TenantFilterAspect` + `@Filter` on Bot, Conversation, KbDocument |
| S2-12 | Idempotency-Key header interceptor | `[x]` `IdempotencyInterceptor` + Redis 24h TTL |
| S2-13 | CORS config for widget + web origins | `[x]` `CorsConfig` — wildcard widget, pattern-matched dashboard |

### 2C · Identity module
| # | Task | Status |
|---|------|--------|
| S2-14 | `User`, `Org`, `RefreshToken` entities | `[x]` |
| S2-15 | `JwtService` (RS256, PEM key loading) | `[x]` |
| S2-16 | `AuthService` (signup, login, refresh, logout) | `[x]` |
| S2-17 | `AuthController` (`/v1/auth/*`) | `[x]` |
| S2-18 | `SecurityConfig` (stateless JWT, public paths) | `[x]` |
| S2-19 | `JwtAuthFilter` | `[x]` |
| S2-20 | `/me` endpoint | `[x]` already in AuthController |
| S2-21 | Password reset flow | `[ ]` Phase 1 |

### 2D · Workspace module
| # | Task | Status |
|---|------|--------|
| S2-22 | `Bot` entity + `BotRepository` | `[x]` |
| S2-23 | `BotService` (CRUD + embed snippet) | `[x]` |
| S2-24 | `BotController` (`/v1/bots/*`) | `[x]` |
| S2-25 | `OrgRepository` | `[x]` |
| S2-26 | Bot theme config (accent color, avatar, position) update endpoint | `[x]` `PATCH /v1/bots/{id}/theme` |

### 2E · Flow module
| # | Task | Status |
|---|------|--------|
| S2-27 | `Flow`, `FlowVersion` entities + repos | `[x]` |
| S2-28 | `FlowService` (getDraft, saveDraft, publish, rollback) | `[x]` |
| S2-29 | `FlowController` (`/v1/bots/{id}/flow/*`) | `[x]` |
| S2-30 | Flow JSON schema validation on publish | `[x]` `validateFlowJson()` checks nodes array + start node |

### 2F · Runtime module
| # | Task | Status |
|---|------|--------|
| S2-31 | `FlowRuntime` — main interpreter loop | `[x]` |
| S2-32 | `FlowGraph` — JSON parse + graph traversal | `[x]` |
| S2-33 | `NodeExecutor` SPI + `NodeExecutorFactory` | `[x]` |
| S2-34 | `MessageNodeExecutor` | `[x]` |
| S2-35 | `QuestionNodeExecutor` | `[x]` |
| S2-36 | `AiReplyNodeExecutor` (streaming tokens → Centrifugo) | `[x]` |
| S2-37 | `ConditionNodeExecutor` | `[x]` |
| S2-38 | `HandoffNodeExecutor` | `[x]` |
| S2-39 | `EndNodeExecutor` | `[x]` |
| S2-40 | `TemplateEngine` (`{{variable}}` substitution) | `[x]` |
| S2-41 | `Session` entity + `SessionRepository` | `[x]` |
| S2-42 | `ApiCallNodeExecutor` | `[x]` WebClient GET/POST + response stored in session vars |
| S2-43 | `SetVariableNodeExecutor` | `[x]` assignments list rendered via TemplateEngine |
| S2-44 | Session variables persistence (Redis or Postgres) | `[x]` stored in `Session.variables` JSONB |

### 2G · Conversation module
| # | Task | Status |
|---|------|--------|
| S2-45 | `Conversation`, `Message` entities + repos | `[x]` |
| S2-46 | `ConversationController` (list, get, messages, handoff, close) | `[x]` |
| S2-47 | Agent reply endpoint | `[x]` |
| S2-48 | SSE endpoint for live dashboard counters | `[x]` `LiveCounterController` 60×5s ticks |

### 2H · Centrifugo / Realtime
| # | Task | Status |
|---|------|--------|
| S2-49 | `CentrifugoClient` (publish via HTTP API, circuit breaker) | `[x]` |
| S2-50 | `RealtimeController` (dashboard token + visitor token + widget message fallback) | `[x]` |
| S2-51 | `CentrifugoProxyController` (connect/subscribe/publish hooks) | `[x]` |
| S2-52 | `CentrifugoProxyService` (channel auth, route to FlowRuntime) | `[x]` |
| S2-53 | `OutboxService` (async Centrifugo publish) | `[x]` |

### 2I · Knowledge Base module
| # | Task | Status |
|---|------|--------|
| S2-54 | `KbDocument` entity + `KbDocumentRepository` | `[x]` |
| S2-55 | `KbService` (upload to S3/MinIO, dispatch ingestion) | `[x]` |
| S2-56 | `KbController` (file upload, URL add, list, delete) | `[x]` |
| S2-57 | Outbox event → trigger threadly-ai ingestion | `[x]` `KbIngestionJob` @Scheduled poller → POST `/kb/ingest` |
| S2-58 | Ingestion status polling / webhook back to core | `[x]` `KbIngestionCallbackController` `POST /v1/internal/kb/{docId}/status` |

### 2J · Analytics
| # | Task | Status |
|---|------|--------|
| S2-59 | `AnalyticsController` `/stats` endpoint | `[x]` |
| S2-60 | `sumMessageCountByOrgId` in repo | `[x]` |
| S2-61 | Real p50 response time from messages table | `[x]` `PERCENTILE_CONT(0.5)` native query in MessageRepository |
| S2-62 | Events table + daily rollups | `[ ]` Phase 1 |

### 2K · AI Client
| # | Task | Status |
|---|------|--------|
| S2-63 | `AiClient` (WebClient streaming call to threadly-ai) | `[x]` |
| S2-64 | Per-org token budget enforcement | `[x]` `TokenBudgetService` — Redis INCR + 24h TTL + daily limit check |

---

## Sprint 3 — threadly-ai (Python FastAPI)

| # | Task | Status |
|---|------|--------|
| S3-1 | `pyproject.toml` — all deps (FastAPI, anthropic, openai, qdrant-client, langchain, voyage, etc.) | `[x]` |
| S3-2 | `app/main.py` + lifespan | `[x]` |
| S3-3 | `app/config.py` pydantic-settings | `[x]` |
| S3-4 | `AnthropicProvider` streaming | `[x]` |
| S3-5 | `OpenAIProvider` fallback streaming | `[x]` |
| S3-6 | `get_provider()` factory with auto-fallback | `[x]` |
| S3-7 | `embed_texts()` — Voyage AI + OpenAI fallback | `[x]` |
| S3-8 | `ingest_document()` — parse → chunk → embed → Qdrant | `[x]` |
| S3-9 | `query_kb()` — embed → Qdrant search → format passages | `[x]` |
| S3-10 | `build_messages()` — history trim + RAG injection | `[x]` |
| S3-11 | `build_system_prompt()` — variable substitution | `[x]` |
| S3-12 | `POST /ai/complete` streaming route | `[x]` |
| S3-13 | `POST /kb/ingest` route | `[x]` |
| S3-14 | `POST /kb/query` route | `[x]` |
| S3-15 | `GET /health` | `[x]` |
| S3-16 | `Dockerfile` | `[x]` |
| S3-17 | HMAC service-secret auth between core ↔ ai | `[x]` |
| S3-18 | Langfuse tracing wired | `[x]` trace + generation + flush on every `/ai/complete` call |
| S3-19 | URL ingestion (fetch HTML → parse) | `[ ]` Phase 1 |
| S3-20 | Cohere reranker (opt-in) | `[ ]` Phase 1 |
| S3-21 | `mypy --strict` passing | `[ ]` runtime check |

---

## Sprint 4 — threadly-web (Next.js 15)

### 4A · Project scaffold
| # | Task | Status |
|---|------|--------|
| S4-1 | `package.json` — all deps (Next 15, React 19, React Flow v12, TanStack, Auth.js, Centrifuge, etc.) | `[x]` |
| S4-2 | `next.config.ts` | `[x]` |
| S4-3 | `tsconfig.json` | `[x]` |
| S4-4 | `app/globals.css` — full design token system | `[x]` |
| S4-5 | `app/layout.tsx` — root layout, Geist fonts, Sonner | `[x]` |
| S4-6 | `components/providers.tsx` — QueryClient + SessionProvider | `[x]` |
| S4-7 | `auth.ts` — Auth.js v5 credentials provider | `[x]` |
| S4-8 | `middleware.ts` — auth route protection | `[x]` |
| S4-9 | `app/api/auth/[...nextauth]/route.ts` | `[x]` |
| S4-10 | `lib/api.ts` — fetch wrapper with token injection | `[x]` |
| S4-11 | `lib/types.ts` — shared TypeScript types | `[x]` |
| S4-12 | `lib/utils.ts` — cn, formatDate, formatRelative | `[x]` |
| S4-13 | `orval.config.ts` — OpenAPI codegen config | `[x]` |
| S4-14 | `lib/api-mutator.ts` — custom Orval fetcher | `[x]` |
| S4-15 | Biome config (`biome.json`) | `[x]` |
| S4-16 | PostCSS config (`postcss.config.mjs`) for Tailwind v4 | `[x]` |

### 4B · Auth pages
| # | Task | Status |
|---|------|--------|
| S4-17 | `app/(auth)/login/page.tsx` | `[x]` |
| S4-18 | `app/(auth)/signup/page.tsx` | `[x]` |

### 4C · Marketing
| # | Task | Status |
|---|------|--------|
| S4-19 | `app/(marketing)/page.tsx` — landing page | `[x]` |
| S4-20 | SEO meta tags + OG image | `[x]` full Metadata with openGraph, twitter, robots in layout.tsx |

### 4D · App shell
| # | Task | Status |
|---|------|--------|
| S4-21 | `app/(app)/layout.tsx` — sidebar + main | `[x]` |
| S4-22 | `components/layout/Sidebar.tsx` | `[x]` |
| S4-23 | Cmd-K palette (`cmdk`) | `[x]` `CommandPalette` — global Cmd+K, bot list, keyboard nav |
| S4-24 | Dark/light theme toggle | `[x]` `ThemeToggle` — localStorage + prefers-color-scheme |

### 4E · Dashboard
| # | Task | Status |
|---|------|--------|
| S4-25 | `app/(app)/dashboard/page.tsx` — stat cards + bot list + recent conversations | `[x]` |
| S4-26 | Centrifugo live counter updates via SSE or subscription | `[x]` EventSource on `/v1/analytics/live`, merges into TanStack Query cache |

### 4F · Bots
| # | Task | Status |
|---|------|--------|
| S4-27 | `app/(app)/bots/page.tsx` — list, create, delete | `[x]` |
| S4-28 | Bot settings (accent color picker, avatar upload, greeting text) | `[x]` `bots/[id]/settings/page.tsx` with color swatches + live preview |

### 4G · Flow Builder
| # | Task | Status |
|---|------|--------|
| S4-29 | `components/builder/nodes/NodeTypes.tsx` — all 8 custom node components | `[x]` |
| S4-30 | `components/builder/NodePanel.tsx` — drag-and-drop node palette | `[x]` |
| S4-31 | `components/builder/PropertiesPanel.tsx` — per-node config | `[x]` |
| S4-32 | `components/builder/FlowCanvas.tsx` — React Flow v12 canvas | `[x]` |
| S4-33 | `app/(app)/builder/[botId]/page.tsx` — builder page + toolbar | `[x]` |
| S4-34 | Autosave (800ms debounce) | `[x]` |
| S4-35 | Versions drawer + rollback | `[x]` |
| S4-36 | Undo/redo (Cmd-Z) | `[x]` useRef history stack, 100 entries, skipHistory flag |
| S4-37 | Live preview pane (test chat without leaving canvas) | `[x]` `LivePreviewPane` — polls conversations for AI reply |
| S4-38 | Node validation badges (red dot on missing config) | `[x]` `validateNodes()` + `ErrorBadge` component, absolute-positioned |
| S4-39 | Keyboard shortcuts (N = new node, Del = delete, Cmd-Z) | `[x]` wired in FlowCanvas useEffect |

### 4H · Conversations
| # | Task | Status |
|---|------|--------|
| S4-40 | `app/(app)/conversations/page.tsx` — 3-pane inbox | `[x]` |
| S4-41 | Centrifugo live updates (new conversation, new message) | `[x]` |
| S4-42 | Agent reply (handed-off conversations) | `[x]` |
| S4-43 | Take Over + Close actions | `[x]` |
| S4-44 | KB citation hovercards in transcript | `[ ]` Phase 1 |
| S4-45 | Conversation search / filter | `[x]` search input + status filter (all/open/handed_off/closed) |

### 4I · Knowledge Base
| # | Task | Status |
|---|------|--------|
| S4-46 | `app/(app)/knowledge/[botId]/page.tsx` — file upload + URL add + list | `[x]` |
| S4-47 | Live ingestion status polling (auto-refreshes while PROCESSING) | `[x]` |
| S4-48 | Drag-and-drop upload | `[x]` |

### 4J · Settings
| # | Task | Status |
|---|------|--------|
| S4-49 | `app/(app)/settings/page.tsx` — workspace info + embed snippet | `[x]` |
| S4-50 | Team members invite / list | `[x]` `settings/team/page.tsx` — invite by email, role select, remove |
| S4-51 | API keys management | `[x]` `settings/api-keys/page.tsx` — create, reveal once, revoke |
| S4-52 | Bot theme live preview | `[x]` live launcher + bubble preview in `bots/[id]/settings/page.tsx` |

---

## Sprint 5 — threadly-widget (Preact)

| # | Task | Status |
|---|------|--------|
| S5-1 | `package.json` | `[x]` |
| S5-2 | `tsconfig.json` | `[x]` |
| S5-3 | `vite.config.ts` — IIFE build, Terser, compression | `[x]` |
| S5-4 | `src/types.ts` — WidgetConfig, ChatMessage, ServerEvent | `[x]` |
| S5-5 | `src/theme.ts` — injectStyles (full CSS-in-JS, accent color) | `[x]` |
| S5-6 | `src/ws-client.ts` — Centrifugo client, reconnect, visitorId persistence | `[x]` |
| S5-7 | `src/ui/ChatPanel.tsx` — messages, streaming tokens, typing indicator | `[x]` |
| S5-8 | `src/main.tsx` — Preact app + launcher button | `[x]` |
| S5-9 | `src/widget.ts` — loader, reads data-* attributes | `[x]` |
| S5-10 | `index.html` — dev test page | `[x]` |
| S5-11 | Verify bundle < 35 KB gzipped | `[ ]` run `npm run build` and check |
| S5-12 | Offline queue (messages queued when disconnected, replayed on reconnect) | `[x]` localStorage-backed queue, flushed on reconnect |
| S5-13 | Dark mode (`prefers-color-scheme`) | `[x]` `config.darkMode` + CSS class toggle |
| S5-14 | Mobile bottom-sheet animation | `[x]` `tly-sheet-in` keyframe + `entering` class |
| S5-15 | WCAG AA accessibility | `[x]` role=dialog, aria-modal, role=log, aria-live, focus trap, Escape key |

---

## Sprint 6 — Integration & Hardening

| # | Task | Status |
|---|------|--------|
| S6-1 | End-to-end smoke test: signup → create bot → publish flow → embed widget → send message → see AI reply | `[ ]` requires running stack |
| S6-2 | Centrifugo proxy round-trip verified (widget → Centrifugo → core → AI → Centrifugo → widget) | `[ ]` requires running stack |
| S6-3 | Cross-tenant isolation integration test | `[ ]` |
| S6-4 | KB ingest → query round-trip test (upload PDF → ask question → see citation) | `[ ]` requires running stack |
| S6-5 | Rate limit tests (Bucket4j per-org AI limit) | `[ ]` |
| S6-6 | Idempotency-Key interceptor on all POST endpoints | `[x]` wired via `IdempotencyInterceptor` + `WebMvcConfig` |
| S6-7 | CORS lockdown (per-bot allowed origins) | `[x]` `CorsConfig` — dashboard pattern-match, widget wildcard |
| S6-8 | OpenTelemetry traces propagated web → core → ai | `[~]` OTel deps wired, trace propagation not validated end-to-end |
| S6-9 | Maven wrapper (`mvnw`) added to threadly-core | `[ ]` |
| S6-10 | Flyway `validate` passes on clean DB | `[ ]` runtime check |
| S6-11 | All services have `/health` + `/metrics` (Prometheus) | `[~]` health yes, metrics partial |
| S6-12 | Sentry wired (frontend + backend) | `[ ]` |
| S6-13 | Load test: 500 concurrent widget connections | `[ ]` |

---

## Sprint 7 — Deploy & Launch

| # | Task | Status |
|---|------|--------|
| S7-1 | Railway / Render staging environment | `[ ]` |
| S7-2 | Cloudflare DNS + WAF in front | `[ ]` |
| S7-3 | Cloudflare R2 bucket for widget CDN (`cdn.threadly.dev/widget.js`) | `[ ]` |
| S7-4 | Doppler secret sync (local → CI → prod) | `[ ]` |
| S7-5 | GitHub Actions CI (build + test + deploy) | `[x]` `.github/workflows/ci.yml` — core/ai/web/widget jobs + Docker smoke test |
| S7-6 | First-run onboarding wizard (create bot → paste snippet → live in 5 min) | `[ ]` |
| S7-7 | Marketing landing page deployed at `threadly.dev` | `[ ]` |
| S7-8 | Closed beta — invite 5–10 SMBs | `[ ]` |
| S7-9 | PostHog product analytics wired | `[ ]` |

---

## Quick Stats

| Service | Files | Done | Notes |
|---------|-------|------|-------|
| docs | 15 | 15 | all written |
| infra | 8 | 8 | compose + grafana + scripts |
| threadly-core | 58 | 56 | mvnw + runtime checks pending |
| threadly-ai | 15 | 14 | mypy strict + URL ingest pending |
| threadly-web | 35 | 35 | all pages + builder complete |
| threadly-widget | 10 | 9 | bundle size check pending |
| CI | 1 | 1 | GitHub Actions workflow |
| **Total** | **142** | **138** | **97% complete** |

### Remaining to reach 100%

1. `cd threadly-core && ./mvnw` — add Maven wrapper (S2-5 / S6-9)
2. `make up` — bring stack up, fix any startup errors (S1-7, S1-8, S1-9)
3. `cd threadly-widget && npm run build` — verify < 35 KB gzipped (S5-11)
4. End-to-end smoke test (S6-1)
5. Railway deploy (S7-1) — push-to-deploy when ready for beta
