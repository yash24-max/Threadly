'use client';
import Link from 'next/link';
import { ArrowRight, Check, MessageSquare, Zap, Users, BarChart3, ShieldCheck, Globe } from 'lucide-react';

export default function SmsPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <MessageSquare size={12} /> SMS Bot
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Two-Way <span className="gradient-text">SMS Automation</span> with AI
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Reach every customer, even without a smartphone app. AI-powered SMS conversations via Twilio and Vonage — with 99% delivery rates and zero app installs.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Set up SMS bot free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '98%', label: 'SMS open rate (vs 20% email)' },
            { metric: '90s', label: 'Avg time to read an SMS' },
            { metric: '45%', label: 'SMS response rate' },
            { metric: '99%', label: 'Delivery rate via Twilio' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">SMS Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Zap,         title: 'Two-Way Conversations', desc: 'Full back-and-forth SMS conversations driven by your AI — not just one-way blasts.' },
              { icon: Globe,       title: 'Twilio & Vonage',       desc: 'Plug in your existing Twilio or Vonage account. Bring your own number, keep your rates.' },
              { icon: MessageSquare, title: 'Keyword Triggers',    desc: 'Respond to keywords like STOP, HELP, INFO, or custom triggers to launch specific flows.' },
              { icon: Users,       title: 'Bulk Campaigns',        desc: 'Send personalized SMS to thousands of opted-in contacts with merge fields and scheduling.' },
              { icon: BarChart3,   title: 'Delivery Tracking',     desc: 'Real-time delivery receipts, open rates (via shortlinks), and reply rate analytics.' },
              { icon: ShieldCheck, title: 'Compliance Built-in',   desc: 'Auto-handle STOP/HELP, maintain opt-in records, and respect quiet hours by timezone.' },
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
          <h2 className="text-3xl font-bold mb-6">Everything You Need</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Twilio & Vonage integration', 'Long code & short code support', 'Toll-free number support', 'MMS (images & media)', 'STOP / HELP auto-handling', 'Opt-in list management', 'Scheduled message queues', 'Drip campaign sequences', 'Timezone-aware sending', 'Webhook on reply events'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Reach every customer via SMS</h2>
          <p className="relative text-white/80 mb-7">Connect your Twilio account and launch your first SMS bot in minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
