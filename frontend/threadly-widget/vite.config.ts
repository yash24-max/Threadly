import { defineConfig } from "vite";
import preact from "@preact/preset-vite";
import compression from "vite-plugin-compression";

export default defineConfig({
  plugins: [
    preact(),
    compression({ algorithm: "gzip", ext: ".gz" }),
    compression({ algorithm: "brotliCompress", ext: ".br" }),
  ],
  test: {
    globals: true,
    environment: "jsdom",
    include: ["src/__tests__/**/*.test.ts"],
    setupFiles: [],
    coverage: {
      reporter: ["text", "lcov"],
      include: ["src/**/*.ts"],
      exclude: ["src/__tests__/**", "src/widget.ts"],
    },
  },
  build: {
    lib: {
      entry: "src/widget.ts",
      name: "ThreadlyWidget",
      fileName: "widget",
      formats: ["iife"],
    },
    rollupOptions: {
      output: {
        // Inline everything into one file — the widget must be self-contained
        inlineDynamicImports: true,
      },
    },
    // Target: < 35KB gzipped
    minify: "terser",
    terserOptions: {
      compress: { drop_console: true, passes: 2 },
      mangle: true,
    },
    outDir: "dist",
    emptyOutDir: true,
    reportCompressedSize: true,
  },
  define: {
    // Allow tree-shaking of dev-only code
    "process.env.NODE_ENV": '"production"',
  },
});
