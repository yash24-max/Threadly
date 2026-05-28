'use client';
import Link from 'next/link';
import { ArrowRight, Check } from 'lucide-react';

export default function MarketingUseCasePage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">Marketing</span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Convert Visitors into <span className="gradient-text">Customers</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Engage website visitors in real-time, capture leads, and nurture them through your funnel automatically.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                Start free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '5×', label: 'More leads captured' },
            { metric: '70%', label: 'Visitor engagement rate' },
            { metric: '2×', label: 'Campaign conversion rate' },
            { metric: '24/7', label: 'Always-on engagement' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Marketing Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { title: 'Lead Capture Bots', desc: 'Engage visitors at the right moment with personalized messages based on behavior.' },
              { title: 'Campaign Automation', desc: 'Run drip campaigns via WhatsApp, SMS, or web chat. Track opens and conversions.' },
              { title: 'A/B Testing Flows', desc: 'Test different conversation flows. Optimize for conversion automatically.' },
              { title: 'Segmentation & Targeting', desc: 'Tag visitors by intent, industry, or behavior. Send hyper-relevant messages.' },
              { title: 'Content Recommendations', desc: 'Suggest relevant content, case studies, or demos based on visitor interest.' },
              { title: 'Marketing Analytics', desc: 'Attribution, funnel analytics, and ROI tracking per campaign.' },
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

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Marketing Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Exit-intent popups & engagement triggers', 'WhatsApp broadcast campaigns', 'Behavioral segmentation', 'Email & SMS nurture sequences', 'Social media DM automation', 'Conversion funnel analytics', 'CRM & MAP integrations', 'Personalization engine'].map((f, i) => (
              <div key={i} className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="mt-0.5 h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <h2 className="text-3xl font-bold text-white mb-3">Start converting more visitors</h2>
          <p className="text-white/80 mb-7">Launch your marketing bot in minutes. No credit card required.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
