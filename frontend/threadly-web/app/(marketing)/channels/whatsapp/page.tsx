'use client';
import Link from 'next/link';
import { ArrowRight, Check, MessageCircle, Zap, Users, BarChart3, ShieldCheck, Globe } from 'lucide-react';

export default function WhatsAppPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <MessageCircle size={12} /> WhatsApp Business
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              AI-Powered <span className="gradient-text">WhatsApp</span> Bots for Business
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Automate support, sales, and notifications on WhatsApp Business API. Reach 2 billion users where they already are, with AI that understands context.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Connect WhatsApp free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '2B+', label: 'WhatsApp monthly users' },
            { metric: '98%', label: 'Message open rate' },
            { metric: '40%', label: 'Higher conversion vs email' },
            { metric: '<3s', label: 'Avg bot response time' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">WhatsApp Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Zap,          title: 'Automated Conversations', desc: 'Handle FAQs, order status, appointment booking, and support — fully automated, 24/7.' },
              { icon: Globe,        title: 'Broadcast Campaigns',    desc: 'Send targeted messages to opted-in users: product updates, order confirmations, reminders.' },
              { icon: Users,        title: 'Human Handoff',          desc: 'Seamlessly transfer complex queries to your live agents with full conversation context preserved.' },
              { icon: MessageCircle,title: 'Rich Messages',          desc: 'Send images, PDFs, videos, quick-reply buttons, and list pickers within the chat.' },
              { icon: BarChart3,    title: 'Delivery Analytics',     desc: 'Track sent, delivered, read, and replied rates for every message and campaign.' },
              { icon: ShieldCheck,  title: 'Official API',           desc: 'Built on WhatsApp Business API — 100% policy compliant, verified business account.' },
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

        {/* Setup steps */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Connect in 3 Steps</h2>
          <div className="grid gap-5 md:grid-cols-3">
            {[
              { step: '01', title: 'Connect your WABA', desc: 'Link your WhatsApp Business Account via Meta Business Manager. Threadly auto-configures the webhook.' },
              { step: '02', title: 'Build your flow', desc: 'Use the visual builder to design conversation flows with AI nodes, quick replies, and handoff logic.' },
              { step: '03', title: 'Go live', desc: 'Activate your bot. Every incoming WhatsApp message is now handled by your AI — instantly.' },
            ].map((s, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-full text-sm font-bold text-white"
                  style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>{s.step}</div>
                <h3 className="font-bold text-[var(--text-primary)] mb-2">{s.title}</h3>
                <p className="text-sm text-[var(--text-secondary)]">{s.desc}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-6">What's Supported</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Text messages & rich media', 'Quick-reply buttons (up to 3)', 'List picker menus (up to 10 items)', 'Template messages (pre-approved)', 'Session messages (24hr window)', 'Product catalog integration', 'Payment link sharing', 'Location messages', 'Contact card sharing', 'Opt-in & opt-out management'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Start automating WhatsApp today</h2>
          <p className="relative text-white/80 mb-7">Free plan. No credit card. Connect your WABA in under 10 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
