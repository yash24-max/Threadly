"use client";

import { Plus, Trash2 } from "lucide-react";
import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface SwitchCase {
  value: string;
  nextNode: string;
}

interface SwitchPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const fieldCls = cn(
  "px-2 py-1.5 text-[12px] rounded-md",
  "bg-[var(--bg-surface)] border border-[var(--border)]",
  "text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors"
);

export function SwitchProperties({ node, onUpdate }: SwitchPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const variable = (data.variable as string) ?? "";
  const cases: SwitchCase[] = (data.cases as SwitchCase[]) ?? [];
  const defaultBranch = (data.defaultBranch as string) ?? "";

  const updateCases = (updated: SwitchCase[]) =>
    onUpdate({ ...data, cases: updated });

  const addCase = () =>
    updateCases([...cases, { value: "", nextNode: "" }]);

  const removeCase = (i: number) =>
    updateCases(cases.filter((_, idx) => idx !== i));

  const updateCase = (i: number, patch: Partial<SwitchCase>) =>
    updateCases(cases.map((c, idx) => (idx === i ? { ...c, ...patch } : c)));

  return (
    <div className="space-y-4">
      {/* Variable */}
      <div>
        <label
          htmlFor="sw-variable"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Variable to Switch On
        </label>
        <div className="relative">
          <span className="absolute left-2 top-1/2 -translate-y-1/2 text-[12px] font-mono text-[var(--text-muted)] pointer-events-none">
            {"{{"}
          </span>
          <input
            id="sw-variable"
            type="text"
            value={variable}
            onChange={(e) =>
              onUpdate({ ...data, variable: e.target.value.replace(/[^a-zA-Z0-9_.]/g, "_") })
            }
            placeholder="intent"
            className={cn(fieldCls, "w-full pl-7 pr-6 font-mono")}
          />
          <span className="absolute right-2 top-1/2 -translate-y-1/2 text-[12px] font-mono text-[var(--text-muted)] pointer-events-none">
            {"}}"}
          </span>
        </div>
      </div>

      {/* Cases */}
      <div>
        <p className="text-[12px] font-medium text-[var(--text-secondary)] mb-2">
          Cases
        </p>
        <div className="space-y-2">
          {cases.length === 0 && (
            <p className="text-[12px] text-[var(--text-muted)] text-center py-2">
              No cases yet
            </p>
          )}
          {cases.map((c, i) => (
            <div
              key={i}
              className="flex items-center gap-1.5 p-2 rounded-lg border border-[var(--border)] bg-[var(--bg-surface)]"
            >
              <input
                type="text"
                value={c.value}
                onChange={(e) => updateCase(i, { value: e.target.value })}
                placeholder={`case ${i + 1} value`}
                className={cn(fieldCls, "flex-1 min-w-0")}
                aria-label={`Case ${i + 1} value`}
              />
              <span className="text-[11px] text-[var(--text-muted)] flex-shrink-0">→</span>
              <input
                type="text"
                value={c.nextNode}
                onChange={(e) => updateCase(i, { nextNode: e.target.value })}
                placeholder="node_id"
                className={cn(fieldCls, "w-24 flex-shrink-0 font-mono text-[11px]")}
                aria-label={`Case ${i + 1} next node`}
              />
              <button
                type="button"
                onClick={() => removeCase(i)}
                className="p-1 text-[var(--text-muted)] hover:text-[var(--danger)] transition-colors flex-shrink-0"
                aria-label={`Remove case ${i + 1}`}
              >
                <Trash2 size={13} />
              </button>
            </div>
          ))}
        </div>
        <button
          type="button"
          onClick={addCase}
          className={cn(
            "mt-2 flex items-center gap-1.5 text-[12px] px-3 py-1.5 rounded-lg w-full justify-center",
            "border border-dashed border-[var(--border)] text-[var(--text-muted)]",
            "hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors"
          )}
        >
          <Plus size={12} />
          Add Case
        </button>
      </div>

      {/* Default branch */}
      <div>
        <label
          htmlFor="sw-default"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Default Branch Node ID
        </label>
        <input
          id="sw-default"
          type="text"
          value={defaultBranch}
          onChange={(e) => onUpdate({ ...data, defaultBranch: e.target.value })}
          placeholder="node_id (fallback)"
          className={cn(fieldCls, "w-full font-mono")}
        />
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Used when no case matches
        </p>
      </div>
    </div>
  );
}
