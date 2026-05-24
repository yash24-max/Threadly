"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

const VALIDATION_TYPES = [
  { value: "none", label: "None" },
  { value: "email", label: "Email address" },
  { value: "phone", label: "Phone number" },
  { value: "number", label: "Number" },
  { value: "regex", label: "Custom regex" },
] as const;

type ValidationType = (typeof VALIDATION_TYPES)[number]["value"];

interface CollectInputPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const fieldCls = cn(
  "px-3 py-2 text-[13px] rounded-lg",
  "bg-[var(--bg-surface)] border border-[var(--border)]",
  "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
  "outline-none focus:border-[var(--accent)] transition-colors"
);

export function CollectInputProperties({ node, onUpdate }: CollectInputPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const prompt = (data.prompt as string) ?? "";
  const variable = (data.variable as string) ?? "";
  const validation = (data.validation as ValidationType) ?? "none";
  const regexPattern = (data.regexPattern as string) ?? "";
  const errorMessage = (data.errorMessage as string) ?? "";

  return (
    <div className="space-y-4">
      {/* Prompt */}
      <div>
        <label
          htmlFor="ci-prompt"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Prompt
        </label>
        <textarea
          id="ci-prompt"
          value={prompt}
          onChange={(e) => onUpdate({ ...data, prompt: e.target.value })}
          rows={3}
          placeholder="Please enter your email address:"
          className={cn(fieldCls, "w-full resize-y font-mono text-[12px]")}
        />
      </div>

      {/* Variable */}
      <div>
        <label
          htmlFor="ci-variable"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Save to Variable
        </label>
        <div className="relative">
          <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-[12px] font-mono text-[var(--text-muted)] pointer-events-none">
            {"{{"}
          </span>
          <input
            id="ci-variable"
            type="text"
            value={variable}
            onChange={(e) =>
              onUpdate({ ...data, variable: e.target.value.replace(/[^a-zA-Z0-9_]/g, "_") })
            }
            placeholder="user_email"
            className={cn(fieldCls, "w-full pl-8 pr-7 font-mono")}
          />
          <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[12px] font-mono text-[var(--text-muted)] pointer-events-none">
            {"}}"}
          </span>
        </div>
      </div>

      {/* Validation type */}
      <div>
        <label
          htmlFor="ci-validation"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Validation
        </label>
        <select
          id="ci-validation"
          value={validation}
          onChange={(e) =>
            onUpdate({ ...data, validation: e.target.value as ValidationType })
          }
          className={cn(fieldCls, "w-full")}
        >
          {VALIDATION_TYPES.map((v) => (
            <option key={v.value} value={v.value}>
              {v.label}
            </option>
          ))}
        </select>
      </div>

      {/* Regex pattern - only when regex selected */}
      {validation === "regex" && (
        <div>
          <label
            htmlFor="ci-regex"
            className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
          >
            Regex Pattern
          </label>
          <input
            id="ci-regex"
            type="text"
            value={regexPattern}
            onChange={(e) => onUpdate({ ...data, regexPattern: e.target.value })}
            placeholder="^[A-Z]{3}-\d{4}$"
            className={cn(fieldCls, "w-full font-mono")}
          />
        </div>
      )}

      {/* Error message */}
      {validation !== "none" && (
        <div>
          <label
            htmlFor="ci-error"
            className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
          >
            Validation Error Message
          </label>
          <textarea
            id="ci-error"
            value={errorMessage}
            onChange={(e) => onUpdate({ ...data, errorMessage: e.target.value })}
            rows={2}
            placeholder="That doesn't look right. Please try again."
            className={cn(fieldCls, "w-full resize-none text-[12px]")}
          />
        </div>
      )}
    </div>
  );
}
