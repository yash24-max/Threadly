'use client';
import Link from 'next/link';
import { ArrowRight, Check, FileText, Zap, Globe, ShieldCheck, Code2, Bot } from 'lucide-react';

export default function WordPressPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <FileText size={12} /> WordPress Plugin
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Add AI Chat to WordPress in <span className="gradient-text">One Click</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Install the Threadly WordPress plugin, connect your bot, and go live. No code, no developer, no configuration headaches. Works with every WordPress theme.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Install plugin free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '43%', label: 'Of all websites run WordPress' },
            { metric: '1-click', label: 'Plugin installation' },
            { metric: '0', label: 'Lines of code required' },
            { metric: '<2min', label: 'From install to live chat' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Install steps */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Install in 3 Steps</h2>
          <div className="grid gap-5 md:grid-cols-3">
            {[
              { step: '01', title: 'Install the plugin', desc: 'Search "Threadly" in WordPress Plugin Directory, click Install, then Activate. Done in 30 seconds.' },
              { step: '02', title: 'Paste your API key', desc: 'Copy your Threadly API key from your dashboard, paste it in the plugin settings. One field.' },
              { step: '03', title: 'Select your bot', desc: 'Choose which bot to display, pick position and colors, click Save. Your AI is now live on every page.' },
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

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Plugin Features</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Bot,      title: 'Any Threadly Bot',     desc: 'Use any bot from your dashboard — support, sales, onboarding — and switch bots anytime.' },
              { icon: Globe,    title: 'Theme Compatible',      desc: 'Works with Elementor, Divi, Avada, Astra, GeneratePress, and every other major theme.' },
              { icon: Zap,      title: 'WooCommerce Ready',     desc: 'Integrate with WooCommerce for order status lookups, product recommendations, and cart recovery.' },
              { icon: Code2,    title: 'Shortcode Support',     desc: 'Embed the chat on specific pages using the [threadly-chat] shortcode for targeted deployment.' },
              { icon: ShieldCheck, title: 'GDPR Compliant',   desc: 'Built-in cookie consent integration, data processing agreements, and EU data hosting option.' },
              { icon: FileText, title: 'Page-Level Control',   desc: 'Show or hide the widget on specific pages, post types, or user roles via plugin settings.' },
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
          <h2 className="text-3xl font-bold mb-6">Everything Included</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['1-click WordPress install', 'WooCommerce integration', 'Elementor widget block', 'Shortcode [threadly-chat]', 'Page/post visibility rules', 'User role targeting', 'Custom CSS support', 'Cookie consent integration', 'Multisite network support', 'Auto-updates via WP dashboard'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Add AI chat to WordPress today</h2>
          <p className="relative text-white/80 mb-7">Free plugin. Free plan. Live in under 2 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
