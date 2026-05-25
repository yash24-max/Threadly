# Threadly Environment Variables Reference

**Version:** 1.0  
**Last Updated:** May 25, 2026

---

## Global Variables

Required for all services:

```bash
# Basic
ENVIRONMENT=production
DEBUG=false
LOG_LEVEL=INFO

# Database (PostgreSQL)
DATABASE_URL=postgresql://user:pass@localhost:5432/threadly
DATABASE_POOL_SIZE=20
DATABASE_MAX_LIFETIME=1800
DATABASE_TIMEOUT=30

# Redis
REDIS_URL=redis://localhost:6379/0
REDIS_TIMEOUT=5
REDIS_POOL_SIZE=10

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=threadly-services
KAFKA_CONSUMER_THREADS=3

# Qdrant (Vector DB)
QDRANT_URL=http://localhost:6333
QDRANT_API_KEY=
QDRANT_TIMEOUT=30

# Centrifugo (Real-time)
CENTRIFUGO_URL=http://localhost:8000
CENTRIFUGO_API_KEY=your-api-key
CENTRIFUGO_API_SECRET=your-api-secret

# Observability
OTEL_ENABLED=true
OTEL_JAEGER_ENDPOINT=http://localhost:14268/api/traces
PROMETHEUS_PORT=9090
LOKI_URL=http://localhost:3100
```

---

## Identity Service

```bash
# Port
IDENTITY_SERVICE_PORT=8001

# JWT
JWT_SECRET_KEY=your-secret-key-min-32-chars
JWT_ALGORITHM=RS256
JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----...
JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----...
JWT_ACCESS_TOKEN_EXPIRY=900           # 15 minutes
JWT_REFRESH_TOKEN_EXPIRY=2592000      # 30 days

# Password
BCRYPT_ROUNDS=12
PASSWORD_MIN_LENGTH=8

# Email
EMAIL_FROM=noreply@threadly.dev
EMAIL_PROVIDER=sendgrid            # sendgrid or smtp
SENDGRID_API_KEY=
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=
SMTP_PASSWORD=

# Verification
EMAIL_VERIFICATION_ENABLED=true
EMAIL_VERIFICATION_EXPIRY=3600     # 1 hour

# Rate Limiting
RATE_LIMIT_SIGNUP=5/hour
RATE_LIMIT_LOGIN=10/hour
RATE_LIMIT_PASSWORD_RESET=3/hour
```

---

## Workspace Service

```bash
# Port
WORKSPACE_SERVICE_PORT=8002

# Storage
STORAGE_TYPE=s3                    # s3 or local
S3_BUCKET=threadly-bots
S3_REGION=us-east-1
S3_ACCESS_KEY=
S3_SECRET_KEY=
LOCAL_STORAGE_PATH=/data/bots

# Features
MAX_BOTS_PER_ORG=50
MAX_TEMPLATES=100
ENABLE_CUSTOM_TEMPLATES=true

# Kafka
WORKSPACE_KAFKA_TOPIC_BOT_CREATED=bot.created
WORKSPACE_KAFKA_TOPIC_BOT_UPDATED=bot.updated
WORKSPACE_KAFKA_TOPIC_BOT_PUBLISHED=bot.published
```

---

## Flow Service

```bash
# Port
FLOW_SERVICE_PORT=8003

# Versioning
MAX_FLOW_VERSIONS=100
ENABLE_FLOW_ROLLBACK=true

# Validation
VALIDATE_FLOW_ON_SAVE=true
VALIDATE_FLOW_ON_PUBLISH=true
MAX_NODES_PER_FLOW=500
MAX_EDGES_PER_FLOW=1000

# Kafka
FLOW_KAFKA_TOPIC_PUBLISHED=flow.published
FLOW_KAFKA_TOPIC_CREATED=flow.created
```

---

## Runtime Service

```bash
# Port
RUNTIME_SERVICE_PORT=8004

# Execution
MAX_EXECUTION_TIME=30000          # 30 seconds
MAX_RETRIES=3
EXECUTION_TIMEOUT=60              # seconds

# Sessions
SESSION_TIMEOUT=3600              # 1 hour
MAX_SESSION_VARIABLES=1000
MAX_CONTEXT_SIZE=50000            # tokens

# Node Executors
NODE_EXECUTOR_THREADS=10
NODE_EXECUTOR_QUEUE_SIZE=1000

# Kafka
RUNTIME_KAFKA_TOPIC_EXECUTED=flow.executed
RUNTIME_KAFKA_TOPIC_FAILED=flow.failed
```

---

## Conversation Service

```bash
# Port
CONVERSATION_SERVICE_PORT=8005

# Storage
CONVERSATION_RETENTION_DAYS=90
ENABLE_CONVERSATION_EXPORT=true
ENABLE_CONVERSATION_ENCRYPTION=true

# Search
ELASTICSEARCH_URL=http://localhost:9200
ENABLE_FULL_TEXT_SEARCH=true

# Analytics
TRACK_VISITOR_BEHAVIOR=true
ENABLE_SENTIMENT_ANALYSIS=false

# Kafka
CONVERSATION_KAFKA_TOPIC_STARTED=conversation.started
CONVERSATION_KAFKA_TOPIC_ENDED=conversation.ended
CONVERSATION_KAFKA_TOPIC_MESSAGE=message.added
```

---

## Knowledge Service

```bash
# Port
KNOWLEDGE_SERVICE_PORT=8006

# Document Processing
MAX_DOCUMENT_SIZE_MB=50
SUPPORTED_FORMATS=pdf,docx,txt,html
ENABLE_OCR=false                   # For scanned PDFs

# Embedding
EMBEDDING_MODEL=all-MiniLM-L6-v2   # local or voyage-ai
EMBEDDING_DIMENSION=384
BATCH_EMBEDDING_SIZE=100
ENABLE_VOYAGE_AI=false
VOYAGE_AI_API_KEY=

# Chunking
CHUNK_SIZE=500                     # tokens
CHUNK_OVERLAP=50                   # tokens
SEMANTIC_CHUNKING_ENABLED=true

# Vector DB
QDRANT_COLLECTION_PREFIX=threadly
QDRANT_VECTOR_SIZE=384
QDRANT_INDEX_TYPE=hnsw

# Kafka
KB_KAFKA_TOPIC_UPLOADED=kb.document.uploaded
KB_KAFKA_TOPIC_INGESTION_COMPLETE=kb.ingestion.completed
```

---

## Analytics Service

```bash
# Port
ANALYTICS_SERVICE_PORT=8007

# Aggregation
AGGREGATION_INTERVAL=3600         # 1 hour
DAILY_ROLLUP_TIME=02:00           # UTC
RETENTION_DAYS=365

# Events
TRACK_CONVERSATIONS=true
TRACK_MESSAGES=true
TRACK_USER_ACTIONS=true
ENABLE_HEAT_MAPS=false

# Metrics
EXPORT_PROMETHEUS=true
EXPORT_GRAFANA=true
GRAFANA_URL=http://localhost:3000
GRAFANA_API_KEY=

# Kafka
ANALYTICS_KAFKA_TOPIC_EVENTS=event.tracked
```

---

## Billing Service

```bash
# Port
BILLING_SERVICE_PORT=8008

# Payment Provider
PAYMENT_PROVIDER=stripe            # stripe or custom
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PRICE_ID_PROFESSIONAL=price_...
STRIPE_PRICE_ID_BUSINESS=price_...

# Billing
BILLING_CYCLE=monthly
INVOICE_GENERATION_DAY=1           # Day of month
TAX_RATE=0.0                       # Percentage (0-1)

# Usage Metering
METERING_INTERVAL=3600             # 1 hour
CONVERSATION_LIMIT_STRICT=true     # Block if exceeded
AI_TOKEN_COST_ANTHROPIC=0.002      # Per 1K tokens
AI_TOKEN_COST_OPENAI=0.003
AI_TOKEN_COST_GEMINI=0.0015

# Kafka
BILLING_KAFKA_TOPIC_USAGE=usage.metered
BILLING_KAFKA_TOPIC_PAYMENT=payment.processed
```

---

## Integration Service

```bash
# Port
INTEGRATION_SERVICE_PORT=8009

# OAuth
OAUTH_CALLBACK_URL=https://api.threadly.dev/oauth/callback
OAUTH_TOKEN_EXPIRY=3600

# Integrations Enabled
ENABLE_SLACK=true
ENABLE_ZAPIER=true
ENABLE_MAKE=true
ENABLE_INTERCOM=true
ENABLE_HUBSPOT=true
ENABLE_SALESFORCE=true

# API Keys
SLACK_CLIENT_ID=
SLACK_CLIENT_SECRET=
ZAPIER_API_KEY=
MAKE_API_KEY=

# Webhooks
WEBHOOK_VERIFICATION_ENABLED=true
WEBHOOK_TIMEOUT=30
WEBHOOK_RETRY_ATTEMPTS=3
WEBHOOK_RETRY_BACKOFF=exponential
```

---

## AI Service (FastAPI)

```bash
# Port
AI_SERVICE_PORT=8010

# LLM Providers
LLM_PROVIDER_PRIMARY=anthropic      # anthropic, openai, gemini
LLM_PROVIDER_FALLBACK=openai
LLM_TIMEOUT=30

# Anthropic
ANTHROPIC_API_KEY=sk-ant-...
ANTHROPIC_MODEL=claude-3-5-sonnet-20241022
ANTHROPIC_MAX_TOKENS=4096

# OpenAI
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4-turbo
OPENAI_MAX_TOKENS=8192

# Google Gemini
GOOGLE_API_KEY=
GOOGLE_GEMINI_MODEL=gemini-1.5-pro

# Embeddings
EMBEDDING_PROVIDER=local            # local or voyage-ai
VOYAGE_API_KEY=

# RAG
RAG_ENABLED=true
RAG_TOP_K=5
RAG_SCORE_THRESHOLD=0.7
RERANKER_ENABLED=false
RERANKER_MODEL=

# Memory
MEMORY_BUILDER_ENABLED=true
MEMORY_WINDOW_SIZE=10               # Last N messages
MEMORY_SUMMARIZATION_ENABLED=true
MEMORY_MAX_TOKENS=2000

# Streaming
STREAMING_ENABLED=true
STREAM_CHUNK_SIZE=50                # tokens

# Cost Tracking
COST_TRACKING_ENABLED=true
LANGFUSE_ENABLED=false
LANGFUSE_API_KEY=
LANGFUSE_SECRET_KEY=

# Rate Limiting
RATE_LIMIT_REQUESTS_PER_MINUTE=100
RATE_LIMIT_TOKENS_PER_DAY=1000000

# Logging
LOG_REQUESTS=true
LOG_RESPONSES=true
LOG_TOKENS=true
LOG_COST=true
```

---

## Frontend (Next.js)

```bash
# Server
NEXT_PUBLIC_API_BASE=https://api.threadly.dev
NEXT_PUBLIC_WIDGET_URL=https://cdn.threadly.dev/widget.js

# Observability
NEXT_PUBLIC_SENTRY_DSN=
NEXT_PUBLIC_AMPLITUDE_KEY=
NEXT_PUBLIC_MIXPANEL_TOKEN=

# Analytics
NEXT_PUBLIC_GA_ID=
NEXT_PUBLIC_HOTJAR_ID=

# Feature Flags
NEXT_PUBLIC_FEATURE_KB=true
NEXT_PUBLIC_FEATURE_INTEGRATIONS=true
NEXT_PUBLIC_FEATURE_ANALYTICS=true
NEXT_PUBLIC_FEATURE_TEAM=true
```

---

## Widget (Preact)

```bash
# Build
VITE_API_BASE=https://api.threadly.dev
VITE_CENTRIFUGO_URL=https://realtime.threadly.dev
VITE_CDN_BASE=https://cdn.threadly.dev

# Security
VITE_ALLOWED_ORIGINS=*              # Comma-separated origins
VITE_CSP_ENABLED=true
VITE_XSS_PROTECTION=true
```

---

## Docker & Infrastructure

```bash
# Docker
DOCKER_REGISTRY=registry.threadly.dev
DOCKER_IMAGE_TAG=latest
DOCKER_PULL_POLICY=IfNotPresent

# Kubernetes
KUBE_NAMESPACE=default
KUBE_REPLICAS=3
KUBE_RESOURCE_REQUESTS_CPU=100m
KUBE_RESOURCE_REQUESTS_MEMORY=256Mi
KUBE_RESOURCE_LIMITS_CPU=500m
KUBE_RESOURCE_LIMITS_MEMORY=1Gi

# Secrets Management
SECRETS_PROVIDER=doppler            # doppler or aws-secrets-manager
DOPPLER_TOKEN=
AWS_REGION=us-east-1
AWS_SECRETS_ARN=
```

---

## Development Only

```bash
# Debug
DEBUG=true
DEBUG_LOG_LEVEL=DEBUG
ENABLE_PROFILING=true

# Seeding
SEED_DATABASE=false
SEED_SAMPLE_BOTS=true
SEED_SAMPLE_CONVERSATIONS=true

# Mocking
MOCK_LLM_RESPONSES=false
MOCK_LLM_LATENCY=100               # milliseconds

# Hot Reload
HOT_RELOAD_ENABLED=true
WATCH_DIRECTORIES=src,handlers,models
```

---

## How to Load Variables

### Docker Compose

```bash
# Create .env file
echo "DATABASE_URL=postgresql://user:pass@postgres:5432/threadly" > .env
echo "JWT_SECRET=your-secret-key" >> .env

# Load in docker-compose.yml
docker-compose --env-file .env up
```

### Kubernetes

```bash
# Create secret
kubectl create secret generic threadly-env \
  --from-literal=DATABASE_URL=postgresql://... \
  --from-literal=JWT_SECRET=...

# Reference in deployment
env:
- name: DATABASE_URL
  valueFrom:
    secretKeyRef:
      name: threadly-env
      key: DATABASE_URL
```

### Development (Next.js)

```bash
# Create .env.local
NEXT_PUBLIC_API_BASE=http://localhost:8080

# Automatically loaded by Next.js
```

---

## Validation

All variables are validated on startup. If missing or invalid, service fails with clear error message.

**Check service status:**
```bash
curl http://localhost:8001/health
```

---

**Last Updated:** May 25, 2026
