export default function PrivacyPage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-3xl px-4 pb-24 pt-10 sm:px-6">
        <div className="mb-10">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Legal</span>
          <h1 className="text-4xl font-bold tracking-tight">Privacy Policy</h1>
          <p className="mt-3 text-[var(--text-muted)]">Last updated: May 28, 2026</p>
        </div>
        <div className="prose max-w-none space-y-8 text-[var(--text-secondary)]">
          {[
            { title: '1. Information We Collect', body: 'We collect information you provide directly (account registration, bot configurations, uploaded documents), information generated through your use of the platform (conversation logs, analytics events), and technical information (IP address, browser type, device identifiers).' },
            { title: '2. How We Use Your Information', body: 'We use collected information to provide and improve the Threadly platform, process transactions, send service-related communications, generate aggregated analytics, and comply with legal obligations. We do not sell your personal data to third parties.' },
            { title: '3. Data Storage and Security', body: 'All data is stored with AES-256 encryption at rest and TLS 1.3 in transit. We operate in tenant-isolated environments to prevent cross-customer data access. We undergo annual SOC 2 Type II audits.' },
            { title: '4. Your Rights (GDPR)', body: 'If you are in the EU/EEA, you have the right to access, rectify, erase, and port your personal data. You may also object to processing or restrict processing in certain circumstances. Contact privacy@threadly.ai to exercise your rights.' },
            { title: '5. Data Retention', body: 'Account data is retained while your account is active. Conversation logs are retained for 12 months by default (configurable). Uploaded knowledge base documents are retained until you delete them. Upon account deletion, data is purged within 30 days.' },
            { title: '6. Third-Party Services', body: 'Threadly integrates with third-party LLM providers (OpenAI, Anthropic, Google) and channel providers (Meta, Twilio, Telegram). Your data transmitted to these services is governed by their respective privacy policies.' },
            { title: '7. Cookies', body: 'We use essential cookies for session management and authentication. We use analytics cookies to understand platform usage. You may opt out of analytics cookies at any time via your account settings.' },
            { title: '8. Contact', body: 'For privacy inquiries: privacy@threadly.ai. For data deletion requests: privacy@threadly.ai. Response time: within 72 hours.' },
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
