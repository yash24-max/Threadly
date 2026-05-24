# Runbook: Service Restart Without Data Loss

**Purpose**: Safely restart individual microservices without losing data  
**Audience**: On-call engineers, DevOps  
**Time Required**: 5-10 minutes per service  

---

## Quick Reference

```bash
# Restart single service (with grace period)
kubectl rollout restart deployment/identity-service -n threadly

# Rolling restart all services (zero downtime)
for svc in identity flow workspace runtime conversation knowledge analytics billing integration; do
  kubectl rollout restart deployment/${svc}-service -n threadly
  kubectl rollout status deployment/${svc}-service -n threadly
done

# Monitor restart
kubectl get pods -n threadly -w
kubectl logs -f deployment/identity-service -n threadly
```

---

## Before Restarting

### 1. Check Service Status

```bash
# Is the service healthy?
curl http://service:3001/health

# Any active errors?
kubectl logs deployment/identity-service -n threadly | tail -20 | grep -i error

# Any recent restarts?
kubectl get deployment identity-service -n threadly
# Check RESTARTS column — if > 3 in 5 mins, investigate root cause
```

### 2. Drain Active Sessions (Runtime Service Only)

If restarting **runtime-service** (handles active sessions):

```bash
# Query active sessions
curl -X GET http://runtime-service:3004/sessions/active | jq '.sessions | length'

# Wait for sessions to complete (max 5 min), or:
# Optionally notify users: "Brief maintenance in 2 minutes"
sleep 120

# Check again
curl -X GET http://runtime-service:3004/sessions/active | jq '.sessions | length'
# Should be 0 or very low
```

### 3. Notify Users (if many active sessions)

Send Slack message:
> Brief maintenance on conversation processing. May see 1-2 min delays. ETA: 10 min.

---

## Restart Procedure

### Option A: Rolling Restart (Recommended - Zero Downtime)

```bash
# Kubernetes rolling restart (replaces pods 1 at a time)
kubectl rollout restart deployment/identity-service -n threadly

# Wait for rollout to complete
kubectl rollout status deployment/identity-service -n threadly -w
# Output:
# Waiting for rollout to finish: 1 old replicas, 2 new replicas ...
# Waiting for rollout to finish: 1 old replicas, 2 new replicas ...
# deployment.apps/identity-service successfully rolled out

# Verify service is healthy
kubectl get pods -n threadly -l app=identity-service
# All pods should be READY 1/1, STATUS Running

curl http://identity-service:3001/health
# Should return 200 OK immediately
```

### Option B: Direct Pod Restart (Fast, Brief Downtime)

```bash
# Get pod name
POD=$(kubectl get pods -n threadly -l app=identity-service -o jsonpath='{.items[0].metadata.name}')

# Delete pod (replacement starts immediately)
kubectl delete pod $POD -n threadly

# Monitor restart
kubectl get pod $POD -n threadly -w
# Should show: ContainerCreating → Running (30-60 seconds)

# Verify service is ready
kubectl wait --for=condition=ready pod $POD -n threadly --timeout=60s
curl http://identity-service:3001/health
```

### Option C: Local Docker Restart (Development Only)

```bash
# For local docker-compose testing
docker-compose restart identity-service

# Verify
docker-compose logs identity-service | tail -10
curl http://localhost:3001/health
```

---

## Post-Restart Verification

### 1. Service Health Check

```bash
# Check endpoint returns 200
curl -w "\nStatus: %{http_code}\n" http://identity-service:3001/health

# Expected: Status: 200
```

### 2. Integration Tests

```bash
# Run smoke tests for this service
mvn test -f threadly-core/pom.xml -Dtest=IdentityServiceTest -DfailIfNoTests=false

# Expected: BUILD SUCCESS (or BUILD SKIPPED if no tests)
```

### 3. Metrics Check

```bash
# Verify metrics are flowing
curl http://identity-service:3001/actuator/metrics | jq '.names | length'
# Should return > 0 (service is instrumented)

# Check for errors in last 2 minutes
kubectl logs deployment/identity-service -n threadly --since=2m | grep -i "ERROR\|EXCEPTION" | wc -l
# Should be 0 or minimal
```

### 4. Cross-Service Communication

```bash
# If this service is called by others, verify they can still reach it
# Example: Runtime Service calls Flow Service
curl -X POST http://localhost:8080/sessions/test/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Test"}'
# Should return 200, not 502/503 (service unavailable)
```

### 5. Kafka Consumer Lag (Optional)

If service consumes Kafka events:

```bash
# Check consumer lag
kafka-consumer-groups.sh --bootstrap-server kafka:9092 \
  --group identity-service-group \
  --describe

# Expected: LAG column shows reasonable lag (< 1000 messages for this service)
```

---

## Service-Specific Considerations

### Identity Service (3001)

**Data at risk**: User sessions, JWT tokens (memory)  
**Safe to restart**: YES (stateless)  
**Recovery time**: 30 seconds

- No user data loss on restart
- All JWT tokens invalidated (users need to re-login)
- Restart during low-traffic window to minimize login surge

### Workspace Service (3002)

**Data at risk**: None (read-heavy)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- No write operations blocked during restart
- Bots continue running via Runtime Service
- Restart any time

### Flow Service (3003)

**Data at risk**: None (read-heavy)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- Active flow executions continue (via Runtime Service)
- Flow publishing may briefly fail (queue on Nginx, retry)

### Runtime Service (3004)

**Data at risk**: Active session state (critical!)  
**Safe to restart**: YES (with caution)  
**Recovery time**: 30 seconds

**Before restart**:
1. Drain active sessions (see "Drain Active Sessions" above)
2. Or notify users of brief session interruption
3. Sessions restart from last saved snapshot (automatic recovery)

**After restart**:
1. Users may need to re-enter messages in current step
2. All session snapshots preserved in database
3. Monitor for session restart errors

### Conversation Service (3005)

**Data at risk**: None (read-heavy, async writes via Kafka)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- Conversation transcripts preserved in database
- Kafka will replay missed events (consumer group offset)

### Knowledge Service (3006)

**Data at risk**: None (reads + async ingestion jobs)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- In-progress KB ingestion jobs (Kafka) will retry automatically
- Qdrant vector DB remains intact

### Analytics Service (3007)

**Data at risk**: None (event aggregation only)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- Analytics data preserved in database
- Kafka will replay any missed events
- Dashboards briefly unavailable during restart

### Billing Service (3008)

**Data at risk**: None (read-heavy)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- Subscription data preserved
- Scheduled billing jobs not affected

### Integration Service (3009)

**Data at risk**: In-flight integration actions (can retry)  
**Safe to restart**: YES  
**Recovery time**: 30 seconds

- Integration credentials preserved
- In-flight actions may retry after restart
- Action logs preserved in database

---

## Troubleshooting Restart Issues

### Pod Stuck in CrashLoopBackOff

```bash
# Check logs for error
kubectl logs deployment/identity-service -n threadly -p  # Previous log
# or
kubectl logs deployment/identity-service -n threadly --tail=50

# Common causes:
# 1. Database connection failed
#    - Check DB credentials in ConfigMap
#    - Verify PostgreSQL is running
#    - Check network policies allow pod → DB

# 2. Config/secret missing
#    - kubectl get secrets -n threadly
#    - kubectl get configmaps -n threadly

# 3. Out of memory
#    - Check pod resource limits: kubectl describe pod <pod-name>
#    - Increase memory if needed

# Fix and retry
kubectl delete pod <pod-name> -n threadly
kubectl logs deployment/identity-service -n threadly -f
```

### Service Slow to Start (> 2 min)

```bash
# Check startup logs
kubectl logs deployment/identity-service -n threadly --timestamps | tail -20

# Common causes:
# 1. Database migrations running (Flyway/Liquibase)
# 2. Schema initialization taking time
# 3. Consul service discovery slow

# Wait for startup to complete, then verify
kubectl rollout status deployment/identity-service -n threadly -w
```

### Restart Triggered by OOMKilled

```bash
# Check memory usage
kubectl describe pod <pod-name> -n threadly | grep -A 5 "Last State"

# Increase memory in deployment
kubectl patch deployment identity-service -n threadly \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"identity-service","resources":{"limits":{"memory":"1Gi"}}}]}}}}'

# Verify
kubectl get deployment identity-service -n threadly -o yaml | grep -A 5 "resources:"
```

### Port Already Bound (Local Docker)

```bash
# Find process using port
lsof -i :3001

# Kill existing container
docker ps | grep identity-service
docker stop <container-id>

# Or change port in docker-compose.override.yml
port:
  - "3011:3001"  # Use port 3011 instead
```

---

## Prevention: Auto-Restart Configuration

### Kubernetes: Configure Liveness Probe

Each deployment should have:

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 3001
  initialDelaySeconds: 30      # Wait 30s before first check
  periodSeconds: 10            # Check every 10s
  timeoutSeconds: 5            # Timeout after 5s
  failureThreshold: 3          # Restart after 3 failures
```

This ensures:
- Unhealthy services auto-restart
- Don't manually intervene for transient failures

### Kubernetes: Configure Resource Requests

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

Prevents:
- OOMKilled restarts
- Resource starvation
- Node evictions

---

## Checklist: Safe Service Restart

- [ ] Service status is healthy before restart (optional, if troubleshooting)
- [ ] For Runtime Service: drain active sessions or notify users
- [ ] Use `kubectl rollout restart` (rolling update)
- [ ] Monitor: `kubectl get pods -w`
- [ ] Verify: `/health` returns 200
- [ ] Run smoke tests (optional)
- [ ] Check logs for errors (last 5 min)
- [ ] Confirm metrics flowing (optional)
- [ ] Update Slack: "Service restart complete"

---

## Emergency: Force Restart (Dangerous)

If normal restart fails and service is blocking traffic:

```bash
# ONLY if rolling restart hangs or fails
kubectl delete deployment identity-service -n threadly  # DESTROYS all pods
kubectl apply -f deploy/identity-service.yaml             # Recreate from manifest

# Or: Force delete stuck pod
kubectl delete pod <pod-name> --grace-period=0 --force -n threadly
```

**Risk**: Temporary service outage, incomplete cleanups  
**Use only**: Last resort, blocking production issue

---

## Related Runbooks

- [RUNBOOK_MIGRATION.md](./RUNBOOK_MIGRATION.md) - Phase 2: Monitor service writes
- [RUNBOOK_KAFKA_RECOVERY.md](./RUNBOOK_KAFKA_RECOVERY.md) - Recover consumer lag
- [RUNBOOK_ROLLBACK.md](./RUNBOOK_ROLLBACK.md) - Full system rollback
