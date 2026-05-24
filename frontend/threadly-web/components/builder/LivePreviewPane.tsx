"use client"

import { useEffect, useRef, useState } from "react"
import { useSession } from "next-auth/react"
import { api } from "@/lib/api"
import { X, Send, Bot } from "lucide-react"

interface Message {
  role: "user" | "ai" | "system"
  content: string
  streaming?: boolean
}

interface Props {
  botId: string
  onClose: () => void
}

/**
 * Live preview pane for the flow builder.
 * Uses the same HTTP fallback endpoint as the widget (POST /v1/widget/message),
 * with polling for responses via the conversations API.
 */
export function LivePreviewPane({ botId, onClose }: Props) {
  const { data: session } = useSession()
  const [messages, setMessages] = useState<Message[]>([
    { role: "system", content: "Preview mode — testing your published flow" },
  ])
  const [input, setInput] = useState("")
  const [visitorId] = useState(() => `preview_${crypto.randomUUID()}`)
  const [loading, setLoading] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" })
  }, [messages])

  async function sendMessage() {
    const text = input.trim()
    if (!text || loading) return
    setInput("")
    setMessages((m) => [...m, { role: "user", content: text }])
    setLoading(true)

    try {
      // Send via public widget HTTP endpoint (no auth needed)
      await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/widget/message`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ botId, visitorId, text }),
      })

      // Poll for AI reply via conversations API (up to 8s)
      let reply = ""
      for (let i = 0; i < 16; i++) {
        await new Promise((r) => setTimeout(r, 500))
        try {
          const conversations = await api.get<any>(
            `/v1/conversations?size=5`, session?.accessToken
          )
          const conv = conversations?.content?.find(
            (c: any) => c.visitorId === visitorId
          )
          if (conv?.id) {
            const msgs = await api.get<any[]>(
              `/v1/conversations/${conv.id}/messages`, session?.accessToken
            )
            const aiMsgs = (msgs ?? []).filter((m: any) => m.role === "ai")
            if (aiMsgs.length > 0) {
              reply = aiMsgs[aiMsgs.length - 1].content
              break
            }
          }
        } catch { /* polling — ignore transient errors */ }
      }

      setMessages((m) => [...m, {
        role: "ai",
        content: reply || "*(no response — check that the flow is published and the AI node is configured)*",
      }])
    } catch (e) {
      setMessages((m) => [...m, { role: "system", content: "Error sending message" }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        width: 320,
        minWidth: 320,
        display: "flex",
        flexDirection: "column",
        background: "var(--bg-panel)",
        borderLeft: "1px solid var(--border)",
        height: "100%",
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: "12px 16px",
          borderBottom: "1px solid var(--border)",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <Bot size={16} style={{ color: "var(--accent)" }} />
          <span style={{ fontSize: 14, fontWeight: 600 }}>Live Preview</span>
        </div>
        <button
          onClick={onClose}
          style={{
            background: "none",
            border: "none",
            cursor: "pointer",
            color: "var(--text-muted)",
            padding: 4,
          }}
        >
          <X size={16} />
        </button>
      </div>

      {/* Messages */}
      <div
        ref={scrollRef}
        style={{
          flex: 1,
          overflow: "auto",
          padding: "12px 16px",
          display: "flex",
          flexDirection: "column",
          gap: 8,
        }}
      >
        {messages.map((msg, i) => (
          <div key={i}>
            {msg.role === "system" ? (
              <p
                style={{
                  fontSize: 11,
                  color: "var(--text-muted)",
                  textAlign: "center",
                  padding: "4px 8px",
                  background: "var(--bg-surface)",
                  borderRadius: "var(--radius-full)",
                }}
              >
                {msg.content}
              </p>
            ) : (
              <div
                style={{
                  display: "flex",
                  justifyContent: msg.role === "user" ? "flex-end" : "flex-start",
                }}
              >
                <div
                  style={{
                    maxWidth: "80%",
                    padding: "8px 12px",
                    borderRadius: msg.role === "user" ? "12px 12px 2px 12px" : "12px 12px 12px 2px",
                    background: msg.role === "user" ? "var(--accent)" : "var(--bg-surface)",
                    color: msg.role === "user" ? "var(--accent-fg)" : "var(--text-primary)",
                    fontSize: 13,
                    lineHeight: 1.5,
                  }}
                >
                  {msg.content}
                </div>
              </div>
            )}
          </div>
        ))}
        {loading && (
          <div style={{ display: "flex", gap: 4, padding: "8px 4px" }}>
            {[0, 1, 2].map((i) => (
              <span
                key={i}
                style={{
                  width: 6,
                  height: 6,
                  borderRadius: "50%",
                  background: "var(--text-muted)",
                  animation: `bounce 1s ${i * 0.15}s infinite`,
                }}
              />
            ))}
          </div>
        )}
      </div>

      {/* Input */}
      <div
        style={{
          padding: "12px 16px",
          borderTop: "1px solid var(--border)",
          display: "flex",
          gap: 8,
        }}
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && sendMessage()}
          placeholder="Type a message…"
          disabled={loading}
          style={{
            flex: 1,
            background: "var(--bg-surface)",
            border: "1px solid var(--border)",
            borderRadius: "var(--radius-md)",
            padding: "8px 12px",
            fontSize: 13,
            color: "var(--text-primary)",
            outline: "none",
          }}
        />
        <button
          onClick={sendMessage}
          disabled={loading || !input.trim()}
          style={{
            background: "var(--accent)",
            border: "none",
            borderRadius: "var(--radius-md)",
            padding: "8px 12px",
            cursor: "pointer",
            color: "var(--accent-fg)",
            display: "flex",
            alignItems: "center",
          }}
        >
          <Send size={14} />
        </button>
      </div>
    </div>
  )
}
