# REST API Endpoints

**Last Updated**: 2025-05-24  
**API Version**: 1.0  
**Authentication**: JWT Bearer Token  

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Response Format](#response-format)
4. [Error Handling](#error-handling)
5. [Service Endpoints](#service-endpoints)
   - [Identity Service (3001)](#identity-service-3001)
   - [Workspace Service (3002)](#workspace-service-3002)
   - [Flow Service (3003)](#flow-service-3003)
   - [Runtime Service (3004)](#runtime-service-3004)
   - [Conversation Service (3005)](#conversation-service-3005)
   - [Knowledge Service (3006)](#knowledge-service-3006)
   - [Analytics Service (3007)](#analytics-service-3007)
   - [Billing Service (3008)](#billing-service-3008)
   - [Integration Service (3009)](#integration-service-3009)
6. [Rate Limiting](#rate-limiting)
7. [Pagination](#pagination)
8. [Filtering & Sorting](#filtering--sorting)

---

## Overview

Threadly exposes a unified REST API across 9 microservices. The API Gateway (Nginx, port 8080) routes requests to the appropriate service.

**Base URL (Local)**:
```
http://localhost:8080/api/v1
```

**Base URL (Production)**:
```
https://api.threadly.io/api/v1
```

**Total Endpoints**: 45+

---

## Authentication

All endpoints (except `/auth/login` and `/health`) require authentication.

### JWT Bearer Token

```bash
# 1. Get token (login)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@threadly.dev",
    "password": "SecurePassword123!"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "expiresIn": 86400
# }

# 2. Use token in header
curl http://localhost:8080/api/v1/bots \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### API Keys (Alternative)

For server-to-server integration:

```bash
# 1. Create API key
curl -X POST http://localhost:8080/api/v1/api-keys \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"name":"Integration Bot"}'

# Response:
# {
#   "id": "key_abc123",
#   "secret": "sk_live_xyz789"
# }

# 2. Use API key
curl http://localhost:8080/api/v1/bots \
  -H "X-API-Key: sk_live_xyz789"
```

---

## Response Format

All responses are JSON with consistent structure:

**Success (2xx)**:
```json
{
  "data": {
    "id": "bot_123",
    "name": "Customer Support Bot",
    "status": "active"
  },
  "meta": {
    "timestamp": "2025-05-24T10:30:00Z",
    "version": "1.0"
  }
}
```

**Error (4xx/5xx)**:
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Bot not found",
    "details": {
      "field": "bot_id",
      "value": "invalid_id"
    }
  },
  "meta": {
    "timestamp": "2025-05-24T10:30:00Z",
    "version": "1.0"
  }
}
```

---

## Error Handling

Common HTTP status codes:

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success | Bot retrieved |
| 201 | Created | New bot created |
| 400 | Bad Request | Missing required field |
| 401 | Unauthorized | Invalid token |
| 403 | Forbidden | Don't have permission |
| 404 | Not Found | Bot doesn't exist |
| 429 | Rate Limited | Too many requests |
| 500 | Server Error | Unexpected error |
| 503 | Unavailable | Service down |

---

## Service Endpoints

### Identity Service (port 3001)

**Base URL**: `/api/v1/auth`

Handles user authentication, organization management, and API keys.

#### Authentication

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/auth/login` | Login with username/password |
| POST | `/auth/logout` | Logout (invalidate token) |
| POST | `/auth/refresh` | Refresh JWT token |
| POST | `/auth/register` | Create new user account |
| POST | `/auth/forgot-password` | Send password reset email |
| POST | `/auth/reset-password` | Reset password with token |

**Example: Login**:
```bash
curl -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@threadly.dev",
    "password": "SecurePassword123!"
  }'
```

#### User Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/me` | Get current user info |
| GET | `/users/{userId}` | Get user by ID |
| PATCH | `/me` | Update current user |
| PATCH | `/users/{userId}` | Update user (admin only) |
| DELETE | `/users/{userId}` | Delete user (admin only) |

#### Organization Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/organizations` | List user's organizations |
| GET | `/organizations/{orgId}` | Get organization details |
| POST | `/organizations` | Create new organization |
| PATCH | `/organizations/{orgId}` | Update organization |
| DELETE | `/organizations/{orgId}` | Delete organization |
| POST | `/organizations/{orgId}/members` | Invite member |
| DELETE | `/organizations/{orgId}/members/{userId}` | Remove member |

#### API Keys

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api-keys` | List API keys |
| POST | `/api-keys` | Create new API key |
| DELETE | `/api-keys/{keyId}` | Delete API key |
| POST | `/api-keys/{keyId}/rotate` | Rotate API key |

---

### Workspace Service (port 3002)

**Base URL**: `/api/v1/workspace`

Manages bots, teams, settings, and workspace configuration.

#### Bot Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/bots` | List bots in workspace |
| GET | `/bots/{botId}` | Get bot details |
| POST | `/bots` | Create new bot |
| PATCH | `/bots/{botId}` | Update bot settings |
| DELETE | `/bots/{botId}` | Delete bot |
| POST | `/bots/{botId}/publish` | Publish bot (make live) |
| POST | `/bots/{botId}/archive` | Archive bot |

**Example: Create Bot**:
```bash
curl -X POST http://localhost:3002/api/v1/workspace/bots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Customer Support Bot",
    "description": "Answers customer questions",
    "initialGreeting": "Hello! How can I help you today?",
    "knowledgeBaseId": "kb_123"
  }'
```

#### Team Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/teams` | List teams |
| GET | `/teams/{teamId}` | Get team details |
| POST | `/teams` | Create team |
| PATCH | `/teams/{teamId}` | Update team |
| DELETE | `/teams/{teamId}` | Delete team |
| POST | `/teams/{teamId}/members` | Add team member |
| DELETE | `/teams/{teamId}/members/{userId}` | Remove team member |

#### Settings

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/settings` | Get workspace settings |
| PATCH | `/settings` | Update workspace settings |
| GET | `/settings/branding` | Get branding config |
| PATCH | `/settings/branding` | Update branding |

---

### Flow Service (port 3003)

**Base URL**: `/api/v1/flows`

Manages chatbot conversation flows and node configuration.

#### Flow CRUD

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/flows` | List flows |
| GET | `/flows/{flowId}` | Get flow details |
| POST | `/flows` | Create new flow |
| PATCH | `/flows/{flowId}` | Update flow |
| DELETE | `/flows/{flowId}` | Delete flow |
| GET | `/flows/{flowId}/versions` | List flow versions |
| GET | `/flows/{flowId}/versions/{versionId}` | Get specific version |

**Example: Create Flow**:
```bash
curl -X POST http://localhost:3003/api/v1/flows \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "botId": "bot_123",
    "name": "Support Flow",
    "nodes": [
      {
        "id": "start",
        "type": "message",
        "data": {
          "text": "What can I help you with?"
        }
      }
    ]
  }'
```

#### Node Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/flows/{flowId}/nodes` | Add node |
| PATCH | `/flows/{flowId}/nodes/{nodeId}` | Update node |
| DELETE | `/flows/{flowId}/nodes/{nodeId}` | Delete node |
| POST | `/flows/{flowId}/edges` | Add connection |
| DELETE | `/flows/{flowId}/edges/{edgeId}` | Remove connection |

#### Publishing

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/flows/{flowId}/publish` | Publish flow version |
| GET | `/flows/{flowId}/published` | Get published version |
| POST | `/flows/{flowId}/unpublish` | Unpublish flow |

---

### Runtime Service (port 3004)

**Base URL**: `/api/v1/runtime`

Executes flows and manages conversation sessions.

#### Session Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/sessions` | List sessions |
| GET | `/sessions/{sessionId}` | Get session details |
| POST | `/sessions` | Create new session |
| PATCH | `/sessions/{sessionId}` | Update session |
| DELETE | `/sessions/{sessionId}` | Delete session |

**Example: Create Session**:
```bash
curl -X POST http://localhost:3004/api/v1/runtime/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "botId": "bot_123",
    "flowId": "flow_456",
    "visitorId": "visitor_789",
    "context": {
      "language": "en",
      "timezone": "UTC"
    }
  }'
```

#### Execution

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/sessions/{sessionId}/execute` | Execute next node |
| POST | `/sessions/{sessionId}/input` | Send user input |
| POST | `/sessions/{sessionId}/reset` | Reset session |
| POST | `/sessions/{sessionId}/pause` | Pause session |
| POST | `/sessions/{sessionId}/resume` | Resume session |

---

### Conversation Service (port 3005)

**Base URL**: `/api/v1/conversations`

Stores conversation history and lead information.

#### Conversation Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/conversations` | List conversations |
| GET | `/conversations/{conversationId}` | Get conversation |
| POST | `/conversations` | Create conversation |
| PATCH | `/conversations/{conversationId}` | Update conversation |
| DELETE | `/conversations/{conversationId}` | Delete conversation |

#### Messages

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/conversations/{conversationId}/messages` | Get messages |
| POST | `/conversations/{conversationId}/messages` | Add message |
| DELETE | `/conversations/{conversationId}/messages/{messageId}` | Delete message |

#### Leads & Handoff

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/conversations/{conversationId}/handoff` | Hand off to human |
| GET | `/leads` | List captured leads |
| POST | `/conversations/{conversationId}/capture-lead` | Capture visitor info |

---

### Knowledge Service (port 3006)

**Base URL**: `/api/v1/knowledge`

Manages knowledge bases, documents, and RAG indexing.

#### Knowledge Base Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/knowledge-bases` | List knowledge bases |
| GET | `/knowledge-bases/{kbId}` | Get KB details |
| POST | `/knowledge-bases` | Create knowledge base |
| PATCH | `/knowledge-bases/{kbId}` | Update KB |
| DELETE | `/knowledge-bases/{kbId}` | Delete KB |

#### Document Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/knowledge-bases/{kbId}/documents` | List documents |
| POST | `/knowledge-bases/{kbId}/documents` | Upload document |
| PATCH | `/knowledge-bases/{kbId}/documents/{docId}` | Update document |
| DELETE | `/knowledge-bases/{kbId}/documents/{docId}` | Delete document |
| POST | `/knowledge-bases/{kbId}/documents/batch` | Batch upload |

#### RAG & Search

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/knowledge-bases/{kbId}/search` | Search documents |
| POST | `/knowledge-bases/{kbId}/embed` | Generate embeddings |
| POST | `/knowledge-bases/{kbId}/reindex` | Reindex all documents |

---

### Analytics Service (port 3007)

**Base URL**: `/api/v1/analytics`

Tracks metrics, dashboards, and conversation analytics.

#### Metrics

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/metrics` | Get overall metrics |
| GET | `/metrics/{botId}` | Get bot-specific metrics |
| GET | `/metrics/{botId}/daily` | Daily metrics breakdown |
| GET | `/metrics/{botId}/conversations` | Conversation analytics |

#### Dashboards

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/dashboards` | List custom dashboards |
| POST | `/dashboards` | Create dashboard |
| PATCH | `/dashboards/{dashboardId}` | Update dashboard |
| DELETE | `/dashboards/{dashboardId}` | Delete dashboard |

#### Exports

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/export/csv` | Export data as CSV |
| POST | `/export/pdf` | Export report as PDF |
| POST | `/export/json` | Export as JSON |

---

### Billing Service (port 3008)

**Base URL**: `/api/v1/billing`

Manages subscriptions, payments, and usage metering.

#### Subscriptions

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/subscriptions` | List subscriptions |
| GET | `/subscriptions/{subId}` | Get subscription |
| POST | `/subscriptions` | Create subscription |
| PATCH | `/subscriptions/{subId}` | Update subscription |
| DELETE | `/subscriptions/{subId}` | Cancel subscription |

#### Invoices

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/invoices` | List invoices |
| GET | `/invoices/{invoiceId}` | Get invoice |
| POST | `/invoices/{invoiceId}/download` | Download PDF |

#### Usage & Metering

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/usage` | Get current usage |
| POST | `/usage/report` | Generate usage report |

---

### Integration Service (port 3009)

**Base URL**: `/api/v1/integrations`

Manages third-party integrations and connectors.

#### Integration Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/integrations` | List installed integrations |
| GET | `/integrations/available` | List available integrations |
| POST | `/integrations/{integrationId}/install` | Install integration |
| DELETE | `/integrations/{integrationId}` | Uninstall integration |
| PATCH | `/integrations/{integrationId}/config` | Update configuration |

#### OAuth Flow

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/integrations/{integrationId}/oauth/authorize` | Get auth URL |
| POST | `/integrations/{integrationId}/oauth/callback` | Handle callback |

#### Actions

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/integrations/{integrationId}/actions/{action}` | Execute action |
| GET | `/integrations/{integrationId}/webhook` | Get webhook URL |

---

## Rate Limiting

Threadly implements rate limiting on all endpoints:

| Plan | Requests/sec | Burst |
|------|-------------|-------|
| Free | 10 | 20 |
| Pro | 100 | 200 |
| Enterprise | Unlimited | Unlimited |

**Rate Limit Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 87
X-RateLimit-Reset: 1621876800
```

**When rate limited (HTTP 429)**:
```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests",
    "retryAfter": 60
  }
}
```

---

## Pagination

List endpoints support pagination:

```bash
curl "http://localhost:8080/api/v1/bots?page=1&limit=20&sort=-created_at"
```

**Parameters**:
- `page` — Page number (1-indexed, default: 1)
- `limit` — Results per page (default: 20, max: 100)
- `sort` — Sort field (prefix with `-` for descending)

**Response**:
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 156,
    "pages": 8
  }
}
```

---

## Filtering & Sorting

List endpoints support filtering and sorting:

**Filtering**:
```bash
# Filter by status
curl "http://localhost:3002/api/v1/bots?status=active&org_id=org_123"

# Multiple values
curl "http://localhost:3002/api/v1/bots?status=active,archived"

# Range filters
curl "http://localhost:3007/api/v1/analytics?created_after=2025-01-01&created_before=2025-05-24"
```

**Sorting**:
```bash
# Ascending
curl "http://localhost:3002/api/v1/bots?sort=name"

# Descending
curl "http://localhost:3002/api/v1/bots?sort=-created_at"

# Multiple fields
curl "http://localhost:3002/api/v1/bots?sort=-status,name"
```

---

## Webhooks

Services emit events to configured webhooks:

**Webhook Events**:
```json
{
  "event": "bot.created",
  "data": {
    "id": "bot_123",
    "name": "New Bot"
  },
  "timestamp": "2025-05-24T10:30:00Z"
}
```

**Configure Webhooks**:
```bash
curl -X POST http://localhost:8080/api/v1/webhooks \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "url": "https://your-app.com/webhooks",
    "events": ["bot.created", "bot.deleted"],
    "secret": "whsec_abc123"
  }'
```

---

## SDKs & Client Libraries

- **JavaScript/TypeScript**: `@threadly/sdk-js`
- **Python**: `threadly-sdk`
- **Go**: `github.com/threadly/sdk-go`
- **Java**: `dev.threadly:sdk-java`

See [docs/reference/CONTRIBUTING.md](../reference/CONTRIBUTING.md) for SDK examples.

---

## Related Documentation

- [Kafka Topics](./kafka-topics.md) — Event streaming topics
- [OpenAPI Spec](./openapi.md) — Full OpenAPI contract
- [Error Handling](./rest-endpoints.md#error-handling) — HTTP status codes reference
- [FAQ](../reference/FAQ.md) — Frequently asked questions about API usage

