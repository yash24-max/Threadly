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

k8s-consul-port-forward:
	@echo "Forwarding Consul UI to http://localhost:8500"
	kubectl port-forward -n threadly svc/consul 8500:8500

# ── Health Checks ──────────────────────────────────────────────────────────────
health:
	@echo "=== Checking Service Health ==="
	@for port in 3001 3002 3003 3004 3005 3006 3007 3008 3009; do \
		echo -n "Service on :$$port - "; \
		curl -s http://localhost:$$port/health | grep -q '"status":"UP"' && echo "✓ UP" || echo "✗ DOWN"; \
	done
	@echo "\n=== Checking Infrastructure ==="
	@echo -n "Consul on :8500 - " && curl -s http://localhost:8500/v1/status/leader | grep -q '8300' && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Kafka on :9092 - " && docker exec threadly-kafka kafka-broker-api-versions.sh --bootstrap-server=localhost:9092 >/dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Redis on :6379 - " && redis-cli -a threadly_dev ping >/dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Postgres on :5432 - " && pg_isready -h localhost -U threadly >/dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Qdrant on :6333 - " && curl -s http://localhost:6333/health >/dev/null 2>&1 && echo "✓ UP" || echo "✗ DOWN"

services:
	@echo "=== Registered Services (Consul) ==="
	curl -s http://localhost:8500/ui/dc1/services | jq -r '.[] | .Service + ": " + .Address' 2>/dev/null || curl -s http://localhost:8500/v1/catalog/services | jq '.' 2>/dev/null

schemas:
	@echo "=== Database Schemas ==="
	docker compose -f infra/docker-compose.yml exec -T postgres psql -U threadly -d threadly -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%_service' ORDER BY schema_name;"

kafka-topics:
	@echo "=== Kafka Topics ==="
	docker exec threadly-kafka kafka-topics.sh --list --bootstrap-server localhost:29092

# ── Seed ─────────────────────────────────────────────────────────────────────
seed:
	bash scripts/seed-demo-bot.sh

# ── Help ─────────────────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "Threadly — Available targets:"
	@echo ""
	@echo "Local Development (Docker Compose):"
	@echo "  make up            Start all services"
	@echo "  make down          Stop all services"
	@echo "  make logs s=<svc>  Tail service logs"
	@echo "  make build-all     Build all services"
	@echo "  make test-all      Run all tests"
	@echo "  make health        Check all service health"
	@echo "  make services      List registered Consul services"
	@echo "  make schemas       Show created database schemas"
	@echo "  make kafka-topics  List Kafka topics"
	@echo ""
	@echo "Build & Quality:"
	@echo "  make build-all     Build all services"
	@echo "  make test-all      Run all tests"
	@echo "  make lint-all      Run all linters"
	@echo "  make fmt-core      Format Java code"
	@echo ""
	@echo "Kubernetes Deployment:"
	@echo "  make k8s-deploy    Deploy to Kubernetes"
	@echo "  make k8s-delete    Delete Kubernetes deployment"
	@echo "  make k8s-status    Show Kubernetes status"
	@echo "  make k8s-logs s=<svc>  Tail Kubernetes logs"
	@echo ""
	@echo "Database & Configuration:"
	@echo "  make db-shell      Open Postgres shell"
	@echo "  make gen-keys      Generate RSA key pair"
	@echo "  make codegen       Regenerate TypeScript API hooks"
	@echo "  make bundle-size   Check widget bundle size"
	@echo ""
