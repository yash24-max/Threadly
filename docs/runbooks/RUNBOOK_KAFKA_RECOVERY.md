# Runbook: Kafka Consumer Lag & Dead-Letter-Queue Recovery

**Severity**: Medium (High if lag > 5 minutes)  
**Runbook Owner**: Platform Engineer  
**Last Updated**: 2025-05-24

---

## Overview

During microservices migration Phase 2-3, Kafka carries critical event streams across all services:
- `conv.msg.*` — Conversation messages
- `flow.exec.*` — Flow execution events
- `kb.indexed` — Knowledge base indexing status
- `lead.created` — CRM lead creation
- `billing.*` — Usage metering, subscription changes

If a service consumer falls behind, **lag accumulates** and messages may be replayed incorrectly.  
If **poison pill** (bad message) appears, consumer crashes and creates **dead-letter-queue (DLQ)** backlog.

This runbook covers:
1. Detecting consumer lag
2. Investigating stuck consumers
3. Replaying messages from DLQ
4. Rebalancing partitions

---

## Section 1: Detect Consumer Lag

### Alert: Consumer Lag > 60 seconds

Prometheus alert fires when:
```
kafka.consumer.lag{service="conversation-service"} > 60000
```

### Manual Check

```bash
# SSH into Kafka broker
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe

# Output:
# GROUP                        TOPIC      PARTITION CURRENT-OFFSET LOG-END-OFFSET LAG
# conversation-service-consumer conv.msg  0         12345          12400          55
# conversation-service-consumer conv.msg  1         23456          23456          0
```

**Lag = LOG-END-OFFSET - CURRENT-OFFSET**

If LAG > 60000 (60 seconds):
1. Check if consumer service is running
2. Check service logs for errors
3. Restart consumer if stuck

---

## Section 2: Investigate Consumer Restart

### Step 2.1: Check Service Health

```bash
# Check if consumer service is up
curl http://conversation-service:3005/health

# If 503 or timeout:
docker logs conversation-service | tail -100
```

### Step 2.2: Common Failure Patterns

#### Pattern A: Database Connection Lost
```
Error: could not connect to database
Lag growing: YES
Consumer group status: REBALANCING
```

**Action:**
```bash
# Check database connectivity
docker exec postgres psql -U threadly -c "SELECT 1"

# Check conversation-service config
docker exec conversation-service env | grep DATABASE_URL

# Restart service
docker restart conversation-service

# Monitor lag
watch -n 5 'docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe'
```

#### Pattern B: Message Parsing Error (Poison Pill)
```
Error: Could not deserialize message: ... unexpected field 'new_field'
Lag STUCK at same offset: YES
Consumer group status: REBALANCING (repeatedly)
```

**Action:**
1. Move poison pill to DLQ
2. Restart consumer
3. Replay DLQ later with fix

```bash
# Identify poison pill offset
docker logs conversation-service | grep "unexpected field" | head -1

# Move to DLQ topic
docker exec kafka kafka-simple-consumer-shell.sh \
  --broker-list localhost:9092 \
  --topic conv.msg \
  --partition 0 \
  --offset 12345 \
  --max-messages 1 >> dlq_messages.json

# Send to DLQ topic manually (or use custom tool)
docker exec kafka kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic conv.msg.dlq < dlq_messages.json

# Remove poison pill from main topic (or skip in consumer config):
# kafka.consumer.skip.offset.gaps=true

# Restart service
docker restart conversation-service

# Monitor recovery
watch -n 5 'docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe'
```

#### Pattern C: Processing Timeout (Slow Consumer)
```
Error: (none - consumer is running but slow)
Lag growing slowly: YES
Consumer group status: STABLE
Service logs: Processing X messages/sec (e.g., 5/sec)
```

**Action:**
1. Check for resource constraints (CPU, memory)
2. Scale consumer threads
3. Optimize message processing logic

```bash
# Check resource usage
docker stats conversation-service

# If CPU > 80% or Memory > 85%:
# Increase resources in docker-compose.yml
# services:
#   conversation-service:
#     deploy:
#       resources:
#         limits:
#           cpus: '2.0'
#           memory: 2G

docker-compose up -d conversation-service

# Or increase consumer threads in application.properties:
# kafka.consumer.threads=10  # increase from default

# Restart service
docker restart conversation-service
```

---

## Section 3: Replay Messages from Dead-Letter-Queue

DLQ topics (e.g., `conv.msg.dlq`) collect messages that could not be processed.

### Step 3.1: Assess DLQ Backlog

```bash
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-dlq-consumer \
  --describe

# If LAG > 1000: address immediately
```

### Step 3.2: Manual Replay (After Fix)

Scenario: Bad message format in version 1.0, fixed in version 1.1

1. **Deploy fix** (new service version)
2. **Prepare replay script**:

```bash
#!/bin/bash
# dlq_replay.sh - Replay messages from DLQ to main topic

DLQ_TOPIC="conv.msg.dlq"
MAIN_TOPIC="conv.msg"
BOOTSTRAP_SERVER="localhost:9092"

# Read messages from DLQ, optionally transform, send back to main topic
docker exec kafka kafka-console-consumer.sh \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic $DLQ_TOPIC \
  --from-beginning \
  --timeout-ms 5000 | \
  jq '.version = "1.1" | .fixed_at = now' | \
  docker exec -i kafka kafka-console-producer.sh \
    --broker-list $BOOTSTRAP_SERVER \
    --topic $MAIN_TOPIC

echo "Replay complete. Monitor consumer lag."
```

3. **Execute and monitor**:

```bash
bash dlq_replay.sh

# Monitor lag (should drop as consumer processes replayed messages)
watch -n 5 'docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe'

# Once lag is 0, verify database consistency
curl http://conversation-service:3005/health/deep
```

---

## Section 4: Rebalance Partitions

If a broker crashes or partition leadership changes, consumers may hang during rebalancing.

### Symptoms:
```
Consumer group status: REBALANCING (for >2 minutes)
No movement in lag
Service logs: "Rejoin group..."
```

### Fix: Force Rebalance

```bash
# Option 1: Restart consumer service
docker restart conversation-service

# Option 2: Reset consumer offset (⚠️ data loss risk)
# ONLY if messages are safely duplicated in database
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --reset-offsets \
  --to-latest \
  --execute

# Restart service
docker restart conversation-service

# Option 3: Delete and recreate consumer group
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --delete \
  --group conversation-service-consumer

# Service will auto-create new consumer group on restart
docker restart conversation-service
```

---

## Section 5: Escalation & Alerts

### Page On-Call If:
- Lag > 5 minutes on any service
- DLQ backlog > 10,000 messages
- Consumer group repeatedly rebalancing (> 5 times/hour)
- Message loss suspected

### Rollback Consideration:
If Kafka unrecoverable during Phase 2:
1. Switch to Phase 1 (shadow mode) — read-only to services
2. Revert Nginx routing to monolith-only
3. Investigate offline (run integration tests)
4. Retry Phase 2 after fix

### Contact:
- **Kafka Admin**: #infrastructure Slack
- **Service Owner**: Check docs/18-microservices-architecture.md for service owner
- **On-Call**: PagerDuty rotation

---

## Appendix: Monitoring Dashboard

Grafana dashboard: `Kafka Health → Consumer Lag`

Key metrics:
```
kafka.consumer.lag{service}          — Milliseconds behind log-end
kafka.consumer.lag.records{service}  — Count of unprocessed messages
kafka.broker.replica.lag{partition}  — ISR (in-sync-replicas) lag
kafka.topic.partition.in.sync        — 0 = replication lag issue
```

Set alerts:
```
kafka.consumer.lag > 60000   → WARNING
kafka.consumer.lag > 300000  → CRITICAL (page on-call)
kafka.broker.replica.lag > 1000 → WARNING
```
