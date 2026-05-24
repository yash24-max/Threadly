import { Sidebar } from "@/components/layout/Sidebar"
import { CommandPalette } from "@/components/command-palette"

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ display: "flex", height: "100vh", overflow: "hidden", background: "var(--bg-canvas)" }}>
      <Sidebar />
      <main style={{ flex: 1, overflow: "auto" }}>{children}</main>
      <CommandPalette />
    </div>
  )
}
