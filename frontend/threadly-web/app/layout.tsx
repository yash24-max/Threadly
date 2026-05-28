import type { Metadata } from "next";
import { GeistSans } from "geist/font/sans";
import { GeistMono } from "geist/font/mono";
import { Toaster } from "sonner";
import Providers from "@/components/providers";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "Threadly — AI Chatbot Builder",
    template: "%s | Threadly",
  },
  description: "Build AI chatbots that remember every thread. Embed a smart widget on your website in minutes.",
  keywords: ["AI chatbot", "chatbot builder", "website chat widget", "knowledge base", "customer support AI"],
  authors: [{ name: "Threadly" }],
  creator: "Threadly",
  openGraph: {
    type: "website",
    locale: "en_US",
    siteName: "Threadly",
    title: "Threadly — AI Chatbot Builder",
    description: "Build AI chatbots that remember every thread.",
    images: [{ url: "/og-image.png", width: 1200, height: 630 }],
  },
  twitter: {
    card: "summary_large_image",
    title: "Threadly — AI Chatbot Builder",
    description: "Build AI chatbots that remember every thread.",
    images: ["/og-image.png"],
  },
  robots: { index: true, follow: true },
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL ?? "https://threadly.dev"),
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning className={`${GeistSans.variable} ${GeistMono.variable}`}>
      <body style={{ background: "var(--bg-canvas)", color: "var(--text-primary)" }} className="antialiased">
        <Providers>
          {children}
          <Toaster richColors position="bottom-right" />
        </Providers>
      </body>
    </html>
  );
}
