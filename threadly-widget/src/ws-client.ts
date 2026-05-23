/**
 * Centrifugo WebSocket client for the widget.
 * Handles connection lifecycle, reconnection, offline queue, and message dispatch.
 */
import { Centrifuge, type Subscription } from "centrifuge"
import type { WidgetConfig, ServerEvent } from "./types"

export type EventHandler = (event: ServerEvent) => void

const MAX_QUEUE = 50

export class WsClient {
  private centrifuge: Centrifuge
  private subscription: Subscription | null = null
  private visitorId: string
  private token: string = ""
  private offlineQueue: string[] = []
  private connected = false

  constructor(
    private config: WidgetConfig,
    private onEvent: EventHandler,
    private onConnected: () => void,
    private onDisconnected: () => void,
  ) {
    this.visitorId = this.getOrCreateVisitorId()
    this._loadQueue()

    this.centrifuge = new Centrifuge(config.centrifugoUrl, {
      getToken: () => this.fetchToken(),
    })

    this.centrifuge.on("connected", () => {
      this.connected = true
      this.onConnected()
      this.subscribe()
      this._flushQueue()
    })

    this.centrifuge.on("disconnected", () => {
      this.connected = false
      this.onDisconnected()
    })
  }

  connect() {
    this.centrifuge.connect()
  }

  disconnect() {
    this.centrifuge.disconnect()
  }

  async sendMessage(text: string): Promise<void> {
    if (!this.connected || !this.subscription) {
      // Queue for later delivery
      this._enqueue(text)
      return
    }
    try {
      await this.subscription.publish({
        type: "user_message",
        content: text,
        visitorId: this.visitorId,
      })
    } catch {
      // Centrifugo publish failed — fall back to HTTP
      try {
        await this._sendMessageHttp(text)
      } catch {
        // HTTP also failed — queue for retry when reconnected
        this._enqueue(text)
      }
    }
  }

  get visitor(): string {
    return this.visitorId
  }

  get queuedCount(): number {
    return this.offlineQueue.length
  }

  private subscribe() {
    const channel = `chat:${this.config.botId}:${this.visitorId}`
    this.subscription = this.centrifuge.newSubscription(channel)
    this.subscription.on("publication", ({ data }) => {
      this.onEvent(data as ServerEvent)
    })
    this.subscription.subscribe()
  }

  private async fetchToken(): Promise<string> {
    const res = await fetch(
      `${this.config.apiUrl}/v1/widget/token?botId=${this.config.botId}&visitorId=${this.visitorId}`,
    )
    if (!res.ok) throw new Error("Failed to fetch widget token")
    const { token } = await res.json()
    this.token = token
    return token
  }

  private async _sendMessageHttp(text: string) {
    const res = await fetch(`${this.config.apiUrl}/v1/widget/message`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        botId: this.config.botId,
        visitorId: this.visitorId,
        text,
      }),
    })
    if (!res.ok) throw new Error("HTTP fallback failed")
  }

  private _enqueue(text: string) {
    if (this.offlineQueue.length >= MAX_QUEUE) {
      this.offlineQueue.shift() // drop oldest
    }
    this.offlineQueue.push(text)
    this._saveQueue()
  }

  private async _flushQueue() {
    const queued = [...this.offlineQueue]
    this.offlineQueue = []
    this._saveQueue()
    for (const text of queued) {
      await this.sendMessage(text)
    }
  }

  private _saveQueue() {
    try {
      const key = `threadly_queue_${this.config.botId}`
      localStorage.setItem(key, JSON.stringify(this.offlineQueue))
    } catch { /* storage may be unavailable */ }
  }

  private _loadQueue() {
    try {
      const key = `threadly_queue_${this.config.botId}`
      const raw = localStorage.getItem(key)
      if (raw) this.offlineQueue = JSON.parse(raw)
    } catch { /* ignore */ }
  }

  private getOrCreateVisitorId(): string {
    const key = `threadly_vid_${this.config.botId}`
    let id = localStorage.getItem(key)
    if (!id) {
      id = crypto.randomUUID()
      localStorage.setItem(key, id)
    }
    return id
  }
}
