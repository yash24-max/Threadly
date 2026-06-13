"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import React, { useState, useEffect } from "react";
import { Logo } from "@/components/ui/Logo";
import {
  Menu, X, ArrowRight, Twitter, Github, Linkedin, Youtube,
} from "lucide-react";

/* ─────────────────────────────────────────────
   NAV
   ───────────────────────────────────────────── */

const navLinks = [
  { label: "Features",     href: "/#features"     },
  { label: "Templates",    href: "/#templates"    },
  { label: "Pricing",      href: "/#pricing"      },
  { label: "Integrations", href: "/#integrations" },
  { label: "Docs",         href: "/docs"          },
  { label: "Contact",      href: "/contact"       },
];

function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 8);
    handler();
    window.addEventListener("scroll", handler, { passive: true });
    return () => window.removeEventListener("scroll", handler);
  }, []);

  useEffect(() => { setMobileOpen(false); }, [pathname]);

  return (
    <>
      <header
        className={`fixed inset-x-0 top-0 z-50 transition-all duration-300 ${
          scrolled
            ? "backdrop-blur-xl border-b border-white/10 bg-[#0B1020]/70 shadow-[0_8px_32px_rgba(0,0,0,0.45)]"
            : "bg-transparent"
        }`}
      >
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          {/* Logo */}
          <Logo size="sm" variant="white" />

          {/* Desktop nav */}
          <nav className="hidden items-center gap-1 md:flex">
            {navLinks.map(link => (
              <Link
                key={link.href}
                href={link.href}
                className="relative rounded-lg px-3.5 py-2 text-sm font-medium text-white/70 transition-colors hover:text-white group"
              >
                {link.label}
                <span className="pointer-events-none absolute inset-x-3 -bottom-0.5 h-px scale-x-0 bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] transition-transform duration-300 group-hover:scale-x-100" />
              </Link>
            ))}
          </nav>

          {/* CTA row */}
          <div className="hidden items-center gap-2 md:flex">
            <Link
              href="/login"
              className="rounded-lg px-4 py-2 text-sm font-medium text-white/80 transition-colors hover:text-white hover:bg-white/5"
            >
              Login
            </Link>
            <Link
              href="/signup"
              className="group relative flex items-center gap-1.5 overflow-hidden rounded-xl px-5 py-2.5 text-sm font-semibold text-white shadow-[0_8px_32px_rgba(124,58,237,0.45)] transition-all hover:-translate-y-px hover:shadow-[0_12px_40px_rgba(124,58,237,0.65)]"
              style={{ background: "linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%)" }}
            >
              <span className="relative z-10">Get Started</span>
              <ArrowRight size={14} className="relative z-10 transition-transform group-hover:translate-x-0.5" />
              <span className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/30 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
            </Link>
          </div>

          {/* Mobile hamburger */}
          <button
            className="rounded-lg p-2 text-white/80 transition-colors hover:bg-white/5 md:hidden"
            onClick={() => setMobileOpen(v => !v)}
            aria-label="Toggle menu"
          >
            {mobileOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </header>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 flex flex-col bg-[#0B1020]/95 backdrop-blur-xl pt-16 md:hidden">
          <div className="flex-1 overflow-y-auto px-4 py-6 space-y-1">
            {navLinks.map(item => (
              <Link
                key={item.href}
                href={item.href}
                className="flex items-center justify-between rounded-xl px-4 py-3 text-base font-medium text-white/90 hover:bg-white/5"
              >
                {item.label}
                <ArrowRight size={16} className="text-white/40" />
              </Link>
            ))}
            <div className="mt-4 space-y-3 border-t border-white/10 pt-4">
              <Link
                href="/login"
                className="block w-full rounded-xl border border-white/15 py-3 text-center text-base font-semibold text-white"
              >
                Login
              </Link>
              <Link
                href="/signup"
                className="block w-full rounded-xl py-3 text-center text-base font-semibold text-white"
                style={{ background: "linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%)" }}
              >
                Get Started Free
              </Link>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

/* ─────────────────────────────────────────────
   FOOTER
   ───────────────────────────────────────────── */

const footerLinks = {
  Product: [
    { label: "Features",     href: "/#features"     },
    { label: "Templates",    href: "/#templates"    },
    { label: "Integrations", href: "/#integrations" },
    { label: "Pricing",      href: "/#pricing"      },
    { label: "Changelog",    href: "/changelog"     },
  ],
  Company: [
    { label: "About",        href: "/about"         },
    { label: "Blog",          href: "/blog"         },
    { label: "Case Studies",  href: "/case-studies" },
    { label: "Contact",       href: "/contact"      },
  ],
  Resources: [
    { label: "Documentation", href: "/docs"  },
    { label: "Help Center",   href: "/help"  },
    { label: "API Reference", href: "/docs"  },
    { label: "Community",     href: "/help"  },
  ],
  Legal: [
    { label: "Privacy Policy",   href: "/privacy" },
    { label: "Terms of Service", href: "/terms"   },
    { label: "Cookies",          href: "/privacy" },
    { label: "Security",         href: "/about"   },
  ],
};

const socials = [
  { icon: Twitter,  href: "https://twitter.com",  label: "Twitter"  },
  { icon: Github,   href: "https://github.com",   label: "GitHub"   },
  { icon: Linkedin, href: "https://linkedin.com", label: "LinkedIn" },
  { icon: Youtube,  href: "https://youtube.com",  label: "YouTube"  },
];

function Footer() {
  const [email, setEmail] = useState("");
  const [subscribed, setSubscribed] = useState(false);

  return (
    <footer className="relative overflow-hidden border-t border-white/10 bg-[#0B1020] py-20">
      {/* Subtle gradient orb in footer */}
      <div
        aria-hidden
        className="pointer-events-none absolute left-1/2 -top-32 h-96 w-96 -translate-x-1/2 rounded-full opacity-20 blur-3xl"
        style={{ background: "radial-gradient(circle, #7C3AED 0%, transparent 70%)" }}
      />

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-x-8 gap-y-12 lg:grid-cols-12">
          {/* Brand + newsletter */}
          <div className="col-span-2 lg:col-span-4">
            <Logo size="sm" variant="white" />
            <p className="mt-4 max-w-xs text-sm leading-relaxed text-white/60">
              Build AI chatbots without writing code. Deploy across WhatsApp, web,
              Instagram, and more in minutes.
            </p>

            <form
              onSubmit={(e) => { e.preventDefault(); if (email) setSubscribed(true); }}
              className="mt-6 flex max-w-sm gap-2"
            >
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@company.com"
                required
                className="flex-1 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2.5 text-sm text-white placeholder:text-white/40 outline-none transition-colors focus:border-[#7C3AED]/60 focus:bg-white/[0.06]"
              />
              <button
                type="submit"
                className="rounded-xl px-4 py-2.5 text-sm font-semibold text-white transition-all hover:-translate-y-px"
                style={{ background: "linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%)" }}
              >
                {subscribed ? "Subscribed ✓" : "Subscribe"}
              </button>
            </form>

            <div className="mt-6 flex items-center gap-2">
              {socials.map(s => (
                <a
                  key={s.label}
                  href={s.href}
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label={s.label}
                  className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/10 bg-white/[0.03] text-white/70 transition-all hover:-translate-y-0.5 hover:border-[#7C3AED]/50 hover:text-white hover:shadow-[0_8px_24px_rgba(124,58,237,0.35)]"
                >
                  <s.icon size={15} />
                </a>
              ))}
            </div>
          </div>

          {Object.entries(footerLinks).map(([section, links]) => (
            <div key={section} className="lg:col-span-2">
              <p className="mb-4 text-xs font-semibold uppercase tracking-widest text-white/40">
                {section}
              </p>
              <ul className="space-y-2.5">
                {links.map(link => (
                  <li key={link.label}>
                    <Link
                      href={link.href}
                      className="text-sm text-white/70 transition-colors hover:text-white"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-16 flex flex-col items-center justify-between gap-3 border-t border-white/10 pt-6 sm:flex-row">
          <p className="text-xs text-white/40">
            &copy; {new Date().getFullYear()} Threadly, Inc. All rights reserved.
          </p>
          <p className="text-xs text-white/40">
            Crafted with care · Status: <span className="text-emerald-400">● All systems operational</span>
          </p>
        </div>
      </div>
    </footer>
  );
}

/* ─────────────────────────────────────────────
   LAYOUT
   ───────────────────────────────────────────── */

export default function MarketingLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="landing-theme min-h-screen bg-[#0B1020] text-white">
      <Navbar />
      <main className="pt-16">{children}</main>
      <Footer />
    </div>
  );
}
