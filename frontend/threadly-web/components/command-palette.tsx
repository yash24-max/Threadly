"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import { AnimatePresence, motion } from "framer-motion";
import {
  LayoutDashboard,
  Bot,
  MessageSquare,
  BookOpen,
  Settings,
  BarChart2,
  Play,
  Upload,
  Database,
  Cog,
  Plus,
  UserPlus,
  Clock,
  Search,
} from "lucide-react";
import { api } from "@/lib/api";
import type { Bot as BotType } from "@/lib/types";
import { cn } from "@/lib/utils";

const RECENT_BOTS_KEY = "tly_recent_bots";
const MAX_RECENT_BOTS = 5;

// ── Recent bots localStorage helpers ─────────────────────────────────────────

interface RecentBot {
  id: string;
  name: string;
}

function getRecentBots(): RecentBot[] {
  if (typeof window === "undefined") return [];
  try {
    return JSON.parse(localStorage.getItem(RECENT_BOTS_KEY) ?? "[]");
  } catch {
    return [];
  }
}

export function recordRecentBot(bot: RecentBot) {
  const recent = getRecentBots().filter((b) => b.id !== bot.id);
  recent.unshift(bot);
  localStorage.setItem(RECENT_BOTS_KEY, JSON.stringify(recent.slice(0, MAX_RECENT_BOTS)));
}

// ── Command types ─────────────────────────────────────────────────────────────

type CommandGroup = {
  id: string;
  label: string;
  items: CommandItem[];
};

type CommandItem = {
  id: string;
  label: string;
  description?: string;
  icon: React.ReactNode;
  shortcut?: string;
  action: () => void;
};

// ── Kbd component ─────────────────────────────────────────────────────────────

function Kbd({ children }: { children: React.ReactNode }) {
  return (
    <kbd
      className={cn(
        "inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-mono",
        "border border-[var(--border)] bg-[var(--bg-surface)] text-[var(--text-muted)]"
      )}
    >
      {children}
    </kbd>
  );
}

// ── Main palette ──────────────────────────────────────────────────────────────

export function CommandPalette() {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();
  const pathname = usePathname();
  const { data: session } = useSession();
  const token = session?.accessToken;

  const isInBuilder =
    pathname.includes("/builder/") || pathname.includes("/bots/");

  const { data: bots } = useQuery<BotType[]>({
    queryKey: ["bots"],
    queryFn: () => api.get("/v1/bots", token),
    enabled: !!token && open,
    staleTime: 30_000,
  });

  // Extract current botId from URL if in builder
  const currentBotId = pathname.match(/\/(?:builder|bots)\/([^/]+)/)?.[1];

  // ── Open / close ────────────────────────────────────────────────────────────

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setOpen((o) => !o);
        setQuery("");
        setSelected(0);
      }
      if (e.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, []);

  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 20);
    }
  }, [open]);

  // ── Build command groups ────────────────────────────────────────────────────

  const recentBots = getRecentBots();

  const groups: CommandGroup[] = [
    {
      id: "navigate",
      label: "Navigate",
      items: [
        {
          id: "nav-dashboard",
          label: "Dashboard",
          icon: <LayoutDashboard size={15} />,
          shortcut: "G D",
          action: () => router.push("/dashboard"),
        },
        {
          id: "nav-bots",
          label: "All Bots",
          icon: <Bot size={15} />,
          shortcut: "G B",
          action: () => router.push("/bots"),
        },
        {
          id: "nav-conversations",
          label: "Conversations",
          icon: <MessageSquare size={15} />,
          shortcut: "G C",
          action: () => router.push("/conversations"),
        },
        {
          id: "nav-knowledge",
          label: "Knowledge Base",
          icon: <BookOpen size={15} />,
          shortcut: "G K",
          action: () => router.push("/knowledge"),
        },
        {
          id: "nav-settings",
          label: "Settings",
          icon: <Settings size={15} />,
          shortcut: "G S",
          action: () => router.push("/settings"),
        },
        {
          id: "nav-analytics",
          label: "Analytics",
          icon: <BarChart2 size={15} />,
          action: () => router.push("/analytics"),
        },
      ],
    },
    ...(isInBuilder && currentBotId
      ? [
          {
            id: "bot-actions",
            label: "Bot Actions",
            items: [
              {
                id: "action-test",
                label: "Test Bot",
                description: "Open preview panel",
                icon: <Play size={15} />,
                shortcut: "⌘ T",
                action: () => {
                  // dispatch custom event that builder listens to
                  window.dispatchEvent(new CustomEvent("tly:test-bot"));
                },
              },
              {
                id: "action-publish",
                label: "Publish Flow",
                description: "Push current draft live",
                icon: <Upload size={15} />,
                shortcut: "⌘ ⇧ P",
                action: () => {
                  window.dispatchEvent(new CustomEvent("tly:publish-flow"));
                },
              },
              {
                id: "action-analytics",
                label: "View Analytics",
                icon: <BarChart2 size={15} />,
                action: () => router.push(`/bots/${currentBotId}/analytics`),
              },
              {
                id: "action-kb",
                label: "Manage Knowledge Base",
                icon: <Database size={15} />,
                action: () => router.push(`/knowledge?bot=${currentBotId}`),
              },
              {
                id: "action-settings",
                label: "Bot Settings",
                icon: <Cog size={15} />,
                action: () => router.push(`/bots/${currentBotId}/settings`),
              },
            ],
          },
        ]
      : []),
    {
      id: "create",
      label: "Create",
      items: [
        {
          id: "create-bot",
          label: "New Bot",
          description: "Open bot creation wizard",
          icon: <Plus size={15} />,
          shortcut: "⌘ N",
          action: () => router.push("/bots?create=1"),
        },
        {
          id: "invite-member",
          label: "Invite Team Member",
          icon: <UserPlus size={15} />,
          action: () => router.push("/settings/team"),
        },
      ],
    },
    ...(recentBots.length > 0
      ? [
          {
            id: "recent",
            label: "Recent Bots",
            items: recentBots.map((b) => ({
              id: `recent-${b.id}`,
              label: b.name,
              description: "Open in flow builder",
              icon: <Clock size={15} />,
              action: () => router.push(`/builder/${b.id}`),
            })),
          },
        ]
      : []),
    ...(bots && bots.length > 0
      ? [
          {
            id: "all-bots",
            label: "All Bots",
            items: bots.map((b) => ({
              id: `bot-${b.id}`,
              label: b.name,
              description: "Edit in flow builder",
              icon: <Bot size={15} />,
              action: () => {
                recordRecentBot({ id: b.id, name: b.name });
                router.push(`/builder/${b.id}`);
              },
            })),
          },
        ]
      : []),
  ];

  // ── Filter groups ───────────────────────────────────────────────────────────

  const searchLower = query.toLowerCase().trim();
  const filteredGroups = searchLower
    ? [
        {
          id: "results",
          label: "Results",
          items: groups
            .flatMap((g) => g.items)
            .filter(
              (item) =>
                item.label.toLowerCase().includes(searchLower) ||
                item.description?.toLowerCase().includes(searchLower)
            ),
        },
      ]
    : groups;

  const allItems = filteredGroups.flatMap((g) => g.items);

  // ── Keyboard navigation ─────────────────────────────────────────────────────

  const handleKey = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "ArrowDown") {
        e.preventDefault();
        setSelected((s) => Math.min(s + 1, allItems.length - 1));
      }
      if (e.key === "ArrowUp") {
        e.preventDefault();
        setSelected((s) => Math.max(s - 1, 0));
      }
      if (e.key === "Enter" && allItems[selected]) {
        allItems[selected].action();
        setOpen(false);
      }
    },
    [allItems, selected]
  );

  // ── Global selected index across groups ─────────────────────────────────────

  let globalIndex = 0;
  const itemToGlobalIndex = new Map<string, number>();
  for (const g of filteredGroups) {
    for (const item of g.items) {
      itemToGlobalIndex.set(item.id, globalIndex++);
    }
  }

  if (!open) return null;

  return (
    <AnimatePresence>
      <div
        className="fixed inset-0 z-[9999] flex items-start justify-center bg-black/50 backdrop-blur-sm pt-[15vh] px-4"
        onClick={() => setOpen(false)}
        role="presentation"
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: -10 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.96, y: -10 }}
          transition={{ duration: 0.15, ease: "easeOut" }}
          onClick={(e) => e.stopPropagation()}
          className={cn(
            "w-full max-w-[580px] rounded-2xl overflow-hidden shadow-2xl",
            "bg-[var(--bg-panel)] border border-[var(--border)]"
          )}
          role="dialog"
          aria-modal="true"
          aria-label="Command palette"
        >
          {/* Search input */}
          <div className="flex items-center gap-2.5 px-4 py-3.5 border-b border-[var(--border)]">
            <Search size={16} className="text-[var(--text-muted)] flex-shrink-0" />
            <input
              ref={inputRef}
              value={query}
              onChange={(e) => {
                setQuery(e.target.value);
                setSelected(0);
              }}
              onKeyDown={handleKey}
              placeholder="Type a command or search…"
              className="flex-1 bg-transparent border-none outline-none text-[15px] text-[var(--text-primary)] placeholder:text-[var(--text-muted)]"
              aria-label="Search commands"
              role="combobox"
              aria-expanded="true"
              aria-haspopup="listbox"
            />
            <Kbd>ESC</Kbd>
          </div>

          {/* Results */}
          <div
            className="max-h-[400px] overflow-y-auto py-2"
            role="listbox"
            aria-label="Commands"
          >
            {allItems.length === 0 ? (
              <div className="py-10 text-center text-[var(--text-muted)] text-[13px]">
                No results for &ldquo;{query}&rdquo;
              </div>
            ) : (
              filteredGroups.map((group) => {
                if (group.items.length === 0) return null;
                return (
                  <div key={group.id}>
                    {group.label !== "Results" && (
                      <p className="px-4 pt-3 pb-1 text-[10px] font-semibold uppercase tracking-wide text-[var(--text-muted)]">
                        {group.label}
                      </p>
                    )}
                    {group.items.map((item) => {
                      const idx = itemToGlobalIndex.get(item.id) ?? 0;
                      const isSelected = selected === idx;
                      return (
                        <button
                          key={item.id}
                          type="button"
                          onClick={() => {
                            item.action();
                            setOpen(false);
                          }}
                          onMouseEnter={() => setSelected(idx)}
                          className={cn(
                            "flex items-center gap-3 w-full px-4 py-2.5 text-left transition-colors",
                            isSelected
                              ? "bg-[var(--bg-surface)]"
                              : "hover:bg-[var(--bg-surface)]/50"
                          )}
                          role="option"
                          aria-selected={isSelected}
                        >
                          {/* Icon */}
                          <div
                            className={cn(
                              "w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0",
                              isSelected
                                ? "bg-[var(--accent)] text-white"
                                : "bg-[var(--bg-surface)] text-[var(--text-muted)] border border-[var(--border)]"
                            )}
                          >
                            {item.icon}
                          </div>

                          {/* Labels */}
                          <div className="flex-1 min-w-0">
                            <p
                              className={cn(
                                "text-[13px] font-medium truncate",
                                isSelected
                                  ? "text-[var(--text-primary)]"
                                  : "text-[var(--text-primary)]"
                              )}
                            >
                              {item.label}
                            </p>
                            {item.description && (
                              <p className="text-[11px] text-[var(--text-muted)] truncate">
                                {item.description}
                              </p>
                            )}
                          </div>

                          {/* Shortcut */}
                          {item.shortcut && (
                            <div className="flex gap-1 flex-shrink-0">
                              {item.shortcut.split(" ").map((key, ki) => (
                                <Kbd key={ki}>{key}</Kbd>
                              ))}
                            </div>
                          )}
                        </button>
                      );
                    })}
                  </div>
                );
              })
            )}
          </div>

          {/* Footer */}
          <div className="border-t border-[var(--border)] px-4 py-2.5 flex items-center gap-4 text-[var(--text-muted)] text-[11px]">
            <span className="flex items-center gap-1">
              <Kbd>↑↓</Kbd> navigate
            </span>
            <span className="flex items-center gap-1">
              <Kbd>↵</Kbd> select
            </span>
            <span className="flex items-center gap-1">
              <Kbd>⌘K</Kbd> close
            </span>
            <span className="ml-auto">
              {allItems.length} command{allItems.length !== 1 ? "s" : ""}
            </span>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
