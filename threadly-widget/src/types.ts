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
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant" | "system";
  content: string;
  streaming?: boolean;
  createdAt: number;
}

export interface ServerEvent {
  type:
    | "message"
    | "token"
    | "typing_start"
    | "typing_end"
    | "handoff"
    | "session_started";
  content?: string;
  conversationId?: string;
  metadata?: Record<string, unknown>;
}
