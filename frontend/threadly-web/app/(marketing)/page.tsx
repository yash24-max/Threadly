"use client";

import Link from "next/link";
import { useState } from "react";
import {
  ArrowRight, Check, Bot, Workflow, Database, Zap, Globe, BarChart3,
  MessageSquare, Users, ShieldCheck, Cpu, Play, Star,
  ChevronLeft, ChevronRight as ChevronRightIcon,
} from "lucide-react";

/* ── Hero ────────────────────────────────────────────────────────────────── */
function HeroSection() {
  const [email, setEmail] = useState("");
  return (
    <section className="relative overflow-hidden py-24 sm:py-32">
      {/* Soft gradient blobs */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-1/2 -top-40 h-[700px] w-[700px] -translate-x-1/2 rounded-full opacity-30 blur-3xl"
          style={{ background: "radial-gradient(circle, rgba(99,102,241,0.35) 0%, transparent 65%)" }} />
        <div className="absolute -right-20 top-1/3 h-[400px] w-[400px] rounded-full opacity-20 blur-3xl"
          style={{ background: "radial-gradient(circle, rgba(139,92,246,0.4) 0%, transparent 65%)" }} />
        <div className="absolute -left-20 bottom-1/4 h-[360px] w-[360px] rounded-full opacity-15 blur-3xl"
          style={{ background: "radial-gradient(circle, rgba(6,182,212,0.35) 0%, transparent 65%)" }} />
        {/* Subtle grid */}
        <div className="absolute inset-0 opacity-[0.04]"
          style={{ backgroundImage:"linear-gradient(var(--text-primary) 1px, transparent 1px),linear-gradient(90deg, var(--text-primary) 1px, transparent 1px)", backgroundSize:"72px 72px" }} />
      </div>

      {/* ── Hero content: full-width flex column, align-items:center = true viewport centre ── */}
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", padding: "0 24px", position: "relative" }}>

        {/* Badge */}
        <span style={{ display: "inline-flex", alignItems: "center", gap: 8, borderRadius: 9999, border: "1px solid var(--border)", background: "var(--bg-panel)", padding: "6px 16px", fontSize: 14, color: "var(--text-secondary)", boxShadow: "var(--shadow-sm)" }}>
          <span style={{ width: 6, height: 6, borderRadius: "50%", background: "var(--success)", animation: "pulse 2s infinite" }} />
          Now with hybrid RAG + GPT-4o support
          <ArrowRight size={12} style={{ color: "var(--accent)" }} />
        </span>

        {/* Headline — centred by align-items on parent */}
        <h1 style={{ fontSize: "clamp(2.5rem, 5vw, 4.5rem)", fontWeight: 800, letterSpacing: "-0.03em", lineHeight: 1.1, marginTop: 28, color: "var(--text-primary)", maxWidth: 900 }}>
          Build AI Chatbots that{" "}
          <span className="gradient-text">Actually Convert</span>
        </h1>

        {/* Description */}
        <p style={{ maxWidth: 560, marginTop: 20, fontSize: "1.125rem", lineHeight: 1.7, color: "var(--text-secondary)" }}>
          Visual flow builder, knowledge-base RAG, and live human handoff — all in
          one platform. Deploy on your website, WhatsApp, Instagram, and Telegram in minutes.
        </p>

        {/* Trust pills */}
        <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", justifyContent: "center", gap: "8px 24px", marginTop: 20, fontSize: 13, color: "var(--text-muted)" }}>
          {["No credit card required", "14-day free trial", "Cancel anytime"].map(t => (
            <span key={t} style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <Check size={13} style={{ color: "var(--success)" }} /> {t}
            </span>
          ))}
        </div>

        {/* Email CTA — width-constrained, centred by parent align-items */}
        <div style={{ display: "flex", gap: 8, marginTop: 32, width: "100%", maxWidth: 460, flexWrap: "wrap" }}>
          <input
            type="email" value={email} onChange={e => setEmail(e.target.value)}
            placeholder="Enter your work email"
            style={{ flex: 1, minWidth: 180, borderRadius: 12, border: "1px solid var(--border)", background: "var(--bg-panel)", padding: "12px 18px", fontSize: 14, color: "var(--text-primary)", outline: "none", boxShadow: "var(--shadow-sm)" }}
          />
          <Link
            href={`/signup${email ? `?email=${encodeURIComponent(email)}` : ""}`}
            style={{ display: "flex", alignItems: "center", gap: 6, borderRadius: 12, padding: "12px 24px", fontSize: 14, fontWeight: 700, color: "#fff", background: "linear-gradient(135deg,#6366F1,#8B5CF6)", whiteSpace: "nowrap", boxShadow: "var(--shadow-accent)", flexShrink: 0 }}>
            Start for Free <ArrowRight size={14} />
          </Link>
        </div>
      </div>

        {/* Product mockup */}
        <div style={{ maxWidth: 960, marginLeft: "auto", marginRight: "auto", marginTop: 64, padding: "0 24px", position: "relative" }}>
          <div className="overflow-hidden rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] shadow-[0_24px_80px_rgba(13,14,26,0.12)]">
            {/* Window chrome */}
            <div className="flex items-center gap-2 border-b border-[var(--border)] bg-[var(--bg-surface)] px-4 py-3">
              <span className="h-3 w-3 rounded-full bg-[#EF4444]" />
              <span className="h-3 w-3 rounded-full bg-[#F59E0B]" />
              <span className="h-3 w-3 rounded-full bg-[#10B981]" />
              <span className="ml-2 flex-1 rounded-md bg-[var(--bg-hover)] px-3 py-1 text-[11px] text-[var(--text-muted)] text-left font-mono">
                app.threadly.dev/builder
              </span>
            </div>
            {/* Builder canvas */}
            <div className="relative flex h-[380px] sm:h-[460px] overflow-hidden">
              {/* Node panel */}
              <div className="w-52 shrink-0 border-r border-[var(--border)] bg-[var(--bg-surface)] p-3 space-y-1.5">
                <p className="px-2 pb-2 pt-1 text-[10px] font-semibold uppercase tracking-widest text-[var(--text-muted)]">Nodes</p>
                {[
                  { label: "Message",      color: "#6366F1" },
                  { label: "AI Response",  color: "#8B5CF6" },
                  { label: "Condition",    color: "#F59E0B" },
                  { label: "API Call",     color: "#06B6D4" },
                  { label: "Collect Input",color: "#059669" },
                  { label: "Handoff",      color: "#EF4444" },
                ].map(n => (
                  <div key={n.label} className="flex items-center gap-2.5 rounded-lg border border-[var(--border)] bg-[var(--bg-panel)] px-3 py-2 cursor-grab select-none">
                    <span className="h-2 w-2 rounded-sm shrink-0" style={{ background: n.color }} />
                    <span className="text-xs font-medium text-[var(--text-secondary)]">{n.label}</span>
                  </div>
                ))}
              </div>

              {/* Canvas */}
              <div className="relative flex-1 overflow-hidden bg-[var(--bg-canvas)]">
                <div className="absolute inset-0 opacity-40"
                  style={{ backgroundImage:"radial-gradient(circle, var(--border) 1px, transparent 1px)", backgroundSize:"28px 28px" }} />
                {[
                  { x:70,  y:55,  label:"Welcome",     color:"#6366F1", w:130 },
                  { x:260, y:38,  label:"Ask: Intent",  color:"#8B5CF6", w:140 },
                  { x:455, y:28,  label:"AI Answer",    color:"#8B5CF6", w:130 },
                  { x:260, y:135, label:"Human?",       color:"#F59E0B", w:105 },
                  { x:435, y:155, label:"Handoff",      color:"#EF4444", w:105 },
                ].map(node => (
                  <div key={node.label}
                    className="absolute flex items-center gap-2 rounded-xl border bg-[var(--bg-panel)] px-3 py-2 shadow-[var(--shadow-sm)]"
                    style={{ left:node.x, top:node.y, width:node.w, borderColor: node.color+"55" }}>
                    <span className="h-2 w-2 rounded-full shrink-0" style={{ background:node.color }} />
                    <span className="text-xs font-semibold text-[var(--text-primary)] truncate">{node.label}</span>
                  </div>
                ))}
                <svg className="absolute inset-0 pointer-events-none w-full h-full">
                  <path d="M 200 71 C 230 71 230 62 260 62" stroke="#6366F1" strokeWidth="1.5" fill="none" strokeDasharray="4 3" opacity="0.5"/>
                  <path d="M 390 62 C 420 62 420 44 455 44" stroke="#8B5CF6" strokeWidth="1.5" fill="none" strokeDasharray="4 3" opacity="0.5"/>
                  <path d="M 260 82 C 260 110 260 120 260 150" stroke="#F59E0B" strokeWidth="1.5" fill="none" strokeDasharray="4 3" opacity="0.5"/>
                  <path d="M 365 163 C 400 163 415 168 435 168" stroke="#EF4444" strokeWidth="1.5" fill="none" strokeDasharray="4 3" opacity="0.5"/>
                </svg>

                {/* Live preview card */}
                <div className="absolute bottom-5 right-5 w-54 rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-3.5 shadow-[var(--shadow-lg)]" style={{ width: 220 }}>
                  <p className="mb-2.5 text-[10px] font-semibold uppercase tracking-widest text-[var(--text-muted)]">Live Preview</p>
                  <div className="space-y-2">
                    <div className="flex"><span className="rounded-xl rounded-tl-none bg-[var(--bg-surface)] px-3 py-2 text-xs text-[var(--text-primary)] max-w-[85%]">Hi! How can I help? 👋</span></div>
                    <div className="flex justify-end"><span className="rounded-xl rounded-tr-none px-3 py-2 text-xs text-white max-w-[80%]" style={{ background:"#6366F1" }}>I need support</span></div>
                    <div className="flex"><span className="rounded-xl rounded-tl-none bg-[var(--bg-surface)] px-3 py-2 text-xs text-[var(--text-primary)] max-w-[90%]">Sure, let me help!</span></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
    </section>
  );
}

/* ── Social Proof ─────────────────────────────────────────────────────────── */
/* ── Social proof: key metrics ──────────────────────────────────────────── */
const stats = [
  { value: "2,400+", label: "Teams using Threadly",    suffix: "" },
  { value: "12M",    label: "Conversations handled",    suffix: "+" },
  { value: "78%",    label: "Avg ticket deflection",    suffix: "" },
  { value: "99.9",   label: "Uptime SLA",               suffix: "%" },
  { value: "<30s",   label: "Avg first response time",  suffix: "" },
];

function SocialProofSection() {
  return (
    <section style={{ borderTop: "1px solid var(--border)", borderBottom: "1px solid var(--border)", background: "var(--bg-panel)" }}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{
          display: "grid",
          gridTemplateColumns: `repeat(${stats.length}, 1fr)`,
          borderLeft: "1px solid var(--border)",
        }}>
          {stats.map((s, i) => (
            <div key={s.label} style={{
              padding: "28px 24px",
              borderRight: "1px solid var(--border)",
              textAlign: "center",
              position: "relative",
            }}>
              {/* Subtle gradient accent on top */}
              <div style={{
                position: "absolute", top: 0, left: "20%", right: "20%", height: 2,
                background: i === 2
                  ? "linear-gradient(90deg, #6366F1, #8B5CF6)"
                  : "transparent",
                borderRadius: "0 0 2px 2px",
              }} />
              <p style={{
                fontSize: "clamp(1.6rem, 2.5vw, 2.25rem)",
                fontWeight: 800,
                letterSpacing: "-0.04em",
                lineHeight: 1,
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text",
              }}>
                {s.value}{s.suffix}
              </p>
              <p style={{
                marginTop: 6,
                fontSize: 12,
                fontWeight: 500,
                color: "var(--text-muted)",
                lineHeight: 1.4,
              }}>
                {s.label}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ── Features ─────────────────────────────────────────────────────────────── */
const features = [
  { icon: Workflow,    title: "Visual Flow Builder",    desc: "Drag-and-drop canvas with conditional branches, API call nodes, delay timers, and AI response nodes. Build any conversation logic without code.", gradient: "from-[#6366F1] to-[#8B5CF6]", tag:"No-code"       },
  { icon: Database,    title: "Hybrid RAG Knowledge Base", desc: "Upload docs, PDFs, or URLs. Threadly indexes with dense (Qdrant) + sparse (BM25) retrieval and RRF ranking for accurate, grounded answers.",  gradient: "from-[#8B5CF6] to-[#06B6D4]", tag:"AI-powered"    },
  { icon: Globe,       title: "Multi-Channel Deployment",  desc: "One bot, everywhere. Embed on your website, connect to WhatsApp Business, Instagram DMs, Telegram, and SMS from one dashboard.",                 gradient: "from-[#06B6D4] to-[#059669]", tag:"Multi-channel" },
  { icon: Users,       title: "Live Human Handoff",        desc: "Real-time inbox with agent takeover, delivery receipts, and read status. Route conversations to the right team automatically.",                   gradient: "from-[#059669] to-[#6366F1]", tag:"Real-time"     },
  { icon: BarChart3,   title: "Analytics & Cost Tracking", desc: "Per-bot conversation metrics, response time p50/p95, drop-off rates, and token cost breakdown per conversation. Know your ROI.",                  gradient: "from-[#D97706] to-[#EF4444]", tag:"Insights"      },
  { icon: ShieldCheck, title: "Enterprise-Grade Security", desc: "Tenant-isolated data, JWT + refresh tokens, RBAC roles, SSO-ready, and full audit logs. Production-grade from day one.",                          gradient: "from-[#EF4444] to-[#8B5CF6]", tag:"Enterprise"    },
];

function FeaturesSection() {
  return (
    <section className="py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{ maxWidth: 640, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 56 }}>
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">
            Everything you need
          </span>
          <h2 className="text-4xl font-bold tracking-tight text-[var(--text-primary)] sm:text-5xl">
            The complete chatbot platform
          </h2>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">
            From first message to full deployment — Threadly handles every layer
            of your conversational AI stack.
          </p>
        </div>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {features.map(f => (
            <div key={f.title}
              className="group relative overflow-hidden rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 transition-all duration-300 hover:border-[var(--accent)] hover:-translate-y-1 hover:shadow-[var(--shadow-lg)]">
              <div className={`absolute inset-0 opacity-0 group-hover:opacity-[0.04] transition-opacity duration-300 bg-gradient-to-br ${f.gradient}`} />
              <div className="relative">
                <div className={`mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${f.gradient}`}>
                  <f.icon size={19} className="text-white" />
                </div>
                <span className="mb-2 inline-block rounded-full border border-[var(--border)] bg-[var(--bg-surface)] px-2.5 py-0.5 text-[10px] font-semibold uppercase tracking-widest text-[var(--text-muted)]">
                  {f.tag}
                </span>
                <h3 className="mt-1 text-base font-bold text-[var(--text-primary)]">{f.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-[var(--text-secondary)]">{f.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ── How It Works ─────────────────────────────────────────────────────────── */
const steps = [
  { step:"01", title:"Build your flow",       desc:"Open the visual canvas and drag nodes to create conversation paths. Add AI nodes backed by your knowledge base, conditions, and API calls.", cta:"Open builder →", href:"/signup"   },
  { step:"02", title:"Train on your content", desc:"Upload PDFs, URLs, or paste text. Threadly chunks, embeds, and indexes everything so your bot can answer accurately from your docs.",       cta:"See KB demo →",  href:"/product"  },
  { step:"03", title:"Deploy everywhere",     desc:"Grab the embed snippet for your site, or connect to WhatsApp, Instagram, or Telegram. Go live in under 5 minutes.",                          cta:"View channels →",href:"/product"  },
];

function HowItWorksSection() {
  return (
    <section className="relative py-24 sm:py-32 bg-[var(--bg-surface)]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{ maxWidth: 640, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 56 }}>
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">
            How it works
          </span>
          <h2 className="text-4xl font-bold tracking-tight text-[var(--text-primary)] sm:text-5xl">
            From zero to live bot in minutes
          </h2>
        </div>
        <div className="grid gap-6 lg:grid-cols-3">
          {steps.map((s, i) => (
            <div key={s.step} className="relative rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-7 shadow-[var(--shadow-sm)]">
              {i < steps.length - 1 && (
                <div className="absolute right-0 top-9 hidden h-px w-6 translate-x-6 border-t border-dashed border-[var(--border)] lg:block" />
              )}
              <div className="mb-5 flex items-center gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-full text-sm font-bold text-white"
                  style={{ background:"linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                  {s.step}
                </span>
                <div className="h-px flex-1 bg-[var(--border)]" />
              </div>
              <h3 className="text-lg font-bold text-[var(--text-primary)]">{s.title}</h3>
              <p className="mt-2.5 text-sm leading-relaxed text-[var(--text-secondary)]">{s.desc}</p>
              <Link href={s.href}
                className="mt-5 inline-flex items-center gap-1 text-sm font-semibold text-[var(--accent)] hover:gap-2 transition-all">
                {s.cta}
              </Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ── Use Cases ────────────────────────────────────────────────────────────── */
const useCaseTabs = [
  {
    id:"support", label:"Customer Support", icon:MessageSquare,
    headline:"Resolve 80% of tickets automatically",
    desc:"Deploy a bot trained on your help docs that handles returns, order tracking, FAQs, and account issues — then hands off to humans for complex queries.",
    bullets:["24/7 instant responses","Smart escalation rules","CSAT auto-scoring","Human handoff with context"],
    chat:[
      {from:"bot",  text:"Hi! I'm here to help. What do you need today? 👋"},
      {from:"user", text:"I want to return my order #4523"},
      {from:"bot",  text:"Found your order! It's eligible for return. Shall I initiate the process?"},
      {from:"user", text:"Yes please"},
      {from:"bot",  text:"Done! Return label sent to your email. Anything else?"},
    ],
  },
  {
    id:"sales", label:"Sales", icon:Zap,
    headline:"Qualify and convert leads 3× faster",
    desc:"Capture visitor intent, qualify with BANT questions, book demos via calendar integration, and sync opportunities to your CRM automatically.",
    bullets:["Lead scoring + routing","Calendar booking flows","CRM auto-sync","Follow-up sequences"],
    chat:[
      {from:"bot",  text:"Looking for a chatbot solution? Let me help you find the right plan."},
      {from:"user", text:"We have a 50-person support team"},
      {from:"bot",  text:"Perfect fit for our Growth plan. Want to book a quick 15-min demo?"},
      {from:"user", text:"Sure, next Tuesday works"},
      {from:"bot",  text:"Booked! Check your email for the calendar invite."},
    ],
  },
  {
    id:"onboarding", label:"Onboarding", icon:Users,
    headline:"Guide every new user to their aha-moment",
    desc:"Personalized onboarding flows based on role and use case. Track completion, surface tips at the right moment, and reduce time-to-value.",
    bullets:["Role-based flows","In-app and email nudges","Completion tracking","Feature discovery"],
    chat:[
      {from:"bot",  text:"Welcome! What's your main goal with Threadly?"},
      {from:"user", text:"Automate customer support"},
      {from:"bot",  text:"Great choice! Let's set up your first support bot in 3 steps."},
      {from:"user", text:"Let's go!"},
      {from:"bot",  text:"Step 1: Upload your help docs. I'll train on them instantly."},
    ],
  },
];

function ChatBubble({ from, text }: { from:"bot"|"user"; text:string }) {
  return (
    <div className={`flex ${from==="user"?"justify-end":"justify-start"}`}>
      <span className={`max-w-[82%] rounded-2xl px-3.5 py-2 text-xs leading-snug ${
        from==="user" ? "rounded-tr-none text-white" : "rounded-tl-none bg-[var(--bg-surface)] text-[var(--text-primary)]"
      }`} style={from==="user" ? { background:"linear-gradient(135deg,#6366F1,#8B5CF6)" } : {}}>
        {text}
      </span>
    </div>
  );
}

function UseCasesSection() {
  const [active, setActive] = useState(0);
  const tab = useCaseTabs[active];
  return (
    <section className="py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{ maxWidth: 640, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 48 }}>
          <h2 className="text-4xl font-bold tracking-tight text-[var(--text-primary)] sm:text-5xl">Built for every team</h2>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">One platform, tailored workflows for support, sales, and beyond.</p>
        </div>
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 40 }}>
          <div className="flex rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-1 gap-1 shadow-[var(--shadow-sm)]">
            {useCaseTabs.map((t,i) => (
              <button key={t.id} onClick={() => setActive(i)}
                className={`flex items-center gap-2 rounded-lg px-5 py-2.5 text-sm font-semibold transition-all ${
                  active===i ? "bg-[var(--accent)] text-white shadow-sm" : "text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)]"
                }`}>
                <t.icon size={14} /> {t.label}
              </button>
            ))}
          </div>
        </div>
        <div className="grid gap-12 lg:grid-cols-2 items-center">
          <div>
            <h3 className="text-3xl font-bold text-[var(--text-primary)]">{tab.headline}</h3>
            <p className="mt-4 text-[var(--text-secondary)] leading-relaxed">{tab.desc}</p>
            <ul className="mt-6 space-y-3">
              {tab.bullets.map(b => (
                <li key={b} className="flex items-center gap-3 text-sm text-[var(--text-primary)]">
                  <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[var(--success-bg)]">
                    <Check size={12} className="text-[var(--success)]" />
                  </span>
                  {b}
                </li>
              ))}
            </ul>
            <Link href="/signup"
              className="mt-8 inline-flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold text-white shadow-[var(--shadow-accent)] transition-all hover:-translate-y-0.5"
              style={{ background:"linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
              Try this use case free <ArrowRight size={14} />
            </Link>
          </div>
          <div className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-5 shadow-[var(--shadow-md)]">
            <div className="flex items-center gap-3 mb-4 pb-4 border-b border-[var(--border)]">
              <div className="h-9 w-9 rounded-full flex items-center justify-center" style={{ background:"linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                <tab.icon size={16} className="text-white" />
              </div>
              <div>
                <p className="text-sm font-bold text-[var(--text-primary)]">Threadly Bot</p>
                <p className="text-xs text-[var(--success)]">● Online</p>
              </div>
            </div>
            <div className="space-y-2.5">
              {tab.chat.map((m,i) => <ChatBubble key={i} from={m.from as any} text={m.text} />)}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

/* ── Pricing ──────────────────────────────────────────────────────────────── */
const pricingPlans = [
  {
    name:"Starter", price:"$29", period:"/mo", note:"Perfect for small teams", cta:"Start free trial",
    features:["1 workspace","3 chatbots","5,000 AI messages/month","Web widget embed","Knowledge base (10 docs)","Email support"],
    highlighted:false,
  },
  {
    name:"Growth", price:"$99", period:"/mo", note:"For scaling support & sales", cta:"Start free trial", badge:"Most Popular",
    features:["5 workspaces","20 chatbots","50,000 AI messages/month","All channels (WA, IG, TG, SMS)","Unlimited knowledge base docs","Live inbox + human handoff","Analytics & cost tracking","Priority support"],
    highlighted:true,
  },
  {
    name:"Enterprise", price:"Custom", period:"", note:"Security and volume at scale", cta:"Contact sales", href:"/contact",
    features:["Unlimited bots & workspaces","Dedicated infrastructure","SLA guarantee + audit exports","SSO & advanced RBAC","Custom integrations","Dedicated success manager","On-premise option"],
    highlighted:false,
  },
];

function PricingSection() {
  const [annual, setAnnual] = useState(false);
  return (
    <section id="pricing" className="py-24 sm:py-32 bg-[var(--bg-surface)]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{ maxWidth: 640, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 48 }}>
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">
            Simple pricing
          </span>
          <h2 className="text-4xl font-bold tracking-tight text-[var(--text-primary)] sm:text-5xl">
            Choose the right plan for your team
          </h2>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">Start free, scale when you grow. No hidden fees.</p>
          <div style={{ marginTop: 24, display: "inline-flex", alignItems: "center", gap: 4, borderRadius: 9999, border: "1px solid var(--border)", background: "var(--bg-panel)", padding: 4, boxShadow: "var(--shadow-sm)", fontSize: 14 }}>
            {["Monthly","Annual"].map(l => (
              <button key={l} onClick={() => setAnnual(l==="Annual")}
                className={`rounded-full px-4 py-1.5 font-semibold transition-colors ${
                  (l==="Annual")===annual ? "bg-[var(--accent)] text-white" : "text-[var(--text-muted)] hover:text-[var(--text-primary)]"
                }`}>
                {l}{l==="Annual" && <span className="ml-1.5 rounded-full bg-[var(--success-bg)] px-1.5 py-0.5 text-[10px] text-[var(--success)]">-20%</span>}
              </button>
            ))}
          </div>
        </div>
        <div className="grid gap-6 lg:grid-cols-3">
          {pricingPlans.map(plan => (
            <article key={plan.name}
              className={`relative flex flex-col rounded-2xl border p-7 transition-all ${
                plan.highlighted
                  ? "border-[var(--accent)] bg-[var(--bg-panel)] shadow-[0_0_0_1px_var(--accent),var(--shadow-xl)]"
                  : "border-[var(--border)] bg-[var(--bg-panel)] hover:border-[var(--border-strong)] hover:shadow-[var(--shadow-md)]"
              }`}>
              {plan.badge && (
                <div className="absolute -top-3.5 left-1/2 -translate-x-1/2">
                  <span className="rounded-full px-3 py-1 text-xs font-bold text-white" style={{ background:"linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                    {plan.badge}
                  </span>
                </div>
              )}
              <p className="text-xs font-bold uppercase tracking-widest text-[var(--text-muted)]">{plan.name}</p>
              <div className="mt-3 flex items-baseline gap-1">
                <span className="text-4xl font-bold text-[var(--text-primary)]">
                  {annual && plan.price!=="Custom" ? `$${Math.round(+plan.price.replace("$","")*0.8)}` : plan.price}
                </span>
                <span className="text-[var(--text-muted)]">{plan.period}</span>
              </div>
              <p className="mt-1 text-sm text-[var(--text-muted)]">{plan.note}</p>
              <ul className="my-7 flex-1 space-y-3 border-t border-[var(--border)] pt-7">
                {plan.features.map(f => (
                  <li key={f} className="flex items-start gap-2.5 text-sm text-[var(--text-secondary)]">
                    <Check size={13} className="mt-0.5 shrink-0 text-[var(--success)]" /> {f}
                  </li>
                ))}
              </ul>
              <Link href={(plan as any).href ?? "/signup"}
                className={`flex items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm font-bold transition-all hover:-translate-y-0.5 ${
                  plan.highlighted ? "text-white shadow-[var(--shadow-accent)]" : "border border-[var(--border)] bg-[var(--bg-surface)] text-[var(--text-primary)] hover:border-[var(--accent)] hover:text-[var(--accent)]"
                }`} style={plan.highlighted ? { background:"linear-gradient(135deg,#6366F1,#8B5CF6)" } : {}}>
                {plan.cta} <ArrowRight size={14} />
              </Link>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ── Testimonials ─────────────────────────────────────────────────────────── */
const testimonials = [
  { name:"Sarah Chen",     role:"Head of Support @ Helio",   text:"We went from 3-hour average first response time to under 30 seconds. Threadly handles 78% of our tickets without human involvement.", rating:5, avatar:"SC" },
  { name:"Marcus Williams",role:"VP Sales @ Orbit",           text:"Our sales bot qualifies leads while we sleep. It booked 40+ demos last month without a single SDR touching it. The ROI is insane.",       rating:5, avatar:"MW" },
  { name:"Priya Nair",     role:"Product Lead @ Draftly",     text:"The knowledge base RAG is genuinely impressive. Our bot answers product questions better than most of our junior support agents.",          rating:5, avatar:"PN" },
  { name:"Tom Fischer",    role:"CTO @ NorthStack",           text:"We evaluated Intercom, Crisp, and Tidio. Threadly is the only one that let us build custom logic flows without writing backend code.",      rating:5, avatar:"TF" },
];

function TestimonialsSection() {
  const [idx, setIdx] = useState(0);
  const show = [testimonials[idx], testimonials[(idx+1)%testimonials.length]];
  return (
    <section className="py-24 sm:py-32 border-t border-[var(--border)]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div style={{ maxWidth: 560, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 48 }}>
          <h2 className="text-4xl font-bold tracking-tight text-[var(--text-primary)]">Loved by teams worldwide</h2>
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          {show.map(t => (
            <div key={t.name} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-7 shadow-[var(--shadow-sm)]">
              <div className="flex mb-4">
                {Array.from({length:t.rating}).map((_,i) => (
                  <Star key={i} size={14} className="text-[var(--warn)]" fill="currentColor" />
                ))}
              </div>
              <p className="text-[var(--text-primary)] leading-relaxed">"{t.text}"</p>
              <div className="mt-5 flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-bold text-white"
                  style={{ background:"linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                  {t.avatar}
                </div>
                <div>
                  <p className="text-sm font-semibold text-[var(--text-primary)]">{t.name}</p>
                  <p className="text-xs text-[var(--text-muted)]">{t.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
        <div style={{ marginTop: 28, display: "flex", justifyContent: "center", gap: 12 }}>
          {[
            { fn:() => setIdx(i=>(i-1+testimonials.length)%testimonials.length), icon:ChevronLeft },
            { fn:() => setIdx(i=>(i+1)%testimonials.length),                     icon:ChevronRightIcon },
          ].map(({fn,icon:Icon},i) => (
            <button key={i} onClick={fn}
              className="flex h-9 w-9 items-center justify-center rounded-full border border-[var(--border)] bg-[var(--bg-panel)] text-[var(--text-secondary)] hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors shadow-sm">
              <Icon size={16} />
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ── Final CTA ────────────────────────────────────────────────────────────── */
function CtaSection() {
  return (
    <section style={{ padding: "80px 24px" }}>
      {/* Gradient banner — full-width flex column, align-items:center guarantees centring */}
      <div style={{
        maxWidth: 1200, marginLeft: "auto", marginRight: "auto",
        borderRadius: 28, overflow: "hidden", position: "relative",
        background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)",
      }}>
        {/* Dot pattern */}
        <div style={{ position: "absolute", inset: 0, opacity: 0.1,
          backgroundImage: "radial-gradient(circle, white 1px, transparent 1px)", backgroundSize: "32px 32px" }} />

        {/* Inner content — flex column, align-items:center = true centre */}
        <div style={{
          position: "relative", padding: "72px 48px",
          display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center",
        }}>
          <h2 style={{ fontSize: "clamp(2rem, 4vw, 3rem)", fontWeight: 800, color: "#fff", maxWidth: 600, lineHeight: 1.2 }}>
            Ready to launch your AI chatbot?
          </h2>
          <p style={{ marginTop: 20, fontSize: "1.125rem", color: "rgba(255,255,255,0.8)", maxWidth: 520, lineHeight: 1.7 }}>
            Join thousands of businesses automating support, sales, and onboarding with Threadly.
            Get started in minutes — no credit card required.
          </p>
          <div style={{ marginTop: 32, display: "flex", flexWrap: "wrap", gap: 12, justifyContent: "center" }}>
            <Link href="/signup" style={{
              display: "flex", alignItems: "center", gap: 8,
              borderRadius: 14, background: "#fff", padding: "12px 28px",
              fontSize: 14, fontWeight: 700, color: "#6366F1",
              textDecoration: "none", transition: "transform 150ms ease",
            }}>
              Start free trial <ArrowRight size={14} />
            </Link>
            <Link href="/product" style={{
              display: "flex", alignItems: "center", gap: 8,
              borderRadius: 14, border: "2px solid rgba(255,255,255,0.3)",
              background: "rgba(255,255,255,0.1)", padding: "12px 28px",
              fontSize: 14, fontWeight: 600, color: "#fff",
              textDecoration: "none", backdropFilter: "blur(8px)",
            }}>
              <Play size={14} /> Watch demo
            </Link>
          </div>
          <p style={{ marginTop: 16, fontSize: 13, color: "rgba(255,255,255,0.55)" }}>
            14-day free trial · No credit card · Cancel anytime
          </p>
        </div>
      </div>
    </section>
  );
}

/* ── Page ─────────────────────────────────────────────────────────────────── */
export default function LandingPage() {
  return (
    <>
      <HeroSection />
      <SocialProofSection />
      <FeaturesSection />
      <HowItWorksSection />
      <UseCasesSection />
      <PricingSection />
      <TestimonialsSection />
      <CtaSection />
    </>
  );
}
