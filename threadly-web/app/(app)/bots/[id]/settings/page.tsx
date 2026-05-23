"use client"

import { useState, useEffect } from "react"
import { useSession } from "next-auth/react"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useParams } from "next/navigation"
import { api } from "@/lib/api"
import type { Bot } from "@/lib/types"
import { Settings, Save, Loader2, Palette, MessageSquare, Monitor } from "lucide-react"
import { toast } from "sonner"

const ACCENT_PRESETS = [
  "#4F46E5", "#0EA5E9", "#10B981", "#F59E0B", "#EF4444",
  "#8B5CF6", "#EC4899", "#F97316", "#14B8A6", "#6366F1",
]

export default function BotSettingsPage() {
  const { id: botId } = useParams<{ id: string }>()
  const { data: session } = useSession()
  const token = session?.accessToken
  const qc = useQueryClient()

  const { data: bot, isLoading } = useQuery<Bot>({
    queryKey: ["bots", botId],
    queryFn: () => api.get(`/v1/bots/${botId}`, token),
    enabled: !!token,
  })

  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [accentColor, setAccentColor] = useState("#4F46E5")
  const [greeting, setGreeting] = useState("Hi! How can I help you today?")
  const [position, setPosition] = useState<"bottom-right" | "bottom-left">("bottom-right")
  const [launcherText, setLauncherText] = useState("Chat with us")

  useEffect(() => {
    if (bot) {
      setName(bot.name ?? "")
      setDescription(bot.description ?? "")
      try {
        const theme = typeof bot.theme === "string" ? JSON.parse(bot.theme) : bot.theme ?? {}
        setAccentColor(theme.color ?? "#4F46E5")
        setGreeting(theme.greeting ?? "Hi! How can I help you today?")
        setPosition(theme.position ?? "bottom-right")
        setLauncherText(theme.launcherText ?? "Chat with us")
      } catch { /* use defaults */ }
    }
  }, [bot])

  const save = useMutation({
    mutationFn: () =>
      api.patch(`/v1/bots/${botId}`, {
        name,
        description,
        theme: JSON.stringify({ color: accentColor, greeting, position, launcherText }),
      }, token),
    onSuccess: () => {
      toast.success("Bot settings saved!")
      qc.invalidateQueries({ queryKey: ["bots", botId] })
    },
    onError: () => toast.error("Failed to save settings"),
  })

  if (isLoading) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%" }}>
        <Loader2 size={24} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
      </div>
    )
  }

  return (
    <div style={{ padding: "28px 32px", maxWidth: 720 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 28 }}>
        <Settings size={20} style={{ color: "var(--accent)" }} />
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>Bot Settings</h1>
          <p style={{ fontSize: 13, color: "var(--text-muted)" }}>{bot?.name}</p>
        </div>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
        {/* Basic info */}
        <Section title="Basic Information" icon={<MessageSquare size={15} />}>
          <Field label="Bot name">
            <input value={name} onChange={(e) => setName(e.target.value)} style={inputStyle} />
          </Field>
          <Field label="Description">
            <textarea value={description} onChange={(e) => setDescription(e.target.value)}
              rows={3} style={{ ...inputStyle, resize: "vertical" }} />
          </Field>
          <Field label="Greeting message">
            <input value={greeting} onChange={(e) => setGreeting(e.target.value)} style={inputStyle} />
          </Field>
        </Section>

        {/* Theme */}
        <Section title="Widget Theme" icon={<Palette size={15} />}>
          <Field label="Accent color">
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {ACCENT_PRESETS.map((c) => (
                <button
                  key={c}
                  onClick={() => setAccentColor(c)}
                  style={{
                    width: 28, height: 28, borderRadius: "50%", background: c, border: "none", cursor: "pointer",
                    outline: c === accentColor ? `3px solid ${c}` : "none",
                    outlineOffset: 2,
                  }}
                />
              ))}
              <input
                type="color"
                value={accentColor}
                onChange={(e) => setAccentColor(e.target.value)}
                style={{ width: 28, height: 28, borderRadius: "50%", border: "1px solid var(--border)", cursor: "pointer", padding: 1 }}
                title="Custom color"
              />
            </div>
          </Field>

          <Field label="Widget position">
            <div style={{ display: "flex", gap: 10 }}>
              {(["bottom-right", "bottom-left"] as const).map((p) => (
                <button
                  key={p}
                  onClick={() => setPosition(p)}
                  style={{
                    padding: "8px 16px", borderRadius: "var(--radius-md)", fontSize: 13, cursor: "pointer",
                    border: `1px solid ${position === p ? "var(--accent)" : "var(--border)"}`,
                    background: position === p ? "rgba(79,70,229,.1)" : "var(--bg-surface)",
                    color: position === p ? "var(--accent)" : "var(--text-secondary)",
                  }}
                >
                  {p === "bottom-right" ? "↘ Bottom right" : "↙ Bottom left"}
                </button>
              ))}
            </div>
          </Field>

          <Field label="Launcher text">
            <input value={launcherText} onChange={(e) => setLauncherText(e.target.value)} style={inputStyle} />
          </Field>
        </Section>

        {/* Live preview */}
        <Section title="Widget Preview" icon={<Monitor size={15} />}>
          <div style={{
            height: 200, background: "var(--bg-surface)", borderRadius: "var(--radius-md)",
            position: "relative", overflow: "hidden",
          }}>
            <div style={{
              position: "absolute",
              bottom: 16,
              ...(position === "bottom-right" ? { right: 16 } : { left: 16 }),
              display: "flex",
              flexDirection: "column",
              alignItems: position === "bottom-right" ? "flex-end" : "flex-start",
              gap: 8,
            }}>
              {/* Sample chat bubble */}
              <div style={{
                background: "var(--bg-panel)",
                border: "1px solid var(--border)",
                borderRadius: 12,
                padding: "10px 14px",
                fontSize: 13,
                maxWidth: 200,
                boxShadow: "var(--shadow-2)",
              }}>
                {greeting}
              </div>
              {/* Launcher */}
              <button style={{
                width: 48, height: 48, borderRadius: "50%",
                background: accentColor, border: "none", cursor: "default",
                display: "flex", alignItems: "center", justifyContent: "center",
                color: "#fff", fontSize: 20, boxShadow: "var(--shadow-2)",
              }}>
                💬
              </button>
            </div>
          </div>
        </Section>

        <button
          onClick={() => save.mutate()}
          disabled={save.isPending}
          style={{
            alignSelf: "flex-start",
            display: "flex", alignItems: "center", gap: 7,
            padding: "10px 22px", borderRadius: "var(--radius-md)",
            background: "var(--accent)", color: "var(--accent-fg)",
            border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
          }}
        >
          {save.isPending ? <Loader2 size={14} style={{ animation: "spin 1s linear infinite" }} /> : <Save size={14} />}
          Save settings
        </button>
      </div>
    </div>
  )
}

const inputStyle: React.CSSProperties = {
  width: "100%", background: "var(--bg-surface)", border: "1px solid var(--border)",
  borderRadius: "var(--radius-md)", padding: "9px 12px",
  color: "var(--text-primary)", fontSize: 14, outline: "none",
}

function Section({ title, icon, children }: { title: string; icon: React.ReactNode; children: React.ReactNode }) {
  return (
    <div style={{
      background: "var(--bg-panel)", border: "1px solid var(--border)",
      borderRadius: "var(--radius-lg)", padding: "20px 24px",
    }}>
      <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 18 }}>
        <span style={{ color: "var(--accent)" }}>{icon}</span>
        <h2 style={{ fontSize: 15, fontWeight: 600 }}>{title}</h2>
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>{children}</div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label style={{ display: "block", fontSize: 13, fontWeight: 500, color: "var(--text-secondary)", marginBottom: 6 }}>
        {label}
      </label>
      {children}
    </div>
  )
}
