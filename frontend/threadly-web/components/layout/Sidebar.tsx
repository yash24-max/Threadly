"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { signOut, useSession } from "next-auth/react";
import {
  LayoutDashboard,
  Bot,
  MessageSquare,
  Database,
  Settings,
  LogOut,
  ChevronDown,
} from "lucide-react"
import { ThemeToggle } from "@/components/theme-toggle"

const nav = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/bots", label: "Bots", icon: Bot },
  { href: "/conversations", label: "Conversations", icon: MessageSquare },
];

export function Sidebar() {
  const pathname = usePathname();
  const { data: session } = useSession();

  return (
    <aside
      style={{
        width: 220,
        minWidth: 220,
        height: "100vh",
        background: "var(--bg-panel)",
        borderRight: "1px solid var(--border)",
        display: "flex",
        flexDirection: "column",
        position: "sticky",
        top: 0,
        overflow: "hidden",
      }}
    >
      {/* Logo */}
      <div
        style={{
          padding: "18px 16px",
          borderBottom: "1px solid var(--border)",
          display: "flex",
          alignItems: "center",
          gap: 8,
        }}
      >
        <span style={{ fontSize: 18, fontWeight: 700, letterSpacing: "-0.5px" }}>
          <span style={{ color: "var(--accent)" }}>thread</span>ly
        </span>
      </div>

      {/* Org selector */}
      {session && (
        <div
          style={{
            padding: "12px 12px 8px",
            borderBottom: "1px solid var(--border)",
          }}
        >
          <button
            style={{
              width: "100%", display: "flex", alignItems: "center",
              justifyContent: "space-between", gap: 8,
              background: "var(--bg-surface)", border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)", padding: "8px 10px",
              cursor: "pointer", color: "var(--text-primary)",
            }}
          >
            <span style={{ fontSize: 13, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {session.user.orgName}
            </span>
            <ChevronDown size={14} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
          </button>
        </div>
      )}

      {/* Nav */}
      <nav style={{ flex: 1, padding: "12px 8px", overflow: "auto" }}>
        {nav.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || pathname.startsWith(href + "/");
          return (
            <Link
              key={href}
              href={href}
              style={{
                display: "flex", alignItems: "center", gap: 10,
                padding: "8px 10px", borderRadius: "var(--radius-md)",
                textDecoration: "none", marginBottom: 2,
                color: active ? "var(--text-primary)" : "var(--text-secondary)",
                background: active ? "var(--bg-surface)" : "transparent",
                fontSize: 14, fontWeight: active ? 500 : 400,
                transition: "all var(--duration-fast)",
              }}
            >
              <Icon size={16} strokeWidth={active ? 2 : 1.5} />
              {label}
            </Link>
          );
        })}
      </nav>

      {/* Bottom */}
      <div style={{ padding: "8px 8px 12px", borderTop: "1px solid var(--border)" }}>
        <Link
          href="/settings"
          style={{
            display: "flex", alignItems: "center", gap: 10,
            padding: "8px 10px", borderRadius: "var(--radius-md)",
            textDecoration: "none", marginBottom: 2,
            color: pathname === "/settings" ? "var(--text-primary)" : "var(--text-secondary)",
            background: pathname === "/settings" ? "var(--bg-surface)" : "transparent",
            fontSize: 14,
          }}
        >
          <Settings size={16} strokeWidth={1.5} />
          Settings
        </Link>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "4px 4px" }}>
          <button
            onClick={() => signOut({ callbackUrl: "/login" })}
            style={{
              display: "flex", alignItems: "center", gap: 10,
              padding: "8px 10px",
              borderRadius: "var(--radius-md)",
              color: "var(--text-secondary)", background: "transparent",
              border: "none", cursor: "pointer", fontSize: 14,
            }}
          >
            <LogOut size={16} strokeWidth={1.5} />
            Sign out
          </button>
          <ThemeToggle />
        </div>
        {session && (
          <div
            style={{
              display: "flex", alignItems: "center", gap: 8,
              padding: "8px 10px", marginTop: 4,
            }}
          >
            <div
              style={{
                width: 28, height: 28, borderRadius: "50%",
                background: "var(--accent)", display: "flex",
                alignItems: "center", justifyContent: "center",
                color: "var(--accent-fg)", fontSize: 12, fontWeight: 700, flexShrink: 0,
              }}
            >
              {session.user.name?.[0]?.toUpperCase() ?? "?"}
            </div>
            <div style={{ overflow: "hidden" }}>
              <p style={{ fontSize: 13, fontWeight: 500, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
                {session.user.name}
              </p>
              <p style={{ fontSize: 11, color: "var(--text-muted)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
                {session.user.email}
              </p>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}
