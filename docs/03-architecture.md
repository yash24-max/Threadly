# Architecture

## Principles
1. **Modular monolith** — one Spring Boot app, strict package boundaries, extract to microservice only when traffic demands.
2. **Centrifugo owns realtime** — Spring Boot stays stateless; Centrifugo manages WebSocket connections at scale.
3. **AI in Python** — LangChain, vector DBs, LLM SDKs are 18 months ahead in Python.
4. **Transactional outbox** — events are written in the same DB transaction as the business write; a poller delivers them. Never lose a message.
5. **Row-level multi-tenancy** — mandatory `org_id` on every tenant table, enforced by Hibernate filter from JWT.

## High-level diagram

```
                     ┌─────────────────────┐
                     │    threadly-web     │
                     │    (Next.js 15)     │
                     │  Builder·Dashboard  │
                     └─────────┬───────────┘
                               │ HTTPS REST + SSE
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                  threadly-core (Spring Boot 3)               │
│  identity │ workspace │ flow │ runtime │ conversation         │
│  knowledge │ analytics │ agent │ billing │ outbox │ proxy     │
└────┬─────────────┬──────────────┬──────────────┬────────────-┘
     │ HTTP/2      │ Centrifugo   │ JDBC          │ Redis
     ▼             │ Publish API  ▼               ▼
┌──────────┐       ▼      ┌─────────────┐  ┌──────────────┐
│threadly  │  ┌─────────┐ │ PostgreSQL  │  │  Redis 7     │
│   -ai    │  │Centrifugo│ │   16       │  │  sessions /  │
│ FastAPI  │  │ (Go)    │ │  + Flyway  │  │  rate-limit  │
│ LLM/RAG  │  │channels │ │  JSONB     │  │  cache       │
└────┬─────┘  │presence │ └─────────────┘  └──────────────┘
     │        │history  │
     ▼        │proxy    │
┌──────────┐  └────┬────┘
│  Qdrant  │       │ WSS (sticky-less)
│  vector  │       ▼
└──────────┘  ┌──────────────────────────────────────┐
              │         threadly-widget               │
              │  <script> embedded on customer site   │
              │  Connects only to Centrifugo          │
              └──────────────────────────────────────┘
```

## Message flow — visitor sends a message

1. **Widget** → publishes RPC to Centrifugo channel `chat:{botId}:{visitorId}`
2. **Centrifugo proxy** → POSTs to `threadly-core /proxy/publish` with JWT-verified identity
3. **Core runtime** → persists message, advances flow, calls `threadly-ai` if AI node
4. **threadly-ai** → queries Qdrant (RAG), calls Anthropic, streams tokens back
5. **Core** → publishes reply (token-by-token) to Centrifugo HTTP publish API
6. **Centrifugo** → fans out to widget + dashboard (if agent watching)

## Centrifugo channels

| Channel | Subscribers | Publisher |
|---|---|---|
| `chat:{botId}:{visitorId}` | widget, watching agents | core runtime |
| `dashboard:{orgId}` | web dashboard | core (new conversations, counts) |
| `agent:{agentId}` | individual agent tab | core (assignment notifications) |

## Core modules

| Module | Key responsibility |
|---|---|
| `identity` | Users, orgs, JWT issuance (access + Centrifugo tokens), password reset |
| `workspace` | Bots CRUD, API keys, widget theme config |
| `flow` | Flow JSON CRUD, schema validation, versioning (draft/published) |
| `runtime` | Flow interpreter, session state (Redis), node executors |
| `conversation` | Transcript read/search, status management |
| `knowledge` | Document upload (MinIO/R2), ingestion dispatch via outbox |
| `analytics` | Event aggregation, dashboard rollup queries |
| `agent` | Human-handoff inbox, assignment, takeover |
| `billing` | Plans/subscriptions stub (Stripe deferred) |
| `outbox` | Transactional outbox poller → publishes to Centrifugo |
| `proxy` | Centrifugo proxy hooks (connect / subscribe / publish) |
| `common` | TenantContext, Hibernate filter, JWT provider, RFC 7807 errors |
