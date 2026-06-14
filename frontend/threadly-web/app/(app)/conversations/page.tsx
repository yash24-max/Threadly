"use client";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { Centrifuge } from "centrifuge";
import { motion, AnimatePresence } from "framer-motion";
import {
  MessageSquare,
  User2,
  Bot as BotIcon,
  Send,
  UserCheck,
  Search,
  ChevronDown,
  Tag,
  Clock,
  Info,
} from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import type { Conversation, Message } from "@/lib/types";
import { formatDate, formatRelative, cn } from "@/lib/utils";
import { useConversationStore } from "@/lib/stores/conversation-store";

const CENTRIFUGO_URL =
  process.env.NEXT_PUBLIC_CENTRIFUGO_URL ?? "ws://localhost:8000/connection/websocket";

type SortOrder = "newest" | "oldest" | "unread";
type StatusFilter = "all" | "open" | "closed" | "handed_off";

// ── Skeleton ──────────────────────────────────────────────────────────────────

function ConvRowSkeleton() {
  return (
    <div className="px-4 py-3 border-b border-[var(--border)] animate-pulse">
      <div className="flex gap-2 items-center mb-2">
        <div className="w-2 h-2 rounded-full bg-[var(--border)]" />
        <div className="h-3 w-32 rounded bg-[var(--border)]" />
        <div className="h-3 w-12 rounded bg-[var(--border)] ml-auto" />
      </div>
      <div className="h-3 w-48 rounded bg-[var(--border)] ml-4" />
    </div>
  );
}

// ── Status badge ──────────────────────────────────────────────────────────────

const STATUS_CONFIG: Record<
  string,
  { label: string; color: string; bg: string }
> = {
  OPEN: { label: "Open", color: "#10B981", bg: "rgba(16,185,129,0.12)" },
  CLOSED: { label: "Closed", color: "#6B7280", bg: "rgba(107,114,128,0.12)" },
  HANDED_OFF: { label: "Handed Off", color: "#F59E0B", bg: "rgba(245,158,11,0.12)" },
};

function StatusBadge({ status }: { status: string }) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.OPEN;
  return (
    <span
      className="inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full"
      style={{ color: cfg.color, background: cfg.bg }}
    >
      <span
        className="w-1.5 h-1.5 rounded-full flex-shrink-0"
        style={{ background: cfg.color }}
      />
      {cfg.label}
    </span>
  );
}

// ── Typing indicator ──────────────────────────────────────────────────────────

function TypingIndicator() {
  return (
    <div className="flex items-center gap-2 px-3 py-2 max-w-[80px]">
      <div className="flex gap-1 items-end h-4">
        {[0, 1, 2].map((i) => (
          <span
            key={i}
            className="w-1.5 h-1.5 rounded-full bg-[var(--accent)]"
            style={{
              animation: `bounce 1.2s ease-in-out ${i * 0.2}s infinite`,
            }}
          />
        ))}
      </div>
    </div>
  );
}

// ── Message bubble ────────────────────────────────────────────────────────────

interface BubbleProps {
  msg: Message;
  botName?: string;
}

function MessageBubble({ msg, botName }: BubbleProps) {
  const isUser = msg.role === "user";
  const isAgent = msg.role === "system"; // system = human agent in our model

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2 }}
      className={cn(
        "flex gap-2.5 max-w-[80%]",
        isUser ? "flex-row-reverse self-end" : "self-start"
      )}
    >
      {/* Avatar */}
      <div
        className={cn(
          "w-7 h-7 rounded-full flex-shrink-0 flex items-center justify-center text-[12px] font-bold",
          isUser
            ? "bg-[var(--bg-surface)] border border-[var(--border)] text-[var(--text-muted)]"
            : isAgent
            ? "bg-[#10B981] text-white"
            : "bg-[var(--accent)] text-white"
        )}
        aria-hidden="true"
      >
        {isUser ? (
          <User2 size={13} />
        ) : isAgent ? (
          "A"
        ) : (
          (botName?.[0] ?? <BotIcon size={13} />)
        )}
      </div>

      {/* Bubble */}
      <div
        className={cn(
          "px-3.5 py-2.5 rounded-2xl text-[13px] leading-relaxed",
          isUser
            ? "bg-[var(--bg-surface)] border border-[var(--border)] text-[var(--text-primary)] rounded-tr-sm"
            : isAgent
            ? "bg-[#10B981] text-white rounded-tl-sm"
            : "bg-[var(--accent)] text-white rounded-tl-sm"
        )}
      >
        <p className="whitespace-pre-wrap break-words">{msg.content}</p>
        <p
          className={cn(
            "text-[10px] mt-1",
            isUser || isAgent ? "text-right" : "text-left",
            isUser
              ? "text-[var(--text-muted)]"
              : "opacity-70"
          )}
        >
          {formatDate(msg.createdAt)}
        </p>
      </div>
    </motion.div>
  );
}

// ── Main inner component ──────────────────────────────────────────────────────

function ConversationsInner() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const searchParams = useSearchParams();
  const qc = useQueryClient();
  const bottomRef = useRef<HTMLDivElement>(null);

  const { selectedId, setSelectedId, isTyping } = useConversationStore();

  // init from URL param
  useEffect(() => {
    const urlId = searchParams.get("id");
    if (urlId) setSelectedId(urlId);
  }, []); // eslint-disable-line

  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");
  const [sortOrder, setSortOrder] = useState<SortOrder>("newest");
  const [tags, setTags] = useState("");
  const [notes, setNotes] = useState("");
  const [replyText, setReplyText] = useState("");

  // ── Queries ────────────────────────────────────────────────────────────────

  const { data: conversations, isLoading } = useQuery<Conversation[]>({
    queryKey: ["conversations"],
    queryFn: () => api.get("/v1/conversations?size=100", token),
    enabled: !!token,
    refetchInterval: 30_000,
    select: (data: unknown) => {
      const raw = data as { content?: Conversation[] } | Conversation[];
      return Array.isArray(raw) ? raw : (raw as { content?: Conversation[] }).content ?? [];
    },
  });

  const convList = conversations ?? [];

  const { data: messages, isLoading: messagesLoading } = useQuery<Message[]>({
    queryKey: ["conversations", selectedId, "messages"],
    queryFn: () => api.get(`/v1/conversations/${selectedId}/messages`, token),
    enabled: !!token && !!selectedId,
  });

  const selected = convList.find((c) => c.id === selectedId) ?? null;

  // ── Centrifugo live updates ────────────────────────────────────────────────

  useEffect(() => {
    if (!token || !session?.user?.orgId) return;
    const orgId = session.user.orgId;

    const fetchToken = async () => {
      const d = await api.get<{ token: string }>("/v1/realtime/token", token);
      return d.token;
    };

    const c = new Centrifuge(CENTRIFUGO_URL, { getToken: fetchToken });
    const sub = c.newSubscription(`dashboard:${orgId}`);

    sub.on("publication", ({ data }) => {
      if (data.type === "new_conversation" || data.type === "conversation_update") {
        qc.invalidateQueries({ queryKey: ["conversations"] });
      }
      if (data.type === "new_message" && data.conversationId === selectedId) {
        qc.invalidateQueries({ queryKey: ["conversations", selectedId, "messages"] });
      }
    });

    sub.subscribe();
    c.connect();
    return () => c.disconnect();
  }, [token, session?.user?.orgId, selectedId]); // eslint-disable-line

  // Auto scroll to bottom
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // ── Mutations ──────────────────────────────────────────────────────────────

  const sendReply = useMutation({
    mutationFn: () =>
      api.post(`/v1/conversations/${selectedId}/messages`, { content: replyText }, token),
    onSuccess: () => {
      setReplyText("");
      qc.invalidateQueries({ queryKey: ["conversations", selectedId, "messages"] });
    },
    onError: () => toast.error("Failed to send message"),
  });

  const takeOver = useMutation({
    mutationFn: (convId: string) =>
      api.post(`/v1/conversations/${convId}/handoff`, {}, token),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["conversations"] });
      toast.success("Conversation assigned to you");
    },
  });

  const resumeAi = useMutation({
    mutationFn: (convId: string) =>
      api.post(`/v1/conversations/${convId}/resume`, {}, token),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["conversations"] });
      toast.success("AI resumed");
    },
  });

  const closeConv = useMutation({
    mutationFn: (convId: string) =>
      api.post(`/v1/conversations/${convId}/close`, {}, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["conversations"] }),
  });

  const saveTags = useCallback(
    (value: string) => {
      if (!selectedId) return;
      api
        .patch(`/v1/conversations/${selectedId}`, { tags: value.split(",").map((t) => t.trim()) }, token)
        .catch(() => {});
    },
    [selectedId, token]
  );

  const saveNotes = useCallback(
    (value: string) => {
      if (!selectedId) return;
      api
        .patch(`/v1/conversations/${selectedId}`, { agentNotes: value }, token)
        .catch(() => {});
    },
    [selectedId, token]
  );

  // ── Filter + sort ──────────────────────────────────────────────────────────

  const filteredConversations = convList
    .filter((c) => {
      const matchSearch =
        !searchQuery ||
        c.visitorId.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.id.toLowerCase().includes(searchQuery.toLowerCase());
      const matchStatus =
        statusFilter === "all" || c.status.toLowerCase() === statusFilter;
      return matchSearch && matchStatus;
    })
    .sort((a, b) => {
      if (sortOrder === "oldest")
        return new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });

  const STATUS_TABS: { value: StatusFilter; label: string }[] = [
    { value: "all", label: "All" },
    { value: "open", label: "Open" },
    { value: "closed", label: "Closed" },
    { value: "handed_off", label: "Handed Off" },
  ];

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div
      className="flex h-full overflow-hidden"
      aria-label="Conversations inbox"
    >
      {/* ─── LEFT PANE ───────────────────────────────────────────────────────── */}
      <div className="w-[280px] flex-shrink-0 border-r border-[var(--border)] flex flex-col overflow-hidden bg-[var(--bg-panel)]">
        {/* Status filter tabs */}
        <div className="border-b border-[var(--border)] px-3 pt-3 pb-0">
          <div className="flex gap-0.5">
            {STATUS_TABS.map((tab) => (
              <button
                key={tab.value}
                type="button"
                onClick={() => setStatusFilter(tab.value)}
                className={cn(
                  "flex-1 py-2 text-[11px] font-semibold rounded-t-md transition-colors",
                  statusFilter === tab.value
                    ? "bg-[var(--bg-surface)] text-[var(--accent)] border-b-2 border-[var(--accent)]"
                    : "text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Search + sort */}
        <div className="px-3 py-2.5 border-b border-[var(--border)] space-y-2">
          <div className="relative">
            <Search
              size={12}
              className="absolute left-2.5 top-1/2 -translate-y-1/2 text-[var(--text-muted)] pointer-events-none"
            />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search conversations…"
              className={cn(
                "w-full pl-7 pr-3 py-1.5 text-[12px] rounded-md",
                "bg-[var(--bg-surface)] border border-[var(--border)]",
                "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
                "outline-none focus:border-[var(--accent)] transition-colors"
              )}
              aria-label="Search conversations"
            />
          </div>

          <div className="flex items-center justify-end">
            <div className="relative">
              <select
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value as SortOrder)}
                className={cn(
                  "appearance-none pl-2 pr-6 py-1 text-[11px] rounded-md",
                  "bg-[var(--bg-surface)] border border-[var(--border)]",
                  "text-[var(--text-muted)] outline-none cursor-pointer"
                )}
                aria-label="Sort order"
              >
                <option value="newest">Newest first</option>
                <option value="oldest">Oldest first</option>
                <option value="unread">Unread first</option>
              </select>
              <ChevronDown
                size={10}
                className="absolute right-1.5 top-1/2 -translate-y-1/2 text-[var(--text-muted)] pointer-events-none"
              />
            </div>
          </div>
        </div>

        {/* Conversation list */}
        <div className="flex-1 overflow-y-auto">
          {isLoading ? (
            Array.from({ length: 6 }).map((_, i) => <ConvRowSkeleton key={i} />)
          ) : filteredConversations.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center p-6 text-[var(--text-muted)]">
              <MessageSquare size={28} className="mb-3 opacity-20" aria-hidden="true" />
              <p className="text-[13px]">No conversations found</p>
            </div>
          ) : (
            <AnimatePresence>
              {filteredConversations.map((conv) => (
                <motion.button
                  key={conv.id}
                  layout
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -10 }}
                  transition={{ duration: 0.15 }}
                  type="button"
                  onClick={() => setSelectedId(conv.id)}
                  className={cn(
                    "w-full text-left px-4 py-3 border-b border-[var(--border)]",
                    "transition-colors hover:bg-[var(--bg-surface)]",
                    selectedId === conv.id
                      ? "bg-[var(--bg-surface)] border-l-2 border-l-[var(--accent)]"
                      : "border-l-2 border-l-transparent"
                  )}
                  aria-pressed={selectedId === conv.id}
                >
                  <div className="flex items-start gap-2 mb-1">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5">
                        <p className="text-[12px] font-semibold text-[var(--text-primary)] truncate">
                          {conv.botId ? `Bot · ` : ""}
                          Visitor {conv.visitorId.slice(0, 8)}
                        </p>
                      </div>
                      {conv.lastMessage && (
                        <p className="text-[11px] text-[var(--text-muted)] truncate mt-0.5">
                          {conv.lastMessage}
                        </p>
                      )}
                    </div>
                    <div className="flex flex-col items-end gap-1 flex-shrink-0">
                      <span className="text-[10px] text-[var(--text-muted)]">
                        {formatRelative(conv.updatedAt)}
                      </span>
                      <StatusBadge status={conv.status} />
                    </div>
                  </div>
                </motion.button>
              ))}
            </AnimatePresence>
          )}
        </div>
      </div>

      {/* ─── CENTER PANE ─────────────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col overflow-hidden bg-[var(--bg-canvas)]">
        {!selectedId || !selected ? (
          <div className="flex flex-col items-center justify-center h-full text-center text-[var(--text-muted)] gap-4">
            <MessageSquare size={48} className="opacity-10" aria-hidden="true" />
            <div>
              <p className="text-[15px] font-medium">Select a conversation</p>
              <p className="text-[12px] mt-1 opacity-60">
                Choose from the list on the left
              </p>
            </div>
          </div>
        ) : (
          <>
            {/* Transcript header */}
            <div className="px-5 py-3 border-b border-[var(--border)] bg-[var(--bg-panel)] flex items-center gap-3">
              <div className="flex-1 min-w-0">
                <p className="text-[14px] font-semibold text-[var(--text-primary)] truncate">
                  Visitor {selected.visitorId.slice(0, 12)}
                </p>
                <p className="text-[11px] text-[var(--text-muted)]">
                  {selected.messageCount} messages
                </p>
              </div>
              <div className="flex items-center gap-2">
                <StatusBadge status={selected.status} />
                {selected.status === "OPEN" ? (
                  <button
                    type="button"
                    onClick={() => takeOver.mutate(selected.id)}
                    disabled={takeOver.isPending}
                    className={cn(
                      "flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[12px] font-medium",
                      "bg-[#F59E0B] text-white hover:bg-[#D97706] transition-colors"
                    )}
                  >
                    <UserCheck size={13} />
                    Take Over
                  </button>
                ) : selected.status === "HANDED_OFF" ? (
                  <button
                    type="button"
                    onClick={() => resumeAi.mutate(selected.id)}
                    disabled={resumeAi.isPending}
                    className={cn(
                      "flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[12px] font-medium",
                      "bg-[var(--accent)] text-white hover:opacity-90 transition-opacity"
                    )}
                  >
                    Resume AI
                  </button>
                ) : null}
                {selected.status !== "CLOSED" && (
                  <button
                    type="button"
                    onClick={() => closeConv.mutate(selected.id)}
                    className={cn(
                      "px-3 py-1.5 rounded-lg text-[12px] font-medium transition-colors",
                      "bg-[var(--bg-surface)] border border-[var(--border)]",
                      "text-[var(--text-secondary)] hover:border-[var(--border-strong)]"
                    )}
                  >
                    Close
                  </button>
                )}
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto px-5 py-5 flex flex-col gap-3">
              {messagesLoading ? (
                <div className="flex-1 flex items-center justify-center">
                  <div className="animate-pulse text-[12px] text-[var(--text-muted)]">
                    Loading messages…
                  </div>
                </div>
              ) : (
                <>
                  {messages?.map((msg) => (
                    <MessageBubble
                      key={msg.id}
                      msg={msg}
                      botName={selected.botId?.slice(0, 1).toUpperCase()}
                    />
                  ))}
                  {isTyping[selectedId] && (
                    <div className="self-start">
                      <TypingIndicator />
                    </div>
                  )}
                </>
              )}
              <div ref={bottomRef} />
            </div>

            {/* Reply input (only in handed-off mode) */}
            {selected.status === "HANDED_OFF" && (
              <div className="px-5 py-3 border-t border-[var(--border)] bg-[var(--bg-panel)] flex gap-2.5 items-end">
                <textarea
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  placeholder="Type a reply… (Enter to send)"
                  rows={2}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      if (replyText.trim()) sendReply.mutate();
                    }
                  }}
                  className={cn(
                    "flex-1 px-3 py-2 text-[13px] rounded-lg resize-none",
                    "bg-[var(--bg-surface)] border border-[var(--border)]",
                    "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
                    "outline-none focus:border-[var(--accent)] transition-colors font-[inherit]"
                  )}
                  aria-label="Reply message"
                />
                <button
                  type="button"
                  onClick={() => replyText.trim() && sendReply.mutate()}
                  disabled={!replyText.trim() || sendReply.isPending}
                  className={cn(
                    "w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0",
                    "bg-[var(--accent)] text-white transition-opacity",
                    "disabled:opacity-40 hover:opacity-90"
                  )}
                  aria-label="Send reply"
                >
                  <Send size={15} />
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {/* ─── RIGHT PANE ──────────────────────────────────────────────────────── */}
      <div className="w-[240px] flex-shrink-0 border-l border-[var(--border)] flex flex-col overflow-y-auto bg-[var(--bg-panel)]">
        {selected ? (
          <div className="p-4 space-y-5">
            {/* Visitor info */}
            <section aria-label="Visitor information">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
                Visitor
              </p>
              <div className="space-y-1.5">
                <div className="flex items-center gap-2">
                  <Info size={12} className="text-[var(--text-muted)] flex-shrink-0" />
                  <div>
                    <p className="text-[11px] text-[var(--text-muted)]">ID</p>
                    <p className="text-[12px] font-mono text-[var(--text-primary)] break-all">
                      {selected.visitorId}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Clock size={12} className="text-[var(--text-muted)] flex-shrink-0" />
                  <div>
                    <p className="text-[11px] text-[var(--text-muted)]">First seen</p>
                    <p className="text-[12px] text-[var(--text-primary)]">
                      {formatDate(selected.createdAt)}
                    </p>
                  </div>
                </div>
              </div>
            </section>

            {/* Conversation info */}
            <section aria-label="Conversation details">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
                Conversation
              </p>
              <div className="space-y-2">
                <div>
                  <p className="text-[11px] text-[var(--text-muted)]">Status</p>
                  <StatusBadge status={selected.status} />
                </div>
                <div>
                  <p className="text-[11px] text-[var(--text-muted)]">Duration</p>
                  <p className="text-[12px] text-[var(--text-primary)]">
                    {formatDate(selected.createdAt)} → now
                  </p>
                </div>
                {selected.botId && (
                  <div>
                    <p className="text-[11px] text-[var(--text-muted)]">Bot ID</p>
                    <p className="text-[12px] font-mono text-[var(--text-primary)] truncate">
                      {selected.botId}
                    </p>
                  </div>
                )}
              </div>
            </section>

            {/* Tags */}
            <section aria-label="Conversation tags">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2 flex items-center gap-1">
                <Tag size={10} />
                Tags
              </p>
              <input
                type="text"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                onBlur={(e) => saveTags(e.target.value)}
                placeholder="urgent, vip, billing…"
                className={cn(
                  "w-full px-2.5 py-1.5 text-[12px] rounded-md",
                  "bg-[var(--bg-surface)] border border-[var(--border)]",
                  "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
                  "outline-none focus:border-[var(--accent)] transition-colors"
                )}
                aria-label="Conversation tags (comma separated)"
              />
              <p className="text-[10px] text-[var(--text-muted)] mt-1">
                Comma-separated, saved on blur
              </p>
            </section>

            {/* Agent notes */}
            <section aria-label="Agent notes">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
                Agent Notes
              </p>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                onBlur={(e) => saveNotes(e.target.value)}
                rows={4}
                placeholder="Internal notes about this conversation…"
                className={cn(
                  "w-full px-2.5 py-2 text-[12px] rounded-md resize-none",
                  "bg-[var(--bg-surface)] border border-[var(--border)]",
                  "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
                  "outline-none focus:border-[var(--accent)] transition-colors font-[inherit]"
                )}
                aria-label="Agent notes, auto-saved on blur"
              />
            </section>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center p-4">
            <p className="text-[12px] text-[var(--text-muted)] text-center">
              Select a conversation to see details
            </p>
          </div>
        )}
      </div>

      {/* Keyframe for typing dots */}
      <style>{`
        @keyframes bounce {
          0%, 100% { transform: translateY(0); opacity: 0.4; }
          50% { transform: translateY(-4px); opacity: 1; }
        }
      `}</style>
    </div>
  );
}

// ── Page export ───────────────────────────────────────────────────────────────

export default function ConversationsPage() {
  return (
    <Suspense>
      <ConversationsInner />
    </Suspense>
  );
}
