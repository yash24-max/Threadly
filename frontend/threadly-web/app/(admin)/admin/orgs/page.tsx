"use client";

import { useSession } from "next-auth/react";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Building2, Loader2, Search, ChevronLeft, ChevronRight } from "lucide-react";

interface Org {
  id:        string;
  name:      string;
  slug:      string;
  ownerId:   string;
  createdAt: string;
  userCount?: number;
}

interface PageResult {
  content:    Org[];
  totalElements: number;
  totalPages:    number;
  number:        number; // current page (0-indexed)
}

const ADMIN_URL = process.env.NEXT_PUBLIC_ADMIN_API_URL ?? "http://localhost:3010";

export default function AdminOrgsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const [page, setPage]     = useState(0);
  const [search, setSearch] = useState("");

  const { data, isLoading } = useQuery<PageResult>({
    queryKey: ["admin-orgs", page],
    queryFn:  () => fetch(`${ADMIN_URL}/admin/orgs?page=${page}&size=20`, {
      headers: { Authorization: `Bearer ${token}` },
    }).then(r => r.json()),
    enabled: !!token,
    staleTime: 30_000,
  });

  const orgs = (data?.content ?? []).filter(o =>
    !search || o.name.toLowerCase().includes(search.toLowerCase()) || o.slug.includes(search.toLowerCase())
  );

  return (
    <div style={{ padding: "28px 32px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
        <Building2 size={20} style={{ color: "var(--accent)" }} />
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>Organizations</h1>
        {data && (
          <span style={{ fontSize: 13, color: "var(--text-muted)", marginLeft: 4 }}>
            ({data.totalElements.toLocaleString()} total)
          </span>
        )}
      </div>

      {/* Search */}
      <div style={{ position: "relative", maxWidth: 340, marginBottom: 20 }}>
        <Search size={14} style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: "var(--text-muted)" }} />
        <input
          value={search} onChange={e => setSearch(e.target.value)}
          placeholder="Search by name or slug…"
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
                {["Name", "Slug", "Owner ID", "Created"].map(h => (
                  <th key={h} style={{
                    padding: "12px 16px", textAlign: "left",
                    fontSize: 11, color: "var(--text-muted)", fontWeight: 600, textTransform: "uppercase",
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {orgs.length === 0 ? (
                <tr>
                  <td colSpan={4} style={{ padding: "40px 16px", textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>
                    {search ? "No organizations match your search." : "No organizations found."}
                  </td>
                </tr>
              ) : orgs.map(org => (
                <tr key={org.id} style={{ borderBottom: "1px solid var(--border)" }}>
                  <td style={{ padding: "14px 16px", fontSize: 14, fontWeight: 500 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <div style={{
                        width: 28, height: 28, borderRadius: 8, flexShrink: 0,
                        background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
                        display: "flex", alignItems: "center", justifyContent: "center",
                        fontSize: 11, fontWeight: 700, color: "#fff",
                      }}>
                        {org.name[0]?.toUpperCase()}
                      </div>
                      {org.name}
                    </div>
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 13, color: "var(--text-secondary)", fontFamily: "monospace" }}>
                    {org.slug}
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 12, color: "var(--text-muted)", fontFamily: "monospace" }}>
                    {org.ownerId?.slice(0, 8)}…
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 13, color: "var(--text-secondary)" }}>
                    {new Date(org.createdAt).toLocaleDateString()}
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
