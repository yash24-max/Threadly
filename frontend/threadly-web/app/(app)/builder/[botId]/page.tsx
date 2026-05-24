"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useRef, useState } from "react";
import dynamic from "next/dynamic";
import { api } from "@/lib/api";
import type { Bot, Flow, FlowVersion, FlowDefinition } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import {
  Save, Send, ChevronLeft, Clock, CheckCircle,
  History, Loader2, Eye, EyeOff,
} from "lucide-react"
import { LivePreviewPane } from "@/components/builder/LivePreviewPane"
import { toast } from "sonner";

// Dynamic import — React Flow requires browser APIs
const FlowCanvas = dynamic(
  () => import("@/components/builder/FlowCanvas").then((m) => ({ default: m.FlowCanvas })),
  { ssr: false, loading: () => (
    <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", color: "var(--text-muted)" }}>
      <Loader2 size={24} style={{ animation: "spin 1s linear infinite" }} />
    </div>
  )}
);

export default function BuilderPage() {
  const { botId } = useParams<{ botId: string }>();
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const router = useRouter();

  const [showVersions, setShowVersions] = useState(false)
  const [showPreview, setShowPreview] = useState(false)
  const [saveStatus, setSaveStatus] = useState<"idle" | "saving" | "saved">("idle");
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pendingDef = useRef<FlowDefinition | null>(null);

  const { data: bot } = useQuery<Bot>({
    queryKey: ["bots", botId],
    queryFn: () => api.get(`/v1/bots/${botId}`, token),
    enabled: !!token,
  });

  const { data: flow, isLoading } = useQuery<Flow>({
    queryKey: ["flows", botId],
    queryFn: () => api.get(`/v1/bots/${botId}/flow`, token),
    enabled: !!token,
  });

  const { data: versions } = useQuery<FlowVersion[]>({
    queryKey: ["flows", botId, "versions"],
    queryFn: () => api.get(`/v1/bots/${botId}/flow/versions`, token),
    enabled: !!token && showVersions,
  });

  const saveDraft = useMutation({
    mutationFn: (def: FlowDefinition) =>
      api.patch(`/v1/bots/${botId}/flow/draft`, { definition: def }, token),
    onSuccess: () => {
      setSaveStatus("saved");
      setTimeout(() => setSaveStatus("idle"), 3000);
    },
    onError: () => toast.error("Failed to save. Check your connection."),
  });

  const publish = useMutation({
    mutationFn: () => api.post(`/v1/bots/${botId}/flow/publish`, {}, token),
    onSuccess: () => {
      toast.success("Flow published!");
      qc.invalidateQueries({ queryKey: ["flows", botId, "versions"] });
    },
    onError: () => toast.error("Publish failed. Fix any validation errors first."),
  });

  const rollback = useMutation({
    mutationFn: (versionId: string) =>
      api.post(`/v1/bots/${botId}/flow/rollback/${versionId}`, {}, token),
    onSuccess: () => {
      toast.success("Rolled back.");
      qc.invalidateQueries({ queryKey: ["flows", botId] });
    },
  });

  // Autosave on every canvas change (debounced 800ms)
  const handleFlowChange = useCallback(
    (def: FlowDefinition) => {
      pendingDef.current = def;
      setSaveStatus("saving");
      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        if (pendingDef.current) {
          saveDraft.mutate(pendingDef.current);
        }
      }, 800);
    },
    [saveDraft]
  );

  const statusIndicator = () => {
    if (saveStatus === "saving") return (
      <span style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 12, color: "var(--text-muted)" }}>
        <Loader2 size={12} style={{ animation: "spin 1s linear infinite" }} />
        Saving…
      </span>
    );
    if (saveStatus === "saved") return (
      <span style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 12, color: "var(--success)" }}>
        <CheckCircle size={12} />
        Saved
      </span>
    );
    return null;
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", overflow: "hidden" }}>
      {/* Toolbar */}
      <div style={{
        height: 52, display: "flex", alignItems: "center", gap: 12,
        padding: "0 16px", borderBottom: "1px solid var(--border)",
        background: "var(--bg-panel)", flexShrink: 0,
      }}>
        <button
          onClick={() => router.push("/bots")}
          style={{ display: "flex", alignItems: "center", gap: 5, background: "none", border: "none", cursor: "pointer", color: "var(--text-secondary)", fontSize: 13 }}
        >
          <ChevronLeft size={16} />
        </button>

        <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
          <span style={{ fontSize: 14, fontWeight: 600 }}>{bot?.name ?? "…"}</span>
          <span style={{ fontSize: 11, color: "var(--text-muted)" }}>Flow Builder</span>
        </div>

        <div style={{ flex: 1 }} />

        {statusIndicator()}

        <button
          onClick={() => setShowPreview((v) => !v)}
          style={{
            display: "flex", alignItems: "center", gap: 5,
            padding: "6px 12px", borderRadius: "var(--radius-md)",
            background: showPreview ? "var(--bg-surface)" : "transparent",
            border: "1px solid var(--border)",
            color: showPreview ? "var(--accent)" : "var(--text-secondary)", cursor: "pointer", fontSize: 13,
          }}
        >
          <Eye size={14} />
          Preview
        </button>

        <button
          onClick={() => setShowVersions((v) => !v)}
          style={{
            display: "flex", alignItems: "center", gap: 5,
            padding: "6px 12px", borderRadius: "var(--radius-md)",
            background: showVersions ? "var(--bg-surface)" : "transparent",
            border: "1px solid var(--border)",
            color: "var(--text-secondary)", cursor: "pointer", fontSize: 13,
          }}
        >
          <History size={14} />
          Versions
        </button>

        <button
          onClick={() => publish.mutate()}
          disabled={publish.isPending}
          style={{
            display: "flex", alignItems: "center", gap: 6,
            padding: "7px 16px", borderRadius: "var(--radius-md)",
            background: "var(--accent)", color: "var(--accent-fg)",
            border: "none", cursor: "pointer", fontSize: 13, fontWeight: 500,
          }}
        >
          {publish.isPending ? (
            <Loader2 size={13} style={{ animation: "spin 1s linear infinite" }} />
          ) : (
            <Send size={13} />
          )}
          Publish
        </button>
      </div>

      {/* Main canvas area */}
      <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
        {isLoading ? (
          <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center" }}>
            <Loader2 size={24} style={{ animation: "spin 1s linear infinite", color: "var(--text-muted)" }} />
          </div>
        ) : (
          <FlowCanvas
            initialDefinition={flow?.definition}
            onChange={handleFlowChange}
          />
        )}

        {/* Live preview pane */}
        {showPreview && (
          <LivePreviewPane botId={botId} onClose={() => setShowPreview(false)} />
        )}

        {/* Versions drawer */}
        {showVersions && (
          <div style={{
            width: 260, borderLeft: "1px solid var(--border)",
            background: "var(--bg-panel)", display: "flex", flexDirection: "column",
            overflow: "hidden",
          }}>
            <div style={{ padding: "12px 14px", borderBottom: "1px solid var(--border)" }}>
              <p style={{ fontSize: 13, fontWeight: 600 }}>Version history</p>
              <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>
                Click to preview, rollback to restore
              </p>
            </div>
            <div style={{ flex: 1, overflow: "auto", padding: 8 }}>
              {/* Current draft */}
              <div style={{
                padding: "10px 12px", borderRadius: "var(--radius-md)",
                border: "1px solid var(--accent)", marginBottom: 6,
              }}>
                <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                  <span style={{ fontSize: 12, fontWeight: 600, color: "var(--accent)" }}>Draft</span>
                  <span style={{ fontSize: 11, color: "var(--text-muted)", marginLeft: "auto" }}>Current</span>
                </div>
                {flow?.updatedAt && (
                  <p style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 2 }}>
                    {formatRelative(flow.updatedAt)}
                  </p>
                )}
              </div>

              {versions?.map((v) => (
                <div
                  key={v.id}
                  style={{
                    padding: "10px 12px", borderRadius: "var(--radius-md)",
                    border: "1px solid var(--border)", marginBottom: 6,
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                    <span style={{ fontSize: 12, fontWeight: 500 }}>v{v.version}</span>
                  </div>
                  <p style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 2 }}>
                    {formatRelative(v.publishedAt)}
                  </p>
                  <button
                    onClick={() => rollback.mutate(v.id)}
                    style={{
                      marginTop: 6, fontSize: 11, padding: "4px 10px",
                      borderRadius: "var(--radius-sm)",
                      background: "var(--bg-surface)", border: "1px solid var(--border)",
                      color: "var(--text-secondary)", cursor: "pointer",
                    }}
                  >
                    Restore
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
