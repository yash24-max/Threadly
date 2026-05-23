.PHONY: up down logs restart build-all test-all lint-all fmt-all codegen db-shell gen-keys deploy-railway

# ── Local Dev ────────────────────────────────────────────────────────────────
up:
	docker compose -f infra/docker-compose.yml up -d --build

down:
	docker compose -f infra/docker-compose.yml down

logs:
	docker compose -f infra/docker-compose.yml logs -f $(s)

restart:
	docker compose -f infra/docker-compose.yml restart $(s)

ps:
	docker compose -f infra/docker-compose.yml ps

# ── Build ────────────────────────────────────────────────────────────────────
build-core:
	cd threadly-core && ./mvnw clean package -DskipTests -q

build-ai:
	docker build -t threadly-ai:latest threadly-ai/

build-web:
	cd threadly-web && npm run build

build-widget:
	cd threadly-widget && npm run build

build-all: build-core build-ai build-web build-widget
	@echo "All services built ✓"

# ── Test ─────────────────────────────────────────────────────────────────────
test-core:
	cd threadly-core && ./mvnw test -Dsurefire.useFile=false

test-ai:
	cd threadly-ai && python -m pytest tests/ -v --tb=short

test-web:
	cd threadly-web && npx vitest run

test-widget:
	cd threadly-widget && npx vitest run

test-e2e:
	cd threadly-web && npx playwright test

test-all: test-core test-ai test-web test-widget
	@echo "All tests passed ✓"

# ── Code Quality ─────────────────────────────────────────────────────────────
lint-core:
	cd threadly-core && ./mvnw spotless:check

fmt-core:
	cd threadly-core && ./mvnw spotless:apply

lint-ai:
	cd threadly-ai && ruff check . && mypy app/ --strict

fmt-ai:
	cd threadly-ai && ruff format .

lint-web:
	cd threadly-web && npx tsc --noEmit && npx biome check .

fmt-web:
	cd threadly-web && npx biome format --write .

lint-widget:
	cd threadly-widget && npx tsc --noEmit

lint-all: lint-core lint-ai lint-web lint-widget
	@echo "All lint checks passed ✓"

# ── Database ──────────────────────────────────────────────────────────────────
db-shell:
	docker compose -f infra/docker-compose.yml exec postgres psql -U threadly -d threadly

db-migrate:
	docker compose -f infra/docker-compose.yml exec threadly-core ./mvnw flyway:info

db-reset:
	docker compose -f infra/docker-compose.yml exec postgres psql -U threadly -d threadly -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "Database reset. Run 'make up' to re-apply migrations."

# ── Code Generation ───────────────────────────────────────────────────────────
codegen:
	cd threadly-web && bash ../scripts/codegen-api.sh

# ── Secrets ───────────────────────────────────────────────────────────────────
gen-keys:
	mkdir -p infra/keys
	openssl genrsa -out infra/keys/private.pem 2048
	openssl rsa -in infra/keys/private.pem -pubout -out infra/keys/public.pem
	@echo "RSA keys generated in infra/keys/"

# ── Widget Bundle Size ────────────────────────────────────────────────────────
bundle-size:
	cd threadly-widget && npm run build && \
	gzip -c dist/widget.js | wc -c | awk '{printf "Widget bundle: %.1f KB gzipped\n", $$1/1024}'

# ── Deploy ────────────────────────────────────────────────────────────────────
deploy-railway:
	railway up --service threadly-core
	railway up --service threadly-ai
	railway up --service threadly-web

# ── Seed ─────────────────────────────────────────────────────────────────────
seed:
	bash scripts/seed-demo-bot.sh

# ── Help ─────────────────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "Threadly — Available targets:"
	@echo "  make up            Start all services"
	@echo "  make down          Stop all services"
	@echo "  make logs s=<svc>  Tail service logs"
	@echo "  make build-all     Build all services"
	@echo "  make test-all      Run all tests"
	@echo "  make lint-all      Run all linters"
	@echo "  make fmt-core      Format Java code"
	@echo "  make codegen       Regenerate TypeScript API hooks"
	@echo "  make gen-keys      Generate RSA key pair"
	@echo "  make db-shell      Open Postgres shell"
	@echo "  make bundle-size   Check widget bundle size"
	@echo ""
