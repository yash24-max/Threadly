# Conversation Service - Complete Implementation

## Overview
Complete Spring Boot 3.3, Java 21 microservice implementation for the Threadly conversation management system. This service handles conversation storage, message history, lead capture, and conversation analytics.

## Architecture

### Package Structure
```
dev.threadly.conversation/
├── entity/              # JPA entities (5 files)
├── repository/          # JPA repositories (5 files)
├── service/             # Business logic (8 files)
├── controller/          # REST endpoints (3 files)
├── dto/                 # Data transfer objects (10 files)
├── exception/           # Custom exceptions (5 files)
├── kafka/               # Event listeners (4 files)
└── config/              # Configuration (2 files)
```

## Implementation Summary

### 1. Entity Classes (5 Files)
- **Conversation.java**: Main conversation entity with status management
- **Message.java**: Immutable message entities (soft-delete only)
- **Lead.java**: Lead capture from conversations
- **ConversationTag.java**: Key-value tagging for conversations
- **ConversationNote.java**: Internal agent notes

### 2. Repository Interfaces (5 Files)
- **ConversationRepository**: Multi-tenant queries with filtering
- **MessageRepository**: Full-text search and time-range queries
- **LeadRepository**: Duplicate detection and status tracking
- **ConversationTagRepository**: Tag management queries
- **ConversationNoteRepository**: Note retrieval and search

### 3. Service Classes (8 Files)

#### Core Services
- **ConversationService**: Conversation lifecycle (create, update, close)
- **MessageService**: Message management with immutability enforcement
- **LeadService**: Lead capture with duplicate prevention
- **HandoffService**: Human agent assignment and conversation routing

#### Analytics & Export
- **ConversationAnalyticsService**: Sentiment analysis, duration metrics, resolution rates
- **ConversationSearchService**: Full-text search with multi-filter support
- **ConversationExportService**: CSV/JSON bulk export
- **TranscriptService**: Plaintext, HTML, and PDF formatting

### 4. REST Controllers (3 Files)
- **ConversationController**: CRUD and status management endpoints
- **MessageController**: Message operations with pagination
- **LeadController**: Lead management and statistics

### 5. Data Transfer Objects (10 Files)
- Request/Response DTOs for all operations
- Search and analytics result objects
- Comprehensive validation annotations

### 6. Exception Handling (5 Files)
- 4 custom exceptions with context information
- Global exception handler with standardized error responses

### 7. Event-Driven Kafka Integration (4 Files)
- **SessionCreatedEventListener**: Auto-creates conversations
- **MessageAddedEventListener**: Stores messages from runtime
- **SessionEndedEventListener**: Auto-closes conversations
- **HandoffInitiatedEventListener**: Handles agent assignments

### 8. Database Schema (V5__conversation_schema.sql)
```sql
Tables:
- conversations       (main entity with soft-delete)
- messages           (immutable with audit trail)
- leads              (with duplicate detection indexes)
- conversation_tags  (flexible key-value metadata)
- conversation_notes (agent notes with audit)
- message_audit_log  (deletion/restoration tracking)

Indexes: 25+ optimized indexes for multi-tenant queries
Constraints: Multi-tenancy, data integrity, referential integrity
```

## Key Features

### Multi-Tenancy
- All queries filtered by `org_id`
- Isolation enforced at repository layer
- Consistent across all endpoints

### Message Immutability
- Messages cannot be edited after creation
- Soft-delete tracking with audit trail
- Optional hard-delete for admins

### Lead Management
- Automatic duplicate detection by email/phone
- Quality scoring (0-100)
- Flexible custom fields JSON
- Status tracking (NEW, CONTACTED, CONVERTED, LOST, DUPLICATE)

### Conversation Lifecycle
- States: OPEN → CLOSED or OPEN → HANDED_OFF
- Automatic timestamp management
- Message count and token tracking
- Metadata JSON for custom data

### Analytics
- Message count by sender type
- Sentiment analysis (heuristic-based)
- Time-to-first-human-response
- Average response times
- Resolution status tracking
- Token usage metrics

### Export Capabilities
- CSV export (conversations, leads, messages)
- JSON export (structured)
- Plaintext transcripts
- HTML transcripts with styling

### Search & Discovery
- Full-text search on message content
- Multi-field filtering
- Pagination support
- Advanced search with combined filters

## REST API Endpoints

### Conversations
```
POST   /api/v1/conversations                    - Create
GET    /api/v1/conversations                    - List with pagination
GET    /api/v1/conversations/{id}               - Get details
PATCH  /api/v1/conversations/{id}               - Update metadata
POST   /api/v1/conversations/{id}/close         - Close
POST   /api/v1/conversations/{id}/handoff       - Assign to agent
GET    /api/v1/conversations/by-status          - Filter by status
GET    /api/v1/conversations/by-visitor/{id}    - Filter by visitor
GET    /api/v1/conversations/stats/open         - Count open
```

### Messages
```
POST   /api/v1/conversations/{id}/messages      - Add message
GET    /api/v1/conversations/{id}/messages      - List with pagination
GET    /api/v1/conversations/{id}/messages/{id} - Get single
GET    /api/v1/conversations/{id}/messages/search - Search
GET    /api/v1/conversations/{id}/messages/by-sender - Filter by sender
DELETE /api/v1/conversations/{id}/messages/{id} - Soft-delete (admin)
GET    /api/v1/conversations/{id}/messages/stats/count - Count
GET    /api/v1/conversations/{id}/messages/stats/tokens - Total tokens
```

### Leads
```
POST   /api/v1/conversations/{id}/lead/capture - Capture from conversation
GET    /api/v1/conversations/{id}/lead         - Get by conversation
GET    /api/v1/leads/{id}                      - Get by ID
GET    /api/v1/leads                           - List all
GET    /api/v1/leads/by-status                 - Filter by status
GET    /api/v1/leads/search                    - Search by email/name
GET    /api/v1/leads/high-quality              - Filter by score
PATCH  /api/v1/leads/{id}/status               - Update status
PATCH  /api/v1/leads/{id}/quality-score        - Update score
GET    /api/v1/leads/stats                     - Get statistics
```

## Configuration Files

### application.yml
- PostgreSQL datasource configuration
- Flyway migration settings
- Kafka producer/consumer config
- Logging configuration
- Resilience4j circuit breaker
- Consul service discovery
- Metrics export (Prometheus)

### pom.xml Dependencies
- Spring Boot 3.3.5 + Spring Data JPA
- PostgreSQL driver with connection pooling
- Flyway for migrations
- Kafka for event streaming
- Resilience4j for fault tolerance
- OpenTelemetry for observability
- Prometheus metrics export

## Data Model Highlights

### Soft Deletes
- All entities use `deleted_at` timestamp
- Queries automatically filter out deleted records
- Supports data recovery and compliance

### Audit Trail
- `created_at`, `updated_at`, `deleted_at` timestamps
- Message deletion tracked in separate audit table
- Agent activity logged in conversation notes

### Performance Optimizations
- 25+ strategic indexes
- Multi-column indexes for common queries
- Partial indexes for soft-deleted records
- Pagination support on all list endpoints

### Flexibility
- JSON metadata fields for custom data
- Flexible custom fields for leads
- Tag system for extensible categorization

## Transaction Management
- All write operations within @Transactional boundaries
- Read-only optimization for queries
- Proper cascade deletion on entity relationships

## Error Handling
- Custom exceptions with context
- Global exception handler with consistent responses
- Validation error reporting with field details
- Detailed logging at all layers

## Security Considerations
- Org ID validation on every operation
- Admin-only endpoints for message deletion
- No direct ID exposure in responses
- CORS and security headers configured

## Testing Readiness
- Lombok for reduced boilerplate
- Comprehensive DTOs for mocking
- Clear separation of concerns
- Transactional test support

## Database File Location
`/Users/yasva/Kapture/Microservice/Project/Threadly/services/conversation-service/src/main/resources/db/migration/V5__conversation_schema.sql`

## Total Files Created
- 5 Entity classes
- 5 Repository interfaces
- 8 Service classes
- 3 REST Controllers
- 10 DTOs
- 5 Exception classes
- 4 Kafka listeners
- 2 Configuration files
- 1 Database migration
- 1 Application configuration (YAML)

**Total: 44 production files**

## Next Steps for Deployment

1. Ensure PostgreSQL database exists: `threadly_conversations`
2. Configure Kafka brokers in application.yml
3. Set Spring profile to appropriate environment
4. Run Flyway migrations on startup (automatic)
5. Register service with Consul
6. Monitor health endpoint: `/conversation-service/actuator/health`

## Notes

- All code follows Spring Boot 3.3 best practices
- Java 21 features leveraged (records, sealed classes compatible)
- Comprehensive JavaDoc on all public methods
- Production-ready error handling and logging
- Multi-tenant isolation enforced throughout
- Event-driven architecture for scalability
