export interface WidgetConfig {
  botId: string
  apiUrl: string
  centrifugoUrl: string
  accentColor?: string
  avatarUrl?: string
  greetingText?: string
  position?: "bottom-right" | "bottom-left"
  botName?: string
  /** true = always dark, false = always light, undefined = follow prefers-color-scheme */
  darkMode?: boolean
  sound?: boolean
}

// ---------------------------------------------------------------------------
// Rich message content types
// ---------------------------------------------------------------------------

export type ButtonStyle = "primary" | "secondary"

export interface ButtonItem {
  label: string
  value: string
  style?: ButtonStyle
}

export interface CardButton {
  label: string
  url?: string
  value?: string
}

export type MessageContent =
  | { type: "text"; text: string }
  | {
      type: "buttons"
      text: string
      buttons: Array<{ label: string; value: string; style?: ButtonStyle }>
    }
  | {
      type: "card"
      title: string
      subtitle?: string
      imageUrl?: string
      buttons?: CardButton[]
    }
  | {
      type: "quick_replies"
      text: string
      replies: Array<{ label: string; value: string }>
      dismissed?: boolean
    }
  | {
      type: "file"
      url: string
      filename: string
      size: number
      mimeType: string
    }

// ---------------------------------------------------------------------------
// Chat message
// ---------------------------------------------------------------------------

export type MessageStatus = "sending" | "sent" | "delivered"

export interface ChatMessage {
  id: string
  role: "user" | "assistant" | "system"
  /** Legacy plain text. If content is provided it takes precedence. */
  content: string
  richContent?: MessageContent
  streaming?: boolean
  createdAt: number
  status?: MessageStatus
}

// ---------------------------------------------------------------------------
// WebSocket server events
// ---------------------------------------------------------------------------

export interface ServerEvent {
  type:
    | "message"
    | "token"
    | "typing_start"
    | "typing_end"
    | "handoff"
    | "session_started"
    | "delivered"
  content?: string
  richContent?: MessageContent
  conversationId?: string
  messageId?: string
  metadata?: Record<string, unknown>
}
