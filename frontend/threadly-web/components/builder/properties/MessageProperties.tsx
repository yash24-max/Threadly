"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface MessagePropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

export function MessageProperties({ node, onUpdate }: MessagePropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const content = (data.content as string) ?? "";
  const MAX_CHARS = 2000;

  return (
    <div className="space-y-4">
      <div>
        <label
          htmlFor="msg-content"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Message Content
        </label>
        <textarea
          id="msg-content"
          value={content}
          onChange={(e) =>
            onUpdate({ ...data, content: e.target.value })
          }
          rows={6}
          maxLength={MAX_CHARS}
          placeholder="Hello! How can I help you today?"
          className={cn(
            "w-full px-3 py-2 text-[13px] rounded-lg resize-y",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors",
            "dark:bg-[var(--bg-surface)] font-mono"
          )}
          aria-describedby="msg-hint msg-counter"
        />
        <div className="flex items-center justify-between mt-1">
          <p id="msg-hint" className="text-[11px] text-[var(--text-muted)]">
            Use{" "}
            <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
              {`{{variable}}`}
            </code>{" "}
            to insert dynamic values
          </p>
          <span
            id="msg-counter"
            className={cn(
              "text-[11px] tabular-nums",
              content.length > MAX_CHARS * 0.9
                ? "text-[var(--warn)]"
                : "text-[var(--text-muted)]"
            )}
          >
            {content.length}/{MAX_CHARS}
          </span>
        </div>
      </div>
    </div>
  );
}
