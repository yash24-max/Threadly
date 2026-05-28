import Link from 'next/link';
import { Calendar, User, ArrowLeft, ArrowRight, Clock, Share2 } from 'lucide-react';

const blogPostsData: Record<string, {
  title: string; author: string; date: string; category: string; readTime: string;
  excerpt: string; content: string;
}> = {
  'why-chatbots-save-support-teams-40-hours-weekly': {
    title: 'Why AI Chatbots Save Support Teams 40+ Hours Weekly',
    author: 'Sarah Chen',
    date: '2025-05-15',
    category: 'Customer Support',
    readTime: '5 min read',
    excerpt: 'How AI chatbots handle 80% of support tickets automatically, freeing your team for high-value work.',
    content: `
      <h2>The Support Crisis</h2>
      <p>Today's support teams are drowning in tickets. Every day, thousands of repetitive questions flood inboxes: "How do I reset my password?" "What are your pricing tiers?" "When is my order shipping?"</p>
      <p>These questions are simple, but they consume enormous amounts of time. A typical support agent spends 60% of their day on repetitive issues that don't require human expertise.</p>

      <h2>The Math of Support Automation</h2>
      <p>Let's look at actual numbers from a 10-person support team handling 150 tickets/day:</p>
      <ul>
        <li>Repetitive questions: 80% of volume (120 tickets/day)</li>
        <li>Time per ticket: 12 minutes average</li>
        <li>Daily time wasted: 1,440 minutes on repetitive issues</li>
        <li>Weekly waste: <strong>2,400 minutes = 40 hours</strong></li>
      </ul>

      <h2>How Threadly's RAG Engine Works</h2>
      <p>Threadly doesn't use simple keyword matching — it uses hybrid Retrieval-Augmented Generation (RAG) to find the right answer from your docs:</p>
      <ul>
        <li>Dense vector search (Qdrant) finds semantically similar content</li>
        <li>Sparse BM25 search handles exact keyword matches</li>
        <li>RRF reranking combines both for maximum accuracy</li>
        <li>Every answer includes a citation to the source document</li>
      </ul>
      <p>The result: 95%+ answer relevance vs 60% for basic chatbots.</p>

      <h2>What Gets Automated</h2>
      <p>With Threadly trained on your help docs, your support bot handles:</p>
      <ul>
        <li>FAQ and knowledge base queries</li>
        <li>Account status checks via API integration</li>
        <li>Billing and pricing questions</li>
        <li>Basic troubleshooting flows</li>
        <li>Password reset guidance</li>
        <li>Order status and shipping tracking</li>
      </ul>

      <h2>Human Handoff When It Matters</h2>
      <p>Threadly knows when to escalate. When the bot reaches its confidence threshold, it seamlessly hands off to a live agent — with the full conversation context, so the customer never has to repeat themselves.</p>

      <h2>Real Results from Helio Cloud</h2>
      <p>Helio Cloud deployed Threadly's support bot trained on 400+ help articles. Results after 90 days:</p>
      <ul>
        <li>78% of tickets resolved without human involvement</li>
        <li>First-response time: 3 hours → &lt;30 seconds</li>
        <li>Annual cost savings: $220,000</li>
        <li>CSAT score: 3.8 → 4.7 out of 5</li>
      </ul>

      <h2>Getting Started in 3 Steps</h2>
      <ol>
        <li>Upload your FAQ, help docs, and policy pages to Threadly</li>
        <li>Configure escalation rules for complex issues</li>
        <li>Embed the widget on your site — one line of code</li>
      </ol>
      <p>Within a week, you'll see measurable deflection. Within a month, your support metrics transform.</p>
    `,
  },
  'build-no-code-chatbots-vs-custom-development': {
    title: 'No-Code Chatbots vs Custom Development: A Cost Analysis',
    author: 'Mike Johnson',
    date: '2025-05-12',
    category: 'Development',
    readTime: '7 min read',
    excerpt: 'Compare deployment time, total cost of ownership, and flexibility between no-code platforms and in-house builds.',
    content: `
      <h2>The Build vs Buy Decision</h2>
      <p>Every growing company faces this choice: build a chatbot in-house, or use a no-code platform like Threadly? The right answer depends on numbers most teams never calculate.</p>

      <h2>True Cost of Custom Development</h2>
      <p>Building a production-grade chatbot with RAG, multi-channel support, and analytics requires:</p>
      <ul>
        <li>Senior backend engineer: 10 weeks @ $200/hr = <strong>$80,000</strong></li>
        <li>Frontend developer: 5 weeks @ $150/hr = <strong>$30,000</strong></li>
        <li>ML engineer (RAG pipeline): 6 weeks @ $220/hr = <strong>$52,800</strong></li>
        <li>DevOps & infrastructure: 4 weeks @ $200/hr = <strong>$32,000</strong></li>
        <li>QA & testing: 3 weeks @ $120/hr = <strong>$14,400</strong></li>
      </ul>
      <p><strong>Total initial investment: $209,200</strong></p>
      <p>Ongoing maintenance: $8,000–12,000/month (security patches, LLM updates, infra)</p>

      <h2>True Cost of Threadly</h2>
      <p>Deploy a production bot in days, not months:</p>
      <ul>
        <li>Setup and configuration: 5 days @ $150/hr = <strong>$6,000</strong></li>
        <li>Platform subscription: $99–299/month</li>
        <li>Ongoing maintenance: Included in subscription</li>
      </ul>
      <p><strong>Total initial: ~$6,000</strong></p>

      <h2>3-Year Total Cost of Ownership</h2>
      <ul>
        <li>Custom: $209,200 + ($10,000 × 36) = <strong>$569,200</strong></li>
        <li>Threadly Growth: $6,000 + ($1,200 × 36) = <strong>$49,200</strong></li>
        <li><strong>Savings with Threadly: $520,000</strong></li>
      </ul>

      <h2>Time to Market</h2>
      <p>Custom development: 4–6 months before first user interaction.</p>
      <p>Threadly: Live in 1–5 days.</p>
      <p>That difference compounds. A bot live 5 months earlier starts generating value and learning from real conversations immediately.</p>

      <h2>What About Customization?</h2>
      <p>Threadly supports full customization through the flow builder and API:</p>
      <ul>
        <li>Visual flow builder with conditional logic</li>
        <li>API call nodes — connect to any external service</li>
        <li>Custom prompt engineering per node</li>
        <li>Variable management and session state</li>
        <li>Webhooks for external event triggers</li>
        <li>Full REST API for programmatic control</li>
      </ul>

      <h2>When Custom Development Wins</h2>
      <p>Custom builds make sense when you need: deeply proprietary ML models, regulatory reasons to own all infrastructure, or deeply unusual conversation patterns not supported by any platform.</p>
      <p>For 95% of teams, Threadly delivers better results faster at a fraction of the cost.</p>
    `,
  },
  'rag-chatbots-vs-traditional-ai-qa-systems': {
    title: 'How Hybrid RAG Achieves 95%+ Answer Accuracy',
    author: 'Emma Rodriguez',
    date: '2025-05-08',
    category: 'AI & ML',
    readTime: '8 min read',
    excerpt: 'The technical breakdown of hybrid dense + sparse retrieval with RRF reranking, and why it beats every other approach.',
    content: `
      <h2>Why Traditional Chatbots Fail</h2>
      <p>Classic chatbots use intent classification + keyword matching. The flow:</p>
      <ol>
        <li>User asks: "How do I cancel my subscription?"</li>
        <li>Bot searches for keywords: "cancel", "subscription"</li>
        <li>Returns the first match, even if outdated or partially wrong</li>
        <li>Result: wrong answers, frustrated users, escalation to humans</li>
      </ol>
      <p>Answer relevance with keyword search: ~60%. Customer satisfaction: low.</p>

      <h2>Retrieval-Augmented Generation (RAG)</h2>
      <p>RAG solves this by combining retrieval (find relevant docs) with generation (synthesize an accurate answer):</p>
      <ol>
        <li>User's question is converted to a vector embedding</li>
        <li>The embedding is compared against all indexed documents</li>
        <li>Top K semantically-relevant chunks are retrieved</li>
        <li>An LLM reads those chunks and generates a grounded answer</li>
        <li>The answer includes citations — "Source: Help Article 23, Section 4"</li>
      </ol>

      <h2>Why Hybrid Retrieval Wins</h2>
      <p>Threadly uses hybrid retrieval — combining two complementary approaches:</p>
      <ul>
        <li><strong>Dense retrieval (Qdrant):</strong> Vector embeddings capture semantic meaning. "Cancel subscription" finds results about "terminate plan", "stop billing", "close account" — even without exact word matches.</li>
        <li><strong>Sparse retrieval (BM25):</strong> Classic keyword search with TF-IDF weighting. Critical for exact product names, model numbers, error codes.</li>
        <li><strong>RRF Reranking:</strong> Reciprocal Rank Fusion combines both result lists, boosting documents that rank highly in both searches.</li>
      </ul>

      <h2>Performance Numbers</h2>
      <ul>
        <li>Answer relevance: <strong>95%+</strong> with hybrid RAG vs 60% for keyword search</li>
        <li>First-contact resolution: <strong>75%</strong> vs 40% for rule-based bots</li>
        <li>Customer satisfaction: <strong>4.6/5</strong> vs 3.1/5 for traditional chatbots</li>
        <li>Hallucination rate: <strong>&lt;2%</strong> (grounded in retrieved docs)</li>
      </ul>

      <h2>Semantic Chunking</h2>
      <p>Document chunking strategy matters as much as retrieval. Threadly uses semantic chunking — splitting documents at natural boundaries (headings, paragraphs, topic shifts) rather than fixed character counts. This preserves context and avoids splitting answers across chunks.</p>

      <h2>Citation Engine</h2>
      <p>Every answer Threadly generates includes the source document and section. This:</p>
      <ul>
        <li>Builds user trust — they can verify the answer</li>
        <li>Makes hallucination easily detectable</li>
        <li>Helps you identify gaps in your knowledge base</li>
        <li>Provides an audit trail for compliance use cases</li>
      </ul>

      <h2>The Future: Agentic RAG</h2>
      <p>The next evolution is agentic RAG — where the bot can decide to retrieve additional context, call APIs to get live data, or ask clarifying questions before answering. Threadly's flow builder already enables this through API call nodes and conditional retrieval.</p>
    `,
  },
  'sales-chatbot-qualification-framework': {
    title: 'The Complete BANT Qualification Framework for Sales Bots',
    author: 'Alex Kim',
    date: '2025-05-05',
    category: 'Sales',
    readTime: '10 min read',
    excerpt: 'Build a qualification bot that scores leads, books demos, and syncs to CRM — without human SDR involvement.',
    content: `
      <h2>Why Sales Bots Beat SDRs for Top-of-Funnel</h2>
      <p>SDRs are expensive, inconsistent, and can only talk to one prospect at a time. A well-built sales bot:</p>
      <ul>
        <li>Qualifies 100 leads simultaneously, 24/7</li>
        <li>Applies the same qualification framework every time</li>
        <li>Books demos directly into your reps' calendars</li>
        <li>Syncs all data to CRM automatically</li>
      </ul>

      <h2>The BANT Framework in Conversational Form</h2>
      <p>BANT (Budget, Authority, Need, Timeline) is the gold standard. Here's how to implement it as a conversation flow:</p>

      <h2>Budget Qualification</h2>
      <ul>
        <li>Ask: "What's your current budget for [category] tools?"</li>
        <li>Use ranges: "Under $5K/yr, $5K–25K/yr, $25K+ /yr"</li>
        <li>Branch: small budget → nurture sequence; large budget → fast-track to AE</li>
      </ul>

      <h2>Authority Qualification</h2>
      <ul>
        <li>Ask: "Are you the main decision-maker for tools like this?"</li>
        <li>Ask: "Who else is involved in the decision?"</li>
        <li>Tag multi-stakeholder deals for longer nurture</li>
      </ul>

      <h2>Need Qualification</h2>
      <ul>
        <li>Ask about current pain: "What's your biggest challenge with [category]?"</li>
        <li>Qualify fit: match their pain to your top use cases</li>
        <li>Use conditional branches based on answer</li>
      </ul>

      <h2>Timeline Qualification</h2>
      <ul>
        <li>Ask: "When are you looking to have a solution in place?"</li>
        <li>Hot leads (within 30 days) → book demo immediately</li>
        <li>Warm leads (1–3 months) → nurture sequence + demo offer</li>
        <li>Cold leads (3+ months) → low-touch newsletter track</li>
      </ul>

      <h2>Lead Scoring Integration</h2>
      <p>Assign scores (0–100) based on BANT responses and trigger different workflows:</p>
      <ul>
        <li>Score 80–100 → Route to AE immediately, book demo</li>
        <li>Score 50–79 → Add to marketing nurture, offer free trial</li>
        <li>Score &lt;50 → Low-touch content track</li>
      </ul>

      <h2>CRM Sync via Threadly API Nodes</h2>
      <p>Use Threadly's API call nodes to:</p>
      <ul>
        <li>Create a contact in HubSpot/Salesforce with all collected data</li>
        <li>Set lead score and lifecycle stage automatically</li>
        <li>Create a deal with estimated value</li>
        <li>Assign to the right AE based on territory or industry</li>
      </ul>

      <h2>Calendar Booking Flow</h2>
      <p>For qualified leads, embed a Calendly or Cal.com booking directly in the chat. The bot captures the booking confirmation and stores it against the CRM record.</p>
    `,
  },
  'multi-channel-deployment-guide': {
    title: 'Deploy Your Bot to Web, WhatsApp & Instagram in 30 Minutes',
    author: 'Jordan Lee',
    date: '2025-05-01',
    category: 'Deployment',
    readTime: '6 min read',
    excerpt: 'Step-by-step guide to deploying the same bot across website, WhatsApp Business, Instagram DMs, and Telegram.',
    content: `
      <h2>One Bot, Multiple Channels</h2>
      <p>The biggest mistake teams make is building separate bots for each channel. Threadly's multi-channel architecture lets you build once and deploy everywhere — the same flow, knowledge base, and logic runs across all channels.</p>

      <h2>Step 1: Web Widget (5 minutes)</h2>
      <p>The web widget is the easiest deployment:</p>
      <ol>
        <li>Go to Channels → Web Widget in your Threadly dashboard</li>
        <li>Customize colors, position, and greeting message</li>
        <li>Copy the single-line embed script</li>
        <li>Paste before &lt;/body&gt; on your website</li>
      </ol>
      <p>The widget is &lt;35KB gzipped, loads asynchronously, and works on any website or app.</p>

      <h2>Step 2: WhatsApp Business (10 minutes)</h2>
      <p>Connect Threadly to WhatsApp Business API:</p>
      <ol>
        <li>Go to Channels → WhatsApp in Threadly</li>
        <li>Enter your WhatsApp Business Account ID and access token</li>
        <li>Verify the webhook URL (auto-configured by Threadly)</li>
        <li>Test with a message — responses appear within 2 seconds</li>
      </ol>
      <p>WhatsApp supports rich messages: images, PDFs, quick-reply buttons, and list pickers.</p>

      <h2>Step 3: Instagram DMs (8 minutes)</h2>
      <p>Connect your Instagram Professional account:</p>
      <ol>
        <li>Go to Channels → Instagram</li>
        <li>Connect via Facebook Login (Instagram uses Facebook's Graph API)</li>
        <li>Authorize Threadly to receive and send DMs</li>
        <li>Enable the "Respond to story mentions" option for proactive engagement</li>
      </ol>

      <h2>Step 4: Telegram Bot (5 minutes)</h2>
      <ol>
        <li>Create a Telegram bot via @BotFather — takes 2 minutes</li>
        <li>Copy the bot token</li>
        <li>Go to Channels → Telegram in Threadly</li>
        <li>Paste the token — live immediately</li>
      </ol>

      <h2>Channel-Specific Customization</h2>
      <p>While the core flow is shared, you can override specific responses per channel using Threadly's channel context variable. WhatsApp users get WhatsApp-formatted responses (no markdown, use bold with asterisks). Web widget users get rich HTML rendering.</p>

      <h2>Analytics Across Channels</h2>
      <p>Threadly's analytics dashboard shows all channels unified — total conversations, deflection rate, CSAT, and cost — with channel breakdowns available. Identify which channels your customers prefer and optimize accordingly.</p>
    `,
  },
  'chatbot-analytics-metrics-that-matter': {
    title: 'The 8 Chatbot Metrics That Actually Drive ROI',
    author: 'David Park',
    date: '2025-04-28',
    category: 'Analytics',
    readTime: '5 min read',
    excerpt: 'Drop-off rates, cost-per-conversation, CSAT, p95 response time — which numbers to watch and how to improve them.',
    content: `
      <h2>Why Most Teams Track the Wrong Metrics</h2>
      <p>Total conversations is a vanity metric. It tells you nothing about business impact. The metrics that matter are the ones tied to cost, satisfaction, and revenue.</p>

      <h2>1. Ticket Deflection Rate</h2>
      <p>Percentage of conversations resolved without human agent involvement. Target: 70%+.</p>
      <p>How to improve: expand your knowledge base, add more flow nodes for edge cases, review conversations where bot said "I don't know".</p>

      <h2>2. First-Response Time</h2>
      <p>How quickly the bot responds to the first message. Should be &lt;2 seconds for web, &lt;5 seconds for WhatsApp.</p>

      <h2>3. Cost Per Conversation</h2>
      <p>Total AI token cost + infrastructure / number of conversations. Threadly tracks this per bot and per workspace automatically. Target: keep under $0.02 for simple FAQ bots.</p>

      <h2>4. Conversation Completion Rate</h2>
      <p>Percentage of conversations where the user got a satisfactory answer (didn't abandon mid-flow). Target: 80%+.</p>

      <h2>5. Drop-Off Rate by Node</h2>
      <p>Which step in your flow causes users to leave? Threadly's funnel view shows drop-off at each node. Fix high drop-off nodes first — usually a confusing question or missing answer.</p>

      <h2>6. CSAT Score</h2>
      <p>Post-conversation rating (1–5 stars). Threadly auto-sends a CSAT prompt at conversation end. Target: 4.2+.</p>
      <p>Correlate low CSAT with specific flows to pinpoint problems.</p>

      <h2>7. Human Handoff Rate</h2>
      <p>Percentage of conversations escalated to a human agent. Target: &lt;20% for support bots, &lt;60% for sales bots (where human close is valuable).</p>

      <h2>8. Response Latency (p50/p95)</h2>
      <p>p50 latency is median response time. p95 is the worst 5% of responses. Watch p95 — outliers hurt CSAT disproportionately. Threadly's LLM streaming keeps p95 under 3 seconds even for complex RAG queries.</p>

      <h2>The ROI Dashboard</h2>
      <p>Combine these metrics into a single ROI view:</p>
      <ul>
        <li>Monthly cost saved = (deflected tickets × avg agent cost per ticket) − platform cost</li>
        <li>Revenue attributed = (sales bot demos booked × avg deal value × close rate)</li>
        <li>Net ROI = cost saved + revenue attributed − Threadly subscription</li>
      </ul>
      <p>Most teams see 10–40× ROI within the first 90 days.</p>
    `,
  },
};

const allPosts = Object.entries(blogPostsData).map(([slug, data]) => ({ slug, ...data }));

const categoryColors: Record<string, string> = {
  'Customer Support': 'text-[#6366F1] bg-[#EEF2FF]',
  'Development':      'text-[#8B5CF6] bg-[#F5F3FF]',
  'AI & ML':          'text-[#06B6D4] bg-[#ECFEFF]',
  'Sales':            'text-[#059669] bg-[#ECFDF5]',
  'Deployment':       'text-[#D97706] bg-[#FFFBEB]',
  'Analytics':        'text-[#6366F1] bg-[#EEF2FF]',
  'Onboarding':       'text-[#8B5CF6] bg-[#F5F3FF]',
};

export default async function BlogPostPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const post = blogPostsData[slug];

  const related = allPosts.filter(p => p.slug !== slug).slice(0, 3);

  if (!post) {
    return (
      <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
        <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-20 text-center">
            <h1 className="text-4xl font-bold mb-4">Post not found</h1>
            <p className="text-[var(--text-secondary)] mb-8">This article doesn't exist or has been moved.</p>
            <Link href="/blog"
              className="inline-flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold text-white"
              style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
              <ArrowLeft size={14} /> Back to Blog
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const colorClass = categoryColors[post.category] ?? 'text-[var(--accent)] bg-[var(--accent-light)]';

  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-4xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Back link */}
        <Link href="/blog"
          className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--text-secondary)] hover:text-[var(--accent)] transition mb-8">
          <ArrowLeft size={15} /> Back to Blog
        </Link>

        {/* Article */}
        <article className="rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden">
          {/* Header banner */}
          <div className="relative h-48 overflow-hidden flex items-center justify-center"
            style={{ background: 'linear-gradient(135deg,rgba(99,102,241,0.1),rgba(139,92,246,0.1))' }}>
            <div className="absolute inset-0 opacity-[0.05]"
              style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '24px 24px' }} />
            <div className="relative flex h-20 w-20 items-center justify-center rounded-2xl text-3xl font-black text-white"
              style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
              {post.author.charAt(0)}
            </div>
          </div>

          <div className="px-8 py-10 sm:px-12">
            {/* Meta */}
            <div className="mb-6 flex flex-wrap items-center gap-3">
              <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${colorClass}`}>
                {post.category}
              </span>
              <span className="flex items-center gap-1.5 text-xs text-[var(--text-muted)]">
                <Calendar size={13} />
                {new Date(post.date).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
              </span>
              <span className="flex items-center gap-1.5 text-xs text-[var(--text-muted)]">
                <User size={13} /> {post.author}
              </span>
              <span className="flex items-center gap-1.5 text-xs text-[var(--text-muted)]">
                <Clock size={13} /> {post.readTime}
              </span>
            </div>

            {/* Title */}
            <h1 className="text-4xl font-bold text-[var(--text-primary)] leading-tight mb-4 sm:text-5xl">
              {post.title}
            </h1>

            <p className="text-lg text-[var(--text-secondary)] mb-6 leading-relaxed">{post.excerpt}</p>

            {/* Share */}
            <button className="mb-8 flex items-center gap-2 rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-2 text-sm font-semibold text-[var(--text-secondary)] hover:border-[var(--accent)] hover:text-[var(--accent)] transition">
              <Share2 size={15} /> Share article
            </button>

            {/* Content */}
            <div
              className="blog-content text-[var(--text-secondary)]"
              style={{
                lineHeight: 1.8,
                fontSize: '1rem',
              }}
              dangerouslySetInnerHTML={{
                __html: post.content
                  .replace(/<h2>/g, '<h2 style="margin-top:2rem;margin-bottom:0.75rem;font-size:1.5rem;font-weight:700;color:var(--text-primary);">')
                  .replace(/<p>/g, '<p style="margin-bottom:1rem;">')
                  .replace(/<ul>/g, '<ul style="margin-bottom:1rem;padding-left:1.5rem;list-style-type:disc;">')
                  .replace(/<ol>/g, '<ol style="margin-bottom:1rem;padding-left:1.5rem;list-style-type:decimal;">')
                  .replace(/<li>/g, '<li style="margin-bottom:0.5rem;">')
              }}
            />

            {/* Author bio */}
            <div className="mt-12 rounded-2xl border border-[var(--border)] bg-[var(--bg-surface)] p-6 flex items-start gap-4">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-sm font-bold text-white"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                {post.author.split(' ').map(n => n[0]).join('')}
              </div>
              <div>
                <p className="font-bold text-[var(--text-primary)]">{post.author}</p>
                <p className="mt-1 text-sm text-[var(--text-secondary)]">
                  AI and chatbot expert at Threadly. Helps teams deploy intelligent bots that actually work.
                </p>
              </div>
            </div>

            {/* Inline CTA */}
            <div className="mt-10 overflow-hidden rounded-2xl p-8 text-center relative"
              style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
              <div className="pointer-events-none absolute inset-0 opacity-10"
                style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '24px 24px' }} />
              <h3 className="relative text-2xl font-bold text-white mb-2">Ready to build your own bot?</h3>
              <p className="relative text-white/80 mb-6">Start free — no credit card required.</p>
              <Link href="/signup"
                className="inline-flex items-center gap-2 rounded-xl bg-white px-6 py-3 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
                Get started free <ArrowRight size={14} />
              </Link>
            </div>
          </div>
        </article>

        {/* Related posts */}
        {related.length > 0 && (
          <section className="mt-12">
            <h2 className="text-2xl font-bold mb-6 text-[var(--text-primary)]">More Articles</h2>
            <div className="grid gap-5 md:grid-cols-3">
              {related.map(rp => {
                const rc = categoryColors[rp.category] ?? 'text-[var(--accent)] bg-[var(--accent-light)]';
                return (
                  <Link key={rp.slug} href={`/blog/${rp.slug}`}
                    className="group rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 hover:border-[var(--accent)] hover:shadow-[0_4px_20px_rgba(99,102,241,0.1)] transition-all">
                    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold mb-3 ${rc}`}>
                      {rp.category}
                    </span>
                    <h3 className="text-sm font-bold text-[var(--text-primary)] leading-snug mb-2 group-hover:text-[var(--accent)] transition-colors line-clamp-2">
                      {rp.title}
                    </h3>
                    <p className="text-xs text-[var(--text-muted)] flex items-center gap-1">
                      <Clock size={11} /> {rp.readTime}
                    </p>
                  </Link>
                );
              })}
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
