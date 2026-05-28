"use client";

import { signIn } from "next-auth/react";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, Suspense } from "react";
import Link from "next/link";
import { Eye, EyeOff, ArrowRight } from "lucide-react";
import { Logo } from "@/components/ui/Logo";

/* ── Floating decorative background elements ─────────────────────────────── */
function FloatingDecor() {
  const bubbles = [
    { x: "4%",  y: "12%", size: 52, color: "#C7D2FE", rotate: -15, opacity: 0.7 },
    { x: "8%",  y: "48%", size: 68, color: "#DDD6FE", rotate: 10,  opacity: 0.5 },
    { x: "3%",  y: "72%", size: 42, color: "#FDE68A", rotate: -20, opacity: 0.6 },
    { x: "14%", y: "82%", size: 36, color: "#FCA5A5", rotate: 15,  opacity: 0.55 },
    { x: "80%", y: "8%",  size: 58, color: "#A5F3FC", rotate: 8,   opacity: 0.45 },
    { x: "88%", y: "30%", size: 72, color: "#C7D2FE", rotate: -10, opacity: 0.5 },
    { x: "82%", y: "58%", size: 44, color: "#FDE68A", rotate: 20,  opacity: 0.6 },
    { x: "90%", y: "78%", size: 60, color: "#FCA5A5", rotate: -5,  opacity: 0.5 },
    { x: "22%", y: "6%",  size: 28, color: "#6366F1", rotate: 12,  opacity: 0.2 },
    { x: "72%", y: "88%", size: 32, color: "#8B5CF6", rotate: -18, opacity: 0.2 },
  ];

  return (
    <div style={{ position: "fixed", inset: 0, pointerEvents: "none", zIndex: 0, overflow: "hidden" }}>
      {bubbles.map((b, i) => (
        <div key={i} style={{
          position: "absolute", left: b.x, top: b.y,
          transform: `rotate(${b.rotate}deg)`,
          opacity: b.opacity,
        }}>
          {/* Chat bubble SVG */}
          <svg width={b.size} height={b.size} viewBox="0 0 48 48" fill="none">
            <rect x="2" y="2" width="36" height="28" rx="8" fill={b.color} />
            <path d="M8 30 L4 38 L16 30 Z" fill={b.color} />
            <rect x="9" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
            <rect x="17" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
            <rect x="25" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
          </svg>
        </div>
      ))}
      {/* Large soft circles */}
      <div style={{ position: "absolute", left: "-60px", top: "30%", width: 200, height: 200, borderRadius: "50%", background: "#C7D2FE", opacity: 0.25 }} />
      <div style={{ position: "absolute", right: "-40px", bottom: "15%", width: 160, height: 160, borderRadius: "50%", background: "#DDD6FE", opacity: 0.3 }} />
      <div style={{ position: "absolute", right: "12%", top: "-30px", width: 120, height: 120, borderRadius: "50%", background: "#A5F3FC", opacity: 0.2 }} />
    </div>
  );
}

/* ── Login form ───────────────────────────────────────────────────────────── */
function LoginForm() {
  const router  = useRouter();
  const params  = useSearchParams();
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [error,    setError]    = useState("");
  const [loading,  setLoading]  = useState(false);
  const [showPw,   setShowPw]   = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(""); setLoading(true);
    const res = await signIn("credentials", { email, password, redirect: false });
    setLoading(false);
    if (res?.error) setError("Invalid email or password.");
    else router.push(params.get("callbackUrl") ?? "/dashboard");
  }

  const inputStyle: React.CSSProperties = {
    width: "100%", boxSizing: "border-box",
    borderRadius: 12, border: "1.5px solid #E8EAF6",
    background: "#FAFAFE", padding: "13px 16px",
    fontSize: 14, color: "#1A1A2E", outline: "none",
    transition: "border-color 200ms ease, box-shadow 200ms ease",
    fontFamily: "inherit",
  };

  return (
    /* Full-screen centered layout */
    <div style={{
      minHeight: "100vh", width: "100%",
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      padding: "24px 16px",
      position: "relative", zIndex: 1,
    }}>
      {/* Card */}
      <div style={{
        width: "100%", maxWidth: 460,
        background: "rgba(255,255,255,0.95)",
        backdropFilter: "blur(20px)",
        borderRadius: 28,
        boxShadow: "0 24px 64px rgba(99,102,241,0.14), 0 8px 24px rgba(99,102,241,0.08)",
        padding: "44px 40px",
      }}>
        {/* Logo + heading */}
        <div style={{ textAlign: "center", marginBottom: 32 }}>
          <div style={{ display: "flex", justifyContent: "center", marginBottom: 20 }}>
            <Logo size="md" variant="dark" />
          </div>
          <h1 style={{ fontSize: 26, fontWeight: 800, color: "#1A1A2E", letterSpacing: "-0.02em", marginBottom: 6 }}>
            Welcome back
          </h1>
          <p style={{ fontSize: 14, color: "#6B7280" }}>
            Sign in to your Threadly workspace
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {/* Email */}
          <div>
            <label style={{ display: "block", fontSize: 12, fontWeight: 600, color: "#374151", marginBottom: 6, letterSpacing: "0.02em", textTransform: "uppercase" }}>
              Email
            </label>
            <input
              type="email" value={email} onChange={e => setEmail(e.target.value)}
              required autoFocus placeholder="you@company.com"
              style={inputStyle}
              onFocus={e => { e.target.style.borderColor = "#6366F1"; e.target.style.boxShadow = "0 0 0 3px rgba(99,102,241,0.1)"; }}
              onBlur={e  => { e.target.style.borderColor = "#E8EAF6"; e.target.style.boxShadow = "none"; }}
            />
          </div>

          {/* Password */}
          <div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
              <label style={{ fontSize: 12, fontWeight: 600, color: "#374151", letterSpacing: "0.02em", textTransform: "uppercase" }}>Password</label>
              <Link href="/forgot-password" style={{ fontSize: 12, color: "#6366F1", textDecoration: "none", fontWeight: 500 }}>Forgot?</Link>
            </div>
            <div style={{ position: "relative" }}>
              <input
                type={showPw ? "text" : "password"} value={password} onChange={e => setPassword(e.target.value)}
                required placeholder="Enter your password"
                style={{ ...inputStyle, paddingRight: 46 }}
                onFocus={e => { e.target.style.borderColor = "#6366F1"; e.target.style.boxShadow = "0 0 0 3px rgba(99,102,241,0.1)"; }}
                onBlur={e  => { e.target.style.borderColor = "#E8EAF6"; e.target.style.boxShadow = "none"; }}
              />
              <button type="button" tabIndex={-1} onClick={() => setShowPw(v => !v)}
                style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "#9CA3AF", padding: 4 }}>
                {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {error && (
            <div style={{ borderRadius: 10, background: "#FEF2F2", border: "1px solid #FECACA", padding: "10px 14px", fontSize: 13, color: "#DC2626" }}>{error}</div>
          )}

          {/* Submit */}
          <button type="submit" disabled={loading} style={{
            width: "100%", display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
            borderRadius: 14, padding: "14px", fontSize: 15, fontWeight: 700, color: "#fff",
            background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
            border: "none", cursor: "pointer",
            boxShadow: "0 6px 20px rgba(99,102,241,0.4)",
            opacity: loading ? 0.75 : 1,
            transition: "transform 150ms ease, box-shadow 150ms ease",
            marginTop: 4,
            fontFamily: "inherit",
          }}
            onMouseEnter={e => { if (!loading) { (e.target as HTMLButtonElement).style.transform = "translateY(-1px)"; (e.target as HTMLButtonElement).style.boxShadow = "0 8px 24px rgba(99,102,241,0.5)"; }}}
            onMouseLeave={e => { (e.target as HTMLButtonElement).style.transform = "none"; (e.target as HTMLButtonElement).style.boxShadow = "0 6px 20px rgba(99,102,241,0.4)"; }}
          >
            {loading
              ? <><span style={{ width: 16, height: 16, borderRadius: "50%", border: "2px solid rgba(255,255,255,0.3)", borderTopColor: "#fff", animation: "spin 1s linear infinite", display: "inline-block" }} />Signing in…</>
              : <>Sign in <ArrowRight size={16} /></>}
          </button>
        </form>

        {/* Divider */}
        <div style={{ margin: "24px 0 0", display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ flex: 1, height: 1, background: "#F3F4F6" }} />
          <span style={{ fontSize: 12, color: "#9CA3AF", fontWeight: 500 }}>OR</span>
          <div style={{ flex: 1, height: 1, background: "#F3F4F6" }} />
        </div>

        <p style={{ textAlign: "center", fontSize: 14, color: "#6B7280", marginTop: 20 }}>
          Don&apos;t have an account?{" "}
          <Link href="/signup" style={{ color: "#6366F1", fontWeight: 700, textDecoration: "none" }}>
            Start free trial
          </Link>
        </p>
      </div>
    </div>
  );
}

/* ── Page ─────────────────────────────────────────────────────────────────── */
function LoginPage() {
  return (
    <div style={{
      minHeight: "100vh", position: "relative",
      background: "linear-gradient(135deg, #EEF2FF 0%, #F5F3FF 40%, #EDE9FE 70%, #E0E7FF 100%)",
    }}>
      <FloatingDecor />

      {/* Top-left logo */}
      <div style={{ position: "fixed", top: 24, left: 32, zIndex: 10 }}>
        <Logo size="sm" variant="dark" href="/" />
      </div>

      <Suspense>
        <LoginForm />
      </Suspense>
    </div>
  );
}

export default function LoginPageWrapper() {
  return <Suspense><LoginPage /></Suspense>;
}
