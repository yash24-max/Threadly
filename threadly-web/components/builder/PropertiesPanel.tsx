"use client";

import type { Node } from "@xyflow/react";

interface Props {
  node: Node | null;
  onChange: (id: string, data: Record<string, unknown>) => void;
}

export function PropertiesPanel({ node, onChange }: Props) {
  if (!node) {
    return (
      <div style={{
        width: 260, borderLeft: "1px solid var(--border)",
        background: "var(--bg-panel)", display: "flex",
        alignItems: "center", justifyContent: "center",
        color: "var(--text-muted)", fontSize: 13, padding: 20, textAlign: "center",
      }}>
        Click a node to configure it
      </div>
    );
  }

  const update = (key: string, value: unknown) =>
    onChange(node.id, { ...(node.data as Record<string, unknown>), [key]: value });

  const label = (text: string) => (
    <label style={{ fontSize: 12, color: "var(--text-secondary)", display: "block", marginBottom: 4 }}>
      {text}
    </label>
  );

  const input = (key: string, placeholder = "", type = "text") => (
    <input
      type={type}
      value={(node.data as any)[key] ?? ""}
      onChange={(e) => update(key, e.target.value)}
      placeholder={placeholder}
      style={{
        width: "100%", padding: "8px 10px",
        background: "var(--bg-surface)", border: "1px solid var(--border)",
        borderRadius: "var(--radius-md)", color: "var(--text-primary)",
        fontSize: 13, outline: "none", boxSizing: "border-box", marginBottom: 12,
      }}
    />
  );

  const textarea = (key: string, placeholder = "", rows = 3) => (
    <textarea
      value={(node.data as any)[key] ?? ""}
      onChange={(e) => update(key, e.target.value)}
      placeholder={placeholder}
      rows={rows}
      style={{
        width: "100%", padding: "8px 10px",
        background: "var(--bg-surface)", border: "1px solid var(--border)",
        borderRadius: "var(--radius-md)", color: "var(--text-primary)",
        fontSize: 13, outline: "none", resize: "vertical",
        boxSizing: "border-box", fontFamily: "inherit", marginBottom: 12,
      }}
    />
  );

  const toggle = (key: string, text: string) => (
    <label style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12, cursor: "pointer" }}>
      <input
        type="checkbox"
        checked={!!(node.data as any)[key]}
        onChange={(e) => update(key, e.target.checked)}
        style={{ width: 14, height: 14, accentColor: "var(--accent)" }}
      />
      <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>{text}</span>
    </label>
  );

  const sectionTitle = (text: string) => (
    <p style={{
      fontSize: 11, fontWeight: 600, letterSpacing: "0.5px",
      textTransform: "uppercase", color: "var(--text-muted)", marginBottom: 10,
    }}>
      {text}
    </p>
  );

  return (
    <div style={{
      width: 260, borderLeft: "1px solid var(--border)",
      background: "var(--bg-panel)", overflow: "auto",
      display: "flex", flexDirection: "column",
    }}>
      <div style={{ padding: "12px 14px", borderBottom: "1px solid var(--border)" }}>
        <p style={{ fontSize: 12, color: "var(--text-muted)", textTransform: "uppercase", letterSpacing: "0.5px", fontWeight: 600 }}>
          {node.type?.replace("_", " ")}
        </p>
        <p style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 2 }}>ID: {node.id}</p>
      </div>

      <div style={{ padding: 14, flex: 1 }}>
        {node.type === "message" && (
          <>
            {sectionTitle("Message")}
            {label("Text (supports {{variables}})")}
            {textarea("text", "Hello! How can I help you today?", 4)}
          </>
        )}

        {node.type === "question" && (
          <>
            {sectionTitle("Question")}
            {label("Question text")}
            {textarea("text", "What's your name?", 3)}
            {label("Save answer to variable")}
            {input("variableName", "customer_name")}
            {label("Placeholder text")}
            {input("placeholder", "Type your answer…")}
          </>
        )}

        {node.type === "ai_reply" && (
          <>
            {sectionTitle("AI Reply")}
            {label("System prompt")}
            {textarea("systemPrompt", "You are a helpful assistant for {{org.name}}.", 5)}
            {label("Max tokens")}
            {input("maxTokens", "500", "number")}
            {toggle("useKb", "Use Knowledge Base (RAG)")}
            {toggle("streamTokens", "Stream tokens in real-time")}
            {label("LLM Provider")}
            <select
              value={(node.data as any).provider ?? "auto"}
              onChange={(e) => update("provider", e.target.value)}
              style={{
                width: "100%", padding: "8px 10px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 13, outline: "none", marginBottom: 12,
              }}
            >
              <option value="auto">Auto (Anthropic → OpenAI)</option>
              <option value="anthropic">Anthropic Claude</option>
              <option value="openai">OpenAI GPT</option>
            </select>
          </>
        )}

        {node.type === "condition" && (
          <>
            {sectionTitle("Condition")}
            {label("Variable")}
            {input("variable", "customer_name")}
            {label("Operator")}
            <select
              value={(node.data as any).operator ?? "equals"}
              onChange={(e) => update("operator", e.target.value)}
              style={{
                width: "100%", padding: "8px 10px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 13, outline: "none", marginBottom: 12,
              }}
            >
              <option value="equals">Equals</option>
              <option value="contains">Contains</option>
              <option value="not_equals">Not equals</option>
              <option value="greater_than">Greater than</option>
              <option value="less_than">Less than</option>
            </select>
            {label("Value")}
            {input("value", "yes")}
            <p style={{ fontSize: 11, color: "var(--text-muted)" }}>
              True → bottom-left handle · False → bottom-right handle
            </p>
          </>
        )}

        {node.type === "api_call" && (
          <>
            {sectionTitle("API Call")}
            {label("Method")}
            <select
              value={(node.data as any).method ?? "GET"}
              onChange={(e) => update("method", e.target.value)}
              style={{
                width: "100%", padding: "8px 10px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 13, outline: "none", marginBottom: 12,
              }}
            >
              <option>GET</option>
              <option>POST</option>
              <option>PATCH</option>
              <option>DELETE</option>
            </select>
            {label("URL")}
            {input("url", "https://api.example.com/data")}
            {label("Headers (JSON)")}
            {textarea("headers", '{"Authorization": "Bearer {{api_key}}"}', 2)}
            {label("Body (JSON, for POST/PATCH)")}
            {textarea("body", '{"name": "{{customer_name}}"}', 3)}
            {label("Save response to variable")}
            {input("responseVariable", "api_response")}
          </>
        )}

        {node.type === "set_variable" && (
          <>
            {sectionTitle("Set Variable")}
            {label("Variable name")}
            {input("variableName", "my_variable")}
            {label("Value")}
            {input("value", "hello or {{other_variable}}")}
          </>
        )}

        {node.type === "handoff" && (
          <>
            {sectionTitle("Handoff")}
            {label("Message to visitor")}
            {textarea("message", "Connecting you to an agent…", 2)}
          </>
        )}

        {(node.type === "start" || node.type === "end") && (
          <p style={{ fontSize: 13, color: "var(--text-muted)" }}>
            No configuration needed for this node.
          </p>
        )}
      </div>
    </div>
  );
}
