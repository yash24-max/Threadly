# Frequently Asked Questions (FAQ)

**Last Updated**: 2025-05-24

---

## Getting Started

### Q: How do I set up Threadly locally?

**A**: Follow the Development Setup guide:
```bash
git clone https://github.com/threadly/threadly.git
cd threadly
make install
make health  # Verify all services
```

See [docs/architecture/10-dev-setup.md](../architecture/10-dev-setup.md)

---

### Q: What services do I need to run?

**A**: For local development, Docker Compose starts everything:
```bash
docker-compose up -d
```

This includes:
- 9 microservices (Spring Boot 3.3)
- PostgreSQL
- Kafka
- Redis
- Consul (service discovery)
- Qdrant (vector DB)
- Nginx (API Gateway)

---

### Q: What's the default port for each service?

**A**: 
```
Identity Service:       3001
Workspace Service:      3002
Flow Service:           3003
Runtime Service:        3004
Conversation Service:   3005
Knowledge Service:      3006
Analytics Service:      3007
Billing Service:        3008
Integration Service:    3009
AI Service (FastAPI):   8001
Frontend (Next.js):     3000
API Gateway (Nginx):    8080
```

---

## API & Integration

### Q: How do I authenticate with the API?

**A**: Use JWT Bearer tokens:

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"username":"user@threadly.dev","password":"Password123!"}'

# 2. Use token
curl http://localhost:8080/api/v1/bots \
  -H "Authorization: Bearer <token>"
```

See [docs/api/rest-endpoints.md#authentication](../api/rest-endpoints.md#authentication)

---

### Q: How do I use the Python SDK?

**A**: Install and authenticate:

```python
from threadly_sdk import ThreadlyAPI

client = ThreadlyAPI(api_key='sk_live_xyz789')
bots = client.bots.list(page=1, limit=20)
```

See [docs/api/rest-endpoints.md#python](../api/rest-endpoints.md#python)

---

### Q: What Kafka topics are available?

**A**: 9 topics, one per service:
- `user-events`
- `org-events`
- `flow-events`
- `session-events`
- `conversation-events`
- `kb-events`
- `analytics-events`
- `billing-events`
- `integration-events`

See [docs/api/kafka-topics.md](../api/kafka-topics.md)

---

## Deployment

### Q: How do I deploy to staging?

**A**: Push to main → automatic staging deploy:

```bash
git push origin main
# GitHub Actions triggers deployment automatically
# Check: https://github.com/threadly/threadly/actions
```

### Q: How do I deploy to production?

**A**: Create a release tag:

```bash
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3
# Triggers production deploy with safety checks
```

See [docs/architecture/11-deployment.md](../architecture/11-deployment.md)

---

### Q: How long does deployment take?

**A**: 
- Staging: 5-10 minutes
- Production: 15-20 minutes (with health checks + gradual rollout)

---

### Q: Can I rollback a deployment?

**A**: Yes, for production:

```bash
git push origin v1.2.2  # Redeploy previous version
# Or use kubectl rollout:
kubectl rollout undo deployment/identity-service -n threadly
```

---

## Microservices Migration

### Q: What is Phase 1, 2, 3?

**A**: Zero-downtime migration from monolith to microservices:

- **Phase 1**: Shadow mode (read-only services alongside monolith) — Week 1
- **Phase 2**: Dual-write (writes go to both) — Week 2
- **Phase 3**: Cutover (microservices become primary) — Week 3
- **Phase 4**: Observe & decommission monolith — Week 4

See [docs/migration/PHASE_1_SHADOW_MODE.md](../migration/PHASE_1_SHADOW_MODE.md)

---

### Q: Can I rollback if Phase 3 fails?

**A**: Yes, within 7 days:

```bash
# Automatic fallback if error rate > 1%
# Or manual:
curl -X POST http://localhost:8000/admin/cutover/fallback
```

See [docs/migration/PHASE_3_CUTOVER.md#rollback-procedure](../migration/PHASE_3_CUTOVER.md#rollback-procedure)

---

## Database

### Q: How do I reset the local database?

**A**:
```bash
# Delete and recreate
docker-compose down postgres
docker volume rm threadly_postgres_data
docker-compose up -d postgres
# Wait for migrations to run automatically
```

---

### Q: How do I check database schemas?

**A**:
```bash
# Connect to database
docker exec -it postgres psql -U threadly

# List schemas
\dn

# List tables in identity service schema
\dt identity_service.*

# Check row counts
SELECT schema_name, COUNT(*) FROM information_schema.tables
WHERE table_schema LIKE '%_service'
GROUP BY schema_name;
```

---

### Q: How do I reset a consumer group lag?

**A**:
```bash
# Reset to latest (skip backlog)
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --reset-offsets \
  --to-latest \
  --execute
```

See [docs/api/kafka-topics.md#monitor-consumer-lag](../api/kafka-topics.md#monitor-consumer-lag)

---

## Troubleshooting

### Q: A service won't start. How do I debug?

**A**: Check logs:

```bash
# Logs for specific service
docker logs threadly-identity-service

# Recent logs
docker logs -f threadly-identity-service --tail 50

# Check Spring Boot actuator
curl http://localhost:3001/actuator/health

# Check environment variables
curl http://localhost:3001/actuator/env
```

---

### Q: I see "Dual-Write lag too high" error. What do I do?

**A**: Services are slow to apply writes. Options:

1. Increase async worker threads
2. Check service resource limits (CPU, memory)
3. Check database connection pool
4. Restart affected service

See [docs/migration/PHASE_2_DUAL_WRITE.md#common-issues](../migration/PHASE_2_DUAL_WRITE.md#common-issues)

---

### Q: Tests are failing. How do I debug?

**A**: Run with verbose logging:

```bash
# Single test
mvn test -Dtest=UserControllerTest#testGetUser

# With debug output
mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=debug test

# Watch mode (TypeScript)
npm test --workspace=frontend/threadly-web -- --watch
```

---

### Q: Kafka consumer is stuck. How do I fix it?

**A**: 
```bash
# Check lag
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --describe

# Restart consumer (service restart)
docker-compose restart conversation-service

# Or reset offset to latest
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group conversation-service-consumer \
  --reset-offsets --to-latest --execute
```

See [docs/runbooks/RUNBOOK_KAFKA_RECOVERY.md](../runbooks/RUNBOOK_KAFKA_RECOVERY.md)

---

## Performance & Optimization

### Q: How do I optimize database queries?

**A**: Use `@Query` with JOIN FETCH and pagination:

```java
@Query("SELECT u FROM User u LEFT JOIN FETCH u.organization WHERE u.id = :id")
Optional<User> findByIdWithOrg(@Param("id") String id);
```

Always use `.limit(20)` for list endpoints.

---

### Q: How do I cache expensive operations?

**A**: Use Spring Cache abstraction:

```java
@Cacheable(value = "users", key = "#id", ttl = "3600")
public User findById(String id) {
  return userRepository.findById(id);
}

@CacheEvict(value = "users", key = "#id")
public User update(String id, UserDTO dto) {
  return userRepository.save(...);
}
```

---

### Q: What are the latency SLOs?

**A**:
- p50: < 100ms
- p95: < 300ms
- p99: < 600ms
- Error rate: < 0.1%

---

## Monitoring & Observability

### Q: How do I check service health?

**A**: Health endpoints return service status:

```bash
# Overall health
curl http://localhost:8080/health

# Specific service
curl http://localhost:3001/health

# Detailed health info
curl http://localhost:3001/actuator/health?full=true
```

---

### Q: Where are logs?

**A**: 
```bash
# Docker Compose
docker logs threadly-identity-service

# Kubernetes
kubectl logs -n threadly identity-service-0 -f

# Log aggregation (Loki)
# Access via Grafana: http://localhost:3000
```

---

### Q: How do I view metrics?

**A**: Prometheus + Grafana:

```
Prometheus: http://localhost:9090
Grafana: http://localhost:3000
```

**Key dashboards**:
- Service Health
- API Latency
- Error Rates
- Kafka Topics
- Database Connections

---

## Contributing

### Q: How do I contribute code?

**A**: Follow the contribution guidelines:

1. Create feature branch: `feat/description`
2. Write tests (min 80% coverage)
3. Follow commit message format: `type(scope): subject`
4. Create PR with clear description
5. Get approval from reviewer
6. Merge to main

See [docs/reference/CONTRIBUTING.md](./CONTRIBUTING.md)

---

### Q: What's the code style?

**A**:
- **Java**: Spring Boot conventions, 2-space indent
- **TypeScript**: ESLint config, run `npm lint`
- **Python**: PEP 8, use `black` formatter

---

### Q: How do I run tests?

**A**:
```bash
# All tests
make test

# Unit tests only
mvn test

# Integration tests
mvn verify -DskipITs=false

# Frontend tests
npm test --workspace=frontend/threadly-web
```

---

## Security

### Q: How do I manage secrets?

**A**: Never commit secrets. Use:

- **Local**: `.env` file (git-ignored)
- **Production**: AWS Secrets Manager / Vault
- **CI/CD**: GitHub Secrets

See [docs/reference/SECURITY.md](./SECURITY.md)

---

### Q: How is multi-tenancy handled?

**A**: Tenant isolation via:

1. Hibernate `@Filter` on `org_id`
2. JWT token includes `org_id`
3. Queries enforce `org_id` in WHERE clause
4. Database per organization (schemas separated)

---

## Billing & Subscriptions

### Q: How do I test Stripe integration?

**A**: Use Stripe test keys:

```bash
# .env
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
```

Use Stripe test card: `4242 4242 4242 4242`

---

## Contact & Support

### Q: Who do I contact for help?

**A**: 
- **Technical questions**: GitHub Discussions
- **Bug reports**: GitHub Issues
- **Security issues**: security@threadly.io (do not open public issue)
- **General support**: support@threadly.io

---

## Additional Resources

- [Architecture Overview](../architecture/03-architecture.md)
- [Microservices Architecture](../architecture/18-microservices-architecture.md)
- [Development Setup](../architecture/10-dev-setup.md)
- [REST API Endpoints](../api/rest-endpoints.md)
- [Kafka Topics](../api/kafka-topics.md)
- [Security Guidelines](./SECURITY.md)
- [Troubleshooting Guide](./TROUBLESHOOTING.md)

