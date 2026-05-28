"use client";

import { useState } from "react";
import { Search, X, Eye, Copy } from "lucide-react";
import { TEMPLATES, type Template } from "@/lib/templates";

const CATEGORY_LABELS: Record<string, string> = {
  Support: "Support",
  LeadGen: "Lead Gen",
  Ecommerce: "E-commerce",
  Healthcare: "Healthcare",
  RealEstate: "Real Estate",
  Education: "Education",
  HR: "HR",
};

const ALL_CATEGORIES = ["All", ...Object.keys(CATEGORY_LABELS)] as const;

const COLOR_MAP: Record<string, string> = {
  Support:     "#6366F1",
  LeadGen:     "#10B981",
  Ecommerce:   "#F59E0B",
  Healthcare:  "#EF4444",
  RealEstate:  "#06B6D4",
  Education:   "#8B5CF6",
  HR:          "#EC4899",
};

// ─── Preview modal ─────────────────────────────────────────────────────────────

function PreviewModal({ template, onClose }: { template: Template; onClose: () => void }) {
  const color = COLOR_MAP[template.category] ?? "#6366F1";
  return (
    <div
      style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000, backdropFilter: "blur(4px)" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 18, width: "100%", maxWidth: 520,
        boxShadow: "0 20px 60px rgba(0,0,0,0.5)", overflow: "hidden",
      }}>
        {/* Header */}
        <div style={{ padding: "20px 24px", borderBottom: "1px solid var(--border)", display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
            <div style={{ fontSize: 36 }}>{template.avatar}</div>
            <div>
              <h2 style={{ fontSize: 17, fontWeight: 700, color: "var(--text-primary)" }}>{template.name}</h2>
              <p style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 2 }}>{template.description}</p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: "var(--bg-surface)", border: "1px solid var(--border)", borderRadius: 8, padding: "5px 7px", cursor: "pointer", color: "var(--text-muted)", flexShrink: 0 }}>
            <X size={14} />
          </button>
        </div>

        {/* Node flow preview */}
        <div style={{ padding: "20px 24px", borderBottom: "1px solid var(--border)" }}>
          <p style={{ fontSize: 11, fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.06em", color: "var(--text-muted)", marginBottom: 12 }}>Flow structure</p>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 6, alignItems: "center" }}>
            {template.definition.nodes.map((node, i) => (
              <span key={node.id} style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
                <span style={{
                  padding: "4px 10px", borderRadius: 8,
                  background: "var(--bg-surface)", border: "1px solid var(--border)",
                  fontSize: 12, color: "var(--text-secondary)", fontWeight: 500,
                }}>
                  {node.type.replace(/_/g, " ")}
                </span>
                {i < template.definition.nodes.length - 1 && (
                  <span style={{ color: "var(--text-muted)", fontSize: 11 }}>→</span>
                )}
              </span>
            ))}
          </div>
        </div>

        {/* Stats */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", padding: "16px 24px", borderBottom: "1px solid var(--border)", gap: 12 }}>
          {[
            { label: "Nodes", value: template.definition.nodes.length },
            { label: "Connections", value: template.definition.edges.length },
            { label: "Complexity", value: template.definition.nodes.length < 5 ? "Low" : template.definition.nodes.length < 10 ? "Medium" : "High" },
          ].map(({ label, value }) => (
            <div key={label} style={{ textAlign: "center" }}>
              <p style={{ fontSize: 22, fontWeight: 700, color, marginBottom: 2 }}>{value}</p>
              <p style={{ fontSize: 11, color: "var(--text-muted)" }}>{label}</p>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div style={{ padding: "16px 24px", display: "flex", gap: 10 }}>
          <button
            onClick={onClose}
            style={{ flex: 1, padding: "10px", borderRadius: 10, background: "var(--bg-surface)", border: "1px solid var(--border)", color: "var(--text-secondary)", cursor: "pointer", fontSize: 13, fontWeight: 500 }}
          >
            Close
          </button>
          <button
            style={{
              flex: 2, display: "flex", alignItems: "center", justifyContent: "center", gap: 6,
              padding: "10px", borderRadius: 10,
              background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
              color: "#fff", border: "none", cursor: "pointer", fontSize: 13, fontWeight: 600,
            }}
            onClick={onClose}
          >
            <Copy size={13} />
            Use Template
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Template card ─────────────────────────────────────────────────────────────

function TemplateCard({ template, onPreview }: { template: Template; onPreview: () => void }) {
  const color = COLOR_MAP[template.category] ?? "#6366F1";
  return (
    <div
      style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 14, overflow: "hidden", display: "flex", flexDirection: "column",
        transition: "border-color 200ms ease, transform 200ms ease",
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border-strong)"; (e.currentTarget as HTMLElement).style.transform = "translateY(-1px)"; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.transform = "none"; }}
    >
      {/* Top accent + avatar */}
      <div style={{ height: 120, background: `linear-gradient(135deg, ${color}20, ${color}08)`, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, borderBottom: "1px solid var(--border)" }}>
        <span style={{ fontSize: 48 }}>{template.avatar ?? "🤖"}</span>
      </div>

      {/* Body */}
      <div style={{ padding: "14px 16px", flex: 1, display: "flex", flexDirection: "column", gap: 10 }}>
        <div>
          <h3 style={{ fontSize: 14, fontWeight: 700, color: "var(--text-primary)", marginBottom: 4 }}>
            {template.name}
          </h3>
          <p style={{ fontSize: 12, color: "var(--text-muted)", lineHeight: 1.5 }}>
            {template.description}
          </p>
        </div>

        <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
          <span style={{
            padding: "2px 10px", borderRadius: 20, fontSize: 11, fontWeight: 600,
            background: color + "18", color,
            border: `1px solid ${color}30`,
          }}>
            {CATEGORY_LABELS[template.category] ?? template.category}
          </span>
          <span style={{ fontSize: 11, color: "var(--text-muted)" }}>
            {template.nodeCount} nodes
          </span>
        </div>

        <div style={{ display: "flex", gap: 8, marginTop: "auto" }}>
          <button
            onClick={onPreview}
            style={{
              flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: 5,
              padding: "7px", borderRadius: 10,
              background: "var(--bg-surface)", border: "1px solid var(--border)",
              color: "var(--text-secondary)", cursor: "pointer", fontSize: 12, fontWeight: 500,
              transition: "border-color 150ms ease, color 150ms ease",
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--accent)"; (e.currentTarget as HTMLElement).style.color = "var(--accent)"; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.color = "var(--text-secondary)"; }}
          >
            <Eye size={12} /> Preview
          </button>
          <button
            style={{
              flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: 5,
              padding: "7px", borderRadius: 10,
              background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
              color: "#fff", border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600,
            }}
          >
            <Copy size={12} /> Use
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function TemplatesPage() {
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<typeof ALL_CATEGORIES[number]>("All");
  const [preview, setPreview] = useState<Template | null>(null);

  const filtered = TEMPLATES.filter((t) => {
    const matchSearch = t.name.toLowerCase().includes(search.toLowerCase()) || t.description.toLowerCase().includes(search.toLowerCase());
    const matchCat = filter === "All" || t.category === filter;
    return matchSearch && matchCat;
  });

  return (
    <div style={{ padding: "28px 36px", maxWidth: 1200, width: "100%" }}>
      {/* Header */}
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px", color: "var(--text-primary)" }}>
          Templates
        </h1>
        <p style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 4 }}>
          Start from a pre-built flow and customize it for your use case.
        </p>
      </div>

      {/* Search + filters */}
      <div style={{ display: "flex", flexDirection: "column", gap: 12, marginBottom: 24 }}>
        <div style={{ position: "relative", maxWidth: 380 }}>
          <Search size={14} style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "var(--text-muted)" }} />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search templates…"
            style={{
              width: "100%", padding: "9px 12px 9px 34px", boxSizing: "border-box",
              background: "var(--bg-panel)", border: "1px solid var(--border)",
              borderRadius: 10, color: "var(--text-primary)", fontSize: 13, outline: "none",
              transition: "border-color 150ms ease",
            }}
            onFocus={(e) => (e.target.style.borderColor = "var(--accent)")}
            onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
          />
        </div>

        <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
          {ALL_CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setFilter(cat)}
              style={{
                padding: "5px 14px", borderRadius: 20, fontSize: 12, fontWeight: 500,
                border: "1px solid",
                borderColor: filter === cat ? "transparent" : "var(--border)",
                background: filter === cat ? "linear-gradient(135deg, #6366F1, #8B5CF6)" : "var(--bg-panel)",
                color: filter === cat ? "#fff" : "var(--text-secondary)",
                cursor: "pointer", transition: "all 150ms ease",
              }}
            >
              {cat === "All" ? "All" : CATEGORY_LABELS[cat] ?? cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid */}
      {filtered.length > 0 ? (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 16 }}>
          {filtered.map((t) => (
            <TemplateCard key={t.id} template={t} onPreview={() => setPreview(t)} />
          ))}
        </div>
      ) : (
        <div style={{ textAlign: "center", padding: "60px 0", color: "var(--text-muted)", fontSize: 14 }}>
          No templates found for &ldquo;{search}&rdquo;
        </div>
      )}

      {preview && <PreviewModal template={preview} onClose={() => setPreview(null)} />}
    </div>
  );
}
