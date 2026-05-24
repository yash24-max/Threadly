"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface SendEmailPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const fieldCls = cn(
  "w-full px-3 py-2 text-[13px] rounded-lg",
  "bg-[var(--bg-surface)] border border-[var(--border)]",
  "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
  "outline-none focus:border-[var(--accent)] transition-colors"
);

export function SendEmailProperties({ node, onUpdate }: SendEmailPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const to = (data.to as string) ?? "";
  const subject = (data.subject as string) ?? "";
  const body = (data.body as string) ?? "";

  return (
    <div className="space-y-4">
      <div>
        <label
          htmlFor="email-to"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          To
        </label>
        <input
          id="email-to"
          type="text"
          value={to}
          onChange={(e) => onUpdate({ ...data, to: e.target.value })}
          placeholder="{{user_email}} or static@example.com"
          className={fieldCls}
          aria-describedby="email-to-hint"
        />
        <p id="email-to-hint" className="text-[11px] text-[var(--text-muted)] mt-1">
          Supports{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
            {`{{variable}}`}
          </code>{" "}
          for dynamic recipients
        </p>
      </div>

      <div>
        <label
          htmlFor="email-subject"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Subject
        </label>
        <input
          id="email-subject"
          type="text"
          value={subject}
          onChange={(e) => onUpdate({ ...data, subject: e.target.value })}
          placeholder="Your order {{order_id}} is confirmed"
          className={fieldCls}
        />
      </div>

      <div>
        <label
          htmlFor="email-body"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Body
        </label>
        <textarea
          id="email-body"
          value={body}
          onChange={(e) => onUpdate({ ...data, body: e.target.value })}
          rows={6}
          placeholder={`Hi {{customer_name}},\n\nThank you for your order!\n\nBest,\nThe Team`}
          className={cn(
            "w-full px-3 py-2 text-[12px] rounded-lg resize-y font-mono",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors"
          )}
        />
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Supports template variables and plain text
        </p>
      </div>
    </div>
  );
}
