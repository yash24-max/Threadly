"use client";

import { MessageSquare, HelpCircle, Sparkles, GitBranch, Zap, Variable, UserCheck, Square } from "lucide-react";

const nodeList = [
  { type: "message", label: "Message", desc: "Send a text message", icon: MessageSquare, color: "var(--accent)" },
  { type: "question", label: "Question", desc: "Ask for input, save to variable", icon: HelpCircle, color: "var(--info)" },
  { type: "ai_reply", label: "AI Reply", desc: "Generate a contextual response", icon: Sparkles, color: "var(--accent)" },
  { type: "condition", label: "Condition", desc: "Branch based on variable value", icon: GitBranch, color: "var(--warn)" },
  { type: "api_call", label: "API Call", desc: "Make an HTTP request", icon: Zap, color: "var(--text-secondary)" },
  { type: "set_variable", label: "Set Variable", desc: "Assign a variable value", icon: Variable, color: "var(--text-secondary)" },
  { type: "handoff", label: "Handoff", desc: "Transfer to human agent", icon: UserCheck, color: "var(--warn)" },
  { type: "end", label: "End", desc: "Terminate the flow", icon: Square, color: "var(--danger)" },
];

export function NodePanel() {
  const onDragStart = (e: React.DragEvent, nodeType: string) => {
    e.dataTransfer.setData("application/reactflow", nodeType);
    e.dataTransfer.effectAllowed = "move";
  };

  return (
    <div style={{
      width: 220, borderRight: "1px solid var(--border)",
      background: "var(--bg-panel)", display: "flex", flexDirection: "column",
      overflow: "hidden",
    }}>
      <div style={{ padding: "14px 14px 10px", borderBottom: "1px solid var(--border)" }}>
        <p style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.5px", textTransform: "uppercase", color: "var(--text-muted)" }}>
          Nodes
        </p>
        <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>Drag to canvas</p>
      </div>
      <div style={{ flex: 1, overflow: "auto", padding: 8 }}>
        {nodeList.map(({ type, label, desc, icon: Icon, color }) => (
          <div
            key={type}
            draggable
            onDragStart={(e) => onDragStart(e, type)}
            style={{
              display: "flex", alignItems: "center", gap: 10,
              padding: "9px 10px", borderRadius: "var(--radius-md)",
              border: "1px solid var(--border)", marginBottom: 6,
              background: "var(--bg-surface)", cursor: "grab",
              userSelect: "none",
            }}
          >
            <div style={{
              width: 28, height: 28, borderRadius: "var(--radius-sm)",
              background: color + "20", display: "flex", alignItems: "center", justifyContent: "center",
              flexShrink: 0,
            }}>
              <Icon size={14} color={color} />
            </div>
            <div>
              <p style={{ fontSize: 13, fontWeight: 500 }}>{label}</p>
              <p style={{ fontSize: 11, color: "var(--text-muted)" }}>{desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
