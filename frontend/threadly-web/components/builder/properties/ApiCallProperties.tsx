"use client";

import { Plus, Trash2 } from "lucide-react";
import type { Node } from "@xyflow/react";
import { cn } from "@/lib/utils";

interface Header {
  key: string;
  value: string;
}

interface ResponseMapping {
  variable: string;
  jsonPath: string;
}

interface ApiCallPropertiesProps {
  node: Node;
  onUpdate: (data: Record<string, unknown>) => void;
}

const METHODS = ["GET", "POST", "PUT", "PATCH", "DELETE"] as const;
type Method = (typeof METHODS)[number];

const fieldCls = cn(
  "px-2 py-1.5 text-[12px] rounded-md",
  "bg-[var(--bg-surface)] border border-[var(--border)]",
  "text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors"
);

const BODY_METHODS: Method[] = ["POST", "PUT", "PATCH"];

export function ApiCallProperties({ node, onUpdate }: ApiCallPropertiesProps) {
  const data = node.data as Record<string, unknown>;
  const method = (data.method as Method) ?? "GET";
  const url = (data.url as string) ?? "";
  const headers: Header[] = (data.headers as Header[]) ?? [];
  const body = (data.body as string) ?? "";
  const credentialId = (data.credentialId as string) ?? "";
  const responseMapping: ResponseMapping = (data.responseMapping as ResponseMapping) ?? {
    variable: "",
    jsonPath: "",
  };

  const updateHeaders = (updated: Header[]) => onUpdate({ ...data, headers: updated });
  const addHeader = () => updateHeaders([...headers, { key: "", value: "" }]);
  const removeHeader = (i: number) => updateHeaders(headers.filter((_, idx) => idx !== i));
  const updateHeader = (i: number, patch: Partial<Header>) =>
    updateHeaders(headers.map((h, idx) => (idx === i ? { ...h, ...patch } : h)));

  const showBody = BODY_METHODS.includes(method);

  return (
    <div className="space-y-4">
      {/* Method + URL */}
      <div>
        <label className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5">
          Request
        </label>
        <div className="flex gap-1.5">
          <select
            value={method}
            onChange={(e) => onUpdate({ ...data, method: e.target.value as Method })}
            className={cn(fieldCls, "w-24 flex-shrink-0 font-mono font-semibold")}
            aria-label="HTTP method"
          >
            {METHODS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
          <input
            type="url"
            value={url}
            onChange={(e) => onUpdate({ ...data, url: e.target.value })}
            placeholder="https://api.example.com/endpoint"
            className={cn(fieldCls, "flex-1 min-w-0")}
            aria-label="Request URL"
          />
        </div>
      </div>

      {/* Credential picker */}
      <div>
        <label
          htmlFor="api-cred"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Credential
        </label>
        <select
          id="api-cred"
          value={credentialId}
          onChange={(e) => onUpdate({ ...data, credentialId: e.target.value })}
          className={cn(fieldCls, "w-full")}
        >
          <option value="">Load credentials…</option>
        </select>
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          Manage in Settings → Credentials
        </p>
      </div>

      {/* Headers */}
      <div>
        <p className="text-[12px] font-medium text-[var(--text-secondary)] mb-2">
          Headers
        </p>
        <div className="space-y-1.5">
          {headers.map((h, i) => (
            <div key={i} className="flex items-center gap-1.5">
              <input
                type="text"
                value={h.key}
                onChange={(e) => updateHeader(i, { key: e.target.value })}
                placeholder="Key"
                className={cn(fieldCls, "flex-1 min-w-0")}
                aria-label={`Header ${i + 1} key`}
              />
              <input
                type="text"
                value={h.value}
                onChange={(e) => updateHeader(i, { value: e.target.value })}
                placeholder="Value"
                className={cn(fieldCls, "flex-1 min-w-0")}
                aria-label={`Header ${i + 1} value`}
              />
              <button
                type="button"
                onClick={() => removeHeader(i)}
                className="p-1 text-[var(--text-muted)] hover:text-[var(--danger)] transition-colors"
                aria-label={`Remove header ${i + 1}`}
              >
                <Trash2 size={13} />
              </button>
            </div>
          ))}
        </div>
        <button
          type="button"
          onClick={addHeader}
          className={cn(
            "mt-1.5 flex items-center gap-1.5 text-[11px] px-2 py-1 rounded-md",
            "border border-dashed border-[var(--border)] text-[var(--text-muted)]",
            "hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors"
          )}
        >
          <Plus size={11} />
          Add Header
        </button>
      </div>

      {/* Body */}
      {showBody && (
        <div>
          <label
            htmlFor="api-body"
            className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
          >
            Request Body (JSON)
          </label>
          <textarea
            id="api-body"
            value={body}
            onChange={(e) => onUpdate({ ...data, body: e.target.value })}
            rows={4}
            placeholder={`{\n  "name": "{{customer_name}}"\n}`}
            className={cn(
              "w-full px-3 py-2 text-[12px] rounded-lg resize-y font-mono",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
              "outline-none focus:border-[var(--accent)] transition-colors"
            )}
          />
        </div>
      )}

      {/* Response mapping */}
      <div>
        <p className="text-[12px] font-medium text-[var(--text-secondary)] mb-2">
          Response Mapping
        </p>
        <div className="flex items-center gap-1.5">
          <div className="relative flex-1 min-w-0">
            <span className="absolute left-2 top-1/2 -translate-y-1/2 text-[11px] font-mono text-[var(--text-muted)] pointer-events-none">
              {"{{"}
            </span>
            <input
              type="text"
              value={responseMapping.variable}
              onChange={(e) =>
                onUpdate({
                  ...data,
                  responseMapping: { ...responseMapping, variable: e.target.value },
                })
              }
              placeholder="response_var"
              className={cn(fieldCls, "w-full pl-7 pr-6 font-mono")}
              aria-label="Response variable name"
            />
            <span className="absolute right-2 top-1/2 -translate-y-1/2 text-[11px] font-mono text-[var(--text-muted)] pointer-events-none">
              {"}}"}
            </span>
          </div>
          <span className="text-[11px] text-[var(--text-muted)] flex-shrink-0">←</span>
          <input
            type="text"
            value={responseMapping.jsonPath}
            onChange={(e) =>
              onUpdate({
                ...data,
                responseMapping: { ...responseMapping, jsonPath: e.target.value },
              })
            }
            placeholder="$.data.id"
            className={cn(fieldCls, "flex-1 min-w-0 font-mono")}
            aria-label="JSON path"
          />
        </div>
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          JSONPath expression to extract from the response
        </p>
      </div>
    </div>
  );
}
