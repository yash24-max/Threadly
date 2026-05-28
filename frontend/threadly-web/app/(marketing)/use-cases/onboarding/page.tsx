'use client';
import Link from 'next/link';
import { ArrowRight, Check } from 'lucide-react';

export default function OnboardingPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">Onboarding</span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Guide Users to Their <span className="gradient-text">Aha-Moment</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Personalized onboarding flows that reduce time-to-value and dramatically cut churn in the first 30 days.
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
            { metric: '45%', label: 'Reduction in time-to-value' },
            { metric: '60%', label: 'Lower 30-day churn' },
            { metric: '3×', label: 'Feature adoption rate' },
            { metric: '90%', label: 'Onboarding completion' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Onboarding Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { title: 'Role-Based Flows', desc: 'Different onboarding paths for different user types. Developers, managers, and end-users each get tailored guidance.' },
              { title: 'Progress Tracking', desc: 'Track completion at each step. Automatically nudge users who get stuck with helpful tips.' },
              { title: 'Feature Discovery', desc: 'Surface the right features at the right time based on user behavior and goals.' },
              { title: 'In-App Tooltips', desc: 'Contextual guidance that appears exactly when and where users need it most.' },
              { title: 'Success Milestones', desc: 'Celebrate progress and guide users to the next value-generating action.' },
              { title: 'Drop-off Recovery', desc: 'Detect when users stall and send proactive messages via email or chat to re-engage.' },
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
          <h2 className="text-3xl font-bold mb-8">Onboarding Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Personalized onboarding by role & goal', 'Interactive product walkthroughs', 'Email + in-app + chat nudges', 'Completion rate dashboards', 'NPS collection during onboarding', 'Churn prediction alerts', 'Segment & Mixpanel integration', 'Custom checklist builder'].map((f, i) => (
              <div key={i} className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="mt-0.5 h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <h2 className="text-3xl font-bold text-white mb-3">Reduce churn from day one</h2>
          <p className="text-white/80 mb-7">Build onboarding flows that actually work. No code required.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
