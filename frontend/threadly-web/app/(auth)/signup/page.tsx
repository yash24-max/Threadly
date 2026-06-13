"use client";

import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Link from "next/link";
import { Eye, EyeOff, ArrowRight } from "lucide-react";
import { api } from "@/lib/api";
import { Logo } from "@/components/ui/Logo";

/* ── Floating orbs (dark-theme) ──────────────────────────────────────────── */
function FloatingDecor() {
  const bubbles = [
    { x: "5%",  y: "10%", size: 48, color: "rgba(139,92,246,0.45)", rotate: -15 },
    { x: "7%",  y: "40%", size: 64, color: "rgba(99,102,241,0.35)", rotate: 10  },
    { x: "4%",  y: "68%", size: 40, color: "rgba(6,182,212,0.4)",   rotate: -20 },
    { x: "12%", y: "80%", size: 52, color: "rgba(99,102,241,0.3)",  rotate: 15  },
    { x: "82%", y: "6%",  size: 56, color: "rgba(6,182,212,0.35)",  rotate: 8   },
    { x: "88%", y: "28%", size: 70, color: "rgba(139,92,246,0.4)",  rotate: -10 },
    { x: "84%", y: "55%", size: 42, color: "rgba(99,102,241,0.35)", rotate: 20  },
    { x: "90%", y: "76%", size: 58, color: "rgba(6,182,212,0.3)",   rotate: -5  },
  ];

  return (
    <div style={{ position: "fixed", inset: 0, pointerEvents: "none", zIndex: 0, overflow: "hidden" }}>
      {/* Glow orbs */}
      <div style={{ position: "absolute", left: "-80px", top: "20%", width: 380, height: 380, borderRadius: "50%", background: "rgba(99,102,241,0.18)", filter: "blur(80px)" }} />
      <div style={{ position: "absolute", left: "-40px", top: "62%", width: 260, height: 260, borderRadius: "50%", background: "rgba(139,92,246,0.12)", filter: "blur(60px)" }} />
      <div style={{ position: "absolute", right: "-60px", top: "8%",  width: 320, height: 320, borderRadius: "50%", background: "rgba(6,182,212,0.12)",  filter: "blur(70px)" }} />
      <div style={{ position: "absolute", right: "-30px", top: "58%", width: 220, height: 220, borderRadius: "50%", background: "rgba(99,102,241,0.10)", filter: "blur(50px)" }} />

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

/* ── Signup form ──────────────────────────────────────────────────────────── */
export default function SignupPage() {
  const router = useRouter();
  const [form, setForm]       = useState({ name: "", orgName: "", email: "", password: "" });
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw]   = useState(false);

  const update = (k: keyof typeof form) =>
    (e: React.ChangeEvent<HTMLInputElement>) => setForm(f => ({ ...f, [k]: e.target.value }));

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      // Create Keycloak user + org via onboarding endpoint
      await api.post("/v1/auth/register", {
        name:     form.name,
        orgName:  form.orgName,
        email:    form.email,
        password: form.password,
      });
      // Auto sign-in via Keycloak ROPC after registration
      const res = await signIn("credentials", { email: form.email, password: form.password, redirect: false });
      if (res?.error) { setError("Account created! Please sign in."); router.push("/login"); }
      else router.push("/dashboard");
    } catch (err: any) {
      setError(err?.message ?? err?.detail ?? "Something went wrong.");
    } finally { setLoading(false); }
  }

  const pwLen = form.password.length;
  const pwStrength = pwLen === 0 ? 0 : pwLen < 6 ? 1 : pwLen < 8 ? 2 : pwLen < 12 ? 3 : 4;
  const pwColor = ["", "#EF4444", "#F59E0B", "#F59E0B", "#10B981"][pwStrength];

  const inputStyle: React.CSSProperties = {
    width: "100%", boxSizing: "border-box",
    borderRadius: 12, border: "1.5px solid rgba(255,255,255,0.1)",
    background: "rgba(255,255,255,0.06)", padding: "12px 16px",
    fontSize: 14, color: "#F1F2F8", outline: "none",
    transition: "border-color 200ms ease, box-shadow 200ms ease, background 200ms ease",
    fontFamily: "inherit",
  };

  const labelStyle: React.CSSProperties = {
    display: "block", fontSize: 11, fontWeight: 600,
    color: "rgba(255,255,255,0.45)", marginBottom: 6,
    letterSpacing: "0.06em", textTransform: "uppercase" as const,
  };

  const inputCls = "auth-input";
  const onFocus = (e: React.FocusEvent<HTMLInputElement>) => {
    e.target.style.borderColor = "#6366F1";
    e.target.style.boxShadow   = "0 0 0 3px rgba(99,102,241,0.15)";
    e.target.style.background  = "rgba(255,255,255,0.09)";
  };
  const onBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    e.target.style.borderColor = "rgba(255,255,255,0.1)";
    e.target.style.boxShadow   = "none";
    e.target.style.background  = "rgba(255,255,255,0.06)";
  };

  return (
    <div className="landing-mesh" style={{ minHeight: "100vh", position: "relative" }}>
      <div className="landing-grid" style={{ position: "fixed", inset: 0, zIndex: 0 }} />
      <FloatingDecor />

      {/* Top-left logo */}
      <div style={{ position: "fixed", top: 24, left: 32, zIndex: 10 }}>
        <Logo size="sm" variant="white" href="/" />
      </div>

      {/* Centered card */}
      <div style={{
        minHeight: "100vh", display: "flex", flexDirection: "column",
        alignItems: "center", justifyContent: "center",
        padding: "80px 16px 32px", position: "relative", zIndex: 1,
      }}>
        <div style={{
          width: "100%", maxWidth: 480,
          background: "rgba(18,22,42,0.75)",
          backdropFilter: "blur(24px)",
          WebkitBackdropFilter: "blur(24px)",
          borderRadius: 28,
          border: "1px solid rgba(255,255,255,0.08)",
          boxShadow: "0 24px 64px rgba(0,0,0,0.45), 0 8px 24px rgba(99,102,241,0.12), inset 0 1px 0 rgba(255,255,255,0.04)",
          padding: "40px 40px 36px",
        }}>
          {/* Heading */}
          <div style={{ textAlign: "center", marginBottom: 28 }}>
            <h1 style={{ fontSize: 25, fontWeight: 800, color: "#F1F2F8", letterSpacing: "-0.02em", marginBottom: 6 }}>
              Create your account
            </h1>
            <p style={{ fontSize: 14, color: "rgba(255,255,255,0.4)" }}>
              Start your 14-day free trial — no card needed
            </p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            {/* Name + Company */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
              <div>
                <label style={labelStyle}>Full name</label>
                <input type="text" value={form.name} onChange={update("name")} required autoFocus placeholder="Alex Carter"
                  className={inputCls} style={inputStyle} onFocus={onFocus} onBlur={onBlur} />
              </div>
              <div>
                <label style={labelStyle}>Company</label>
                <input type="text" value={form.orgName} onChange={update("orgName")} required placeholder="Acme Inc"
                  className={inputCls} style={inputStyle} onFocus={onFocus} onBlur={onBlur} />
              </div>
            </div>

            {/* Email */}
            <div>
              <label style={labelStyle}>Work email</label>
              <input type="email" value={form.email} onChange={update("email")} required placeholder="alex@acme.com"
                className={inputCls} style={inputStyle} onFocus={onFocus} onBlur={onBlur} />
            </div>

            {/* Password */}
            <div>
              <label style={labelStyle}>Password</label>
              <div style={{ position: "relative" }}>
                <input type={showPw ? "text" : "password"} value={form.password} onChange={update("password")} required minLength={8} placeholder="Minimum 8 characters"
                  className={inputCls} style={{ ...inputStyle, paddingRight: 46 }} onFocus={onFocus} onBlur={onBlur} />
                <button type="button" tabIndex={-1} onClick={() => setShowPw(v => !v)}
                  style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "rgba(255,255,255,0.35)", padding: 4 }}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {pwLen > 0 && (
                <div style={{ display: "flex", gap: 4, marginTop: 6 }}>
                  {[1, 2, 3, 4].map(i => (
                    <div key={i} style={{ flex: 1, height: 3, borderRadius: 2, background: i <= pwStrength ? pwColor : "rgba(255,255,255,0.1)", transition: "background 200ms" }} />
                  ))}
                </div>
              )}
            </div>

            {error && (
              <div style={{ borderRadius: 10, background: "rgba(239,68,68,0.12)", border: "1px solid rgba(239,68,68,0.3)", padding: "9px 14px", fontSize: 13, color: "#FCA5A5" }}>
                {error}
              </div>
            )}

            <p style={{ fontSize: 11, color: "rgba(255,255,255,0.3)", lineHeight: 1.5 }}>
              By creating an account you agree to our{" "}
              <Link href="/terms" style={{ color: "#818CF8", textDecoration: "underline" }}>Terms</Link>
              {" "}and{" "}
              <Link href="/privacy" style={{ color: "#818CF8", textDecoration: "underline" }}>Privacy Policy</Link>.
            </p>

            <button type="submit" disabled={loading} style={{
              width: "100%", display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
              borderRadius: 14, padding: "14px", fontSize: 15, fontWeight: 700, color: "#fff",
              background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
              border: "none", cursor: "pointer",
              boxShadow: "0 6px 24px rgba(99,102,241,0.45)",
              opacity: loading ? 0.75 : 1, fontFamily: "inherit",
              transition: "transform 150ms ease, box-shadow 150ms ease",
            }}
              onMouseEnter={e => { if (!loading) { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 10px 30px rgba(99,102,241,0.55)"; }}}
              onMouseLeave={e => { e.currentTarget.style.transform = "none"; e.currentTarget.style.boxShadow = "0 6px 24px rgba(99,102,241,0.45)"; }}
            >
              {loading
                ? <><span style={{ width: 16, height: 16, borderRadius: "50%", border: "2px solid rgba(255,255,255,0.3)", borderTopColor: "#fff", animation: "spin 1s linear infinite", display: "inline-block" }} />Creating…</>
                : <>Create free account <ArrowRight size={16} /></>}
            </button>
          </form>

          <div style={{ margin: "20px 0 0", display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ flex: 1, height: 1, background: "rgba(255,255,255,0.08)" }} />
            <span style={{ fontSize: 12, color: "rgba(255,255,255,0.25)", fontWeight: 500 }}>OR</span>
            <div style={{ flex: 1, height: 1, background: "rgba(255,255,255,0.08)" }} />
          </div>

          <p style={{ textAlign: "center", fontSize: 14, color: "rgba(255,255,255,0.4)", marginTop: 18 }}>
            Already have an account?{" "}
            <Link href="/login" style={{ color: "#818CF8", fontWeight: 700, textDecoration: "none" }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
