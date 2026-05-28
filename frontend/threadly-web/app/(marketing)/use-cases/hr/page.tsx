'use client';
import Link from 'next/link';
import { ArrowRight, Check, Briefcase, Users, FileText, Zap, BarChart3, ShieldCheck } from 'lucide-react';

export default function HRPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <Briefcase size={12} /> HR & Recruiting
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Hire Faster, <span className="gradient-text">Retain Longer</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Screen candidates at scale, answer employee HR questions instantly, and automate repetitive recruiting tasks — all with AI bots that integrate with your existing HR stack.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Start free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '60%', label: 'Faster time-to-hire' },
            { metric: '80%', label: 'HR FAQ tickets deflected' },
            { metric: '3×', label: 'More candidates screened' },
            { metric: '40%', label: 'Recruiter time saved' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">HR & Recruiting Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Briefcase,  title: 'Candidate Screening', desc: 'Ask role-specific questions, score candidates on your criteria, and route qualified applicants to recruiters automatically.' },
              { icon: FileText,   title: 'HR Policy Bot',      desc: 'Train on your employee handbook, benefits docs, and PTO policies. Employees get instant answers without emailing HR.' },
              { icon: Users,      title: 'Interview Scheduler',desc: 'Qualified candidates book interview slots directly in the chat with Calendly or Google Calendar integration.' },
              { icon: Zap,        title: 'Onboarding Flows',   desc: 'Guide new hires through their first weeks: IT setup, benefits enrollment, culture intro, and role-specific training.' },
              { icon: BarChart3,  title: 'Recruiting Analytics',desc: 'Track candidate pipeline, drop-off rates by stage, time-to-hire, and cost-per-hire across all channels.' },
              { icon: ShieldCheck,title: 'Compliance Ready',   desc: 'GDPR-compliant candidate data handling, opt-in consent, and automatic data retention policies for recruiting data.' },
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
          <h2 className="text-3xl font-bold mb-6">HR Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Multi-stage candidate screening', 'Custom scoring rubrics', 'ATS integration (Greenhouse, Lever)', 'Calendar booking for interviews', 'Employee handbook Q&A bot', 'Benefits & PTO policy answers', 'New hire onboarding flows', 'Internal job posting distribution', 'Exit interview automation', 'GDPR-compliant data handling'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Build your recruiting bot today</h2>
          <p className="relative text-white/80 mb-7">Free plan. No credit card. Start screening candidates in under an hour.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
