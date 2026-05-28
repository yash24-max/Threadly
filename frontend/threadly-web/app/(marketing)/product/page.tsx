'use client';

import Link from 'next/link';
import { ArrowRight, Zap, Brain, MessageSquare, Users, BarChart3, Code2, Lock, TrendingUp, Settings, Check } from 'lucide-react';

export default function ProductPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: "radial-gradient(circle, #6366F1 1px, transparent 1px)", backgroundSize: "32px 32px" }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              Product
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Enterprise-Grade <span className="gradient-text">AI Chatbots</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              The only platform combining visual flow building, knowledge base RAG, real-time conversations, and seamless integrations.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                Start building free <ArrowRight size={15} />
              </Link>
              <Link href="/#pricing" className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-7 py-3 text-sm font-semibold text-[var(--text-primary)] hover:border-[var(--accent)] transition">
                View pricing
              </Link>
            </div>
          </div>
        </section>

        {/* Core features grid */}
        <section className="mt-16">
          <div style={{ maxWidth: 640, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 48 }}>
            <h2 className="text-3xl font-bold">Core Features</h2>
            <p className="mt-3 text-[var(--text-secondary)]">Everything you need to build, deploy, and scale AI chatbots.</p>
          </div>

          <div className="space-y-8">
            {/* Flow Builder */}
            <div className="grid gap-8 md:grid-cols-2 items-center rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
              <div>
                <div className="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl text-white"
                  style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                  <Zap size={20} />
                </div>
                <h3 className="text-2xl font-bold mb-3">Visual Flow Builder</h3>
                <p className="text-[var(--text-secondary)] mb-5">
                  Drag-and-drop interface with pre-built nodes for every scenario. Build complex conversation flows without writing code.
                </p>
                <ul className="space-y-2">
                  {['Message and Question nodes', 'Conditional logic & branching', 'AI-powered replies with streaming', 'API calls & webhooks', 'Variable management', 'Auto-saving as you build'].map(f => (
                    <li key={f} className="flex items-center gap-2.5 text-sm text-[var(--text-secondary)]">
                      <Check size={14} className="shrink-0 text-[var(--success)]" /> {f}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="flex h-56 items-center justify-center rounded-2xl border border-[var(--border)] bg-[var(--bg-surface)]">
                <div className="text-center text-[var(--text-muted)]">
                  <Zap size={48} className="mx-auto mb-2 opacity-30" />
                  <p className="text-sm">Flow Builder</p>
                </div>
              </div>
            </div>

            {/* Knowledge Base */}
            <div className="grid gap-8 md:grid-cols-2 items-center rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
              <div className="order-2 md:order-1 flex h-56 items-center justify-center rounded-2xl border border-[var(--border)] bg-[var(--bg-surface)]">
                <div className="text-center text-[var(--text-muted)]">
                  <Brain size={48} className="mx-auto mb-2 opacity-30" />
                  <p className="text-sm">Knowledge Base RAG</p>
                </div>
              </div>
              <div className="order-1 md:order-2">
                <div className="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl text-white"
                  style={{ background: "linear-gradient(135deg,#8B5CF6,#06B6D4)" }}>
                  <Brain size={20} />
                </div>
                <h3 className="text-2xl font-bold mb-3">Knowledge Base RAG</h3>
                <p className="text-[var(--text-secondary)] mb-5">
                  Upload documents, PDFs, websites, or text. Your bot learns and cites from your content with hybrid vector search.
                </p>
                <ul className="space-y-2">
                  {['PDF & document upload', 'Website crawling & indexing', 'Dense + sparse retrieval (RRF)', 'Citation accuracy tracking', 'Automatic content updates', 'Multi-language support'].map(f => (
                    <li key={f} className="flex items-center gap-2.5 text-sm text-[var(--text-secondary)]">
                      <Check size={14} className="shrink-0 text-[var(--success)]" /> {f}
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {/* Widget */}
            <div className="grid gap-8 md:grid-cols-2 items-center rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
              <div>
                <div className="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl text-white"
                  style={{ background: "linear-gradient(135deg,#06B6D4,#6366F1)" }}>
                  <MessageSquare size={20} />
                </div>
                <h3 className="text-2xl font-bold mb-3">Embeddable Widget</h3>
                <p className="text-[var(--text-secondary)] mb-5">
                  One-line embed code. Streaming AI replies. Mobile-responsive. Fully brandable to match your site.
                </p>
                <ul className="space-y-2">
                  {['Single script tag embed', 'Streaming AI responses', 'Mobile-optimized UI', 'Custom colors & branding', 'Conversation history', 'Real-time notifications'].map(f => (
                    <li key={f} className="flex items-center gap-2.5 text-sm text-[var(--text-secondary)]">
                      <Check size={14} className="shrink-0 text-[var(--success)]" /> {f}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="flex h-56 items-center justify-center rounded-2xl border border-[var(--border)] bg-[var(--bg-surface)]">
                <div className="text-center text-[var(--text-muted)]">
                  <MessageSquare size={48} className="mx-auto mb-2 opacity-30" />
                  <p className="text-sm">Chat Widget</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Advanced features */}
        <section className="mt-16 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <div style={{ maxWidth: 560, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 40 }}>
            <h2 className="text-3xl font-bold">Advanced Capabilities</h2>
          </div>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-4">
            {[
              { icon: Users,      title: 'Human Handoff',  desc: 'Seamless agent takeover with full context. Hand back to AI anytime.' },
              { icon: BarChart3,  title: 'Analytics',       desc: 'Real-time metrics: conversations, response times, costs, satisfaction.' },
              { icon: Code2,      title: 'REST API',        desc: 'Type-safe SDKs for Node, Python, Go. Full programmatic control.' },
              { icon: Zap,        title: 'Integrations',    desc: 'Connect to Slack, Zapier, Make, HubSpot, Salesforce, and more.' },
              { icon: Lock,       title: 'Security',        desc: 'Enterprise SSO, HIPAA compliance, data residency, audit logs.' },
              { icon: Settings,   title: 'Customization',   desc: 'Full control over prompts, model selection, and behavior.' },
              { icon: TrendingUp, title: 'Scaling',         desc: 'Auto-scales to millions of conversations. 99.9% uptime SLA.' },
              { icon: Zap,        title: 'Performance',     desc: 'Sub-100ms latency. Real-time streaming. Global CDN.' },
            ].map((f, i) => {
              const Icon = f.icon;
              return (
                <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-5 hover:border-[var(--accent)] transition">
                  <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white"
                    style={{ background: "linear-gradient(135deg,#6366F1,#8B5CF6)" }}>
                    <Icon size={16} />
                  </div>
                  <h3 className="font-bold text-[var(--text-primary)] mb-1">{f.title}</h3>
                  <p className="text-xs text-[var(--text-secondary)] leading-relaxed">{f.desc}</p>
                </div>
              );
            })}
          </div>
        </section>

        {/* Comparison table */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Why Threadly?</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[var(--border)]">
                  <th className="py-3 px-4 text-sm font-semibold text-[var(--text-muted)] uppercase tracking-wider">Feature</th>
                  <th className="py-3 px-4 text-sm font-semibold text-[var(--accent)]">Threadly</th>
                  <th className="py-3 px-4 text-sm font-semibold text-[var(--text-muted)]">Others</th>
                </tr>
              </thead>
              <tbody>
                {[
                  ['Visual Flow Builder', true, true],
                  ['Knowledge Base RAG', true, false],
                  ['Free Plan', true, false],
                  ['Hybrid RAG (Dense + Sparse)', true, false],
                  ['Real-time Analytics', true, false],
                  ['Human Handoff', true, true],
                  ['REST API', true, true],
                  ['Multi-LLM Support', true, false],
                ].map(([feature, threadly, other], i) => (
                  <tr key={i} className="border-b border-[var(--border)]">
                    <td className="py-3 px-4 text-sm text-[var(--text-primary)]">{feature as string}</td>
                    <td className="py-3 px-4 text-sm font-semibold text-[var(--success)]">{threadly ? '✓ Yes' : '–'}</td>
                    <td className="py-3 px-4 text-sm text-[var(--text-muted)]">{other ? '✓' : '–'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Security */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <div style={{ maxWidth: 560, marginLeft: "auto", marginRight: "auto", textAlign: "center", marginBottom: 40 }}>
            <h2 className="text-3xl font-bold">Enterprise Security</h2>
            <p className="mt-3 text-[var(--text-secondary)]">Your data is protected with enterprise-grade security standards.</p>
          </div>
          <div className="grid gap-5 md:grid-cols-3">
            {[
              { title: 'Data Privacy',    items: ['End-to-end encryption', 'GDPR compliant', 'Data residency options', 'Regular audits'] },
              { title: 'Access Control',  items: ['Enterprise SSO', 'Role-based access', 'API key rotation', '2FA enforcement'] },
              { title: 'Compliance',      items: ['SOC 2 Type II', 'HIPAA ready', 'PCI DSS compliant', 'Audit logs'] },
            ].map(sec => (
              <div key={sec.title} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <h3 className="font-bold text-[var(--text-primary)] mb-4">{sec.title}</h3>
                <ul className="space-y-2">
                  {sec.items.map(item => (
                    <li key={item} className="flex items-center gap-2 text-sm text-[var(--text-secondary)]">
                      <Lock size={13} className="text-[var(--success)]" /> {item}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: "linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)" }}>
          <h2 className="text-3xl font-bold text-white mb-3">Ready to build amazing bots?</h2>
          <p className="text-white/80 mb-7">Start free today. No credit card required.</p>
          <div className="flex flex-wrap justify-center gap-3">
            <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
              Start building free <ArrowRight size={15} />
            </Link>
            <a href="mailto:sales@threadly.ai" className="inline-flex items-center gap-2 rounded-xl border-2 border-white/30 bg-white/10 px-8 py-3.5 text-sm font-semibold text-white backdrop-blur hover:bg-white/20 transition">
              Schedule demo
            </a>
          </div>
        </section>

      </div>
    </div>
  );
}
