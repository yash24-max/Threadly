# Design System

## Brand
- **Name:** Threadly
- **Tagline:** Build AI chatbots that remember every thread.
- **Personality:** Calm, professional, smart — not "startup-cute".
- **Brand accent:** `#4F46E5` (Indigo 600) — adjustable per workspace.

## Typography

| Role | Font | Weight | Size |
|---|---|---|---|
| UI labels, body | Geist Sans | 400/500 | 13–16px |
| Headings | Geist Sans | 600/700 | 18–36px |
| Code, node labels | Geist Mono | 400 | 12–14px |

## Color tokens (CSS variables)

```css
/* Backgrounds */
--bg-canvas: #09090b;       /* dark: canvas behind builder */
--bg-panel: #18181b;        /* dark: sidebar, panel surfaces */
--bg-surface: #27272a;      /* dark: cards, inputs */
--bg-hover: #3f3f46;        /* hover state */

/* Text */
--text-primary: #fafafa;
--text-secondary: #a1a1aa;
--text-muted: #71717a;
--text-inverse: #09090b;

/* Accent */
--accent: #4F46E5;          /* primary brand */
--accent-hover: #4338CA;
--accent-fg: #ffffff;

/* Semantic */
--success: #22c55e;
--warn: #f59e0b;
--danger: #ef4444;
--info: #3b82f6;

/* Borders */
--border: #3f3f46;
--border-strong: #52525b;
```

Light mode overrides:
```css
[data-theme="light"] {
  --bg-canvas: #f9f9fa;
  --bg-panel: #ffffff;
  --bg-surface: #f4f4f5;
  --text-primary: #09090b;
  --text-secondary: #52525b;
  --border: #e4e4e7;
}
```

## Spacing
4px grid. Common values: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64.

## Radius
```
--radius-sm: 4px    (badges, small chips)
--radius-md: 8px    (inputs, buttons)
--radius-lg: 12px   (cards, panels)
--radius-xl: 16px   (modals, widget bubble)
--radius-full: 9999px (pills, avatars)
```

## Shadow
```
--shadow-1: 0 1px 3px rgba(0,0,0,.12), 0 1px 2px rgba(0,0,0,.08);
--shadow-2: 0 4px 6px rgba(0,0,0,.1), 0 2px 4px rgba(0,0,0,.08);
--shadow-3: 0 10px 15px rgba(0,0,0,.1), 0 4px 6px rgba(0,0,0,.08);
```

## Motion tokens
```
--duration-instant: 80ms
--duration-fast: 160ms
--duration-base: 240ms
--duration-slow: 400ms
--ease-spring: cubic-bezier(0.16, 1, 0.3, 1)
--ease-out: cubic-bezier(0, 0, 0.2, 1)
```

## Icon system
- Library: **Lucide** (v0.400+)
- Stroke width: **1.5px**
- Default size: **16px** (inline), **20px** (button), **24px** (standalone)

## Component catalogue

| Component | Source | Notes |
|---|---|---|
| Button | shadcn/ui | `default`, `secondary`, `ghost`, `destructive`, `link` |
| Input | shadcn/ui | With label + error state |
| Dialog | shadcn/ui + Radix | Focus trap, ESC to close |
| Command palette | cmdk | Cmd-K global, `⌘K` shortcut chip |
| Toast | Sonner | Bottom-right, auto-dismiss 4s |
| Dropdown | Radix DropdownMenu | Keyboard navigable |
| Tooltip | Radix Tooltip | Delayed 300ms |
| Badge | shadcn/ui | `default`, `outline`, `success`, `warn`, `danger` |
| Skeleton | shadcn/ui | Match layout shape exactly |
| Avatar | Radix Avatar | Fallback initials |

## Builder-specific

| Element | Spec |
|---|---|
| Node card | `border-radius: 12px`, shadow-1, 200px min-width |
| Node selected | `border: 2px solid var(--accent)` |
| Node error | `border: 2px solid var(--danger)`, red dot top-right |
| Edge | Bezier curve, stroke `--border-strong`, animated when flow runs |
| Canvas bg | Dot grid pattern `--bg-canvas` |

## Widget

| Element | Spec |
|---|---|
| Launcher button | 56px circle, `var(--accent)`, shadow-3, spring animation open |
| Chat panel | 380×600px (desktop), full-screen bottom-sheet (mobile) |
| Message bubble (AI) | `bg-surface`, `radius-xl`, max-width 85% |
| Message bubble (user) | `bg-accent`, white text, `radius-xl`, right-aligned |
| Input bar | `bg-panel`, border-top, send button = icon |
| Typing indicator | 3 dots, 600ms stagger animation |
