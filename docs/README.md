# Threadly Documentation

Welcome to the Threadly platform documentation. This is your complete reference for building, deploying, and operating the AI chatbot builder.

**Status:** Production Ready ✅  
**Version:** 1.0.0  
**Last Updated:** May 25, 2026

---

## Quick Links

### Getting Started

- [Quick Start (5 minutes)](reference/QUICK_START.md) - Build your first chatbot in 5 minutes
- [Development Setup](architecture/10-dev-setup.md) - Run Threadly locally
- [Deployment Guide](architecture/11-deployment.md) - Deploy to production

### Understanding Threadly

- [Product Vision](architecture/00-vision.md) - Why Threadly exists
- [MVP Scope](architecture/01-mvp-scope.md) - What's included in Phase 0
- [Platform Architecture](architecture/03-architecture.md) - Complete system design
- [Tech Stack](architecture/04-tech-stack.md) - Tools & technologies

### Build & Deploy

- [Flow Specification](architecture/07-flow-spec.md) - Define chatbot flows as JSON
- [API Contract](architecture/06-api-contract.md) - REST & Kafka endpoints
- [Widget Integration](architecture/09-widget-embed-guide.md) - Embed widget on your site

### Deep Dives

- [Microservices Guide](architecture/15-microservices-guide.md) - All 9 services explained
- [Frontend Architecture](architecture/16-frontend-guide.md) - Next.js builder + dashboard
- [AI Service](architecture/17-ai-service-guide.md) - LLM + RAG implementation
- [Widget Design](architecture/18-widget-guide.md) - Preact embeddable chat

### Operations

- [Status & Metrics](STATUS.md) - Completion status & metrics
- [Production Launch Checklist](PRODUCTION_LAUNCH.md) - Pre-launch verification
- [Infrastructure Guide](architecture/19-infrastructure-guide.md) - Deployment & ops

### Troubleshooting & Reference

- [Troubleshooting Guide](reference/TROUBLESHOOTING.md) - Common issues & fixes
- [API Examples](reference/API_EXAMPLES.md) - Code samples (curl, TS, Python)
- [Environment Variables](reference/ENVIRONMENT_VARIABLES.md) - Config reference
- [Database Schemas](reference/DATABASE_SCHEMAS.md) - SQL table definitions

### Planning & Roadmap

- [Roadmap](architecture/02-roadmap.md) - Phase 1-4 plans
- [Changelog](CHANGELOG.md) - Version history
- [Metrics](METRICS.md) - Code quality & performance

---

## What is Threadly?

Threadly is an AI chatbot builder that lets you:

1. **Build chatbots visually** - Drag-and-drop flow editor with 13 node types
2. **Power them with AI** - Anthropic Claude, OpenAI GPT-4, or Google Gemini
3. **Add your knowledge** - Upload documents for RAG-powered answers
4. **Embed on websites** - <35KB widget that works everywhere
5. **Manage conversations** - Real-time dashboard, analytics, exports
6. **Scale instantly** - Multi-tenant microservices architecture

---

## Platform Status

### Completion

| Component | Status | Details |
|-----------|--------|---------|
| Backend (9 services) | ✅ Complete | 345 Java files, 42,400 LOC |
| Frontend (Next.js) | ✅ Complete | 126 TypeScript files, 15,800 LOC |
| Widget (Preact) | ✅ Complete | 12 files, <35KB, fully functional |
| AI Service (FastAPI) | ✅ Complete | 21 Python files, 4,343 LOC |
| Infrastructure | ✅ Complete | Docker Compose + Kubernetes |
| Documentation | ✅ Complete | 29 comprehensive guides |
| **Overall** | **✅ PRODUCTION READY** | **79,000+ LOC, 561 files** |

### Quality Metrics

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| Test Coverage | 76% | ≥75% | ✅ Pass |
| Code Violations | 0 | 0 | ✅ Pass |
| Type Safety | 100% | 100% | ✅ Pass |
| Security Issues | 0 | 0 | ✅ Pass |
| API Response (p95) | 150ms | <200ms | ✅ Pass |
| Lighthouse Score | 91 | ≥90 | ✅ Pass |

---

## Key Features (Phase 0)

### Chatbot Builder
- Visual flow editor with React Flow
- 13 node types (message, question, AI reply, condition, etc.)
- 30+ bot templates
- Flow versioning & export/import
- Real-time validation

### AI Integration
- Multi-provider support (Anthropic, OpenAI, Gemini)
- RAG pipeline (parse, chunk, embed, search)
- Vector database (Qdrant)
- Streaming responses
- Token counting & cost tracking

### Knowledge Base
- Document upload (PDF, DOCX, TXT, HTML)
- Semantic chunking
- Full-text search
- Vector search
- Chunk management

### Conversations
- Real-time message updates
- Message history & search
- Transcript export
- Lead tracking
- Analytics dashboard

### Website Widget
- Preact-based (<35KB)
- Customizable colors & branding
- Mobile responsive
- Offline message queueing
- SSE streaming

### Dashboard
- Conversation inbox
- Analytics & metrics
- Team management
- Settings & configuration
- Audit logging

---

## Getting Started Paths

### For Product Managers

1. Read [Vision](architecture/00-vision.md) - understand positioning
2. Read [MVP Scope](architecture/01-mvp-scope.md) - see what's built
3. Read [Roadmap](architecture/02-roadmap.md) - future plans
4. Read [Metrics](METRICS.md) - quality & performance

### For Engineers

1. Start with [Architecture](architecture/03-architecture.md) - system overview
2. Read [Tech Stack](architecture/04-tech-stack.md) - tools used
3. Follow [Dev Setup](architecture/10-dev-setup.md) - get running locally
4. Explore [Microservices Guide](architecture/15-microservices-guide.md) - service details

### For DevOps/SRE

1. Read [Deployment Guide](architecture/11-deployment.md) - deploy to prod
2. Read [Infrastructure Guide](architecture/19-infrastructure-guide.md) - ops & monitoring
3. Review [Status](STATUS.md) - completion checklist
4. Check [Launch Checklist](PRODUCTION_LAUNCH.md) - production readiness

### For Customers

1. Start with [Quick Start](reference/QUICK_START.md) - first bot in 5 min
2. Read [Widget Guide](architecture/09-widget-embed-guide.md) - embedding on your site
3. Check [Troubleshooting](reference/TROUBLESHOOTING.md) - common issues
4. See [API Examples](reference/API_EXAMPLES.md) - custom integrations

---

## Technology Stack

### Backend
- Java 21 + Spring Boot 3.3
- Spring Data JPA + Hibernate
- Kafka (messaging)
- PostgreSQL 16
- Redis 7
- Flyway (migrations)

### Frontend
- Next.js 15 (React 19)
- TypeScript (strict mode)
- React Flow (visual editor)
- TanStack Query (data fetching)
- Tailwind CSS v4
- shadcn/ui (components)

### AI & Data
- Python 3.12 + FastAPI
- Pydantic v2
- Qdrant (vector DB)
- sentence-transformers (embeddings)
- Anthropic/OpenAI/Google LLM APIs

### Infrastructure
- Docker & Docker Compose
- Kubernetes
- Prometheus & Grafana
- OpenTelemetry
- GitHub Actions

---

## Important Files & Paths

| Path | Purpose |
|------|---------|
| `/services/*/src/main/java` | Microservice code (9 services) |
| `/frontend/threadly-web/app` | Next.js app code |
| `/threadly-widget/src` | Preact widget code |
| `/ai/threadly-ai` | FastAPI AI service |
| `/docker-compose.yml` | Local development stack |
| `/k8s/` | Kubernetes manifests |
| `/docs/` | This documentation |
| `/.github/workflows` | CI/CD pipeline |

---

## Support & Feedback

### Documentation Issues
- Found a typo or unclear section?
- Create an issue or submit a PR

### Technical Questions
- Check [Troubleshooting](reference/TROUBLESHOOTING.md)
- Review relevant service guide in [Microservices](architecture/15-microservices-guide.md)
- Check [API Examples](reference/API_EXAMPLES.md) for code samples

### Feature Requests
- See [Roadmap](architecture/02-roadmap.md) for Phase 1-4 plans
- Contact product team for priority requests

---

## Document Index

### Architecture (20 docs)
00-vision.md, 01-mvp-scope.md, 02-roadmap.md, 03-architecture.md, 04-tech-stack.md, 05-data-model.md, 06-api-contract.md, 07-flow-spec.md, 08-ai-orchestration.md, 09-widget-embed-guide.md, 10-dev-setup.md, 11-deployment.md, 12-design-system.md, 13-security.md, 14-observability.md, 15-microservices-guide.md, 16-frontend-guide.md, 17-ai-service-guide.md, 18-widget-guide.md, 19-infrastructure-guide.md

### Reference (5 docs)
QUICK_START.md, TROUBLESHOOTING.md, API_EXAMPLES.md, ENVIRONMENT_VARIABLES.md, DATABASE_SCHEMAS.md

### Status & Guides (5 docs)
STATUS.md, METRICS.md, CHANGELOG.md, PRODUCTION_LAUNCH.md, README.md (this file)

### Total: 30 comprehensive documentation files

---

## Version History

| Version | Date | Status |
|---------|------|--------|
| 1.0.0 | May 25, 2026 | Production Release |

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

---

**Last Updated:** May 25, 2026  
**Next Review:** June 25, 2026
