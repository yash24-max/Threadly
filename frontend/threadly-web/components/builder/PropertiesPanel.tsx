"use client";

import { useCallback, useRef } from "react";
import type { Node } from "@xyflow/react";
import { Settings2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { MessageProperties } from "./properties/MessageProperties";
import { QuestionProperties } from "./properties/QuestionProperties";
import { AiReplyProperties } from "./properties/AiReplyProperties";
import { ConditionProperties } from "./properties/ConditionProperties";
import { SwitchProperties } from "./properties/SwitchProperties";
import { ApiCallProperties } from "./properties/ApiCallProperties";
import { DelayProperties } from "./properties/DelayProperties";
import { CollectInputProperties } from "./properties/CollectInputProperties";
import { SendEmailProperties } from "./properties/SendEmailProperties";
import { SetVariableProperties } from "./properties/SetVariableProperties";
import { HandoffProperties } from "./properties/HandoffProperties";
import { EndProperties } from "./properties/EndProperties";

interface PropertiesPanelProps {
  selectedNode: Node | null;
  onUpdateNode: (nodeId: string, data: Record<string, unknown>) => void;
}

const NODE_TYPE_LABELS: Record<string, string> = {
  message: "Send Message",
  question: "Ask Question",
  collect_input: "Collect Input",
  condition: "Condition",
  switch: "Switch",
  set_variable: "Set Variable",
  delay: "Delay",
  ai_reply: "AI Reply",
  api_call: "HTTP Request",
  send_email: "Send Email",
  handoff: "Human Handoff",
  end: "End Flow",
  start: "Start",
};

const NODE_TYPE_COLORS: Record<string, string> = {
  message: "#3B82F6",
  question: "#3B82F6",
  collect_input: "#3B82F6",
  condition: "#8B5CF6",
  switch: "#8B5CF6",
  set_variable: "#8B5CF6",
  delay: "#8B5CF6",
  ai_reply: "#EC4899",
  api_call: "#F59E0B",
  send_email: "#F59E0B",
  handoff: "#10B981",
  end: "#6B7280",
  start: "#10B981",
};

export function PropertiesPanel({ selectedNode, onUpdateNode }: PropertiesPanelProps) {
  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleUpdate = useCallback(
    (data: Record<string, unknown>) => {
      if (!selectedNode) return;
      // Debounce 300ms to batch rapid changes and trigger autosave
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
      debounceTimer.current = setTimeout(() => {
        onUpdateNode(selectedNode.id, data);
      }, 300);
    },
    [selectedNode, onUpdateNode]
  );

  if (!selectedNode) {
    return (
      <div
        className={cn(
          "w-[260px] flex-shrink-0",
          "border-l border-[var(--border)] bg-[var(--bg-panel)]",
          "flex flex-col items-center justify-center",
          "text-[var(--text-muted)] p-6 text-center"
        )}
        aria-label="Properties panel"
      >
        <Settings2
          size={32}
          className="mb-3 opacity-20"
          aria-hidden="true"
        />
        <p className="text-[13px] font-medium text-[var(--text-muted)]">
          No node selected
        </p>
        <p className="text-[11px] text-[var(--text-muted)] mt-1 leading-relaxed">
          Click a node on the canvas to configure its properties
        </p>
      </div>
    );
  }

  const nodeType = selectedNode.type ?? "";
  const label = NODE_TYPE_LABELS[nodeType] ?? nodeType;
  const accentColor = NODE_TYPE_COLORS[nodeType] ?? "#6B7280";

  const renderProperties = () => {
    switch (nodeType) {
      case "message":
        return (
          <MessageProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "question":
        return (
          <QuestionProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "collect_input":
        return (
          <CollectInputProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "condition":
        return (
          <ConditionProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "switch":
        return (
          <SwitchProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "set_variable":
        return (
          <SetVariableProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "delay":
        return (
          <DelayProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "ai_reply":
        return (
          <AiReplyProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "api_call":
        return (
          <ApiCallProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "send_email":
        return (
          <SendEmailProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "handoff":
        return (
          <HandoffProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "end":
        return (
          <EndProperties
            node={selectedNode}
            onUpdate={handleUpdate}
          />
        );
      case "start":
        return (
          <div className="text-[12px] text-[var(--text-muted)] py-4 text-center">
            <p>The start node has no configurable properties.</p>
            <p className="mt-1 text-[11px]">Connect it to the first node in your flow.</p>
          </div>
        );
      default:
        return (
          <p className="text-[12px] text-[var(--text-muted)] py-4 text-center">
            No properties available for this node type.
          </p>
        );
    }
  };

  return (
    <div
      className={cn(
        "w-[260px] flex-shrink-0",
        "border-l border-[var(--border)] bg-[var(--bg-panel)]",
        "flex flex-col overflow-hidden"
      )}
      aria-label="Node properties"
    >
      {/* Header */}
      <div
        className="px-4 py-3 border-b border-[var(--border)]"
        style={{ borderTopColor: accentColor, borderTopWidth: 2 }}
      >
        <p
          className="text-[11px] font-semibold uppercase tracking-wide"
          style={{ color: accentColor }}
        >
          {label}
        </p>
        <p className="text-[11px] text-[var(--text-muted)] mt-0.5 font-mono truncate">
          {selectedNode.id}
        </p>
      </div>

      {/* Properties form */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {renderProperties()}
      </div>
    </div>
  );
}
