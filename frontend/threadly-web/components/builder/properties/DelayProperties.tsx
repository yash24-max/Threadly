"use client";

import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

const UNITS = ["seconds", "minutes", "hours"] as const;
type Unit = (typeof UNITS)[number];

interface DelayPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

export function DelayProperties({ node, onUpdate }: DelayPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const amount = (data.amount as number) ?? 5;
  const unit = (data.unit as Unit) ?? "seconds";

  const fieldCls = cn(
    "px-3 py-2 text-[13px] rounded-lg",
    "bg-[var(--bg-surface)] border border-[var(--border)]",
    "text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors"
  );

  // Human-readable summary
  const summary =
    amount === 1 ? `1 ${unit.replace("s", "")}` : `${amount} ${unit}`;

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5">
          Wait Duration
        </label>
        <div className="flex items-center gap-2">
          <input
            type="number"
            value={amount}
            min={1}
            max={unit === "seconds" ? 3600 : unit === "minutes" ? 1440 : 72}
            onChange={(e) =>
              onUpdate({ ...data, amount: Math.max(1, Number(e.target.value)) })
            }
            className={cn(fieldCls, "w-24")}
            aria-label="Delay amount"
          />
          <select
            value={unit}
            onChange={(e) => onUpdate({ ...data, unit: e.target.value as Unit })}
            className={cn(fieldCls, "flex-1")}
            aria-label="Delay unit"
          >
            {UNITS.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="rounded-lg bg-[var(--bg-surface)] border border-[var(--border)] px-3 py-2">
        <p className="text-[12px] text-[var(--text-muted)]">
          Flow will pause for{" "}
          <span className="text-[var(--accent)] font-semibold">{summary}</span>{" "}
          before continuing.
        </p>
      </div>
    </div>
  );
}
