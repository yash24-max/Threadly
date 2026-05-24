/**
 * Conversation persistence in sessionStorage.
 * Keyed by `{botId}_{visitorId}` — pass a pre-joined key for simplicity.
 * Max 200 messages stored to prevent unbounded growth.
 */
import type { ChatMessage } from "./types"

const MAX_STORED = 200

export function saveHistory(key: string, messages: ChatMessage[]): void {
  try {
    const toStore = messages.slice(-MAX_STORED)
    sessionStorage.setItem(`tly_hist_${key}`, JSON.stringify(toStore))
  } catch {
    // sessionStorage may be unavailable (private browsing, storage quota)
  }
}

export function loadHistory(key: string): ChatMessage[] {
  try {
    const raw = sessionStorage.getItem(`tly_hist_${key}`)
    if (!raw) return []
    const parsed: ChatMessage[] = JSON.parse(raw)
    // Validate shape
    if (!Array.isArray(parsed)) return []
    return parsed.filter(
      (m): m is ChatMessage =>
        typeof m === "object" &&
        m !== null &&
        typeof m.id === "string" &&
        typeof m.content === "string" &&
        typeof m.createdAt === "number"
    )
  } catch {
    return []
  }
}

export function clearHistory(key: string): void {
  try {
    sessionStorage.removeItem(`tly_hist_${key}`)
  } catch {
    // ignore
  }
}
