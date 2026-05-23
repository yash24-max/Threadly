"use client";

import { Plus, Trash2 } from "lucide-react";
import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface Assignment {
  variable: string;
  value: string;
}

interface SetVariablePropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const fieldCls = cn(
  "px-2 py-1.5 text-[12px] rounded-md",
  "bg-[var(--bg-surface)] border border-[var(--border)]",
  "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
  "outline-none focus:border-[var(--accent)] transition-colors"
);

export function SetVariableProperties({ node, onUpdate }: SetVariablePropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const assignments: Assignment[] = (data.assignments as Assignment[]) ?? [];

  const updateAssignments = (updated: Assignment[]) =>
    onUpdate({ ...data, assignments: updated });

  const addAssignment = () =>
    updateAssignments([...assignments, { variable: "", value: "" }]);

  const removeAssignment = (i: number) =>
    updateAssignments(assignments.filter((_, idx) => idx !== i));

  const updateAssignment = (i: number, patch: Partial<Assignment>) =>
    updateAssignments(assignments.map((a, idx) => (idx === i ? { ...a, ...patch } : a)));

  return (
    <div className="space-y-3">
      <p className="text-[12px] font-medium text-[var(--text-secondary)]">
        Assignments
      </p>

      {assignments.length === 0 && (
        <p className="text-[12px] text-[var(--text-muted)] text-center py-2">
          No assignments yet
        </p>
      )}

      {assignments.map((a, i) => (
        <div
          key={i}
          className="flex items-center gap-1.5 p-2 rounded-lg border border-[var(--border)] bg-[var(--bg-surface)]"
        >
          {/* Variable name */}
          <div className="relative flex-1 min-w-0">
            <span className="absolute left-1.5 top-1/2 -translate-y-1/2 text-[10px] font-mono text-[var(--text-muted)] pointer-events-none">
              {"{{"}
            </span>
            <input
              type="text"
              value={a.variable}
              onChange={(e) =>
                updateAssignment(i, {
                  variable: e.target.value.replace(/[^a-zA-Z0-9_.]/g, "_"),
                })
              }
              placeholder="var_name"
              className={cn(fieldCls, "w-full pl-6 pr-5 font-mono")}
              aria-label={`Variable ${i + 1} name`}
            />
            <span className="absolute right-1.5 top-1/2 -translate-y-1/2 text-[10px] font-mono text-[var(--text-muted)] pointer-events-none">
              {"}}"}
            </span>
          </div>

          <span className="text-[11px] text-[var(--text-muted)] flex-shrink-0 font-mono">=</span>

          {/* Value */}
          <input
            type="text"
            value={a.value}
            onChange={(e) => updateAssignment(i, { value: e.target.value })}
            placeholder="value or {{var}}"
            className={cn(fieldCls, "flex-1 min-w-0")}
            aria-label={`Variable ${i + 1} value`}
          />

          <button
            type="button"
            onClick={() => removeAssignment(i)}
            className="p-1 text-[var(--text-muted)] hover:text-[var(--danger)] transition-colors flex-shrink-0"
            aria-label={`Remove assignment ${i + 1}`}
          >
            <Trash2 size={13} />
          </button>
        </div>
      ))}

      <button
        type="button"
        onClick={addAssignment}
        className={cn(
          "flex items-center gap-1.5 text-[12px] px-3 py-1.5 rounded-lg w-full justify-center",
          "border border-dashed border-[var(--border)] text-[var(--text-muted)]",
          "hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors"
        )}
      >
        <Plus size={12} />
        Add Assignment
      </button>

      <p className="text-[11px] text-[var(--text-muted)]">
        Values support{" "}
        <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
          {`{{template}}`}
        </code>{" "}
        syntax
      </p>
    </div>
  );
}
