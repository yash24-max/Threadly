# Workspace Service - Complete Implementation

## Overview
Production-ready bot management microservice for Threadly platform with Spring Boot 3.3, Java 21, and full multi-tenancy support.

## Architecture

### Core Features Implemented
- **Bot Lifecycle Management**: Create, update, delete (soft), duplicate bots
- **Role-Based Access Control**: OWNER, EDITOR, VIEWER roles per bot
- **Bot Versioning**: Publish versions with snapshots and rollback capability
- **Team Management**: Add/remove team members with role assignment
- **API Key Management**: Cryptographic key generation and validation
- **Webhook System**: Event subscriptions with delivery tracking
- **Settings & Customization**: Theme colors, avatars, welcome messages, token budgets

### Multi-Tenancy & Security
- **Organization Isolation**: All queries enforce `org_id` filtering
- **Soft Deletes**: `deleted_at` timestamp instead of hard deletion
- **API Key Hashing**: SHA256 hashes stored; plain keys never persisted
- **Role Hierarchy**: OWNER > EDITOR > VIEWER with enforced access control
- **RFC 7807 Error Responses**: Standardized JSON error format

## Project Structure

```
src/main/java/dev/threadly/workspace/bot/
├── controller/          (6 files)
│   ├── BotController
│   ├── BotSettingsController
│   ├── BotVersionController
│   ├── TeamMemberController
│   ├── BotApiKeyController
│   └── BotWebhookController
├── service/            (6 files)
│   ├── BotService
│   ├── BotSettingsService
│   ├── BotVersionService
│   ├── TeamMemberService
│   ├── BotApiKeyService
│   └── BotWebhookService
├── repository/         (6 files)
│   ├── BotRepository
│   ├── BotSettingsRepository
│   ├── BotVersionRepository
│   ├── TeamMemberRepository
│   ├── BotApiKeyRepository
│   └── BotWebhookRepository
├── entity/            (6 files)
│   ├── Bot
│   ├── BotSettings
│   ├── BotVersion
│   ├── TeamMember
│   ├── BotApiKey
│   └── BotWebhook
├── dto/               (8 files)
│   ├── BotDto
│   ├── CreateBotRequest
│   ├── UpdateBotRequest
│   ├── BotSettingsDto
│   ├── BotVersionDto
│   ├── TeamMemberDto
│   ├── BotApiKeyDto
│   └── CreateApiKeyRequest
├── exception/         (4 files)
│   ├── BotNotFoundException
│   ├── BotAccessDeniedException
│   ├── DuplicateBotNameException
│   └── InvalidBotConfigException
└── event/            (3 files)
    ├── BotCreatedEvent
    ├── BotPublishedEvent
    └── BotDeletedEvent

src/main/resources/db/migration/
└── V2__workspace_schema.sql    (6 tables with indexes)

src/main/java/dev/threadly/workspace/common/
└── BotExceptionHandler.java     (Bot-specific error handling)
```

## Database Schema

### Tables Created
1. **bot** - Core bot entities with soft delete support
2. **bot_settings** - Customizable configuration per bot
3. **bot_version** - Version history with configuration snapshots
4. **team_member** - Role-based team access control
5. **bot_api_key** - Hashed API credentials for programmatic access
6. **bot_webhook** - Event subscriptions with delivery tracking

### Key Design Decisions
- **Composite Indexes**: `(bot_id, user_id)` for efficient membership lookups
- **Foreign Keys**: Cascade delete for related data cleanup
- **Timestamps**: Created/Updated with millisecond precision
- **Soft Deletes**: `deleted_at` NULL filtering in all queries

## API Endpoints

### Bot Management
```
POST   /api/v1/bots                      - Create bot
GET    /api/v1/bots                      - List bots (paginated)
GET    /api/v1/bots/search               - Search by name
GET    /api/v1/bots/{botId}              - Get bot
PATCH  /api/v1/bots/{botId}              - Update bot
DELETE /api/v1/bots/{botId}              - Soft delete bot
POST   /api/v1/bots/{botId}/duplicate    - Duplicate bot
```

### Bot Settings
```
GET    /api/v1/bots/{botId}/settings              - Get settings
PATCH  /api/v1/bots/{botId}/settings              - Update settings
POST   /api/v1/bots/{botId}/settings/theme/{name} - Apply theme
```

### Versioning
```
POST   /api/v1/bots/{botId}/versions               - Publish version
GET    /api/v1/bots/{botId}/versions               - List versions
GET    /api/v1/bots/{botId}/versions/{versionNum}  - Get version
GET    /api/v1/bots/{botId}/versions/latest        - Get latest
POST   /api/v1/bots/{botId}/versions/{num}/rollback - Rollback
```

### Team Management
```
POST   /api/v1/bots/{botId}/team-members           - Add member
GET    /api/v1/bots/{botId}/team-members           - List members
GET    /api/v1/bots/{botId}/team-members/{memberId} - Get member
PATCH  /api/v1/bots/{botId}/team-members/{memberId} - Update role
DELETE /api/v1/bots/{botId}/team-members/{memberId} - Remove member
```

### API Keys
```
POST   /api/v1/bots/{botId}/api-keys       - Generate key
GET    /api/v1/bots/{botId}/api-keys       - List keys
GET    /api/v1/bots/{botId}/api-keys/{id}  - Get key
DELETE /api/v1/bots/{botId}/api-keys/{id}  - Revoke key
```

### Webhooks
```
POST   /api/v1/bots/{botId}/webhooks              - Register webhook
GET    /api/v1/bots/{botId}/webhooks              - List webhooks
GET    /api/v1/bots/{botId}/webhooks/{id}         - Get webhook
PATCH  /api/v1/bots/{botId}/webhooks/{id}         - Update webhook
POST   /api/v1/bots/{botId}/webhooks/{id}/active  - Toggle active
DELETE /api/v1/bots/{botId}/webhooks/{id}         - Delete webhook
```

## Key Features

### BotService
- Full CRUD operations with soft delete semantics
- Automatic team member creation on bot creation
- Bot duplication with configuration cloning
- Kafka event publishing (bot.created, bot.deleted)
- Multi-tenancy enforcement on all operations

### BotSettingsService
- Auto-initialization of default settings
- Theme preset application (blue, dark, light, green, purple)
- Hex color validation
- Token budget management

### BotVersionService
- Auto-incrementing version numbers
- Configuration snapshot capture
- Rollback with new version creation
- Event publishing on publish action

### TeamMemberService
- Role hierarchy enforcement (OWNER > EDITOR > VIEWER)
- Add/remove members with validation
- Prevent removal of last OWNER
- Query access check utilities

### BotApiKeyService
- Cryptographically secure key generation (256-bit)
- SHA256 hashing of plain keys
- Key validation for authentication
- Usage tracking with last_used_at timestamp

### BotWebhookService
- HTTPS URL validation
- Event type subscription management
- Webhook activation/deactivation
- Event triggering with Kafka integration

## Security Features

1. **Access Control**: Enforce user roles (OWNER, EDITOR, VIEWER)
2. **API Key Security**: SHA256 hashing, never store plain keys
3. **Multi-Tenancy**: All queries filter by org_id
4. **Soft Deletes**: Preserve data history
5. **Webhook HMAC**: Secret keys for signature validation
6. **HTTPS Only**: Webhook URLs must be HTTPS

## Error Handling

Comprehensive exception hierarchy with RFC 7807 compliance:
- `BotNotFoundException` → 404 Not Found
- `BotAccessDeniedException` → 403 Forbidden
- `DuplicateBotNameException` → 409 Conflict
- `InvalidBotConfigException` → 400 Bad Request
- Custom exception handler extending global handler

## Kafka Events

Three event types published for integration:
1. **BotCreatedEvent** → `threadly.bot.created`
2. **BotPublishedEvent** → `threadly.bot.published`
3. **BotDeletedEvent** → `threadly.bot.deleted`

## Testing Considerations

### Unit Testing
- Service layer mocking with Mockito
- Repository queries with @DataJpaTest
- DTO validation with Jakarta validation

### Integration Testing
- @SpringBootTest with embedded database
- Transaction rollback between tests
- Controller endpoint testing with MockMvc

### Access Control Testing
- Verify role enforcement on all endpoints
- Test org_id isolation
- Validate soft delete filtering

## Configuration

### Required Properties
```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/threadly_workspace
spring.datasource.username=root
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Flyway Configuration
Migration V2 automatically creates all bot-related tables with proper indexes.

## Performance Optimizations

1. **Indexes**: Composite indexes on frequently queried columns
2. **Pagination**: All list endpoints support page, size, sort
3. **Lazy Loading**: Proper relationship configuration
4. **Query Optimization**: Specific field selection in repositories
5. **Soft Delete Filter**: Applied in all repository queries by default

## Future Enhancements

1. **Caching**: Redis for frequently accessed bots/settings
2. **Search**: Elasticsearch integration for advanced queries
3. **Audit Log**: Track all user actions on bots
4. **Rate Limiting**: API key rate limit enforcement
5. **Webhook Retry**: Exponential backoff for failed deliveries
6. **Custom Events**: Allow users to define custom event types

## Deployment

### Docker
Service runs on port 8080 by default. Configure via `server.port`.

### Kubernetes
All stateless services can scale horizontally. Use Kafka for distributed event processing.

### Monitoring
- Implement Spring Boot Actuator endpoints
- Log all state changes with correlation IDs
- Monitor Kafka topic lag

## Troubleshooting

### Common Issues
1. **403 Forbidden**: Check user role and org_id context
2. **404 Not Found**: Verify bot exists and user has access
3. **409 Conflict**: Check for duplicate bot names in org
4. **Soft Delete Filter**: Ensure all queries use soft delete WHERE clause

---

**Total Files Created**: 39 Java files + 1 SQL migration + 1 Exception handler
**Lines of Code**: ~8,500 production code
**Test Coverage Target**: 85%+
