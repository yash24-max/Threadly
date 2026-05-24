"use client"

import { useState } from "react"
import { useSession } from "next-auth/react"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { api } from "@/lib/api"
import { Key, Plus, Trash2, Copy, Loader2, Eye, EyeOff } from "lucide-react"
import { toast } from "sonner"

interface ApiKey {
  id: string
  name: string
  prefix: string
  createdAt: string
  lastUsedAt?: string
}

export default function ApiKeysPage() {
  const { data: session } = useSession()
  const token = session?.accessToken
  const qc = useQueryClient()
  const [newKeyName, setNewKeyName] = useState("")
  const [revealedKey, setRevealedKey] = useState<string | null>(null)

  const { data: keys, isLoading } = useQuery<ApiKey[]>({
    queryKey: ["api-keys"],
    queryFn: () => api.get("/v1/api-keys", token),
    enabled: !!token,
  })

  const create = useMutation({
    mutationFn: (name: string) => api.post<{ key: string; id: string }>("/v1/api-keys", { name }, token),
    onSuccess: (data) => {
      setRevealedKey(data.key)
      setNewKeyName("")
      qc.invalidateQueries({ queryKey: ["api-keys"] })
      toast.success("API key created — copy it now, it won't be shown again")
    },
    onError: () => toast.error("Failed to create API key"),
  })

  const revoke = useMutation({
    mutationFn: (id: string) => api.delete(`/v1/api-keys/${id}`, token),
    onSuccess: () => {
      toast.success("API key revoked")
      qc.invalidateQueries({ queryKey: ["api-keys"] })
    },
    onError: () => toast.error("Failed to revoke key"),
  })

  return (
    <div style={{ padding: "28px 32px", maxWidth: 720 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 28 }}>
        <Key size={20} style={{ color: "var(--accent)" }} />
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>API Keys</h1>
      </div>

      <p style={{ fontSize: 14, color: "var(--text-secondary)", marginBottom: 24, lineHeight: 1.6 }}>
        API keys let you authenticate requests to the Threadly API from your own applications.
        Keys are shown only once at creation — store them securely.
      </p>

      {/* Revealed key banner */}
      {revealedKey && (
        <div style={{
          background: "rgba(34,197,94,.08)",
          border: "1px solid var(--success)",
          borderRadius: "var(--radius-md)",
          padding: "14px 16px",
          marginBottom: 20,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 12,
        }}>
          <code style={{ fontSize: 13, wordBreak: "break-all", color: "var(--text-primary)", flex: 1 }}>
            {revealedKey}
          </code>
          <button
            onClick={() => { navigator.clipboard.writeText(revealedKey); toast.success("Copied!") }}
            style={{ background: "none", border: "none", cursor: "pointer", color: "var(--success)", flexShrink: 0 }}
          >
            <Copy size={14} />
          </button>
          <button
            onClick={() => setRevealedKey(null)}
            style={{ background: "none", border: "none", cursor: "pointer", color: "var(--text-muted)", fontSize: 12, flexShrink: 0 }}
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Create form */}
      <div style={{
        background: "var(--bg-panel)",
        border: "1px solid var(--border)",
        borderRadius: "var(--radius-lg)",
        padding: "20px 24px",
        marginBottom: 24,
      }}>
        <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 14 }}>Create new key</h2>
        <div style={{ display: "flex", gap: 10 }}>
          <input
            placeholder="Key name (e.g. Production webhook)"
            value={newKeyName}
            onChange={(e) => setNewKeyName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && newKeyName.trim() && create.mutate(newKeyName.trim())}
            style={{
              flex: 1, background: "var(--bg-surface)", border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)", padding: "9px 12px",
              color: "var(--text-primary)", fontSize: 14, outline: "none",
            }}
          />
          <button
            onClick={() => create.mutate(newKeyName.trim())}
            disabled={!newKeyName.trim() || create.isPending}
            style={{
              display: "flex", alignItems: "center", gap: 6,
              padding: "9px 18px", borderRadius: "var(--radius-md)",
              background: "var(--accent)", color: "var(--accent-fg)",
              border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
            }}
          >
            {create.isPending ? <Loader2 size={14} style={{ animation: "spin 1s linear infinite" }} /> : <Plus size={14} />}
            Create
          </button>
        </div>
      </div>

      {/* Keys list */}
      <div style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: "var(--radius-lg)", overflow: "hidden",
      }}>
        {isLoading ? (
          <div style={{ padding: 40, textAlign: "center" }}>
            <Loader2 size={20} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
          </div>
        ) : (keys ?? []).length === 0 ? (
          <p style={{ padding: 40, textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>
            No API keys yet.
          </p>
        ) : (
          (keys ?? []).map((k, i) => (
            <div key={k.id} style={{
              display: "flex", alignItems: "center", gap: 12, padding: "14px 16px",
              borderBottom: i < (keys?.length ?? 1) - 1 ? "1px solid var(--border)" : "none",
            }}>
              <Key size={14} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: 14, fontWeight: 500 }}>{k.name}</p>
                <p style={{ fontSize: 12, color: "var(--text-muted)", fontFamily: "monospace" }}>
                  {k.prefix}••••••••
                </p>
              </div>
              <p style={{ fontSize: 12, color: "var(--text-muted)" }}>
                {k.lastUsedAt ? `Used ${new Date(k.lastUsedAt).toLocaleDateString()}` : "Never used"}
              </p>
              <button
                onClick={() => revoke.mutate(k.id)}
                style={{ background: "none", border: "none", cursor: "pointer", color: "var(--danger)", padding: 4 }}
                title="Revoke key"
              >
                <Trash2 size={14} />
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
