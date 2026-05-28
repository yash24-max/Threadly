'use client';

import Link from 'next/link';
import { Check, X, ArrowRight, Zap } from 'lucide-react';

const comparisonData = [
  {
    category: 'Core Platform',
    features: [
      { name: 'Visual Flow Builder (drag-and-drop)', threadly: true, intercom: false, tidio: true, chatbot: false },
      { name: 'Conditional Logic & Branching', threadly: true, intercom: true, tidio: true, chatbot: false },
      { name: 'API Call Nodes in Flow', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Variable Management', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Multi-LLM Support (GPT-4o, Claude, Gemini)', threadly: true, intercom: false, tidio: false, chatbot: false },
    ],
  },
  {
    category: 'Knowledge Base & RAG',
    features: [
      { name: 'Hybrid RAG (Dense + Sparse)', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Citation Tracking in Answers', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'PDF & Document Upload', threadly: true, intercom: true, tidio: false, chatbot: true },
      { name: 'Website Crawling & Indexing', threadly: true, intercom: true, tidio: false, chatbot: true },
      { name: 'Notion & Confluence Sync', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Auto Re-Index on Content Change', threadly: true, intercom: false, tidio: false, chatbot: false },
    ],
  },
  {
    category: 'Channels & Deployment',
    features: [
      { name: 'Embeddable Web Widget', threadly: true, intercom: true, tidio: true, chatbot: true },
      { name: 'WhatsApp Business', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'Instagram DMs', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Telegram Bot', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'SMS', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'WordPress Plugin', threadly: true, intercom: false, tidio: true, chatbot: true },
    ],
  },
  {
    category: 'Analytics & Insights',
    features: [
      { name: 'Real-Time Conversation Dashboard', threadly: true, intercom: true, tidio: true, chatbot: false },
      { name: 'Per-Message Cost Tracking', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Drop-off & Funnel Analytics', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'Response Time p50/p95 Metrics', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'CSAT Auto-Scoring', threadly: true, intercom: true, tidio: false, chatbot: false },
    ],
  },
  {
    category: 'Live Agent & Handoff',
    features: [
      { name: 'Human Handoff with Full Context', threadly: true, intercom: true, tidio: true, chatbot: false },
      { name: 'Shared Team Inbox', threadly: true, intercom: true, tidio: true, chatbot: false },
      { name: 'Agent Assignment Rules', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'Typing Indicators & Read Receipts', threadly: true, intercom: true, tidio: true, chatbot: false },
    ],
  },
  {
    category: 'Enterprise & Security',
    features: [
      { name: 'SAML / SSO', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'HIPAA Ready', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'Data Residency Options', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'Audit Logs', threadly: true, intercom: true, tidio: false, chatbot: false },
      { name: 'On-Premise Deployment', threadly: true, intercom: false, tidio: false, chatbot: false },
      { name: 'Free Tier Available', threadly: true, intercom: false, tidio: true, chatbot: true },
    ],
  },
];

const competitors = [
  { key: 'intercom', name: 'Intercom', color: '#1F8EFF' },
  { key: 'tidio', name: 'Tidio', color: '#8B5CF6' },
  { key: 'chatbot', name: 'ChatBot.com', color: '#64748B' },
];

const advantages = [
  {
    title: 'Only Hybrid RAG Platform',
    desc: 'Threadly is the only chatbot builder combining dense vector search (Qdrant) with sparse BM25 retrieval and RRF reranking — achieving 95%+ answer accuracy vs competitors\' basic keyword matching.',
  },
  {
    title: 'True Flow Builder with API Nodes',
    desc: 'Build flows that call external APIs, transform data, and branch on results — all without writing code. Intercom and Tidio only offer simple rule-based triggers.',
  },
  {
    title: 'Real-Time Cost Visibility',
    desc: 'Track AI token costs per conversation, per bot, and per workspace. Know your ROI in real time — no surprise LLM bills at month end.',
  },
  {
    title: 'Multi-Channel from Day One',
    desc: 'One bot deployed to web, WhatsApp, Instagram, Telegram, and SMS simultaneously. Competitors charge extra per channel or don\'t support them at all.',
  },
  {
    title: 'Generous Free Tier',
    desc: 'Start with 3 bots and 5,000 AI messages/month at zero cost. Intercom\'s cheapest plan starts at $74/month with no free tier.',
  },
  {
    title: 'Open Ecosystem',
    desc: 'Full REST API, webhooks, and native integrations with Slack, HubSpot, Salesforce, Zapier and Make. Build any workflow — not just what the vendor decides to support.',
  },
];

const pricingRows = [
  { feature: 'Starting price', threadly: 'Free / $29/mo', intercom: '$74/mo', tidio: 'Free / $29/mo', chatbot: '$52/mo' },
  { feature: 'Knowledge base RAG', threadly: 'Hybrid RAG', intercom: 'Basic search', tidio: 'None', chatbot: 'Basic keyword' },
  { feature: 'Chatbots included', threadly: 'Up to 20', intercom: '1 per seat', tidio: '3', chatbot: '1' },
  { feature: 'AI messages/month', threadly: '50K (Growth)', intercom: '500 (Starter)', tidio: '100 (Free)', chatbot: '1K' },
  { feature: 'WhatsApp channel', threadly: 'Included', intercom: '+$16/seat', tidio: 'Not available', chatbot: 'Not available' },
  { feature: 'Flow builder', threadly: 'Full drag-drop', intercom: 'Limited', tidio: 'Basic', chatbot: 'None' },
];

export default function ComparisonPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              Comparison
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Threadly vs <span className="gradient-text">The Competition</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              See how Threadly stacks up against Intercom, Tidio, and ChatBot.com across features, pricing, and AI capabilities.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup"
                className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Try Threadly free <ArrowRight size={15} />
              </Link>
              <Link href="#comparison-table"
                className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-7 py-3 text-sm font-semibold text-[var(--text-primary)] hover:border-[var(--accent)] transition">
                See full comparison
              </Link>
            </div>
          </div>
        </section>

        {/* Stats bar */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '2×', label: 'More features than Tidio' },
            { metric: '95%', label: 'Answer accuracy with RAG' },
            { metric: '60%', label: 'Lower cost vs Intercom' },
            { metric: 'Free', label: 'Tier with 3 bots included' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Pricing overview table */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-2">Pricing at a Glance</h2>
          <p className="text-[var(--text-secondary)] mb-8">What you actually get for your money.</p>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead>
                <tr className="border-b border-[var(--border)]">
                  <th className="py-3 px-4 text-[var(--text-muted)] font-semibold uppercase tracking-wider text-xs">Feature</th>
                  <th className="py-3 px-4 text-[var(--accent)] font-bold">Threadly</th>
                  {competitors.map(c => (
                    <th key={c.key} className="py-3 px-4 text-[var(--text-muted)] font-semibold">{c.name}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {pricingRows.map((row, i) => (
                  <tr key={i} className="border-b border-[var(--border)] hover:bg-[var(--bg-surface)] transition">
                    <td className="py-3 px-4 font-medium text-[var(--text-primary)]">{row.feature}</td>
                    <td className="py-3 px-4 font-semibold text-[var(--success)]">{row.threadly}</td>
                    <td className="py-3 px-4 text-[var(--text-secondary)]">{row.intercom}</td>
                    <td className="py-3 px-4 text-[var(--text-secondary)]">{row.tidio}</td>
                    <td className="py-3 px-4 text-[var(--text-secondary)]">{row.chatbot}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Full feature comparison */}
        <section id="comparison-table" className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-2">Full Feature Comparison</h2>
          <p className="text-[var(--text-secondary)] mb-8">Every capability, side-by-side.</p>

          {comparisonData.map((section) => (
            <div key={section.category} className="mb-8">
              <h3 className="text-sm font-bold uppercase tracking-widest text-[var(--accent)] mb-3 px-1">{section.category}</h3>
              <div className="overflow-x-auto rounded-xl border border-[var(--border)]">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-[var(--border)] bg-[var(--bg-surface)]">
                      <th className="px-5 py-3 text-left font-semibold text-[var(--text-muted)] w-1/2">Feature</th>
                      <th className="px-5 py-3 text-center font-bold text-[var(--accent)]">Threadly</th>
                      {competitors.map(c => (
                        <th key={c.key} className="px-5 py-3 text-center font-semibold text-[var(--text-muted)]">{c.name}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {section.features.map((feature, fi) => (
                      <tr key={feature.name}
                        className={`border-b border-[var(--border)] ${fi % 2 === 0 ? 'bg-[var(--bg-panel)]' : 'bg-[var(--bg-surface)]'}`}>
                        <td className="px-5 py-3 font-medium text-[var(--text-primary)]">{feature.name}</td>
                        <td className="px-5 py-3 text-center">
                          {feature.threadly
                            ? <Check className="mx-auto h-4 w-4 text-[var(--success)]" />
                            : <X className="mx-auto h-4 w-4 text-[var(--text-muted)]" />}
                        </td>
                        {competitors.map(c => (
                          <td key={c.key} className="px-5 py-3 text-center">
                            {(feature as any)[c.key]
                              ? <Check className="mx-auto h-4 w-4 text-[var(--success)]" />
                              : <X className="mx-auto h-4 w-4 text-[var(--text-muted)]" />}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </section>

        {/* Why Threadly wins */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-2">Why Teams Choose Threadly</h2>
          <p className="text-[var(--text-secondary)] mb-8">Key differentiators that make Threadly the clear choice.</p>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {advantages.map((item, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white"
                  style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                  <Zap size={16} />
                </div>
                <h3 className="font-bold text-[var(--text-primary)] mb-2">{item.title}</h3>
                <p className="text-sm text-[var(--text-secondary)] leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Ready to make the switch?</h2>
          <p className="relative text-white/80 mb-7 max-w-md mx-auto">
            Start for free. No credit card required. Migrate from Intercom or Tidio in under an hour.
          </p>
          <Link href="/signup"
            className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
