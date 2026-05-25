# Threadly API Examples

**Version:** 1.0  
**Base URL:** https://api.threadly.dev  
**Auth:** Bearer token in Authorization header

---

## Authentication

### Sign Up

```bash
curl -X POST https://api.threadly.dev/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "orgName": "My Company"
  }'

# Response
{
  "success": true,
  "userId": "user-123",
  "orgId": "org-456",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### Login

```bash
curl -X POST https://api.threadly.dev/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

---

## Bot Management

### Create Bot

```bash
curl -X POST https://api.threadly.dev/bots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Customer Support Bot",
    "description": "Handles customer inquiries",
    "templateId": "customer-support" # optional
  }'

# Response
{
  "id": "bot-789",
  "name": "Customer Support Bot",
  "status": "draft",
  "createdAt": "2026-05-25T10:00:00Z"
}
```

### List Bots

```bash
curl -X GET "https://api.threadly.dev/bots?page=1&limit=10" \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "bots": [
    {
      "id": "bot-789",
      "name": "Customer Support Bot",
      "status": "published",
      "conversations": 245,
      "createdAt": "2026-05-25T10:00:00Z"
    }
  ],
  "total": 5,
  "page": 1
}
```

### Update Bot

```bash
curl -X PATCH https://api.threadly.dev/bots/bot-789 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Bot Name",
    "description": "Updated description"
  }'
```

### Delete Bot

```bash
curl -X DELETE https://api.threadly.dev/bots/bot-789 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Flows

### Get Flow

```bash
curl -X GET https://api.threadly.dev/bots/bot-789/flows/latest \
  -H "Authorization: Bearer $TOKEN"

# Response - Full flow definition
{
  "id": "flow-123",
  "version": 3,
  "nodes": [
    {
      "id": "node-1",
      "type": "start",
      "position": { "x": 0, "y": 0 }
    },
    {
      "id": "node-2",
      "type": "message",
      "content": "How can I help?",
      "position": { "x": 100, "y": 100 }
    },
    {
      "id": "node-3",
      "type": "ai_reply",
      "prompt": "You are a helpful support agent",
      "useKb": true,
      "position": { "x": 200, "y": 100 }
    }
  ],
  "edges": [
    { "from": "node-1", "to": "node-2" },
    { "from": "node-2", "to": "node-3" }
  ]
}
```

### Update Flow

```bash
curl -X PUT https://api.threadly.dev/bots/bot-789/flows \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nodes": [...],
    "edges": [...]
  }'
```

### Publish Flow

```bash
curl -X POST https://api.threadly.dev/bots/bot-789/flows/publish \
  -H "Authorization: Bearer $TOKEN"

# Makes the flow live for the widget
```

---

## Conversations

### List Conversations

```bash
curl -X GET "https://api.threadly.dev/bots/bot-789/conversations?limit=20&offset=0" \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "conversations": [
    {
      "id": "conv-456",
      "visitorId": "visitor-123",
      "visitorEmail": "customer@example.com",
      "messageCount": 5,
      "status": "active",
      "createdAt": "2026-05-25T10:00:00Z"
    }
  ],
  "total": 1250,
  "limit": 20
}
```

### Get Single Conversation

```bash
curl -X GET https://api.threadly.dev/conversations/conv-456 \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "id": "conv-456",
  "messages": [
    {
      "id": "msg-1",
      "sender": "visitor",
      "content": "How do I reset my password?",
      "timestamp": "2026-05-25T10:00:00Z"
    },
    {
      "id": "msg-2",
      "sender": "bot",
      "content": "Go to Settings > Reset Password...",
      "timestamp": "2026-05-25T10:00:05Z"
    }
  ]
}
```

### Export Conversations

```bash
# JSON format
curl -X GET "https://api.threadly.dev/bots/bot-789/conversations/export?format=json" \
  -H "Authorization: Bearer $TOKEN" \
  > conversations.json

# CSV format
curl -X GET "https://api.threadly.dev/bots/bot-789/conversations/export?format=csv" \
  -H "Authorization: Bearer $TOKEN" \
  > conversations.csv
```

---

## Knowledge Base

### Upload Document

```bash
curl -X POST https://api.threadly.dev/kb/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@document.pdf" \
  -F "botId=bot-789"

# Response
{
  "id": "doc-123",
  "filename": "document.pdf",
  "status": "ingesting",
  "chunks": 0
}
```

### Check Upload Status

```bash
curl -X GET https://api.threadly.dev/kb/doc-123 \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "id": "doc-123",
  "status": "ingested",
  "chunks": 24,
  "tokens": 5320,
  "createdAt": "2026-05-25T10:00:00Z"
}
```

### Search Knowledge Base

```bash
curl -X POST https://api.threadly.dev/kb/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "botId": "bot-789",
    "query": "How do I reset my password?",
    "topK": 5
  }'

# Response
{
  "results": [
    {
      "id": "chunk-123",
      "content": "To reset your password: 1. Go to...",
      "score": 0.92,
      "source": "document.pdf"
    }
  ]
}
```

### Delete Document

```bash
curl -X DELETE https://api.threadly.dev/kb/doc-123 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Analytics

### Get Bot Metrics

```bash
curl -X GET "https://api.threadly.dev/bots/bot-789/metrics?from=2026-05-01&to=2026-05-31" \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "totalConversations": 1250,
  "activeConversations": 45,
  "avgConversationLength": 3.2,
  "avgResponseTime": 1200,
  "uniqueVisitors": 890,
  "fallbackRate": 0.05
}
```

### Get Analytics by Date

```bash
curl -X GET "https://api.threadly.dev/bots/bot-789/analytics?groupBy=day&limit=30" \
  -H "Authorization: Bearer $TOKEN"

# Response
[
  {
    "date": "2026-05-25",
    "conversations": 50,
    "messages": 180,
    "uniqueVisitors": 40,
    "avgResponseTime": 1100
  }
]
```

---

## Admin & Organization

### Create Team Member

```bash
curl -X POST https://api.threadly.dev/org/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teammate@example.com",
    "role": "editor"
  }'

# Roles: admin, editor, viewer
```

### Get Organization Settings

```bash
curl -X GET https://api.threadly.dev/org/settings \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "orgId": "org-456",
  "name": "My Company",
  "plan": "business",
  "members": 3,
  "createdAt": "2026-01-15T00:00:00Z"
}
```

---

## Node Types & Catalog

### Get Available Node Types

```bash
curl -X GET https://api.threadly.dev/v1/catalogs/node-types \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "nodeTypes": [
    {
      "id": "start",
      "label": "Start",
      "category": "control",
      "inputs": [],
      "outputs": [{ "id": "out", "label": "Next" }]
    },
    {
      "id": "message",
      "label": "Message",
      "category": "action",
      "inputs": [{ "id": "in", "label": "Input" }],
      "outputs": [{ "id": "out", "label": "Next" }]
    },
    // ... 11 more node types
  ]
}
```

### Get Templates

```bash
curl -X GET https://api.threadly.dev/v1/catalogs/templates \
  -H "Authorization: Bearer $TOKEN"

# Response
{
  "templates": [
    {
      "id": "customer-support",
      "name": "Customer Support Bot",
      "description": "Handle customer inquiries",
      "flow": { /* full flow definition */ }
    },
    // ... 30+ templates
  ]
}
```

---

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Email is required",
    "details": {
      "field": "email"
    }
  }
}
```

### Common Error Codes

| Code | Status | Meaning |
|------|--------|---------|
| UNAUTHORIZED | 401 | Invalid/missing token |
| FORBIDDEN | 403 | No permission |
| NOT_FOUND | 404 | Resource not found |
| INVALID_REQUEST | 400 | Bad request |
| CONFLICT | 409 | Resource already exists |
| RATE_LIMIT | 429 | Too many requests |
| SERVER_ERROR | 500 | Internal error |

---

## TypeScript Examples

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://api.threadly.dev',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Create bot
const bot = await api.post('/bots', {
  name: 'My Bot',
  templateId: 'customer-support'
});

// List conversations
const conversations = await api.get(`/bots/${bot.id}/conversations`);

// Upload KB
const formData = new FormData();
formData.append('file', file);
formData.append('botId', bot.id);
const upload = await api.post('/kb/upload', formData);

// Search KB
const results = await api.post('/kb/search', {
  botId: bot.id,
  query: 'reset password'
});
```

---

## Python Examples

```python
import requests
import json

API_BASE = 'https://api.threadly.dev'
headers = {'Authorization': f'Bearer {token}'}

# Create bot
response = requests.post(
  f'{API_BASE}/bots',
  headers=headers,
  json={
    'name': 'My Bot',
    'templateId': 'customer-support'
  }
)
bot = response.json()

# List conversations
conversations = requests.get(
  f'{API_BASE}/bots/{bot["id"]}/conversations',
  headers=headers
).json()

# Upload KB document
with open('document.pdf', 'rb') as f:
  files = {'file': f}
  data = {'botId': bot['id']}
  upload = requests.post(
    f'{API_BASE}/kb/upload',
    headers=headers,
    files=files,
    data=data
  ).json()

# Search KB
search = requests.post(
  f'{API_BASE}/kb/search',
  headers=headers,
  json={
    'botId': bot['id'],
    'query': 'reset password'
  }
).json()
```

---

## Rate Limiting

All endpoints are rate limited:
- **Public endpoints:** 100 requests/minute per IP
- **Authenticated endpoints:** 1000 requests/minute per org
- **LLM endpoints:** 100 calls/minute per org

Check response headers:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1653475200
```

If rate limited, wait until X-RateLimit-Reset timestamp.

---

## Pagination

All list endpoints support pagination:

```bash
curl "https://api.threadly.dev/conversations?limit=50&offset=100"
```

Response includes:
- `limit`: Items per page
- `offset`: Starting position
- `total`: Total items
- `next`: URL for next page (if more results)

---

**API Version:** 1.0  
**Last Updated:** May 25, 2026
