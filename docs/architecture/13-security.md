# Security

## Threat model

| Threat | Mitigation |
|---|---|
| Cross-tenant data access | Hibernate `@Filter org_id` on all tenant tables + Spring interceptor check |
| JWT token theft | Short-lived access tokens (15 min) + rotating refresh tokens + `revoked` flag |
| Widget abuse (unauthorized bot usage) | Per-origin allow-list enforced in Centrifugo proxy `/proxy/connect` |
| LLM prompt injection via KB | Sanitize user inputs before injecting into prompts; KB passages are in a separate XML block |
| Excessive AI token spend | Per-org daily token budget in Redis; hard-stop returns 402 |
| SSRF via API Call node | Block private IP ranges (10.x, 172.16.x, 192.168.x, 127.x, 169.254.x) in API Call node executor |
| Secret exfiltration | Secrets only in env vars / Doppler; never logged; `application.yml` has no secrets |
| SQL injection | JPA/jOOQ parameterized queries only; no raw string concatenation |
| File upload abuse | Allowlist: PDF, TXT, HTML, DOCX, MD only; max 10 MB; ClamAV scan (Phase 1) |

## Auth flow

```
POST /auth/login
→ verify password (BCrypt)
→ issue access_token (RS256 JWT, 15 min, claims: sub=userId, org=orgId, role)
→ issue refresh_token (opaque random 256-bit, stored as SHA-256 hash in DB)

POST /auth/refresh
→ verify refresh_token hash in DB, check not expired/revoked
→ rotate: invalidate old, issue new refresh_token
→ issue new access_token

Widget visitor token (POST /widget/token)
→ public endpoint, rate-limited per IP (5/min)
→ issues visitor JWT (no org claims) used only for Centrifugo channel auth
```

## Centrifugo channel auth

```
Centrifugo proxy → POST /proxy/subscribe
  payload: { channel: "chat:botId:visitorId", user: "visitor_jwt_sub" }

Core verifies:
  1. Visitor JWT is valid
  2. botId exists and is active
  3. Channel name matches JWT claims
  4. Rate limit: 1 active session per visitorId per bot
```

## API key auth (for external integrations)
- Keys stored as `SHA-256(key)` in DB
- Displayed once on creation
- Scoped to org
- Can be revoked

## Secrets management
- Local: `.env` (gitignored)
- Production: Doppler → injected as env vars at runtime
- Key rotation: JWT RS256 key pair rotatable without downtime (Centrifugo uses separate HMAC)

## CORS
- `/v1/**` — allows `app.threadly.dev` + localhost in dev
- `/widget/**` — allows any origin (widget is embedded on customer sites)
- Centrifugo — origin check via proxy `/proxy/connect` hook

## Rate limits (Bucket4j + Redis)

| Endpoint | Limit |
|---|---|
| `/auth/login` | 10 / min per IP |
| `/widget/token` | 5 / min per IP |
| `/proxy/publish` (AI calls) | 60 / min per org |
| All other `/v1/**` | 300 / min per org |
