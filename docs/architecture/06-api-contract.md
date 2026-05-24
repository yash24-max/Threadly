# API Contract

## Base URL
- Core REST: `https://api.threadly.dev/v1`
- Core OpenAPI: `/v3/api-docs` (dev: `http://localhost:8080/v3/api-docs`)

## Auth
All `/v1/**` endpoints require `Authorization: Bearer <access_token>`.  
Widget endpoints use visitor JWT issued by `/v1/widget/token`.

---

## Auth endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/v1/auth/signup` | Create org + admin user |
| POST | `/v1/auth/login` | Returns access + refresh tokens |
| POST | `/v1/auth/refresh` | Rotate refresh token |
| POST | `/v1/auth/logout` | Revoke refresh token |
| GET | `/v1/auth/me` | Current user + org |

## Bots

| Method | Path | Description |
|---|---|---|
| GET | `/v1/bots` | List bots for org |
| POST | `/v1/bots` | Create bot |
| GET | `/v1/bots/{id}` | Get bot |
| PATCH | `/v1/bots/{id}` | Update bot name / theme |
| DELETE | `/v1/bots/{id}` | Delete bot |
| GET | `/v1/bots/{id}/embed` | Get embed snippet + config |

## Flows

| Method | Path | Description |
|---|---|---|
| GET | `/v1/bots/{botId}/flow` | Get current draft flow |
| PUT | `/v1/bots/{botId}/flow` | Save draft flow |
| POST | `/v1/bots/{botId}/flow/publish` | Publish draft → creates flow version |
| GET | `/v1/bots/{botId}/flow/versions` | List published versions |
| POST | `/v1/bots/{botId}/flow/versions/{n}/rollback` | Rollback to version N |

## Knowledge Base

| Method | Path | Description |
|---|---|---|
| GET | `/v1/bots/{botId}/kb` | List KB documents |
| POST | `/v1/bots/{botId}/kb` | Upload document (multipart) |
| DELETE | `/v1/bots/{botId}/kb/{docId}` | Delete document |

## Conversations

| Method | Path | Description |
|---|---|---|
| GET | `/v1/conversations` | List conversations (filter: botId, status, date) |
| GET | `/v1/conversations/{id}` | Get conversation with messages |
| PATCH | `/v1/conversations/{id}` | Update status (close, reopen) |
| POST | `/v1/conversations/{id}/handoff` | Trigger human handoff |
| POST | `/v1/conversations/{id}/takeover` | Agent takes over |
| POST | `/v1/conversations/{id}/resume-ai` | Return to bot |
| POST | `/v1/conversations/{id}/messages` | Agent sends message |

## Analytics

| Method | Path | Description |
|---|---|---|
| GET | `/v1/analytics/overview` | Summary cards for dashboard |
| GET | `/v1/analytics/conversations` | Daily conversation counts |
| GET | `/v1/analytics/costs` | LLM cost over time |

## Realtime — Centrifugo token

| Method | Path | Description |
|---|---|---|
| POST | `/v1/realtime/token` | Issue Centrifugo JWT for dashboard user |
| POST | `/v1/widget/token` | Issue visitor JWT for widget (public endpoint) |

## Centrifugo proxy hooks (internal — called by Centrifugo)

| Method | Path | Description |
|---|---|---|
| POST | `/v1/proxy/connect` | Validate JWT on WS connect |
| POST | `/v1/proxy/subscribe` | Authorize channel subscription |
| POST | `/v1/proxy/publish` | Handle visitor message |
| POST | `/v1/proxy/rpc` | Handle RPC calls from widget |

## Centrifugo channels

| Channel | Purpose |
|---|---|
| `chat:{botId}:{visitorId}` | Bidirectional chat for one visitor session |
| `dashboard:{orgId}` | Live updates (new conversations, counts) for dashboard |
| `agent:{agentId}` | Agent-specific notifications (new assignment) |

## Error format (RFC 7807)
```json
{
  "type": "https://threadly.dev/errors/not-found",
  "title": "Bot not found",
  "status": 404,
  "detail": "No bot with id 'abc' exists in your organization.",
  "traceId": "abc123"
}
```
