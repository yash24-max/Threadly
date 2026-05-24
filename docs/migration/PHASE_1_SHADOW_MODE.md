# Phase 1: Shadow Mode — Parallel Read-Only Deployment

**Duration**: Week 1 (5 days)  
**Status**: Read-only services running alongside monolith  
**Risk Level**: Low (can roll back anytime)  
**Success Criteria**: All 9 services healthy, data consistency verified, integration tests pass

---

## Overview

In Phase 1, all 9 microservices are deployed in **read-only mode** alongside the existing monolith. The monolith remains the source of truth for all writes. Services receive a copy of the data and validate it matches.

**Key characteristics**:
- Monolith handles ALL reads and writes
- Services receive data replicated from monolith
- No writes to services (read-only SQL views or replicated tables)
- All Kafka topics mirror monolith events
- Validates data consistency before proceeding

---

## Timeline

### Day 1-2: Preparation & Dry Runs

**Pre-flight Checks** (all in parallel):
```bash
# 1. Verify all 9 service Docker images are built
mvn clean package -DskipTests -f services/pom.xml
docker images | grep threadly-

# Expected output:
# threadly/identity-service             latest
# threadly/workspace-service            latest
# threadly/flow-service                 latest
# threadly/runtime-service              latest
# threadly/conversation-service         latest
# threadly/knowledge-service            latest
# threadly/analytics-service            latest
# threadly/billing-service              latest
# threadly/integration-service          latest

# 2. Verify database schemas created
docker exec postgres psql -U threadly -c "
  SELECT schema_name FROM information_schema.schemata 
  WHERE schema_name LIKE '%_service'
  ORDER BY schema_name;
"

# 3. Verify infrastructure ready (Docker Compose or K8s)
docker-compose -f infrastructure/docker/docker-compose.yml ps | grep -E "postgres|kafka|redis|consul"

# 4. Verify on-call engineer trained
- [ ] Reviewed RUNBOOK_MIGRATION.md
- [ ] Reviewed RUNBOOK_ROLLBACK.md
- [ ] Walked through dry-run with team lead
- [ ] Knows escalation path
```

**Dry-Run Data Migration**:
```bash
# Run the migration script in dry-run mode (no commit)
bash scripts/migrate-monolith-to-services.sh phase1-dry-run

# Expected output:
# [INFO] Phase 1 Dry Run - Shadow Mode Data Migration
# [INFO] Copying users to identity_service.users...
#   Monolith: 1250 rows | Services: 1250 rows ✓
# [INFO] Copying organizations to workspace_service...
#   Monolith: 342 rows | Services: 342 rows ✓
# [INFO] Copying flows to flow_service...
#   Monolith: 2891 rows | Services: 2891 rows ✓
# ... (all 9 services)
# [INFO] Dry-run complete. 0 errors, ready to proceed.
```

**Verify Row Counts**:
```bash
# Check all service schemas are created
docker exec postgres psql -U threadly << 'EOF'
SELECT schema_name, COUNT(*) as table_count
FROM information_schema.tables
WHERE table_schema IN (
  'identity_service', 'workspace_service', 'flow_service',
  'runtime_service', 'conversation_service', 'knowledge_service',
  'analytics_service', 'billing_service', 'integration_service'
)
GROUP BY schema_name
ORDER BY schema_name;
EOF
```

### Day 3: Deploy Services

**Start Infrastructure**:
```bash
# Start all supporting services (Kafka, PostgreSQL, Redis, etc.)
cd infrastructure/docker
docker-compose up -d postgres kafka redis consul qdrant elasticsearch
docker-compose ps

# Wait for all to be healthy (2-3 min)
docker-compose logs -f | grep -E "healthy|ready"
```

**Initialize Database Schemas & Data**:
```bash
# Copy schema and data from monolith to service schemas
bash scripts/migrate-monolith-to-services.sh phase1

# Verify row counts match exactly
bash scripts/validate-data-consistency.sh phase1

# Expected: 100% match on all tables
```

**Deploy All 9 Services** (in parallel):
```bash
# Option A: Docker Compose (local development)
docker-compose -f infrastructure/docker/docker-compose.yml up -d \
  identity-service workspace-service flow-service runtime-service \
  conversation-service knowledge-service analytics-service \
  billing-service integration-service

# Option B: Kubernetes (staging/prod)
kubectl apply -f infrastructure/kubernetes/namespace.yaml
kubectl apply -f infrastructure/kubernetes/configmap.yaml
kubectl apply -f infrastructure/kubernetes/secrets.yaml
kubectl apply -f infrastructure/kubernetes/service-deployments/

# Verify all services are ready
kubectl rollout status deployment/identity-service -n threadly --timeout=5m
kubectl rollout status deployment/workspace-service -n threadly --timeout=5m
# ... (repeat for all 9)
```

**Health Checks** (all must return HTTP 200):
```bash
# Check all service health endpoints
for service in identity workspace flow runtime conversation knowledge analytics billing integration; do
  echo "Checking $service-service..."
  curl -s http://localhost:$(echo $service | sed 's/identity/3001/;s/workspace/3002/;s/flow/3003/;s/runtime/3004/;s/conversation/3005/;s/knowledge/3006/;s/analytics/3007/;s/billing/3008/;s/integration/3009/')/health | jq .
done

# Expected: All return {"status": "UP"} or similar healthy state
```

### Day 4-5: Integration Tests & Validation

**Cross-Service Integration Tests**:
```bash
# Run integration test suite (includes data consistency checks)
mvn -f services/threadly-common-spring/pom.xml verify -DskipITs=false

# Expected output:
# [INFO] Tests run: 48
# [INFO] Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS

# Check specific test results
mvn -f services/threadly-common-spring/pom.xml test-report
```

**Smoke Tests** (verify each service can read data):
```bash
# Generate test JWT token
TEST_JWT=$(curl -s -X POST http://localhost:3001/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@threadly.dev","password":"Test123!"}' \
  | jq -r '.token')

# Test each service
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3001/api/me
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3002/api/bots
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3003/api/flows
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3004/api/sessions
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3005/api/conversations
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3006/api/knowledge-bases
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3007/api/analytics
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3008/api/subscriptions
curl -H "Authorization: Bearer $TEST_JWT" http://localhost:3009/api/integrations

# Expected: All return 200 OK with data
```

**Data Consistency Validation** (comprehensive check):
```bash
# Run the validation script
bash scripts/validate-data-consistency.sh phase1-detailed

# Check specific consistency issues
# 1. Orphaned records (flows without bots)
docker exec postgres psql -U threadly << 'EOF'
SELECT f.id, f.bot_id 
FROM flow_service.flows f 
LEFT JOIN workspace_service.bots b ON f.bot_id = b.id 
WHERE b.id IS NULL;
EOF

# 2. Duplicate checks (should be 0)
SELECT table_name, COUNT(*) - COUNT(DISTINCT id) as duplicates
FROM information_schema.tables t
WHERE table_schema LIKE '%_service'
GROUP BY table_name
HAVING COUNT(*) > COUNT(DISTINCT id);

# Expected: No orphaned records, no duplicates
```

**Performance Baseline** (24-hour monitoring):
```bash
# Start load test in background
ab -n 10000 -c 50 http://localhost:8080/api/bots -H "Authorization: Bearer $TEST_JWT" &

# Monitor in Grafana
# Dashboard: "Threadly Microservices Migration"
# Panels to watch:
#   - Service Health Status (all 9 should be green)
#   - Request Latency (p95, p99 baseline)
#   - Error Rate (should be 0%)
#   - Kafka Topic Lag (should be near 0)
#   - Database Query Performance

# Let run for 24 hours, then:
# - Check for any anomalies
# - Record p50, p95, p99 latencies
# - Verify no memory leaks (check JVM heap over 24h)
# - Verify graceful handling of errors
```

---

## Sign-Off Checklist

Before proceeding to Phase 2, verify:

- [ ] All 9 microservices deployed and passing health checks
- [ ] Data migration completed with 100% row count verification
- [ ] Integration test suite passes (0 failures)
- [ ] Smoke tests pass for all 9 services
- [ ] Data consistency checks pass (0 orphans, 0 duplicates)
- [ ] Performance baseline recorded (p95/p99 latencies)
- [ ] 24-hour monitoring completed (no anomalies)
- [ ] Runbook walkthrough completed with team
- [ ] On-call engineer ready for Phase 2
- [ ] Team lead approval to proceed

---

## Success Metrics

Phase 1 is successful when:
1. **Availability**: 99.95%+ uptime (0 service outages)
2. **Data Integrity**: 100% row count match, 0 orphaned records
3. **Performance**: <5% increase from baseline
4. **Test Coverage**: All integration tests pass
5. **Team Readiness**: Team comfortable with procedures

---

## Rollback Procedure (if needed)

If Phase 1 fails, rollback is simple:
```bash
# 1. Stop services (they're read-only, no data at risk)
docker-compose down

# 2. Verify monolith still healthy
curl http://localhost:8000/health

# 3. No data loss (monolith was never modified)
```

---

## Next Steps

Once Phase 1 sign-off is complete:
- Notify tech lead (@yasva)
- Schedule Phase 2 (dual-write mode) for the following week
- Brief incident commander on status
- Update PRODUCT_STATUS.md with Phase 1 completion

See **PHASE_2_DUAL_WRITE.md** for next steps.
