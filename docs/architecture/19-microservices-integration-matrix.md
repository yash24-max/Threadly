# Threadly Microservices Architecture — Integration Matrix & Analysis
**Generated:** 2026-05-24  
**Status:** Post-Sprint 3, Pre-Microservices Migration  
**Phase:** Architecture Validation (Phase 0 → Phase 1 transition)

---

## Executive Summary

**Current State:** 
- ✅ Monolithic Spring Boot core (11 modules, 69 Java classes)
- ✅ All 70 features implemented with 100% feature parity
- ✅ Microservices skeleton created (9 services + shared lib with 25 classes)
- ✅ Complete documentation, patterns, and resilience infrastructure in place
- 🟡 **Gap:** Microservices are NOT yet populated with domain logic (only scaffolding)
- 🟡 **Gap:** Data migration strategy needs validation
- 🟡 **Gap:** Event schema contracts incomplete for some services

**Critical Path:** Monolith → Microservices transition requires:
1. Service boundary validation (check AGENTS.md alignment with STRUCTURE.md)
2. API contract definition (OpenAPI specs per service)
3. Event schema definitions (Kafka topic schemas)
4. Data ownership mapping (which service owns which tables)
5. Dependency graph (inter-service call patterns)
6. Integration test matrix (cross-service scenarios)

---

## Service Boundary Matrix

### 9 Microservices Defined

| Service | Port | Domain Ownership | Owned Tables | Dependencies | Status |
|---------|------|------------------|--------------|--------------|--------|
| **Identity Service** | 3001 | Auth, JWT, users, orgs, API keys | users, orgs, api_keys, refresh_tokens | None | 🟡 Scaffold |
| **Workspace Service** | 3002 | Bots, teams, settings | bots, org_memberships | identity | 🟡 Scaffold |
| **Flow Service** | 3003 | Flow CRUD, versioning, publishing | flows, flow_versions, templates | workspace | 🟡 Scaffold |
| **Runtime Service** | 3004 | Session execution, node executors | sessions, variables (Redis) | flow, knowledge, integration, conversation | 🟡 Scaffold |
| **Conversation Service** | 3005 | Transcripts, leads, handoff, handoff logs | conversations, messages, leads | workspace, runtime | 🟡 Scaffold |
| **Knowledge Service** | 3006 | KB, RAG, embeddings, ingestion | kb_documents, embeddings (Qdrant) | workspace | 🟡 Scaffold |
| **Analytics Service** | 3007 | Metrics, dashboards, exports | analytics_events, costs, usage_records | workspace, conversation, runtime | 🟡 Scaffold |
| **Billing Service** | 3008 | Stripe, subscriptions, usage metering | plans, subscriptions, usage_records | identity, analytics | 🟡 Scaffold |
| **Integration Service** | 3009 | Connectors, OAuth, custom actions | integrations, credentials | workspace, conversation | 🟡 Scaffold |

### Service Dependency Graph

```
┌─────────────────┐
│ Identity (3001) │  ← Root: all services depend on it
└────────┬────────┘
         │
    ┌────┴──────────┬──────────────┐
    │               │              │
┌───▼─────────┐ ┌──▼──────┐ ┌────▼────┐
│ Workspace   │ │ Billing │ │ Analytics
│ (3002)      │ │ (3008)  │ │ (3007)
└───┬─┬───────┘ └─────────┘ └──────────┘
    │ │
    │ └──────────┐
    │            │
┌───▼──┐ ┌──────▼──┐ ┌──────────────┐
│ Flow │ │Knowledge│ │ Integration
│(3003)│ │ (3006)  │ │ (3009)
└───┬──┘ └─────────┘ └──────────────┘
    │
┌───▼────────┬──────────────────────┐
│            │                      │
┌───▼──────┐ ┌──▼─────────┐ ┌──────▼──┐
│ Runtime  │ │Conversation│ │Analytics │
│ (3004)   │ │  (3005)    │ │ (3007)   │
└──────────┘ └────────────┘ └──────────┘
```

### Owned Table Summary (Data Ownership Per Service)

| Service | Tables | Schema Namespace | Migration |
|---------|--------|------------------|-----------|
| identity | users, orgs, api_keys, refresh_tokens | identity_schema | V10 (identity) |
| workspace | bots, org_memberships | workspace_schema | V10 (workspace) |
| flow | flows, flow_versions, templates | flow_schema | V10 (flow) |
| runtime | sessions | runtime_schema | V10 (runtime) |
| conversation | conversations, messages, leads, handoff_logs | conversation_schema | V10 (conversation) |
| knowledge | kb_documents | knowledge_schema | V10 (knowledge) |
| analytics | events, costs, usage_records | analytics_schema | V10 (analytics) |
| billing | plans, subscriptions | billing_schema | V10 (billing) |
| integration | integrations, credentials | integration_schema | V10 (integration) |

**Critical Issue:** V10 migration (10-create-schemas.sql) creates 9 isolated schemas. Must verify:
- [ ] Drop existing monolith tables
- [ ] Flyway V1-V9 applied per-service (not monolith-wide)
- [ ] Data seeding for each service

---

## Inter-Service Communication Matrix

### REST API Contracts (Feign Clients)

| Caller | Callee | Endpoint | Method | Status |
|--------|--------|----------|--------|--------|
| workspace | identity | POST /v1/identity/users/{id} | GET | 🟡 Needs OpenAPI |
| flow | workspace | GET /v1/workspace/bots/{id} | GET | 🟡 Needs OpenAPI |
| runtime | flow | GET /v1/flow/{id}/versions | GET | 🟡 Needs OpenAPI |
| conversation | workspace | POST /v1/workspace/bots/{id}/conversation/created | POST | 🟡 Needs OpenAPI |
| integration | workspace | GET /v1/workspace/integrations | GET | 🟡 Needs OpenAPI |
| runtime | knowledge | POST /v1/knowledge/query | POST | 🟡 Needs OpenAPI |
| runtime | integration | POST /v1/integration/execute | POST | 🟡 Needs OpenAPI |
| analytics | workspace, conversation | (Event consumption only) | - | 🟡 Kafka-only |

**All Feign clients in threadly-common-spring:** ✅ Present
- IdentityServiceClient
- WorkspaceServiceClient
- FlowServiceClient
- ConversationServiceClient
- KnowledgeServiceClient
- IntegrationServiceClient

**Gap:** No OpenAPI specs yet. Services must export `/v3/api-docs`.

### Kafka Event Topics (Async Integration)

| Topic | Publisher | Subscribers | Event Types | Status |
|-------|-----------|-------------|-------------|--------|
| workspace-events | workspace | flow, integration | bot.created, bot.updated, bot.deleted | 🟡 Schema missing |
| flow-events | flow | runtime | flow.published, flow.updated | 🟡 Schema missing |
| runtime-events | runtime | analytics, conversation | session.started, session.completed, node.executed | 🟡 Schema missing |
| conversation-events | conversation | analytics, runtime | message.created, conversation.completed, handoff.requested | 🟡 Schema missing |
| integration-events | integration | runtime | integration.executed, integration.failed | 🟡 Schema missing |
| analytics-events | analytics | (internal only) | event.tracked, cost.recorded | 🟡 Schema missing |
| billing-events | billing | analytics | subscription.created, usage.metered | 🟡 Schema missing |
| knowledge-events | knowledge | runtime | document.indexed, document.updated | 🟡 Schema missing |

**Missing:** Kafka topic schema definitions (JSON Schema or Avro)

---

## Data Flow Scenarios

### Scenario 1: Create Bot & Generate Widget

```
Frontend (Next.js)
  ↓ POST /v1/workspace/bots
Nginx (8080)
  ↓
Workspace Service (3002)
  ├─ Save Bot entity → workspace_schema.bots
  ├─ Publish bot.created event → workspace-events topic
  └─ Return bot + embed snippet
  
Flow Service (3003) [Kafka listener]
  ├─ Receive bot.created
  ├─ Create default flow template
  └─ Publish flow.published
  
Widget loads at customer site
  ↓ Open iframe → https://widget.threadly.io/embed?botId=xxx
Widget loads conversation
  ↓
Widget connects to Centrifugo WS
  ↓
Runtime Service (3004)
  ├─ GET /v1/flow/{botId}/version/published
  ├─ Create session (Redis)
  └─ Return initial flow state
```

**Issue:** Flow Service doesn't yet have integration logic.

### Scenario 2: Customer Sends Message → AI Reply

```
Widget sends message
  ↓ WS message via Centrifugo
Runtime Service (3004)
  ├─ Parse message
  ├─ Execute flow node (AiReply)
  ├─ Call POST /kb/query (Knowledge Service)
  │   └─ Qdrant dense search + BM25 sparse
  ├─ Get context
  └─ Call FastAPI /ai/complete
  
AI Service (Python, port 8001)
  ├─ LLM call (Anthropic/OpenAI)
  ├─ Return streamed tokens
  └─ Track cost
  
Runtime receives tokens
  ├─ Stream to widget via Centrifugo
  └─ Save message to Conversation Service
  
Conversation Service (3005)
  ├─ Save message → conversation_schema.messages
  ├─ Publish message.created event
  └─ Update conversation last_message_at
  
Analytics Service (3007) [Kafka listener]
  └─ Track event + cost
```

**Issues:**
- Runtime Service → Knowledge Service call needs Feign client instantiation
- Cost tracking in Analytics needs event schema
- Message streaming latency not measured

### Scenario 3: Webhook Delivery (Bidirectional)

```
Workspace creates webhook
  ↓ POST /v1/workspace/bots/{id}/webhooks
  
Webhook Service (if integrated into Workspace)
  ├─ Register webhook
  └─ Wait for trigger events
  
Integration Service (3009)
  ├─ Execute custom integration
  └─ Trigger webhook (Resilience4j retry)
  
External system receives:
  POST <customer_webhook_url>
  X-Threadly-Signature: sha256=<hmac>
  { "type": "...", "data": {...} }
```

**Issue:** Webhook service NOT yet separated (still in core). Must be in Integration Service.

---

## API Contract Gaps

### Missing OpenAPI Specs

Each service needs to export at `/v3/api-docs`:

1. **Identity Service (3001)**
   - Missing: User CRUD, org CRUD, API key CRUD, JWT refresh
   
2. **Workspace Service (3002)**
   - Missing: Bot CRUD, team CRUD, integration CRUD
   
3. **Flow Service (3003)**
   - Missing: Flow CRUD, versioning, publishing, templates
   
4. **Runtime Service (3004)**
   - Missing: Session start, node execution, variable management
   
5. **Conversation Service (3005)**
   - Missing: Message create, conversation close, lead capture
   
6. **Knowledge Service (3006)**
   - Missing: Document upload, KB query, scraping
   
7. **Analytics Service (3007)**
   - Missing: Event aggregation, export, dashboards
   
8. **Billing Service (3008)**
   - Missing: Subscription CRUD, metering
   
9. **Integration Service (3009)**
   - Missing: Connector registry, action execute

**Action Required:** Generate Springdoc OpenAPI specs for each service.

---

## Event Schema Contracts (Kafka)

### Missing JSON Schemas

Each topic needs a schema file in `infrastructure/kafka/schemas/`:

```
infrastructure/kafka/schemas/
├── workspace-events.schema.json
├── flow-events.schema.json
├── runtime-events.schema.json
├── conversation-events.schema.json
├── integration-events.schema.json
├── analytics-events.schema.json
├── billing-events.schema.json
└── knowledge-events.schema.json
```

**Example (workspace-events.schema.json):**
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "WorkspaceEvent",
  "oneOf": [
    {
      "type": "object",
      "properties": {
        "eventType": { "const": "bot.created" },
        "aggregateId": { "type": "string", "format": "uuid" },
        "timestamp": { "type": "string", "format": "date-time" },
        "data": {
          "type": "object",
          "properties": {
            "botId": { "type": "string" },
            "orgId": { "type": "string" },
            "name": { "type": "string" }
          },
          "required": ["botId", "orgId", "name"]
        }
      },
      "required": ["eventType", "aggregateId", "timestamp", "data"]
    },
    ...
  ]
}
```

---

## Multi-Tenancy Enforcement Check

**Current:** Hibernate `@Filter` on entities in monolith
**Microservices:** Must replicate per service

- [ ] Identity Service: `@Filter(name = "tenantFilter")` on User, OrgMembership
- [ ] Workspace Service: `@Filter` on Bot, OrgMembership
- [ ] Flow Service: `@Filter` on Flow, FlowVersion
- [ ] Runtime Service: `@Filter` on Session
- [ ] Conversation Service: `@Filter` on Conversation, Message, Lead
- [ ] Knowledge Service: `@Filter` on KbDocument
- [ ] Integration Service: `@Filter` on Integration, Credential
- [ ] Billing Service: `@Filter` on Subscription

**Critical:** TenantContext ThreadLocal set by JwtAuthFilter in Nginx/API Gateway.

---

## Cross-Cutting Concerns

### 1. Distributed Tracing (W3C Trace Context)

**Status:** ✅ TraceIdPropagator implemented in threadly-common-spring

**Needed:**
- [ ] Nginx adds traceparent header
- [ ] Each service propagates in HTTP calls (Feign interceptor)
- [ ] Kafka producer injects traceparent header
- [ ] Kafka consumer extracts and sets RequestContext
- [ ] Logs include traceId

**Files:**
```
services/threadly-common-spring/src/main/java/dev/threadly/common/tracing/
├── TraceIdPropagator.java ✅
└── TraceIdInterceptor.java (needs creation)
```

### 2. Circuit Breaker & Resilience

**Status:** ✅ CircuitBreakerConfig in threadly-common-spring

**Needed:**
- [ ] Apply to all Feign clients
- [ ] Retry on 5xx errors (exponential backoff: 100ms, 200ms, 400ms)
- [ ] Fail-fast fallback (return error DTO, not exception)
- [ ] Metrics exported to Prometheus

### 3. Saga Orchestration

**Status:** ✅ SagaOrchestrator + ConversationHandoffSaga implemented

**When Needed:**
- [ ] Conversation handoff (check billing → assign agent → notify)
- [ ] Multi-step integrations (OAuth → execute → callback)
- [ ] Subscription upgrade (reserve capacity → charge → activate)

### 4. Rate Limiting

**Current:** Bucket4j + Redis in core
**Microservices:** Move to Nginx layer or keep per-service?

**Recommendation:** Nginx layer (centralized)
```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=100r/s;
limit_req_zone $http_x_api_key zone=apikey:10m rate=1000r/s;
```

### 5. Health Checks

**Status:** ✅ HealthController in threadly-common-spring

**Implementation:**
```java
GET /health      ← Liveness (is process alive?)
GET /ready       ← Readiness (are dependencies up?)
GET /metrics     ← Prometheus metrics
```

---

## Database Migration Strategy

### Current State
- Monolith: V1–V9 in resources/db/migration/
- Microservices: V10 creates 9 isolated schemas

### Required Steps

1. **Schema Isolation (V10)** ✅ Prepared
   ```sql
   CREATE SCHEMA IF NOT EXISTS identity_schema;
   CREATE SCHEMA IF NOT EXISTS workspace_schema;
   ... (9 total)
   ```

2. **Per-Service Migrations** 🟡 Needs creation
   ```
   identity-service/resources/db/migration/V1__init.sql
   workspace-service/resources/db/migration/V1__init.sql
   ... (9 total)
   ```

3. **Data Migration** 🟡 Needs script
   ```bash
   # Phase 0: Shadow read (write to both monolith + microservices)
   # Phase 1: Dual-write mode (both read+write)
   # Phase 2: Cutover (read from microservices)
   ```

---

## Security Boundaries

### JWT Token Validation

**Current:** RS256, issued by Identity Service
**Required per service:**
- [ ] Import public key from Identity Service
- [ ] Validate JWT in every request (JwtAuthFilter)
- [ ] Extract orgId + userId from claims
- [ ] Set TenantContext.setOrgId()

### HMAC Signing (Service-to-Service)

**Current:** Core ↔ AI
**Needed:** Core ↔ Microservices

**All inter-service calls must include:**
```
X-Service-Signature: sha256=<hmac>
```

**Secret:** Environment variable `CORE_<SERVICE>_SECRET`

---

## Identified Gaps & Action Items

### Critical (Blocking Phase 1)

1. **Service Scaffolding Incomplete**
   - [ ] Migrate domain logic from monolith → 9 services
   - [ ] Create Controllers, Services, Repositories per service
   - [ ] Implement per-service Flyway migrations
   
2. **API Contracts Missing**
   - [ ] Generate OpenAPI specs for all 9 services
   - [ ] Validate Feign client compatibility
   - [ ] Version API endpoints
   
3. **Kafka Event Schemas Missing**
   - [ ] Define JSON schemas for 9 topics
   - [ ] Implement event producer/consumer in each service
   - [ ] Add dead-letter-queue topics
   
4. **Data Ownership Validation**
   - [ ] Verify each service owns its tables (no cross-schema queries)
   - [ ] Create cross-tenant isolation tests
   - [ ] Document expected storage per service
   
5. **Service Discovery Config**
   - [ ] Consul configuration (service registration)
   - [ ] Health check endpoints
   - [ ] Load balancer setup

### High (Phase 1 - Week 1)

6. **Inter-Service Testing**
   - [ ] Create integration tests for each Feign call
   - [ ] Test saga orchestration (happy path + failure)
   - [ ] Test event consumption (retry + DLQ)
   
7. **Distributed Tracing**
   - [ ] Nginx adds traceparent header
   - [ ] Feign client injects in outbound calls
   - [ ] Kafka producer/consumer propagates
   - [ ] Logs include traceId
   
8. **Resilience Patterns**
   - [ ] Apply CircuitBreakerConfig to all Feign clients
   - [ ] Test circuit breaker transitions
   - [ ] Implement exponential backoff
   
9. **Helm Charts / K8s Manifests**
   - [ ] Service deployments (3 replicas each)
   - [ ] ConfigMaps for per-service config
   - [ ] PVCs for stateful services (Postgres, Redis)
   
10. **Monitoring & Alerting**
    - [ ] Prometheus scrape config
    - [ ] Grafana dashboards per service
    - [ ] Alert rules for circuit breaker OPEN state

### Medium (Phase 1 - Week 2)

11. **Backward Compatibility**
    - [ ] Versioned API endpoints (/v1/*, /v2/*)
    - [ ] Feature flags for gradual rollout
    - [ ] Deprecation timeline
    
12. **Performance Tuning**
    - [ ] Measure latency per hop (HTTP, Kafka, DB)
    - [ ] Cache strategies (Redis, CDN)
    - [ ] Database query optimization
    
13. **Documentation**
    - [ ] Update AGENTS.md with phase 1 status
    - [ ] Add service runbooks
    - [ ] Create incident response guides

---

## Validation Checklist

### Before Phase 1 Cutover

- [ ] All 9 services compile without errors
- [ ] All services pass local unit tests (70%+ coverage)
- [ ] Monolith ↔ Microservice Feign calls succeed
- [ ] Event publishing/consumption works end-to-end
- [ ] Cross-tenant isolation enforced (TenantIsolationTest)
- [ ] Circuit breaker transitions correctly
- [ ] Distributed tracing correlates requests
- [ ] Kubernetes manifests apply cleanly
- [ ] Docker Compose local dev works
- [ ] Health checks pass (liveness + readiness)
- [ ] Rate limiting works
- [ ] HMAC signing validated
- [ ] Database migrations run forward-only
- [ ] No hardcoded secrets (all env vars)
- [ ] All dependencies pinned to exact versions

---

## Next Steps (Tech Lead)

### Week 1: Foundation
1. Populate 9 microservices with domain logic
2. Generate OpenAPI specs for each service
3. Define Kafka event schemas
4. Create per-service Flyway migrations

### Week 2: Integration
1. Implement inter-service Feign clients
2. Test saga orchestration
3. Set up distributed tracing
4. Create Helm charts

### Week 3: Testing
1. Integration test matrix
2. Cross-service E2E tests
3. Performance benchmarks

### Week 4: Validation
1. Shadow mode in production
2. Cutover readiness assessment
3. Rollback procedures documented

---

**Created by:** Tech Lead Agent  
**Last Updated:** 2026-05-24  
**Next Review:** After services populated with domain logic
