#!/bin/bash

##############################################################################
# Threadly Microservices Migration Script
# Phase 1: Shadow Mode (Data Copy from Monolith to Service Schemas)
#
# Usage: bash scripts/migrate-monolith-to-services.sh [phase]
#        phase: phase1 (default), phase2-start, phase3-cutover
##############################################################################

set -e

# Configuration
PHASE=${1:-"phase1"}
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-"threadly"}
DB_USER=${DB_USER:-"threadly"}
DB_PASSWORD=${DB_PASSWORD:-"dev"}
PSQL_CMD="psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
  echo -e "${RED}[ERROR]${NC} $1"
  exit 1
}

log_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

verify_db_connection() {
  log_info "Verifying database connection..."
  if ! $PSQL_CMD -c "SELECT 1" > /dev/null 2>&1; then
    log_error "Failed to connect to database at $DB_HOST:$DB_PORT"
  fi
  log_info "Database connection successful"
}

##############################################################################
# PHASE 1: Shadow Mode - Copy data from monolith to new service schemas
##############################################################################
phase1_shadow_mode() {
  log_info "Starting PHASE 1: Shadow Mode (Data Copy)"
  log_info "This phase copies data from public schema to service schemas (read-only)"

  verify_db_connection

  # Create service schemas if they don't exist
  log_info "Creating service schemas..."
  $PSQL_CMD << EOF
CREATE SCHEMA IF NOT EXISTS identity_service;
CREATE SCHEMA IF NOT EXISTS workspace_service;
CREATE SCHEMA IF NOT EXISTS flow_service;
CREATE SCHEMA IF NOT EXISTS runtime_service;
CREATE SCHEMA IF NOT EXISTS conversation_service;
CREATE SCHEMA IF NOT EXISTS knowledge_service;
CREATE SCHEMA IF NOT EXISTS analytics_service;
CREATE SCHEMA IF NOT EXISTS billing_service;
CREATE SCHEMA IF NOT EXISTS integration_service;
EOF

  log_info "Copying users to identity_service.users..."
  count_before=$(echo "SELECT COUNT(*) FROM public.users;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO identity_service.users
  SELECT id, email, password_hash, email_verified, oauth_provider, oauth_id,
         created_at, updated_at
  FROM public.users
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM identity_service.users;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying organizations to identity_service.organizations..."
  count_before=$(echo "SELECT COUNT(*) FROM public.organizations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO identity_service.organizations
  SELECT id, name, slug, owner_id, subscription_plan, billing_email,
         created_at, updated_at
  FROM public.organizations
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM identity_service.organizations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying memberships to identity_service.memberships..."
  count_before=$(echo "SELECT COUNT(*) FROM public.memberships;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO identity_service.memberships
  SELECT id, org_id, user_id, role, created_at
  FROM public.memberships
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM identity_service.memberships;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying api_keys to identity_service.api_keys..."
  count_before=$(echo "SELECT COUNT(*) FROM public.api_keys;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO identity_service.api_keys
  SELECT id, org_id, name, key_hash, scopes, last_used_at, created_at
  FROM public.api_keys
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM identity_service.api_keys;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying bots to workspace_service.bots..."
  count_before=$(echo "SELECT COUNT(*) FROM public.bots;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO workspace_service.bots
  SELECT id, org_id, name, description, language, accent_color, avatar_url,
         welcome_message, kb_search_enabled, integrations, webhook_signing_key,
         status, created_at, updated_at
  FROM public.bots
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM workspace_service.bots;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying flows to flow_service.flows..."
  count_before=$(echo "SELECT COUNT(*) FROM public.flows;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO flow_service.flows
  SELECT id, bot_id, org_id, name, description, flow_json, created_at, updated_at
  FROM public.flows
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM flow_service.flows;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying flow_versions to flow_service.flow_versions..."
  count_before=$(echo "SELECT COUNT(*) FROM public.flow_versions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO flow_service.flow_versions
  SELECT id, flow_id, version_number, flow_json, published_by_id, published_at,
         is_published, created_at
  FROM public.flow_versions
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM flow_service.flow_versions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying sessions to runtime_service.sessions..."
  count_before=$(echo "SELECT COUNT(*) FROM public.sessions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO runtime_service.sessions
  SELECT id, bot_id, org_id, visitor_id, flow_id, current_node_id, variables,
         status, started_at, last_activity_at, ended_at
  FROM public.sessions
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM runtime_service.sessions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying conversations to conversation_service.conversations..."
  count_before=$(echo "SELECT COUNT(*) FROM public.conversations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO conversation_service.conversations
  SELECT id, bot_id, org_id, visitor_id, started_at, ended_at, status,
         assigned_agent_id, is_lead, lead_id
  FROM public.conversations
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM conversation_service.conversations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying messages to conversation_service.messages..."
  count_before=$(echo "SELECT COUNT(*) FROM public.messages;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO conversation_service.messages
  SELECT id, conversation_id, sender_type, sender_id, sender_name, content,
         message_type, metadata, created_at
  FROM public.messages
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM conversation_service.messages;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying kb_documents to knowledge_service.kb_documents..."
  count_before=$(echo "SELECT COUNT(*) FROM public.kb_documents;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO knowledge_service.kb_documents
  SELECT id, bot_id, org_id, document_name, document_type, source_url, file_path,
         file_size_bytes, page_count, status, error_message, chunk_count,
         indexed_at, created_at
  FROM public.kb_documents
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM knowledge_service.kb_documents;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying kb_chunks to knowledge_service.kb_chunks..."
  count_before=$(echo "SELECT COUNT(*) FROM public.kb_chunks;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO knowledge_service.kb_chunks
  SELECT id, kb_document_id, chunk_number, content, token_count, metadata,
         qdrant_point_id, created_at
  FROM public.kb_chunks
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM knowledge_service.kb_chunks;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying analytics_events to analytics_service.analytics_events..."
  count_before=$(echo "SELECT COUNT(*) FROM public.analytics_events;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO analytics_service.analytics_events
  SELECT id, org_id, bot_id, event_type, event_data, created_at
  FROM public.analytics_events
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM analytics_service.analytics_events;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying plans to billing_service.plans..."
  count_before=$(echo "SELECT COUNT(*) FROM public.plans;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO billing_service.plans
  SELECT id, name, price_usd, billing_period, features, stripe_product_id,
         stripe_price_id, created_at
  FROM public.plans
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM billing_service.plans;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying subscriptions to billing_service.subscriptions..."
  count_before=$(echo "SELECT COUNT(*) FROM public.subscriptions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO billing_service.subscriptions
  SELECT id, org_id, plan_id, stripe_subscription_id, stripe_customer_id, status,
         current_period_start, current_period_end, trial_end, cancel_at_period_end,
         auto_renew, created_at, updated_at
  FROM public.subscriptions
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM billing_service.subscriptions;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info "Copying integrations to integration_service.integrations..."
  count_before=$(echo "SELECT COUNT(*) FROM public.integrations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  $PSQL_CMD << EOF
INSERT INTO integration_service.integrations
  SELECT id, org_id, name, type, display_config, status, created_at, updated_at
  FROM public.integrations
  ON CONFLICT (id) DO NOTHING;
EOF
  count_after=$(echo "SELECT COUNT(*) FROM integration_service.integrations;" | $PSQL_CMD -t -c "$(cat)" 2>/dev/null || echo "0")
  log_info "  Before: $count_before | After: $count_after"

  log_info ""
  log_info "PHASE 1 COMPLETE: Shadow mode data copy successful"
  log_info "All data has been copied to service schemas (monolith still primary)"
  log_info "Next step: Deploy all 9 microservices and run integration tests"
  log_info "Then: Phase 2 - Enable dual-writes"
}

##############################################################################
# PHASE 2: Dual-Write Mode - Enable writes to both monolith and services
##############################################################################
phase2_dual_write() {
  log_info "Starting PHASE 2: Dual-Write Mode"
  log_info "This phase enables dual-writes to monolith + services"

  verify_db_connection

  log_info "Checking microservices health..."
  services=(
    "identity-service:3001"
    "workspace-service:3002"
    "flow-service:3003"
    "runtime-service:3004"
    "conversation-service:3005"
    "knowledge-service:3006"
    "analytics-service:3007"
    "billing-service:3008"
    "integration-service:3009"
  )

  for service in "${services[@]}"; do
    IFS=':' read -r svc_name svc_port <<< "$service"
    if curl -s "http://localhost:$svc_port/health" > /dev/null; then
      log_info "  ✓ $svc_name is healthy"
    else
      log_error "$svc_name is not responding at port $svc_port"
    fi
  done

  log_info "Enable DualWriteInterceptor in application.properties:"
  log_info "  migration.dual.write.enabled=true"
  log_info "  migration.dual.write.fail.strategy=log_only"
  log_info ""
  log_info "PHASE 2: Configure dual-writes in threadly-common-spring"
  log_info "This enables writes to both monolith + services during Phase 2"
  log_info "Monitor metrics: migration.write.lag, migration.write.failures"
}

##############################################################################
# PHASE 3: Cutover - Switch all traffic to microservices
##############################################################################
phase3_cutover() {
  log_info "Starting PHASE 3: Cutover"
  log_info "This phase switches all traffic from monolith to microservices"

  verify_db_connection

  log_info "Cutover strategy:"
  log_info "  1. Update Nginx routing to bypass monolith"
  log_info "  2. Set monolith to read-only maintenance mode"
  log_info "  3. Monitor for 2 weeks (safe rollback window)"
  log_info "  4. Decommission monolith (week 4)"
  log_info ""
  log_info "Manual steps required:"
  log_info "  • Restart Nginx with cutover config"
  log_info "  • Set monolith Spring Boot property: server.read.only=true"
  log_info "  • Monitor metrics: all services /health must return 200"
  log_info "  • Run full integration test suite"
}

##############################################################################
# Display usage and run requested phase
##############################################################################
case "$PHASE" in
  phase1)
    phase1_shadow_mode
    ;;
  phase2)
    phase2_dual_write
    ;;
  phase3)
    phase3_cutover
    ;;
  *)
    log_error "Unknown phase: $PHASE"
    log_info "Usage: bash scripts/migrate-monolith-to-services.sh [phase1|phase2|phase3]"
    exit 1
    ;;
esac

log_info "Migration phase $PHASE completed successfully"
