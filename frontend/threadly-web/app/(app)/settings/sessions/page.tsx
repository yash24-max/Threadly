"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "@/lib/api";
import {
  Monitor, Smartphone, Globe, Loader2, ShieldCheck, LogOut, RefreshCw,
} from "lucide-react";

interface KeycloakSession {
  id:          string;
  userId:      string;
  start:       number; // epoch ms
  lastAccess:  number; // epoch ms
  ipAddress:   string;
  clients:     Record<string, string>; // clientId → clientName
  username?:   string;
}

function formatRelative(epochMs: number): string {
  const diff = Math.floor((Date.now() - epochMs) / 1000);
  if (diff < 60)  return "just now";
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return `${Math.floor(diff / 86400)}d ago`;
}

function formatDate(epochMs: number): string {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium", timeStyle: "short",
  }).format(new Date(epochMs));
}

function DeviceIcon({ clients }: { clients: Record<string, string> }) {
  const names = Object.values(clients).join(" ").toLowerCase();
  if (names.includes("mobile") || names.includes("android") || names.includes("ios")) {
    return <Smartphone size={16} style={{ color: "var(--accent)" }} />;
  }
  if (names.includes("web") || names.includes("app")) {
    return <Monitor size={16} style={{ color: "var(--accent)" }} />;
  }
  return <Globe size={16} style={{ color: "var(--text-muted)" }} />;
}

export default function SessionsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const [revoking, setRevoking] = useState<string | null>(null);

  const { data: sessions, isLoading, isError, refetch, isFetching } = useQuery<KeycloakSession[]>({
    queryKey: ["my-sessions"],
    queryFn: () => api.get("/v1/me/sessions", token),
    enabled: !!token,
    staleTime: 30_000,
  });

  const revoke = useMutation({
    mutationFn: (sessionId: string) => api.delete(`/v1/me/sessions/${sessionId}`, token),
    onMutate: (id) => setRevoking(id),
    onSettled: () => {
      setRevoking(null);
      qc.invalidateQueries({ queryKey: ["my-sessions"] });
    },
  });

  // Keycloak doesn't directly expose "current session" in the list response,
  // so we mark the most-recently-accessed session as current.
  const currentSessionId = sessions?.reduce<KeycloakSession | null>(
    (latest, s) => (!latest || s.lastAccess > latest.lastAccess) ? s : latest,
    null
  )?.id;

  return (
    <div style={{ padding: "28px 32px", maxWidth: 720 }}>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <ShieldCheck size={20} style={{ color: "var(--accent)" }} />
          <div>
            <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 2 }}>Active Sessions</h1>
            <p style={{ fontSize: 13, color: "var(--text-secondary)", marginTop: 0 }}>
              Manage where you&apos;re signed in. Revoke any session you don&apos;t recognise.
            </p>
          </div>
        </div>
        <button
          onClick={() => refetch()}
          disabled={isFetching}
          title="Refresh"
          style={{
            display: "flex", alignItems: "center", gap: 6,
            padding: "7px 12px", borderRadius: "var(--radius-md)",
            background: "var(--bg-surface)", border: "1px solid var(--border)",
            color: "var(--text-secondary)", cursor: "pointer", fontSize: 13,
          }}
        >
          <RefreshCw size={13} style={{ animation: isFetching ? "spin 1s linear infinite" : "none" }} />
          Refresh
        </button>
      </div>

      {/* Content */}
      {isLoading ? (
        <div style={{ display: "flex", justifyContent: "center", padding: "60px 0" }}>
          <Loader2 size={22} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
        </div>
      ) : isError ? (
        <div style={{
          padding: "20px 24px", borderRadius: "var(--radius-lg)",
          background: "rgba(239,68,68,0.08)", border: "1px solid rgba(239,68,68,0.2)",
          fontSize: 14, color: "#FCA5A5",
        }}>
          Failed to load sessions. The identity service may be unavailable.
        </div>
      ) : !sessions || sessions.length === 0 ? (
        <p style={{ color: "var(--text-muted)", fontSize: 14, padding: "40px 0", textAlign: "center" }}>
          No active sessions found.
        </p>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {/* Sort: current first, then by last access desc */}
          {[...sessions]
            .sort((a, b) => {
              if (a.id === currentSessionId) return -1;
              if (b.id === currentSessionId) return 1;
              return b.lastAccess - a.lastAccess;
            })
            .map((s) => {
              const isCurrent   = s.id === currentSessionId;
              const isRevoking  = revoking === s.id;
              const clientNames = Object.values(s.clients).join(", ") || "Unknown client";

              return (
                <div
                  key={s.id}
                  style={{
                    display: "flex", alignItems: "center", gap: 16,
                    padding: "16px 20px",
                    background: isCurrent
                      ? "linear-gradient(135deg, rgba(99,102,241,0.08), rgba(139,92,246,0.04))"
                      : "var(--bg-panel)",
                    border: `1px solid ${isCurrent ? "rgba(99,102,241,0.25)" : "var(--border)"}`,
                    borderRadius: "var(--radius-lg)",
                  }}
                >
                  {/* Icon */}
                  <div style={{
                    width: 40, height: 40, borderRadius: 10, flexShrink: 0,
                    background: "var(--bg-surface)", border: "1px solid var(--border)",
                    display: "flex", alignItems: "center", justifyContent: "center",
                  }}>
                    <DeviceIcon clients={s.clients} />
                  </div>

                  {/* Details */}
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 3 }}>
                      <span style={{ fontSize: 14, fontWeight: 600, color: "var(--text-primary)" }}>
                        {clientNames}
                      </span>
                      {isCurrent && (
                        <span style={{
                          fontSize: 11, fontWeight: 600, padding: "2px 8px",
                          borderRadius: "var(--radius-full)",
                          background: "rgba(99,102,241,0.15)", color: "var(--accent)",
                        }}>
                          Current
                        </span>
                      )}
                    </div>
                    <div style={{ display: "flex", gap: 16, flexWrap: "wrap" }}>
                      <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                        IP: {s.ipAddress}
                      </span>
                      <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                        Started: {formatDate(s.start)}
                      </span>
                      <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                        Last active: {formatRelative(s.lastAccess)}
                      </span>
                    </div>
                  </div>

                  {/* Revoke */}
                  {!isCurrent && (
                    <button
                      onClick={() => revoke.mutate(s.id)}
                      disabled={!!revoking}
                      title="Revoke this session"
                      style={{
                        display: "flex", alignItems: "center", gap: 5,
                        padding: "7px 12px", borderRadius: "var(--radius-md)",
                        background: "transparent",
                        border: "1px solid rgba(239,68,68,0.3)",
                        color: isRevoking ? "var(--text-muted)" : "#F87171",
                        cursor: revoking ? "not-allowed" : "pointer",
                        fontSize: 12, fontWeight: 500,
                        flexShrink: 0,
                        transition: "all 150ms ease",
                      }}
                      onMouseEnter={e => { if (!revoking) e.currentTarget.style.background = "rgba(239,68,68,0.08)"; }}
                      onMouseLeave={e => { e.currentTarget.style.background = "transparent"; }}
                    >
                      {isRevoking
                        ? <Loader2 size={12} style={{ animation: "spin 1s linear infinite" }} />
                        : <LogOut size={12} />}
                      Revoke
                    </button>
                  )}
                </div>
              );
            })}
        </div>
      )}

      {sessions && sessions.length > 1 && (
        <div style={{ marginTop: 20, paddingTop: 20, borderTop: "1px solid var(--border)" }}>
          <button
            onClick={() => {
              const others = sessions.filter(s => s.id !== currentSessionId);
              others.forEach(s => revoke.mutate(s.id));
            }}
            disabled={!!revoking}
            style={{
              display: "flex", alignItems: "center", gap: 6,
              padding: "8px 16px", borderRadius: "var(--radius-md)",
              background: "transparent",
              border: "1px solid rgba(239,68,68,0.35)",
              color: "#F87171", cursor: "pointer", fontSize: 13, fontWeight: 500,
            }}
          >
            <LogOut size={14} />
            Sign out all other sessions
          </button>
        </div>
      )}
    </div>
  );
}
