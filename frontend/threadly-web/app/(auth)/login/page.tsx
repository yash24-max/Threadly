"use client";

import { signIn } from "next-auth/react";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, Suspense } from "react";
import Link from "next/link";

function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    const res = await signIn("credentials", {
      email,
      password,
      redirect: false,
    });
    setLoading(false);
    if (res?.error) {
      setError("Invalid email or password");
    } else {
      const cb = params.get("callbackUrl") ?? "/dashboard";
      router.push(cb);
    }
  }

  return (
    <div style={{
      minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center",
      background: "var(--bg-canvas)",
    }}>
      <div style={{
        width: "100%", maxWidth: 400, padding: 32,
        background: "var(--bg-panel)", borderRadius: "var(--radius-xl)",
        border: "1px solid var(--border)",
        boxShadow: "var(--shadow-3)",
      }}>
        <div style={{ textAlign: "center", marginBottom: 28 }}>
          <span style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px" }}>
            <span style={{ color: "var(--accent)" }}>thread</span>ly
          </span>
          <p style={{ color: "var(--text-secondary)", fontSize: 14, marginTop: 8 }}>
            Sign in to your workspace
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, color: "var(--text-secondary)", display: "block", marginBottom: 6 }}>
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@company.com"
              style={{
                width: "100%", padding: "10px 14px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 14, outline: "none",
                boxSizing: "border-box",
              }}
            />
          </div>

          <div>
            <label style={{ fontSize: 13, color: "var(--text-secondary)", display: "block", marginBottom: 6 }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              style={{
                width: "100%", padding: "10px 14px",
                background: "var(--bg-surface)", border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)", color: "var(--text-primary)",
                fontSize: 14, outline: "none",
                boxSizing: "border-box",
              }}
            />
          </div>

          {error && (
            <p style={{
              color: "var(--danger)", fontSize: 13,
              background: "rgba(239,68,68,0.1)", padding: "8px 12px",
              borderRadius: "var(--radius-sm)",
            }}>
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{
              width: "100%", padding: "11px",
              background: loading ? "var(--bg-surface)" : "var(--accent)",
              color: loading ? "var(--text-muted)" : "var(--accent-fg)",
              border: "none", borderRadius: "var(--radius-md)",
              fontSize: 15, fontWeight: 600, cursor: loading ? "not-allowed" : "pointer",
            }}
          >
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: 20, fontSize: 13, color: "var(--text-secondary)" }}>
          No account yet?{" "}
          <Link href="/signup" style={{ color: "var(--accent)", textDecoration: "none" }}>
            Create one free
          </Link>
        </p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
