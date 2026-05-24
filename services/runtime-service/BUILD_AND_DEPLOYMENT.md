# Runtime Service - Build and Deployment Guide

## Prerequisites

### System Requirements
- Java 21 JDK
- PostgreSQL 13+
- Apache Kafka 3.x
- Consul (for service discovery)
- OpenTelemetry Collector (optional, for observability)

### Build Tools
- Maven 3.8+
- Git

## Building the Service

### Build Steps

1. **Clean and Build**
```bash
cd /Users/yasva/Kapture/Microservice/Project/Threadly/services/runtime-service
mvn clean package
```

2. **Skipping Tests (if needed)**
```bash
mvn clean package -DskipTests
```

3. **Build with Logging**
```bash
mvn clean package -X
```

## Database Setup

### 1. Create Database Schema

```sql
CREATE SCHEMA IF NOT EXISTS runtime_service;
GRANT ALL PRIVILEGES ON SCHEMA runtime_service TO threadly;
```

### 2. Run Migrations

Migrations are automatic on startup via Flyway:
- `V4__runtime_schema.sql` creates all tables and indexes

### 3. Verify Schema

```sql
\dt runtime_service.*
\di runtime_service.*
```

## Configuration

### Environment Variables

```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=threadly
export DB_USER=threadly
export DB_PASSWORD=your_password

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Consul
export CONSUL_HOST=localhost
export CONSUL_PORT=8500

# OpenTelemetry
export OTEL_EXPORTER_OTLP_ENDPOINT=localhost:4317

# Application
export SERVER_PORT=3004
```

### application.yml Configuration

Key configuration sections:
```yaml
spring:
  application:
    name: runtime-service
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: runtime_service

server:
  port: 3004

runtime:
  max-execution-depth: 100
  node-execution-timeout-ms: 30000
```

## Running the Service

### Option 1: JAR Execution

```bash
java -jar target/runtime-service-0.1.0-SNAPSHOT.jar
```

### Option 2: Spring Boot Maven Plugin

```bash
mvn spring-boot:run
```

### Option 3: Docker (if Dockerfile available)

```bash
docker build -t threadly/runtime-service:0.1.0 .
docker run -p 3004:3004 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e CONSUL_HOST=consul \
  threadly/runtime-service:0.1.0
```

## Service Verification

### Health Check

```bash
curl http://localhost:3004/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

### Metrics Endpoint

```bash
curl http://localhost:3004/actuator/metrics
```

### Service Discovery

Verify in Consul:
```bash
curl http://localhost:8500/v1/catalog/service/runtime-service
```

## API Testing

### Create Session

```bash
curl -X POST http://localhost:3004/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "botId": "bot-123",
    "flowId": "flow-456",
    "visitorId": "visitor-789"
  }'
```

Expected response:
```json
{
  "id": "session-uuid",
  "botId": "bot-123",
  "flowId": "flow-456",
  "visitorId": "visitor-789",
  "state": "ACTIVE",
  "variables": {},
  "tokenUsageCount": 0,
  "createdAt": "2025-05-24T10:30:00"
}
```

### Get Session

```bash
curl http://localhost:3004/api/v1/sessions/{sessionId}
```

### Send Message

```bash
curl -X POST http://localhost:3004/api/v1/sessions/{sessionId}/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, bot!",
    "userId": "user-123"
  }'
```

### Get Execution Log

```bash
curl http://localhost:3004/api/v1/sessions/{sessionId}/execution-log
```

## Monitoring and Debugging

### Application Logs

```bash
tail -f application.log
```

Look for:
- Session creation/termination
- Node execution traces
- Variable resolution details
- Error stack traces

### Database Queries

Monitor slow queries:
```sql
SELECT query, calls, mean_time 
FROM pg_stat_statements 
WHERE query LIKE '%runtime_service%' 
ORDER BY mean_time DESC;
```

### Kafka Topics

List topics:
```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

Monitor events:
```bash
kafka-console-consumer.sh --topic threadly.sessions \
  --from-beginning \
  --bootstrap-server localhost:9092
```

## Performance Tuning

### Database Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 600000
      max-lifetime: 1800000
```

### JPA/Hibernate

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
```

### JVM Settings

```bash
java -Xmx2g -Xms1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar target/runtime-service-0.1.0-SNAPSHOT.jar
```

## Troubleshooting

### Common Issues

**1. Database Connection Failed**
- Check PostgreSQL is running
- Verify credentials in environment variables
- Check network connectivity: `ping localhost`

**2. Migration Errors**
- Verify schema exists: `\dn` in psql
- Check migration file syntax
- Reset Flyway: `DROP TABLE flyway_schema_history;`

**3. Kafka Connection Failed**
- Verify Kafka broker is running
- Check bootstrap servers configuration
- Test connectivity: `telnet localhost 9092`

**4. Consul Registration Failed**
- Verify Consul agent is running
- Check service discovery configuration
- Test endpoint: `curl http://localhost:8500/v1/agent/self`

**5. Node Executor Not Found**
- Verify executor class exists
- Check component scan includes executor package
- Verify @Component annotation present

### Debug Mode

Enable debug logging:
```yaml
logging:
  level:
    dev.threadly: DEBUG
    org.springframework: DEBUG
    org.hibernate: DEBUG
```

### Heap Dump on OOM

```bash
java -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heap.dump \
  -jar target/runtime-service-0.1.0-SNAPSHOT.jar
```

## Security Considerations

1. **Database Passwords**: Use environment variables, not hardcoded
2. **API Keys**: Implement API authentication (not included in basic implementation)
3. **SQL Injection**: Use parameterized queries (JPA does this)
4. **CORS**: Configure if frontend is on different domain
5. **Rate Limiting**: Implement for production

## Backup and Recovery

### Database Backup

```bash
pg_dump -U threadly -h localhost threadly > backup.sql
```

### Database Restore

```bash
psql -U threadly -h localhost threadly < backup.sql
```

## Deployment Checklist

- [ ] Java 21 JDK installed
- [ ] PostgreSQL database created and accessible
- [ ] Kafka cluster running
- [ ] Consul agent running
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] Application builds successfully
- [ ] Health endpoint responding
- [ ] Database queries executing
- [ ] Service registered in Consul
- [ ] Metrics visible in monitoring
- [ ] Sample session creation works
- [ ] Logs being written correctly
- [ ] Performance acceptable under load

## Continuous Integration

### Maven Build in CI/CD

```bash
mvn clean package \
  -DskipTests \
  -Dmaven.test.skip=true \
  -Dfindbugs.skip=true
```

### Database Migrations in CI/CD

```bash
mvn clean flyway:migrate \
  -Dflyway.url=jdbc:postgresql://ci-db:5432/threadly \
  -Dflyway.user=ci_user \
  -Dflyway.password=$CI_DB_PASSWORD
```

## Production Deployment

### Recommended Setup
1. Use managed PostgreSQL (AWS RDS, etc.)
2. Use managed Kafka (AWS MSK, Confluent Cloud, etc.)
3. Use managed Consul or Kubernetes service discovery
4. Deploy in Docker containers
5. Use Kubernetes or similar orchestration
6. Enable comprehensive monitoring and alerting
7. Implement backup and disaster recovery
8. Use service mesh (Istio, Linkerd) for advanced features

## Support and Maintenance

- Review logs regularly for errors
- Monitor performance metrics
- Update dependencies periodically
- Test major version upgrades
- Document any custom executors added
- Maintain runbooks for common issues
