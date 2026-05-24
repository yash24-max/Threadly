# Phase 3: Cutover — Switch to Microservices

**Duration**: Week 3 (5 days)  
**Status**: Microservices become primary, monolith becomes fallback  
**Risk Level**: High (but with 7-day rollback window)  
**Success Criteria**: 99.9%+ availability, <5% latency increase, 100% data consistency

---

## Overview

In Phase 3, traffic is switched from the monolith to microservices. This is done in stages:
1. **Stage 1** (Days 1-2): Redirect 5% of read traffic to services
2. **Stage 2** (Days 2-3): Increase to 25%, then 50%
3. **Stage 3** (Days 3-4): Full cutover (100% reads + writes to services)
4. **Stage 4** (Days 4-5): Monitor for 48 hours, then decommission monolith

**Key characteristics**:
- Microservices handle ALL reads + writes
- Monolith operates in parallel (read-only) as fallback
- Automatic failover if service health degrades
- 7-day rollback window in case of critical issues

---

## Pre-Cutover Verification (Day 0)

Before starting Phase 3, complete these checks:

**Verify Phase 2 Completion**:
```bash
# 1. Ensure dual-write has been stable for 48+ hours
curl http://localhost:8000/metrics/dual-write | jq '.uptime'
# Expected: > 172800 (48 hours in seconds)

# 2. Zero consistency drift
bash scripts/validate-data-consistency.sh final
# Expected output: "consistency_drift: 0.00%"

# 3. All services healthy
kubectl get deployments -n threadly
# Expected: All "Ready" column shows desired count
```

**Test Failover Procedure** (dry-run):
```bash
# 1. Stop monolith (simulate failure)
docker-compose stop threadly-monolith

# 2. Verify services handle traffic
curl http://localhost:8080/api/bots -H "Authorization: Bearer $TEST_JWT"
# Expected: 200 OK (services respond)

# 3. Restart monolith
docker-compose up -d threadly-monolith
docker-compose exec threadly-monolith sh -c "while [ ! -f /tmp/ready ]; do sleep 1; done"
```

**Brief Incident Commander**:
```bash
# 1. Review cutover plan together
# 2. Verify escalation path is clear
# 3. Confirm on-call team is ready
# 4. Set status page to "Investigating" if needed
```

---

## Timeline

### Day 1: Prepare Traffic Routing (5% Cutover)

**Stage 1: Redirect 5% Read Traffic to Services**

```bash
# 1. Update API Gateway routing (Nginx)
cat > infrastructure/nginx/cutover-stage1.conf << 'EOF'
upstream monolith {
  server monolith:8000;
}

upstream services {
  server api-gateway:8080;
}

server {
  listen 8080;
  
  # 95% reads go to monolith, 5% to services
  location ~ ^/api/.*$ {
    if ($request_method = GET) {
      # Read requests: 95/5 split
      set $upstream monolith;
      if ($request_time > 0.005) { set $upstream services; }  # Canary: 5% of requests
      proxy_pass http://$upstream;
    }
    
    # Write requests: still go to monolith (Phase 2 dual-write handles services)
    if ($request_method != GET) {
      proxy_pass http://monolith;
      proxy_set_header X-Dual-Write enabled;
    }
  }
}
EOF

# 2. Deploy routing configuration
docker exec nginx -c /etc/nginx/cutover-stage1.conf nginx -t
docker exec nginx nginx -s reload

# 3. Verify traffic is split
curl http://localhost:8080/api/health
# Should occasionally see service responses (different headers, timings)
```

**Monitor Stage 1** (24 hours):
```bash
# Key metrics to watch in Grafana:
# - Error rate (read-path) - should be 0%
# - Service latency p95/p99
# - Monolith latency (should be unaffected)
# - Consistency (dual-write still active)

watch -n 10 'curl -s http://localhost:8080/metrics/cutover | jq'

# Expected healthy state:
# {
#   "reads_to_monolith": 4750,
#   "reads_to_services": 250,
#   "error_rate": 0.0,
#   "latency_p99_monolith": 245,
#   "latency_p99_services": 289
# }

# If error rate > 0.1%, rollback to previous stage:
curl -X POST http://localhost:8000/admin/cutover/rollback-to-monolith
```

### Day 2: Increase to 25% → 50% Read Traffic

**Stage 2A: 25% of reads to services** (after 12-24 hours of Stage 1):
```bash
# Update routing weights
sed -i 's/weight=20;  # 5%/weight=5;   # 25%/g' infrastructure/nginx/cutover-stage2a.conf
docker exec nginx nginx -s reload

# Monitor for 12 hours
# Same metrics as Stage 1, but expect:
# - latency_p99_services may slightly increase (higher load)
# - consistency should still be perfect (dual-write active)

# Check for any anomalies
curl http://localhost:8000/metrics/cutover | jq '.anomalies'
```

**Stage 2B: 50% of reads to services** (after Stage 2A successful):
```bash
# Update routing weights to 50/50
sed -i 's/weight=5;   # 25%/weight=2;   # 50%/g' infrastructure/nginx/cutover-stage2b.conf
docker exec nginx nginx -s reload

# Monitor for 12 hours
# At 50%, services are handling half the read traffic
# If everything is healthy, proceed to full cutover

# Health check at 50%:
curl http://localhost:8080/metrics/cutover | jq '{
  reads_split: .reads_to_services / (.reads_to_services + .reads_to_monolith) * 100,
  error_rate: .error_rate,
  consistency: .consistency_drift
}'

# Expected:
# {
#   "reads_split": 50.0,
#   "error_rate": 0.0,
#   "consistency": 0.0
# }
```

### Day 3: Full Cutover (100% to Services)

**Stage 3: Disable Dual-Write, Switch to Services**

```bash
# 1. Disable dual-write (monolith no longer writes to services)
docker exec threadly-monolith curl -X POST http://localhost:8000/admin/dual-write/disable

# 2. Verify dual-write is off
curl http://localhost:8000/health/dual-write
# Expected: {"status": "disabled"}

# 3. Update API Gateway to route ALL traffic to services
cat > infrastructure/nginx/cutover-stage3.conf << 'EOF'
upstream services {
  server api-gateway:8080;
}

server {
  listen 8080;
  location ~ ^/api/.*$ {
    proxy_pass http://services;
  }
}
EOF

docker exec nginx nginx -s reload

# 4. Verify all traffic goes to services
curl http://localhost:8080/api/health | jq .
```

**Verify Cutover Success** (hourly checks):
```bash
# 1. Check service health
kubectl get deployments -n threadly | awk '{print $2, $3}'
# Expected: All "Ready" column shows desired count

# 2. Check error rates
curl http://localhost:8080/metrics | jq '.error_rate'
# Expected: < 0.1%

# 3. Check latency
curl http://localhost:8080/metrics | jq '.latency'
# Expected: p99 < 600ms (baseline + acceptable increase)

# 4. Check data consistency
bash scripts/validate-data-consistency.sh cutover
# Expected: "consistency_drift: 0.00%"

# 5. Check Kafka consumer lag
kubectl exec -n threadly kafka-0 -- kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe
# Expected: Lag for all partitions = 0 or catching up
```

**Create Fallback to Monolith** (in case of issues):
```bash
# Set up automatic fallback if services fail
cat > /tmp/fallback-monolith.sh << 'EOF'
#!/bin/bash
ERROR_RATE=$(curl -s http://localhost:8080/metrics | jq '.error_rate')
if (( $(echo "$ERROR_RATE > 1.0" | bc -l) )); then
  echo "ERROR_RATE too high ($ERROR_RATE%), falling back to monolith"
  curl -X POST http://localhost:8000/admin/cutover/fallback
  exit 1
fi
EOF

# Schedule this check every 5 minutes
*/5 * * * * /tmp/fallback-monolith.sh
```

### Days 4-5: Extended Monitoring & Stabilization

**Monitor Full Cutover** (48+ hours):
```bash
# Watch all critical metrics continuously
# Set Grafana dashboard to full-screen view with auto-refresh (30s)

# Metrics to monitor:
# 1. Service error rate (should be 0%)
# 2. Service latency p95/p99 (should be < 5% increase from baseline)
# 3. Kafka lag (should be < 1s for all topics)
# 4. Database connection pool (should be stable)
# 5. Memory usage (should not grow unbounded)
# 6. CPU usage (should stay < 80%)

# Command to check health every hour
for i in {1..48}; do
  echo "=== Hour $i ==="
  curl -s http://localhost:8080/metrics | jq '{
    error_rate: .error_rate,
    latency_p99: .latency.p99,
    kafka_lag: .kafka.lag,
    consistency_drift: .consistency.drift
  }'
  sleep 3600
done
```

**No Issues? Start Monolith Decommission Prep**:
```bash
# After 48+ hours of stable cutover:

# 1. Take final monolith backup
docker exec postgres pg_dump -U threadly threadly_core > /backups/monolith-final-$(date +%s).sql

# 2. Verify monolith hasn't been used
docker logs threadly-monolith | grep -i "error\|warning" | tail -20

# 3. Scale down monolith (don't delete yet, keep for 7 days)
kubectl scale deployment/threadly-monolith --replicas=0 -n threadly

# 4. Note monolith is decommissioned in PRODUCT_STATUS.md
echo "Monolith decommissioned on $(date)" >> PRODUCT_STATUS.md
```

---

## Rollback Procedure (if needed during Phase 3)

If Phase 3 has critical issues, complete rollback within 30 minutes:

**Quick Rollback (< 30 min)**:
```bash
# 1. Detect issue (automated or manual)
ERROR_RATE=$(curl -s http://localhost:8080/metrics | jq '.error_rate')
if (( $(echo "$ERROR_RATE > 1.0" | bc -l) )); then
  echo "CRITICAL: Error rate $ERROR_RATE%, initiating rollback"
  
  # 2. Re-enable monolith
  kubectl scale deployment/threadly-monolith --replicas=3 -n threadly
  kubectl rollout status deployment/threadly-monolith -n threadly --timeout=5m
  
  # 3. Re-enable dual-write
  curl -X POST http://localhost:8000/admin/dual-write/enable
  
  # 4. Switch traffic back to monolith
  cat > /tmp/rollback-monolith.conf << 'EOF'
  upstream monolith { server monolith:8000; }
  server {
    listen 8080;
    location ~ ^/api/.*$ { proxy_pass http://monolith; }
  }
  EOF
  docker exec nginx nginx -s reload
  
  # 5. Verify rollback successful
  curl http://localhost:8080/api/health
  
  # 6. Alert incident commander
  echo "Rolled back to monolith at $(date)" | \
    curl -X POST -d @- https://slack.webhook/alerts
fi
```

**Data Recovery After Rollback**:
```bash
# After rollback, monolith becomes primary again
# Services may have stale data, but monolith is source of truth

# 1. Clean service data (optional)
bash scripts/reset-service-data.sh

# 2. Verify data consistency
bash scripts/validate-data-consistency.sh post-rollback

# 3. Investigate root cause
docker logs api-gateway | grep -i "error" | tail -50
kubectl logs -n threadly identity-service-0 | grep -i "error" | tail -50
```

---

## Sign-Off Checklist

Before considering Phase 3 complete:

- [ ] Cutover completed successfully (100% traffic to services)
- [ ] All 9 services healthy and responsive
- [ ] Error rate < 0.1% (lower than monolith baseline)
- [ ] Latency p99 within 5% of baseline
- [ ] Kafka lag stable (< 1s for all topics)
- [ ] Data consistency verified (0% drift)
- [ ] Monolith scaled down (no longer handling traffic)
- [ ] 48+ hours of stable operation verified
- [ ] Incident commander briefed on success
- [ ] Team confidence high (no anomalies detected)
- [ ] Post-mortem template prepared (if any issues occurred)

---

## Post-Cutover Actions (Days 5-7)

### Day 5: Keep Monolith Available (7-day Rollback Window)

```bash
# Monolith is decommissioned from traffic but kept alive
# In case we need to roll back

# 1. Keep monolith running (but scaled to 0 replicas in K8s)
# Keep database backup available
# Keep dual-write code in services (for safety)

# 2. Monitor for any data inconsistencies
bash scripts/validate-data-consistency.sh daily

# 3. If issues found, can still roll back within 7 days
```

### Day 6-7: Final Verification

```bash
# After 7 days, if no issues:

# 1. Verify all data in services is correct
bash scripts/validate-data-consistency.sh final-verification

# 2. Delete monolith backup (after keeping 7 days)
rm /backups/monolith-backup-*.sql

# 3. Remove dual-write code from services
git commit -m "refactor: Remove dual-write fallback code (Phase 3 complete)"

# 4. Update PRODUCT_STATUS.md
# Microservices Migration: COMPLETE (Phase 1-3 done)
# Monolith: DECOMMISSIONED

# 5. Celebrate! 🎉
```

---

## Monitoring Dashboards During Phase 3

Create a custom Grafana dashboard with these panels:

1. **Traffic Distribution**
   - % traffic to monolith (should go 95% → 50% → 0%)
   - % traffic to services (should go 5% → 50% → 100%)

2. **Error Rates**
   - Monolith error rate
   - Services error rate
   - Combined error rate (should be < 0.1%)

3. **Latency Comparison**
   - Monolith p95/p99
   - Services p95/p99
   - Difference (% increase)

4. **Service Health**
   - Deployment status (all 9 services)
   - Replica count (should be stable)
   - Pod restart count (should be 0)

5. **Data Consistency**
   - Drift percentage
   - Orphaned records count
   - Duplicate records count

---

## Common Issues & Solutions

| Issue | Symptoms | Solution |
|-------|----------|----------|
| High error rate | Error rate > 0.5% | Check service logs, verify DB connections, rollback if > 1% |
| High latency | p99 > 600ms | Check Kafka lag, verify service resource limits, scale up if needed |
| Kafka consumer lag | Lag > 5 minutes | Check consumer health, restart consumer group if needed |
| Data inconsistency | Drift > 0.1% | Identify missing events, replay from Kafka, restart services |
| Memory leak | Memory growing over time | Check for connection leaks, restart affected service |

---

## Next Steps

Once Phase 3 sign-off is complete:
- Proceed to Week 4 (observational phase)
- Monitor for 1 week with no issues
- Prepare to decommission monolith permanently
- Update PRODUCT_STATUS.md

See **docs/runbooks/README.md** for operational procedures.
