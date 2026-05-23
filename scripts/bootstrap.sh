#!/usr/bin/env bash
set -euo pipefail

echo "==> Threadly bootstrap starting..."

# ── .env ──────────────────────────────────────────────────────────
if [ ! -f ".env" ]; then
  cp .env.example .env
  echo "  Created .env from .env.example — set ANTHROPIC_API_KEY before running make up"
fi

# ── JWT keys ──────────────────────────────────────────────────────
if [ ! -d "infra/keys" ]; then
  mkdir -p infra/keys
  openssl genrsa -out infra/keys/private.pem 2048
  openssl rsa -in infra/keys/private.pem -pubout -out infra/keys/public.pem
  echo "  Generated JWT RS256 key pair in infra/keys/"
fi

# ── Java 21 check ─────────────────────────────────────────────────
if ! java -version 2>&1 | grep -q "21"; then
  echo "  WARNING: Java 21 not detected. Install: brew install temurin@21"
fi

# ── Node check ────────────────────────────────────────────────────
if ! command -v pnpm &>/dev/null; then
  echo "  Installing pnpm..."
  npm install -g pnpm
fi

# ── Python / uv check ─────────────────────────────────────────────
if ! command -v uv &>/dev/null; then
  echo "  Installing uv..."
  curl -LsSf https://astral.sh/uv/install.sh | sh
fi

# ── Install JS deps ───────────────────────────────────────────────
echo "  Installing threadly-web deps..."
(cd threadly-web && pnpm install --frozen-lockfile 2>/dev/null || pnpm install)

echo "  Installing threadly-widget deps..."
(cd threadly-widget && pnpm install --frozen-lockfile 2>/dev/null || pnpm install)

# ── Install Python deps ───────────────────────────────────────────
echo "  Installing threadly-ai deps..."
(cd threadly-ai && uv sync)

# ── Pull Docker images ────────────────────────────────────────────
echo "  Pulling Docker images..."
docker compose -f infra/docker-compose.yml pull --quiet postgres redis qdrant centrifugo minio

# ── Create MinIO bucket ───────────────────────────────────────────
echo "  Starting MinIO to create bucket..."
docker compose -f infra/docker-compose.yml up minio -d --wait 2>/dev/null || true
sleep 2
docker run --rm --network host --entrypoint sh minio/mc \
  -c "mc alias set local http://localhost:9000 minioadmin minioadmin && mc mb --ignore-existing local/threadly-kb" 2>/dev/null || true
docker compose -f infra/docker-compose.yml stop minio 2>/dev/null || true

echo ""
echo "==> Bootstrap complete!"
echo "    Run: make up"
echo "    Then: make seed"
echo "    Open: http://localhost:3000"
