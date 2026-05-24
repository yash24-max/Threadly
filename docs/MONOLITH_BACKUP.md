# Threadly Monolith Backup Documentation

> **Status**: Archived 2026-05-24  
> **Reason**: Refactored from monolithic Spring Boot application to 9 distributed microservices  
> **Preserved for reference**: Yes — this document captures the old architecture for rollback/reference

---

## Old Monolith Structure

The original Threadly core was a single Spring Boot 3.3 modular monolith with 11 domain modules:

### Package Structure

```
dev.threadly.core/
├── identity/              # Users, orgs, auth, JWT, invites
├── workspace/             # Bots, team management, settings
├── flow/                  # Flow definitions, versioning, publishing
├── runtime/               # Session execution, node executors, variables
├── conversation/          # Transcripts, messages, leads, handoff
├── knowledge/             # KB documents, RAG, embeddings, ingestion
├── analytics/             # Events, metrics, dashboards
├── agent/                 # Human takeover, agent inbox
├── billing/               # Stripe, subscriptions, metering
├── outbox/                # Transactional outbox for events
├── proxy/                 # Centrifugo proxy endpoints
└── common/                # Shared: tenancy, security, errors
```

### Key Classes (Pre-Refactor)

#### Identity Module
- **User.java** — User entity with email, oauth_provider, password_hash
- **UserRepository.java** — JPA repo with custom queries
- **AuthService.java** — Login, signup, password reset logic
- **AuthController.java** — REST endpoints (/auth/login, /auth/signup, /auth/refresh)
- **JwtService.java** — Token generation (RS256), claims extraction
- **JwtAuthFilter.java** — Spring Security filter for JWT validation
- **SecurityConfig.java** — Spring Security + JWT configuration
- **RefreshToken.java** — Refresh token entity for rotation
- **OrgMembership.java** — User ↔ Org relationship (owner/admin/member)
- **TeamController.java** — Team management endpoints
- **InviteEmailService.java** — Invite email dispatch (Resend)

#### Workspace Module
- **Bot.java** — Bot entity (name, language, accent_color, webhook_signing_key, integrations JSONB)
- **BotRepository.java** — CRUD + custom queries
- **WorkspaceService.java** — Bot operations, settings management
- **WorkspaceController.java** — REST endpoints (/bots, /workspace/settings)
- **BotMetricsSnapshot.java** — Daily metrics (conversation_count, response_time, api_calls)

#### Flow Module
- **Flow.java** — Flow definition (name, flow_json, description)
- **FlowVersion.java** — Published versions with timestamps
- **FlowTrigger.java** — Webhook/cron/manual triggers
- **FlowTemplate.java** — Reusable flow templates
- **FlowService.java** — CRUD, validation, publishing logic
- **FlowController.java** — REST endpoints (/flows, /flows/{id}, /flows/{id}/publish)
- **FlowValidator.java** — JSON schema validation, edge/node references

#### Runtime Module
- **Session.java** — Session entity (visitor_id, bot_id, variables JSONB, status)
- **SessionRepository.java** — JPA repo with custom queries
- **NodeExecutionResult.java** — Result of node execution (nextNodeId, output, error)
- **NodeExecutor.java** — Interface for node execution strategy pattern
- **FlowInterpreter.java** — Main flow execution engine (reads flow JSON, executes nodes)
- **RuntimeService.java** — Session management, message handling
- **RuntimeController.java** — REST endpoints (/sessions, /sessions/{id}/message)

##### Node Executors (Strategy Pattern)
- **MessageNodeExecutor.java** — Display text message
- **QuestionNodeExecutor.java** — Collect user input
- **ConditionNodeExecutor.java** — If/else branching
- **AIReplyNodeExecutor.java** — LLM integration (calls threadly-ai)
- **APICallNodeExecutor.java** — External API calls
- **HandoffNodeExecutor.java** — Human agent takeover
- **LoopNodeExecutor.java** — Array iteration with loop.item/index/total
- **SubflowNodeExecutor.java** — Nested flow execution
- **IntegrationNodeExecutor.java** — Third-party integrations (Slack, etc.)
- **ErrorNodeExecutor.java** — Error handling (retry, fallback)

#### Conversation Module
- **Conversation.java** — Chat thread entity (bot_id, visitor_id, status, assigned_agent_id)
- **Message.java** — Individual message (sender_type: visitor/bot/human_agent, content, metadata)
- **Lead.java** — CRM lead (email, name, phone, custom_fields JSONB, status, tags)
- **LeadActivity.java** — Timeline events (contacted, emailed, call, note, status_changed)
- **ConversationRepository.java** — Query by bot/visitor/status
- **ConversationService.java** — Transcript management, search, export
- **ConversationController.java** — REST endpoints (/conversations, /conversations/{id}, /conversations/{id}/export)
- **LeadCaptureService.java** — Lead creation, CRM integration hooks

#### Knowledge Module
- **KbDocument.java** — Document metadata (file_path, status, chunk_count, indexed_at)
- **KbChunk.java** — Document chunks (content, token_count, qdrant_point_id)
- **KbIngestionJob.java** — Async ingestion tracking (status, progress_percent, error)
- **KnowledgeService.java** — Upload, ingest, query RAG
- **KnowledgeController.java** — REST endpoints (/kb/documents, /kb/upload, /kb/query, /kb/scrape-url)
- **KnowledgeIndexer.java** — Chunking, embedding, Qdrant upsert
- **QdrantClient.java** — Qdrant vector DB integration (hybrid search, reranking)

#### Analytics Module
- **AnalyticsEvent.java** — Event entity (event_type, event_data JSONB)
- **DailyMetrics.java** — Daily aggregations (conversation_count, response_time, cost)
- **ConversationCosts.java** — Per-conversation LLM/embedding costs
- **AnalyticsService.java** — Event ingestion, aggregation, queries
- **AnalyticsController.java** — REST endpoints (/dashboard/*, /export/*)
- **EventProcessor.java** — Kafka consumer for events (from outbox)
- **MetricsAggregator.java** — Daily rollup job (Spring @Scheduled)

#### Agent Module (Human Handoff)
- **AgentAssignment.java** — Agent ↔ Conversation assignment
- **AgentService.java** — Takeover logic, agent inbox
- **AgentController.java** — REST endpoints (/agents/inbox, /agents/{id}/assign)

#### Billing Module
- **Plan.java** — Pricing tier (FREE, PRO $29, BUSINESS $99, ENTERPRISE)
- **Subscription.java** — Customer subscription (plan_id, stripe_subscription_id, status)
- **UsageRecord.java** — Metered usage (metric_name, value, period_start/end)
- **Invoice.java** — Stripe invoice tracking
- **BillingService.java** — Stripe webhook handling, metering
- **BillingController.java** — REST endpoints (/billing/*, /webhook/stripe)
- **StripeWebhookHandler.java** — Webhook signature verification, event routing

#### Outbox Module (Transactional Outbox Pattern)
- **OutboxEvent.java** — Event entity (eventType, aggregateId, payload JSONB, publishedAt)
- **OutboxRepository.java** — Find unpublished events
- **OutboxService.java** — Publish event (saves to DB + outbox in same transaction)
- **OutboxPoller.java** — Spring @Scheduled job (every 5s) that publishes unpublished events to Kafka

#### Proxy Module (Centrifugo Integration)
- **CentrifugoProxyController.java** — POST /proxy/connect, /proxy/subscribe, /proxy/publish
- **CentrifugoProxyService.java** — Centrifugo authentication, proxy logic
- **RealtimeService.java** — WebSocket message publishing to Centrifugo channels

#### Common Module (Cross-Cutting)
- **TenantContext.java** — Thread-local org_id holder
- **TenantFilter.java** — Hibernate @Filter for automatic org_id filtering
- **ErrorModel.java** — RFC 7807 Problem+JSON responses
- **IdempotencyKeyHandler.java** — @Aspect for POST/PATCH deduplication
- **FeignConfig.java** — (later) Feign client setup
- **Resilience4jConfig.java** — (later) Circuit breaker configuration
- **OpenTelemetryConfig.java** — Distributed tracing auto-configuration

---

## Old Database Schema (Single Schema: `public`)

### Core Tables
- **users** — id, email, password_hash, oauth_provider, oauth_id, email_verified, created_at, updated_at
- **organizations** — id, name, slug, owner_id (FK users), subscription_plan, billing_email
- **memberships** — id, org_id (FK org), user_id (FK user), role, created_at
- **api_keys** — id, org_id (FK org), name, key_hash, scopes, last_used_at

### Bot Management
- **bots** — id, org_id, name, description, language, accent_color, avatar_url, welcome_message, kb_search_enabled, integrations (JSONB), webhook_signing_key, status
- **workspace_settings** — id, org_id (UNIQUE), custom_domain, custom_branding (JSONB), sso_config (JSONB), rate_limits (JSONB)
- **bot_metrics_snapshot** — id, bot_id, date, conversation_count, unique_visitors, api_calls, kb_queries, avg_response_time_ms

### Flow Management
- **flows** — id, bot_id, org_id, name, description, flow_json (JSONB)
- **flow_versions** — id, flow_id, version_number, flow_json (JSONB), published_by_id, published_at, is_published
- **flow_triggers** — id, flow_id, trigger_type (webhook/cron/manual), trigger_config (JSONB)
- **flow_templates** — id, org_id, name, description, category, flow_json (JSONB), thumbnail_url, is_public

### Runtime
- **sessions** — id, bot_id, org_id, visitor_id, flow_id, current_node_id, variables (JSONB), status, started_at, last_activity_at, ended_at
- **session_snapshots** — id, session_id, node_id, variables (JSONB), execution_context (JSONB), timestamp
- **node_executions** — id, session_id, node_id, node_type, execution_input (JSONB), execution_output (JSONB), status, error_message, duration_ms, executed_at

### Conversations & Leads
- **conversations** — id, bot_id, org_id, visitor_id, started_at, ended_at, status, assigned_agent_id, is_lead, lead_id
- **messages** — id, conversation_id, sender_type (visitor/bot/human_agent/system), sender_id, sender_name, content, message_type (text/image/file/form), metadata (JSONB), created_at
- **leads** — id, org_id, bot_id, conversation_id, email, phone, name, custom_fields (JSONB), tags (TEXT[]), status (new/contacted/qualified/converted/lost), pipeline_stage, created_at, updated_at
- **lead_activities** — id, lead_id, activity_type (contacted/email_sent/call/note/status_changed), description, created_at, created_by_id

### Knowledge Base
- **kb_documents** — id, bot_id, org_id, document_name, document_type (pdf/txt/url/html), source_url, file_path, file_size_bytes, page_count, status (pending/processing/active/error), error_message, chunk_count, indexed_at
- **kb_chunks** — id, kb_document_id, chunk_number, content, token_count, metadata (JSONB), qdrant_point_id
- **kb_ingestion_jobs** — id, org_id, kb_document_id, job_type (parse/embed/reindex), status (pending/processing/completed/failed), progress_percent, error_message, started_at, completed_at

### Analytics
- **analytics_events** — id, org_id, bot_id, event_type, event_data (JSONB), created_at
- **daily_metrics** — id, org_id, bot_id, date, conversation_count, unique_visitors, message_count, api_calls, kb_queries, avg_response_time_ms, handoff_count, lead_count, cost_usd, satisfaction_score
- **conversation_costs** — id, conversation_id, bot_id, org_id, llm_tokens, llm_cost_usd, embedding_calls, embedding_cost_usd, total_cost_usd

### Billing
- **plans** — id, name (free/pro/business/enterprise), price_usd, billing_period (monthly/annual), features (JSONB), stripe_product_id, stripe_price_id
- **subscriptions** — id, org_id (UNIQUE), plan_id, stripe_subscription_id, stripe_customer_id, status (active/past_due/canceled/trialing), current_period_start, current_period_end, trial_end, cancel_at_period_end, auto_renew
- **usage_records** — id, subscription_id, org_id, metric_name, value, period_start, period_end, recorded_at
- **invoices** — id, subscription_id, stripe_invoice_id, amount_usd, status (draft/open/paid/uncollectible/void), issued_at, due_at, paid_at

### Integrations
- **integrations** — id, org_id, name, type (slack/gmail/hubspot/etc.), display_config (JSONB), status (active/inactive), created_at, updated_at
- **integration_credentials** — id, integration_id, credential_type (api_key/oauth_token/webhook_secret), encrypted_value (AES-256), expires_at
- **integration_actions_log** — id, integration_id, action_name, action_params (JSONB), result (JSONB), status (success/error), error_message, duration_ms, executed_at
- **integration_oauth_state** — id, state_token (UNIQUE), org_id, integration_type, return_url, created_at, expires_at

### Outbox (Event Reliability)
- **outbox** — id, event_type, aggregate_id, payload (JSONB), published_at, created_at

---

## Old Migrations (Flyway)

| Version | Purpose |
|---------|---------|
| V1__init.sql | Initial schema (users, orgs, memberships, api_keys) |
| V2__bots.sql | Bot management (bots, workspace_settings, metrics) |
| V3__flows.sql | Flow definitions (flows, flow_versions, triggers, templates) |
| V4__runtime.sql | Session execution (sessions, snapshots, node_executions) |
| V5__conversations.sql | Transcripts (conversations, messages, leads, activities) |
| V6__kb.sql | Knowledge base (kb_documents, chunks, ingestion_jobs) |
| V7__integrations.sql | Integration connectors (integrations, credentials, logs) |
| V8__leads_crm.sql | Enhanced CRM (leads status workflow, custom fields, tags) |
| V9__billing.sql | Billing (plans, subscriptions, usage, invoices) |

---

## Old Dependencies (pom.xml Monolith)

```xml
<!-- Spring Boot -->
<spring-boot-starter-web>
<spring-boot-starter-webflux>
<spring-boot-starter-security>
<spring-boot-starter-data-jpa>
<spring-boot-starter-data-redis>
<spring-boot-starter-validation>
<spring-boot-starter-actuator>
<spring-boot-starter-cache>

<!-- Database -->
<postgresql>
<flyway-core>
<flyway-database-postgresql>

<!-- JWT -->
<jjwt-api>, <jjwt-impl>, <jjwt-jackson>

<!-- Mapping & Utils -->
<mapstruct>
<lombok>

<!-- Resilience -->
<resilience4j-spring-boot3>
<resilience4j-reactor>

<!-- Rate Limiting -->
<bucket4j-core>, <bucket4j-redis>

<!-- Caching -->
<caffeine>

<!-- Cloud & Messaging -->
<aws-sdk-s3>
<spring-cloud-starter-consul>
<spring-kafka>

<!-- OpenAPI & Monitoring -->
<springdoc-openapi-starter-webmvc-ui>
<micrometer-registry-prometheus>
<micrometer-tracing-bridge-otel>
<opentelemetry-exporter-otlp>

<!-- Third-Party APIs -->
<stripe-java>
<cron-parser-spring>
<spring-boot-starter-mail>
<hypersistence-utils-hibernate-63>

<!-- Testing -->
<spring-boot-starter-test>
<spring-security-test>
<testcontainers-junit-jupiter>
<testcontainers-postgresql>
```

---

## Old API Endpoints (Summary)

### Auth (11 endpoints)
- POST /auth/signup
- POST /auth/login
- POST /auth/refresh
- POST /auth/logout
- GET /me
- POST /password-reset
- POST /password-reset/confirm
- GET /email-verification/{token}
- POST /oauth/callback/{provider}
- POST /apikeys
- DELETE /apikeys/{keyId}

### Workspace (16 endpoints)
- GET /orgs/{orgId}/members
- POST /orgs/{orgId}/members
- DELETE /orgs/{orgId}/members/{userId}
- GET /bots
- POST /bots
- GET /bots/{botId}
- PATCH /bots/{botId}
- DELETE /bots/{botId}
- GET /bots/{botId}/metrics
- GET /workspace/settings
- PATCH /workspace/settings
- POST /workspace/invite
- GET /workspace/integrations

### Flow (18 endpoints)
- GET /flows
- POST /flows
- GET /flows/{flowId}
- PATCH /flows/{flowId}
- DELETE /flows/{flowId}
- POST /flows/{flowId}/publish
- GET /flows/{flowId}/versions
- POST /flows/{flowId}/versions/{versionNum}/rollback
- POST /flows/{flowId}/clone
- POST /flows/{flowId}/test
- GET /templates
- POST /flows/from-template/{templateId}
- POST /flows/{flowId}/triggers
- DELETE /flows/{flowId}/triggers/{triggerId}

### Runtime (11 endpoints)
- POST /sessions/{botId}/start
- POST /sessions/{sessionId}/message
- GET /sessions/{sessionId}
- POST /sessions/{sessionId}/pause
- POST /sessions/{sessionId}/resume
- POST /sessions/{sessionId}/reset
- GET /sessions/{botId}
- GET /realtime/token
- POST /proxy/connect
- POST /proxy/subscribe
- POST /proxy/publish

### Conversations (18 endpoints)
- GET /conversations
- GET /conversations/{conversationId}
- GET /conversations/{conversationId}/messages
- POST /conversations/{conversationId}/takeover
- POST /conversations/{conversationId}/message
- POST /conversations/{conversationId}/close
- GET /leads
- POST /leads
- PATCH /leads/{leadId}
- GET /leads/{leadId}/activities
- POST /leads/{leadId}/activities
- GET /conversations/{botId}/export

### Knowledge Base (10 endpoints)
- GET /bots/{botId}/kb/documents
- POST /bots/{botId}/kb/upload
- POST /bots/{botId}/kb/scrape-url
- POST /bots/{botId}/kb/scrape-sitemap
- DELETE /bots/{botId}/kb/documents/{docId}
- GET /bots/{botId}/kb/jobs
- POST /bots/{botId}/kb/query
- POST /bots/{botId}/kb/reindex

### Analytics (9 endpoints)
- GET /dashboard/summary
- GET /dashboard/conversations
- GET /dashboard/cost
- GET /dashboard/funnel
- GET /dashboard/top-intents
- GET /export/conversations
- GET /export/costs

### Billing (8 endpoints)
- GET /subscription
- POST /subscription/upgrade/{planId}
- POST /subscription/cancel
- GET /billing/usage
- GET /billing/invoices
- POST /billing/payment-method
- POST /webhook/stripe (private)

### Integrations (8 endpoints)
- GET /integrations/marketplace
- GET /integrations/{orgId}
- POST /integrations/{orgId}/{type}/auth
- GET /integrations/oauth/callback
- POST /integrations/{integrationId}/test
- DELETE /integrations/{integrationId}
- POST /integrations/{integrationId}/action/{actionName}

---

## Kafka Topics (Pre-Refactor)

```
user-events              → user.created, user.email_verified
org-events               → org.created, membership.updated
flow-events              → flow.published, flow.updated, flow.deleted
session-events           → session.started, session.message, session.node.executed, session.completed, session.error
conversation-events      → conversation.started, conversation.message.added, conversation.handoff, conversation.closed
kb-events                → kb.document.uploaded, kb.ingestion.*, kb.reindexed
analytics-events         → (all domain events for aggregation)
billing-events           → billing.subscription.*, billing.invoice.issued, billing.overage.charged
integration-events       → integration.connected, integration.action.executed, integration.error
```

---

## Migration Path to Microservices

The monolith has been refactored into **9 distributed microservices**:

| Old Module | New Service | Port |
|---|---|---|
| identity | identity-service | 3001 |
| workspace | workspace-service | 3002 |
| flow | flow-service | 3003 |
| runtime | runtime-service | 3004 |
| conversation | conversation-service | 3005 |
| knowledge | knowledge-service | 3006 |
| analytics | analytics-service | 3007 |
| billing | billing-service | 3008 |
| integration | integration-service | 3009 |
| proxy | (Centrifugo + runtime-service) | — |
| outbox | (threadly-common-spring) | — |
| common | threadly-common-spring | — |

**Shared library**: `threadly-common-spring` consolidates tenancy, security, event patterns, Feign clients.

---

## Key Files for Reference

If you need to understand old implementation details:

1. **Old execution logic** → Runtime Module classes (FlowInterpreter.java, NodeExecutor implementations)
2. **Old multi-tenancy** → Common Module (TenantContext, TenantFilter)
3. **Old event patterns** → Outbox Module (OutboxService.java, OutboxPoller.java)
4. **Old Centrifugo integration** → Proxy Module (CentrifugoProxyController.java)
5. **Old API contracts** → Each module's Controller classes (AuthController.java, etc.)

---

## Rollback Plan

If needed to revert to monolith:

1. Restore this directory from git: `git checkout HEAD~1 -- threadly-core/src threadly-core/pom.xml`
2. Run old migrations: `flyway migrate`
3. Rebuild monolith: `mvn clean install`
4. Start: `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"`

However, data migrated to microservices will need to be re-consolidated if rolling back beyond Phase 1.

---

**Created**: 2026-05-24  
**Reason**: Microservices refactoring complete  
**Preserved By**: Tech Lead team during architecture transition
