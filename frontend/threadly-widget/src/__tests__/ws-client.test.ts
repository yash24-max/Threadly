import { describe, it, expect, vi, beforeEach } from "vitest"
import type { WidgetConfig, ServerEvent } from "../types"

// ---------------------------------------------------------------------------
// Mock the centrifuge library before importing WsClient so the real
// WebSocket implementation is never instantiated in the test environment.
// ---------------------------------------------------------------------------
const mockConnect = vi.fn()
const mockDisconnect = vi.fn()
const mockPublish = vi.fn().mockResolvedValue(undefined)
const mockSubscribe = vi.fn().mockReturnValue({
  on: vi.fn().mockReturnThis(),
  subscribe: vi.fn(),
  publish: mockPublish,
  unsubscribe: vi.fn(),
})
const mockCentrifugeOn = vi.fn().mockReturnThis()

vi.mock("centrifuge", () => ({
  Centrifuge: vi.fn().mockImplementation(() => ({
    connect: mockConnect,
    disconnect: mockDisconnect,
    subscribe: mockSubscribe,
    on: mockCentrifugeOn,
  })),
}))

// Import after mock so the mocked module is used
import { WsClient } from "../ws-client"

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function makeConfig(overrides: Partial<WidgetConfig> = {}): WidgetConfig {
  return {
    botId: "bot-ws-001",
    apiUrl: "https://api.threadly.dev",
    centrifugoUrl: "wss://rt.threadly.dev/connection/websocket",
    ...overrides,
  }
}

const noopEventHandler = vi.fn<[ServerEvent], void>()
const noopConnected = vi.fn()
const noopDisconnected = vi.fn()

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------
describe("WsClient", () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset localStorage/sessionStorage entries written by visitorId logic
    sessionStorage.clear()
    localStorage.clear()
  })

  it("initializes without connecting immediately", () => {
    new WsClient(makeConfig(), noopEventHandler, noopConnected, noopDisconnected)

    // connect() must NOT have been called in the constructor
    expect(mockConnect).not.toHaveBeenCalled()
  })

  it("messageQueue buffers messages while disconnected", async () => {
    const client = new WsClient(
      makeConfig(),
      noopEventHandler,
      noopConnected,
      noopDisconnected,
    )

    // Send messages before connecting — they should not throw
    await client.sendMessage("buffered message 1")
    await client.sendMessage("buffered message 2")

    // The underlying centrifuge publish must NOT be called yet (client is offline)
    expect(mockPublish).not.toHaveBeenCalled()
  })

  it("connect calls centrifuge.connect()", () => {
    const client = new WsClient(
      makeConfig(),
      noopEventHandler,
      noopConnected,
      noopDisconnected,
    )

    client.connect()

    expect(mockConnect).toHaveBeenCalledTimes(1)
  })

  it("disconnect calls centrifuge.disconnect()", () => {
    const client = new WsClient(
      makeConfig(),
      noopEventHandler,
      noopConnected,
      noopDisconnected,
    )

    client.connect()
    client.disconnect()

    expect(mockDisconnect).toHaveBeenCalledTimes(1)
  })

  it("exposes a stable visitorId after construction", () => {
    const client = new WsClient(
      makeConfig(),
      noopEventHandler,
      noopConnected,
      noopDisconnected,
    )

    const visitorId = client.visitor

    expect(typeof visitorId).toBe("string")
    expect(visitorId.length).toBeGreaterThan(0)
  })

  it("reuses the same visitorId across instances for the same bot", () => {
    const config = makeConfig()
    const clientA = new WsClient(config, noopEventHandler, noopConnected, noopDisconnected)
    const clientB = new WsClient(config, noopEventHandler, noopConnected, noopDisconnected)

    // Both instances share storage — they should resolve to the same visitorId
    expect(clientA.visitor).toBe(clientB.visitor)
  })

  it("registers 'connected' and 'disconnected' event handlers on the centrifuge instance", () => {
    new WsClient(makeConfig(), noopEventHandler, noopConnected, noopDisconnected)

    // The constructor must attach lifecycle handlers
    const registeredEvents = mockCentrifugeOn.mock.calls.map((call) => call[0] as string)
    expect(registeredEvents).toContain("connected")
    expect(registeredEvents).toContain("disconnected")
  })
})
