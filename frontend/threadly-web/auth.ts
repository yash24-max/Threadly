import NextAuth from "next-auth";
import Credentials from "next-auth/providers/credentials";
import type { AuthResponse } from "@/lib/types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Generate a trace ID for distributed tracing across Nginx gateway and microservices.
 */
function generateTraceId(): string {
  return Array.from(crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

/**
 * Refresh access token via Nginx gateway (routes to identity-service).
 * Returns updated token with new accessToken and expiresAt.
 */
async function refreshAccessToken(token: any) {
  try {
    const traceId = generateTraceId();
    // Backend RefreshTokenRequest expects { refreshToken } in request body (not Authorization header)
    const res = await fetch(`${API_BASE}/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Trace-ID": traceId,
      },
      body: JSON.stringify({ refreshToken: token.refreshToken }),
    });

    if (!res.ok) {
      // Refresh failed — token is invalid, return null to force re-login
      return { ...token, accessToken: null };
    }

    const data = await res.json();
    return {
      ...token,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken ?? token.refreshToken,
      expiresAt: Date.now() + 15 * 60 * 1000,
    };
  } catch (error) {
    console.error("Token refresh failed:", error);
    // Return token as-is on network error; user will be re-logged on next request failure
    return token;
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Credentials({
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) return null;
        try {
          const traceId = generateTraceId();
          // Login endpoint routed through Nginx gateway to identity-service
          const res = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "X-Trace-ID": traceId,
            },
            body: JSON.stringify({
              email: credentials.email,
              password: credentials.password,
            }),
          });
          if (!res.ok) return null;
          // LoginResponse (Spring Boot flat shape):
          // { userId, email, fullName, organizationId, accessToken, refreshToken, expiresIn, tokenType }
          const data = await res.json();
          return {
            id: data.userId ?? data.user?.id,
            email: data.email ?? data.user?.email,
            name: data.fullName ?? data.user?.name,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            orgId: data.organizationId ?? data.user?.org?.id,
            orgName: data.organizationName ?? data.user?.org?.name ?? "",
            orgSlug: data.organizationSlug ?? data.user?.org?.slug ?? "",
            role: data.role ?? data.user?.role ?? "OWNER",
          };
        } catch {
          return null;
        }
      },
    }),
  ],
  callbacks: {
    jwt({ token, user, account }) {
      if (user) {
        token.accessToken = (user as any).accessToken;
        token.refreshToken = (user as any).refreshToken;
        token.orgId = (user as any).orgId;
        token.orgName = (user as any).orgName;
        token.orgSlug = (user as any).orgSlug;
        token.role = (user as any).role;
        token.expiresAt = Date.now() + 15 * 60 * 1000; // 15 min expiry
      }

      // Refresh token if expired (within 1 min)
      if (token.expiresAt && Date.now() > (token.expiresAt as number) - 60 * 1000) {
        return refreshAccessToken(token);
      }

      return token;
    },
    session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.user.orgId = token.orgId as string;
      session.user.orgName = token.orgName as string;
      session.user.orgSlug = token.orgSlug as string;
      session.user.role = token.role as string;
      return session;
    },
  },
  pages: {
    signIn: "/login",
    error: "/login",
  },
  session: { strategy: "jwt" },
});

declare module "next-auth" {
  interface Session {
    accessToken: string;
    user: {
      id: string;
      email: string;
      name: string;
      orgId: string;
      orgName: string;
      orgSlug: string;
      role: string;
    };
  }
}
