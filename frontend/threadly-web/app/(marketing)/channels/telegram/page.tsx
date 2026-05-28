'use client';
import Link from 'next/link';
import { ArrowRight, Check, Send, Zap, Code2, Users, BarChart3, Bot } from 'lucide-react';

export default function TelegramPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <Send size={12} /> Telegram Bot
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Deploy Powerful <span className="gradient-text">Telegram Bots</span> with AI
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Build feature-rich Telegram bots using the full Bot API. Inline keyboards, deep links, group bots, channel bots — all powered by your AI and knowledge base.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Create Telegram bot free <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '900M+', label: 'Telegram monthly users' },
            { metric: '5 mins', label: 'Setup time with @BotFather' },
            { metric: 'Free', label: 'Telegram Bot API (no fees)' },
            { metric: '∞', label: 'Messages, no limits' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Features */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">Telegram Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: Bot,    title: 'AI-Powered Replies',    desc: 'Every message answered by your AI, trained on your knowledge base with RAG-powered accuracy.' },
              { icon: Zap,    title: 'Inline Keyboards',      desc: 'Interactive button menus, callback queries, and inline mode for rich interactive experiences.' },
              { icon: Code2,  title: 'Command Handlers',      desc: 'Register /commands for quick actions: /help, /status, /book, /support — all handled by flows.' },
              { icon: Users,  title: 'Group & Channel Bots',  desc: 'Deploy in Telegram groups for community management, or channels for broadcast automation.' },
              { icon: Send,   title: 'Deep Links',            desc: 'Create start links with parameters to launch specific flows for campaigns and onboarding.' },
              { icon: BarChart3, title: 'Analytics',          desc: 'Track user growth, conversation volume, command usage, and drop-off by flow step.' },
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
          <h2 className="text-3xl font-bold mb-6">Full API Support</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Text, photo, video, document messages', 'Inline keyboard buttons', 'Reply keyboard menus', 'Deep link parameters', '/command handling', 'Group & supergroup support', 'Channel post automation', 'Webhook & long polling', 'User authentication via Telegram Login', 'Payments via Telegram Pay'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Build your Telegram bot today</h2>
          <p className="relative text-white/80 mb-7">Create a @BotFather token, paste it in Threadly, and go live in 5 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
