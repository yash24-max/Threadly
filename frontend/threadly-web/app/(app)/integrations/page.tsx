"use client"

import { useState, useCallback } from "react"
import { motion, AnimatePresence } from "framer-motion"
import {
  Search,
  X,
  CheckCircle2,
  Slack,
  Mail,
  Zap,
  Sheet,
  Database,
  MessageSquare,
  Send,
  Github,
  FileText,
  BarChart3,
  DollarSign,
  ShoppingCart,
  Users,
  GitBranch,
  Layers,
} from "lucide-react"
import { cn } from "@/lib/utils"

interface Integration {
  id: string
  name: string
  category: "Messaging" | "CRM" | "Productivity" | "Analytics" | "E-commerce"
  description: string
  logo: React.ComponentType<{ size?: number; className?: string }>
  color: string
  connected: boolean
  actions: string[]
}

const INTEGRATIONS: Integration[] = [
  {
    id: "slack",
    name: "Slack",
    category: "Messaging",
    description: "Send messages to Slack channels",
    logo: Slack,
    color: "#36C5F0",
    connected: false,
    actions: ["Send Message", "Create Thread", "Upload File"],
  },
  {
    id: "gmail",
    name: "Gmail",
    category: "Messaging",
    description: "Send and receive emails",
    logo: Mail,
    color: "#EA4335",
    connected: true,
    actions: ["Send Email", "Create Draft", "Add Label"],
  },
  {
    id: "hubspot",
    name: "HubSpot",
    category: "CRM",
    description: "Sync contacts and create deals",
    logo: Zap,
    color: "#FF7A59",
    connected: false,
    actions: ["Create Contact", "Update Deal", "Log Activity"],
  },
  {
    id: "notion",
    name: "Notion",
    category: "Productivity",
    description: "Create pages and database entries",
    logo: FileText,
    color: "#000000",
    connected: true,
    actions: ["Create Page", "Add Database Item", "Update Property"],
  },
  {
    id: "google_sheets",
    name: "Google Sheets",
    category: "Productivity",
    description: "Append rows and update cells",
    logo: Sheet,
    color: "#34A853",
    connected: false,
    actions: ["Append Row", "Update Cell", "Create Chart"],
  },
  {
    id: "sendgrid",
    name: "SendGrid",
    category: "Messaging",
    description: "Send transactional emails",
    logo: Mail,
    color: "#00A8E1",
    connected: false,
    actions: ["Send Email", "Track Opens", "Manage Lists"],
  },
  {
    id: "twilio",
    name: "Twilio",
    category: "Messaging",
    description: "Send SMS and WhatsApp messages",
    logo: MessageSquare,
    color: "#F22F46",
    connected: false,
    actions: ["Send SMS", "Send WhatsApp", "Make Call"],
  },
  {
    id: "stripe",
    name: "Stripe",
    category: "E-commerce",
    description: "Process payments and manage subscriptions",
    logo: DollarSign,
    color: "#625BEE",
    connected: false,
    actions: ["Create Payment", "Manage Subscription", "Refund"],
  },
  {
    id: "shopify",
    name: "Shopify",
    category: "E-commerce",
    description: "Sync orders and product data",
    logo: ShoppingCart,
    color: "#96BE00",
    connected: false,
    actions: ["Get Order", "Update Product", "Create Fulfillment"],
  },
  {
    id: "discord",
    name: "Discord",
    category: "Messaging",
    description: "Send messages to Discord servers",
    logo: Send,
    color: "#5865F2",
    connected: false,
    actions: ["Send Message", "Create Invite", "Manage Roles"],
  },
  {
    id: "github",
    name: "GitHub",
    category: "Productivity",
    description: "Create issues and manage repositories",
    logo: Github,
    color: "#181717",
    connected: false,
    actions: ["Create Issue", "Comment", "Create Pull Request"],
  },
  {
    id: "linear",
    name: "Linear",
    category: "Productivity",
    description: "Manage issues and projects",
    logo: GitBranch,
    color: "#5E6AD2",
    connected: false,
    actions: ["Create Issue", "Update Status", "Add Comment"],
  },
  {
    id: "jira",
    name: "Jira",
    category: "Productivity",
    description: "Create and update issues",
    logo: Zap,
    color: "#0052CC",
    connected: false,
    actions: ["Create Issue", "Transition Status", "Add Comment"],
  },
  {
    id: "airtable",
    name: "Airtable",
    category: "Productivity",
    description: "Create records in bases",
    logo: Layers,
    color: "#18BFFF",
    connected: false,
    actions: ["Create Record", "Update Record", "Delete Record"],
  },
  {
    id: "mailchimp",
    name: "Mailchimp",
    category: "Analytics",
    description: "Manage subscribers and campaigns",
    logo: Mail,
    color: "#FFE01B",
    connected: false,
    actions: ["Add Subscriber", "Create Campaign", "Manage List"],
  },
  {
    id: "mixpanel",
    name: "Mixpanel",
    category: "Analytics",
    description: "Track user events and properties",
    logo: BarChart3,
    color: "#25293C",
    connected: false,
    actions: ["Track Event", "Set Property", "Create Cohort"],
  },
  {
    id: "segment",
    name: "Segment",
    category: "Analytics",
    description: "Route data to 300+ destinations",
    logo: Database,
    color: "#00D4AA",
    connected: false,
    actions: ["Track Event", "Identify User", "Group Event"],
  },
  {
    id: "make",
    name: "Make.com",
    category: "Productivity",
    description: "Connect to 1000+ apps",
    logo: Zap,
    color: "#FF6B6B",
    connected: false,
    actions: ["Trigger Scenario", "Execute Module", "Get Data"],
  },
  {
    id: "teams",
    name: "Microsoft Teams",
    category: "Messaging",
    description: "Send messages to Teams channels",
    logo: Users,
    color: "#6264A7",
    connected: false,
    actions: ["Send Message", "Create Chat", "Share File"],
  },
  {
    id: "salesforce",
    name: "Salesforce",
    category: "CRM",
    description: "Manage leads, accounts, and opportunities",
    logo: Database,
    color: "#00A1DE",
    connected: false,
    actions: ["Create Lead", "Update Account", "Create Opportunity"],
  },
]

interface AuthSheetProps {
  integration: Integration | null
  onClose: () => void
}

function AuthSheet({ integration, onClose }: AuthSheetProps) {
  const [email, setEmail] = useState("")
  const [apiKey, setApiKey] = useState("")
  const [testing, setTesting] = useState(false)
  const [testPassed, setTestPassed] = useState(false)

  const handleTestConnection = async () => {
    setTesting(true)
    await new Promise((r) => setTimeout(r, 1000))
    setTestPassed(true)
    setTesting(false)
  }

  const handleSave = () => {
    onClose()
    setEmail("")
    setApiKey("")
    setTestPassed(false)
  }

  if (!integration) return null

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center"
        onClick={onClose}
      >
        <motion.div
          initial={{ x: "100%" }}
          animate={{ x: 0 }}
          exit={{ x: "100%" }}
          transition={{ type: "spring", damping: 20 }}
          onClick={(e) => e.stopPropagation()}
          className="w-full max-w-md bg-white dark:bg-slate-900 rounded-lg shadow-xl overflow-hidden"
        >
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b dark:border-slate-700">
            <div className="flex items-center gap-3">
              <div
                className="w-10 h-10 rounded-lg flex items-center justify-center"
                style={{ background: integration.color + "20" }}
              >
                <integration.logo size={20} style={{ color: integration.color }} />
              </div>
              <div>
                <h2 className="font-semibold text-slate-900 dark:text-white">
                  Connect {integration.name}
                </h2>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  {integration.description}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded transition-colors"
            >
              <X size={20} className="text-slate-500" />
            </button>
          </div>

          {/* Body */}
          <div className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your@email.com"
                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                API Key / Secret
              </label>
              <input
                type="password"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="••••••••••••••••"
                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Available Actions */}
            <div>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase mb-2">
                Available Actions
              </p>
              <div className="flex flex-wrap gap-1.5">
                {integration.actions.map((action) => (
                  <span
                    key={action}
                    className="px-2 py-1 text-xs rounded-full bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300"
                  >
                    {action}
                  </span>
                ))}
              </div>
            </div>

            {/* Test Connection */}
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={handleTestConnection}
              disabled={!email || !apiKey || testing}
              className={cn(
                "w-full py-2 rounded-lg font-medium transition-all",
                testPassed
                  ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300"
                  : "bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 disabled:opacity-50"
              )}
            >
              {testing ? "Testing..." : testPassed ? "Connection OK" : "Test Connection"}
            </motion.button>
          </div>

          {/* Footer */}
          <div className="flex gap-3 p-6 border-t dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50">
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors font-medium"
            >
              Cancel
            </button>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={handleSave}
              disabled={!email || !apiKey || !testPassed}
              className="flex-1 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Save
            </motion.button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}

interface IntegrationCardProps {
  integration: Integration
  onConnect: (integration: Integration) => void
}

function IntegrationCard({ integration, onConnect }: IntegrationCardProps) {
  return (
    <motion.div
      whileHover={{ y: -4, boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.15)" }}
      className={cn(
        "relative p-4 rounded-lg border transition-all cursor-pointer",
        integration.connected
          ? "bg-green-50 dark:bg-green-900/10 border-green-200 dark:border-green-800"
          : "bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600"
      )}
      onClick={() => !integration.connected && onConnect(integration)}
    >
      {/* Header with logo */}
      <div className="flex items-start justify-between mb-3">
        <div
          className="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0"
          style={{ background: integration.color + "15" }}
        >
          <integration.logo size={24} style={{ color: integration.color }} />
        </div>
        {integration.connected && (
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: "spring", damping: 15 }}
          >
            <CheckCircle2 size={20} className="text-green-600 dark:text-green-400" />
          </motion.div>
        )}
      </div>

      {/* Content */}
      <h3 className="font-semibold text-slate-900 dark:text-white mb-1">
        {integration.name}
      </h3>
      <p className="text-xs text-slate-600 dark:text-slate-400 mb-3">
        {integration.description}
      </p>

      {/* Actions preview */}
      <div className="flex flex-wrap gap-1 mb-4">
        {integration.actions.slice(0, 2).map((action) => (
          <span
            key={action}
            className="px-2 py-0.5 text-xs rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400"
          >
            {action}
          </span>
        ))}
        {integration.actions.length > 2 && (
          <span className="px-2 py-0.5 text-xs rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
            +{integration.actions.length - 2}
          </span>
        )}
      </div>

      {/* Button */}
      <motion.button
        whileHover={{ scale: 1.02 }}
        whileTap={{ scale: 0.98 }}
        onClick={(e) => {
          e.stopPropagation()
          onConnect(integration)
        }}
        className={cn(
          "w-full py-2 rounded-lg font-medium text-sm transition-all",
          integration.connected
            ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 cursor-default"
            : "bg-blue-600 hover:bg-blue-700 text-white"
        )}
      >
        {integration.connected ? "Connected" : "Connect"}
      </motion.button>
    </motion.div>
  )
}

const CATEGORY_FILTERS = [
  "All",
  "Messaging",
  "CRM",
  "Productivity",
  "Analytics",
  "E-commerce",
] as const

export default function IntegrationsPage() {
  const [search, setSearch] = useState("")
  const [filter, setFilter] = useState<typeof CATEGORY_FILTERS[number]>("All")
  const [selectedIntegration, setSelectedIntegration] = useState<Integration | null>(null)

  const filtered = INTEGRATIONS.filter((int) => {
    const matchesSearch = int.name
      .toLowerCase()
      .includes(search.toLowerCase())
    const matchesFilter = filter === "All" || int.category === filter
    return matchesSearch && matchesFilter
  })

  const handleConnect = useCallback((integration: Integration) => {
    if (!integration.connected) {
      setSelectedIntegration(integration)
    }
  }, [])

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="h-full overflow-auto bg-white dark:bg-slate-950"
    >
      <div className="max-w-7xl mx-auto p-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white mb-2">
            Integrations
          </h1>
          <p className="text-slate-600 dark:text-slate-400">
            Connect your favorite tools and services to automate workflows
          </p>
        </div>

        {/* Search and Filter */}
        <div className="mb-8 space-y-4">
          {/* Search */}
          <div className="relative">
            <Search
              size={18}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search integrations..."
              className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Filter pills */}
          <div className="flex gap-2 overflow-x-auto pb-2">
            {CATEGORY_FILTERS.map((cat) => (
              <motion.button
                key={cat}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setFilter(cat)}
                className={cn(
                  "px-4 py-1.5 rounded-full font-medium text-sm whitespace-nowrap transition-all",
                  filter === cat
                    ? "bg-blue-600 text-white"
                    : "bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700"
                )}
              >
                {cat}
              </motion.button>
            ))}
          </div>
        </div>

        {/* Grid */}
        {filtered.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filtered.map((integration) => (
              <IntegrationCard
                key={integration.id}
                integration={integration}
                onConnect={handleConnect}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-slate-500 dark:text-slate-400">
              No integrations found for "{search}"
            </p>
          </div>
        )}
      </div>

      {/* Auth sheet */}
      <AuthSheet
        integration={selectedIntegration}
        onClose={() => setSelectedIntegration(null)}
      />
    </motion.div>
  )
}
