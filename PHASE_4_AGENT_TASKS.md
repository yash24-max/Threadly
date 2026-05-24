# Phase 4: Agent Task Assignments
**Duration:** 7 days (May 24 - May 31, 2026)
**Goal:** From 85% → Production-Ready

---

## 🔵 Backend Agent [Java/Spring Boot]

### Day 1: Critical Blocker — API Contracts & Idempotency

**Task 1.1: Add Springdoc & Annotate Controllers** [2 hours]
- [ ] Add `springdoc-openapi-starter-webmvc-ui` to `threadly-core/pom.xml`
- [ ] Add `@Tag` to all 10 controllers (Auth, Bot, Flow, Conversation, KB, Analytics, Team, ApiKey, Webhook, Credentials)
- [ ] Add `@Operation`, `@Parameter`, `@RequestBody`, `@ApiResponse` to all endpoints
- [ ] Run `mvn springdoc-openapi:generate`
- [ ] Verify `/v3/api-docs` returns complete OpenAPI spec
- [ ] Commit: `feat: Add Springdoc OpenAPI generation with complete endpoint annotations`

**Task 1.2: Implement Idempotency** [4 hours]
- [ ] Create `IdempotencyInterceptor` class
- [ ] Add Redis configuration to `application.yml`
- [ ] Add `IdempotencyService` with Redis caching
- [ ] Create `@Idempotent` annotation
- [ ] Apply `@Idempotent` to these endpoints:
  - `ConversationController.createConversation()`
  - `FlowController.createFlow()`
  - `BotController.createBot()`
  - `KbController.createDocument()`
  - `ApiKeyController.createApiKey()`
- [ ] Write `IdempotencyIntegrationTest` with 4 test cases
- [ ] Commit: `feat: Implement idempotency with Redis caching and @Idempotent annotation`

**Task 1.3: Fix CVEs** [1 hour]
- [ ] Run `mvn dependency-check:check`
- [ ] Update Jackson to 2.15.2 (fixes Jackson deserialization vulnerability)
- [ ] Re-run audit, verify all CVEs resolved
- [ ] Commit: `chore: Update Jackson to 2.15.2 — fix MEDIUM CVE`

### Day 2: Code Quality & Testing

**Task 2.1: Increase Test Coverage** [4 hours]
- [ ] Target: 68% → 75% line coverage
- [ ] Add tests for error paths:
  - Missing required fields
  - Invalid JWT tokens
  - Rate limit exceeded
  - Tenant access denied
- [ ] Run `mvn verify -Pcoverage` and update coverage report
- [ ] Commit: `test: Add error handling and edge case coverage`

**Task 2.2: Fix Checkstyle Violations** [1 hour]
- [ ] Run `mvn checkstyle:check`
- [ ] Fix 4 violations in `ConversationController.java`
- [ ] Commit: `chore: Fix checkstyle violations in ConversationController`

### Day 3-4: Integration Testing

**Task 3.1: Contract Testing** [2 hours]
- [ ] Create `ContractTest` class
- [ ] Write 5+ contract tests:
  - GET /v1/bots → proper schema
  - POST /v1/conversations → valid DTO
  - GET /v1/conversations/{id}/messages → message list with pagination
  - POST /v1/webhooks → webhook registration
  - GET /v1/analytics/bots/{id} → analytics response
- [ ] Verify all responses match OpenAPI schema
- [ ] Commit: `test: Add API contract validation tests`

**Task 3.2: Tenant Isolation Tests** [Covered by Testing Agent]

### Day 5-7: Production Hardening

**Task 5.1: Load Testing Preparation** [1 hour]
- [ ] Ensure connection pooling configured in `application.yml`
- [ ] Verify database connection limits
- [ ] Configure Centrifugo for 100 concurrent connections
- [ ] Commit: `chore: Configure for load testing — connection pooling, limits`

**Task 5.2: Webhook Retry Logic** [2 hours]
- [ ] Implement exponential backoff in `WebhookDeliveryService`
- [ ] Add DLQ (Dead Letter Queue) for failed webhooks
- [ ] Write tests for retry behavior
- [ ] Commit: `feat: Implement webhook retry with exponential backoff and DLQ`

**Deliverables:**
- ✅ API contracts auto-generated & tested
- ✅ Idempotency fully implemented
- ✅ 75% test coverage
- ✅ All CVEs resolved
- ✅ Webhook delivery hardened

---

## 🟢 Frontend Agent [Next.js/React/TypeScript]

### Day 1: Critical Blocker — API Type Safety

**Task 1.1: Generate API Client** [1 hour]
- [ ] Ensure `threadly-core` server running on port 8080
- [ ] Verify `orval.config.ts` configuration is correct
- [ ] Run `npm run codegen` (or `npx orval --config orval.config.ts`)
- [ ] Verify `src/lib/generated/` populated with:
  - `index.ts` (barrel export)
  - `auth/index.ts` (AuthController hooks)
  - `workspace/index.ts` (BotController hooks)
  - `conversation/index.ts` (ConversationController hooks)
  - `knowledge/index.ts` (KbController hooks)
  - `analytics/index.ts` (AnalyticsController hooks)
- [ ] Commit: `feat: Generate API client types from OpenAPI spec via Orval`

**Task 1.2: Replace Hand-Written Fetch Calls** [2 hours]
- [ ] Find all `fetch()` calls in components:
  ```bash
  grep -r "fetch(" src/components src/app | grep -v node_modules
  ```
- [ ] Replace with generated hooks:
  - `useBotControllerGetBots()` instead of `fetch('/v1/bots')`
  - `useConversationControllerCreateConversation()` instead of POST fetch
  - `useKbControllerQueryDocuments()` for knowledge search
- [ ] Add error handling with React Query's `useQuery` error states
- [ ] Commit: `refactor: Replace hand-written API calls with generated React Query hooks`

### Day 2: Code Quality

**Task 2.1: Fix TypeScript Errors** [1 hour]
- [ ] Run `npm run typecheck`
- [ ] Fix type errors in:
  - `NodePanel.tsx` (accept `NodeCatalogEntryDto`)
  - `FlowCanvas.tsx` (proper Flow entity types)
  - Any component using API response types
- [ ] Remove unused imports from all components
- [ ] Expected result: < 5 remaining type errors
- [ ] Commit: `fix: Resolve TypeScript errors after Orval codegen`

**Task 2.2: Optimize Widget Bundle** [2 hours]
- [ ] cd `threadly-widget/`
- [ ] Run `npm run build` and check current size (42 KB)
- [ ] Analyze dependencies: `npm ls --depth=0`
- [ ] Apply optimizations:
  - Code-split non-critical components with `lazy()`
  - Remove unused CSS utilities
  - Tree-shake unused dependencies
  - Consider dynamic imports for theme
- [ ] Target: < 35 KB gzipped
- [ ] Verify no breaking changes: run `npm run test`
- [ ] Commit: `perf: Optimize widget bundle — reduce from 42KB to <35KB`

**Task 2.3: Add Skeleton Loaders** [1 hour]
- [ ] Complete `skeleton-loader.tsx` component
- [ ] Add to pages:
  - Dashboard (bot cards)
  - Conversations (message list)
  - Builder (node catalog)
  - Knowledge base (document list)
- [ ] Use with React Query `isLoading` state
- [ ] Commit: `feat: Add skeleton loaders to all main pages`

### Day 3-4: Integration Testing

**Task 3.1: E2E Test: Authentication Flow** [2 hours]
- [ ] File: `tests/e2e/auth.spec.ts`
- [ ] Test login → redirect → dashboard
- [ ] Verify token storage and auto-refresh
- [ ] Test logout and session cleanup
- [ ] Run: `npm run e2e`
- [ ] Commit: `test: Add E2E tests for authentication flow`

**Task 3.2: E2E Test: Builder Flow** [2 hours]
- [ ] File: `tests/e2e/builder.spec.ts`
- [ ] Test: Create node, drag on canvas, update properties
- [ ] Test: Publish flow, verify validation
- [ ] Test: Error handling for invalid nodes
- [ ] Run: `npm run e2e`
- [ ] Commit: `test: Add E2E tests for flow builder`

**Task 3.3: Component Unit Tests** [2 hours]
- [ ] Target: 45% → 70% coverage
- [ ] Add tests for:
  - `NodePanel.tsx` (props update, validation)
  - `ChatPanel.tsx` (message rendering, scrolling)
  - `ConversationList.tsx` (virtual scrolling, search)
- [ ] Run: `npm run test -- --coverage`
- [ ] Commit: `test: Increase component test coverage to 70%`

### Day 5-7: Polish

**Task 5.1: Form Validation in PropertiesPanel** [1 hour]
- [ ] Implement client-side validation
- [ ] Show error messages for invalid inputs
- [ ] Disable submit button until valid
- [ ] Commit: `feat: Add form validation to PropertiesPanel`

**Task 5.2: Loading States & Error Boundaries** [1 hour]
- [ ] Expand `error-boundary.tsx` to catch all errors
- [ ] Add retry button in error UI
- [ ] Test with intentional errors
- [ ] Commit: `feat: Enhance error boundary and loading states`

**Deliverables:**
- ✅ Type-safe API client integrated
- ✅ No hand-written fetch calls
- ✅ < 5 TypeScript errors
- ✅ Widget < 35 KB
- ✅ E2E tests passing
- ✅ 70% unit test coverage

---

## 🟣 AI/ML Agent [Python/FastAPI]

### Day 1-2: Configuration & Testing

**Task 1.1: Configure Embedding Model** [2 hours]
- [ ] File: `threadly-ai/app/config.py`
- [ ] Add embedding model configuration:
  ```python
  EMBEDDING_MODEL = "text-embedding-3-small"  # or use local model
  EMBEDDING_DIMENSION = 1536
  RERANKER_MODEL = "rerank-english-v2.0"
  ```
- [ ] Test embedding generation:
  ```python
  embeddings = embedding_provider.embed(["test document"])
  assert embeddings[0].shape == (1536,)
  ```
- [ ] Commit: `feat: Configure embedding and reranker models`

**Task 1.2: Fix Lint Issues** [1 hour]
- [ ] Run `ruff check app/`
- [ ] Fix 2 violations in `rag/retrieval.py` (import ordering)
- [ ] Run `black --check app/` and format
- [ ] Commit: `chore: Fix linting issues in RAG module`

**Task 1.3: Increase Test Coverage** [2 hours]
- [ ] Current: 71% (already meets 70% target)
- [ ] Add edge case tests:
  - Empty document list
  - No matching results
  - Reranker failures
  - Large document processing
- [ ] Run: `pytest --cov=app`
- [ ] Commit: `test: Add edge case coverage for RAG pipeline`

### Day 3-4: Integration & Performance

**Task 3.1: Test RAG Pipeline End-to-End** [2 hours]
- [ ] File: `tests/test_kb_integration.py`
- [ ] Test flow:
  1. Document ingestion (chunk, embed, upsert to Qdrant)
  2. Query (dense search in Qdrant + sparse search with BM25)
  3. RRF ranking
  4. Optional Cohere reranking
  5. Verify top-k results match expected documents
- [ ] Run: `pytest tests/test_kb_integration.py`
- [ ] Commit: `test: Add RAG pipeline integration tests`

**Task 3.2: Performance Benchmarking** [2 hours]
- [ ] File: `tests/test_performance.py`
- [ ] Benchmark:
  - Embedding latency per document
  - Query latency (dense + sparse + RRF)
  - Reranking latency
- [ ] Target latencies:
  - Document embedding: < 500ms per document
  - Query: < 200ms
  - Reranking: < 100ms for top-10 results
- [ ] Commit: `test: Add performance benchmarks for RAG pipeline`

### Day 5-7: Cost Tracking & Observability

**Task 5.1: Cost Tracking Dashboard** [2 hours]
- [ ] File: `threadly-ai/app/routes/cost_tracking.py`
- [ ] Implement `/costs` endpoint:
  ```python
  @router.get("/costs")
  async def get_costs(
    bot_id: str,
    start_date: datetime,
    end_date: datetime
  ) -> CostSummaryResponse:
    # Return: total cost, per-model breakdown, per-user breakdown
  ```
- [ ] Store costs in costs table (already in database)
- [ ] Commit: `feat: Add cost tracking API endpoint`

**Task 5.2: Langfuse Integration Verification** [1 hour]
- [ ] Verify traces captured for:
  - Document ingestion
  - Query execution
  - LLM inference
  - Cost calculations
- [ ] Test: Create bot → ingest doc → query → verify in Langfuse UI
- [ ] Commit: `test: Verify Langfuse tracing integration`

**Deliverables:**
- ✅ Embedding model configured
- ✅ 0 lint violations
- ✅ 71%+ test coverage
- ✅ RAG pipeline E2E tested
- ✅ Performance benchmarks documented
- ✅ Cost tracking API ready

---

## 🟡 Testing/QA Agent [JUnit5, Pytest, Playwright]

### Day 1: Critical Tests

**Task 1.1: Tenant Isolation Tests** [3 hours]
- [ ] File: `services/threadly-core/src/test/java/.../TenantIsolationTest.java`
- [ ] Create test base class with org setup
- [ ] Write tests (see Phase 4 roadmap Task 1.4 for details):
  - orgACannotAccessOrgBsBots()
  - orgACannotAccessOrgBsConversations()
  - orgAApiKeyCannotAccessOrgBEndpoints()
  - tenantContextClearedAfterRequest()
  - botCreatedWithOrgIdNotAccessibleByOtherOrg()
- [ ] Verify all pass
- [ ] Commit: `test: Add comprehensive tenant isolation tests`

**Task 1.2: Idempotency Tests** [2 hours]
- [ ] Covered by Backend Agent
- [ ] Review & run: `mvn test -Dtest=IdempotencyIntegrationTest`
- [ ] Verify 100% pass

### Day 2: Quality Gate Tests

**Task 2.1: Fix Existing Test Failures** [2 hours]
- [ ] Run `mvn verify` and identify 67% passing integration tests
- [ ] Debug failing tests (likely Testcontainers issues)
- [ ] Fix database migration issues
- [ ] Target: 90%+ passing
- [ ] Commit: `fix: Resolve integration test failures`

**Task 2.2: Security Test Suite** [2 hours]
- [ ] File: `ThreadlySecurityTest.java`
- [ ] Test:
  - HMAC signature verification (valid/invalid signatures)
  - JWT token validation (expired, invalid signature)
  - CORS headers presence
  - SQL injection prevention (parameterized queries)
  - CSRF token handling
- [ ] Commit: `test: Add security-focused integration tests`

### Day 3-4: E2E Testing

**Task 3.1: E2E: Auth Flow** [2 hours]
- [ ] File: `tests/e2e/auth.spec.ts`
- [ ] Steps:
  1. Visit login page
  2. Enter credentials
  3. Submit form
  4. Redirect to dashboard
  5. Verify token stored in localStorage
  6. Verify API calls include Authorization header
  7. Logout, verify token cleared
- [ ] Run: `npm run e2e`
- [ ] Commit: `test: Add E2E auth flow tests`

**Task 3.2: E2E: Conversation Flow** [3 hours]
- [ ] File: `tests/e2e/conversations.spec.ts`
- [ ] Steps:
  1. Navigate to conversations page
  2. Open conversation
  3. Send message
  4. Verify message appears (real-time via Centrifugo)
  5. Test pagination/virtual scrolling
  6. Test search functionality
- [ ] Run: `npm run e2e`
- [ ] Commit: `test: Add E2E conversation flow tests`

**Task 3.3: E2E: KB Ingestion** [2 hours]
- [ ] File: `tests/e2e/knowledge.spec.ts`
- [ ] Steps:
  1. Navigate to knowledge base
  2. Upload document
  3. Wait for processing
  4. Search knowledge base
  5. Verify document appears in results
- [ ] Run: `npm run e2e`
- [ ] Commit: `test: Add E2E knowledge base flow tests`

### Day 5-7: Load Testing

**Task 5.1: Load Test Configuration** [2 hours]
- [ ] File: `infra/load-test/k6-script.js`
- [ ] Create k6 script (see Phase 4 roadmap for details)
- [ ] Define scenarios:
  - Ramp up: 0 → 100 concurrent over 2 min
  - Sustain: 100 concurrent for 5 min
  - Ramp down: 100 → 0 over 2 min
- [ ] Define thresholds:
  - p95 latency < 500ms
  - p99 latency < 1000ms
  - failure rate < 10%
- [ ] Commit: `test: Add load testing script for 100 concurrent users`

**Task 5.2: Run Load Test** [1 hour]
- [ ] Start all services (core, ai, widget, realtime)
- [ ] Run: `k6 run -e BASE_URL=http://localhost:8080 load-test/k6-script.js`
- [ ] Document results:
  - Actual p95, p99 latencies
  - Actual failure rate
  - Resource utilization (CPU, memory, database connections)
- [ ] Identify bottlenecks
- [ ] Commit: `docs: Document load testing results and findings`

**Deliverables:**
- ✅ Tenant isolation verified (critical security)
- ✅ Idempotency tested
- ✅ 90%+ test coverage (unit + integration)
- ✅ E2E tests passing (auth, conversations, KB)
- ✅ Load test: 100 concurrent, p95 < 500ms documented

---

## 🔴 Tech Lead Agent [Infrastructure/DevOps]

### Day 1: Dependency & Kubernetes

**Task 1.1: CVE Audit & Fixes** [1 hour]
- [ ] Run `mvn dependency-check:check`
- [ ] Work with Backend Agent to resolve CVE in Jackson
- [ ] Verify all CVEs cleared
- [ ] Commit: `chore: Audit and fix all dependency CVEs`

**Task 1.2: Kubernetes Manifests Finalization** [2 hours]
- [ ] Review `infrastructure/kubernetes/05-microservices-deployments-complete.yaml`
- [ ] Verify all 9 services have proper:
  - Resource requests/limits
  - Health checks (liveness, readiness probes)
  - Environment variables
  - Service routing
- [ ] Finalize `06-ingress-controller.yaml` and `07-ingress-routes.yaml`
- [ ] Test locally: `kubectl apply -f infrastructure/kubernetes/`
- [ ] Commit: `chore: Finalize Kubernetes manifests and ingress configuration`

### Day 2: Configuration & Monitoring

**Task 2.1: Production Configuration** [1 hour]
- [ ] File: `threadly-core/src/main/resources/application-prod.yml`
- [ ] Ensure prod profile has:
  - Proper database connection pooling for load
  - Redis cluster configuration
  - Logging level (INFO, not DEBUG)
  - Security headers enabled
  - CORS configured
- [ ] Commit: `chore: Configure production application profile`

**Task 2.2: Grafana Dashboard** [2 hours]
- [ ] File: `infra/grafana/dashboards/threadly-prod.json`
- [ ] Create dashboard with:
  - Request rate (requests/sec)
  - p95/p99 latency
  - Error rate (%)
  - Database connection pool usage
  - Redis memory usage
  - JVM heap memory
  - Pod CPU/memory usage
- [ ] Test: Verify metrics appear in Grafana UI
- [ ] Commit: `feat: Add comprehensive Grafana dashboard for production monitoring`

### Day 3-4: Load Testing & Performance

**Task 3.1: Load Test Infrastructure** [1 hour]
- [ ] Ensure all services support horizontal scaling
- [ ] Configure auto-scaling in Kubernetes (if using managed K8s)
- [ ] Verify load balancer distributes traffic across replicas
- [ ] Commit: `chore: Configure load balancing and auto-scaling`

**Task 3.2: Database Optimization** [2 hours]
- [ ] Review Flyway migrations for missing indexes
- [ ] Create indexes on frequently queried columns:
  - `conversations(org_id, bot_id, created_at)`
  - `messages(conversation_id, created_at)`
  - `kb_documents(bot_id, status)`
- [ ] Verify query plans with EXPLAIN ANALYZE
- [ ] Commit: `perf: Add database indexes for query optimization`

### Day 5-7: Deployment & Validation

**Task 5.1: Railway Configuration** [1 hour]
- [ ] File: `railway.toml`
- [ ] Configure all services for Railway deployment
- [ ] Set environment variables from Railway secrets:
  - Database URL
  - Redis URL
  - JWT secret
  - API keys for providers (Anthropic, OpenAI)
  - Centrifugo secret
- [ ] Commit: `chore: Configure Railway deployment environment`

**Task 5.2: Staging Deployment** [2 hours]
- [ ] Deploy to Railway staging environment
- [ ] Run smoke tests:
  ```bash
  curl https://threadly-staging.railway.app/v1/health
  curl https://threadly-staging.railway.app/v1/bots
  ```
- [ ] Verify Centrifugo connects
- [ ] Test KB ingestion end-to-end
- [ ] Document deployment steps
- [ ] Commit: `docs: Document Railway staging deployment and smoke tests`

**Task 5.3: Rollback Plan** [1 hour]
- [ ] Document steps to rollback in production
- [ ] Database: Keep migrations backward-compatible
- [ ] API: Support v1 and v2 endpoints during transition
- [ ] Services: Keep previous version deployable for 24 hours
- [ ] File: `DEPLOYMENT.md` with rollback section
- [ ] Commit: `docs: Document production rollback procedures`

**Deliverables:**
- ✅ All CVEs resolved
- ✅ Kubernetes manifests production-ready
- ✅ Grafana dashboard operational
- ✅ Database indexes optimized
- ✅ Railway staging deployed & validated
- ✅ Rollback plan documented

---

## 📊 Product Agent [Documentation & Roadmap]

### Day 1-2: Feature Registry

**Task 1.1: Update PRODUCT_STATUS.md** [2 hours]
- [ ] Mark completed features from Sprint 3
- [ ] Document new features added in Phase 4:
  - Springdoc OpenAPI auto-generation
  - Idempotency with Redis
  - Tenant isolation guarantees
  - Load test results (100 concurrent)
- [ ] Update feature matrix: % complete for each service
- [ ] Commit: `docs: Update feature registry for Phase 4 completion`

**Task 1.2: Write FEATURES.md** [2 hours]
- [ ] Document all implemented features with user stories
- [ ] Format:
  ```
  ## Feature: Message Idempotency
  **User Story:** As a mobile user with unreliable connection, I want messages
  to not duplicate when I retry sending

  **Implementation:** X-Idempotency-Key header, Redis cache, TTL 1 hour
  **Status:** ✅ Complete
  **Owner:** Backend Agent
  ```
- [ ] Commit: `docs: Add feature registry with user stories`

### Day 3-4: Sprint Documentation

**Task 3.1: Update CHANGELOG.md** [1 hour]
- [ ] Document all changes from Phase 4:
  - API: Springdoc OpenAPI generation
  - API: Idempotency endpoints
  - Infrastructure: Kubernetes finalizations
  - Testing: Tenant isolation, E2E, load tests
- [ ] Format:
  ```
  ## [1.0.0] - 2026-05-31
  ### Added
  - OpenAPI spec auto-generation via Springdoc
  - Idempotency support for POST endpoints
  ...
  ```
- [ ] Commit: `docs: Add Phase 4 changelog`

**Task 3.2: Update AGENTS.md** [1 hour]
- [ ] Verify agent responsibilities match actual implementation
- [ ] Update key files section with new files added
- [ ] Update owned tables with any schema changes
- [ ] Document inter-agent contracts verified in Phase 4
- [ ] Commit: `docs: Update AGENTS.md with Phase 4 architecture`

### Day 5-7: Launch Preparation

**Task 5.1: PRODUCT_LAUNCH_CHECKLIST.md** [2 hours]
- [ ] Create go/no-go checklist:
  - [x] API contracts finalized & tested
  - [x] Idempotency implemented
  - [x] Tenant isolation verified
  - [x] 70%+ test coverage achieved
  - [x] Load test: 100 concurrent, p95 < 500ms
  - [x] E2E tests passing
  - [x] Monitoring dashboards ready
  - [x] Rollback plan documented
  - [x] Staging deployment validated
- [ ] Sign-off template:
  ```
  ## Sign-Off

  **Backend Agent:** ✅ All features implemented, tests passing
  **Frontend Agent:** ✅ UI complete, E2E tests passing
  **AI Agent:** ✅ RAG pipeline optimized, tests passing
  **Testing Agent:** ✅ 90%+ coverage, load test successful
  **Tech Lead:** ✅ Infrastructure ready, monitoring operational
  **Product:** ✅ Feature complete, documentation current

  **Launch Decision:** ✅ READY FOR PRODUCTION
  **Date:** 2026-05-31
  ```
- [ ] Commit: `docs: Add product launch checklist`

**Task 5.2: User Guide & API Documentation** [2 hours]
- [ ] Create quick-start guide for widget integration
- [ ] Document API authentication (JWT, API keys)
- [ ] Document webhook registration & retry behavior
- [ ] Create troubleshooting guide for common issues
- [ ] Files: `docs/user-guide/`, `docs/api/`
- [ ] Commit: `docs: Add user guide and API documentation`

**Task 5.3: Release Notes** [1 hour]
- [ ] File: `RELEASE_NOTES_v1.0.md`
- [ ] Sections:
  - What's new (all features)
  - Breaking changes (none expected)
  - Migration guide (from monolith)
  - Known limitations
  - Roadmap (Sprint 4 features)
- [ ] Commit: `docs: Add version 1.0.0 release notes`

**Deliverables:**
- ✅ Feature registry updated
- ✅ CHANGELOG documenting Phase 4
- ✅ AGENTS.md reflects final architecture
- ✅ Launch checklist & sign-off template ready
- ✅ User guide & API docs complete
- ✅ Release notes published

---

## 📅 Milestone Timeline

| Day | Milestone | Owner(s) | Status |
|-----|-----------|----------|--------|
| 1 (May 24) | 🔴 Critical blockers fixed | Backend, Frontend, Testing | Targeted |
| 2 (May 25) | ✅ Code quality gates passing | Backend, Frontend, AI | Targeted |
| 3-4 (May 26-27) | ✅ Integration tests passing | Testing, Backend, Frontend | Targeted |
| 5-7 (May 28-31) | ✅ Load test, staging, production ready | All agents | Targeted |

---

## Daily Standup Template

**Time:** 9 AM UTC
**Format:** 2 min per agent
**Async:** Slack #threadly-phase4-standup

```
🔵 Backend: [feature] done, [next] in progress, [blocker] none
🟢 Frontend: [feature] done, [next] in progress, [blocker] none
🟣 AI: [feature] done, [next] in progress, [blocker] none
🟡 Testing: [feature] done, [next] in progress, [blocker] none
🔴 Tech Lead: [feature] done, [next] in progress, [blocker] none
📊 Product: [feature] done, [next] in progress, [blocker] none
```

---

## Success Criteria Summary

✅ All 3 blockers fixed by EOD Day 1
✅ Code quality gates green by EOD Day 2
✅ Integration tests passing by EOD Day 4
✅ Load test successful by EOD Day 7
✅ Staging deployment validated by EOD Day 7
✅ Ready for production launch by May 31

