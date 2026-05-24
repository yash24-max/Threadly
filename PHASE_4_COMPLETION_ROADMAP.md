# Phase 4: Microservices Completion Roadmap
**Objective:** From 85% complete to production-ready in 7 days
**Target Date:** June 1, 2026
**Team Allocation:** All 6 agents (Backend, Frontend, AI, Testing, Tech Lead, Product)

---

## Day 1: Unblock Critical Paths (Blockers)

### Task 1.1: Fix API Contract Generation [Backend Agent]
**Priority:** 🔴 CRITICAL | **Time:** 2 hours

**Step 1: Add Springdoc Dependency**
```bash
# File: services/threadly-core/pom.xml
# Add after existing springdoc dependencies:
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.1.0</version>
</dependency>
```

**Step 2: Annotate All Controllers**
```java
// Every @RestController must have @Tag
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Auth endpoints")
public class AuthController { ... }

// Every endpoint must have @Operation
@PostMapping("/login")
@Operation(summary = "User login")
public ResponseEntity<TokenResponse> login(...) { ... }
```

**Files to Annotate:**
- `AuthController.java`
- `BotController.java`
- `FlowController.java`
- `ConversationController.java`
- `KbController.java`
- `AnalyticsController.java`
- `TeamController.java`
- `ApiKeyController.java`
- `WebhookController.java`
- `CredentialsController.java`

**Step 3: Generate OpenAPI Spec**
```bash
cd services/threadly-core
mvn springdoc-openapi:generate
# Verify: Check if src/main/resources/openapi.yaml was created
```

**Verification:**
```bash
# Start server
mvn spring-boot:run

# Verify endpoint
curl http://localhost:8080/v3/api-docs | jq .paths

# Should show all endpoints with proper schema
```

**Expected Outcome:**
- ✅ `springdoc-openapi-starter-webmvc-ui` dependency added
- ✅ All controllers annotated with @Tag
- ✅ All endpoints annotated with @Operation, @Parameter, @RequestBody, @ApiResponse
- ✅ `/v3/api-docs` endpoint returns complete OpenAPI spec

---

### Task 1.2: Generate Frontend API Types [Frontend Agent]
**Priority:** 🔴 CRITICAL | **Time:** 1 hour

**Step 1: Verify Orval Config**
```bash
# File: frontend/threadly-web/orval.config.ts
# Already configured, just needs to run

cat orval.config.ts
# Should show:
# input: 'http://localhost:8080/v3/api-docs'
# output: { target: 'src/lib/generated' }
```

**Step 2: Start Backend Server**
```bash
cd services/threadly-core
mvn spring-boot:run &
# Wait for "Started ... in X seconds"
```

**Step 3: Run Orval Code Generation**
```bash
cd frontend/threadly-web
npm install  # Ensure orval installed
npm run codegen
# or: npx orval --config orval.config.ts
```

**Step 4: Verify Generated Types**
```bash
# Check files were created
ls -la src/lib/generated/
# Should contain:
# index.ts (barrel export)
# auth/index.ts (AuthController queries/mutations)
# workspace/index.ts (BotController)
# conversation/index.ts (ConversationController)
# etc.
```

**Step 5: Replace Hand-Written Calls**
```bash
# Find all instances of direct fetch calls
grep -r "fetch(" src/components/ src/app/ | grep -v "node_modules"

# Replace with generated React Query hooks
# Example:
# OLD: const response = await fetch('/v1/bots', { ... })
# NEW: const { data } = useBotControllerGetBots()
```

**Expected Outcome:**
- ✅ `src/lib/generated/` populated with typed API client
- ✅ All Controllers have corresponding generated hooks
- ✅ Frontend TypeScript errors from API types reduced from 23 → ~3
- ✅ All API calls now type-safe

---

### Task 1.3: Implement Idempotency Handling [Backend Agent]
**Priority:** 🔴 CRITICAL | **Time:** 4 hours

**Step 1: Configure Redis for Idempotency Cache**
```yaml
# File: services/threadly-core/src/main/resources/application.yml
# Add Redis config section:
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    timeout: 2000ms
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

threadly:
  idempotency:
    enabled: true
    cache-ttl: 3600  # 1 hour
    key-prefix: "idempotent:"
```

**Step 2: Create Idempotency Interceptor**
```java
// File: services/threadly-core/src/main/java/dev/threadly/core/idempotency/IdempotencyInterceptor.java
@Component
public class IdempotencyInterceptor extends HandlerInterceptorAdapter {
  private final IdempotencyService idempotencyService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String idempotencyKey = request.getHeader("X-Idempotency-Key");
    if (idempotencyKey != null && request.getMethod().equals("POST")) {
      Optional<String> cachedResponse = idempotencyService.getResponse(idempotencyKey);
      if (cachedResponse.isPresent()) {
        response.setStatus(200);
        response.getWriter().write(cachedResponse.get());
        return false;  // Skip handler
      }
    }
    return true;
  }
}
```

**Step 3: Apply @Idempotent to POST Endpoints**
```java
// Files to update:
// - ConversationController.createConversation()
// - FlowController.createFlow()
// - BotController.createBot()
// - Any POST that creates resources

@PostMapping
@Idempotent(ttlSeconds = 3600)
public ResponseEntity<ConversationDto> createConversation(
    @RequestHeader("X-Idempotency-Key") String idempotencyKey,
    @RequestBody CreateConversationRequest req) {
  ConversationDto result = conversationService.create(req);
  idempotencyService.cacheResponse(idempotencyKey, jsonSerialize(result));
  return ResponseEntity.ok(result);
}
```

**Step 4: Write Idempotency Tests**
```java
// File: services/threadly-core/src/test/java/.../IdempotencyIntegrationTest.java
@Test
void sameIdempotencyKeyReturnsCachedResponse() {
  String key = UUID.randomUUID().toString();

  // First request
  ConversationDto first = postCreateConversation(key, request);

  // Second request with same key
  ConversationDto second = postCreateConversation(key, request);

  // Must be identical (same ID, same data)
  assertEquals(first.getId(), second.getId());
  assertEquals(first.getCreatedAt(), second.getCreatedAt());
}

@Test
void differentIdempotencyKeysCreateSeparateResources() {
  String key1 = UUID.randomUUID().toString();
  String key2 = UUID.randomUUID().toString();

  ConversationDto first = postCreateConversation(key1, request);
  ConversationDto second = postCreateConversation(key2, request);

  assertNotEquals(first.getId(), second.getId());
}
```

**Step 5: Update Widget to Use Idempotency Keys**
```typescript
// File: threadly-widget/src/ws-client.ts
async sendMessage(text: string): Promise<Message> {
  const idempotencyKey = generateIdempotencyKey();  // UUID

  const response = await fetch('/v1/conversations/{id}/messages', {
    method: 'POST',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ content: text }),
  });

  return response.json();
}
```

**Expected Outcome:**
- ✅ Redis idempotency cache configured
- ✅ Idempotency interceptor catches duplicate requests
- ✅ All POST endpoints require `X-Idempotency-Key` header
- ✅ Tests verify idempotency behavior
- ✅ Widget generates and sends idempotency keys

---

### Task 1.4: Write Tenant Isolation Tests [Testing Agent]
**Priority:** 🔴 CRITICAL | **Time:** 3 hours

**Step 1: Create Test Base Class**
```java
// File: services/threadly-core/src/test/java/.../TenantIsolationTest.java
@SpringBootTest
@TestcontainersTest
public class TenantIsolationTest extends AbstractIntegrationTest {
  @Autowired private RestClient restClient;
  @Autowired private BotRepository botRepository;
  @Autowired private ConversationRepository conversationRepository;

  private String orgAToken;  // JWT with orgId=ORG_A
  private String orgBToken;  // JWT with orgId=ORG_B

  @BeforeEach
  void setUp() {
    // Create two separate organizations
    orgAToken = generateJwt("ORG_A", "user@orga.com");
    orgBToken = generateJwt("ORG_B", "user@orgb.com");
  }
}
```

**Step 2: Test Bot Isolation**
```java
@Test
void orgACannotAccessOrgBsBots() {
  // Setup: Org A creates bot
  TenantContext.set("ORG_A");
  Bot botA = botRepository.save(new Bot()
    .setName("Bot A")
    .setOrgId("ORG_A")
  );
  TenantContext.clear();

  // Setup: Org B creates bot
  TenantContext.set("ORG_B");
  Bot botB = botRepository.save(new Bot()
    .setName("Bot B")
    .setOrgId("ORG_B")
  );
  TenantContext.clear();

  // Test: Org A API key lists bots
  List<Bot> botsSeenByOrgA = botRepository.findAll();  // Uses @Filter

  // Assert: Should only see Bot A
  assertEquals(1, botsSeenByOrgA.size());
  assertEquals("ORG_A", botsSeenByOrgA.get(0).getOrgId());
}
```

**Step 3: Test Conversation Isolation**
```java
@Test
void orgACannotAccessOrgBsConversations() {
  // Create conversations in different orgs
  Conversation convA = createConversation("ORG_A", "Visitor A");
  Conversation convB = createConversation("ORG_B", "Visitor B");

  // Query as Org A
  List<Conversation> visibleToOrgA = conversationRepository.findAll();

  // Assert: Only see Org A conversations
  assertEquals(1, visibleToOrgA.size());
  assertEquals(convA.getId(), visibleToOrgA.get(0).getId());
}
```

**Step 4: Test API Key Scoping**
```java
@Test
void orgAApiKeyCannotAccessOrgBEndpoints() {
  // Org A creates bot
  String botIdA = createBotAs("ORG_A", "Bot A").getId();

  // Org B creates bot
  String botIdB = createBotAs("ORG_B", "Bot B").getId();

  // Org A API key tries to access Org B's bot
  Response response = restClient
    .get("/v1/bots/" + botIdB)
    .header("Authorization", "Bearer " + orgAApiKey)
    .execute();

  // Assert: 403 Forbidden
  assertEquals(403, response.statusCode());
}
```

**Step 5: Test TenantContext Cleanup**
```java
@Test
void tenantContextClearedAfterRequest() {
  // Make request as Org A
  restClient.get("/v1/bots").header("Authorization", "Bearer " + orgAToken).execute();

  // Assert: TenantContext is null (thread-safe cleanup in interceptor)
  assertNull(TenantContext.get());
}
```

**Expected Outcome:**
- ✅ `TenantIsolationTest.java` created with 5+ test cases
- ✅ All isolation tests pass
- ✅ Hibernate @Filter verified to work
- ✅ API key scoping verified
- ✅ TenantContext cleanup verified

---

## Day 2: Resolve Code Quality Issues

### Task 2.1: Fix CVEs and Dependencies [Tech Lead Agent]
**Priority:** 🟡 HIGH | **Time:** 2 hours

**Step 1: Run Dependency Audit**
```bash
cd services/threadly-core
mvn dependency-check:check

# Output should show 1 MEDIUM CVE in Jackson
# CVE-XXXX: Jackson deserialization vulnerability
```

**Step 2: Update Jackson Version**
```xml
<!-- File: services/threadly-core/pom.xml -->
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.15.2</version>  <!-- Updated from 2.14.x -->
</dependency>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson</groupId>
      <artifactId>jackson-bom</artifactId>
      <version>2.15.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

**Step 3: Re-run Audit**
```bash
mvn clean dependency-check:check
# Assert: All CVEs resolved
```

---

### Task 2.2: Fix TypeScript Type Errors [Frontend Agent]
**Priority:** 🟡 HIGH | **Time:** 2 hours

**Step 1: Run Type Check**
```bash
cd frontend/threadly-web
npm run typecheck
# Should show reduced errors after Orval codegen
# Expected: 23 → ~3 errors (in node_modules or build files)
```

**Step 2: Fix Remaining Errors**
```typescript
// File: components/builder/NodePanel.tsx
// Fix: Remove unused props, add proper types from generated API

// OLD:
interface Props {
  node: any
  data: any
}

// NEW:
import { NodeCatalogEntryDto } from '@/lib/generated/flow'
interface Props {
  node: NodeCatalogEntryDto
  onUpdate: (node: NodeCatalogEntryDto) => void
}
```

**Expected Outcome:**
- ✅ TypeScript errors < 5
- ✅ All API types from Orval generated client
- ✅ No unused imports in main components

---

### Task 2.3: Optimize Widget Bundle Size [Frontend Agent]
**Priority:** 🟡 HIGH | **Time:** 2 hours

**Step 1: Analyze Current Build**
```bash
cd threadly-widget
npm run build
# Output: 42 KB (target: < 35 KB)
```

**Step 2: Identify Large Dependencies**
```bash
npm ls --depth=0
# Check: preact, ws-client, theme.ts file sizes
```

**Step 3: Optimize**
```typescript
// File: threadly-widget/src/main.tsx
// 1. Code-split non-critical components
const ChatPanel = lazy(() => import('./ui/ChatPanel.tsx'))

// 2. Remove unused polyfills
// 3. Tree-shake CSS utilities

// 4. Dynamic import for theme
const loadTheme = () => import('./theme.ts')
```

**Step 4: Verify Size**
```bash
npm run build
# Assert: < 35 KB gzipped
```

**Expected Outcome:**
- ✅ Widget bundle < 35 KB gzipped
- ✅ No breaking changes in API

---

## Day 3-4: Integration Testing

### Task 3.1: API Contract Validation [Testing Agent]
**Priority:** 🟢 MEDIUM | **Time:** 4 hours

**Step 1: Contract Test: List Bots**
```java
// File: services/threadly-core/src/test/java/.../ContractTest.java
@Test
void getBots_ReturnsProperSchema() {
  Response response = restClient
    .get("/v1/bots")
    .header("Authorization", "Bearer " + token)
    .execute();

  assertEquals(200, response.statusCode());

  BotListResponse body = response.as(BotListResponse.class);
  assertNotNull(body.getData());
  assertTrue(body.getData().size() >= 0);

  if (!body.getData().isEmpty()) {
    BotDto bot = body.getData().get(0);
    assertNotNull(bot.getId());
    assertNotNull(bot.getName());
    assertNotNull(bot.getCreatedAt());
  }
}
```

**Step 2: Contract Test: Create Conversation**
```java
@Test
void createConversation_ReturnsValidDto() {
  CreateConversationRequest req = new CreateConversationRequest()
    .setBotId(botId)
    .setVisitorId("visitor_123");

  Response response = restClient
    .post("/v1/conversations")
    .body(req)
    .header("Authorization", "Bearer " + token)
    .execute();

  assertEquals(201, response.statusCode());

  ConversationDto conv = response.as(ConversationDto.class);
  assertNotNull(conv.getId());
  assertEquals(botId, conv.getBotId());
}
```

---

### Task 3.2: Real-Time Messaging Test [Testing Agent]
**Priority:** 🟢 MEDIUM | **Time:** 3 hours

**Step 1: Test Centrifugo Message Delivery**
```javascript
// File: threadly-widget/tests/e2e/centrifugo.spec.ts
test('widget receives message from core via centrifugo', async () => {
  // 1. Start widget in browser
  const page = await browser.newPage();
  await page.goto('http://localhost:3000/test-widget');

  // 2. Send message from core via HTTP
  const response = await fetch('http://localhost:8080/v1/conversations/{id}/messages', {
    method: 'POST',
    headers: { 'Authorization': 'Bearer ...' },
    body: JSON.stringify({ content: 'Hello from backend' })
  });

  // 3. Widget should receive via WebSocket
  const messageElement = await page.waitForSelector('[data-testid="message"]');
  const text = await messageElement.textContent();
  expect(text).toContain('Hello from backend');
});
```

---

### Task 3.3: KB Ingestion End-to-End [Testing Agent]
**Priority:** 🟢 MEDIUM | **Time:** 2 hours

**Step 1: Test Document Ingestion Flow**
```java
@Test
void kbDocument_IngestedSuccessfully() {
  // 1. Upload document
  KbDocument doc = kbService.createDocument(
    botId,
    "integration_guide.pdf",
    fileContent
  );
  assertEquals(KbDocumentStatus.PROCESSING, doc.getStatus());

  // 2. Wait for ingestion job
  Thread.sleep(5000);

  // 3. Verify document ready
  doc = kbRepository.findById(doc.getId()).get();
  assertEquals(KbDocumentStatus.READY, doc.getStatus());

  // 4. Test search
  List<KbDocument> results = kbService.search(botId, "integration");
  assertEquals(1, results.size());
}
```

---

## Day 5-7: Production Hardening

### Task 5.1: Complete Test Coverage [Testing Agent]
**Priority:** 🟢 MEDIUM | **Time:** 6 hours

**Targets:**
- Java: 68% → 75% coverage
- Python: 71% → 75% coverage
- TypeScript: 45% → 70% coverage

**Focus Areas:**
- Error handling paths
- Security filters (HMAC, Rate limit, JWT)
- Edge cases (empty list, null values, boundary conditions)

---

### Task 5.2: Load Testing [Tech Lead Agent]
**Priority:** 🟢 MEDIUM | **Time:** 4 hours

**Test Scenario: 100 Concurrent Visitors**
```bash
# File: infra/load-test/k6-script.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },  // Ramp up
    { duration: '5m', target: 100 },  // Hold
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],  // 95th percentile < 500ms
    'http_req_failed': ['rate<0.1'],  // < 10% failure rate
  },
};

export default function () {
  // Send message
  let res = http.post(`${__ENV.BASE_URL}/v1/conversations/${convId}/messages`, {
    content: `Test message ${__VU}`,
  }, {
    headers: { 'Authorization': `Bearer ${token}` },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
```

**Run Test:**
```bash
k6 run -e BASE_URL=http://localhost:8080 load-test/k6-script.js

# Expected Results:
# - p95 latency < 500ms
# - p99 latency < 1000ms
# - failure rate < 10%
```

---

### Task 5.3: Staging Deployment [Tech Lead Agent]
**Priority:** 🟢 MEDIUM | **Time:** 2 hours

**Deploy to Railway Staging:**
```bash
# Push to main
git add .
git commit -m "Phase 4: Complete microservices implementation"
git push origin main

# Railway auto-deploys from main
# Verify: https://threadly-staging.railway.app

# Smoke tests
curl https://threadly-staging.railway.app/v1/health
curl https://threadly-staging.railway.app/api/v1/bots
```

---

## Success Criteria

### Day 1 Completion (All 3 Blockers Fixed)
- [ ] API contracts auto-generated and validating
- [ ] Idempotency keys working on all POST endpoints
- [ ] Tenant isolation tests passing

### Day 2 Completion (Code Quality)
- [ ] All CVEs resolved
- [ ] TypeScript < 5 type errors
- [ ] Widget bundle < 35 KB

### Day 4 Completion (Integration)
- [ ] All integration tests passing
- [ ] Real-time messaging E2E verified
- [ ] KB ingestion flow tested

### Day 7 Completion (Production Ready)
- [ ] 70%+ test coverage across all services
- [ ] Load test: 100 concurrent, p95 < 500ms
- [ ] Staging deployment passing smoke tests
- [ ] Rollback plan documented

---

## Git Workflow

```bash
# Day 1: Blockers
git worktree add ../threadly-phase4-day1 feat/phase4-blockers
# Fix API contracts, idempotency, tenant tests
git commit -m "fix: Unblock critical paths — API contracts, idempotency, tenant isolation"
git push origin feat/phase4-blockers
# Merge to main

# Day 2: Quality
git worktree add ../threadly-phase4-quality feat/phase4-quality
# Fix CVEs, type errors, bundle size
git commit -m "fix: Resolve code quality issues — CVEs, types, bundle size"
git push origin feat/phase4-quality
# Merge to main

# Day 3-4: Testing
git worktree add ../threadly-phase4-testing feat/phase4-testing
# Add integration tests, load tests, E2E
git commit -m "test: Add integration and load testing — contract validation, Centrifugo, KB flow"
git push origin feat/phase4-testing
# Merge to main

# Final: Deploy to staging
git checkout main
git merge feat/phase4-*
git push origin main
```

---

## Communication Checklist

- [ ] Day 1 EOD: Blockers resolved — notify product launch possible
- [ ] Day 2 EOD: Code quality green — ready for integration
- [ ] Day 4 EOD: Integration tests passing — ready for load test
- [ ] Day 7: Production readiness signoff from all agents

