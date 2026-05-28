'use client';
import Link from 'next/link';
import { ArrowRight, Check } from 'lucide-react';

export default function InternalToolsPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">Internal Tools</span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              AI Agents for Your <span className="gradient-text">Team</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Build internal chatbots that help your team query data, automate repetitive tasks, and get instant answers.
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
            { metric: '8h', label: 'Saved per employee/week' },
            { metric: '90%', label: 'Faster data lookup' },
            { metric: '50%', label: 'Fewer Slack interruptions' },
            { metric: '3×', label: 'Productivity improvement' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Internal Tool Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { title: 'Policy & Handbook Bot', desc: 'Employees get instant answers about HR policies, benefits, and procedures without digging through docs.' },
              { title: 'Data Query Assistant', desc: 'Connect to databases and let team members query data in plain English. No SQL required.' },
              { title: 'IT Helpdesk Bot', desc: 'Handle common IT requests automatically. Provision access, reset passwords, troubleshoot issues.' },
              { title: 'Meeting Prep Assistant', desc: 'Before meetings, automatically pull context: last conversation, open tasks, recent activity.' },
              { title: 'Onboarding Helper', desc: 'Guide new employees through their first weeks. Answer questions about tools, processes, and culture.' },
              { title: 'Slack Integration', desc: 'Deploy internal bots directly in Slack channels. Employees ask questions where they already work.' },
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
          <h2 className="text-3xl font-bold mb-8">Internal Tool Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Slack & Teams integration', 'Database query in natural language', 'Role-based access control', 'Audit logs for compliance', 'SAML/SSO authentication', 'Private deployment option', 'Custom API connectors', 'Workflow automation triggers'].map((f, i) => (
              <div key={i} className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                <Check className="mt-0.5 h-4 w-4 shrink-0 text-[var(--success)]" />
                <span className="text-sm text-[var(--text-secondary)]">{f}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <h2 className="text-3xl font-bold text-white mb-3">Empower your team with AI</h2>
          <p className="text-white/80 mb-7">Build your first internal tool bot in under 30 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
