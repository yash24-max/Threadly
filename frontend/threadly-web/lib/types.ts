export interface Org {
  id: string;
  name: string;
  slug: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
  role: "ADMIN" | "MEMBER" | "AGENT";
  org: Org;
}

export interface Bot {
  id: string;
  orgId: string;
  name: string;
  description?: string;
  systemPrompt: string;
  accentColor: string;
  avatarUrl?: string;
  greetingText: string;
  widgetPosition: "BOTTOM_RIGHT" | "BOTTOM_LEFT";
  status: "ACTIVE" | "INACTIVE";
  theme?: Record<string, unknown> | string;
  createdAt: string;
  updatedAt: string;
}

export interface FlowNode {
  id: string;
  type:
    | "start"
    | "message"
    | "question"
    | "condition"
    | "ai_reply"
    | "api_call"
    | "set_variable"
    | "handoff"
    | "end";
  position: { x: number; y: number };
  data: Record<string, unknown>;
}

export interface FlowEdge {
  id: string;
  source: string;
  target: string;
  sourceHandle?: string;
  targetHandle?: string;
  label?: string;
}

export interface FlowDefinition {
  nodes: FlowNode[];
  edges: FlowEdge[];
}

export interface Flow {
  id: string;
  botId: string;
  status: "DRAFT" | "PUBLISHED";
  definition: FlowDefinition;
  updatedAt: string;
}

export interface FlowVersion {
  id: string;
  flowId: string;
  version: number;
  definition: FlowDefinition;
  publishedAt: string;
}

export interface Conversation {
  id: string;
  botId: string;
  orgId: string;
  visitorId: string;
  status: "OPEN" | "CLOSED" | "HANDED_OFF";
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  lastMessage?: string;
}

export interface Message {
  id: string;
  conversationId: string;
  role: "user" | "assistant" | "system";
  content: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

export interface KbDocument {
  id: string;
  botId: string;
  orgId: string;
  name: string;
  docType: "pdf" | "txt" | "url";
  sourceUrl?: string;
  status: "PENDING" | "PROCESSING" | "READY" | "FAILED";
  chunkCount?: number;
  createdAt: string;
}

export interface DashboardStats {
  totalConversations: number;
  openConversations: number;
  handoffConversations: number;
  p50ResponseMs: number;
  totalMessages: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}
