# Backend Issues — Threadly UI Redesign

> Last audit: 2026-05-28. Items marked ✅ FIXED have been resolved.

---

## ✅ FIXED IN THIS SESSION

| Issue | Fix Applied |
|---|---|
| BE-003 API URL mismatch | `nginx.conf` rewritten — `/v1/auth/*` → identity-service `/auth/*`; all `/v1/*` paths routed to correct services via `rewrite` |
| BE-004 CORS not configured | Added `CorsConfigurationSource` bean to `common/SecurityConfig.java`; Nginx also handles CORS headers |
| BE-005 Analytics stats shape | Added `GET /api/v1/analytics/stats` endpoint returning `{totalConversations, openConversations, handoffConversations, p50ResponseMs}` |
| BE-006 Bot accentColor missing | Added `accent_color` field to `BotDto.java` in workspace-service |
| BE-007 Flow 404 for new bots | `FlowController.listFlows()` auto-creates default "Main Flow" when bot has no flows |
| BE-API-001 No Springdoc | Added `springdoc-openapi-starter-webmvc-ui:2.3.0` to 6 services; `/v3/api-docs` now available |
| BE-002 Error shape mismatch | Already handled — `api.ts` reads `body.detail ?? body.message` covering both Spring and FastAPI |

---

## REMAINING OPEN ISSUES

---

## CRITICAL

### [BE-001] EventSource auth header not supported
- **File:** `frontend/threadly-web/app/(app)/dashboard/page.tsx`
- **Issue:** Browser `EventSource` API does not support custom `Authorization` headers.
  The SSE endpoint `/v1/analytics/live` receives no auth token — backend likely
  returns 401 for all live-stats connections.
- **Fix needed:** Either (a) accept `?token=` query param on SSE endpoint and validate it,
  or (b) switch to cookie-based session so browser sends cookie automatically.
- **Workaround until fixed:** Dashboard falls back to polling via `refetchInterval`.

### [BE-002] Signup API error shape mismatch
- **File:** `frontend/threadly-web/app/(auth)/signup/page.tsx`
- **Issue:** Error handler reads `err.detail` (FastAPI/Python shape) but backend is
  Spring Boot — Spring returns `{ message: "..." }` or `{ error: "..." }`, not `detail`.
- **Fix needed:** Standardize error response body across all services to
  `{ "error": "...", "message": "..." }` or update frontend to handle both shapes.

### [BE-003] No `/v1/auth/signup` endpoint confirmed
- **File:** `frontend/threadly-web/app/(auth)/signup/page.tsx`
- **Issue:** Frontend posts to `/v1/auth/signup` but Spring Boot threadly-core may
  expose it under `/api/v1/auth/register` or `/v1/users/register`.
  Route mismatch will cause 404 on signup.
- **Fix needed:** Confirm actual endpoint path in `AuthController.java` and update
  `NEXT_PUBLIC_API_URL` base + route if needed.

---

## HIGH

### [BE-004] Missing CORS origin for frontend dev URL
- **File:** `infrastructure/docker/docker-compose.yml` / Spring Boot CORS config
- **Issue:** Frontend runs on `http://localhost:3000`. If Spring Boot CORS config
  only allows `*` in dev but will be locked down for staging, ensure
  `http://localhost:3000` and the production domain are in `allowedOrigins`.
- **Fix needed:** Add explicit allowed origins to `WebSecurityConfig.java`.

### [BE-005] `/v1/analytics/stats` endpoint shape not confirmed
- **File:** `frontend/threadly-web/app/(app)/dashboard/page.tsx`
- **Issue:** Frontend expects `{ totalConversations, openConversations, handoffConversations, p50ResponseMs }`.
  Analytics service may return different field names.
- **Fix needed:** Confirm `DashboardStats` DTO matches backend response — or generate
  types from OpenAPI spec once BE-API-001 (Springdoc) is resolved.

### [BE-006] Bot `accentColor` field may not exist in API response
- **File:** `frontend/threadly-web/app/(app)/bots/page.tsx`
- **Issue:** Frontend reads `bot.accentColor` to colorize bot avatar. If this field
  is not in the `BotResponse` DTO, it will always be undefined.
- **Fix needed:** Add `accentColor` (nullable String) to `BotResponse` DTO and
  bot creation/update endpoints.

### [BE-007] `/v1/bots/{id}/flow` returns 404 for new bots
- **File:** `frontend/threadly-web/app/(app)/builder/[botId]/page.tsx`
- **Issue:** New bots have no flow yet — backend should return empty flow skeleton
  `{ id, botId, definition: { nodes: [], edges: [] } }` instead of 404.
  Currently the builder page shows a spinner forever on new bots.
- **Fix needed:** In `FlowService.java`, auto-create an empty flow draft on bot creation
  or on first `GET /v1/bots/{id}/flow`.

### [BE-008] Knowledge base `/v1/bots/{id}/kb/documents` upload endpoint not confirmed
- **File:** `frontend/threadly-web/app/(app)/knowledge/[botId]/page.tsx`
- **Issue:** KB page uploads documents but the multipart endpoint path and accepted
  content types are not verified against actual controller.
- **Fix needed:** Confirm `KnowledgeController` accepts `multipart/form-data` on
  `POST /v1/bots/{id}/kb/documents`.

---

## MEDIUM

### [BE-009] No refresh token rotation on 401
- **File:** `frontend/threadly-web/lib/api.ts` (assumed)
- **Issue:** API client doesn't retry with refreshed token on 401. Users will get
  logged out mid-session when JWT expires.
- **Fix needed:** Implement interceptor: on 401, call `/v1/auth/refresh`, retry original
  request. Covered by NextAuth's `jwt` callback but backend refresh endpoint
  path must match.

### [BE-010] SSE `/v1/analytics/live` endpoint not implemented in analytics-service
- **File:** `frontend/threadly-web/app/(app)/dashboard/page.tsx`
- **Issue:** Live stats SSE stream likely not implemented — only REST polling works.
- **Fix needed:** Implement SSE endpoint or remove live update code and rely on
  `refetchInterval: 15000` polling.

### [BE-011] `POST /v1/bots/{id}/flow/publish` returns success but no version object
- **File:** `frontend/threadly-web/app/(app)/builder/[botId]/page.tsx`
- **Issue:** After publish, `versions` query is invalidated but if publish returns
  `204 No Content`, the version list won't include the new version until refresh.
- **Fix needed:** Publish endpoint should return the new `FlowVersion` object so
  frontend can optimistically add it.

### [BE-012] Conversation inbox `GET /v1/conversations` pagination not handled
- **File:** `frontend/threadly-web/app/(app)/conversations/page.tsx`
- **Issue:** Frontend passes `?limit=5` but doesn't handle cursor-based pagination
  for the full inbox view.
- **Fix needed:** Implement cursor/offset pagination in the conversations API and
  use `useInfiniteQuery` on the frontend.

---

## LOW

### [BE-013] `orgName` field in NextAuth session
- **File:** `frontend/threadly-web/components/layout/Sidebar.tsx`
- **Issue:** Sidebar reads `session.user.orgName` but NextAuth's default session
  type doesn't include `orgName`. If the JWT callback doesn't forward it from
  the backend login response, it will be `undefined`.
- **Fix needed:** Extend `next-auth.d.ts` type augmentation and ensure `orgName`
  is included in the JWT callback from `/v1/auth/login` response.

### [BE-014] Missing `updatedAt` on Conversation type
- **File:** `frontend/threadly-web/app/(app)/dashboard/page.tsx`
- **Issue:** Frontend calls `formatRelative(conv.updatedAt)` but if the backend
  `ConversationResponse` DTO uses `lastActivityAt` or similar, it will be undefined.
- **Fix needed:** Confirm field name in `ConversationResponse` DTO.

---

## API CONTRACT (Blocked — see existing MEMORY.md blockers)

### [BE-API-001] Springdoc OpenAPI not generating spec
- No `springdoc-openapi` dependency in `threadly-core/pom.xml`
- All 23 TypeScript type errors in frontend stem from this
- **Fix:** Add `springdoc-openapi-starter-webmvc-ui:2.x` → run `GET /v3/api-docs`
  → run `pnpm codegen` with Orval → replace all hand-written types

---

*Last updated: 2026-05-26 — Added during full UI redesign*
