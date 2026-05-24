# Phase 1: Microservices Migration Roadmap

**Status:** Pre-Migration (Phase 0 ✅ Complete)  
**Target Completion:** 4 weeks (May 27 - June 23, 2026)  
**Current Architecture:** Monolith (Phase 0) → Microservices (Phase 1)

---

## Overview

Phase 1 converts the monolithic Spring Boot core into 9 distributed microservices. The current codebase has:
- ✅ All 70 features fully implemented
- ✅ Microservice scaffolding created (9 services + shared lib)
- ✅ Resilience patterns, tracing, saga orchestration ready
- 🟡 Domain logic NOT yet migrated to services
- 🟡 API contracts undefined
- 🟡 Kafka schemas missing

**Success Criteria:**
- All services deployable independently
- Zero hardcoded values (all env vars)
- Multi-tenant isolation enforced
- Circuit breaker + saga patterns active
- End-to-end tracing working
- 100% backward compatible (dual-write phase)

---

## Week 1: Service Population & API Contracts

### Day 1-2: Service Scaffolding Complete

**Deliverable:** Each of 9 services has full controller/service/repository layer

**Tasks per service:**
1. Copy relevant domain logic from monolith
2. Create REST Controllers (copy from core)
3. Create Service classes (business logic)
4. Create Repository interfaces (Spring Data JPA)
5. Create DTOs + request/response classes
6. Add Feign clients for dependencies
7. Configure application.yml per service

**Example (Identity Service):**
```java
// src/main/java/dev/threadly/identity/
├── controller/AuthController.java        (copy from core)
├── controller/UserController.java        (copy from core)
├── service/AuthService.java              (copy from core)
├── service/UserService.java              (copy from core)
├── repository/UserRepository.java        (copy from core)
├── repository/OrgRepository.java         (copy from core)
├── entity/User.java                      (copy from core)
├── entity/Org.java                       (copy from core)
├── dto/UserDto.java                      (copy from core)
└── config/IdentityServiceConfig.java     (NEW)
```

**Effort:** 40 hours (Backend Agent)
**Blocker:** None

### Day 3-4: OpenAPI Specs Generated

**Deliverable:** Each service exports `/v3/api-docs`

**Process:**
1. Add `org.springdoc:springdoc-openapi-starter-webmvc-api:2.4.0` to pom.xml
2. Configure in application.yml:
   ```yaml
   springdoc:
     api-docs:
       path: /v3/api-docs
     swagger-ui:
       enabled: false  # Disable UI in non-dev
   ```
3. Validate specs at `http://localhost:3001/v3/api-docs`

**Key Endpoints per Service:**
```
Identity Service (3001):
- POST /v1/auth/signup
- POST /v1/auth/login
- POST /v1/auth/refresh
- GET /v1/users/{id}
- POST /v1/users
- GET /v1/orgs/{id}

Workspace Service (3002):
- POST /v1/bots
- GET /v1/bots/{id}
- PUT /v1/bots/{id}
- DELETE /v1/bots/{id}
- POST /v1/bots/{id}/embed-code

... (similar for 7 more services)
```

**Effort:** 16 hours (Tech Lead Agent)
**Blocker:** Services must compile first

### Day 5: Kafka Event Schemas Defined

**Deliverable:** JSON Schema files for 9 Kafka topics

**File Structure:**
```
infrastructure/kafka/schemas/
├── workspace-events.schema.json
├── flow-events.schema.json
├── runtime-events.schema.json
├── conversation-events.schema.json
├── integration-events.schema.json
├── analytics-events.schema.json
├── billing-events.schema.json
├── knowledge-events.schema.json
└── README.md (schema guide)
```

**Each Schema Format:**
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "WorkspaceEvent",
  "description": "Events published by Workspace Service",
  "oneOf": [
    {
      "type": "object",
      "properties": {
        "eventType": { "const": "bot.created" },
        "aggregateId": { "type": "string", "format": "uuid" },
        "timestamp": { "type": "string", "format": "date-time" },
        "orgId": { "type": "string", "format": "uuid" },
        "data": {
          "type": "object",
          "properties": {
            "botId": { "type": "string" },
            "name": { "type": "string" },
            "model": { "type": "string" }
          }
        }
      },
      "required": ["eventType", "aggregateId", "timestamp", "orgId", "data"]
    },
    ... (other event types)
  ]
}
```

**Topics & Event Types:**
- **workspace-events:** bot.created, bot.updated, bot.deleted
- **flow-events:** flow.published, flow.updated, flow.deleted
- **conversation-events:** message.created, conversation.completed, handoff.requested
- **integration-events:** integration.executed, integration.failed
- **analytics-events:** event.tracked, cost.recorded
- **billing-events:** subscription.created, subscription.updated
- **knowledge-events:** document.indexed, document.updated
- **runtime-events:** session.started, session.completed, node.executed

**Effort:** 12 hours (Tech Lead Agent)
**Blocker:** None

### Day 6-7: Per-Service Flyway Migrations

**Deliverable:** Each service has its own Flyway migrations

**Current State:** V1-V9 in monolith, V10 creates 9 schemas

**New Structure:**
```
services/identity-service/src/main/resources/db/migration/
├── V1__init.sql              (create users, orgs, api_keys, refresh_tokens)
├── V2__indexes.sql           (add performance indexes)
└── V99__tenant_filter.sql    (add @Filter configuration)

services/workspace-service/src/main/resources/db/migration/
├── V1__init.sql              (create bots, org_memberships)
└── V99__tenant_filter.sql

... (8 more services)
```

**Steps:**
1. Create directory: `services/{service}/src/main/resources/db/migration/`
2. Extract relevant DDL from monolith V1-V9
3. Organize by schema (e.g., identity_schema.* → identity-service/V1)
4. Add tenant filters: `ALTER TABLE ... ADD COLUMN org_id UUID`

**Critical Files:**
- `/infrastructure/postgres/flyway/V10__create-schemas.sql` (keep, creates 9 schemas)
- Each service's V1-V9 isolated to its schema

**Effort:** 24 hours (Backend + Tech Lead Agents)
**Blocker:** Services must compile first

---

## Week 2: Integration & Event-Driven Architecture

### Day 8-9: Feign Clients Instantiated

**Deliverable:** All inter-service calls working

**Tasks:**
1. Populate Feign clients from threadly-common-spring
2. Add `@EnableFeignClients` to each service main class
3. Configure service URLs in application.yml:
   ```yaml
   service:
     identity:
       url: http://identity-service:3001
     workspace:
       url: http://workspace-service:3002
     ... (7 more)
   ```
4. Test each Feign call in integration tests

**Example (Runtime Service calling Knowledge Service):**
```java
// services/runtime-service/src/main/java/dev/threadly/runtime/config/
@Configuration
public class KnowledgeServiceClientConfig {
  @Bean
  public KnowledgeServiceClient knowledgeServiceClient(
      KnowledgeServiceClient client,
      CircuitBreakerRegistry registry,
      RetryRegistry retryRegistry) {
    // Apply circuit breaker + retry
    CircuitBreaker cb = registry.circuitBreaker(
        "knowledge", 
        CircuitBreakerConfig.getDefaultCircuitBreakerConfig()
    );
    Retry retry = retryRegistry.retry("knowledge", CircuitBreakerConfig.getDefaultRetryConfig());
    FeignDecorators decorators = CircuitBreakerConfig.buildFeignDecorators(cb, retry);
    return Resilience4jFeign.builder(decorators).build();
  }
}
```

**Feign Calls:**
- Workspace → Identity: verify org ownership
- Flow → Workspace: get bot config
- Runtime → Flow, Knowledge, Integration: execute flow
- Conversation → Workspace: bot context
- Integration → Workspace: webhook URLs
- Analytics → Workspace, Conversation: aggregate data

**Effort:** 20 hours (Backend Agent)
**Blocker:** OpenAPI specs must be defined

### Day 10-11: Event Publishing & Consumption

**Deliverable:** Kafka topics live, publishers + consumers working

**Tasks:**
1. Create Kafka topics (9 total)
   ```bash
   kafka-topics --create --topic workspace-events
   kafka-topics --create --topic flow-events
   ... (7 more)
   
   # Dead-letter queues
   kafka-topics --create --topic workspace-events.dlq
   ... (8 more DLQ topics)
   ```

2. Implement `@KafkaListener` in each service
   ```java
   // services/flow-service/src/main/java/dev/threadly/flow/event/
   @Service
   @Slf4j
   public class WorkspaceEventListener extends AbstractEventListener {
     
     @KafkaListener(topics = "workspace-events", groupId = "flow-service")
     public void onWorkspaceEvent(
         @Payload Map<String, Object> event,
         Acknowledgment ack) {
       try {
         handleEvent(event, (String) event.get("eventType"));
         ack.acknowledge();
       } catch (Exception e) {
         handleError(e, event, ...);
       }
     }
     
     @Override
     protected void handleEvent(Map<String, Object> event, String eventType) {
       switch (eventType) {
         case "bot.created":
           onBotCreated((Map) event.get("data"));
           break;
         ...
       }
     }
   }
   ```

3. Implement event publishing:
   ```java
   // services/workspace-service/src/main/java/dev/threadly/workspace/service/
   @Service
   public class BotService {
     @Autowired private EventPublisher eventPublisher;
     
     @Transactional
     public Bot createBot(BotCreateRequest req) {
       Bot bot = new Bot();
       bot.setName(req.getName());
       bot.setOrgId(TenantContext.getOrgId());
       botRepository.save(bot);
       
       // Publish event (same transaction)
       eventPublisher.publishEvent("workspace-events",
         OutboxEvent.builder()
           .eventType("bot.created")
           .aggregateId(UUID.fromString(bot.getId()))
           .payload(objectMapper.valueToTree(Map.of(
             "botId", bot.getId(),
             "orgId", bot.getOrgId(),
             "name", bot.getName()
           )))
           .build()
       );
       return bot;
     }
   }
   ```

**Test with LocalStack:**
```bash
docker-compose -f infrastructure/docker/docker-compose.yml up kafka zookeeper
# Wait for Kafka to be ready
# Test: kafka-console-consumer --topic workspace-events
```

**Effort:** 28 hours (AI+Widget Agent + Backend Agent)
**Blocker:** Kafka infrastructure must be running

### Day 12-13: Distributed Tracing Setup

**Deliverable:** W3C Trace Context flowing through all hops

**Tasks:**
1. Configure TraceIdInterceptor in each service:
   ```java
   // services/*/src/main/java/dev/threadly/{service}/config/
   @Configuration
   public class TracingConfig implements WebMvcConfigurer {
     @Autowired private TraceIdPropagator propagator;
     
     @Override
     public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new HandlerInterceptor() {
         @Override
         public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
           String traceparent = req.getHeader("traceparent");
           propagator.setTraceIdFromHeader(traceparent);
           return true;
         }
       });
     }
   }
   ```

2. Add to Nginx:
   ```nginx
   # infrastructure/nginx/nginx.conf
   server {
     listen 8080;
     
     location ~ /v1/(.+) {
       # Generate traceparent if missing
       set $traceparent $http_traceparent;
       if ($traceparent = "") {
         set $traceparent "00-$(uuid)-$(span_id)-01";
       }
       
       proxy_set_header traceparent $traceparent;
       proxy_pass http://UPSTREAM;
     }
   }
   ```

3. Logs include traceId:
   ```xml
   <!-- logback-spring.xml -->
   <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
     <encoder>
       <pattern>
         %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId}] - %msg%n
       </pattern>
     </encoder>
   </appender>
   ```

**Verify with:**
```bash
curl -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" \
  http://localhost:8080/v1/bots

# Check logs for traceId correlation across services
grep "4bf92f3577b34da6a3ce929d0e0e4736" services/*/logs/*.log
```

**Effort:** 16 hours (Tech Lead Agent)
**Blocker:** None

### Day 14: Saga Orchestration Activated

**Deliverable:** ConversationHandoffSaga + 1 additional saga working

**Tasks:**
1. Activate ConversationHandoffSaga (already implemented):
   - Check billing (call BillingServiceClient)
   - Transfer conversation (update DB)
   - Notify users (call NotificationService)
   - Automatic rollback on failure

2. Create new saga for subscription upgrade:
   ```java
   @Service
   public class SubscriptionUpgradeSaga extends SagaOrchestrator {
     public SubscriptionUpgradeSaga(EventPublisher eventPublisher, ObjectMapper mapper) {
       super(mapper);
       addStep(new ReserveCapacityStep());
       addStep(new ChargeStripeStep());
       addStep(new ActivatePlanStep());
     }
     
     @KafkaListener(topics = "billing-events")
     public void onUpgradeRequested(@Payload Map<String, Object> event) {
       try {
         executeAll();
         publishSagaCompleted(event);
       } catch (SagaExecutionException e) {
         // Auto rollback already happened
         publishSagaFailed(e);
       }
     }
   }
   ```

3. Test with SagaTestCase:
   ```java
   @SpringBootTest
   public class SubscriptionUpgradeSagaTest extends SagaTestCase {
     @Test
     public void testSagaRollsBackOnStripeFailure() {
       // Publish upgrade event
       // Simulate Stripe failure at step 2
       // Verify step 1 rolled back
     }
   }
   ```

**Effort:** 12 hours (Backend Agent)
**Blocker:** Kafka must be working

---

## Week 3: Testing & Validation

### Day 15-16: Integration Test Matrix

**Deliverable:** Cross-service integration tests passing (80%+ coverage)

**Test Coverage:**
```
✓ Identity Service
  ✓ User signup → JWT issued
  ✓ JWT refresh
  ✓ Org creation + multi-tenant filtering

✓ Workspace Service
  ✓ Bot creation → workspace-events published
  ✓ Team invite → org_memberships created
  ✓ Cross-service Feign call to Identity

✓ Flow Service
  ✓ Receive bot.created event → create default flow
  ✓ Flow publish → flow-events published
  ✓ Cross-service Feign call to Workspace

✓ Runtime Service
  ✓ Session start → Redis state created
  ✓ Execute AiReply node → call Knowledge Service
  ✓ Execute IntegrationNode → call Integration Service
  ✓ Streaming tokens to widget via Centrifugo

✓ Conversation Service
  ✓ Receive node.executed event from Runtime
  ✓ Save message → publish message.created
  ✓ Handoff saga triggered

... (similar for 4 more services)
```

**Test Framework:**
- Testcontainers (Postgres, Redis, Kafka)
- Embedded Kafka (@EmbeddedKafka)
- WireMock for external service mocks
- Mock CircuitBreaker states

**Location:**
```
services/*/src/test/java/dev/threadly/*/
├── BotIntegrationTest.java
├── WorkspaceEventListenerTest.java
├── FeignClientIntegrationTest.java
├── SagaTest.java
└── TenantIsolationTest.java
```

**Effort:** 32 hours (Testing Agent)
**Blocker:** All services must compile + be integrated

### Day 17-18: E2E Test Scenarios

**Deliverable:** 5 full workflow tests passing

**Scenarios:**
1. **Signup → Create Bot → Embed Widget → Send Message → AI Reply**
   ```gherkin
   Given a user signs up
   When they create a bot
   And embed the widget code
   And customer visits widget
   And sends a message
   Then bot replies with AI response
   And message saved to Conversation Service
   And cost tracked in Analytics Service
   And all requests correlated by traceId
   ```

2. **Create Integration → Execute → Log Result**
   ```
   Workspace.createIntegration()
     → Integration.registerConnector()
     → Runtime.executeIntegration()
     → Integration.handleCallback()
     → publish integration.executed event
     → Analytics.trackEvent()
   ```

3. **Conversation Handoff Saga**
   ```
   Widget requests handoff
     → Runtime publishes conversation.handoff
     → ConversationHandoffSaga.executeAll()
       → Step 1: Check billing
       → Step 2: Assign agent
       → Step 3: Notify users
     → publish saga.completed
   ```

4. **Knowledge Base Ingestion**
   ```
   Customer uploads KB file
     → Conversation publishes kb.ingestion_requested
     → Knowledge.ingestDocument()
     → Chunk + embed with Voyage
     → Upsert to Qdrant
     → publish knowledge.document_indexed
     → Flow can now use RAG in AiReply nodes
   ```

5. **Analytics Export**
   ```
   Dashboard requests analytics export
     → Analytics.generateExport()
     → Query conversation_schema.messages + runtime_schema.sessions
     → Join with events from Kafka topic
     → Return CSV
   ```

**Test Framework:** Playwright (full browser E2E)
**Location:** `tests/e2e/`
**Coverage:** 50+ assertions across 5 tests

**Effort:** 24 hours (Testing Agent)
**Blocker:** All services fully integrated

### Day 19-20: Performance Benchmarks

**Deliverable:** Latency baseline captured

**Metrics to Track:**
```
HTTP call latencies (p50, p95, p99):
- Identity.signup → 150ms
- Workspace.createBot → 200ms (includes event publish)
- Runtime.executeFlow → 500ms (includes Knowledge + AI call)

Kafka latency:
- Publish to topic → 50ms
- Consumer lag → <1s

Database queries (top 10):
- Bot lookup (botId, orgId) → 10ms
- Message insert → 20ms
- Analytics aggregation → 500ms

Widget metrics:
- Time to interactive → <1.5s
- First token from AI → <2s
- Message delivery → <200ms

Circuit breaker transitions:
- Healthy → Open: 5 failures
- Recovery time: 30s
```

**Tools:** JMeter, k6, Prometheus
**Location:** `infrastructure/performance-tests/`

**Effort:** 16 hours (Tech Lead Agent)
**Blocker:** All services running in Docker Compose

### Day 21: Security Review

**Deliverable:** Multi-tenant isolation verified + secrets validated

**Security Checklist:**
- [ ] No hardcoded secrets (all env vars)
- [ ] JWT RS256 validation in all services
- [ ] HMAC signing on inter-service calls
- [ ] SQL injection tests (parameterized queries)
- [ ] XSS tests (widget + dashboard)
- [ ] CSRF token on state-changing endpoints
- [ ] Rate limiting enforced (Nginx layer)
- [ ] TenantContext set from JWT orgId claim
- [ ] Cross-tenant queries return empty (not error)
- [ ] Secrets in CI/CD masked in logs

**Test Class:** TenantIsolationSecurityTest.java
```java
@Test
public void testOrgACannotReadOrgBData() {
  // Create 2 orgs, 2 users
  // User A logs in, gets token with orgId=A
  // User A tries to access Bot owned by Org B
  // Verify 404 (not 403, not 500)
}
```

**Effort:** 8 hours (Tech Lead Agent)
**Blocker:** None

---

## Week 4: Validation & Cutover Prep

### Day 22-23: Kubernetes Deployment

**Deliverable:** All 9 services deploy to local K8s cluster

**Requirements:**
- 1 ConfigMap per service (environment variables)
- 1 Deployment per service (3 replicas, resource requests)
- 1 Service per service (ClusterIP for internal routing)
- 1 Ingress for external traffic (nginx-ingress)
- StatefulSets for Postgres, Redis, Kafka

**K8s Manifests:**
```yaml
# infrastructure/kubernetes/
├── namespace.yaml
├── configmap.yaml                (9 services)
├── deployments/
│   ├── identity-service-deployment.yaml
│   ├── workspace-service-deployment.yaml
│   ... (7 more)
├── services/
│   ├── identity-service.yaml
│   ... (8 more)
├── statefulsets/
│   ├── postgres-statefulset.yaml
│   ├── redis-statefulset.yaml
│   └── kafka-statefulset.yaml
├── ingress.yaml
└── postgres-secret.yaml
```

**Deploy with:**
```bash
kubectl apply -f infrastructure/kubernetes/
kubectl get pods -n threadly
kubectl logs -f identity-service-xxxxx -n threadly
```

**Health Checks (Kubernetes probes):**
```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

**Effort:** 20 hours (Tech Lead Agent)
**Blocker:** All services must pass tests

### Day 24-25: Docker Compose Verification

**Deliverable:** `make up` brings up full stack (13 services)

**Services:**
1. Nginx (API Gateway)
2. Postgres (9 schemas)
3. Redis
4. Kafka + Zookeeper
5-13. 9 Microservices

**Verify with:**
```bash
make up                    # Start all services
make health                # Check health endpoints
make logs                  # View aggregated logs

# Test workflow
curl -X POST http://localhost:8080/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}'

# Verify tracing
grep "traceId" services/*/logs/*.log
```

**Effort:** 12 hours (Tech Lead Agent)
**Blocker:** Docker Compose must be configured

### Day 26-27: Smoke Tests (Happy Path)

**Deliverable:** Critical workflows validated

**Smoke Test Suite:**
1. Signup + Login
2. Create Bot
3. Create Flow + Publish
4. Embed Widget + Send Message
5. AI Reply (mock LLM)
6. Analytics query
7. Webhook delivery
8. Knowledge base search
9. Team invite
10. Subscription create

**Test Framework:** Playwright
**Pass Criteria:** All 10 tests pass without manual intervention
**Duration:** <5 minutes total

**Run with:**
```bash
npm run smoke:tests --workspace=tests
```

**Effort:** 8 hours (Testing Agent)
**Blocker:** Full stack must be running

### Day 28: Phase 1 Readiness Gate

**Deliverable:** Phase 1 cutover approved

**Checklist:**
- [ ] All 9 services compile + pass tests
- [ ] OpenAPI specs generated for all services
- [ ] Kafka topics created + schemas defined
- [ ] Feign clients working (all dependencies resolved)
- [ ] Event publishing/consumption end-to-end
- [ ] Saga orchestration active + tested
- [ ] Distributed tracing working
- [ ] Circuit breaker tested
- [ ] Kubernetes manifests apply cleanly
- [ ] Docker Compose brings up full stack
- [ ] Smoke tests pass
- [ ] Security review completed
- [ ] Performance benchmarks captured
- [ ] Runbooks written (deployment, incident response)
- [ ] Rollback procedure documented

**Gate Approval:** Tech Lead + Backend Agent sign-off

**Effort:** 4 hours (Tech Lead Agent + PM Agent)
**Blocker:** All above tasks complete

---

## Deliverables Summary

| Week | Deliverable | Effort | Owner | Status |
|------|-------------|--------|-------|--------|
| W1 | Service scaffolding complete | 40h | Backend | 🟡 To Do |
| W1 | OpenAPI specs (9 services) | 16h | Tech Lead | 🟡 To Do |
| W1 | Kafka event schemas (9 topics) | 12h | Tech Lead | 🟡 To Do |
| W1 | Per-service Flyway migrations | 24h | Backend + Tech Lead | 🟡 To Do |
| W2 | Feign clients instantiated | 20h | Backend | 🟡 To Do |
| W2 | Kafka publishing/consumption | 28h | AI + Backend | 🟡 To Do |
| W2 | Distributed tracing setup | 16h | Tech Lead | 🟡 To Do |
| W2 | Saga orchestration activated | 12h | Backend | 🟡 To Do |
| W3 | Integration test matrix | 32h | Testing | 🟡 To Do |
| W3 | E2E test scenarios | 24h | Testing | 🟡 To Do |
| W3 | Performance benchmarks | 16h | Tech Lead | 🟡 To Do |
| W3 | Security review | 8h | Tech Lead | 🟡 To Do |
| W4 | Kubernetes deployment | 20h | Tech Lead | 🟡 To Do |
| W4 | Docker Compose verification | 12h | Tech Lead | 🟡 To Do |
| W4 | Smoke tests | 8h | Testing | 🟡 To Do |
| W4 | Readiness gate | 4h | Tech Lead + PM | 🟡 To Do |
| **TOTAL** | | **292 hours** | **All** | **🟡 To Do** |

**Team Allocation (4 weeks):**
- Backend Agent: 72 hours (18 hours/week)
- Frontend Agent: 0 hours (v2 future sprint)
- AI+Widget Agent: 28 hours (7 hours/week)
- Testing Agent: 64 hours (16 hours/week)
- Tech Lead Agent: 116 hours (29 hours/week)
- PM Agent: 12 hours (3 hours/week)

---

## Risk Mitigation

### High Risk: Service Dependencies

**Risk:** Runtime Service depends on 4 services; if one fails, entire flow fails.

**Mitigation:**
- [ ] Apply CircuitBreakerConfig to all Feign calls
- [ ] Implement fallback logic (return error DTO)
- [ ] Test circuit breaker transitions
- [ ] Monitor circuit breaker open count in Prometheus

### High Risk: Data Migration

**Risk:** Moving data from monolith to 9 isolated schemas might lose consistency.

**Mitigation:**
- [ ] Test migration script on copy of production DB
- [ ] Run shadow mode (Week 1-2) before cutover
- [ ] Implement dual-write (Week 2-3)
- [ ] Use Kafka for change data capture (CDC)
- [ ] Rollback procedure ready

### Medium Risk: Kafka Consumer Lag

**Risk:** If Kafka falls behind, events process late.

**Mitigation:**
- [ ] Monitor consumer lag per topic
- [ ] Alert if lag > 5 minutes
- [ ] Implement dead-letter-queue (DLQ)
- [ ] Replay from DLQ when fixed

### Medium Risk: Multi-Tenancy Enforcement

**Risk:** Accidental data leak between orgs (SQL injection, missing filters).

**Mitigation:**
- [ ] TenantIsolationSecurityTest (cross-org data access blocked)
- [ ] Parameterized queries everywhere
- [ ] Code review on all data access paths
- [ ] Automated SQL injection scanning (sonarqube)

---

## Success Metrics

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| Services deployed independently | 9/9 | 0/9 | 🟡 |
| Feign client calls succeed | 100% | 0% | 🟡 |
| Kafka events published/consumed | 100% | 0% | 🟡 |
| Circuit breaker working | Yes | N/A | 🟡 |
| Distributed tracing working | Yes | N/A | 🟡 |
| Multi-tenant isolation enforced | 100% | 100% | ✅ |
| Smoke tests pass | 10/10 | 0/10 | 🟡 |
| Performance: API p50 < 150ms | Yes | N/A | 🟡 |
| Performance: Widget < 35KB | Yes | Yes | ✅ |
| Kubernetes deployment | Yes | N/A | 🟡 |
| Security review signed off | Yes | N/A | 🟡 |

---

## Contingency Plans

### If Feign Clients Fail
**Fallback:** Synchronous gRPC instead of REST
**Timeline Impact:** +1 week
**Files:** `infrastructure/grpc/`

### If Kafka Topics Lag
**Fallback:** Redis Streams instead of Kafka
**Timeline Impact:** +3 days
**Files:** `services/*/config/RedisStreamConfig.java`

### If Kubernetes Deployment Fails
**Fallback:** Docker Swarm for orchestration
**Timeline Impact:** +2 days
**Files:** `infrastructure/swarm/`

### If Multi-Tenancy Enforcement Gaps Found
**Fallback:** Implement database row-level security (RLS)
**Timeline Impact:** +1 week
**Files:** `infrastructure/postgres/rls-policies.sql`

---

## Phase 2 Readiness

Upon Phase 1 completion:

1. **Cutover to Microservices** (Week 1)
   - Shut down monolith
   - Point DNS to Nginx (9 services)
   - Verify no data loss

2. **Optimize Performance** (Week 2)
   - Add caching (Redis)
   - Optimize DB queries
   - Tune JVM heap per service

3. **Scale Horizontally** (Week 3)
   - Increase replicas (K8s HPA)
   - Load test to 100 concurrent users
   - Monitor autoscaling

4. **Advanced Features** (Week 4)
   - Service mesh (Istio)
   - Multi-region deployment
   - Advanced monitoring (OpenTelemetry)

---

**Document Created:** 2026-05-24  
**Last Updated:** 2026-05-24  
**Next Review:** 2026-05-27 (Phase 1 kickoff)
