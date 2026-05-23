"use client"

import { useEffect, useRef, useState } from "react"
import { useRouter } from "next/navigation"
import { useQuery } from "@tanstack/react-query"
import { useSession } from "next-auth/react"
import { api } from "@/lib/api"
import type { Bot } from "@/lib/types"

type CommandItem = {
  id: string
  label: string
  description?: string
  action: () => void
  icon?: string
}

export function CommandPalette() {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")
  const [selected, setSelected] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null)
  const router = useRouter()
  const { data: session } = useSession()

  const { data: bots } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", session?.accessToken),
    enabled: !!session?.accessToken && open,
  })

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault()
        setOpen((o) => !o)
        setQuery("")
        setSelected(0)
      }
      if (e.key === "Escape") setOpen(false)
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [])

  useEffect(() => {
    if (open) setTimeout(() => inputRef.current?.focus(), 0)
  }, [open])

  const staticItems: CommandItem[] = [
    { id: "nav-dashboard", label: "Go to Dashboard", icon: "⚡", action: () => router.push("/dashboard") },
    { id: "nav-bots", label: "Go to Bots", icon: "🤖", action: () => router.push("/bots") },
    { id: "nav-conversations", label: "Go to Conversations", icon: "💬", action: () => router.push("/conversations") },
    { id: "nav-settings", label: "Go to Settings", icon: "⚙️", action: () => router.push("/settings") },
    { id: "create-bot", label: "Create New Bot", description: "Open the bots page to create", icon: "✨", action: () => router.push("/bots?create=1") },
  ]

  const botItems: CommandItem[] = (bots ?? []).map((b) => ({
    id: `bot-${b.id}`,
    label: `Edit: ${b.name}`,
    description: "Open flow builder",
    icon: "🔗",
    action: () => router.push(`/builder/${b.id}`),
  }))

  const allItems = [...staticItems, ...botItems]
  const filtered = query.trim()
    ? allItems.filter((i) =>
        i.label.toLowerCase().includes(query.toLowerCase()) ||
        i.description?.toLowerCase().includes(query.toLowerCase())
      )
    : allItems

  function handleKey(e: React.KeyboardEvent) {
    if (e.key === "ArrowDown") { e.preventDefault(); setSelected((s) => Math.min(s + 1, filtered.length - 1)) }
    if (e.key === "ArrowUp") { e.preventDefault(); setSelected((s) => Math.max(s - 1, 0)) }
    if (e.key === "Enter" && filtered[selected]) {
      filtered[selected].action()
      setOpen(false)
    }
  }

  if (!open) return null

  return (
    <div
      style={{
        position: "fixed", inset: 0, zIndex: 9999,
        background: "rgba(0,0,0,0.6)", backdropFilter: "blur(2px)",
        display: "flex", alignItems: "flex-start", justifyContent: "center",
        paddingTop: "15vh",
      }}
      onClick={() => setOpen(false)}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "min(580px, 90vw)",
          background: "var(--bg-panel)",
          border: "1px solid var(--border)",
          borderRadius: "var(--radius-xl)",
          boxShadow: "var(--shadow-3)",
          overflow: "hidden",
        }}
      >
        {/* Input */}
        <div style={{ padding: "12px 16px", borderBottom: "1px solid var(--border)", display: "flex", gap: 10, alignItems: "center" }}>
          <span style={{ color: "var(--text-muted)", fontSize: 16 }}>⌘</span>
          <input
            ref={inputRef}
            value={query}
            onChange={(e) => { setQuery(e.target.value); setSelected(0) }}
            onKeyDown={handleKey}
            placeholder="Type a command or search…"
            style={{
              flex: 1, background: "transparent", border: "none", outline: "none",
              fontSize: 15, color: "var(--text-primary)",
            }}
          />
          <kbd style={{ fontSize: 11, color: "var(--text-muted)", border: "1px solid var(--border)", borderRadius: 4, padding: "2px 5px" }}>ESC</kbd>
        </div>

        {/* Results */}
        <div style={{ maxHeight: 380, overflow: "auto", padding: "8px 0" }}>
          {filtered.length === 0 ? (
            <p style={{ padding: "24px 16px", textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>
              No results for &ldquo;{query}&rdquo;
            </p>
          ) : (
            filtered.map((item, i) => (
              <button
                key={item.id}
                onClick={() => { item.action(); setOpen(false) }}
                style={{
                  display: "flex", alignItems: "center", gap: 12,
                  width: "100%", padding: "10px 16px",
                  background: i === selected ? "var(--bg-surface)" : "transparent",
                  border: "none", cursor: "pointer", textAlign: "left",
                }}
                onMouseEnter={() => setSelected(i)}
              >
                <span style={{ fontSize: 16, width: 24, textAlign: "center" }}>{item.icon}</span>
                <div>
                  <p style={{ fontSize: 14, color: "var(--text-primary)", fontWeight: 500 }}>{item.label}</p>
                  {item.description && (
                    <p style={{ fontSize: 12, color: "var(--text-muted)" }}>{item.description}</p>
                  )}
                </div>
              </button>
            ))
          )}
        </div>

        <div style={{ borderTop: "1px solid var(--border)", padding: "8px 16px", display: "flex", gap: 16, color: "var(--text-muted)", fontSize: 11 }}>
          <span><kbd style={{ border: "1px solid var(--border)", borderRadius: 3, padding: "1px 4px" }}>↑↓</kbd> navigate</span>
          <span><kbd style={{ border: "1px solid var(--border)", borderRadius: 3, padding: "1px 4px" }}>↵</kbd> select</span>
          <span><kbd style={{ border: "1px solid var(--border)", borderRadius: 3, padding: "1px 4px" }}>⌘K</kbd> close</span>
        </div>
      </div>
    </div>
  )
}
