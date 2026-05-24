# 🎓 COMPREHENSIVE LEARNING GUIDE: BACKEND, CLOUD & DEVOPS
## From Threadly Microservices Project

**Created:** May 25, 2026
**Level:** Intermediate to Advanced
**Duration:** Self-paced learning journey

---

## 📚 TABLE OF CONTENTS

1. [Backend Architecture Fundamentals](#1-backend-architecture-fundamentals)
2. [Spring Boot & Spring Framework](#2-spring-boot--spring-framework)
3. [Microservices Architecture](#3-microservices-architecture)
4. [Database Design & JPA](#4-database-design--jpa)
5. [Event-Driven Architecture](#5-event-driven-architecture)
6. [Security & Authentication](#6-security--authentication)
7. [Cloud Architecture](#7-cloud-architecture)
8. [DevOps & Deployment](#8-devops--deployment)
9. [Monitoring & Observability](#9-monitoring--observability)
10. [Design Patterns](#10-design-patterns-used)
11. [Best Practices & Anti-Patterns](#11-best-practices--anti-patterns)
12. [Real-World Production Considerations](#12-real-world-production-considerations)

---

# 1. BACKEND ARCHITECTURE FUNDAMENTALS

## 1.1 What is Backend Architecture?

Backend is the server-side logic that processes requests, manages data, and returns responses.

```
User/Client
    ↓ (HTTP Request)
Load Balancer
    ↓ (Route)
API Gateway
    ↓ (Authenticate)
Microservice
    ↓ (Process)
Database
    ↓ (Store/Retrieve)
Service
    ↓ (HTTP Response)
Client
```

## 1.2 Key Backend Responsibilities

### Request Handling
```java
// Example from conversation-service
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable UUID id) {
        // 1. Receive request
        // 2. Validate input
        // 3. Fetch from database
        // 4. Return response
        return ResponseEntity.ok(conversationService.findById(id));
    }
}
```

### Business Logic Processing
```java
// Service layer processes business logic
@Service
public class ConversationService {

    public void closeConversation(UUID conversationId) {
        // Business logic:
        // 1. Find conversation
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException());

        // 2. Validate state
        if (conversation.getStatus() != ConversationStatus.OPEN) {
            throw new InvalidStateException("Can only close OPEN conversations");
        }

        // 3. Update state
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());

        // 4. Persist to database
        conversationRepository.save(conversation);

        // 5. Publish event for other services
        eventPublisher.publishEvent(new ConversationClosedEvent(conversationId));
    }
}
```

### Data Persistence
```java
// Repository handles database operations
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByOrgIdAndStatusAndCreatedAtBetween(
        UUID orgId,
        ConversationStatus status,
        LocalDateTime start,
        LocalDateTime end
    );
}
```

## 1.3 Layered Architecture Pattern

```
┌─────────────────────────────────────┐
│    Presentation Layer               │
│  (REST Controllers, DTOs)           │
│  Responsibility: Handle HTTP        │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Service Layer                    │
│  (Business Logic)                   │
│  Responsibility: Process requests   │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Repository Layer                 │
│  (Data Access)                      │
│  Responsibility: CRUD operations    │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Database Layer                   │
│  (PostgreSQL)                       │
│  Responsibility: Store/Retrieve     │
└─────────────────────────────────────┘
```

**In Threadly Project:**

```
Controller (ConversationController)
    ↓ @Autowired
Service (ConversationService)
    ↓ @Autowired
Repository (ConversationRepository)
    ↓ Spring Data JPA
Database (PostgreSQL)
```

---

# 2. SPRING BOOT & SPRING FRAMEWORK

## 2.1 What is Spring Framework?

Spring is a dependency injection and inversion of control container that manages object lifecycles.

### Dependency Injection (DI)

**Without Spring (Manual):**
```java
public class ConversationController {
    private ConversationService service;
    private ConversationRepository repository;

    public ConversationController() {
        // Manually create dependencies
        this.repository = new ConversationRepository();
        this.service = new ConversationService(repository);
    }
}
```

**With Spring (Automatic):**
```java
@RestController
public class ConversationController {
    private final ConversationService service;

    // Spring automatically injects dependencies
    @Autowired
    public ConversationController(ConversationService service) {
        this.service = service;
    }
}
```

### Benefits
- ✅ Loose coupling between components
- ✅ Easy to test (mock dependencies)
- ✅ Configuration management
- ✅ Lifecycle management

## 2.2 Spring Boot Auto-Configuration

**What it does:**
Spring Boot automatically configures your application based on jar dependencies.

### In Threadly Project:

**application.yml configuration:**
```yaml
spring:
  application:
    name: conversation-service

  # Database Configuration (Auto-configured)
  datasource:
    url: jdbc:postgresql://localhost:5432/threadly_conversations
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  # JPA Configuration (Auto-configured)
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQL13Dialect

  # Kafka Configuration (Auto-configured)
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: conversation-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer

  # Service Discovery (Auto-configured)
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      discovery:
        service-name: conversation-service
        health-check-path: /actuator/health
```

### What Spring Boot Auto-Configures:
- ✅ Database connection pooling (HikariCP)
- ✅ JPA/Hibernate configuration
- ✅ Kafka producers/consumers
- ✅ Web server (Tomcat)
- ✅ Logging (Logback)
- ✅ Metrics (Micrometer)
- ✅ Actuator endpoints

## 2.3 Spring Annotations

### Common Annotations in Threadly:

```java
// Component Registration
@SpringBootApplication       // Main application entry point
@Configuration              // Bean definitions
@Service                    // Business logic component
@Repository                 // Data access component
@Controller / @RestController // HTTP handlers
@Component                  // Generic component

// Dependency Injection
@Autowired                  // Automatic dependency injection
@Qualifier("name")          // Specify which bean to inject
@Primary                    // Default bean when multiple available

// Request Mapping
@GetMapping                 // GET requests
@PostMapping                // POST requests
@PutMapping                 // PUT requests
@DeleteMapping              // DELETE requests
@RequestBody                // Parse request body to object
@PathVariable               // Get path parameter
@RequestParam               // Get query parameter

// Data Management
@Transactional              // Database transaction
@Entity                     // JPA entity
@Table                      // Database table mapping
@Id                         // Primary key
@Column                     // Column mapping
@OneToMany / @ManyToOne     // Relationships

// Kafka Integration
@KafkaListener              // Listen to Kafka topic
@KafkaListeners             // Multiple listeners
@Payload                    // Kafka message content

// Configuration Properties
@Value                      // Inject property value
@ConfigurationProperties    // Bind properties to class
@EnableConfigurationProperties

// Scheduling
@Scheduled                  // Periodic task
@EnableScheduling           // Enable scheduling

// Error Handling
@ExceptionHandler           // Handle specific exceptions
@RestControllerAdvice       // Global exception handler

// Logging
@Slf4j                      // Lombok SLF4J integration
```

### Real Example from Threadly:

```java
@Service
@Slf4j  // Provides logger
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Constructor injection - Spring injects dependencies
    @Autowired
    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ApplicationEventPublisher eventPublisher) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

    // Business logic with transaction
    @Transactional  // Overrides class-level readOnly=true
    public void closeConversation(UUID conversationId) {
        log.info("Closing conversation: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        // Publish event for other services
        eventPublisher.publishEvent(
            new ConversationClosedEvent(conversationId, conversation.getOrgId())
        );

        log.info("Conversation closed successfully");
    }

    // Read-only operation
    public Page<ConversationDto> listConversations(UUID orgId, Pageable pageable) {
        return conversationRepository
            .findByOrgId(orgId, pageable)
            .map(ConversationMapper::toDto);
    }
}
```

## 2.4 Spring Boot Embedded Server

Spring Boot includes embedded Tomcat server - no need to deploy WAR file.

```bash
# Traditional approach (Pre-Spring Boot)
# 1. Build WAR file
# 2. Deploy to external Tomcat server
# 3. Manage Tomcat lifecycle

# Spring Boot approach
java -jar conversation-service-1.0.jar
# Server starts automatically on port 8080
# No external server needed
```

---

# 3. MICROSERVICES ARCHITECTURE

## 3.1 What are Microservices?

Breaking monolith into small, independent services that communicate via APIs/events.

### Monolith vs Microservices

**Monolith:**
```
┌────────────────────────────────┐
│    Single Application           │
│  ┌──────────────────────────┐   │
│  │ User Management          │   │
│  ├──────────────────────────┤   │
│  │ Conversation Logic       │   │
│  ├──────────────────────────┤   │
│  │ Flow Execution           │   │
│  ├──────────────────────────┤   │
│  │ Analytics                │   │
│  └──────────────────────────┘   │
│         Single Database         │
└────────────────────────────────┘

Challenges:
❌ Cannot scale individual features
❌ One bug crashes everything
❌ Difficult to deploy changes
❌ Technology lock-in
```

**Microservices:**
```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ identity-service │  │ workspace-service │  │ runtime-service  │
│  (Auth, JWT)     │  │  (Bot Mgmt)      │  │  (Flow Engine)   │
│  PostgreSQL      │  │  PostgreSQL      │  │  PostgreSQL      │
└──────────────────┘  └──────────────────┘  └──────────────────┘
        ↓                    ↓                       ↓
        └────────────────────┼───────────────────────┘
                       Event Bus (Kafka)

Benefits:
✅ Independent scaling
✅ Fault isolation
✅ Technology flexibility
✅ Parallel deployment
✅ Team autonomy
```

## 3.2 Threadly's 9 Microservices

```
┌─────────────────────────────────────────────────────────┐
│                   API Gateway                            │
│        (Request routing, authentication)                │
└─────────────────────────────────────────────────────────┘
        ↓        ↓        ↓        ↓        ↓        ↓
   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
   │Identity │ │Workspace│ │  Flow   │ │ Runtime │
   │Service  │ │Service  │ │Service  │ │Service  │
   └─────────┘ └─────────┘ └─────────┘ └─────────┘
        ↓        ↓        ↓        ↓
   ┌─────────┐ ┌─────────┐ ┌─────────┐
   │Conver-  │ │Knowledge│ │Analytics│
   │sation   │ │Service  │ │Service  │
   └─────────┘ └─────────┘ └─────────┘
        ↓        ↓        ↓
   ┌─────────────────────────────┐
   │   Integration Service       │
   │  (OAuth, Slack, WhatsApp)   │
   └─────────────────────────────┘
        ↓
   ┌─────────────────────────────┐
   │   Billing Service           │
   │  (Plans, Subscriptions)     │
   └─────────────────────────────┘

Event Bus:
┌─────────────────────────────────────────────────────────┐
│             Apache Kafka                                │
│  Topics: conversation-events, runtime-events, etc.      │
└─────────────────────────────────────────────────────────┘
```

## 3.3 Service Communication Patterns

### Synchronous (Request-Response)

Service A waits for response from Service B.

```java
// conversation-service calling workspace-service
@Service
public class ConversationService {

    @Autowired
    private RestTemplate restTemplate;

    public void updateBotLastActive(UUID botId) {
        // Synchronous call - wait for response
        restTemplate.postForObject(
            "http://workspace-service/api/v1/bots/{botId}/last-active",
            null,
            Void.class,
            botId
        );
    }
}
```

**When to use:**
- ✅ Immediate response needed
- ✅ Critical operations
- ✅ User-facing transactions

**Challenges:**
- ❌ Slow if called service is down
- ❌ Tight coupling
- ❌ Cascading failures

### Asynchronous (Event-Driven)

Service A publishes event, Service B listens independently.

```java
// runtime-service publishes event
@Service
public class RuntimeExecutor {

    @Autowired
    private KafkaTemplate<String, NodeExecutedEvent> kafkaTemplate;

    public void executeNode(ExecutionContext context) {
        // Execute node...
        ExecutionResult result = executor.execute(context);

        // Publish event (don't wait for listeners)
        kafkaTemplate.send("runtime-events",
            new NodeExecutedEvent(
                context.getNodeId(),
                result.getStatus(),
                result.getDuration()
            )
        );
    }
}

// analytics-service listens to events
@Service
public class AnalyticsEventListener {

    @KafkaListener(topics = "runtime-events", groupId = "analytics-service")
    public void onNodeExecuted(@Payload NodeExecutedEvent event) {
        // Process asynchronously
        analyticsService.recordMetric("node_execution_time", event.getDuration());
    }
}
```

**When to use:**
- ✅ Non-critical notifications
- ✅ Cross-service updates
- ✅ Eventual consistency acceptable
- ✅ High throughput needed

**Benefits:**
- ✅ Loose coupling
- ✅ Resilient (works even if listener is down)
- ✅ Scalable
- ✅ Async processing

## 3.4 Challenges & Solutions

### Challenge 1: Distributed Transactions

**Problem:** Transaction spans multiple services, one fails.

```
conversation-service: Add message ✅
analytics-service: Record event ❌ (fails)

Result: Inconsistent state!
```

**Solution: Saga Pattern**

```java
// Orchestration approach
@Service
public class ConversationSaga {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Transactional
    public void addMessageWithAnalytics(UUID conversationId, Message message) {
        try {
            // Step 1: Add message
            conversationService.addMessage(conversationId, message);

            // Step 2: Publish event for analytics
            kafkaTemplate.send("analytics-events",
                new MessageAddedEvent(conversationId, message)
            );

        } catch (Exception e) {
            // Step 3: Compensate on failure
            log.error("Message add failed, rolling back", e);
            conversationService.deleteMessage(conversationId, message.getId());
            throw e;
        }
    }
}
```

### Challenge 2: Service Discovery

**Problem:** How does Service A find Service B if it moves?

```
// Without service discovery
RestTemplate.getForObject("http://192.168.1.5:8080/...")
// IP hardcoded - breaks if service moves

// With service discovery
RestTemplate.getForObject("http://workspace-service/...")
// Service name resolved dynamically via Consul
```

**Solution: Spring Cloud Service Discovery**

```yaml
# Consul configuration
spring:
  cloud:
    consul:
      host: consul.example.com
      port: 8500
      discovery:
        service-name: conversation-service
        health-check-path: /actuator/health
        ip-address: 192.168.1.10
        port: 8080
```

All services register themselves, others discover them automatically.

### Challenge 3: Distributed Logging

**Problem:** Request spans 5 services, need to trace it.

```
Request enters: identity-service
  ↓ calls: workspace-service
    ↓ calls: runtime-service
      ↓ calls: knowledge-service
        ↓ calls: analytics-service

How to trace across all services?
```

**Solution: Correlation ID**

```java
// Filter adds correlation ID
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        String correlationId = request.getHeader(CORRELATION_ID);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Store in logging context
        MDC.put(CORRELATION_ID, correlationId);

        // Pass to next service
        response.addHeader(CORRELATION_ID, correlationId);

        filterChain.doFilter(request, response);
        MDC.remove(CORRELATION_ID);
    }
}

// Logging includes correlation ID
@Slf4j
@Service
public class ConversationService {
    public void addMessage(UUID conversationId, Message message) {
        // Logs automatically include X-Correlation-ID
        log.info("Adding message to conversation: {}", conversationId);
    }
}

// Log output:
// 2026-05-25 10:30:45.123 [X-Correlation-ID: abc-123] [conversation-service] Adding message...
```

---

# 4. DATABASE DESIGN & JPA

## 4.1 JPA (Java Persistence API)

JPA is a standard for mapping Java objects to database tables.

### Core Concepts

**Entity:**
```java
@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orgId;  // Multi-tenancy

    @Column(nullable = false)
    private String botName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Relationships
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }
}
```

**Repository:**
```java
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Derived query - Spring generates SQL automatically
    List<Conversation> findByOrgIdAndStatusAndDeletedAtIsNull(
        UUID orgId,
        ConversationStatus status
    );

    // Custom query
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId " +
           "AND c.status = :status " +
           "AND c.deletedAt IS NULL " +
           "AND c.createdAt BETWEEN :start AND :end")
    Page<Conversation> findByOrgAndDateRange(
        @Param("orgId") UUID orgId,
        @Param("status") ConversationStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    // Projection query - fetch only needed columns
    @Query("SELECT new map(c.id as id, c.botName as botName, COUNT(m) as messageCount) " +
           "FROM Conversation c LEFT JOIN c.messages m " +
           "WHERE c.orgId = :orgId GROUP BY c.id")
    List<Map<String, Object>> getConversationSummary(@Param("orgId") UUID orgId);
}
```

## 4.2 Advanced JPA Features

### Relationships

**One-to-Many:**
```java
@Entity
public class Conversation {
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
}

@Entity
public class Message {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
}
```

**Many-to-One:**
```java
@Entity
public class Message {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
}
```

**Fetch Strategy:**
```java
// LAZY: Load only when accessed (better performance)
@ManyToOne(fetch = FetchType.LAZY)
private Conversation conversation;

// EAGER: Load immediately (can cause N+1 problem)
@ManyToOne(fetch = FetchType.EAGER)
private Conversation conversation;
```

### Soft Deletes

Instead of removing records, mark as deleted.

```java
@Entity
public class Conversation {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // Only fetch non-deleted
    List<Conversation> findByOrgIdAndDeletedAtIsNull(UUID orgId);
}
```

**Benefits:**
- ✅ Data recovery possible
- ✅ Audit trail maintained
- ✅ Referential integrity preserved

### Optimistic Locking

Prevent concurrent update conflicts.

```java
@Entity
public class Conversation {
    @Version
    private Long version;  // Auto-incremented on each update
}

// If two requests update simultaneously:
// Thread 1: Update version 1 → 2 ✅
// Thread 2: Try update version 1 → fails ❌
// Prevents lost updates

@Transactional
public void updateConversation(UUID id, ConversationUpdate update) {
    Conversation conv = repository.findById(id).get();
    conv.setBotName(update.getBotName());
    conv.setStatus(update.getStatus());

    try {
        repository.save(conv);
    } catch (OptimisticLockingFailureException e) {
        // Conflict detected - retry or fail gracefully
        log.warn("Concurrent update conflict, retrying...");
    }
}
```

### Custom Queries with @Query

```java
// Native SQL
@Query(value = "SELECT * FROM conversations WHERE org_id = :orgId LIMIT 10",
       nativeQuery = true)
List<Conversation> findRecentConversations(@Param("orgId") UUID orgId);

// JPQL with aggregation
@Query("SELECT new map(c.status as status, COUNT(*) as count) " +
       "FROM Conversation c WHERE c.orgId = :orgId GROUP BY c.status")
List<Map<String, Object>> getStatusDistribution(@Param("orgId") UUID orgId);

// Named parameters and pagination
@Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.status = :status")
Page<Conversation> findByOrgAndStatus(
    @Param("orgId") UUID orgId,
    @Param("status") ConversationStatus status,
    Pageable pageable
);
```

## 4.3 N+1 Problem

**Problem:**
```java
// Get all conversations
List<Conversation> conversations = repository.findAll();

// For each conversation, load messages
for (Conversation conv : conversations) {
    int msgCount = conv.getMessages().size();  // N queries! (1 + N)
}

// 1 query to fetch conversations
// N queries to fetch messages for each conversation
// Total: 1 + N queries = N+1 problem!
```

**Solution 1: Eager Fetch**
```java
@Entity
public class Conversation {
    @OneToMany(fetch = FetchType.EAGER)
    private List<Message> messages;
}
```

**Solution 2: JOIN FETCH**
```java
@Query("SELECT DISTINCT c FROM Conversation c " +
       "LEFT JOIN FETCH c.messages " +
       "WHERE c.orgId = :orgId")
List<Conversation> findWithMessages(@Param("orgId") UUID orgId);
```

**Solution 3: Projection**
```java
@Query("SELECT new map(c.id as id, c.botName as botName, COUNT(m) as msgCount) " +
       "FROM Conversation c LEFT JOIN c.messages m GROUP BY c.id")
List<Map<String, Object>> findConversationCounts();
```

## 4.4 Database Indexing

Indexes speed up queries.

```java
@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_org_status", columnList = "org_id, status"),
    @Index(name = "idx_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_bot_id", columnList = "bot_id")
})
public class Conversation {
    @Column(name = "org_id")
    private UUID orgId;

    @Enumerated(EnumType.STRING)
    private ConversationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**Query Planning:**
```sql
-- Without index (table scan - slow)
EXPLAIN ANALYZE
SELECT * FROM conversations WHERE org_id = '123' AND status = 'OPEN';

-- With index (index scan - fast)
-- Uses idx_org_status for quick lookup
```

**Indexing Strategies:**

```
Single Column Index:
├─ Use for: WHERE clause on single column
└─ org_id indexed

Composite Index:
├─ Use for: Multiple columns in WHERE clause
└─ (org_id, status) indexed for: WHERE org_id = ? AND status = ?

Partial Index:
├─ Use for: Conditional queries
└─ WHERE deleted_at IS NULL (only index non-deleted)

Full-Text Index:
├─ Use for: Text search
└─ Search in conversation content
```

---

# 5. EVENT-DRIVEN ARCHITECTURE

## 5.1 What is Event-Driven Architecture?

Services communicate through events rather than direct calls.

```
Traditional (Request-Response):
Service A ──request──> Service B ──response──> Service A
  (waits)

Event-Driven:
Service A ──publishes event──> Event Bus ──notifies──> Service B, C, D
  (doesn't wait)
```

## 5.2 Kafka in Threadly

Kafka is a distributed event bus.

```
┌──────────────────────────────────────────────────────────────┐
│                     Kafka Cluster                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ conversation-events (Topic)                             │ │
│  │  Partition 0: [Event1] [Event2] [Event3]...            │ │
│  │  Partition 1: [Event4] [Event5] [Event6]...            │ │
│  │  Partition 2: [Event7] [Event8] [Event9]...            │ │
│  │  Replication Factor: 3 (each partition replicated)      │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
        ↑                    ↓
   Producers          Consumers
   (publish)        (subscribe)
```

## 5.3 Publishing Events

```java
// Define event
@Data
@AllArgsConstructor
public class ConversationStartedEvent {
    private UUID conversationId;
    private UUID botId;
    private UUID orgId;
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;
}

// Publish from service
@Service
public class ConversationService {

    @Autowired
    private KafkaTemplate<String, ConversationStartedEvent> kafkaTemplate;

    @Transactional
    public Conversation createConversation(CreateConversationRequest request) {
        // 1. Create conversation
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setOrgId(request.getOrgId());
        conversation.setBotId(request.getBotId());
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setCreatedAt(LocalDateTime.now());

        // 2. Save to database
        Conversation saved = conversationRepository.save(conversation);

        // 3. Publish event (asynchronous)
        ConversationStartedEvent event = new ConversationStartedEvent(
            saved.getId(),
            saved.getBotId(),
            saved.getOrgId(),
            saved.getCreatedAt(),
            Collections.emptyMap()
        );

        kafkaTemplate.send("conversation-events",
            String.valueOf(saved.getOrgId()),  // Partition key
            event
        );

        return saved;
    }
}
```

## 5.4 Listening to Events

```java
@Service
@Slf4j
public class AnalyticsEventListener {

    @Autowired
    private AnalyticsService analyticsService;

    // Listen to conversation-events topic
    @KafkaListener(topics = "conversation-events", groupId = "analytics-service")
    public void onConversationEvent(
            @Payload ConversationStartedEvent event,
            @Headers(name = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Headers(name = KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("Received event: {} from topic: {} offset: {}",
                event.getConversationId(), topic, offset);

            // Process event
            analyticsService.recordEvent(
                "conversation_started",
                event.getOrgId(),
                Map.of(
                    "conversationId", event.getConversationId(),
                    "botId", event.getBotId(),
                    "timestamp", event.getCreatedAt()
                )
            );

        } catch (Exception e) {
            log.error("Error processing event", e);
            // Manual acknowledgment on error
            throw e;
        }
    }
}
```

## 5.5 Event Sourcing

Store events as source of truth.

```java
// Instead of storing final state
@Entity
public class Conversation {
    private ConversationStatus status;
    private LocalDateTime closedAt;
}

// Store events
@Entity
public class ConversationEvent {
    private UUID conversationId;
    private String eventType;  // "OPENED", "MESSAGE_ADDED", "CLOSED"
    private LocalDateTime timestamp;
    private Map<String, Object> data;
}

// Reconstruct state from events
@Service
public class ConversationEventStore {

    public ConversationState getConversationState(UUID conversationId) {
        List<ConversationEvent> events = eventRepository
            .findByConversationIdOrderByTimestampAsc(conversationId);

        ConversationState state = new ConversationState();

        for (ConversationEvent event : events) {
            switch (event.getEventType()) {
                case "OPENED":
                    state.setStatus(ConversationStatus.OPEN);
                    state.setOpenedAt((LocalDateTime) event.getData().get("timestamp"));
                    break;
                case "MESSAGE_ADDED":
                    state.getMessages().add((Message) event.getData().get("message"));
                    break;
                case "CLOSED":
                    state.setStatus(ConversationStatus.CLOSED);
                    state.setClosedAt((LocalDateTime) event.getData().get("timestamp"));
                    break;
            }
        }

        return state;
    }
}
```

**Benefits:**
- ✅ Complete audit trail
- ✅ Can replay events
- ✅ Temporal queries (state at any point in time)
- ✅ Debugging easier

## 5.6 Event Patterns in Threadly

**Pattern 1: Event Notification**
```
Service A publishes → Service B, C, D notified
No response needed
```

**Pattern 2: Event-Carried State Transfer**
```
Event includes all needed data
Listener doesn't need to call Service A for details
```

```java
@Data
public class ConversationClosedEvent {
    private UUID conversationId;
    private UUID orgId;
    private LocalDateTime closedAt;
    private int messageCount;
    private String resolution;
    private Map<String, Object> metrics;  // All data needed
}
```

**Pattern 3: Event Sourcing + CQRS**
```
Write (Commands) ─events→ Event Store
                            ↓
                    Rebuilds Read Models
                            ↓
Read (Queries) ←─────── Read Database
```

---

# 6. SECURITY & AUTHENTICATION

## 6.1 Authentication vs Authorization

```
Authentication: "Who are you?"
├─ Username/password
├─ OAuth tokens
├─ API keys
└─ Certificates

Authorization: "What can you do?"
├─ Role-based (Admin, User, Viewer)
├─ Permission-based
├─ Attribute-based
└─ Org-based (multi-tenancy)
```

## 6.2 JWT (JSON Web Tokens)

```
Structure:
header.payload.signature

header:
{
  "alg": "HS256",
  "typ": "JWT"
}

payload:
{
  "sub": "user-123",
  "org_id": "org-456",
  "roles": ["ADMIN"],
  "exp": 1622505600,
  "iat": 1622419200
}

signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  "secret-key"
)
```

**In Threadly:**
```java
@Service
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Generate token
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .claim("email", userPrincipal.getEmail())
            .claim("orgId", userPrincipal.getOrgId())
            .claim("roles", userPrincipal.getRoles())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Get user info from token
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
```

## 6.3 OAuth 2.0 / OpenID Connect

For third-party authentication (Google, GitHub, etc).

```
┌──────────────┐                    ┌─────────────────┐
│   User       │                    │  OAuth Provider │
│ (Browser)    │                    │  (Google)       │
└──────────────┘                    └─────────────────┘
      ↓                                     ↑
      │ 1. Redirect to Google              │
      ├────────────────────────────────────→
      │                                     │
      │ 2. User logs in with Google        │
      │                                     │
      │ 3. Redirect with auth code ←───────┤
      ←─────────────────────────────────────┤
      │                                     │
      ↓                                     ↓
┌──────────────┐                    ┌─────────────────┐
│   Threadly   │ 4. Exchange code   │  OAuth Provider │
│   Service    │────────────────────→ (Google)       │
│              │ 5. Return token    │                 │
│              │←────────────────────┤                 │
└──────────────┘                    └─────────────────┘
      ↓
      │ 6. Create session / Return JWT
      └────→ User authenticated!
```

## 6.4 Multi-Tenancy & Org Isolation

Prevent Org A from accessing Org B's data.

```java
// Security Context Holder stores current org
@Component
public class TenantContextHolder {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

// Filter sets tenant from JWT token
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                UUID orgId = jwtTokenProvider.getOrgIdFromToken(token);
                TenantContextHolder.setTenantId(orgId);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}

// All database queries automatically filter by org
@Service
public class ConversationService {

    @Transactional(readOnly = true)
    public List<Conversation> listConversations() {
        UUID orgId = TenantContextHolder.getTenantId();  // Get from context
        return conversationRepository.findByOrgId(orgId);
    }
}

// Repository enforces tenant isolation
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByOrgId(UUID orgId);  // Always filtered
}
```

**Testing Org Isolation:**
```java
@Test
public void testOrgIsolation() {
    // Organization A user
    TenantContextHolder.setTenantId(orgA.getId());
    List<Conversation> orgAConversations = service.listConversations();

    // Organization B user
    TenantContextHolder.setTenantId(orgB.getId());
    List<Conversation> orgBConversations = service.listConversations();

    // Should not contain shared conversations
    assertEquals(0, orgAConversations.stream()
        .filter(c -> orgB.getId().equals(c.getOrgId()))
        .count());
}
```

## 6.5 Role-Based Access Control (RBAC)

```java
// Define roles
@Data
@Entity
public class Role {
    @Id
    private Long id;
    private String name;  // "ADMIN", "EDITOR", "VIEWER"
    private Set<Permission> permissions;
}

// Define permissions
@Data
@Entity
public class Permission {
    @Id
    private Long id;
    private String name;  // "READ_CONVERSATION", "WRITE_CONVERSATION"
}

// Enforce at method level
@Service
public class ConversationService {

    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can delete
    public void deleteConversation(UUID id) {
        conversationRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")  // ADMIN or EDITOR can update
    public void updateConversation(UUID id, UpdateRequest request) {
        Conversation conv = conversationRepository.findById(id).get();
        conv.setBotName(request.getBotName());
        conversationRepository.save(conv);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")  // Anyone can view
    public Conversation getConversation(UUID id) {
        return conversationRepository.findById(id).get();
    }
}

// Or at controller level
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<Conversation> getConversation(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getConversation(id));
    }
}
```

## 6.6 API Key Authentication

For programmatic access.

```java
@Entity
@Data
public class ApiKey {
    @Id
    private UUID id;

    private UUID orgId;

    @Column(unique = true)
    private String keyHash;  // Never store plain key

    private String keyPrefix;  // For display: sk_live_abc123...

    @Column(nullable = false)
    private ApiKeyStatus status;  // ACTIVE, REVOKED

    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
}

// Generate key (only shown once)
@Service
public class ApiKeyService {

    public ApiKeyResponse generateKey(UUID orgId, String description) {
        // Generate random key
        String plainKey = "sk_live_" + generateSecureRandomString(32);

        // Hash key (irreversible)
        String keyHash = bcryptEncoder.encode(plainKey);

        // Store hashed version
        ApiKey apiKey = new ApiKey();
        apiKey.setOrgId(orgId);
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(plainKey.substring(0, 16));  // First 16 chars for display
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setCreatedAt(LocalDateTime.now());

        apiKeyRepository.save(apiKey);

        // Return plain key (only this once)
        return new ApiKeyResponse(plainKey, apiKey.getKeyPrefix());
    }

    // Validate key
    public boolean validateKey(String plainKey) {
        // Hash provided key
        String providedHash = bcryptEncoder.encode(plainKey);

        // Compare with stored hash
        ApiKey apiKey = apiKeyRepository.findByKeyHash(providedHash)
            .orElse(null);

        if (apiKey == null || apiKey.getStatus() != ApiKeyStatus.ACTIVE) {
            return false;
        }

        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);

        return true;
    }
}

// Validate in filter
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (apiKeyService.validateKey(token)) {
                // Set authentication in context
                request.setAttribute("apiKey", token);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

# 7. CLOUD ARCHITECTURE

## 7.1 Container Basics

### What is a Container?

A container is a lightweight, standalone package containing:
- Application code
- Runtime (Java 21)
- Dependencies (JARs)
- Configuration
- OS libraries

```
Traditional VM:
┌─────────────────────────────┐
│   Application               │
│   Java Runtime              │
│   Dependencies              │
│   Guest OS                  │
│   Hypervisor                │
└─────────────────────────────┘
Size: ~2-10 GB

Container:
┌─────────────────────────────┐
│   Application               │
│   Java Runtime              │
│   Dependencies              │
│   Minimal OS                │
├─────────────────────────────┤
│   Shared Host OS            │
│   Docker Daemon             │
└─────────────────────────────┘
Size: ~200-500 MB
```

## 7.2 Docker for Threadly Services

**Dockerfile for conversation-service:**
```dockerfile
# Multi-stage build (optimize size)
FROM maven:3.8.1-openjdk-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-slim
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/conversation-service-*.jar conversation-service.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Security: run as non-root
USER app

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "conversation-service.jar"]
```

**Build and run:**
```bash
# Build image
docker build -t threadly/conversation-service:1.0 .

# Run container
docker run -d \
  --name conversation-service \
  -p 8080:8080 \
  -e DB_HOST=postgres.example.com \
  -e DB_USER=app \
  -e DB_PASSWORD=secret \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  threadly/conversation-service:1.0

# View logs
docker logs -f conversation-service

# Check health
curl http://localhost:8080/actuator/health
```

## 7.3 Kubernetes Orchestration

Kubernetes manages containers at scale.

```
┌──────────────────────────────────────────────────────────┐
│                  Kubernetes Cluster                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │    Node 1    │  │    Node 2    │  │    Node 3    │  │
│  │┌─────────────┤  │┌─────────────┤  │┌─────────────┤  │
│  ││Pod-A (v1)  │  ││Pod-A (v1)  │  ││Pod-B (v2)  │  │
│  ││Container   │  ││Container   │  ││Container   │  │
│  │└─────────────┤  │└─────────────┤  │└─────────────┤  │
│  │┌─────────────┤  │┌─────────────┤  │┌─────────────┤  │
│  ││Pod-C (v1)  │  ││Pod-D (v2)  │  ││Pod-E (v2)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         ↓                 ↓                 ↓           │
│    [Kubelet]         [Kubelet]         [Kubelet]       │
│         ↓                 ↓                 ↓           │
│  [Docker Daemon]   [Docker Daemon]  [Docker Daemon]   │
└──────────────────────────────────────────────────────────┘
        ↑
   ┌─────────────────┐
   │ Control Plane   │
   │ (API Server)    │
   │ (Scheduler)     │
   │ (Controller)    │
   └─────────────────┘
```

**Kubernetes Deployment for conversation-service:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: conversation-service
  namespace: threadly
spec:
  replicas: 3  # Run 3 instances
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0

  selector:
    matchLabels:
      app: conversation-service
      version: v1

  template:
    metadata:
      labels:
        app: conversation-service
        version: v1
    spec:
      # Init container (runs before main container)
      initContainers:
      - name: wait-for-db
        image: busybox:1.28
        command: ['sh', '-c', 'until nc -z postgres:5432; do sleep 1; done']

      containers:
      - name: conversation-service
        image: threadly/conversation-service:1.0
        imagePullPolicy: IfNotPresent

        # Port exposed
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        - name: metrics
          containerPort: 9090
          protocol: TCP

        # Resource limits
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"

        # Environment variables
        env:
        - name: JAVA_OPTS
          value: "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: db.host
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password

        # Health checks
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3

        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3

        # Volume mounts
        volumeMounts:
        - name: config-volume
          mountPath: /etc/config
          readOnly: true

      # Pod-level settings
      serviceAccountName: conversation-service
      restartPolicy: Always

      # Node affinity (prefer certain nodes)
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - conversation-service
              topologyKey: kubernetes.io/hostname

      volumes:
      - name: config-volume
        configMap:
          name: app-config
```

**Kubernetes Service (Load Balancer):**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: conversation-service
  namespace: threadly
spec:
  type: ClusterIP  # Internal service
  selector:
    app: conversation-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  sessionAffinity: ClientIP  # Sticky sessions
```

## 7.4 Helm Charts (Package Manager for Kubernetes)

```yaml
# Chart structure
threadly-microservices-helm/
├── Chart.yaml
├── values.yaml  # Default values
├── templates/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   └── secrets.yaml
└── README.md

# values.yaml - Configurable values
replicaCount: 3

image:
  registry: docker.io
  repository: threadly
  tag: "1.0"
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

service:
  type: ClusterIP
  port: 8080

ingress:
  enabled: true
  hosts:
    - host: api.threadly.com
      paths:
        - path: /conversations
          pathType: Prefix

# Deploy with Helm
helm install threadly ./threadly-microservices-helm \
  --namespace threadly \
  --create-namespace \
  --values production-values.yaml
```

## 7.5 Service Mesh (Advanced)

Istio for advanced networking and security.

```
Without Service Mesh:
App 1 ──SSL/TLS─→ App 2
    ┌─ Retry logic
    ├─ Circuit breaker
    └─ Rate limiting
(All handled in application code)

With Service Mesh (Istio):
┌─────────────┐ ┌─────────────┐
│   App 1     │ │   App 2     │
├─────────────┤ ├─────────────┤
│  Sidecar    │ │  Sidecar    │
│  (Envoy)    │ │  (Envoy)    │
└─────────────┘ └─────────────┘
       ↓               ↓
     SSL/TLS, Retry, Circuit Breaker,
     Rate Limiting, Observability
     (All handled by service mesh)
```

```yaml
# Istio VirtualService (traffic routing)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: conversation-service
spec:
  hosts:
  - conversation-service
  http:
  - match:
    - headers:
        user-type:
          exact: "admin"
    route:
    - destination:
        host: conversation-service
        subset: v2  # Route admins to v2
  - route:
    - destination:
        host: conversation-service
        subset: v1  # Route others to v1
      weight: 90
    - destination:
        host: conversation-service
        subset: v2
      weight: 10  # Canary deployment

---
# DestinationRule (load balancing policy)
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: conversation-service
spec:
  host: conversation-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 100
        http2MaxRequests: 100
    outlierDetection:
      consecutive5xxErrors: 5
      interval: 30s
      baseEjectionTime: 30s
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

---

# 8. DEVOPS & DEPLOYMENT

## 8.1 CI/CD Pipeline

Automated build, test, and deployment.

```
Git Push
   ↓
GitHub/GitLab
   ↓
Trigger Pipeline
   ↓
┌─ Build Stage
│  ├─ Checkout code
│  ├─ Compile
│  ├─ Run tests
│  └─ Build Docker image
   ↓
┌─ Test Stage
│  ├─ Unit tests
│  ├─ Integration tests
│  └─ Code quality checks
   ↓
┌─ Staging Stage
│  ├─ Deploy to staging
│  ├─ Run smoke tests
│  └─ Performance tests
   ↓
┌─ Production Stage
│  ├─ Manual approval
│  ├─ Deploy to production
│  └─ Health checks
   ↓
Pipeline Complete
```

**GitHub Actions Example:**
```yaml
name: Build and Deploy Conversation Service

on:
  push:
    branches: [main]
    paths:
      - 'services/conversation-service/**'
  pull_request:
    branches: [main]

env:
  REGISTRY: docker.io
  IMAGE_NAME: threadly/conversation-service

jobs:
  # Build stage
  build:
    runs-on: ubuntu-latest
    outputs:
      image: ${{ steps.build.outputs.image }}
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run tests
        run: |
          cd services/conversation-service
          mvn test -B

      - name: Build JAR
        run: |
          cd services/conversation-service
          mvn package -DskipTests -B

      - name: Log in to Docker Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ secrets.DOCKER_USER }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push Docker image
        id: build
        uses: docker/build-push-action@v4
        with:
          context: ./services/conversation-service
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
          outputs: image=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

  # Test stage
  test:
    needs: build
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: password
          POSTGRES_DB: threadly_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

      kafka:
        image: confluentinc/cp-kafka:7.0.0
        env:
          KAFKA_BROKER_ID: 1
          KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
        ports:
          - 9092:9092

    steps:
      - uses: actions/checkout@v3

      - name: Integration Tests
        run: |
          cd services/conversation-service
          mvn verify -B

      - name: Code Quality Analysis
        run: |
          cd services/conversation-service
          mvn sonar:sonar \
            -Dsonar.host.url=${{ secrets.SONAR_HOST }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }}

  # Deploy stage
  deploy:
    needs: [build, test]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Deploy to staging
        run: |
          # Deploy using Kubernetes
          kubectl set image deployment/conversation-service \
            conversation-service=${{ needs.build.outputs.image }} \
            -n staging

          # Wait for rollout
          kubectl rollout status deployment/conversation-service -n staging

      - name: Run smoke tests
        run: |
          ./scripts/smoke-tests.sh https://staging-api.threadly.com

      - name: Manual approval for production
        uses: actions/github-script@v6
        with:
          script: |
            github.rest.actions.createWorkflowDispatch({
              owner: context.repo.owner,
              repo: context.repo.repo,
              workflow_id: 'deploy-production.yml',
              ref: context.ref,
              inputs: {
                image: '${{ needs.build.outputs.image }}'
              }
            })

      - name: Deploy to production
        if: github.event.inputs.approved == 'true'
        run: |
          kubectl set image deployment/conversation-service \
            conversation-service=${{ github.event.inputs.image }} \
            -n production

          kubectl rollout status deployment/conversation-service -n production

          # Health check
          sleep 30
          curl -f https://api.threadly.com/actuator/health || exit 1
```

## 8.2 Infrastructure as Code (IaC)

Define infrastructure in code, version control it.

**Terraform for AWS:**
```hcl
# Provider
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# VPC and networking
resource "aws_vpc" "threadly" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true

  tags = {
    Name = "threadly-vpc"
  }
}

# EKS Cluster
resource "aws_eks_cluster" "threadly" {
  name            = "threadly-cluster"
  role_arn        = aws_iam_role.eks_cluster_role.arn
  version         = "1.27"

  vpc_config {
    subnet_ids = aws_subnet.threadly[*].id
  }

  enabled_cluster_log_types = [
    "api",
    "audit",
    "authenticator",
    "controllerManager",
    "scheduler"
  ]

  depends_on = [aws_iam_role_policy_attachment.eks_cluster_policy]

  tags = {
    Name = "threadly-eks"
  }
}

# Node Group
resource "aws_eks_node_group" "threadly" {
  cluster_name    = aws_eks_cluster.threadly.name
  node_group_name = "threadly-nodes"
  node_role_arn   = aws_iam_role.eks_node_role.arn
  subnet_ids      = aws_subnet.threadly[*].id

  scaling_config {
    desired_size = 3
    max_size     = 10
    min_size     = 1
  }

  instance_types = ["t3.large"]

  tags = {
    Name = "threadly-nodes"
  }
}

# RDS PostgreSQL
resource "aws_db_instance" "threadly" {
  identifier            = "threadly-db"
  engine                = "postgres"
  engine_version        = "15.3"
  instance_class        = "db.t3.medium"
  allocated_storage     = 100
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = "threadly"
  username = var.db_username
  password = var.db_password

  multi_az               = true
  publicly_accessible    = false
  skip_final_snapshot    = false
  final_snapshot_identifier = "threadly-db-final-snapshot"

  backup_retention_period = 30
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"

  tags = {
    Name = "threadly-db"
  }
}

# ElastiCache for Redis
resource "aws_elasticache_cluster" "threadly_cache" {
  cluster_id           = "threadly-cache"
  engine               = "redis"
  engine_version       = "7.0"
  node_type            = "cache.t3.medium"
  num_cache_nodes      = 3
  parameter_group_name = "default.redis7"
  port                 = 6379
  automatic_failover_enabled = true

  security_group_ids = [aws_security_group.cache.id]

  tags = {
    Name = "threadly-cache"
  }
}

# Output values
output "eks_cluster_endpoint" {
  value = aws_eks_cluster.threadly.endpoint
}

output "rds_endpoint" {
  value = aws_db_instance.threadly.endpoint
}

output "redis_endpoint" {
  value = aws_elasticache_cluster.threadly_cache.cache_nodes[0].address
}
```

Deploy with Terraform:
```bash
# Initialize Terraform
terraform init

# Plan changes
terraform plan -out=tfplan

# Apply changes
terraform apply tfplan

# Destroy infrastructure
terraform destroy
```

---

# 9. MONITORING & OBSERVABILITY

## 9.1 Three Pillars of Observability

### 1. Logs
```java
@Slf4j
@Service
public class ConversationService {

    public void addMessage(UUID conversationId, Message message) {
        // Structured logging
        log.info("Adding message to conversation",
            "conversationId", conversationId,
            "messageId", message.getId(),
            "sender", message.getSender(),
            "length", message.getContent().length()
        );

        try {
            conversationRepository.addMessage(conversationId, message);
            log.info("Message added successfully",
                "conversationId", conversationId
            );
        } catch (Exception e) {
            log.error("Failed to add message",
                "conversationId", conversationId,
                "error", e.getMessage(),
                e  // Stack trace
            );
            throw new ApplicationException("Failed to add message", e);
        }
    }
}
```

**Log Aggregation (ELK Stack):**
```
┌─────────────────────┐
│ Application Logs    │
│ (conversation-svc)  │
└─────────────────────┘
        ↓
┌─────────────────────┐
│ Filebeat            │
│ (Log Shipper)       │
└─────────────────────┘
        ↓
┌─────────────────────┐
│ Elasticsearch       │
│ (Log Storage)       │
└─────────────────────┘
        ↓
┌─────────────────────┐
│ Kibana              │
│ (Visualization)     │
└─────────────────────┘
        ↓
    Analytics & Alerts
```

### 2. Metrics

```java
@Service
@Slf4j
public class ConversationService {

    @Autowired
    private MeterRegistry meterRegistry;

    public void addMessage(UUID conversationId, Message message) {
        long startTime = System.currentTimeMillis();

        try {
            conversationRepository.addMessage(conversationId, message);

            // Record success metric
            meterRegistry.counter("messages.added").increment();
            meterRegistry.timer("message.add.duration")
                .record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            // Record failure metric
            meterRegistry.counter("messages.add.failed").increment();
            throw e;
        }
    }
}
```

**Metrics Collection:**
```
Application
    ↓ (Micrometer)
Prometheus
    ↓ (Scrapes /metrics endpoint)
Grafana
    ↓
Dashboards & Alerts
```

**Prometheus Metrics Example:**
```
# HELP messages_added_total Total messages added
# TYPE messages_added_total counter
messages_added_total{org_id="org-123"} 1543.0

# HELP message_add_duration_seconds Message addition duration
# TYPE message_add_duration_seconds histogram
message_add_duration_seconds_bucket{le="0.1"} 145.0
message_add_duration_seconds_bucket{le="0.5"} 1520.0
message_add_duration_seconds_bucket{le="1.0"} 1543.0
```

### 3. Traces

Distributed tracing for request flow.

```java
@Service
@Slf4j
public class ConversationService {

    @Autowired
    private Tracer tracer;

    public void addMessage(UUID conversationId, Message message) {
        // Start span
        Span span = tracer.startSpan("addMessage");
        span.setAttribute("conversation.id", conversationId.toString());
        span.setAttribute("message.id", message.getId().toString());

        try {
            // Nested span for database operation
            Span dbSpan = tracer.startSpan("database.addMessage");
            conversationRepository.addMessage(conversationId, message);
            dbSpan.end();

            // Nested span for event publishing
            Span eventSpan = tracer.startSpan("event.publish");
            eventPublisher.publish(new MessageAddedEvent(conversationId, message));
            eventSpan.end();

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

**Distributed Trace Example:**
```
GET /api/v1/conversations/123/messages
  │
  ├─ [user-service] Authenticate request (50ms)
  │   └─ [cache] Check token (5ms)
  │
  ├─ [conversation-service] Fetch conversation (80ms)
  │   ├─ [database] Query conversation (30ms)
  │   └─ [cache] Update cache (20ms)
  │
  ├─ [knowledge-service] Get KB context (200ms)
  │   ├─ [vector-db] Vector search (150ms)
  │   └─ [llm] Rerank results (50ms)
  │
  └─ [API Gateway] Return response (10ms)

Total Request Time: 340ms
```

## 9.2 Prometheus + Grafana Setup

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus

volumes:
  prometheus_data:
  grafana_data:
```

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'conversation-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'runtime-service'
    static_configs:
      - targets: ['localhost:8081']
    metrics_path: '/actuator/prometheus'

  - job_name: 'knowledge-service'
    static_configs:
      - targets: ['localhost:8082']
    metrics_path: '/actuator/prometheus'
```

## 9.3 Alerting

```yaml
# alert-rules.yml
groups:
  - name: application
    rules:
      - alert: HighErrorRate
        expr: rate(messages_add_failed[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate in message addition"

      - alert: ServiceDown
        expr: up{job="conversation-service"} == 0
        for: 1m
        annotations:
          summary: "Conversation service is down"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        annotations:
          summary: "High memory usage detected"

      - alert: SlowQueries
        expr: http_requests_duration_seconds_bucket{le="+Inf"} > 1.0
        for: 5m
        annotations:
          summary: "Slow HTTP requests detected"
```

---

# 10. DESIGN PATTERNS USED

## 10.1 Repository Pattern

Abstracts database access.

```java
// Abstraction
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByOrgId(UUID orgId);
}

// Use in service (depends on abstraction, not implementation)
@Service
public class ConversationService {
    @Autowired
    private ConversationRepository repository;

    public List<Conversation> list(UUID orgId) {
        return repository.findByOrgId(orgId);
    }
}

// Benefits:
// ✅ Easy to mock in tests
// ✅ Switch database implementations
// ✅ Single responsibility
```

## 10.2 Dependency Injection

Spring injects dependencies automatically.

```java
// Manual (tightly coupled)
public class ConversationService {
    private ConversationRepository repo = new ConversationRepository();
}

// With DI (loosely coupled)
@Service
public class ConversationService {
    private final ConversationRepository repo;

    @Autowired
    public ConversationService(ConversationRepository repo) {
        this.repo = repo;
    }
}

// Benefits:
// ✅ Testable (mock repository)
// ✅ Flexible (swap implementations)
// ✅ Clean code
```

## 10.3 Decorator Pattern

Wrapping for additional behavior.

```java
// Base implementation
@Service
public class ConversationService {
    public void addMessage(UUID id, Message msg) { ... }
}

// Decorator (adds logging, metrics)
@Service
public class MonitoredConversationService {
    @Autowired
    private ConversationService delegate;

    @Autowired
    private MeterRegistry metrics;

    public void addMessage(UUID id, Message msg) {
        log.info("Adding message");
        long start = System.currentTimeMillis();

        try {
            delegate.addMessage(id, msg);
            metrics.counter("messages.added").increment();
        } catch (Exception e) {
            metrics.counter("messages.failed").increment();
            throw e;
        } finally {
            metrics.timer("message.add.duration")
                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
        }
    }
}

// Benefits:
// ✅ Separation of concerns
// ✅ Reusable decorators
// ✅ Open/Closed principle
```

## 10.4 Factory Pattern

Create objects without specifying exact classes.

```java
// Node execution factory
@Component
public class NodeExecutorFactory {

    private final Map<String, NodeExecutor> executors;

    @Autowired
    public NodeExecutorFactory(List<NodeExecutor> executorList) {
        // Register all node executors
        this.executors = executorList.stream()
            .collect(Collectors.toMap(
                NodeExecutor::getNodeType,
                Function.identity()
            ));
    }

    public NodeExecutor getExecutor(String nodeType) {
        NodeExecutor executor = executors.get(nodeType);
        if (executor == null) {
            throw new UnsupportedNodeTypeException(nodeType);
        }
        return executor;
    }
}

// New node types automatically registered
@Component
public class MessageNodeExecutor extends NodeExecutor {
    @Override
    public String getNodeType() {
        return "MESSAGE_NODE";
    }
}

// Benefits:
// ✅ Add new types without changing factory
// ✅ Extensible
// ✅ Loose coupling
```

## 10.5 Observer Pattern

Event listeners.

```java
// Event
public class ConversationClosedEvent {
    private UUID conversationId;
    private UUID orgId;
    private LocalDateTime closedAt;
}

// Subject publishes event
@Service
public class ConversationService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void closeConversation(UUID id) {
        Conversation conv = repository.findById(id).get();
        conv.setStatus(CLOSED);
        repository.save(conv);

        // Notify all observers
        eventPublisher.publishEvent(
            new ConversationClosedEvent(id, conv.getOrgId(), LocalDateTime.now())
        );
    }
}

// Observers listen for event
@Service
public class AnalyticsListener {
    @EventListener
    public void onConversationClosed(ConversationClosedEvent event) {
        analyticsService.recordEvent("conversation_closed", event.getOrgId());
    }
}

@Service
public class NotificationListener {
    @EventListener
    public void onConversationClosed(ConversationClosedEvent event) {
        notificationService.sendNotification(event.getOrgId(), "Conversation closed");
    }
}

// Benefits:
// ✅ Decoupled services
// ✅ Multiple listeners
// ✅ Asynchronous processing
```

## 10.6 Strategy Pattern

Switch algorithms at runtime.

```java
// Strategy interface
public interface ConversationSearchStrategy {
    List<Conversation> search(SearchCriteria criteria);
}

// Implementations
@Component
public class FullTextSearchStrategy implements ConversationSearchStrategy {
    @Override
    public List<Conversation> search(SearchCriteria criteria) {
        // Full-text search using database
    }
}

@Component
public class ElasticsearchStrategy implements ConversationSearchStrategy {
    @Override
    public List<Conversation> search(SearchCriteria criteria) {
        // Search using Elasticsearch
    }
}

// Use based on criteria
@Service
public class ConversationSearchService {

    @Autowired
    @Qualifier("fullTextSearchStrategy")
    private ConversationSearchStrategy defaultStrategy;

    @Autowired
    @Qualifier("elasticsearchStrategy")
    private ConversationSearchStrategy elasticsearchStrategy;

    public List<Conversation> search(SearchCriteria criteria) {
        if (criteria.isComplex()) {
            return elasticsearchStrategy.search(criteria);
        } else {
            return defaultStrategy.search(criteria);
        }
    }
}

// Benefits:
// ✅ Algorithm encapsulation
// ✅ Runtime selection
// ✅ Easy to add new strategies
```

---

# 11. BEST PRACTICES & ANTI-PATTERNS

## 11.1 SOLID Principles

### S - Single Responsibility Principle

**Bad:**
```java
@Service
public class ConversationService {
    // Too many responsibilities
    public void addMessage() { ... }
    public void sendEmail() { ... }
    public void generateReport() { ... }
    public void updateCache() { ... }
}
```

**Good:**
```java
@Service
public class ConversationService {
    // Only conversation management
    public void addMessage() { ... }
}

@Service
public class EmailService {
    // Only email sending
    public void sendEmail() { ... }
}

@Service
public class ReportService {
    // Only report generation
    public void generateReport() { ... }
}

@Service
public class CacheService {
    // Only cache management
    public void updateCache() { ... }
}
```

### O - Open/Closed Principle

**Bad:**
```java
@Service
public class ExecutorService {
    public void executeNode(Node node) {
        if (node.getType() == NodeType.MESSAGE) {
            // Message logic
        } else if (node.getType() == NodeType.QUESTION) {
            // Question logic
        } else if (node.getType() == NodeType.CONDITION) {
            // Condition logic
        }
        // Adding new node type requires modifying this method
    }
}
```

**Good:**
```java
public interface NodeExecutor {
    void execute(Node node);
}

@Component
public class MessageNodeExecutor implements NodeExecutor { ... }

@Component
public class QuestionNodeExecutor implements NodeExecutor { ... }

@Component
public class ConditionNodeExecutor implements NodeExecutor { ... }

// New node types don't require modifying existing code
@Component
public class CustomNodeExecutor implements NodeExecutor { ... }
```

### L - Liskov Substitution Principle

**Bad:**
```java
// Bird is-a Animal
public class Bird extends Animal {
    @Override
    public void fly() {
        // Some birds can't fly!
    }
}
```

**Good:**
```java
public abstract class Animal { }

public abstract class FlyingBird extends Animal {
    public abstract void fly();
}

public abstract class NonFlyingBird extends Animal {
    // No fly() method
}

public class Penguin extends NonFlyingBird { }
public class Eagle extends FlyingBird { }
```

### I - Interface Segregation Principle

**Bad:**
```java
public interface UserService {
    User getUser(UUID id);
    void updateUser(User user);
    void deleteUser(UUID id);
    void sendEmail(String email);
    void generateReport();
}
```

**Good:**
```java
public interface UserRepository {
    User findById(UUID id);
    void save(User user);
    void delete(UUID id);
}

public interface EmailService {
    void send(String email, String subject, String body);
}

public interface ReportGenerator {
    void generate(UUID userId);
}

// Implement only needed interfaces
@Service
public class UserService implements UserRepository {
    // Only user operations
}
```

### D - Dependency Inversion Principle

**Bad:**
```java
// High-level depends on low-level implementation
@Service
public class ConversationService {
    private PostgresRepository repo = new PostgresRepository();
}
```

**Good:**
```java
// High-level depends on abstraction
@Service
public class ConversationService {
    private ConversationRepository repo;

    @Autowired
    public ConversationService(ConversationRepository repo) {
        this.repo = repo;
    }
}

// Low-level implements abstraction
@Repository
public class PostgresRepository implements ConversationRepository { }
```

## 11.2 Anti-Patterns to Avoid

### Anti-Pattern 1: Service Locator

**Bad:**
```java
@Service
public class ConversationService {
    @Autowired
    private ApplicationContext context;  // Service locator!

    public void doSomething() {
        // Runtime lookup - hard to test
        Repository repo = context.getBean(Repository.class);
    }
}
```

**Good:**
```java
@Service
public class ConversationService {
    private final Repository repo;

    @Autowired
    public ConversationService(Repository repo) {
        this.repo = repo;  // Explicit dependency
    }

    public void doSomething() {
        repo.save(...);
    }
}
```

### Anti-Pattern 2: God Object

**Bad:**
```java
@Entity
public class Conversation {
    // Handles everything
    public void saveToDatabase() { }
    public void sendEmail() { }
    public void generateReport() { }
    public void updateCache() { }
    public void publishEvent() { }
}
```

**Good:**
```java
@Entity
public class Conversation {
    // Only data
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
}

// Services handle operations
@Service
public class ConversationService { }

@Service
public class EmailService { }

@Service
public class ReportService { }
```

### Anti-Pattern 3: LeakyAbstraction

**Bad:**
```java
@Repository
public interface ConversationRepository {
    // Details leaking to caller
    List<Conversation> findUsingJdbcTemplate(String sql);
    void saveWithHibernate(Conversation conv);
}
```

**Good:**
```java
@Repository
public interface ConversationRepository {
    // Clean abstraction
    List<Conversation> findByOrgId(UUID orgId);
    void save(Conversation conversation);
}
```

### Anti-Pattern 4: Premature Optimization

**Bad:**
```java
// Over-engineered caching before knowing bottleneck
@Service
public class ConversationService {
    @Cached  // Multiple cache annotations
    @RedisCacheable
    @LocalCacheable
    @DistributedCacheable
    public Conversation findById(UUID id) {
        return repository.findById(id);
    }
}
```

**Good:**
```java
// First: make it work
@Service
public class ConversationService {
    public Conversation findById(UUID id) {
        return repository.findById(id);
    }
}

// Then: profile and optimize if needed
// Only then: add caching where it helps
```

---

# 12. REAL-WORLD PRODUCTION CONSIDERATIONS

## 12.1 Deployment Checklist

```
Pre-Deployment:
□ Code review completed
□ Unit tests passing (>80% coverage)
□ Integration tests passing
□ Performance tests showing acceptable latency
□ Security scan passing (SAST)
□ Dependency vulnerabilities checked
□ Database migrations reviewed
□ Breaking API changes documented
□ Rollback plan documented

Deployment:
□ Infrastructure provisioned
□ Secrets configured
□ Configuration updated
□ Database migrations run
□ Service deployed
□ Health checks passing
□ Smoke tests passing
□ Monitoring alerts configured
□ Team notified

Post-Deployment:
□ Monitor error rates
□ Monitor latency
□ Check log aggregation
□ Verify metrics
□ Customer feedback
□ Performance baselines established
```

## 12.2 Incident Response

```
Incident Detected
    ↓
Page on-call engineer
    ↓
Assess severity
    ├─ P1 (Critical) - Service down
    ├─ P2 (High) - Degradation
    ├─ P3 (Medium) - Partial impact
    └─ P4 (Low) - Minor issue
    ↓
Implement immediate fix
    ├─ Restart service
    ├─ Scale up
    ├─ Route traffic away
    └─ Rollback if needed
    ↓
Root cause analysis (post-incident)
    ├─ What went wrong?
    ├─ Why didn't we catch it?
    ├─ How do we prevent?
    └─ Document lessons learned
```

## 12.3 Scaling Strategies

**Vertical Scaling:**
```
Before:
┌─────────────────────┐
│  Single Service     │
│  4 CPU, 8 GB RAM    │
│  Max: 1000 req/sec  │
└─────────────────────┘

After:
┌─────────────────────┐
│  Single Service     │
│  16 CPU, 32 GB RAM  │
│  Max: 4000 req/sec  │
└─────────────────────┘

Pros: Simple
Cons: Limited (hardware limits)
```

**Horizontal Scaling:**
```
Before:
┌─────────────────┐
│ Service Instance│
│ 4 CPU, 8 GB     │
└─────────────────┘

After:
┌─────────────────┐
│ Service Instance│
│ 4 CPU, 8 GB     │
├─────────────────┤
│ Service Instance│
│ 4 CPU, 8 GB     │
├─────────────────┤
│ Service Instance│
│ 4 CPU, 8 GB     │
└─────────────────┘
      ↓
 Load Balancer
 (Distributes traffic)

Pros: Unlimited
Cons: Stateless design required, data consistency
```

**Database Scaling:**
```
Read Replicas:
Primary (Write) ──replication──> Replica 1 (Read)
                              ├──> Replica 2 (Read)
                              └──> Replica 3 (Read)

Sharding:
Org A-M ──→ Shard 1
Org N-Z ──→ Shard 2

Caching:
Database ──→ Redis Cache ──→ Application
```

## 12.4 Cost Optimization

```
Cloud Cost Breakdown (Typical):
├─ Compute (K8s nodes): 40%
│  └─ Right-size instances
│  └─ Use reserved instances
│  └─ Spot instances for non-critical
│
├─ Database: 25%
│  └─ Use read replicas efficiently
│  └─ Archive old data
│  └─ Use managed services
│
├─ Storage: 15%
│  └─ Delete old logs
│  └─ Compress archives
│  └─ Use different storage tiers
│
├─ Data Transfer: 12%
│  └─ Use CDN
│  └─ Reduce cross-region traffic
│  └─ Compress responses
│
└─ Monitoring/Logging: 8%
   └─ Sample logs (not all)
   └─ Delete old metrics
   └─ Use log retention policies
```

## 12.5 Disaster Recovery

```
RPO (Recovery Point Objective):
Acceptable data loss
↓
Last backup + 1 hour data

RTO (Recovery Time Objective):
Time to recovery
↓
Less than 1 hour

Backup Strategy:
Production Database
    ↓
Daily Backups (stored in S3)
    ↓
Monthly Archives (stored in Glacier)
    ↓
Disaster Recovery region
    ↓
Can restore in <1 hour
```

## 12.6 Security Best Practices

```
✅ DO:
├─ Use HTTPS/TLS for all communication
├─ Hash passwords with bcrypt
├─ Store secrets in Vault
├─ Validate all inputs
├─ Use parameterized queries (prevent SQL injection)
├─ Implement rate limiting
├─ Enable request logging
├─ Use least privilege principle
├─ Rotate secrets regularly
├─ Keep dependencies updated
└─ Run security scans in CI/CD

❌ DON'T:
├─ Hardcode secrets
├─ Log sensitive data
├─ Trust user input
├─ Use plain text passwords
├─ Expose internal details in errors
├─ Skip authentication on "internal" APIs
├─ Store credit cards
├─ Use default credentials
├─ Skip HTTPS in production
└─ Run as root in containers
```

---

## 🎓 LEARNING JOURNEY SUMMARY

### Foundation (Week 1)
- [x] Backend fundamentals
- [x] Spring Boot & Framework
- [x] Layered architecture
- [x] REST APIs

### Intermediate (Week 2-3)
- [x] Microservices patterns
- [x] Database design (JPA/Hibernate)
- [x] Event-driven architecture
- [x] Authentication & Authorization

### Advanced (Week 4-5)
- [x] Cloud deployment (Docker, K8s)
- [x] DevOps & CI/CD
- [x] Monitoring & Observability
- [x] Design patterns & best practices

### Expert (Week 6+)
- [x] Production readiness
- [x] Scaling strategies
- [x] Disaster recovery
- [x] Security hardening

---

## 📚 KEY TAKEAWAYS

1. **Layered Architecture** separates concerns (Controller → Service → Repository)
2. **Dependency Injection** makes code testable and maintainable
3. **Microservices** provide scalability but require careful orchestration
4. **Event-Driven Architecture** enables loose coupling between services
5. **Docker & Kubernetes** provide infrastructure abstraction
6. **Monitoring & Observability** are critical for production systems
7. **Security** must be designed in, not added later
8. **Design Patterns** solve recurring problems
9. **SOLID Principles** make code maintainable
10. **Production Readiness** requires monitoring, alerting, and runbooks

---

**Document Created:** May 25, 2026
**Total Content:** 12 sections, 200+ code examples, 50+ diagrams
**Suggested Study Time:** 40-60 hours of hands-on learning

Happy learning! 🚀
