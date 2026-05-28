import Link from 'next/link';
import { ArrowRight, Search } from 'lucide-react';

const faqs = [
  { q: 'How do I create my first bot?', a: 'Go to Dashboard → New Bot, choose a template or start from scratch, then use the visual flow builder to design your conversation. Takes under 5 minutes.' },
  { q: 'Which file formats can I upload to the knowledge base?', a: 'PDF, Word (.docx), PowerPoint, Excel, plain text, Markdown, CSV, and web URLs. We support 50+ formats total.' },
  { q: 'How do I embed the chat widget on my website?', a: 'Copy the one-line script tag from your bot\'s "Deploy" settings and paste it before </body> on your site. No developer required.' },
  { q: 'Can I connect to WhatsApp?', a: 'Yes. Go to Channels → WhatsApp, enter your WhatsApp Business Account ID and access token. Threadly auto-configures the webhook in under 2 minutes.' },
  { q: 'How does human handoff work?', a: 'When the bot reaches a handoff node (or confidence threshold), it routes the conversation to your live inbox. Agents see the full conversation history and can take over seamlessly.' },
  { q: 'Is there a free plan?', a: 'Yes — the free plan includes 3 bots, 5,000 AI messages/month, and web widget deployment. No credit card required.' },
  { q: 'How is the AI answer accuracy so high?', a: 'Threadly uses hybrid RAG: dense vector search (Qdrant) + sparse BM25, reranked with RRF. Every answer is grounded in your documents with citations.' },
  { q: 'Can I use my own OpenAI or Anthropic key?', a: 'Yes. In Settings → AI Models, you can bring your own API key for GPT-4o, Claude, or Gemini and select the model per bot.' },
  { q: 'What analytics are available?', a: 'Conversation volume, ticket deflection rate, CSAT scores, response time p50/p95, drop-off by flow node, and per-message token costs.' },
  { q: 'Is my data secure?', a: 'Yes. Tenant-isolated infrastructure, end-to-end encryption, GDPR compliance, HIPAA-ready configuration, and full audit logs. Data never leaves your region.' },
];

export default function HelpPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl text-center mb-12">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Help Center</span>
          <h1 className="text-5xl font-bold tracking-tight">How can we help?</h1>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">Common questions answered. Can't find it? Contact support.</p>
          <div className="relative mt-6 mx-auto max-w-md flex items-center gap-2 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-3">
            <Search size={16} className="shrink-0 text-[var(--text-muted)]" />
            <input type="text" placeholder="Search help articles..." className="flex-1 bg-transparent text-sm text-[var(--text-primary)] outline-none placeholder:text-[var(--text-muted)]" />
          </div>
        </div>

        <div className="mx-auto max-w-3xl space-y-3">
          {faqs.map((faq, i) => (
            <details key={i} className="group rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden">
              <summary className="flex cursor-pointer items-center justify-between gap-4 px-6 py-4 font-semibold text-[var(--text-primary)] hover:text-[var(--accent)] transition list-none">
                {faq.q}
                <span className="text-[var(--text-muted)] group-open:rotate-180 transition-transform duration-200 shrink-0">▾</span>
              </summary>
              <div className="px-6 pb-5 text-sm text-[var(--text-secondary)] leading-relaxed border-t border-[var(--border)] pt-4">{faq.a}</div>
            </details>
          ))}
        </div>

        <div className="mt-10 mx-auto max-w-3xl rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8 text-center">
          <h2 className="text-2xl font-bold mb-3">Still need help?</h2>
          <p className="text-[var(--text-secondary)] mb-6">Our support team responds within 24 hours.</p>
          <div className="flex flex-wrap justify-center gap-3">
            <Link href="/contact" className="flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold text-white"
              style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
              Contact support <ArrowRight size={14} />
            </Link>
            <Link href="/docs" className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-6 py-3 text-sm font-semibold hover:border-[var(--accent)] transition">
              View documentation
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
