import type { NextConfig } from "next";

// Service URLs — override via env in production (docker/k8s)
// All default to their dedicated ports for local dev
const SVC = {
  identity:     process.env.IDENTITY_SERVICE_URL     ?? "http://localhost:3001",
  workspace:    process.env.WORKSPACE_SERVICE_URL    ?? "http://localhost:3002",
  flow:         process.env.FLOW_SERVICE_URL         ?? "http://localhost:3003",
  runtime:      process.env.RUNTIME_SERVICE_URL      ?? "http://localhost:3004",
  conversation: process.env.CONVERSATION_SERVICE_URL ?? "http://localhost:3005",
  knowledge:    process.env.KNOWLEDGE_SERVICE_URL    ?? "http://localhost:3006",
  analytics:    process.env.ANALYTICS_SERVICE_URL    ?? "http://localhost:3007",
  admin:        process.env.ADMIN_SERVICE_URL        ?? "http://localhost:3010",
};

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: "standalone",
  devIndicators: false,
  experimental: {
    serverActions: {
      allowedOrigins: ["localhost:3000"],
    },
  },
  env: {
    NEXT_PUBLIC_CENTRIFUGO_URL: process.env.NEXT_PUBLIC_CENTRIFUGO_URL ?? "ws://localhost:8000/connection/websocket",
  },
  /**
   * All API calls from the browser go to port 3000 (Next.js).
   * Next.js proxies them to the correct microservice — no CORS, no direct service calls.
   *
   * Order matters: more specific patterns must come before general ones.
   */
  async rewrites() {
    return [
      // ── Knowledge (specific: must be before /v1/bots/:path*) ──────────────
      { source: "/v1/bots/:botId/kb/:path*",   destination: `${SVC.knowledge}/api/v1/bots/:botId/kb/:path*` },
      { source: "/v1/bots/:botId/kb",          destination: `${SVC.knowledge}/api/v1/bots/:botId/kb` },

      // ── Analytics ─────────────────────────────────────────────────────────
      { source: "/v1/analytics/:path*",        destination: `${SVC.analytics}/api/v1/analytics/:path*` },
      { source: "/v1/analytics",               destination: `${SVC.analytics}/api/v1/analytics` },

      // ── Conversations ──────────────────────────────────────────────────────
      { source: "/v1/conversations/:path*",    destination: `${SVC.conversation}/api/v1/conversations/:path*` },
      { source: "/v1/conversations",           destination: `${SVC.conversation}/api/v1/conversations` },

      // ── Workspace (bots) ───────────────────────────────────────────────────
      { source: "/v1/bots/:path*",             destination: `${SVC.workspace}/api/v1/bots/:path*` },
      { source: "/v1/bots",                    destination: `${SVC.workspace}/api/v1/bots` },
      { source: "/v1/catalogs/:path*",         destination: `${SVC.workspace}/v1/catalogs/:path*` },
      { source: "/v1/templates",               destination: `${SVC.workspace}/v1/bots/templates` },

      // ── Flows ─────────────────────────────────────────────────────────────
      { source: "/v1/flows/:path*",            destination: `${SVC.flow}/api/v1/flows/:path*` },
      { source: "/v1/flows",                   destination: `${SVC.flow}/api/v1/flows` },

      // ── Runtime ───────────────────────────────────────────────────────────
      { source: "/v1/sessions/:path*",         destination: `${SVC.runtime}/api/v1/sessions/:path*` },
      { source: "/v1/sessions",                destination: `${SVC.runtime}/api/v1/sessions` },
      { source: "/v1/realtime/:path*",         destination: `${SVC.runtime}/api/v1/realtime/:path*` },
      { source: "/v1/proxy/:path*",            destination: `${SVC.runtime}/api/v1/proxy/:path*` },

      // ── Identity ──────────────────────────────────────────────────────────
      { source: "/v1/auth/:path*",             destination: `${SVC.identity}/v1/auth/:path*` },
      { source: "/v1/me/:path*",               destination: `${SVC.identity}/v1/me/:path*` },
      { source: "/v1/me",                      destination: `${SVC.identity}/v1/me` },
      { source: "/v1/team/:path*",             destination: `${SVC.identity}/v1/team/:path*` },
      { source: "/v1/team",                    destination: `${SVC.identity}/v1/team` },
      { source: "/v1/api-keys/:path*",         destination: `${SVC.identity}/v1/api-keys/:path*` },
      { source: "/v1/api-keys",                destination: `${SVC.identity}/v1/api-keys` },
      { source: "/auth/:path*",                destination: `${SVC.identity}/auth/:path*` },

      // ── Admin (super-admin only) ───────────────────────────────────────────
      { source: "/admin/api/:path*",           destination: `${SVC.admin}/admin/:path*` },
    ];
  },
};

export default nextConfig;
