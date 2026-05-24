#!/bin/bash

echo "════════════════════════════════════════════════════════════"
echo "FINAL VERIFICATION: Nginx Gateway Routing"
echo "════════════════════════════════════════════════════════════"
echo ""

# Check 1: Orval config uses /openapi.json
echo "1. Orval OpenAPI endpoint:"
grep "openapi.json" orval.config.ts && echo "   ✓ Correct" || echo "   ✗ FAILED"

# Check 2: Auth.ts has refresh logic
echo ""
echo "2. Auth.js token refresh logic:"
grep -q "refreshAccessToken" auth.ts && echo "   ✓ Added" || echo "   ✗ FAILED"
grep -q "/auth/refresh" auth.ts && echo "   ✓ Routes via gateway" || echo "   ✗ FAILED"

# Check 3: API client uses env var
echo ""
echo "3. API client configuration:"
grep -q "process.env.NEXT_PUBLIC_API_URL" lib/api.ts && echo "   ✓ api.ts uses env var" || echo "   ✗ FAILED"
grep -q "process.env.NEXT_PUBLIC_API_URL" lib/api-mutator.ts && echo "   ✓ api-mutator.ts uses env var" || echo "   ✗ FAILED"

# Check 4: Auth headers injected
echo ""
echo "4. Request headers:"
grep -q "Authorization" lib/api.ts && echo "   ✓ Auth header in api.ts" || echo "   ✗ FAILED"
grep -q "X-Trace-ID" lib/api.ts && echo "   ✓ Trace ID in api.ts" || echo "   ✗ FAILED"

# Check 5: No hardcoded service URLs
echo ""
echo "5. Hardcoded service URLs:"
if grep -r "://.*:300[1-9]\|://.*:8001" --include="*.ts" --include="*.tsx" \
   --exclude-dir=node_modules --exclude-dir=.next . 2>/dev/null | \
   grep -v "orval.config" | grep -v "→" | grep -v "GATEWAY_ROUTING"; then
   echo "   ✗ FAILED: Found hardcoded URLs"
else
   echo "   ✓ No hardcoded service URLs"
fi

# Check 6: Environment files
echo ""
echo "6. Environment configuration:"
grep -q "NEXT_PUBLIC_API_URL=http://localhost:8080" .env.local && \
   echo "   ✓ .env.local correct" || echo "   ✗ .env.local FAILED"
grep -q "NEXT_PUBLIC_API_URL=https://api.threadly.dev" .env.production && \
   echo "   ✓ .env.production correct" || echo "   ✗ .env.production FAILED"

# Check 7: Generated API code path
echo ""
echo "7. Orval output configuration:"
grep -q "lib/generated/api.ts" orval.config.ts && \
   echo "   ✓ Output path correct" || echo "   ✗ FAILED"
grep -q "api-mutator.ts" orval.config.ts && \
   echo "   ✓ Mutator configured" || echo "   ✗ FAILED"

# Check 8: Real-time token endpoint
echo ""
echo "8. Realtime endpoints:"
grep -q "/realtime/token" app/\(app\)/conversations/page.tsx 2>/dev/null && \
   echo "   ✓ Realtime token via gateway" || echo "   ✗ FAILED"

# Check 9: Test files use gateway
echo ""
echo "9. Test configuration:"
grep -q "NEXT_PUBLIC_API_URL" tests/auth.spec.ts && \
   echo "   ✓ Tests use gateway URL" || echo "   ✗ FAILED"

# Check 10: Package.json has codegen script
echo ""
echo "10. Build script:"
grep -q '"codegen": "orval' package.json && \
   echo "   ✓ Codegen script available" || echo "   ✗ FAILED"

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Verification complete!"
echo "════════════════════════════════════════════════════════════"
