/**
 * DEPRECATED: Do not use hardcoded integrations export
 *
 * Integrations are now fetched dynamically from the backend API.
 * Use the useIntegrations() hook from hooks/useCatalog.ts instead.
 *
 * This file is kept for backward compatibility only during migration.
 */

export interface Integration {
  id: string;
  name: string;
  description: string;
  category: string;
  icon: string;
  color: string;
  isConnected: boolean;
  nodeType: string;
  authType: "oauth" | "api_key" | "none";
  scopes?: string[];
  connectUrl?: string;
  docUrl?: string;
}

/**
 * @deprecated Use useIntegrations() from hooks/useCatalog.ts instead
 * This export is kept only for backward compatibility during migration.
 */
export const INTEGRATIONS: Integration[] = [
  {
    id: "slack",
    name: "Slack",
    description: "Send messages to Slack channels",
    category: "Messaging",
    icon: "Send",
    color: "#E01E5A",
    isConnected: false,
    nodeType: "slack",
    authType: "oauth",
    scopes: ["chat:write", "channels:read"],
    connectUrl: "/integrations/configure/slack",
    docUrl: "https://docs.threadly.dev/integrations/slack",
  },
  {
    id: "hubspot",
    name: "HubSpot",
    description: "Sync contacts and create deals",
    category: "CRM",
    icon: "Database",
    color: "#FF5700",
    isConnected: false,
    nodeType: "hubspot",
    authType: "oauth",
    scopes: [
      "crm.objects.contacts.write",
      "crm.objects.deals.write",
    ],
    connectUrl: "/integrations/configure/hubspot",
    docUrl: "https://docs.threadly.dev/integrations/hubspot",
  },
  {
    id: "google_sheets",
    name: "Google Sheets",
    description: "Append responses to spreadsheets",
    category: "Productivity",
    icon: "Sheet",
    color: "#34A853",
    isConnected: false,
    nodeType: "google_sheets",
    authType: "oauth",
    scopes: ["spreadsheets", "drive"],
    connectUrl: "/integrations/configure/google-sheets",
    docUrl: "https://docs.threadly.dev/integrations/google-sheets",
  },
  {
    id: "twilio",
    name: "Twilio",
    description: "Send SMS and voice calls",
    category: "Messaging",
    icon: "MessageCircle",
    color: "#F22F46",
    isConnected: false,
    nodeType: "twilio",
    authType: "api_key",
    scopes: ["sms:send", "voice:calls"],
    connectUrl: "/integrations/configure/twilio",
    docUrl: "https://docs.threadly.dev/integrations/twilio",
  },
];
