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
make up          # boots postgres, redis, qdrant, centrifugo, minio, core, ai, web
make seed        # creates a demo org + bot
make test        # runs the full test suite
make codegen     # regenerates typed API hooks from OpenAPI
```

Open:
- Web app: http://localhost:3000
- API docs: http://localhost:8080/swagger-ui
- Centrifugo admin: http://localhost:8000 (password in `.env`)
- Grafana: http://localhost:3001 (admin / admin)

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

---

## License

All rights reserved. Personal project — not yet open source.
