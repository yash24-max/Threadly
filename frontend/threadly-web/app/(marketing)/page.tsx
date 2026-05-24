import Link from "next/link";

export default function LandingPage() {
  return (
    <main style={{ background: "var(--bg-canvas)", minHeight: "100vh", color: "var(--text-primary)" }}>
      {/* Nav */}
      <nav style={{
        display: "flex", alignItems: "center", justifyContent: "space-between",
        padding: "20px 48px", borderBottom: "1px solid var(--border)",
        background: "var(--bg-canvas)/80", backdropFilter: "blur(12px)",
        position: "sticky", top: 0, zIndex: 50,
      }}>
        <span style={{ fontSize: 20, fontWeight: 700, letterSpacing: "-0.5px" }}>
          <span style={{ color: "var(--accent)" }}>thread</span>ly
        </span>
        <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
          <Link href="/login" style={{
            color: "var(--text-secondary)", textDecoration: "none", fontSize: 14,
          }}>
            Sign in
          </Link>
          <Link href="/signup" style={{
            background: "var(--accent)", color: "var(--accent-fg)", textDecoration: "none",
            padding: "8px 20px", borderRadius: "var(--radius-md)", fontSize: 14, fontWeight: 500,
          }}>
            Get started free
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section style={{
        maxWidth: 800, margin: "0 auto", padding: "120px 48px 80px",
        textAlign: "center",
      }}>
        <div style={{
          display: "inline-flex", alignItems: "center", gap: 8,
          background: "var(--bg-surface)", border: "1px solid var(--border)",
          borderRadius: "var(--radius-full)", padding: "6px 16px",
          fontSize: 13, color: "var(--text-secondary)", marginBottom: 32,
        }}>
          <span style={{
            width: 6, height: 6, borderRadius: "50%", background: "var(--success)",
            display: "inline-block",
          }} />
          Now in closed beta — apply for early access
        </div>

        <h1 style={{
          fontSize: "clamp(40px, 6vw, 72px)", fontWeight: 800,
          lineHeight: 1.05, letterSpacing: "-2px", marginBottom: 24,
        }}>
          Build AI chatbots that<br />
          <span style={{ color: "var(--accent)" }}>remember every thread</span>
        </h1>

        <p style={{
          fontSize: 20, color: "var(--text-secondary)", lineHeight: 1.6,
          marginBottom: 48, maxWidth: 560, margin: "0 auto 48px",
        }}>
          Drag-and-drop flow builder, knowledge base RAG, and a widget that
          embeds on your site in under 5 minutes. No code required.
        </p>

        <div style={{ display: "flex", gap: 16, justifyContent: "center", flexWrap: "wrap" }}>
          <Link href="/signup" style={{
            background: "var(--accent)", color: "var(--accent-fg)", textDecoration: "none",
            padding: "14px 32px", borderRadius: "var(--radius-md)",
            fontSize: 16, fontWeight: 600,
            boxShadow: "0 0 0 0 var(--accent)",
          }}>
            Start building for free →
          </Link>
          <a href="#demo" style={{
            color: "var(--text-primary)", textDecoration: "none",
            padding: "14px 32px", borderRadius: "var(--radius-md)",
            fontSize: 16, fontWeight: 500,
            border: "1px solid var(--border)",
          }}>
            Watch demo
          </a>
        </div>
      </section>

      {/* Feature cards */}
      <section style={{
        maxWidth: 1100, margin: "0 auto", padding: "80px 48px",
        display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: 24,
      }}>
        {[
          {
            icon: "⚡",
            title: "Visual Flow Builder",
            desc: "Drag-and-drop nodes — Message, Question, Condition, AI Reply, API Call. Autosaves as you build.",
          },
          {
            icon: "🧠",
            title: "Knowledge Base RAG",
            desc: "Upload PDFs, TXT, or URLs. Your bot instantly cites from your documents using vector search.",
          },
          {
            icon: "💬",
            title: "Website Widget",
            desc: "Single <script> tag. Streaming AI replies. Mobile-responsive. Fully brandable in your colors.",
          },
          {
            icon: "👤",
            title: "Human Handoff",
            desc: "One-click takeover. Agents see live transcripts. Seamlessly return to AI when done.",
          },
          {
            icon: "📊",
            title: "Conversation Analytics",
            desc: "Track conversations, response times, AI costs, and satisfaction — all in a live dashboard.",
          },
          {
            icon: "🔌",
            title: "API & Webhooks",
            desc: "REST API with typed SDKs. Connect to your CRM, helpdesk, or any third-party tool.",
          },
        ].map((f) => (
          <div
            key={f.title}
            style={{
              background: "var(--bg-panel)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-lg)",
              padding: 28,
            }}
          >
            <div style={{ fontSize: 28, marginBottom: 12 }}>{f.icon}</div>
            <h3 style={{ fontSize: 17, fontWeight: 600, marginBottom: 8 }}>{f.title}</h3>
            <p style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.6 }}>{f.desc}</p>
          </div>
        ))}
      </section>

      {/* CTA */}
      <section style={{
        textAlign: "center", padding: "80px 48px 120px",
        borderTop: "1px solid var(--border)",
      }}>
        <h2 style={{ fontSize: 36, fontWeight: 700, marginBottom: 16, letterSpacing: "-1px" }}>
          Ready to ship your first bot?
        </h2>
        <p style={{ color: "var(--text-secondary)", marginBottom: 32, fontSize: 17 }}>
          Free during beta. No credit card required.
        </p>
        <Link href="/signup" style={{
          background: "var(--accent)", color: "var(--accent-fg)", textDecoration: "none",
          padding: "16px 40px", borderRadius: "var(--radius-md)",
          fontSize: 17, fontWeight: 600,
        }}>
          Get started free
        </Link>
      </section>

      <footer style={{
        borderTop: "1px solid var(--border)", padding: "24px 48px",
        color: "var(--text-muted)", fontSize: 13, textAlign: "center",
      }}>
        © 2025 Threadly · Built AI-first
      </footer>
    </main>
  );
}
