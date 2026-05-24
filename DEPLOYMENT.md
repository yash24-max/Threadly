# Threadly Deployment Guide

Complete deployment runbook for Threadly microservices across development, staging, and production environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Kubernetes Cluster Setup](#kubernetes-cluster-setup)
4. [Image Building and Registry](#image-building-and-registry)
5. [Deploying to Kubernetes](#deploying-to-kubernetes)
6. [Environment-Specific Deployments](#environment-specific-deployments)
7. [Database Migrations](#database-migrations)
8. [Secrets Management](#secrets-management)
9. [Monitoring and Observability](#monitoring-and-observability)
10. [Rollback Procedures](#rollback-procedures)
11. [Incident Response](#incident-response)
12. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Tools

- **Docker**: v24.0+
- **Kubernetes**: v1.27+ (kubectl installed)
- **Helm**: v3.12+ (optional but recommended)
- **Kustomize**: v5.0+ (for multi-environment deployments)
- **PostgreSQL Client**: psql (for database migrations)

### Required Permissions

- Docker Hub or GitHub Container Registry write access
- Kubernetes cluster admin role or namespace admin for `threadly` namespace
- Secrets access in Kubernetes

---

## Local Development Setup

### Option 1: Docker Compose (Recommended for Local Dev)

```bash
cd infrastructure/docker
docker compose build
docker compose up -d --wait
docker compose ps
```

### Option 2: Local Kubernetes (Kind/Minikube)

```bash
kind create cluster --name threadly
kubectl config use-context kind-threadly
kubectl create namespace threadly
kubectl apply -k infrastructure/kubernetes/overlays/dev
```

---

## Kubernetes Cluster Setup

### Initial Setup

```bash
kubectl create namespace threadly
kubectl label namespace threadly name=threadly
```

---

## Image Building and Registry

### Building Images Locally

```bash
# Build identity-service
docker build \
  -t threadly/identity-service:v1.0.0 \
  --build-arg SERVICE_NAME=identity-service \
  -f Dockerfile.core .

# Build all services
for service in identity workspace flow runtime conversation knowledge analytics billing integration; do
  docker build \
    -t threadly/${service}-service:v1.0.0 \
    --build-arg SERVICE_NAME=${service}-service \
    -f Dockerfile.core .
done

# Build supporting services
docker build -t threadly/threadly-web:v1.0.0 threadly-web/
docker build -t threadly/threadly-ai:v1.0.0 threadly-ai/
docker build -t threadly/threadly-widget:v1.0.0 threadly-widget/
```

---

## Deploying to Kubernetes

### Using Kustomize (Recommended)

```bash
# Development deployment
kubectl apply -k infrastructure/kubernetes/overlays/dev

# Production deployment
kubectl apply -k infrastructure/kubernetes/overlays/prod
```

### Verify Deployment

```bash
kubectl get pods -n threadly
kubectl get svc -n threadly
kubectl get ingress -n threadly
```

---

## Environment-Specific Deployments

- **Development**: 1 replica, minimal resources, local registry
- **Staging**: 2 replicas, moderate resources
- **Production**: 3+ replicas, full resources, HA enabled

---

## Database Migrations

```bash
# Connect to PostgreSQL
kubectl exec -it -n threadly postgres-0 -- psql -U threadly

# View schemas
\dn
```

---

## Secrets Management

```bash
# Create secret from .env file
kubectl create secret generic app-secrets \
  --from-env-file=.env \
  -n threadly
```

---

## Monitoring and Observability

```bash
# Port forward to Prometheus
kubectl port-forward -n threadly svc/prometheus-service 9090:9090

# Port forward to Grafana
kubectl port-forward -n threadly svc/grafana-service 3000:3000
```

---

## Rollback Procedures

```bash
# Rollback deployment
kubectl rollout undo deployment/identity-service -n threadly

# Check rollout status
kubectl rollout status deployment/identity-service -n threadly
```

---

## Incident Response

```bash
# Check pod status
kubectl get pods -n threadly

# View logs
kubectl logs -n threadly identity-service-xxxxx --tail 100

# Check resource usage
kubectl top pods -n threadly
```

---

## Troubleshooting

```bash
# Check events
kubectl describe pod <pod-name> -n threadly

# Test DNS resolution
kubectl exec -it -n threadly identity-service-xxxxx -- nslookup postgres-service

# Check PVC status
kubectl get pvc -n threadly
```

---

For complete deployment guide, see the detailed DEPLOYMENT.md in the project root.
