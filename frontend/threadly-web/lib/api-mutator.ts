/**
 * Custom fetch mutator for Orval generated hooks.
 * Reads session token from next-auth and injects it + distributed tracing headers into every request.
 * All requests routed through Nginx gateway at process.env.NEXT_PUBLIC_API_URL.
 */
import { getSession } from "next-auth/react";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Generate a trace ID for distributed tracing across Nginx gateway and microservices.
 * Format: 32-char hex (OpenTelemetry trace ID format)
 */
function generateTraceId(): string {
  return Array.from(crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

export const customInstance = async <T>(config: {
  url: string;
  method: string;
  params?: Record<string, string>;
  data?: unknown;
  headers?: Record<string, string>;
  signal?: AbortSignal;
}): Promise<T> => {
  const session = await getSession();
  const token = (session as any)?.accessToken;
  const traceId = generateTraceId();

  const url = new URL(`${API_BASE}${config.url}`);
  if (config.params) {
    Object.entries(config.params).forEach(([k, v]) => url.searchParams.set(k, v));
  }

  const res = await fetch(url.toString(), {
    method: config.method.toUpperCase(),
    headers: {
      "Content-Type": "application/json",
      "X-Trace-ID": traceId,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...config.headers,
    },
    body: config.data ? JSON.stringify(config.data) : undefined,
    signal: config.signal,
  });

  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
};
