"""Template catalog — 20 pre-built flow templates for quick bot setup."""
from __future__ import annotations

import uuid
from typing import Any, TypedDict


class FlowNode(TypedDict, total=False):
    """Flow node definition."""

    id: str
    type: str
    label: str
    config: dict[str, Any]
    position: dict[str, int]


class FlowEdge(TypedDict):
    """Flow edge definition."""

    source: str
    target: str
    label: str | None


class TemplateDefinition(TypedDict):
    """Template definition."""

    id: str
    name: str
    description: str
    category: str
    node_count: int
    estimated_setup_minutes: int
    flow_json: dict[str, Any]


def _gen_node_id() -> str:
    """Generate a unique node ID."""
    return f"node_{uuid.uuid4().hex[:8]}"


def _build_flow(nodes: list[FlowNode], edges: list[FlowEdge]) -> dict[str, Any]:
    """Build flow JSON structure."""
    return {"nodes": nodes, "edges": edges, "version": "1.0"}


TEMPLATES: dict[str, TemplateDefinition] = {}

# ============================================================================
# 1. Customer Support FAQ Bot
# ============================================================================
_n_start = _gen_node_id()
_n_question = _gen_node_id()
_n_switch = _gen_node_id()
_n_faq_msg = _gen_node_id()
_n_ai_reply = _gen_node_id()
_n_escalate = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["customer-support-faq"] = {
    "id": "customer-support-faq",
    "name": "Customer Support Bot",
    "description": "FAQ-driven support with escalation to human agents",
    "category": "SUPPORT",
    "node_count": 7,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_question,
                "type": "question",
                "label": "Get Customer Question",
                "config": {"prompt": "How can I help you today?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_switch,
                "type": "switch",
                "label": "Search KB",
                "config": {"intent_detection": True, "top_k": 5},
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_faq_msg,
                "type": "message",
                "label": "Send FAQ Answer",
                "config": {
                    "template": "Based on our KB: {{kb_result}}\n\nWas this helpful?"
                },
                "position": {"x": 250, "y": 250},
            },
            {
                "id": _n_ai_reply,
                "type": "ai_reply",
                "label": "Generate AI Response",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "You are a helpful customer support agent. Be concise and professional.",
                },
                "position": {"x": 250, "y": 150},
            },
            {
                "id": _n_escalate,
                "type": "handoff",
                "label": "Escalate to Agent",
                "config": {"queue": "support", "priority": "normal"},
                "position": {"x": 450, "y": 250},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 450, "y": 350},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_question,
                "label": None,
            },
            {
                "source": _n_question,
                "target": _n_switch,
                "label": None,
            },
            {
                "source": _n_switch,
                "target": _n_faq_msg,
                "label": "KB found",
            },
            {
                "source": _n_switch,
                "target": _n_ai_reply,
                "label": "No match",
            },
            {
                "source": _n_faq_msg,
                "target": _n_end,
                "label": None,
            },
            {
                "source": _n_ai_reply,
                "target": _n_escalate,
                "label": "user_not_satisfied",
            },
            {
                "source": _n_escalate,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 2. Lead Capture Bot
# ============================================================================
_n_start = _gen_node_id()
_n_lead_form = _gen_node_id()
_n_store = _gen_node_id()
_n_thank = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["lead-capture"] = {
    "id": "lead-capture",
    "name": "Lead Capture Form",
    "description": "Collect contact details and qualification info from visitors",
    "category": "SALES",
    "node_count": 5,
    "estimated_setup_minutes": 3,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "widget_open"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_lead_form,
                "type": "form",
                "label": "Lead Form",
                "config": {
                    "fields": [
                        {"name": "name", "type": "text", "label": "Full Name", "required": True},
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "phone",
                            "type": "phone",
                            "label": "Phone",
                            "required": False,
                        },
                        {
                            "name": "company",
                            "type": "text",
                            "label": "Company",
                            "required": False,
                        },
                    ]
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_store,
                "type": "action",
                "label": "Store Lead",
                "config": {"action": "create_lead", "api_endpoint": "/leads"},
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_thank,
                "type": "message",
                "label": "Thank You",
                "config": {
                    "template": "Thanks {{name}}! We'll be in touch within 24 hours."
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 450},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_lead_form,
                "label": None,
            },
            {
                "source": _n_lead_form,
                "target": _n_store,
                "label": None,
            },
            {
                "source": _n_store,
                "target": _n_thank,
                "label": None,
            },
            {
                "source": _n_thank,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 3. Product Recommendation Bot
# ============================================================================
_n_start = _gen_node_id()
_n_ask_need = _gen_node_id()
_n_classify = _gen_node_id()
_n_recommend = _gen_node_id()
_n_upsell = _gen_node_id()
_n_cta = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["product-recommendation"] = {
    "id": "product-recommendation",
    "name": "Product Recommendation Engine",
    "description": "Personalized product suggestions based on customer needs",
    "category": "ECOMMERCE",
    "node_count": 7,
    "estimated_setup_minutes": 8,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_ask_need,
                "type": "question",
                "label": "Ask Customer Need",
                "config": {"prompt": "What are you looking for?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_classify,
                "type": "ai_reply",
                "label": "Classify Intent",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "Classify the customer intent to a product category.",
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_recommend,
                "type": "ai_reply",
                "label": "Generate Recommendations",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "Recommend 3 products matching their needs with prices and features.",
                },
                "position": {"x": 250, "y": 250},
            },
            {
                "id": _n_upsell,
                "type": "action",
                "label": "Add to Cart",
                "config": {"action": "add_to_cart", "api_endpoint": "/cart"},
                "position": {"x": 250, "y": 350},
            },
            {
                "id": _n_cta,
                "type": "message",
                "label": "Checkout CTA",
                "config": {"template": "Ready? Click below to checkout!"},
                "position": {"x": 450, "y": 250},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 450, "y": 350},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_ask_need,
                "label": None,
            },
            {
                "source": _n_ask_need,
                "target": _n_classify,
                "label": None,
            },
            {
                "source": _n_classify,
                "target": _n_recommend,
                "label": None,
            },
            {
                "source": _n_recommend,
                "target": _n_upsell,
                "label": None,
            },
            {
                "source": _n_upsell,
                "target": _n_cta,
                "label": None,
            },
            {
                "source": _n_cta,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 4. Appointment Booking
# ============================================================================
_n_start = _gen_node_id()
_n_get_service = _gen_node_id()
_n_check_availability = _gen_node_id()
_n_booking_form = _gen_node_id()
_n_confirm = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["appointment-booking"] = {
    "id": "appointment-booking",
    "name": "Appointment Booking",
    "description": "Schedule appointments with availability checking and confirmation",
    "category": "BUSINESS",
    "node_count": 6,
    "estimated_setup_minutes": 10,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_get_service,
                "type": "buttons",
                "label": "Select Service",
                "config": {
                    "text": "What service would you like?",
                    "options": ["Consultation", "Support", "Demo"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_check_availability,
                "type": "action",
                "label": "Check Slots",
                "config": {"action": "list_availability", "api_endpoint": "/calendar/slots"},
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_booking_form,
                "type": "form",
                "label": "Booking Details",
                "config": {
                    "fields": [
                        {
                            "name": "name",
                            "type": "text",
                            "label": "Your Name",
                            "required": True,
                        },
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "datetime",
                            "type": "datetime",
                            "label": "Preferred Time",
                            "required": True,
                        },
                    ]
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_confirm,
                "type": "message",
                "label": "Confirmation",
                "config": {
                    "template": "Your appointment is confirmed for {{datetime}}. Check your email for details."
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_get_service,
                "label": None,
            },
            {
                "source": _n_get_service,
                "target": _n_check_availability,
                "label": None,
            },
            {
                "source": _n_check_availability,
                "target": _n_booking_form,
                "label": None,
            },
            {
                "source": _n_booking_form,
                "target": _n_confirm,
                "label": None,
            },
            {
                "source": _n_confirm,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 5. Feedback Collection
# ============================================================================
_n_start = _gen_node_id()
_n_rating = _gen_node_id()
_n_reason = _gen_node_id()
_n_store_feedback = _gen_node_id()
_n_thank = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["feedback-collection"] = {
    "id": "feedback-collection",
    "name": "Feedback Collection",
    "description": "Gather customer satisfaction and improvement suggestions",
    "category": "ANALYTICS",
    "node_count": 6,
    "estimated_setup_minutes": 4,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_rating,
                "type": "buttons",
                "label": "Rate Experience",
                "config": {
                    "text": "How would you rate your experience?",
                    "options": ["😀 Excellent", "😊 Good", "😐 Okay", "😞 Poor"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_reason,
                "type": "question",
                "label": "Get Feedback",
                "config": {"prompt": "What could we improve?"},
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_store_feedback,
                "type": "action",
                "label": "Store Feedback",
                "config": {"action": "store_feedback", "api_endpoint": "/feedback"},
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_thank,
                "type": "message",
                "label": "Thank You",
                "config": {"template": "Thanks for the feedback! It helps us improve."},
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_rating,
                "label": None,
            },
            {
                "source": _n_rating,
                "target": _n_reason,
                "label": None,
            },
            {
                "source": _n_reason,
                "target": _n_store_feedback,
                "label": None,
            },
            {
                "source": _n_store_feedback,
                "target": _n_thank,
                "label": None,
            },
            {
                "source": _n_thank,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 6. Knowledge Base Search
# ============================================================================
_n_start = _gen_node_id()
_n_search_query = _gen_node_id()
_n_semantic_search = _gen_node_id()
_n_display = _gen_node_id()
_n_helpful = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["knowledge-base-search"] = {
    "id": "knowledge-base-search",
    "name": "Knowledge Base Search",
    "description": "Semantic search through internal knowledge base with feedback loop",
    "category": "SUPPORT",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_search_query,
                "type": "question",
                "label": "Search Query",
                "config": {"prompt": "What would you like to know?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_semantic_search,
                "type": "action",
                "label": "Search KB",
                "config": {
                    "action": "semantic_search",
                    "api_endpoint": "/kb/query",
                    "top_k": 5,
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_display,
                "type": "message",
                "label": "Display Results",
                "config": {"template": "Here's what I found:\n{{kb_results}}"},
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_helpful,
                "type": "buttons",
                "label": "Helpful?",
                "config": {
                    "text": "Was this helpful?",
                    "options": ["Yes", "No", "Ask Agent"],
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_search_query,
                "label": None,
            },
            {
                "source": _n_search_query,
                "target": _n_semantic_search,
                "label": None,
            },
            {
                "source": _n_semantic_search,
                "target": _n_display,
                "label": None,
            },
            {
                "source": _n_display,
                "target": _n_helpful,
                "label": None,
            },
            {
                "source": _n_helpful,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 7. Complaint Resolution
# ============================================================================
_n_start = _gen_node_id()
_n_collect_issue = _gen_node_id()
_n_categorize = _gen_node_id()
_n_attempt_resolve = _gen_node_id()
_n_escalate = _gen_node_id()
_n_follow_up = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["complaint-resolution"] = {
    "id": "complaint-resolution",
    "name": "Complaint Resolution",
    "description": "Structured complaint handling with categorization and escalation",
    "category": "SUPPORT",
    "node_count": 7,
    "estimated_setup_minutes": 7,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_collect_issue,
                "type": "question",
                "label": "Describe Issue",
                "config": {"prompt": "Please describe your issue in detail"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_categorize,
                "type": "ai_reply",
                "label": "Categorize Complaint",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "Categorize the complaint as: billing, product, delivery, service, or other.",
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_attempt_resolve,
                "type": "ai_reply",
                "label": "Suggest Resolution",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "Provide empathetic resolution suggestions based on the complaint category.",
                },
                "position": {"x": 250, "y": 250},
            },
            {
                "id": _n_escalate,
                "type": "handoff",
                "label": "Escalate if Needed",
                "config": {"queue": "complaints", "priority": "high"},
                "position": {"x": 450, "y": 250},
            },
            {
                "id": _n_follow_up,
                "type": "message",
                "label": "Follow-up",
                "config": {
                    "template": "We've recorded your complaint. A specialist will follow up within 24 hours."
                },
                "position": {"x": 450, "y": 350},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 450, "y": 450},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_collect_issue,
                "label": None,
            },
            {
                "source": _n_collect_issue,
                "target": _n_categorize,
                "label": None,
            },
            {
                "source": _n_categorize,
                "target": _n_attempt_resolve,
                "label": None,
            },
            {
                "source": _n_attempt_resolve,
                "target": _n_escalate,
                "label": None,
            },
            {
                "source": _n_escalate,
                "target": _n_follow_up,
                "label": None,
            },
            {
                "source": _n_follow_up,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 8. Course/Training Enrollment
# ============================================================================
_n_start = _gen_node_id()
_n_intro = _gen_node_id()
_n_level = _gen_node_id()
_n_enroll_form = _gen_node_id()
_n_confirmation = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["course-enrollment"] = {
    "id": "course-enrollment",
    "name": "Course Enrollment",
    "description": "Enroll students in courses with skill-level assessment",
    "category": "EDUCATION",
    "node_count": 6,
    "estimated_setup_minutes": 6,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_intro,
                "type": "message",
                "label": "Welcome",
                "config": {
                    "template": "Welcome to our courses! Let's find the perfect fit for you."
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_level,
                "type": "buttons",
                "label": "Skill Level",
                "config": {
                    "text": "What's your experience level?",
                    "options": ["Beginner", "Intermediate", "Advanced"],
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_enroll_form,
                "type": "form",
                "label": "Enrollment Form",
                "config": {
                    "fields": [
                        {
                            "name": "name",
                            "type": "text",
                            "label": "Full Name",
                            "required": True,
                        },
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "phone",
                            "type": "phone",
                            "label": "Phone",
                            "required": False,
                        },
                    ]
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_confirmation,
                "type": "message",
                "label": "Enrolled",
                "config": {
                    "template": "Great! You're enrolled. Check your email for course details."
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_intro,
                "label": None,
            },
            {
                "source": _n_intro,
                "target": _n_level,
                "label": None,
            },
            {
                "source": _n_level,
                "target": _n_enroll_form,
                "label": None,
            },
            {
                "source": _n_enroll_form,
                "target": _n_confirmation,
                "label": None,
            },
            {
                "source": _n_confirmation,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 9. Invoice/Payment Follow-up
# ============================================================================
_n_start = _gen_node_id()
_n_invoice_check = _gen_node_id()
_n_status = _gen_node_id()
_n_overdue = _gen_node_id()
_n_reminder = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["invoice-followup"] = {
    "id": "invoice-followup",
    "name": "Invoice & Payment Follow-up",
    "description": "Automated invoice reminders and payment status checks",
    "category": "FINANCE",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "scheduled", "interval": "daily"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_invoice_check,
                "type": "action",
                "label": "Check Invoices",
                "config": {
                    "action": "get_overdue_invoices",
                    "api_endpoint": "/invoices/overdue",
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_status,
                "type": "message",
                "label": "Status Message",
                "config": {
                    "template": "Invoice {{invoice_id}} is due. Balance: {{amount}}"
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_overdue,
                "type": "switch",
                "label": "Check Days Overdue",
                "config": {"condition": "days_overdue > 7"},
                "position": {"x": 250, "y": 250},
            },
            {
                "id": _n_reminder,
                "type": "message",
                "label": "Escalated Reminder",
                "config": {"template": "Your payment is overdue. Please settle immediately."},
                "position": {"x": 450, "y": 250},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 450, "y": 350},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_invoice_check,
                "label": None,
            },
            {
                "source": _n_invoice_check,
                "target": _n_status,
                "label": None,
            },
            {
                "source": _n_status,
                "target": _n_overdue,
                "label": None,
            },
            {
                "source": _n_overdue,
                "target": _n_reminder,
                "label": "overdue",
            },
            {
                "source": _n_reminder,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 10. Event Registration
# ============================================================================
_n_start = _gen_node_id()
_n_event_select = _gen_node_id()
_n_ticket_type = _gen_node_id()
_n_reg_form = _gen_node_id()
_n_payment = _gen_node_id()
_n_confirmation = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["event-registration"] = {
    "id": "event-registration",
    "name": "Event Registration",
    "description": "Register attendees for events with ticket selection and payment",
    "category": "EVENTS",
    "node_count": 7,
    "estimated_setup_minutes": 9,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_event_select,
                "type": "buttons",
                "label": "Select Event",
                "config": {
                    "text": "Which event are you interested in?",
                    "options": ["Tech Summit 2024", "Workshop Series", "Networking Mixer"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_ticket_type,
                "type": "buttons",
                "label": "Ticket Type",
                "config": {
                    "text": "Choose your ticket",
                    "options": ["Standard ($50)", "VIP ($100)", "Group ($40 ea)"],
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_reg_form,
                "type": "form",
                "label": "Registration Details",
                "config": {
                    "fields": [
                        {
                            "name": "name",
                            "type": "text",
                            "label": "Full Name",
                            "required": True,
                        },
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "company",
                            "type": "text",
                            "label": "Company",
                            "required": False,
                        },
                    ]
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_payment,
                "type": "action",
                "label": "Process Payment",
                "config": {
                    "action": "process_payment",
                    "api_endpoint": "/payments/charge",
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_confirmation,
                "type": "message",
                "label": "Confirmation",
                "config": {
                    "template": "You're registered! Confirmation sent to {{email}}"
                },
                "position": {"x": 50, "y": 550},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 650},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_event_select,
                "label": None,
            },
            {
                "source": _n_event_select,
                "target": _n_ticket_type,
                "label": None,
            },
            {
                "source": _n_ticket_type,
                "target": _n_reg_form,
                "label": None,
            },
            {
                "source": _n_reg_form,
                "target": _n_payment,
                "label": None,
            },
            {
                "source": _n_payment,
                "target": _n_confirmation,
                "label": None,
            },
            {
                "source": _n_confirmation,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 11. Account Onboarding
# ============================================================================
_n_start = _gen_node_id()
_n_welcome = _gen_node_id()
_n_profile = _gen_node_id()
_n_prefs = _gen_node_id()
_n_tutorial = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["account-onboarding"] = {
    "id": "account-onboarding",
    "name": "Account Onboarding",
    "description": "Welcome new users and configure their account preferences",
    "category": "ONBOARDING",
    "node_count": 6,
    "estimated_setup_minutes": 6,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "account_created"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_welcome,
                "type": "message",
                "label": "Welcome",
                "config": {
                    "template": "Welcome to Threadly! Let's get your account set up."
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_profile,
                "type": "form",
                "label": "Complete Profile",
                "config": {
                    "fields": [
                        {
                            "name": "first_name",
                            "type": "text",
                            "label": "First Name",
                            "required": True,
                        },
                        {
                            "name": "last_name",
                            "type": "text",
                            "label": "Last Name",
                            "required": True,
                        },
                        {
                            "name": "timezone",
                            "type": "select",
                            "label": "Timezone",
                            "required": True,
                        },
                    ]
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_prefs,
                "type": "buttons",
                "label": "Notification Preferences",
                "config": {
                    "text": "How should we contact you?",
                    "options": ["Email", "SMS", "Both"],
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_tutorial,
                "type": "message",
                "label": "Tutorial Link",
                "config": {
                    "template": "Check out our quick tutorial: https://docs.threadly.io/quickstart"
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_welcome,
                "label": None,
            },
            {
                "source": _n_welcome,
                "target": _n_profile,
                "label": None,
            },
            {
                "source": _n_profile,
                "target": _n_prefs,
                "label": None,
            },
            {
                "source": _n_prefs,
                "target": _n_tutorial,
                "label": None,
            },
            {
                "source": _n_tutorial,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 12. Survey/Quiz
# ============================================================================
_n_start = _gen_node_id()
_n_intro = _gen_node_id()
_n_q1 = _gen_node_id()
_n_q2 = _gen_node_id()
_n_results = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["survey-quiz"] = {
    "id": "survey-quiz",
    "name": "Survey & Quiz",
    "description": "Conduct surveys or quizzes with scoring and result analysis",
    "category": "ANALYTICS",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_intro,
                "type": "message",
                "label": "Instructions",
                "config": {
                    "template": "This survey takes about 2 minutes. Your feedback helps us improve!"
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_q1,
                "type": "buttons",
                "label": "Question 1",
                "config": {
                    "text": "How satisfied are you with our service?",
                    "options": ["Very Satisfied", "Satisfied", "Neutral", "Unsatisfied"],
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_q2,
                "type": "question",
                "label": "Question 2",
                "config": {"prompt": "What's one thing we should improve?"},
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_results,
                "type": "message",
                "label": "Thank You",
                "config": {"template": "Thanks for completing the survey!"},
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_intro,
                "label": None,
            },
            {
                "source": _n_intro,
                "target": _n_q1,
                "label": None,
            },
            {
                "source": _n_q1,
                "target": _n_q2,
                "label": None,
            },
            {
                "source": _n_q2,
                "target": _n_results,
                "label": None,
            },
            {
                "source": _n_results,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 13. Password Reset Flow
# ============================================================================
_n_start = _gen_node_id()
_n_email = _gen_node_id()
_n_verify = _gen_node_id()
_n_new_pass = _gen_node_id()
_n_success = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["password-reset"] = {
    "id": "password-reset",
    "name": "Password Reset",
    "description": "Guided password reset with email verification",
    "category": "SECURITY",
    "node_count": 6,
    "estimated_setup_minutes": 3,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_email,
                "type": "form",
                "label": "Enter Email",
                "config": {
                    "fields": [
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Account Email",
                            "required": True,
                        }
                    ]
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_verify,
                "type": "form",
                "label": "Verify Code",
                "config": {
                    "fields": [
                        {
                            "name": "code",
                            "type": "text",
                            "label": "Verification Code",
                            "required": True,
                        }
                    ]
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_new_pass,
                "type": "form",
                "label": "New Password",
                "config": {
                    "fields": [
                        {
                            "name": "password",
                            "type": "password",
                            "label": "New Password",
                            "required": True,
                        },
                        {
                            "name": "confirm",
                            "type": "password",
                            "label": "Confirm Password",
                            "required": True,
                        },
                    ]
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_success,
                "type": "message",
                "label": "Success",
                "config": {"template": "Password updated! You can now log in."},
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_email,
                "label": None,
            },
            {
                "source": _n_email,
                "target": _n_verify,
                "label": None,
            },
            {
                "source": _n_verify,
                "target": _n_new_pass,
                "label": None,
            },
            {
                "source": _n_new_pass,
                "target": _n_success,
                "label": None,
            },
            {
                "source": _n_success,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 14. Subscription Management
# ============================================================================
_n_start = _gen_node_id()
_n_action = _gen_node_id()
_n_upgrade = _gen_node_id()
_n_cancel = _gen_node_id()
_n_confirm = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["subscription-management"] = {
    "id": "subscription-management",
    "name": "Subscription Management",
    "description": "Upgrade, downgrade, or cancel subscriptions",
    "category": "BILLING",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_action,
                "type": "buttons",
                "label": "Choose Action",
                "config": {
                    "text": "What would you like to do?",
                    "options": ["Upgrade Plan", "Downgrade Plan", "Cancel Subscription"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_upgrade,
                "type": "message",
                "label": "Show Plans",
                "config": {
                    "template": "Available plans:\n- Pro: $29/mo\n- Enterprise: $99/mo"
                },
                "position": {"x": 250, "y": 150},
            },
            {
                "id": _n_cancel,
                "type": "buttons",
                "label": "Confirm Cancel",
                "config": {
                    "text": "Are you sure you want to cancel?",
                    "options": ["Yes, Cancel", "No, Keep Subscription"],
                },
                "position": {"x": 450, "y": 150},
            },
            {
                "id": _n_confirm,
                "type": "message",
                "label": "Confirmation",
                "config": {"template": "Your subscription has been updated."},
                "position": {"x": 450, "y": 250},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 450, "y": 350},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_action,
                "label": None,
            },
            {
                "source": _n_action,
                "target": _n_upgrade,
                "label": "upgrade",
            },
            {
                "source": _n_action,
                "target": _n_cancel,
                "label": "cancel",
            },
            {
                "source": _n_upgrade,
                "target": _n_confirm,
                "label": None,
            },
            {
                "source": _n_cancel,
                "target": _n_confirm,
                "label": "yes",
            },
            {
                "source": _n_confirm,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 15. Job Application Processing
# ============================================================================
_n_start = _gen_node_id()
_n_job_select = _gen_node_id()
_n_qualifications = _gen_node_id()
_n_apply = _gen_node_id()
_n_screening = _gen_node_id()
_n_confirmation = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["job-application"] = {
    "id": "job-application",
    "name": "Job Application",
    "description": "Process job applications with automatic screening",
    "category": "HR",
    "node_count": 7,
    "estimated_setup_minutes": 8,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_job_select,
                "type": "buttons",
                "label": "Select Position",
                "config": {
                    "text": "Which position are you applying for?",
                    "options": ["Software Engineer", "Product Manager", "Designer"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_qualifications,
                "type": "form",
                "label": "Application Details",
                "config": {
                    "fields": [
                        {
                            "name": "name",
                            "type": "text",
                            "label": "Full Name",
                            "required": True,
                        },
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "resume_url",
                            "type": "url",
                            "label": "Resume Link",
                            "required": True,
                        },
                        {
                            "name": "years_exp",
                            "type": "number",
                            "label": "Years of Experience",
                            "required": True,
                        },
                    ]
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_apply,
                "type": "action",
                "label": "Submit Application",
                "config": {
                    "action": "create_application",
                    "api_endpoint": "/applications",
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_screening,
                "type": "ai_reply",
                "label": "Auto-Screen",
                "config": {
                    "provider": "anthropic",
                    "model": "claude-sonnet-4-5",
                    "system_prompt": "Screen the candidate and provide a summary recommendation.",
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_confirmation,
                "type": "message",
                "label": "Thank You",
                "config": {
                    "template": "Your application has been received. We'll review it and be in touch."
                },
                "position": {"x": 50, "y": 550},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 650},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_job_select,
                "label": None,
            },
            {
                "source": _n_job_select,
                "target": _n_qualifications,
                "label": None,
            },
            {
                "source": _n_qualifications,
                "target": _n_apply,
                "label": None,
            },
            {
                "source": _n_apply,
                "target": _n_screening,
                "label": None,
            },
            {
                "source": _n_screening,
                "target": _n_confirmation,
                "label": None,
            },
            {
                "source": _n_confirmation,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 16. Claim Processing
# ============================================================================
_n_start = _gen_node_id()
_n_claim_type = _gen_node_id()
_n_details = _gen_node_id()
_n_docs = _gen_node_id()
_n_submit = _gen_node_id()
_n_confirmation = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["claim-processing"] = {
    "id": "claim-processing",
    "name": "Insurance Claim Processing",
    "description": "Submit and track insurance claims with document upload",
    "category": "INSURANCE",
    "node_count": 7,
    "estimated_setup_minutes": 7,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_claim_type,
                "type": "buttons",
                "label": "Claim Type",
                "config": {
                    "text": "What type of claim?",
                    "options": ["Health", "Auto", "Property", "Other"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_details,
                "type": "form",
                "label": "Claim Details",
                "config": {
                    "fields": [
                        {
                            "name": "policy_number",
                            "type": "text",
                            "label": "Policy Number",
                            "required": True,
                        },
                        {
                            "name": "incident_date",
                            "type": "date",
                            "label": "Date of Incident",
                            "required": True,
                        },
                        {
                            "name": "description",
                            "type": "textarea",
                            "label": "Detailed Description",
                            "required": True,
                        },
                    ]
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_docs,
                "type": "file_upload",
                "label": "Upload Documents",
                "config": {
                    "allowed_types": ["pdf", "jpg", "png"],
                    "max_files": 5,
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_submit,
                "type": "action",
                "label": "Submit Claim",
                "config": {
                    "action": "create_claim",
                    "api_endpoint": "/claims",
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_confirmation,
                "type": "message",
                "label": "Submitted",
                "config": {
                    "template": "Claim {{claim_id}} submitted. Track status at {{tracking_url}}"
                },
                "position": {"x": 50, "y": 550},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 650},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_claim_type,
                "label": None,
            },
            {
                "source": _n_claim_type,
                "target": _n_details,
                "label": None,
            },
            {
                "source": _n_details,
                "target": _n_docs,
                "label": None,
            },
            {
                "source": _n_docs,
                "target": _n_submit,
                "label": None,
            },
            {
                "source": _n_submit,
                "target": _n_confirmation,
                "label": None,
            },
            {
                "source": _n_confirmation,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 17. Reservation Management
# ============================================================================
_n_start = _gen_node_id()
_n_view_booking = _gen_node_id()
_n_modify = _gen_node_id()
_n_cancel = _gen_node_id()
_n_confirm = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["reservation-management"] = {
    "id": "reservation-management",
    "name": "Reservation Management",
    "description": "Modify or cancel existing reservations",
    "category": "BUSINESS",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_view_booking,
                "type": "question",
                "label": "Get Booking Ref",
                "config": {"prompt": "What's your booking reference?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_modify,
                "type": "buttons",
                "label": "Choose Action",
                "config": {
                    "text": "What would you like to do?",
                    "options": ["Modify Dates", "Change Room", "Cancel Booking"],
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_cancel,
                "type": "buttons",
                "label": "Confirm Cancellation",
                "config": {
                    "text": "Are you sure you want to cancel?",
                    "options": ["Yes, Cancel", "No, Keep Booking"],
                },
                "position": {"x": 250, "y": 250},
            },
            {
                "id": _n_confirm,
                "type": "message",
                "label": "Confirmation",
                "config": {
                    "template": "Your reservation has been updated. Confirmation email sent."
                },
                "position": {"x": 250, "y": 350},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 250, "y": 450},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_view_booking,
                "label": None,
            },
            {
                "source": _n_view_booking,
                "target": _n_modify,
                "label": None,
            },
            {
                "source": _n_modify,
                "target": _n_cancel,
                "label": "cancel",
            },
            {
                "source": _n_cancel,
                "target": _n_confirm,
                "label": "yes",
            },
            {
                "source": _n_confirm,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 18. Technical Support Troubleshooting
# ============================================================================
_n_start = _gen_node_id()
_n_issue_desc = _gen_node_id()
_n_symptoms = _gen_node_id()
_n_steps = _gen_node_id()
_n_resolved = _gen_node_id()
_n_escalate = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["tech-support-troubleshooting"] = {
    "id": "tech-support-troubleshooting",
    "name": "Technical Support Troubleshooting",
    "description": "Step-by-step troubleshooting guide with escalation",
    "category": "SUPPORT",
    "node_count": 7,
    "estimated_setup_minutes": 7,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_issue_desc,
                "type": "question",
                "label": "Describe Issue",
                "config": {"prompt": "What problem are you experiencing?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_symptoms,
                "type": "buttons",
                "label": "Confirm Symptoms",
                "config": {
                    "text": "Which of these apply?",
                    "options": ["App won't open", "Connection error", "Crashes", "Slow performance"],
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_steps,
                "type": "message",
                "label": "Troubleshooting Steps",
                "config": {
                    "template": "1. Try restarting the app\n2. Clear cache\n3. Update to latest version\n4. Restart device"
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_resolved,
                "type": "buttons",
                "label": "Issue Resolved?",
                "config": {
                    "text": "Did that fix it?",
                    "options": ["Yes", "No"],
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_escalate,
                "type": "handoff",
                "label": "Escalate",
                "config": {"queue": "technical", "priority": "high"},
                "position": {"x": 250, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 250, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_issue_desc,
                "label": None,
            },
            {
                "source": _n_issue_desc,
                "target": _n_symptoms,
                "label": None,
            },
            {
                "source": _n_symptoms,
                "target": _n_steps,
                "label": None,
            },
            {
                "source": _n_steps,
                "target": _n_resolved,
                "label": None,
            },
            {
                "source": _n_resolved,
                "target": _n_end,
                "label": "yes",
            },
            {
                "source": _n_resolved,
                "target": _n_escalate,
                "label": "no",
            },
            {
                "source": _n_escalate,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 19. Content Delivery & Downloads
# ============================================================================
_n_start = _gen_node_id()
_n_category = _gen_node_id()
_n_content_list = _gen_node_id()
_n_select = _gen_node_id()
_n_delivery = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["content-delivery"] = {
    "id": "content-delivery",
    "name": "Content Delivery & Downloads",
    "description": "Catalog browsing and file delivery system",
    "category": "CONTENT",
    "node_count": 6,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_category,
                "type": "buttons",
                "label": "Select Category",
                "config": {
                    "text": "What are you looking for?",
                    "options": ["Whitepapers", "Case Studies", "Templates", "Guides"],
                },
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_content_list,
                "type": "action",
                "label": "List Content",
                "config": {
                    "action": "list_content",
                    "api_endpoint": "/content/category",
                },
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_select,
                "type": "buttons",
                "label": "Choose Item",
                "config": {
                    "text": "Which would you like?",
                    "options": ["Item 1", "Item 2", "Item 3"],
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_delivery,
                "type": "message",
                "label": "Send Download Link",
                "config": {
                    "template": "Download link sent to {{email}}\nAccess your content: {{download_url}}"
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 550},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_category,
                "label": None,
            },
            {
                "source": _n_category,
                "target": _n_content_list,
                "label": None,
            },
            {
                "source": _n_content_list,
                "target": _n_select,
                "label": None,
            },
            {
                "source": _n_select,
                "target": _n_delivery,
                "label": None,
            },
            {
                "source": _n_delivery,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}

# ============================================================================
# 20. Warranty & Support Registration
# ============================================================================
_n_start = _gen_node_id()
_n_product = _gen_node_id()
_n_serial = _gen_node_id()
_n_owner_info = _gen_node_id()
_n_register = _gen_node_id()
_n_confirmation = _gen_node_id()
_n_end = _gen_node_id()

TEMPLATES["warranty-registration"] = {
    "id": "warranty-registration",
    "name": "Warranty & Support Registration",
    "description": "Register products for warranty and priority support",
    "category": "SUPPORT",
    "node_count": 7,
    "estimated_setup_minutes": 5,
    "flow_json": _build_flow(
        [
            {
                "id": _n_start,
                "type": "start",
                "label": "Start",
                "config": {"trigger": "user_message"},
                "position": {"x": 50, "y": 50},
            },
            {
                "id": _n_product,
                "type": "question",
                "label": "Product Model",
                "config": {"prompt": "What product are you registering?"},
                "position": {"x": 50, "y": 150},
            },
            {
                "id": _n_serial,
                "type": "question",
                "label": "Serial Number",
                "config": {"prompt": "What's the serial number? (on box or device)"},
                "position": {"x": 50, "y": 250},
            },
            {
                "id": _n_owner_info,
                "type": "form",
                "label": "Owner Information",
                "config": {
                    "fields": [
                        {
                            "name": "name",
                            "type": "text",
                            "label": "Full Name",
                            "required": True,
                        },
                        {
                            "name": "email",
                            "type": "email",
                            "label": "Email",
                            "required": True,
                        },
                        {
                            "name": "phone",
                            "type": "phone",
                            "label": "Phone",
                            "required": False,
                        },
                    ]
                },
                "position": {"x": 50, "y": 350},
            },
            {
                "id": _n_register,
                "type": "action",
                "label": "Register Product",
                "config": {
                    "action": "register_warranty",
                    "api_endpoint": "/warranty/register",
                },
                "position": {"x": 50, "y": 450},
            },
            {
                "id": _n_confirmation,
                "type": "message",
                "label": "Registration Complete",
                "config": {
                    "template": "Product registered! Your warranty ID: {{warranty_id}}"
                },
                "position": {"x": 50, "y": 550},
            },
            {
                "id": _n_end,
                "type": "end",
                "label": "End",
                "config": {},
                "position": {"x": 50, "y": 650},
            },
        ],
        [
            {
                "source": _n_start,
                "target": _n_product,
                "label": None,
            },
            {
                "source": _n_product,
                "target": _n_serial,
                "label": None,
            },
            {
                "source": _n_serial,
                "target": _n_owner_info,
                "label": None,
            },
            {
                "source": _n_owner_info,
                "target": _n_register,
                "label": None,
            },
            {
                "source": _n_register,
                "target": _n_confirmation,
                "label": None,
            },
            {
                "source": _n_confirmation,
                "target": _n_end,
                "label": None,
            },
        ],
    ),
}
