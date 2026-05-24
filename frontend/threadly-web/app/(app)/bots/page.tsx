"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import type { Bot } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import { Plus, Pencil, Copy, Trash2, Bot as BotIcon, Loader2 } from "lucide-react";

export default function BotsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();

  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const { data: bots, isLoading } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  const create = useMutation({
    mutationFn: () => api.post("/v1/bots", { name, description }, token),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bots"] });
      setShowCreate(false);
      setName("");
      setDescription("");
    },
  });

  const deleteBot = useMutation({
    mutationFn: (id: string) => api.delete(`/v1/bots/${id}`, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["bots"] }),
  });

  return (
    <div style={{ padding: "32px 40px", maxWidth: 1100 }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 28 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.5px" }}>Bots</h1>
          <p style={{ color: "var(--text-secondary)", fontSize: 14, marginTop: 4 }}>
            Manage your AI chatbots.
          </p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          style={{
            display: "flex", alignItems: "center", gap: 6,
            background: "var(--accent)", color: "var(--accent-fg)",
            border: "none", padding: "9px 18px",
            borderRadius: "var(--radius-md)", fontSize: 14, fontWeight: 500, cursor: "pointer",
          }}
        >
          <Plus size={16} />
          New Bot
        </button>
      </div>

      {/* Create modal */}
      {showCreate && (
        <div style={{
          position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)",
          display: "flex", alignItems: "center", justifyContent: "center",
          zIndex: 1000,
        }}>
          <div style={{
            background: "var(--bg-panel)", border: "1px solid var(--border)",
            borderRadius: "var(--radius-xl)", padding: 32, width: 420,
            boxShadow: "var(--shadow-3)",
          }}>
            <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 20 }}>Create new bot</h2>
            <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              <div>
                <label style={{ fontSize: 13, color: "var(--text-secondary)", display: "block", marginBottom: 6 }}>
                  Bot name *
                </label>
                <input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Customer Support Bot"
                  style={{
                    width: "100%", padding: "9px 12px",
                    background: "var(--bg-surface)", border: "1px solid var(--border)",
                    borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                    fontSize: 14, outline: "none", boxSizing: "border-box",
                  }}
                />
              </div>
              <div>
                <label style={{ fontSize: 13, color: "var(--text-secondary)", display: "block", marginBottom: 6 }}>
                  Description
                </label>
                <input
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Handles tier-1 support questions"
                  style={{
                    width: "100%", padding: "9px 12px",
                    background: "var(--bg-surface)", border: "1px solid var(--border)",
                    borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                    fontSize: 14, outline: "none", boxSizing: "border-box",
                  }}
                />
              </div>
              <div style={{ display: "flex", gap: 10, justifyContent: "flex-end", marginTop: 4 }}>
                <button
                  onClick={() => setShowCreate(false)}
                  style={{
                    padding: "9px 18px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", border: "1px solid var(--border)",
                    color: "var(--text-secondary)", cursor: "pointer", fontSize: 14,
                  }}
                >
                  Cancel
                </button>
                <button
                  onClick={() => create.mutate()}
                  disabled={!name.trim() || create.isPending}
                  style={{
                    padding: "9px 18px", borderRadius: "var(--radius-md)",
                    background: "var(--accent)", color: "var(--accent-fg)",
                    border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
                  }}
                >
                  {create.isPending ? "Creating…" : "Create"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Bot grid */}
      {isLoading ? (
        <div style={{ display: "flex", justifyContent: "center", paddingTop: 80 }}>
          <Loader2 size={24} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
        </div>
      ) : !bots?.length ? (
        <div style={{
          textAlign: "center", padding: "80px 0",
          border: "2px dashed var(--border)", borderRadius: "var(--radius-xl)",
        }}>
          <BotIcon size={40} style={{ margin: "0 auto 16px", color: "var(--text-muted)" }} />
          <h3 style={{ fontSize: 17, fontWeight: 500, marginBottom: 8 }}>No bots yet</h3>
          <p style={{ color: "var(--text-secondary)", fontSize: 14, marginBottom: 20 }}>
            Create your first bot and embed it on your website in minutes.
          </p>
          <button
            onClick={() => setShowCreate(true)}
            style={{
              display: "inline-flex", alignItems: "center", gap: 6,
              background: "var(--accent)", color: "var(--accent-fg)",
              border: "none", padding: "10px 20px",
              borderRadius: "var(--radius-md)", fontSize: 14, fontWeight: 500, cursor: "pointer",
            }}
          >
            <Plus size={16} />
            Create first bot
          </button>
        </div>
      ) : (
        <div style={{
          display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 20,
        }}>
          {bots.map((bot) => (
            <div
              key={bot.id}
              style={{
                background: "var(--bg-panel)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-lg)", padding: 20,
                display: "flex", flexDirection: "column", gap: 12,
              }}
            >
              <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
                <div style={{
                  width: 40, height: 40, borderRadius: "var(--radius-md)",
                  background: bot.accentColor || "var(--accent)",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 18, fontWeight: 700, color: "#fff", flexShrink: 0,
                }}>
                  {bot.name[0]}
                </div>
                <div style={{ flex: 1 }}>
                  <h3 style={{ fontSize: 15, fontWeight: 600 }}>{bot.name}</h3>
                  {bot.description && (
                    <p style={{ fontSize: 13, color: "var(--text-secondary)", marginTop: 2 }}>{bot.description}</p>
                  )}
                </div>
                <span style={{
                  fontSize: 11, fontWeight: 500, padding: "3px 8px",
                  borderRadius: "var(--radius-full)",
                  background: bot.status === "ACTIVE" ? "rgba(34,197,94,0.15)" : "var(--bg-surface)",
                  color: bot.status === "ACTIVE" ? "var(--success)" : "var(--text-muted)",
                }}>
                  {bot.status === "ACTIVE" ? "Active" : "Inactive"}
                </span>
              </div>

              <p style={{ fontSize: 12, color: "var(--text-muted)" }}>
                Updated {formatRelative(bot.updatedAt)}
              </p>

              <div style={{ display: "flex", gap: 8, borderTop: "1px solid var(--border)", paddingTop: 12 }}>
                <Link
                  href={`/builder/${bot.id}`}
                  style={{
                    flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: 6,
                    padding: "8px", borderRadius: "var(--radius-md)",
                    background: "var(--accent)", color: "var(--accent-fg)",
                    textDecoration: "none", fontSize: 13, fontWeight: 500,
                  }}
                >
                  <Pencil size={13} />
                  Edit Flow
                </Link>
                <Link
                  href={`/knowledge/${bot.id}`}
                  style={{
                    padding: "8px 12px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", color: "var(--text-secondary)",
                    textDecoration: "none", fontSize: 13, border: "1px solid var(--border)",
                  }}
                >
                  KB
                </Link>
                <button
                  onClick={() => {
                    if (confirm(`Delete bot "${bot.name}"?`)) deleteBot.mutate(bot.id);
                  }}
                  style={{
                    padding: "8px 10px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", color: "var(--danger)",
                    border: "1px solid var(--border)", cursor: "pointer",
                  }}
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
