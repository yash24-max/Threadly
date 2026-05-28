import Link from 'next/link';
import { ArrowRight, Users, Zap, Globe, ShieldCheck } from 'lucide-react';

export default function AboutPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">About Threadly</span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Built for teams who need <span className="gradient-text">AI that works</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Threadly is the AI chatbot platform that combines visual flow building, hybrid RAG knowledge bases, and real-time conversations — so any team can deploy intelligent bots in minutes, not months.
            </p>
          </div>
        </section>

        <section className="mt-10 grid gap-5 md:grid-cols-2">
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
            <h2 className="text-2xl font-bold mb-4">Our Mission</h2>
            <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
              We believe every business deserves AI that actually understands their customers. Not hallucinating chatbots — real, grounded, accurate answers from your own content.
            </p>
            <p className="text-[var(--text-secondary)] leading-relaxed">
              Threadly was founded by engineers who built enterprise AI systems and saw firsthand how hard it was to deploy production-quality chatbots. We built the platform we wished existed.
            </p>
          </div>
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
            <h2 className="text-2xl font-bold mb-4">What makes us different</h2>
            <ul className="space-y-3">
              {[
                { icon: Zap, text: 'Hybrid RAG (dense + sparse) for 95%+ answer accuracy — not keyword matching' },
                { icon: Globe, text: 'One bot, deployed to web, WhatsApp, Instagram, Telegram, and SMS simultaneously' },
                { icon: Users, text: 'Visual flow builder with API call nodes — no code, full flexibility' },
                { icon: ShieldCheck, text: 'Enterprise security from day one: SSO, RBAC, audit logs, tenant isolation' },
              ].map((item, i) => (
                <li key={i} className="flex items-start gap-3 text-sm text-[var(--text-secondary)]">
                  <item.icon size={16} className="mt-0.5 shrink-0 text-[var(--accent)]" />
                  {item.text}
                </li>
              ))}
            </ul>
          </div>
        </section>

        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '2,400+', label: 'Teams using Threadly' },
            { metric: '12M+', label: 'Conversations handled' },
            { metric: '78%', label: 'Avg ticket deflection' },
            { metric: '99.9%', label: 'Platform uptime' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Join us</h2>
          <p className="relative text-white/80 mb-7">Start for free. No credit card required.</p>
          <div className="flex flex-wrap justify-center gap-3">
            <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
              Get started free <ArrowRight size={15} />
            </Link>
            <Link href="/contact" className="inline-flex items-center gap-2 rounded-xl border-2 border-white/30 bg-white/10 px-8 py-3.5 text-sm font-semibold text-white hover:bg-white/20 transition">
              Contact us
            </Link>
          </div>
        </section>
      </div>
    </div>
  );
}
