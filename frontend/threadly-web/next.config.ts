import type { NextConfig } from "next";

const BACKEND_URL  = process.env.NEXT_PUBLIC_API_URL       ?? "http://localhost:8080";
const ADMIN_URL    = process.env.NEXT_PUBLIC_ADMIN_API_URL  ?? "http://localhost:3010";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: "standalone",
  devIndicators: false,
  experimental: {
    serverActions: {
      allowedOrigins: ["localhost:3000", "localhost:3002"],
    },
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
    NEXT_PUBLIC_CENTRIFUGO_URL: process.env.NEXT_PUBLIC_CENTRIFUGO_URL ?? "ws://localhost:8000/connection/websocket",
    NEXT_PUBLIC_ADMIN_API_URL:  process.env.NEXT_PUBLIC_ADMIN_API_URL  ?? "http://localhost:3010",
  },
  // Proxy API calls through the Next.js server so the browser never makes
  // cross-origin requests — eliminates CORS preflight entirely.
  async rewrites() {
    return [
      { source: "/v1/:path*",      destination: `${BACKEND_URL}/v1/:path*`    },
      { source: "/auth/:path*",    destination: `${BACKEND_URL}/auth/:path*`  },
      { source: "/admin/api/:path*", destination: `${ADMIN_URL}/admin/:path*` },
    ];
  },
};

export default nextConfig;
