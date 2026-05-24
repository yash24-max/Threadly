# Threadly

> **Build AI chatbots that remember every thread.**

An AI-first chatbot builder + embeddable website chat widget. Businesses sign up, design a flow in a visual builder, upload their knowledge base, paste a `<script>` snippet on their site, and have an AI assistant live in minutes.

Phase 0 ships a focused chatbot product. Future phases expand to omnichannel (WhatsApp, Instagram), full inbox, and CRM-lite.

---

## Repo layout

```
threadly-core/    Spring Boot 3 modular monolith (Java 21)
threadly-ai/      Python FastAPI sidecar for LLM + RAG
threadly-web/     Next.js 15 builder + dashboard
threadly-widget/  Preact embeddable chat widget (< 35 KB gzipped)
infra/            Docker Compose, Centrifugo config, Grafana dashboards
docs/             Product, architecture, API, design system
scripts/          One-shot dev scripts (bootstrap, codegen, seed)
```

---

## Quickstart (local dev)

Prerequisites: Docker Desktop, Java 21, Node 20+, Python 3.12, Make.

```bash
make up          # boots postgres, redis, qdrant, kafka, consul, minio + 9 microservices + ai, web
make seed        # creates a demo org + bot
make test        # runs the full test suite
make codegen     # regenerates typed API hooks from OpenAPI
```

Open:
- Web app: http://localhost:3000
- API Gateway: http://localhost:8080
- Identity Service: http://localhost:3001/health
- Workspace Service: http://localhost:3002/health
- Flow Service: http://localhost:3003/health
- Runtime Service: http://localhost:3004/health
- Conversation Service: http://localhost:3005/health
- Knowledge Service: http://localhost:3006/health
- Analytics Service: http://localhost:3007/health
- Billing Service: http://localhost:3008/health
- Integration Service: http://localhost:3009/health
- Grafana: http://localhost:3000/monitoring (admin / admin)

---

## Documentation

| Doc | What it covers |
|---|---|
| [00-vision.md](docs/00-vision.md) | Product vision, positioning, name |
| [01-mvp-scope.md](docs/01-mvp-scope.md) | Phase 0 scope (what's in, what's out) |
| [02-roadmap.md](docs/02-roadmap.md) | Phase 1–4 plans |
| [03-architecture.md](docs/03-architecture.md) | Services, data flow, Centrifugo integration |
| [04-tech-stack.md](docs/04-tech-stack.md) | Every tool chosen and why |
| [05-data-model.md](docs/05-data-model.md) | ER diagram, tables, tenancy |
| [06-api-contract.md](docs/06-api-contract.md) | REST endpoints + Centrifugo channels |
| [07-flow-spec.md](docs/07-flow-spec.md) | Flow JSON schema + runtime semantics |
| [08-ai-orchestration.md](docs/08-ai-orchestration.md) | Prompts, RAG, memory, evals |
| [09-widget-embed-guide.md](docs/09-widget-embed-guide.md) | Public docs draft |
| [10-dev-setup.md](docs/10-dev-setup.md) | Local dev environment |
| [11-deployment.md](docs/11-deployment.md) | Staging + prod deploy |
| [12-design-system.md](docs/12-design-system.md) | Tokens, motion, typography |
| [13-security.md](docs/13-security.md) | Threat model, tenancy, secrets |
| [14-observability.md](docs/14-observability.md) | Tracing, logs, metrics, LLM cost |
| [15-integrations.md](docs/15-integrations.md) | External integrations (Stripe, Slack, etc.) |
| [16-billing.md](docs/16-billing.md) | Pricing, metering, Stripe webhooks |
| [17-crm.md](docs/17-crm.md) | Lead tracking, handoff, CRM sync |
| [18-microservices-architecture.md](docs/18-microservices-architecture.md) | Microservices topology, database-per-service, Kafka events |
| [DEPLOYMENT_PLAN.md](docs/DEPLOYMENT_PLAN.md) | Phase 1-4 migration timeline + go/no-go criteria |
| [RUNBOOK_MIGRATION.md](docs/RUNBOOK_MIGRATION.md) | Step-by-step Phase 1-3 migration procedures |
| [RUNBOOK_SERVICE_RESTART.md](docs/RUNBOOK_SERVICE_RESTART.md) | Emergency service restart procedures |
| [RUNBOOK_KAFKA_RECOVERY.md](docs/RUNBOOK_KAFKA_RECOVERY.md) | Kafka consumer lag + DLQ recovery |
| [RUNBOOK_ROLLBACK.md](docs/RUNBOOK_ROLLBACK.md) | Emergency rollback from Phase 3 to Phase 1 |

---

## License

All rights reserved. Personal project — not yet open source.
