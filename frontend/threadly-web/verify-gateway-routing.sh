#!/bin/bash
set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║  Verifying Nginx Gateway Routing Setup                ║"
echo "╚════════════════════════════════════════════════════════╝"

ISSUES=0

# 1. Check for hardcoded service URLs
echo ""
echo "▸ Checking for hardcoded service URLs (3001-3009, 8001)..."
if grep -r "3001\|3002\|3003\|3004\|3005\|3006\|3007\|3008\|3009\|8001" \
    --include="*.ts" --include="*.tsx" --include="*.js" \
    --exclude-dir=node_modules --exclude-dir=.next \
    . 2>/dev/null | grep -v "orval.config" | grep -v "→" | grep -v "port" | grep -v "GATEWAY_ROUTING" | grep -v "verify-gateway"; then
    echo "  ✗ FAILED: Found hardcoded service URLs (see above)"
    ISSUES=$((ISSUES+1))
else
    echo "  ✓ PASSED: No hardcoded service URLs found"
fi

# 2. Check env vars
echo ""
echo "▸ Checking environment variables..."
if [ -f ".env.local" ] && grep -q "NEXT_PUBLIC_API_URL" ".env.local"; then
    echo "  ✓ PASSED: .env.local has NEXT_PUBLIC_API_URL"
else
    echo "  ✗ FAILED: .env.local missing NEXT_PUBLIC_API_URL"
    ISSUES=$((ISSUES+1))
fi

if [ -f ".env.production" ] && grep -q "NEXT_PUBLIC_API_URL" ".env.production"; then
    echo "  ✓ PASSED: .env.production has NEXT_PUBLIC_API_URL"
else
    echo "  ✗ FAILED: .env.production missing NEXT_PUBLIC_API_URL"
    ISSUES=$((ISSUES+1))
fi

# 3. Check API files exist
echo ""
echo "▸ Checking required API files..."
for file in "lib/api.ts" "lib/api-mutator.ts" "auth.ts" "orval.config.ts"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file exists"
    else
        echo "  ✗ FAILED: $file missing"
        ISSUES=$((ISSUES+1))
    fi
done

# 4. Check api.ts uses NEXT_PUBLIC_API_URL
echo ""
echo "▸ Checking api.ts configuration..."
if grep -q "process.env.NEXT_PUBLIC_API_URL" "lib/api.ts"; then
    echo "  ✓ PASSED: api.ts uses NEXT_PUBLIC_API_URL"
else
    echo "  ✗ FAILED: api.ts doesn't use NEXT_PUBLIC_API_URL"
    ISSUES=$((ISSUES+1))
fi

# 5. Check auth.ts routes through gateway
echo ""
echo "▸ Checking auth.ts configuration..."
if grep -q "process.env.NEXT_PUBLIC_API_URL" "auth.ts" && grep -q "/auth/login" "auth.ts"; then
    echo "  ✓ PASSED: auth.ts routes login through gateway"
else
    echo "  ✗ FAILED: auth.ts not properly configured"
    ISSUES=$((ISSUES+1))
fi

# 6. Check api-mutator.ts injects auth headers
echo ""
echo "▸ Checking api-mutator.ts configuration..."
if grep -q "Authorization" "lib/api-mutator.ts" && grep -q "X-Trace-ID" "lib/api-mutator.ts"; then
    echo "  ✓ PASSED: api-mutator.ts injects auth + tracing headers"
else
    echo "  ✗ FAILED: api-mutator.ts missing auth/tracing headers"
    ISSUES=$((ISSUES+1))
fi

# 7. Check orval config
echo ""
echo "▸ Checking orval.config.ts..."
if grep -q "openapi.json" "orval.config.ts"; then
    echo "  ✓ PASSED: orval.config.ts points to /openapi.json"
else
    echo "  ✗ FAILED: orval.config.ts not pointing to /openapi.json"
    ISSUES=$((ISSUES+1))
fi

if grep -q "api-mutator.ts" "orval.config.ts"; then
    echo "  ✓ PASSED: orval.config.ts uses custom mutator"
else
    echo "  ✗ FAILED: orval.config.ts not using custom mutator"
    ISSUES=$((ISSUES+1))
fi

# 8. Summary
echo ""
echo "╔════════════════════════════════════════════════════════╗"
if [ $ISSUES -eq 0 ]; then
    echo "║  ✓ All checks passed!                                 ║"
else
    echo "║  ✗ Found $ISSUES issue(s)                                  ║"
fi
echo "╚════════════════════════════════════════════════════════╝"

exit $ISSUES
