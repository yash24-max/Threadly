# Microservices Migration Deployment Plan

**Project**: Threadly Monolith → 9 Microservices  
**Timeline**: 4 weeks (Week 1-4 as of May 27, 2025)  
**Status**: Ready for Phase 1 launch  
**Success Criteria**: 0 data loss, <5% latency increase, 99.5% uptime, zero customer-facing downtime  

---

## Executive Summary

Threadly will transition from a modular monolith to 9 independent microservices deployed via Kubernetes/Docker Compose. The migration is **zero-downtime** using shadow mode + dual-write + gradual cutover.

| Phase | Duration | Risk | Rollback Window |
|-------|----------|------|-----------------|
| 1: Shadow | Week 1 | Low | Unlimited |
| 2: Dual-Write | Week 2 | Medium | Unlimited |
| 3: Cutover | Week 3 | High | 7 days post-cutover |
| 4: Observe | Week 4 | Low | N/A (monolith decommissioned) |

---

## Week 1: Phase 1 — Shadow Mode

**Goal**: Deploy all 9 services read-only alongside monolith. Validate data consistency.

### Monday-Tuesday: Preparation

```bash
# 1. Pre-flight checks (in parallel)
- [ ] All 9 service Docker images built (mvn spring-boot:build-image)
- [ ] PostgreSQL with 9 service schemas created
- [ ] Redis, Kafka, Qdrant, Consul deployed (docker-compose)
- [ ] Prometheus + Grafana stacks online
- [ ] On-call engineer trained on runbooks
- [ ] Rollback playbook reviewed + approved

# 2. Dry-run data migration
bash scripts/migrate-monolith-to-services.sh phase1

# Expected output:
# [INFO] Copying users to identity_service.users...
#   Before: 1250 | After: 1250
# [INFO] Copying bots to workspace_service.bots...
#   Before: 456 | After: 456
# ...

# 3. Verify row counts match
docker exec postgres psql -U threadly -c "
  SELECT schema_name, COUNT(*) as table_count
  FROM information_schema.tables
  WHERE table_schema IN (
    'identity_service', 'workspace_service', 'flow_service',
    'runtime_service', 'conversation_service', 'knowledge_service',
    'analytics_service', 'billing_service', 'integration_service'
  )
  GROUP BY schema_name;
"
```

### Wednesday: Deploy Services

```bash
# 1. Start all services in read-only mode
make up

# 2. Health checks (all must return 200)
for port in 3001 3002 3003 3004 3005 3006 3007 3008 3009; do
  curl http://localhost:$port/health || echo "FAILED"
done

# 3. Smoke tests (verify services can read data)
curl http://localhost:3001/me -H "Authorization: Bearer $TEST_JWT"
curl http://localhost:3002/bots -H "Authorization: Bearer $TEST_JWT"
curl http://localhost:3003/flows -H "Authorization: Bearer $TEST_JWT"

# Expected: All return 200 with data from copied schemas
```

### Thursday-Friday: Integration Tests & Validation

```bash
# 1. Run cross-service integration tests
mvn -f threadly-common-spring/pom.xml verify -DskipITs=false

# Expected:
# [INFO] Tests run: 48
# [INFO] Failures: 0, Errors: 0, Skipped: 0

# 2. Check data consistency
# Script: validate_data_consistency.sh
bash scripts/validate-data-consistency.sh

# 3. Monitor metrics for 24 hours
# Dashboard: Threadly Microservices Migration Dashboard
# Alert on: dual_write_failures, kafka_lag, error_rate

# 4. Performance baseline (shadow reads)
ab -n 1000 -c 10 http://localhost:3001/bots
# Record p50, p95, p99 latency for Phase 2 comparison
```

### Sign-Off: Phase 1 Complete
- [ ] All services deployed and healthy
- [ ] Data row counts verified (100% match)
- [ ] Integration tests pass (0 failures)
- [ ] Team has conducted runbook walkthrough
- [ ] **Decision**: Proceed to Phase 2 (dual-write)

---

## Week 2: Phase 2 — Dual-Write Mode

**Goal**: Enable writes to BOTH monolith (primary) + services (shadow). Monitor for consistency gaps.

### Monday: Enable Dual-Write Configuration

```bash
# 1. Update application.properties in threadly-common-spring
# File: threadly-common-spring/src/main/resources/application.properties
cat >> application.properties << 'EOF'
migration.dual.write.enabled=true
migration.dual.write.fail.strategy=log_only
migration.dual.write.timeout.ms=5000
migration.dual.write.service.base.url=http://api-gateway:8080
EOF

# 2. Rebuild monolith and redeploy
mvn -f threadly-core/pom.xml clean package -DskipTests
docker build -t threadly-monolith:phase2 .
docker run -d \
  --name monolith \
  -e SPRING_PROFILES_ACTIVE=dual-write \
  threadly-monolith:phase2

# 3. Verify dual-write is active
curl http://localhost:8000/health/dual-write
# Expected: {"status": "enabled", "lag_ms": 45, "failures": 0}
```

### Tuesday-Wednesday: Gradual Traffic Ramp

```bash
# 1. Start with 5% of write traffic
# Nginx/Kong configuration:
upstream monolith {
  server monolith:8000 weight=95;  # 95% primary
}
upstream services {
  server api-gateway:8080 weight=5; # 5% shadow (dual-write)
}

# 2. Monitor metrics closely
# Watch in Grafana:
# - dual_write_lag_ms{service} should be < 500ms
# - dual_write_failures_total{service} should be ~0
# - kafka_consumer_lag should stay < 30s

# 3. Alert thresholds during Phase 2
# - dual_write_lag > 1000ms → WARNING
# - dual_write_failures > 10/min → CRITICAL (page on-call)
# - kafka_lag > 60s → WARNING
```

### Thursday: 25% Traffic Ramp

```bash
# Increase shadow traffic to 25% after Tuesday/Wednesday validation
# Update Kong/Nginx routing:
upstream monolith {
  server monolith:8000 weight=75;
}
upstream services {
  server api-gateway:8080 weight=25;
}

# Re-check metrics
# Accept higher lag but failures must stay near 0
```

### Friday: 50% Traffic Ramp + Synthetic Testing

```bash
# Increase to 50% traffic
upstream monolith {
  server monolith:8000 weight=50;
}
upstream services {
  server api-gateway:8080 weight=50;
}

# Run continuous synthetic tests
./scripts/synthetic_tests.sh &  # Background

# Create bot via API, verify in both monolith + service databases
CREATE_BOT_ID=$(curl -X POST http://localhost:8080/api/bots \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"TestBot_$(date +%s)\"}" | jq -r '.id')

# Verify in monolith
curl http://localhost:8000/api/bots/$CREATE_BOT_ID | jq '.name'

# Verify in services
curl http://localhost:3002/api/bots/$CREATE_BOT_ID | jq '.name'

# Both must return same data
```

### Sign-Off: Phase 2 Complete
- [ ] Dual-write enabled and stable (lag < 500ms, failures < 1/min)
- [ ] 50%+ traffic flowing through services without errors
- [ ] Kafka consumer lag stable (< 30s on all topics)
- [ ] Synthetic tests pass hourly
- [ ] **Decision**: Proceed to Phase 3 (cutover)

---

## Week 3: Phase 3 — Cutover & Validation

**Goal**: Switch all traffic to microservices. Keep monolith in read-only maintenance mode for 7 days.

### Monday: Pre-Cutover Checklist

```bash
# 1. Health checks on all 9 services
for service in identity workspace flow runtime conversation knowledge analytics billing integration; do
  curl http://$service-service:300X/health || echo "FAILED: $service"
done

# 2. Verify database integrity
docker exec postgres psql -U threadly -c "
  SELECT tablename FROM pg_tables
  WHERE schemaname IN (
    'identity_service', 'workspace_service', 'flow_service',
    'runtime_service', 'conversation_service', 'knowledge_service',
    'analytics_service', 'billing_service', 'integration_service'
  )
  ORDER BY tablename;
" | wc -l
# Expected: ~80+ tables across all schemas

# 3. Check Kafka consumer lag
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --all-groups \
  --describe | grep LAG
# All LAG values must be < 10s

# 4. Backup monolith database (just in case)
docker exec postgres pg_dump -U threadly -F c > /tmp/monolith_backup_$(date +%Y%m%d_%H%M%S).dump
ls -lh /tmp/monolith_backup_*.dump
```

### Tuesday: Switch Traffic to Microservices (No Downtime)

```bash
# 1. Update Nginx/Kong routing (gradual)
# 11:00 AM: 90% services, 10% monolith
upstream monolith {
  server monolith:8000 weight=10;
}
upstream services {
  server api-gateway:8080 weight=90;
}
systemctl reload nginx  # No downtime

# Monitor metrics for 10 minutes
# Expected: Error rate stays < 0.5%, latency p95 < 100ms

# 11:15 AM: 95% services, 5% monolith
# 11:30 AM: 100% services (cutover complete)

upstream services {
  server api-gateway:8080;
}
systemctl reload nginx

echo "✓ Cutover complete - all traffic to microservices"
```

### Tuesday-Wednesday: Set Monolith to Read-Only

```bash
# 1. Enable read-only mode on monolith
docker exec monolith bash -c "
  echo 'server.read.only=true' >> /app/application.properties
  kill -HUP 1  # Reload Spring Boot without downtime
"

# 2. Verify write operations fail gracefully
curl -X POST http://localhost:8000/api/bots \
  -d '{"name": "TestBot"}' \
  -H "Content-Type: application/json"
# Expected: 503 Service Unavailable (not 500 Error)

# 3. Verify read operations still work
curl http://localhost:8000/api/bots
# Expected: 200 OK with bot list
```

### Thursday-Friday: Validation & Stress Testing

```bash
# 1. Run full integration test suite
mvn -f threadly-core/pom.xml verify -DskipITs=false

# 2. End-to-end test scenarios
./tests/e2e/create_bot_and_flow.sh
./tests/e2e/send_message_and_track.sh
./tests/e2e/upload_kb_and_query.sh

# 3. Load test (1000 req/sec for 30 min)
wrk -t 4 -c 100 -d 30m --script=tests/load/bots_read.lua \
  http://localhost:8080/api/bots

# Expected:
# Requests/sec: 1000+
# Latency p99: < 200ms
# Errors: < 0.1%

# 4. Database consistency check
SELECT * FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;
# Verify row counts stable (no unexplained deletes)
```

### Sign-Off: Phase 3 Complete
- [ ] 100% traffic on microservices (monolith read-only)
- [ ] All health checks pass
- [ ] Integration tests pass (0 failures)
- [ ] Load test passes (p99 < 200ms, errors < 0.1%)
- [ ] Kafka lag near 0 (events processed)
- [ ] No customer complaints reported
- [ ] **Decision**: Phase 4 observation + monolith decommission

---

## Week 4: Phase 4 — Observation & Decommission

### Monday-Thursday: Monitor Stability (Observation)

```bash
# 1. Daily health checks
# - All 9 services /health → 200
# - Kafka consumer lag < 10s
# - Error rate < 0.1%
# - p99 latency < 150ms

# 2. Analytics review
# - Conversation count trending normally
# - API call volume stable
# - No unusual errors in logs

# 3. Customer feedback
# - Monitor support tickets for migration-related issues
# - Monitor Twitter/Slack mentions
# - Validate bot responses are consistent
```

### Thursday: Decommission Monolith (Optional - Can Postpone)

```bash
# 1. Final backup of monolith
docker exec postgres pg_dump -U threadly -F c > /tmp/monolith_final_backup.dump

# 2. Archive monolith codebase
git tag -a v1.0-monolith -m "Pre-microservices version"

# 3. Disable monolith container (don't delete yet)
docker stop monolith
docker update --restart=no monolith

# 4. Update documentation
# - Remove monolith setup instructions
# - Update dev-setup.md to reference microservices
# - Archive docs/old/ with monolith architecture

# 5. Communicate to team
# - Email: Monolith decommissioned, all traffic on microservices
# - Wiki: Update team runbooks
# - Slack: #announcements post

echo "✓ Monolith decommissioned - Phase 4 complete"
```

### Sign-Off: Migration Complete
- [ ] 7+ days observation window passed
- [ ] Zero customer-reported issues
- [ ] All success metrics met:
  - [ ] 0 data loss
  - [ ] Latency increase < 5% (from baseline)
  - [ ] Uptime >= 99.5%
  - [ ] Zero unplanned downtime
- [ ] Monolith archived + decommissioned
- [ ] Team trained on new architecture
- [ ] Runbooks documented

---

## Rollback Decision Criteria

### Automatic Rollback Triggers
If **any** of these occur during Phase 2-3:
1. Customer-facing downtime > 5 minutes
2. Data loss detected (row counts don't match)
3. Error rate > 5% on any service
4. Kafka consumer lag > 5 minutes
5. Database corruption detected

### Manual Rollback Decision
Tech Lead + Team decision based on:
- Severity of failure
- Time to fix vs. time to rollback
- Customer impact
- Data consistency risk

**Rollback Timeline**: 15-30 minutes (see RUNBOOK_ROLLBACK.md)

---

## Success Metrics

### Primary Metrics (Must-Haves)
| Metric | Target | Measurement |
|--------|--------|-------------|
| Data Loss | 0 rows | Row count diff across all tables |
| Unplanned Downtime | 0 minutes | Customer-facing outages |
| Latency Increase | < 5% | p99 latency (Phase 3 vs. Phase 1) |
| Uptime | >= 99.5% | Availability during migration |

### Secondary Metrics (Nice-to-Haves)
| Metric | Target |
|--------|--------|
| Dual-Write Lag | < 500ms avg |
| Kafka Lag | < 10s |
| Error Rate | < 0.1% |
| Service P99 Latency | < 150ms |

### Post-Migration SLOs
| Service | Availability | Latency P99 | Error Rate |
|---------|--------------|-------------|-----------|
| Identity | 99.9% | 50ms | 0.01% |
| Workspace | 99.95% | 100ms | 0.02% |
| Flow | 99.9% | 120ms | 0.02% |
| Runtime | 99.99% | 80ms | 0.01% |
| Conversation | 99.95% | 150ms | 0.02% |
| Knowledge | 99.9% | 200ms | 0.05% |
| Analytics | 99.5% | 500ms | 0.1% |
| Billing | 99.99% | 100ms | 0.01% |
| Integration | 99.9% | 300ms | 0.05% |

---

## Team & On-Call

### Roles & Responsibilities

| Role | Name | Contact |
|------|------|---------|
| **Tech Lead** | Yasva | @yasva, yasva@threadly.dev |
| **Infra Lead** | [TBD] | Slack: #infrastructure |
| **Database Admin** | [TBD] | Slack: #database |
| **On-Call Engineer** | Rotation | PagerDuty |
| **Product Lead** | [TBD] | Slack: #product |

### Escalation Path
1. **First Response** (On-Call): Declare incident, page Tech Lead
2. **Tech Lead** (15 min): Assess severity, decide rollback
3. **CEO** (if > 1 hour downtime): Notify @yasva
4. **Support** (parallel): Update customers

### Communication Channels
- **Real-time**: #incident Slack (all updates)
- **Status**: statuspage.io (customer-facing)
- **Alerts**: PagerDuty (on-call)
- **Post-mortem**: Internal wiki + team meeting

---

## Risk Mitigation

### Risk: Data Inconsistency
**Mitigation**:
- Phase 1: Shadow copy + validate row counts
- Phase 2: Dual-write + reconciliation checks
- Phase 3: Kafka event replay if needed
- **Backup**: Daily SQL dumps of all schemas

### Risk: Service Latency Increase
**Mitigation**:
- Performance baseline established in Week 1
- Load testing (1000 req/sec) before cutover
- Circuit breakers + timeouts configured
- **Rollback threshold**: p99 > baseline + 5%

### Risk: Kafka Consumer Lag
**Mitigation**:
- Consumer groups health monitored hourly
- DLQ backlog checked before cutover
- Lag recovery script prepared (see RUNBOOK_KAFKA_RECOVERY.md)
- **Rollback threshold**: Lag > 5 minutes for > 10 minutes

### Risk: Monolith → Service API Differences
**Mitigation**:
- API contract tests in Phase 1
- Mock service responses in integration tests
- Canary deployment (5% traffic initially)
- **Rollback threshold**: Error rate > 1% on read paths

---

## Documentation References

- **Architecture**: docs/18-microservices-architecture.md
- **Migration Steps**: RUNBOOK_MIGRATION.md
- **Kafka Troubleshooting**: RUNBOOK_KAFKA_RECOVERY.md
- **Emergency Rollback**: RUNBOOK_ROLLBACK.md
- **Service Restart**: RUNBOOK_SERVICE_RESTART.md
- **Monitoring**: monitoring/grafana-migration-dashboard.json

---

## Go/No-Go Decision Points

### Phase 1 → Phase 2 (Friday, June 6)
**Go if:**
- [ ] All 9 services deployed and healthy
- [ ] Data row counts 100% match
- [ ] Integration tests pass
- [ ] Team trained on Phase 2 procedures

**No-Go if:**
- [ ] Any service failing health checks
- [ ] Data loss detected (row count mismatch)
- [ ] Integration test failures
- [ ] Unresolved critical bugs

### Phase 2 → Phase 3 (Friday, June 13)
**Go if:**
- [ ] Dual-write stable for 5+ days
- [ ] Dual-write lag < 500ms avg
- [ ] Dual-write failures < 1/min
- [ ] All services tested at 50% load
- [ ] Kafka lag < 30s consistently

**No-Go if:**
- [ ] Dual-write lag > 1000ms
- [ ] Service failures under load
- [ ] Kafka rebalancing issues
- [ ] Data consistency gaps detected

### Phase 3 → Phase 4 (Friday, June 20)
**Go if:**
- [ ] 48+ hours 100% traffic on services
- [ ] Zero data loss confirmed
- [ ] Error rate < 0.5%
- [ ] All integration tests pass
- [ ] Customer satisfaction maintained

**No-Go if:**
- [ ] Data loss discovered
- [ ] Service failures > 1% of requests
- [ ] Customers report issues
- [ ] Undefined behavior in edge cases

---

## Post-Migration Roadmap

Once Phase 4 complete:

1. **Service-to-Service Communication** (Week 5-6)
   - Replace Feign clients with gRPC for internal calls
   - Reduce latency for inter-service hops

2. **Event-Driven Workflows** (Week 7-8)
   - Saga orchestration for multi-step operations
   - Compensation transactions for failures

3. **Observability Enhancements** (Week 9)
   - Distributed tracing full coverage
   - Service mesh (Istio) for traffic management

4. **Scaling & Auto-Scaling** (Week 10+)
   - Horizontal pod autoscaling on Kubernetes
   - Database sharding strategy per service

---

**Document Version**: 1.0  
**Last Updated**: 2025-05-24  
**Next Review**: 2025-06-01 (pre-Phase 2)
