"use client";

import { useSession } from "next-auth/react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useEffect } from "react";
import { api } from "@/lib/api";
import type { DashboardStats, Conversation, Bot } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import {
  MessageSquare, Bot as BotIcon, Clock, TrendingUp,
  Plus, ArrowRight, Loader2, Activity, Zap,
} from "lucide-react";

// ─── Stat card ────────────────────────────────────────────────────────────────

interface StatCardProps {
  label: string;
  value: string | number;
  delta?: string;
  icon: React.ElementType;
  color: string;
  loading?: boolean;
}

function StatCard({ label, value, delta, icon: Icon, color, loading }: StatCardProps) {
  return (
    <div
      style={{
        background: "var(--bg-panel)",
        border: "1px solid var(--border)",
        borderRadius: 14,
        padding: "20px 22px",
        transition: "border-color 150ms ease",
      }}
    >
      <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 14 }}>
        <p style={{ fontSize: 12, fontWeight: 500, color: "var(--text-muted)", letterSpacing: "0.02em" }}>{label}</p>
        <div
          style={{
            width: 34, height: 34, borderRadius: 10,
            background: color + "20",
            display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0,
          }}
        >
          <Icon size={16} color={color} />
        </div>
      </div>

      {loading ? (
        <div style={{ height: 32, background: "var(--bg-surface)", borderRadius: 8, width: "60%", animation: "pulse 1.5s infinite" }} />
      ) : (
        <p style={{ fontSize: 28, fontWeight: 700, letterSpacing: "-1.5px", color: "var(--text-primary)" }}>
          {value}
        </p>
      )}

      {delta && !loading && (
        <p style={{ fontSize: 11, color: "var(--success)", marginTop: 4 }}>{delta}</p>
      )}
    </div>
  );
}

// ─── Empty state ──────────────────────────────────────────────────────────────

function EmptyState({ icon: Icon, title, desc, href, cta }: {
  icon: React.ElementType; title: string; desc: string; href: string; cta: string;
}) {
  return (
    <div style={{ textAlign: "center", padding: "40px 20px" }}>
      <div style={{
        width: 48, height: 48, borderRadius: 14, margin: "0 auto 12px",
        background: "var(--bg-surface)",
        display: "flex", alignItems: "center", justifyContent: "center",
      }}>
        <Icon size={22} style={{ color: "var(--text-muted)" }} />
      </div>
      <p style={{ fontSize: 14, fontWeight: 600, color: "var(--text-primary)", marginBottom: 4 }}>{title}</p>
      <p style={{ fontSize: 13, color: "var(--text-muted)", marginBottom: 14 }}>{desc}</p>
      <Link
        href={href}
        style={{
          display: "inline-flex", alignItems: "center", gap: 5,
          padding: "7px 16px", borderRadius: 10,
          background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
          color: "#fff", textDecoration: "none", fontSize: 13, fontWeight: 600,
        }}
      >
        {cta} <ArrowRight size={13} />
      </Link>
    </div>
  );
}

// ─── Status dot ──────────────────────────────────────────────────────────────

function StatusDot({ status }: { status: string }) {
  const color =
    status === "OPEN"        ? "var(--success)" :
    status === "HANDED_OFF"  ? "var(--warn)"    :
    "var(--text-muted)";
  return <span style={{ width: 7, height: 7, borderRadius: "50%", background: color, flexShrink: 0, display: "inline-block" }} />;
}

// ─── Dashboard Page ────────────────────────────────────────────────────────────

export default function DashboardPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();

  // Live counter SSE — pushes updated stats every 5 s from analytics-service
  useEffect(() => {
    if (!token) return;
    const es = new EventSource(`/v1/analytics/live`);
    es.addEventListener("stats", (e) => {
      try {
        const data = JSON.parse(e.data);
        qc.setQueryData(["analytics", "stats"], (prev: DashboardStats | undefined) =>
          prev ? { ...prev, ...data } : data
        );
      } catch {}
    });
    return () => es.close();
  }, [token, qc]);

  const { data: stats, isLoading: statsLoading } = useQuery<DashboardStats>({
    queryKey: ["analytics", "stats"],
    queryFn: () => api.get("/v1/analytics/stats", token),
    enabled: !!token,
    refetchInterval: 30_000,
  });

  const { data: bots, isLoading: botsLoading } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  const { data: recentConversations, isLoading: convsLoading } = useQuery<Conversation[]>({
    queryKey: ["conversations", "recent"],
    queryFn: () => api.get("/v1/conversations?limit=5", token),
    enabled: !!token,
    refetchInterval: 15_000,
  });

  const firstName = session?.user.name?.split(" ")[0] ?? null;

  const statCards = [
    {
      label: "Total Conversations",
      value: stats?.totalConversations ?? "—",
      icon: MessageSquare,
      color: "#6366F1",
    },
    {
      label: "Active Now",
      value: stats?.openConversations ?? "—",
      icon: Activity,
      color: "#10B981",
      delta: stats ? "Live" : undefined,
    },
    {
      label: "Awaiting Agent",
      value: stats?.handoffConversations ?? "—",
      icon: Zap,
      color: "#F59E0B",
    },
    {
      label: "Avg Response Time",
      value: stats?.p50ResponseMs
        ? `${(stats.p50ResponseMs / 1000).toFixed(1)}s`
        : "—",
      icon: Clock,
      color: "#3B82F6",
      delta: undefined,
    },
  ];

  return (
    <div style={{ padding: "28px 36px", maxWidth: 1200, width: "100%" }}>

      {/* Page header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 28 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px", color: "var(--text-primary)" }}>
            {firstName ? `Good to see you, ${firstName}` : "Dashboard"}
          </h1>
          <p style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 4 }}>
            Here's what's happening across your bots today.
          </p>
        </div>
        <Link
          href="/bots"
          style={{
            display: "flex", alignItems: "center", gap: 6,
            background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
            color: "#fff", textDecoration: "none",
            padding: "9px 18px", borderRadius: 10, fontSize: 13, fontWeight: 600,
            boxShadow: "0 2px 12px rgba(99,102,241,0.35)",
            transition: "transform 150ms ease",
          }}
        >
          <Plus size={15} />
          New Bot
        </Link>
      </div>

      {/* Stats grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 14, marginBottom: 24 }}>
        {statCards.map((card) => (
          <StatCard key={card.label} {...card} loading={statsLoading} />
        ))}
      </div>

      {/* Active bots mini-strip */}
      {!botsLoading && bots && bots.length > 0 && (
        <div
          style={{
            background: "var(--bg-panel)", border: "1px solid var(--border)",
            borderRadius: 14, padding: "14px 18px", marginBottom: 24,
            display: "flex", alignItems: "center", gap: 12, overflowX: "auto",
          }}
        >
          <p style={{ fontSize: 12, fontWeight: 600, color: "var(--text-muted)", flexShrink: 0 }}>
            BOTS
          </p>
          <div style={{ display: "flex", gap: 8, flexWrap: "nowrap" }}>
            {bots.slice(0, 6).map((bot) => (
              <Link
                key={bot.id}
                href={`/builder/${bot.id}`}
                style={{
                  display: "inline-flex", alignItems: "center", gap: 6,
                  padding: "5px 12px", borderRadius: 20,
                  background: bot.status === "ACTIVE" ? "rgba(99,102,241,0.1)" : "var(--bg-surface)",
                  border: `1px solid ${bot.status === "ACTIVE" ? "rgba(99,102,241,0.25)" : "var(--border)"}`,
                  textDecoration: "none", fontSize: 12, fontWeight: 500,
                  color: bot.status === "ACTIVE" ? "#818CF8" : "var(--text-secondary)",
                  whiteSpace: "nowrap", flexShrink: 0,
                }}
              >
                <span style={{ width: 6, height: 6, borderRadius: "50%", background: bot.status === "ACTIVE" ? "#10B981" : "var(--text-muted)", flexShrink: 0 }} />
                {bot.name}
              </Link>
            ))}
            {bots.length > 6 && (
              <Link href="/bots" style={{
                display: "inline-flex", alignItems: "center",
                padding: "5px 12px", borderRadius: 20,
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                textDecoration: "none", fontSize: 12, color: "var(--text-muted)", whiteSpace: "nowrap",
              }}>
                +{bots.length - 6} more
              </Link>
            )}
          </div>
        </div>
      )}

      {/* Two-column content */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>

        {/* Bots panel */}
        <div style={{ background: "var(--bg-panel)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "16px 20px", borderBottom: "1px solid var(--border)" }}>
            <h2 style={{ fontSize: 14, fontWeight: 600, color: "var(--text-primary)" }}>Your Bots</h2>
            <Link
              href="/bots"
              style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 12, color: "var(--accent)", textDecoration: "none", fontWeight: 500 }}
            >
              View all <ArrowRight size={12} />
            </Link>
          </div>

          {botsLoading ? (
            <div style={{ padding: "20px" }}>
              {[...Array(3)].map((_, i) => (
                <div key={i} style={{ height: 52, background: "var(--bg-surface)", borderRadius: 10, marginBottom: 8 }} />
              ))}
            </div>
          ) : !bots?.length ? (
            <EmptyState
              icon={BotIcon}
              title="No bots yet"
              desc="Create your first bot and embed it on your website in minutes."
              href="/bots"
              cta="Create first bot"
            />
          ) : (
            <div style={{ padding: "10px" }}>
              {bots.slice(0, 5).map((bot) => (
                <Link
                  key={bot.id}
                  href={`/builder/${bot.id}`}
                  style={{
                    display: "flex", alignItems: "center", gap: 12,
                    padding: "10px 12px", borderRadius: 10,
                    textDecoration: "none", color: "var(--text-primary)",
                    transition: "background 150ms ease",
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = "var(--bg-surface)")}
                  onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
                >
                  <div style={{
                    width: 36, height: 36, borderRadius: 10, flexShrink: 0,
                    background: bot.accentColor ?? "linear-gradient(135deg, #6366F1, #8B5CF6)",
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 14, fontWeight: 700, color: "#fff",
                  }}>
                    {bot.name[0]}
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: 13, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {bot.name}
                    </p>
                    {bot.description && (
                      <p style={{ fontSize: 11, color: "var(--text-muted)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {bot.description}
                      </p>
                    )}
                  </div>
                  <span style={{
                    fontSize: 10, fontWeight: 600, padding: "2px 8px", borderRadius: 20,
                    background: bot.status === "ACTIVE" ? "rgba(16,185,129,0.12)" : "var(--bg-surface)",
                    color: bot.status === "ACTIVE" ? "var(--success)" : "var(--text-muted)",
                    border: `1px solid ${bot.status === "ACTIVE" ? "rgba(16,185,129,0.25)" : "var(--border)"}`,
                    flexShrink: 0,
                  }}>
                    {bot.status === "ACTIVE" ? "Active" : "Draft"}
                  </span>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Recent conversations panel */}
        <div style={{ background: "var(--bg-panel)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "16px 20px", borderBottom: "1px solid var(--border)" }}>
            <h2 style={{ fontSize: 14, fontWeight: 600, color: "var(--text-primary)" }}>Recent Conversations</h2>
            <Link
              href="/conversations"
              style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 12, color: "var(--accent)", textDecoration: "none", fontWeight: 500 }}
            >
              View all <ArrowRight size={12} />
            </Link>
          </div>

          {convsLoading ? (
            <div style={{ padding: "20px" }}>
              {[...Array(4)].map((_, i) => (
                <div key={i} style={{ height: 44, background: "var(--bg-surface)", borderRadius: 10, marginBottom: 8 }} />
              ))}
            </div>
          ) : !recentConversations?.length ? (
            <EmptyState
              icon={MessageSquare}
              title="No conversations yet"
              desc="Conversations will appear here once your bot is live."
              href="/bots"
              cta="Activate a bot"
            />
          ) : (
            <div style={{ padding: "10px" }}>
              {recentConversations.map((conv) => (
                <Link
                  key={conv.id}
                  href={`/conversations?id=${conv.id}`}
                  style={{
                    display: "flex", alignItems: "center", gap: 12,
                    padding: "10px 12px", borderRadius: 10,
                    textDecoration: "none", color: "var(--text-primary)",
                    transition: "background 150ms ease",
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = "var(--bg-surface)")}
                  onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
                >
                  <StatusDot status={conv.status} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: 13, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", color: "var(--text-primary)" }}>
                      {conv.lastMessage ?? "Started conversation"}
                    </p>
                    <p style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 1 }}>
                      {conv.status === "OPEN"
                        ? "Open"
                        : conv.status === "HANDED_OFF"
                        ? "Needs agent"
                        : "Closed"}
                    </p>
                  </div>
                  <span style={{ fontSize: 11, color: "var(--text-muted)", flexShrink: 0 }}>
                    {formatRelative(conv.updatedAt)}
                  </span>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Quick actions row */}
      <div style={{ marginTop: 20, display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 14 }}>
        {[
          { href: "/templates", icon: TrendingUp, title: "Browse templates", desc: "Start from a pre-built flow" },
          { href: "/integrations", icon: Zap, title: "Connect channels", desc: "WhatsApp, Instagram, Telegram" },
          { href: "/settings/api-keys", icon: MessageSquare, title: "Get embed snippet", desc: "Add widget to your website" },
        ].map((item) => (
          <Link
            key={item.href}
            href={item.href}
            style={{
              display: "flex", alignItems: "center", gap: 12,
              padding: "14px 16px", borderRadius: 12,
              background: "var(--bg-panel)", border: "1px solid var(--border)",
              textDecoration: "none", transition: "border-color 150ms ease",
            }}
            onMouseEnter={(e) => (e.currentTarget.style.borderColor = "var(--accent)")}
            onMouseLeave={(e) => (e.currentTarget.style.borderColor = "var(--border)")}
          >
            <div style={{
              width: 36, height: 36, borderRadius: 10, flexShrink: 0,
              background: "var(--accent-light)",
              display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              <item.icon size={16} style={{ color: "var(--accent)" }} />
            </div>
            <div>
              <p style={{ fontSize: 13, fontWeight: 600, color: "var(--text-primary)" }}>{item.title}</p>
              <p style={{ fontSize: 11, color: "var(--text-muted)" }}>{item.desc}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
