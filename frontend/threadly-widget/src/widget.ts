/**
 * Threadly Widget Loader
 *
 * Entry point loaded via <script> tag. Reads config from data attributes,
 * injects styles, mounts the widget, and manages the launcher badge + sound.
 *
 * Usage:
 *   <script
 *     src="https://cdn.threadly.dev/widget.js"
 *     data-bot="<botId>"
 *     data-api-url="https://api.threadly.dev"
 *     data-centrifugo-url="wss://rt.threadly.dev/connection/websocket"
 *     data-accent="#4f46e5"
 *     data-position="bottom-right"
 *     data-greeting="Hi! How can I help?"
 *     data-bot-name="Support"
 *     data-sound="true"
 *   ></script>
 */
import type { WidgetConfig } from "./types"
import { injectStyles } from "./theme"
import { mount } from "./main"

// ---------------------------------------------------------------------------
// Notification chime — base64-encoded minimal WAV (0.5 s, 440 Hz sine)
// Generated offline to keep the bundle self-contained.
// ---------------------------------------------------------------------------
const CHIME_B64 =
  "UklGRiQAAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQAAAAA="

let _audioCtx: AudioContext | null = null

function playChime(): void {
  try {
    if (!_audioCtx) _audioCtx = new AudioContext()
    const ctx = _audioCtx
    const oscillator = ctx.createOscillator()
    const gainNode = ctx.createGain()
    oscillator.connect(gainNode)
    gainNode.connect(ctx.destination)
    oscillator.type = "sine"
    oscillator.frequency.setValueAtTime(880, ctx.currentTime)
    gainNode.gain.setValueAtTime(0.15, ctx.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5)
    oscillator.start(ctx.currentTime)
    oscillator.stop(ctx.currentTime + 0.5)
  } catch {
    // Audio may be blocked by browser policy — fail silently
  }
}

// ---------------------------------------------------------------------------
// Badge management
// ---------------------------------------------------------------------------

function createBadge(launcher: HTMLElement): HTMLSpanElement {
  const badge = document.createElement("span")
  badge.id = "threadly-badge"
  badge.setAttribute("aria-live", "polite")
  badge.setAttribute("aria-label", "unread messages")
  badge.style.display = "none"
  launcher.style.position = "relative"
  launcher.appendChild(badge)
  return badge
}

function updateBadge(badge: HTMLSpanElement, count: number): void {
  if (count <= 0) {
    badge.style.display = "none"
    badge.textContent = ""
  } else {
    badge.style.display = "flex"
    badge.textContent = count > 99 ? "99+" : String(count)
  }
}

// ---------------------------------------------------------------------------
// Bootstrap
// ---------------------------------------------------------------------------

function bootstrap(): void {
  const script =
    (document.currentScript as HTMLScriptElement | null) ??
    document.querySelector<HTMLScriptElement>('script[data-bot][src*="widget"]')

  const botId = script?.dataset.bot ?? ""
  if (!botId) {
    console.warn("[Threadly] data-bot attribute is required.")
    return
  }

  const soundEnabled = script?.dataset.sound !== "false"

  const config: WidgetConfig = {
    botId,
    apiUrl: script?.dataset.apiUrl ?? "https://api.threadly.dev",
    centrifugoUrl:
      script?.dataset.centrifugoUrl ?? "wss://rt.threadly.dev/connection/websocket",
    accentColor: script?.dataset.accent,
    avatarUrl: script?.dataset.avatarUrl,
    greetingText: script?.dataset.greeting ?? "Hi! How can I help you today?",
    position:
      (script?.dataset.position as WidgetConfig["position"]) ?? "bottom-right",
    botName: script?.dataset.botName ?? "Support",
    sound: soundEnabled,
  }

  // Inject CSS
  injectStyles(config)

  // Mount Preact app — receives a callback when a new message arrives
  let unreadCount = 0
  let badgeEl: HTMLSpanElement | null = null

  const onNewMessage = () => {
    const launcher = document.getElementById("threadly-launcher")
    const panel = document.getElementById("threadly-panel")
    const isPanelOpen = panel !== null && !panel.classList.contains("hidden")

    // Only badge/sound when panel is closed
    if (!isPanelOpen) {
      unreadCount++
      if (!badgeEl && launcher) {
        badgeEl = createBadge(launcher)
      }
      if (badgeEl) updateBadge(badgeEl, unreadCount)
      if (soundEnabled) playChime()
    }
  }

  const onPanelOpen = () => {
    unreadCount = 0
    if (badgeEl) updateBadge(badgeEl, 0)
  }

  mount(config, { onNewMessage, onPanelOpen })
}

// Run after DOM ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", bootstrap)
} else {
  bootstrap()
}
