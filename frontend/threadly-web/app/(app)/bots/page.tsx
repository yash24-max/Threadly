"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import type { Bot } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import { Plus, Pencil, Trash2, Bot as BotIcon, Loader2, Database, BarChart3, ExternalLink, Search, X } from "lucide-react";
import { toast } from "sonner";

// ─── Create Bot Modal ─────────────────────────────────────────────────────────

function CreateBotModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const create = useMutation({
    mutationFn: () => api.post("/v1/bots", { name, description }, token),
    onSuccess: () => {
      toast.success("Bot created!");
      onCreated();
      onClose();
    },
    onError: () => toast.error("Failed to create bot."),
  });

  return (
    <div
      style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000, backdropFilter: "blur(4px)" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 18, padding: 28, width: 440, boxShadow: "0 20px 60px rgba(0,0,0,0.5)",
      }}>
        {/* Header */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 22 }}>
          <div>
            <h2 style={{ fontSize: 17, fontWeight: 700, color: "var(--text-primary)" }}>Create new bot</h2>
            <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>
              Add a name and start building in the flow editor.
            </p>
          </div>
          <button
            onClick={onClose}
            style={{ background: "var(--bg-surface)", border: "1px solid var(--border)", borderRadius: 8, padding: "5px 7px", cursor: "pointer", color: "var(--text-muted)" }}
          >
            <X size={14} />
          </button>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          <div>
            <label style={{ display: "block", fontSize: 12, fontWeight: 500, color: "var(--text-secondary)", marginBottom: 6 }}>
              Bot name <span style={{ color: "var(--danger)" }}>*</span>
            </label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
              placeholder="e.g. Customer Support Bot"
              style={{
                width: "100%", padding: "10px 14px", boxSizing: "border-box",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: 10, color: "var(--text-primary)", fontSize: 14,
                outline: "none", transition: "border-color 150ms ease",
              }}
              onFocus={(e) => (e.target.style.borderColor = "var(--accent)")}
              onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
            />
          </div>
          <div>
            <label style={{ display: "block", fontSize: 12, fontWeight: 500, color: "var(--text-secondary)", marginBottom: 6 }}>
              Description <span style={{ color: "var(--text-muted)" }}>(optional)</span>
            </label>
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Handles tier-1 support questions"
              style={{
                width: "100%", padding: "10px 14px", boxSizing: "border-box",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: 10, color: "var(--text-primary)", fontSize: 14,
                outline: "none", transition: "border-color 150ms ease",
              }}
              onFocus={(e) => (e.target.style.borderColor = "var(--accent)")}
              onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
            />
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 6 }}>
            <button
              onClick={onClose}
              style={{
                flex: 1, padding: "10px", borderRadius: 10,
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                color: "var(--text-secondary)", cursor: "pointer", fontSize: 13, fontWeight: 500,
              }}
            >
              Cancel
            </button>
            <button
              onClick={() => create.mutate()}
              disabled={!name.trim() || create.isPending}
              style={{
                flex: 2, display: "flex", alignItems: "center", justifyContent: "center", gap: 6,
                padding: "10px", borderRadius: 10,
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                color: "#fff", border: "none", cursor: "pointer", fontSize: 13, fontWeight: 600,
                opacity: !name.trim() ? 0.5 : 1,
              }}
            >
              {create.isPending && <Loader2 size={13} style={{ animation: "spin 1s linear infinite" }} />}
              {create.isPending ? "Creating…" : "Create Bot"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Bot card ─────────────────────────────────────────────────────────────────

function BotCard({ bot, onDelete }: { bot: Bot; onDelete: (id: string) => void }) {
  const accentColors = ["#6366F1", "#8B5CF6", "#06B6D4", "#10B981", "#F59E0B", "#EF4444"];
  const color = bot.accentColor ?? accentColors[bot.name.charCodeAt(0) % accentColors.length];

  return (
    <div
      style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 16, padding: 20, display: "flex", flexDirection: "column", gap: 14,
        transition: "border-color 200ms ease, transform 200ms ease",
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--border-strong)";
        (e.currentTarget as HTMLElement).style.transform = "translateY(-1px)";
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--border)";
        (e.currentTarget as HTMLElement).style.transform = "none";
      }}
    >
      {/* Header row */}
      <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
        <div style={{
          width: 44, height: 44, borderRadius: 12, flexShrink: 0,
          background: `linear-gradient(135deg, ${color}, ${color}bb)`,
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: 18, fontWeight: 700, color: "#fff",
          boxShadow: `0 4px 14px ${color}40`,
        }}>
          {bot.name[0]}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <h3 style={{ fontSize: 14, fontWeight: 700, color: "var(--text-primary)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
            {bot.name}
          </h3>
          {bot.description && (
            <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {bot.description}
            </p>
          )}
        </div>
        <span style={{
          fontSize: 10, fontWeight: 700, padding: "3px 9px", borderRadius: 20, flexShrink: 0,
          background: bot.status === "ACTIVE" ? "rgba(16,185,129,0.12)" : "var(--bg-surface)",
          color: bot.status === "ACTIVE" ? "var(--success)" : "var(--text-muted)",
          border: `1px solid ${bot.status === "ACTIVE" ? "rgba(16,185,129,0.3)" : "var(--border)"}`,
          letterSpacing: "0.04em", textTransform: "uppercase",
        }}>
          {bot.status === "ACTIVE" ? "Live" : "Draft"}
        </span>
      </div>

      {/* Meta */}
      <p style={{ fontSize: 11, color: "var(--text-muted)" }}>
        Updated {formatRelative(bot.updatedAt)}
      </p>

      {/* Actions */}
      <div style={{ display: "flex", gap: 8, borderTop: "1px solid var(--border)", paddingTop: 14 }}>
        <Link
          href={`/builder/${bot.id}`}
          style={{
            flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: 5,
            padding: "8px", borderRadius: 10,
            background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
            color: "#fff", textDecoration: "none", fontSize: 12, fontWeight: 600,
          }}
        >
          <Pencil size={12} />
          Edit Flow
        </Link>

        <Link
          href={`/knowledge/${bot.id}`}
          title="Knowledge Base"
          style={{
            display: "flex", alignItems: "center", justifyContent: "center",
            padding: "8px 11px", borderRadius: 10,
            background: "var(--bg-surface)", border: "1px solid var(--border)",
            color: "var(--text-secondary)", textDecoration: "none",
            transition: "border-color 150ms ease, color 150ms ease",
          }}
          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--accent)"; (e.currentTarget as HTMLElement).style.color = "var(--accent)"; }}
          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.color = "var(--text-secondary)"; }}
        >
          <Database size={14} />
        </Link>

        <Link
          href={`/bots/${bot.id}/analytics`}
          title="Analytics"
          style={{
            display: "flex", alignItems: "center", justifyContent: "center",
            padding: "8px 11px", borderRadius: 10,
            background: "var(--bg-surface)", border: "1px solid var(--border)",
            color: "var(--text-secondary)", textDecoration: "none",
            transition: "border-color 150ms ease, color 150ms ease",
          }}
          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--accent)"; (e.currentTarget as HTMLElement).style.color = "var(--accent)"; }}
          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.color = "var(--text-secondary)"; }}
        >
          <BarChart3 size={14} />
        </Link>

        <button
          onClick={() => { if (confirm(`Delete "${bot.name}"? This cannot be undone.`)) onDelete(bot.id); }}
          title="Delete bot"
          style={{
            display: "flex", alignItems: "center", justifyContent: "center",
            padding: "8px 11px", borderRadius: 10,
            background: "var(--bg-surface)", border: "1px solid var(--border)",
            color: "var(--text-muted)", cursor: "pointer",
            transition: "border-color 150ms ease, color 150ms ease, background 150ms ease",
          }}
          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--danger)"; (e.currentTarget as HTMLElement).style.color = "var(--danger)"; (e.currentTarget as HTMLElement).style.background = "var(--danger-bg)"; }}
          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.color = "var(--text-muted)"; (e.currentTarget as HTMLElement).style.background = "var(--bg-surface)"; }}
        >
          <Trash2 size={14} />
        </button>
      </div>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function BotsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [search, setSearch] = useState("");

  const { data: bots, isLoading } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  const deleteBot = useMutation({
    mutationFn: (id: string) => api.delete(`/v1/bots/${id}`, token),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bots"] });
      toast.success("Bot deleted.");
    },
    onError: () => toast.error("Failed to delete bot."),
  });

  const filtered = bots?.filter((b) =>
    !search || b.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div style={{ padding: "28px 36px", maxWidth: 1200, width: "100%" }}>

      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px", color: "var(--text-primary)" }}>
            Bots
          </h1>
          <p style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 4 }}>
            {bots?.length
              ? `${bots.length} bot${bots.length !== 1 ? "s" : ""} · ${bots.filter((b) => b.status === "ACTIVE").length} live`
              : "Manage your AI chatbots"}
          </p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          style={{
            display: "flex", alignItems: "center", gap: 7,
            background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
            color: "#fff", border: "none", padding: "9px 18px",
            borderRadius: 10, fontSize: 13, fontWeight: 600, cursor: "pointer",
            boxShadow: "0 2px 12px rgba(99,102,241,0.35)",
          }}
        >
          <Plus size={15} />
          New Bot
        </button>
      </div>

      {/* Search + filter bar */}
      {bots && bots.length > 0 && (
        <div style={{ marginBottom: 20, display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ position: "relative", flex: 1, maxWidth: 320 }}>
            <Search size={14} style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "var(--text-muted)" }} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search bots…"
              style={{
                width: "100%", padding: "8px 12px 8px 34px", boxSizing: "border-box",
                background: "var(--bg-panel)", border: "1px solid var(--border)",
                borderRadius: 10, color: "var(--text-primary)", fontSize: 13, outline: "none",
                transition: "border-color 150ms ease",
              }}
              onFocus={(e) => (e.target.style.borderColor = "var(--accent)")}
              onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
            />
          </div>
          {search && (
            <button
              onClick={() => setSearch("")}
              style={{ display: "flex", alignItems: "center", gap: 4, padding: "8px 12px", borderRadius: 10, background: "var(--bg-panel)", border: "1px solid var(--border)", color: "var(--text-muted)", cursor: "pointer", fontSize: 12 }}
            >
              <X size={12} /> Clear
            </button>
          )}
        </div>
      )}

      {/* Content */}
      {isLoading ? (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 18 }}>
          {[...Array(6)].map((_, i) => (
            <div key={i} style={{ height: 200, background: "var(--bg-panel)", border: "1px solid var(--border)", borderRadius: 16 }} />
          ))}
        </div>
      ) : !filtered?.length ? (
        search ? (
          <div style={{ textAlign: "center", padding: "60px 0", color: "var(--text-muted)", fontSize: 14 }}>
            No bots match "{search}"
          </div>
        ) : (
          <div style={{
            textAlign: "center", padding: "80px 20px",
            border: "2px dashed var(--border)", borderRadius: 20,
          }}>
            <div style={{
              width: 56, height: 56, borderRadius: 16, margin: "0 auto 16px",
              background: "var(--bg-surface)", display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              <BotIcon size={26} style={{ color: "var(--text-muted)" }} />
            </div>
            <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 6, color: "var(--text-primary)" }}>No bots yet</h3>
            <p style={{ fontSize: 13, color: "var(--text-muted)", marginBottom: 20, maxWidth: 300, margin: "0 auto 20px" }}>
              Create your first chatbot and embed it on your website in minutes.
            </p>
            <button
              onClick={() => setShowCreate(true)}
              style={{
                display: "inline-flex", alignItems: "center", gap: 6,
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                color: "#fff", border: "none", padding: "10px 22px",
                borderRadius: 10, fontSize: 13, fontWeight: 600, cursor: "pointer",
              }}
            >
              <Plus size={15} />
              Create first bot
            </button>
          </div>
        )
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 18 }}>
          {filtered.map((bot) => (
            <BotCard key={bot.id} bot={bot} onDelete={(id) => deleteBot.mutate(id)} />
          ))}
        </div>
      )}

      {/* Create modal */}
      {showCreate && (
        <CreateBotModal
          onClose={() => setShowCreate(false)}
          onCreated={() => qc.invalidateQueries({ queryKey: ["bots"] })}
        />
      )}
    </div>
  );
}
