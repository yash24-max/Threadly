"use client";

import Link from "next/link";

interface LogoProps {
  size?: "xs" | "sm" | "md" | "lg";
  variant?: "default" | "white" | "dark";
  href?: string;
  className?: string;
}

const sizeMap = {
  xs: { badge: 24, text: "text-base",  gap: "gap-1.5" },
  sm: { badge: 30, text: "text-lg",    gap: "gap-2"   },
  md: { badge: 36, text: "text-2xl",   gap: "gap-2.5" },
  lg: { badge: 44, text: "text-3xl",   gap: "gap-3"   },
};

export function Logo({ size = "md", variant = "default", href = "/", className = "" }: LogoProps) {
  const { badge, text, gap } = sizeMap[size];

  const textColor =
    variant === "white" ? "text-white" :
    variant === "dark"  ? "text-[#0D0E1A]" :
    "text-[var(--text-primary)]";

  const content = (
    <span className={`inline-flex items-center ${gap} select-none ${className}`}>
      {/* Badge mark */}
      <svg
        width={badge}
        height={badge}
        viewBox="0 0 44 44"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
        style={{ flexShrink: 0 }}
      >
        <defs>
          <linearGradient id="logo-grad" x1="0" y1="0" x2="44" y2="44" gradientUnits="userSpaceOnUse">
            <stop offset="0%"   stopColor="#6366F1" />
            <stop offset="60%"  stopColor="#8B5CF6" />
            <stop offset="100%" stopColor="#06B6D4" />
          </linearGradient>
        </defs>
        {/* Rounded square background */}
        <rect width="44" height="44" rx="12" fill="url(#logo-grad)" />
        {/* Chat bubble shape — stylised "T" */}
        {/* Horizontal bar */}
        <rect x="10" y="13" width="24" height="4.5" rx="2.25" fill="white" />
        {/* Vertical stem */}
        <rect x="19.75" y="17.5" width="4.5" height="10" rx="2.25" fill="white" />
        {/* Bubble tail */}
        <path d="M14 31 Q10 31 10 28 L10 27 Q10 30 13 30 L16 30 L14 33 Z" fill="white" opacity="0.85" />
      </svg>

      {/* Wordmark */}
      <span className={`font-semibold tracking-tight ${text} ${textColor}`}>
        thread<span style={{ color: "#6366F1" }}>ly</span>
      </span>
    </span>
  );

  if (href) {
    return (
      <Link href={href} className="no-underline" style={{ textDecoration: "none" }}>
        {content}
      </Link>
    );
  }

  return content;
}

export default Logo;
