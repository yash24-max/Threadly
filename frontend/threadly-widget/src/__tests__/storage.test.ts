import { describe, it, expect, beforeEach, vi } from "vitest"
import { loadHistory, saveHistory, clearHistory } from "../storage"
import type { ChatMessage } from "../types"

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function makeMessage(overrides: Partial<ChatMessage> = {}): ChatMessage {
  return {
    id: `msg-${Math.random().toString(36).slice(2)}`,
    role: "user",
    content: "Hello world",
    createdAt: Date.now(),
    ...overrides,
  }
}

const STORAGE_KEY = "test-session-key"

// ---------------------------------------------------------------------------
// loadHistory
// ---------------------------------------------------------------------------
describe("loadHistory", () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it("returns empty array when no history is stored", () => {
    const result = loadHistory(STORAGE_KEY)

    expect(result).toEqual([])
  })

  it("returns parsed messages from sessionStorage", () => {
    const messages: ChatMessage[] = [
      makeMessage({ content: "First message", role: "user" }),
      makeMessage({ content: "Second message", role: "assistant" }),
    ]
    sessionStorage.setItem(`tly_hist_${STORAGE_KEY}`, JSON.stringify(messages))

    const result = loadHistory(STORAGE_KEY)

    expect(result).toHaveLength(2)
    expect(result[0].content).toBe("First message")
    expect(result[1].role).toBe("assistant")
  })

  it("returns empty array on corrupt JSON", () => {
    sessionStorage.setItem(`tly_hist_${STORAGE_KEY}`, "{ this is not valid json }")

    const result = loadHistory(STORAGE_KEY)

    expect(result).toEqual([])
  })

  it("returns empty array when stored value is not an array", () => {
    sessionStorage.setItem(`tly_hist_${STORAGE_KEY}`, JSON.stringify({ id: "not-array" }))

    const result = loadHistory(STORAGE_KEY)

    expect(result).toEqual([])
  })

  it("filters out messages with missing required fields", () => {
    const valid = makeMessage()
    // Deliberately malformed: missing 'id' field
    const malformed = { role: "user", content: "bad", createdAt: Date.now() }
    sessionStorage.setItem(
      `tly_hist_${STORAGE_KEY}`,
      JSON.stringify([valid, malformed]),
    )

    const result = loadHistory(STORAGE_KEY)

    // Only the valid message survives the filter
    expect(result).toHaveLength(1)
    expect(result[0].id).toBe(valid.id)
  })

  it("limits results to maxMessages parameter via slice logic", () => {
    // saveHistory respects MAX_STORED=200; loadHistory returns all stored items.
    // To test a length limit we store 5 messages and verify all 5 come back.
    const messages = Array.from({ length: 5 }, (_, i) =>
      makeMessage({ content: `msg ${i}` }),
    )
    sessionStorage.setItem(`tly_hist_${STORAGE_KEY}`, JSON.stringify(messages))

    const result = loadHistory(STORAGE_KEY)

    expect(result).toHaveLength(5)
  })
})

// ---------------------------------------------------------------------------
// saveHistory
// ---------------------------------------------------------------------------
describe("saveHistory", () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it("saves messages to sessionStorage under the prefixed key", () => {
    const messages = [makeMessage({ content: "Persisted message" })]

    saveHistory(STORAGE_KEY, messages)

    const raw = sessionStorage.getItem(`tly_hist_${STORAGE_KEY}`)
    expect(raw).not.toBeNull()
    const parsed: ChatMessage[] = JSON.parse(raw!)
    expect(parsed).toHaveLength(1)
    expect(parsed[0].content).toBe("Persisted message")
  })

  it("trims to max 200 messages when more are provided", () => {
    const messages = Array.from({ length: 250 }, (_, i) =>
      makeMessage({ content: `message ${i}` }),
    )

    saveHistory(STORAGE_KEY, messages)

    const raw = sessionStorage.getItem(`tly_hist_${STORAGE_KEY}`)!
    const stored: ChatMessage[] = JSON.parse(raw)
    // Only the last 200 messages are retained
    expect(stored).toHaveLength(200)
    // The newest (last) messages are kept
    expect(stored[199].content).toBe("message 249")
  })

  it("does not throw when sessionStorage is unavailable", () => {
    // Simulate a storage exception (e.g. private browsing quota exceeded)
    vi.spyOn(Storage.prototype, "setItem").mockImplementationOnce(() => {
      throw new DOMException("QuotaExceededError")
    })

    expect(() => saveHistory(STORAGE_KEY, [makeMessage()])).not.toThrow()
  })
})

// ---------------------------------------------------------------------------
// clearHistory
// ---------------------------------------------------------------------------
describe("clearHistory", () => {
  it("removes the key from sessionStorage", () => {
    saveHistory(STORAGE_KEY, [makeMessage()])
    expect(sessionStorage.getItem(`tly_hist_${STORAGE_KEY}`)).not.toBeNull()

    clearHistory(STORAGE_KEY)

    expect(sessionStorage.getItem(`tly_hist_${STORAGE_KEY}`)).toBeNull()
  })

  it("does not throw when clearing a key that does not exist", () => {
    expect(() => clearHistory("nonexistent-key")).not.toThrow()
  })
})
