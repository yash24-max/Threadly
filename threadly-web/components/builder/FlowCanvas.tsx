"use client"

import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState,
  type Connection,
  type Node,
  type Edge,
  BackgroundVariant,
  ReactFlowProvider,
  Panel,
} from "@xyflow/react"
import "@xyflow/react/dist/style.css"
import { useCallback, useRef, useState, useEffect } from "react"
import { motion } from "framer-motion"
import { nodeTypes } from "./nodes/NodeTypes"
import { NodePanel } from "./NodePanel"
import { PropertiesPanel } from "./PropertiesPanel"
import type { FlowDefinition } from "@/lib/types"
import {
  ZoomIn,
  ZoomOut,
  Maximize2,
  Play,
  Search,
  AlertCircle,
} from "lucide-react"

interface Props {
  initialDefinition?: FlowDefinition
  onChange: (def: FlowDefinition) => void
}

const REQUIRED_FIELDS: Record<string, string[]> = {
  message: ["content"],
  question: ["content"],
  ai_reply: ["systemPrompt"],
  api_call: ["url"],
  condition: ["variable"],
  set_variable: ["assignments"],
  send_email: ["to", "subject"],
}

export function validateNodes(nodes: Node[]): Record<string, string[]> {
  const errors: Record<string, string[]> = {}
  for (const node of nodes) {
    const required = REQUIRED_FIELDS[node.type ?? ""] ?? []
    const missing = required.filter((f) => {
      const val = (node.data as Record<string, unknown>)[f]
      return val === undefined || val === null || val === ""
    })
    if (missing.length > 0) errors[node.id] = missing
  }
  return errors
}

const defaultNodes: Node[] = [
  { id: "start_1", type: "start", position: { x: 300, y: 60 }, data: {} },
]

let idCounter = 100
const newId = (type: string) => `${type}_${++idCounter}`

const MAX_HISTORY = 100

function Canvas({ initialDefinition, onChange }: Props) {
  const reactFlowWrapper = useRef<HTMLDivElement>(null)
  const [reactFlowInstance, setReactFlowInstance] = useState<any>(null)
  const [selectedNode, setSelectedNode] = useState<Node | null>(null)
  const [searchOpen, setSearchOpen] = useState(false)
  const [nodeTestMode, setNodeTestMode] = useState<string | null>(null)
  const [zoom, setZoom] = useState(1)

  const [nodes, setNodes, onNodesChange] = useNodesState(
    initialDefinition?.nodes?.length ? (initialDefinition.nodes as Node[]) : defaultNodes
  )
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialDefinition?.edges ?? [])

  // Undo/redo history
  const history = useRef<{ nodes: Node[]; edges: Edge[] }[]>([])
  const historyIndex = useRef(-1)
  const skipHistory = useRef(false)

  function pushHistory(ns: Node[], es: Edge[]) {
    if (skipHistory.current) return
    history.current = history.current.slice(0, historyIndex.current + 1)
    history.current.push({ nodes: ns, edges: es })
    if (history.current.length > MAX_HISTORY) history.current.shift()
    historyIndex.current = history.current.length - 1
  }

  function undo() {
    if (historyIndex.current <= 0) return
    historyIndex.current -= 1
    const snap = history.current[historyIndex.current]
    skipHistory.current = true
    setNodes(snap.nodes as Node[])
    setEdges(snap.edges as any)
    skipHistory.current = false
  }

  function redo() {
    if (historyIndex.current >= history.current.length - 1) return
    historyIndex.current += 1
    const snap = history.current[historyIndex.current]
    skipHistory.current = true
    setNodes(snap.nodes as Node[])
    setEdges(snap.edges as any)
    skipHistory.current = false
  }

  useEffect(() => {
    onChange({ nodes: nodes as any, edges: edges as any })
    pushHistory(nodes, edges)
  }, [nodes, edges])

  // Keyboard shortcuts
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement).tagName
      if (tag === "INPUT" || tag === "TEXTAREA") return

      if ((e.metaKey || e.ctrlKey) && !e.shiftKey && e.key === "z") {
        e.preventDefault()
        undo()
        return
      }
      if (
        ((e.metaKey || e.ctrlKey) && e.shiftKey && e.key === "z") ||
        ((e.metaKey || e.ctrlKey) && e.key === "y")
      ) {
        e.preventDefault()
        redo()
        return
      }

      if (e.key === "Delete" || e.key === "Backspace") {
        e.preventDefault()
        setNodes((nds) => nds.filter((n) => !n.selected))
        setEdges((eds) => eds.filter((eg) => !(eg as any).selected))
      }

      if ((e.metaKey || e.ctrlKey) && e.key === "d") {
        e.preventDefault()
        const selected = nodes.filter((n) => n.selected)
        if (selected.length === 0) return
        setNodes((nds) => [
          ...nds,
          ...selected.map((n) => ({
            ...n,
            id: newId(n.type ?? "node"),
            position: { x: n.position.x + 30, y: n.position.y + 30 },
          })),
        ])
      }

      if ((e.metaKey || e.ctrlKey) && e.key === "a") {
        e.preventDefault()
        setNodes((nds) => nds.map((n) => ({ ...n, selected: true })))
      }

      if ((e.metaKey || e.ctrlKey) && e.key === "f") {
        e.preventDefault()
        setSearchOpen((o) => !o)
      }
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [nodes, setNodes, setEdges])

  const onConnect = useCallback(
    (params: Connection) =>
      setEdges((eds) =>
        addEdge(
          {
            ...params,
            animated: false,
            style: { stroke: "#64748b", strokeWidth: 2 },
          } as any,
          eds
        )
      ),
    [setEdges]
  )

  const onDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = "move"
  }, [])

  const onDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault()
      const type = e.dataTransfer.getData("application/reactflow")
      if (!type || !reactFlowInstance) return
      const position = reactFlowInstance.screenToFlowPosition({
        x: e.clientX,
        y: e.clientY,
      })
      let defaultData: Record<string, unknown> = {}
      try {
        const raw = e.dataTransfer.getData("application/reactflow-data")
        if (raw) defaultData = JSON.parse(raw)
      } catch {}
      setNodes((nds) => [...nds, { id: newId(type), type, position, data: defaultData }])
    },
    [reactFlowInstance, setNodes]
  )

  const handleAddNode = useCallback(
    (type: string, defaultData: Record<string, unknown>) => {
      const viewport = reactFlowInstance?.getViewport()
      const x = viewport ? -viewport.x / viewport.zoom + 300 : 300
      const y = viewport ? -viewport.y / viewport.zoom + 200 : 200
      setNodes((nds) => [...nds, { id: newId(type), type, position: { x, y }, data: defaultData }])
    },
    [reactFlowInstance, setNodes]
  )

  const onNodeClick = useCallback((_: React.MouseEvent, node: Node) => {
    setSelectedNode(node)
  }, [])

  const onPaneClick = useCallback(() => {
    setSelectedNode(null)
  }, [])

  const handleNodeDataChange = useCallback(
    (id: string, data: Record<string, unknown>) => {
      setNodes((nds) => nds.map((n) => (n.id === id ? { ...n, data } : n)))
      setSelectedNode((prev) => (prev?.id === id ? { ...prev, data } : prev))
    },
    [setNodes]
  )

  const handleTestNode = useCallback(
    (nodeId: string) => {
      setNodeTestMode(nodeId)
      setTimeout(() => setNodeTestMode(null), 1500)
    },
    []
  )

  const handleFitView = useCallback(() => {
    reactFlowInstance?.fitView()
  }, [reactFlowInstance])

  const validationErrors = validateNodes(nodes)
  const nodesWithBadges = nodes.map((n) => ({
    ...n,
    data: {
      ...n.data,
      _hasError: !!validationErrors[n.id],
      _errorFields: validationErrors[n.id],
    },
  }))

  const errorCount = Object.keys(validationErrors).length

  return (
    <div className="flex flex-1 overflow-hidden bg-slate-950">
      <NodePanel onAddNode={handleAddNode} />
      <div ref={reactFlowWrapper} className="flex-1 relative">
        <ReactFlow
          nodes={nodesWithBadges}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onInit={setReactFlowInstance}
          onDrop={onDrop}
          onDragOver={onDragOver}
          onNodeClick={onNodeClick}
          onPaneClick={onPaneClick}
          nodeTypes={nodeTypes}
          fitView
          snapToGrid
          snapGrid={[16, 16]}
          style={{ background: "#0f172a" }}
          defaultEdgeOptions={{
            style: { stroke: "#64748b", strokeWidth: 2 },
          }}
        >
          <Background
            variant={BackgroundVariant.Dots}
            gap={16}
            size={1}
            color="#334155"
            style={{ opacity: 0.4 }}
          />

          {/* Top toolbar */}
          <Panel position="top-left" className="flex items-center gap-2 bg-slate-900/80 backdrop-blur border border-slate-700 rounded-lg p-2">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => reactFlowInstance?.zoomIn()}
              className="p-1.5 hover:bg-slate-800 rounded transition-colors"
              title="Zoom in (Ctrl++)"
            >
              <ZoomIn size={16} className="text-slate-300" />
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => reactFlowInstance?.zoomOut()}
              className="p-1.5 hover:bg-slate-800 rounded transition-colors"
              title="Zoom out (Ctrl+-)"
            >
              <ZoomOut size={16} className="text-slate-300" />
            </motion.button>
            <div className="w-10 text-center text-xs text-slate-400 font-mono">
              {Math.round(zoom * 100)}%
            </div>
            <div className="w-px h-4 bg-slate-700" />
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={handleFitView}
              className="p-1.5 hover:bg-slate-800 rounded transition-colors"
              title="Fit to screen"
            >
              <Maximize2 size={16} className="text-slate-300" />
            </motion.button>
          </Panel>

          {/* Run/Test toolbar */}
          <Panel position="top-center" className="flex items-center gap-3 bg-slate-900/80 backdrop-blur border border-slate-700 rounded-lg p-3">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => handleTestNode("all")}
              className="flex items-center gap-2 px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white rounded text-sm font-medium transition-colors"
            >
              <Play size={14} />
              Run Flow
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setSearchOpen(!searchOpen)}
              className="p-1.5 hover:bg-slate-800 rounded transition-colors"
              title="Search nodes (Ctrl+F)"
            >
              <Search size={16} className="text-slate-300" />
            </motion.button>
          </Panel>

          {/* Validation panel */}
          {errorCount > 0 && (
            <Panel position="bottom-left" className="bg-red-900/80 backdrop-blur border border-red-700 rounded-lg p-3 max-w-xs">
              <div className="flex items-start gap-2">
                <AlertCircle size={16} className="text-red-200 flex-shrink-0 mt-0.5" />
                <div className="text-xs text-red-100">
                  <p className="font-semibold">{errorCount} validation error(s)</p>
                  <p className="mt-1 opacity-80">
                    {errorCount === 1
                      ? "1 node is missing required fields"
                      : `${errorCount} nodes are missing required fields`}
                  </p>
                </div>
              </div>
            </Panel>
          )}

          {/* Mini-map */}
          <MiniMap
            style={{
              background: "#1e293b",
              border: "1px solid #475569",
              borderRadius: "0.5rem",
            }}
            maskColor="rgba(15, 23, 42, 0.7)"
            nodeColor={(node: any) => {
              const colors: Record<string, string> = {
                message: "#3B82F6",
                question: "#3B82F6",
                collect_input: "#3B82F6",
                send_email: "#3B82F6",
                condition: "#8B5CF6",
                switch: "#8B5CF6",
                set_variable: "#8B5CF6",
                delay: "#8B5CF6",
                loop: "#8B5CF6",
                subflow: "#8B5CF6",
                ai_reply: "#10B981",
                classify_intent: "#10B981",
                extract_entities: "#10B981",
                sentiment_analysis: "#10B981",
                api_call: "#F59E0B",
                slack: "#F59E0B",
                hubspot: "#F59E0B",
                google_sheets: "#F59E0B",
                twilio: "#F59E0B",
                notion: "#F59E0B",
                handoff: "#EF4444",
                end: "#EF4444",
                cron_trigger: "#EF4444",
                webhook_trigger: "#EF4444",
                error_branch: "#EF4444",
              }
              return colors[node.type] || "#64748b"
            }}
          />
        </ReactFlow>
      </div>
      <PropertiesPanel
        selectedNode={selectedNode}
        onUpdateNode={handleNodeDataChange}
        onTestNode={handleTestNode}
      />
    </div>
  )
}

export function FlowCanvas(props: Props) {
  return (
    <ReactFlowProvider>
      <Canvas {...props} />
    </ReactFlowProvider>
  )
}
