"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface QuestionPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

export function QuestionProperties({ node, onUpdate }: QuestionPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const content = (data.content as string) ?? "";
  const variable = (data.variable as string) ?? "";

  return (
    <div className="space-y-4">
      <div>
        <label
          htmlFor="q-content"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Question Text
        </label>
        <textarea
          id="q-content"
          value={content}
          onChange={(e) => onUpdate({ ...data, content: e.target.value })}
          rows={4}
          placeholder="What's your name?"
          className={cn(
            "w-full px-3 py-2 text-[13px] rounded-lg resize-y",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors font-mono"
          )}
        />
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Supports{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
            {`{{variable}}`}
          </code>{" "}
          interpolation
        </p>
      </div>

      <div>
        <label
          htmlFor="q-variable"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Save Answer to Variable
        </label>
        <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] text-[13px] font-mono pointer-events-none select-none">
            {"{{"}
          </span>
          <input
            id="q-variable"
            type="text"
            value={variable}
            onChange={(e) =>
              onUpdate({
                ...data,
                variable: e.target.value
                  .replace(/\s+/g, "_")
                  .replace(/[^a-zA-Z0-9_]/g, ""),
              })
            }
            placeholder="customer_name"
            className={cn(
              "w-full pl-8 pr-8 py-2 text-[13px] rounded-lg font-mono",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
              "outline-none focus:border-[var(--accent)] transition-colors"
            )}
          />
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] text-[13px] font-mono pointer-events-none select-none">
            {"}}"}
          </span>
        </div>
        {variable && (
          <p className="text-[11px] text-[var(--text-muted)] mt-1">
            User reply stored in{" "}
            <code className="text-[var(--accent)]">{`{{${variable}}}`}</code>
          </p>
        )}
      </div>
    </div>
  );
}
