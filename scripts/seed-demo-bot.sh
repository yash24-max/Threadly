#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${CORE_URL:-http://localhost:8080}"

echo "==> Seeding demo org + bot..."

# 1. Create org + admin user
SIGNUP=$(curl -sf -X POST "$BASE_URL/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "orgName": "Demo Company",
    "name": "Demo Admin",
    "email": "admin@demo.threadly.dev",
    "password": "Demo1234!"
  }')

TOKEN=$(echo "$SIGNUP" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "  Created org. Token: ${TOKEN:0:20}..."

# 2. Create demo bot
BOT=$(curl -sf -X POST "$BASE_URL/v1/bots" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo Support Bot",
    "description": "A demo AI chatbot for testing",
    "language": "en"
  }')

BOT_ID=$(echo "$BOT" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "  Created bot: $BOT_ID"

# 3. Publish a simple demo flow
curl -sf -X PUT "$BASE_URL/v1/bots/$BOT_ID/flow" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "version": 1,
    "nodes": [
      {"id":"start","type":"start","position":{"x":100,"y":100},"data":{}},
      {"id":"n1","type":"message","position":{"x":100,"y":220},"data":{"text":"Hi! I am the Threadly demo bot 👋 How can I help you today?"}},
      {"id":"n2","type":"ai_reply","position":{"x":100,"y":360},"data":{"system_prompt":"You are a friendly support assistant. Be helpful and concise.","use_kb":false,"max_tokens":300}}
    ],
    "edges": [
      {"id":"e1","source":"start","target":"n1"},
      {"id":"e2","source":"n1","target":"n2"}
    ]
  }' > /dev/null

curl -sf -X POST "$BASE_URL/v1/bots/$BOT_ID/flow/publish" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

echo "  Published demo flow"

echo ""
echo "==> Seed complete!"
echo "    Login:  admin@demo.threadly.dev / Demo1234!"
echo "    Bot ID: $BOT_ID"
echo "    Embed snippet:"
echo "    <script src='http://localhost:8080/widget/embed.js' data-bot='$BOT_ID' async></script>"
