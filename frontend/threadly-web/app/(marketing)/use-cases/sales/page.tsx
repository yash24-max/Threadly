'use client';
import Link from 'next/link';
import { ArrowRight, Check } from 'lucide-react';

export default function SalesPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">Sales</span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Close More Deals with <span className="gradient-text">AI Sales Bots</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Qualify leads 24/7, book demos automatically, and nurture prospects until they're ready to buy.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                Start free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '3×', label: 'Faster lead qualification' },
            { metric: '40%', label: 'More demos booked' },
            { metric: '60%', label: 'Lead response improvement' },
            { metric: '24/7', label: 'Always-on sales coverage' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">How Threadly Powers Your Sales</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { title: 'Instant Lead Qualification', desc: 'Ask BANT questions automatically. Route hot leads to your sales team immediately.' },
              { title: 'Demo Booking Automation', desc: 'Integrate with Calendly or Cal.com. Book meetings without human intervention.' },
              { title: 'Product Demos via Chat', desc: 'Walk prospects through features interactively. Answer objections with trained AI.' },
              { title: 'CRM Auto-Sync', desc: 'Push qualified leads to Salesforce, HubSpot, or Pipedrive automatically.' },
              { title: 'Follow-Up Sequences', desc: 'Re-engage cold leads with automated WhatsApp or SMS follow-up flows.' },
              { title: 'Sales Analytics', desc: 'Track conversion rates, drop-off points, and revenue attribution per bot.' },
            ].map((item, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white text-sm font-bold"
                  style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>{i + 1}</div>
                <h3 className="font-bold text-[var(--text-primary)] mb-2">{item.title}</h3>
                <p className="text-sm text-[var(--text-secondary)]">{item.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Features list */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Sales Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['BANT qualification flows', 'Calendar integration for demo booking', 'Lead scoring & routing', 'CRM sync (Salesforce, HubSpot)', 'Multi-channel outreach (WhatsApp, SMS)', 'Objection handling with AI', 'Product recommendation engine', 'Revenue attribution tracking'].map((f, i) => (
              <div key={i} className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="mt-0.5 h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <h2 className="text-3xl font-bold text-white mb-3">Start closing more deals today</h2>
          <p className="text-white/80 mb-7">Set up your sales bot in minutes. No credit card required.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
