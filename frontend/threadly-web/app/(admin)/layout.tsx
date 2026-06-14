import { auth } from "@/auth";
import { redirect } from "next/navigation";
import Link from "next/link";
import { Logo } from "@/components/ui/Logo";
import { LayoutDashboard, Building2, Users, BarChart3, ShieldCheck } from "lucide-react";

const adminNav = [
  { href: "/admin",        label: "Overview",      icon: LayoutDashboard },
  { href: "/admin/orgs",   label: "Organizations", icon: Building2       },
  { href: "/admin/users",  label: "Users",         icon: Users           },
];

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const session = await auth();

  // Guard: only super_admin may access
  if (!session || session.user.role !== "super_admin") {
    redirect("/dashboard");
  }

  return (
    <div style={{ display: "flex", height: "100vh", overflow: "hidden", background: "var(--bg-canvas)" }}>
      {/* Sidebar */}
      <aside style={{
        width: 220, minWidth: 220, height: "100vh",
        background: "var(--bg-panel)", borderRight: "1px solid var(--border)",
        display: "flex", flexDirection: "column",
      }}>
        <div style={{ height: 56, display: "flex", alignItems: "center", padding: "0 16px", borderBottom: "1px solid var(--border)", gap: 8 }}>
          <ShieldCheck size={16} style={{ color: "var(--accent)" }} />
          <span style={{ fontSize: 13, fontWeight: 700, color: "var(--text-primary)" }}>Admin Console</span>
        </div>

        <nav style={{ flex: 1, padding: "10px 8px", display: "flex", flexDirection: "column", gap: 2 }}>
          {adminNav.map(({ href, label, icon: Icon }) => (
            <Link
              key={href}
              href={href}
              style={{
                display: "flex", alignItems: "center", gap: 10,
                padding: "9px 10px", borderRadius: 10,
                textDecoration: "none", fontSize: 13,
                color: "var(--text-secondary)",
              }}
            >
              <Icon size={16} strokeWidth={1.8} />
              {label}
            </Link>
          ))}
        </nav>

        <div style={{ padding: "12px 16px", borderTop: "1px solid var(--border)" }}>
          <Link href="/dashboard" style={{ fontSize: 12, color: "var(--text-muted)", textDecoration: "none" }}>
            ← Back to app
          </Link>
        </div>
      </aside>

      {/* Main */}
      <main style={{ flex: 1, overflow: "auto", display: "flex", flexDirection: "column", minWidth: 0 }}>
        {children}
      </main>
    </div>
  );
}
