/** @jsxImportSource preact */
import { render } from "preact"
import { useEffect, useRef, useState } from "preact/hooks"
import { ChatPanel } from "./ui/ChatPanel"
import type { WidgetConfig } from "./types"

interface MountOptions {
  onNewMessage?: () => void
  onPanelOpen?: () => void
}

interface AppProps {
  config: WidgetConfig
  options: MountOptions
}

function App({ config, options }: AppProps) {
  const [open, setOpen] = useState(false)
  const [entering, setEntering] = useState(false)
  const panelRef = useRef<HTMLDivElement>(null)

  function openPanel() {
    setOpen(true)
    setEntering(true)
    options.onPanelOpen?.()
    setTimeout(() => setEntering(false), 320)
  }

  function closePanel() {
    setOpen(false)
  }

  // Mobile slide-up animation class
  useEffect(() => {
    const panel = document.getElementById("threadly-panel")
    if (panel && entering) panel.classList.add("entering")
    else if (panel) panel.classList.remove("entering")
  }, [entering])

  const chatIcon = (
    <svg viewBox="0 0 24 24" style={{ fill: "white", width: 24, height: 24 }} aria-hidden="true">
      <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" />
    </svg>
  )

  const closeIcon = (
    <svg viewBox="0 0 24 24" style={{ fill: "white", width: 22, height: 22 }} aria-hidden="true">
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
    </svg>
  )

  return (
    <>
      {/* Launcher button — badge is injected by widget.ts into this element */}
      <button
        id="threadly-launcher"
        onClick={() => (open ? closePanel() : openPanel())}
        aria-label={open ? "Close chat" : `Chat with ${config.botName ?? "us"}`}
        aria-expanded={open}
        aria-controls="threadly-panel"
        aria-haspopup="dialog"
      >
        {open ? closeIcon : chatIcon}
      </button>

      {/* Chat panel */}
      {open && (
        <ChatPanel
          config={config}
          onClose={closePanel}
          onNewMessage={options.onNewMessage}
        />
      )}
    </>
  )
}

export function mount(config: WidgetConfig, options: MountOptions = {}): void {
  const root = document.createElement("div")
  root.id = "threadly-root"
  root.setAttribute("role", "region")
  root.setAttribute("aria-label", "Chat widget")
  document.body.appendChild(root)
  render(<App config={config} options={options} />, root)
}
