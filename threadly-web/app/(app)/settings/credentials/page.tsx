"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2, Key, Eye, EyeOff, X, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { formatDate, cn } from "@/lib/utils";

// ── Types ─────────────────────────────────────────────────────────────────────

type CredentialType = "API_KEY" | "OAUTH_TOKEN" | "BASIC_AUTH" | "CUSTOM";

interface Credential {
  id: string;
  name: string;
  type: CredentialType;
  botId: string;
  botName?: string;
  createdAt: string;
  lastUsedAt?: string;
}

interface Bot {
  id: string;
  name: string;
}

// ── Constants ─────────────────────────────────────────────────────────────────

const CRED_TYPES: { value: CredentialType; label: string }[] = [
  { value: "API_KEY", label: "API Key" },
  { value: "OAUTH_TOKEN", label: "OAuth Token" },
  { value: "BASIC_AUTH", label: "Basic Auth" },
  { value: "CUSTOM", label: "Custom" },
];

const TYPE_COLORS: Record<CredentialType, { color: string; bg: string }> = {
  API_KEY: { color: "#3B82F6", bg: "rgba(59,130,246,0.1)" },
  OAUTH_TOKEN: { color: "#10B981", bg: "rgba(16,185,129,0.1)" },
  BASIC_AUTH: { color: "#F59E0B", bg: "rgba(245,158,11,0.1)" },
  CUSTOM: { color: "#8B5CF6", bg: "rgba(139,92,246,0.1)" },
};

// ── Skeleton ──────────────────────────────────────────────────────────────────

function TableRowSkeleton() {
  return (
    <tr className="border-b border-[var(--border)] animate-pulse">
      <td className="px-5 py-4">
        <div className="h-3.5 w-32 rounded bg-[var(--border)]" />
      </td>
      <td className="px-5 py-4">
        <div className="h-5 w-20 rounded-full bg-[var(--border)]" />
      </td>
      <td className="px-5 py-4">
        <div className="h-3.5 w-24 rounded bg-[var(--border)]" />
      </td>
      <td className="px-5 py-4">
        <div className="h-3.5 w-28 rounded bg-[var(--border)]" />
      </td>
      <td className="px-5 py-4">
        <div className="h-3.5 w-20 rounded bg-[var(--border)]" />
      </td>
      <td className="px-5 py-4">
        <div className="h-5 w-5 rounded bg-[var(--border)]" />
      </td>
    </tr>
  );
}

// ── Add credential sheet ──────────────────────────────────────────────────────

interface AddCredentialSheetProps {
  open: boolean;
  onClose: () => void;
  bots: Bot[];
  onSave: (data: {
    name: string;
    type: CredentialType;
    botId: string;
    value: string;
  }) => Promise<void>;
}

function AddCredentialSheet({ open, onClose, bots, onSave }: AddCredentialSheetProps) {
  const [name, setName] = useState("");
  const [type, setType] = useState<CredentialType>("API_KEY");
  const [botId, setBotId] = useState("");
  const [value, setValue] = useState("");
  const [showValue, setShowValue] = useState(false);
  const [saving, setSaving] = useState(false);

  const reset = () => {
    setName("");
    setType("API_KEY");
    setBotId("");
    setValue("");
    setShowValue(false);
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleSave = async () => {
    if (!name.trim() || !botId || !value.trim()) return;
    setSaving(true);
    try {
      await onSave({ name: name.trim(), type, botId, value });
      reset();
      onClose();
    } finally {
      setSaving(false);
    }
  };

  const fieldCls = cn(
    "w-full px-3 py-2.5 text-[13px] rounded-lg",
    "bg-[var(--bg-surface)] border border-[var(--border)]",
    "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
    "outline-none focus:border-[var(--accent)] transition-colors"
  );

  if (!open) return null;

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 z-40 bg-black/40"
        onClick={handleClose}
        aria-hidden="true"
      />

      {/* Sheet */}
      <div
        className={cn(
          "fixed right-0 top-0 bottom-0 z-50 w-[380px] max-w-full",
          "bg-[var(--bg-panel)] border-l border-[var(--border)] shadow-2xl",
          "flex flex-col overflow-hidden"
        )}
        role="dialog"
        aria-modal="true"
        aria-label="Add credential"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--border)]">
          <div className="flex items-center gap-2.5">
            <Key size={16} className="text-[var(--accent)]" />
            <h2 className="text-[15px] font-semibold text-[var(--text-primary)]">
              Add Credential
            </h2>
          </div>
          <button
            type="button"
            onClick={handleClose}
            className="p-1.5 rounded-lg text-[var(--text-muted)] hover:bg-[var(--bg-surface)] transition-colors"
            aria-label="Close"
          >
            <X size={16} />
          </button>
        </div>

        {/* Form */}
        <div className="flex-1 overflow-y-auto px-5 py-5 space-y-4">
          <div>
            <label
              htmlFor="cred-name"
              className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Name
            </label>
            <input
              id="cred-name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="My API Key"
              autoFocus
              className={fieldCls}
            />
          </div>

          <div>
            <label
              htmlFor="cred-type"
              className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Type
            </label>
            <select
              id="cred-type"
              value={type}
              onChange={(e) => setType(e.target.value as CredentialType)}
              className={fieldCls}
            >
              {CRED_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="cred-bot"
              className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Bot
            </label>
            <select
              id="cred-bot"
              value={botId}
              onChange={(e) => setBotId(e.target.value)}
              className={fieldCls}
            >
              <option value="">Select a bot…</option>
              {bots.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="cred-value"
              className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Secret Value
            </label>
            <div className="relative">
              <input
                id="cred-value"
                type={showValue ? "text" : "password"}
                value={value}
                onChange={(e) => setValue(e.target.value)}
                placeholder="sk-..."
                className={cn(fieldCls, "pr-10 font-mono")}
              />
              <button
                type="button"
                onClick={() => setShowValue((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
                aria-label={showValue ? "Hide value" : "Show value"}
              >
                {showValue ? <EyeOff size={14} /> : <Eye size={14} />}
              </button>
            </div>
            <p className="text-[11px] text-[var(--text-muted)] mt-1">
              Stored encrypted. Not shown after saving.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="px-5 py-4 border-t border-[var(--border)] flex gap-3">
          <button
            type="button"
            onClick={handleClose}
            className={cn(
              "flex-1 py-2.5 rounded-lg border border-[var(--border)]",
              "text-[var(--text-secondary)] text-[13px] font-medium",
              "hover:border-[var(--border-strong)] transition-colors"
            )}
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSave}
            disabled={!name.trim() || !botId || !value.trim() || saving}
            className={cn(
              "flex-1 py-2.5 rounded-lg bg-[var(--accent)] text-white",
              "text-[13px] font-semibold disabled:opacity-40",
              "hover:opacity-90 transition-opacity"
            )}
          >
            {saving ? "Saving…" : "Save Credential"}
          </button>
        </div>
      </div>
    </>
  );
}

// ── Confirm delete dialog ─────────────────────────────────────────────────────

interface ConfirmDeleteProps {
  credential: Credential | null;
  onConfirm: () => void;
  onCancel: () => void;
}

function ConfirmDeleteDialog({ credential, onConfirm, onCancel }: ConfirmDeleteProps) {
  if (!credential) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      role="dialog"
      aria-modal="true"
      aria-label="Confirm deletion"
    >
      <div className="w-[360px] max-w-[90vw] bg-[var(--bg-panel)] rounded-2xl border border-[var(--border)] shadow-2xl p-6 space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[var(--danger)]/10 flex items-center justify-center">
            <AlertTriangle size={18} className="text-[var(--danger)]" />
          </div>
          <div>
            <p className="text-[15px] font-semibold text-[var(--text-primary)]">
              Delete Credential
            </p>
            <p className="text-[12px] text-[var(--text-muted)]">
              This action cannot be undone
            </p>
          </div>
        </div>

        <p className="text-[13px] text-[var(--text-secondary)]">
          Are you sure you want to delete{" "}
          <strong className="text-[var(--text-primary)]">{credential.name}</strong>? Any
          bot node using this credential will stop working.
        </p>

        <div className="flex gap-3">
          <button
            type="button"
            onClick={onCancel}
            className={cn(
              "flex-1 py-2.5 rounded-xl border border-[var(--border)]",
              "text-[var(--text-secondary)] text-[13px] font-medium",
              "hover:border-[var(--border-strong)] transition-colors"
            )}
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className={cn(
              "flex-1 py-2.5 rounded-xl bg-[var(--danger)] text-white",
              "text-[13px] font-semibold hover:opacity-90 transition-opacity"
            )}
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function CredentialsPage() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const qc = useQueryClient();

  const [sheetOpen, setSheetOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Credential | null>(null);

  const { data: bots = [] } = useQuery<Bot[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token,
  });

  // Aggregate credentials across all bots
  const { data: credentials = [], isLoading } = useQuery<Credential[]>({
    queryKey: ["credentials"],
    queryFn: async () => {
      if (!bots.length) return [];
      const results = await Promise.all(
        bots.map((b) =>
          api
            .get<Credential[]>(`/v1/bots/${b.id}/credentials`, token)
            .then((creds) => creds.map((c) => ({ ...c, botName: b.name })))
            .catch(() => [] as Credential[])
        )
      );
      return results.flat();
    },
    enabled: !!token && bots.length > 0,
  });

  const createCredential = useMutation({
    mutationFn: async (data: {
      name: string;
      type: CredentialType;
      botId: string;
      value: string;
    }) => {
      await api.post(`/v1/bots/${data.botId}/credentials`, data, token);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["credentials"] });
      toast.success("Credential saved successfully");
    },
    onError: () => toast.error("Failed to save credential"),
  });

  const deleteCredential = useMutation({
    mutationFn: async (cred: Credential) => {
      await api.delete(`/v1/bots/${cred.botId}/credentials/${cred.id}`, token);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["credentials"] });
      toast.success("Credential deleted");
      setDeleteTarget(null);
    },
    onError: () => toast.error("Failed to delete credential"),
  });

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-6">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-[var(--text-primary)]">
            Credentials
          </h1>
          <p className="text-[13px] text-[var(--text-muted)] mt-0.5">
            Manage API keys and secrets used by your bot flows
          </p>
        </div>
        <button
          type="button"
          onClick={() => setSheetOpen(true)}
          className={cn(
            "flex items-center gap-2 px-4 py-2.5 rounded-xl",
            "bg-[var(--accent)] text-white text-[13px] font-semibold",
            "hover:opacity-90 transition-opacity"
          )}
        >
          <Plus size={15} />
          Add Credential
        </button>
      </div>

      {/* Table */}
      <div className="rounded-xl border border-[var(--border)] overflow-hidden bg-[var(--bg-panel)]">
        {isLoading ? (
          <table className="w-full">
            <thead>
              <tr className="border-b border-[var(--border)]">
                {["Name", "Type", "Bot", "Created", "Last Used", ""].map((h) => (
                  <th
                    key={h}
                    className="text-left px-5 py-3 text-[11px] font-semibold uppercase tracking-wide text-[var(--text-muted)]"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: 4 }).map((_, i) => (
                <TableRowSkeleton key={i} />
              ))}
            </tbody>
          </table>
        ) : credentials.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Key size={32} className="text-[var(--text-muted)] opacity-30 mb-3" />
            <p className="text-[14px] font-medium text-[var(--text-primary)]">
              No credentials yet
            </p>
            <p className="text-[12px] text-[var(--text-muted)] mt-1">
              Add your first credential to use in HTTP Request nodes
            </p>
            <button
              type="button"
              onClick={() => setSheetOpen(true)}
              className={cn(
                "mt-4 flex items-center gap-2 px-4 py-2 rounded-lg",
                "bg-[var(--accent)] text-white text-[13px] font-medium",
                "hover:opacity-90 transition-opacity"
              )}
            >
              <Plus size={14} />
              Add Credential
            </button>
          </div>
        ) : (
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-[var(--border)]">
                {["Name", "Type", "Bot", "Created", "Last Used", ""].map((h) => (
                  <th
                    key={h}
                    className="text-left px-5 py-3 text-[11px] font-semibold uppercase tracking-wide text-[var(--text-muted)]"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {credentials.map((cred) => {
                const tc = TYPE_COLORS[cred.type] ?? TYPE_COLORS.CUSTOM;
                return (
                  <tr
                    key={cred.id}
                    className="border-b border-[var(--border)] last:border-b-0 hover:bg-[var(--bg-surface)] transition-colors"
                  >
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <Key size={13} className="text-[var(--text-muted)] flex-shrink-0" />
                        <span className="font-medium text-[var(--text-primary)]">
                          {cred.name}
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <span
                        className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold"
                        style={{ color: tc.color, background: tc.bg }}
                      >
                        {CRED_TYPES.find((t) => t.value === cred.type)?.label ?? cred.type}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-[var(--text-secondary)]">
                      {cred.botName ?? cred.botId.slice(0, 8)}
                    </td>
                    <td className="px-5 py-4 text-[var(--text-muted)]">
                      {formatDate(cred.createdAt)}
                    </td>
                    <td className="px-5 py-4 text-[var(--text-muted)]">
                      {cred.lastUsedAt ? formatDate(cred.lastUsedAt) : "Never"}
                    </td>
                    <td className="px-5 py-4">
                      <button
                        type="button"
                        onClick={() => setDeleteTarget(cred)}
                        className="p-1.5 rounded-lg text-[var(--text-muted)] hover:text-[var(--danger)] hover:bg-[var(--danger)]/10 transition-colors"
                        aria-label={`Delete ${cred.name}`}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Add sheet */}
      <AddCredentialSheet
        open={sheetOpen}
        onClose={() => setSheetOpen(false)}
        bots={bots}
        onSave={createCredential.mutateAsync}
      />

      {/* Delete confirm */}
      <ConfirmDeleteDialog
        credential={deleteTarget}
        onConfirm={() => deleteTarget && deleteCredential.mutate(deleteTarget)}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
