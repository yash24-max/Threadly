# Runbook: Microservices Migration Process

**Status**: Phase-based migration from monolith to 9 microservices  
**Timeline**: 3 weeks + 1 week observation = 4 weeks total  
**Rollback Window**: 7 days (full rollback possible), then risky  
**Success Metric**: 0 data loss, <5% latency increase, 99.5% uptime  

---

## Overview

Migration executes in 3 phases:
1. **Phase 1 (Week 1)**: Shadow Mode — Copy data, verify consistency
2. **Phase 2 (Week 2)**: Dual-Write Mode — Write to both monolith + services
3. **Phase 3 (Week 3)**: Cutover — All traffic → microservices
4. **Week 4**: Observation + Decommission monolith

---

## Phase 1: Shadow Mode (Week 1)

### Goals
- Deploy all 9 microservices alongside monolith
- Copy monolith data to service schemas
- Run integration tests (verify consistency)
- Keep monolith as primary (all reads/writes still go to monolith)

### Prerequisites
- [ ] All 9 service Docker images built (`mvn spring-boot:build-image` per service)
- [ ] PostgreSQL configured with 9 service schemas
- [ ] Redis, Kafka, Qdrant, Consul running (docker-compose)
- [ ] Monitoring stack (Prometheus, Grafana) deployed
- [ ] On-call engineer assigned + pager active

### Step 1.1: Deploy Services (Read-Only)

```bash
# Start full stack
make up

# Verify all services are healthy
curl http://localhost:3001/health  # identity-service
curl http://localhost:3002/health  # workspace-service
curl http://localhost:3003/health  # flow-service
curl http://localhost:3004/health  # runtime-service
curl http://localhost:3005/health  # conversation-service
curl http://localhost:3006/health  # knowledge-service
curl http://localhost:3007/health  # analytics-service
curl http://localhost:3008/health  # billing-service
curl http://localhost:3009/health  # integration-service

# All should return 200 OK
```

### Step 1.2: Run Data Migration (Copy from Monolith)

```bash
# Export credentials
export DB_HOST="postgres.default.svc.cluster.local"
export DB_PORT="5432"
export DB_NAME="threadly"
export DB_USER="threadly"
export DB_PASSWORD="$(kubectl get secret postgres-creds -o jsonpath='{.data.password}' | base64 -d)"

# Execute Phase 1 migration
bash scripts/migrate-monolith-to-services.sh phase1

# Output should show row counts:
# - identity_service.users: 1,542 rows
# - identity_service.organizations: 86 rows
# - workspace_service.bots: 342 rows
# - flow_service.flows: 2,105 rows
# - runtime_service.sessions: 18,234 rows
# - conversation_service.conversations: 45,678 rows
# - etc.
```

### Step 1.3: Verify Data Consistency

```sql
-- Run consistency checks in each schema
SELECT
  'users' as table_name,
  (SELECT COUNT(*) FROM public.users) as monolith_count,
  (SELECT COUNT(*) FROM identity_service.users) as service_count,
  (SELECT COUNT(*) FROM public.users) = (SELECT COUNT(*) FROM identity_service.users) as consistent
UNION ALL
SELECT
  'bots',
  (SELECT COUNT(*) FROM public.bots),
  (SELECT COUNT(*) FROM workspace_service.bots),
  (SELECT COUNT(*) FROM public.bots) = (SELECT COUNT(*) FROM workspace_service.bots)
UNION ALL
SELECT
  'flows',
  (SELECT COUNT(*) FROM public.flows),
  (SELECT COUNT(*) FROM flow_service.flows),
  (SELECT COUNT(*) FROM public.flows) = (SELECT COUNT(*) FROM flow_service.flows)
-- ... repeat for all major tables
```

**Expected**: All rows marked as `consistent = true`

### Step 1.4: Run Integration Tests

```bash
# Run cross-service integration tests
mvn test -f threadly-core/pom.xml -Dtest=*IntegrationTest

# Sample tests:
# - Create bot in workspace-service, verify it appears in flow-service
# - Send message to runtime-service, verify transcript appears in conversation-service
# - Index KB doc in knowledge-service, verify query returns results in runtime-service
# - Create user in identity-service, verify membership reflects in workspace-service
```

**Expected**: All tests pass with 0 failures

### Step 1.5: Monitor for 24 Hours

- [ ] All service /health endpoints return 200
- [ ] No error logs in any service (check aggregated logs)
- [ ] Metrics dashboard shows services receiving telemetry
- [ ] Monolith still handling 100% of user traffic
- [ ] No alerts firing

---

## Phase 2: Dual-Write Mode (Week 2)

### Goals
- Enable writes to BOTH monolith + new services
- Monitor write lag and consistency
- Ensure monolith + services stay in sync
- Build confidence in service write capability

### Prerequisites
- [ ] Phase 1 complete (all services deployed, data migrated)
- [ ] DualWriteInterceptor configured in threadly-common-spring
- [ ] Metrics dashboard updated to show write-lag & write-failures
- [ ] Alert: if write-lag > 2000ms, page on-call
- [ ] Alert: if write-failure-rate > 1%, page on-call

### Step 2.1: Enable Dual-Write Interceptor

Update `threadly-core/src/main/resources/application.yml`:

```yaml
migration:
  dual:
    write:
      enabled: true
      fail:
        strategy: log_only  # Don't fail fast, log and continue
      timeout:
        ms: 5000            # Max wait for shadow write
      service:
        base:
          url: http://localhost:8080  # Via Nginx/API gateway
```

Redeploy monolith:
```bash
mvn spring-boot:build-image -DskipTests -f threadly-core/pom.xml
docker stop threadly-core
docker run -d --name threadly-core \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/threadly" \
  threadly/threadly-core:latest
```

### Step 2.2: Create Write Lag Dashboard in Grafana

```promql
# Query: Average write lag by service
avg(rate(dual_write.lag_ms[5m])) by (service)

# Query: Write failure rate by service
rate(dual_write.failures[5m]) / (rate(dual_write.failures[5m]) + rate(dual_write.success[5m])) by (service)

# Query: Slow writes (lag > 1000ms)
dual_write.lag_ms > 1000
```

### Step 2.3: Run Smoke Tests

Create a synthetic load test that exercises all write paths:

```bash
# Run 1-hour load test
./scripts/load-test-dual-write.sh --duration 3600 --rps 100

# Verify:
# - All writes succeed to both monolith + services
# - Write lag stays below 500ms (p95)
# - No data loss in either system
```

### Step 2.4: Monitor for Data Consistency

Run periodically (every 4 hours):

```bash
# Compare row counts: monolith vs services
bash scripts/verify-consistency.sh

# Output format:
# ✓ users: 1,542 (monolith) = 1,542 (service) ✓
# ✓ bots: 342 (monolith) = 342 (service) ✓
# ✓ flows: 2,105 (monolith) = 2,105 (service) ✓
# ...
# All tables consistent!
```

### Step 2.5: Monitor for 5-7 Days

**Success Criteria**:
- [ ] Write lag avg < 100ms, p95 < 500ms
- [ ] Write failure rate < 0.1%
- [ ] No data loss alerts
- [ ] All integration tests pass continuously
- [ ] Zero data inconsistencies detected

**If Issues Found**:
- If write-lag > 1000ms: Check service response times, Kafka lag
- If write-failures > 1%: Check service logs for errors, network issues
- If data inconsistencies: Stop dual-writes, investigate, fix, retry

---

## Phase 3: Cutover (Week 3)

### Goals
- Switch all traffic from monolith → microservices
- Put monolith in maintenance mode (read-only)
- Keep monolith available for 2 weeks (safe rollback)

### Prerequisites
- [ ] Phase 2 complete (dual-writes stable, no consistency issues)
- [ ] All integration tests passing
- [ ] Write lag & failure metrics healthy
- [ ] Load test shows services handle expected peak load
- [ ] Cutover window scheduled: off-peak hours, all hands ready

### Step 3.1: Pre-Cutover Validation

```bash
# 1. Verify all services are healthy
bash scripts/healthcheck-all-services.sh

# 2. Verify database consistency one more time
bash scripts/verify-consistency.sh

# 3. Run full integration test suite
mvn test -f threadly-core/pom.xml -Dtest=*IntegrationTest

# 4. Verify Kafka consumers lag < 30 seconds
bash scripts/check-kafka-lag.sh

# Expected:
# ✓ All services healthy (200 OK)
# ✓ All tables consistent
# ✓ All tests pass
# ✓ Kafka lag < 30s
```

### Step 3.2: Update Nginx Routing (Cutover)

Edit `/etc/nginx/conf.d/threadly.conf`:

**Before (monolith primary)**:
```nginx
location ~ ^/auth/ {
  proxy_pass http://monolith:8888;
}
location ~ ^/(orgs|bots)/ {
  proxy_pass http://monolith:8888;
}
# ... all traffic to monolith
```

**After (services primary)**:
```nginx
location ~ ^/auth/ {
  proxy_pass http://identity-service:3001;
}
location ~ ^/(orgs|bots)/ {
  proxy_pass http://workspace-service:3002;
}
location ~ ^/flows/ {
  proxy_pass http://flow-service:3003;
}
location ~ ^/(sessions|realtime)/ {
  proxy_pass http://runtime-service:3004;
}
location ~ ^/conversations/ {
  proxy_pass http://conversation-service:3005;
}
location ~ ^/kb/ {
  proxy_pass http://knowledge-service:3006;
}
location ~ ^/dashboard/ {
  proxy_pass http://analytics-service:3007;
}
location ~ ^/billing/ {
  proxy_pass http://billing-service:3008;
}
location ~ ^/integrations/ {
  proxy_pass http://integration-service:3009;
}
```

Reload Nginx:
```bash
nginx -s reload
```

### Step 3.3: Set Monolith to Maintenance Mode

Update monolith `application.yml`:

```yaml
server:
  read-only: true        # Reject all writes
  maintenance:
    enabled: true
    message: "Migrating to microservices. Read-only mode."
```

Restart monolith:
```bash
docker restart threadly-core
```

### Step 3.4: Disable Dual-Write Interceptor

Update monolith `application.yml`:

```yaml
migration:
  dual:
    write:
      enabled: false     # Disable shadow writes to services
```

Restart monolith.

### Step 3.5: Verify Cutover Success

```bash
# 1. Check traffic is flowing to services (not monolith)
curl -v http://localhost:8080/bots
# Response should come from workspace-service:3002, not monolith

# 2. Verify no writes go to monolith
tail -f /var/log/threadly/monolith.log | grep -i "write"
# Should see NO write operations

# 3. Verify services handle all traffic
watch -n 5 'curl -s http://localhost:3002/health | jq'

# 4. Run integration tests
mvn test -f threadly-core/pom.xml -Dtest=*IntegrationTest
```

### Step 3.6: Monitor Cutover (First 2 Hours)

- [ ] All service /health endpoints return 200
- [ ] No increase in error rate (check Grafana)
- [ ] Latency remains < 5% above baseline
- [ ] No alert storms firing
- [ ] Users report normal experience (check Slack/Discord)

**If Critical Issue Found**:
```bash
# Immediate rollback: switch Nginx back to monolith
# (See Rollback section below)
```

---

## Phase 4: Observation & Decommission (Week 4+)

### Week 4: Keep Both Running

- Monolith in read-only mode (warm standby)
- All traffic → microservices
- Monitor metrics for stability
- Any critical issues: manual read-only fallback to monolith

### Week 2-3 After Cutover: Decommission Monolith

```bash
# 1. Verify 2 weeks of stable operation
# 2. Archive monolith database (backup)
pg_dump -h postgres -U threadly -d threadly -f threadly_monolith_$(date +%s).sql

# 3. Remove monolith from production
kubectl delete deployment threadly-core -n threadly

# 4. Remove monolith routing from Nginx (already done in Phase 3)

# 5. Update documentation & runbooks (remove monolith references)
```

---

## Rollback Procedure (Emergency)

### Window: Up to 7 Days After Cutover

If critical data loss or integrity issues detected:

```bash
# 1. Immediately stop all service writes
kubectl scale deployment --replicas=0 -l app in (identity-service, workspace-service, flow-service, ...)

# 2. Switch Nginx back to monolith
# Edit /etc/nginx/conf.d/threadly.conf (restore old config)
# location ~ ^/auth/ { proxy_pass http://monolith:8888; }
# ... (restore all routes to monolith)

# 3. Disable read-only mode on monolith
# Set server.read-only=false in application.yml
docker restart threadly-core

# 4. Reload Nginx
nginx -s reload

# 5. Verify traffic is flowing to monolith
curl -v http://localhost:8080/bots
# Response should come from monolith

# 6. Run smoke tests
bash scripts/smoke-tests.sh

# 7. Alert on-call for post-incident review
# - What went wrong?
# - How to prevent?
# - When to retry migration?
```

### After 7 Days: Cannot Fully Rollback

- Monolith data is stale (real writes only to services for 7+ days)
- Can only use monolith as read-only fallback
- Must fix services and proceed with migration

---

## Monitoring & Alerts

### Key Metrics (Dashboard)

1. **Service Health**
   - `/health` endpoint returns 200 for all 9 services
   - Alert: any service /health non-200 for 1 minute

2. **Data Consistency** (Phase 2)
   - Row count diff (monolith vs service) < 1%
   - Alert: any table inconsistency > 5 rows

3. **Write Latency**
   - Dual-write lag p95 < 500ms
   - Alert: write lag > 1000ms

4. **Write Errors**
   - Dual-write failure rate < 0.1%
   - Alert: failure rate > 1%

5. **Kafka Consumers**
   - Consumer lag < 30 seconds all topics
   - Alert: lag > 60 seconds

6. **API Latency**
   - P95 latency < 200ms (same as baseline)
   - Alert: P95 > baseline + 50ms

7. **Uptime**
   - Target: 99.5% during migration
   - Alert: <99% in any 1-hour window

### Dashboards

- Grafana: `/d/threadly-migration`
- Includes: health, consistency, lag, latency, errors
- Accessible to: on-call engineer, tech lead

---

## Troubleshooting

### Services Won't Start

```bash
# Check logs
docker logs identity-service | tail -50

# Common issues:
# - Database connection failed: check DB_URL, credentials, network
# - Consul not found: start Consul (consul agent -server -bootstrap-expect=1)
# - Port conflict: check if port already bound (lsof -i :3001)
```

### Write Lag High (> 1000ms)

```bash
# 1. Check service response times
curl -w "Time: %{time_total}s\n" http://localhost:3002/health

# 2. Check Kafka broker latency
kafka-consumer-perf-test.sh --broker-list localhost:9092 --topic test --messages 1000

# 3. Check database query performance
# In each service's database, look at slow queries

# Solutions:
# - Increase service replicas (auto-scale)
# - Increase Kafka partitions
# - Optimize slow database queries
```

### Data Inconsistency Detected

```bash
# 1. STOP all writes immediately
migration.dual.write.enabled=false  # Monolith only

# 2. Identify inconsistency
bash scripts/verify-consistency.sh

# 3. Investigate cause
# - Check service logs for errors during writes
# - Check Kafka consumer lag (missed messages?)
# - Check database transactions (deadlocks?)

# 4. Rebuild affected service data
# - Option A: Re-sync from monolith (wipe service schema first)
# - Option B: Manual fix using SQL (if isolated records)

# 5. Resume dual-writes after verification
migration.dual.write.enabled=true
```

### Cannot Rollback (Past 7 Days)

If you need to stop using services after 7 days:

1. **Read-Only Hybrid Mode**:
   - Keep services running (reads)
   - Forward writes to monolith (live copy?)
   - High risk of data divergence

2. **Controlled Shutdown**:
   - Plan data export from services
   - Archive to data warehouse
   - Accept data loss / create backups

3. **Long-Term**:
   - Continue with services
   - Fix issues going forward
   - Never attempt monolith switch again

---

## Checklist: Full Migration

- [ ] Week 1 Day 1: Phase 1 deployment complete
- [ ] Week 1 Day 2: Data migration complete, consistency verified
- [ ] Week 1 Day 3-7: Integration tests passing, monitoring stable
- [ ] Week 2 Day 1: Phase 2 dual-write enabled
- [ ] Week 2 Day 2-7: Write metrics healthy, no consistency issues
- [ ] Week 3 Day 1: Phase 3 cutover executed
- [ ] Week 3 Day 1-7: Monitoring shows stable operation
- [ ] Week 4 Day 1+: Monolith in maintenance mode, ready for decommission

---

## Contact

- **On-Call Lead**: [Slack channel #incident]
- **Tech Lead**: [Email]
- **Database**: [Postgres Admin contact]
