import { auth } from "@/auth";
import { NextResponse } from "next/server";

// All public marketing + auth paths — no login required
const PUBLIC_PREFIXES = [
  "/",
  "/login",
  "/signup",
  "/product",
  "/use-cases",
  "/pricing",
  "/blog",
  "/docs",
  "/help",
  "/case-studies",
  "/comparison",
  "/channels",
  "/docs",
  "/help",
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

  if (!req.auth && !isPublic) {
    const loginUrl = new URL("/login", req.url);
    loginUrl.searchParams.set("callbackUrl", req.url);
    return NextResponse.redirect(loginUrl);
  }

  if (req.auth && (pathname === "/login" || pathname === "/signup")) {
    return NextResponse.redirect(new URL("/dashboard", req.url));
  }

  return NextResponse.next();
});

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|.*\\..*).*)"],
};
