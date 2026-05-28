"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import React, { useState, useEffect, useRef } from "react";
import { Logo } from "@/components/ui/Logo";
import {
  Menu, X, ChevronDown, ChevronRight,
  MessageCircle, Target, Wallet, MessageSquare,
  Bot, Instagram, Send, FileText, BookOpen,
  Cpu, Users, Workflow, Database,
  HelpCircle, ArrowRight, ShoppingCart, Briefcase,
  BarChart3, Globe, Zap, ShieldCheck, UserCheck, Headphones,
} from "lucide-react";

/* ── Nav data ─────────────────────────────────────────────────────────────── */

type NavItem = { icon: React.ElementType; label: string; href: string; desc?: string };

// Business use cases — left column
const useCasesLeft: NavItem[] = [
  { icon: Headphones,    label: "Customer Support",   href: "/use-cases/customer-support", desc: "Automate tier-1 support 24/7"       },
  { icon: Wallet,        label: "Sales",               href: "/use-cases/sales",              desc: "Qualify & close leads faster"      },
  { icon: Target,        label: "Marketing",           href: "/use-cases/marketing",          desc: "Nurture & convert visitors"         },
  { icon: UserCheck,     label: "Onboarding",          href: "/use-cases/onboarding",         desc: "Guide users to aha-moment"          },
  { icon: ShoppingCart,  label: "E-Commerce",          href: "/use-cases/ecommerce",          desc: "Orders, returns & recommendations" },
  { icon: Briefcase,     label: "HR & Recruiting",     href: "/use-cases/hr",                 desc: "Screen candidates & FAQs"           },
];

// Channel / product capabilities — right column
const useCasesRight: NavItem[] = [
  { icon: Bot,           label: "Web Chat Widget",     href: "/channels/web-widget", desc: "Embed on any website in 2 mins"    },
  { icon: MessageCircle, label: "WhatsApp Business",   href: "/channels/whatsapp",   desc: "Business API, broadcasts & flows"  },
  { icon: Instagram,     label: "Instagram DM Bot",    href: "/channels/instagram",  desc: "Auto-reply & story reactions"      },
  { icon: Send,          label: "Telegram Bot",        href: "/channels/telegram",   desc: "Bot API + inline keyboards"        },
  { icon: MessageSquare, label: "SMS Bot",             href: "/channels/sms",        desc: "Twilio / Vonage integration"       },
  { icon: FileText,      label: "WordPress Plugin",    href: "/channels/wordpress",  desc: "One-click install, no code"        },
];

const useCases: NavItem[] = [...useCasesLeft, ...useCasesRight];

const resources: NavItem[] = [
  { icon: BookOpen,   label: "Documentation", href: "/docs",         desc: "API references & guides"     },
  { icon: FileText,   label: "Blog",          href: "/blog",         desc: "Tips, tutorials & updates"   },
  { icon: Cpu,        label: "Case Studies",  href: "/case-studies", desc: "How teams use Threadly"      },
  { icon: HelpCircle, label: "Help Center",   href: "/help",         desc: "Answers to common questions" },
];

/* ── Dropdown ─────────────────────────────────────────────────────────────── */

function NavDropdown({ label, children, wide }: { label: string; children: React.ReactNode; wide?: boolean }) {
  const [open, setOpen] = useState(false);
  const closeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  function handleMouseEnter() {
    if (closeTimer.current) clearTimeout(closeTimer.current);
    setOpen(true);
  }

  function handleMouseLeave() {
    // 200ms grace period so cursor can move from button to dropdown without closing
    closeTimer.current = setTimeout(() => setOpen(false), 200);
  }

  return (
    <div className="relative" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
      <button
        className="flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)] transition-colors duration-150"
        onClick={() => setOpen(v => !v)}
      >
        {label}
        <ChevronDown size={13} className={`transition-transform duration-200 ${open ? "rotate-180" : ""}`} />
      </button>
      {open && (
        <div className={`absolute left-1/2 top-[calc(100%+2px)] z-50 -translate-x-1/2 pt-2 ${wide ? "w-[640px]" : "w-[520px]"}`}
          onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
          <div className="rounded-2xl border border-[var(--border)] bg-[var(--bg-panel)] p-3 shadow-[var(--shadow-xl)]">
            {children}
          </div>
        </div>
      )}
    </div>
  );
}

function DropdownGrid({ items }: { items: NavItem[] }) {
  return (
    <div className="grid grid-cols-2 gap-0.5">
      {items.map(item => (
        <Link key={item.href + item.label} href={item.href}
          className="flex items-start gap-3 rounded-xl px-3 py-2.5 transition-colors hover:bg-[var(--bg-surface)] group">
          <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[var(--accent-light)] text-[var(--accent)]">
            <item.icon size={15} />
          </span>
          <div>
            <p className="text-sm font-medium text-[var(--text-primary)] group-hover:text-[var(--accent)] transition-colors leading-tight">
              {item.label}
            </p>
            {item.desc && <p className="mt-0.5 text-xs text-[var(--text-muted)]">{item.desc}</p>}
          </div>
        </Link>
      ))}
    </div>
  );
}

/* ── Navbar ───────────────────────────────────────────────────────────────── */

function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 8);
    window.addEventListener("scroll", handler, { passive: true });
    return () => window.removeEventListener("scroll", handler);
  }, []);

  useEffect(() => { setMobileOpen(false); }, [pathname]);

  return (
    <>
      <header className={`fixed inset-x-0 top-0 z-40 transition-all duration-200 ${
        scrolled
          ? "bg-white/90 backdrop-blur-md border-b border-[var(--border)] shadow-sm"
          : "bg-transparent"
      }`}>
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">

          {/* Logo */}
          <Logo size="sm" variant="dark" />

          {/* Desktop nav */}
          <nav className="hidden items-center md:flex">
            <Link href="/"
              className="rounded-lg px-3 py-2 text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)] transition-colors">
              Home
            </Link>
            <NavDropdown label="Use Cases" wide>
              <div className="grid grid-cols-2 divide-x divide-[var(--border)]">
                {/* Left: Business use cases */}
                <div className="pr-2">
                  <p className="mb-1.5 px-3 pt-1 text-[10px] font-semibold uppercase tracking-widest text-[var(--text-muted)]">By Team</p>
                  {useCasesLeft.map(item => (
                    <Link key={item.label} href={item.href}
                      className="flex items-center justify-between rounded-xl px-3 py-2.5 transition-colors hover:bg-[var(--bg-surface)] group">
                      <div className="flex items-center gap-3">
                        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[var(--accent-light)] text-[var(--accent)]">
                          <item.icon size={14} />
                        </span>
                        <div>
                          <p className="text-sm font-semibold text-[var(--text-primary)] group-hover:text-[var(--accent)] transition-colors leading-tight">{item.label}</p>
                          {item.desc && <p className="text-xs text-[var(--text-muted)]">{item.desc}</p>}
                        </div>
                      </div>
                      <ChevronRight size={13} className="text-[var(--text-muted)] opacity-0 group-hover:opacity-100 transition-opacity" />
                    </Link>
                  ))}
                </div>
                {/* Right: Channel bots */}
                <div className="pl-2">
                  <p className="mb-1.5 px-3 pt-1 text-[10px] font-semibold uppercase tracking-widest text-[var(--text-muted)]">By Channel</p>
                  {useCasesRight.map(item => (
                    <Link key={item.label} href={item.href}
                      className="flex items-center justify-between rounded-xl px-3 py-2.5 transition-colors hover:bg-[var(--bg-surface)] group">
                      <div className="flex items-center gap-3">
                        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[var(--accent-light)] text-[var(--accent)]">
                          <item.icon size={14} />
                        </span>
                        <div>
                          <p className="text-sm font-semibold text-[var(--text-primary)] group-hover:text-[var(--accent)] transition-colors leading-tight">{item.label}</p>
                          {item.desc && <p className="text-xs text-[var(--text-muted)]">{item.desc}</p>}
                        </div>
                      </div>
                      <ChevronRight size={13} className="text-[var(--text-muted)] opacity-0 group-hover:opacity-100 transition-opacity" />
                    </Link>
                  ))}
                </div>
              </div>
            </NavDropdown>
            <NavDropdown label="Resources">
              <DropdownGrid items={resources} />
            </NavDropdown>
            <Link href="/#pricing"
              className="rounded-lg px-3 py-2 text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)] transition-colors">
              Pricing
            </Link>
          </nav>

          {/* CTA row */}
          <div className="hidden items-center gap-2 md:flex">
            <Link href="/login"
              className="rounded-lg px-4 py-2 text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)] transition-colors">
              Login
            </Link>
            <Link href="/signup"
              className="flex items-center gap-1.5 rounded-lg px-5 py-2.5 text-sm font-semibold text-white shadow-[var(--shadow-accent)] transition-all hover:-translate-y-px hover:shadow-lg"
              style={{ background: "linear-gradient(135deg, #6366F1, #8B5CF6)" }}>
              Get Started Free
              <ArrowRight size={14} />
            </Link>
          </div>

          {/* Mobile hamburger */}
          <button
            className="rounded-lg p-2 text-[var(--text-secondary)] hover:bg-[var(--bg-surface)] md:hidden"
            onClick={() => setMobileOpen(v => !v)}
          >
            {mobileOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </header>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="fixed inset-0 z-30 flex flex-col bg-[var(--bg-panel)] pt-16 md:hidden">
          <div className="flex-1 overflow-y-auto px-4 py-6 space-y-1">
            {[
              { href: "/",             label: "Home"         },
              { href: "/#pricing",     label: "Pricing"      },
              { href: "/blog",         label: "Blog"         },
              { href: "/docs",         label: "Docs"         },
              { href: "/case-studies", label: "Case Studies" },
            ].map(item => (
              <Link key={item.href} href={item.href}
                className="flex items-center justify-between rounded-xl px-4 py-3 text-base font-medium text-[var(--text-primary)] hover:bg-[var(--bg-surface)]">
                {item.label}
                <ChevronRight size={16} className="text-[var(--text-muted)]" />
              </Link>
            ))}
            <div className="mt-4 space-y-3 border-t border-[var(--border)] pt-4">
              <Link href="/login"
                className="block w-full rounded-xl border border-[var(--border)] py-3 text-center text-base font-semibold text-[var(--text-primary)]">
                Login
              </Link>
              <Link href="/signup"
                className="block w-full rounded-xl py-3 text-center text-base font-semibold text-white"
                style={{ background: "linear-gradient(135deg, #6366F1, #8B5CF6)" }}>
                Get Started Free
              </Link>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

/* ── Footer ───────────────────────────────────────────────────────────────── */

const footerLinks = {
  Platform: [
    { label: "Flow Builder",     href: "/use-cases/customer-support" },
    { label: "Knowledge Base",   href: "/use-cases/knowledge"        },
    { label: "Live Inbox",       href: "/use-cases/customer-support" },
    { label: "Analytics",        href: "/comparison"                 },
    { label: "Widget Embed",     href: "/use-cases/marketing"        },
    { label: "Integrations",     href: "/use-cases/internal-tools"   },
  ],
  "Use Cases": [
    { label: "Customer Support", href: "/use-cases/customer-support" },
    { label: "Sales",            href: "/use-cases/sales"            },
    { label: "Marketing",        href: "/use-cases/marketing"        },
    { label: "Onboarding",       href: "/use-cases/onboarding"       },
    { label: "Internal Tools",   href: "/use-cases/internal-tools"   },
  ],
  Resources: [
    { label: "Documentation", href: "/docs"         },
    { label: "Blog",          href: "/blog"         },
    { label: "Case Studies",  href: "/case-studies" },
    { label: "Help Center",   href: "/help"         },
    { label: "Changelog",     href: "/changelog"    },
  ],
  Company: [
    { label: "About",           href: "/about"    },
    { label: "Pricing",         href: "/#pricing" },
    { label: "Privacy Policy",  href: "/privacy"  },
    { label: "Terms of Service",href: "/terms"    },
    { label: "Contact",         href: "/contact"  },
  ],
};

function Footer() {
  return (
    <footer className="border-t border-[var(--border)] bg-[var(--bg-panel)] py-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-10 lg:grid-cols-6">
          {/* Brand col */}
          <div className="col-span-2">
            <Logo size="sm" variant="dark" />
            <p className="mt-4 max-w-xs text-sm leading-relaxed text-[var(--text-muted)]">
              Build AI chatbots trained on your data. Deploy across WhatsApp, web,
              Instagram, and more in minutes.
            </p>
            <Link href="/signup"
              className="mt-5 inline-flex items-center gap-1.5 rounded-lg px-4 py-2.5 text-sm font-semibold text-white transition-all hover:-translate-y-px"
              style={{ background: "linear-gradient(135deg, #6366F1, #8B5CF6)" }}>
              Start free trial <ArrowRight size={13} />
            </Link>
          </div>

          {Object.entries(footerLinks).map(([section, links]) => (
            <div key={section}>
              <p className="mb-4 text-xs font-semibold uppercase tracking-widest text-[var(--text-muted)]">{section}</p>
              <ul className="space-y-2.5">
                {links.map(link => (
                  <li key={link.label}>
                    <Link href={link.href}
                      className="text-sm text-[var(--text-secondary)] hover:text-[var(--accent)] transition-colors">
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-12 flex flex-col items-center justify-between gap-3 border-t border-[var(--border)] pt-6 sm:flex-row">
          <p className="text-xs text-[var(--text-muted)]">&copy; {new Date().getFullYear()} Threadly, Inc. All rights reserved.</p>
          <div className="flex items-center gap-5 text-xs text-[var(--text-muted)]">
            {["Privacy","Terms","Cookies"].map(l => (
              <Link key={l} href={`/${l.toLowerCase()}`} className="hover:text-[var(--text-secondary)] transition-colors">{l}</Link>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}

/* ── Layout ───────────────────────────────────────────────────────────────── */

export default function MarketingLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-[var(--bg-canvas)] text-[var(--text-primary)]">
      <Navbar />
      <main className="pt-16">{children}</main>
      <Footer />
    </div>
  );
}
