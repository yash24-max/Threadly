# Changelog

All notable changes to Threadly are documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [0.2.0-alpha] — Sprint 3 (In Progress)

> Sprint 3 started 2026-05-24. 67 tasks across 6 agents.
> Focus: n8n feature parity, growth features (integrations, CRM, billing, A/B testing, email sequences).

### Added — Flow Triggers

#### Cron-Based Flow Triggers
- `CronTrigger` entity — schedule flows on cron expressions (Quartz Scheduler integration)
- `CronTriggerController` — create, list, update, delete scheduled flows
- `CronTriggerJob` — background job executor for scheduled flow invocations
- Timezone support for cron schedules

#### Inbound Webhook Triggers
- `InboundWebhook` entity — generate HMAC-validated webhook URLs per bot
- `POST /webhooks/trigger/{token}` — public endpoint to trigger flows with `content-type: application/json` payload
- HMAC-SHA256 signature validation via `X-Threadly-Signature` header
- Token generation + expiration management

### Added — Advanced Flow Nodes

#### Loop/ForEach Node
- `ForEachNodeExecutor` — iterate over array session variable
- Execute child subflow N times with each array element in scope
- Break/continue support

#### Subflows / Reusable Blocks
- `SubflowDefinition` entity — encapsulate reusable flow logic
- `SubflowNodeExecutor` — invoke subflow with parameter passing
- Subflow version control independent of parent flow
- Scoped variable passing (input/output mappings)

#### Error Handling Branches
- `ErrorNodeExecutor` — catch errors from upstream nodes
- `FlowRuntime` onError routing — define error handler edges
- Error context (type, message) available in error handler scope

#### Integration Node
- `IntegrationNodeExecutor` — generic executor for 20 pre-built integrations
- `IntegrationPlugin` interface — standardized plugin pattern
- `IntegrationRegistry` — plugin discovery + dynamic loading
- Per-node integration action configuration (Slack post, Gmail send, HubSpot upsert, etc.)

### Added — Integrations (20 Pre-Built)

#### Integration Connection Management
- `IntegrationConnection` entity — store OAuth tokens, refresh token, expiration
- `IntegrationConnectionController` — connect, disconnect, refresh token
- Standardized OAuth 2.0 flow (code → token, refresh on expiration)
- Connection-scoped rate limiting and error tracking

#### Supported Integrations
1. **Slack** — post messages to channels, create threads, add reactions
2. **Gmail** — send emails, draft creation, label management
3. **HubSpot** — create/update contacts, companies, deals
4. **Notion** — create pages, update databases, append blocks
5. **Google Sheets** — append rows, update cells, batch operations
6. **Airtable** — create/update records, bulk operations
7. **Twilio** — send SMS, voice calls, WhatsApp messages
8. **SendGrid** — send emails via template, manage contacts
9. **Mailchimp** — add subscribers, trigger campaigns, manage lists
10. **Shopify** — create orders, update products, manage customers
11. **Discord** — send messages, create threads, manage roles
12. **GitHub** — create issues, push commits, manage repos
13. **Linear** — create issues, update cycles, manage projects
14. **Jira** — create issues, transition tickets, add comments
15. **Stripe** — create charges, manage subscriptions, refunds
16. **Mixpanel** — track events, update user profiles
17. **Segment** — send events, track users, audience sync
18. **Make.com** — trigger scenarios via webhook
19. **Microsoft Teams** — send messages, create channels
20. **Salesforce** — create leads, update accounts, manage opportunities

### Added — CRM Module

#### Lead Management
- `Lead` entity — email, phone, name, status (NEW/QUALIFIED/CONVERTED/LOST), custom fields JSONB
- `LeadController` — CRUD endpoints + bulk operations
- `LeadCaptureController` — auto-capture from widget form submissions
- Lead deduplication on email/phone

#### Lead Enrichment
- `LeadTag` — tagging system for lead segmentation
- `CustomFieldDefinition` — per-bot custom field schema
- `LeadNote` — private notes with author + timestamp
- `LeadTimelineEvent` — interaction history (conversation opened, handoff, email sent, etc.)

#### CRM Pipeline
- Lead status workflow: NEW → QUALIFIED → CONVERTED (or LOST)
- Pipeline stage tracking in `LeadTimelineEvent`
- Bulk status update endpoint

### Added — Email Sequences

#### Sequence Engine
- `EmailSequence` entity — multi-step email campaigns
- `EmailSequenceStep` — individual email with delay, subject, body
- `EmailSequenceEnrollment` — track lead enrollment in sequences
- Trigger on lead creation, lead tag, conversation handoff, or manual enrollment

#### Delivery
- `SequenceStepScheduler` — background job scheduler (Quartz)
- SMTP/SendGrid integration for actual email delivery
- Bounce/complaint handling via webhook callbacks
- Unsubscribe link generation + tracking

### Added — Billing & Subscriptions

#### Stripe Integration
- `BillingController` — checkout session creation, subscription management
- `StripeWebhookController` — handle charge.succeeded, subscription.updated, subscription.deleted
- `BillingService` — create subscription, upgrade/downgrade plan, cancel
- `PlanFeatureGate` — enforce plan limits (conversation count, KB size, members)

#### Usage Metering
- `BillingMeterJob` — periodic job to aggregate daily conversation + KB usage
- `BillingUsage` entity — daily usage snapshots per subscription
- Overage tracking (pay-per-use add-on model)

#### Pricing Plans
- **FREE:** $0/mo, 1 bot, 500 conv/mo, 1 KB (1 GB), 1 member
- **PRO:** $29/mo, 5 bots, 5000 conv/mo, 50 KB (50 GB), 5 members, A/B testing, CSV export
- **BUSINESS:** $99/mo, unlimited bots, 50k conv/mo, unlimited KB, 25 members, white-label, SSO
- **ENTERPRISE:** Custom pricing, dedicated support, SLA

### Added — A/B Testing

#### Test Management
- `AbTest` entity — traffic split %, control/treatment variants, hypothesis
- `AbTestVariant` — flow variant ID + variant name + traffic %
- `AbTestConversion` entity — track conversions per variant (visitor completed flow, etc.)

#### Variant Assignment
- Visitor assignment to variant via murmur3 hash (deterministic, sticky)
- Traffic split enforcement in `FlowRuntime`
- Conversion event capture

#### Results & Reporting
- `GET /v1/ab-tests/{id}/results` — conversion rate, confidence interval, winner declaration
- Chi-square significance testing (95% confidence)

### Added — Frontend Enhancements

#### Bot Management
- `POST /v1/bots/{id}/clone` — deep copy flow, settings, theme, KB
- One-click "Duplicate" button in bot list UI

#### Flow Templates
- 20+ pre-built templates (customer support, FAQ, lead qualification, survey, appointment booking, etc.)
- `GET /v1/templates` — list available templates
- `POST /v1/bots/{id}/from-template` — create new bot from template

#### UI Overhaul
- Dark canvas background (`bg-neutral-950`) inspired by n8n
- Clean sidebar + properties panel (chatbotbuilder.net-style)
- Node catalog with 20+ types (integration, cron, webhook, subflow, foreach, error-handler, ab-test, send-sms, send-slack, create-lead, update-lead, send-sequence)
- Integration marketplace page with OAuth connection flow
- CRM contacts list, pipeline Kanban, contact profile with timeline
- Billing page with plan cards, usage meters, invoice table
- Analytics CSV export + funnel chart (Recharts) + cohort retention analysis
- A/B test management UI (`/bots/[id]/ab-tests` — create test, set traffic split %, view results)
- Email sequence builder (`/sequences` — step builder with delay config)

### Added — Widget Enhancements

#### Lead Capture Form
- Pre-chat form component (email, phone, name, custom fields)
- Configurable required fields per bot
- Form submission to `POST /v1/bots/{id}/leads/capture`

#### CSAT Widget
- Post-conversation 1–5 star satisfaction rating
- `CsatWidget` component in chat footer
- Submission to `POST /v1/conversations/{id}/csat`

#### Knowledge Base Improvements
- URL scraping (`url_scraper.py` — BeautifulSoup + Playwright, `robots.txt` respect)
- Sitemap ingestion (`sitemap_parser.py` — parse XML, enqueue URLs for scraping)
- Cohere reranker (complete implementation, fully opt-in per bot)
- Document metadata indexing (source URL, upload date, language, type)

### Added — Analytics Enhancements

#### Data Export
- CSV export of conversation analytics (date, conversations, messages, cost, handoff %)
- `GET /v1/bots/{id}/analytics/export`

#### Visualization
- Funnel chart (Recharts): conversations started → completed → handed off → closed
- Cohort retention analysis: returning visitor tracking per signup cohort

### Added — Testing & Quality

#### Integration Tests
- `LeadIntegrationTest.java` — CRUD, bulk operations, deduplication
- `BillingIntegrationTest.java` — checkout, webhook events, plan enforcement
- `AbTestIntegrationTest.java` — test creation, traffic split, conversion tracking
- `IntegrationPluginTest.java` — mock external APIs, executor validation
- `EmailSequenceIntegrationTest.java` — enrollment, step scheduling, delivery

#### E2E Tests
- Playwright: full happy path (login → create bot → add nodes → publish → embed → send message → see reply → lead captured)
- Widget e2e: lead form capture, CSAT submission, file uploads

#### Performance
- k6 load test: 500 concurrent widget connections
- Measure message latency under load

### Added — Infrastructure & Ops

#### Database Migrations
- **V7:** `integration_connections` table (org_id, integration_type, access_token, refresh_token, expires_at)
- **V8:** `billing_subscriptions` table (org_id, plan_id, stripe_subscription_id, status, current_period_end)
- **V9:** `billing_usage` table (subscription_id, billing_date, conversation_count, kb_size_gb)
- **V10:** CRM tables (leads, lead_notes, lead_timeline_events, lead_tags, custom_field_definitions)
- **V11:** `subflow_definitions` table
- **V12:** Email sequence tables (email_sequences, email_sequence_steps, email_sequence_enrollments)
- **V13:** `ab_tests`, `ab_test_variants`, `ab_test_conversions`
- **V14:** `csat_ratings` table

#### Docker Compose
- MailHog service (dev SMTP server, port 1025 + 8025 web UI)
- Stripe CLI container (webhook forwarding to `localhost:8080/webhooks/stripe`)

#### Observability
- Grafana dashboard panels: active email sequences, CRM lead funnel, Stripe MRR meter, integration call rate/error
- Application metrics: active sequences, leads created, stripe MRR

#### API Documentation
- Full OpenAPI 3.0 spec update (all Sprint 3 endpoints)
- Swagger UI generated client SDKs (Java, Python, TypeScript)

---

## [Unreleased] — Sprint 2

> Sprint 2 started 2026-05-21. Backend Agent and AI+Widget Agent complete.
> Frontend, Testing, and Tech Lead agents in progress.

### Added — Backend Agent

#### Team Management
- `OrgMembership` entity with roles: `OWNER`, `ADMIN`, `AGENT`
- `TeamController` — invite by email, list members, update role, remove member
- Role-based access enforcement via `@PreAuthorize` annotations
- Flyway V2 migration: `org_memberships` + `api_keys` tables

#### API Keys
- `ApiKey` entity with `tly_live_` prefix and BCrypt-hashed secret
- `ApiKeyController` — create (returns plaintext once), list (hashed), revoke
- `JwtAuthFilter` integration — API key auth as alternative to Bearer JWT
- Keys scoped to org; cannot be used across tenants

#### Webhooks
- `Webhook` entity + `WebhookDelivery` delivery log entity
- `WebhookController` — register endpoint, list, update, delete
- `WebhookDeliveryService` — async HTTP delivery, HMAC-SHA256 `X-Threadly-Signature` header
- Resilience4j retry policy (3 attempts, exponential backoff)
- Flyway V3 migration: `webhooks` + `webhook_deliveries` tables

#### Flow Import/Export
- `GET /v1/bots/{id}/flow/export` — download flow JSON (Content-Disposition: attachment)
- `POST /v1/bots/{id}/flow/import` — upload flow JSON, overwrites draft

#### Node Executors
- `DelayNodeExecutor` — configurable pause (seconds) via `Thread.sleep` or scheduled future
- `SwitchNodeExecutor` — multi-branch switch on session variable value
- `SendEmailNodeExecutor` — SMTP via Spring Mail, subject/body template engine support
- `CollectInputNodeExecutor` — waits for visitor reply, stores into named session variable

#### Conversation Bulk Operations
- `POST /v1/conversations/bulk-close` — close multiple conversations by ID array
- `POST /v1/conversations/bulk-assign` — assign multiple conversations to agent by ID
- `GET /v1/conversations/export` — CSV export with headers: id, visitor_id, status, opened_at, closed_at, message_count

#### Per-Bot Analytics API
- `GET /v1/bots/{id}/analytics/summary` — total conversations, messages, handoff rate, avg duration
- `GET /v1/bots/{id}/analytics/daily` — daily message count time series (last 30 days)
- `GET /v1/bots/{id}/analytics/funnel` — conversations started → completed → handed off → closed

#### Credentials Store
- `Credential` entity — name, encrypted value, iv, type
- AES-256-GCM encryption with PBKDF2-derived key (org-scoped salt)
- `CredentialsController` — store, list names (no value exposure), delete
- Credentials referenceable in `ApiCallNodeExecutor` via `{{credential:name}}` template syntax
- Flyway V4 migration: `credentials` table

#### Security Hardening
- `SecurityHeadersFilter` — sets `Content-Security-Policy`, `Strict-Transport-Security`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`
- `RateLimitFilter` — Bucket4j + Redis: 10 req/min on `/v1/auth/*`, 1000 req/min per `orgId`
- Rate limit headers: `X-RateLimit-Remaining`, `X-RateLimit-Reset`
- HTTP 429 response with `Retry-After` header on limit exceeded

#### Events
- `Event` entity — domain events (conversation_started, message_received, handoff_requested, conversation_closed)
- Flyway V5 migration: `events` table with `bot_id`, `org_id`, `event_type`, `occurred_at`, `payload` JSONB

---

### Added — AI+Widget Agent

#### Multi-Provider LLM
- `AnthropicProvider` — streaming via Anthropic SDK (claude-3-5-sonnet-20241022)
- `OpenAIProvider` — streaming fallback via OpenAI SDK (gpt-4o)
- `ProviderChain` — tries primary provider, falls back to secondary on timeout/error
- Provider selection configurable per bot via `preferredProvider` field

#### Cost Tracking
- `COSTS` table: `call_id`, `provider`, `model`, `input_tokens`, `output_tokens`, `input_cost_usd`, `output_cost_usd`, `created_at`
- `CostTracker` — records cost after every LLM call using provider-specific $/token rates
- Cost visible in Analytics API summary response

#### Hybrid RAG
- BM25 sparse retriever (in-memory index per bot, rebuilt on ingestion)
- Reciprocal Rank Fusion (RRF) merging dense Qdrant results + BM25 results
- Optional Cohere reranker (`COHERE_API_KEY` in env, enabled per-request via `rerank=true` param)
- Improved chunking: 512-token chunks, 50-token overlap (up from 256/0)
- Ingestion progress callbacks to `POST /v1/internal/kb/{docId}/status`

#### Citations
- `Citation` model: `index`, `document_name`, `page_number`, `passage` (first 200 chars)
- Inline citation markers `[1]`, `[2]` inserted into AI response text
- Citation list appended to `/ai/complete` streaming response after `[DONE]` marker
- `CitationFormatter` — deduplicates citations, sorts by first appearance

#### AI Utility Routes
- `POST /ai/summarize` — summarizes conversation transcript into 2-3 sentences
- `POST /ai/suggest-replies` — returns 3 suggested agent reply options (JSON array)
- `POST /ai/extract-entities` — NER extraction (name, email, phone, company, topic)
- `POST /ai/classify-intent` — intent classification with confidence score

#### Langfuse Tracing
- `LangfuseClient` — wraps every LLM call with trace + generation spans
- Traces include: model, provider, prompt tokens, completion tokens, latency, cost
- Flush on process shutdown via lifespan handler
- `LANGFUSE_PUBLIC_KEY` + `LANGFUSE_SECRET_KEY` + `LANGFUSE_HOST` in env

#### Widget: Rich Messages
- `ButtonMessage` — renders array of button options; click sends value to flow
- `CardMessage` — image + title + subtitle + action button
- `QuickReplies` — horizontal chip row for fast response options
- File attachment rendering — download link with icon + filename + size

#### Widget: File Upload
- XHR upload with progress bar (0–100% fill animation)
- `POST /v1/bots/{id}/kb/upload` integration for in-chat document submission
- Accepts: PDF, DOCX, TXT, PNG, JPG (configurable via `allowedTypes` widget config)
- Max 10 MB enforced client-side with user-friendly error

#### Widget: UX Improvements
- Unread badge — red dot on launcher when tab is hidden and new message arrives
- Web Audio API notification sound (short 440 Hz beep, respects system mute)
- Message timestamps shown on hover (`HH:mm` format, local timezone)
- Delivery status indicators: single ✓ (sent), double ✓✓ (read by agent)
- Conversation persistence via `sessionStorage` (survives page navigation, max 200 messages)

#### Widget: Theme System
- 12 CSS custom properties: `--tly-primary`, `--tly-bg`, `--tly-text`, `--tly-border`, `--tly-shadow`, `--tly-radius`, `--tly-font`, `--tly-launcher-size`, `--tly-panel-width`, `--tly-panel-height`, `--tly-z-index`, `--tly-header-height`
- `darkMode` config: `"auto"` (follows `prefers-color-scheme`), `"light"`, `"dark"`
- All colors override-able via `data-theme-*` attributes on the script tag

---

### In Progress — Frontend Agent

- n8n-inspired node catalog with categories and search
- PropertiesPanel supporting 12 input types (text, number, select, multiselect, code, json, condition-builder, email-template, variable-picker, credential-picker, toggle, slider)
- Analytics page with Chart.js bar/line charts and funnel visualization
- Conversation inbox redesign with virtual scrolling (react-virtual)
- Onboarding wizard: step 1 (create bot) → step 2 (install snippet) → step 3 (go live)
- Credentials manager UI (list, create, delete)
- Command palette enhancements (recent actions, keyboard navigation improvements)
- Theme preview live panel in bot settings

---

### In Progress — Testing Agent

- JUnit5 + Testcontainers integration tests: auth flows, bot CRUD, flow publish/rollback
- Tenant isolation tests: verifying cross-org data access is blocked
- Pytest unit tests for all AI routes
- Playwright E2E: signup → create bot → publish flow → widget chat
- Vitest unit tests for widget theme system and ws-client

---

### In Progress — Tech Lead Agent

- `FlowSchemaValidator` with full JSON Schema (required fields per node type)
- `ConditionNodeExecutor` — 12 operators: `eq`, `neq`, `gt`, `lt`, `gte`, `lte`, `contains`, `startsWith`, `endsWith`, `in`, `notIn`, `regex`
- HMAC signing on all Core→AI internal requests
- `AuditLog` entity: `entity_type`, `entity_id`, `action`, `actor_id`, `old_value` JSONB, `new_value` JSONB, `occurred_at`
- Flyway V6 migration: `audit_log` table
- `railway.toml` for Railway.app deployment
- Cloudflare Worker for widget CDN edge caching
- Grafana production dashboard (request rate, error rate, latency p50/p99, active conversations, LLM cost/day)
- `ThreadlyMetrics` custom Micrometer meters: `threadly.messages.total`, `threadly.conversations.active`, `threadly.llm.cost.usd`, `threadly.handoffs.total`
- Production `application-prod.yml` profile

---

## [0.1.0] — 2026-05-21

> Phase 0 MVP — full system from scratch. All services functional and wired together.

### Added — Infrastructure

- `infra/docker-compose.yml` — 10 services: Postgres 16, Redis 7, Qdrant, MinIO, Centrifugo v5, Prometheus, Grafana, threadly-core, threadly-ai, threadly-web
- `infra/centrifugo/config.json` — namespaces, proxy hooks, JWT auth
- `infra/test-embed/index.html` — widget integration test page
- `infra/grafana/` — provisioning YAML + `threadly-overview.json` dashboard
- `scripts/bootstrap.sh` — JWT RS256 key generation, Docker pull, MinIO bucket creation
- `scripts/seed-demo-bot.sh` — seeds demo org + bot + example flow
- `scripts/codegen-api.sh` — Orval codegen runner
- `Makefile` — `make up`, `make down`, `make logs`, `make codegen`, `make test`
- `.github/workflows/ci.yml` — core/ai/web/widget build + test + Docker smoke test

### Added — threadly-core (Spring Boot 3.3, Java 21)

- Modular monolith with 11 modules: identity, workspace, flow, runtime, conversation, realtime, knowledge, analytics, common, ai-client, api
- `V1__init.sql` — 10 core tables: orgs, users, refresh_tokens, bots, flows, flow_versions, sessions, conversations, messages, kb_documents
- `TenantContext` ThreadLocal + `TenantFilterAspect` + Hibernate `@Filter` on all org-owned entities
- `ApiError` RFC 7807 Problem+JSON response shape
- `GlobalExceptionHandler` — handles validation, auth, not found, and unexpected errors
- `IdempotencyInterceptor` — Redis 24h dedup by `Idempotency-Key` header
- `CorsConfig` — wildcard for widget origins, pattern-matched for dashboard origins
- `S3Config` — MinIO / AWS SDK v2 S3 client bean
- JWT RS256 auth: `JwtService` (PEM key loading), `AuthService` (signup/login/refresh/logout), `AuthController`, `JwtAuthFilter`, `SecurityConfig`
- `/v1/auth/me` — returns current user + org membership
- `Bot` entity + `BotService` (CRUD, embed snippet, theme update)
- `BotController` — full CRUD + `PATCH /theme` + `GET /snippet`
- `Flow` + `FlowVersion` entities; `FlowService` (getDraft, saveDraft, publish, rollback); `FlowController`
- Flow JSON validation on publish: checks nodes array non-empty + start node present
- `FlowRuntime` — main interpreter loop; `FlowGraph` — adjacency list traversal
- `NodeExecutor` SPI + `NodeExecutorFactory` registry
- `MessageNodeExecutor` — sends text to Centrifugo channel
- `QuestionNodeExecutor` — waits for visitor input, captures to session variable
- `AiReplyNodeExecutor` — calls `AiClient.streamComplete()`, streams tokens to Centrifugo
- `ConditionNodeExecutor` — eq/neq/gt/lt operators, two output handles (true/false)
- `HandoffNodeExecutor` — sets conversation status to `HANDED_OFF`, sends Centrifugo event
- `EndNodeExecutor` — terminates flow, sets conversation status to `COMPLETED`
- `ApiCallNodeExecutor` — WebClient GET/POST, stores response body into session variable
- `SetVariableNodeExecutor` — evaluates template expressions, writes to session variables
- `TemplateEngine` — `{{variable}}` and `{{credential:name}}` substitution
- `Session` entity + `SessionRepository` — `variables` JSONB, Redis-backed active sessions
- `Conversation` + `Message` entities; `ConversationController` (list, get, messages, handoff, close, agent reply)
- `LiveCounterController` — SSE endpoint pushing live dashboard stats every 5 seconds
- `CentrifugoClient` — HTTP publish API, circuit breaker (Resilience4j)
- `RealtimeController` — dashboard token + visitor token + widget fallback
- `CentrifugoProxyController` — connect/subscribe/publish hooks
- `CentrifugoProxyService` — channel auth, route publish events to `FlowRuntime`
- `OutboxService` — async Centrifugo publish with retry
- `KbDocument` entity; `KbService` (upload to MinIO, dispatch ingestion job); `KbController`
- `KbIngestionJob` — `@Scheduled` poller, POSTs to `POST /kb/ingest` (threadly-ai)
- `KbIngestionCallbackController` — receives ingestion status from threadly-ai
- `AnalyticsController` — `/stats` with total messages, p50 response time, active bots
- `AiClient` — WebClient streaming call to threadly-ai `/ai/complete`
- `TokenBudgetService` — Redis INCR + 24h TTL, per-org daily token budget enforcement

### Added — threadly-ai (Python FastAPI)

- FastAPI app with lifespan (Qdrant collection init, Langfuse flush on shutdown)
- `config.py` — Pydantic BaseSettings for all env vars
- `AnthropicProvider` — streaming inference (claude-3-5-sonnet)
- `OpenAIProvider` — streaming fallback (gpt-4o)
- `get_provider()` factory with auto-fallback on error
- `embed_texts()` — Voyage AI primary, OpenAI `text-embedding-3-small` fallback
- `ingest_document()` — parse (PyMuPDF/docx), chunk (LangChain RecursiveCharacterTextSplitter), embed, upsert Qdrant
- `query_kb()` — embed query, Qdrant nearest-neighbor search, format passages
- `build_messages()` — message history trim to context window, RAG passage injection
- `build_system_prompt()` — variable substitution, persona injection
- `POST /ai/complete` — streaming SSE route (text/event-stream)
- `POST /kb/ingest` — document ingestion with status callback
- `POST /kb/query` — retrieval-only route for explicit queries
- `GET /health` — readiness probe
- HMAC-SHA256 service secret authentication on all routes
- Langfuse tracing: trace + generation span per `/ai/complete` call

### Added — threadly-web (Next.js 15, React 19)

- Next.js 15 App Router project with TypeScript strict mode
- Tailwind CSS v4, Geist fonts, Sonner toast notifications
- Auth.js v5 credentials provider (delegates to `/v1/auth/login`)
- Middleware route protection (redirects unauthenticated to `/login`)
- TanStack Query v5 for all server state
- Orval-generated React Query hooks from OpenAPI spec
- `lib/api.ts` — fetch wrapper with Bearer token injection + refresh on 401
- Landing page at `/` with hero, features, pricing sections
- Login page at `/login`, signup page at `/signup`
- App shell: sidebar navigation, org switcher, dark/light theme toggle
- `CommandPalette` — Cmd+K global search (bots, conversations, settings)
- Dashboard with stat cards (messages, conversations, handoffs) + bot list + recent conversations
- Live counter updates via EventSource on `/v1/analytics/live`
- Bot list page: create, delete, embed snippet copy
- Bot settings page: accent color picker, avatar upload, greeting text, live theme preview
- Flow builder: React Flow v12 canvas, node palette, `PropertiesPanel`, autosave (800ms debounce), versions drawer, undo/redo (Cmd-Z, 100-entry history), live preview pane, node validation badges
- Custom node components: Message, Question, AiReply, Condition, Handoff, End, ApiCall, SetVariable
- Conversations inbox: 3-pane layout (list / transcript / details), status filter, search, Take Over / Resume AI / Close actions, agent reply input
- Centrifugo live updates in inbox: new conversation toast, new message auto-scroll
- KB management: drag-and-drop upload, URL ingestion, live status polling (auto-refresh while PROCESSING)
- Settings pages: workspace info + embed snippet, team members, API keys
- Biome linting + formatting configuration

### Added — threadly-widget (Preact)

- Preact IIFE bundle via Vite, Terser minification, brotli + gzip compression
- `widget.ts` loader — reads `data-bot-id`, `data-theme-*` attributes from script tag
- `src/ws-client.ts` — Centrifugo JS client v5, exponential reconnect, localStorage offline queue
- `ChatPanel.tsx` — messages list, streaming token rendering, typing indicator
- `main.tsx` — launcher button (circle + Threadly icon), slide-up animation, open/close state
- `theme.ts` — CSS-in-JS `injectStyles()` with all widget styles
- `types.ts` — `WidgetConfig`, `ChatMessage`, `ServerEvent` TypeScript types
- Offline queue: messages buffered in `localStorage.tly_queue`, replayed on reconnect
- Dark mode: `config.darkMode` + CSS class toggle based on `prefers-color-scheme`
- Mobile bottom-sheet animation: `tly-sheet-in` keyframe, `entering` class
- WCAG AA accessibility: `role=dialog`, `aria-modal`, `role=log`, `aria-live=polite`, focus trap, Escape key to close
- Widget dev test page at `index.html`
