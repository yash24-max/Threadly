"use client";

import { useSession } from "next-auth/react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Building2, Users, Bot, GitBranch, Loader2, TrendingUp } from "lucide-react";

interface PlatformStats {
  totalOrgs:  number;
  totalUsers: number;
  totalBots:  number;
  totalFlows: number;
}

function StatCard({ label, value, icon: Icon, color }: {
  label: string; value: number | undefined; icon: React.ElementType; color: string;
}) {
  return (
    <div style={{
      background: "var(--bg-panel)", border: "1px solid var(--border)",
      borderRadius: "var(--radius-lg)", padding: "20px 24px",
      display: "flex", alignItems: "center", gap: 16,
    }}>
      <div style={{
        width: 44, height: 44, borderRadius: 12, flexShrink: 0,
        background: `${color}18`, display: "flex", alignItems: "center", justifyContent: "center",
      }}>
        <Icon size={20} style={{ color }} />
      </div>
      <div>
        <p style={{ fontSize: 12, color: "var(--text-muted)", marginBottom: 4, textTransform: "uppercase", letterSpacing: "0.06em" }}>
          {label}
        </p>
        <p style={{ fontSize: 28, fontWeight: 700, color: "var(--text-primary)" }}>
          {value !== undefined ? value.toLocaleString() : <Loader2 size={18} style={{ animation: "spin 1s linear infinite" }} />}
        </p>
      </div>
    </div>
  );
}

export default function AdminOverviewPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;

  const { data: stats } = useQuery<PlatformStats>({
    queryKey: ["admin-stats"],
    queryFn: () => api.get<PlatformStats>("/admin/api/stats", token),
    enabled: !!token,
    staleTime: 60_000,
  });

  return (
    <div style={{ padding: "28px 32px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 28 }}>
        <TrendingUp size={20} style={{ color: "var(--accent)" }} />
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>Platform Overview</h1>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: 16 }}>
        <StatCard label="Organizations" value={stats?.totalOrgs}  icon={Building2} color="#6366F1" />
        <StatCard label="Users"         value={stats?.totalUsers} icon={Users}     color="#8B5CF6" />
        <StatCard label="Bots"          value={stats?.totalBots}  icon={Bot}       color="#06B6D4" />
        <StatCard label="Flows"         value={stats?.totalFlows} icon={GitBranch} color="#10B981" />
      </div>
    </div>
  );
}
