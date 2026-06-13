# threadly-core

Spring Boot 3.3 modular monolith — the main backend for Threadly.

## Modules

| Module | Responsibility |
|---|---|
| `identity` | Auth, JWT, users, orgs |
| `workspace` | Bots CRUD, API keys, embed config |
| `flow` | Flow JSON CRUD, versioning, publish |
| `runtime` | Flow interpreter, session state, node executors |
| `conversation` | Transcript read API, agent inbox |
| `knowledge` | KB upload, ingestion dispatch |
| `analytics` | Dashboard metrics |
| `centrifugo` | Centrifugo HTTP API client + token issuance |
| `proxy` | Centrifugo proxy hooks |
| `outbox` | Event publishing |
| `ai` | AI sidecar HTTP client |
| `common` | Tenancy, errors, S3 config |

## Run locally

```bash
# With Docker infra running:
./mvnw spring-boot:run

# Or via Make:
make core-run
```

API docs: http://localhost:8080/swagger-ui
