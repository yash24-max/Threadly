"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { signOut, useSession } from "next-auth/react";
import { useState } from "react";
import {
  LayoutDashboard, Bot, MessageSquare, Database, Settings,
  LogOut, ChevronDown, Puzzle, BarChart3, FileText,
  ChevronLeft, ChevronRight, Bell, ShieldCheck,
} from "lucide-react";
import { Logo } from "@/components/ui/Logo";
import { ThemeToggle } from "@/components/theme-toggle";

// ─── Nav items ────────────────────────────────────────────────────────────────

const nav = [
  { href: "/dashboard",     label: "Dashboard",     icon: LayoutDashboard },
  { href: "/bots",          label: "Bots",           icon: Bot             },
  { href: "/conversations", label: "Conversations",  icon: MessageSquare   },
  { href: "/templates",     label: "Templates",      icon: FileText        },
  { href: "/integrations",  label: "Integrations",   icon: Puzzle          },
];

const bottomNav = [
  { href: "/settings",          label: "Settings",       icon: Settings    },
  { href: "/settings/sessions", label: "Sessions",       icon: ShieldCheck },
];

// ─── Sidebar ──────────────────────────────────────────────────────────────────

export function Sidebar() {
  const pathname = usePathname();
  const { data: session } = useSession();
  const [collapsed, setCollapsed] = useState(false);
  const [orgDropOpen, setOrgDropOpen] = useState(false);

  const w = collapsed ? 64 : 240;

  function isActive(href: string) {
    return pathname === href || pathname.startsWith(href + "/");
  }

  return (
    <aside
      style={{
        width: w,
        minWidth: w,
        height: "100vh",
        background: "var(--bg-panel)",
        borderRight: "1px solid var(--border)",
        display: "flex",
        flexDirection: "column",
        position: "sticky",
        top: 0,
        overflow: "hidden",
        transition: "width 240ms cubic-bezier(0.16,1,0.3,1)",
        flexShrink: 0,
        zIndex: 20,
      }}
    >
      {/* Header: logo + collapse toggle */}
      <div
        style={{
          height: 56,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: collapsed ? "0 18px" : "0 12px 0 16px",
          borderBottom: "1px solid var(--border)",
          flexShrink: 0,
        }}
      >
        {!collapsed && <Logo size="xs" />}
        {collapsed && (
          /* Mini logo mark when collapsed */
          <svg width="24" height="24" viewBox="0 0 44 44" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="sb-grad" x1="0" y1="0" x2="44" y2="44" gradientUnits="userSpaceOnUse">
                <stop offset="0%"  stopColor="#6366F1" />
                <stop offset="100%" stopColor="#8B5CF6" />
              </linearGradient>
            </defs>
            <rect width="44" height="44" rx="12" fill="url(#sb-grad)" />
            <rect x="10" y="13" width="24" height="4.5" rx="2.25" fill="white" />
            <rect x="19.75" y="17.5" width="4.5" height="10" rx="2.25" fill="white" />
          </svg>
        )}
        <button
          onClick={() => setCollapsed((v) => !v)}
          style={{
            display: "flex", alignItems: "center", justifyContent: "center",
            width: 24, height: 24, borderRadius: 6,
            border: "1px solid var(--border)",
            background: "var(--bg-surface)",
            color: "var(--text-muted)",
            cursor: "pointer", flexShrink: 0,
            marginLeft: collapsed ? "auto" : 0,
          }}
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? <ChevronRight size={12} /> : <ChevronLeft size={12} />}
        </button>
      </div>

      {/* Org selector */}
      {session && !collapsed && (
        <div style={{ padding: "10px 10px 6px", borderBottom: "1px solid var(--border)" }}>
          <button
            onClick={() => setOrgDropOpen((v) => !v)}
            style={{
              width: "100%", display: "flex", alignItems: "center",
              justifyContent: "space-between", gap: 8,
              background: "var(--bg-surface)", border: "1px solid var(--border)",
              borderRadius: 10, padding: "8px 12px",
              cursor: "pointer", color: "var(--text-primary)",
              transition: "border-color 150ms ease",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
              <div
                style={{
                  width: 22, height: 22, borderRadius: 6, flexShrink: 0,
                  background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 10, fontWeight: 700, color: "#fff",
                }}
              >
                {session.user.orgName?.[0]?.toUpperCase() ?? "T"}
              </div>
              <span style={{ fontSize: 13, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                {session.user.orgName ?? "My workspace"}
              </span>
            </div>
            <ChevronDown
              size={13}
              style={{
                color: "var(--text-muted)", flexShrink: 0,
                transform: orgDropOpen ? "rotate(180deg)" : "none",
                transition: "transform 150ms ease",
              }}
            />
          </button>
        </div>
      )}

      {/* Collapsed org dot */}
      {session && collapsed && (
        <div style={{ display: "flex", justifyContent: "center", padding: "10px 0 6px", borderBottom: "1px solid var(--border)" }}>
          <div
            style={{
              width: 30, height: 30, borderRadius: 8,
              background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: 12, fontWeight: 700, color: "#fff",
            }}
            title={session.user.orgName ?? "Workspace"}
          >
            {session.user.orgName?.[0]?.toUpperCase() ?? "T"}
          </div>
        </div>
      )}

      {/* Main nav */}
      <nav style={{ flex: 1, padding: "10px 8px", overflowY: "auto", overflowX: "hidden" }}>
        {/* Label */}
        {!collapsed && (
          <p style={{ fontSize: 10, fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.08em", color: "var(--text-muted)", padding: "2px 10px 6px" }}>
            Menu
          </p>
        )}

        {nav.map(({ href, label, icon: Icon }) => {
          const active = isActive(href);
          return (
            <Link
              key={href}
              href={href}
              title={collapsed ? label : undefined}
              style={{
                display: "flex", alignItems: "center",
                gap: collapsed ? 0 : 10,
                padding: collapsed ? "10px 0" : "9px 10px",
                justifyContent: collapsed ? "center" : "flex-start",
                borderRadius: 10, textDecoration: "none",
                marginBottom: 2,
                color: active ? "var(--text-primary)" : "var(--text-secondary)",
                background: active
                  ? "linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.08))"
                  : "transparent",
                border: active ? "1px solid rgba(99,102,241,0.2)" : "1px solid transparent",
                fontSize: 13, fontWeight: active ? 600 : 400,
                transition: "all 150ms ease",
                position: "relative",
              }}
            >
              {/* Active indicator bar */}
              {active && !collapsed && (
                <span
                  style={{
                    position: "absolute", left: 0, top: "50%",
                    transform: "translateY(-50%)",
                    width: 3, height: "60%", borderRadius: 2,
                    background: "linear-gradient(180deg, #6366F1, #8B5CF6)",
                  }}
                />
              )}
              <Icon
                size={16}
                strokeWidth={active ? 2.5 : 1.8}
                style={{ color: active ? "#818CF8" : "var(--text-secondary)", flexShrink: 0 }}
              />
              {!collapsed && <span>{label}</span>}
            </Link>
          );
        })}
      </nav>

      {/* Bottom section */}
      <div style={{ padding: "8px 8px 4px", borderTop: "1px solid var(--border)" }}>
        {bottomNav.map(({ href, label, icon: Icon }) => {
          const active = isActive(href);
          return (
            <Link
              key={href}
              href={href}
              title={collapsed ? label : undefined}
              style={{
                display: "flex", alignItems: "center",
                gap: collapsed ? 0 : 10,
                padding: collapsed ? "10px 0" : "9px 10px",
                justifyContent: collapsed ? "center" : "flex-start",
                borderRadius: 10, textDecoration: "none", marginBottom: 2,
                color: active ? "var(--text-primary)" : "var(--text-secondary)",
                background: active ? "var(--bg-surface)" : "transparent",
                border: "1px solid transparent",
                fontSize: 13, fontWeight: active ? 600 : 400,
                transition: "all 150ms ease",
              }}
            >
              <Icon size={16} strokeWidth={1.8} style={{ flexShrink: 0 }} />
              {!collapsed && <span>{label}</span>}
            </Link>
          );
        })}

        {/* Sign out + theme toggle row */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: collapsed ? "center" : "space-between", padding: "4px 4px" }}>
          <button
            onClick={() => signOut({ callbackUrl: "/login" })}
            title="Sign out"
            style={{
              display: "flex", alignItems: "center", gap: collapsed ? 0 : 8,
              padding: collapsed ? "8px" : "8px 10px",
              borderRadius: 10,
              color: "var(--text-muted)", background: "transparent",
              border: "none", cursor: "pointer", fontSize: 13,
              transition: "color 150ms ease",
            }}
          >
            <LogOut size={15} strokeWidth={1.8} style={{ flexShrink: 0 }} />
            {!collapsed && <span>Sign out</span>}
          </button>
          {!collapsed && <ThemeToggle />}
        </div>

        {/* User avatar */}
        {session && !collapsed && (
          <div
            style={{
              display: "flex", alignItems: "center", gap: 10,
              padding: "10px 10px", marginTop: 4,
              borderRadius: 12, background: "var(--bg-surface)",
            }}
          >
            <div
              style={{
                width: 32, height: 32, borderRadius: "50%",
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                display: "flex", alignItems: "center", justifyContent: "center",
                color: "#fff", fontSize: 13, fontWeight: 700, flexShrink: 0,
              }}
            >
              {session.user.name?.[0]?.toUpperCase() ?? "?"}
            </div>
            <div style={{ overflow: "hidden", flex: 1 }}>
              <p style={{ fontSize: 13, fontWeight: 600, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", color: "var(--text-primary)" }}>
                {session.user.name}
              </p>
              <p style={{ fontSize: 11, color: "var(--text-muted)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
                {session.user.email}
              </p>
            </div>
            <Bell size={13} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
          </div>
        )}

        {/* Collapsed avatar */}
        {session && collapsed && (
          <div style={{ display: "flex", justifyContent: "center", padding: "8px 0 4px" }}>
            <div
              title={session.user.name ?? ""}
              style={{
                width: 30, height: 30, borderRadius: "50%",
                background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                display: "flex", alignItems: "center", justifyContent: "center",
                color: "#fff", fontSize: 12, fontWeight: 700, cursor: "default",
              }}
            >
              {session.user.name?.[0]?.toUpperCase() ?? "?"}
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}
