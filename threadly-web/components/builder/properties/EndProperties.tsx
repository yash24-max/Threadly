"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface EndPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

export function EndProperties({ node, onUpdate }: EndPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const message = (data.message as string) ?? "";
  const showRating = (data.showRating as boolean) ?? false;

  return (
    <div className="space-y-4">
      <div>
        <label
          htmlFor="end-message"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Final Message (optional)
        </label>
        <textarea
          id="end-message"
          value={message}
          onChange={(e) => onUpdate({ ...data, message: e.target.value })}
          rows={3}
          placeholder="Thanks for chatting! Have a great day."
          className={cn(
            "w-full px-3 py-2 text-[12px] rounded-lg resize-y font-mono",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors"
          )}
        />
      </div>

      <div className="flex items-center justify-between py-2 px-3 rounded-lg bg-[var(--bg-surface)] border border-[var(--border)]">
        <div>
          <p className="text-[13px] font-medium text-[var(--text-primary)]">
            Satisfaction Rating
          </p>
          <p className="text-[11px] text-[var(--text-muted)]">
            Ask visitor to rate the conversation
          </p>
        </div>
        <button
          type="button"
          role="switch"
          aria-checked={showRating}
          onClick={() => onUpdate({ ...data, showRating: !showRating })}
          className={cn(
            "relative inline-flex h-5 w-9 items-center rounded-full transition-colors",
            showRating ? "bg-[var(--accent)]" : "bg-[var(--border)]"
          )}
        >
          <span
            className={cn(
              "inline-block h-3.5 w-3.5 rounded-full bg-white shadow transition-transform",
              showRating ? "translate-x-[18px]" : "translate-x-[2px]"
            )}
          />
        </button>
      </div>
    </div>
  );
}
