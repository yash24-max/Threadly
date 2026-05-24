/** @jsxImportSource preact */
import { useState, useRef, useEffect } from "preact/hooks"
import type { WidgetConfig } from "../types"

interface LeadCaptureFormProps {
  botId: string
  fields: Array<"name" | "email" | "phone">
  onSubmit: (data: LeadData) => Promise<void>
  onSkip: () => void
  config?: WidgetConfig
}

export interface LeadData {
  name?: string
  email?: string
  phone?: string
}

interface FormState {
  name: string
  email: string
  phone: string
  errors: Record<string, string>
  isSubmitting: boolean
  success: boolean
}

/**
 * LeadCaptureForm — Pre-chat lead capture with validation
 * Renders a clean form in widget style with smooth animations
 */
export function LeadCaptureForm({
  botId,
  fields,
  onSubmit,
  onSkip,
  config,
}: LeadCaptureFormProps) {
  const [state, setState] = useState<FormState>({
    name: "",
    email: "",
    phone: "",
    errors: {},
    isSubmitting: false,
    success: false,
  })

  const formRef = useRef<HTMLFormElement>(null)
  const [visibleFields, setVisibleFields] = useState<number>(0)

  // Stagger field animations on mount
  useEffect(() => {
    let timeouts: number[] = []
    fields.forEach((_, idx) => {
      const timeout = window.setTimeout(() => {
        setVisibleFields((prev) => Math.min(prev + 1, fields.length))
      }, idx * 100)
      timeouts.push(timeout)
    })
    return () => timeouts.forEach(clearTimeout)
  }, [fields.length])

  const validateField = (name: string, value: string): string => {
    if (name === "email") {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!value) return "Email is required"
      if (!emailRegex.test(value)) return "Invalid email format"
    }
    if (name === "name") {
      if (!value || value.trim().length === 0) return "Name is required"
      if (value.trim().length < 2) return "Name must be at least 2 characters"
    }
    if (name === "phone") {
      if (!value) return ""
      const phoneRegex = /^[\d\s\-\+\(\)]+$/
      if (!phoneRegex.test(value)) return "Invalid phone number"
      if (value.replace(/\D/g, "").length < 10) return "Phone number too short"
    }
    return ""
  }

  const handleFieldChange = (fieldName: string, value: string) => {
    setState((prev) => ({
      ...prev,
      [fieldName]: value,
      errors: {
        ...prev.errors,
        [fieldName]: "",
      },
    }))
  }

  const handleBlur = (fieldName: string) => {
    const value = state[fieldName as keyof Omit<FormState, "errors" | "isSubmitting" | "success">]
    const error = validateField(fieldName, String(value))
    setState((prev) => ({
      ...prev,
      errors: {
        ...prev.errors,
        [fieldName]: error,
      },
    }))
  }

  const handleSubmit = async (e: Event) => {
    e.preventDefault()

    // Validate all fields
    const errors: Record<string, string> = {}
    fields.forEach((field) => {
      const value = state[field]
      const error = validateField(field, value)
      if (error) {
        errors[field] = error
      }
    })

    if (Object.keys(errors).length > 0) {
      setState((prev) => ({
        ...prev,
        errors,
      }))
      return
    }

    setState((prev) => ({
      ...prev,
      isSubmitting: true,
    }))

    try {
      const leadData: LeadData = {}
      fields.forEach((field) => {
        if (state[field]) {
          leadData[field] = state[field]
        }
      })

      await onSubmit(leadData)

      setState((prev) => ({
        ...prev,
        success: true,
        isSubmitting: false,
      }))

      // Close form after success animation
      setTimeout(() => {
        onSkip()
      }, 1200)
    } catch (error) {
      log.error("lead.form.submit.failed", { error })
      setState((prev) => ({
        ...prev,
        isSubmitting: false,
        errors: {
          ...prev.errors,
          submit: error instanceof Error ? error.message : "Submission failed",
        },
      }))
    }
  }

  if (state.success) {
    return (
      <div
        style={{
          padding: "24px",
          textAlign: "center",
          animation: "fadeIn 0.3s ease-in-out",
        }}
      >
        <div
          style={{
            width: "48px",
            height: "48px",
            margin: "0 auto 16px",
            borderRadius: "50%",
            background: "var(--tly-accent)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            animation: "scaleIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)",
          }}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
        <p
          style={{
            margin: 0,
            fontSize: "14px",
            fontWeight: "500",
            color: "var(--tly-text)",
            marginBottom: "8px",
          }}
        >
          Thanks for reaching out!
        </p>
        <p
          style={{
            margin: 0,
            fontSize: "12px",
            color: "var(--tly-muted)",
          }}
        >
          We'll be in touch shortly
        </p>
      </div>
    )
  }

  // Check if all required fields (name, email) are in the fields list
  const allFieldsOptional = !fields.includes("name") && !fields.includes("email")

  return (
    <div
      style={{
        padding: "20px",
        animation: "slideUp 0.3s ease-out",
      }}
    >
      <style>{`
        @keyframes slideUp {
          from {
            opacity: 0;
            transform: translateY(16px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes fadeIn {
          from {
            opacity: 0;
          }
          to {
            opacity: 1;
          }
        }

        @keyframes scaleIn {
          from {
            opacity: 0;
            transform: scale(0.8);
          }
          to {
            opacity: 1;
            transform: scale(1);
          }
        }

        .tly-lead-field {
          animation: slideUp 0.3s ease-out backwards;
        }

        .tly-lead-field:nth-child(1) { animation-delay: 0ms; }
        .tly-lead-field:nth-child(2) { animation-delay: 100ms; }
        .tly-lead-field:nth-child(3) { animation-delay: 200ms; }

        .tly-lead-input {
          width: 100%;
          padding: 10px 12px;
          margin-top: 6px;
          border: 1px solid var(--tly-border);
          border-radius: 6px;
          background: var(--tly-input-bg);
          color: var(--tly-text);
          font-size: 14px;
          font-family: inherit;
          transition: all 0.2s ease;
          box-sizing: border-box;
        }

        .tly-lead-input:focus {
          outline: none;
          border-color: var(--tly-accent);
          box-shadow: 0 0 0 2px rgba(var(--tly-accent-rgb), 0.1);
        }

        .tly-lead-input.error {
          border-color: #ef4444;
          background: rgba(239, 68, 68, 0.05);
        }

        .tly-lead-error {
          font-size: 12px;
          color: #ef4444;
          margin-top: 4px;
          display: block;
        }

        .tly-lead-label {
          display: block;
          font-size: 13px;
          font-weight: 500;
          color: var(--tly-text);
          margin-top: 12px;
        }

        .tly-lead-label:first-child {
          margin-top: 0;
        }

        .tly-lead-submit {
          width: 100%;
          padding: 10px 16px;
          margin-top: 20px;
          background: var(--tly-accent);
          color: white;
          border: none;
          border-radius: 6px;
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s ease;
          font-family: inherit;
        }

        .tly-lead-submit:hover:not(:disabled) {
          opacity: 0.9;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
        }

        .tly-lead-submit:active:not(:disabled) {
          transform: translateY(0);
        }

        .tly-lead-submit:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .tly-lead-skip {
          width: 100%;
          padding: 10px 16px;
          margin-top: 8px;
          background: transparent;
          color: var(--tly-text);
          border: 1px solid var(--tly-border);
          border-radius: 6px;
          font-size: 14px;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.2s ease;
          font-family: inherit;
        }

        .tly-lead-skip:hover {
          background: var(--tly-input-bg);
        }

        .tly-lead-skip:active {
          opacity: 0.8;
        }

        .tly-lead-submit.loading {
          opacity: 0.8;
          cursor: wait;
          position: relative;
        }

        .tly-lead-submit.loading::after {
          content: '';
          position: absolute;
          width: 14px;
          height: 14px;
          top: 50%;
          right: 12px;
          margin-top: -7px;
          border: 2px solid rgba(255, 255, 255, 0.3);
          border-top-color: white;
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>

      <h3
        style={{
          margin: "0 0 4px 0",
          fontSize: "16px",
          fontWeight: "600",
          color: "var(--tly-text)",
        }}
      >
        {config?.botName ? `Connect with ${config.botName}` : "Start the conversation"}
      </h3>

      <p
        style={{
          margin: "4px 0 16px 0",
          fontSize: "13px",
          color: "var(--tly-muted)",
        }}
      >
        Please provide your details
      </p>

      <form ref={formRef} onSubmit={handleSubmit}>
        {visibleFields > 0 && fields.includes("name") && (
          <div class="tly-lead-field">
            <label class="tly-lead-label" for="lead-name">
              Full Name
            </label>
            <input
              id="lead-name"
              type="text"
              class={`tly-lead-input ${state.errors.name ? "error" : ""}`}
              value={state.name}
              onChange={(e) => handleFieldChange("name", e.currentTarget.value)}
              onBlur={() => handleBlur("name")}
              placeholder="John Doe"
              disabled={state.isSubmitting}
              required
            />
            {state.errors.name && <span class="tly-lead-error">{state.errors.name}</span>}
          </div>
        )}

        {visibleFields > 1 && fields.includes("email") && (
          <div class="tly-lead-field">
            <label class="tly-lead-label" for="lead-email">
              Email
            </label>
            <input
              id="lead-email"
              type="email"
              class={`tly-lead-input ${state.errors.email ? "error" : ""}`}
              value={state.email}
              onChange={(e) => handleFieldChange("email", e.currentTarget.value)}
              onBlur={() => handleBlur("email")}
              placeholder="you@example.com"
              disabled={state.isSubmitting}
              required
            />
            {state.errors.email && <span class="tly-lead-error">{state.errors.email}</span>}
          </div>
        )}

        {visibleFields > 2 && fields.includes("phone") && (
          <div class="tly-lead-field">
            <label class="tly-lead-label" for="lead-phone">
              Phone
            </label>
            <input
              id="lead-phone"
              type="tel"
              class={`tly-lead-input ${state.errors.phone ? "error" : ""}`}
              value={state.phone}
              onChange={(e) => handleFieldChange("phone", e.currentTarget.value)}
              onBlur={() => handleBlur("phone")}
              placeholder="+1 (555) 000-0000"
              disabled={state.isSubmitting}
            />
            {state.errors.phone && <span class="tly-lead-error">{state.errors.phone}</span>}
          </div>
        )}

        {state.errors.submit && (
          <div
            style={{
              marginTop: "12px",
              padding: "8px 10px",
              background: "rgba(239, 68, 68, 0.1)",
              border: "1px solid #fecaca",
              borderRadius: "4px",
              fontSize: "12px",
              color: "#dc2626",
            }}
          >
            {state.errors.submit}
          </div>
        )}

        <button
          type="submit"
          class={`tly-lead-submit ${state.isSubmitting ? "loading" : ""}`}
          disabled={state.isSubmitting}
        >
          {state.isSubmitting ? "Submitting..." : "Continue to Chat"}
        </button>

        {allFieldsOptional && (
          <button
            type="button"
            class="tly-lead-skip"
            onClick={onSkip}
            disabled={state.isSubmitting}
          >
            Skip for now
          </button>
        )}
      </form>
    </div>
  )
}

// Simple logger for development
const log = {
  error(msg: string, data?: unknown) {
    console.error(`[Threadly] ${msg}`, data)
  },
}
