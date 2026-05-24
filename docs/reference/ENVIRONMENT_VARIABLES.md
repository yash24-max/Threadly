# Environment Variables Reference

**Last Updated**: 2025-05-24  
**Scope**: All services (Spring Boot, FastAPI, Next.js)

---

## Table of Contents

1. [Overview](#overview)
2. [Spring Boot Services (Java)](#spring-boot-services-java)
3. [Python AI Service](#python-ai-service)
4. [Frontend (Next.js)](#frontend-nextjs)
5. [Infrastructure & Deployment](#infrastructure--deployment)
6. [Local Development](#local-development)
7. [Production & Staging](#production--staging)

---

## Overview

Environment variables control service behavior across development, staging, and production. Use `.env` files (local) or secret management (production).

**Best Practices**:
- Never commit `.env` files with secrets
- Use `.env.example` as template
- Rotate secrets regularly
- Document default values
- Use `SPRING_PROFILES_ACTIVE` to switch configurations

---

## Spring Boot Services (Java)

All Java microservices share common environment variables:

### Database Configuration

```bash
# PostgreSQL Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/threadly_core
SPRING_DATASOURCE_USERNAME=threadly
SPRING_DATASOURCE_PASSWORD=SecurePassword123!
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Connection Pool
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=600000

# JPA/Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQL10Dialect
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_USE_SQL_COMMENTS=true

# Database Migrations (Flyway)
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_OUT_OF_ORDER=false
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
```

### Kafka Configuration

```bash
# Kafka Broker
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_PRODUCER_KEY_SERIALIZER=org.apache.kafka.common.serialization.StringSerializer
SPRING_KAFKA_PRODUCER_VALUE_SERIALIZER=org.springframework.kafka.support.serializer.JsonSerializer
SPRING_KAFKA_CONSUMER_KEY_DESERIALIZER=org.apache.kafka.common.serialization.StringDeserializer
SPRING_KAFKA_CONSUMER_VALUE_DESERIALIZER=org.springframework.kafka.support.serializer.JsonDeserializer
SPRING_KAFKA_CONSUMER_GROUP_ID=threadly-service-group

# Producer
SPRING_KAFKA_PRODUCER_ACKS=all
SPRING_KAFKA_PRODUCER_RETRIES=3
SPRING_KAFKA_PRODUCER_LINGER_MS=10
SPRING_KAFKA_PRODUCER_BATCH_SIZE=16384
SPRING_KAFKA_PRODUCER_COMPRESSION_TYPE=snappy

# Consumer
SPRING_KAFKA_CONSUMER_FETCH_MIN_BYTES=1024
SPRING_KAFKA_CONSUMER_FETCH_MAX_WAIT_MS=3000
SPRING_KAFKA_CONSUMER_MAX_POLL_RECORDS=500
SPRING_KAFKA_CONSUMER_SESSION_TIMEOUT_MS=30000
```

### Redis Configuration

```bash
# Redis Connection
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
SPRING_REDIS_TIMEOUT=60000
SPRING_REDIS_DATABASE=0

# Redis Pool
SPRING_REDIS_JEDIS_POOL_MAX_ACTIVE=20
SPRING_REDIS_JEDIS_POOL_MAX_IDLE=10
SPRING_REDIS_JEDIS_POOL_MIN_IDLE=5
SPRING_REDIS_JEDIS_POOL_MAX_WAIT_MS=-1

# Caching
SPRING_CACHE_TYPE=redis
SPRING_CACHE_REDIS_TIME_TO_LIVE=3600000
SPRING_CACHE_REDIS_USE_KEY_PREFIX=true
```

### Security & JWT

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-key-min-256-bits-long-change-in-prod
JWT_EXPIRATION_MS=86400000  # 24 hours
JWT_REFRESH_EXPIRATION_MS=604800000  # 7 days

# OAuth2 (if using)
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISS_URI=https://your-auth-provider.com
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://your-auth-provider.com/.well-known/jwks.json

# CORS
SPRING_WEB_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://threadly.io
SPRING_WEB_CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
SPRING_WEB_CORS_ALLOW_CREDENTIALS=true
```

### Service Discovery (Consul)

```bash
# Consul Configuration
SPRING_CLOUD_CONSUL_HOST=localhost
SPRING_CLOUD_CONSUL_PORT=8500
SPRING_CLOUD_CONSUL_DISCOVERY_ENABLED=true
SPRING_CLOUD_CONSUL_DISCOVERY_PREFER_IP_ADDRESS=true
SPRING_CLOUD_CONSUL_DISCOVERY_SERVICE_NAME=identity-service
SPRING_CLOUD_CONSUL_DISCOVERY_PORT=3001
SPRING_CLOUD_CONSUL_DISCOVERY_HEALTH_CHECK_PATH=/health
SPRING_CLOUD_CONSUL_DISCOVERY_HEALTH_CHECK_INTERVAL=10s
```

### Observability

```bash
# OpenTelemetry
OTEL_SDK_DISABLED=false
OTEL_TRACES_EXPORTER=jaeger
OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
OTEL_EXPORTER_JAEGER_AGENT_HOST=localhost
OTEL_EXPORTER_JAEGER_AGENT_PORT=6831
OTEL_METRICS_EXPORTER=prometheus
OTEL_LOGS_EXPORTER=otlp

# Spring Boot Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus,info
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when-authorized
MANAGEMENT_METRICS_ENABLE_PROCESS=true
MANAGEMENT_METRICS_ENABLE_JVM=true
```

### Application-Specific

```bash
# Service Configuration
SERVER_PORT=3001  # Identity Service (3001-3009 for each service)
SERVER_SERVLET_CONTEXT_PATH=/api/v1
SPRING_APPLICATION_NAME=identity-service
SERVER_TOMCAT_THREADS_MAX=200
SERVER_TOMCAT_ACCEPT_COUNT=100

# Profiles
SPRING_PROFILES_ACTIVE=dev,local
# Options: dev, staging, prod

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_DEV_THREADLY=DEBUG
LOGGING_PATTERN_CONSOLE=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
LOGGING_FILE_NAME=logs/threadly.log
LOGGING_FILE_MAX_SIZE=10MB
LOGGING_FILE_MAX_HISTORY=10
```

### Migration Settings (Phase 2+)

```bash
# Dual-Write Configuration (Phase 2)
MIGRATION_DUAL_WRITE_ENABLED=false
MIGRATION_DUAL_WRITE_FAIL_STRATEGY=log_only
MIGRATION_DUAL_WRITE_TIMEOUT_MS=5000
MIGRATION_DUAL_WRITE_ASYNC=true
MIGRATION_DUAL_WRITE_SERVICE_BASE_URL=http://api-gateway:8080
MIGRATION_DUAL_WRITE_BATCH_SIZE=50
MIGRATION_DUAL_WRITE_BATCH_INTERVAL_MS=1000
MIGRATION_DUAL_WRITE_METRICS_ENABLED=true
MIGRATION_DUAL_WRITE_LAG_ALERT_THRESHOLD_MS=1000
MIGRATION_DUAL_WRITE_FAILURE_ALERT_THRESHOLD_PERCENT=1.0
```

---

## Python AI Service

FastAPI sidecar for LLM orchestration:

```bash
# FastAPI Configuration
FASTAPI_ENV=development
DEBUG=true
LOG_LEVEL=INFO

# Server
HOST=0.0.0.0
PORT=8001
WORKERS=4

# LLM Provider Configuration
LLM_PROVIDER=anthropic  # anthropic, openai, together
ANTHROPIC_API_KEY=sk-ant-...
OPENAI_API_KEY=sk-...
TOGETHER_API_KEY=...

# Model Selection
LLM_MODEL=claude-3-sonnet-20240229
LLM_TEMPERATURE=0.7
LLM_MAX_TOKENS=2048
LLM_TOP_P=0.95

# RAG Configuration
QDRANT_URL=http://localhost:6333
EMBEDDING_MODEL=sentence-transformers/all-MiniLM-L6-v2
EMBEDDING_BATCH_SIZE=32
CHUNK_SIZE=512
CHUNK_OVERLAP=50

# Vector DB
VECTOR_DB_HOST=localhost
VECTOR_DB_PORT=6333
VECTOR_DB_NAME=threadly
VECTOR_DB_COLLECTION=conversations

# Conversation Memory
MEMORY_BACKEND=redis
REDIS_URL=redis://localhost:6379/0
MEMORY_MAX_HISTORY=50
MEMORY_RETENTION_DAYS=90

# Prompting
SYSTEM_PROMPT_PATH=./prompts/system.md
RETRIEVAL_K=5  # Number of similar docs to retrieve
RELEVANCE_THRESHOLD=0.3

# Rate Limiting
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_PERIOD_SECONDS=60

# External Services
THREADLY_API_URL=http://localhost:8080
THREADLY_API_KEY=sk-...

# Monitoring
SENTRY_DSN=https://...@sentry.io/...
DATADOG_API_KEY=...
```

---

## Frontend (Next.js)

Threadly Web client:

```bash
# Next.js Configuration
NODE_ENV=development
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_WIDGET_URL=http://localhost:3000/widget.js
NEXT_PUBLIC_ENVIRONMENT=local

# Authentication
NEXTAUTH_SECRET=super-secret-key-change-in-prod
NEXTAUTH_URL=http://localhost:3000
NEXT_PUBLIC_AUTH_PROVIDER=threadly
NEXTAUTH_PROVIDERS_GITHUB_ID=...
NEXTAUTH_PROVIDERS_GITHUB_SECRET=...

# Analytics
NEXT_PUBLIC_GA_ID=G-XXXXXXXXXX
NEXT_PUBLIC_SENTRY_DSN=https://...@sentry.io/...
NEXT_PUBLIC_MIXPANEL_TOKEN=...
NEXT_PUBLIC_POSTHOG_KEY=...

# Feature Flags
NEXT_PUBLIC_ENABLE_BETA_FEATURES=false
NEXT_PUBLIC_ENABLE_DARK_MODE=true
NEXT_PUBLIC_ENABLE_CUSTOMIZATION=true

# AI Features
NEXT_PUBLIC_AI_SERVICE_URL=http://localhost:8001
NEXT_PUBLIC_AI_ENABLED=true

# Stripe (Billing)
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...

# Social
NEXT_PUBLIC_TWITTER_URL=https://twitter.com/threadly
NEXT_PUBLIC_GITHUB_URL=https://github.com/threadly

# Content
NEXT_PUBLIC_DOCS_URL=https://docs.threadly.io
NEXT_PUBLIC_SUPPORT_EMAIL=support@threadly.io
```

---

## Infrastructure & Deployment

### Docker Compose

```bash
# Compose Configuration
COMPOSE_PROJECT_NAME=threadly
COMPOSE_FILE=docker-compose.yml
POSTGRES_DB=threadly_core
POSTGRES_USER=threadly
POSTGRES_PASSWORD=SecurePassword123!
POSTGRES_INITDB_ARGS=--encoding=UTF8
KAFKA_ADVERTISED_HOST_NAME=kafka
KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
REDIS_PASSWORD=
CONSUL_VERSION=latest
QDRANT_API_KEY=
ELASTICSEARCH_PASSWORD=changeme
```

### Kubernetes

```bash
# K8s Configuration
KUBE_NAMESPACE=threadly
KUBE_CONTEXT=docker-desktop
REGISTRY_HOST=docker.io
REGISTRY_USERNAME=threadlyio
REGISTRY_PASSWORD=...

# Resource Limits
POD_CPU_REQUEST=100m
POD_MEMORY_REQUEST=256Mi
POD_CPU_LIMIT=500m
POD_MEMORY_LIMIT=512Mi
```

---

## Local Development

Set these in `.env.local`:

```bash
# Copy from .env.example
cp .env.example .env.local

# Edit .env.local
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/threadly_dev
SPRING_DATASOURCE_PASSWORD=dev-password
JWT_SECRET=dev-secret-key-256-bits-minimum
DEBUG=true
LOGGING_LEVEL_ROOT=DEBUG
```

**Start services**:
```bash
docker-compose up -d postgres kafka redis consul
export $(cat .env.local | xargs)
mvn spring-boot:run  # For each service
```

---

## Production & Staging

### Sensitive Variables (Use Secret Management)

```bash
# AWS Secrets Manager
THREADLY_JWT_SECRET=***
THREADLY_STRIPE_SECRET_KEY=sk_live_***
THREADLY_DATABASE_PASSWORD=***
THREADLY_API_KEY_MASTER=sk_live_***
THREADLY_SENDGRID_API_KEY=SG.***
THREADLY_STRIPE_WEBHOOK_SECRET=whsec_***
```

### Staging Configuration

```bash
SPRING_PROFILES_ACTIVE=staging
SPRING_DATASOURCE_URL=jdbc:postgresql://staging-db.rds.amazonaws.com:5432/threadly
DEBUG=false
LOGGING_LEVEL_ROOT=INFO
NEXT_PUBLIC_ENVIRONMENT=staging
NEXTAUTH_URL=https://staging.threadly.io
NEXT_PUBLIC_API_URL=https://api-staging.threadly.io/api/v1
```

### Production Configuration

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.rds.amazonaws.com:5432/threadly
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=50
DEBUG=false
LOGGING_LEVEL_ROOT=WARN
NEXT_PUBLIC_ENVIRONMENT=production
NEXTAUTH_URL=https://threadly.io
NEXT_PUBLIC_API_URL=https://api.threadly.io/api/v1
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://threadly.io,https://www.threadly.io
```

---

## Verification

Check variable values at runtime:

### Spring Boot

```bash
# Check active profile
curl http://localhost:3001/actuator/env | jq '.propertySources[] | select(.name | contains("systemProperties"))'

# Check property value
curl http://localhost:3001/actuator/env/SPRING_DATASOURCE_URL | jq '.property.value'
```

### FastAPI

```bash
# Check environment
curl http://localhost:8001/health | jq '.environment'
```

### Frontend

```bash
# Check public environment variables
console.log(process.env.NEXT_PUBLIC_API_URL)
// Output: http://localhost:8080/api/v1
```

---

## Related Documentation

- [Development Setup](./10-dev-setup.md) — Local environment guide
- [Security](./SECURITY.md) — Secrets management best practices
- [Deployment](./11-deployment.md) — Staging & production configuration

