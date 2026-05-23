"use client";

import { Plus, Trash2 } from "lucide-react";
import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

const OPERATORS = [
  { value: "eq", label: "equals" },
  { value: "neq", label: "not equals" },
  { value: "gt", label: "greater than" },
  { value: "gte", label: "≥" },
  { value: "lt", label: "less than" },
  { value: "lte", label: "≤" },
  { value: "contains", label: "contains" },
  { value: "not_contains", label: "not contains" },
  { value: "starts_with", label: "starts with" },
  { value: "ends_with", label: "ends with" },
  { value: "empty", label: "is empty" },
  { value: "not_empty", label: "is not empty" },
] as const;

type Operator = (typeof OPERATORS)[number]["value"];

interface Condition {
  variable: string;
  operator: Operator;
  value: string;
}

interface ConditionPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const VALUE_HIDDEN_OPS: Operator[] = ["empty", "not_empty"];

export function ConditionProperties({ node, onUpdate }: ConditionPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const conditions: Condition[] = (data.conditions as Condition[]) ?? [];
  const logicalOperator = (data.logicalOperator as string) ?? "AND";
  const trueBranch = (data.trueBranch as string) ?? "";
  const falseBranch = (data.falseBranch as string) ?? "";

  const updateConditions = (updated: Condition[]) =>
    onUpdate({ ...data, conditions: updated });

  const addCondition = () =>
    updateConditions([...conditions, { variable: "", operator: "eq", value: "" }]);

  const removeCondition = (i: number) =>
    updateConditions(conditions.filter((_, idx) => idx !== i));

  const updateCondition = (i: number, patch: Partial<Condition>) =>
    updateConditions(conditions.map((c, idx) => (idx === i ? { ...c, ...patch } : c)));

  const fieldCls = cn(
    "px-2 py-1.5 text-[12px] rounded-md",
    "bg-[var(--bg-surface)] border border-[var(--border)]",
    "text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors"
  );

  return (
    <div className="space-y-4">
      {/* Logical operator */}
      <div>
        <p className="text-[12px] font-medium text-[var(--text-secondary)] mb-2">
          Match Logic
        </p>
        <div className="flex gap-2">
          {(["AND", "OR"] as const).map((op) => (
            <button
              key={op}
              type="button"
              onClick={() => onUpdate({ ...data, logicalOperator: op })}
              className={cn(
                "flex-1 py-1.5 text-[12px] font-semibold rounded-lg border transition-colors",
                logicalOperator === op
                  ? "bg-[var(--accent)] border-[var(--accent)] text-white"
                  : "bg-[var(--bg-surface)] border-[var(--border)] text-[var(--text-muted)] hover:border-[var(--accent)]"
              )}
            >
              {op}
            </button>
          ))}
        </div>
      </div>

      {/* Conditions table */}
      <div>
        <p className="text-[12px] font-medium text-[var(--text-secondary)] mb-2">
          Conditions
        </p>
        <div className="space-y-2">
          {conditions.length === 0 && (
            <p className="text-[12px] text-[var(--text-muted)] py-2 text-center">
              No conditions yet — click + to add
            </p>
          )}
          {conditions.map((cond, i) => {
            const hideValue = VALUE_HIDDEN_OPS.includes(cond.operator as Operator);
            return (
              <div key={i} className="flex items-start gap-1.5 p-2 rounded-lg border border-[var(--border)] bg-[var(--bg-surface)]">
                <div className="flex flex-col gap-1.5 flex-1 min-w-0">
                  {/* Variable */}
                  <div className="relative">
                    <span className="absolute left-2 top-1/2 -translate-y-1/2 text-[11px] font-mono text-[var(--text-muted)] pointer-events-none">
                      {"{{"}
                    </span>
                    <input
                      type="text"
                      value={cond.variable}
                      onChange={(e) => updateCondition(i, { variable: e.target.value })}
                      placeholder="variable"
                      className={cn(fieldCls, "w-full pl-7 pr-6 font-mono")}
                      aria-label="Variable name"
                    />
                    <span className="absolute right-2 top-1/2 -translate-y-1/2 text-[11px] font-mono text-[var(--text-muted)] pointer-events-none">
                      {"}}"}
                    </span>
                  </div>
                  {/* Operator */}
                  <select
                    value={cond.operator}
                    onChange={(e) =>
                      updateCondition(i, { operator: e.target.value as Operator })
                    }
                    className={cn(fieldCls, "w-full")}
                    aria-label="Operator"
                  >
                    {OPERATORS.map((op) => (
                      <option key={op.value} value={op.value}>
                        {op.label}
                      </option>
                    ))}
                  </select>
                  {/* Value */}
                  {!hideValue && (
                    <input
                      type="text"
                      value={cond.value}
                      onChange={(e) => updateCondition(i, { value: e.target.value })}
                      placeholder="value or {{var}}"
                      className={cn(fieldCls, "w-full")}
                      aria-label="Comparison value"
                    />
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => removeCondition(i)}
                  className="p-1.5 text-[var(--text-muted)] hover:text-[var(--danger)] transition-colors mt-0.5 flex-shrink-0"
                  aria-label={`Remove condition ${i + 1}`}
                >
                  <Trash2 size={13} />
                </button>
              </div>
            );
          })}
        </div>
        <button
          type="button"
          onClick={addCondition}
          className={cn(
            "mt-2 flex items-center gap-1.5 text-[12px] px-3 py-1.5 rounded-lg w-full justify-center",
            "border border-dashed border-[var(--border)] text-[var(--text-muted)]",
            "hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors"
          )}
        >
          <Plus size={12} />
          Add Condition
        </button>
      </div>

      {/* Branch targets */}
      <div className="grid grid-cols-2 gap-2">
        <div>
          <label
            htmlFor="cond-true"
            className="block text-[11px] text-[var(--text-muted)] mb-1"
          >
            True → Node ID
          </label>
          <input
            id="cond-true"
            type="text"
            value={trueBranch}
            onChange={(e) => onUpdate({ ...data, trueBranch: e.target.value })}
            placeholder="node_id"
            className={cn(fieldCls, "w-full text-[var(--success)]")}
          />
        </div>
        <div>
          <label
            htmlFor="cond-false"
            className="block text-[11px] text-[var(--text-muted)] mb-1"
          >
            False → Node ID
          </label>
          <input
            id="cond-false"
            type="text"
            value={falseBranch}
            onChange={(e) => onUpdate({ ...data, falseBranch: e.target.value })}
            placeholder="node_id"
            className={cn(fieldCls, "w-full text-[var(--danger)]")}
          />
        </div>
      </div>
    </div>
  );
}
