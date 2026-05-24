# Runbook: Emergency Rollback (Phase 3 → Phase 1)

**Severity**: Critical  
**Runbook Owner**: Tech Lead  
**Estimated Time to Rollback**: 15-30 minutes  
**Rollback Window**: **7 days post-cutover** (full data consistency guaranteed)

---

## Overview

If microservices cutover (Phase 3) experiences **critical failures**, this runbook enables rapid rollback to monolith (Phase 1 state).

**Safe Window**: 0-7 days post-cutover  
**After 7 days**: Rollback is risky — monolith data may be stale, inconsistent

### Failure Scenarios Requiring Rollback

1. **Data Loss**: Service writes not replicated to monolith
2. **API Breaking Changes**: Upstream clients expect monolith API format
3. **Cascading Service Failures**: 2+ critical services down simultaneously
4. **Database Corruption**: Detected during integration tests or alerts
5. **Unrecoverable Kafka State**: Event stream poisoned (DLQ > 100K messages)

---

## Pre-Rollback Checklist

### Step 0: Confirm Rollback is Necessary

```bash
# 1. Check service health
for service in identity workspace flow runtime conversation knowledge analytics billing integration; do
  echo "=== $service-service ==="
  curl http://localhost:300X/health || echo "DOWN"
done

# 2. Check database integrity
docker exec postgres psql -U threadly -c "
  SELECT schema_name, COUNT(*) as table_count
  FROM information_schema.tables
  WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
  GROUP BY schema_name
  ORDER BY schema_name;
"

# 3. Check Kafka consumer lag
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --all-groups \
  --describe | grep LAG

# 4. Check for recent errors in logs
for service in identity workspace flow runtime conversation knowledge analytics billing integration; do
  docker logs $service-service 2>&1 | grep -i "error\|exception" | tail -5
done
```

### Step 1: Declare Incident

- [ ] Page on-call Tech Lead
- [ ] Notify #incident Slack channel
- [ ] Create incident ticket (Jira: THREADLY-XXX)
- [ ] Set status: "🔴 Rollback In Progress"

### Step 2: Backup Current Service Data (Optional but Recommended)

```bash
# Timestamp for backup
BACKUP_TS=$(date +%Y%m%d_%H%M%S)

# Dump service schemas for post-mortem analysis
for schema in identity_service workspace_service flow_service runtime_service \
              conversation_service knowledge_service analytics_service \
              billing_service integration_service; do
  docker exec postgres pg_dump -U threadly -n $schema --no-acl \
    > /tmp/backup_${schema}_${BACKUP_TS}.sql
done

echo "Backups created in /tmp/backup_*_${BACKUP_TS}.sql"
```

---

## Phase 1: Switch Traffic Back to Monolith (5-10 minutes)

### Step 1A: Update Nginx Routing

```bash
# Backup current Nginx config
cp /etc/nginx/conf.d/threadly.conf \
   /etc/nginx/conf.d/threadly.conf.phase3_backup

# Edit routing to point back to monolith
# File: /etc/nginx/conf.d/threadly.conf
# Change FROM:
#   upstream threadly_backend {
#     server identity-service:3001;
#     server workspace-service:3002;
#     ...
#   }
#   location / {
#     proxy_pass http://threadly_backend;
#   }
#
# Change TO:
cat > /etc/nginx/conf.d/threadly.conf << 'EOF'
upstream monolith_backend {
  server monolith:8000;
}

server {
  listen 8080;
  location / {
    proxy_pass http://monolith_backend;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_buffering off;
  }
}
EOF

# Validate Nginx config
nginx -t

# Reload Nginx (no downtime)
systemctl reload nginx

echo "✓ Nginx routing updated to monolith"
```

### Step 1B: Verify Routing

```bash
# Test endpoint through Nginx
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TEST_JWT_TOKEN"

# Expected: 200 (from monolith)
# Check response source
curl -v http://localhost:8080/auth/me 2>&1 | grep "X-Service-Name"
```

### Step 1C: Stop Microservices (Optional)

If you want to reduce confusion and prevent dual-writes:

```bash
# Graceful shutdown (allow 30s for in-flight requests)
docker-compose -f docker-compose.yml \
  down \
  identity-service workspace-service flow-service runtime-service \
  conversation-service knowledge-service analytics-service \
  billing-service integration-service

# If graceful shutdown hangs, force kill:
docker kill identity-service workspace-service flow-service runtime-service \
           conversation-service knowledge-service analytics-service \
           billing-service integration-service
```

---

## Phase 2: Verify Monolith Health (5 minutes)

### Step 2A: Health Checks

```bash
# Check monolith API
curl http://localhost:8000/health

# Check database connectivity
curl http://localhost:8000/health/db

# Check Redis connectivity
curl http://localhost:8000/health/redis

# Check critical endpoints
curl http://localhost:8000/api/bots \
  -H "Authorization: Bearer $TEST_JWT_TOKEN"

# Should return 200 with bot list
```

### Step 2B: Monitor Logs

```bash
# Tail monolith logs for errors
docker logs -f monolith --tail 50

# Watch for:
# - "Connection refused" (dep down)
# - "Database error" (stale state)
# - "Authentication failed" (JWT issue)
```

### Step 2C: Sample End-to-End Test

```bash
# Create test organization
ORG_ID=$(curl -X POST http://localhost:8000/api/orgs \
  -H "Content-Type: application/json" \
  -d '{"name":"RollbackTest"}' | jq -r '.id')

# Create test bot
BOT_ID=$(curl -X POST http://localhost:8000/api/bots \
  -H "Content-Type: application/json" \
  -d "{\"org_id\":\"$ORG_ID\",\"name\":\"TestBot\"}" | jq -r '.id')

# Verify bot was created
curl http://localhost:8000/api/bots/$BOT_ID | jq '.name'

# Expected: "TestBot"

echo "✓ End-to-end test passed"
```

---

## Phase 3: Data Reconciliation (10-15 minutes)

**Important**: Monolith has stale data from when Phase 3 started.  
Services may have processed events not yet synced back.

### Step 3A: Identify Gaps

```bash
# Check last update timestamps
docker exec postgres psql -U threadly -c "
  SELECT schema_name, 
         MAX(updated_at) as last_update
  FROM (
    SELECT 'public' as schema_name, MAX(updated_at) as updated_at FROM public.bots
    UNION ALL
    SELECT 'public', MAX(updated_at) FROM public.conversations
    UNION ALL
    SELECT 'public', MAX(updated_at) FROM public.flows
  ) as updates
  GROUP BY schema_name
  ORDER BY last_update DESC;
"

# If latest service update > Phase 3 cutover time:
# → Some data was written only to services, NOT synced back to monolith
# → Decide: accept data loss OR replay events from Kafka
```

### Step 3B: Replay Recent Events (Recommended)

```bash
# Export Kafka consumer offsets at cutover time
CUTOVER_TIME=$(date -d "30 minutes ago" +%s)000

# Reset service consumer groups to read from cutover point
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group monolith-event-consumer \
  --reset-offsets \
  --to-timestamp $CUTOVER_TIME \
  --execute

echo "✓ Kafka reset to cutover point (events will be replayed)"

# Start monolith event consumer to replay
docker exec monolith bash -c "
  java -Dspring.profiles.active=replay \
       -Dkafka.replay.enabled=true \
       -Dkafka.replay.start_time=$CUTOVER_TIME \
       -jar app.jar
"

# Monitor replay progress
while true; do
  LAG=$(docker exec kafka kafka-consumer-groups.sh \
    --bootstrap-server localhost:9092 \
    --group monolith-event-consumer \
    --describe | grep LAG | awk '{sum+=$NF} END {print sum}')
  
  echo "$(date): Kafka lag = $LAG messages"
  
  if [ "$LAG" -eq 0 ]; then
    echo "✓ Replay complete"
    break
  fi
  sleep 10
done
```

### Step 3C: Verify Data Consistency

```bash
# Count records monolith vs. services
echo "=== MONOLITH ==="
docker exec postgres psql -U threadly -c "
  SELECT 'users' as table, COUNT(*) FROM public.users
  UNION ALL SELECT 'bots', COUNT(*) FROM public.bots
  UNION ALL SELECT 'conversations', COUNT(*) FROM public.conversations
  UNION ALL SELECT 'messages', COUNT(*) FROM public.messages;
"

echo "=== SERVICES (last snapshot) ==="
docker exec postgres psql -U threadly -c "
  SELECT 'users' as table, COUNT(*) FROM identity_service.users
  UNION ALL SELECT 'bots', COUNT(*) FROM workspace_service.bots
  UNION ALL SELECT 'conversations', COUNT(*) FROM conversation_service.conversations
  UNION ALL SELECT 'messages', COUNT(*) FROM conversation_service.messages;
"

# If counts differ:
# → Log discrepancy ticket (RCA needed)
# → Document in incident report
```

---

## Phase 4: Post-Rollback Validation (5 minutes)

### Step 4A: Run Integration Tests

```bash
# Copy integration tests
docker cp tests/integration monolith:/tmp/tests

# Run against monolith
docker exec monolith bash -c "
  cd /tmp/tests && \
  mvn verify -DskipITs=false
"

# Should see:
# [INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
```

### Step 4B: Smoke Tests (Client Perspective)

```bash
# If dashboard exists, verify key pages load:
# - Login page
# - Bot list
# - Flow builder
# - Analytics dashboard

curl -I http://localhost:3000/login
curl -I http://localhost:3000/bots
curl -I http://localhost:3000/flows
curl -I http://localhost:3000/analytics
```

### Step 4C: Alert on Rollback

```bash
# Set maintenance mode alert
curl -X POST http://localhost:9090/api/v1/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "alert":"ROLLBACK_IN_PROGRESS",
    "message":"Monolith restored. Phase 3 rollback completed.",
    "severity":"warning"
  }'

# Update status page
# → Notify users via #status-page Slack
# → Update statuspage.io

echo "✓ Rollback validation complete"
```

---

## Phase 5: Post-Incident Actions

### Step 5A: Document Findings

```bash
# Create incident report
cat > /tmp/incident_report_${BACKUP_TS}.md << 'EOF'
# Rollback Incident Report — Phase 3

**Date**: $(date)
**Trigger**: [Describe failure]
**Duration**: [How long services were down]
**Impact**: [Users affected, data loss (if any)]

## Timeline
- **T+0min**: Failure detected
- **T+5min**: Rollback initiated
- **T+15min**: Monolith restored
- **T+30min**: Data reconciliation complete

## Root Cause
[Findings from logs, metrics, database state]

## Recovery Steps Taken
1. ...

## Lessons Learned
- ...

## Action Items
- [ ] Code change to prevent future occurrence
- [ ] Test coverage gap identified
- [ ] Deploy fix and validate

EOF

echo "Report saved to /tmp/incident_report_${BACKUP_TS}.md"
```

### Step 5B: Prepare Phase 3 Retry

Once root cause is fixed and validated:

1. **Code changes committed**: PR reviewed, tested
2. **Staging validation**: Full integration test suite passes
3. **Pre-flight checklist**: 
   - [ ] All 9 services healthy
   - [ ] Kafka lag = 0 on all topics
   - [ ] Database integrity checks pass
   - [ ] Nginx config re-validated
4. **Retry Phase 3**: Follow RUNBOOK_MIGRATION.md Phase 3

---

## Quick Reference: Rollback Command Sequence

```bash
# All-in-one rollback (if time is critical):

# 1. Switch Nginx
cp /etc/nginx/conf.d/threadly.conf.phase1_backup \
   /etc/nginx/conf.d/threadly.conf
nginx -t && systemctl reload nginx

# 2. Wait for traffic to stabilize (2 minutes)
sleep 120

# 3. Stop services
docker-compose down identity-service workspace-service flow-service \
  runtime-service conversation-service knowledge-service \
  analytics-service billing-service integration-service

# 4. Verify monolith
curl http://localhost:8000/health

# 5. Alert team
# Send message to #incident Slack channel

echo "✓ Rollback complete in $(date)"
```

---

## Escalation Contacts

| Role | Contact | Slack |
|------|---------|-------|
| **Tech Lead** | @yasva | #incident |
| **Infra Team** | @platform-team | #infrastructure |
| **CEO** (if >1 hour downtime) | @yasva | #executive |
| **Support Team** | @support-lead | #support |

---

## Appendix: Prevention

To reduce rollback likelihood:

1. **Canary Deployment**: Deploy to 5% of traffic first (Phase 3A)
2. **Health Checks**: Validate all services /health before full cutover
3. **Kafka Consumer Lag**: Ensure < 10 seconds before cutover
4. **Synthetic Monitoring**: Automated end-to-end tests during Phase 2-3
5. **Feature Flags**: Gradual feature rollout (not just database cutover)

See: `docs/18-microservices-architecture.md` Section 10: Deployment Checklist
