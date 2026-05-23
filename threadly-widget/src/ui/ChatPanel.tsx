/** @jsxImportSource preact */
import { useState, useEffect, useRef, useCallback } from "preact/hooks"
import type { ChatMessage, ServerEvent, WidgetConfig } from "../types"
import { WsClient } from "../ws-client"

interface Props {
  config: WidgetConfig
  onClose: () => void
}

let msgIdCounter = 0
const nextId = () => `m_${++msgIdCounter}_${Date.now()}`

export function ChatPanel({ config, onClose }: Props) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState("")
  const [connected, setConnected] = useState(false)
  const [reconnecting, setReconnecting] = useState(false)
  const [typing, setTyping] = useState(false)
  const [queuedCount, setQueuedCount] = useState(0)
  const bottomRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const wsRef = useRef<WsClient | null>(null)
  const streamRef = useRef<string>("")

  // Greeting
  useEffect(() => {
    if (config.greetingText) {
      setMessages([{
        id: nextId(),
        role: "assistant",
        content: config.greetingText,
        createdAt: Date.now(),
      }])
    }
    // Focus input on open
    setTimeout(() => inputRef.current?.focus(), 100)
  }, [])

  // Connect WS
  useEffect(() => {
    const handleEvent = (event: ServerEvent) => {
      switch (event.type) {
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
            return [...prev, {
              id: nextId(),
              role: "assistant",
              content: token,
              streaming: true,
              createdAt: Date.now(),
            }]
          })
          break
        }

        case "message": {
          setTyping(false)
          streamRef.current = ""
          setMessages((prev) => {
            const last = prev[prev.length - 1]
            if (last?.role === "assistant" && last.streaming) {
              return [...prev.slice(0, -1), { ...last, streaming: false, content: event.content ?? last.content }]
            }
            return [...prev, {
              id: nextId(),
              role: "assistant",
              content: event.content ?? "",
              createdAt: Date.now(),
            }]
          })
          break
        }

        case "handoff":
          setMessages((prev) => [...prev, {
            id: nextId(),
            role: "system",
            content: "You're now connected to a live agent.",
            createdAt: Date.now(),
          }])
          break

        case "session_started":
          // Already handled by greeting
          break
      }
    }

    const ws = new WsClient(
      config,
      handleEvent,
      () => { setConnected(true); setReconnecting(false); setQueuedCount(0) },
      () => { setConnected(false); setReconnecting(true) },
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

  const send = useCallback(async () => {
    const text = input.trim()
    if (!text || !wsRef.current) return
    setInput("")
    streamRef.current = ""
    setMessages((prev) => [...prev, { id: nextId(), role: "user", content: text, createdAt: Date.now() }])
    await wsRef.current.sendMessage(text)
    if (wsRef.current.queuedCount > 0) setQueuedCount(wsRef.current.queuedCount)
  }, [input])

  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      send()
    }
    // Trap focus inside panel
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

  // Keyboard: Escape closes panel
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose()
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [onClose])

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

  return (
    <div
      id="threadly-panel"
      role="dialog"
      aria-modal="true"
      aria-label={`Chat with ${config.botName ?? "Support"}`}
    >
      {/* Mobile drag handle */}
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
                style={{ textAlign: "center", fontSize: 12, color: "#a1a1aa", padding: "2px 0" }}
              >
                {msg.content}
              </div>
            )
          }
          const isUser = msg.role === "user"
          return (
            <div
              key={msg.id}
              class={`tly-bubble ${msg.role}`}
              role="article"
              aria-label={isUser ? "Your message" : `${config.botName ?? "Support"} replied`}
            >
              <div class="tly-bubble-avatar" aria-hidden="true">
                {isUser ? "Y" : botInitial}
              </div>
              <div class={`tly-bubble-content${msg.streaming ? " tly-cursor" : ""}`}>
                {msg.content}
              </div>
            </div>
          )
        })}

        {typing && !messages.some((m) => m.streaming) && (
          <div class="tly-typing" role="status" aria-label="Support is typing">
            <span aria-hidden="true" />
            <span aria-hidden="true" />
            <span aria-hidden="true" />
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* Input area */}
      <div class="tly-input-area">
        <textarea
          ref={inputRef}
          class="tly-input"
          value={input}
          onInput={(e) => setInput((e.target as HTMLTextAreaElement).value)}
          onKeyDown={handleKeyDown as any}
          placeholder="Type a message…"
          rows={1}
          aria-label="Message input"
          aria-multiline="true"
        />
        <button
          class="tly-send"
          onClick={send}
          disabled={!input.trim()}
          aria-label="Send message"
          aria-disabled={!input.trim()}
        >
          {sendIcon}
        </button>
      </div>

      {/* Branding */}
      <div class="tly-branding">
        Powered by{" "}
        <a href="https://threadly.dev" target="_blank" rel="noopener noreferrer">
          Threadly
        </a>
      </div>
    </div>
  )
}
