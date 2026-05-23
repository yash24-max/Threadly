export interface NodeCatalogEntry {
  type: string;
  label: string;
  description: string;
  icon: string; // lucide icon name
  category: "Messaging" | "Logic" | "AI" | "Integration" | "Flow Control";
  color: string; // hex for node header accent
  defaultData: Record<string, unknown>;
}

export const NODE_CATALOG: NodeCatalogEntry[] = [
  // Messaging
  {
    type: "message",
    label: "Send Message",
    description: "Send text to the user",
    icon: "MessageSquare",
    category: "Messaging",
    color: "#3B82F6",
    defaultData: { content: "" },
  },
  {
    type: "question",
    label: "Ask Question",
    description: "Ask and capture reply",
    icon: "HelpCircle",
    category: "Messaging",
    color: "#3B82F6",
    defaultData: { content: "", variable: "" },
  },
  {
    type: "collect_input",
    label: "Collect Input",
    description: "Validate user input",
    icon: "FormInput",
    category: "Messaging",
    color: "#3B82F6",
    defaultData: { variable: "", validation: "none", prompt: "" },
  },
  // Logic
  {
    type: "condition",
    label: "Condition",
    description: "Branch on variable value",
    icon: "GitBranch",
    category: "Logic",
    color: "#8B5CF6",
    defaultData: { conditions: [], logicalOperator: "AND" },
  },
  {
    type: "switch",
    label: "Switch",
    description: "Multi-branch on variable",
    icon: "Shuffle",
    category: "Logic",
    color: "#8B5CF6",
    defaultData: { variable: "", cases: [] },
  },
  {
    type: "set_variable",
    label: "Set Variable",
    description: "Set session variables",
    icon: "Variable",
    category: "Logic",
    color: "#8B5CF6",
    defaultData: { assignments: [] },
  },
  {
    type: "delay",
    label: "Delay",
    description: "Wait before continuing",
    icon: "Clock",
    category: "Logic",
    color: "#8B5CF6",
    defaultData: { seconds: 5 },
  },
  // AI
  {
    type: "ai_reply",
    label: "AI Reply",
    description: "LLM response with RAG",
    icon: "Sparkles",
    category: "AI",
    color: "#EC4899",
    defaultData: {
      systemPrompt: "",
      maxTokens: 1024,
      temperature: 0.7,
      useKnowledgeBase: true,
    },
  },
  // Integration
  {
    type: "api_call",
    label: "HTTP Request",
    description: "Call external APIs",
    icon: "Globe",
    category: "Integration",
    color: "#F59E0B",
    defaultData: { method: "GET", url: "", headers: [], body: "" },
  },
  {
    type: "send_email",
    label: "Send Email",
    description: "Send an email",
    icon: "Mail",
    category: "Integration",
    color: "#F59E0B",
    defaultData: { to: "", subject: "", body: "" },
  },
  // Flow Control
  {
    type: "handoff",
    label: "Human Handoff",
    description: "Transfer to human agent",
    icon: "UserCheck",
    category: "Flow Control",
    color: "#10B981",
    defaultData: { message: "Connecting you to a human agent..." },
  },
  {
    type: "end",
    label: "End Flow",
    description: "End the conversation",
    icon: "Square",
    category: "Flow Control",
    color: "#6B7280",
    defaultData: { message: "" },
  },
];

export const CATEGORY_ORDER = [
  "Messaging",
  "Logic",
  "AI",
  "Integration",
  "Flow Control",
] as const;
