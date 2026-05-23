# Threadly — top-level orchestration
# Run `make help` for all targets.

.PHONY: help up down logs ps restart clean \
        bootstrap seed codegen test \
        core-run ai-run web-run widget-run \
        core-test ai-test web-test \
        fmt lint

COMPOSE := docker compose -f infra/docker-compose.yml

help: ## Show this help
	@awk 'BEGIN {FS = ":.*##"; printf "Threadly targets:\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

# ── Infra ────────────────────────────────────────────────────────────
up: ## Boot full local stack (postgres, redis, qdrant, centrifugo, minio, services)
	$(COMPOSE) up -d --build
	@echo "→ web:        http://localhost:3000"
	@echo "→ core:       http://localhost:8080"
	@echo "→ ai:         http://localhost:8081"
	@echo "→ centrifugo: http://localhost:8000"
	@echo "→ grafana:    http://localhost:3001"

down: ## Stop the stack
	$(COMPOSE) down

logs: ## Tail logs from all services
	$(COMPOSE) logs -f --tail=100

ps: ## Show container status
	$(COMPOSE) ps

restart: down up ## Restart everything

clean: ## Stop + remove volumes (DESTRUCTIVE — wipes DB)
	$(COMPOSE) down -v

# ── First-time setup ─────────────────────────────────────────────────
bootstrap: ## Install all language toolchains and prime caches
	./scripts/bootstrap.sh

seed: ## Insert demo org + bot for local testing
	./scripts/seed-demo-bot.sh

# ── Codegen ─────────────────────────────────────────────────────────
codegen: ## Regenerate typed API hooks from core OpenAPI
	./scripts/codegen-api.sh

# ── Per-service run (for hot-reload dev outside Docker) ─────────────
core-run: ## Run threadly-core via Maven (hot reload)
	cd threadly-core && ./mvnw spring-boot:run

ai-run: ## Run threadly-ai via uvicorn (hot reload)
	cd threadly-ai && uv run uvicorn app.main:app --reload --port 8081

web-run: ## Run threadly-web via Next.js dev server
	cd threadly-web && pnpm dev

widget-run: ## Run threadly-widget Vite dev server
	cd threadly-widget && pnpm dev

# ── Tests ───────────────────────────────────────────────────────────
test: core-test ai-test web-test ## Run all test suites

core-test:
	cd threadly-core && ./mvnw test

ai-test:
	cd threadly-ai && uv run pytest

web-test:
	cd threadly-web && pnpm test

# ── Lint / format ───────────────────────────────────────────────────
fmt: ## Format all code
	cd threadly-core && ./mvnw spotless:apply
	cd threadly-ai && uv run ruff format .
	cd threadly-web && pnpm biome format --write .
	cd threadly-widget && pnpm biome format --write .

lint: ## Lint all code
	cd threadly-core && ./mvnw spotless:check
	cd threadly-ai && uv run ruff check . && uv run mypy app
	cd threadly-web && pnpm biome check .
	cd threadly-widget && pnpm biome check .
