import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: "standalone",
  devIndicators: false,
  experimental: {
    serverActions: {
      allowedOrigins: ["localhost:3000"],
    },
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
    NEXT_PUBLIC_CENTRIFUGO_URL: process.env.NEXT_PUBLIC_CENTRIFUGO_URL ?? "ws://localhost:8000/connection/websocket",
  },
};

export default nextConfig;
