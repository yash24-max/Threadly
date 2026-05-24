# Threadly Microservices: Comprehensive Test Strategy

**Last Updated:** 2026-05-24
**Status:** ACTIVE - Implementation in Progress
**Owner:** QA & Testing Tech Lead
**Target Coverage:** >80% critical paths, 70%+ overall

---

## Executive Summary

This document outlines a comprehensive testing strategy for the Threadly microservices architecture spanning 9 distributed services, 2 frontend applications, and 1 SDK/widget.

### Current State Assessment

| Category | Status | Coverage | Confidence |
|----------|--------|----------|-----------|
| **Unit Tests (Java)** | MINIMAL | <5% | LOW |
| **Unit Tests (TypeScript)** | PARTIAL | 15% | MEDIUM |
| **Integration Tests** | STARTED | 8% | LOW |
| **E2E Tests** | PARTIAL | 20% | MEDIUM |
| **Test Infrastructure** | GOOD | N/A | HIGH |
| **CI/CD Pipeline** | ESTABLISHED | N/A | HIGH |

### Gap Analysis

**Critical Gaps:**
1. 8 of 9 Java services lack unit test coverage
2. No repository/DAO layer tests
3. Limited controller/endpoint tests
4. No service-to-service contract tests
5. Missing API gateway integration tests
6. Limited widget unit tests
7. No performance/load tests
8. Insufficient security test coverage

---

## 1. Testing Pyramid & Strategy

### 1.1 Testing Levels (Ideal Target)

```
                    ▲
                   /│\
                  / │ \
                 /  │  \
                / E2E \     5-10% (End-to-End)
               /───────\
              /         \
             /   API &   \   15-25% (Integration)
            / Integration \
           /─────────────\
          /               \
         /  Unit Tests &   \  65-75% (Unit)
        /   Component Tests \
       /─────────────────────\
```

### 1.2 Test Categories by Scope

#### UNIT TESTS (70-75% of total)
- **Java Services:** Controllers, Services, Repositories, Utilities
- **TypeScript/React:** Components, Hooks, Utils, Business Logic
- **Coverage Goal:** >85% on critical services
- **Speed:** < 100ms per test
- **Isolation:** No I/O, no DB, no network calls

#### INTEGRATION TESTS (15-25% of total)
- **Java:** Service + Database, Service + Kafka, Feign clients
- **TypeScript:** API hooks with mocked responses, Component integration
- **Coverage Goal:** All microservice APIs, critical workflows
- **Speed:** < 5s per test
- **Isolation:** Real test containers (PostgreSQL, Kafka, Redis)

#### E2E TESTS (5-10% of total)
- **Playwright:** User workflows (auth, bot creation, flow builder)
- **Coverage Goal:** Happy path + critical failure scenarios
- **Speed:** < 30s per test (with parallel execution)
- **Isolation:** Full stack running (all services)

#### SPECIALIZED TESTS

**Contract/API Tests:**
- Verify service-to-service contract compliance
- OpenAPI/schema validation
- Request/response payload validation

**Performance Tests:**
- Load testing with JMeter or K6
- Database query performance
- API response time baselines
- Concurrent user simulation

**Security Tests:**
- OWASP Top 10 validation
- SQL injection testing
- Auth/JWT token validation
- Rate limiting verification
- Dependency vulnerability scanning

---

## 2. Technology Stack

### Java Services (JUnit5 + Spring Test)

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
  <!-- Includes: JUnit5, Mockito, AssertJ, RestAssured -->
</dependency>

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers</artifactId>
  <version>1.20.3</version>
  <scope>test</scope>
  <!-- PostgreSQL, Kafka, Generic containers -->
</dependency>

<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <scope>test</scope>
  <!-- Fluent API testing -->
</dependency>
```

**Core Testing Libraries:**
- **JUnit5**: Test framework, parameterized tests, display names
- **Mockito**: Mocking dependencies, spy on interactions
- **AssertJ**: Fluent assertions
- **TestContainers**: Ephemeral PostgreSQL, Kafka, Redis
- **REST Assured**: REST API testing fluently
- **Hamcrest**: Matcher library for assertions
- **Spring Boot Test**: @SpringBootTest, @WebMvcTest, @DataJpaTest

### TypeScript/React Frontend

```json
{
  "devDependencies": {
    "vitest": "^2.1.5",           // Unit & component tests
    "@playwright/test": "^1.48.0", // E2E testing
    "@testing-library/react": "^14.0.0"  // Component testing
  }
}
```

**Core Libraries:**
- **Vitest**: Lightning-fast unit test runner
- **Playwright**: E2E testing across browsers
- **React Testing Library**: Component testing (user-centric)
- **MSW (Mock Service Worker)**: HTTP mocking
- **@testing-library/jest-dom**: Custom matchers

### Test Data & Fixtures

```
tests/
├── fixtures/
│   ├── bot-fixtures.ts         // Bot payloads, IDs
│   ├── flow-fixtures.ts        // Flow, node definitions
│   ├── conversation-fixtures.ts // Message, user data
│   └── auth-fixtures.ts        // JWT tokens, users
├── factories/
│   ├── BotFactory.java
│   ├── FlowFactory.java
│   └── UserFactory.java
└── seeds/
    ├── test-data.sql           // Initial DB state
    └── kafka-test-topics.ts    // Topic configs
```

---

## 3. Service-by-Service Coverage Plan

### 3.1 Identity Service (`identity-service`)

**Current State:** Test directory exists, no tests yet

**Coverage Goals:**
- JWT token generation & validation (95%)
- User registration & authentication (90%)
- Org membership & RBAC (85%)
- Session management (80%)

**Unit Tests (15-20 tests):**
```
AuthenticationServiceTest
├── testGenerateJwtToken()
├── testValidateJwtToken()
├── testParseTokenClaims()
├── testTokenExpiration()
└── testRefreshToken()

UserServiceTest
├── testCreateUser()
├── testFindUserById()
├── testUpdateUserProfile()
├── testDeleteUser()
└── testUserValidation()

OrgMembershipServiceTest
├── testAddUserToOrg()
├── testRemoveUserFromOrg()
├── testListOrgMembers()
├── testValidateOrgOwnership()
└── testRoleAssignment()
```

**Integration Tests (8-10 tests):**
```
AuthenticationIntegrationTest
├── testSignupFlow()
├── testLoginFlow()
├── testTokenRefresh()
├── testLogout()
└── testInvalidCredentials()

OrgIntegrationTest
├── testCreateOrgWithUsers()
├── testOrgIsolation()
└── testInvitationFlow()
```

**API Contract Tests:**
```
IdentityServiceContractTest
├── POST /auth/signup (201, 400, 409)
├── POST /auth/login (200, 401, 404)
├── POST /auth/refresh (200, 401)
├── POST /auth/validate (200, 401)
└── GET /users/{userId} (200, 404)
```

---

### 3.2 Workspace Service (`workspace-service`)

**Current State:** No test directory

**Coverage Goals:**
- Bot CRUD operations (90%)
- Bot settings & configuration (85%)
- Workspace hierarchy (80%)
- Bot versioning (80%)

**Unit Tests (20-25 tests):**
```
BotServiceTest
├── testCreateBot()
├── testUpdateBot()
├── testDeleteBot()
├── testBotValidation()
└── testBotNameUniqueness()

BotSettingsServiceTest
├── testUpdateBotLanguage()
├── testUpdateBotTheme()
├── testGetBotSettings()
└── testSettingsValidation()

WorkspaceServiceTest
├── testGetWorkspaceMetadata()
├── testListBots()
├── testSearchBots()
└── testBotPagination()
```

**Integration Tests (10-12 tests):**
```
BotLifecycleIntegrationTest
├── testCreateBotTriggersEvent()
├── testUpdateBotPropagates()
├── testDeleteBotCascades()
└── testBotVisibilityAcrossServices()
```

---

### 3.3 Flow Service (`flow-service`)

**Current State:** Catalog DTO structure exists

**Coverage Goals:**
- Flow CRUD (90%)
- Node catalog & definitions (85%)
- Version management (85%)
- Template system (80%)

**Unit Tests (25-30 tests):**
```
FlowServiceTest
├── testCreateFlow()
├── testUpdateFlowStructure()
├── testPublishFlow()
├── testFlowValidation()
└── testFlowVersioning()

NodeCatalogServiceTest
├── testGetNodeDefinition()
├── testGetAllNodes()
├── testNodeValidation()
└── testNodeInputOutputSchema()

TemplateServiceTest
├── testCreateTemplate()
├── testListTemplates()
├── testApplyTemplate()
└── testTemplateValidation()
```

**Integration Tests (12-15 tests):**
```
FlowExecutionIntegrationTest
├── testFlowPublishedEvent()
├── testNodeExecutionQueue()
├── testFlowVersionControl()
└── testTemplateIntegration()
```

---

### 3.4 Runtime Service (`runtime-service`)

**Current State:** No test directory

**Coverage Goals:**
- Session creation & management (90%)
- Node execution (85%)
- Queue handling (80%)
- Error recovery (80%)

**Unit Tests (20-25 tests):**
```
SessionServiceTest
├── testCreateSession()
├── testGetSession()
├── testUpdateSessionState()
├── testSessionTimeout()
└── testCloseSession()

NodeExecutorServiceTest
├── testExecuteNode()
├── testNodeInputValidation()
├── testNodeOutputMapping()
├── testErrorHandling()
└── testNodeTimeout()

QueueServiceTest
├── testEnqueueExecution()
├── testDequeueTask()
├── testQueuePriority()
└── testQueueRetry()
```

**Integration Tests (10-12 tests):**
```
RuntimeExecutionIntegrationTest
├── testEndToEndNodeExecution()
├── testMultipleNodesSequence()
├── testErrorRecovery()
└── testSessionCleanup()
```

---

### 3.5 Conversation Service (`conversation-service`)

**Current State:** No test directory

**Coverage Goals:**
- Message CRUD (90%)
- Conversation lifecycle (85%)
- Lead management (80%)
- Human handoff (75%)

**Unit Tests (25-30 tests):**
```
ConversationServiceTest
├── testCreateConversation()
├── testAddMessage()
├── testUpdateMessageStatus()
├── testListMessages()
└── testConversationMetadata()

LeadServiceTest
├── testCreateLead()
├── testUpdateLeadInfo()
├── testLeadQualification()
└── testLeadAssignment()

HandoffServiceTest
├── testInitiateHandoff()
├── testTransferConversation()
├── testAssignToAgent()
└── testHandoffCleanup()
```

**Integration Tests (12-15 tests):**
```
ConversationLifecycleIntegrationTest
├── testConversationStartToEnd()
├── testLeadCreationFromConversation()
├── testHandoffWorkflow()
└── testMessageDelivery()
```

---

### 3.6 Knowledge Service (`knowledge-service`)

**Current State:** No test directory

**Coverage Goals:**
- Document ingestion (85%)
- Vector embedding (80%)
- RAG querying (85%)
- Search & retrieval (85%)

**Unit Tests (20-25 tests):**
```
DocumentServiceTest
├── testUploadDocument()
├── testProcessDocument()
├── testParseDocument()
├── testDocumentDeletion()
└── testDocumentMetadata()

EmbeddingServiceTest
├── testGenerateEmbedding()
├── testEmbeddingStorage()
├── testSimilaritySearch()
└── testEmbeddingValidation()

RAGServiceTest
├── testQueryKnowledgeBase()
├── testRetrieveContext()
├── testRankSearchResults()
└── testRAGPipeline()
```

**Integration Tests (10-12 tests):**
```
KnowledgeBaseIntegrationTest
├── testDocumentIngestionToQuery()
├── testVectorDatabaseIntegration()
├── testRAGAccuracy()
└── testDocumentUpdate()
```

---

### 3.7 Analytics Service (`analytics-service`)

**Current State:** No test directory

**Coverage Goals:**
- Event processing (90%)
- Metrics aggregation (85%)
- Dashboard queries (80%)
- Report generation (75%)

**Unit Tests (20-25 tests):**
```
EventServiceTest
├── testPublishEvent()
├── testParseEvent()
├── testEventValidation()
└── testEventDeduplication()

MetricsServiceTest
├── testAggregateMetrics()
├── testCalculateTimeSeries()
├── testGroupByDimensions()
└── testMetricsValidation()

DashboardServiceTest
├── testBuildDashboard()
├── testApplyFilters()
├── testDashboardPagination()
└── testCustomMetrics()
```

**Integration Tests (10-12 tests):**
```
AnalyticsIntegrationTest
├── testEventToMetricsPipeline()
├── testKafkaEventConsumption()
├── testDashboardDataFreshness()
└── testReportGeneration()
```

---

### 3.8 Billing Service (`billing-service`)

**Current State:** No test directory

**Coverage Goals:**
- Plan management (90%)
- Subscription lifecycle (85%)
- Usage metering (85%)
- Invoice generation (80%)

**Unit Tests (20-25 tests):**
```
PlanServiceTest
├── testCreatePlan()
├── testUpdatePlan()
├── testPlanValidation()
└── testPlanPricing()

SubscriptionServiceTest
├── testCreateSubscription()
├── testRenewSubscription()
├── testCancelSubscription()
├── testUpgradeDowngrade()
└── testSubscriptionStatus()

UsageMeterServiceTest
├── testRecordUsage()
├── testCalculateCharges()
├── testLimitEnforcement()
└── testUsageReport()

InvoiceServiceTest
├── testGenerateInvoice()
├── testSendInvoice()
├── testPaymentReconciliation()
└── testInvoiceArchival()
```

**Integration Tests (10-12 tests):**
```
BillingIntegrationTest
├── testSubscriptionLifecycle()
├── testUsageToInvoicing()
├── testPaymentProcessing()
└── testBillingReports()
```

---

### 3.9 Integration Service (`integration-service`)

**Current State:** No test directory

**Coverage Goals:**
- Connector management (85%)
- Action execution (85%)
- Webhook handling (80%)
- Marketplace integration (75%)

**Unit Tests (25-30 tests):**
```
ConnectorServiceTest
├── testRegisterConnector()
├── testAuthenticateConnector()
├── testValidateConnectorConfig()
└── testDisconnectConnector()

ActionExecutorTest
├── testExecuteAction()
├── testMappingInputs()
├── testExtractOutputs()
├── testErrorHandling()
└── testActionTimeout()

WebhookServiceTest
├── testReceiveWebhook()
├── testValidateWebhookSignature()
├── testDeliverWebhook()
└── testRetry()

MarketplaceServiceTest
├── testListIntegrations()
├── testGetIntegrationDetails()
├── testRateIntegration()
└── testReviewIntegration()
```

**Integration Tests (12-15 tests):**
```
IntegrationLifecycleTest
├── testConnectorSetupFlow()
├── testActionExecution()
├── testWebhookHandling()
└── testMarketplaceNavigation()
```

---

### 3.10 Common Library (`threadly-common-spring`)

**Current State:** Base classes exist (AbstractIntegrationTest, MicroservicesIntegrationTest)

**Coverage Goals:**
- Utilities (95%)
- Common annotations (90%)
- Test infrastructure (95%)
- DTO validation (90%)

**Unit Tests (10-15 tests):**
```
UtilityTest
├── testDateFormatting()
├── testStringUUID()
├── testPagination()
└── testErrorHandling()

CommonAnnotationTest
├── testTenantAnnotation()
├── testAuditAnnotation()
└── testValidationAnnotation()

TestInfrastructureTest
├── testAbstractIntegrationTest()
├── testMicroservicesIntegrationTest()
└── testTestFixtures()
```

---

## 4. Frontend Testing Strategy

### 4.1 threadly-web (Next.js + React 19)

**Current State:** Playwright E2E tests exist, minimal component tests

**Unit Tests (Component & Hooks):**
```
tests/components/
├── BotBuilder.test.tsx          (80% coverage)
├── FlowCanvas.test.tsx          (85% coverage)
├── NodePanel.test.tsx           (80% coverage)
├── ConversationWindow.test.tsx  (75% coverage)
└── Dashboard.test.tsx           (70% coverage)

tests/hooks/
├── useAuth.test.ts              (95% coverage)
├── useBot.test.ts               (85% coverage)
├── useFlow.test.ts              (85% coverage)
└── useConversation.test.ts      (80% coverage)

tests/utils/
├── api-client.test.ts           (90% coverage)
├── validators.test.ts           (95% coverage)
└── formatters.test.ts           (85% coverage)
```

**Integration Tests:**
```
tests/integration/
├── auth-flow.test.ts            (User signup/login/logout)
├── bot-creation.test.ts         (Bot CRUD operations)
├── flow-building.test.ts        (Flow canvas interactions)
└── conversation.test.ts         (Message sending/receiving)
```

**E2E Tests (Playwright):**
```
tests/
├── auth.spec.ts                 ✓ EXISTING (Signup, login)
├── builder.spec.ts              ✓ EXISTING (Flow builder)
├── conversations.spec.ts        ✓ EXISTING (Chat interactions)
└── dashboard.spec.ts            (Dashboard navigation)
```

**Configuration:**
```
vitest.config.ts
├── Test environment setup
├── Module aliases (@/components, @/lib)
├── Coverage thresholds (lines: 70%, functions: 70%)
└── Mock server worker setup

playwright.config.ts ✓ EXISTING
├── Browser: Chromium
├── Reporter: HTML
├── Retries: 2 (CI), 0 (local)
└── Screenshot: Only on failure
```

---

### 4.2 threadly-widget (Standalone SDK)

**Current State:** 3 test files exist (storage, theme, ws-client)

**Unit Tests (All critical paths >85%):**
```
tests/
├── ws-client.test.ts            ✓ EXISTING (WebSocket)
├── storage.test.ts              ✓ EXISTING (LocalStorage)
├── theme.test.ts                ✓ EXISTING (Theme engine)
├── widget-loader.test.ts        (Widget initialization)
├── event-emitter.test.ts        (Event system)
├── message-handler.test.ts      (Message processing)
└── ui-renderer.test.ts          (DOM rendering)
```

**Integration Tests:**
```
tests/integration/
├── widget-lifecycle.test.ts     (Mount → Unmount)
├── message-flow.test.ts         (Send/receive messages)
└── theme-switching.test.ts      (Live theme updates)
```

**E2E Tests (Playwright):**
```
tests/e2e/
├── widget-embed.spec.ts         (HTML embed scenario)
├── multiple-instances.spec.ts   (Multiple widgets)
└── cross-domain.spec.ts         (Iframe cross-domain)
```

---

## 5. Test Infrastructure & Setup

### 5.1 Test Containers Configuration

**Java Services - Docker Compose Style:**
```java
@TestContainers
public class ServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("threadly_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> kafka = new GenericContainer<>("confluentinc/cp-kafka:7.6.0")
        .withExposedPorts(9092)
        .withEnv("KAFKA_ZOOKEEPER_CONNECT", "localhost:2181");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Container
    static GenericContainer<?> qdrant = new GenericContainer<>("qdrant/qdrant:latest")
        .withExposedPorts(6333)
        .waitingFor(Wait.forHttp("/health").forPort(6333));
}
```

### 5.2 Test Database Strategy

**Schema & Seeds:**
```sql
-- tests/fixtures/schema.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- tests/fixtures/seed-data.sql
INSERT INTO users (id, email, password_hash)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'test@threadly.dev',
    'hashed_password'
);
```

**Cleanup Strategy:**
```java
@BeforeEach
void setupTestDatabase() {
    // Truncate tables, reset sequences
    jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    jdbcTemplate.execute("TRUNCATE TABLE bots CASCADE");
}

@AfterEach
void cleanupTestDatabase() {
    // Full cleanup, verify no orphaned records
    assertAllTablesEmpty();
}
```

### 5.3 Mock & Fixture Factory Pattern

**Example: User Factory (Java)**
```java
@Component
public class UserFactory {

    public User createUser(String email, String orgId) {
        return User.builder()
            .id(UUID.randomUUID())
            .email(email)
            .password(PasswordUtil.hash("Test@123"))
            .orgId(orgId)
            .role(Role.MEMBER)
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public User createAdmin(String email, String orgId) {
        User user = createUser(email, orgId);
        user.setRole(Role.ADMIN);
        return user;
    }
}
```

**Example: Bot Fixture (TypeScript)**
```typescript
export const botFixtures = {
  simple: {
    name: "Test Bot",
    description: "A test bot",
    language: "en",
    theme: { primary: "#000000" }
  },

  withFlow: {
    ...simple,
    flows: [{
      id: uuid(),
      name: "Welcome Flow",
      nodes: [/* ... */]
    }]
  }
};
```

### 5.4 Mocking Strategy

**REST API Mocking (TypeScript - MSW):**
```typescript
// tests/mocks/handlers.ts
export const handlers = [
  rest.post('/api/bots', (req, res, ctx) => {
    return res(
      ctx.status(201),
      ctx.json({ id: uuid(), ...req.body })
    );
  }),

  rest.get('/api/bots/:botId', (req, res, ctx) => {
    return res(ctx.json(botFixtures.simple));
  })
];
```

**Service Mocking (Java - Mockito):**
```java
@ExtendWith(MockitoExtension.class)
public class BotServiceTest {

    @Mock
    BotRepository botRepository;

    @InjectMocks
    BotService botService;

    @Test
    void testCreateBot() {
        Bot bot = new Bot("Test Bot", "test-org");
        when(botRepository.save(any())).thenReturn(bot);

        Bot result = botService.createBot(bot);

        assertThat(result).isNotNull();
        verify(botRepository, times(1)).save(any());
    }
}
```

---

## 6. Continuous Integration & Testing Pipeline

### 6.1 GitHub Actions Test Jobs

**File:** `.github/workflows/ci.yml`

```yaml
jobs:
  # Java Services (All 10 services)
  java-tests:
    name: Java Services Testing
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env: { POSTGRES_DB: threadly_test, ... }
        ports: [ 5432:5432 ]
      kafka:
        image: confluentinc/cp-kafka:7.6.0
        ports: [ 9092:9092 ]
      redis:
        image: redis:7-alpine
        ports: [ 6379:6379 ]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: "21", distribution: "temurin", cache: "maven" }

      # Test each service
      - name: Test Identity Service
        run: cd services/identity-service && mvn test -Dsurefire.useFile=false -B

      - name: Test Workspace Service
        run: cd services/workspace-service && mvn test -Dsurefire.useFile=false -B

      # ... repeat for all services

      # Aggregate coverage
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./services/*/target/site/jacoco/jacoco.xml
          fail_ci_if_error: false

  # Frontend Testing
  typescript-tests:
    name: TypeScript Testing (threadly-web & widget)
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: "20.x", cache: "pnpm" }
      - run: pnpm install

      - name: Vitest Unit Tests
        run: pnpm test:unit --coverage

      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/coverage-final.json

  # E2E Tests
  e2e-tests:
    name: End-to-End Tests (Playwright)
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: "20.x" }

      - name: Start services (docker-compose)
        run: docker-compose -f docker-compose.test.yml up -d

      - name: Run Playwright Tests
        run: pnpm exec playwright test

      - name: Upload Playwright Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
```

### 6.2 Coverage Quality Gates

**SonarQube Integration:**
```yaml
sonarqube:
  host_url: https://sonarqube.threadly.dev

  quality_gates:
    - metric: coverage
      operator: ">"
      value: 70
      severity: BLOCKER

    - metric: new_coverage
      operator: ">"
      value: 75
      severity: MAJOR

    - metric: duplicated_lines_density
      operator: "<"
      value: 3
      severity: MAJOR

    - metric: security_rating
      operator: "="
      value: "A"
      severity: BLOCKER
```

### 6.3 Test Reporting & Dashboards

**Coverage Badge (README):**
```markdown
[![codecov](https://codecov.io/gh/threadly/threadly/branch/main/graph/badge.svg?token=XXXX)](https://codecov.io/gh/threadly/threadly)
[![Test Status](https://github.com/threadly/threadly/actions/workflows/ci.yml/badge.svg)](https://github.com/threadly/threadly/actions/workflows/ci.yml)
```

---

## 7. Test Execution & Environments

### 7.1 Local Development

**Run All Tests Locally:**
```bash
# Java services
cd services/[service-name]
mvn test                           # Unit tests only
mvn verify                         # Unit + integration tests
mvn verify -Dit.skip=false         # Include IT tests

# TypeScript
pnpm test                          # Vitest unit tests
pnpm test:watch                    # Watch mode
pnpm test:coverage                 # With coverage report

# E2E (requires running backend)
pnpm exec playwright test          # Headless
pnpm exec playwright test --ui     # UI mode (debug)
```

### 7.2 CI Environment

**PR Gate Requirements:**
1. All unit tests pass
2. Coverage must not decrease
3. No critical security issues
4. Lint/format checks pass
5. Type checking passes

**Main Branch Gate:**
1. All tests pass (unit + integration + E2E)
2. Coverage >80% on changed files
3. Coverage >70% overall
4. Security scan passes
5. Performance benchmarks pass

### 7.3 Staging Environment

**Test Execution:**
```bash
# Deploy services to staging
docker compose -f docker-compose.staging.yml up -d

# Run E2E tests against staging
NEXT_PUBLIC_API_URL=https://staging-api.threadly.dev \
  pnpm exec playwright test
```

---

## 8. Performance & Load Testing

### 8.1 JMeter Load Test Plan

**File:** `infrastructure/performance/load-test.jmx`

```jmeter
Test Plan: Threadly Microservices Load Test
├── Thread Group: 100 users, ramp-up 5s, duration 300s
│
├── Bot CRUD Operations
│   ├── POST /api/bots           (20 TPS target)
│   ├── GET /api/bots            (50 TPS target)
│   ├── PATCH /api/bots/{id}     (10 TPS target)
│   └── DELETE /api/bots/{id}    (5 TPS target)
│
├── Conversation Operations
│   ├── POST /api/conversations  (30 TPS target)
│   ├── POST /api/messages       (100 TPS target)
│   └── GET /api/conversations   (50 TPS target)
│
└── Assertions
    ├── Response time P95 < 500ms
    ├── Response time P99 < 1000ms
    ├── Error rate < 0.1%
    └── Throughput ≥ 200 TPS
```

### 8.2 K6 Performance Test

**File:** `infrastructure/performance/k6-test.js`

```javascript
import http from 'k6/http';
import { check, group, sleep } from 'k6';

export const options = {
  vus: 100,
  duration: '5m',
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.1']
  }
};

export default function () {
  group('Bot CRUD', () => {
    let botId = createBot();
    getBot(botId);
    updateBot(botId);
    deleteBot(botId);
  });

  sleep(1);
}

function createBot() {
  let res = http.post(
    `${__ENV.API_URL}/api/bots`,
    JSON.stringify({
      name: `Bot_${__VU}_${__ITER}`,
      description: 'Load test bot'
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { 'bot created': r => r.status === 201 });
  return res.json('id');
}
```

---

## 9. Security Testing

### 9.1 OWASP Dependency Scanning

**Maven Dependency Check:**
```xml
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>9.0.0</version>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

**GitHub Actions Integration:**
```yaml
- name: Run dependency check
  run: mvn dependency-check:check -Ddependency-check.fail.build=true
```

### 9.2 SQL Injection & Auth Security Tests

**Test Case: SQL Injection Prevention**
```java
@Test
void testSqlInjectionPrevention() {
    String maliciousInput = "'; DROP TABLE users; --";

    // Using parameterized queries
    User result = userRepository.findByEmail(maliciousInput);

    // Should safely return null, not execute DROP TABLE
    assertThat(result).isNull();

    // Verify table still exists
    assertThat(userRepository.count()).isGreaterThan(0);
}
```

**Test Case: JWT Token Validation**
```java
@Test
void testInvalidJwtToken() {
    String invalidToken = "invalid.token.here";

    JwtException exception = assertThrows(JwtException.class, () -> {
        authService.validateToken(invalidToken);
    });

    assertThat(exception.getMessage()).contains("Invalid token");
}

@Test
void testExpiredJwtToken() {
    String expiredToken = createExpiredToken();

    JwtException exception = assertThrows(JwtException.class, () -> {
        authService.validateToken(expiredToken);
    });

    assertThat(exception).isInstanceOf(ExpiredJwtException.class);
}
```

### 9.3 Rate Limiting & DDoS Tests

```java
@Test
void testRateLimiting() {
    // Simulate 100 requests in 1 second
    for (int i = 0; i < 100; i++) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bots");

        if (i < 50) {
            // First 50 should succeed (within rate limit)
            assertThat(rateLimiter.allow("user123")).isTrue();
        } else {
            // Next 50 should be blocked
            assertThat(rateLimiter.allow("user123")).isFalse();
        }
    }
}
```

---

## 10. Test Naming Conventions & Best Practices

### 10.1 Naming Pattern: Given-When-Then

**Java Tests:**
```java
@DisplayName("User authentication")
class AuthenticationServiceTest {

    @Test
    @DisplayName("Given valid credentials, When user authenticates, Then JWT token is returned")
    void testUserAuthenticationWithValidCredentials() {
        // Given
        String email = "user@test.com";
        String password = "Test@123";

        // When
        String token = authService.authenticate(email, password);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.validateToken(token)).isTrue();
    }
}
```

**TypeScript Tests:**
```typescript
describe('BotService', () => {
  it('given a valid bot payload, when creating a bot, then returns created bot with ID', () => {
    // Given
    const payload = { name: 'Test Bot', description: 'Test' };

    // When
    const result = botService.createBot(payload);

    // Then
    expect(result).toHaveProperty('id');
    expect(result.name).toBe('Test Bot');
  });
});
```

### 10.2 Test Organization by Concern

**Arrange-Act-Assert (AAA) Pattern:**
```java
@Test
void testBotDeletion() {
    // Arrange: Setup initial state
    Bot bot = botFactory.createBot("Test Bot", orgId);
    botRepository.save(bot);

    // Act: Perform action
    botService.deleteBot(bot.getId());

    // Assert: Verify outcome
    assertThat(botRepository.existsById(bot.getId())).isFalse();
}
```

### 10.3 Test Isolation & Independence

**Good: Independent Tests**
```java
@Test
void testBotCreation() {
    Bot bot = botService.createBot("Bot A", orgId);
    assertThat(bot.getId()).isNotNull();
}

@Test
void testBotDeletion() {
    // Creates its own bot, doesn't depend on testBotCreation
    Bot bot = botService.createBot("Bot B", orgId);
    botService.deleteBot(bot.getId());
    assertThat(botRepository.findById(bot.getId())).isEmpty();
}
```

**Bad: Interdependent Tests**
```java
@Test
void testBotCreation() {
    this.botId = botService.createBot("Bot", orgId).getId();
    // Relies on this.botId being set
}

@Test
void testBotDeletion() {
    // FAILS if testBotCreation doesn't run first!
    botService.deleteBot(this.botId);
}
```

---

## 11. Coverage Metrics & Reporting

### 11.1 Target Coverage by Service Tier

| Service | Unit | Integration | E2E | Overall Target |
|---------|------|-------------|-----|--------|
| Identity Service | 90% | 85% | 80% | 85% |
| Workspace Service | 85% | 80% | 75% | 80% |
| Flow Service | 85% | 80% | 75% | 80% |
| Runtime Service | 85% | 80% | 75% | 80% |
| Conversation Service | 80% | 75% | 70% | 75% |
| Knowledge Service | 80% | 75% | 70% | 75% |
| Analytics Service | 75% | 70% | 65% | 70% |
| Billing Service | 80% | 75% | 70% | 75% |
| Integration Service | 75% | 70% | 65% | 70% |
| **Frontend (web)** | 70% | 65% | 80% | 70% |
| **Widget (SDK)** | 85% | 75% | 70% | 80% |

### 11.2 Coverage Report Generation

**Maven Coverage (JaCoCo):**
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.10</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

**Vitest Coverage (TypeScript):**
```bash
pnpm test:coverage
# Generates: coverage/coverage-final.json
# HTML Report: coverage/index.html
```

**Codecov Integration:**
```yaml
# .codecov.yml
coverage:
  precision: 2
  round: down
  range: "70..100"

  status:
    project:
      default:
        target: 75
        threshold: 1%
    patch:
      default:
        target: 75
        threshold: 2%
```

---

## 12. Implementation Roadmap

### Phase 1: Foundation (Week 1-2)
- [ ] Create test base classes & infrastructure
- [ ] Set up TestContainers for all services
- [ ] Add 30 unit tests to identity-service
- [ ] Add 20 unit tests to workspace-service
- [ ] Configure Codecov integration

### Phase 2: Service Coverage (Week 3-6)
- [ ] Add 100+ unit tests across remaining 7 services
- [ ] Add 50+ integration tests
- [ ] Implement contract tests between services
- [ ] Achieve 70%+ coverage on critical services

### Phase 3: Frontend & E2E (Week 7-8)
- [ ] Add 50+ component tests to threadly-web
- [ ] Add 30+ component tests to threadly-widget
- [ ] Expand E2E tests (auth, bot creation, conversation)
- [ ] Set up Playwright visual regression testing

### Phase 4: Advanced Testing (Week 9-10)
- [ ] Implement performance/load testing (JMeter, K6)
- [ ] Add security testing (SQL injection, OWASP, auth)
- [ ] Implement API contract testing
- [ ] Set up SonarQube quality gates

### Phase 5: Optimization & CI/CD (Week 11-12)
- [ ] Optimize test execution time
- [ ] Parallelize test runs in CI
- [ ] Add coverage trending
- [ ] Create testing documentation & runbooks

---

## 13. Test Execution Checklist

**Before Submitting PR:**
```bash
# Local development
mvn clean test                    # Java unit tests
pnpm test:unit                    # TypeScript unit tests
pnpm lint && pnpm format          # Code quality

# Optional (but recommended)
mvn verify                        # Java + integration
pnpm test:coverage               # Coverage report
```

**CI Will Automatically:**
```bash
✓ Run all unit tests
✓ Run all integration tests
✓ Run E2E tests (if UI changed)
✓ Generate coverage reports
✓ Upload to Codecov
✓ Check against quality gates
✓ Run security scans
✓ Post results to PR
```

---

## 14. Troubleshooting Common Test Issues

### Issue: Flaky Tests (Intermittent Failures)

**Causes & Solutions:**
1. **Timing Issues** → Use explicit waits, not Thread.sleep()
   ```java
   // Bad
   Thread.sleep(1000);

   // Good
   await().atMost(5, SECONDS).until(() -> resource.isReady());
   ```

2. **Order Dependencies** → Run tests in random order
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-surefire-plugin</artifactId>
     <configuration>
       <runOrder>random</runOrder>
     </configuration>
   </plugin>
   ```

3. **Port Conflicts** → Use random ports
   ```java
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   ```

### Issue: Test Container Startup Failures

**Solution: Increase Timeout & Retries**
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
    .waitingFor(Wait.forLogMessage(".*ready to accept connections.*", 1))
    .withStartupTimeout(Duration.ofMinutes(2));
```

### Issue: Memory Leaks in Long-Running Tests

**Solution: Proper Resource Cleanup**
```java
@AfterEach
void cleanup() {
    // Close connections
    mockServer.shutdown();
    executorService.shutdownNow();

    // Verify cleanup
    assertThat(openConnections.get()).isZero();
}
```

---

## 15. Additional Resources

### Testing Documentation
- JUnit5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- TestContainers: https://www.testcontainers.org/
- Vitest Guide: https://vitest.dev/
- Playwright Documentation: https://playwright.dev/

### Code Examples Repository
```
infrastructure/testing/
├── examples/
│   ├── java-unit-test-example.md
│   ├── java-integration-test-example.md
│   ├── typescript-unit-test-example.md
│   ├── typescript-e2e-test-example.md
│   └── docker-compose.test.yml
├── templates/
│   ├── ServiceTest.java.template
│   ├── ServiceIntegrationTest.java.template
│   └── component.test.tsx.template
└── scripts/
    ├── run-all-tests.sh
    ├── generate-coverage-report.sh
    └── ci-test-gate.sh
```

---

## Sign-Off & Approvals

| Role | Name | Date | Signature |
|------|------|------|-----------|
| **QA Lead** | — | — | — |
| **Tech Lead** | — | — | — |
| **DevOps** | — | — | — |

---

**Document Version:** 1.0
**Last Updated:** 2026-05-24
**Next Review:** 2026-06-07
