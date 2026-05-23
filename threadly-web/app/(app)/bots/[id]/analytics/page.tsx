"use client";

import { Suspense, useState } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { MessageSquare, Clock, Star, TrendingUp } from "lucide-react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";

// ── Types ─────────────────────────────────────────────────────────────────────

interface AnalyticsSummary {
  totalConversations: number;
  openConversations: number;
  avgResponseTimeMs: number;
  satisfactionRate: number;
}

interface DailyDataPoint {
  date: string;
  conversations: number;
  messages: number;
}

interface NodeFunnelRow {
  nodeId: string;
  nodeLabel: string;
  completionCount: number;
  percentReached: number;
}

interface AnalyticsDailyResponse {
  data: DailyDataPoint[];
  funnelData?: NodeFunnelRow[];
}

// ── Date range helpers ────────────────────────────────────────────────────────

type DateRange = "7d" | "30d" | "90d";

function getDateRange(range: DateRange): { from: string; to: string } {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - Number(range.replace("d", "")));
  return {
    from: from.toISOString().split("T")[0],
    to: to.toISOString().split("T")[0],
  };
}

// ── Skeleton components ───────────────────────────────────────────────────────

function CardSkeleton() {
  return (
    <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-5 animate-pulse">
      <div className="h-3 w-24 rounded bg-[var(--border)] mb-3" />
      <div className="h-7 w-16 rounded bg-[var(--border)]" />
    </div>
  );
}

function ChartSkeleton({ height = 220 }: { height?: number }) {
  return (
    <div
      className="w-full rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] animate-pulse"
      style={{ height }}
    />
  );
}

function TableSkeleton() {
  return (
    <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={i} className="flex gap-4 px-4 py-3 border-b border-[var(--border)] animate-pulse">
          <div className="h-3 w-48 rounded bg-[var(--border)]" />
          <div className="h-3 w-16 rounded bg-[var(--border)] ml-auto" />
          <div className="h-3 w-12 rounded bg-[var(--border)]" />
        </div>
      ))}
    </div>
  );
}

// ── Summary card ─────────────────────────────────────────────────────────────

interface SummaryCardProps {
  label: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
}

function SummaryCard({ label, value, icon, color }: SummaryCardProps) {
  return (
    <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-5">
      <div className="flex items-center justify-between mb-3">
        <p className="text-[12px] font-medium text-[var(--text-muted)] uppercase tracking-wide">
          {label}
        </p>
        <div
          className="w-8 h-8 rounded-lg flex items-center justify-center"
          style={{ background: color + "20", color }}
        >
          {icon}
        </div>
      </div>
      <p className="text-2xl font-bold text-[var(--text-primary)]">{value}</p>
    </div>
  );
}

// ── Inner page (needs useSearchParams) ───────────────────────────────────────

function AnalyticsInner() {
  const params = useParams<{ id: string }>();
  const botId = params.id;
  const { data: session } = useSession();
  const token = session?.accessToken;

  const searchParams = useSearchParams();
  const initialRange = (searchParams.get("range") as DateRange) ?? "30d";
  const [range, setRange] = useState<DateRange>(initialRange);
  const router = useRouter();

  const { from, to } = getDateRange(range);

  const { data: summary, isLoading: summaryLoading } = useQuery<AnalyticsSummary>({
    queryKey: ["analytics", botId, "summary", range],
    queryFn: () =>
      api.get<AnalyticsSummary>(`/v1/bots/${botId}/analytics/summary?from=${from}&to=${to}`, token),
    enabled: !!token && !!botId,
  });

  const { data: dailyRes, isLoading: dailyLoading } = useQuery<AnalyticsDailyResponse>({
    queryKey: ["analytics", botId, "daily", range],
    queryFn: () =>
      api.get<AnalyticsDailyResponse>(
        `/v1/bots/${botId}/analytics/daily?from=${from}&to=${to}`,
        token
      ),
    enabled: !!token && !!botId,
  });

  const dailyData = dailyRes?.data ?? [];
  const funnelData = dailyRes?.funnelData ?? [];

  const formatMs = (ms: number) =>
    ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${ms}ms`;

  const setRangeWithParam = (r: DateRange) => {
    setRange(r);
    router.replace(`?range=${r}`, { scroll: false });
  };

  const DATE_RANGES: { label: string; value: DateRange }[] = [
    { label: "Last 7 days", value: "7d" },
    { label: "Last 30 days", value: "30d" },
    { label: "Last 90 days", value: "90d" },
  ];

  return (
    <div className="flex flex-col gap-6 p-6 max-w-5xl mx-auto">
      {/* Page header */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-xl font-semibold text-[var(--text-primary)]">
          Bot Analytics
        </h1>
        {/* Date range pills */}
        <div className="flex gap-1.5 p-1 rounded-lg bg-[var(--bg-surface)] border border-[var(--border)]">
          {DATE_RANGES.map((dr) => (
            <button
              key={dr.value}
              type="button"
              onClick={() => setRangeWithParam(dr.value)}
              className={cn(
                "px-3 py-1.5 rounded-md text-[12px] font-medium transition-colors",
                range === dr.value
                  ? "bg-[var(--accent)] text-white"
                  : "text-[var(--text-muted)] hover:text-[var(--text-primary)]"
              )}
            >
              {dr.label}
            </button>
          ))}
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {summaryLoading ? (
          Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)
        ) : (
          <>
            <SummaryCard
              label="Total Conversations"
              value={(summary?.totalConversations ?? 0).toLocaleString()}
              icon={<MessageSquare size={16} />}
              color="#3B82F6"
            />
            <SummaryCard
              label="Open"
              value={(summary?.openConversations ?? 0).toLocaleString()}
              icon={<TrendingUp size={16} />}
              color="#10B981"
            />
            <SummaryCard
              label="Avg Response Time"
              value={formatMs(summary?.avgResponseTimeMs ?? 0)}
              icon={<Clock size={16} />}
              color="#F59E0B"
            />
            <SummaryCard
              label="Satisfaction Rate"
              value={`${((summary?.satisfactionRate ?? 0) * 100).toFixed(1)}%`}
              icon={<Star size={16} />}
              color="#EC4899"
            />
          </>
        )}
      </div>

      {/* Line chart – daily conversations */}
      <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-5">
        <p className="text-[13px] font-semibold text-[var(--text-primary)] mb-4">
          Daily Conversations
        </p>
        {dailyLoading ? (
          <ChartSkeleton height={220} />
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={dailyData} margin={{ top: 4, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 11, fill: "var(--text-muted)" }}
                tickFormatter={(v: string) => v.slice(5)}
              />
              <YAxis tick={{ fontSize: 11, fill: "var(--text-muted)" }} width={32} />
              <Tooltip
                contentStyle={{
                  background: "var(--bg-panel)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px",
                  fontSize: 12,
                }}
              />
              <Line
                type="monotone"
                dataKey="conversations"
                stroke="#3B82F6"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Bar chart – daily messages */}
      <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-5">
        <p className="text-[13px] font-semibold text-[var(--text-primary)] mb-4">
          Daily Messages
        </p>
        {dailyLoading ? (
          <ChartSkeleton height={200} />
        ) : (
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={dailyData} margin={{ top: 4, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 11, fill: "var(--text-muted)" }}
                tickFormatter={(v: string) => v.slice(5)}
              />
              <YAxis tick={{ fontSize: 11, fill: "var(--text-muted)" }} width={32} />
              <Tooltip
                contentStyle={{
                  background: "var(--bg-panel)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px",
                  fontSize: 12,
                }}
              />
              <Bar dataKey="messages" fill="#8B5CF6" radius={[3, 3, 0, 0]} maxBarSize={32} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Node funnel table */}
      <div>
        <p className="text-[13px] font-semibold text-[var(--text-primary)] mb-3">
          Node Funnel
        </p>
        {dailyLoading ? (
          <TableSkeleton />
        ) : funnelData.length === 0 ? (
          <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] p-8 text-center">
            <p className="text-[13px] text-[var(--text-muted)]">
              No funnel data available for this period
            </p>
          </div>
        ) : (
          <div className="rounded-xl border border-[var(--border)] bg-[var(--bg-panel)] overflow-hidden">
            <table className="w-full text-[12px]">
              <thead>
                <tr className="border-b border-[var(--border)]">
                  <th className="text-left px-4 py-3 text-[var(--text-muted)] font-semibold uppercase tracking-wide text-[11px]">
                    Node
                  </th>
                  <th className="text-right px-4 py-3 text-[var(--text-muted)] font-semibold uppercase tracking-wide text-[11px]">
                    Completions
                  </th>
                  <th className="text-right px-4 py-3 text-[var(--text-muted)] font-semibold uppercase tracking-wide text-[11px]">
                    % Reached
                  </th>
                </tr>
              </thead>
              <tbody>
                {funnelData.map((row, i) => (
                  <tr
                    key={row.nodeId}
                    className={cn(
                      "border-b border-[var(--border)] last:border-b-0",
                      i % 2 === 0 ? "bg-[var(--bg-panel)]" : "bg-[var(--bg-surface)]"
                    )}
                  >
                    <td className="px-4 py-3 text-[var(--text-primary)] font-medium">
                      {row.nodeLabel}
                      <span className="ml-2 text-[10px] text-[var(--text-muted)] font-mono">
                        {row.nodeId}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right text-[var(--text-secondary)] tabular-nums">
                      {row.completionCount.toLocaleString()}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <div className="w-16 h-1.5 rounded-full bg-[var(--border)] overflow-hidden">
                          <div
                            className="h-full rounded-full bg-[var(--accent)]"
                            style={{ width: `${Math.min(100, row.percentReached)}%` }}
                          />
                        </div>
                        <span className="text-[var(--text-secondary)] tabular-nums w-10 text-right">
                          {row.percentReached.toFixed(1)}%
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

// ── Page export (wrapped in Suspense for useSearchParams) ─────────────────────

export default function BotAnalyticsPage() {
  return (
    <Suspense
      fallback={
        <div className="flex flex-col gap-6 p-6 max-w-5xl mx-auto">
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)}
          </div>
          <ChartSkeleton height={220} />
          <ChartSkeleton height={200} />
        </div>
      }
    >
      <AnalyticsInner />
    </Suspense>
  );
}
