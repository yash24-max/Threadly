"use client";

import { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  MessageSquare,
  HelpCircle,
  GitBranch,
  Shuffle,
  Variable,
  Clock,
  Sparkles,
  Globe,
  Mail,
  UserCheck,
  Square,
  Search,
  ChevronDown,
  ChevronRight,
  FormInput,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { NODE_CATALOG, CATEGORY_ORDER, type NodeCatalogEntry } from "@/lib/node-catalog";

const RECENT_KEY = "tly_recent_nodes";
const MAX_RECENT = 5;

const ICON_MAP: Record<string, React.ComponentType<{ size?: number; color?: string; className?: string }>> = {
  MessageSquare,
  HelpCircle,
  FormInput,
  GitBranch,
  Shuffle,
  Variable,
  Clock,
  Sparkles,
  Globe,
  Mail,
  UserCheck,
  Square,
};

function getRecentNodes(): string[] {
  if (typeof window === "undefined") return [];
  try {
    return JSON.parse(localStorage.getItem(RECENT_KEY) ?? "[]");
  } catch {
    return [];
  }
}

function recordRecentNode(type: string) {
  const recent = getRecentNodes().filter((t) => t !== type);
  recent.unshift(type);
  localStorage.setItem(RECENT_KEY, JSON.stringify(recent.slice(0, MAX_RECENT)));
}

interface NodeItemProps {
  entry: NodeCatalogEntry;
  onAdd: (type: string, defaultData: Record<string, unknown>) => void;
  onDragStart: (e: React.DragEvent, entry: NodeCatalogEntry) => void;
}

function NodeItem({ entry, onAdd, onDragStart }: NodeItemProps) {
  const Icon = ICON_MAP[entry.icon] ?? Square;

  return (
    <motion.div
      layout
      whileHover={{ y: -1, scale: 1.01 }}
      whileTap={{ scale: 0.98 }}
      transition={{ duration: 0.15 }}
      draggable
      onDragStart={(e) => onDragStart(e as unknown as React.DragEvent, entry)}
      onClick={() => onAdd(entry.type, entry.defaultData)}
      className={cn(
        "group flex items-center gap-2.5 px-3 py-2.5 rounded-lg border cursor-grab active:cursor-grabbing",
        "border-[var(--border)] bg-[var(--bg-surface)] hover:bg-[var(--bg-hover,var(--bg-surface))]",
        "hover:border-[var(--border-strong,var(--border))] transition-colors select-none",
        "dark:border-[var(--border)] dark:bg-[var(--bg-surface)]"
      )}
      role="button"
      tabIndex={0}
      aria-label={`Add ${entry.label} node`}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onAdd(entry.type, entry.defaultData);
        }
      }}
    >
      {/* color border accent */}
      <div
        className="absolute left-0 top-0 bottom-0 w-0.5 rounded-l-lg"
        style={{ background: entry.color }}
      />
      <div
        className="flex items-center justify-center w-7 h-7 rounded-md flex-shrink-0"
        style={{ background: entry.color + "20" }}
      >
        <Icon size={14} color={entry.color} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-[13px] font-medium text-[var(--text-primary)] truncate">{entry.label}</p>
        <p className="text-[11px] text-[var(--text-muted)] truncate">{entry.description}</p>
      </div>
    </motion.div>
  );
}

interface CategorySectionProps {
  category: string;
  entries: NodeCatalogEntry[];
  onAdd: (type: string, defaultData: Record<string, unknown>) => void;
  onDragStart: (e: React.DragEvent, entry: NodeCatalogEntry) => void;
  defaultOpen?: boolean;
}

function CategorySection({ category, entries, onAdd, onDragStart, defaultOpen = true }: CategorySectionProps) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="mb-2">
      <button
        onClick={() => setOpen((o) => !o)}
        className={cn(
          "flex items-center gap-1.5 w-full px-2 py-1.5 text-left",
          "text-[11px] font-semibold uppercase tracking-wide text-[var(--text-muted)]",
          "hover:text-[var(--text-secondary)] transition-colors rounded"
        )}
        aria-expanded={open}
      >
        {open ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
        {category}
        <span className="ml-auto text-[10px] font-normal opacity-60">{entries.length}</span>
      </button>
      <AnimatePresence initial={false}>
        {open && (
          <motion.div
            key="content"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.2, ease: "easeInOut" }}
            className="overflow-hidden"
          >
            <div className="flex flex-col gap-1.5 pb-1 relative">
              {entries.map((entry) => (
                <NodeItem
                  key={entry.type}
                  entry={entry}
                  onAdd={onAdd}
                  onDragStart={onDragStart}
                />
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

interface NodePanelProps {
  onAddNode: (type: string, defaultData: Record<string, unknown>) => void;
}

export function NodePanel({ onAddNode }: NodePanelProps) {
  const [search, setSearch] = useState("");
  const [recentTypes, setRecentTypes] = useState<string[]>([]);

  useEffect(() => {
    setRecentTypes(getRecentNodes());
  }, []);

  const handleAdd = useCallback(
    (type: string, defaultData: Record<string, unknown>) => {
      recordRecentNode(type);
      setRecentTypes(getRecentNodes());
      onAddNode(type, defaultData);
    },
    [onAddNode]
  );

  const handleDragStart = useCallback((e: React.DragEvent, entry: NodeCatalogEntry) => {
    e.dataTransfer.setData("application/reactflow", entry.type);
    e.dataTransfer.setData("application/reactflow-data", JSON.stringify(entry.defaultData));
    e.dataTransfer.effectAllowed = "move";
    recordRecentNode(entry.type);
    setRecentTypes(getRecentNodes());
  }, []);

  const searchLower = search.toLowerCase().trim();
  const filtered = searchLower
    ? NODE_CATALOG.filter(
        (e) =>
          e.label.toLowerCase().includes(searchLower) ||
          e.description.toLowerCase().includes(searchLower) ||
          e.category.toLowerCase().includes(searchLower)
      )
    : null;

  const recentEntries = recentTypes
    .map((t) => NODE_CATALOG.find((e) => e.type === t))
    .filter((e): e is NodeCatalogEntry => Boolean(e));

  return (
    <div
      className={cn(
        "flex flex-col overflow-hidden",
        "w-[220px] flex-shrink-0",
        "border-r border-[var(--border)] bg-[var(--bg-panel)]"
      )}
      aria-label="Node catalog"
    >
      {/* Header */}
      <div className="px-3 pt-3 pb-2 border-b border-[var(--border)]">
        <p className="text-[11px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
          Nodes
        </p>
        {/* Search */}
        <div className="relative">
          <Search
            size={12}
            className="absolute left-2.5 top-1/2 -translate-y-1/2 text-[var(--text-muted)] pointer-events-none"
          />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search nodes…"
            className={cn(
              "w-full pl-7 pr-2 py-1.5 text-[12px] rounded-md",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-primary)] placeholder-[var(--text-muted)]",
              "outline-none focus:border-[var(--accent)] transition-colors"
            )}
            aria-label="Search nodes"
          />
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto px-2 py-2">
        {filtered ? (
          /* Search results */
          filtered.length === 0 ? (
            <p className="text-[12px] text-[var(--text-muted)] text-center py-6">
              No nodes match &ldquo;{search}&rdquo;
            </p>
          ) : (
            <div className="flex flex-col gap-1.5">
              {filtered.map((entry) => (
                <NodeItem
                  key={entry.type}
                  entry={entry}
                  onAdd={handleAdd}
                  onDragStart={handleDragStart}
                />
              ))}
            </div>
          )
        ) : (
          <>
            {/* Recently used */}
            {recentEntries.length > 0 && (
              <CategorySection
                category="Recently Used"
                entries={recentEntries}
                onAdd={handleAdd}
                onDragStart={handleDragStart}
                defaultOpen={true}
              />
            )}

            {/* Categories */}
            {CATEGORY_ORDER.map((cat) => {
              const entries = NODE_CATALOG.filter((e) => e.category === cat);
              if (entries.length === 0) return null;
              return (
                <CategorySection
                  key={cat}
                  category={cat}
                  entries={entries}
                  onAdd={handleAdd}
                  onDragStart={handleDragStart}
                  defaultOpen={true}
                />
              );
            })}
          </>
        )}
      </div>

      {/* Footer hint */}
      <div className="px-3 py-2 border-t border-[var(--border)]">
        <p className="text-[10px] text-[var(--text-muted)] text-center">
          Drag or click to add
        </p>
      </div>
    </div>
  );
}
