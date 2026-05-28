'use client';
import Link from 'next/link';
import { ArrowRight, Check, Instagram, Zap, Users, BarChart3, Heart, MessageCircle } from 'lucide-react';

export default function InstagramPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <Instagram size={12} /> Instagram DM Bot
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Turn Instagram DMs into <span className="gradient-text">Sales & Support</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Auto-reply to DMs, story mentions, and comments. Qualify leads, answer product questions, and book sales calls — all without leaving Instagram.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Connect Instagram free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '2B+', label: 'Instagram monthly users' },
            { metric: '70%', label: 'Users discover products on IG' },
            { metric: '3×', label: 'More DMs than posts engagement' },
            { metric: '<5s', label: 'Auto-reply response time' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Instagram Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: MessageCircle, title: 'DM Auto-Reply',        desc: 'Instantly respond to every DM with AI-powered answers trained on your products and FAQs.' },
              { icon: Heart,         title: 'Story Reply Bot',      desc: 'Auto-respond when users reply to your stories. Turn story engagement into conversations.' },
              { icon: Zap,           title: 'Comment Triggers',     desc: 'Set keyword triggers on post comments — reply "PRICE" to get pricing sent to your DMs.' },
              { icon: Users,         title: 'Lead Qualification',   desc: 'Ask qualifying questions in DM, score leads, and route hot prospects to your sales team.' },
              { icon: BarChart3,     title: 'DM Analytics',         desc: 'Track conversation volume, response rates, and revenue attributed to Instagram DMs.' },
              { icon: Instagram,     title: 'Human Handoff',        desc: 'Escalate to a live agent when needed. Full DM history visible in your Threadly inbox.' },
            ].map((item, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white"
                  style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                  <item.icon size={16} />
                </div>
                <h3 className="font-bold text-[var(--text-primary)] mb-2">{item.title}</h3>
                <p className="text-sm text-[var(--text-secondary)]">{item.desc}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-6">Supported Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['DM auto-reply with AI', 'Story mention responses', 'Comment keyword triggers', 'Quick reply buttons in DM', 'Product catalog sharing', 'Ice breaker questions', 'Persistent menu in inbox', 'Lead capture forms via DM', 'Human handoff with context', 'GDPR opt-out handling'].map((f, i) => (
              <div key={i} className="flex items-center gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Turn your IG audience into customers</h2>
          <p className="relative text-white/80 mb-7">Connect your Instagram Professional account in under 8 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
