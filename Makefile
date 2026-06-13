.PHONY: up down logs restart ps build-services build-ai build-web build-widget build-all \
        test-all test-ai test-web test-widget test-e2e \
        lint-all lint-ai lint-web lint-widget fmt-ai fmt-web \
        db-shell db-reset codegen bundle-size deploy-railway \
        k8s-deploy k8s-delete k8s-status k8s-logs \
        keycloak-open keycloak-realm-export keycloak-reset \
        health schemas seed help

COMPOSE = docker compose -f infra/docker-compose.yml

# ── Local Dev ────────────────────────────────────────────────────────────────
up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down

logs:
	$(COMPOSE) logs -f $(s)

restart:
	$(COMPOSE) restart $(s)

ps:
	$(COMPOSE) ps

# ── Build ────────────────────────────────────────────────────────────────────
build-services:
	@for svc in services/*/; do \
		echo "Building $$svc..."; \
		cd $$svc && ./mvnw clean package -DskipTests -q && cd ../..; \
	done

build-ai:
	docker build -t threadly-ai:latest ai/

build-web:
	cd frontend/threadly-web && pnpm run build

build-widget:
	cd threadly-widget && npm run build

build-all: build-services build-ai build-web build-widget
	@echo "All services built ✓"

# ── Test ─────────────────────────────────────────────────────────────────────
test-services:
	@for svc in services/*/; do \
		echo "Testing $$svc..."; \
		cd $$svc && ./mvnw test -Dsurefire.useFile=false && cd ../..; \
	done

test-ai:
	cd ai && python -m pytest tests/ -v --tb=short

test-web:
	cd frontend/threadly-web && npx vitest run

test-widget:
	cd threadly-widget && npx vitest run

test-e2e:
	cd frontend/threadly-web && npx playwright test

test-all: test-services test-ai test-web test-widget
	@echo "All tests passed ✓"

# ── Code Quality ─────────────────────────────────────────────────────────────
lint-services:
	@for svc in services/*/; do \
		cd $$svc && ./mvnw spotless:check && cd ../..; \
	done

fmt-services:
	@for svc in services/*/; do \
		cd $$svc && ./mvnw spotless:apply && cd ../..; \
	done

lint-ai:
	cd ai && ruff check . && mypy app/ --strict

fmt-ai:
	cd ai && ruff format .

lint-web:
	cd frontend/threadly-web && npx tsc --noEmit && npx biome check .

fmt-web:
	cd frontend/threadly-web && npx biome format --write .

lint-widget:
	cd threadly-widget && npx tsc --noEmit

lint-all: lint-services lint-ai lint-web lint-widget
	@echo "All lint checks passed ✓"

# ── Keycloak ──────────────────────────────────────────────────────────────────
keycloak-open:
	@echo "Opening Keycloak Admin UI..."
	open http://localhost:8090/admin || xdg-open http://localhost:8090/admin

keycloak-realm-export:
	@echo "Exporting threadly realm to infra/keycloak/threadly-realm.json..."
	$(COMPOSE) exec keycloak /opt/keycloak/bin/kc.sh export \
		--dir /tmp/export --realm threadly
	$(COMPOSE) cp keycloak:/tmp/export/threadly-realm.json infra/keycloak/threadly-realm.json
	@echo "Realm exported ✓"

keycloak-reset:
	@echo "Resetting Keycloak realm (deletes all users/sessions)..."
	$(COMPOSE) restart keycloak
	@echo "Keycloak restarted — realm will re-import from infra/keycloak/threadly-realm.json"

# ── Database ──────────────────────────────────────────────────────────────────
db-shell:
	$(COMPOSE) exec postgres psql -U threadly -d threadly

db-reset:
	$(COMPOSE) exec postgres psql -U threadly -d threadly -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "Database reset. Run 'make up' to re-apply migrations."

schemas:
	@echo "=== Database Schemas ==="
	$(COMPOSE) exec -T postgres psql -U threadly -d threadly \
		-c "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%_service' ORDER BY schema_name;"

# ── Code Generation ───────────────────────────────────────────────────────────
codegen:
	cd frontend/threadly-web && bash ../../scripts/codegen-api.sh

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
	railway up --service threadly-ai
	railway up --service threadly-web

# ── Kubernetes ─────────────────────────────────────────────────────────────────
k8s-deploy:
	kubectl apply -f infra/k8s/00-namespace.yaml
	kubectl apply -f infra/k8s/01-postgres-statefulset.yaml
	kubectl apply -f infra/k8s/02-redis-statefulset.yaml
	kubectl apply -f infra/k8s/03-kafka-statefulset.yaml
	kubectl apply -f infra/k8s/04-nginx-configmap.yaml
	kubectl apply -f infra/k8s/05-microservices-deployments.yaml
	@echo "Kubernetes deployment started"

k8s-delete:
	kubectl delete namespace threadly
	@echo "Kubernetes namespace deleted"

k8s-status:
	@echo "=== Threadly Kubernetes Status ==="
	kubectl get all -n threadly
	@echo "\n=== Services ==="
	kubectl get svc -n threadly
	@echo "\n=== StatefulSets ==="
	kubectl get statefulset -n threadly

k8s-logs:
	kubectl logs -n threadly -f deployment/$(s) 2>/dev/null || kubectl logs -n threadly -f statefulset/$(s)

# ── Health Checks ──────────────────────────────────────────────────────────────
health:
	@echo "=== Checking Service Health ==="
	@echo -n "threadly-core  :8080 - " && curl -sf http://localhost:8080/actuator/health | python3 -c "import sys,json; print('✓ UP' if json.load(sys.stdin).get('status')=='UP' else '✗ DOWN')" 2>/dev/null || echo "✗ DOWN"
	@echo -n "threadly-ai    :8081 - " && curl -sf http://localhost:8081/health > /dev/null && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "threadly-web   :3000 - " && curl -sf http://localhost:3000 > /dev/null && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "centrifugo     :8000 - " && curl -sf http://localhost:8000/health > /dev/null && echo "✓ UP" || echo "✗ DOWN"
	@echo ""
	@echo "=== Infrastructure ==="
	@echo -n "Redis    :6379 - " && redis-cli -a threadly_dev ping > /dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Postgres :5432 - " && pg_isready -h localhost -U threadly > /dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Qdrant   :6333 - " && curl -sf http://localhost:6333/health > /dev/null && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "MinIO    :9000 - " && curl -sf http://localhost:9000/minio/health/live > /dev/null && echo "✓ UP" || echo "✗ DOWN"

# ── Dev (frontend only, backend via Docker) ───────────────────────────────────
dev-web:
	cd frontend/threadly-web && pnpm dev --port 3002

# ── Seed ─────────────────────────────────────────────────────────────────────
seed:
	bash scripts/seed-demo-bot.sh

# ── Help ─────────────────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "Threadly — Available targets:"
	@echo ""
	@echo "Local Development (Docker Compose):"
	@echo "  make up              Start all services"
	@echo "  make down            Stop all services"
	@echo "  make logs s=<svc>    Tail service logs"
	@echo "  make restart s=<svc> Restart a service"
	@echo "  make ps              Show running containers"
	@echo "  make health          Check all service health"
	@echo "  make dev-web         Start frontend dev server on :3002"
	@echo ""
	@echo "Build:"
	@echo "  make build-services  Build all Java microservices"
	@echo "  make build-ai        Build AI service Docker image"
	@echo "  make build-web       Build Next.js frontend"
	@echo "  make build-all       Build everything"
	@echo ""
	@echo "Test & Quality:"
	@echo "  make test-all        Run all tests"
	@echo "  make lint-all        Run all linters"
	@echo "  make fmt-services    Format all Java code"
	@echo ""
	@echo "Database:"
	@echo "  make db-shell        Open Postgres shell"
	@echo "  make db-reset        Reset database (destructive)"
	@echo "  make schemas         Show service schemas"
	@echo "  make gen-keys        Generate RSA key pair"
	@echo "  make codegen         Regenerate TypeScript API hooks"
	@echo "  make bundle-size     Check widget bundle size"
	@echo ""
	@echo "Kubernetes:"
	@echo "  make k8s-deploy      Deploy to Kubernetes"
	@echo "  make k8s-status      Show Kubernetes status"
	@echo "  make k8s-logs s=<svc> Tail Kubernetes logs"
	@echo ""
