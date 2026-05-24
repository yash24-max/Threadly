# Tech Stack

## Backend — threadly-core

| Layer | Tool | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.x |
| Reactive | Spring WebFlux | (runtime module only) |
| ORM | Hibernate 6 + Spring Data JPA | — |
| Complex queries | jOOQ | 3.19 |
| Migrations | Flyway | 10.x |
| Validation | Jakarta Bean Validation | — |
| Mapping | MapStruct | 1.6 |
| HTTP client | Spring WebClient | — |
| Resilience | Resilience4j | 2.x |
| Rate limiting | Bucket4j + Redis | 8.x |
| Cache | Caffeine (L1) + Redis (L2) | — |
| Auth | Spring Security 6 + JWT RS256 | — |
| API docs | Springdoc OpenAPI | 2.x |
| Testing | JUnit 5 + AssertJ + Testcontainers + WireMock | — |
| Format | Spotless + google-java-format | — |

## Realtime — Centrifugo

| Concern | Choice |
|---|---|
| Realtime server | Centrifugo OSS |
| Broker | Redis (MVP) → Nats Jetstream (scale) |
| Client SDK | centrifuge-js |
| Auth | JWT (separate secret from app JWT) |

## AI sidecar — threadly-ai

| Layer | Tool |
|---|---|
| Language | Python 3.12 |
| Framework | FastAPI + Pydantic v2 |
| LLM (primary) | Anthropic Claude (claude-sonnet-4-5) |
| LLM (fallback) | OpenAI GPT-4o |
| Embeddings | voyage-3-lite or text-embedding-3-small |
| Vector DB | Qdrant |
| RAG pipeline | LangChain (splitters only) + custom retrieval |
| Reranker | Cohere Rerank v3 (opt-in) |
| Doc parsing | unstructured + pypdf |
| LLM tracing | Langfuse (self-hosted) |
| Format | ruff + mypy --strict |

## Frontend — threadly-web

| Layer | Tool |
|---|---|
| Framework | Next.js 15 (App Router) |
| Language | TypeScript (strict) |
| Styling | Tailwind CSS v4 |
| Components | shadcn/ui + Radix UI |
| Flow builder | React Flow (xyflow) v12 |
| Data fetching | TanStack Query v5 + Orval codegen |
| Forms | react-hook-form + Zod |
| Tables | TanStack Table |
| State | Zustand |
| Auth client | Auth.js v5 (credentials → core) |
| Realtime | centrifuge-js |
| Animation | Framer Motion + tailwindcss-animate |
| Charts | Recharts |
| Icons | Lucide |
| Fonts | Geist + Geist Mono |
| Toasts | Sonner |
| Command | cmdk |
| Testing | Vitest + Playwright |
| Lint/format | Biome |

## Widget — threadly-widget

| Layer | Tool |
|---|---|
| Framework | Preact 10 |
| Build | Vite + Rollup |
| Styling | Scoped CSS (no Tailwind — bundle size) |
| Realtime | centrifuge-js |
| Bundle target | < 35 KB gzipped |

## Infrastructure / DevX

| Concern | Tool |
|---|---|
| Local dev | Docker Compose |
| CI | GitHub Actions |
| Hosting (MVP) | Railway or Render |
| Hosting (scale) | AWS ECS Fargate + RDS + ElastiCache |
| DNS/edge | Cloudflare |
| Object storage | Cloudflare R2 (S3 API) |
| Email | Resend |
| Observability | Grafana Cloud (logs + metrics + traces) |
| LLM tracing | Langfuse self-hosted |
| Error tracking | Sentry |
| Feature flags | PostHog |
| Secrets | Doppler |
