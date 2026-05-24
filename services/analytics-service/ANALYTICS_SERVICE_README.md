# Analytics Service

Production-ready analytics microservice for Threadly, providing event tracking, real-time metrics aggregation, and dashboards.

## Overview

The Analytics Service captures domain events from Kafka topics, processes them into actionable metrics, and provides analytics APIs for dashboards, reports, and insights.

### Key Features

- **Event Tracking**: Captures events from all Threadly services via Kafka
- **Real-Time Metrics**: Computes metrics from events using pluggable processors
- **Daily Rollups**: Pre-aggregated metrics for efficient historical queries
- **Custom Dashboards**: User-defined analytics dashboards with caching
- **Report Generation**: CSV, JSON, and PDF export formats
- **Multi-Tenancy**: Complete org_id isolation for all data
- **Performance**: Time-series optimized with strategic indexing
- **Reliability**: Event processing with retry logic and manual acknowledgment

## Architecture

### Components

#### Entities
- `AnalyticsEvent`: Raw events from domain (90-day retention)
- `Metric`: Computed metrics from events
- `DailyRollup`: Pre-aggregated daily metrics (1-year retention)
- `DashboardView`: User-created dashboard configurations

#### Kafka Event Listeners
- `ConversationStartedEventListener`: Tracks conversation initiation
- `ConversationEndedEventListener`: Tracks conversation completion
- `MessageAddedEventListener`: Tracks message exchanges
- `AiReplyRequestedEventListener`: Tracks AI call initiation
- `HandoffInitiatedEventListener`: Tracks human handoffs
- `NodeExecutedEventListener`: Tracks flow node execution

#### Metric Processors
- `ConversationMetricProcessor`: Conversation-level metrics
- `AiCallMetricProcessor`: AI usage and performance metrics

#### Services
- `EventTrackingService`: Event persistence and tracking
- `MetricAggregationService`: Metric computation from events
- `AnalyticsService`: High-level analytics queries and export
- `DashboardService`: Dashboard CRUD with caching
- `BotMetricsService`: Bot-level aggregate metrics
- `ConversationMetricsService`: Conversation-level metrics
- `AiCallMetricsService`: AI-specific metrics and analysis
- `ReportGenerationService`: Report generation in multiple formats

#### Controllers
- `AnalyticsController`: Metrics queries, overview, export
- `DashboardController`: Dashboard management
- `ReportController`: Report generation and delivery

## Data Model

### Analytics Events
```
{
  id: UUID,
  org_id: UUID,
  event_type: CONVERSATION_STARTED | MESSAGE_ADDED | AI_REPLY_REQUESTED | HANDOFF_INITIATED | ...,
  bot_id: UUID,
  conversation_id: UUID,
  session_id: UUID,
  event_data_json: { ... },
  event_timestamp: Instant,
  created_at: Instant
}
```

### Metrics
```
{
  id: UUID,
  org_id: UUID,
  metric_name: "messages_count" | "avg_response_time_ms" | "ai_tokens_consumed" | ...,
  bot_id: UUID,
  value: Double,
  tags_json: { "channel": "web", ... },
  metric_timestamp: Instant,
  created_at: Instant
}
```

### Daily Rollups
```
{
  id: UUID,
  org_id: UUID,
  bot_id: UUID,
  rollup_date: LocalDate,
  conversations_count: Long,
  messages_count: Long,
  ai_calls_count: Long,
  avg_response_time_ms: Double,
  resolution_rate: Double (0-100),
  avg_csat_score: Double,
  total_tokens_consumed: Long,
  total_cost_cents: Long,
  handoffs_count: Long,
  created_at: Instant,
  updated_at: Instant
}
```

## API Endpoints

### Analytics Overview
```
POST /api/v1/analytics/overview
Headers: X-Org-ID: <org-id>
Query: days=30 (default)
Returns: AnalyticsOverviewDto with period and comparison metrics
```

### Metric Queries
```
GET /api/v1/analytics/metrics/{metricName}
Headers: X-Org-ID: <org-id>
Query: bot_id=<optional>, days=30, page=0, size=50
Returns: Paginated metrics
```

### Custom Metric Query
```
POST /api/v1/analytics/query
Headers: X-Org-ID: <org-id>
Body: MetricQueryRequest { metric_names, bot_ids, start_time, end_time, aggregation, period }
Returns: MetricQueryResponse with series and summary
```

### Export Metrics
```
GET /api/v1/analytics/export
Headers: X-Org-ID: <org-id>
Query: metric=<name>, format=csv|json|pdf, days=30
Returns: File download in specified format
```

### Dashboards
```
GET    /api/v1/dashboards              # List all dashboards
GET    /api/v1/dashboards/{id}         # Get specific dashboard
POST   /api/v1/dashboards              # Create dashboard
PUT    /api/v1/dashboards/{id}         # Update dashboard
DELETE /api/v1/dashboards/{id}         # Delete dashboard
GET    /api/v1/dashboards/default/view # Get default dashboard
```

### Reports
```
POST   /api/v1/reports/generate                # Generate report
GET    /api/v1/reports/{id}                    # Get report status
POST   /api/v1/reports/{id}/email              # Email report to recipients
```

## Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/threadly
    username: threadly
    password: dev
  jpa:
    hibernate.ddl-auto: validate
  kafka:
    bootstrap-servers: localhost:9092
  cache:
    type: simple
    cache-names: dashboards,metrics,rollups

management:
  endpoints.web.exposure.include: health,prometheus,metrics
```

### Environment Variables
- `DB_HOST`: Database hostname (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: threadly)
- `DB_USER`: Database user (default: threadly)
- `DB_PASSWORD`: Database password (default: dev)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka brokers (default: localhost:9092)
- `CONSUL_HOST`: Consul hostname (default: localhost)
- `CONSUL_PORT`: Consul port (default: 8500)
- `OTEL_EXPORTER_OTLP_ENDPOINT`: OpenTelemetry endpoint (default: localhost:4317)

## Kafka Topics

### Subscribed Topics
- `conversation-events`: CONVERSATION_STARTED, CONVERSATION_ENDED, MESSAGE_ADDED, HANDOFF_INITIATED
- `runtime-events`: AI_REPLY_REQUESTED, NODE_EXECUTED

### Consumer Group
- `analytics-service`: Manual acknowledgment with retry on error

## Metric Processor Pattern

Metrics are computed using the pluggable `MetricProcessor` pattern:

1. Raw event received from Kafka
2. EventTrackingService stores event
3. MetricAggregationService processes event asynchronously
4. Appropriate MetricProcessor generates metrics based on event type
5. Generated metrics stored in metrics table
6. Daily rollup updated asynchronously

### Custom Processor Example
```java
@Component
@Slf4j
public class CustomMetricProcessor extends MetricProcessor {
    
    @Override
    public List<Metric> processEvent(AnalyticsEvent event) {
        List<Metric> metrics = new ArrayList<>();
        // Process event and generate metrics
        return metrics;
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return "CUSTOM_EVENT".equals(eventType);
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"CUSTOM_EVENT"};
    }
}
```

## Caching Strategy

- Dashboard cache: 5-minute TTL
- Metrics cache: 5-minute TTL
- Cache invalidation on create/update/delete operations
- In-memory cache using ConcurrentMapCacheManager

## Time-Series Queries

Optimized indexes for efficient time-range queries:
- `idx_events_org_timestamp`: Organization + timestamp (DESC)
- `idx_metrics_org_name_timestamp`: Org + metric name + timestamp (DESC)
- `idx_rollup_org_date`: Organization + date (DESC)

## Retention Policies

- **Analytics Events**: 90-day rolling window (auto-delete via Flyway job)
- **Metrics**: 1-year retention
- **Daily Rollups**: 1-year retention (indefinite with archiving)
- **Dashboards**: No automatic deletion (user-managed)

## Error Handling

- Kafka listeners don't acknowledge on error (automatic retry)
- Metrics processing failures logged but don't block event storage
- All endpoints return structured error responses via GlobalExceptionHandler
- Timeouts configured via application.properties

## Performance Considerations

1. **Asynchronous Processing**: Metrics computed in thread pool to avoid blocking event storage
2. **Batch Operations**: Daily rollups computed once per day via scheduled jobs
3. **Index Strategy**: Composite indexes on org_id + timestamp for fast range queries
4. **Cache Invalidation**: Selective cache eviction on dashboard updates
5. **Pagination**: All list endpoints support pagination to limit memory usage

## Security

- Multi-tenancy: `org_id` enforced on all queries via `X-Org-ID` header
- Event data in JSON allows flexible schema evolution
- No sensitive data stored in event_data_json field (encrypted separately if needed)
- Database user has minimal required permissions
- Kafka consumer group configured for this service only

## Monitoring

### Metrics
- HTTP request duration and count: `http.server.requests`
- Database connection pool: Spring Data
- Kafka consumer lag: `kafka.consumer.lag`
- Event processing latency

### Health Checks
- GET `/health`: Service health status
- Includes database connectivity
- Kafka broker connectivity
- Dependencies status

### Distributed Tracing
- OpenTelemetry enabled
- Traces exported to OTLP endpoint
- Request correlation across services

## Building & Running

```bash
# Build
mvn clean package

# Run locally
mvn spring-boot:run

# Docker
docker build -f Dockerfile -t analytics-service:latest .
docker run -p 3007:3007 analytics-service:latest

# With Consul discovery
CONSUL_HOST=consul KAFKA_BOOTSTRAP_SERVERS=kafka:9092 java -jar analytics-service.jar
```

## Development

### Project Structure
```
src/main/java/dev/threadly/analytics/
├── entity/              # JPA entities
├── repository/          # Spring Data repositories
├── service/             # Business logic services
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── kafka/               # Kafka listeners
├── processor/           # Metric processors
├── exception/           # Custom exceptions
└── config/              # Spring configuration
```

### Testing
- Unit tests for services
- Integration tests for repositories
- Controller tests with MockMvc

## Troubleshooting

### Events Not Being Tracked
1. Verify Kafka connectivity: Check `KAFKA_BOOTSTRAP_SERVERS`
2. Verify topics exist: `kafka-topics --list --bootstrap-server localhost:9092`
3. Check consumer group lag: `kafka-consumer-groups --list --bootstrap-server localhost:9092`
4. Review logs: Check for filter exceptions or listener errors

### Metrics Not Being Generated
1. Verify processor implementation
2. Check async task executor: `taskExecutor` bean must be initialized
3. Review processor logs for event processing errors
4. Verify metric name spelling matches expected values

### Dashboard Cache Issues
1. Clear cache: Application restart
2. Check cache configuration: CacheConfig class
3. Verify cache eviction on updates: @CacheEvict annotations

### Performance Issues
1. Check database indexes: Ensure all recommended indexes exist
2. Review query plans: Analyze slow queries in database logs
3. Monitor Kafka consumer lag: May indicate processing bottleneck
4. Check thread pool: Adjust ThreadPoolTaskExecutor parameters if needed

## Future Enhancements

- [ ] Materialized view for pre-aggregated queries
- [ ] Time-series database integration (InfluxDB, TimescaleDB)
- [ ] Streaming aggregations with Apache Flink
- [ ] Anomaly detection on metrics
- [ ] Real-time alerting on SLA violations
- [ ] Distributed caching with Redis
- [ ] WebSocket support for real-time dashboards
- [ ] Machine learning-based forecasting

## Contributing

See [CONTRIBUTING.md](../../CONTRIBUTING.md) for guidelines.

## License

Proprietary - Threadly Platform
