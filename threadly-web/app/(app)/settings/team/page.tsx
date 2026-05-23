"use client"

import { useState } from "react"
import { useSession } from "next-auth/react"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/lib/api"
import { UserPlus, Trash2, Loader2, Users } from "lucide-react"
import { toast } from "sonner"

interface Member {
  id: string
  name: string
  email: string
  role: string
  createdAt: string
}

export default function TeamPage() {
  const { data: session } = useSession()
  const token = session?.accessToken
  const qc = useQueryClient()

  const [inviteEmail, setInviteEmail] = useState("")
  const [inviteRole, setInviteRole] = useState("agent")

  const { data: members, isLoading } = useQuery<Member[]>({
    queryKey: ["team-members"],
    queryFn: () => api.get("/v1/team/members", token),
    enabled: !!token,
  })

  const invite = useMutation({
    mutationFn: (body: { email: string; role: string }) =>
      api.post("/v1/team/invite", body, token),
    onSuccess: () => {
      toast.success("Invitation sent!")
      setInviteEmail("")
      qc.invalidateQueries({ queryKey: ["team-members"] })
    },
    onError: () => toast.error("Failed to send invite"),
  })

  const remove = useMutation({
    mutationFn: (id: string) => api.delete(`/v1/team/members/${id}`, token),
    onSuccess: () => {
      toast.success("Member removed")
      qc.invalidateQueries({ queryKey: ["team-members"] })
    },
    onError: () => toast.error("Failed to remove member"),
  })

  return (
    <div style={{ padding: "28px 32px", maxWidth: 720 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 28 }}>
        <Users size={20} style={{ color: "var(--accent)" }} />
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>Team Members</h1>
      </div>

      {/* Invite form */}
      <div style={{
        background: "var(--bg-panel)",
        border: "1px solid var(--border)",
        borderRadius: "var(--radius-lg)",
        padding: "20px 24px",
        marginBottom: 24,
      }}>
        <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16 }}>Invite a member</h2>
        <div style={{ display: "flex", gap: 10 }}>
          <input
            type="email"
            placeholder="colleague@company.com"
            value={inviteEmail}
            onChange={(e) => setInviteEmail(e.target.value)}
            style={{
              flex: 1,
              background: "var(--bg-surface)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)",
              padding: "9px 12px",
              color: "var(--text-primary)",
              fontSize: 14,
              outline: "none",
            }}
          />
          <select
            value={inviteRole}
            onChange={(e) => setInviteRole(e.target.value)}
            style={{
              background: "var(--bg-surface)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)",
              padding: "9px 12px",
              color: "var(--text-primary)",
              fontSize: 14,
              outline: "none",
              cursor: "pointer",
            }}
          >
            <option value="agent">Agent</option>
            <option value="admin">Admin</option>
          </select>
          <button
            onClick={() => invite.mutate({ email: inviteEmail, role: inviteRole })}
            disabled={!inviteEmail.trim() || invite.isPending}
            style={{
              display: "flex", alignItems: "center", gap: 6,
              padding: "9px 18px", borderRadius: "var(--radius-md)",
              background: "var(--accent)", color: "var(--accent-fg)",
              border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
            }}
          >
            {invite.isPending ? <Loader2 size={14} style={{ animation: "spin 1s linear infinite" }} /> : <UserPlus size={14} />}
            Invite
          </button>
        </div>
      </div>

      {/* Members list */}
      <div style={{
        background: "var(--bg-panel)",
        border: "1px solid var(--border)",
        borderRadius: "var(--radius-lg)",
        overflow: "hidden",
      }}>
        {isLoading ? (
          <div style={{ padding: 40, textAlign: "center" }}>
            <Loader2 size={20} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
          </div>
        ) : (members ?? []).length === 0 ? (
          <p style={{ padding: 40, textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>
            No members yet. Invite your first teammate above.
          </p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border)" }}>
                {["Name", "Email", "Role", ""].map((h) => (
                  <th key={h} style={{
                    padding: "12px 16px", textAlign: "left",
                    fontSize: 12, color: "var(--text-muted)", fontWeight: 600, textTransform: "uppercase",
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {(members ?? []).map((m) => (
                <tr key={m.id} style={{ borderBottom: "1px solid var(--border)" }}>
                  <td style={{ padding: "14px 16px", fontSize: 14, fontWeight: 500 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                      <div style={{
                        width: 30, height: 30, borderRadius: "50%",
                        background: "var(--accent)", color: "var(--accent-fg)",
                        display: "flex", alignItems: "center", justifyContent: "center",
                        fontSize: 12, fontWeight: 700,
                      }}>
                        {m.name?.[0]?.toUpperCase() ?? "?"}
                      </div>
                      {m.name}
                      {m.id === (session?.user as any)?.id && (
                        <span style={{ fontSize: 11, color: "var(--accent)", background: "rgba(79,70,229,.12)", padding: "2px 7px", borderRadius: "var(--radius-full)" }}>You</span>
                      )}
                    </div>
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 14, color: "var(--text-secondary)" }}>{m.email}</td>
                  <td style={{ padding: "14px 16px" }}>
                    <span style={{
                      fontSize: 11, fontWeight: 600, textTransform: "capitalize",
                      padding: "3px 9px", borderRadius: "var(--radius-full)",
                      background: m.role === "admin" ? "rgba(79,70,229,.12)" : "var(--bg-surface)",
                      color: m.role === "admin" ? "var(--accent)" : "var(--text-secondary)",
                    }}>
                      {m.role}
                    </span>
                  </td>
                  <td style={{ padding: "14px 16px", textAlign: "right" }}>
                    {m.id !== (session?.user as any)?.id && (
                      <button
                        onClick={() => remove.mutate(m.id)}
                        style={{
                          background: "none", border: "none", cursor: "pointer",
                          color: "var(--danger)", padding: 4,
                        }}
                        title="Remove member"
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
