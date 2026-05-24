"use client";

import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";

export default function SignupPage() {
  const router = useRouter();
  const [form, setForm] = useState({ name: "", orgName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.post("/v1/auth/signup", form);
      const res = await signIn("credentials", {
        email: form.email,
        password: form.password,
        redirect: false,
      });
      if (res?.error) {
        setError("Account created but sign-in failed. Please log in.");
        router.push("/login");
      } else {
        router.push("/dashboard");
      }
    } catch (err: any) {
      setError(err.detail ?? "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  const field = (key: keyof typeof form, label: string, type = "text", placeholder = "") => (
    <div>
      <label style={{ fontSize: 13, color: "var(--text-secondary)", display: "block", marginBottom: 6 }}>
        {label}
      </label>
      <input
        type={type}
        value={form[key]}
        onChange={(e) => setForm((f) => ({ ...f, [key]: e.target.value }))}
        required
        placeholder={placeholder}
        style={{
          width: "100%", padding: "10px 14px",
          background: "var(--bg-surface)", border: "1px solid var(--border)",
          borderRadius: "var(--radius-md)", color: "var(--text-primary)",
          fontSize: 14, outline: "none", boxSizing: "border-box",
        }}
      />
    </div>
  );

  return (
    <div style={{
      minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center",
      background: "var(--bg-canvas)",
    }}>
      <div style={{
        width: "100%", maxWidth: 420, padding: 32,
        background: "var(--bg-panel)", borderRadius: "var(--radius-xl)",
        border: "1px solid var(--border)", boxShadow: "var(--shadow-3)",
      }}>
        <div style={{ textAlign: "center", marginBottom: 28 }}>
          <span style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.5px" }}>
            <span style={{ color: "var(--accent)" }}>thread</span>ly
          </span>
          <p style={{ color: "var(--text-secondary)", fontSize: 14, marginTop: 8 }}>
            Create your workspace
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {field("name", "Your name", "text", "Alex")}
          {field("orgName", "Company / workspace name", "text", "Acme Inc.")}
          {field("email", "Work email", "email", "alex@acme.com")}
          {field("password", "Password (8+ chars)", "password", "••••••••")}

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
            {loading ? "Creating workspace…" : "Create workspace →"}
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: 20, fontSize: 13, color: "var(--text-secondary)" }}>
          Already have an account?{" "}
          <Link href="/login" style={{ color: "var(--accent)", textDecoration: "none" }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
