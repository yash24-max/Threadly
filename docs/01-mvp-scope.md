# MVP Scope — Phase 0

## Goal
A business can sign up, build an AI chatbot in a visual flow builder, upload a knowledge base, embed the widget on their website, and see conversations + analytics in a dashboard.

## In scope

| # | Feature | Details |
|---|---|---|
| 1 | Auth & multi-tenancy | Sign up, login, organization, projects (bots) |
| 2 | Visual flow builder | Drag-drop nodes: Message, Question, Condition, AI Reply, API Call, Handoff, End |
| 3 | AI reply node | Calls LLM (Anthropic primary, OpenAI fallback), configurable system prompt + variables |
| 4 | Knowledge base + RAG | Upload PDF/TXT/URL, chunked + embedded, queried as context for AI |
| 5 | Conversation memory | Per-visitor session memory across turns, persisted in Postgres |
| 6 | Flow runtime engine | Interprets flow JSON node-by-node, manages session state in Redis |
| 7 | Embeddable widget | Single `<script>` snippet, customizable colors + avatar |
| 8 | Realtime transport | Centrifugo WebSocket — widget never connects to Spring Boot directly |
| 9 | Dashboard | Conversation list, transcripts, basic analytics |
| 10 | Human handoff | Take-over button, agent inbox, "Resume AI" |

## Out of scope (Phase 0)
- WhatsApp, Instagram, Facebook, Telegram, Email, SMS
- Full CRM (contacts, deals, pipelines)
- Workflow automation outside the chat flow
- Campaigns / broadcasts / sequences
- Billing / Stripe integration
- Vertical templates
- Multi-agent AI / agent marketplace

## Definition of "fully live" (when Phase 1 planning begins)
- Deployed to public Railway/Render URL
- At least 1 real external user has built a bot and embedded it
- No P0/P1 bugs open for 7 consecutive days
- Billing decision made (free beta vs. paid)
