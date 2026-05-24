import { describe, it, expect, beforeEach, vi, afterEach } from "vitest"
import { buildTheme, injectStyles, type WidgetTheme } from "../theme"
import type { WidgetConfig } from "../types"

// ---------------------------------------------------------------------------
// Minimal config factory
// ---------------------------------------------------------------------------
function makeConfig(overrides: Partial<WidgetConfig> = {}): WidgetConfig {
  return {
    botId: "bot-001",
    apiUrl: "https://api.threadly.dev",
    centrifugoUrl: "wss://rt.threadly.dev/connection/websocket",
    ...overrides,
  }
}

// ---------------------------------------------------------------------------
// buildTheme
// ---------------------------------------------------------------------------
describe("buildTheme", () => {
  it("returns default values for empty config", () => {
    const theme = buildTheme(makeConfig())

    expect(theme.primaryColor).toBe("#4f46e5")
    expect(theme.backgroundColor).toBe("#ffffff")
    expect(theme.textColor).toBe("#1a1a1a")
    expect(theme.botName).toBe("Support")
    expect(theme.placeholder).toBe("Type a message…")
    expect(theme.position).toBe("bottom-right")
    expect(theme.launcherSize).toBe(56)
    expect(theme.borderRadius).toBe(16)
  })

  it("applies primaryColor from accentColor config field", () => {
    const theme = buildTheme(makeConfig({ accentColor: "#ff6600" }))

    expect(theme.primaryColor).toBe("#ff6600")
  })

  it("applies botName from config", () => {
    const theme = buildTheme(makeConfig({ botName: "Aria" }))

    expect(theme.botName).toBe("Aria")
  })

  it("applies welcomeMessage from greetingText config field", () => {
    const theme = buildTheme(makeConfig({ greetingText: "How can I help?" }))

    expect(theme.welcomeMessage).toBe("How can I help?")
  })

  it("applies position from config", () => {
    const theme = buildTheme(makeConfig({ position: "bottom-left" }))

    expect(theme.position).toBe("bottom-left")
  })

  it("dark mode auto uses prefers-color-scheme when darkMode is undefined", () => {
    const theme = buildTheme(makeConfig({ darkMode: undefined }))

    expect(theme.darkMode).toBe("auto")
  })

  it("dark mode is 'dark' when darkMode config is true", () => {
    const theme = buildTheme(makeConfig({ darkMode: true }))

    expect(theme.darkMode).toBe("dark")
  })

  it("dark mode is 'light' when darkMode config is false", () => {
    const theme = buildTheme(makeConfig({ darkMode: false }))

    expect(theme.darkMode).toBe("light")
  })
})

// ---------------------------------------------------------------------------
// applyTheme (injectStyles)
// ---------------------------------------------------------------------------
describe("applyTheme (injectStyles)", () => {
  let container: HTMLDivElement

  beforeEach(() => {
    // Mock matchMedia so dark-mode detection works in jsdom
    Object.defineProperty(window, "matchMedia", {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    })

    container = document.createElement("div")
    document.body.appendChild(container)
  })

  afterEach(() => {
    document.body.removeChild(container)
    // Remove any injected style elements
    document.querySelectorAll("#threadly-styles").forEach((el) => el.remove())
  })

  it("sets CSS custom properties on element via <style> injection", () => {
    injectStyles(makeConfig({ accentColor: "#a855f7" }))

    const styleEl = document.getElementById("threadly-styles")
    expect(styleEl).not.toBeNull()
    const cssText = styleEl!.textContent ?? ""
    // The accent variable must reference the supplied color
    expect(cssText).toContain("#a855f7")
  })

  it("replaces existing style element on re-injection to avoid duplicates", () => {
    injectStyles(makeConfig({ accentColor: "#111111" }))
    injectStyles(makeConfig({ accentColor: "#222222" }))

    const styleEls = document.querySelectorAll("#threadly-styles")
    expect(styleEls).toHaveLength(1)
    expect(styleEls[0].textContent).toContain("#222222")
  })

  it("handles invalid hex color gracefully without throwing", () => {
    // Passing an invalid color should not throw — it just passes through to CSS
    expect(() =>
      injectStyles(makeConfig({ accentColor: "not-a-color" })),
    ).not.toThrow()

    const styleEl = document.getElementById("threadly-styles")
    expect(styleEl).not.toBeNull()
    // The invalid value ends up in the CSS verbatim — browsers ignore it safely
    expect(styleEl!.textContent).toContain("not-a-color")
  })
})
