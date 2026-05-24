/** @jsxImportSource preact */
import { useState, useEffect, useRef, useCallback } from "preact/hooks"
import type { ChatMessage, MessageContent, ServerEvent, WidgetConfig } from "../types"
import { WsClient } from "../ws-client"
import { loadHistory, saveHistory } from "../storage"

interface Props {
  config: WidgetConfig
  onClose: () => void
  onNewMessage?: () => void
}

let msgIdCounter = 0
const nextId = () => `m_${++msgIdCounter}_${Date.now()}`

function formatTime(ts: number): string {
  const d = new Date(ts)
  const h = d.getHours().toString().padStart(2, "0")
  const m = d.getMinutes().toString().padStart(2, "0")
  return `${h}:${m}`
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1048576).toFixed(1)} MB`
}

function fileIcon(mimeType: string): string {
  if (mimeType.startsWith("image/")) return "🖼️"
  if (mimeType === "application/pdf") return "📄"
  if (mimeType.startsWith("text/")) return "📝"
  return "📎"
}

// ---------------------------------------------------------------------------
// Rich content renderers
// ---------------------------------------------------------------------------

interface RichProps {
  content: MessageContent
  onButtonClick: (value: string) => void
  onQuickReply: (msgId: string, value: string) => void
  msgId: string
}

function RichContent({ content, onButtonClick, onQuickReply, msgId }: RichProps) {
  if (content.type === "text") {
    return <span>{content.text}</span>
  }

  if (content.type === "buttons") {
    return (
      <div>
        <span>{content.text}</span>
        <div class="tly-buttons">
          {content.buttons.map((btn) => (
            <button
              key={btn.value}
              class={`tly-btn${btn.style === "primary" ? " primary" : ""}`}
              onClick={() => onButtonClick(btn.value)}
            >
              {btn.label}
            </button>
          ))}
        </div>
      </div>
    )
  }

  if (content.type === "card") {
    return (
      <div class="tly-card">
        {content.imageUrl && (
          <img class="tly-card-img" src={content.imageUrl} alt={content.title} loading="lazy" />
        )}
        <div class="tly-card-body">
          <div class="tly-card-title">{content.title}</div>
          {content.subtitle && <div class="tly-card-subtitle">{content.subtitle}</div>}
          {content.buttons && content.buttons.length > 0 && (
            <div class="tly-card-actions">
              {content.buttons.map((btn) => (
                <button
                  key={btn.label}
                  class="tly-btn primary"
                  onClick={() => {
                    if (btn.url) window.open(btn.url, "_blank", "noopener,noreferrer")
                    else if (btn.value) onButtonClick(btn.value)
                  }}
                >
                  {btn.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    )
  }

  if (content.type === "quick_replies") {
    if (content.dismissed) {
      return <span>{content.text}</span>
    }
    return (
      <div>
        <span>{content.text}</span>
        <div class="tly-quick-replies">
          {content.replies.map((r) => (
            <button
              key={r.value}
              class="tly-qr-chip"
              onClick={() => onQuickReply(msgId, r.value)}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>
    )
  }

  if (content.type === "file") {
    return (
      <a
        class="tly-file"
        href={content.url}
        target="_blank"
        rel="noopener noreferrer"
        download={content.filename}
      >
        <span class="tly-file-icon">{fileIcon(content.mimeType)}</span>
        <div>
          <div class="tly-file-name">{content.filename}</div>
          <div class="tly-file-size">{formatBytes(content.size)}</div>
        </div>
      </a>
    )
  }

  return null
}

// ---------------------------------------------------------------------------
// Upload progress message
// ---------------------------------------------------------------------------

interface UploadItem {
  id: string
  filename: string
  progress: number
}

// ---------------------------------------------------------------------------
// Main ChatPanel
// ---------------------------------------------------------------------------

export function ChatPanel({ config, onClose, onNewMessage }: Props) {
  const storageKey = `${config.botId}_history`

  // Load persisted history synchronously before first render
  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    const saved = loadHistory(storageKey)
    if (saved.length > 0) return saved
    if (config.greetingText) {
      return [
        {
          id: nextId(),
          role: "assistant",
          content: config.greetingText,
          createdAt: Date.now(),
        },
      ]
    }
    return []
  })

  const [input, setInput] = useState("")
  const [connected, setConnected] = useState(false)
  const [reconnecting, setReconnecting] = useState(false)
  const [typing, setTyping] = useState(false)
  const [queuedCount, setQueuedCount] = useState(0)
  const [uploads, setUploads] = useState<UploadItem[]>([])
  const [conversationId, setConversationId] = useState<string | null>(null)

  const bottomRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const wsRef = useRef<WsClient | null>(null)
  const streamRef = useRef<string>("")

  // Persist history whenever messages change
  useEffect(() => {
    saveHistory(storageKey, messages)
  }, [messages])

  // Focus input on open
  useEffect(() => {
    setTimeout(() => inputRef.current?.focus(), 100)
  }, [])

  // Connect WS
  useEffect(() => {
    const handleEvent = (event: ServerEvent) => {
      switch (event.type) {
        case "session_started":
          if (event.conversationId) setConversationId(event.conversationId)
          break

        case "typing_start":
          setTyping(true)
          break

        case "typing_end":
          setTyping(false)
          break

        case "token": {
          setTyping(false)
          const token = event.content ?? ""
          streamRef.current += token
          setMessages((prev) => {
            const last = prev[prev.length - 1]
            if (last?.role === "assistant" && last.streaming) {
              return [...prev.slice(0, -1), { ...last, content: streamRef.current }]
            }
            streamRef.current = token
            return [
              ...prev,
              {
                id: nextId(),
                role: "assistant",
                content: token,
                streaming: true,
                createdAt: Date.now(),
              },
            ]
          })
          onNewMessage?.()
          break
        }

        case "message": {
          setTyping(false)
          streamRef.current = ""
          setMessages((prev) => {
            const last = prev[prev.length - 1]
            if (last?.role === "assistant" && last.streaming) {
              return [
                ...prev.slice(0, -1),
                {
                  ...last,
                  streaming: false,
                  content: event.content ?? last.content,
                  richContent: event.richContent ?? last.richContent,
                },
              ]
            }
            return [
              ...prev,
              {
                id: nextId(),
                role: "assistant",
                content: event.content ?? "",
                richContent: event.richContent,
                createdAt: Date.now(),
              },
            ]
          })
          onNewMessage?.()
          break
        }

        case "delivered": {
          if (event.messageId) {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === event.messageId ? { ...m, status: "delivered" as const } : m
              )
            )
          }
          break
        }

        case "handoff":
          setMessages((prev) => [
            ...prev,
            {
              id: nextId(),
              role: "system",
              content: "You're now connected to a live agent.",
              createdAt: Date.now(),
            },
          ])
          break
      }
    }

    const ws = new WsClient(
      config,
      handleEvent,
      () => {
        setConnected(true)
        setReconnecting(false)
        setQueuedCount(0)
      },
      () => {
        setConnected(false)
        setReconnecting(true)
      }
    )
    ws.connect()
    wsRef.current = ws
    return () => ws.disconnect()
  }, [config])

  // Scroll to bottom
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages, typing])

  // Poll queue count when disconnected
  useEffect(() => {
    if (!reconnecting || !wsRef.current) return
    const t = setInterval(() => setQueuedCount(wsRef.current?.queuedCount ?? 0), 1000)
    return () => clearInterval(t)
  }, [reconnecting])

  const send = useCallback(
    async (text?: string) => {
      const msg = (text ?? input).trim()
      if (!msg || !wsRef.current) return
      if (!text) setInput("")
      streamRef.current = ""
      const msgId = nextId()
      setMessages((prev) => [
        ...prev,
        {
          id: msgId,
          role: "user",
          content: msg,
          createdAt: Date.now(),
          status: "sending",
        },
      ])
      await wsRef.current.sendMessage(msg)
      // Mark as sent
      setMessages((prev) =>
        prev.map((m) => (m.id === msgId ? { ...m, status: "sent" as const } : m))
      )
      if (wsRef.current.queuedCount > 0) setQueuedCount(wsRef.current.queuedCount)
    },
    [input]
  )

  const handleQuickReply = useCallback(
    (msgId: string, value: string) => {
      // Dismiss the quick replies for this message
      setMessages((prev) =>
        prev.map((m) => {
          if (m.id !== msgId) return m
          if (m.richContent?.type === "quick_replies") {
            return { ...m, richContent: { ...m.richContent, dismissed: true } }
          }
          return m
        })
      )
      send(value)
    },
    [send]
  )

  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      send()
    }
    if (e.key === "Tab") {
      const panel = document.getElementById("threadly-panel")
      if (!panel) return
      const focusable = panel.querySelectorAll<HTMLElement>(
        'button, textarea, input, [tabindex]:not([tabindex="-1"])'
      )
      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault()
        last.focus()
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault()
        first.focus()
      }
    }
  }

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose()
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [onClose])

  // ---------------------------------------------------------------------------
  // File upload
  // ---------------------------------------------------------------------------
  const handleFileSelect = useCallback(
    async (e: Event) => {
      const files = (e.target as HTMLInputElement).files
      if (!files?.length) return

      const convId = conversationId
      if (!convId) {
        console.warn("[Threadly] No conversation ID yet, cannot upload")
        return
      }

      for (const file of Array.from(files)) {
        const uploadId = nextId()
        setUploads((prev) => [...prev, { id: uploadId, filename: file.name, progress: 0 }])

        try {
          const formData = new FormData()
          formData.append("file", file)

          // Use XHR for progress tracking
          const url = `${config.apiUrl}/v1/conversations/${convId}/attachments`
          const { fileUrl } = await uploadWithProgress(url, formData, (pct) => {
            setUploads((prev) =>
              prev.map((u) => (u.id === uploadId ? { ...u, progress: pct } : u))
            )
          })

          // Remove from uploads list
          setUploads((prev) => prev.filter((u) => u.id !== uploadId))

          // Send a file message
          const fileMsg: ChatMessage = {
            id: nextId(),
            role: "user",
            content: file.name,
            richContent: {
              type: "file",
              url: fileUrl,
              filename: file.name,
              size: file.size,
              mimeType: file.type || "application/octet-stream",
            },
            createdAt: Date.now(),
            status: "sent",
          }
          setMessages((prev) => [...prev, fileMsg])

          // Also notify via WS
          if (wsRef.current) {
            await wsRef.current.sendMessage(
              JSON.stringify({ type: "file", url: fileUrl, filename: file.name })
            )
          }
        } catch (err) {
          setUploads((prev) => prev.filter((u) => u.id !== uploadId))
          console.error("[Threadly] Upload failed", err)
        }
      }

      // Reset file input
      if (fileInputRef.current) fileInputRef.current.value = ""
    },
    [config.apiUrl, conversationId]
  )

  // ---------------------------------------------------------------------------
  // Render
  // ---------------------------------------------------------------------------
  const botInitial = (config.botName ?? "T")[0].toUpperCase()

  const sendIcon = (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
    </svg>
  )

  const closeIcon = (
    <svg viewBox="0 0 24 24" width={18} height={18} fill="white" aria-hidden="true">
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
    </svg>
  )

  const attachIcon = (
    <svg viewBox="0 0 24 24" width={18} height={18} fill="currentColor" aria-hidden="true">
      <path d="M16.5 6v11.5c0 2.21-1.79 4-4 4s-4-1.79-4-4V5c0-1.38 1.12-2.5 2.5-2.5s2.5 1.12 2.5 2.5v10.5c0 .55-.45 1-1 1s-1-.45-1-1V6H10v9.5c0 1.38 1.12 2.5 2.5 2.5s2.5-1.12 2.5-2.5V5c0-2.21-1.79-4-4-4S7 2.79 7 5v12.5c0 3.04 2.46 5.5 5.5 5.5s5.5-2.46 5.5-5.5V6h-1.5z" />
    </svg>
  )

  return (
    <div
      id="threadly-panel"
      role="dialog"
      aria-modal="true"
      aria-label={`Chat with ${config.botName ?? "Support"}`}
    >
      <div class="tly-drag-handle" aria-hidden="true" />

      {/* Header */}
      <div class="tly-header" role="banner">
        <div class="tly-header-avatar" aria-hidden="true">
          {config.avatarUrl ? <img src={config.avatarUrl} alt="" /> : botInitial}
        </div>
        <div>
          <div class="tly-header-name">{config.botName ?? "Support"}</div>
          <div class="tly-header-status" aria-live="polite">
            {reconnecting ? "Reconnecting…" : connected ? "Online" : "Connecting…"}
          </div>
        </div>
        <button class="tly-header-close" onClick={onClose} aria-label="Close chat panel">
          {closeIcon}
        </button>
      </div>

      {reconnecting && (
        <div class="tly-reconnect" role="status">
          Connection lost — reconnecting…
          {queuedCount > 0 && ` (${queuedCount} message${queuedCount > 1 ? "s" : ""} queued)`}
        </div>
      )}

      {/* Messages */}
      <div
        class="tly-messages"
        role="log"
        aria-live="polite"
        aria-label="Chat messages"
        aria-atomic="false"
      >
        {messages.map((msg) => {
          if (msg.role === "system") {
            return (
              <div
                key={msg.id}
                role="status"
                style={{ textAlign: "center", fontSize: 12, color: "var(--tly-muted)", padding: "2px 0" }}
              >
                {msg.content}
              </div>
            )
          }

          const isUser = msg.role === "user"
          const statusMark =
            msg.status === "delivered"
              ? "✓✓"
              : msg.status === "sent" || msg.status === "sending"
              ? "✓"
              : ""

          return (
            <div
              key={msg.id}
              class={`tly-bubble ${msg.role}`}
              role="article"
              aria-label={isUser ? "Your message" : `${config.botName ?? "Support"} replied`}
            >
              <div class="tly-bubble-avatar" aria-hidden="true">
                {isUser ? "Y" : (config.avatarUrl ? <img src={config.avatarUrl} alt="" style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "50%" }} /> : botInitial)}
              </div>

              <div class="tly-bubble-inner">
                <div class={`tly-bubble-content${msg.streaming ? " tly-cursor" : ""}`}>
                  {msg.richContent ? (
                    <RichContent
                      content={msg.richContent}
                      onButtonClick={(val) => send(val)}
                      onQuickReply={handleQuickReply}
                      msgId={msg.id}
                    />
                  ) : (
                    msg.content
                  )}
                </div>

                <div style={{ display: "flex", gap: 4, alignItems: "center" }}>
                  <span class="tly-timestamp">{formatTime(msg.createdAt)}</span>
                  {isUser && statusMark && (
                    <span class="tly-status" aria-label={msg.status}>{statusMark}</span>
                  )}
                </div>
              </div>
            </div>
          )
        })}

        {/* Upload progress indicators */}
        {uploads.map((u) => (
          <div key={u.id} class="tly-bubble user">
            <div class="tly-bubble-avatar" aria-hidden="true">Y</div>
            <div class="tly-upload-progress">
              <div>{u.filename}</div>
              <div class="tly-progress-bar">
                <div class="tly-progress-fill" style={{ width: `${u.progress}%` }} />
              </div>
            </div>
          </div>
        ))}

        {typing && !messages.some((m) => m.streaming) && (
          <div class="tly-typing" role="status" aria-label="Support is typing">
            <span aria-hidden="true" />
            <span aria-hidden="true" />
            <span aria-hidden="true" />
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*,.pdf,.txt"
        style={{ display: "none" }}
        onChange={handleFileSelect as any}
        aria-hidden="true"
      />

      {/* Input area */}
      <div class="tly-input-area">
        <button
          class="tly-attach"
          onClick={() => fileInputRef.current?.click()}
          aria-label="Attach file"
          title="Attach file"
        >
          {attachIcon}
        </button>
        <textarea
          ref={inputRef}
          class="tly-input"
          value={input}
          onInput={(e) => setInput((e.target as HTMLTextAreaElement).value)}
          onKeyDown={handleKeyDown as any}
          placeholder={config.greetingText ? "Type a message…" : "Type a message…"}
          rows={1}
          aria-label="Message input"
          aria-multiline="true"
        />
        <button
          class="tly-send"
          onClick={() => send()}
          disabled={!input.trim()}
          aria-label="Send message"
          aria-disabled={!input.trim()}
        >
          {sendIcon}
        </button>
      </div>

      <div class="tly-branding">
        Powered by{" "}
        <a href="https://threadly.dev" target="_blank" rel="noopener noreferrer">
          Threadly
        </a>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Upload helper with XHR progress
// ---------------------------------------------------------------------------

function uploadWithProgress(
  url: string,
  formData: FormData,
  onProgress: (pct: number) => void
): Promise<{ fileUrl: string }> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open("POST", url)

    xhr.upload.addEventListener("progress", (e) => {
      if (e.lengthComputable) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    })

    xhr.addEventListener("load", () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const data = JSON.parse(xhr.responseText)
          resolve({ fileUrl: data.url ?? data.fileUrl ?? "" })
        } catch {
          reject(new Error("Invalid JSON response"))
        }
      } else {
        reject(new Error(`Upload failed: ${xhr.status}`))
      }
    })

    xhr.addEventListener("error", () => reject(new Error("Network error during upload")))
    xhr.addEventListener("abort", () => reject(new Error("Upload aborted")))

    xhr.send(formData)
  })
}
