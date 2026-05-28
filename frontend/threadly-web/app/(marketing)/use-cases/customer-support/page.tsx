'use client';

import Link from 'next/link';
import { ArrowRight, Check } from 'lucide-react';

const accent = "#6366F1";

export default function CustomerSupportPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              Use Case
            </span>
            <h1 className="text-5xl font-bold leading-tight tracking-tight text-[var(--text-primary)] sm:text-6xl">
              Customer Support at{" "}
              <span className="gradient-text">Scale</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Resolve 80% of support tickets automatically with AI, freeing your team to focus on complex issues.
            </p>
            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                Try for free <ArrowRight size={15} />
              </Link>
              <Link href="/#pricing" className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-7 py-3 text-sm font-semibold text-[var(--text-primary)] hover:border-[var(--accent)] transition">
                See pricing
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '80%', label: 'Tickets resolved by AI' },
            { metric: '4h', label: 'Avg response time cut' },
            { metric: '30%', label: 'Cost per ticket reduction' },
            { metric: '92%', label: 'Customer satisfaction' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Challenge */}
        <section className="mt-16 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold tracking-tight mb-8">The Challenge</h2>
          <div className="grid gap-5 md:grid-cols-3">
            {[
              { title: 'High Ticket Volume', desc: 'Drowning in repetitive support requests that eat team bandwidth.' },
              { title: 'Long Response Times', desc: 'Customers waiting hours for simple answers hurts satisfaction.' },
              { title: 'Team Burnout', desc: 'Support staff overwhelmed with basic questions instead of complex issues.' },
            ].map((item, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <h3 className="font-bold text-[var(--accent)] mb-2">{item.title}</h3>
                <p className="text-sm text-[var(--text-secondary)]">{item.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Solution */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold tracking-tight mb-8">How Threadly Solves It</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { title: 'Instant AI Responses', desc: 'RAG-powered knowledge base resolves common issues in seconds, not hours.' },
              { title: 'Smart Ticket Routing', desc: 'Complex issues auto-escalate to humans. Your team handles only real expertise.' },
              { title: 'Multi-Channel Support', desc: 'Web chat, WhatsApp, Facebook, Telegram — one bot handles all channels.' },
              { title: 'Real-Time Handoff', desc: 'Hand off to live agents without losing context. Full conversation history preserved.' },
              { title: 'Conversation Analytics', desc: 'Track satisfaction, resolution rates, and avg response time with live dashboards.' },
              { title: 'Continuous Learning', desc: 'Bot improves over time as you train it on your FAQ, docs, and past tickets.' },
            ].map((item, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white text-sm font-bold"
                  style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                  {i + 1}
                </div>
                <h3 className="font-bold text-[var(--text-primary)] mb-2">{item.title}</h3>
                <p className="text-sm text-[var(--text-secondary)]">{item.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold tracking-tight mb-8">Built for Support Teams</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {[
              'Knowledge base RAG with automatic source citations',
              'Live agent dashboard with conversation context',
              'Sentiment analysis and escalation triggers',
              'Conversation quality scoring',
              'Cost tracking per conversation',
              'Multi-language support',
              'Integration with Intercom, Zendesk, Slack',
              'Custom workflows for complex issues',
            ].map((feature, i) => (
              <div key={i} className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="mt-0.5 h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{feature}</span>
              </div>
            ))}
          </div>
        </section>

        {/* Steps */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold tracking-tight mb-8">Get Started in 3 Steps</h2>
          <div className="space-y-4">
            {[
              { step: 1, title: 'Upload Your Docs', desc: 'Import FAQ, help articles, product docs, and past tickets.' },
              { step: 2, title: 'Train Your Bot', desc: 'Threadly builds a RAG index. Configure handoff rules to your agents.' },
              { step: 3, title: 'Deploy', desc: 'Launch on web, WhatsApp, or SMS. Monitor conversations in real-time.' },
            ].map((item) => (
              <div key={item.step} className="flex gap-4 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-white font-bold"
                  style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                  {item.step}
                </div>
                <div>
                  <h3 className="font-bold text-[var(--text-primary)]">{item.title}</h3>
                  <p className="mt-1 text-sm text-[var(--text-secondary)]">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: "radial-gradient(circle, white 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <h2 className="text-3xl font-bold text-white mb-3">Ready to transform your support?</h2>
          <p className="text-white/80 mb-7">Deploy your first support bot today. No credit card required.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Start free trial <ArrowRight size={15} />
          </Link>
        </section>

      </div>
    </div>
  );
}
