'use client';
import { useState } from 'react';
import Link from 'next/link';
import { ArrowRight, Mail, MessageSquare, Calendar } from 'lucide-react';

export default function ContactPage() {
  const [submitted, setSubmitted] = useState(false);

  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl text-center mb-12">
          <span className="inline-flex rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-4">Contact</span>
          <h1 className="text-5xl font-bold tracking-tight">Get in touch</h1>
          <p className="mt-4 text-lg text-[var(--text-secondary)]">Questions, demos, or enterprise inquiries — we're here to help.</p>
        </div>

        <div className="grid gap-6 lg:grid-cols-3 mb-10">
          {[
            { icon: MessageSquare, title: 'General Support', desc: 'Questions about features, billing, or your account.', cta: 'support@threadly.ai', href: 'mailto:support@threadly.ai' },
            { icon: Calendar,      title: 'Book a Demo',    desc: 'See Threadly in action with a 30-min guided walkthrough.', cta: 'Schedule a call', href: '/signup' },
            { icon: Mail,          title: 'Sales',          desc: 'Enterprise pricing, custom contracts, or volume deals.', cta: 'sales@threadly.ai', href: 'mailto:sales@threadly.ai' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6">
              <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-lg text-white"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                <item.icon size={16} />
              </div>
              <h3 className="font-bold text-[var(--text-primary)] mb-2">{item.title}</h3>
              <p className="text-sm text-[var(--text-secondary)] mb-4">{item.desc}</p>
              <a href={item.href} className="text-sm font-semibold text-[var(--accent)] hover:underline flex items-center gap-1">
                {item.cta} <ArrowRight size={13} />
              </a>
            </div>
          ))}
        </div>

        <div className="mx-auto max-w-xl rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          {submitted ? (
            <div className="text-center py-8">
              <div className="mb-4 flex h-14 w-14 mx-auto items-center justify-center rounded-full text-white text-2xl"
                style={{ background: 'linear-gradient(135deg,#059669,#10B981)' }}>✓</div>
              <h2 className="text-2xl font-bold mb-2">Message sent!</h2>
              <p className="text-[var(--text-secondary)]">We'll get back to you within 24 hours.</p>
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-bold mb-6">Send us a message</h2>
              <form onSubmit={(e) => { e.preventDefault(); setSubmitted(true); }} className="space-y-4">
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-1.5">Name</label>
                    <input required type="text" placeholder="Your name" className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-2.5 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-1.5">Email</label>
                    <input required type="email" placeholder="you@company.com" className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-2.5 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition" />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-1.5">Subject</label>
                  <input type="text" placeholder="How can we help?" className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-2.5 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-1.5">Message</label>
                  <textarea required rows={4} placeholder="Tell us about your use case..." className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-2.5 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition resize-none" />
                </div>
                <button type="submit" className="w-full flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-bold text-white transition hover:-translate-y-0.5"
                  style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                  Send message <ArrowRight size={14} />
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
