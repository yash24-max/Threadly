"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Check, Copy, ExternalLink, Upload, X } from "lucide-react";
import { toast } from "sonner";
import { useSession } from "next-auth/react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";

const ONBOARDING_KEY = "tly_onboarding_done";
const TOTAL_STEPS = 5;

// ── Templates ─────────────────────────────────────────────────────────────────

const TEMPLATES = [
  {
    id: "customer_support",
    name: "Customer Support",
    description: "Handle FAQs, collect issue details, escalate to agents",
    nodeCount: 8,
    definition: {
      nodes: [
        { id: "start_1", type: "start", position: { x: 300, y: 60 }, data: {} },
        { id: "msg_1", type: "message", position: { x: 300, y: 180 }, data: { content: "Hi! I'm your support assistant. How can I help you today?" } },
        { id: "ai_1", type: "ai_reply", position: { x: 300, y: 300 }, data: { systemPrompt: "You are a helpful customer support agent. Answer questions concisely.", maxTokens: 512, temperature: 0.7, useKnowledgeBase: true } },
        { id: "end_1", type: "end", position: { x: 300, y: 460 }, data: { message: "Thanks for reaching out! Have a great day." } },
      ],
      edges: [
        { id: "e1", source: "start_1", target: "msg_1" },
        { id: "e2", source: "msg_1", target: "ai_1" },
        { id: "e3", source: "ai_1", target: "end_1" },
      ],
    },
  },
  {
    id: "lead_capture",
    name: "Lead Capture",
    description: "Collect visitor name, email, and qualification info",
    nodeCount: 6,
    definition: {
      nodes: [
        { id: "start_1", type: "start", position: { x: 300, y: 60 }, data: {} },
        { id: "msg_1", type: "message", position: { x: 300, y: 180 }, data: { content: "Welcome! I'd love to learn more about how we can help you." } },
        { id: "q_1", type: "question", position: { x: 300, y: 300 }, data: { content: "What's your name?", variable: "lead_name" } },
        { id: "q_2", type: "question", position: { x: 300, y: 420 }, data: { content: "And your email?", variable: "lead_email" } },
        { id: "msg_2", type: "message", position: { x: 300, y: 540 }, data: { content: "Thanks {{lead_name}}! Our team will reach out to {{lead_email}} shortly." } },
        { id: "end_1", type: "end", position: { x: 300, y: 660 }, data: {} },
      ],
      edges: [
        { id: "e1", source: "start_1", target: "msg_1" },
        { id: "e2", source: "msg_1", target: "q_1" },
        { id: "e3", source: "q_1", target: "q_2" },
        { id: "e4", source: "q_2", target: "msg_2" },
        { id: "e5", source: "msg_2", target: "end_1" },
      ],
    },
  },
  {
    id: "faq_bot",
    name: "FAQ Bot",
    description: "Answer common questions from your knowledge base",
    nodeCount: 4,
    definition: {
      nodes: [
        { id: "start_1", type: "start", position: { x: 300, y: 60 }, data: {} },
        { id: "msg_1", type: "message", position: { x: 300, y: 180 }, data: { content: "Hello! Ask me anything about our products and services." } },
        { id: "ai_1", type: "ai_reply", position: { x: 300, y: 300 }, data: { systemPrompt: "You are a knowledgeable assistant. Answer only from the knowledge base. If you don't know, say so politely.", maxTokens: 1024, temperature: 0.3, useKnowledgeBase: true } },
        { id: "end_1", type: "end", position: { x: 300, y: 460 }, data: { message: "Hope that helped! Feel free to ask more questions." } },
      ],
      edges: [
        { id: "e1", source: "start_1", target: "msg_1" },
        { id: "e2", source: "msg_1", target: "ai_1" },
        { id: "e3", source: "ai_1", target: "end_1" },
      ],
    },
  },
] as const;

const BOT_COLORS = [
  { name: "Indigo", hex: "#6366F1" },
  { name: "Blue", hex: "#3B82F6" },
  { name: "Green", hex: "#10B981" },
  { name: "Pink", hex: "#EC4899" },
  { name: "Orange", hex: "#F59E0B" },
  { name: "Purple", hex: "#8B5CF6" },
] as const;

const BOT_EMOJIS = ["🤖", "🧠", "💬", "✨", "🎯", "🚀", "💡", "🌟", "🦾", "🤝", "⚡", "🔮"] as const;

// ── Step components ───────────────────────────────────────────────────────────

function StepWelcome({ onNext }: { onNext: () => void }) {
  return (
    <div className="flex flex-col items-center text-center py-8 px-4">
      <div className="w-20 h-20 rounded-2xl bg-[var(--accent)] flex items-center justify-center text-4xl mb-6 shadow-lg">
        ✨
      </div>
      <h2 className="text-2xl font-bold text-[var(--text-primary)] mb-3">
        Welcome to Threadly
      </h2>
      <p className="text-[15px] text-[var(--text-muted)] max-w-sm leading-relaxed mb-2">
        Build powerful AI chatbots with a visual flow builder. No code required.
      </p>
      <p className="text-[13px] text-[var(--text-muted)] mb-8">
        Let&apos;s set up your first bot in about 3 minutes.
      </p>
      <button
        type="button"
        onClick={onNext}
        className="px-8 py-3 rounded-xl bg-[var(--accent)] text-white font-semibold text-[15px] hover:opacity-90 transition-opacity"
      >
        Get started →
      </button>
    </div>
  );
}

interface BotConfig {
  name: string;
  color: string;
  emoji: string;
}

function StepNameBot({
  config,
  setConfig,
  onNext,
  onBack,
}: {
  config: BotConfig;
  setConfig: (c: BotConfig) => void;
  onNext: () => void;
  onBack: () => void;
}) {
  return (
    <div className="space-y-6">
      <div>
        <label
          htmlFor="bot-name"
          className="block text-[12px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2"
        >
          Bot Name
        </label>
        <input
          id="bot-name"
          type="text"
          value={config.name}
          onChange={(e) => setConfig({ ...config, name: e.target.value })}
          placeholder="My Support Bot"
          autoFocus
          maxLength={60}
          className={cn(
            "w-full px-4 py-3 text-[15px] rounded-xl",
            "bg-[var(--bg-surface)] border border-[var(--border)]",
            "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
            "outline-none focus:border-[var(--accent)] transition-colors"
          )}
        />
      </div>

      <div>
        <p className="text-[12px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
          Accent Color
        </p>
        <div className="flex gap-2.5">
          {BOT_COLORS.map((c) => (
            <button
              key={c.hex}
              type="button"
              onClick={() => setConfig({ ...config, color: c.hex })}
              className={cn(
                "w-8 h-8 rounded-full transition-all",
                config.color === c.hex && "ring-2 ring-offset-2 ring-[var(--border-strong)] scale-110"
              )}
              style={{ background: c.hex }}
              aria-label={`Select ${c.name} color`}
              title={c.name}
            />
          ))}
        </div>
      </div>

      <div>
        <p className="text-[12px] font-semibold uppercase tracking-wide text-[var(--text-muted)] mb-2">
          Avatar Emoji
        </p>
        <div className="grid grid-cols-6 gap-2">
          {BOT_EMOJIS.map((emoji) => (
            <button
              key={emoji}
              type="button"
              onClick={() => setConfig({ ...config, emoji })}
              className={cn(
                "w-10 h-10 rounded-xl text-xl flex items-center justify-center transition-all",
                "bg-[var(--bg-surface)] border border-[var(--border)]",
                "hover:border-[var(--accent)]",
                config.emoji === emoji && "border-[var(--accent)] bg-[var(--accent)]/10 scale-110"
              )}
              aria-label={`Select ${emoji} emoji`}
            >
              {emoji}
            </button>
          ))}
        </div>
      </div>

      {/* Preview */}
      {config.name && (
        <div
          className="flex items-center gap-3 p-4 rounded-xl border border-[var(--border)]"
          style={{ borderLeftColor: config.color, borderLeftWidth: 3 }}
        >
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center text-xl"
            style={{ background: config.color + "20" }}
          >
            {config.emoji}
          </div>
          <p className="font-semibold text-[var(--text-primary)]">{config.name}</p>
        </div>
      )}

      <div className="flex gap-3 pt-2">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-[var(--text-secondary)] text-[14px] font-medium hover:border-[var(--border-strong)] transition-colors"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onNext}
          disabled={!config.name.trim()}
          className="flex-1 py-2.5 rounded-xl bg-[var(--accent)] text-white text-[14px] font-semibold disabled:opacity-40 hover:opacity-90 transition-opacity"
        >
          Next →
        </button>
      </div>
    </div>
  );
}

function StepChooseTemplate({
  selectedTemplate,
  setSelectedTemplate,
  onNext,
  onBack,
}: {
  selectedTemplate: string | null;
  setSelectedTemplate: (id: string) => void;
  onNext: () => void;
  onBack: () => void;
}) {
  return (
    <div className="space-y-4">
      <p className="text-[13px] text-[var(--text-muted)]">
        Choose a starting template or start from scratch
      </p>
      <div className="space-y-3">
        {TEMPLATES.map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => setSelectedTemplate(t.id)}
            className={cn(
              "w-full text-left p-4 rounded-xl border-2 transition-all",
              selectedTemplate === t.id
                ? "border-[var(--accent)] bg-[var(--accent)]/5"
                : "border-[var(--border)] hover:border-[var(--border-strong)] bg-[var(--bg-surface)]"
            )}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="text-[14px] font-semibold text-[var(--text-primary)]">
                  {t.name}
                </p>
                <p className="text-[12px] text-[var(--text-muted)] mt-0.5">
                  {t.description}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[11px] text-[var(--text-muted)] bg-[var(--bg-surface)] border border-[var(--border)] px-2 py-0.5 rounded-full">
                  {t.nodeCount} nodes
                </span>
                {selectedTemplate === t.id && (
                  <div className="w-5 h-5 rounded-full bg-[var(--accent)] flex items-center justify-center">
                    <Check size={11} className="text-white" />
                  </div>
                )}
              </div>
            </div>
          </button>
        ))}
      </div>

      <div className="flex gap-3 pt-2">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-[var(--text-secondary)] text-[14px] font-medium"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onNext}
          disabled={!selectedTemplate}
          className="flex-1 py-2.5 rounded-xl bg-[var(--accent)] text-white text-[14px] font-semibold disabled:opacity-40 hover:opacity-90 transition-opacity"
        >
          Next →
        </button>
      </div>
    </div>
  );
}

function StepAddKnowledge({ onNext, onBack }: { onNext: () => void; onBack: () => void }) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [url, setUrl] = useState("");
  const [dragging, setDragging] = useState(false);
  const [uploaded, setUploaded] = useState<string[]>([]);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) setUploaded((prev) => [...prev, file.name]);
  };

  return (
    <div className="space-y-4">
      <p className="text-[13px] text-[var(--text-muted)]">
        Add documents or URLs to give your bot knowledge. You can skip this and add later.
      </p>

      {/* Drop zone */}
      <button
        type="button"
        onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
        onClick={() => fileRef.current?.click()}
        className={cn(
          "w-full border-2 border-dashed rounded-xl p-8 text-center transition-all",
          dragging
            ? "border-[var(--accent)] bg-[var(--accent)]/5"
            : "border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--bg-surface)]"
        )}
        aria-label="Drop files or click to upload"
      >
        <Upload size={24} className="mx-auto mb-2 text-[var(--text-muted)]" />
        <p className="text-[13px] font-medium text-[var(--text-primary)]">
          Drop PDF files here or click to browse
        </p>
        <p className="text-[11px] text-[var(--text-muted)] mt-1">
          PDF, TXT supported
        </p>
      </button>
      <input
        ref={fileRef}
        type="file"
        accept=".pdf,.txt"
        className="hidden"
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) setUploaded((prev) => [...prev, f.name]);
        }}
      />

      {uploaded.length > 0 && (
        <div className="space-y-1.5">
          {uploaded.map((name, i) => (
            <div
              key={i}
              className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[var(--bg-surface)] border border-[var(--border)]"
            >
              <Check size={12} className="text-[var(--success)] flex-shrink-0" />
              <span className="text-[12px] text-[var(--text-primary)] truncate flex-1">{name}</span>
            </div>
          ))}
        </div>
      )}

      {/* URL input */}
      <div>
        <label
          htmlFor="kb-url"
          className="block text-[12px] font-medium text-[var(--text-secondary)] mb-1.5"
        >
          Or add a URL
        </label>
        <div className="flex gap-2">
          <input
            id="kb-url"
            type="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://docs.example.com"
            className={cn(
              "flex-1 px-3 py-2 text-[13px] rounded-lg",
              "bg-[var(--bg-surface)] border border-[var(--border)]",
              "text-[var(--text-primary)] placeholder:text-[var(--text-muted)]",
              "outline-none focus:border-[var(--accent)] transition-colors"
            )}
          />
          <button
            type="button"
            onClick={() => {
              if (url) {
                setUploaded((prev) => [...prev, url]);
                setUrl("");
              }
            }}
            disabled={!url}
            className="px-3 py-2 rounded-lg bg-[var(--accent)] text-white text-[12px] disabled:opacity-40"
          >
            Add
          </button>
        </div>
      </div>

      <div className="flex gap-3 pt-2">
        <button
          type="button"
          onClick={onBack}
          className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-[var(--text-secondary)] text-[14px] font-medium"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onNext}
          className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-[var(--text-muted)] text-[14px]"
        >
          Skip for now
        </button>
        {uploaded.length > 0 && (
          <button
            type="button"
            onClick={onNext}
            className="flex-1 py-2.5 rounded-xl bg-[var(--accent)] text-white text-[14px] font-semibold hover:opacity-90 transition-opacity"
          >
            Next →
          </button>
        )}
      </div>
    </div>
  );
}

function StepEmbedCode({
  botId,
  onDone,
  onBack,
}: {
  botId: string | null;
  onDone: () => void;
  onBack: () => void;
}) {
  const [copied, setCopied] = useState(false);
  const scriptTag = `<script src="https://cdn.threadly.dev/widget.js" data-bot="${botId ?? "YOUR_BOT_ID"}"></script>`;

  const handleCopy = useCallback(async () => {
    await navigator.clipboard.writeText(scriptTag);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }, [scriptTag]);

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2 text-[var(--success)]">
        <div className="w-8 h-8 rounded-full bg-[var(--success)]/20 flex items-center justify-center">
          <Check size={16} />
        </div>
        <p className="text-[15px] font-semibold text-[var(--text-primary)]">
          Your bot is ready!
        </p>
      </div>

      <p className="text-[13px] text-[var(--text-muted)]">
        Add this snippet to your website&apos;s{" "}
        <code className="text-[var(--accent)] bg-[var(--bg-surface)] px-1 rounded">
          &lt;/body&gt;
        </code>{" "}
        tag:
      </p>

      <div className="relative rounded-xl bg-[var(--bg-surface)] border border-[var(--border)] p-4">
        <pre className="text-[11px] font-mono text-[var(--text-primary)] whitespace-pre-wrap break-all pr-10">
          {scriptTag}
        </pre>
        <button
          type="button"
          onClick={handleCopy}
          className={cn(
            "absolute top-3 right-3 p-2 rounded-lg transition-colors",
            copied
              ? "text-[var(--success)] bg-[var(--success)]/10"
              : "text-[var(--text-muted)] hover:text-[var(--text-primary)] bg-[var(--bg-panel)]"
          )}
          aria-label="Copy embed code"
        >
          {copied ? <Check size={14} /> : <Copy size={14} />}
        </button>
      </div>

      {botId && (
        <a
          href={`/builder/${botId}`}
          className="flex items-center justify-center gap-2 w-full py-2.5 rounded-xl border border-[var(--border)] text-[var(--text-secondary)] text-[14px] hover:border-[var(--accent)] hover:text-[var(--accent)] transition-colors"
        >
          <ExternalLink size={14} />
          Open in Flow Builder
        </a>
      )}

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          className="py-2.5 px-5 rounded-xl border border-[var(--border)] text-[var(--text-secondary)] text-[14px] font-medium"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onDone}
          className="flex-1 py-2.5 rounded-xl bg-[var(--accent)] text-white text-[14px] font-semibold hover:opacity-90 transition-opacity"
        >
          Done — Go to Dashboard
        </button>
      </div>
    </div>
  );
}

// ── Main wizard ───────────────────────────────────────────────────────────────

const STEP_TITLES = [
  "Welcome",
  "Name your bot",
  "Choose template",
  "Add knowledge",
  "Get embed code",
];

interface OnboardingWizardProps {
  /** Called when the wizard is dismissed or completed */
  onClose?: () => void;
}

export function OnboardingWizard({ onClose }: OnboardingWizardProps) {
  const { data: session } = useSession();
  const token = session?.accessToken;

  const [show, setShow] = useState(false);
  const [step, setStep] = useState(0);
  const [createdBotId, setCreatedBotId] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  const [botConfig, setBotConfig] = useState<BotConfig>({
    name: "",
    color: "#6366F1",
    emoji: "🤖",
  });
  const [selectedTemplate, setSelectedTemplate] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const done = localStorage.getItem(ONBOARDING_KEY);
      if (!done) setShow(true);
    }
  }, []);

  const dismiss = useCallback(() => {
    setShow(false);
    onClose?.();
  }, [onClose]);

  const handleComplete = useCallback(async () => {
    setIsCreating(true);
    try {
      const template = TEMPLATES.find((t) => t.id === selectedTemplate);
      const bot = await api.post<{ id: string }>(
        "/v1/bots",
        {
          name: botConfig.name || "My Bot",
          accentColor: botConfig.color,
          greetingText: `Hi! I'm ${botConfig.name || "your assistant"}.`,
          definition: template?.definition ?? null,
        },
        token
      );
      setCreatedBotId(bot.id);
      setStep(4);
    } catch {
      toast.error("Failed to create bot — please try again");
    } finally {
      setIsCreating(false);
    }
  }, [botConfig, selectedTemplate, token]);

  const finishWizard = useCallback(() => {
    localStorage.setItem(ONBOARDING_KEY, "1");
    setShow(false);
    onClose?.();
  }, [onClose]);

  if (!show) return null;

  const progress = ((step + 1) / TOTAL_STEPS) * 100;

  const slideVariants = {
    enter: (dir: number) => ({ x: dir > 0 ? 40 : -40, opacity: 0 }),
    center: { x: 0, opacity: 1 },
    exit: (dir: number) => ({ x: dir > 0 ? -40 : 40, opacity: 0 }),
  };

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4"
      role="dialog"
      aria-modal="true"
      aria-label="Onboarding wizard"
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ duration: 0.25 }}
        className="w-full max-w-md bg-[var(--bg-panel)] rounded-2xl shadow-2xl border border-[var(--border)] overflow-hidden"
      >
        {/* Progress bar */}
        <div className="h-1 bg-[var(--border)]">
          <motion.div
            className="h-full bg-[var(--accent)]"
            animate={{ width: `${progress}%` }}
            transition={{ duration: 0.3 }}
          />
        </div>

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[var(--border)]">
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-wide text-[var(--text-muted)]">
              Step {step + 1} of {TOTAL_STEPS}
            </p>
            <p className="text-[15px] font-semibold text-[var(--text-primary)]">
              {STEP_TITLES[step]}
            </p>
          </div>
          <button
            type="button"
            onClick={dismiss}
            className="p-2 rounded-lg text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)] transition-colors"
            aria-label="Close wizard"
          >
            <X size={16} />
          </button>
        </div>

        {/* Step dots */}
        <div className="flex items-center justify-center gap-2 pt-4 px-6">
          {Array.from({ length: TOTAL_STEPS }).map((_, i) => (
            <div
              key={i}
              className={cn(
                "rounded-full transition-all duration-300",
                i === step
                  ? "w-6 h-2 bg-[var(--accent)]"
                  : i < step
                  ? "w-2 h-2 bg-[var(--accent)] opacity-50"
                  : "w-2 h-2 bg-[var(--border)]"
              )}
            />
          ))}
        </div>

        {/* Step content */}
        <div className="px-6 py-5 min-h-[340px]">
          <AnimatePresence mode="wait" custom={1}>
            <motion.div
              key={step}
              custom={1}
              variants={slideVariants}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.2, ease: "easeOut" }}
            >
              {step === 0 && <StepWelcome onNext={() => setStep(1)} />}
              {step === 1 && (
                <StepNameBot
                  config={botConfig}
                  setConfig={setBotConfig}
                  onNext={() => setStep(2)}
                  onBack={() => setStep(0)}
                />
              )}
              {step === 2 && (
                <StepChooseTemplate
                  selectedTemplate={selectedTemplate}
                  setSelectedTemplate={setSelectedTemplate}
                  onNext={() => setStep(3)}
                  onBack={() => setStep(1)}
                />
              )}
              {step === 3 && (
                <StepAddKnowledge
                  onNext={handleComplete}
                  onBack={() => setStep(2)}
                />
              )}
              {step === 4 && (
                <StepEmbedCode
                  botId={createdBotId}
                  onDone={finishWizard}
                  onBack={() => setStep(3)}
                />
              )}
            </motion.div>
          </AnimatePresence>
        </div>

        {isCreating && (
          <div className="absolute inset-0 bg-black/30 flex items-center justify-center rounded-2xl">
            <div className="bg-[var(--bg-panel)] rounded-xl px-6 py-4 text-[13px] text-[var(--text-primary)] font-medium border border-[var(--border)]">
              Creating your bot…
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
}
