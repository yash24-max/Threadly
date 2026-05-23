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
  type NodeChange,
  type EdgeChange,
  BackgroundVariant,
  ReactFlowProvider,
} from "@xyflow/react"
import "@xyflow/react/dist/style.css"
import { useCallback, useRef, useState, useEffect } from "react"
import { nodeTypes } from "./nodes/NodeTypes"
import { NodePanel } from "./NodePanel"
import { PropertiesPanel } from "./PropertiesPanel"
import type { FlowDefinition } from "@/lib/types"

interface Props {
  initialDefinition?: FlowDefinition
  onChange: (def: FlowDefinition) => void
}

/** Fields required per node type. Missing required fields = validation error. */
const REQUIRED_FIELDS: Record<string, string[]> = {
  message: ["content"],
  question: ["content"],
  ai_reply: ["system_prompt"],
  api_call: ["url"],
  condition: ["variable"],
  set_variable: ["assignments"],
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

  const [nodes, setNodes, onNodesChange] = useNodesState(
    initialDefinition?.nodes?.length ? (initialDefinition.nodes as Node[]) : defaultNodes
  )
  const [edges, setEdges, onEdgesChange] = useEdgesState(
    initialDefinition?.edges ?? []
  )

  // Undo/redo history
  const history = useRef<{ nodes: Node[]; edges: Edge[] }[]>([])
  const historyIndex = useRef(-1)
  const skipHistory = useRef(false)

  function pushHistory(ns: Node[], es: Edge[]) {
    if (skipHistory.current) return
    // Trim forward history
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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    setEdges(snap.edges as any)
    skipHistory.current = false
  }

  function redo() {
    if (historyIndex.current >= history.current.length - 1) return
    historyIndex.current += 1
    const snap = history.current[historyIndex.current]
    skipHistory.current = true
    setNodes(snap.nodes as Node[])
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    setEdges(snap.edges as any)
    skipHistory.current = false
  }

  // Notify parent + push history when flow changes
  useEffect(() => {
    onChange({ nodes: nodes as any, edges: edges as any })
    pushHistory(nodes, edges)
  }, [nodes, edges]) // eslint-disable-line

  // Keyboard shortcuts
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement).tagName
      if (tag === "INPUT" || tag === "TEXTAREA") return

      // Undo: Cmd+Z
      if ((e.metaKey || e.ctrlKey) && !e.shiftKey && e.key === "z") { e.preventDefault(); undo(); return }
      // Redo: Cmd+Shift+Z or Cmd+Y
      if (((e.metaKey || e.ctrlKey) && e.shiftKey && e.key === "z") ||
          ((e.metaKey || e.ctrlKey) && e.key === "y")) { e.preventDefault(); redo(); return }

      // Delete selected node: Backspace / Delete
      if (e.key === "Delete" || e.key === "Backspace") {
        setNodes((nds) => nds.filter((n) => !n.selected))
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        setEdges((eds) => eds.filter((eg) => !(eg as any).selected))
      }

      // N = new message node at centre
      if (e.key === "n" && !e.metaKey && !e.ctrlKey) {
        const pos = reactFlowInstance?.getViewport()
        setNodes((nds) => [
          ...nds,
          {
            id: newId("message"),
            type: "message",
            position: { x: (pos?.x ?? 300) + 100, y: (pos?.y ?? 200) + 100 },
            data: {},
          },
        ])
      }
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [reactFlowInstance]) // eslint-disable-line

  const onConnect = useCallback(
    (params: Connection) =>
      setEdges((eds) =>
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        addEdge({ ...params, animated: false, style: { stroke: "var(--border-strong)" } } as any, eds)
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
      const position = reactFlowInstance.screenToFlowPosition({ x: e.clientX, y: e.clientY })
      // Grab defaultData that NodePanel serialised in the drag event
      let defaultData: Record<string, unknown> = {}
      try {
        const raw = e.dataTransfer.getData("application/reactflow-data")
        if (raw) defaultData = JSON.parse(raw)
      } catch {}
      setNodes((nds) => [...nds, { id: newId(type), type, position, data: defaultData }])
    },
    [reactFlowInstance, setNodes]
  )

  // Click-to-add: called by NodePanel when user clicks a node in the catalog
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

  // Validation badges
  const validationErrors = validateNodes(nodes)
  const nodesWithBadges = nodes.map((n) => ({
    ...n,
    data: {
      ...n.data,
      _hasError: !!validationErrors[n.id],
      _errorFields: validationErrors[n.id],
    },
  }))

  return (
    <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
      <NodePanel onAddNode={handleAddNode} />
      <div ref={reactFlowWrapper} style={{ flex: 1 }}>
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
          style={{ background: "var(--bg-canvas)" }}
          defaultEdgeOptions={{
            style: { stroke: "var(--border-strong)", strokeWidth: 1.5 },
          }}
        >
          <Background variant={BackgroundVariant.Dots} gap={16} size={1} color="var(--border)" />
          <Controls
            style={{
              background: "var(--bg-panel)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)",
            }}
          />
          <MiniMap
            style={{
              background: "var(--bg-panel)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-md)",
            }}
            nodeColor="var(--accent)"
          />
        </ReactFlow>
      </div>
      <PropertiesPanel selectedNode={selectedNode} onUpdateNode={handleNodeDataChange} />
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
