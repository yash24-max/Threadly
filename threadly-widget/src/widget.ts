/**
 * Threadly Widget Loader
 *
 * This is the entry point loaded via <script> tag.
 * It reads config from data attributes, injects styles, and mounts the widget.
 *
 * Usage:
 *   <script
 *     src="https://cdn.threadly.dev/widget.js"
 *     data-bot="<botId>"
 *     data-api-url="https://api.threadly.dev"
 *     data-centrifugo-url="wss://rt.threadly.dev/connection/websocket"
 *     data-accent="#4f46e5"
 *     data-position="bottom-right"
 *     data-greeting="Hi! How can I help?"
 *     data-bot-name="Support"
 *   ></script>
 */
import type { WidgetConfig } from "./types";
import { injectStyles } from "./theme";
import { mount } from "./main";

function bootstrap() {
  // Find our own script tag
  const script =
    document.currentScript as HTMLScriptElement | null ??
    document.querySelector<HTMLScriptElement>(
      'script[data-bot][src*="widget"]'
    );

  const botId = script?.dataset.bot ?? "";
  if (!botId) {
    console.warn("[Threadly] data-bot attribute is required.");
    return;
  }

  const config: WidgetConfig = {
    botId,
    apiUrl: script?.dataset.apiUrl ?? "https://api.threadly.dev",
    centrifugoUrl:
      script?.dataset.centrifugoUrl ??
      "wss://rt.threadly.dev/connection/websocket",
    accentColor: script?.dataset.accent,
    avatarUrl: script?.dataset.avatarUrl,
    greetingText: script?.dataset.greeting ?? "Hi! How can I help you today?",
    position:
      (script?.dataset.position as WidgetConfig["position"]) ?? "bottom-right",
    botName: script?.dataset.botName ?? "Support",
  };

  // Inject CSS
  injectStyles(config);

  // Mount Preact app
  mount(config);
}

// Run after DOM ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", bootstrap);
} else {
  bootstrap();
}
