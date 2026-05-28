'use client';

import Link from 'next/link';
import { ArrowRight, TrendingUp, Check } from 'lucide-react';

const caseStudies = [
  {
    company: 'Helio Cloud',
    industry: 'SaaS',
    logo: 'HC',
    color: '#6366F1',
    challenge: 'Support team overwhelmed — 3-hour avg first-response time, 150 tickets/day, 80% repetitive.',
    solution: 'Deployed Threadly knowledge-base bot trained on 400+ help docs. Enabled human handoff for complex issues.',
    results: [
      '78% of tickets resolved by bot without human',
      'First-response time cut from 3 hours to < 30 seconds',
      '$220K annual support cost reduction',
      'CSAT improved from 3.8 to 4.7 / 5',
    ],
    quote: 'Threadly\'s hybrid RAG means our bot actually understands what customers are asking, not just keyword-matching. Night and day difference.',
    quoter: 'Sarah Chen, VP of Support at Helio Cloud',
    metrics: [{ label: 'Ticket Deflection', value: '78%' }, { label: 'Cost Saved', value: '$220K' }, { label: 'CSAT Score', value: '4.7' }],
  },
  {
    company: 'Orbit Growth',
    industry: 'B2B SaaS',
    logo: 'OG',
    color: '#8B5CF6',
    challenge: 'Sales team spending 60% of time on unqualified leads. Demo bookings slow, CRM data incomplete.',
    solution: 'Built BANT qualification flow with calendar integration and automatic HubSpot CRM sync.',
    results: [
      '40+ demos booked per month without SDR involvement',
      '3× improvement in lead-to-demo conversion rate',
      'Sales cycle shortened from 45 to 28 days',
      'CRM data quality went from 40% to 95% complete',
    ],
    quote: 'Our sales bot qualifies leads while we sleep. It booked 43 demos last month. The ROI paid for 3 years of Threadly in one quarter.',
    quoter: 'Marcus Williams, VP Sales at Orbit Growth',
    metrics: [{ label: 'Demos / Month', value: '40+' }, { label: 'Cycle Reduced', value: '38%' }, { label: 'CRM Quality', value: '95%' }],
  },
  {
    company: 'NorthStack',
    industry: 'Enterprise Software',
    logo: 'NS',
    color: '#06B6D4',
    challenge: 'New enterprise customers slow to activate. Avg time-to-value: 47 days. Early churn 28% in first 90 days.',
    solution: 'Personalized onboarding flows by user role (admin vs. end-user). Progress tracking, nudge sequences, and feature discovery tips.',
    results: [
      'Time-to-value reduced from 47 days to 18 days',
      '90-day churn fell from 28% to 14%',
      'Feature adoption rate 3× higher in first 30 days',
      '85% onboarding completion (up from 42%)',
    ],
    quote: 'We saw immediate impact. New customers activate 2.6× faster. And our CS team is no longer babysitting every onboarding manually.',
    quoter: 'Tom Fischer, CTO at NorthStack',
    metrics: [{ label: 'Time-to-Value', value: '−62%' }, { label: 'Churn Cut', value: '50%' }, { label: 'Completion', value: '85%' }],
  },
  {
    company: 'MedEngage',
    industry: 'Healthcare',
    logo: 'ME',
    color: '#059669',
    challenge: 'Patient intake process manual, taking 22 minutes per patient. Compliance risks from inconsistent data collection.',
    solution: 'HIPAA-compliant intake bot with structured data collection, EHR integration, and zero human touch for standard intake.',
    results: [
      'Intake time cut from 22 minutes to 3.5 minutes',
      'Zero compliance incidents in 18 months post-deployment',
      '96% data completeness (up from 71%)',
      'Patient NPS improved by 28 points',
    ],
    quote: 'Threadly\'s enterprise security features gave us the HIPAA compliance we needed from day one. Patients love it and staff are finally free to focus on care.',
    quoter: 'Dr. Jennifer Lee, CMO at MedEngage',
    metrics: [{ label: 'Intake Time', value: '−84%' }, { label: 'Data Complete', value: '96%' }, { label: 'NPS Gain', value: '+28' }],
  },
  {
    company: 'CartRevive',
    industry: 'E-commerce',
    logo: 'CR',
    color: '#D97706',
    challenge: '72% cart abandonment rate, poor conversion on PDP pages, low repeat purchase rate.',
    solution: 'Proactive cart recovery bot, product recommendation flows, and post-purchase upsell sequences across web and WhatsApp.',
    results: [
      'Cart abandonment rate reduced from 72% to 51%',
      '35% lift in overall conversion rate',
      'Average order value up 22% with upsell flows',
      'WhatsApp channel became #1 revenue channel in 60 days',
    ],
    quote: 'The WhatsApp flow alone generates $40K revenue monthly. Our chatbot is now our highest-performing marketing channel.',
    quoter: 'Lisa Wang, Head of Marketing at CartRevive',
    metrics: [{ label: 'Conversion Lift', value: '+35%' }, { label: 'AOV Increase', value: '+22%' }, { label: 'Cart Recovery', value: '21%' }],
  },
  {
    company: 'FluxOps',
    industry: 'Operations Platform',
    logo: 'FO',
    color: '#EF4444',
    challenge: 'Internal team spending 20+ hours/week on manual data queries, report requests, and workflow triggers.',
    solution: 'Internal Slack bot with natural-language database queries, automated report generation, and workflow automation triggers.',
    results: [
      '20+ hours/week saved across 50-person team',
      '95% reduction in ad-hoc report requests to data team',
      'Slack interruptions reduced by 60%',
      'Complete audit trail for all automated actions',
    ],
    quote: 'Our team asks the bot "show me last week\'s conversion funnel by region" and gets a chart in Slack in 3 seconds. No SQL, no waiting.',
    quoter: 'Alex Kumar, Head of Operations at FluxOps',
    metrics: [{ label: 'Time Saved/wk', value: '20+ hrs' }, { label: 'Report Requests', value: '−95%' }, { label: 'Team Size', value: '50+' }],
  },
];

const overallStats = [
  { stat: '2,400+', label: 'Teams using Threadly' },
  { stat: '78%', label: 'Average ticket deflection' },
  { stat: '$2.5M+', label: 'Customer cost savings tracked' },
  { stat: '4.8/5', label: 'Average CSAT across deployments' },
];

export default function CaseStudiesPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">

        {/* Hero */}
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              Case Studies
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Real Teams, <span className="gradient-text">Real Results</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              See how leading companies use Threadly to automate support, accelerate sales, and cut operational costs.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup"
                className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Start your own story <ArrowRight size={15} />
              </Link>
            </div>
          </div>
        </section>

        {/* Overall stats */}
        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {overallStats.map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.stat}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        {/* Case studies grid */}
        <section className="mt-10 grid gap-6 lg:grid-cols-2">
          {caseStudies.map((study, i) => (
            <article key={i} className="overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] transition hover:border-[var(--accent)] hover:shadow-[0_8px_30px_rgba(99,102,241,0.1)]">
              {/* Card header */}
              <div className="flex items-center justify-between gap-4 border-b border-[var(--border)] bg-[var(--bg-surface)] px-7 py-5">
                <div className="flex items-center gap-4">
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl text-sm font-bold text-white"
                    style={{ background: `linear-gradient(135deg, ${study.color}, ${study.color}99)` }}>
                    {study.logo}
                  </div>
                  <div>
                    <h3 className="font-bold text-[var(--text-primary)]">{study.company}</h3>
                    <p className="text-xs text-[var(--text-muted)]">{study.industry}</p>
                  </div>
                </div>
                <TrendingUp className="h-5 w-5 shrink-0 text-[var(--success)]" />
              </div>

              {/* Metrics */}
              <div className="grid grid-cols-3 divide-x divide-[var(--border)] border-b border-[var(--border)]">
                {study.metrics.map((m, mi) => (
                  <div key={mi} className="py-4 text-center px-2">
                    <p className="text-xl font-bold gradient-text">{m.value}</p>
                    <p className="mt-0.5 text-xs text-[var(--text-muted)]">{m.label}</p>
                  </div>
                ))}
              </div>

              {/* Body */}
              <div className="px-7 py-6">
                <div className="mb-4">
                  <p className="mb-1 text-xs font-bold uppercase tracking-widest text-[var(--text-muted)]">Challenge</p>
                  <p className="text-sm text-[var(--text-secondary)]">{study.challenge}</p>
                </div>
                <div className="mb-4">
                  <p className="mb-1 text-xs font-bold uppercase tracking-widest text-[var(--text-muted)]">Solution</p>
                  <p className="text-sm text-[var(--text-secondary)]">{study.solution}</p>
                </div>
                <div className="mb-5">
                  <p className="mb-2 text-xs font-bold uppercase tracking-widest text-[var(--text-muted)]">Results</p>
                  <ul className="space-y-1.5">
                    {study.results.map((result, j) => (
                      <li key={j} className="flex items-start gap-2 text-sm text-[var(--text-secondary)]">
                        <Check size={13} className="mt-0.5 shrink-0 text-[var(--success)]" />
                        {result}
                      </li>
                    ))}
                  </ul>
                </div>

                <blockquote className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-4">
                  <p className="text-sm italic text-[var(--text-primary)]">"{study.quote}"</p>
                  <p className="mt-2 text-xs font-semibold text-[var(--text-muted)]">— {study.quoter}</p>
                </blockquote>
              </div>
            </article>
          ))}
        </section>

        {/* Testimonials row */}
        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8 text-center">What our customers say</h2>
          <div className="grid gap-5 md:grid-cols-3">
            {[
              { text: 'Best platform investment of the year. The ROI was clear within 3 weeks.', author: 'CTO, Fortune 500 Retail' },
              { text: 'Our support team finally has time to focus on complex issues. The bot handles everything else.', author: 'VP Support, Series B SaaS' },
              { text: 'Finally a no-code tool that doesn\'t compromise on power. The RAG quality is exceptional.', author: 'Head of AI, Enterprise Software' },
            ].map((t, i) => (
              <div key={i} className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] p-6">
                <div className="mb-3 flex">
                  {[1,2,3,4,5].map(s => (
                    <svg key={s} className="h-4 w-4 text-[var(--warn)]" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                    </svg>
                  ))}
                </div>
                <p className="text-sm text-[var(--text-secondary)] mb-4 italic">"{t.text}"</p>
                <p className="text-xs font-semibold text-[var(--text-primary)]">— {t.author}</p>
              </div>
            ))}
          </div>
        </section>

        {/* CTA */}
        <section className="mt-10 overflow-hidden rounded-3xl p-10 text-center relative"
          style={{ background: 'linear-gradient(135deg,#6366F1 0%,#8B5CF6 60%,#06B6D4 100%)' }}>
          <div className="pointer-events-none absolute inset-0 opacity-10"
            style={{ backgroundImage: 'radial-gradient(circle, white 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <h2 className="relative text-3xl font-bold text-white mb-3">Ready to write your own success story?</h2>
          <p className="relative text-white/80 mb-7 max-w-md mx-auto">
            Join 2,400+ teams automating support, sales, and operations with Threadly.
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
