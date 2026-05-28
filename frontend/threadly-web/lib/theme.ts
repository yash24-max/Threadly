import { createTheme, alpha } from "@mui/material/styles";

// ─── Threadly MUI Theme ───────────────────────────────────────────────────────
// Dual-mode: dark for app UI, light available for marketing/auth overrides.
// Accent: Indigo #6366F1   Secondary: Violet #8B5CF6   Cyan: #06B6D4

declare module "@mui/material/styles" {
  interface Palette {
    brand: {
      gradient: string;
      primary: string;
      secondary: string;
      cyan: string;
    };
  }
  interface PaletteOptions {
    brand?: {
      gradient?: string;
      primary?: string;
      secondary?: string;
      cyan?: string;
    };
  }
}

// Shared shape / typography tokens
const shape = { borderRadius: 10 };

const typography = {
  fontFamily: "var(--font-geist-sans, 'Inter', ui-sans-serif, system-ui, sans-serif)",
  h1: { fontSize: "3rem",   fontWeight: 700, letterSpacing: "-0.03em", lineHeight: 1.15 },
  h2: { fontSize: "2.25rem", fontWeight: 700, letterSpacing: "-0.025em", lineHeight: 1.2 },
  h3: { fontSize: "1.5rem",  fontWeight: 600, letterSpacing: "-0.02em", lineHeight: 1.3 },
  h4: { fontSize: "1.25rem", fontWeight: 600, letterSpacing: "-0.015em", lineHeight: 1.35 },
  h5: { fontSize: "1.125rem", fontWeight: 600, lineHeight: 1.4 },
  h6: { fontSize: "1rem",    fontWeight: 600, lineHeight: 1.45 },
  body1: { fontSize: "0.9375rem", lineHeight: 1.65 },
  body2: { fontSize: "0.875rem",  lineHeight: 1.6  },
  caption: { fontSize: "0.75rem", lineHeight: 1.5 },
  button: { fontWeight: 600, textTransform: "none" as const, letterSpacing: "0.01em" },
};

// ─── DARK THEME (App UI) ──────────────────────────────────────────────────────
export const darkTheme = createTheme({
  palette: {
    mode: "dark",
    primary:   { main: "#6366F1", light: "#818CF8", dark: "#4F52D9", contrastText: "#fff" },
    secondary: { main: "#8B5CF6", light: "#A78BFA", dark: "#7C3AED", contrastText: "#fff" },
    error:     { main: "#EF4444" },
    warning:   { main: "#F59E0B" },
    info:      { main: "#3B82F6" },
    success:   { main: "#10B981" },
    background: { default: "#0B0C12", paper: "#131420" },
    divider: "#252637",
    text: {
      primary:   "#F1F2F8",
      secondary: "#9496B0",
      disabled:  "#5C5E72",
    },
    brand: {
      gradient: "linear-gradient(135deg, #6366F1 0%, #8B5CF6 60%, #06B6D4 100%)",
      primary:   "#6366F1",
      secondary: "#8B5CF6",
      cyan:      "#06B6D4",
    },
  },
  shape,
  typography,
  components: {
    MuiCssBaseline: {
      styleOverrides: { body: { backgroundColor: "#0B0C12", color: "#F1F2F8" } },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: {
          borderRadius: 10,
          padding: "8px 20px",
          fontWeight: 600,
          transition: "all 150ms cubic-bezier(0.16,1,0.3,1)",
        },
        containedPrimary: {
          background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
          "&:hover": {
            background: "linear-gradient(135deg, #4F52D9, #7C3AED)",
            transform: "translateY(-1px)",
            boxShadow: "0 6px 20px rgba(99,102,241,0.4)",
          },
        },
        outlinedPrimary: {
          borderColor: alpha("#6366F1", 0.5),
          "&:hover": { borderColor: "#6366F1", background: alpha("#6366F1", 0.08) },
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: "outlined" },
      styleOverrides: {
        root: {
          "& .MuiOutlinedInput-root": {
            borderRadius: 10,
            background: "#1C1D2A",
            "& fieldset": { borderColor: "#252637" },
            "&:hover fieldset": { borderColor: "#363752" },
            "&.Mui-focused fieldset": { borderColor: "#6366F1" },
          },
          "& .MuiInputLabel-root": { color: "#9496B0" },
          "& .MuiInputLabel-root.Mui-focused": { color: "#6366F1" },
        },
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          background: "#131420",
          border: "1px solid #252637",
          borderRadius: 14,
          transition: "border-color 150ms ease, box-shadow 150ms ease",
          "&:hover": { borderColor: "#363752" },
        },
      },
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: { background: "#131420", backgroundImage: "none", border: "1px solid #252637" },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 6, fontWeight: 500, fontSize: "0.75rem" },
        colorSuccess: { background: "rgba(16,185,129,0.12)", color: "#10B981", border: "1px solid rgba(16,185,129,0.2)" },
        colorWarning: { background: "rgba(245,158,11,0.12)", color: "#F59E0B", border: "1px solid rgba(245,158,11,0.2)" },
        colorError:   { background: "rgba(239,68,68,0.12)",  color: "#EF4444", border: "1px solid rgba(239,68,68,0.2)" },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { background: "#252637", color: "#F1F2F8", fontSize: "0.75rem", borderRadius: 6, padding: "5px 10px" },
        arrow: { color: "#252637" },
      },
    },
    MuiDivider: {
      styleOverrides: { root: { borderColor: "#252637" } },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: "1px 0",
          transition: "all 150ms ease",
          "&.Mui-selected": {
            background: "rgba(99,102,241,0.15)",
            color: "#818CF8",
            "& .MuiListItemIcon-root": { color: "#818CF8" },
            "&:hover": { background: "rgba(99,102,241,0.2)" },
          },
          "&:hover": { background: "#1C1D2A" },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: { root: { minWidth: 36, color: "#9496B0" } },
    },
    MuiLinearProgress: {
      styleOverrides: {
        root: { borderRadius: 4, background: "#252637" },
        bar: { background: "linear-gradient(90deg, #6366F1, #8B5CF6)" },
      },
    },
    MuiSkeleton: {
      styleOverrides: { root: { background: "#1C1D2A" } },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderBottom: "1px solid #252637", color: "#F1F2F8" },
        head: { color: "#9496B0", fontWeight: 600, fontSize: "0.75rem", textTransform: "uppercase", letterSpacing: "0.05em" },
      },
    },
    MuiAlert: {
      styleOverrides: {
        standardError:   { background: "rgba(239,68,68,0.1)",  border: "1px solid rgba(239,68,68,0.2)"  },
        standardWarning: { background: "rgba(245,158,11,0.1)", border: "1px solid rgba(245,158,11,0.2)" },
        standardSuccess: { background: "rgba(16,185,129,0.1)", border: "1px solid rgba(16,185,129,0.2)" },
        standardInfo:    { background: "rgba(59,130,246,0.1)", border: "1px solid rgba(59,130,246,0.2)"  },
      },
    },
  },
});

// ─── LIGHT THEME (Auth / Marketing MUI sections) ──────────────────────────────
export const lightTheme = createTheme({
  palette: {
    mode: "light",
    primary:   { main: "#6366F1", light: "#818CF8", dark: "#4F52D9", contrastText: "#fff" },
    secondary: { main: "#8B5CF6", light: "#A78BFA", dark: "#7C3AED", contrastText: "#fff" },
    error:     { main: "#EF4444" },
    warning:   { main: "#F59E0B" },
    info:      { main: "#3B82F6" },
    success:   { main: "#10B981" },
    background: { default: "#F8F9FF", paper: "#FFFFFF" },
    divider: "#E2E4F0",
    text: { primary: "#0D0E1A", secondary: "#5C6080", disabled: "#9496A8" },
    brand: {
      gradient: "linear-gradient(135deg, #6366F1 0%, #8B5CF6 60%, #06B6D4 100%)",
      primary:   "#6366F1",
      secondary: "#8B5CF6",
      cyan:      "#06B6D4",
    },
  },
  shape,
  typography,
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 10, padding: "9px 22px", fontWeight: 600, transition: "all 150ms ease" },
        containedPrimary: {
          background: "linear-gradient(135deg, #6366F1, #8B5CF6)",
          "&:hover": {
            background: "linear-gradient(135deg, #4F52D9, #7C3AED)",
            transform: "translateY(-1px)",
            boxShadow: "0 6px 20px rgba(99,102,241,0.35)",
          },
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: "outlined" },
      styleOverrides: {
        root: {
          "& .MuiOutlinedInput-root": {
            borderRadius: 10,
            background: "#F8F9FF",
            "& fieldset": { borderColor: "#E2E4F0" },
            "&:hover fieldset": { borderColor: "#C8CBE0" },
            "&.Mui-focused fieldset": { borderColor: "#6366F1" },
          },
        },
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: { background: "#FFFFFF", border: "1px solid #E2E4F0", borderRadius: 14 },
      },
    },
  },
});

export default darkTheme;
