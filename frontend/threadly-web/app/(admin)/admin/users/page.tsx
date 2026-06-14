"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "@/lib/api";
import { Users, Loader2, Search, ChevronLeft, ChevronRight, ToggleLeft, ToggleRight } from "lucide-react";

interface AdminUser {
  id:        string;
  email:     string;
  name:      string;
  orgId:     string;
  orgName:   string;
  role:      string;
  enabled:   boolean;
  createdAt: string;
}

interface PageResult {
  content:       AdminUser[];
  totalElements: number;
  totalPages:    number;
  number:        number;
}


export default function AdminUsersPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const [page, setPage]     = useState(0);
  const [search, setSearch] = useState("");

  const { data, isLoading } = useQuery<PageResult>({
    queryKey: ["admin-users", page],
    queryFn:  () => api.get<PageResult>(`/admin/api/users?page=${page}&size=25`, token),
    enabled: !!token,
    staleTime: 30_000,
  });

  const toggleStatus = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      api.patch(`/admin/api/users/${id}/status`, { enabled }, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-users"] }),
  });

  const users = (data?.content ?? []).filter(u =>
    !search ||
    u.name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase()) ||
    u.orgName?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div style={{ padding: "28px 32px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
        <Users size={20} style={{ color: "var(--accent)" }} />
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>Users</h1>
        {data && (
          <span style={{ fontSize: 13, color: "var(--text-muted)", marginLeft: 4 }}>
            ({data.totalElements.toLocaleString()} total)
          </span>
        )}
      </div>

      {/* Search */}
      <div style={{ position: "relative", maxWidth: 380, marginBottom: 20 }}>
        <Search size={14} style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "var(--text-muted)" }} />
        <input
          value={search} onChange={e => setSearch(e.target.value)}
          placeholder="Search by name, email, or org…"
          style={{
            width: "100%", boxSizing: "border-box",
            paddingLeft: 34, paddingRight: 12, paddingTop: 8, paddingBottom: 8,
            background: "var(--bg-surface)", border: "1px solid var(--border)",
            borderRadius: "var(--radius-md)", color: "var(--text-primary)", fontSize: 13, outline: "none",
          }}
        />
      </div>

      {/* Table */}
      <div style={{ background: "var(--bg-panel)", border: "1px solid var(--border)", borderRadius: "var(--radius-lg)", overflow: "hidden" }}>
        {isLoading ? (
          <div style={{ padding: 60, display: "flex", justifyContent: "center" }}>
            <Loader2 size={20} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
          </div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border)" }}>
                {["User", "Email", "Org", "Role", "Status", ""].map(h => (
                  <th key={h} style={{
                    padding: "12px 16px", textAlign: "left",
                    fontSize: 11, color: "var(--text-muted)", fontWeight: 600, textTransform: "uppercase",
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ padding: "40px 16px", textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>
                    {search ? "No users match your search." : "No users found."}
                  </td>
                </tr>
              ) : users.map(user => (
                <tr key={user.id} style={{ borderBottom: "1px solid var(--border)", opacity: user.enabled ? 1 : 0.6 }}>
                  <td style={{ padding: "14px 16px" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <div style={{
                        width: 30, height: 30, borderRadius: "50%", flexShrink: 0,
                        background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                        display: "flex", alignItems: "center", justifyContent: "center",
                        fontSize: 12, fontWeight: 700, color: "#fff",
                      }}>
                        {user.name?.[0]?.toUpperCase() ?? "?"}
                      </div>
                      <span style={{ fontSize: 14, fontWeight: 500 }}>{user.name}</span>
                    </div>
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 13, color: "var(--text-secondary)" }}>{user.email}</td>
                  <td style={{ padding: "14px 16px", fontSize: 13, color: "var(--text-secondary)" }}>{user.orgName}</td>
                  <td style={{ padding: "14px 16px" }}>
                    <span style={{
                      fontSize: 11, fontWeight: 600, padding: "2px 8px",
                      borderRadius: "var(--radius-full)", textTransform: "capitalize",
                      background: user.role === "admin" ? "rgba(99,102,241,0.12)" : "var(--bg-surface)",
                      color: user.role === "admin" ? "var(--accent)" : "var(--text-secondary)",
                    }}>
                      {user.role}
                    </span>
                  </td>
                  <td style={{ padding: "14px 16px" }}>
                    <span style={{
                      fontSize: 11, fontWeight: 600, padding: "2px 8px",
                      borderRadius: "var(--radius-full)",
                      background: user.enabled ? "rgba(16,185,129,0.12)" : "rgba(239,68,68,0.1)",
                      color: user.enabled ? "#10B981" : "#F87171",
                    }}>
                      {user.enabled ? "Active" : "Disabled"}
                    </span>
                  </td>
                  <td style={{ padding: "14px 16px", textAlign: "right" }}>
                    <button
                      onClick={() => toggleStatus.mutate({ id: user.id, enabled: !user.enabled })}
                      disabled={toggleStatus.isPending}
                      title={user.enabled ? "Disable user" : "Enable user"}
                      style={{
                        background: "none", border: "none", cursor: "pointer",
                        color: user.enabled ? "#F87171" : "#10B981",
                        display: "flex", alignItems: "center", gap: 4, fontSize: 12,
                      }}
                    >
                      {user.enabled
                        ? <><ToggleRight size={16} /> Disable</>
                        : <><ToggleLeft  size={16} /> Enable</>}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 16, justifyContent: "flex-end" }}>
          <span style={{ fontSize: 13, color: "var(--text-muted)" }}>
            Page {data.number + 1} of {data.totalPages}
          </span>
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={data.number === 0}
            style={{
              padding: "6px 10px", borderRadius: "var(--radius-md)",
              background: "var(--bg-surface)", border: "1px solid var(--border)",
              color: "var(--text-secondary)", cursor: "pointer",
            }}
          >
            <ChevronLeft size={14} />
          </button>
          <button
            onClick={() => setPage(p => Math.min(data.totalPages - 1, p + 1))}
            disabled={data.number >= data.totalPages - 1}
            style={{
              padding: "6px 10px", borderRadius: "var(--radius-md)",
              background: "var(--bg-surface)", border: "1px solid var(--border)",
              color: "var(--text-secondary)", cursor: "pointer",
            }}
          >
            <ChevronRight size={14} />
          </button>
        </div>
      )}
    </div>
  );
}
