# Threadly-Web Deployment Guide

## Pre-Deployment Checklist

```bash
# 1. Verify all gateway routing in place
bash verify-gateway-routing.sh

# 2. Run final verification
bash final-verification.sh

# 3. Run tests
npm test

# 4. Build with production environment
NEXT_PUBLIC_API_URL=https://api.threadly.dev npm run build

# 5. Verify no build errors
npm run lint
```

## Local Development

```bash
# Ensure Nginx gateway is running on :8080
# Ensure all microservices are running (:3001-3009)

# 1. Generate API hooks from gateway's OpenAPI schema
npm run codegen

# 2. Install dependencies (if needed)
npm install

# 3. Run dev server
npm run dev

# Gateway routes to:
# http://localhost:8080 → Nginx
# http://localhost:3000 → threadly-web
```

## Testing the Setup

### Test Authentication Flow
```bash
# 1. Login via credentials provider
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'

# Response: { "accessToken": "...", "refreshToken": "..." }
```

### Test API Calls via Gateway
```bash
# 1. Get access token
JWT="..."

# 2. Call workspace service via gateway
curl -H "Authorization: Bearer $JWT" \
  http://localhost:8080/bots

# Should return list of bots (routed to workspace-service:3002)
```

### Test Token Refresh
```bash
# When token is within 1 minute of expiry:
# nextAuth will automatically call:
curl -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer {refreshToken}"

# Returns: { "accessToken": "...", "refreshToken": "..." }
```

### Test Distributed Tracing
```bash
# Every request includes X-Trace-ID header
curl -v http://localhost:8080/bots \
  -H "Authorization: Bearer $JWT"

# Look for response header: X-Trace-ID: <32-char-hex>
```

## Production Deployment

### Environment Configuration
```bash
# Set in production environment:
export NEXT_PUBLIC_API_URL=https://api.threadly.dev

# Or in .env.production:
NEXT_PUBLIC_API_URL=https://api.threadly.dev
```

### Build & Deploy
```bash
# Build Next.js app
npm run build

# Start production server
npm start

# Verify API calls route through:
https://api.threadly.dev (Nginx gateway)
```

### Monitoring

Monitor Nginx gateway logs for:
```bash
# 1. All requests go through gateway
tail -f /var/log/nginx/access.log | grep "GET /bots\|POST /auth"

# 2. Trace IDs are present
grep "X-Trace-ID" /var/log/nginx/access.log

# 3. No direct service calls
# Should NOT see :3001-3009 in client logs
```

## Troubleshooting

### Issue: 404 on `/openapi.json`
```
Solution: Ensure Nginx gateway has SpringDocs configured
- Check: curl http://localhost:8080/openapi.json
- If fails: Manually configure Nginx to return OpenAPI spec
```

### Issue: "No authorization token"
```
Solution: Verify getSession() is called before API request
- Ensure component is inside <SessionProvider>
- Check that session.accessToken is available
```

### Issue: Realtime connection fails
```
Solution: Verify Centrifugo token endpoint
- Check: GET /realtime/token returns valid JWT
- Verify token includes org_id and user_id
- Check Centrifugo CORS configuration
```

## Rollback Plan

If issues occur:

1. **Check Nginx gateway status**
   ```bash
   docker ps | grep nginx
   docker logs <nginx-container>
   ```

2. **Verify microservices are up**
   ```bash
   curl http://localhost:3001/health  # identity-service
   curl http://localhost:3002/health  # workspace-service
   # ... etc for all services
   ```

3. **Restart threadly-web**
   ```bash
   npm run build
   npm start
   ```

4. **Check Nginx routing**
   ```bash
   curl -v http://localhost:8080/health
   ```

## Success Criteria

✅ All API calls routed through gateway
✅ Authentication flow works (login → refresh → logout)
✅ X-Trace-ID header on every request
✅ Tests pass with gateway URLs
✅ No hardcoded service URLs in code
✅ Production build uses correct domain

---

**See GATEWAY_ROUTING.md for technical architecture details.**
