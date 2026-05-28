export default function TermsPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-3xl px-4 pb-24 pt-10 sm:px-6">
        <div className="mb-10">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Legal</span>
          <h1 className="text-4xl font-bold tracking-tight">Terms of Service</h1>
          <p className="mt-3 text-[var(--text-muted)]">Last updated: May 28, 2026</p>
        </div>
        <div className="space-y-6 text-[var(--text-secondary)]">
          {[
            { title: '1. Acceptance of Terms', body: 'By accessing or using Threadly, you agree to be bound by these Terms of Service. If you do not agree, you may not use the platform.' },
            { title: '2. Account Registration', body: 'You must provide accurate information when creating an account. You are responsible for maintaining the security of your account credentials and for all activities that occur under your account.' },
            { title: '3. Acceptable Use', body: 'You may not use Threadly to transmit spam, illegal content, or material that violates third-party rights. You may not attempt to reverse-engineer the platform, exceed your plan limits, or resell access without authorization.' },
            { title: '4. Subscription and Billing', body: 'Paid plans are billed monthly or annually in advance. Unused portions of a subscription are non-refundable. You may cancel at any time; cancellation takes effect at the end of the current billing period.' },
            { title: '5. Data Ownership', body: 'You retain ownership of all data you upload to Threadly, including conversation logs, knowledge base documents, and bot configurations. You grant Threadly a limited license to process this data solely to provide the service.' },
            { title: '6. Service Availability', body: 'Threadly targets 99.9% uptime. Planned maintenance is announced 48 hours in advance. We are not liable for downtime caused by third-party providers (LLMs, channel APIs, infrastructure).' },
            { title: '7. Limitation of Liability', body: 'To the maximum extent permitted by law, Threadly\'s liability for any claim is limited to the fees paid in the 3 months preceding the claim. We are not liable for indirect, consequential, or punitive damages.' },
            { title: '8. Termination', body: 'We may terminate accounts that violate these terms or remain inactive for 12+ months (with 30 days notice). Upon termination, your data will be deleted within 30 days.' },
            { title: '9. Governing Law', body: 'These terms are governed by the laws of Delaware, USA. Disputes shall be resolved by binding arbitration under AAA rules, except for injunctive relief claims.' },
            { title: '10. Contact', body: 'Legal inquiries: legal@threadly.ai' },
          ].map((section, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6">
              <h2 className="text-lg font-bold text-[var(--text-primary)] mb-3">{section.title}</h2>
              <p className="text-sm leading-relaxed">{section.body}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
