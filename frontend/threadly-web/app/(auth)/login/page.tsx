"use client";

import { signIn } from "next-auth/react";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, Suspense } from "react";
import Link from "next/link";
import { Eye, EyeOff, ArrowRight } from "lucide-react";
import { Logo } from "@/components/ui/Logo";

/* ── Floating orbs (dark-theme) ──────────────────────────────────────────── */
function FloatingDecor() {
  const bubbles = [
    { x: "5%",  y: "10%", size: 44, color: "rgba(99,102,241,0.5)",  rotate: -15 },
    { x: "8%",  y: "45%", size: 60, color: "rgba(139,92,246,0.35)", rotate: 10  },
    { x: "4%",  y: "72%", size: 36, color: "rgba(6,182,212,0.4)",   rotate: -20 },
    { x: "13%", y: "82%", size: 48, color: "rgba(99,102,241,0.3)",  rotate: 15  },
    { x: "81%", y: "7%",  size: 52, color: "rgba(6,182,212,0.35)",  rotate: 8   },
    { x: "87%", y: "30%", size: 64, color: "rgba(139,92,246,0.4)",  rotate: -10 },
    { x: "83%", y: "57%", size: 40, color: "rgba(99,102,241,0.35)", rotate: 20  },
    { x: "89%", y: "77%", size: 54, color: "rgba(6,182,212,0.3)",   rotate: -5  },
  ];

  return (
    <div style={{ position: "fixed", inset: 0, pointerEvents: "none", zIndex: 0, overflow: "hidden" }}>
      {/* Glow orbs */}
      <div style={{ position: "absolute", left: "-80px", top: "15%", width: 360, height: 360, borderRadius: "50%", background: "rgba(99,102,241,0.18)", filter: "blur(80px)" }} />
      <div style={{ position: "absolute", left: "-40px", top: "58%", width: 240, height: 240, borderRadius: "50%", background: "rgba(139,92,246,0.12)", filter: "blur(60px)" }} />
      <div style={{ position: "absolute", right: "-60px", top: "5%",  width: 300, height: 300, borderRadius: "50%", background: "rgba(6,182,212,0.12)",  filter: "blur(70px)" }} />
      <div style={{ position: "absolute", right: "-30px", top: "62%", width: 200, height: 200, borderRadius: "50%", background: "rgba(99,102,241,0.10)", filter: "blur(50px)" }} />

      {/* Chat bubble icons */}
      {bubbles.map((b, i) => (
        <div key={i} style={{ position: "absolute", left: b.x, top: b.y, transform: `rotate(${b.rotate}deg)`, opacity: 0.55 }}>
          <svg width={b.size} height={b.size} viewBox="0 0 48 48" fill="none">
            <rect x="2" y="2" width="36" height="28" rx="8" fill={b.color} />
            <path d="M8 30 L4 38 L16 30 Z" fill={b.color} />
            <rect x="9"  y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.3" />
            <rect x="17" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.3" />
            <rect x="25" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.3" />
          </svg>
        </div>
      ))}
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
    borderRadius: 12, border: "1.5px solid rgba(255,255,255,0.1)",
    background: "rgba(255,255,255,0.06)", padding: "13px 16px",
    fontSize: 14, color: "#F1F2F8", outline: "none",
    transition: "border-color 200ms ease, box-shadow 200ms ease, background 200ms ease",
    fontFamily: "inherit",
  };
  const inputCls = "auth-input";

  const labelStyle: React.CSSProperties = {
    display: "block", fontSize: 11, fontWeight: 600,
    color: "rgba(255,255,255,0.45)", marginBottom: 6,
    letterSpacing: "0.06em", textTransform: "uppercase" as const,
  };

  return (
    <div style={{
      minHeight: "100vh", width: "100%",
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      padding: "24px 16px", position: "relative", zIndex: 1,
    }}>
      <div style={{
        width: "100%", maxWidth: 460,
        background: "rgba(18,22,42,0.75)",
        backdropFilter: "blur(24px)",
        WebkitBackdropFilter: "blur(24px)",
        borderRadius: 28,
        border: "1px solid rgba(255,255,255,0.08)",
        boxShadow: "0 24px 64px rgba(0,0,0,0.45), 0 8px 24px rgba(99,102,241,0.12), inset 0 1px 0 rgba(255,255,255,0.04)",
        padding: "44px 40px",
      }}>
        {/* Logo + heading */}
        <div style={{ textAlign: "center", marginBottom: 32 }}>
          <div style={{ display: "flex", justifyContent: "center", marginBottom: 20 }}>
            <Logo size="md" variant="white" />
          </div>
          <h1 style={{ fontSize: 26, fontWeight: 800, color: "#F1F2F8", letterSpacing: "-0.02em", marginBottom: 6 }}>
            Welcome back
          </h1>
          <p style={{ fontSize: 14, color: "rgba(255,255,255,0.4)" }}>
            Sign in to your Threadly workspace
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div>
            <label style={labelStyle}>Email</label>
            <input
              type="email" value={email} onChange={e => setEmail(e.target.value)}
              required autoFocus placeholder="you@company.com"
              className={inputCls} style={inputStyle}
              onFocus={e => { e.target.style.borderColor = "#6366F1"; e.target.style.boxShadow = "0 0 0 3px rgba(99,102,241,0.15)"; e.target.style.background = "rgba(255,255,255,0.09)"; }}
              onBlur={e  => { e.target.style.borderColor = "rgba(255,255,255,0.1)"; e.target.style.boxShadow = "none"; e.target.style.background = "rgba(255,255,255,0.06)"; }}
            />
          </div>

          <div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
              <label style={{ ...labelStyle, marginBottom: 0 }}>Password</label>
              <span style={{ fontSize: 12, color: "#818CF8", fontWeight: 500, cursor: "pointer" }}>Forgot?</span>
            </div>
            <div style={{ position: "relative", marginTop: 6 }}>
              <input
                type={showPw ? "text" : "password"} value={password} onChange={e => setPassword(e.target.value)}
                required placeholder="Enter your password"
                className={inputCls} style={{ ...inputStyle, paddingRight: 46 }}
                onFocus={e => { e.target.style.borderColor = "#6366F1"; e.target.style.boxShadow = "0 0 0 3px rgba(99,102,241,0.15)"; e.target.style.background = "rgba(255,255,255,0.09)"; }}
                onBlur={e  => { e.target.style.borderColor = "rgba(255,255,255,0.1)"; e.target.style.boxShadow = "none"; e.target.style.background = "rgba(255,255,255,0.06)"; }}
              />
              <button type="button" tabIndex={-1} onClick={() => setShowPw(v => !v)}
                style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "rgba(255,255,255,0.35)", padding: 4 }}>
                {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {error && (
            <div style={{ borderRadius: 10, background: "rgba(239,68,68,0.12)", border: "1px solid rgba(239,68,68,0.3)", padding: "10px 14px", fontSize: 13, color: "#FCA5A5" }}>
              {error}
            </div>
          )}

          <button type="submit" disabled={loading} style={{
            width: "100%", display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
            borderRadius: 14, padding: "14px", fontSize: 15, fontWeight: 700, color: "#fff",
            background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
            border: "none", cursor: "pointer",
            boxShadow: "0 6px 24px rgba(99,102,241,0.45)",
            opacity: loading ? 0.75 : 1,
            transition: "transform 150ms ease, box-shadow 150ms ease",
            marginTop: 4, fontFamily: "inherit",
          }}
            onMouseEnter={e => { if (!loading) { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 10px 30px rgba(99,102,241,0.55)"; }}}
            onMouseLeave={e => { e.currentTarget.style.transform = "none"; e.currentTarget.style.boxShadow = "0 6px 24px rgba(99,102,241,0.45)"; }}
          >
            {loading
              ? <><span style={{ width: 16, height: 16, borderRadius: "50%", border: "2px solid rgba(255,255,255,0.3)", borderTopColor: "#fff", animation: "spin 1s linear infinite", display: "inline-block" }} />Signing in…</>
              : <>Sign in <ArrowRight size={16} /></>}
          </button>
        </form>

        <div style={{ margin: "24px 0 0", display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ flex: 1, height: 1, background: "rgba(255,255,255,0.08)" }} />
          <span style={{ fontSize: 12, color: "rgba(255,255,255,0.25)", fontWeight: 500 }}>OR</span>
          <div style={{ flex: 1, height: 1, background: "rgba(255,255,255,0.08)" }} />
        </div>

        <p style={{ textAlign: "center", fontSize: 14, color: "rgba(255,255,255,0.4)", marginTop: 20 }}>
          Don&apos;t have an account?{" "}
          <Link href="/signup" style={{ color: "#818CF8", fontWeight: 700, textDecoration: "none" }}>
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
    <div className="landing-mesh" style={{ minHeight: "100vh", position: "relative" }}>
      <div className="landing-grid" style={{ position: "fixed", inset: 0, zIndex: 0 }} />
      <FloatingDecor />

      <div style={{ position: "fixed", top: 24, left: 32, zIndex: 10 }}>
        <Logo size="sm" variant="white" href="/" />
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
