# Threadly Microservices Resilience Patterns Guide

This guide documents the production-ready microservices patterns implemented in `threadly-common-spring`. These patterns provide resilience, event-driven architecture, saga orchestration, and distributed tracing across the Threadly microservices ecosystem.

## Overview

The following patterns have been implemented:

1. **Circuit Breaker Pattern** - Prevent cascade failures in inter-service calls
2. **Saga Orchestration** - Coordinate multi-step distributed transactions
3. **Kafka Event Publishing (Outbox Pattern)** - Guarantee event delivery via database
4. **Kafka Event Consumption** - Reliable event processing with retries and idempotency
5. **Distributed Tracing** - Correlate requests across service boundaries
6. **Health Checks** - Kubernetes liveness and readiness probes
7. **Testing Infrastructure** - Base test cases for sagas, listeners, and circuit breakers

---

## 1. Circuit Breaker Pattern

### Location
- `dev.threadly.common.resilience.CircuitBreakerConfig`

### Purpose
Prevent cascade failures when a downstream service is unhealthy or slow.

### Configuration (Defaults)
```java
CircuitBreakerConfig config = CircuitBreakerConfig.getDefaultCircuitBreakerConfig();
// - failureRateThreshold: 50%
// - slowCallRateThreshold: 50%
// - slowCallDurationThreshold: 10s
// - waitDurationInOpenState: 30s
// - permittedNumberOfCallsInHalfOpenState: 5
// - slidingWindowSize: 10 requests
```

### States
- **CLOSED** (normal): Requests pass through
- **OPEN** (failing): Requests fail immediately with fallback
- **HALF_OPEN** (testing): Limited requests allowed to test recovery
- **DISABLED** (testing): For manual testing

### Usage in Feign Client

```java
@Configuration
@RequiredArgsConstructor
public class BillingServiceClientConfig {
  
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RetryRegistry retryRegistry;

  @Bean
  public BillingServiceClient billingServiceClient(Decoder decoder, Encoder encoder) {
    io.github.resilience4j.circuitbreaker.CircuitBreaker cb = 
        circuitBreakerRegistry.circuitBreaker("billingService", 
            CircuitBreakerConfig.getDefaultCircuitBreakerConfig());
    
    io.github.resilience4j.retry.Retry retry = 
        retryRegistry.retry("billingService", CircuitBreakerConfig.getDefaultRetryConfig());

    FeignDecorators decorators = CircuitBreakerConfig.buildFeignDecorators(cb, retry);
    
    return Resilience4jFeign.builder(decorators)
        .decoder(decoder)
        .encoder(encoder)
        .target(BillingServiceClient.class, "http://billing-service:8080");
  }
}
```

### Fallback Handling
When the circuit is OPEN, return error DTOs instead of throwing exceptions:

```java
@FeignClient(name = "billing-service", url = "${billing-service.url}")
public interface BillingServiceClient {
  
  @PostMapping("/check")
  BillingCheckResponse checkBillingStatus(String customerId);
  
  // Default fallback
  default BillingCheckResponse checkBillingStatusFallback(String customerId) {
    return BillingCheckResponse.builder()
        .isAllowed(false)
        .reason("Service temporarily unavailable - billing check skipped")
        .build();
  }
}
```

---

## 2. Saga Orchestration

### Location
- `dev.threadly.common.saga.SagaOrchestrator` - Base class
- `dev.threadly.common.saga.SagaStep` - Step interface
- `dev.threadly.common.saga.ConversationHandoffSaga` - Example implementation

### Purpose
Coordinate multi-step distributed transactions across services. If any step fails, automatically compensate (rollback) all previous steps.

### Example: Conversation Handoff Saga

Flow:
```
1. conversation.handoff event received
   ↓
2. Check billing: Can user converse?
   ↓
3. Transfer conversation: Assign to agent
   ↓
4. Notify users: Inform customer and agent
   ↓
5. Complete: Emit saga.completed event

On failure at step 3:
   ↓
5. Rollback step 2: Unassign agent
4. Rollback step 1: (no state to rollback)
   ↓
6. Emit saga.failed event
```

### Creating a Custom Saga

```java
@Service
public class MyCustomSaga extends SagaOrchestrator {
  
  @Autowired private EventPublisher eventPublisher;
  @Autowired private ObjectMapper objectMapper;

  public MyCustomSaga(EventPublisher eventPublisher, ObjectMapper objectMapper) {
    super(objectMapper);
    this.eventPublisher = eventPublisher;

    // Define steps in order (each executes and compensates)
    addStep(new Step1());
    addStep(new Step2());
    addStep(new Step3());
  }

  // Implement each step as inner class
  private class Step1 implements SagaStep {
    @Override
    public void execute(String sagaId, int stepIndex) throws Exception {
      // Call external service, update database
      log.info("Step 1 executing: sagaId={}", sagaId);
    }

    @Override
    public void compensate(String sagaId, int stepIndex) throws Exception {
      // Undo step 1
      log.info("Step 1 compensating: sagaId={}", sagaId);
    }
  }

  // Listen for trigger event
  @KafkaListener(topics = "my-events", groupId = "my-saga")
  public void onTrigger(@Payload Map<String, Object> event) {
    try {
      executeAll(); // Execute all steps in order
    } catch (SagaExecutionException e) {
      // Automatic rollback already happened
      publishSagaFailed(e);
    }
  }
}
```

### Idempotency

Each step is keyed by `(sagaId, stepNumber)`. Processing the same event twice will yield the same result:

```java
public void execute(String sagaId, int stepIndex) throws Exception {
  String key = sagaId + "#" + stepIndex;
  
  // Check if already executed
  if (stepRepository.existsByKey(key)) {
    return; // Skip, already done
  }

  // Do the work
  doWork();

  // Mark as executed
  stepRepository.save(new StepRecord(key, "step.name", "SUCCESS"));
}
```

### Event Publishing in Sagas

```java
// Publish saga.started
eventPublisher.publishEvent("saga-events",
  OutboxEvent.builder()
    .eventType("saga.started")
    .aggregateId(workspaceId)
    .payload(toJsonNode(Map.of(
      "sagaId", sagaId,
      "sagaType", "conversation.handoff"
    )))
    .build()
);

// Publish saga.failed on error
eventPublisher.publishEvent("saga-events",
  OutboxEvent.builder()
    .eventType("saga.failed")
    .aggregateId(workspaceId)
    .payload(toJsonNode(Map.of(
      "sagaId", sagaId,
      "completedSteps", currentStep,
      "errorMessage", e.getMessage()
    )))
    .build()
);
```

---

## 3. Kafka Event Publishing (Outbox Pattern)

### Location
- `dev.threadly.common.kafka.EventPublisher` - Facade
- `dev.threadly.common.kafka.OutboxService` - Implementation
- `dev.threadly.common.kafka.OutboxEvent` - Data model
- `dev.threadly.common.kafka.OutboxRepository` - Database access

### Purpose
Guarantee delivery of domain events via database. Events are saved to an `outbox` table within the same transaction as business logic, then asynchronously published to Kafka.

### Flow
```
1. Business logic runs in @Transactional context
2. Save domain entity (e.g., Conversation)
3. eventPublisher.publishEvent() → saved to outbox table in same transaction
4. Transaction commits (atomicity guaranteed)
5. Outbox poller (every 5s) queries unpublished events
6. Publish to Kafka
7. Mark as published in database
```

### Usage

```java
@Service
public class ConversationService {
  
  @Autowired private EventPublisher eventPublisher;
  @Autowired private ConversationRepository conversationRepository;
  @Autowired private ObjectMapper objectMapper;

  @Transactional
  public void completeConversation(String conversationId) {
    // Step 1: Update domain entity
    Conversation conv = conversationRepository.findById(conversationId).orElseThrow();
    conv.setStatus("COMPLETED");
    conv.setCompletedAt(LocalDateTime.now());
    conversationRepository.save(conv);

    // Step 2: Publish event (same transaction)
    eventPublisher.publishEvent("conversation-events",
      OutboxEvent.builder()
        .eventType("conversation.completed")
        .aggregateId(UUID.fromString(conv.getId()))
        .payload(objectMapper.valueToTree(Map.of(
          "conversationId", conv.getId(),
          "customerId", conv.getCustomerId(),
          "duration", Duration.between(conv.getStartedAt(), conv.getCompletedAt()).getSeconds(),
          "completedAt", conv.getCompletedAt()
        )))
        .build()
    );
  }
}
```

### Configuration (application.yml)
```yaml
threadly:
  outbox:
    batch-size: 100         # Events per poll cycle
    polling-interval: 5000  # Poll every 5 seconds
```

### Database Schema
```sql
CREATE TABLE outbox_events (
  id UUID PRIMARY KEY,
  event_type VARCHAR(255) NOT NULL,
  aggregate_id UUID NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  published_at TIMESTAMP NULL,
  version INT DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published_at) 
  WHERE published_at IS NULL;
```

---

## 4. Kafka Event Consumption

### Location
- `dev.threadly.common.kafka.AbstractEventListener` - Base class for all listeners

### Purpose
Reliable event processing with:
- Exponential backoff retry (100ms, 200ms, 400ms)
- Dead-letter-queue routing on persistent failures
- Manual offset commit for idempotency
- Trace ID propagation for distributed tracing

### Creating a Custom Listener

```java
@Service
@Slf4j
public class ConversationEventListener extends AbstractEventListener {

  @Autowired private ConversationService conversationService;
  @Autowired private ObjectMapper objectMapper;

  public ConversationEventListener(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @KafkaListener(
      topics = "conversation-events",
      groupId = "conversation-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onConversationEvent(
      @Payload Map<String, Object> event,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      @Header(value = "traceparent", required = false) String traceParent,
      Acknowledgment ack) {

    String eventType = (String) event.get("eventType");
    try {
      // Extract trace ID from header for distributed tracing
      String traceId = extractTraceId(traceParent);
      log.info("Processing event: type={}, traceId={}", eventType, traceId);

      // Handle the event
      handleEvent(event, eventType);

      // Acknowledge (commit offset) only on success
      ack.acknowledge();
      
    } catch (Exception e) {
      // Trigger retry + DLQ routing
      handleError(e, event, eventType, partition, offset);
    }
  }

  @Override
  protected void handleEvent(Map<String, Object> event, String eventType) throws Exception {
    switch (eventType) {
      case "conversation.started" -> {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        String conversationId = (String) payload.get("conversationId");
        conversationService.onStarted(conversationId);
      }
      case "conversation.completed" -> {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        String conversationId = (String) payload.get("conversationId");
        conversationService.onCompleted(conversationId);
      }
    }
  }
}
```

### Retry Policy
- Max attempts: 3
- Backoff: 100ms → 200ms → 400ms
- On persistent failure: routed to dead-letter-queue

### Idempotency
```java
@Transactional
public void onStarted(String conversationId) {
  // Check if event already processed
  Optional<Conversation> existing = conversationRepository.findById(conversationId);
  if (existing.isPresent() && existing.get().getStatus().equals("STARTED")) {
    return; // Already processed, skip
  }

  // Process event
  Conversation conv = new Conversation();
  conv.setId(conversationId);
  conv.setStatus("STARTED");
  conversationRepository.save(conv);
}
```

---

## 5. Distributed Tracing

### Location
- `dev.threadly.common.tracing.TraceIdPropagator`

### Purpose
Correlate requests across service boundaries using W3C Trace Context standard.

### Trace Flow
```
Client → (Nginx with traceparent header)
  ↓
Conversation Service
  ├─ Receives traceparent in header
  ├─ Stores in RequestContext
  ├─ Calls Billing Service (injects header)
  └─ Publishes to Kafka (injects header)
     ↓
Kafka Consumer
  ├─ Receives traceparent in Kafka message
  ├─ Sets in RequestContext
  └─ Calls downstream services
     ↓
  All requests in trace have same traceId
```

### Header Format (W3C Trace Context)
```
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
             ├─ version: 00 (W3C 1.0)
             ├─ traceId: 32-char hex
             ├─ spanId: 16-char hex
             └─ sampled: 01 (sampled) / 00 (not sampled)
```

### Usage

```java
@Configuration
public class TraceIdInterceptor implements HandlerInterceptor {

  @Autowired private TraceIdPropagator propagator;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String traceparent = request.getHeader("traceparent");
    propagator.setTraceIdFromHeader(traceparent);
    return true;
  }
}

// In Feign client
@FeignClient("billing-service")
public interface BillingServiceClient {
  
  @PostMapping("/check")
  @Headers("traceparent: {traceparent}")
  BillingCheckResponse checkBilling(@Param("traceparent") String traceparent, String customerId);
}

// In service
@Service
public class ConversationService {
  
  @Autowired private BillingServiceClient billingClient;
  @Autowired private TraceIdPropagator propagator;

  public void transferConversation(String conversationId, String agentId) {
    String traceparent = propagator.getTraceparent();
    BillingCheckResponse billing = billingClient.checkBilling(traceparent, customerId);
  }
}

// In Kafka producer
eventPublisher.publishEvent("conversation-events",
  OutboxEvent.builder()
    .eventType("conversation.completed")
    .aggregateId(UUID.fromString(conversationId))
    .traceId(propagator.getTraceId()) // Inject trace ID
    .payload(...)
    .build()
);
```

---

## 6. Health Checks (Kubernetes)

### Location
- `dev.threadly.common.health.HealthController`

### Endpoints
```
GET /health      → Liveness probe (is service running?)
GET /ready       → Readiness probe (is service ready for traffic?)
GET /metrics     → Prometheus metrics on :9090/actuator/prometheus
```

### Kubernetes Configuration
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: conversation-service
spec:
  template:
    spec:
      containers:
      - name: conversation-service
        image: conversation-service:1.0
        ports:
        - containerPort: 8080
        - containerPort: 9090  # Metrics

        # Liveness: restart pod if service hangs
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        # Readiness: remove from load balancer if not ready
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 2
```

### Health Checks Performed
- `/health` → Service process is alive
- `/ready` → Database connectivity, Kafka connectivity, external services reachable

---

## 7. Testing Infrastructure

### Test Base Classes

#### SagaTestCase
```java
@SpringBootTest
public class ConversationHandoffSagaTest extends SagaTestCase {

  @Autowired private ConversationHandoffSaga saga;
  @Autowired private ConversationRepository conversationRepository;

  @Test
  public void testSagaCompletesSuccessfully() throws Exception {
    // Publish event
    publishEvent("conversation-events", Map.of(
      "eventType", "conversation.handoff",
      "conversationId", "conv-123",
      "agentId", "agent-456"
    ));

    // Wait for saga to complete
    waitForSagaCompletion("saga-id", 5000);

    // Verify state
    Conversation conv = conversationRepository.findById("conv-123").orElseThrow();
    assertEquals("ASSIGNED", conv.getStatus());
  }
}
```

#### EventListenerTestCase
```java
@SpringBootTest
public class ConversationEventListenerTest extends EventListenerTestCase {

  @Autowired private ConversationEventListener listener;
  @Autowired private ConversationRepository conversationRepository;

  @Test
  public void testEventProcessingIsIdempotent() throws Exception {
    Map<String, Object> event = Map.of(
      "eventType", "conversation.started",
      "conversationId", "conv-123"
    );

    publishEvent("conversation-events", event);
    publishEvent("conversation-events", event); // Same event twice

    waitForEventProcessing(1000);

    // State should have changed only once
    Conversation conv = conversationRepository.findById("conv-123").orElseThrow();
    assertEquals(1, conv.getVersionNumber());
  }
}
```

#### CircuitBreakerTestCase
```java
@SpringBootTest
public class BillingServiceClientTest extends CircuitBreakerTestCase {

  @Autowired private BillingServiceClient client;
  @Autowired private CircuitBreakerRegistry registry;

  @Test
  public void testCircuitBreakerOpensAfterThreshold() {
    CircuitBreaker breaker = getCircuitBreaker("billingService");

    // Simulate 5 failures
    for (int i = 0; i < 5; i++) {
      simulateFailure();
      try { client.checkBilling("cust-123"); } catch (Exception ignored) {}
    }

    // Verify OPEN state
    assertCircuitBreakerState(breaker, CircuitBreaker.State.OPEN);

    // Next request fails immediately
    BillingResponse result = client.checkBilling("cust-123");
    assertFalse(result.isAllowed());
  }
}
```

---

## Integration Points

### 1. Feign Clients with Circuit Breaker
See section 1: Circuit Breaker Pattern

### 2. Service Events
- Use `EventPublisher.publishEvent()` after business logic in `@Transactional` method
- Events go to outbox table, then to Kafka

### 3. Event Listeners
- Extend `AbstractEventListener`
- Add `@KafkaListener` annotation
- Implement `handleEvent()` method
- Framework handles retries, offsets, trace propagation

### 4. Saga Orchestration
- Extend `SagaOrchestrator`
- Implement steps via `SagaStep` interface
- Listen for trigger event on Kafka
- Call `executeAll()` to start saga
- Framework handles compensation on failure

### 5. Distributed Tracing
- Inject `TraceIdPropagator` in HTTP interceptor
- Call `setTraceIdFromHeader()` on incoming request
- Framework automatically injects into Feign calls and Kafka messages

---

## Example: Complete Conversation Flow

```
User initiates conversation handoff
    ↓
Client sends: POST /conversations/{id}/handoff with agent ID
    ↓
Nginx adds traceparent header
    ↓
ConversationService receives request
    ├─ TraceIdPropagator.setTraceIdFromHeader() → stores in RequestContext
    ├─ Publish "conversation.handoff" event via EventPublisher
    │   └─ Saved to outbox table in same transaction
    └─ Return 202 Accepted
    ↓
OutboxService polls (every 5s)
    ├─ Finds unpublished "conversation.handoff" event
    └─ Publishes to Kafka topic "conversation-events"
    ↓
ConversationHandoffSaga listens on Kafka
    ├─ Step 1: Check billing (call BillingServiceClient)
    │   └─ Circuit breaker + retry for resilience
    ├─ Step 2: Transfer conversation (update database)
    ├─ Step 3: Notify users (call NotificationService)
    └─ Publish "saga.completed" event
    ↓
ConversationEventListener on "conversation-events" topic
    ├─ Receives "saga.completed" event
    ├─ Updates conversation status in database
    └─ Acknowledges Kafka offset
    ↓
Response visible to user
```

All requests in this flow share the same traceId, visible in logs, metrics, and OpenTelemetry traces.

---

## Configuration Checklist

- [ ] Add threadly-common-spring dependency to pom.xml
- [ ] Configure Kafka bootstrap servers in application.yml
- [ ] Configure outbox polling interval (default: 5000ms)
- [ ] Create OutboxEvent table in database
- [ ] Add TraceIdInterceptor to Spring WebMvcConfigurer
- [ ] Configure health check endpoints for Kubernetes
- [ ] Add @EnableKafka to main application class
- [ ] Configure @KafkaListener group IDs for each listener
- [ ] Set up distributed tracing exporters (OpenTelemetry)
- [ ] Add Prometheus metrics endpoint for monitoring

---

## Key Files Created

1. **Resilience**
   - `/dev/threadly/common/resilience/CircuitBreakerConfig.java`

2. **Saga Orchestration**
   - `/dev/threadly/common/saga/SagaOrchestrator.java`
   - `/dev/threadly/common/saga/SagaStep.java`
   - `/dev/threadly/common/saga/ConversationHandoffSaga.java`

3. **Kafka Event Publishing**
   - `/dev/threadly/common/kafka/EventPublisher.java`

4. **Kafka Event Consumption**
   - `/dev/threadly/common/kafka/AbstractEventListener.java`

5. **Distributed Tracing**
   - `/dev/threadly/common/tracing/TraceIdPropagator.java`

6. **Health Checks**
   - `/dev/threadly/common/health/HealthController.java`

7. **Testing**
   - `/dev/threadly/common/test/SagaTestCase.java`
   - `/dev/threadly/common/test/EventListenerTestCase.java`
   - `/dev/threadly/common/test/CircuitBreakerTestCase.java`

---

## References

- Resilience4j: https://resilience4j.readme.io/
- Kafka Best Practices: https://kafka.apache.org/documentation/
- Outbox Pattern: https://microservices.io/patterns/data/transactional-outbox.html
- Saga Pattern: https://microservices.io/patterns/data/saga.html
- W3C Trace Context: https://www.w3.org/TR/trace-context/
- OpenTelemetry: https://opentelemetry.io/
