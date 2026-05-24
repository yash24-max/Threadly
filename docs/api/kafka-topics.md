# Kafka Topics — Event Streaming Schema

**Last Updated**: 2025-05-24  
**Kafka Version**: 7.5.0  
**Replication Factor**: 1 (development) / 3 (production)  
**Partitions**: 3 per topic (scalable for throughput)

---

## Table of Contents

1. [Overview](#overview)
2. [Topic Management](#topic-management)
3. [Event Topics](#event-topics)
4. [Schemas](#schemas)
5. [Consumer Groups](#consumer-groups)
6. [Error Handling (Dead Letter Queues)](#error-handling-dead-letter-queues)
7. [Monitoring](#monitoring)

---

## Overview

Threadly uses Kafka for asynchronous event propagation between microservices. All events follow a consistent schema and are published by services, consumed by other services.

**Total Topics**: 9 (one per service domain)

**Event Flow Pattern**:
```
Service A (producer)
    ↓
Topic: domain-events
    ↓
[Consumer Group: service-b-consumer]
    ↓
Service B (consumer)
```

---

## Topic Management

### List All Topics

```bash
# Connect to Kafka broker
docker exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# Output:
# user-events
# org-events
# flow-events
# session-events
# conversation-events
# kb-events
# analytics-events
# billing-events
# integration-events
```

### Create New Topic

```bash
docker exec kafka kafka-topics.sh \
  --create \
  --topic custom-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000  # 7 days
```

### Delete Topic

```bash
docker exec kafka kafka-topics.sh \
  --delete \
  --topic custom-topic \
  --bootstrap-server localhost:9092
```

---

## Event Topics

### 1. user-events (Identity Service)

**Producers**: Identity Service  
**Consumers**: Workspace Service, Analytics Service, Billing Service  
**Retention**: 7 days (604800000 ms)  
**Partitions**: 3 (partition key: `org_id`)

**Schema**:
```json
{
  "event_id": "evt_123abc456def",
  "event_type": "user.created|user.updated|user.deleted|user.activated|user.deactivated",
  "aggregate_id": "user_789xyz",
  "aggregate_type": "User",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_123",
  "tenant_id": "tenant_456",
  "source": "identity-service",
  "version": "1.0",
  "data": {
    "user_id": "user_789xyz",
    "email": "alice@company.com",
    "username": "alice.smith",
    "first_name": "Alice",
    "last_name": "Smith",
    "status": "active|inactive|pending",
    "role": "admin|user|viewer",
    "metadata": {
      "last_login": "2025-05-24T09:00:00Z",
      "login_count": 42,
      "api_key_count": 3
    }
  },
  "correlation_id": "corr_abc123",
  "causation_id": "evt_122",
  "metadata": {
    "user_agent": "Mozilla/5.0...",
    "ip_address": "192.168.1.100"
  }
}
```

**Events**:
- `user.created` — New user registered
- `user.updated` — User profile updated
- `user.deleted` — User deleted
- `user.activated` — User activated
- `user.deactivated` — User deactivated

---

### 2. org-events (Identity Service)

**Producers**: Identity Service, Workspace Service  
**Consumers**: All services (broadcast)  
**Retention**: 30 days  
**Partitions**: 3 (partition key: `org_id`)

**Schema**:
```json
{
  "event_id": "evt_def789ghi",
  "event_type": "org.created|org.updated|org.deleted|org.subscription_changed",
  "aggregate_id": "org_456",
  "aggregate_type": "Organization",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "tenant_id": "tenant_456",
  "source": "identity-service",
  "version": "1.0",
  "data": {
    "org_id": "org_456",
    "org_name": "Acme Corp",
    "status": "active|suspended|deleted",
    "plan": "free|pro|enterprise",
    "member_count": 5,
    "bot_count": 3,
    "settings": {
      "language": "en",
      "timezone": "UTC",
      "sso_enabled": true
    }
  },
  "correlation_id": "corr_abc123",
  "causation_id": "evt_121"
}
```

---

### 3. flow-events (Flow Service)

**Producers**: Flow Service  
**Consumers**: Runtime Service, Analytics Service  
**Retention**: 14 days  
**Partitions**: 3 (partition key: `bot_id`)

**Schema**:
```json
{
  "event_id": "evt_jkl345mno",
  "event_type": "flow.created|flow.updated|flow.published|flow.deleted|flow.version_created",
  "aggregate_id": "flow_999",
  "aggregate_type": "Flow",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "bot_id": "bot_123",
  "source": "flow-service",
  "version": "1.0",
  "data": {
    "flow_id": "flow_999",
    "bot_id": "bot_123",
    "flow_name": "Customer Support Flow",
    "status": "draft|published|archived",
    "node_count": 12,
    "edge_count": 11,
    "published_version": 3,
    "nodes": [
      {
        "id": "node_1",
        "type": "message|branch|action|handoff",
        "data": {}
      }
    ],
    "metadata": {
      "created_by": "user_789xyz",
      "updated_by": "user_789xyz",
      "last_tested": "2025-05-23T14:20:00Z"
    }
  },
  "correlation_id": "corr_abc123"
}
```

---

### 4. session-events (Runtime Service)

**Producers**: Runtime Service  
**Consumers**: Conversation Service, Analytics Service  
**Retention**: 7 days  
**Partitions**: 3 (partition key: `bot_id`)

**Schema**:
```json
{
  "event_id": "evt_pqr567stu",
  "event_type": "session.started|session.message|session.action_executed|session.ended|session.paused|session.resumed",
  "aggregate_id": "session_aaa111",
  "aggregate_type": "Session",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "bot_id": "bot_123",
  "flow_id": "flow_999",
  "session_id": "session_aaa111",
  "source": "runtime-service",
  "version": "1.0",
  "data": {
    "session_id": "session_aaa111",
    "bot_id": "bot_123",
    "flow_id": "flow_999",
    "visitor_id": "visitor_xyz123",
    "status": "active|paused|completed|error",
    "current_node": "node_5",
    "message": "Hello, how can I help?",
    "user_input": "I have a billing question",
    "ai_response": "I can help with that...",
    "metadata": {
      "duration_seconds": 45,
      "message_count": 3,
      "context": {
        "language": "en",
        "timezone": "UTC",
        "custom_data": {}
      }
    }
  },
  "correlation_id": "corr_abc123"
}
```

---

### 5. conversation-events (Conversation Service)

**Producers**: Conversation Service, Runtime Service  
**Consumers**: Analytics Service, Billing Service  
**Retention**: 90 days  
**Partitions**: 3 (partition key: `bot_id`)

**Schema**:
```json
{
  "event_id": "evt_vwx789yza",
  "event_type": "conversation.started|conversation.message|conversation.lead_captured|conversation.handed_off|conversation.ended",
  "aggregate_id": "conversation_bbb222",
  "aggregate_type": "Conversation",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "bot_id": "bot_123",
  "conversation_id": "conversation_bbb222",
  "source": "conversation-service",
  "version": "1.0",
  "data": {
    "conversation_id": "conversation_bbb222",
    "bot_id": "bot_123",
    "visitor_id": "visitor_xyz123",
    "visitor_email": "customer@example.com",
    "visitor_name": "John Doe",
    "status": "open|closed|handed_off",
    "message_count": 7,
    "lead_captured": true,
    "lead_data": {
      "email": "john@example.com",
      "phone": "+1-555-1234",
      "company": "Acme Inc",
      "custom_fields": {}
    },
    "ai_response_count": 5,
    "human_handoff": {
      "handed_off_at": "2025-05-24T10:35:00Z",
      "handed_off_to": "agent_123",
      "reason": "Request for complex issue"
    },
    "messages": [
      {
        "role": "visitor|ai|human",
        "content": "...",
        "timestamp": "2025-05-24T10:30:20Z"
      }
    ]
  },
  "correlation_id": "corr_abc123"
}
```

---

### 6. kb-events (Knowledge Service)

**Producers**: Knowledge Service  
**Consumers**: Runtime Service (RAG queries), Analytics Service  
**Retention**: 30 days  
**Partitions**: 3 (partition key: `kb_id`)

**Schema**:
```json
{
  "event_id": "evt_bcd012efg",
  "event_type": "kb.created|kb.updated|kb.document_added|kb.document_removed|kb.reindexed|kb.search_executed",
  "aggregate_id": "kb_111",
  "aggregate_type": "KnowledgeBase",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "kb_id": "kb_111",
  "source": "knowledge-service",
  "version": "1.0",
  "data": {
    "kb_id": "kb_111",
    "kb_name": "Customer Support Docs",
    "document_count": 42,
    "indexed_chunks": 1205,
    "total_tokens": 450000,
    "embedding_model": "text-embedding-3-small",
    "last_indexed": "2025-05-24T10:20:00Z",
    "documents": [
      {
        "document_id": "doc_456",
        "filename": "faq.pdf",
        "size_bytes": 125000,
        "chunk_count": 25,
        "indexed_at": "2025-05-24T10:20:00Z"
      }
    ],
    "search_query": "billing issues",
    "top_results": [
      {
        "document_id": "doc_456",
        "score": 0.92,
        "content_preview": "..."
      }
    ]
  },
  "correlation_id": "corr_abc123"
}
```

---

### 7. analytics-events (Analytics Service)

**Producers**: Analytics Service (aggregator)  
**Consumers**: External data warehouse, reporting systems  
**Retention**: 365 days  
**Partitions**: 3 (partition key: `bot_id`)

**Schema**:
```json
{
  "event_id": "evt_hij345klm",
  "event_type": "analytics.daily_summary|analytics.hourly_snapshot|analytics.metric_computed",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "bot_id": "bot_123",
  "source": "analytics-service",
  "version": "1.0",
  "data": {
    "period": "2025-05-24",
    "metrics": {
      "conversations_started": 125,
      "conversations_completed": 98,
      "conversion_rate": 78.4,
      "average_session_length": 245,
      "leads_captured": 45,
      "handoffs": 12,
      "satisfaction_score": 4.3,
      "ai_accuracy": 92.5,
      "error_rate": 0.2,
      "peak_hour": 14,
      "total_messages": 1205,
      "avg_response_time_ms": 450
    }
  },
  "correlation_id": "corr_abc123"
}
```

---

### 8. billing-events (Billing Service)

**Producers**: Billing Service  
**Consumers**: Analytics Service, reporting systems  
**Retention**: 365 days (compliance requirement)  
**Partitions**: 3 (partition key: `org_id`)

**Schema**:
```json
{
  "event_id": "evt_nop678qrs",
  "event_type": "billing.subscription_created|billing.subscription_updated|billing.payment_succeeded|billing.payment_failed|billing.invoice_generated|billing.usage_reported",
  "aggregate_id": "subscription_111",
  "aggregate_type": "Subscription",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "source": "billing-service",
  "version": "1.0",
  "data": {
    "subscription_id": "subscription_111",
    "org_id": "org_456",
    "plan": "pro|enterprise|custom",
    "status": "active|suspended|canceled",
    "billing_cycle": "monthly|annual",
    "amount_cents": 9900,
    "currency": "USD",
    "next_billing_date": "2025-06-24",
    "usage": {
      "conversations": 1245,
      "messages": 12450,
      "leads": 85,
      "knowledge_base_size_mb": 250
    },
    "overages": {
      "conversation_overage_cost": 1000,
      "total_overage_cost": 1000
    },
    "payment_method": "card_xxxx1234",
    "invoice_id": "inv_123",
    "metadata": {
      "stripe_subscription_id": "sub_abc123",
      "invoice_number": "INV-2025-05-001"
    }
  },
  "correlation_id": "corr_abc123"
}
```

---

### 9. integration-events (Integration Service)

**Producers**: Integration Service  
**Consumers**: Workspace Service, Analytics Service  
**Retention**: 30 days  
**Partitions**: 3 (partition key: `bot_id`)

**Schema**:
```json
{
  "event_id": "evt_tuv901wxy",
  "event_type": "integration.installed|integration.configured|integration.action_executed|integration.uninstalled|integration.error",
  "aggregate_id": "integration_abc",
  "aggregate_type": "Integration",
  "timestamp": "2025-05-24T10:30:15.123Z",
  "org_id": "org_456",
  "bot_id": "bot_123",
  "source": "integration-service",
  "version": "1.0",
  "data": {
    "integration_id": "integration_abc",
    "bot_id": "bot_123",
    "integration_type": "slack|github|zapier|custom",
    "status": "active|inactive|error",
    "action": "send_message|create_ticket|sync_data",
    "config": {
      "webhook_url": "https://...",
      "api_key": "***",
      "timeout_ms": 5000
    },
    "execution": {
      "status": "success|failure",
      "duration_ms": 245,
      "error_message": null,
      "payload": {}
    },
    "metadata": {
      "installed_by": "user_789xyz",
      "last_executed": "2025-05-24T10:25:00Z",
      "execution_count": 1205
    }
  },
  "correlation_id": "corr_abc123"
}
```

---

## Schemas

### Common Event Fields

All events include these standard fields:

| Field | Type | Description |
|-------|------|-------------|
| `event_id` | string | Unique event ID (evt_*) |
| `event_type` | string | Domain.action format |
| `aggregate_id` | string | Primary entity ID |
| `aggregate_type` | string | Entity type |
| `timestamp` | ISO8601 | Event creation time (UTC) |
| `org_id` | string | Organization ID (tenancy) |
| `source` | string | Service name |
| `version` | string | Schema version |
| `correlation_id` | string | Request trace ID |
| `causation_id` | string | Previous event ID (if chained) |
| `data` | object | Event-specific payload |

### Event Versioning

Topics support multiple event versions:

```bash
# Topic with versioned events
topic: user-events

# Consumers must handle multiple versions
// Consumer
if (event.version == "1.0") {
  // Handle v1 schema
} else if (event.version == "2.0") {
  // Handle v2 schema (backward compatible)
}
```

---

## Consumer Groups

Kafka consumer groups enable parallel processing:

### List Consumer Groups

```bash
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list

# Output:
# identity-service-consumer
# workspace-service-consumer
# flow-service-consumer
# runtime-service-consumer
# conversation-service-consumer
# knowledge-service-consumer
# analytics-service-consumer
# billing-service-consumer
# integration-service-consumer
```

### Monitor Consumer Lag

```bash
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe

# Output:
# TOPIC                 PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
# conversation-events   0          12450           12450           0
# conversation-events   1          11200           11200           0
# conversation-events   2          10800           10800           0
```

**Healthy lag**: 0-10  
**Warning lag**: 10-100  
**Critical lag**: > 100 (service can't keep up)

### Reset Consumer Offset

```bash
# Reset to earliest (reprocess all messages)
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --reset-offsets \
  --to-earliest \
  --execute

# Reset to latest (skip backlog)
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --reset-offsets \
  --to-latest \
  --execute
```

---

## Error Handling (Dead Letter Queues)

Messages that fail processing are sent to DLQ topics:

### DLQ Topics

```
Primary Topic → Failed Messages → DLQ Topic (suffix: -dlq)

conversation-events → conversation-events-dlq
```

### Monitor DLQ

```bash
# Check DLQ message count
docker exec kafka kafka-run-class.sh kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec \
  --attributes Count \
  --reporting-interval 1000 | grep conversation-events-dlq
```

### Replay Messages from DLQ

```bash
# Copy messages from DLQ back to primary topic (manual replay)
docker exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic conversation-events-dlq \
  --from-beginning | \
docker exec -i kafka kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic conversation-events
```

---

## Monitoring

### Key Metrics

| Metric | Threshold | Action |
|--------|-----------|--------|
| Consumer lag | > 100 | Restart consumer, check service logs |
| DLQ message count | > 10 | Investigate failures, replay messages |
| Topic partition imbalance | > 20% difference | Rebalance partitions |
| Message throughput | Drops > 50% | Check producer health |
| Commit latency | > 5s | Check broker performance |

### Grafana Dashboard

Create dashboard for Kafka monitoring:

```json
{
  "panels": [
    {
      "title": "Consumer Lag by Topic",
      "targets": [
        {
          "expr": "kafka_consumergroup_lag"
        }
      ]
    },
    {
      "title": "Messages Per Second",
      "targets": [
        {
          "expr": "kafka_topic_messages_in_total"
        }
      ]
    },
    {
      "title": "DLQ Message Count",
      "targets": [
        {
          "expr": "kafka_topic_partitions{topic=~'.*-dlq'}"
        }
      ]
    }
  ]
}
```

---

## Best Practices

1. **Partition Key Selection** — Use `org_id` or `bot_id` for even distribution
2. **Event Idempotency** — Include `event_id` to detect duplicates
3. **Correlation Tracking** — Propagate `correlation_id` across services
4. **Consumer Error Handling** — Send failures to DLQ, alert team
5. **Message Retention** — Balance storage cost vs compliance needs
6. **Monitoring** — Alert on consumer lag > 100 messages

---

## Related Documentation

- [REST Endpoints](./rest-endpoints.md) — HTTP API reference
- [Runbook: Kafka Recovery](../runbooks/RUNBOOK_KAFKA_RECOVERY.md) — Troubleshooting guide
- [Observability](../reference/14-observability.md) — Monitoring setup and metrics

