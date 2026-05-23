#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${CORE_URL:-http://localhost:8080}"
OUT_DIR="threadly-web/app/_generated"

echo "==> Fetching OpenAPI spec from $BASE_URL..."
curl -sf "$BASE_URL/v3/api-docs" -o /tmp/threadly-openapi.json

echo "==> Generating typed TanStack Query hooks via Orval..."
cd threadly-web
pnpm orval --config orval.config.ts
cd ..

echo "==> Done. Generated files in $OUT_DIR/"
ls "$OUT_DIR/"
