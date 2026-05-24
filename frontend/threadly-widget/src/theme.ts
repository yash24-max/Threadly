import type { WidgetConfig } from "./types"

// ---------------------------------------------------------------------------
// Full theme interface
// ---------------------------------------------------------------------------

export interface WidgetTheme {
  primaryColor: string
  backgroundColor: string
  textColor: string
  botName: string
  welcomeMessage: string
  placeholder: string
  position: "bottom-right" | "bottom-left"
  launcherSize: number
  borderRadius: number
  fontFamily: string
  darkMode: "auto" | "light" | "dark"
}

export function buildTheme(config: WidgetConfig): WidgetTheme {
  return {
    primaryColor: config.accentColor ?? "#4f46e5",
    backgroundColor: "#ffffff",
    textColor: "#1a1a1a",
    botName: config.botName ?? "Support",
    welcomeMessage: config.greetingText ?? "Hi! How can I help you today?",
    placeholder: "Type a message…",
    position: config.position ?? "bottom-right",
    launcherSize: 56,
    borderRadius: 16,
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    darkMode: config.darkMode === true ? "dark" : config.darkMode === false ? "light" : "auto",
  }
}

// ---------------------------------------------------------------------------
// Style injection — applies CSS variables to shadow DOM root or document
// ---------------------------------------------------------------------------

export function injectStyles(config: WidgetConfig, root?: ShadowRoot | Document): void {
  const theme = buildTheme(config)
  const accent = theme.primaryColor

  const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)").matches
  const isDark =
    theme.darkMode === "dark" ||
    (theme.darkMode === "auto" && prefersDark)

  const bg = isDark ? "#18181b" : "#ffffff"
  const bgMsg = isDark ? "#09090b" : "#f9f9fa"
  const bgBubbleAi = isDark ? "#27272a" : "#ffffff"
  const border = isDark ? "#3f3f46" : "#e4e4e7"
  const textPrimary = isDark ? "#fafafa" : "#1a1a1a"
  const textMuted = isDark ? "#a1a1aa" : "#6b7280"
  const inputBg = isDark ? "#27272a" : "#f4f4f5"
  const reconnectBg = isDark ? "#451a03" : "#fef3c7"
  const reconnectText = isDark ? "#fbbf24" : "#92400e"

  const target = root ?? document
  const existingId = "threadly-styles"

  // Remove stale style element
  const existing = (target as Document).getElementById
    ? (target as Document).getElementById(existingId)
    : (root as ShadowRoot).querySelector(`#${existingId}`)
  existing?.remove()

  const style = document.createElement("style")
  style.id = existingId
  style.textContent = `
    :host, #threadly-root * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: ${theme.fontFamily};
    }

    /* CSS variables for theming */
    :host, #threadly-root {
      --tly-accent: ${accent};
      --tly-bg: ${bg};
      --tly-bg-msg: ${bgMsg};
      --tly-bg-bubble-ai: ${bgBubbleAi};
      --tly-border: ${border};
      --tly-text: ${textPrimary};
      --tly-muted: ${textMuted};
      --tly-input-bg: ${inputBg};
      --tly-radius: ${theme.borderRadius}px;
      --tly-launcher-size: ${theme.launcherSize}px;
    }

    #threadly-launcher {
      position: fixed;
      ${theme.position === "bottom-left" ? "left: 20px;" : "right: 20px;"}
      bottom: 20px;
      width: var(--tly-launcher-size);
      height: var(--tly-launcher-size);
      border-radius: 50%;
      background: var(--tly-accent);
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 16px rgba(0,0,0,0.25);
      z-index: 2147483647;
      transition: transform 160ms cubic-bezier(0.16, 1, 0.3, 1),
                  box-shadow 160ms ease;
    }
    #threadly-launcher:hover {
      transform: scale(1.08);
      box-shadow: 0 6px 24px rgba(0,0,0,0.32);
    }
    #threadly-launcher:focus-visible {
      outline: 3px solid var(--tly-accent);
      outline-offset: 3px;
    }
    #threadly-launcher svg {
      width: 24px;
      height: 24px;
      fill: white;
    }

    /* Unread badge */
    #threadly-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      min-width: 18px;
      height: 18px;
      border-radius: 9px;
      background: #ef4444;
      color: white;
      font-size: 10px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
      border: 2px solid white;
      pointer-events: none;
    }

    #threadly-panel {
      position: fixed;
      ${theme.position === "bottom-left" ? "left: 16px;" : "right: 16px;"}
      bottom: 88px;
      width: 360px;
      height: 560px;
      max-height: calc(100vh - 108px);
      background: var(--tly-bg);
      border-radius: var(--tly-radius);
      box-shadow: 0 16px 48px rgba(0,0,0,0.22);
      z-index: 2147483646;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      transform-origin: ${theme.position === "bottom-left" ? "bottom left" : "bottom right"};
      transition: transform 280ms cubic-bezier(0.16, 1, 0.3, 1),
                  opacity 220ms ease;
    }

    @media (max-width: 480px) {
      #threadly-panel {
        left: 0 !important;
        right: 0 !important;
        bottom: 0;
        width: 100%;
        height: 90dvh;
        height: 90vh;
        border-radius: 20px 20px 0 0;
        transform-origin: bottom center;
      }
      #threadly-panel.entering {
        animation: tly-sheet-in 280ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
      }
    }

    @keyframes tly-sheet-in {
      from { transform: translateY(100%); opacity: 0; }
      to   { transform: translateY(0);    opacity: 1; }
    }

    #threadly-panel.hidden {
      transform: scale(0.92) translateY(10px);
      opacity: 0;
      pointer-events: none;
    }

    .tly-header {
      background: var(--tly-accent);
      color: white;
      padding: 14px 16px;
      display: flex;
      align-items: center;
      gap: 10px;
      flex-shrink: 0;
    }
    .tly-header-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(255,255,255,0.25);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      font-weight: 700;
      flex-shrink: 0;
      overflow: hidden;
    }
    .tly-header-avatar img { width: 100%; height: 100%; object-fit: cover; }
    .tly-header-name { font-size: 15px; font-weight: 600; }
    .tly-header-status { font-size: 11px; opacity: 0.8; margin-top: 1px; }
    .tly-header-close {
      margin-left: auto;
      background: none;
      border: none;
      cursor: pointer;
      color: white;
      opacity: 0.8;
      display: flex;
      align-items: center;
      padding: 4px;
      border-radius: 6px;
    }
    .tly-header-close:hover { opacity: 1; background: rgba(255,255,255,0.15); }
    .tly-header-close:focus-visible { outline: 2px solid rgba(255,255,255,0.6); outline-offset: 2px; }

    .tly-drag-handle {
      display: none;
      width: 36px;
      height: 4px;
      background: rgba(0,0,0,0.15);
      border-radius: 2px;
      margin: 8px auto;
      flex-shrink: 0;
    }
    @media (max-width: 480px) { .tly-drag-handle { display: block; } }

    .tly-messages {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      background: var(--tly-bg-msg);
      scroll-behavior: smooth;
    }
    .tly-messages::-webkit-scrollbar { width: 4px; }
    .tly-messages::-webkit-scrollbar-thumb { background: var(--tly-border); border-radius: 2px; }

    /* Message bubble wrapper */
    .tly-bubble {
      display: flex;
      gap: 8px;
      align-items: flex-end;
    }
    .tly-bubble.user { flex-direction: row-reverse; }

    .tly-bubble-avatar {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      font-weight: 700;
      flex-shrink: 0;
    }
    .tly-bubble.user .tly-bubble-avatar { background: var(--tly-border); color: var(--tly-muted); }
    .tly-bubble.assistant .tly-bubble-avatar { background: var(--tly-accent); color: white; }

    .tly-bubble-inner {
      max-width: 78%;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .tly-bubble.user .tly-bubble-inner { align-items: flex-end; }

    .tly-bubble-content {
      padding: 10px 13px;
      border-radius: 14px;
      font-size: 14px;
      line-height: 1.5;
      color: var(--tly-text);
      word-break: break-word;
      position: relative;
    }
    .tly-bubble.user .tly-bubble-content {
      background: var(--tly-accent);
      color: white;
      border-bottom-right-radius: 4px;
    }
    .tly-bubble.assistant .tly-bubble-content {
      background: var(--tly-bg-bubble-ai);
      border: 1px solid var(--tly-border);
      border-bottom-left-radius: 4px;
    }

    /* Timestamp shown on hover */
    .tly-timestamp {
      font-size: 10px;
      color: var(--tly-muted);
      opacity: 0;
      transition: opacity 150ms ease;
      white-space: nowrap;
      padding: 0 2px;
    }
    .tly-bubble:hover .tly-timestamp { opacity: 1; }

    /* Message status (sent/delivered) */
    .tly-status {
      font-size: 10px;
      color: var(--tly-muted);
      text-align: right;
      padding: 0 2px;
    }

    /* Streaming cursor */
    .tly-cursor::after {
      content: '▋';
      animation: tly-blink 1s step-end infinite;
      font-size: 13px;
      color: var(--tly-accent);
    }
    @keyframes tly-blink { 0%,100% { opacity: 1; } 50% { opacity: 0; } }

    .tly-typing {
      display: flex;
      gap: 4px;
      padding: 10px 14px;
      align-items: center;
      background: var(--tly-bg-bubble-ai);
      border: 1px solid var(--tly-border);
      border-radius: 14px;
      border-bottom-left-radius: 4px;
      width: fit-content;
      margin-left: 34px;
    }
    .tly-typing span {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--tly-muted);
      animation: tly-bounce 1.2s ease-in-out infinite;
    }
    .tly-typing span:nth-child(2) { animation-delay: 0.2s; }
    .tly-typing span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes tly-bounce {
      0%, 60%, 100% { transform: translateY(0); }
      30% { transform: translateY(-4px); }
    }

    /* Rich content: Buttons */
    .tly-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      padding: 6px 0 2px;
    }
    .tly-btn {
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 13px;
      cursor: pointer;
      border: 1.5px solid var(--tly-accent);
      background: transparent;
      color: var(--tly-accent);
      font-family: inherit;
      transition: background 120ms ease, color 120ms ease;
    }
    .tly-btn:hover { background: var(--tly-accent); color: white; }
    .tly-btn.primary { background: var(--tly-accent); color: white; }
    .tly-btn.primary:hover { opacity: 0.85; }
    .tly-btn:focus-visible { outline: 2px solid var(--tly-accent); outline-offset: 2px; }

    /* Rich content: Card */
    .tly-card {
      border: 1px solid var(--tly-border);
      border-radius: 12px;
      overflow: hidden;
      background: var(--tly-bg-bubble-ai);
      max-width: 260px;
    }
    .tly-card-img { width: 100%; max-height: 140px; object-fit: cover; }
    .tly-card-body { padding: 10px 12px; }
    .tly-card-title { font-size: 14px; font-weight: 600; color: var(--tly-text); }
    .tly-card-subtitle { font-size: 12px; color: var(--tly-muted); margin-top: 2px; }
    .tly-card-actions { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }

    /* Rich content: Quick replies */
    .tly-quick-replies {
      display: flex;
      gap: 6px;
      overflow-x: auto;
      padding: 6px 0 2px;
      scrollbar-width: none;
    }
    .tly-quick-replies::-webkit-scrollbar { display: none; }
    .tly-qr-chip {
      padding: 5px 13px;
      border-radius: 16px;
      font-size: 13px;
      cursor: pointer;
      border: 1.5px solid var(--tly-accent);
      background: transparent;
      color: var(--tly-accent);
      white-space: nowrap;
      font-family: inherit;
      flex-shrink: 0;
      transition: background 120ms ease, color 120ms ease;
    }
    .tly-qr-chip:hover { background: var(--tly-accent); color: white; }
    .tly-qr-chip:focus-visible { outline: 2px solid var(--tly-accent); outline-offset: 2px; }

    /* Rich content: File */
    .tly-file {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 13px;
      border: 1px solid var(--tly-border);
      border-radius: 10px;
      background: var(--tly-bg-bubble-ai);
      text-decoration: none;
      color: var(--tly-text);
      max-width: 260px;
    }
    .tly-file:hover { border-color: var(--tly-accent); }
    .tly-file-icon { font-size: 22px; flex-shrink: 0; }
    .tly-file-name { font-size: 13px; font-weight: 500; word-break: break-all; }
    .tly-file-size { font-size: 11px; color: var(--tly-muted); margin-top: 2px; }

    /* File upload progress */
    .tly-upload-progress {
      padding: 8px 13px;
      border-radius: 10px;
      background: var(--tly-bg-bubble-ai);
      border: 1px solid var(--tly-border);
      font-size: 13px;
      color: var(--tly-muted);
    }
    .tly-progress-bar {
      height: 4px;
      border-radius: 2px;
      background: var(--tly-border);
      margin-top: 6px;
      overflow: hidden;
    }
    .tly-progress-fill {
      height: 100%;
      background: var(--tly-accent);
      border-radius: 2px;
      transition: width 200ms ease;
    }

    /* Input area */
    .tly-input-area {
      padding: 12px;
      background: var(--tly-bg);
      border-top: 1px solid var(--tly-border);
      display: flex;
      gap: 8px;
      align-items: flex-end;
      flex-shrink: 0;
    }
    .tly-attach {
      width: 36px;
      height: 36px;
      min-width: 36px;
      border-radius: 10px;
      background: var(--tly-input-bg);
      border: 1px solid var(--tly-border);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: var(--tly-muted);
      transition: border-color 120ms ease;
    }
    .tly-attach:hover { border-color: var(--tly-accent); color: var(--tly-accent); }
    .tly-attach:focus-visible { outline: 2px solid var(--tly-accent); outline-offset: 2px; }
    .tly-input {
      flex: 1;
      padding: 10px 12px;
      border: 1px solid var(--tly-border);
      border-radius: 10px;
      font-size: 14px;
      outline: none;
      resize: none;
      font-family: inherit;
      line-height: 1.4;
      max-height: 100px;
      color: var(--tly-text);
      background: var(--tly-input-bg);
    }
    .tly-input:focus { border-color: var(--tly-accent); }
    .tly-input:focus-visible { outline: 2px solid var(--tly-accent); outline-offset: 1px; }
    .tly-send {
      width: 36px;
      height: 36px;
      min-width: 36px;
      border-radius: 10px;
      background: var(--tly-accent);
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: opacity 160ms ease;
    }
    .tly-send:disabled { opacity: 0.4; cursor: not-allowed; }
    .tly-send:focus-visible { outline: 3px solid var(--tly-accent); outline-offset: 2px; }
    .tly-send svg { fill: white; width: 16px; height: 16px; }

    .tly-reconnect {
      text-align: center;
      padding: 6px 12px;
      background: ${reconnectBg};
      font-size: 12px;
      color: ${reconnectText};
      flex-shrink: 0;
    }

    .tly-offline-badge {
      text-align: center;
      padding: 4px;
      background: var(--tly-border);
      font-size: 11px;
      color: var(--tly-muted);
      flex-shrink: 0;
    }

    .tly-branding {
      text-align: center;
      font-size: 11px;
      color: var(--tly-muted);
      padding: 6px;
      flex-shrink: 0;
    }
    .tly-branding a { color: var(--tly-muted); text-decoration: none; }
    .tly-branding a:hover { text-decoration: underline; }
  `

  if (root) {
    root.appendChild(style)
  } else {
    document.head.appendChild(style)
  }
}
