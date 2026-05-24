-- Flyway database migration for analytics-service schema
-- Creates tables for analytics events, metrics, rollups, and dashboards

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS analytics_service;

-- Analytics Events Table
-- Stores raw analytics events captured from domain events
CREATE TABLE IF NOT EXISTS analytics_service.analytics_events (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    conversation_id VARCHAR(36),
    session_id VARCHAR(36),
    event_data_json TEXT NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_org FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

-- Create indexes for efficient time-series queries
CREATE INDEX IF NOT EXISTS idx_events_org_timestamp ON analytics_service.analytics_events(org_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_events_bot_timestamp ON analytics_service.analytics_events(bot_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_events_conversation ON analytics_service.analytics_events(conversation_id);
CREATE INDEX IF NOT EXISTS idx_events_session ON analytics_service.analytics_events(session_id);
CREATE INDEX IF NOT EXISTS idx_events_type ON analytics_service.analytics_events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_created ON analytics_service.analytics_events(created_at DESC);

-- Metrics Table
-- Stores computed metrics from analytics events
CREATE TABLE IF NOT EXISTS analytics_service.metrics (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    value NUMERIC(15, 4) NOT NULL,
    tags_json TEXT,
    metric_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_metrics_org FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

-- Create indexes for metric queries
CREATE INDEX IF NOT EXISTS idx_metrics_org_name_timestamp ON analytics_service.metrics(org_id, metric_name, metric_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_bot_name_timestamp ON analytics_service.metrics(bot_id, metric_name, metric_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_name ON analytics_service.metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON analytics_service.metrics(metric_timestamp DESC);

-- Daily Rollup Table
-- Pre-computed aggregated metrics on a daily basis
CREATE TABLE IF NOT EXISTS analytics_service.daily_rollups (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    rollup_date DATE NOT NULL,
    conversations_count BIGINT NOT NULL DEFAULT 0,
    messages_count BIGINT NOT NULL DEFAULT 0,
    ai_calls_count BIGINT NOT NULL DEFAULT 0,
    avg_response_time_ms NUMERIC(10, 2) NOT NULL DEFAULT 0,
    resolution_rate NUMERIC(5, 2) NOT NULL DEFAULT 0,
    avg_csat_score NUMERIC(3, 2) DEFAULT 0,
    total_tokens_consumed BIGINT NOT NULL DEFAULT 0,
    total_cost_cents BIGINT NOT NULL DEFAULT 0,
    handoffs_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rollup_org FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT uk_rollup_org_bot_date UNIQUE(org_id, bot_id, rollup_date)
);

-- Create indexes for rollup queries
CREATE INDEX IF NOT EXISTS idx_rollup_org_date ON analytics_service.daily_rollups(org_id, rollup_date DESC);
CREATE INDEX IF NOT EXISTS idx_rollup_bot_date ON analytics_service.daily_rollups(bot_id, rollup_date DESC);
CREATE INDEX IF NOT EXISTS idx_rollup_date ON analytics_service.daily_rollups(rollup_date DESC);

-- Dashboard Views Table
-- User-created custom analytics dashboards
CREATE TABLE IF NOT EXISTS analytics_service.dashboard_views (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    view_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    widgets_json TEXT NOT NULL,
    filters_json TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_viewed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dashboard_org FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT uk_dashboard_org_name UNIQUE(org_id, view_name)
);

-- Create indexes for dashboard queries
CREATE INDEX IF NOT EXISTS idx_dashboard_org ON analytics_service.dashboard_views(org_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_created ON analytics_service.dashboard_views(created_at DESC);

-- Create partitioning setup for analytics_events (optional, for very large datasets)
-- Uncomment if needed for production with high event volume
-- ALTER TABLE analytics_service.analytics_events
-- PARTITION BY RANGE (YEAR(created_at)) (
--     PARTITION p_2024 VALUES LESS THAN (2025),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- Grant permissions (adjust as needed for your organization)
-- GRANT SELECT, INSERT, UPDATE ON analytics_service.* TO 'analytics_app_user'@'%';
-- GRANT SELECT ON analytics_service.* TO 'analytics_readonly'@'%';

-- Add retention policy comment (implement via jobs as needed)
-- Events: Keep 90 days of detailed data, then aggregate to rollups
-- Rollups: Keep 1 year of daily aggregations
-- Dashboards: No automatic deletion (user-managed)
