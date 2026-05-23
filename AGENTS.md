# Threadly — Agent Coordination

> Inter-agent contracts, domain ownership, git worktree workflow, and code quality gates.
> Updated: 2026-05-23

---

## Agent Roster

### 1. Backend Agent

**Domain:** threadly-core (Spring Boot Java 21)

**Responsibilities:**
- All REST controllers, services, repositories
- Database entities and Flyway migrations
- Security: JWT auth, RBAC, rate limiting, API keys
- Webhook delivery with retry
- Credentials store
- Conversation bulk operations and CSV export
- Per-bot analytics API

**Key Files:**
```
threadly-core/src/main/java/dev/threadly/core/
  auth/           AuthController, AuthService, JwtService, JwtAuthFilter
  workspace/      BotController, BotService, Bot (entity)
  flow/           FlowController, FlowService, Flow, FlowVersion
  runtime/        FlowRuntime, FlowGraph, NodeExecutor SPI, *NodeExecutor
  conversation/   ConversationController, ConversationService
  realtime/       CentrifugoClient, RealtimeController, CentrifugoProxyController
  knowledge/      KbController, KbService, KbDocument
  analytics/      AnalyticsController
  team/           TeamController, OrgMembership
  apikey/         ApiKeyController, ApiKey
  webhook/        WebhookController, WebhookDeliveryService
  credentials/    CredentialsController, CredentialsService (AES-GCM)
  common/         TenantContext, ApiError, GlobalExceptionHandler, SecurityHeadersFilter, RateLimitFilter

threadly-core/src/main/resources/
  db/migration/   V1__init.sql through V5__events.sql
  application.yml
```

**Owned Tables:** orgs, users, refresh_tokens, bots, flows, flow_versions, sessions, conversations, messages, kb_documents, org_memberships, api_keys, webhooks, webhook_deliveries, credentials, costs, events

---

### 2. AI+Widget Agent

**Domain:** threadly-ai (Python FastAPI) + threadly-widget (Preact)

**Responsibilities:**
- Multi-provider LLM inference (Anthropic, OpenAI, ProviderChain)
- Hybrid RAG pipeline (dense Qdrant + sparse BM25 + RRF + optional Cohere rerank)
- Document ingestion (chunk, embed, upsert Qdrant)
- AI utility routes (summarize, suggest-replies, extract-entities, classify-intent)
- Cost tracking per LLM call
- Langfuse tracing
- Widget UI (rich messages, file upload, theme system, delivery status)
- Widget bundle build (Vite IIFE, < 35 KB gzip)

**Key Files:**
```
threadly-ai/app/
  main.py              FastAPI app + lifespan
  config.py            Pydantic settings
  providers/           AnthropicProvider, OpenAIProvider, ProviderChain
  rag/                 ingestion.py, retrieval.py, bm25.py, reranker.py
  routes/              complete.py, kb.py, ai_utils.py
  tracing/             langfuse_client.py
  costs/               cost_tracker.py, COSTS model

threadly-widget/src/
  main.tsx             Preact app entry + launcher
  widget.ts            Loader, reads data-* attributes
  ws-client.ts         Centrifugo client, reconnect, offline queue
  ui/ChatPanel.tsx     Messages, streaming tokens, rich messages
  theme.ts             injectStyles, 12 CSS custom properties
  types.ts             WidgetConfig, ChatMessage, ServerEvent
```

---

### 3. Frontend Agent

**Domain:** threadly-web (Next.js 15)

**Responsibilities:**
- All dashboard pages and React components
- Flow builder UI (React Flow v12, node catalog, PropertiesPanel)
- Conversation inbox (3-pane layout, virtual scrolling)
- Analytics page with charts
- Onboarding wizard
- Credentials manager UI
- Orval-generated API client (auto-generated from OpenAPI spec)
- Centrifugo client integration (live updates)

**Key Files:**
```
threadly-web/
  app/(app)/
    dashboard/page.tsx
    bots/page.tsx, [id]/settings/page.tsx
    builder/[botId]/page.tsx
    conversations/page.tsx
    knowledge/[botId]/page.tsx
    settings/page.tsx, team/page.tsx, api-keys/page.tsx
    analytics/page.tsx           ← Sprint 2
    credentials/page.tsx         ← Sprint 2
    onboarding/page.tsx          ← Sprint 2
  components/
    builder/
      FlowCanvas.tsx
      NodePanel.tsx              ← Sprint 2 n8n-style catalog
      PropertiesPanel.tsx        ← Sprint 2 12 input types
      nodes/NodeTypes.tsx
    layout/Sidebar.tsx
    CommandPalette.tsx
  lib/
    api.ts                       fetch wrapper
    api-mutator.ts               Orval custom fetcher
    types.ts
  orval.config.ts
```

---

### 4. Testing Agent

**Domain:** All test suites across all services

**Responsibilities:**
- JUnit5 + Testcontainers for threadly-core (Postgres, Redis)
- Pytest for threadly-ai routes
- Playwright E2E for full user flows
- Vitest for threadly-widget components
- Test coverage reporting

**Key Files:**
```
threadly-core/src/test/java/dev/threadly/core/
  auth/AuthIntegrationTest.java
  workspace/BotIntegrationTest.java
  flow/FlowIntegrationTest.java
  conversation/ConversationIntegrationTest.java
  tenant/TenantIsolationTest.java

threadly-ai/tests/
  test_complete.py
  test_kb.py
  test_ai_utils.py

threadly-web/tests/
  e2e/auth.spec.ts
  e2e/builder.spec.ts
  e2e/conversations.spec.ts
  e2e/knowledge.spec.ts

threadly-widget/src/__tests__/
  theme.test.ts
  ws-client.test.ts
  ChatPanel.test.tsx
```

---

### 5. Tech Lead Agent

**Domain:** Cross-cutting infrastructure, production hardening

**Responsibilities:**
- `FlowSchemaValidator` (strict publish-time validation)
- Enhanced `ConditionNodeExecutor` (12 operators)
- Core↔AI HMAC request signing
- `AuditLog` entity and interceptor
- Flyway V6 migration
- Railway deployment config
- Cloudflare Worker for widget CDN
- Grafana production dashboard
- `ThreadlyMetrics` custom Micrometer meters
- Production `application.yml` profile

**Key Files:**
```
threadly-core/src/main/java/dev/threadly/core/
  flow/FlowSchemaValidator.java           ← Sprint 2
  runtime/ConditionNodeExecutor.java      ← enhanced 12 ops
  audit/AuditLog.java, AuditInterceptor   ← Sprint 2
  common/HmacRequestFilter.java           ← Sprint 2
  metrics/ThreadlyMetrics.java            ← Sprint 2

threadly-core/src/main/resources/
  db/migration/V6__audit_log.sql          ← Sprint 2
  application-prod.yml                    ← Sprint 2

infra/
  railway.toml                            ← Sprint 2
  cloudflare/worker.js                    ← Sprint 2
  grafana/dashboards/threadly-prod.json   ← Sprint 2
```

---

### 6. PM Agent

**Domain:** Product documentation, sprint tracking, feature registry

**Responsibilities:**
- PRODUCT_STATUS.md — master feature + API tracker
- AGENTS.md — agent coordination contracts (this file)
- CHANGELOG.md — version history
- SPRINT.md — sprint task tracking
- FEATURES.md — feature registry with user stories

**Key Files:**
```
/Users/yasva/Kapture/Microservice/Project/Threadly/
  PRODUCT_STATUS.md
  AGENTS.md
  CHANGELOG.md
  SPRINT.md
  FEATURES.md
```

---

## Inter-Agent Contracts

### Contract 1: Backend ↔ Frontend (OpenAPI / Orval)

**Owner:** Backend Agent produces, Frontend Agent consumes

**How it works:**
1. Backend Agent maintains `threadly-core/src/main/resources/openapi.yaml` (auto-generated via Springdoc at `/v3/api-docs`)
2. Frontend Agent runs `npm run codegen` (`scripts/codegen-api.sh`) which calls Orval to generate typed React Query hooks into `threadly-web/src/lib/generated/`
3. Frontend Agent MUST NOT hand-write fetch calls for endpoints covered by OpenAPI

**Orval config:** `threadly-web/orval.config.ts`
```typescript
export default defineConfig({
  threadly: {
    input: 'http://localhost:8080/v3/api-docs',
    output: {
      mode: 'tags-split',
      target: 'src/lib/generated',
      client: 'react-query',
      mutator: 'src/lib/api-mutator.ts',
    },
  },
});
```

**Breaking change rule:** Backend Agent must version any breaking API change and notify Frontend Agent via a PR comment linking to the CHANGELOG entry.

---

### Contract 2: Backend ↔ AI (HMAC Auth)

**Owner:** Backend Agent calls, AI Agent verifies

**Header:** `X-Service-Signature: sha256=<hex_hmac>`
**Secret:** `CORE_AI_SECRET` environment variable (shared via Docker Compose / Railway secrets)

**Algorithm:**
```
body_bytes = request_body_as_utf8_bytes
signature  = HMAC-SHA256(secret=CORE_AI_SECRET, message=body_bytes)
header     = "sha256=" + hex(signature)
```

**Verification (threadly-ai):**
```python
import hmac, hashlib
def verify_signature(body: bytes, header: str, secret: str) -> bool:
    expected = "sha256=" + hmac.new(
        secret.encode(), body, hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, header)
```

**Routes that require HMAC:** All routes in threadly-ai (`/ai/complete`, `/ai/summarize`, `/kb/ingest`, `/kb/query`, etc.)

---

### Contract 3: Core ↔ Widget (Centrifugo Channels)

**Owner:** Backend Agent publishes, Widget Agent subscribes

**Channel naming:**
```
conversation:{orgId}:{conversationId}   ← visitor + dashboard agents
org:{orgId}                             ← dashboard live counters
bot:{botId}                             ← bot status updates
```

**Visitor token:** `POST /v1/realtime/token/visitor` → returns Centrifugo JWT scoped to `conversation:{orgId}:*`
**Dashboard token:** `POST /v1/realtime/token/dashboard` → returns Centrifugo JWT scoped to `org:{orgId}:*`

**Message envelope (published to Centrifugo):**
```json
{
  "type": "message" | "token" | "status_change" | "handoff_request",
  "conversationId": "uuid",
  "data": { ... }
}
```

**Widget subscribes:** `conversation:{orgId}:{conversationId}` using Centrifuge JS client

---

### Contract 4: Backend ↔ Backend (KB Ingestion Callback)

**Trigger:** Backend's `KbIngestionJob` polls and POSTs to `POST /kb/ingest` (threadly-ai)
**Callback:** threadly-ai POSTs to `POST /v1/internal/kb/{docId}/status` on threadly-core
**Auth:** Same HMAC signature (Contract 2 applies)

**Status values:** `PROCESSING` → `READY` | `FAILED`

---

## Git Worktree Workflow

Each agent works in a dedicated worktree to avoid conflicts:

```bash
# Create worktree per agent
git worktree add ../threadly-backend-agent   feat/sprint2-backend
git worktree add ../threadly-ai-agent        feat/sprint2-ai-widget
git worktree add ../threadly-frontend-agent  feat/sprint2-frontend
git worktree add ../threadly-testing-agent   feat/sprint2-testing
git worktree add ../threadly-techlead-agent  feat/sprint2-techlead
git worktree add ../threadly-pm-agent        feat/sprint2-pm
```

**Merge order (prevents conflicts):**
1. `feat/sprint2-backend` → `main` (no dependencies)
2. `feat/sprint2-ai-widget` → `main` (no dependencies)
3. `feat/sprint2-techlead` → `main` (depends on Backend for V6 migration)
4. `feat/sprint2-frontend` → `main` (depends on Backend OpenAPI)
5. `feat/sprint2-testing` → `main` (depends on all above)
6. `feat/sprint2-pm` → `main` (docs only, no conflicts)

**Conflict-prone paths (agents must coordinate):**
- `threadly-core/src/main/resources/application.yml` — Backend + Tech Lead
- `threadly-core/pom.xml` — Backend + Tech Lead
- `infra/docker-compose.yml` — all infra changes go through Tech Lead

---

## Code Quality Gates

### Java (threadly-core)

| Gate | Tool | Threshold | CI Step |
|------|------|-----------|---------|
| Compile | Maven | 0 errors | `mvn compile` |
| Unit tests | JUnit5 | 100% pass | `mvn test` |
| Integration tests | Testcontainers | 100% pass | `mvn verify` |
| Code coverage | JaCoCo | ≥ 70% line | `mvn verify -Pcoverage` |
| Code style | Checkstyle (Google) | 0 violations | `mvn checkstyle:check` |
| Dependency audit | OWASP | 0 critical CVEs | `mvn dependency-check:check` |

### Python (threadly-ai)

| Gate | Tool | Threshold | CI Step |
|------|------|-----------|---------|
| Type checking | mypy --strict | 0 errors | `mypy app/` |
| Unit tests | pytest | 100% pass | `pytest tests/` |
| Coverage | pytest-cov | ≥ 70% | `pytest --cov=app` |
| Lint | ruff | 0 violations | `ruff check app/` |
| Format | black | no diffs | `black --check app/` |

### TypeScript (threadly-web, threadly-widget)

| Gate | Tool | Threshold | CI Step |
|------|------|-----------|---------|
| Type checking | tsc --noEmit | 0 errors | `npm run typecheck` |
| Lint + format | Biome | 0 violations | `npx biome check .` |
| Unit tests | Vitest | 100% pass | `npm run test` |
| E2E tests | Playwright | 100% pass | `npm run e2e` |
| Bundle size | Vite build | widget < 35 KB | `npm run build:widget` |

---

## Tenant Isolation Rule

**This is a hard contract that all agents must respect.**

Every database query that touches org-owned data MUST include a tenant filter. There are three enforcement layers:

1. **Hibernate `@Filter`** — `TenantFilter` on `Bot`, `Conversation`, `KbDocument`, `Flow`, `Session`, `OrgMembership`, `ApiKey`, `Webhook`, `Credential` entities. Applied automatically by `TenantFilterAspect` on every request.

2. **`TenantContext`** ThreadLocal — set by `JwtAuthFilter` from the JWT claim `orgId`. Cleared in `TenantFilterAspect.after()`.

3. **Repository method contracts** — all `Repository` methods that are NOT scoped by Hibernate filter (e.g., `findById`) MUST be called only after verifying ownership, or replaced with `findByIdAndOrgId`.

**Violation:** Any controller calling `repository.findById()` without subsequent org ownership check is a security bug. Backend Agent and Tech Lead Agent are jointly responsible for catching this in code review.

**Test requirement (Testing Agent):** `TenantIsolationTest` must verify that Org A's API key cannot read Org B's bots, conversations, or knowledge base documents.
