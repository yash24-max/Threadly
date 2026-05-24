# Observability

## Stack

| Layer | Tool |
|---|---|
| Traces | OpenTelemetry → Grafana Tempo (or Honeycomb) |
| Metrics | Micrometer → Prometheus → Grafana |
| Logs | Logback JSON → Grafana Loki |
| LLM traces | Langfuse |
| Errors | Sentry |
| Product analytics | PostHog |

## Trace propagation
One `traceId` flows: Browser → Next.js → Core → threadly-ai → Centrifugo  
Headers: `traceparent` (W3C standard)

## Key metrics

| Metric | Alert threshold |
|---|---|
| `chat.message.latency_p50` | > 2s |
| `chat.message.latency_p99` | > 5s |
| `ai.call.error_rate` | > 5% over 5 min |
| `centrifugo.connections` | > 80% of limit |
| `db.pool.wait_time` | > 100ms |
| `kb.ingestion.queue_depth` | > 50 |
| `org.daily_token_budget.pct` | > 80% |

## Log format (structured JSON)
```json
{
  "timestamp": "2025-05-21T10:00:00.000Z",
  "level": "INFO",
  "service": "threadly-core",
  "traceId": "abc123",
  "spanId": "def456",
  "orgId": "org-uuid",        // masked in WARN/ERROR for PII
  "botId": "bot-uuid",
  "conversationId": "conv-uuid",
  "msg": "Flow runtime advanced to node ai_reply",
  "nodeId": "n2",
  "durationMs": 45
}
```

## Dashboards (Grafana pre-provisioned)

1. **Overview** — RPS, error rate, p50/p99 latency across all services
2. **Chat runtime** — messages/min, active sessions, handoff rate, AI call rate
3. **AI cost** — tokens/day, cost/org, top cost bots, provider breakdown
4. **KB ingestion** — queue depth, ingestion time, error rate
5. **Centrifugo** — active connections, subscriptions, publish rate
6. **Infrastructure** — DB pool, Redis memory, CPU/mem per container

## LLM tracing (Langfuse)

Every AI call creates a Langfuse trace:
- `input`: full prompt (system + context + history + user message)
- `output`: completion text
- `metadata`: model, tokens_in, tokens_out, cost_usd, latency_ms, org_id, bot_id

Allows: prompt debugging, cost audit, regression detection.
