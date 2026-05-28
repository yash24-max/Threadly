import { Sidebar } from "@/components/layout/Sidebar";
import { CommandPalette } from "@/components/command-palette";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    /* Force dark theme for the app shell */
    <div
      className="dark"
      style={{
        display: "flex",
        height: "100vh",
        overflow: "hidden",
        background: "var(--bg-canvas)",
        colorScheme: "dark",
      }}
    >
      <Sidebar />
      <main
        style={{
          flex: 1,
          overflow: "auto",
          display: "flex",
          flexDirection: "column",
          minWidth: 0,
          background: "var(--bg-canvas)",
        }}
      >
        {children}
      </main>
      <CommandPalette />
    </div>
  );
}
