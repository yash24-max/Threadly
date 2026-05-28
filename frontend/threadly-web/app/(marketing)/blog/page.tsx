'use client';

import Link from 'next/link';
import { ArrowRight, Calendar, User, Search, Clock } from 'lucide-react';
import { useState } from 'react';

const blogPosts = [
  {
    id: 1,
    slug: 'why-chatbots-save-support-teams-40-hours-weekly',
    title: 'Why AI Chatbots Save Support Teams 40+ Hours Weekly',
    excerpt: 'How AI chatbots handle 80% of support tickets automatically, freeing your team for high-value work. Real numbers, real results.',
    author: 'Sarah Chen',
    date: '2025-05-15',
    category: 'Customer Support',
    readTime: '5 min read',
    featured: true,
  },
  {
    id: 2,
    slug: 'build-no-code-chatbots-vs-custom-development',
    title: 'No-Code Chatbots vs Custom Development: A Cost Analysis',
    excerpt: 'Compare deployment time, total cost of ownership, and flexibility. Spoiler: building custom rarely wins.',
    author: 'Mike Johnson',
    date: '2025-05-12',
    category: 'Development',
    readTime: '7 min read',
    featured: false,
  },
  {
    id: 3,
    slug: 'rag-chatbots-vs-traditional-ai-qa-systems',
    title: 'How Hybrid RAG Achieves 95%+ Answer Accuracy',
    excerpt: 'Understanding dense + sparse retrieval with RRF reranking, and why it outperforms every other knowledge base approach.',
    author: 'Emma Rodriguez',
    date: '2025-05-08',
    category: 'AI & ML',
    readTime: '8 min read',
    featured: true,
  },
  {
    id: 4,
    slug: 'sales-chatbot-qualification-framework',
    title: 'The Complete BANT Qualification Framework for Sales Bots',
    excerpt: 'Build a qualification bot that scores leads, books demos, and syncs to CRM — without human intervention.',
    author: 'Alex Kim',
    date: '2025-05-05',
    category: 'Sales',
    readTime: '10 min read',
    featured: false,
  },
  {
    id: 5,
    slug: 'multi-channel-deployment-guide',
    title: 'Deploy Your Bot to Web, WhatsApp & Instagram in 30 Minutes',
    excerpt: 'Step-by-step guide to deploying the same bot across website, WhatsApp Business, Instagram DMs, and Telegram from one dashboard.',
    author: 'Jordan Lee',
    date: '2025-05-01',
    category: 'Deployment',
    readTime: '6 min read',
    featured: false,
  },
  {
    id: 6,
    slug: 'chatbot-analytics-metrics-that-matter',
    title: 'The 8 Chatbot Metrics That Actually Drive ROI',
    excerpt: 'Drop-off rates, cost-per-conversation, CSAT, p95 response time — which numbers to watch and how to improve them.',
    author: 'David Park',
    date: '2025-04-28',
    category: 'Analytics',
    readTime: '5 min read',
    featured: false,
  },
  {
    id: 7,
    slug: 'human-handoff-best-practices',
    title: 'Human Handoff Patterns That Keep Customers Happy',
    excerpt: 'The right way to transition from bot to agent. Context preservation, warm handoff scripts, and when to escalate.',
    author: 'Lisa Wang',
    date: '2025-04-22',
    category: 'Customer Support',
    readTime: '6 min read',
    featured: false,
  },
  {
    id: 8,
    slug: 'knowledge-base-chunking-strategies',
    title: 'Knowledge Base Chunking Strategies for Better RAG Results',
    excerpt: 'Fixed-size vs semantic chunking, optimal chunk sizes, and how to structure your docs for maximum retrieval accuracy.',
    author: 'Emma Rodriguez',
    date: '2025-04-18',
    category: 'AI & ML',
    readTime: '9 min read',
    featured: false,
  },
  {
    id: 9,
    slug: 'onboarding-bot-reduces-churn',
    title: 'How Onboarding Bots Reduced 30-Day Churn by 45%',
    excerpt: 'A case study in role-based onboarding flows, feature discovery nudges, and proactive drop-off recovery sequences.',
    author: 'James Park',
    date: '2025-04-12',
    category: 'Onboarding',
    readTime: '7 min read',
    featured: false,
  },
];

const categoryColors: Record<string, string> = {
  'Customer Support': 'text-[#6366F1] bg-[#EEF2FF]',
  'Development':      'text-[#8B5CF6] bg-[#F5F3FF]',
  'AI & ML':          'text-[#06B6D4] bg-[#ECFEFF]',
  'Sales':            'text-[#059669] bg-[#ECFDF5]',
  'Deployment':       'text-[#D97706] bg-[#FFFBEB]',
  'Analytics':        'text-[#6366F1] bg-[#EEF2FF]',
  'Onboarding':       'text-[#8B5CF6] bg-[#F5F3FF]',
};

export default function BlogPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  const categories = [...new Set(blogPosts.map(post => post.category))];

  const filteredPosts = blogPosts.filter(post => {
    const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.excerpt.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = !selectedCategory || post.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const featured = filteredPosts.filter(p => p.featured);
  const regular = filteredPosts.filter(p => !p.featured);

  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-14 pt-14 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              Blog
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Insights for <span className="gradient-text">AI Chatbot Builders</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Guides, case studies, and technical deep-dives on building AI chatbots that actually work.
            </p>
            {/* Search */}
            <div className="relative mx-auto mt-8 max-w-xl">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-[var(--text-muted)]" />
              <input
                type="text"
                placeholder="Search articles..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] py-3 pl-11 pr-4 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition"
              />
            </div>
          </div>
        </section>

        {/* Category filters */}
        <div className="mt-8 flex flex-wrap gap-2 justify-center">
          <button
            onClick={() => setSelectedCategory(null)}
            className={`rounded-full px-4 py-1.5 text-sm font-semibold transition ${
              !selectedCategory
                ? 'text-white shadow-sm'
                : 'border border-[var(--border)] bg-[var(--bg-panel)] text-[var(--text-secondary)] hover:border-[var(--accent)]'
            }`}
            style={!selectedCategory ? { background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' } : {}}>
            All Topics
          </button>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat === selectedCategory ? null : cat)}
              className={`rounded-full px-4 py-1.5 text-sm font-semibold transition ${
                selectedCategory === cat
                  ? 'text-white shadow-sm'
                  : 'border border-[var(--border)] bg-[var(--bg-panel)] text-[var(--text-secondary)] hover:border-[var(--accent)]'
              }`}
              style={selectedCategory === cat ? { background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' } : {}}>
              {cat}
            </button>
          ))}
        </div>

        {/* Featured posts */}
        {featured.length > 0 && (
          <section className="mt-10">
            <h2 className="text-xl font-bold mb-5 text-[var(--text-primary)]">Featured</h2>
            <div className="grid gap-6 md:grid-cols-2">
              {featured.map(post => {
                const colorClass = categoryColors[post.category] ?? 'text-[var(--accent)] bg-[var(--accent-light)]';
                return (
                  <Link key={post.id} href={`/blog/${post.slug}`}
                    className="group rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden hover:border-[var(--accent)] hover:shadow-[0_8px_30px_rgba(99,102,241,0.12)] transition-all">
                    <div className="h-40 flex items-center justify-center text-5xl relative overflow-hidden"
                      style={{ background: 'linear-gradient(135deg,rgba(99,102,241,0.08),rgba(139,92,246,0.08))' }}>
                      <span className="absolute inset-0 opacity-[0.06]"
                        style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '24px 24px' }} />
                      <div className="relative flex h-16 w-16 items-center justify-center rounded-2xl text-white text-2xl font-bold"
                        style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                        {post.author.charAt(0)}
                      </div>
                    </div>
                    <div className="p-6">
                      <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold mb-3 ${colorClass}`}>
                        {post.category}
                      </span>
                      <h3 className="text-lg font-bold text-[var(--text-primary)] leading-snug mb-3 group-hover:text-[var(--accent)] transition-colors">
                        {post.title}
                      </h3>
                      <p className="text-sm text-[var(--text-secondary)] mb-4 line-clamp-2">{post.excerpt}</p>
                      <div className="flex items-center gap-4 text-xs text-[var(--text-muted)]">
                        <span className="flex items-center gap-1"><User size={12} /> {post.author}</span>
                        <span className="flex items-center gap-1"><Calendar size={12} />
                          {new Date(post.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                        </span>
                        <span className="flex items-center gap-1"><Clock size={12} /> {post.readTime}</span>
                      </div>
                    </div>
                  </Link>
                );
              })}
            </div>
          </section>
        )}

        {/* All posts */}
        <section className="mt-10">
          {regular.length > 0 && featured.length > 0 && (
            <h2 className="text-xl font-bold mb-5 text-[var(--text-primary)]">All Articles</h2>
          )}
          {filteredPosts.length === 0 ? (
            <div className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-16 text-center">
              <p className="text-[var(--text-muted)]">No articles found for "{searchTerm}". Try a different search.</p>
            </div>
          ) : (
            <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
              {(selected => selected)(featured.length > 0 ? regular : filteredPosts).map(post => {
                const colorClass = categoryColors[post.category] ?? 'text-[var(--accent)] bg-[var(--accent-light)]';
                return (
                  <Link key={post.id} href={`/blog/${post.slug}`}
                    className="group rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 hover:border-[var(--accent)] hover:shadow-[0_4px_20px_rgba(99,102,241,0.1)] transition-all">
                    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold mb-3 ${colorClass}`}>
                      {post.category}
                    </span>
                    <h3 className="font-bold text-[var(--text-primary)] leading-snug mb-3 group-hover:text-[var(--accent)] transition-colors">
                      {post.title}
                    </h3>
                    <p className="text-sm text-[var(--text-secondary)] mb-4 line-clamp-2">{post.excerpt}</p>
                    <div className="flex items-center justify-between text-xs text-[var(--text-muted)]">
                      <span className="flex items-center gap-1"><User size={12} /> {post.author}</span>
                      <span className="flex items-center gap-1"><Clock size={12} /> {post.readTime}</span>
                    </div>
                  </Link>
                );
              })}
            </div>
          )}
        </section>

        {/* Newsletter CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Get the latest in your inbox</h2>
          <p className="relative text-white/80 mb-7">Chatbot guides, AI news, and product updates. Unsubscribe anytime.</p>
          <div className="relative mx-auto flex max-w-md gap-2">
            <input
              type="email"
              placeholder="your@email.com"
              className="flex-1 rounded-xl border border-white/20 bg-white/10 px-4 py-3 text-sm text-white placeholder:text-white/50 outline-none backdrop-blur focus:border-white/40 transition"
            />
            <button
              className="flex items-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-bold text-[#6366F1] hover:shadow-lg transition">
              Subscribe <ArrowRight size={14} />
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}
