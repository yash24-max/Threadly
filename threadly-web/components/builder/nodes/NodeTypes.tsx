"use client";

import { Handle, Position, type NodeProps } from "@xyflow/react";

const baseStyle: React.CSSProperties = {
  background: "var(--bg-panel)",
  border: "1px solid var(--border)",
  borderRadius: "var(--radius-md)",
  padding: "10px 14px",
  minWidth: 180,
  fontSize: 13,
  color: "var(--text-primary)",
  boxShadow: "var(--shadow-1)",
  position: "relative",
}

/** Red dot badge shown when a node has missing required fields */
function ErrorBadge() {
  return (
    <span
      title="Missing required fields"
      style={{
        position: "absolute",
        top: -5,
        right: -5,
        width: 10,
        height: 10,
        borderRadius: "50%",
        background: "var(--danger)",
        border: "2px solid var(--bg-panel)",
      }}
    />
  )
}

const labelStyle: React.CSSProperties = {
  fontSize: 10,
  fontWeight: 600,
  letterSpacing: "0.5px",
  textTransform: "uppercase",
  marginBottom: 4,
};

const handleStyle: React.CSSProperties = {
  width: 10, height: 10,
  background: "var(--bg-surface)",
  border: "2px solid var(--border)",
};

export function StartNode({ data }: NodeProps) {
  return (
    <div style={{ ...baseStyle, borderColor: "var(--success)", borderWidth: 2 }}>
      <p style={{ ...labelStyle, color: "var(--success)" }}>Start</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>Flow begins here</p>
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function MessageNode({ data }: NodeProps) {
  const d = data as any
  return (
    <div style={baseStyle}>
      {d._hasError && <ErrorBadge />}
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--accent)" }}>Message</p>
      <p style={{ fontSize: 13, lineHeight: 1.4 }}>
        {d.content || d.text || <span style={{ color: "var(--text-muted)" }}>Enter message…</span>}
      </p>
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  )
}

export function QuestionNode({ data }: NodeProps) {
  const d = data as any
  return (
    <div style={{ ...baseStyle, borderColor: "var(--info)" }}>
      {d._hasError && <ErrorBadge />}
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--info)" }}>Question</p>
      <p style={{ fontSize: 13, lineHeight: 1.4 }}>
        {(data as any).text || <span style={{ color: "var(--text-muted)" }}>Ask a question…</span>}
      </p>
      {(data as any).variableName && (
        <p style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 4 }}>
          Saves to: <code style={{ color: "var(--info)" }}>{`{{${(data as any).variableName}}}`}</code>
        </p>
      )}
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function AiReplyNode({ data }: NodeProps) {
  const d = data as any
  return (
    <div style={{ ...baseStyle, borderColor: "var(--accent)", background: "rgba(79,70,229,0.06)" }}>
      {d._hasError && <ErrorBadge />}
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--accent)" }}>AI Reply</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)", lineHeight: 1.4 }}>
        {(data as any).systemPrompt
          ? `"${String((data as any).systemPrompt).slice(0, 60)}…"`
          : <span style={{ color: "var(--text-muted)" }}>Configure prompt…</span>
        }
      </p>
      {(data as any).useKb && (
        <span style={{
          display: "inline-block", marginTop: 6, fontSize: 10, fontWeight: 600,
          padding: "2px 7px", borderRadius: "var(--radius-full)",
          background: "rgba(79,70,229,0.15)", color: "var(--accent)",
        }}>
          KB
        </span>
      )}
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function ConditionNode({ data }: NodeProps) {
  return (
    <div style={{ ...baseStyle, borderColor: "var(--warn)" }}>
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--warn)" }}>Condition</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>
        {(data as any).variable
          ? `if {{${(data as any).variable}}} ${(data as any).operator} "${(data as any).value}"`
          : "Configure condition…"
        }
      </p>
      <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6 }}>
        <Handle
          type="source"
          position={Position.Bottom}
          id="true"
          style={{ ...handleStyle, left: "30%", background: "var(--success)" }}
        />
        <Handle
          type="source"
          position={Position.Bottom}
          id="false"
          style={{ ...handleStyle, left: "70%", background: "var(--danger)" }}
        />
      </div>
      <div style={{ display: "flex", justifyContent: "space-around", marginTop: 4, fontSize: 10, color: "var(--text-muted)" }}>
        <span>Yes</span>
        <span>No</span>
      </div>
    </div>
  );
}

export function ApiCallNode({ data }: NodeProps) {
  const d = data as any
  return (
    <div style={baseStyle}>
      {d._hasError && <ErrorBadge />}
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--text-secondary)" }}>API Call</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>
        {(data as any).method && (data as any).url
          ? `${(data as any).method} ${String((data as any).url).slice(0, 40)}`
          : "Configure request…"
        }
      </p>
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function SetVariableNode({ data }: NodeProps) {
  return (
    <div style={baseStyle}>
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--text-secondary)" }}>Set Variable</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>
        {(data as any).variableName
          ? `{{${(data as any).variableName}}} = ${(data as any).value}`
          : "Configure variable…"
        }
      </p>
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function HandoffNode({ data }: NodeProps) {
  return (
    <div style={{ ...baseStyle, borderColor: "var(--warn)" }}>
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--warn)" }}>Handoff</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>Transfer to human agent</p>
      <Handle type="source" position={Position.Bottom} style={handleStyle} />
    </div>
  );
}

export function EndNode({ data }: NodeProps) {
  return (
    <div style={{ ...baseStyle, borderColor: "var(--danger)", borderWidth: 2 }}>
      <Handle type="target" position={Position.Top} style={handleStyle} />
      <p style={{ ...labelStyle, color: "var(--danger)" }}>End</p>
      <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>Flow ends here</p>
    </div>
  );
}

export const nodeTypes = {
  start: StartNode,
  message: MessageNode,
  question: QuestionNode,
  ai_reply: AiReplyNode,
  condition: ConditionNode,
  api_call: ApiCallNode,
  set_variable: SetVariableNode,
  handoff: HandoffNode,
  end: EndNode,
};
