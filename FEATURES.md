# Threadly — Feature Registry

> Canonical list of all product features with user stories, status, and key files.
> Updated: 2026-05-23 · 38 features across Phase 0 and Sprint 2.

---

## Feature ID Index

| Range | Domain |
|-------|--------|
| F001–F005 | Authentication & Identity |
| F006–F010 | Bot Management |
| F011–F016 | Flow Builder |
| F017–F020 | Flow Runtime & Node Types |
| F021–F025 | Conversations & Inbox |
| F026–F028 | Knowledge Base & RAG |
| F029–F031 | Realtime & Widget |
| F032–F035 | Team & Security |
| F036–F038 | Analytics & Observability |

---

## Authentication & Identity

### F001 — User Signup

**User story:** As a new user, I can create an account with my email and password so that I can access the Threadly dashboard.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/auth/AuthController.java`
- `threadly-core/src/main/java/dev/threadly/core/auth/AuthService.java`
- `threadly-web/app/(auth)/signup/page.tsx`

**API endpoints:**
- `POST /v1/auth/signup` — creates user + org, returns JWT access token + refresh token

---

### F002 — JWT Login / Logout

**User story:** As a registered user, I can log in with my email and password and receive a JWT so that I can authenticate API requests.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/auth/AuthController.java`
- `threadly-core/src/main/java/dev/threadly/core/auth/JwtService.java`
- `threadly-core/src/main/java/dev/threadly/core/auth/JwtAuthFilter.java`

**API endpoints:**
- `POST /v1/auth/login`
- `POST /v1/auth/logout`
- `POST /v1/auth/refresh`
- `GET /v1/auth/me`

---

### F003 — RS256 Token Signing

**User story:** As a security-conscious operator, I need JWT tokens signed with RS256 so that private key material never leaves the server and tokens can be verified with a public key.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/auth/JwtService.java`
- `threadly-core/src/main/resources/application.yml` (jwt.private-key-path, jwt.public-key-path)
- `scripts/bootstrap.sh` (generates RS256 key pair)

**API endpoints:** Underlying all authenticated endpoints

---

### F004 — API Key Authentication

**User story:** As a developer integrating Threadly into my backend, I can create an API key so that I can authenticate programmatic requests without user credentials.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/apikey/ApiKeyController.java`
- `threadly-core/src/main/java/dev/threadly/core/auth/JwtAuthFilter.java`
- `threadly-web/app/(app)/settings/api-keys/page.tsx`

**API endpoints:**
- `POST /v1/api-keys` — creates key, returns `tly_live_<secret>` plaintext once
- `GET /v1/api-keys` — list (hashed, no secret)
- `DELETE /v1/api-keys/{id}` — revoke

---

### F005 — Org Multi-Tenancy

**User story:** As a SaaS operator, I need complete data isolation between organizations so that one customer's data is never accessible to another.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/common/TenantContext.java`
- `threadly-core/src/main/java/dev/threadly/core/common/TenantFilterAspect.java`
- `threadly-core/src/main/resources/db/migration/V1__init.sql` (org_id columns + RLS indexes)

**API endpoints:** Enforced on all authenticated endpoints via Hibernate `@Filter`

---

## Bot Management

### F006 — Bot CRUD

**User story:** As a business owner, I can create multiple chatbots for different use cases so that I can deploy tailored conversational experiences.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/workspace/BotController.java`
- `threadly-core/src/main/java/dev/threadly/core/workspace/BotService.java`
- `threadly-web/app/(app)/bots/page.tsx`

**API endpoints:**
- `GET /v1/bots`
- `POST /v1/bots`
- `GET /v1/bots/{id}`
- `PUT /v1/bots/{id}`
- `DELETE /v1/bots/{id}`

---

### F007 — Embeddable Widget Snippet

**User story:** As a website owner, I can copy a one-line script tag from the dashboard so that I can embed the chat widget on my website in under a minute.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/workspace/BotService.java` (`generateSnippet()`)
- `threadly-web/app/(app)/settings/page.tsx`

**API endpoints:**
- `GET /v1/bots/{id}/snippet` — returns `<script>` HTML string

---

### F008 — Bot Theme Customization

**User story:** As a brand-conscious business, I can customize my chatbot's colors, avatar, and position so that it matches my website's visual identity.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/workspace/BotController.java`
- `threadly-web/app/(app)/bots/[id]/settings/page.tsx`
- `threadly-widget/src/theme.ts`

**API endpoints:**
- `PATCH /v1/bots/{id}/theme`

---

### F009 — Flow Import/Export

**User story:** As a developer, I can export my flow as a JSON file and import it into another bot so that I can reuse flows across projects or share them with teammates.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/flow/FlowController.java`
- `threadly-core/src/main/java/dev/threadly/core/flow/FlowService.java`

**API endpoints:**
- `GET /v1/bots/{id}/flow/export`
- `POST /v1/bots/{id}/flow/import`

---

### F010 — Credentials Store

**User story:** As a developer using the API Call node, I can store API keys and secrets in a secure vault so that they are never exposed in flow JSON or logs.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/credentials/CredentialsService.java`
- `threadly-core/src/main/java/dev/threadly/core/credentials/CredentialsController.java`
- `threadly-core/src/main/resources/db/migration/V4__credentials.sql`

**API endpoints:**
- `POST /v1/credentials`
- `GET /v1/credentials`
- `DELETE /v1/credentials/{id}`

---

## Flow Builder

### F011 — Visual Flow Builder Canvas

**User story:** As a non-technical business owner, I can drag and drop nodes onto a canvas and connect them with edges so that I can build a conversational flow without writing code.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-web/components/builder/FlowCanvas.tsx`
- `threadly-web/components/builder/nodes/NodeTypes.tsx`
- `threadly-web/app/(app)/builder/[botId]/page.tsx`

**API endpoints:**
- `GET /v1/bots/{id}/flow/draft`
- `PUT /v1/bots/{id}/flow/draft`

---

### F012 — Flow Autosave

**User story:** As a flow designer, my changes are saved automatically as I work so that I never lose my progress.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-web/components/builder/FlowCanvas.tsx` (800ms debounce useEffect)
- `threadly-core/src/main/java/dev/threadly/core/flow/FlowController.java`

**API endpoints:**
- `PUT /v1/bots/{id}/flow/draft`

---

### F013 — Flow Publish & Version History

**User story:** As a bot manager, I can publish my draft flow and roll back to any previous version so that I can deploy safely and recover from mistakes.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/flow/FlowService.java`
- `threadly-web/components/builder/VersionsDrawer.tsx`

**API endpoints:**
- `POST /v1/bots/{id}/flow/publish`
- `POST /v1/bots/{id}/flow/rollback`
- `GET /v1/bots/{id}/flow/versions`

---

### F014 — Node Palette (Drag and Drop)

**User story:** As a flow designer, I can see all available node types in a sidebar panel and drag them onto the canvas so that I can build flows intuitively.

**Status:** ✅ Phase 0 (basic) / 🔄 Sprint 2 (n8n-style catalog)

**Key files:**
- `threadly-web/components/builder/NodePanel.tsx`
- `threadly-web/components/builder/nodes/NodeTypes.tsx`

**API endpoints:** None (client-only)

---

### F015 — Properties Panel

**User story:** As a flow designer, I can click any node and see a properties panel where I configure that node's behavior so that I can customize each step of the conversation.

**Status:** ✅ Phase 0 (basic) / 🔄 Sprint 2 (12 input types)

**Key files:**
- `threadly-web/components/builder/PropertiesPanel.tsx`

**API endpoints:** None (client-only; config saved on autosave)

---

### F016 — Builder Keyboard Shortcuts

**User story:** As a power user building complex flows, I can use keyboard shortcuts (N to add node, Del to delete, Cmd-Z to undo) so that I can work faster without reaching for the mouse.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-web/components/builder/FlowCanvas.tsx` (useEffect keyboard handler)

**API endpoints:** None (client-only)

---

## Flow Runtime & Node Types

### F017 — Flow Runtime Engine

**User story:** As a visitor chatting with a bot, my messages trigger the correct next step in the configured flow so that the conversation follows the business logic defined by the bot owner.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/runtime/FlowRuntime.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/FlowGraph.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/NodeExecutorFactory.java`

**API endpoints:** Triggered internally by `CentrifugoProxyService` on visitor publish events

---

### F018 — AI Reply Node (Streaming)

**User story:** As a visitor, I see the bot's AI-generated response appear word by word so that the experience feels fast and natural.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/runtime/AiReplyNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/ai/AiClient.java`
- `threadly-ai/app/routes/complete.py`

**API endpoints:**
- `POST /ai/complete` (internal, streaming SSE)

---

### F019 — Condition & Switch Routing

**User story:** As a flow designer, I can branch the conversation based on visitor inputs or session variables so that different users see different paths.

**Status:** ✅ Complete (Phase 0 basic) / 🔄 Sprint 2 (12 operators)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/runtime/ConditionNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/SwitchNodeExecutor.java`

**API endpoints:** None (runtime-internal)

---

### F020 — Action Nodes (Delay, SendEmail, CollectInput, ApiCall)

**User story:** As a flow designer, I can add delay pauses, send emails, collect structured input, and call external APIs so that my chatbot can integrate with my business systems.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/runtime/DelayNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/SendEmailNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/CollectInputNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/runtime/ApiCallNodeExecutor.java`

**API endpoints:** None (runtime-internal; ApiCall reaches external URLs)

---

## Conversations & Inbox

### F021 — Conversation Inbox (3-Pane)

**User story:** As a support agent, I can see all conversations in a list, read the full transcript in the center, and see visitor details on the right so that I can handle customer inquiries efficiently.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-web/app/(app)/conversations/page.tsx`
- `threadly-core/src/main/java/dev/threadly/core/conversation/ConversationController.java`

**API endpoints:**
- `GET /v1/conversations`
- `GET /v1/conversations/{id}/messages`

---

### F022 — Human Handoff (Take Over / Resume AI)

**User story:** As a support agent, I can take over a conversation from the AI bot when complex issues arise, and return control to the AI when resolved so that customers always get the best help.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/runtime/HandoffNodeExecutor.java`
- `threadly-core/src/main/java/dev/threadly/core/conversation/ConversationController.java`
- `threadly-web/app/(app)/conversations/page.tsx`

**API endpoints:**
- `POST /v1/conversations/{id}/takeover`
- `POST /v1/conversations/{id}/handoff`
- `POST /v1/conversations/{id}/resume`

---

### F023 — Conversation Bulk Operations

**User story:** As an operations manager, I can select multiple conversations and close or assign them in bulk so that I can manage high conversation volumes efficiently.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/conversation/ConversationController.java`

**API endpoints:**
- `POST /v1/conversations/bulk-close`
- `POST /v1/conversations/bulk-assign`

---

### F024 — Conversation CSV Export

**User story:** As a business analyst, I can export conversation data to CSV so that I can analyze it in spreadsheets or import it into my BI tool.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/conversation/ConversationController.java`

**API endpoints:**
- `GET /v1/conversations/export` (returns `text/csv`)

---

### F025 — Live Conversation Updates

**User story:** As a support agent with the inbox open, I see new messages and conversations appear in real time without refreshing the page so that I never miss a customer message.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/realtime/CentrifugoClient.java`
- `threadly-web/app/(app)/conversations/page.tsx` (Centrifugo JS subscription)

**API endpoints:**
- `POST /v1/realtime/token/dashboard`
- Centrifugo channel: `conversation:{orgId}:{conversationId}`

---

## Knowledge Base & RAG

### F026 — Knowledge Base Upload

**User story:** As a bot owner, I can upload PDF, DOCX, and TXT files to my bot's knowledge base so that the AI can answer questions based on my documents.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/knowledge/KbController.java`
- `threadly-core/src/main/java/dev/threadly/core/knowledge/KbService.java`
- `threadly-web/app/(app)/knowledge/[botId]/page.tsx`

**API endpoints:**
- `POST /v1/bots/{id}/kb/upload`
- `POST /v1/bots/{id}/kb/url`
- `GET /v1/bots/{id}/kb`
- `DELETE /v1/bots/{id}/kb/{docId}`

---

### F027 — Hybrid RAG (Dense + Sparse + RRF)

**User story:** As a bot visitor, when I ask a question, the AI retrieves the most relevant passages from the knowledge base using both semantic and keyword matching so that I get accurate, grounded answers.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-ai/app/rag/retrieval.py`
- `threadly-ai/app/rag/bm25.py`
- `threadly-ai/app/routes/complete.py`

**API endpoints:**
- `POST /kb/query` (internal)
- `POST /ai/complete` (uses RAG context internally)

---

### F028 — Citation Formatting

**User story:** As a visitor receiving an AI answer, I can see inline citation markers like [1] and [2] that reference the source documents so that I can verify the information.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-ai/app/rag/citations.py`
- `threadly-ai/app/routes/complete.py`

**API endpoints:** Part of `POST /ai/complete` response payload

---

## Realtime & Widget

### F029 — Embeddable Chat Widget

**User story:** As a website owner, I can embed Threadly's chat widget with a single script tag so that my visitors can start chatting with my bot without any complex setup.

**Status:** ✅ Complete (Phase 0)

**Key files:**
- `threadly-widget/src/widget.ts`
- `threadly-widget/src/main.tsx`
- `threadly-widget/vite.config.ts`

**API endpoints:**
- Widget connects to Centrifugo via WebSocket
- `POST /v1/realtime/token/visitor`

---

### F030 — Widget Rich Messages

**User story:** As a bot designer, I can send buttons, cards, and quick reply chips so that visitors have interactive options in the chat instead of only text.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-widget/src/ui/ChatPanel.tsx`
- `threadly-widget/src/types.ts`

**API endpoints:** Rich message payloads published via Centrifugo channel

---

### F031 — Widget Theme System

**User story:** As a business with a brand identity, I can customize the widget's colors, fonts, and dark/light mode via HTML data attributes so that the widget feels native to my website.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-widget/src/theme.ts`
- `threadly-widget/src/types.ts`

**API endpoints:** None (client-only; reads `data-theme-*` attributes)

---

## Team & Security

### F032 — Team Management (RBAC)

**User story:** As a team lead, I can invite colleagues to my workspace with specific roles (Owner, Admin, Agent) so that each person has appropriate access to manage bots and handle conversations.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/team/TeamController.java`
- `threadly-core/src/main/resources/db/migration/V2__memberships.sql`
- `threadly-web/app/(app)/settings/team/page.tsx`

**API endpoints:**
- `POST /v1/team/invite`
- `GET /v1/team/members`
- `PUT /v1/team/members/{id}/role`
- `DELETE /v1/team/members/{id}`

---

### F033 — Webhook Event Delivery

**User story:** As a developer, I can register a webhook URL so that Threadly pushes conversation events to my system in real time for downstream automation.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/webhook/WebhookController.java`
- `threadly-core/src/main/java/dev/threadly/core/webhook/WebhookDeliveryService.java`

**API endpoints:**
- `POST /v1/webhooks`
- `GET /v1/webhooks`
- `PUT /v1/webhooks/{id}`
- `DELETE /v1/webhooks/{id}`

---

### F034 — Rate Limiting

**User story:** As a platform operator, I need rate limits enforced per org and per IP so that abusive clients cannot degrade service for other tenants.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/common/RateLimitFilter.java`

**API endpoints:** Enforced on all endpoints; 10/min on `/v1/auth/*`, 1000/min per org on all others

---

### F035 — Security Headers

**User story:** As a security-conscious operator, my API responses include proper security headers so that browser-based clients are protected against common web attacks.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/common/SecurityHeadersFilter.java`

**API endpoints:** Applied globally via `OncePerRequestFilter`

---

## Analytics & Observability

### F036 — Per-Bot Analytics Dashboard

**User story:** As a bot owner, I can view conversation volume, AI cost, handoff rate, and completion funnel for each of my bots so that I can measure performance and ROI.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-core/src/main/java/dev/threadly/core/analytics/AnalyticsController.java`
- `threadly-web/app/(app)/analytics/page.tsx` (🔄 Frontend Agent)

**API endpoints:**
- `GET /v1/bots/{id}/analytics/summary`
- `GET /v1/bots/{id}/analytics/daily`
- `GET /v1/bots/{id}/analytics/funnel`

---

### F037 — LLM Cost Tracking

**User story:** As a business owner, I can see how much I'm spending on AI inference per bot per day so that I can optimize my flow designs and control costs.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-ai/app/costs/cost_tracker.py`
- `threadly-core/src/main/resources/db/migration/V5__events.sql` (costs table)

**API endpoints:** Cost data surfaced in `/analytics/summary` response

---

### F038 — Langfuse LLM Tracing

**User story:** As an AI engineer debugging bot responses, I can view detailed traces of every LLM call in Langfuse including prompt, completion, latency, and cost so that I can optimize and debug AI behavior.

**Status:** ✅ Complete (Sprint 2)

**Key files:**
- `threadly-ai/app/tracing/langfuse_client.py`
- `threadly-ai/app/routes/complete.py`

**API endpoints:** Traces sent to Langfuse cloud/self-hosted; no Threadly-facing endpoint
