'use client';
import Link from 'next/link';
import { ArrowRight, Check, ShoppingCart, Zap, Users, BarChart3, MessageCircle, Star } from 'lucide-react';

export default function EcommercePage() {
  return (
    <div className="bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <div className="mx-auto max-w-7xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
        <section className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] px-8 pb-16 pt-16 text-center shadow-sm">
          <div className="pointer-events-none absolute inset-0 opacity-[0.04]"
            style={{ backgroundImage: 'radial-gradient(circle, #6366F1 1px, transparent 1px)', backgroundSize: '32px 32px' }} />
          <div className="relative mx-auto max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-[var(--accent-light)] bg-[var(--accent-light)] px-3 py-1 text-xs font-semibold text-[var(--accent)] mb-5">
              <ShoppingCart size={12} /> E-Commerce
            </span>
            <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
              Turn Browsers into <span className="gradient-text">Buyers</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-secondary)]">
              Recover abandoned carts, recommend products, answer pre-purchase questions, and support orders — all with AI bots across web, WhatsApp, and Instagram.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link href="/signup" className="flex items-center gap-2 rounded-xl px-7 py-3 text-sm font-bold text-white shadow-[0_4px_14px_rgba(99,102,241,0.4)] transition hover:-translate-y-0.5"
                style={{ background: 'linear-gradient(135deg,#6366F1,#8B5CF6)' }}>
                Start free <ArrowRight size={15} />
              </Link>
              <Link href="/case-studies" className="rounded-xl border border-[var(--border)] bg-[var(--bg-surface)] px-7 py-3 text-sm font-semibold hover:border-[var(--accent)] transition">
                See results
              </Link>
            </div>
          </div>
        </section>

        <div className="mt-10 grid grid-cols-2 gap-5 lg:grid-cols-4">
          {[
            { metric: '35%', label: 'Higher conversion rate' },
            { metric: '21%', label: 'Cart abandonment recovery' },
            { metric: '22%', label: 'Average order value increase' },
            { metric: '60%', label: 'Lower cost per acquisition' },
          ].map((item, i) => (
            <div key={i} className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-6 text-center">
              <p className="text-4xl font-bold gradient-text">{item.metric}</p>
              <p className="mt-2 text-sm text-[var(--text-muted)]">{item.label}</p>
            </div>
          ))}
        </div>

        <section className="mt-10 rounded-3xl border border-[var(--border)] bg-[var(--bg-panel)] p-8">
          <h2 className="text-3xl font-bold mb-8">E-Commerce Bot Capabilities</h2>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[
              { icon: ShoppingCart, title: 'Cart Recovery Bot', desc: 'Detect abandonment and automatically send personalized recovery messages via web chat, WhatsApp, or SMS with a direct checkout link.' },
              { icon: Star,         title: 'Product Recommender', desc: 'Ask about preferences, budget, and use case — then recommend the right product from your catalog using AI-powered flows.' },
              { icon: MessageCircle,title: 'Pre-Purchase Support', desc: 'Answer questions about sizing, shipping, compatibility, and returns before customers leave your site.' },
              { icon: Zap,          title: 'Order Tracking Bot', desc: 'Let customers self-serve order status, delivery updates, and return initiation — without contacting support.' },
              { icon: Users,        title: 'Post-Purchase Upsell', desc: 'After a purchase, trigger flows that offer accessories, bundles, and loyalty points to increase LTV.' },
              { icon: BarChart3,    title: 'Review Collection', desc: 'Automatically request product reviews via WhatsApp or SMS at the right time. Higher review rates, better social proof.' },
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
          <h2 className="text-3xl font-bold mb-6">Integrations & Features</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {['Shopify & WooCommerce integration', 'Real-time inventory lookup', 'Dynamic discount code delivery', 'Order status self-service', 'Return & refund initiation', 'Wishlist & back-in-stock alerts', 'Multi-currency & multi-language', 'WhatsApp catalog browsing', 'Post-purchase review requests', 'Loyalty points & rewards'].map((f, i) => (
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
          <h2 className="relative text-3xl font-bold text-white mb-3">Stop losing sales to abandoned carts</h2>
          <p className="relative text-white/80 mb-7">Free plan. No credit card. Your first recovery bot in 30 minutes.</p>
          <Link href="/signup" className="inline-flex items-center gap-2 rounded-xl bg-white px-8 py-3.5 text-sm font-bold text-[#6366F1] hover:-translate-y-0.5 transition hover:shadow-xl">
            Get started free <ArrowRight size={15} />
          </Link>
        </section>
      </div>
    </div>
  );
}
