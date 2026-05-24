"use client";

import { useSession } from "next-auth/react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { DashboardStats, Conversation, Bot } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import { MessageSquare, Bot as BotIcon, Clock, TrendingUp, Plus, ArrowRight } from "lucide-react";

export default function DashboardPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const [liveStats, setLiveStats] = useState<Partial<DashboardStats>>({});

  // SSE live counter updates
  useEffect(() => {
    if (!token) return;
    const es = new EventSource(
      `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/analytics/live`,
      // Note: EventSource doesn't natively support Authorization headers.
      // Production: use cookie-based auth or pass token as query param.
    );
    es.addEventListener("stats", (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data);
        setLiveStats(data);
        qc.setQueryData(["analytics", "stats"], (old: DashboardStats | undefined) => old ? { ...old, ...data } : data);
      } catch { /* ignore parse errors */ }
    });
    return () => es.close();
  }, [token]);

  const { data: stats } = useQuery<DashboardStats>({
    queryKey: ["analytics", "stats"],
    queryFn: () => api.get("/v1/analytics/stats", token),
    enabled: !!token,
  });

  const { data: bots } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  const { data: recentConversations } = useQuery<Conversation[]>({
    queryKey: ["conversations", "recent"],
    queryFn: () => api.get("/v1/conversations?limit=5", token),
    enabled: !!token,
    refetchInterval: 15_000,
  });

  const statCards = [
    {
      label: "Total Conversations",
      value: stats?.totalConversations ?? "—",
      icon: MessageSquare,
      color: "var(--accent)",
    },
    {
      label: "Open Now",
      value: stats?.openConversations ?? "—",
      icon: TrendingUp,
      color: "var(--success)",
    },
    {
      label: "Awaiting Agent",
      value: stats?.handoffConversations ?? "—",
      icon: BotIcon,
      color: "var(--warn)",
    },
    {
      label: "Avg Response",
      value: stats?.p50ResponseMs ? `${(stats.p50ResponseMs / 1000).toFixed(1)}s` : "—",
      icon: Clock,
      color: "var(--info)",
    },
  ];

  return (
    <div style={{ padding: "32px 40px", maxWidth: 1200 }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 32 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.5px" }}>
            Good to see you{session?.user.name ? `, ${session.user.name.split(" ")[0]}` : ""}
          </h1>
          <p style={{ color: "var(--text-secondary)", marginTop: 4, fontSize: 14 }}>
            Here's what's happening across your bots today.
          </p>
        </div>
        <Link
          href="/bots"
          style={{
            display: "flex", alignItems: "center", gap: 6,
            background: "var(--accent)", color: "var(--accent-fg)",
            textDecoration: "none", padding: "9px 18px",
            borderRadius: "var(--radius-md)", fontSize: 14, fontWeight: 500,
          }}
        >
          <Plus size={16} />
          New Bot
        </Link>
      </div>

      {/* Stats grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16, marginBottom: 32 }}>
        {statCards.map(({ label, value, icon: Icon, color }) => (
          <div
            key={label}
            style={{
              background: "var(--bg-panel)", border: "1px solid var(--border)",
              borderRadius: "var(--radius-lg)", padding: "20px 22px",
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <p style={{ fontSize: 13, color: "var(--text-secondary)" }}>{label}</p>
              <div
                style={{
                  width: 32, height: 32, borderRadius: "var(--radius-sm)",
                  background: color + "20", display: "flex", alignItems: "center", justifyContent: "center",
                }}
              >
                <Icon size={16} color={color} />
              </div>
            </div>
            <p style={{ fontSize: 28, fontWeight: 700, marginTop: 8, letterSpacing: "-1px" }}>{value}</p>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24 }}>
        {/* Bots */}
        <div
          style={{
            background: "var(--bg-panel)", border: "1px solid var(--border)",
            borderRadius: "var(--radius-lg)", padding: 24,
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
            <h2 style={{ fontSize: 16, fontWeight: 600 }}>Your Bots</h2>
            <Link href="/bots" style={{ fontSize: 13, color: "var(--accent)", textDecoration: "none", display: "flex", alignItems: "center", gap: 4 }}>
              View all <ArrowRight size={13} />
            </Link>
          </div>
          {!bots?.length ? (
            <div style={{
              textAlign: "center", padding: "32px 0",
              color: "var(--text-muted)", fontSize: 14,
            }}>
              <BotIcon size={32} style={{ margin: "0 auto 12px", opacity: 0.3 }} />
              <p>No bots yet.</p>
              <Link href="/bots" style={{ color: "var(--accent)", textDecoration: "none", fontSize: 13, marginTop: 8, display: "inline-block" }}>
                Create your first bot →
              </Link>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {bots.slice(0, 4).map((bot) => (
                <Link
                  key={bot.id}
                  href={`/builder/${bot.id}`}
                  style={{
                    display: "flex", alignItems: "center", gap: 12,
                    padding: "10px 12px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", textDecoration: "none",
                    color: "var(--text-primary)", border: "1px solid transparent",
                  }}
                >
                  <div style={{
                    width: 32, height: 32, borderRadius: "var(--radius-md)",
                    background: bot.accentColor || "var(--accent)",
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 14, fontWeight: 700, color: "#fff", flexShrink: 0,
                  }}>
                    {bot.name[0]}
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: 14, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{bot.name}</p>
                    <p style={{
                      fontSize: 12, color: bot.status === "ACTIVE" ? "var(--success)" : "var(--text-muted)",
                    }}>
                      {bot.status === "ACTIVE" ? "Active" : "Inactive"}
                    </p>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Recent conversations */}
        <div
          style={{
            background: "var(--bg-panel)", border: "1px solid var(--border)",
            borderRadius: "var(--radius-lg)", padding: 24,
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
            <h2 style={{ fontSize: 16, fontWeight: 600 }}>Recent Conversations</h2>
            <Link href="/conversations" style={{ fontSize: 13, color: "var(--accent)", textDecoration: "none", display: "flex", alignItems: "center", gap: 4 }}>
              View all <ArrowRight size={13} />
            </Link>
          </div>
          {!recentConversations?.length ? (
            <div style={{ textAlign: "center", padding: "32px 0", color: "var(--text-muted)", fontSize: 14 }}>
              <MessageSquare size={32} style={{ margin: "0 auto 12px", opacity: 0.3 }} />
              <p>No conversations yet.</p>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              {recentConversations.map((conv) => (
                <Link
                  key={conv.id}
                  href={`/conversations?id=${conv.id}`}
                  style={{
                    display: "flex", alignItems: "center", gap: 12,
                    padding: "10px 12px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", textDecoration: "none",
                    color: "var(--text-primary)",
                  }}
                >
                  <div style={{
                    width: 8, height: 8, borderRadius: "50%", flexShrink: 0,
                    background: conv.status === "OPEN" ? "var(--success)"
                      : conv.status === "HANDED_OFF" ? "var(--warn)" : "var(--text-muted)",
                  }} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: 13, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {conv.lastMessage ?? "Started conversation"}
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
    </div>
  );
}
