"use client"

import { useState, useCallback } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Search, X, Copy, Eye } from "lucide-react"
import { cn } from "@/lib/utils"
import { TEMPLATES, TEMPLATE_CATEGORIES, type Template } from "@/lib/templates"

interface TemplatePreviewProps {
  template: Template | null
  onClose: () => void
}

function TemplatePreview({ template, onClose }: TemplatePreviewProps) {
  if (!template) return null

  const nodeCount = template.definition.nodes.length
  const edgeCount = template.definition.edges.length

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      >
        <motion.div
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          onClick={(e) => e.stopPropagation()}
          className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-lg shadow-2xl overflow-hidden"
        >
          {/* Header */}
          <div className="flex items-start justify-between p-6 border-b dark:border-slate-700 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-slate-800 dark:to-slate-900">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <span className="text-4xl">{template.avatar}</span>
                <div>
                  <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
                    {template.name}
                  </h2>
                  <p className="text-sm text-slate-600 dark:text-slate-400">
                    {template.description}
                  </p>
                </div>
              </div>
              <div className="flex flex-wrap gap-2 mt-3">
                <span className="px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300">
                  {template.category}
                </span>
                <span className="px-2 py-1 text-xs rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                  {nodeCount} nodes
                </span>
                <span className="px-2 py-1 text-xs rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                  {edgeCount} connections
                </span>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1 hover:bg-slate-200 dark:hover:bg-slate-700 rounded transition-colors"
            >
              <X size={24} className="text-slate-500" />
            </button>
          </div>

          {/* Preview visualization */}
          <div className="p-6">
            <div className="bg-slate-50 dark:bg-slate-800 rounded-lg p-4 mb-6 overflow-x-auto">
              <div className="flex flex-wrap gap-4 justify-center items-center">
                {template.definition.nodes.map((node, idx) => (
                  <motion.div
                    key={node.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                    className="flex items-center gap-2"
                  >
                    <div className="px-3 py-2 rounded-lg bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 text-xs font-medium text-slate-700 dark:text-slate-300 whitespace-nowrap">
                      {node.type === "start" && "Start"}
                      {node.type === "message" && "Message"}
                      {node.type === "question" && "Question"}
                      {node.type === "ai_reply" && "AI"}
                      {node.type === "api_call" && "API"}
                      {node.type === "condition" && "Branch"}
                      {node.type === "handoff" && "Handoff"}
                      {node.type === "end" && "End"}
                      {node.type === "hubspot" && "HubSpot"}
                      {node.type === "webhook_trigger" && "Webhook"}
                      {node.type === "classify_intent" && "Classify"}
                      {!["start", "message", "question", "ai_reply", "api_call", "condition", "handoff", "end", "hubspot", "webhook_trigger", "classify_intent"].includes(node.type) &&
                        node.type.replace(/_/g, " ")}
                    </div>
                    {idx < template.definition.nodes.length - 1 && (
                      <div className="text-slate-400">→</div>
                    )}
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Flow summary */}
            <div className="grid grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-blue-50 dark:bg-blue-900/20">
                <p className="text-xs text-slate-600 dark:text-slate-400 mb-1">Nodes</p>
                <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                  {nodeCount}
                </p>
              </div>
              <div className="p-4 rounded-lg bg-indigo-50 dark:bg-indigo-900/20">
                <p className="text-xs text-slate-600 dark:text-slate-400 mb-1">Connections</p>
                <p className="text-2xl font-bold text-indigo-600 dark:text-indigo-400">
                  {edgeCount}
                </p>
              </div>
              <div className="p-4 rounded-lg bg-purple-50 dark:bg-purple-900/20">
                <p className="text-xs text-slate-600 dark:text-slate-400 mb-1">Complexity</p>
                <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                  {nodeCount < 5 ? "Low" : nodeCount < 10 ? "Medium" : "High"}
                </p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="flex gap-3 p-6 border-t dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50">
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors font-medium"
            >
              Close
            </button>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex-1 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium transition-colors flex items-center justify-center gap-2"
            >
              <Copy size={16} />
              Use Template
            </motion.button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}

interface TemplateCardProps {
  template: Template
  onUse: (template: Template) => void
  onPreview: (template: Template) => void
}

function TemplateCard({ template, onUse, onPreview }: TemplateCardProps) {
  return (
    <motion.div
      whileHover={{ y: -4 }}
      className="group flex flex-col bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm hover:shadow-md transition-all"
    >
      {/* Preview area */}
      <div className="relative h-40 bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-800 dark:to-slate-900 flex items-center justify-center overflow-hidden">
        <div className="text-5xl filter drop-shadow-sm">{template.avatar}</div>
        <div className="absolute inset-0 bg-gradient-to-t from-black/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
      </div>

      {/* Content */}
      <div className="flex-1 flex flex-col p-4">
        <h3 className="font-bold text-slate-900 dark:text-white mb-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
          {template.name}
        </h3>
        <p className="text-xs text-slate-600 dark:text-slate-400 mb-3 flex-1 line-clamp-2">
          {template.description}
        </p>

        {/* Badges */}
        <div className="flex flex-wrap gap-1.5 mb-3">
          <span className="px-2 py-0.5 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300">
            {template.category}
          </span>
          <span className="px-2 py-0.5 text-xs rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
            {template.nodeCount} nodes
          </span>
        </div>

        {/* Buttons */}
        <div className="flex gap-2">
          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => onPreview(template)}
            className="flex-1 px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm font-medium flex items-center justify-center gap-1"
          >
            <Eye size={14} />
            Preview
          </motion.button>
          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => onUse(template)}
            className="flex-1 px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white transition-colors text-sm font-medium flex items-center justify-center gap-1"
          >
            <Copy size={14} />
            Use
          </motion.button>
        </div>
      </div>
    </motion.div>
  )
}

export default function TemplatesPage() {
  const [search, setSearch] = useState("")
  const [filter, setFilter] = useState<typeof TEMPLATE_CATEGORIES[number]>("All")
  const [previewTemplate, setPreviewTemplate] = useState<Template | null>(null)
  const [usedTemplate, setUsedTemplate] = useState<Template | null>(null)

  const filtered = TEMPLATES.filter((template) => {
    const matchesSearch = (
      template.name.toLowerCase().includes(search.toLowerCase()) ||
      template.description.toLowerCase().includes(search.toLowerCase())
    )
    const matchesFilter = filter === "All" || template.category === filter
    return matchesSearch && matchesFilter
  })

  const handleUse = useCallback((template: Template) => {
    setUsedTemplate(template)
    setTimeout(() => {
      setUsedTemplate(null)
    }, 2000)
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
            Template Gallery
          </h1>
          <p className="text-slate-600 dark:text-slate-400">
            Choose a pre-built workflow and customize it for your needs
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
              placeholder="Search templates..."
              className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Filter pills */}
          <div className="flex gap-2 overflow-x-auto pb-2">
            {TEMPLATE_CATEGORIES.map((cat) => (
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
            {filtered.map((template, idx) => (
              <motion.div
                key={template.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.05 }}
              >
                <TemplateCard
                  template={template}
                  onUse={handleUse}
                  onPreview={setPreviewTemplate}
                />
              </motion.div>
            ))}
          </div>
        ) : (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-center py-16"
          >
            <p className="text-slate-500 dark:text-slate-400 mb-2">
              No templates found for "{search}"
            </p>
            <p className="text-sm text-slate-400 dark:text-slate-500">
              Try adjusting your search or category filter
            </p>
          </motion.div>
        )}
      </div>

      {/* Preview Modal */}
      <TemplatePreview
        template={previewTemplate}
        onClose={() => setPreviewTemplate(null)}
      />

      {/* Toast notification */}
      <AnimatePresence>
        {usedTemplate && (
          <motion.div
            initial={{ opacity: 0, y: 20, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.95 }}
            className="fixed bottom-8 right-8 bg-green-600 text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-2 z-40"
          >
            <div className="w-2 h-2 bg-green-200 rounded-full" />
            Template "{usedTemplate.name}" copied to your builder!
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  )
}
