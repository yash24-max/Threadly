# Threadly Common Spring Library - Usage Guide

**Version**: 1.0.0-SNAPSHOT  
**Location**: `/Users/yasva/Kapture/Microservice/Project/Threadly/threadly-common-spring`  
**Maven**: `dev.threadly:threadly-common-spring`

This shared library provides all common abstractions, configurations, and utilities needed by all 9 Threadly microservices.

---

## Quick Start

Add to your microservice's `pom.xml`:

```xml
<dependency>
  <groupId>dev.threadly</groupId>
  <artifactId>threadly-common-spring</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

All features auto-enable via Spring Boot auto-configuration.

---

## Core Components

### 1. Tenant Context (Multi-Tenancy)

**File**: `TenantContext.java`

Holds current org_id, user_id, and email in thread-local storage. Automatically extracted from JWT claims by SecurityConfig.

```java
// In a service method
UUID orgId = TenantContext.getTenantId();  // org_id from JWT
UUID userId = TenantContext.getUserId();
String email = TenantContext.getEmail();

// Optional (returns null if not set)
UUID orgIdOptional = TenantContext.getTenantIdOptional();
```

**Auto-set by**: SecurityConfig filter on each request  
**Cleared by**: SecurityConfig filter after request completes

---

### 2. Hibernate Tenant Filter

**File**: `TenantFilter.java`

Automatically filters all queries to only return rows where `org_id = current_tenant_id`.

#### Usage in entities:

```java
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "orgId", type = "java.util.UUID")
)
@Filter(name = "tenantFilter", condition = "org_id = :orgId")
public class Bot {
  @Id
  private UUID id;
  
  @Column(nullable = false)
  private UUID org_id;  // Must exist in all multi-tenant tables
  
  // ... other fields
}
```

#### Enable filter:

```java
// In your session factory or entity listener
import org.hibernate.Session;
import dev.threadly.common.context.TenantFilter;

@Component
public class TenantFilterInit {
  
  @EventListener
  public void onSessionCreate(org.springframework.orm.hibernate5.HibernateOperationsSessionCallback event) {
    Session session = event.getSession();
    TenantFilter.enableTenantFilter(session);
  }
}
```

**Result**: All queries automatically filtered by current org_id. No manual filtering needed.

---

### 3. JWT Security & Token Validation

**File**: `SecurityConfig.java`

Validates JWT tokens (RS256), extracts org_id/user_id from claims, and sets TenantContext.

#### Configuration:

```yaml
threadly:
  jwt:
    issuer: https://threadly.dev
    public-key-url: http://identity-service:3001/.well-known/jwks.json

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://threadly.dev
          jwk-set-uri: http://identity-service:3001/.well-known/jwks.json
```

#### Public endpoints (bypass auth):

- `/auth/**` (signup, login, refresh)
- `/health`
- `/actuator/**`

#### Protected endpoints:

All other endpoints require `Authorization: Bearer <token>` header.

#### JWT claims expected:

```json
{
  "sub": "user-uuid",
  "org_id": "org-uuid",
  "email": "user@example.com",
  "iss": "https://threadly.dev"
}
```

---

### 4. Error Responses (RFC 7807)

**File**: `ErrorModel.java`

Standardized error response following RFC 7807 Problem+JSON format.

#### Usage:

```java
import dev.threadly.common.dto.ErrorModel;

@GetMapping("/bots/{botId}")
public ResponseEntity<BotDTO> getBot(@PathVariable UUID botId) {
  Bot bot = botRepository.findById(botId)
    .orElseThrow(() -> new ResourceNotFoundException(botId));
  
  if (!bot.getOrgId().equals(TenantContext.getTenantId())) {
    throw new ForbiddenException("Bot not found");
  }
  
  return ResponseEntity.ok(new BotDTO(bot));
}

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorModel> handleNotFound(ResourceNotFoundException e) {
  return ResponseEntity
    .status(404)
    .body(ErrorModel.notFound("BOT_NOT_FOUND", "Bot", e.getId().toString()));
}

@ExceptionHandler(ForbiddenException.class)
public ResponseEntity<ErrorModel> handleForbidden(ForbiddenException e) {
  return ResponseEntity
    .status(403)
    .body(ErrorModel.forbidden("FORBIDDEN", e.getMessage()));
}
```

#### Pre-built helpers:

```java
ErrorModel.badRequest(code, title, detail)
ErrorModel.unauthorized(code, detail)
ErrorModel.forbidden(code, detail)
ErrorModel.notFound(code, resource, identifier)
ErrorModel.conflict(code, detail)
ErrorModel.tooManyRequests(detail)
ErrorModel.internalServerError(code, detail)
ErrorModel.serviceUnavailable(detail)
```

#### Add trace ID for debugging:

```java
ErrorModel.notFound("BOT_NOT_FOUND", "Bot", botId)
  .withTraceId(request.getHeader("X-Trace-ID"))
  .withContext("attempted_org", TenantContext.getTenantIdOptional());
```

---

## Service-to-Service Communication

### Feign Clients

6 Feign clients auto-configured for inter-service REST calls with circuit breaker + retry.

#### 1. IdentityServiceClient

```java
import dev.threadly.common.feign.IdentityServiceClient;
import org.springframework.cloud.openfeign.FeignClient;

@Service
public class WorkspaceService {
  @Autowired
  private IdentityServiceClient identityClient;
  
  public void validateOrg(UUID orgId) {
    var response = identityClient.validateTenancy(
      new IdentityServiceClient.TenancyValidationRequest(orgId, TenantContext.getUserId())
    );
    
    if (!response.valid()) {
      throw new ForbiddenException("User not member of org");
    }
  }
}
```

**Methods**:
- `signup(SignupRequest)` → SignupResponse
- `login(LoginRequest)` → LoginResponse
- `refreshToken(RefreshTokenRequest)` → TokenResponse
- `logout(token)` → void
- `getCurrentUser(token)` → CurrentUserResponse
- `validateTenancy(TenancyValidationRequest)` → TenancyValidationResponse
- `generateApiKey(request, token)` → ApiKeyResponse
- `revokeApiKey(keyId, token)` → void

---

#### 2. WorkspaceServiceClient

```java
@Service
public class FlowService {
  @Autowired
  private WorkspaceServiceClient workspaceClient;
  
  public void verifyBotOwnership(UUID botId) {
    var botDto = workspaceClient.getBot(botId, getAuthToken());
    
    if (!botDto.orgId().equals(TenantContext.getTenantId())) {
      throw new ForbiddenException("Bot not in current org");
    }
  }
}
```

---

#### 3. FlowServiceClient

```java
@Service
public class RuntimeService {
  @Autowired
  private FlowServiceClient flowClient;
  
  public void executeFlow(UUID flowId, Session session) {
    FlowServiceClient.FlowDTO flow = flowClient.getFlow(flowId, getAuthToken());
    // Execute flow.flowJson() (JsonNode)
  }
}
```

---

#### 4. ConversationServiceClient

```java
@Service
public class RuntimeService {
  @Autowired
  private ConversationServiceClient conversationClient;
  
  public void saveMessage(UUID conversationId, String content) {
    conversationClient.saveMessage(conversationId, 
      new SaveMessageRequest("bot", content), 
      getAuthToken()
    );
  }
}
```

---

#### 5. KnowledgeServiceClient

```java
@Service
public class RuntimeService {
  @Autowired
  private KnowledgeServiceClient kbClient;
  
  public List<String> queryKnowledgeBase(UUID botId, String query) {
    var results = kbClient.query(botId,
      new QueryRequest(query, 5), // top-5 results
      getAuthToken()
    );
    return results.results();
  }
}
```

---

#### 6. IntegrationServiceClient

```java
@Service
public class RuntimeService {
  @Autowired
  private IntegrationServiceClient integrationClient;
  
  public void executeAction(UUID integrationId, String actionName, Map<String, Object> params) {
    integrationClient.executeAction(integrationId,
      new ExecuteActionRequest(actionName, params),
      getAuthToken()
    );
  }
}
```

---

### Feign Configuration

**File**: `FeignConfig.java`

- 10-second connection timeout
- 10-second read timeout
- OkHttp client with connection pooling
- Automatic retry on failure (see Resilience4j)
- Request/response logging

Override service URL via environment variable:

```bash
IDENTITY_SERVICE_URL=http://custom-identity-host:3001
WORKSPACE_SERVICE_URL=http://workspace-service:3002
FLOW_SERVICE_URL=http://flow-service:3003
CONVERSATION_SERVICE_URL=http://conversation-service:3005
KNOWLEDGE_SERVICE_URL=http://knowledge-service:3006
INTEGRATION_SERVICE_URL=http://integration-service:3009
```

---

## Event-Driven Architecture

### Outbox Pattern (Reliable Event Publishing)

**Files**: `OutboxEvent.java`, `OutboxRepository.java`, `OutboxService.java`

Guarantees events are published to Kafka if and only if the main database transaction succeeds (no distributed transaction needed).

#### Usage:

```java
import dev.threadly.common.kafka.OutboxEvent;
import dev.threadly.common.kafka.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConversationService {
  
  @Autowired
  private OutboxService outboxService;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @Transactional
  public void completeConversation(UUID conversationId) {
    // 1. Update domain entity
    Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
    conversation.setStatus("closed");
    conversationRepository.save(conversation);
    
    // 2. Publish event (same transaction)
    outboxService.publish(
      OutboxEvent.builder()
        .eventType("conversation.completed")
        .aggregateId(conversationId)
        .payload(objectMapper.valueToTree(Map.of(
          "conversationId", conversationId,
          "botId", conversation.getBotId(),
          "orgId", conversation.getOrgId(),
          "status", "closed"
        )))
        .build()
    );
  }
}
```

**How it works**:
1. `publish()` saves event to `outbox_events` table (same database transaction)
2. Background job polls `outbox_events` every 5 seconds
3. Unpublished events are sent to Kafka (topic derived from eventType)
4. On successful send, `published_at` is set to current timestamp
5. Next poll skips already-published events

**Topic naming**:
- `conversation.completed` → topic `conversation-events`
- `flow.published` → topic `flow-events`
- `kb.ingestion.completed` → topic `kb-events`

---

### Kafka Producer

**File**: `KafkaProducerConfig.java`

Auto-configured with:
- String key serializer (for partitioning by org_id or aggregate_id)
- JSON value serializer
- Acknowledgment: all (wait for all replicas)
- Retries: 3
- Compression: Snappy

#### Direct publish (if not using Outbox):

```java
@Service
public class AnalyticsService {
  
  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;
  
  public void trackEvent(String eventType, Map<String, Object> data) {
    kafkaTemplate.send("analytics-events", 
      UUID.randomUUID().toString(), // key
      data                          // value
    );
  }
}
```

---

### Kafka Consumer

**File**: `KafkaConsumerConfig.java`

Auto-configured with:
- String key deserializer
- JSON value deserializer (auto-infer type)
- Manual offset commit (for idempotency)
- Consumer group: `${SPRING_APPLICATION_NAME}`
- Concurrency: 3 listeners per topic

#### Usage with AbstractEventListener base class:

```java
import dev.threadly.common.kafka.AbstractEventListener;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class ConversationEventListener extends AbstractEventListener {
  
  @Autowired
  private ConversationService conversationService;
  
  @KafkaListener(
    topics = "conversation-events",
    groupId = "conversation-service",
    containerFactory = "kafkaListenerContainerFactory"
  )
  public void onConversationEvent(
      @Payload Map<String, Object> event,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment ack) {
    
    String eventType = (String) event.get("eventType");
    try {
      handleEvent(event, eventType);
      ack.acknowledge(); // Manual commit on success
    } catch (Exception e) {
      handleError(e, event, eventType, partition, offset);
      // Retry logic + eventual DLQ routing in handleError
    }
  }
  
  @Override
  protected void handleEvent(Map<String, Object> event, String eventType) throws Exception {
    switch (eventType) {
      case "conversation.started":
        var convId = (String) event.get("conversationId");
        conversationService.onStarted(UUID.fromString(convId));
        break;
        
      case "conversation.completed":
        var completedId = (String) event.get("conversationId");
        conversationService.onCompleted(UUID.fromString(completedId));
        break;
    }
  }
}
```

**Features of AbstractEventListener**:
- Automatic retry with exponential backoff (100ms, 200ms, 400ms)
- Dead-letter-queue routing on persistent failure
- Trace ID extraction from Kafka headers (W3C format)
- Distributed tracing support

---

## Resilience Patterns

### Circuit Breaker

**File**: `Resilience4jConfig.java`

Auto-configured on all Feign client calls:

- **Opens** after 5 failures (50% fail rate or 50%+ slow calls)
- **Waits** 30 seconds before attempting recovery
- **Half-open** state: test 3 requests to determine if service recovered
- **Slow call threshold**: requests > 10 seconds marked as slow

#### Manual usage:

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class RuntimeService {
  
  @CircuitBreaker(name = "flowService", fallbackMethod = "fallbackGetFlow")
  @Retry(name = "flowService")
  public FlowDTO getFlow(UUID flowId) {
    return flowClient.getFlow(flowId, getToken());
  }
  
  public FlowDTO fallbackGetFlow(UUID flowId, Exception ex) {
    log.warn("Circuit breaker open for Flow Service, returning cached flow", ex);
    return cachedFlowService.getCached(flowId);
  }
}
```

---

### Timeout

Feign timeout: 10 seconds (configured in FeignConfig)

Override per-service via environment:

```bash
FEIGN_CONNECT_TIMEOUT=5000
FEIGN_READ_TIMEOUT=15000
```

---

## Idempotency

**File**: `IdempotencyKeyHandler.java`

Automatic request deduplication for POST/PATCH operations.

#### Usage:

```java
import dev.threadly.common.handler.IdempotencyKeyHandler.Idempotent;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
  
  @PostMapping
  @Idempotent(ttlSeconds = 3600) // Cache for 1 hour
  public ResponseEntity<ConversationDTO> createConversation(
      @RequestBody CreateConversationRequest request) {
    // Expensive operation...
    return ResponseEntity.ok(new ConversationDTO(...));
  }
}
```

**How client uses it**:

```bash
curl -X POST https://threadly.dev/conversations \
  -H "Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{"botId": "...", "visitorId": "..."}'

# Same request with same key = cached response (no re-execution)
# Different request with same key = still returns cached (idempotent)
```

**Storage**:
- Redis preferred (fast, distributed)
- In-memory fallback if Redis unavailable

---

## Distributed Tracing

**File**: `OpenTelemetryConfig.java`

Automatically enabled. Traces all requests and sends to OTLP collector.

#### Configuration:

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0  # Sample 100% of traces
  otlp:
    tracing:
      endpoint: http://otel-collector:4317
```

#### Manual instrumentation:

```java
import io.opentelemetry.api.trace.Tracer;

@Service
public class RuntimeService {
  
  @Autowired
  private Tracer tracer;
  
  public void executeFlow(UUID flowId) {
    try (var span = tracer.spanBuilder("execute-flow")
        .setAttribute("flow_id", flowId.toString())
        .setAttribute("org_id", TenantContext.getTenantId().toString())
        .startSpan()) {
      
      // Execution logic
      span.addEvent("flow_started");
      
      // ...
      
      span.addEvent("flow_completed");
    }
  }
}
```

**Trace propagation**:
- Automatic via HTTP headers (W3C Trace Context)
- Kafka headers: `traceparent`
- One trace ID spans all service hops

**Exporters**: Honeycomb, Tempo, Jaeger (via OTLP)

---

## Integration Testing

**Files**: `AbstractIntegrationTest.java`, `TestFixtures.java`

Base class with Testcontainers for Postgres + Kafka.

#### Usage:

```java
import dev.threadly.common.test.AbstractIntegrationTest;
import dev.threadly.common.test.TestFixtures;
import dev.threadly.common.test.TestFixtures.*;

@SpringBootTest
public class ConversationServiceIntegrationTest extends AbstractIntegrationTest {
  
  @Autowired
  private MockMvc mockMvc;
  
  @Autowired
  private ConversationRepository conversationRepository;
  
  @Test
  void testCreateConversation() throws Exception {
    // Create test fixtures
    UserFixture user = TestFixtures.createUser();
    OrganizationFixture org = TestFixtures.createOrganization(user.userId());
    BotFixture bot = TestFixtures.createBot(org.orgId());
    ConversationFixture conversation = TestFixtures.createConversation(bot.botId(), org.orgId());
    
    // Save to database
    conversationRepository.save(new Conversation(
      conversation.conversationId(),
      conversation.botId(),
      conversation.orgId(),
      conversation.visitorId(),
      conversation.status()
    ));
    
    // Test API endpoint
    mockMvc.perform(get("/conversations/" + conversation.conversationId())
        .header("Authorization", "Bearer " + generateToken(org.orgId(), user.userId())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("open"));
  }
}
```

**Fixtures available**:
- UserFixture
- OrganizationFixture
- BotFixture
- ConversationFixture
- MessageFixture
- LeadFixture
- FlowFixture
- SessionFixture

---

## Advanced Patterns

### Saga Pattern (Cross-Service Transactions)

**Files**: `SagaOrchestrator.java`, `SagaStep.java`, `ConversationHandoffSaga.java`

Orchestrates multi-step transactions across services using events.

#### Example: Conversation handoff saga

```java
import dev.threadly.common.saga.SagaOrchestrator;
import dev.threadly.common.saga.SagaStep;
import org.springframework.stereotype.Component;

@Component
public class ConversationHandoffSaga extends SagaOrchestrator {
  
  @Autowired
  private BillingServiceClient billingClient;
  
  @Autowired
  private ConversationService conversationService;
  
  public void executeHandoff(UUID conversationId) {
    // Step 1: Check billing
    var step1 = new SagaStep("billing-check", () -> {
      var plan = billingClient.checkPlan(TenantContext.getTenantId(), getToken());
      if (plan.handoffsRemaining() <= 0) {
        throw new BillingException("Handoff limit exceeded");
      }
      return Map.of("status", "approved");
    });
    
    // Step 2: Update conversation
    var step2 = new SagaStep("assign-agent", () -> {
      conversationService.assignAgent(conversationId, getAgentId());
      return Map.of("assigned_at", Instant.now());
    });
    
    // Step 3: Notify agent
    var step3 = new SagaStep("notify-agent", () -> {
      publishEvent("agent.assigned", Map.of(
        "conversationId", conversationId,
        "agentId", getAgentId()
      ));
      return Map.of("notified", true);
    });
    
    // Execute saga
    executeSteps(step1, step2, step3);
  }
}
```

---

### Dual-Write Pattern (Migration)

**Files**: `DualWriteService.java`, `DualWriteInterceptor.java`

For migrating from monolith to microservices. Writes to both simultaneously.

#### Configuration:

```yaml
threadly:
  migration:
    dual-write-enabled: true
    dual-write-services:
      - conversation-service
      - flow-service
```

#### Usage:

```java
@Service
public class ConversationService {
  
  @Autowired
  private DualWriteService dualWrite;
  
  @Transactional
  public Conversation createConversation(CreateConversationRequest request) {
    // 1. Create in new service (primary)
    Conversation newConversation = new Conversation(...);
    conversationRepository.save(newConversation);
    
    // 2. Dual-write to monolith (secondary)
    dualWrite.sync("conversations", newConversation.getId(), newConversation);
    
    return newConversation;
  }
}
```

---

## Configuration Best Practices

### Environment Variables

```bash
# JWT/Security
JWT_ISSUER=https://threadly.dev
JWT_JWKS_URL=http://identity-service:3001/.well-known/jwks.json

# Service URLs
IDENTITY_SERVICE_URL=http://identity-service:3001
WORKSPACE_SERVICE_URL=http://workspace-service:3002
FLOW_SERVICE_URL=http://flow-service:3003
CONVERSATION_SERVICE_URL=http://conversation-service:3005
KNOWLEDGE_SERVICE_URL=http://knowledge-service:3006
INTEGRATION_SERVICE_URL=http://integration-service:3009

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Observability
TRACING_ENABLED=true
OTLP_ENDPOINT=http://otel-collector:4317

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/threadly
SPRING_DATASOURCE_USERNAME=threadly
SPRING_DATASOURCE_PASSWORD=threadly
```

---

## Monitoring & Metrics

All services expose metrics endpoint:

```
GET /actuator/prometheus
```

Key metrics:
- `http_server_requests_seconds` - Request latency
- `resilience4j_circuitbreaker_state` - Circuit breaker state
- `kafka_consumer_records_lag_total` - Kafka consumer lag
- `jvm_memory_used_bytes` - JVM memory
- `otel_traces_total` - OpenTelemetry trace count

---

## Troubleshooting

### TenantContext is null

**Cause**: Request not authenticated or SecurityConfig filter not running  
**Fix**: Add `Authorization: Bearer <token>` header, ensure `/auth/**` is public

### Feign client timeout

**Cause**: Remote service slow or unreachable  
**Fix**: Check service health, increase timeout via `FEIGN_READ_TIMEOUT`

### Kafka consumer lag

**Cause**: Event processing slow or consumer crash  
**Fix**: Check logs, verify database connectivity, increase consumer concurrency

### OpenTelemetry no traces

**Cause**: OTLP collector unreachable  
**Fix**: Set `TRACING_ENABLED=false` to disable (won't break app), fix collector URL

---

## Support & References

- Microservices Architecture: `/docs/18-microservices-architecture.md`
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Cloud: https://spring.io/cloud
- Resilience4j: https://resilience4j.readme.io/
- OpenTelemetry: https://opentelemetry.io/
