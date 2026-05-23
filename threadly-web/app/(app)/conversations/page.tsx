"use client";

import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useRef, useState } from "react";
import { Centrifuge } from "centrifuge";
import { api } from "@/lib/api";
import type { Conversation, Message, Bot } from "@/lib/types";
import { formatDate, formatRelative, cn } from "@/lib/utils";
import { MessageSquare, User2, Bot as BotIcon, Send, Loader2, UserCheck } from "lucide-react";

const CENTRIFUGO_URL = process.env.NEXT_PUBLIC_CENTRIFUGO_URL ?? "ws://localhost:8000/connection/websocket";

function ConversationsInner() {
  const { data: session } = useSession();
  const token = session?.accessToken;
  const params = useSearchParams();
  const qc = useQueryClient();
  const bottomRef = useRef<HTMLDivElement>(null);

  const [selectedId, setSelectedId] = useState<string | null>(params.get("id"))
  const [replyText, setReplyText] = useState("")
  const [centrifuge, setCentrifuge] = useState<Centrifuge | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")

  const { data: conversations, isLoading } = useQuery<any>({
    queryKey: ["conversations"],
    queryFn: () => api.get("/v1/conversations?size=50", token),
    enabled: !!token,
    refetchInterval: 30_000,
  })

  const convList: Conversation[] = conversations?.content ?? conversations ?? []

  const filteredConversations = convList.filter((c: Conversation) => {
    const matchesSearch = !searchQuery || c.visitorId.includes(searchQuery) ||
      c.id.includes(searchQuery)
    const matchesStatus = statusFilter === "all" || c.status?.toLowerCase() === statusFilter
    return matchesSearch && matchesStatus
  })

  const { data: messages } = useQuery<Message[]>({
    queryKey: ["conversations", selectedId, "messages"],
    queryFn: () => api.get(`/v1/conversations/${selectedId}/messages`, token),
    enabled: !!token && !!selectedId,
  });

  const selected = convList.find((c) => c.id === selectedId);

  // Centrifugo for live updates
  useEffect(() => {
    if (!token) return;
    const orgId = session?.user.orgId;
    if (!orgId) return;

    const fetchToken = async () => {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/realtime/token`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const data = await res.json();
      return data.token;
    };

    const c = new Centrifuge(CENTRIFUGO_URL, {
      getToken: fetchToken,
    });

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
    setCentrifuge(c);
    return () => c.disconnect();
  }, [token, session?.user.orgId, selectedId]);

  // Scroll to bottom on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const sendReply = useMutation({
    mutationFn: async () => {
      await api.post(`/v1/conversations/${selectedId}/messages`, { content: replyText }, token);
    },
    onSuccess: () => {
      setReplyText("");
      qc.invalidateQueries({ queryKey: ["conversations", selectedId, "messages"] });
    },
  });

  const takeOver = useMutation({
    mutationFn: (convId: string) => api.post(`/v1/conversations/${convId}/handoff`, {}, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["conversations"] }),
  });

  const closeConv = useMutation({
    mutationFn: (convId: string) => api.post(`/v1/conversations/${convId}/close`, {}, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["conversations"] }),
  });

  const statusColor = (s: Conversation["status"]) =>
    s === "OPEN" ? "var(--success)" : s === "HANDED_OFF" ? "var(--warn)" : "var(--text-muted)";

  return (
    <div style={{ display: "flex", height: "100%", overflow: "hidden" }}>
      {/* List pane */}
      <div style={{
        width: 320, borderRight: "1px solid var(--border)",
        display: "flex", flexDirection: "column", overflow: "hidden",
      }}>
        <div style={{ padding: "16px", borderBottom: "1px solid var(--border)" }}>
          <h2 style={{ fontSize: 17, fontWeight: 600, marginBottom: 10 }}>Conversations</h2>
          <input
            placeholder="Search by visitor ID…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{
              width: "100%", background: "var(--bg-surface)", border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)", padding: "8px 12px",
              color: "var(--text-primary)", fontSize: 13, outline: "none", marginBottom: 8,
            }}
          />
          <div style={{ display: "flex", gap: 6 }}>
            {["all", "open", "handed_off", "closed"].map((s) => (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                style={{
                  padding: "3px 9px", borderRadius: "var(--radius-full)", fontSize: 11, cursor: "pointer",
                  border: `1px solid ${statusFilter === s ? "var(--accent)" : "var(--border)"}`,
                  background: statusFilter === s ? "rgba(79,70,229,.12)" : "transparent",
                  color: statusFilter === s ? "var(--accent)" : "var(--text-muted)",
                  textTransform: "capitalize",
                }}
              >
                {s === "all" ? "All" : s === "handed_off" ? "Agent" : s}
              </button>
            ))}
          </div>
        </div>

        <div style={{ flex: 1, overflow: "auto" }}>
          {isLoading ? (
            <div style={{ display: "flex", justifyContent: "center", paddingTop: 48 }}>
              <Loader2 size={20} style={{ color: "var(--text-muted)", animation: "spin 1s linear infinite" }} />
            </div>
          ) : !conversations?.length ? (
            <div style={{ textAlign: "center", padding: "48px 16px", color: "var(--text-muted)", fontSize: 14 }}>
              <MessageSquare size={28} style={{ margin: "0 auto 12px", opacity: 0.3 }} />
              No conversations yet
            </div>
          ) : (
            filteredConversations.map((conv) => (
              <button
                key={conv.id}
                onClick={() => setSelectedId(conv.id)}
                style={{
                  display: "block", width: "100%", textAlign: "left",
                  padding: "14px 16px",
                  background: selectedId === conv.id ? "var(--bg-surface)" : "transparent",
                  border: "none", borderBottom: "1px solid var(--border)",
                  cursor: "pointer", color: "var(--text-primary)",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                  <span style={{
                    width: 8, height: 8, borderRadius: "50%", flexShrink: 0,
                    background: statusColor(conv.status),
                  }} />
                  <span style={{ fontSize: 13, fontWeight: 500, flex: 1, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    Visitor {conv.visitorId.slice(0, 8)}
                  </span>
                  <span style={{ fontSize: 11, color: "var(--text-muted)", flexShrink: 0 }}>
                    {formatRelative(conv.updatedAt)}
                  </span>
                </div>
                {conv.lastMessage && (
                  <p style={{ fontSize: 12, color: "var(--text-secondary)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", paddingLeft: 16 }}>
                    {conv.lastMessage}
                  </p>
                )}
              </button>
            ))
          )}
        </div>
      </div>

      {/* Transcript pane */}
      {selectedId && selected ? (
        <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
          {/* Transcript header */}
          <div style={{
            padding: "16px 24px", borderBottom: "1px solid var(--border)",
            display: "flex", alignItems: "center", gap: 12,
          }}>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: 14, fontWeight: 600 }}>Visitor {selected.visitorId.slice(0, 8)}</p>
              <p style={{ fontSize: 12, color: "var(--text-secondary)" }}>
                {selected.messageCount} messages · {selected.status.toLowerCase().replace("_", " ")}
              </p>
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              {selected.status === "OPEN" && (
                <button
                  onClick={() => takeOver.mutate(selected.id)}
                  style={{
                    display: "flex", alignItems: "center", gap: 6,
                    padding: "7px 14px", borderRadius: "var(--radius-md)",
                    background: "var(--warn)", color: "#fff",
                    border: "none", cursor: "pointer", fontSize: 13, fontWeight: 500,
                  }}
                >
                  <UserCheck size={14} />
                  Take Over
                </button>
              )}
              {selected.status !== "CLOSED" && (
                <button
                  onClick={() => closeConv.mutate(selected.id)}
                  style={{
                    padding: "7px 14px", borderRadius: "var(--radius-md)",
                    background: "var(--bg-surface)", border: "1px solid var(--border)",
                    color: "var(--text-secondary)", cursor: "pointer", fontSize: 13,
                  }}
                >
                  Close
                </button>
              )}
            </div>
          </div>

          {/* Messages */}
          <div style={{ flex: 1, overflow: "auto", padding: "20px 24px", display: "flex", flexDirection: "column", gap: 12 }}>
            {messages?.map((msg) => (
              <div
                key={msg.id}
                style={{
                  display: "flex", gap: 10,
                  flexDirection: msg.role === "user" ? "row" : "row-reverse",
                }}
              >
                <div style={{
                  width: 28, height: 28, borderRadius: "50%", flexShrink: 0,
                  background: msg.role === "user" ? "var(--bg-surface)" : "var(--accent)",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  border: "1px solid var(--border)",
                }}>
                  {msg.role === "user"
                    ? <User2 size={14} color="var(--text-muted)" />
                    : <BotIcon size={14} color="#fff" />
                  }
                </div>
                <div style={{
                  maxWidth: "70%",
                  background: msg.role === "user" ? "var(--bg-surface)" : "var(--accent)",
                  color: msg.role === "user" ? "var(--text-primary)" : "#fff",
                  padding: "10px 14px", borderRadius: "var(--radius-lg)",
                  fontSize: 14, lineHeight: 1.5,
                  border: msg.role === "user" ? "1px solid var(--border)" : "none",
                }}>
                  {msg.content}
                  <p style={{
                    fontSize: 10, marginTop: 4, opacity: 0.6, textAlign: "right",
                  }}>
                    {formatDate(msg.createdAt)}
                  </p>
                </div>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>

          {/* Reply (only when handed off to agent) */}
          {selected.status === "HANDED_OFF" && (
            <div style={{
              padding: "16px 24px", borderTop: "1px solid var(--border)",
              display: "flex", gap: 10, alignItems: "flex-end",
            }}>
              <textarea
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                placeholder="Type a reply…"
                rows={2}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    if (replyText.trim()) sendReply.mutate();
                  }
                }}
                style={{
                  flex: 1, padding: "10px 14px",
                  background: "var(--bg-surface)", border: "1px solid var(--border)",
                  borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                  fontSize: 14, resize: "none", outline: "none",
                  fontFamily: "inherit",
                }}
              />
              <button
                onClick={() => replyText.trim() && sendReply.mutate()}
                disabled={!replyText.trim() || sendReply.isPending}
                style={{
                  width: 40, height: 40, borderRadius: "var(--radius-md)",
                  background: "var(--accent)", border: "none",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  cursor: "pointer",
                }}
              >
                <Send size={16} color="#fff" />
              </button>
            </div>
          )}
        </div>
      ) : (
        <div style={{
          flex: 1, display: "flex", alignItems: "center", justifyContent: "center",
          color: "var(--text-muted)", flexDirection: "column", gap: 12,
        }}>
          <MessageSquare size={40} style={{ opacity: 0.2 }} />
          <p style={{ fontSize: 14 }}>Select a conversation to view the transcript</p>
        </div>
      )}
    </div>
  );
}

export default function ConversationsPage() {
  return (
    <Suspense>
      <ConversationsInner />
    </Suspense>
  );
}
