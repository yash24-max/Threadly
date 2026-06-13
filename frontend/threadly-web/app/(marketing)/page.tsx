"use client";

import Link from "next/link";
import React, { useState, useEffect, useRef, useMemo } from "react";
import { motion, useInView, AnimatePresence, useReducedMotion } from "framer-motion";
import {
  ArrowRight, Play, Check, Star, ChevronDown,
  MousePointer2, Sparkles, MessageCircle, BarChart3,
  Languages, UserCheck, ShoppingCart, Stethoscope,
  Landmark, GraduationCap, Briefcase, Headphones,
  Send, Bot, Zap, Slack,
} from "lucide-react";

/* ═══════════════════════════════════════════════════════════════════
   UTILITIES
   ═══════════════════════════════════════════════════════════════════ */

const fadeUp = {
  initial: { opacity: 0, y: 24 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: "-80px" },
  transition: { duration: 0.6, ease: [0.16, 1, 0.3, 1] as const },
};

function SectionHeader({
  eyebrow, title, subtitle, align = "center",
}: {
  eyebrow?: string;
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  align?: "center" | "left";
}) {
  return (
    <motion.div
      {...fadeUp}
      className={`max-w-2xl ${align === "center" ? "mx-auto text-center" : ""} mb-14`}
    >
      {eyebrow && (
        <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-3 py-1 text-xs font-semibold uppercase tracking-widest text-white/70 backdrop-blur-sm">
          <span className="h-1.5 w-1.5 rounded-full bg-[#7C3AED]" />
          {eyebrow}
        </span>
      )}
      <h2 className="mt-4 text-4xl font-bold tracking-tight text-white sm:text-5xl">
        {title}
      </h2>
      {subtitle && (
        <p className="mt-5 text-lg leading-relaxed text-white/65">{subtitle}</p>
      )}
    </motion.div>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   HERO
   ═══════════════════════════════════════════════════════════════════ */

function FloatingParticles() {
  const reduce = useReducedMotion();
  const [mounted, setMounted] = useState(false);

  const particles = useMemo(
    () =>
      Array.from({ length: 18 }, (_, i) => ({
        id: i,
        left: Math.random() * 100,
        top: Math.random() * 100,
        delay: Math.random() * 4,
        size: 1 + Math.random() * 2,
        opacity: 0.25 + Math.random() * 0.5,
      })),
    []
  );

  useEffect(() => { setMounted(true); }, []);

  if (reduce || !mounted) return null;
  return (
    <div aria-hidden className="pointer-events-none absolute inset-0 overflow-hidden">
      {particles.map(p => (
        <span
          key={p.id}
          className="absolute rounded-full bg-white animate-float-slow"
          style={{
            left: `${p.left}%`,
            top: `${p.top}%`,
            width: p.size,
            height: p.size,
            opacity: p.opacity,
            animationDelay: `${p.delay}s`,
            boxShadow: "0 0 6px rgba(255,255,255,0.6)",
          }}
        />
      ))}
    </div>
  );
}

function ChatPreview() {
  const conversation = useMemo(
    () => [
      { from: "bot" as const,  text: "Hi! I'm Threadly. What can I help you build today? 👋" },
      { from: "user" as const, text: "I need a support bot for my Shopify store" },
      { from: "bot" as const,  text: "Perfect. I'll create one trained on your help docs and connect it to WhatsApp." },
      { from: "user" as const, text: "How fast can it go live?" },
      { from: "bot" as const,  text: "Under 5 minutes. Want me to start the setup? ✨" },
    ],
    []
  );

  const [shown, setShown] = useState<number>(1);
  const [typing, setTyping] = useState(false);

  useEffect(() => {
    if (shown >= conversation.length) {
      const reset = setTimeout(() => setShown(1), 4000);
      return () => clearTimeout(reset);
    }
    setTyping(true);
    const t1 = setTimeout(() => {
      setTyping(false);
      setShown(s => s + 1);
    }, 1500);
    return () => clearTimeout(t1);
  }, [shown, conversation.length]);

  return (
    <div className="relative mx-auto w-full max-w-sm">
      {/* Glow halo behind */}
      <div
        aria-hidden
        className="pointer-events-none absolute -inset-6 rounded-[32px] opacity-70 blur-3xl"
        style={{ background: "radial-gradient(circle at 30% 30%, rgba(124,58,237,0.55), transparent 60%), radial-gradient(circle at 70% 80%, rgba(6,182,212,0.45), transparent 65%)" }}
      />

      <div className="relative glass-card overflow-hidden rounded-3xl shadow-[0_24px_80px_rgba(0,0,0,0.55)]">
        {/* Header */}
        <div className="flex items-center gap-3 border-b border-white/10 bg-white/[0.03] px-5 py-4">
          <div
            className="relative flex h-10 w-10 items-center justify-center rounded-full"
            style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
          >
            <Bot size={18} className="text-white" />
            <span className="absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full border-2 border-[#0B1020] bg-emerald-400" />
          </div>
          <div>
            <p className="text-sm font-semibold text-white">Threadly AI</p>
            <p className="flex items-center gap-1 text-xs text-emerald-400">
              <span className="h-1 w-1 rounded-full bg-emerald-400" />
              Online · responds instantly
            </p>
          </div>
        </div>

        {/* Messages */}
        <div className="flex h-[360px] flex-col gap-2.5 overflow-hidden bg-gradient-to-b from-white/[0.01] to-transparent p-5">
          <AnimatePresence initial={false}>
            {conversation.slice(0, shown).map((m, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 10, scale: 0.96 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
                className={`flex ${m.from === "user" ? "justify-end" : "justify-start"}`}
              >
                <span
                  className={`max-w-[80%] rounded-2xl px-3.5 py-2.5 text-sm leading-snug ${
                    m.from === "user"
                      ? "rounded-tr-sm text-white shadow-[0_4px_16px_rgba(124,58,237,0.35)]"
                      : "rounded-tl-sm bg-white/[0.06] text-white/90 backdrop-blur-sm border border-white/[0.05]"
                  }`}
                  style={
                    m.from === "user"
                      ? { background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }
                      : undefined
                  }
                >
                  {m.text}
                </span>
              </motion.div>
            ))}
            {typing && shown < conversation.length && conversation[shown].from === "bot" && (
              <motion.div
                key="typing"
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                className="flex justify-start"
              >
                <span className="flex items-center gap-1 rounded-2xl rounded-tl-sm border border-white/[0.05] bg-white/[0.06] px-4 py-3">
                  {[0, 1, 2].map(i => (
                    <span
                      key={i}
                      className="h-1.5 w-1.5 rounded-full bg-white/70"
                      style={{
                        animation: `typing-dot 1.2s ease-in-out ${i * 0.15}s infinite`,
                      }}
                    />
                  ))}
                </span>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Input */}
        <div className="border-t border-white/10 bg-white/[0.02] px-5 py-3.5">
          <div className="flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] pl-4 pr-1.5 py-1.5">
            <span className="text-sm text-white/40">Message Threadly...</span>
            <div className="ml-auto flex h-8 w-8 items-center justify-center rounded-full" style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}>
              <Send size={13} className="text-white" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function HeroSection() {
  const reduce = useReducedMotion();
  const ref = useRef<HTMLDivElement | null>(null);
  const [glow, setGlow] = useState({ x: 0, y: 0, visible: false });

  function onMove(e: React.MouseEvent<HTMLDivElement>) {
    if (reduce) return;
    const rect = e.currentTarget.getBoundingClientRect();
    setGlow({ x: e.clientX - rect.left, y: e.clientY - rect.top, visible: true });
  }

  return (
    <section
      ref={ref}
      onMouseMove={onMove}
      onMouseLeave={() => setGlow(g => ({ ...g, visible: false }))}
      className="landing-mesh relative overflow-hidden pt-20 pb-28 sm:pt-28 sm:pb-36"
    >
      {/* Animated blobs */}
      <div aria-hidden className="pointer-events-none absolute inset-0">
        <div
          className="absolute -top-32 left-[10%] h-[480px] w-[480px] rounded-full opacity-60 blur-3xl animate-blob"
          style={{ background: "radial-gradient(circle, rgba(124,58,237,0.55) 0%, transparent 60%)" }}
        />
        <div
          className="absolute top-[20%] right-[5%] h-[420px] w-[420px] rounded-full opacity-50 blur-3xl animate-blob-slow"
          style={{ background: "radial-gradient(circle, rgba(6,182,212,0.55) 0%, transparent 65%)" }}
        />
        <div
          className="absolute bottom-[-10%] left-1/3 h-[360px] w-[360px] rounded-full opacity-40 blur-3xl animate-blob"
          style={{ background: "radial-gradient(circle, rgba(99,102,241,0.45) 0%, transparent 65%)", animationDelay: "4s" }}
        />
      </div>

      {/* Grid texture */}
      <div aria-hidden className="landing-grid pointer-events-none absolute inset-0" />

      {/* Particles */}
      <FloatingParticles />

      {/* Cursor follow glow */}
      {glow.visible && !reduce && (
        <div
          aria-hidden
          className="pointer-events-none absolute h-72 w-72 rounded-full opacity-30 blur-3xl transition-opacity"
          style={{
            left: glow.x - 144,
            top: glow.y - 144,
            background: "radial-gradient(circle, rgba(124,58,237,0.6) 0%, transparent 70%)",
          }}
        />
      )}

      <div className="relative mx-auto grid max-w-7xl gap-14 px-4 sm:px-6 lg:grid-cols-2 lg:gap-12 lg:px-8">
        {/* LEFT */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
          className="flex flex-col items-start"
        >
          {/* Badge */}
          <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-3.5 py-1.5 text-xs font-medium text-white/80 backdrop-blur-md">
            <Sparkles size={12} className="text-[#A78BFA]" />
            New · GPT-4o + multi-channel agents
            <span className="text-white/30">·</span>
            <span className="text-[#06B6D4]">v2.0 →</span>
          </span>

          {/* Headline */}
          <h1 className="mt-6 text-5xl font-bold leading-[1.05] tracking-tight text-white sm:text-6xl lg:text-[4.25rem]">
            Build AI Chatbots{" "}
            <span className="relative inline-block">
              <span className="landing-gradient-text">Without Writing Code</span>
              <svg
                aria-hidden
                viewBox="0 0 320 12"
                className="absolute -bottom-2 left-0 h-3 w-full"
                preserveAspectRatio="none"
              >
                <path
                  d="M2 8 C 80 2, 160 12, 318 4"
                  stroke="url(#hl-grad)"
                  strokeWidth="3"
                  strokeLinecap="round"
                  fill="none"
                />
                <defs>
                  <linearGradient id="hl-grad" x1="0" x2="1">
                    <stop offset="0%" stopColor="#7C3AED" />
                    <stop offset="100%" stopColor="#06B6D4" />
                  </linearGradient>
                </defs>
              </svg>
            </span>
          </h1>

          {/* Subtitle */}
          <p className="mt-7 max-w-xl text-lg leading-relaxed text-white/70">
            Create intelligent customer support, sales, and automation chatbots in
            minutes using our visual drag-and-drop builder.
          </p>

          {/* CTA buttons */}
          <div className="mt-9 flex flex-wrap items-center gap-3">
            <Link
              href="/signup"
              className="group relative flex items-center gap-2 overflow-hidden rounded-2xl px-7 py-3.5 text-sm font-semibold text-white shadow-[0_12px_40px_rgba(124,58,237,0.50)] transition-all hover:-translate-y-0.5"
              style={{ background: "linear-gradient(135deg,#7C3AED 0%,#06B6D4 100%)" }}
            >
              <span className="relative z-10">Start Free</span>
              <ArrowRight size={16} className="relative z-10 transition-transform group-hover:translate-x-0.5" />
              <span className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/25 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
            </Link>
            <button
              type="button"
              className="group flex items-center gap-2.5 rounded-2xl border border-white/15 bg-white/[0.04] px-6 py-3.5 text-sm font-semibold text-white backdrop-blur-md transition-all hover:-translate-y-0.5 hover:border-white/30 hover:bg-white/[0.08]"
            >
              <span className="flex h-7 w-7 items-center justify-center rounded-full bg-white/10 transition-colors group-hover:bg-white/20">
                <Play size={11} className="ml-0.5 fill-white text-white" />
              </span>
              Watch Demo
              <span className="text-xs text-white/40">2 min</span>
            </button>
          </div>

          {/* Trust row */}
          <div className="mt-10 flex flex-wrap items-center gap-x-7 gap-y-2 text-xs text-white/55">
            {["No credit card", "14-day free trial", "Cancel anytime"].map(t => (
              <span key={t} className="flex items-center gap-1.5">
                <Check size={12} className="text-emerald-400" /> {t}
              </span>
            ))}
          </div>
        </motion.div>

        {/* RIGHT — Chat preview */}
        <motion.div
          initial={{ opacity: 0, y: 20, scale: 0.97 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.8, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
          className="relative lg:pl-8"
        >
          <ChatPreview />
        </motion.div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   TRUSTED BY (marquee)
   ═══════════════════════════════════════════════════════════════════ */

const trustedLogos = [
  "Acme", "Northwind", "Globex", "Initech", "Stark Labs",
  "Wayne Co.", "Hooli", "Pied Piper", "Soylent", "Vehement",
  "Massive Dynamic", "Cyberdyne",
];

function TrustedBy() {
  return (
    <section className="border-y border-white/5 bg-black/20 py-12">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <p className="mb-8 text-center text-xs font-semibold uppercase tracking-[0.25em] text-white/40">
          Trusted by teams at fast-growing companies
        </p>

        <div className="relative overflow-hidden [mask-image:linear-gradient(to_right,transparent,black_10%,black_90%,transparent)]">
          <div className="marquee-track animate-marquee">
            {[...trustedLogos, ...trustedLogos].map((logo, i) => (
              <div
                key={`${logo}-${i}`}
                className="flex items-center gap-2 whitespace-nowrap text-2xl font-semibold tracking-tight text-white/40 transition-colors hover:text-white"
                style={{ minWidth: "fit-content" }}
              >
                <span className="h-2 w-2 rounded-sm bg-gradient-to-br from-[#7C3AED] to-[#06B6D4] opacity-70" />
                {logo}
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   FEATURES
   ═══════════════════════════════════════════════════════════════════ */

const features = [
  {
    icon: MousePointer2,
    title: "Drag & Drop Builder",
    desc: "Visual canvas with conditional branches, API calls, and AI nodes. Build any flow without code.",
    gradient: "from-[#7C3AED] to-[#A78BFA]",
  },
  {
    icon: Sparkles,
    title: "GPT Integration",
    desc: "GPT-4o, Claude, and Gemini natively integrated. Bring your own key or use ours.",
    gradient: "from-[#A78BFA] to-[#06B6D4]",
  },
  {
    icon: MessageCircle,
    title: "WhatsApp & Web Chat",
    desc: "Deploy on your website, WhatsApp Business, Instagram, Telegram, and SMS — one dashboard.",
    gradient: "from-[#06B6D4] to-[#10B981]",
  },
  {
    icon: BarChart3,
    title: "Live Analytics",
    desc: "Real-time metrics on resolution rate, response time, deflection, and cost per conversation.",
    gradient: "from-[#10B981] to-[#7C3AED]",
  },
  {
    icon: Languages,
    title: "Multi-language Support",
    desc: "Auto-detect language and respond fluently in 95+ languages with native-level accuracy.",
    gradient: "from-[#7C3AED] to-[#EC4899]",
  },
  {
    icon: UserCheck,
    title: "Human Handoff",
    desc: "Seamless escalation to live agents with full context, transcript, and sentiment included.",
    gradient: "from-[#EC4899] to-[#06B6D4]",
  },
];

function FeaturesSection() {
  return (
    <section id="features" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Features"
          title={<>Everything you need to <span className="landing-gradient-text">ship AI agents</span></>}
          subtitle="From the first message to full deployment — Threadly handles every layer of your conversational AI stack."
        />

        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f, i) => (
            <motion.article
              key={f.title}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.55, delay: i * 0.06, ease: [0.16, 1, 0.3, 1] }}
              className="group glass-card relative overflow-hidden p-7 transition-all duration-300 hover:-translate-y-1 hover:border-white/20 hover:shadow-[0_24px_60px_rgba(124,58,237,0.25)]"
            >
              {/* Hover glow */}
              <div
                aria-hidden
                className={`pointer-events-none absolute -inset-px rounded-[20px] opacity-0 transition-opacity duration-300 group-hover:opacity-100 bg-gradient-to-br ${f.gradient} blur-2xl`}
                style={{ opacity: 0 }}
              />
              <div className="relative">
                <div
                  className={`mb-5 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br ${f.gradient} shadow-lg transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3`}
                >
                  <f.icon size={20} className="text-white" />
                </div>
                <h3 className="text-lg font-bold text-white">{f.title}</h3>
                <p className="mt-2.5 text-sm leading-relaxed text-white/65">{f.desc}</p>
              </div>
              {/* Bottom accent line */}
              <div
                aria-hidden
                className={`absolute bottom-0 left-6 right-6 h-px scale-x-0 bg-gradient-to-r ${f.gradient} opacity-70 transition-transform duration-500 group-hover:scale-x-100`}
              />
            </motion.article>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   INTERACTIVE BUILDER DEMO
   ═══════════════════════════════════════════════════════════════════ */

function BuilderDemoSection() {
  const nodes = [
    { x: 24,  y: 30,  label: "Welcome",      icon: "👋",  color: "#7C3AED", w: 130 },
    { x: 200, y: 18,  label: "Detect Intent",icon: "🧠",  color: "#A78BFA", w: 150 },
    { x: 200, y: 130, label: "Knowledge Q&A",icon: "📚",  color: "#06B6D4", w: 150 },
    { x: 410, y: 30,  label: "GPT Answer",   icon: "✨",  color: "#06B6D4", w: 140 },
    { x: 410, y: 130, label: "Human Agent",  icon: "🧑‍💻", color: "#EC4899", w: 140 },
  ];

  const edges = [
    { d: "M 154 50 C 178 50 178 38 200 38",   color: "#7C3AED" },
    { d: "M 154 60 C 178 60 178 150 200 150", color: "#7C3AED" },
    { d: "M 350 38 C 380 38 380 50 410 50",   color: "#A78BFA" },
    { d: "M 350 150 C 380 150 380 150 410 150", color: "#06B6D4" },
  ];

  // Animated chat on right
  const chat = useMemo(
    () => [
      { from: "user" as const, text: "Where's my order?" },
      { from: "bot"  as const, text: "Hi! Let me check that for you. Could you share your order number? 📦" },
      { from: "user" as const, text: "#A-23142" },
      { from: "bot"  as const, text: "Found it! Your order shipped yesterday and arrives Friday. Tracking link sent ✓" },
    ],
    []
  );
  const [step, setStep] = useState(1);
  useEffect(() => {
    if (step >= chat.length) {
      const t = setTimeout(() => setStep(1), 3500);
      return () => clearTimeout(t);
    }
    const t = setTimeout(() => setStep(s => s + 1), 1700);
    return () => clearTimeout(t);
  }, [step, chat.length]);

  return (
    <section className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Builder"
          title={<>Design conversations <span className="landing-gradient-text">visually</span>, deploy instantly</>}
          subtitle="Drag, drop, connect. Your changes go live the moment you save — no rebuilds, no waiting."
        />

        <motion.div
          {...fadeUp}
          className="glass-card grid overflow-hidden lg:grid-cols-[1.3fr_1fr]"
        >
          {/* Builder canvas */}
          <div className="relative border-b border-white/10 lg:border-b-0 lg:border-r">
            {/* Window chrome */}
            <div className="flex items-center gap-2 border-b border-white/10 bg-white/[0.03] px-4 py-3">
              <span className="h-2.5 w-2.5 rounded-full bg-[#EF4444]/80" />
              <span className="h-2.5 w-2.5 rounded-full bg-[#F59E0B]/80" />
              <span className="h-2.5 w-2.5 rounded-full bg-[#10B981]/80" />
              <span className="ml-2 rounded-md bg-white/[0.04] px-3 py-1 font-mono text-[11px] text-white/50">
                threadly.dev/builder · Support Flow
              </span>
              <span className="ml-auto flex items-center gap-1 text-[10px] font-semibold uppercase tracking-widest text-emerald-400">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" /> Live
              </span>
            </div>

            <div
              className="relative h-[420px] overflow-hidden"
              style={{
                backgroundImage:
                  "radial-gradient(circle, rgba(255,255,255,0.04) 1px, transparent 1px)",
                backgroundSize: "22px 22px",
              }}
            >
              {/* Edges */}
              <svg className="absolute inset-0 h-full w-full pointer-events-none" viewBox="0 0 600 220" preserveAspectRatio="none">
                {edges.map((e, i) => (
                  <path
                    key={i}
                    d={e.d}
                    stroke={e.color}
                    strokeWidth="1.8"
                    fill="none"
                    strokeDasharray="6 4"
                    className="animate-dash"
                    opacity="0.7"
                  />
                ))}
              </svg>

              {/* Nodes */}
              {nodes.map((n, i) => (
                <motion.div
                  key={n.label}
                  initial={{ opacity: 0, scale: 0.9 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: 0.1 + i * 0.08 }}
                  className="absolute flex items-center gap-2 rounded-xl border border-white/15 bg-white/[0.06] px-3 py-2 backdrop-blur-md shadow-lg"
                  style={{ left: n.x, top: n.y, width: n.w, boxShadow: `0 8px 24px ${n.color}33` }}
                >
                  <span
                    className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg text-sm"
                    style={{ background: `${n.color}33`, border: `1px solid ${n.color}88` }}
                  >
                    {n.icon}
                  </span>
                  <span className="truncate text-xs font-semibold text-white">{n.label}</span>
                  <span className="ml-auto h-1.5 w-1.5 rounded-full" style={{ background: n.color }} />
                </motion.div>
              ))}

              {/* Floating chip cursor */}
              <motion.div
                aria-hidden
                animate={{ x: [0, 220, 220, 0, 0], y: [0, 0, 100, 100, 0] }}
                transition={{ duration: 8, repeat: Infinity, ease: "easeInOut" }}
                className="absolute left-[160px] top-[60px] flex items-center gap-1.5 rounded-full border border-white/20 bg-[#0B1020]/80 px-2 py-1 backdrop-blur-md"
              >
                <MousePointer2 size={11} className="text-white" />
                <span className="text-[10px] font-semibold text-white">Sarah</span>
              </motion.div>
            </div>

            {/* Toolbar */}
            <div className="flex items-center gap-2 border-t border-white/10 bg-white/[0.02] px-4 py-2.5">
              {["Welcome", "AI", "Condition", "API", "Handoff"].map((t, i) => (
                <span
                  key={t}
                  className="rounded-md border border-white/10 bg-white/[0.04] px-2.5 py-1 text-[11px] font-medium text-white/70"
                >
                  {t}
                </span>
              ))}
              <span className="ml-auto text-[10px] text-white/40">5 nodes · auto-saved</span>
            </div>
          </div>

          {/* Live chat preview */}
          <div className="relative bg-gradient-to-b from-white/[0.02] to-transparent">
            <div className="flex items-center justify-between border-b border-white/10 px-5 py-3.5">
              <div className="flex items-center gap-2.5">
                <div
                  className="flex h-8 w-8 items-center justify-center rounded-full"
                  style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
                >
                  <Bot size={14} className="text-white" />
                </div>
                <div>
                  <p className="text-xs font-bold text-white">Live Preview</p>
                  <p className="text-[10px] text-emerald-400">Connected</p>
                </div>
              </div>
              <span className="rounded-md border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[10px] uppercase tracking-widest text-white/50">
                Real-time
              </span>
            </div>

            <div className="flex h-[420px] flex-col gap-2 overflow-hidden p-5">
              <AnimatePresence initial={false}>
                {chat.slice(0, step).map((m, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.35 }}
                    className={`flex ${m.from === "user" ? "justify-end" : "justify-start"}`}
                  >
                    <span
                      className={`max-w-[85%] rounded-2xl px-3.5 py-2 text-xs leading-snug ${
                        m.from === "user"
                          ? "rounded-tr-sm text-white"
                          : "rounded-tl-sm bg-white/[0.06] text-white/90 border border-white/[0.05]"
                      }`}
                      style={
                        m.from === "user"
                          ? { background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }
                          : undefined
                      }
                    >
                      {m.text}
                    </span>
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   STATS / COUNTERS
   ═══════════════════════════════════════════════════════════════════ */

function Counter({ value, suffix = "", decimals = 0 }: { value: number; suffix?: string; decimals?: number }) {
  const ref = useRef<HTMLSpanElement | null>(null);
  const inView = useInView(ref, { once: true, margin: "-60px" });
  const [displayed, setDisplayed] = useState(0);

  useEffect(() => {
    if (!inView) return;
    const duration = 1800;
    const start = performance.now();
    let raf = 0;
    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      setDisplayed(value * eased);
      if (t < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [inView, value]);

  const formatted = useMemo(() => {
    if (decimals > 0) return displayed.toFixed(decimals);
    if (value >= 1_000_000) return `${(displayed / 1_000_000).toFixed(1)}M`;
    if (value >= 1_000)     return `${Math.round(displayed / 1_000)}K`;
    return Math.round(displayed).toString();
  }, [displayed, value, decimals]);

  return (
    <span ref={ref}>
      {formatted}
      {suffix}
    </span>
  );
}

const statsData = [
  { value: 10_000_000, suffix: "+",  label: "Messages Processed", decimals: 0 },
  { value: 50_000,     suffix: "+",  label: "Bots Created",        decimals: 0 },
  { value: 120,        suffix: "+",  label: "Countries",           decimals: 0 },
  { value: 99.99,      suffix: "%",  label: "Uptime",              decimals: 2 },
];

function StatsSection() {
  return (
    <section className="relative py-24 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <motion.div
          {...fadeUp}
          className="glass-card relative overflow-hidden p-10 sm:p-14"
        >
          <div
            aria-hidden
            className="pointer-events-none absolute inset-0 opacity-50"
            style={{
              background:
                "radial-gradient(ellipse 60% 50% at 10% 10%, rgba(124,58,237,0.25), transparent 60%), radial-gradient(ellipse 60% 50% at 90% 90%, rgba(6,182,212,0.20), transparent 60%)",
            }}
          />
          <div className="relative grid grid-cols-2 gap-y-10 lg:grid-cols-4">
            {statsData.map((s, i) => (
              <motion.div
                key={s.label}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.08 }}
                className="px-4 text-center lg:border-l lg:border-white/10 lg:first:border-l-0"
              >
                <p className="landing-gradient-text text-5xl font-bold leading-none tracking-tight sm:text-6xl">
                  <Counter value={s.value} suffix={s.suffix} decimals={s.decimals} />
                </p>
                <p className="mt-3 text-xs font-semibold uppercase tracking-[0.18em] text-white/55">
                  {s.label}
                </p>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   TEMPLATES
   ═══════════════════════════════════════════════════════════════════ */

const templates = [
  { icon: Headphones,     title: "Customer Support", desc: "Resolve 80% of tickets automatically with deflection flows.", tags: ["FAQ", "Tickets", "Returns"], color: "#7C3AED" },
  { icon: ShoppingCart,   title: "E-commerce",       desc: "Order tracking, recommendations, abandoned-cart recovery.",      tags: ["Orders", "Recos", "Cart"],    color: "#06B6D4" },
  { icon: Stethoscope,    title: "Healthcare",        desc: "Symptom triage, appointment booking, HIPAA-ready flows.",       tags: ["Triage", "Booking", "Records"], color: "#10B981" },
  { icon: Landmark,       title: "Banking",           desc: "Account balance, fraud alerts, KYC, secure auth.",              tags: ["KYC", "Alerts", "Auth"],      color: "#A78BFA" },
  { icon: GraduationCap,  title: "Education",         desc: "Course Q&A, enrollment, study buddy with citations.",            tags: ["Q&A", "Enroll", "Tutoring"],  color: "#EC4899" },
  { icon: Briefcase,      title: "HR Assistant",      desc: "Candidate screening, employee FAQ, policy lookup.",              tags: ["FAQ", "Hiring", "Policy"],    color: "#F59E0B" },
];

function TemplatesSection() {
  return (
    <section id="templates" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Templates"
          title={<>Launch in minutes with <span className="landing-gradient-text">battle-tested templates</span></>}
          subtitle="Hand-crafted for every industry. Customize the copy and you're live."
        />

        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {templates.map((t, i) => (
            <motion.article
              key={t.title}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.55, delay: i * 0.06, ease: [0.16, 1, 0.3, 1] }}
              className="group glass-card relative cursor-pointer overflow-hidden p-7 transition-all duration-300 hover:-translate-y-1.5 hover:border-white/20"
              style={{
                boxShadow: `0 0 0 1px rgba(255,255,255,0.05)`,
              }}
            >
              <div
                aria-hidden
                className="pointer-events-none absolute -inset-px rounded-[20px] opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                style={{ boxShadow: `inset 0 0 0 1px ${t.color}55, 0 24px 60px ${t.color}25` }}
              />
              <div className="relative">
                <div
                  className="mb-5 inline-flex h-12 w-12 items-center justify-center rounded-xl transition-transform duration-300 group-hover:scale-110 group-hover:-rotate-3"
                  style={{ background: `${t.color}22`, border: `1px solid ${t.color}55`, boxShadow: `0 0 24px ${t.color}33` }}
                >
                  <t.icon size={20} style={{ color: t.color }} />
                </div>
                <h3 className="text-lg font-bold text-white">{t.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-white/65">{t.desc}</p>
                <div className="mt-5 flex flex-wrap gap-1.5">
                  {t.tags.map(tag => (
                    <span
                      key={tag}
                      className="rounded-md border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[11px] font-medium text-white/60"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
                <div className="mt-6 flex items-center gap-1.5 text-sm font-semibold text-white transition-all group-hover:gap-2.5" style={{ color: t.color }}>
                  Use template <ArrowRight size={14} />
                </div>
              </div>
            </motion.article>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   INTEGRATIONS
   ═══════════════════════════════════════════════════════════════════ */

const integrations = [
  { name: "OpenAI",     icon: "○",  hue: "#10A37F" },
  { name: "WhatsApp",   icon: "💬", hue: "#25D366" },
  { name: "Slack",      icon: "⚡", hue: "#4A154B" },
  { name: "Salesforce", icon: "☁",  hue: "#00A1E0" },
  { name: "Shopify",    icon: "🛍", hue: "#95BF47" },
  { name: "HubSpot",    icon: "🟠", hue: "#FF7A59" },
  { name: "Zapier",     icon: "⚡", hue: "#FF4F00" },
  { name: "Discord",    icon: "🎮", hue: "#5865F2" },
];

function IntegrationsSection() {
  return (
    <section id="integrations" className="relative overflow-hidden py-24 sm:py-32">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 opacity-60"
        style={{
          background:
            "radial-gradient(ellipse 80% 50% at 50% 50%, rgba(124,58,237,0.12), transparent 70%)",
        }}
      />

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Integrations"
          title={<>Plug into your <span className="landing-gradient-text">entire stack</span></>}
          subtitle="Native connectors for the tools your team already uses. 100+ more via Zapier."
        />

        <div className="relative mx-auto max-w-4xl">
          {/* Central hub */}
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            whileInView={{ scale: 1, opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
            className="relative mx-auto flex h-28 w-28 items-center justify-center rounded-3xl animate-glow-pulse"
            style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
          >
            <Bot size={42} className="text-white" />
            <div className="absolute inset-0 rounded-3xl border border-white/20" />
          </motion.div>

          <p className="mt-3 text-center text-xs font-semibold uppercase tracking-widest text-white/50">
            Threadly Core
          </p>

          {/* Integration ring */}
          <div className="mt-12 grid gap-4 sm:grid-cols-4">
            {integrations.map((int, i) => (
              <motion.div
                key={int.name}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.06 }}
                className="group glass-card relative flex items-center gap-3 px-4 py-4 transition-all duration-300 hover:-translate-y-1 hover:border-white/20"
              >
                <span
                  className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-lg transition-transform group-hover:scale-110"
                  style={{ background: `${int.hue}1F`, border: `1px solid ${int.hue}55`, color: int.hue }}
                >
                  {int.icon}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-white">{int.name}</p>
                  <p className="text-[11px] text-white/50">Connected</p>
                </div>
                <span
                  className="ml-auto h-1.5 w-1.5 shrink-0 rounded-full"
                  style={{ background: int.hue, boxShadow: `0 0 12px ${int.hue}` }}
                />
              </motion.div>
            ))}
          </div>

          {/* Connecting lines decoration */}
          <svg
            aria-hidden
            className="pointer-events-none absolute inset-0 -z-0 hidden h-full w-full sm:block"
            preserveAspectRatio="none"
            viewBox="0 0 800 400"
          >
            <defs>
              <linearGradient id="line-grad" x1="0" x2="1">
                <stop offset="0%" stopColor="#7C3AED" stopOpacity="0.6" />
                <stop offset="100%" stopColor="#06B6D4" stopOpacity="0.1" />
              </linearGradient>
            </defs>
            {[
              "M 400 110 L 130 280", "M 400 110 L 310 280",
              "M 400 110 L 490 280", "M 400 110 L 670 280",
            ].map((d, i) => (
              <path key={i} d={d} stroke="url(#line-grad)" strokeWidth="1" fill="none" strokeDasharray="3 4" className="animate-dash" />
            ))}
          </svg>
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   TESTIMONIALS
   ═══════════════════════════════════════════════════════════════════ */

const testimonials = [
  {
    text: "We replaced three tools with Threadly. Our team ships new flows in hours, not weeks. The visual builder is a game changer.",
    name: "Sarah Chen", role: "Head of Support, Helio", avatar: "SC", company: "HELIO", rating: 5,
  },
  {
    text: "Our sales bot booked 40+ demos last month with zero SDR effort. The qualification logic is shockingly good out of the box.",
    name: "Marcus Williams", role: "VP Sales, Orbit", avatar: "MW", company: "ORBIT", rating: 5,
  },
  {
    text: "The knowledge-base RAG is genuinely impressive. Our bot answers product questions better than most junior agents.",
    name: "Priya Nair", role: "Product Lead, Draftly", avatar: "PN", company: "DRAFTLY", rating: 5,
  },
];

function TestimonialsSection() {
  return (
    <section className="py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Testimonials"
          title={<>Loved by teams <span className="landing-gradient-text">worldwide</span></>}
          subtitle="From early-stage startups to enterprise teams shipping at scale."
        />

        <div className="grid gap-6 lg:grid-cols-3">
          {testimonials.map((t, i) => (
            <motion.figure
              key={t.name}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.6, delay: i * 0.08, ease: [0.16, 1, 0.3, 1] }}
              className="glass-card relative flex flex-col p-7 transition-all duration-300 hover:-translate-y-1"
            >
              <div className="flex items-center gap-1 mb-4">
                {Array.from({ length: t.rating }).map((_, j) => (
                  <Star key={j} size={14} className="text-amber-400" fill="currentColor" />
                ))}
              </div>
              <blockquote className="flex-1 text-[15px] leading-relaxed text-white/85">
                &ldquo;{t.text}&rdquo;
              </blockquote>
              <figcaption className="mt-6 flex items-center gap-3 border-t border-white/10 pt-5">
                <div
                  className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full text-sm font-bold text-white"
                  style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
                >
                  {t.avatar}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold text-white">{t.name}</p>
                  <p className="truncate text-xs text-white/55">{t.role}</p>
                </div>
                <span className="rounded-md border border-white/10 bg-white/[0.04] px-2 py-1 text-[10px] font-bold tracking-widest text-white/60">
                  {t.company}
                </span>
              </figcaption>
            </motion.figure>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   PRICING
   ═══════════════════════════════════════════════════════════════════ */

const plans = [
  {
    name: "Starter",
    price: { monthly: 29, annual: 23 },
    note: "Perfect for small teams",
    cta: "Start free",
    features: [
      "1 workspace",
      "3 chatbots",
      "5,000 AI messages/mo",
      "Web widget embed",
      "Knowledge base (10 docs)",
      "Email support",
    ],
    highlight: false,
  },
  {
    name: "Professional",
    price: { monthly: 99, annual: 79 },
    note: "For scaling support & sales",
    cta: "Start free trial",
    badge: "Most Popular",
    features: [
      "5 workspaces",
      "20 chatbots",
      "50,000 AI messages/mo",
      "All channels (WA, IG, TG, SMS)",
      "Unlimited knowledge base",
      "Live inbox + human handoff",
      "Analytics & cost tracking",
      "Priority support",
    ],
    highlight: true,
  },
  {
    name: "Enterprise",
    price: null,
    note: "Security & volume at scale",
    cta: "Contact sales",
    href: "/contact",
    features: [
      "Unlimited bots & workspaces",
      "Dedicated infrastructure",
      "SLA + audit exports",
      "SSO + advanced RBAC",
      "Custom integrations",
      "Dedicated success manager",
      "On-premise option",
    ],
    highlight: false,
  },
];

function PricingSection() {
  const [annual, setAnnual] = useState(true);

  return (
    <section id="pricing" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="Pricing"
          title={<>Simple pricing, <span className="landing-gradient-text">no surprises</span></>}
          subtitle="Start free, scale when you grow. Cancel any time."
        />

        {/* Billing toggle */}
        <div className="mb-12 flex justify-center">
          <div className="inline-flex items-center gap-1 rounded-full border border-white/10 bg-white/[0.04] p-1 backdrop-blur-md">
            {[
              { label: "Monthly", value: false },
              { label: "Annual",  value: true  },
            ].map(opt => (
              <button
                key={opt.label}
                type="button"
                onClick={() => setAnnual(opt.value)}
                className={`relative rounded-full px-5 py-1.5 text-sm font-semibold transition-colors ${
                  annual === opt.value ? "text-white" : "text-white/55 hover:text-white/80"
                }`}
              >
                {annual === opt.value && (
                  <motion.span
                    layoutId="billing-pill"
                    className="absolute inset-0 rounded-full"
                    style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
                    transition={{ type: "spring", stiffness: 380, damping: 30 }}
                  />
                )}
                <span className="relative">{opt.label}</span>
                {opt.value && (
                  <span className="relative ml-1.5 rounded-full bg-emerald-400/20 px-1.5 py-0.5 text-[10px] font-bold text-emerald-300">
                    -20%
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-3 lg:items-center">
          {plans.map((plan, i) => (
            <motion.article
              key={plan.name}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.6, delay: i * 0.08, ease: [0.16, 1, 0.3, 1] }}
              className={`relative flex flex-col p-8 transition-all duration-300 hover:-translate-y-1 ${
                plan.highlight ? "glass-card glow-border lg:scale-105" : "glass-card"
              }`}
            >
              {plan.badge && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                  <span
                    className="rounded-full px-3 py-1 text-xs font-bold text-white shadow-[0_8px_24px_rgba(124,58,237,0.45)]"
                    style={{ background: "linear-gradient(135deg,#7C3AED,#06B6D4)" }}
                  >
                    {plan.badge}
                  </span>
                </div>
              )}

              <p className="text-xs font-bold uppercase tracking-[0.18em] text-white/55">
                {plan.name}
              </p>

              <div className="mt-4 flex items-baseline gap-1">
                {plan.price ? (
                  <>
                    <span className="text-5xl font-bold text-white">
                      ${annual ? plan.price.annual : plan.price.monthly}
                    </span>
                    <span className="text-white/55">/mo</span>
                  </>
                ) : (
                  <span className="text-5xl font-bold text-white">Custom</span>
                )}
              </div>
              <p className="mt-2 text-sm text-white/60">{plan.note}</p>

              <ul className="my-8 flex-1 space-y-3 border-t border-white/10 pt-7">
                {plan.features.map(f => (
                  <li key={f} className="flex items-start gap-2.5 text-sm text-white/80">
                    <span
                      className={`mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full ${
                        plan.highlight ? "" : "bg-white/10"
                      }`}
                      style={plan.highlight ? { background: "linear-gradient(135deg,#7C3AED,#06B6D4)" } : undefined}
                    >
                      <Check size={10} className="text-white" />
                    </span>
                    {f}
                  </li>
                ))}
              </ul>

              <Link
                href={(plan as any).href ?? "/signup"}
                className={`group flex items-center justify-center gap-2 rounded-2xl px-5 py-3.5 text-sm font-bold transition-all hover:-translate-y-px ${
                  plan.highlight
                    ? "text-white shadow-[0_12px_40px_rgba(124,58,237,0.50)]"
                    : "border border-white/15 bg-white/[0.04] text-white hover:bg-white/[0.08]"
                }`}
                style={plan.highlight ? { background: "linear-gradient(135deg,#7C3AED,#06B6D4)" } : undefined}
              >
                {plan.cta} <ArrowRight size={14} className="transition-transform group-hover:translate-x-0.5" />
              </Link>
            </motion.article>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   FAQ
   ═══════════════════════════════════════════════════════════════════ */

const faqs = [
  {
    q: "Do I need to know how to code?",
    a: "Not at all. Threadly is a visual drag-and-drop builder. If you can use a tool like Notion or Figma, you can build a chatbot — no programming required.",
  },
  {
    q: "Which AI models do you support?",
    a: "We support OpenAI (GPT-4o, GPT-4o mini), Anthropic Claude, Google Gemini, and open-source models via Ollama. You can use our keys or bring your own.",
  },
  {
    q: "How long does it take to deploy a bot?",
    a: "Most teams launch their first bot in under 10 minutes. Pick a template, customize the copy, paste your knowledge base, and you're live on web, WhatsApp, or any supported channel.",
  },
  {
    q: "Is my data secure?",
    a: "Yes. We're SOC 2 Type II, GDPR-ready, and offer EU data residency. Every workspace is tenant-isolated. Enterprise plans include private VPC deployment.",
  },
  {
    q: "What happens if I exceed my message quota?",
    a: "We never cut you off mid-conversation. We'll notify you ahead of time and you can either upgrade or pay overage at a discounted rate. No surprise bills, ever.",
  },
  {
    q: "Can I migrate from another platform?",
    a: "Absolutely. We have one-click importers for Intercom, Drift, Tidio, and Crisp. Our success team will hand-hold larger migrations free of charge.",
  },
];

function FaqItem({ q, a, open, onToggle }: { q: string; a: string; open: boolean; onToggle: () => void }) {
  return (
    <div className="glass-card overflow-hidden transition-all duration-300 hover:border-white/15">
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center justify-between gap-4 px-6 py-5 text-left"
        aria-expanded={open}
      >
        <span className="text-base font-semibold text-white">{q}</span>
        <span
          className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-white/10 transition-all duration-300 ${
            open ? "rotate-180 border-[#7C3AED]/60 bg-[#7C3AED]/15" : "bg-white/[0.03]"
          }`}
        >
          <ChevronDown size={14} className="text-white/80" />
        </span>
      </button>
      <AnimatePresence initial={false}>
        {open && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
            className="overflow-hidden"
          >
            <p className="px-6 pb-6 text-sm leading-relaxed text-white/70">{a}</p>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

function FaqSection() {
  const [open, setOpen] = useState<number | null>(0);
  return (
    <section className="py-24 sm:py-32">
      <div className="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
        <SectionHeader
          eyebrow="FAQ"
          title={<>Questions, <span className="landing-gradient-text">answered</span></>}
          subtitle="Can't find what you're looking for? Reach out — we reply within an hour."
        />

        <div className="space-y-3">
          {faqs.map((item, i) => (
            <FaqItem
              key={item.q}
              q={item.q}
              a={item.a}
              open={open === i}
              onToggle={() => setOpen(open === i ? null : i)}
            />
          ))}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   FINAL CTA
   ═══════════════════════════════════════════════════════════════════ */

function FinalCtaSection() {
  return (
    <section className="px-4 pb-24 sm:pb-32 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        className="relative mx-auto max-w-5xl overflow-hidden rounded-[32px] p-1"
        style={{ background: "linear-gradient(135deg,#7C3AED 0%,#A78BFA 40%,#06B6D4 100%)" }}
      >
        <div className="relative overflow-hidden rounded-[28px] bg-[#0B1020] px-8 py-16 sm:px-16 sm:py-20">
          {/* Background glow */}
          <div
            aria-hidden
            className="pointer-events-none absolute inset-0"
            style={{
              background:
                "radial-gradient(ellipse 80% 60% at 50% 0%, rgba(124,58,237,0.35), transparent 70%), radial-gradient(ellipse 80% 60% at 50% 100%, rgba(6,182,212,0.30), transparent 70%)",
            }}
          />
          {/* Dot pattern */}
          <div
            aria-hidden
            className="pointer-events-none absolute inset-0 opacity-30"
            style={{
              backgroundImage: "radial-gradient(circle, rgba(255,255,255,0.18) 1px, transparent 1px)",
              backgroundSize: "28px 28px",
              maskImage: "radial-gradient(ellipse 70% 60% at center, black, transparent 80%)",
              WebkitMaskImage: "radial-gradient(ellipse 70% 60% at center, black, transparent 80%)",
            }}
          />

          <div className="relative text-center">
            <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/[0.06] px-3 py-1 text-xs font-semibold uppercase tracking-widest text-white/80 backdrop-blur-md">
              <Sparkles size={11} className="text-[#A78BFA]" /> Get started
            </span>

            <h2 className="mx-auto mt-6 max-w-3xl text-4xl font-bold leading-tight tracking-tight text-white sm:text-6xl">
              Ready to Build Your <span className="landing-gradient-text">AI Assistant?</span>
            </h2>
            <p className="mx-auto mt-5 max-w-xl text-lg leading-relaxed text-white/70">
              Join thousands of teams automating support, sales, and onboarding.
              No credit card. Cancel anytime.
            </p>

            <div className="mt-9 flex flex-wrap items-center justify-center gap-3">
              <Link
                href="/signup"
                className="group relative flex items-center gap-2 overflow-hidden rounded-2xl bg-white px-8 py-4 text-sm font-bold text-[#0B1020] shadow-[0_16px_48px_rgba(255,255,255,0.18)] transition-all hover:-translate-y-0.5"
              >
                <span className="relative z-10">Start Free</span>
                <ArrowRight size={16} className="relative z-10 transition-transform group-hover:translate-x-0.5" />
              </Link>
              <Link
                href="/contact"
                className="flex items-center gap-2.5 rounded-2xl border border-white/15 bg-white/[0.06] px-7 py-4 text-sm font-semibold text-white backdrop-blur-md transition-all hover:-translate-y-0.5 hover:border-white/30 hover:bg-white/[0.10]"
              >
                <Play size={13} className="fill-white text-white" />
                Book a Demo
              </Link>
            </div>

            <p className="mt-7 text-xs text-white/45">
              14-day free trial · No credit card · Cancel anytime
            </p>
          </div>
        </div>
      </motion.div>
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   PAGE
   ═══════════════════════════════════════════════════════════════════ */

export default function LandingPage() {
  return (
    <>
      <HeroSection />
      <TrustedBy />
      <FeaturesSection />
      <BuilderDemoSection />
      <StatsSection />
      <TemplatesSection />
      <IntegrationsSection />
      <TestimonialsSection />
      <PricingSection />
      <FaqSection />
      <FinalCtaSection />
    </>
  );
}
