"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface HandoffPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

export function HandoffProperties({ node, onUpdate }: HandoffPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const message = (data.message as string) ?? "Connecting you to a human agent...";
  const teamId = (data.teamId as string) ?? "";

  return (
    <div className="space-y-4">
      <div>
        <label
          htmlFor="handoff-msg"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Message to Visitor
        </label>
        <textarea
          id="handoff-msg"
          value={message}
          onChange={(e) => onUpdate({ ...data, message: e.target.value })}
          rows={3}
          placeholder="Please wait while we connect you to an agent…"
          className={cn(
            "w-full px-3 py-2 text-[13px] rounded-lg resize-y font-mono text-[12px]",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors"
          )}
        />
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Shown to the visitor while waiting for an agent
        </p>
      </div>

      <div>
        <label
          htmlFor="handoff-team"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Team Assignment
        </label>
        <select
          id="handoff-team"
          value={teamId}
          onChange={(e) => onUpdate({ ...data, teamId: e.target.value })}
          className={cn(
            "w-full px-3 py-2 text-[13px] rounded-lg",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors"
          )}
          disabled
        >
          <option value="">Coming soon…</option>
        </select>
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Team routing will be available in a future update
        </p>
      </div>
    </div>
  );
}
