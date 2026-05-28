"use client";

import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Link from "next/link";
import { Eye, EyeOff, ArrowRight } from "lucide-react";
import { api } from "@/lib/api";
import { Logo } from "@/components/ui/Logo";

/* ── Floating decorative background elements ─────────────────────────────── */
function FloatingDecor() {
  const bubbles = [
    { x: "5%",  y: "10%", size: 48, color: "#FDE68A", rotate: -15, opacity: 0.65 },
    { x: "7%",  y: "40%", size: 64, color: "#C7D2FE", rotate: 10,  opacity: 0.5  },
    { x: "4%",  y: "68%", size: 40, color: "#FCA5A5", rotate: -20, opacity: 0.6  },
    { x: "12%", y: "80%", size: 52, color: "#DDD6FE", rotate: 15,  opacity: 0.5  },
    { x: "82%", y: "6%",  size: 56, color: "#A5F3FC", rotate: 8,   opacity: 0.45 },
    { x: "88%", y: "28%", size: 70, color: "#C7D2FE", rotate: -10, opacity: 0.5  },
    { x: "84%", y: "55%", size: 42, color: "#FDE68A", rotate: 20,  opacity: 0.6  },
    { x: "90%", y: "76%", size: 58, color: "#FCA5A5", rotate: -5,  opacity: 0.5  },
    { x: "24%", y: "5%",  size: 26, color: "#6366F1", rotate: 12,  opacity: 0.2  },
    { x: "70%", y: "88%", size: 30, color: "#8B5CF6", rotate: -18, opacity: 0.2  },
  ];

  return (
    <div style={{ position: "fixed", inset: 0, pointerEvents: "none", zIndex: 0, overflow: "hidden" }}>
      {bubbles.map((b, i) => (
        <div key={i} style={{ position: "absolute", left: b.x, top: b.y, transform: `rotate(${b.rotate}deg)`, opacity: b.opacity }}>
          <svg width={b.size} height={b.size} viewBox="0 0 48 48" fill="none">
            <rect x="2" y="2" width="36" height="28" rx="8" fill={b.color} />
            <path d="M8 30 L4 38 L16 30 Z" fill={b.color} />
            <rect x="9" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
            <rect x="17" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
            <rect x="25" y="11" width="5" height="5" rx="2.5" fill="white" opacity="0.7" />
          </svg>
        </div>
      ))}
      <div style={{ position: "absolute", left: "-60px", top: "25%", width: 200, height: 200, borderRadius: "50%", background: "#C7D2FE", opacity: 0.25 }} />
      <div style={{ position: "absolute", right: "-40px", bottom: "20%", width: 160, height: 160, borderRadius: "50%", background: "#DDD6FE", opacity: 0.3 }} />
      <div style={{ position: "absolute", right: "10%", top: "-40px", width: 130, height: 130, borderRadius: "50%", background: "#A5F3FC", opacity: 0.2 }} />
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
      // Map form fields to SignupRequest DTO shape expected by identity-service:
      // { email, fullName, password, organizationName }
      await api.post("/v1/auth/signup", {
        email: form.email,
        fullName: form.name,
        password: form.password,
        organizationName: form.orgName,
      });
      const res = await signIn("credentials", { email: form.email, password: form.password, redirect: false });
      if (res?.error) { setError("Account created! Please sign in."); router.push("/login"); }
      else router.push("/dashboard");
    } catch (err: any) {
      setError(err?.message ?? err?.detail ?? "Something went wrong.");
    } finally { setLoading(false); }
  }

  const pwLen = form.password.length;
  const pwStrength = pwLen === 0 ? 0 : pwLen < 6 ? 1 : pwLen < 8 ? 2 : pwLen < 12 ? 3 : 4;
  const pwColor = ["", "#EF4444","#F59E0B","#F59E0B","#059669"][pwStrength];

  const inputStyle: React.CSSProperties = {
    width: "100%", boxSizing: "border-box",
    borderRadius: 12, border: "1.5px solid #E8EAF6",
    background: "#FAFAFE", padding: "12px 16px",
    fontSize: 14, color: "#1A1A2E", outline: "none",
    transition: "border-color 200ms ease, box-shadow 200ms ease",
    fontFamily: "inherit",
  };

  const labelStyle: React.CSSProperties = {
    display: "block", fontSize: 12, fontWeight: 600, color: "#374151",
    marginBottom: 6, letterSpacing: "0.02em", textTransform: "uppercase" as const,
  };

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

      {/* Centered card */}
      <div style={{
        minHeight: "100vh", display: "flex", flexDirection: "column",
        alignItems: "center", justifyContent: "center",
        padding: "80px 16px 32px", position: "relative", zIndex: 1,
      }}>
        <div style={{
          width: "100%", maxWidth: 480,
          background: "rgba(255,255,255,0.95)",
          backdropFilter: "blur(20px)",
          borderRadius: 28,
          boxShadow: "0 24px 64px rgba(99,102,241,0.14), 0 8px 24px rgba(99,102,241,0.08)",
          padding: "40px 40px 36px",
        }}>
          {/* Logo + heading */}
          <div style={{ textAlign: "center", marginBottom: 28 }}>
            <h1 style={{ fontSize: 25, fontWeight: 800, color: "#1A1A2E", letterSpacing: "-0.02em", marginBottom: 6 }}>
              Create your account
            </h1>
            <p style={{ fontSize: 14, color: "#6B7280" }}>
              Start your 14-day free trial — no card needed
            </p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            {/* Row: name + company */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
              <div>
                <label style={labelStyle}>Full name</label>
                <input type="text" value={form.name} onChange={update("name")} required autoFocus placeholder="Alex Carter"
                  style={inputStyle}
                  onFocus={e => { e.target.style.borderColor="#6366F1"; e.target.style.boxShadow="0 0 0 3px rgba(99,102,241,0.1)"; }}
                  onBlur={e  => { e.target.style.borderColor="#E8EAF6"; e.target.style.boxShadow="none"; }} />
              </div>
              <div>
                <label style={labelStyle}>Company</label>
                <input type="text" value={form.orgName} onChange={update("orgName")} required placeholder="Acme Inc"
                  style={inputStyle}
                  onFocus={e => { e.target.style.borderColor="#6366F1"; e.target.style.boxShadow="0 0 0 3px rgba(99,102,241,0.1)"; }}
                  onBlur={e  => { e.target.style.borderColor="#E8EAF6"; e.target.style.boxShadow="none"; }} />
              </div>
            </div>

            {/* Email */}
            <div>
              <label style={labelStyle}>Work email</label>
              <input type="email" value={form.email} onChange={update("email")} required placeholder="alex@acme.com"
                style={inputStyle}
                onFocus={e => { e.target.style.borderColor="#6366F1"; e.target.style.boxShadow="0 0 0 3px rgba(99,102,241,0.1)"; }}
                onBlur={e  => { e.target.style.borderColor="#E8EAF6"; e.target.style.boxShadow="none"; }} />
            </div>

            {/* Password */}
            <div>
              <label style={labelStyle}>Password</label>
              <div style={{ position: "relative" }}>
                <input type={showPw ? "text" : "password"} value={form.password} onChange={update("password")} required minLength={8} placeholder="Minimum 8 characters"
                  style={{ ...inputStyle, paddingRight: 46 }}
                  onFocus={e => { e.target.style.borderColor="#6366F1"; e.target.style.boxShadow="0 0 0 3px rgba(99,102,241,0.1)"; }}
                  onBlur={e  => { e.target.style.borderColor="#E8EAF6"; e.target.style.boxShadow="none"; }} />
                <button type="button" tabIndex={-1} onClick={() => setShowPw(v => !v)}
                  style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "#9CA3AF", padding: 4 }}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {pwLen > 0 && (
                <div style={{ display: "flex", gap: 4, marginTop: 6 }}>
                  {[1,2,3,4].map(i => (
                    <div key={i} style={{ flex: 1, height: 3, borderRadius: 2, background: i <= pwStrength ? pwColor : "#E8EAF6", transition: "background 200ms" }} />
                  ))}
                </div>
              )}
            </div>

            {error && (
              <div style={{ borderRadius: 10, background: "#FEF2F2", border: "1px solid #FECACA", padding: "9px 14px", fontSize: 13, color: "#DC2626" }}>{error}</div>
            )}

            <p style={{ fontSize: 11, color: "#9CA3AF", lineHeight: 1.5 }}>
              By creating an account you agree to our{" "}
              <Link href="/terms" style={{ color: "#6366F1", textDecoration: "underline" }}>Terms</Link>
              {" "}and{" "}
              <Link href="/privacy" style={{ color: "#6366F1", textDecoration: "underline" }}>Privacy Policy</Link>.
            </p>

            <button type="submit" disabled={loading} style={{
              width: "100%", display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
              borderRadius: 14, padding: "14px", fontSize: 15, fontWeight: 700, color: "#fff",
              background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
              border: "none", cursor: "pointer",
              boxShadow: "0 6px 20px rgba(99,102,241,0.4)",
              opacity: loading ? 0.75 : 1, fontFamily: "inherit",
            }}
              onMouseEnter={e => { if (!loading) { (e.target as HTMLButtonElement).style.transform = "translateY(-1px)"; (e.target as HTMLButtonElement).style.boxShadow = "0 8px 24px rgba(99,102,241,0.5)"; }}}
              onMouseLeave={e => { (e.target as HTMLButtonElement).style.transform = "none"; (e.target as HTMLButtonElement).style.boxShadow = "0 6px 20px rgba(99,102,241,0.4)"; }}
            >
              {loading
                ? <><span style={{ width: 16, height: 16, borderRadius: "50%", border: "2px solid rgba(255,255,255,0.3)", borderTopColor: "#fff", animation: "spin 1s linear infinite", display: "inline-block" }} />Creating…</>
                : <>Create free account <ArrowRight size={16} /></>}
            </button>
          </form>

          <div style={{ margin: "20px 0 0", display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ flex: 1, height: 1, background: "#F3F4F6" }} />
            <span style={{ fontSize: 12, color: "#9CA3AF", fontWeight: 500 }}>OR</span>
            <div style={{ flex: 1, height: 1, background: "#F3F4F6" }} />
          </div>

          <p style={{ textAlign: "center", fontSize: 14, color: "#6B7280", marginTop: 18 }}>
            Already have an account?{" "}
            <Link href="/login" style={{ color: "#6366F1", fontWeight: 700, textDecoration: "none" }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
