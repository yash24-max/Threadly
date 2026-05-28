import Link from 'next/link';

const releases = [
  {
    version: 'v2.4.0', date: 'May 28, 2026', tag: 'Major',
    changes: [
      { type: 'new', text: 'Instagram DM bot channel — connect to Instagram Professional accounts' },
      { type: 'new', text: 'WordPress plugin — 1-click install, zero code required' },
      { type: 'new', text: 'Channel-specific pages with full feature documentation' },
      { type: 'improved', text: 'Use Cases dropdown now 2-column with By Team and By Channel sections' },
      { type: 'fixed', text: 'Navbar dropdown closing too fast on hover — added 200ms grace period' },
    ],
  },
  {
    version: 'v2.3.0', date: 'May 20, 2026', tag: 'Feature',
    changes: [
      { type: 'new', text: 'Hybrid RAG v2 — improved RRF ranking with BM25 + Qdrant dense retrieval' },
      { type: 'new', text: 'Per-message cost tracking in analytics dashboard' },
      { type: 'new', text: 'Multi-LLM support — bring your own OpenAI, Anthropic, or Gemini key' },
      { type: 'improved', text: 'Knowledge base indexing speed improved 3×' },
      { type: 'fixed', text: 'Citation accuracy for multi-page PDF documents' },
    ],
  },
  {
    version: 'v2.2.0', date: 'May 10, 2026', tag: 'Feature',
    changes: [
      { type: 'new', text: 'SMS Bot channel via Twilio and Vonage integration' },
      { type: 'new', text: 'CSAT auto-scoring — collect ratings after every conversation' },
      { type: 'new', text: 'Webhook triggers — fire external APIs on conversation events' },
      { type: 'improved', text: 'Flow builder canvas performance with 50+ node flows' },
      { type: 'fixed', text: 'Variable scoping issue in nested conditional branches' },
    ],
  },
  {
    version: 'v2.1.0', date: 'April 28, 2026', tag: 'Feature',
    changes: [
      { type: 'new', text: 'Telegram Bot channel — full Bot API support with inline keyboards' },
      { type: 'new', text: 'Conversation history persistence — users can continue across sessions' },
      { type: 'improved', text: 'Streaming response latency reduced by 40%' },
      { type: 'fixed', text: 'WhatsApp template message approval status sync' },
    ],
  },
  {
    version: 'v2.0.0', date: 'April 14, 2026', tag: 'Major',
    changes: [
      { type: 'new', text: 'Complete UI redesign — new indigo design system with light theme' },
      { type: 'new', text: 'WhatsApp Business API channel with broadcast campaigns' },
      { type: 'new', text: 'Analytics v2 — funnel analysis, drop-off by node, p95 response time' },
      { type: 'new', text: 'Human handoff inbox with agent assignment rules' },
      { type: 'improved', text: 'Knowledge base supports Notion and Confluence sync' },
    ],
  },
];

const tagColors: Record<string, string> = {
  Major: 'text-[#6366F1] bg-[#EEF2FF]',
  Feature: 'text-[#059669] bg-[#ECFDF5]',
  Fix: 'text-[#D97706] bg-[#FFFBEB]',
};

const changeColors: Record<string, string> = {
  new: 'text-[#059669] bg-[#ECFDF5]',
  improved: 'text-[#6366F1] bg-[#EEF2FF]',
  fixed: 'text-[#D97706] bg-[#FFFBEB]',
};

export default function ChangelogPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-3xl px-4 pb-24 pt-10 sm:px-6">
        <div className="mb-12">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Changelog</span>
          <h1 className="text-4xl font-bold tracking-tight">What's new in Threadly</h1>
          <p className="mt-3 text-[var(--text-secondary)]">Product updates, new features, and improvements.</p>
        </div>

        <div className="space-y-8">
          {releases.map((release, i) => (
            <article key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden">
              <div className="flex items-center gap-3 border-b border-[var(--border)] bg-[var(--bg-surface)] px-6 py-4">
                <span className="text-lg font-bold text-[var(--text-primary)]">{release.version}</span>
                <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${tagColors[release.tag]}`}>{release.tag}</span>
                <span className="ml-auto text-sm text-[var(--text-muted)]">{release.date}</span>
              </div>
              <ul className="px-6 py-5 space-y-2.5">
                {release.changes.map((change, j) => (
                  <li key={j} className="flex items-start gap-3 text-sm text-[var(--text-secondary)]">
                    <span className={`inline-flex shrink-0 rounded px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wider mt-0.5 ${changeColors[change.type]}`}>
                      {change.type}
                    </span>
                    {change.text}
                  </li>
                ))}
              </ul>
            </article>
          ))}
        </div>

        <div className="mt-12 rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
          <p className="text-[var(--text-secondary)] mb-4">Want to get notified about updates?</p>
          <Link href="/blog" className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--accent)] hover:underline">
            Follow our blog for in-depth release notes →
          </Link>
        </div>
      </div>
    </div>
  );
}
