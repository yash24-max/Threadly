"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "@/lib/api";
import type { Bot } from "@/lib/types";
import { Copy, Check } from "lucide-react";

export default function SettingsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const [copied, setCopied] = useState<string | null>(null);

  const { data: bots } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  const [selectedBotId, setSelectedBotId] = useState<string>("");
  const [embedSnippet, setEmbedSnippet] = useState<string>("");

  const getEmbed = useMutation({
    mutationFn: (botId: string) => api.get<{ snippet: string }>(`/v1/bots/${botId}/embed`, token),
    onSuccess: (data) => setEmbedSnippet(data.snippet),
  });

  const copy = async (text: string, key: string) => {
    await navigator.clipboard.writeText(text);
    setCopied(key);
    setTimeout(() => setCopied(null), 2000);
  };

  return (
    <div style={{ padding: "32px 40px", maxWidth: 760 }}>
      <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.5px", marginBottom: 8 }}>Settings</h1>
      <p style={{ color: "var(--text-secondary)", fontSize: 14, marginBottom: 32 }}>
        Workspace configuration and embed snippets.
      </p>

      {/* Workspace info */}
      <section style={{ marginBottom: 36 }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Workspace</h2>
        <div style={{
          background: "var(--bg-panel)", border: "1px solid var(--border)",
          borderRadius: "var(--radius-lg)", padding: 20,
          display: "flex", flexDirection: "column", gap: 14,
        }}>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Org name</span>
            <span style={{ fontSize: 14, fontWeight: 500 }}>{session?.user.orgName}</span>
          </div>
          <div style={{ height: 1, background: "var(--border)" }} />
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Slug</span>
            <span style={{ fontSize: 14, fontWeight: 500 }}>{session?.user.orgSlug}</span>
          </div>
          <div style={{ height: 1, background: "var(--border)" }} />
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Your email</span>
            <span style={{ fontSize: 14, fontWeight: 500 }}>{session?.user.email}</span>
          </div>
          <div style={{ height: 1, background: "var(--border)" }} />
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Role</span>
            <span style={{
              fontSize: 12, fontWeight: 500, padding: "3px 10px",
              borderRadius: "var(--radius-full)",
              background: "rgba(79,70,229,0.15)", color: "var(--accent)",
            }}>
              {session?.user.role}
            </span>
          </div>
        </div>
      </section>

      {/* Embed snippet */}
      <section>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 6 }}>Embed Widget</h2>
        <p style={{ fontSize: 14, color: "var(--text-secondary)", marginBottom: 16 }}>
          Add the chat widget to any website with a single script tag.
        </p>

        <div style={{
          background: "var(--bg-panel)", border: "1px solid var(--border)",
          borderRadius: "var(--radius-lg)", padding: 20, display: "flex", flexDirection: "column", gap: 16,
        }}>
          <div style={{ display: "flex", gap: 10 }}>
            <select
              value={selectedBotId}
              onChange={(e) => setSelectedBotId(e.target.value)}
              style={{
                flex: 1, padding: "9px 12px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: selectedBotId ? "var(--text-primary)" : "var(--text-muted)",
                fontSize: 14, outline: "none",
              }}
            >
              <option value="">Select a bot…</option>
              {bots?.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
            <button
              onClick={() => selectedBotId && getEmbed.mutate(selectedBotId)}
              disabled={!selectedBotId || getEmbed.isPending}
              style={{
                padding: "9px 18px", borderRadius: "var(--radius-md)",
                background: "var(--accent)", color: "var(--accent-fg)",
                border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
              }}
            >
              Get snippet
            </button>
          </div>

          {embedSnippet && (
            <div style={{ position: "relative" }}>
              <pre style={{
                background: "var(--bg-canvas)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", padding: 16,
                fontSize: 13, overflow: "auto", whiteSpace: "pre-wrap", wordBreak: "break-all",
                color: "var(--text-secondary)", lineHeight: 1.6,
              }}>
                {embedSnippet}
              </pre>
              <button
                onClick={() => copy(embedSnippet, "snippet")}
                style={{
                  position: "absolute", top: 10, right: 10,
                  display: "flex", alignItems: "center", gap: 5,
                  padding: "5px 10px", borderRadius: "var(--radius-sm)",
                  background: "var(--bg-surface)", border: "1px solid var(--border)",
                  color: copied === "snippet" ? "var(--success)" : "var(--text-secondary)",
                  cursor: "pointer", fontSize: 12,
                }}
              >
                {copied === "snippet" ? <Check size={12} /> : <Copy size={12} />}
                {copied === "snippet" ? "Copied!" : "Copy"}
              </button>
            </div>
          )}

          <p style={{ fontSize: 12, color: "var(--text-muted)" }}>
            Paste this snippet just before the <code>&lt;/body&gt;</code> tag on your website.
          </p>
        </div>
      </section>
    </div>
  );
}
