'use client';
import Link from 'next/link';
import { ArrowRight, Check, Code2, Palette, Zap, Globe, MessageSquare, Bot } from 'lucide-react';

export default function WebWidgetPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <Bot size={12} /> Web Chat Widget
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Add AI Chat to Any Website in <span className="gradient-text">2 Minutes</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              One line of code. Streaming AI responses. Fully brandable. Works on any website, app, or landing page — no framework required.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Get embed code free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '<35KB', label: 'Widget bundle size (gzipped)' },
            { metric: '<100ms', label: 'Initial load time' },
            { metric: '1 line', label: 'Code to embed' },
            { metric: '99.9%', label: 'Uptime SLA' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Embed code preview */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-2">One Line to Deploy</h2>
          <p className="text-[var(--text-secondary)] mb-6">Copy the snippet, paste before <code className="rounded bg-[var(--bg-surface)] px-1.5 py-0.5 text-sm font-mono text-[var(--accent)]">&lt;/body&gt;</code>, done.</p>
          <div className="rounded-xl bg-[#0D0E1A] p-5 font-mono text-sm leading-relaxed overflow-x-auto">
            <p className="text-[#9496B0]">{'<!-- Threadly Chat Widget -->'}</p>
            <p className="text-[#F1F2F8]">{'<script'}</p>
            <p className="ml-4 text-[#8B5CF6]">{'  src="https://cdn.threadly.ai/widget.js"'}</p>
            <p className="ml-4 text-[#06B6D4]">{'  data-bot-id="YOUR_BOT_ID"'}</p>
            <p className="ml-4 text-[#059669]">{'  data-primary-color="#6366F1"'}</p>
            <p className="text-[#F1F2F8]">{'  async>'}</p>
            <p className="text-[#F1F2F8]">{'</script>'}</p>
          </div>
        </section>

        {/* Features grid */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Widget Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Zap,          title: 'Streaming Responses',   desc: 'Real-time token streaming so users see answers appear instantly — no waiting for full generation.' },
              { icon: Palette,      title: 'Full Brand Control',    desc: 'Match your brand: custom colors, logo, fonts, welcome message, and position (bottom-right or bottom-left).' },
              { icon: Globe,        title: 'Works Everywhere',      desc: 'React, Vue, plain HTML, Webflow, WordPress, Squarespace, Shopify — any site with a <body> tag.' },
              { icon: MessageSquare,title: 'Conversation History',  desc: 'Persists chat history across page loads. Users can continue conversations even after closing the widget.' },
              { icon: Code2,        title: 'JavaScript SDK',        desc: 'Programmatic control: open/close widget, pre-fill messages, identify users, and listen to events.' },
              { icon: Bot,          title: 'AI + Flow Hybrid',      desc: 'Combine structured conversation flows with open-ended AI responses — the best of both worlds.' },
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

        {/* Checklist */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Everything Included</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Mobile-responsive design', 'Dark & light mode support', 'Typing indicators', 'Read receipts', 'File & image upload', 'CSAT rating prompt', 'Human handoff button', 'Custom launcher icon', 'Pre-chat form (name/email)', 'Webhook on conversation end'].map((f, i) => (
              <div key={i} className="flex items-center gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Add chat to your site today</h2>
          <p className="relative text-white/80 mb-7">Free plan includes 5,000 messages/month. No credit card required.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get your embed code <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
