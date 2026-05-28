import Link from 'next/link';
import { ArrowRight, Code2, BookOpen, Zap, Bot, Database, Globe } from 'lucide-react';

const docSections = [
  {
    icon: Zap,
    title: 'Getting Started',
    desc: 'Create your first bot in 5 minutes. Connect a channel and go live.',
    links: ['Quick Start Guide', 'Create your first bot', 'Connect WhatsApp', 'Embed web widget'],
  },
  {
    icon: Bot,
    title: 'Flow Builder',
    desc: 'Build conversation flows with drag-and-drop nodes, conditions, and AI responses.',
    links: ['Node types overview', 'Conditional logic', 'AI response nodes', 'API call nodes'],
  },
  {
    icon: Database,
    title: 'Knowledge Base',
    desc: 'Upload documents and train your bot with hybrid RAG retrieval.',
    links: ['Upload documents', 'RAG configuration', 'Citation settings', 'Auto-sync URLs'],
  },
  {
    icon: Globe,
    title: 'Channels',
    desc: 'Deploy your bot to web, WhatsApp, Instagram, Telegram, and SMS.',
    links: ['Web widget setup', 'WhatsApp Business API', 'Instagram DMs', 'Telegram Bot'],
  },
  {
    icon: Code2,
    title: 'REST API',
    desc: 'Full programmatic control over bots, conversations, and analytics.',
    links: ['API authentication', 'Conversations API', 'Bots API', 'Webhooks'],
  },
  {
    icon: BookOpen,
    title: 'Integrations',
    desc: 'Connect Threadly to your CRM, helpdesk, and analytics stack.',
    links: ['HubSpot', 'Salesforce', 'Zapier', 'Slack'],
  },
];

export default function DocsPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl text-center mb-12">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Documentation</span>
          <h1 className="text-5xl font-bold tracking-tight">Threadly Docs</h1>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">Everything you need to build, deploy, and scale AI chatbots.</p>
          <div className="relative mt-6 mx-auto max-w-md">
            <input type="text" placeholder="Search documentation..." className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] py-3 pl-4 pr-4 text-sm outline-none focus:border-[var(--accent)] transition" />
          </div>
        </div>

        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          {docSections.map((section, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 hover:border-[var(--accent)] transition">
              <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                <section.icon size={16} />
              </div>
              <h3 className="font-bold text-[var(--text-primary)] mb-2">{section.title}</h3>
              <p className="text-sm text-[var(--text-secondary)] mb-4">{section.desc}</p>
              <ul className="space-y-1.5">
                {section.links.map((link, j) => (
                  <li key={j}>
                    <a href="#" className="text-sm text-[var(--accent)] hover:underline flex items-center gap-1">
                      <ArrowRight size={11} /> {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8 text-center">
          <h2 className="text-2xl font-bold mb-3">Can't find what you need?</h2>
          <p className="text-[var(--text-secondary)] mb-6">Our team responds to all support inquiries within 24 hours.</p>
          <div className="flex flex-wrap justify-center gap-3">
            <Link href="/contact" className="flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold text-white"
              style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
              Contact support <ArrowRight size={14} />
            </Link>
            <Link href="/help" className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-6 py-3 text-sm font-semibold hover:border-[var(--accent)] transition">
              Help Center
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
