# Threadly Deployment Topology Guide

Complete deployment setup for local development (Docker Compose) and production (Kubernetes).

## Overview

**Deployment Targets:**
- **Local Dev**: Docker Compose with 9 Java microservices, FastAPI/Next.js, Consul, Nginx, Kafka, Postgres, Redis, Qdrant
- **Production**: Kubernetes with StatefulSets for databases, 3-replica Kafka, and 3-replica microservice deployments

**Architecture**: Database-per-service with event-driven consistency via Kafka

---

## Local Development Setup (Docker Compose)

### Start the Complete Stack

```bash
make up
# or
docker compose -f infra/docker-compose.yml up -d --build
```

This launches:
- **Service Discovery**: Consul (UI on :8500)
- **Databases**: PostgreSQL 16 (9 schemas), Redis 7
- **Message Queue**: Kafka + Zookeeper (topics pre-created)
- **Vector DB**: Qdrant (:6333)
- **File Storage**: MinIO (:9000)
- **API Gateway**: Nginx (:8080) with rate limiting & CORS
- **9 Java Services** (ports 3001-3009):
  - identity-service:3001
  - workspace-service:3002
  - flow-service:3003
  - runtime-service:3004
  - conversation-service:3005
  - knowledge-service:3006
  - analytics-service:3007
  - billing-service:3008
  - integration-service:3009
- **Supporting Services**:
  - threadly-ai (FastAPI):8001
  - threadly-web (Next.js):3000
- **Realtime**: Centrifugo (:8000)
- **Observability**: Prometheus (:9090), Grafana (:3001)

### Verify Health

```bash
# Check all services are responding
make health

# Expected output:
# ✓ All services on :3001-3009 return 200 /health
# ✓ Consul on :8500 is UP
# ✓ Kafka, Redis, Postgres, Qdrant are UP

# List registered services in Consul
make services

# Show created database schemas
make schemas

# List Kafka topics
make kafka-topics
```

### Access Services

- **Threadly UI**: http://localhost:3000
- **Consul**: http://localhost:8500
- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Centrifugo Admin**: http://localhost:8000/admin
- **MinIO Console**: http://localhost:9001

### API Gateway Routing (Nginx)

All requests go through Nginx on `:8080`. Routes:

- `POST /auth/signup` → identity-service:3001
- `POST /auth/login` → identity-service:3001
- `GET /bots` → workspace-service:3002
- `GET /flows/{botId}` → flow-service:3003
- `POST /sessions/{botId}/start` → runtime-service:3004
- `GET /conversations/{botId}` → conversation-service:3005
- `POST /kb/upload` → knowledge-service:3006
- `GET /dashboard/summary` → analytics-service:3007
- `GET /billing/invoices` → billing-service:3008
- `GET /integrations/marketplace` → integration-service:3009
- **Rate Limits**: 100 req/s general, 5 req/m auth, 1000 req/s widget

### Database Schema Architecture

PostgreSQL has **9 isolated schemas**, one per service:

```sql
-- Verify schemas exist:
make schemas

-- Or manually:
docker compose -f infra/docker-compose.yml exec postgres psql -U threadly -d threadly -c \
  "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%_service';"
```

Each schema contains tables for that service only. Flyway migrations auto-run on service startup.

### Kafka Topics (Dev Mode)

9 topics created automatically on startup:

```bash
make kafka-topics

# Expected:
# user-events
# org-events
# flow-events
# session-events
# conversation-events
# kb-events
# analytics-events
# billing-events
# integration-events
```

Each topic has **3 partitions, replication factor 1** (dev mode).

### Consul Service Discovery

Each service registers with Consul on startup:

```bash
curl http://localhost:8500/v1/catalog/services | jq '.'

# Expected (partial):
# {
#   "identity-service": [...],
#   "workspace-service": [...],
#   ...
#   "threadly-ai": [...]
# }
```

Feign clients auto-resolve service names via Consul DNS.

### View Service Logs

```bash
# Tail all logs
make logs

# Tail specific service
make logs s=identity-service

# Or manually
docker compose -f infra/docker-compose.yml logs -f identity-service
```

### Stop and Clean Up

```bash
# Stop all services (keep volumes)
make down

# Full cleanup (remove volumes)
docker compose -f infra/docker-compose.yml down -v
```

---

## Database Migrations

### Flyway Auto-Migration

On service startup, Flyway runs all migrations in `threadly-core/src/main/resources/db/migration/`:

- `V1__init.sql`: Create service-specific tables
- `V2__add_indexes.sql`: Add performance indexes
- `V10__create_schemas.sql`: Create microservice schemas (runs in postgres init)

### Manual Database Access

```bash
# Open psql shell
make db-shell

# Or via docker
docker compose -f infra/docker-compose.yml exec postgres psql -U threadly -d threadly
```

### View Migration History

```bash
# Check Flyway history
docker compose -f infra/docker-compose.yml exec threadly-core ./mvnw flyway:info

# Expected output shows all applied migrations
```

---

## Production Deployment (Kubernetes)

### Prerequisites

- Kubernetes cluster (1.20+)
- kubectl configured
- Docker images pushed to registry

### Deploy to Kubernetes

```bash
# Apply all manifests (namespace, postgres, redis, kafka, nginx, services)
make k8s-deploy

# This applies:
# 1. Namespace (threadly)
# 2. PostgreSQL StatefulSet (1 pod, 10Gi volume)
# 3. Redis StatefulSet (1 pod)
# 4. Kafka + Zookeeper StatefulSets (3 Kafka brokers, 1 Zookeeper)
# 5. Nginx ConfigMap + LoadBalancer Service + 2-replica Deployment
# 6. Microservices (3 replicas each, with secrets + configmaps)
```

### Verify Kubernetes Deployment

```bash
# Check cluster status
make k8s-status

# Expected:
# - 9 service deployments (3 replicas each)
# - 1 nginx deployment (2 replicas)
# - 1 postgres statefulset
# - 1 redis statefulset
# - 3 kafka brokers + 1 zookeeper statefulset

# View service endpoints
kubectl get endpoints -n threadly

# Access Consul UI (port-forward required)
make k8s-consul-port-forward
# Then visit http://localhost:8500
```

### Kubernetes Architecture

**Namespace**: `threadly`

**Services**:
- `postgres-service` (ClusterIP:None, StatefulSet headless)
- `redis-service` (ClusterIP:None, StatefulSet headless)
- `zookeeper-service` (ClusterIP:None, StatefulSet headless)
- `kafka-service` (ClusterIP:None, StatefulSet headless)
- `identity-service` through `integration-service` (ClusterIP)
- `nginx` (LoadBalancer, routes to all services)

**StatefulSets**:
- postgres (1 pod, 10Gi PVC)
- redis (1 pod, 2Gi PVC)
- zookeeper (1 pod, 2Gi + 2Gi PVCs)
- kafka (3 pods, 5Gi PVC each)

**Deployments**:
- nginx (2 replicas, LoadBalancer)
- identity-service (3 replicas)
- workspace-service (3 replicas)
- flow-service (3 replicas)
- runtime-service (3 replicas)
- conversation-service (3 replicas)
- knowledge-service (3 replicas)
- analytics-service (3 replicas)
- billing-service (3 replicas)
- integration-service (3 replicas)

**Secrets**:
- `postgres-credentials` (username, password, database)
- `redis-credentials` (password)
- `microservices-secrets` (all shared configs)

### Health Checks

```bash
# All services have liveness/readiness probes
# Check pod status
kubectl get pods -n threadly

# Expected: all pods Running, Ready 1/1

# View pod logs
make k8s-logs s=identity-service

# Check service endpoints
kubectl get endpoints -n threadly
```

### Load Balancer Access

Nginx is exposed via LoadBalancer service:

```bash
# Get external IP (may be pending on local minikube)
kubectl get svc nginx -n threadly

# On cloud providers (AWS, GCP, Azure):
# EXTERNAL-IP will be assigned, access via:
# http://<EXTERNAL-IP>:8080/health
```

### Scale Services

```bash
# Scale identity-service to 5 replicas
kubectl scale deployment identity-service -n threadly --replicas=5

# Scale Kafka to 5 brokers
kubectl scale statefulset kafka -n threadly --replicas=5
```

### Upgrade Services

```bash
# Rolling update image
kubectl set image deployment/identity-service \
  identity-service=threadly/identity-service:v2.0.0 -n threadly

# Watch rollout
kubectl rollout status deployment/identity-service -n threadly
```

### Delete Deployment

```bash
# Remove entire threadly namespace and all resources
make k8s-delete

# Or manually
kubectl delete namespace threadly
```

---

## Nginx Configuration (API Gateway)

Location: `infra/nginx/nginx.conf`

**Features**:
- Rate limiting zones: general (100 r/s), auth (5 r/m), widget (1000 r/s)
- CORS headers on all responses
- Request forwarding to upstream services
- Proxy headers (X-Real-IP, X-Forwarded-For, etc.)
- WebSocket upgrade support for realtime sessions

**Route Mapping**:

| Path | Upstream | Service | Rate Limit |
|------|----------|---------|------------|
| `/auth/*` | identity-service:3001 | identity | 5 r/m |
| `/orgs/*`, `/bots/*`, `/workspace/*` | workspace-service:3002 | workspace | general |
| `/flows/*` | flow-service:3003 | flow | general |
| `/sessions/*`, `/realtime/*` | runtime-service:3004 | runtime | 1000 r/s |
| `/conversations/*` | conversation-service:3005 | conversation | general |
| `/kb/*` | knowledge-service:3006 | knowledge | general |
| `/dashboard/*` | analytics-service:3007 | analytics | general |
| `/billing/*` | billing-service:3008 | billing | general |
| `/integrations/*` | integration-service:3009 | integration | general |
| `/ai/*` | threadly-ai:8001 | ai-sidecar | general |
| `/` | threadly-web:3000 | frontend | general |

---

## Environment Variables

### Docker Compose (.env)

```bash
POSTGRES_PASSWORD=threadly_dev
REDIS_PASSWORD=threadly_dev
S3_BUCKET=threadly-kb
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
CENTRIFUGO_HTTP_API_KEY=dev_api_key
CENTRIFUGO_TOKEN_HMAC_SECRET=dev_secret_change_in_prod
NEXTAUTH_SECRET=dev_nextauth_secret
ANTHROPIC_API_KEY=sk-ant-...
OPENAI_API_KEY=sk-...
VOYAGE_API_KEY=...
LANGFUSE_PUBLIC_KEY=...
LANGFUSE_SECRET_KEY=...
LANGFUSE_HOST=...
STRIPE_API_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

### Kubernetes (Secrets)

Stored in K8s secrets:
- `postgres-credentials`
- `redis-credentials`
- `microservices-secrets`

---

## Troubleshooting

### Service fails to start

```bash
# Check logs
make logs s=identity-service

# Common issues:
# 1. Port already in use: `lsof -i :3001`
# 2. Postgres not ready: check health with `make health`
# 3. Kafka not ready: check `make kafka-topics`
```

### Database migration fails

```bash
# Reset database (clears all data)
make db-reset
make up

# View migration history
docker compose exec threadly-core ./mvnw flyway:info
```

### Consul services not showing

```bash
# Check consul health
curl http://localhost:8500/v1/status/leader

# Check service registration (from app logs)
docker compose logs identity-service | grep -i consul

# Manually register (if needed)
# Services auto-register via Spring Cloud Consul
```

### Kafka topics not created

```bash
# Verify Kafka is running
docker compose ps kafka

# Create topics manually
docker exec threadly-kafka kafka-topics.sh --create \
  --topic user-events \
  --bootstrap-server localhost:29092 \
  --partitions 3 --replication-factor 1
```

### Nginx routing not working

```bash
# Test route directly
curl http://localhost:8080/auth/login

# Check nginx logs
docker compose logs nginx

# Verify service is reachable
curl http://identity-service:3001/health
```

---

## Performance Tuning

### Local Development

- Kafka: 3 partitions, RF=1 (fast, not durable)
- Redis: single instance, no replication
- Postgres: single instance, 256MB buffer pool
- Nginx: 100 worker connections per process

### Production (Kubernetes)

- Kafka: 3 partitions, RF=3 (durable)
- Redis: StatefulSet with persistence
- Postgres: 10Gi volume with WAL archiving
- Nginx: 2 replicas, 50-100 worker connections each
- Services: 3 replicas, resource limits (512Mi mem, 500m CPU max)

---

## Monitoring & Observability

### Prometheus Metrics

```bash
# Prometheus scrapes all services on /actuator/prometheus
# Access dashboard: http://localhost:9090

# Query examples:
# http_requests_total (request count)
# jvm_memory_used_bytes (memory usage)
# kafka_consumer_lag (Kafka lag)
```

### Grafana Dashboards

```bash
# Access: http://localhost:3001 (admin/admin)
# Pre-configured dashboards:
# - Spring Boot Application Metrics
# - Kafka Metrics
# - Nginx Request Rate
```

### OpenTelemetry Tracing

Each service exports traces via OTLP. To integrate with Honeycomb/Tempo:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://otel-collector:4317
```

One trace ID spans:
1. Nginx gateway (incoming request)
2. Service handler (business logic)
3. Kafka event publishing
4. Downstream service consumption

---

## Appendix: File Structure

```
infra/
├── docker-compose.yml        # Complete dev stack
├── nginx/
│   └── nginx.conf            # API gateway routing
├── postgres-init/
│   ├── 01-langfuse.sql       # Langfuse DB setup
│   └── 10-create-schemas.sql # Microservice schemas
├── k8s/
│   ├── 00-namespace.yaml
│   ├── 01-postgres-statefulset.yaml
│   ├── 02-redis-statefulset.yaml
│   ├── 03-kafka-statefulset.yaml
│   ├── 04-nginx-configmap.yaml
│   └── 05-microservices-deployments.yaml
├── grafana/
│   └── prometheus.yml
├── centrifugo/
│   └── config.json
└── keys/
    ├── private.pem
    └── public.pem
```

---

## Quick Commands

```bash
# Local dev
make up                    # Start all
make down                  # Stop all
make health               # Check health
make services             # List registered services
make schemas              # Show DB schemas

# Kubernetes
make k8s-deploy           # Deploy to K8s
make k8s-status           # Check K8s status
make k8s-delete           # Delete K8s deployment

# Database
make db-shell             # Open psql
make db-reset             # Reset database

# Information
make kafka-topics         # List Kafka topics
make help                 # Show all targets
```
