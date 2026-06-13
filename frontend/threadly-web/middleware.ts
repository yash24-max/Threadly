import { auth } from "@/auth";
import { NextResponse } from "next/server";

// All public marketing + auth paths — no login required
const PUBLIC_PREFIXES = [
  "/",
  "/login",
  "/signup",
  "/use-cases",
  "/pricing",
  "/blog",
  "/docs",
  "/help",
  "/case-studies",
  "/comparison",
  "/channels",
  "/about",
  "/contact",
  "/privacy",
  "/terms",
  "/cookies",
  "/changelog",
  "/api/auth",
];

export default auth((req) => {
  const { pathname } = req.nextUrl;

  const isPublic =
    pathname === "/" ||
    PUBLIC_PREFIXES.some(
      (p) => pathname === p || pathname.startsWith(p + "/") || pathname.startsWith("/api/auth")
    );

  // A session is only "valid" if it has a real accessToken.
  // req.auth can be truthy with accessToken=null when the refresh token has expired —
  // in that case we must NOT treat the user as logged in.
  const hasValidSession = !!(req.auth as any)?.accessToken;

  if (!hasValidSession && !isPublic) {
    const loginUrl = new URL("/login", req.url);
    loginUrl.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (hasValidSession && (pathname === "/login" || pathname === "/signup")) {
    return NextResponse.redirect(new URL("/dashboard", req.url));
  }

  return NextResponse.next();
});

export const config = {
  // Exclude static assets, images, favicons, file extensions, and API proxy paths
  matcher: ["/((?!_next/static|_next/image|favicon.ico|v1/|auth/|.*\\..*).*)"],
};
