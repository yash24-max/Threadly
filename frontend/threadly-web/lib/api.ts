// In the browser: use relative URL so requests go to Next.js (port 3000)
// which proxies them via rewrites → no cross-origin CORS preflight.
// On the server (SSR/RSC): use the full backend URL directly.
const API_BASE =
  typeof window !== "undefined"
    ? ""
    : (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080");

export class ApiError extends Error {
  constructor(
    public status: number,
    public detail: string,
    message?: string
  ) {
    super(message ?? detail);
  }
}

/**
 * Generate a trace ID for distributed tracing across Nginx gateway and microservices.
 * Format: 32-char hex (OpenTelemetry trace ID format)
 */
function generateTraceId(): string {
  return Array.from(crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

/** Returns true if the string is just an HTTP status phrase (not a useful message). */
function isHttpStatusWord(s: string): boolean {
  return /^(forbidden|unauthorized|not found|bad request|internal server error|ok|conflict|created|no content)$/i.test(s.trim());
}

/** Maps HTTP status codes to user-friendly messages. */
function friendlyError(status: number, fallback: string): string {
  const map: Record<number, string> = {
    400: "Invalid request — please check your input.",
    401: "Your session has expired. Please sign in again.",
    403: "You don't have permission to do this.",
    404: "The requested resource was not found.",
    409: "This already exists. Try a different value.",
    422: "The request data is invalid.",
    429: "Too many requests — please wait a moment and try again.",
    500: "A server error occurred. Please try again.",
    502: "The server is temporarily unavailable.",
    503: "Service unavailable — please try again shortly.",
  };
  return map[status] ?? (isHttpStatusWord(fallback) ? `Request failed (${status}).` : fallback);
}

async function request<T>(
  path: string,
  options: RequestInit & { token?: string } = {}
): Promise<T> {
  const { token, ...rest } = options;
  const traceId = generateTraceId();

  const res = await fetch(`${API_BASE}${path}`, {
    ...rest,
    headers: {
      "Content-Type": "application/json",
      "X-Trace-ID": traceId,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...rest.headers,
    },
  });

  if (!res.ok) {
    let detail = friendlyError(res.status, res.statusText);
    // Only use backend message for client errors (4xx) — never expose raw 5xx internals.
    if (res.status < 500) {
      try {
        const body = await res.json();
        // Spring Boot error shape: { message } or { detail } or { error }
        const raw = body.message ?? body.detail ?? body.error ?? null;
        if (raw && typeof raw === "string" && !isHttpStatusWord(raw)) {
          detail = raw;
        }
      } catch {}
    }
    throw new ApiError(res.status, detail);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  get: <T>(path: string, token?: string) =>
    request<T>(path, { method: "GET", token }),

  post: <T>(path: string, body: unknown, token?: string) =>
    request<T>(path, {
      method: "POST",
      body: JSON.stringify(body),
      token,
    }),

  patch: <T>(path: string, body: unknown, token?: string) =>
    request<T>(path, {
      method: "PATCH",
      body: JSON.stringify(body),
      token,
    }),

  delete: <T>(path: string, token?: string) =>
    request<T>(path, { method: "DELETE", token }),

  upload: <T>(path: string, form: FormData, token?: string) =>
    request<T>(path, {
      method: "POST",
      body: form,
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      token: undefined, // don't double-set
    }),
};
