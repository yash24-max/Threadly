# Threadly Documentation

**Last Updated**: 2025-05-24  
**Total Files**: 42 markdown documents  
**Scope**: Complete system design, API reference, migration guides, and operational procedures

---

## Quick Navigation

### 📐 Architecture (20 files)

System design, technology choices, and long-term vision.

- **[00-vision.md](architecture/00-vision.md)** — Product vision, positioning, market fit
- **[01-mvp-scope.md](architecture/01-mvp-scope.md)** — Phase 0 MVP scope and feature matrix
- **[02-roadmap.md](architecture/02-roadmap.md)** — Phase 1-4 roadmap with timelines
- **[03-architecture.md](architecture/03-architecture.md)** — System diagram, service boundaries, data flow
- **[04-tech-stack.md](architecture/04-tech-stack.md)** — Technologies, versions, and rationale
- **[05-data-model.md](architecture/05-data-model.md)** — Database schema, ER diagrams, relationships
- **[06-api-contract.md](architecture/06-api-contract.md)** — REST API principles, error handling, authentication
- **[07-flow-spec.md](architecture/07-flow-spec.md)** — Flow JSON schema, node types, execution semantics
- **[08-ai-orchestration.md](architecture/08-ai-orchestration.md)** — LLM providers, RAG strategy, prompt engineering
- **[09-widget-embed-guide.md](architecture/09-widget-embed-guide.md)** — Customer-facing widget, customization options
- **[18-microservices-architecture.md](architecture/18-microservices-architecture.md)** — Detailed service specs, schemas, APIs

**Start Here**: Read [00-vision.md](architecture/00-vision.md) for product overview, then [03-architecture.md](architecture/03-architecture.md) for system design.

---

### 🔌 API Reference (3 files)

Complete API documentation with code examples and integration guides.

- **[rest-endpoints.md](api/rest-endpoints.md)** — 45+ REST endpoints (9 services), authentication, pagination
- **[kafka-topics.md](api/kafka-topics.md)** — 9 Kafka event topics with complete schemas and examples
- **[openapi.md](api/openapi.md)** — OpenAPI 3.0 spec, code generation with Orval, SDK setup

**Start Here**: [rest-endpoints.md](api/rest-endpoints.md) for HTTP API overview, then [kafka-topics.md](api/kafka-topics.md) for events.

---

### 🚀 Migration Guides (6 files)

Zero-downtime migration from monolith to 9 microservices (4-week plan).

- **[DEPLOYMENT_PLAN.md](migration/DEPLOYMENT_PLAN.md)** — 4-week timeline, Phase 1-4 overview
- **[MIGRATION_SUMMARY.md](migration/MIGRATION_SUMMARY.md)** — Toolkit overview, what exists, how to use
- **[IMPLEMENTATION_SUMMARY.md](migration/IMPLEMENTATION_SUMMARY.md)** — What was built, statistics, verification
- **[PHASE_1_SHADOW_MODE.md](migration/PHASE_1_SHADOW_MODE.md)** — Week 1: Deploy services read-only alongside monolith
- **[PHASE_2_DUAL_WRITE.md](migration/PHASE_2_DUAL_WRITE.md)** — Week 2: Writes go to both monolith and services
- **[PHASE_3_CUTOVER.md](migration/PHASE_3_CUTOVER.md)** — Week 3-4: Switch to services as primary, decommission monolith

**Start Here**: [DEPLOYMENT_PLAN.md](migration/DEPLOYMENT_PLAN.md) for overview, then [PHASE_1_SHADOW_MODE.md](migration/PHASE_1_SHADOW_MODE.md) for execution details.

---

### 📚 Reference Materials (8 files)

Developer guides, operational procedures, and best practices.

- **[10-dev-setup.md](reference/10-dev-setup.md)** — Local development environment, Docker Compose, hot reload
- **[11-deployment.md](reference/11-deployment.md)** — Staging and production deployment procedures
- **[12-design-system.md](reference/12-design-system.md)** — Design tokens, Tailwind config, component library
- **[13-security.md](reference/13-security.md)** — Threat model, secrets management, CORS, multi-tenancy
- **[14-observability.md](reference/14-observability.md)** — Tracing, logging, metrics, Grafana dashboards
- **[ENVIRONMENT_VARIABLES.md](reference/ENVIRONMENT_VARIABLES.md)** — All env vars, defaults, production values
- **[CONTRIBUTING.md](reference/CONTRIBUTING.md)** — Development workflow, code style, testing, PR process
- **[FAQ.md](reference/FAQ.md)** — 40+ FAQs: setup, API, deployment, troubleshooting, performance

**Start Here**: [10-dev-setup.md](reference/10-dev-setup.md) for local development, [CONTRIBUTING.md](reference/CONTRIBUTING.md) for coding guidelines.

---

### 🔧 Runbooks (5 files)

Step-by-step operational procedures for production support.

- **[README.md](runbooks/README.md)** — Runbook index, when to use each, escalation path
- **[RUNBOOK_MIGRATION.md](runbooks/RUNBOOK_MIGRATION.md)** — Phase 1-3 procedures, week-by-week walkthrough
- **[RUNBOOK_SERVICE_RESTART.md](runbooks/RUNBOOK_SERVICE_RESTART.md)** — Emergency service restart procedures
- **[RUNBOOK_KAFKA_RECOVERY.md](runbooks/RUNBOOK_KAFKA_RECOVERY.md)** — Consumer lag, DLQ handling, rebalancing
- **[RUNBOOK_ROLLBACK.md](runbooks/RUNBOOK_ROLLBACK.md)** — Emergency rollback from Phase 3 (< 30 min)

**Start Here**: [README.md](runbooks/README.md) to understand all runbooks, then choose the relevant one.

---

## By Audience

### 👨‍💻 Developers

**First Time?**
1. Read: [Architecture Overview](architecture/00-vision.md)
2. Setup: [Development Setup](reference/10-dev-setup.md)
3. Code: [Contributing Guide](reference/CONTRIBUTING.md)
4. API: [REST Endpoints](api/rest-endpoints.md)
5. FAQ: [Frequently Asked Questions](reference/FAQ.md)

**Building a Feature?**
- [REST Endpoints](api/rest-endpoints.md) — HTTP API reference
- [Kafka Topics](api/kafka-topics.md) — Event topics and schemas
- [Contributing Guide](reference/CONTRIBUTING.md) — Code style, testing, PR process
- [FAQ](reference/FAQ.md) — Common questions

**Need Help?**
- [FAQ](reference/FAQ.md) — 40+ Q&A
- [Troubleshooting](reference/FAQ.md#troubleshooting) section

---

### 🚀 DevOps / Platform Team

**Deploying to Staging?**
1. [Deployment Guide](reference/11-deployment.md)
2. [Environment Variables](reference/ENVIRONMENT_VARIABLES.md)
3. [Security](reference/13-security.md)

**Running a Microservices Migration?**
1. [Migration Deployment Plan](migration/DEPLOYMENT_PLAN.md)
2. [Phase 1: Shadow Mode](migration/PHASE_1_SHADOW_MODE.md)
3. [Phase 2: Dual-Write](migration/PHASE_2_DUAL_WRITE.md)
4. [Phase 3: Cutover](migration/PHASE_3_CUTOVER.md)

**Production Support?**
1. [Runbooks README](runbooks/README.md)
2. Choose runbook:
   - Service restart → [SERVICE_RESTART](runbooks/RUNBOOK_SERVICE_RESTART.md)
   - Kafka issues → [KAFKA_RECOVERY](runbooks/RUNBOOK_KAFKA_RECOVERY.md)
   - Emergency rollback → [ROLLBACK](runbooks/RUNBOOK_ROLLBACK.md)

**Monitoring & Observability?**
- [Observability](reference/14-observability.md)
- [Environment Variables](reference/ENVIRONMENT_VARIABLES.md) (Prometheus, Grafana, Jaeger)

---

### 📊 Product / Leadership

**Understanding the Product?**
1. [Vision](architecture/00-vision.md) — What is Threadly?
2. [Roadmap](architecture/02-roadmap.md) — Phases 1-4 timeline
3. [Architecture](architecture/03-architecture.md) — How it works

**Integration Needs?**
- [REST API Endpoints](api/rest-endpoints.md) — Full API reference
- [Kafka Topics](api/kafka-topics.md) — Event topics for integrations

---

### 🤝 Customers / Partners

**Getting Started?**
- [Widget Embed Guide](architecture/09-widget-embed-guide.md) — How to embed the widget
- [REST API Reference](api/rest-endpoints.md) — Build custom integrations
- [FAQ](reference/FAQ.md) — Common questions

**Building Integrations?**
- [OpenAPI Specification](api/openapi.md) — Generate SDK code
- [REST Endpoints](api/rest-endpoints.md) — All available endpoints
- [Kafka Topics](api/kafka-topics.md) — Event topics to subscribe to

---

## Search & Discovery

### By Topic

| Topic | Location |
|-------|----------|
| API endpoints | [api/rest-endpoints.md](api/rest-endpoints.md) |
| Database schema | [architecture/05-data-model.md](architecture/05-data-model.md) |
| Deployment | [reference/11-deployment.md](reference/11-deployment.md) |
| Development setup | [reference/10-dev-setup.md](reference/10-dev-setup.md) |
| Events (Kafka) | [api/kafka-topics.md](api/kafka-topics.md) |
| Migration | [migration/PHASE_1_SHADOW_MODE.md](migration/PHASE_1_SHADOW_MODE.md) |
| Environment vars | [reference/ENVIRONMENT_VARIABLES.md](reference/ENVIRONMENT_VARIABLES.md) |
| Security | [reference/13-security.md](reference/13-security.md) |
| Monitoring | [reference/14-observability.md](reference/14-observability.md) |
| Troubleshooting | [reference/FAQ.md#troubleshooting](reference/FAQ.md#troubleshooting) |

### By Technology

| Technology | Location |
|-----------|----------|
| Spring Boot | [architecture/04-tech-stack.md](architecture/04-tech-stack.md) |
| Next.js | [architecture/04-tech-stack.md](architecture/04-tech-stack.md) |
| FastAPI (AI) | [architecture/08-ai-orchestration.md](architecture/08-ai-orchestration.md) |
| Kafka | [api/kafka-topics.md](api/kafka-topics.md) |
| PostgreSQL | [architecture/05-data-model.md](architecture/05-data-model.md) |
| Kubernetes | [reference/11-deployment.md](reference/11-deployment.md) |
| Docker Compose | [reference/10-dev-setup.md](reference/10-dev-setup.md) |

---

## File Statistics

```
docs/
├── architecture/    20 files   System design & roadmap
├── api/             3 files    API reference & integration
├── migration/       6 files    Migration guides (Phase 1-3)
├── reference/       8 files    Best practices & guides
├── runbooks/        5 files    Operational procedures
└── README.md                    This file

Total: 42 markdown documents
```

---

## Key Features of This Documentation

✓ **Comprehensive** — 42 files covering all aspects of the system  
✓ **Organized** — Clear folder structure, multiple navigation paths  
✓ **Practical** — Code examples, step-by-step procedures, runbooks  
✓ **Current** — Updated 2025-05-24, reflects live codebase  
✓ **Cross-Referenced** — Linked throughout, no dead links  
✓ **Audience-Focused** — Tailored for developers, ops, product, customers  
✓ **Searchable** — Plain markdown, works with GitHub search and CI/CD  

---

## Contributing to Docs

Found an issue? Want to improve the documentation?

1. Edit the relevant markdown file
2. Ensure links are relative (`docs/*.md` format)
3. Update the timestamp at the top
4. Create a PR with description
5. Get approval from doc reviewer
6. Merge to main

See [CONTRIBUTING.md](reference/CONTRIBUTING.md) for full guidelines.

---

## Maintenance Schedule

| Task | Frequency | Owner |
|------|-----------|-------|
| Review & update docs | Quarterly (next: 2025-08-24) | Tech lead (@yasva) |
| Fix broken links | Monthly | Team |
| Update API endpoints | When services change | Developer |
| Update deployment guide | When process changes | DevOps |
| Review FAQ | Quarterly | Product/Support |

---

## Quick Links

- **GitHub Repository**: https://github.com/threadly/threadly
- **Live Demo**: https://threadly.io
- **Support**: support@threadly.io
- **Report Bug**: GitHub Issues

---

**Last Updated**: 2025-05-24  
**Maintained by**: @yasva and team  
**Next Review**: 2025-08-24
