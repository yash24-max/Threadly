import NextAuth from "next-auth";
import Credentials from "next-auth/providers/credentials";

// Keycloak token endpoint (ROPC grant — enabled on threadly-app client)
const KEYCLOAK_TOKEN_URL =
  `${process.env.KEYCLOAK_ISSUER ?? "http://localhost:8090/realms/threadly"}/protocol/openid-connect/token`;

const KEYCLOAK_CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID ?? "threadly-app";

/**
 * Refresh Keycloak access token using the refresh_token grant.
 * Called automatically by the JWT callback when the access token is within
 * 60 seconds of expiry.
 */
async function refreshKeycloakToken(token: any) {
  try {
    const params = new URLSearchParams({
      grant_type:    "refresh_token",
      client_id:     KEYCLOAK_CLIENT_ID,
      refresh_token: token.refreshToken,
    });

    const res = await fetch(KEYCLOAK_TOKEN_URL, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString(),
    });

    if (!res.ok) {
      console.warn("Keycloak refresh failed:", res.status);
      return { ...token, accessToken: null };
    }

    const data = await res.json();
    return {
      ...token,
      accessToken:  data.access_token,
      refreshToken: data.refresh_token ?? token.refreshToken,
      // Keycloak returns expires_in in seconds
      expiresAt: Date.now() + data.expires_in * 1000,
    };
  } catch (err) {
    console.error("Token refresh error:", err);
    return { ...token, accessToken: null };
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Credentials({
      credentials: {
        email:    { label: "Email",    type: "email"    },
        password: { label: "Password", type: "password" },
      },

      /**
       * Authenticate against Keycloak using the Resource Owner Password
       * Credentials (ROPC) grant.  This keeps our own login UI intact while
       * Keycloak becomes the authoritative token issuer.
       *
       * ROPC is enabled on the threadly-app client in the realm config
       * (directAccessGrantsEnabled: true).
       */
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) return null;

        try {
          const params = new URLSearchParams({
            grant_type: "password",
            client_id:  KEYCLOAK_CLIENT_ID,
            username:   credentials.email as string,
            password:   credentials.password as string,
            scope:      "openid profile email",
          });

          const res = await fetch(KEYCLOAK_TOKEN_URL, {
            method:  "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body:    params.toString(),
          });

          if (!res.ok) return null;

          const data = await res.json();

          // Decode the JWT payload to extract custom claims set by Protocol Mappers
          const payload = JSON.parse(
            Buffer.from(data.access_token.split(".")[1], "base64url").toString()
          );

          return {
            id:           payload.sub,
            email:        payload.email        ?? credentials.email,
            name:         payload.name         ?? payload.preferred_username ?? "",
            accessToken:  data.access_token,
            refreshToken: data.refresh_token,
            expiresIn:    data.expires_in,        // seconds
            orgId:        payload.orgId   ?? "",
            orgName:      payload.orgName ?? "",
            orgSlug:      payload.orgSlug ?? (payload.orgName ?? "").toLowerCase().replace(/[^a-z0-9]+/g, "-"),
            role:         payload.role    ?? "member",
          };
        } catch {
          return null;
        }
      },
    }),
  ],

  callbacks: {
    jwt({ token, user }) {
      // First sign-in: copy all fields from the authorized user
      if (user) {
        token.accessToken  = (user as any).accessToken;
        token.refreshToken = (user as any).refreshToken;
        token.orgId        = (user as any).orgId;
        token.orgName      = (user as any).orgName;
        token.orgSlug      = (user as any).orgSlug;
        token.role         = (user as any).role;
        token.expiresAt    = Date.now() + (user as any).expiresIn * 1000;
      }

      // Auto-refresh when within 60 s of expiry
      if (token.expiresAt && Date.now() > (token.expiresAt as number) - 60_000) {
        return refreshKeycloakToken(token);
      }

      return token;
    },

    session({ session, token }) {
      session.accessToken      = token.accessToken as string;
      session.user.orgId       = token.orgId   as string;
      session.user.orgName     = token.orgName as string;
      session.user.orgSlug     = token.orgSlug as string;
      session.user.role        = token.role    as string;
      return session;
    },
  },

  pages: {
    signIn: "/login",
    error:  "/login",
  },
  session: { strategy: "jwt" },
});

declare module "next-auth" {
  interface Session {
    accessToken: string;
    user: {
      id:      string;
      email:   string;
      name:    string;
      orgId:   string;
      orgName: string;
      orgSlug: string;
      role:    string;
    };
  }
}
