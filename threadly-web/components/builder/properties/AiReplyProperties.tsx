"use client";

import type { Node } from "@xyflow/react";
import { toast } from "sonner";
import { Sparkles } from "lucide-react";
import { cn } from "@/lib/utils";

interface AiReplyPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

function Slider({
  id,
  label,
  value,
  min,
  max,
  step,
  onChange,
  display,
}: {
  id: string;
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  onChange: (v: number) => void;
  display?: string;
}) {
  return (
    <div>
      <div className="flex items-center justify-between mb-1.5">
        <label
          htmlFor={id}
          className="text-[12px] font-medium text-[var(--text-secondary)]"
        >
          {label}
        </label>
        <span className="text-[12px] font-mono text-[var(--accent)] bg-[var(--bg-surface)] px-2 py-0.5 rounded border border-[var(--border)]">
          {display ?? value}
        </span>
      </div>
      <input
        id={id}
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full h-1.5 rounded-full accent-[var(--accent)] cursor-pointer"
        aria-valuenow={value}
        aria-valuemin={min}
        aria-valuemax={max}
      />
      <div className="flex justify-between text-[10px] text-[var(--text-muted)] mt-0.5">
        <span>{min}</span>
        <span>{max}</span>
      </div>
    </div>
  );
}

export function AiReplyProperties({ node, onUpdate }: AiReplyPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const systemPrompt = (data.systemPrompt as string) ?? "";
  const maxTokens = (data.maxTokens as number) ?? 1024;
  const temperature = (data.temperature as number) ?? 0.7;
  const useKnowledgeBase = (data.useKnowledgeBase as boolean) ?? true;
  const topK = (data.topK as number) ?? 5;

  // Approximate token count (4 chars ≈ 1 token)
  const estimatedTokens = Math.ceil(systemPrompt.length / 4);

  return (
    <div className="space-y-4">
      {/* System Prompt */}
      <div>
        <div className="flex items-center justify-between mb-1.5">
          <label
            htmlFor="ai-prompt"
            className="text-[12px] font-medium text-[var(--text-secondary)]"
          >
            System Prompt
          </label>
          <button
            type="button"
            onClick={() => toast.info("AI prompt suggestions coming soon!")}
            className={cn(
              "flex items-center gap-1 text-[11px] px-2 py-1 rounded-md",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-muted)] hover:text-[var(--accent)] hover:border-[var(--accent)]",
              "transition-colors cursor-pointer"
            )}
          >
            <Sparkles size={10} />
            Improve
          </button>
        </div>
        <textarea
          id="ai-prompt"
          value={systemPrompt}
          onChange={(e) => onUpdate({ ...data, systemPrompt: e.target.value })}
          rows={5}
          placeholder={`You are a helpful assistant for {{org.name}}.\nAnswer questions concisely based on the knowledge base.`}
          className={cn(
            "w-full px-3 py-2 text-[12px] rounded-lg resize-y font-mono",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors"
          )}
        />
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          ~{estimatedTokens} tokens · Supports{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
            {`{{variable}}`}
          </code>
        </p>
      </div>

      {/* Max Tokens Slider */}
      <Slider
        id="ai-max-tokens"
        label="Max Tokens"
        value={maxTokens}
        min={100}
        max={4000}
        step={100}
        onChange={(v) => onUpdate({ ...data, maxTokens: v })}
      />

      {/* Temperature Slider */}
      <Slider
        id="ai-temperature"
        label="Temperature"
        value={temperature}
        min={0}
        max={1}
        step={0.1}
        onChange={(v) => onUpdate({ ...data, temperature: v })}
        display={temperature.toFixed(1)}
      />

      {/* Knowledge Base Toggle */}
      <div className="flex items-center justify-between py-2 px-3 rounded-lg bg-[var(--bg-surface)] border border-[var(--border)]">
        <div>
          <p className="text-[13px] font-medium text-[var(--text-primary)]">
            Use Knowledge Base
          </p>
          <p className="text-[11px] text-[var(--text-muted)]">
            Enable RAG for context-aware replies
          </p>
        </div>
        <button
          type="button"
          role="switch"
          aria-checked={useKnowledgeBase}
          onClick={() => onUpdate({ ...data, useKnowledgeBase: !useKnowledgeBase })}
          className={cn(
            "relative inline-flex h-5 w-9 items-center rounded-full transition-colors",
            useKnowledgeBase ? "bg-[var(--accent)]" : "bg-[var(--border)]"
          )}
        >
          <span
            className={cn(
              "inline-block h-3.5 w-3.5 rounded-full bg-white shadow transition-transform",
              useKnowledgeBase ? "translate-x-[18px]" : "translate-x-[2px]"
            )}
          />
        </button>
      </div>

      {/* Top K - only shown when KB is on */}
      {useKnowledgeBase && (
        <div>
          <label
            htmlFor="ai-topk"
            className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
          >
            Top K Results
          </label>
          <input
            id="ai-topk"
            type="number"
            value={topK}
            min={1}
            max={20}
            onChange={(e) =>
              onUpdate({ ...data, topK: Math.min(20, Math.max(1, Number(e.target.value))) })
            }
            className={cn(
              "w-24 px-3 py-2 text-[13px] rounded-lg",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-primary)]",
              "outline-none focus:border-[var(--accent)] transition-colors"
            )}
          />
          <p className="text-[11px] text-[var(--text-muted)] mt-1">
            Number of KB passages to retrieve (1–20)
          </p>
        </div>
      )}
    </div>
  );
}
