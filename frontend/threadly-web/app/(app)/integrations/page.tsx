"use client";

import { useState } from "react";
import {
  Search, X, CheckCircle2, ExternalLink,
  Slack, Mail, Zap, FileText, Database, MessageSquare, Send,
  Github, BarChart3, DollarSign, ShoppingCart, Users, GitBranch, Layers, Sheet,
} from "lucide-react";

interface Integration {
  id: string;
  name: string;
  category: "Messaging" | "CRM" | "Productivity" | "Analytics" | "E-commerce";
  description: string;
  logo: React.ComponentType<{ size?: number; style?: React.CSSProperties }>;
  color: string;
  docsUrl?: string;
}

const INTEGRATIONS: Integration[] = [
  { id: "slack",      name: "Slack",            category: "Messaging",   description: "Send messages to Slack channels when a bot escalates a conversation.", logo: Slack,         color: "#36C5F0" },
  { id: "gmail",      name: "Gmail",            category: "Messaging",   description: "Send transactional or follow-up emails directly from your bot flows.", logo: Mail,          color: "#EA4335" },
  { id: "hubspot",    name: "HubSpot",          category: "CRM",         description: "Auto-create or update contacts and deals from bot conversations.", logo: Zap,            color: "#FF7A59" },
  { id: "salesforce", name: "Salesforce",       category: "CRM",         description: "Push leads, log activity and update opportunities in Salesforce.", logo: Database,       color: "#00A1DE" },
  { id: "notion",     name: "Notion",           category: "Productivity",description: "Create pages and database entries from conversation data.", logo: FileText,       color: "#000000" },
  { id: "sheets",     name: "Google Sheets",    category: "Productivity",description: "Append rows or update cells with captured lead and chat data.", logo: Sheet,          color: "#34A853" },
  { id: "github",     name: "GitHub",           category: "Productivity",description: "Create issues or comment on PRs from support bot flows.", logo: Github,         color: "#181717" },
  { id: "linear",     name: "Linear",           category: "Productivity",description: "Auto-create and update issues from customer conversations.", logo: GitBranch,      color: "#5E6AD2" },
  { id: "jira",       name: "Jira",             category: "Productivity",description: "Create and transition Jira issues directly from bot escalations.", logo: Zap,            color: "#0052CC" },
  { id: "airtable",   name: "Airtable",         category: "Productivity",description: "Write records to Airtable bases as conversations complete.", logo: Layers,         color: "#18BFFF" },
  { id: "sendgrid",   name: "SendGrid",         category: "Messaging",   description: "Send transactional emails via SendGrid from within flows.", logo: Mail,            color: "#00A8E1" },
  { id: "twilio",     name: "Twilio",           category: "Messaging",   description: "Send SMS and WhatsApp messages through Twilio programmable messaging.", logo: MessageSquare,  color: "#F22F46" },
  { id: "discord",    name: "Discord",          category: "Messaging",   description: "Notify Discord channels when conversations need human attention.", logo: Send,           color: "#5865F2" },
  { id: "teams",      name: "Microsoft Teams",  category: "Messaging",   description: "Route handoffs and alerts to Microsoft Teams channels.", logo: Users,          color: "#6264A7" },
  { id: "stripe",     name: "Stripe",           category: "E-commerce",  description: "Trigger payment links and check subscription status inside flows.", logo: DollarSign,    color: "#625BEE" },
  { id: "shopify",    name: "Shopify",          category: "E-commerce",  description: "Look up orders, products and customers from Shopify in real time.", logo: ShoppingCart,  color: "#96BE00" },
  { id: "mailchimp",  name: "Mailchimp",        category: "Analytics",   description: "Add subscribers and trigger email sequences from bot opt-ins.", logo: Mail,           color: "#FFE01B" },
  { id: "mixpanel",   name: "Mixpanel",         category: "Analytics",   description: "Track custom events and user properties from every conversation.", logo: BarChart3,     color: "#25293C" },
  { id: "segment",    name: "Segment",          category: "Analytics",   description: "Route conversation events to 300+ Segment destinations.", logo: Database,       color: "#00D4AA" },
  { id: "make",       name: "Make.com",         category: "Productivity",description: "Trigger Make scenarios and connect to 1000+ apps from bot flows.", logo: Zap,           color: "#FF6B6B" },
];

const CATEGORIES = ["All", "Messaging", "CRM", "Productivity", "Analytics", "E-commerce"] as const;

// ─── Connect modal ────────────────────────────────────────────────────────────

function ConnectModal({ integration, onClose }: { integration: Integration; onClose: () => void }) {
  const [apiKey, setApiKey] = useState("");
  const [saving, setSaving] = useState(false);

  function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    // TODO: call POST /v1/integrations with { integrationId, apiKey } once backend is ready
    setTimeout(() => { setSaving(false); onClose(); }, 600);
  }

  return (
    <div
      style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000, backdropFilter: "blur(4px)" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 18, padding: 28, width: 460, boxShadow: "0 20px 60px rgba(0,0,0,0.5)",
      }}>
        {/* Header */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 22 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 10, background: integration.color + "20", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
              <integration.logo size={20} style={{ color: integration.color }} />
            </div>
            <div>
              <h2 style={{ fontSize: 16, fontWeight: 700, color: "var(--text-primary)" }}>
                Connect {integration.name}
              </h2>
              <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>
                {integration.description}
              </p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: "var(--bg-surface)", border: "1px solid var(--border)", borderRadius: 8, padding: "5px 7px", cursor: "pointer", color: "var(--text-muted)" }}>
            <X size={14} />
          </button>
        </div>

        <form onSubmit={handleSave} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div>
            <label style={{ display: "block", fontSize: 12, fontWeight: 500, color: "var(--text-secondary)", marginBottom: 6 }}>
              API Key / Secret
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              required
              autoFocus
              placeholder="Paste your API key here"
              style={{
                width: "100%", padding: "10px 14px", boxSizing: "border-box",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: 10, color: "var(--text-primary)", fontSize: 14, outline: "none",
                transition: "border-color 150ms ease",
              }}
              onFocus={(e) => (e.target.style.borderColor = "var(--accent)")}
              onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
            />
          </div>

          <p style={{ fontSize: 12, color: "var(--text-muted)" }}>
            Your credentials are encrypted at rest and never shared.{" "}
            {integration.docsUrl && (
              <a href={integration.docsUrl} target="_blank" rel="noreferrer" style={{ color: "var(--accent)", textDecoration: "none" }}>
                View docs <ExternalLink size={11} style={{ display: "inline" }} />
              </a>
            )}
          </p>

          <div style={{ display: "flex", gap: 10, marginTop: 2 }}>
            <button
              type="button"
              onClick={onClose}
              style={{ flex: 1, padding: "10px", borderRadius: 10, background: "var(--bg-surface)", border: "1px solid var(--border)", color: "var(--text-secondary)", cursor: "pointer", fontSize: 13, fontWeight: 500 }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!apiKey.trim() || saving}
              style={{
                flex: 2, padding: "10px", borderRadius: 10,
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                color: "#fff", border: "none", cursor: "pointer", fontSize: 13, fontWeight: 600,
                opacity: !apiKey.trim() ? 0.5 : 1,
              }}
            >
              {saving ? "Saving…" : "Save & Connect"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Integration card ─────────────────────────────────────────────────────────

function IntegrationCard({ integration, onConnect }: { integration: Integration; onConnect: () => void }) {
  return (
    <div
      style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: 14, padding: 18, display: "flex", flexDirection: "column", gap: 12,
        transition: "border-color 200ms ease, transform 200ms ease",
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border-strong)"; (e.currentTarget as HTMLElement).style.transform = "translateY(-1px)"; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = "var(--border)"; (e.currentTarget as HTMLElement).style.transform = "none"; }}
    >
      <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
        <div style={{ width: 44, height: 44, borderRadius: 12, background: integration.color + "18", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
          <integration.logo size={22} style={{ color: integration.color }} />
        </div>
        <span style={{
          fontSize: 10, fontWeight: 600, padding: "3px 8px", borderRadius: 20,
          background: "var(--bg-surface)", color: "var(--text-muted)",
          border: "1px solid var(--border)", textTransform: "uppercase", letterSpacing: "0.04em",
        }}>
          {integration.category}
        </span>
      </div>

      <div>
        <h3 style={{ fontSize: 14, fontWeight: 700, color: "var(--text-primary)", marginBottom: 4 }}>
          {integration.name}
        </h3>
        <p style={{ fontSize: 12, color: "var(--text-muted)", lineHeight: 1.5 }}>
          {integration.description}
        </p>
      </div>

      <button
        onClick={onConnect}
        style={{
          width: "100%", padding: "8px", borderRadius: 10, marginTop: "auto",
          background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
          color: "#fff", border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600,
          transition: "opacity 150ms ease",
        }}
        onMouseEnter={(e) => ((e.target as HTMLButtonElement).style.opacity = "0.9")}
        onMouseLeave={(e) => ((e.target as HTMLButtonElement).style.opacity = "1")}
      >
        Connect
      </button>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function IntegrationsPage() {
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<typeof CATEGORIES[number]>("All");
  const [selected, setSelected] = useState<Integration | null>(null);

  const filtered = INTEGRATIONS.filter((i) => {
    const matchSearch = i.name.toLowerCase().includes(search.toLowerCase()) || i.description.toLowerCase().includes(search.toLowerCase());
    const matchCat = filter === "All" || i.category === filter;
    return matchSearch && matchCat;
  });

  return (
    <div style={{ padding: "28px 36px", maxWidth: 1200, width: "100%" }}>
      {/* Header */}
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px", color: "var(--text-primary)" }}>
          Integrations
        </h1>
        <p style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 4 }}>
          Connect your tools to automate workflows and sync data.
        </p>
      </div>

      {/* Search + category filters */}
      <div style={{ display: "flex", flexDirection: "column", gap: 12, marginBottom: 24 }}>
        <div style={{ position: "relative", maxWidth: 380 }}>
          <Search size={14} style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "var(--text-muted)" }} />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search integrations…"
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
          {CATEGORIES.map((cat) => (
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
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid */}
      {filtered.length > 0 ? (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 16 }}>
          {filtered.map((integration) => (
            <IntegrationCard
              key={integration.id}
              integration={integration}
              onConnect={() => setSelected(integration)}
            />
          ))}
        </div>
      ) : (
        <div style={{ textAlign: "center", padding: "60px 0", color: "var(--text-muted)", fontSize: 14 }}>
          No integrations found for &ldquo;{search}&rdquo;
        </div>
      )}

      {selected && (
        <ConnectModal integration={selected} onClose={() => setSelected(null)} />
      )}
    </div>
  );
}
