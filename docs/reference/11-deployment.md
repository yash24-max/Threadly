# Deployment

## MVP — Railway

Railway provides managed Postgres, Redis, and container hosting with push-to-deploy.

### Services to deploy
1. `threadly-core` — Spring Boot container
2. `threadly-ai` — Python FastAPI container
3. `threadly-web` — Next.js container
4. `threadly-widget` — static files on Cloudflare R2 / CDN
5. `centrifugo` — container with Redis as broker
6. Managed `PostgreSQL` — Railway addon
7. Managed `Redis` — Railway addon
8. `Qdrant` — Railway container (or Qdrant Cloud free tier)

### Widget CDN
The widget bundle (`widget.js`, `widget.bundle.js`) is deployed to Cloudflare R2 + served via Cloudflare CDN at `cdn.threadly.dev`.

### Deploy steps

```bash
# 1. Install Railway CLI
npm i -g @railway/cli
railway login

# 2. Link project
railway link

# 3. Deploy all services
railway up
```

## CI/CD (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
on:
  push:
    branches: [main]
jobs:
  deploy-core:   # builds + deploys threadly-core
  deploy-ai:     # builds + deploys threadly-ai
  deploy-web:    # builds + deploys threadly-web
  deploy-widget: # builds + deploys widget to R2
```

All services run tests before deploy. Failed tests block deploy.

## Scale path (AWS)

When Railway limits are hit:
1. Migrate DB to RDS (Postgres) — point-in-time restore, multi-AZ
2. Redis → ElastiCache
3. Core → ECS Fargate (auto-scaling task definitions)
4. AI sidecar → ECS Fargate GPU (if fine-tuning later)
5. Centrifugo → ECS Fargate (scale to multiple nodes, Nats Jetstream as broker)
6. Widget CDN stays on Cloudflare

## Domain setup

| Subdomain | Service |
|---|---|
| `threadly.dev` | Marketing + web app |
| `app.threadly.dev` | Web app (dashboard + builder) |
| `api.threadly.dev` | Core REST |
| `rt.threadly.dev` | Centrifugo WebSocket |
| `cdn.threadly.dev` | Widget CDN |

## Backup strategy
- PostgreSQL: daily automated snapshots (Railway) + weekly to R2
- Qdrant: export collections weekly to R2
- Rollback: Flyway baseline + last snapshot
