import type { WidgetConfig } from "./types"

export function injectStyles(config: WidgetConfig) {
  const accent = config.accentColor ?? "#4f46e5"
  const id = "threadly-styles"
  if (document.getElementById(id)) return

  // Detect dark mode preference
  const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)").matches
  const isDark = config.darkMode === true || (config.darkMode !== false && prefersDark)

  const bg = isDark ? "#18181b" : "#ffffff"
  const bgMsg = isDark ? "#09090b" : "#f9f9fa"
  const bgBubbleAi = isDark ? "#27272a" : "#ffffff"
  const border = isDark ? "#3f3f46" : "#e4e4e7"
  const textPrimary = isDark ? "#fafafa" : "#1a1a1a"
  const textMuted = isDark ? "#a1a1aa" : "#6b7280"
  const inputBg = isDark ? "#27272a" : "#f4f4f5"
  const reconnectBg = isDark ? "#451a03" : "#fef3c7"
  const reconnectText = isDark ? "#fbbf24" : "#92400e"

  const style = document.createElement("style")
  style.id = id
  style.textContent = `
    #threadly-root * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    }

    #threadly-launcher {
      position: fixed;
      ${config.position === "bottom-left" ? "left: 20px;" : "right: 20px;"}
      bottom: 20px;
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: ${accent};
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
      outline: 3px solid ${accent};
      outline-offset: 3px;
    }
    #threadly-launcher svg {
      width: 24px;
      height: 24px;
      fill: white;
    }

    #threadly-panel {
      position: fixed;
      ${config.position === "bottom-left" ? "left: 16px;" : "right: 16px;"}
      bottom: 88px;
      width: 360px;
      height: 560px;
      max-height: calc(100vh - 108px);
      background: ${bg};
      border-radius: 16px;
      box-shadow: 0 16px 48px rgba(0,0,0,0.22);
      z-index: 2147483646;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      transform-origin: ${config.position === "bottom-left" ? "bottom left" : "bottom right"};
      transition: transform 280ms cubic-bezier(0.16, 1, 0.3, 1),
                  opacity 220ms ease;
    }

    /* Mobile: full-screen bottom sheet */
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
      background: ${accent};
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

    /* Mobile handle hint */
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
      background: ${bgMsg};
      scroll-behavior: smooth;
    }
    .tly-messages::-webkit-scrollbar { width: 4px; }
    .tly-messages::-webkit-scrollbar-thumb { background: ${border}; border-radius: 2px; }

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
    .tly-bubble.user .tly-bubble-avatar { background: ${border}; color: ${textMuted}; }
    .tly-bubble.assistant .tly-bubble-avatar { background: ${accent}; color: white; }

    .tly-bubble-content {
      max-width: 78%;
      padding: 10px 13px;
      border-radius: 14px;
      font-size: 14px;
      line-height: 1.5;
      color: ${textPrimary};
      word-break: break-word;
    }
    .tly-bubble.user .tly-bubble-content {
      background: ${accent};
      color: white;
      border-bottom-right-radius: 4px;
    }
    .tly-bubble.assistant .tly-bubble-content {
      background: ${bgBubbleAi};
      border: 1px solid ${border};
      border-bottom-left-radius: 4px;
    }

    /* Streaming cursor */
    .tly-cursor::after {
      content: '▋';
      animation: tly-blink 1s step-end infinite;
      font-size: 13px;
      color: ${accent};
    }
    @keyframes tly-blink { 0%,100% { opacity: 1; } 50% { opacity: 0; } }

    .tly-typing {
      display: flex;
      gap: 4px;
      padding: 10px 14px;
      align-items: center;
      background: ${bgBubbleAi};
      border: 1px solid ${border};
      border-radius: 14px;
      border-bottom-left-radius: 4px;
      width: fit-content;
      margin-left: 34px;
    }
    .tly-typing span {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: ${textMuted};
      animation: tly-bounce 1.2s ease-in-out infinite;
    }
    .tly-typing span:nth-child(2) { animation-delay: 0.2s; }
    .tly-typing span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes tly-bounce {
      0%, 60%, 100% { transform: translateY(0); }
      30% { transform: translateY(-4px); }
    }

    .tly-input-area {
      padding: 12px;
      background: ${bg};
      border-top: 1px solid ${border};
      display: flex;
      gap: 8px;
      align-items: flex-end;
      flex-shrink: 0;
    }
    .tly-input {
      flex: 1;
      padding: 10px 12px;
      border: 1px solid ${border};
      border-radius: 10px;
      font-size: 14px;
      outline: none;
      resize: none;
      font-family: inherit;
      line-height: 1.4;
      max-height: 100px;
      color: ${textPrimary};
      background: ${inputBg};
    }
    .tly-input:focus { border-color: ${accent}; }
    .tly-input:focus-visible { outline: 2px solid ${accent}; outline-offset: 1px; }
    .tly-send {
      width: 36px;
      height: 36px;
      min-width: 36px;
      border-radius: 10px;
      background: ${accent};
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: opacity 160ms ease;
    }
    .tly-send:disabled { opacity: 0.4; cursor: not-allowed; }
    .tly-send:focus-visible { outline: 3px solid ${accent}; outline-offset: 2px; }
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
      background: ${border};
      font-size: 11px;
      color: ${textMuted};
      flex-shrink: 0;
    }

    .tly-branding {
      text-align: center;
      font-size: 11px;
      color: ${textMuted};
      padding: 6px;
      flex-shrink: 0;
    }
    .tly-branding a { color: ${textMuted}; text-decoration: none; }
    .tly-branding a:hover { text-decoration: underline; }
  `
  document.head.appendChild(style)
}
