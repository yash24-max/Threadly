"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { useRef, useState } from "react";
import { api } from "@/lib/api";
import type { Bot, KbDocument } from "@/lib/types";
import { formatRelative } from "@/lib/utils";
import { Upload, FileText, Globe, Trash2, Loader2, CheckCircle, AlertCircle, Clock } from "lucide-react";

export default function KnowledgePage() {
  const { botId } = useParams<{ botId: string }>();
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();
  const fileRef = useRef<HTMLInputElement>(null);

  const [urlInput, setUrlInput] = useState("");
  const [uploading, setUploading] = useState(false);
  const [tab, setTab] = useState<"file" | "url">("file");

  const { data: bot } = useQuery<Bot>({
    queryKey: ["bots", botId],
    queryFn: () => api.get(`/v1/bots/${botId}`, token),
    enabled: !!token,
  });

  const { data: docs, isLoading } = useQuery<KbDocument[]>({
    queryKey: ["kb", botId],
    queryFn: () => api.get(`/v1/bots/${botId}/kb`, token),
    enabled: !!token,
    refetchInterval: (query) => {
      const docs = query.state.data as KbDocument[] | undefined;
      const hasProcessing = docs?.some((d) => d.status === "PROCESSING" || d.status === "PENDING");
      return hasProcessing ? 3000 : false;
    },
  });

  const uploadFile = async (file: File) => {
    setUploading(true);
    try {
      const form = new FormData();
      form.append("file", file);
      form.append("docName", file.name);
      form.append("docType", file.name.endsWith(".pdf") ? "pdf" : "txt");
      await api.upload(`/v1/bots/${botId}/kb`, form, token);
      qc.invalidateQueries({ queryKey: ["kb", botId] });
    } finally {
      setUploading(false);
    }
  };

  const addUrl = useMutation({
    mutationFn: () =>
      api.post(`/v1/bots/${botId}/kb/url`, { sourceUrl: urlInput, docName: urlInput }, token),
    onSuccess: () => {
      setUrlInput("");
      qc.invalidateQueries({ queryKey: ["kb", botId] });
    },
  });

  const deleteDoc = useMutation({
    mutationFn: (docId: string) => api.delete(`/v1/bots/${botId}/kb/${docId}`, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["kb", botId] }),
  });

  const statusIcon = (status: KbDocument["status"]) => {
    if (status === "READY") return <CheckCircle size={14} color="var(--success)" />;
    if (status === "FAILED") return <AlertCircle size={14} color="var(--danger)" />;
    return <Loader2 size={14} color="var(--warn)" style={{ animation: "spin 1s linear infinite" }} />;
  };

  const statusLabel = (status: KbDocument["status"]) => ({
    PENDING: "Pending",
    PROCESSING: "Indexing…",
    READY: "Ready",
    FAILED: "Failed",
  }[status] ?? status);

  return (
    <div style={{ padding: "32px 40px", maxWidth: 900 }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 13, color: "var(--text-secondary)" }}>
          {bot?.name ?? "Bot"} · Knowledge Base
        </p>
        <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.5px", marginTop: 4 }}>
          Knowledge Base
        </h1>
        <p style={{ color: "var(--text-secondary)", fontSize: 14, marginTop: 4 }}>
          Upload PDFs, text files, or URLs. Your bot will cite them automatically.
        </p>
      </div>

      {/* Upload card */}
      <div style={{
        background: "var(--bg-panel)", border: "1px solid var(--border)",
        borderRadius: "var(--radius-lg)", padding: 24, marginBottom: 24,
      }}>
        {/* Tabs */}
        <div style={{ display: "flex", gap: 4, marginBottom: 20, background: "var(--bg-surface)", borderRadius: "var(--radius-md)", padding: 4, width: "fit-content" }}>
          {(["file", "url"] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              style={{
                padding: "6px 16px", borderRadius: "var(--radius-sm)", border: "none",
                background: tab === t ? "var(--bg-panel)" : "transparent",
                color: tab === t ? "var(--text-primary)" : "var(--text-secondary)",
                cursor: "pointer", fontSize: 13, fontWeight: tab === t ? 500 : 400,
              }}
            >
              {t === "file" ? "Upload file" : "Add URL"}
            </button>
          ))}
        </div>

        {tab === "file" ? (
          <div>
            <div
              onClick={() => fileRef.current?.click()}
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault();
                const file = e.dataTransfer.files[0];
                if (file) uploadFile(file);
              }}
              style={{
                border: "2px dashed var(--border)", borderRadius: "var(--radius-lg)",
                padding: "40px 24px", textAlign: "center", cursor: "pointer",
                background: "var(--bg-surface)",
              }}
            >
              {uploading ? (
                <Loader2 size={28} style={{ margin: "0 auto 12px", color: "var(--text-muted)", animation: "spin 1s linear infinite" }} />
              ) : (
                <Upload size={28} style={{ margin: "0 auto 12px", color: "var(--text-muted)" }} />
              )}
              <p style={{ fontSize: 14, fontWeight: 500 }}>
                {uploading ? "Uploading…" : "Drop a file here or click to browse"}
              </p>
              <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 4 }}>
                PDF, TXT — up to 50 MB
              </p>
            </div>
            <input
              ref={fileRef}
              type="file"
              accept=".pdf,.txt"
              style={{ display: "none" }}
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) uploadFile(file);
                e.target.value = "";
              }}
            />
          </div>
        ) : (
          <div style={{ display: "flex", gap: 10 }}>
            <input
              value={urlInput}
              onChange={(e) => setUrlInput(e.target.value)}
              placeholder="https://help.example.com/article/123"
              style={{
                flex: 1, padding: "10px 14px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 14, outline: "none",
              }}
            />
            <button
              onClick={() => urlInput.trim() && addUrl.mutate()}
              disabled={!urlInput.trim() || addUrl.isPending}
              style={{
                padding: "10px 20px", borderRadius: "var(--radius-md)",
                background: "var(--accent)", color: "var(--accent-fg)",
                border: "none", cursor: "pointer", fontSize: 14, fontWeight: 500,
              }}
            >
              {addUrl.isPending ? "Adding…" : "Add URL"}
            </button>
          </div>
        )}
      </div>

      {/* Documents list */}
      <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>
        Documents ({docs?.length ?? 0})
      </h2>

      {isLoading ? (
        <div style={{ textAlign: "center", paddingTop: 40 }}>
          <Loader2 size={20} style={{ color: "var(--text-muted)", animation: "spin 1s linear infinite" }} />
        </div>
      ) : !docs?.length ? (
        <div style={{
          textAlign: "center", padding: "40px 0",
          color: "var(--text-muted)", fontSize: 14,
          border: "1px solid var(--border)", borderRadius: "var(--radius-lg)",
        }}>
          No documents yet. Upload a file or add a URL above.
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {docs.map((doc) => (
            <div
              key={doc.id}
              style={{
                display: "flex", alignItems: "center", gap: 12,
                padding: "14px 16px",
                background: "var(--bg-panel)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)",
              }}
            >
              {doc.docType === "url"
                ? <Globe size={18} style={{ color: "var(--text-secondary)", flexShrink: 0 }} />
                : <FileText size={18} style={{ color: "var(--text-secondary)", flexShrink: 0 }} />
              }
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontSize: 14, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {doc.name}
                </p>
                <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>
                  {formatRelative(doc.createdAt)}
                  {doc.chunkCount ? ` · ${doc.chunkCount} chunks` : ""}
                </p>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                {statusIcon(doc.status)}
                <span style={{ fontSize: 12, color: "var(--text-secondary)" }}>
                  {statusLabel(doc.status)}
                </span>
              </div>
              <button
                onClick={() => deleteDoc.mutate(doc.id)}
                style={{
                  padding: "6px 8px", borderRadius: "var(--radius-sm)",
                  background: "transparent", border: "none",
                  color: "var(--text-muted)", cursor: "pointer",
                }}
              >
                <Trash2 size={14} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
