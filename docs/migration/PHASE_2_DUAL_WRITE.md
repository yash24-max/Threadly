# Phase 2: Dual-Write Mode — Parallel Writes

**Duration**: Week 2 (5 days)  
**Status**: All writes go to BOTH monolith (primary) AND services  
**Risk Level**: Medium (easier rollback, but data consistency requires monitoring)  
**Success Criteria**: Dual-write lag <1s, consistency drift <0.1%, zero write failures

---

## Overview

In Phase 2, the DualWriteInterceptor is enabled in the monolith. All writes (POST/PATCH/DELETE) are:
1. Executed on monolith (blocks response)
2. Forwarded asynchronously to services (non-blocking)

Services are still read-only for client traffic, but receive write events from the monolith.

**Key characteristics**:
- Monolith writes happen first (synchronous)
- Services receive async write events
- Read traffic still goes to monolith
- Services validate and apply writes
- Monitor for consistency drift and failures

---

## Timeline

### Day 1: Enable Dual-Write Configuration

**Update Configuration** (monolith + services):
```bash
# 1. Update monolith application.properties
cat >> services/threadly-common-spring/src/main/resources/application-dual-write.properties << 'EOF'
# Dual-Write Configuration
migration.dual.write.enabled=true
migration.dual.write.fail.strategy=log_only
migration.dual.write.timeout.ms=5000
migration.dual.write.async=true
migration.dual.write.service.base.url=http://api-gateway:8080
migration.dual.write.batch.size=50
migration.dual.write.batch.interval.ms=1000

# Metrics
migration.dual.write.metrics.enabled=true
migration.dual.write.lag.alert.threshold.ms=1000
migration.dual.write.failure.alert.threshold.percent=1.0
EOF

# 2. Rebuild monolith with dual-write profile
mvn -f services/threadly-common-spring/pom.xml clean package -DskipTests
docker build -t threadly-monolith:dual-write \
  -f services/Dockerfile \
  --build-arg PROFILE=dual-write \
  .

# 3. Update services to accept writes
# Services should already be accepting writes from Phase 1
# Verify all services have DualWriteInterceptor imported
grep -r "DualWriteInterceptor" services/ || echo "WARNING: Not found"
```

**Deploy Updated Monolith**:
```bash
# Docker Compose
docker-compose down threadly-monolith
docker-compose up -d threadly-monolith --force-recreate

# Wait for startup
docker logs -f threadly-monolith | grep "Started"

# Kubernetes
kubectl set image deployment/threadly-monolith threadly-monolith=threadly-monolith:dual-write -n threadly
kubectl rollout status deployment/threadly-monolith -n threadly --timeout=5m
```

**Verify Dual-Write is Active**:
```bash
# Check health endpoint
curl http://localhost:8000/health/dual-write

# Expected response:
# {
#   "status": "enabled",
#   "writeCount": 0,
#   "failureCount": 0,
#   "avgLagMs": 0,
#   "lastCheck": "2025-05-27T10:15:30Z"
# }

# Check logs for "DualWriteInterceptor enabled"
docker logs threadly-monolith | grep -i "dualwrite"
```

### Day 2: Gradual Traffic Ramp (5% → 25%)

**Start with 5% Write Traffic to Services**:
```bash
# Update API Gateway (Nginx) configuration
cat > infrastructure/nginx/dual-write.conf << 'EOF'
upstream monolith {
  server monolith:8000 weight=100;
}

upstream services {
  server api-gateway:8080 weight=1;  # 1% of write verification
}

server {
  listen 8080;
  
  # Write operations go to monolith (primary) + dual-write async to services
  location ~ ^/api/(bots|flows|conversations|settings)/.*$ {
    if ($request_method != GET) {
      proxy_pass http://monolith;
      # Monolith's DualWriteInterceptor handles service replication
      proxy_set_header X-Dual-Write enabled;
    }
  }
}
EOF

# Reload Nginx
docker exec nginx nginx -s reload

# Monitor dual-write metrics
curl http://localhost:8000/metrics/dual-write | jq .
```

**Monitor Dual-Write Metrics** (24 hours):
```bash
# Watch in Grafana dashboard "Threadly Microservices Migration"
# Key panels:
#   1. Dual-Write Lag (ms) - should be <500ms
#   2. Dual-Write Failures (%) - should be 0%
#   3. Service Write Latency (p95, p99)
#   4. Consistency Drift (%) - should be <0.1%

# Command-line monitoring
watch -n 5 'curl -s http://localhost:8000/metrics/dual-write | jq'

# Expected output after 1 hour:
# {
#   "writes": 2450,
#   "failures": 0,
#   "avgLag": 245,
#   "p95Lag": 890,
#   "p99Lag": 1200
# }
```

**Increase to 25% After 24 Hours** (if metrics are good):
```bash
# Update Nginx weight
sed -i 's/weight=1;  # 1%/weight=4;  # 4%/g' infrastructure/nginx/dual-write.conf
docker exec nginx nginx -s reload

# Continue monitoring for another 24 hours
```

### Day 3-4: Increase to 50% → 100%

**Gradually Increase Write Coverage**:
```bash
# Day 3: 50% of writes to services
# Update Nginx weights: services weight=1, monolith weight=1
# OR: Use the monolith's DualWriteInterceptor (simpler)

# Verify data consistency at each stage
bash scripts/validate-data-consistency.sh phase2-increment

# Check for:
# - Consistency drift < 0.1%
# - No orphaned records created
# - Service write latencies acceptable
# - Monolith response time not degraded
```

**Full Dual-Write at 100%** (Day 4):
```bash
# All writes now go to both monolith AND services
# Monolith is still primary (reads + writes)
# Services receive all writes asynchronously

# Verify:
# 1. All dual-write metrics healthy
curl http://localhost:8000/metrics/dual-write | jq '.'

# 2. Zero consistency drift
bash scripts/validate-data-consistency.sh phase2-detailed

# 3. No write failures
docker logs threadly-monolith | grep -i "dual.*error" | wc -l  # Should be 0

# 4. Service write latency acceptable
curl http://localhost:8080/metrics/writes | jq '.latencies'
```

### Day 5: Extended Monitoring & Sign-Off

**24+ Hour Full Dual-Write Monitoring**:
```bash
# Monitor all critical metrics continuously
# In Grafana, set up alerts for:
#   - Dual-write lag > 2s
#   - Dual-write failure rate > 0.5%
#   - Service write latency p99 > 5s
#   - Consistency drift > 0.5%

# Command to check all metrics
curl http://localhost:8000/metrics/dual-write | jq '{
  writes: .writes,
  failures: .failures,
  failureRate: (.failures / .writes * 100),
  avgLag: .avgLag,
  p99Lag: .p99Lag,
  consistencyDrift: .consistencyDrift
}'

# Expected healthy state:
# {
#   "writes": 48500,
#   "failures": 2,
#   "failureRate": 0.004,
#   "avgLag": 312,
#   "p99Lag": 1489,
#   "consistencyDrift": 0.02
# }
```

**Final Consistency Check**:
```bash
# Comprehensive row count and data validation
bash scripts/validate-data-consistency.sh phase2-final

# Sample queries to verify manually:
docker exec postgres psql -U threadly << 'EOF'
-- Check monolith vs services row counts
SELECT 'monolith' as source, COUNT(*) as users FROM public.users
UNION ALL
SELECT 'identity-service' as source, COUNT(*) as users FROM identity_service.users;

-- Check for orphaned records
SELECT f.id, f.bot_id 
FROM workspace_service.bots b
JOIN flow_service.flows f ON b.id = f.bot_id
WHERE b.org_id != f.org_id;  -- Should be 0 rows

-- Check for duplicate IDs
SELECT id, COUNT(*) FROM identity_service.users GROUP BY id HAVING COUNT(*) > 1;
EOF

# Expected: All results show 0 anomalies
```

**Integration Test Validation**:
```bash
# Run integration tests again to ensure Phase 2 compatibility
mvn -f services/threadly-common-spring/pom.xml verify -DskipITs=false

# All tests should still pass
# Some tests may be slower due to async writes, but no failures
```

---

## Rollback Procedure (if needed)

If Phase 2 fails, rollback is quick:

```bash
# 1. Disable dual-write
docker exec threadly-monolith curl -X POST http://localhost:8000/admin/dual-write/disable

# 2. Verify monolith is now primary again
curl http://localhost:8000/health/dual-write
# Expected: {"status": "disabled"}

# 3. No data loss in monolith (it's always been primary)
# Services may have inconsistent data, but it doesn't matter

# 4. Clear inconsistent data from services (optional)
bash scripts/reset-service-data.sh

# 5. Redeploy services clean
docker-compose down identity-service workspace-service flow-service ...
docker-compose up -d identity-service workspace-service flow-service ...
```

---

## Sign-Off Checklist

Before proceeding to Phase 3 (cutover):

- [ ] Dual-write enabled and healthy for 24+ hours
- [ ] Dual-write lag consistently <1s (p99 <2s)
- [ ] Dual-write failure rate <0.5%
- [ ] Consistency drift <0.1%
- [ ] Zero orphaned or duplicate records
- [ ] Integration tests still pass
- [ ] Monolith response time not degraded
- [ ] Services handling writes reliably
- [ ] Team confident with dual-write behavior
- [ ] Incident commander briefed on status
- [ ] Rollback procedure tested (dry-run)
- [ ] Tech lead approval to proceed to Phase 3

---

## Monitoring Dashboards

Key Grafana panels to watch during Phase 2:

1. **Dual-Write Health**
   - Write count (cumulative)
   - Failure rate (%)
   - Average lag (ms)

2. **Service Write Performance**
   - Latency p50/p95/p99 (ms)
   - Throughput (writes/sec)
   - Error rate (%)

3. **Data Consistency**
   - Drift detection (%)
   - Row count mismatches
   - Orphaned records count

4. **Monolith Impact**
   - Response time before/after dual-write
   - Memory usage (should not increase)
   - Error rate (should stay 0%)

---

## Common Issues & Solutions

| Issue | Symptoms | Solution |
|-------|----------|----------|
| Dual-write lag > 5s | Services slow to apply writes | Check service health, increase async workers |
| Consistency drift | Row count mismatches | Run `validate-data-consistency.sh`, replay recent writes |
| Write timeouts | Some writes not reaching services | Increase `migration.dual.write.timeout.ms` to 10000 |
| Monolith response slow | p99 latency increases by >10% | Reduce dual-write batch size or async workers |

---

## Next Steps

Once Phase 2 sign-off is complete:
- Notify tech lead (@yasva)
- Schedule Phase 3 (cutover) for the following week
- Update PRODUCT_STATUS.md with Phase 2 completion
- Brief team on cutover procedure

See **PHASE_3_CUTOVER.md** for next steps.
