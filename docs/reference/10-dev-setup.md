# Local Dev Setup

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Docker Desktop | Latest | https://docker.com |
| Java | 21 | `brew install temurin@21` |
| Node.js | 20+ | `brew install node` |
| pnpm | 9+ | `npm i -g pnpm` |
| Python | 3.12 | `brew install python@3.12` |
| uv | Latest | `curl -LsSf https://astral.sh/uv/install.sh | sh` |
| Make | — | Pre-installed on Mac/Linux |

## First-time setup

```bash
# 1. Clone and enter
cd /Users/yasva/Kapture/Microservice/Project/Threadly

# 2. Copy env template
cp .env.example .env
# Edit .env — set ANTHROPIC_API_KEY at minimum

# 3. Bootstrap (installs deps, pulls Docker images)
make bootstrap

# 4. Start the full stack
make up

# 5. Seed demo data
make seed

# 6. Open http://localhost:3000
```

## Environment variables (.env)

```bash
# Database
POSTGRES_PASSWORD=threadly_dev

# Redis
REDIS_PASSWORD=threadly_dev

# JWT
JWT_PRIVATE_KEY_PATH=./infra/keys/private.pem
JWT_PUBLIC_KEY_PATH=./infra/keys/public.pem
CENTRIFUGO_TOKEN_HMAC_SECRET=dev_secret_change_in_prod

# AI
ANTHROPIC_API_KEY=sk-ant-...
OPENAI_API_KEY=sk-...       # optional fallback
VOYAGE_API_KEY=...           # optional, for better embeddings

# Storage (MinIO locally)
S3_ENDPOINT=http://localhost:9000
S3_BUCKET=threadly-kb
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin

# Langfuse (optional, for LLM tracing)
LANGFUSE_PUBLIC_KEY=pk-lf-...
LANGFUSE_SECRET_KEY=sk-lf-...
LANGFUSE_HOST=http://localhost:3003

# Centrifugo
CENTRIFUGO_HTTP_API_KEY=dev_api_key
CENTRIFUGO_ADMIN_PASSWORD=dev_admin
```

## Services and ports

| Service | Port | URL |
|---|---|---|
| threadly-web | 3000 | http://localhost:3000 |
| threadly-core | 8080 | http://localhost:8080 |
| threadly-ai | 8081 | http://localhost:8081 |
| Centrifugo | 8000 | http://localhost:8000 (admin) |
| PostgreSQL | 5432 | — |
| Redis | 6379 | — |
| Qdrant | 6333 | http://localhost:6333 (UI) |
| MinIO | 9000 / 9001 | http://localhost:9001 (console) |
| Grafana | 3001 | http://localhost:3001 |
| Langfuse | 3003 | http://localhost:3003 |

## Hot reload dev (outside Docker)

For faster iteration, run individual services outside Docker:

```bash
# Terminal 1 — infra only
docker compose -f infra/docker-compose.yml up postgres redis qdrant centrifugo minio -d

# Terminal 2 — core
make core-run

# Terminal 3 — ai
make ai-run

# Terminal 4 — web
make web-run

# Terminal 5 — widget (if modifying)
make widget-run
```

Or use `mprocs` with the included `mprocs.yaml` for a single terminal panel.

## Generating typed API hooks

After changing any Core endpoint:

```bash
make codegen
# → writes to threadly-web/app/_generated/
```
