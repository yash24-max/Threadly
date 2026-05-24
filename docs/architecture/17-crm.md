# Threadly — CRM & Contact Model

> Sprint 3 · Contact/Lead management linked to conversations, with tags, pipeline stages, custom fields, and timeline view.
> The CRM is tenant-scoped — each org has its own isolated contact database.

---

## Overview

The Threadly CRM captures leads and contacts generated through chatbot conversations. Every time a widget visitor submits a lead capture form or the flow collects contact information (email, phone, name), a `Lead` record is created or updated.

The CRM module provides:
- **Leads table** — searchable, filterable list of all contacts
- **Contact profile** — full lead details with custom fields and tags
- **Contact timeline** — chronological view of all conversations linked to this contact
- **Pipeline view** — Kanban-style pipeline with status columns
- **Tag management** — flexible tagging system for segmentation
- **Notes** — agent-authored notes attached to a contact
- **Bulk operations** — bulk tag, bulk assign, bulk export

---

## Data Model

### Lead Entity

The `leads` table is the core CRM record. Each row represents one unique contact (deduplicated by email per org).

```sql
-- Flyway V10: leads table
CREATE TABLE leads (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID NOT NULL REFERENCES orgs(id),
    bot_id            UUID REFERENCES bots(id) ON DELETE SET NULL,
    conversation_id   UUID REFERENCES conversations(id) ON DELETE SET NULL,
    
    -- Identity
    email             TEXT,
    phone             TEXT,
    name              TEXT,
    
    -- Source & Status
    source            TEXT NOT NULL DEFAULT 'CHAT',  -- CHAT, IMPORT, API, FORM
    status            TEXT NOT NULL DEFAULT 'NEW',   -- NEW, CONTACTED, QUALIFIED, CONVERTED, LOST
    
    -- Pipeline
    pipeline_stage    TEXT NOT NULL DEFAULT 'INBOX', -- INBOX, IN_PROGRESS, QUALIFIED, PROPOSAL, CLOSED_WON, CLOSED_LOST
    pipeline_order    INTEGER NOT NULL DEFAULT 0,    -- drag-drop ordering within stage
    assigned_to       UUID REFERENCES users(id) ON DELETE SET NULL,
    
    -- Extended Data
    custom_fields     JSONB NOT NULL DEFAULT '{}',   -- org-defined custom field values
    tags              TEXT[] NOT NULL DEFAULT '{}',  -- array of tag names
    
    -- Metadata
    first_seen_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    -- Constraints
    CONSTRAINT leads_email_org_unique UNIQUE (email, org_id)
);

-- Indexes
CREATE INDEX leads_org_id_idx ON leads(org_id);
CREATE INDEX leads_org_status_idx ON leads(org_id, status);
CREATE INDEX leads_org_stage_idx ON leads(org_id, pipeline_stage);
CREATE INDEX leads_org_assigned_idx ON leads(org_id, assigned_to);
CREATE INDEX leads_email_idx ON leads(email);
CREATE INDEX leads_tags_idx ON leads USING GIN(tags);
CREATE INDEX leads_custom_fields_idx ON leads USING GIN(custom_fields);
CREATE INDEX leads_created_at_idx ON leads(created_at DESC);

-- Hibernate @Filter for tenant isolation
-- TenantFilterAspect applies: WHERE org_id = :currentOrgId
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key, auto-generated |
| `org_id` | UUID | Tenant isolation FK — all queries filtered by this |
| `bot_id` | UUID | Which bot originated this lead (nullable if imported) |
| `conversation_id` | UUID | Most recent or originating conversation link |
| `email` | TEXT | Email address (unique per org) |
| `phone` | TEXT | Phone number in E.164 format (`+15551234567`) |
| `name` | TEXT | Full name (first + last, or just name if only one provided) |
| `source` | TEXT enum | How the lead entered the CRM: `CHAT` (widget form), `IMPORT` (CSV), `API` (REST), `FORM` (dedicated embed form) |
| `status` | TEXT enum | Qualification status (see below) |
| `pipeline_stage` | TEXT enum | Current pipeline column (see below) |
| `pipeline_order` | INTEGER | Drag-and-drop order within the pipeline column |
| `assigned_to` | UUID | Agent user assigned to handle this lead |
| `custom_fields` | JSONB | Key-value map of org-defined custom field values |
| `tags` | TEXT[] | Array of tag names for segmentation |
| `first_seen_at` | TIMESTAMPTZ | When the lead was first captured |
| `last_seen_at` | TIMESTAMPTZ | Most recent conversation or interaction timestamp |

---

## Status Enum

```java
public enum LeadStatus {
    NEW,         // Just entered the CRM; no outreach yet
    CONTACTED,   // At least one conversation or email has occurred
    QUALIFIED,   // Agent marked as meeting ideal customer profile
    CONVERTED,   // Became a paying customer / took desired action
    LOST         // Disqualified or unsubscribed
}
```

**Status transitions:**

```
NEW → CONTACTED (auto: on first bot reply or agent reply)
    → QUALIFIED (manual: agent sets)
    → LOST      (manual: agent sets or unsubscribe event)

CONTACTED → QUALIFIED (manual)
          → LOST      (manual or GDPR delete request)

QUALIFIED → CONVERTED (manual: agent marks conversion)
          → LOST      (manual)
```

---

## Pipeline Stages

```java
public enum PipelineStage {
    INBOX,        // New/unreviewed leads
    IN_PROGRESS,  // Actively in conversation or follow-up
    QUALIFIED,    // Confirmed as good fit
    PROPOSAL,     // Quote/demo/proposal sent
    CLOSED_WON,   // Deal closed, customer converted
    CLOSED_LOST   // Lead lost
}
```

The pipeline is displayed as a **Kanban board** on the CRM contacts page. Each column is a stage. Leads can be dragged between columns (updates `pipeline_stage` and `pipeline_order`).

**Custom pipelines** (Business+ plan): Orgs can rename stages and add up to 3 custom stages via `pipeline_config` in the org settings.

---

## Tag Model

```sql
-- Flyway V10 (continued)
CREATE TABLE lead_tags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL REFERENCES orgs(id),
    name        TEXT NOT NULL,
    color       TEXT NOT NULL DEFAULT '#6B7280',  -- hex color for tag chip UI
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT lead_tags_name_org_unique UNIQUE (name, org_id)
);

CREATE INDEX lead_tags_org_idx ON lead_tags(org_id);
```

Tags are stored as `TEXT[]` on the `leads` table for performance (no join needed for list views). `lead_tags` stores the metadata (color, display name) for the org's tag library.

**Tag operations:**
- `POST /v1/crm/tags` — create a new tag with color
- `GET /v1/crm/tags` — list all org tags with colors
- `DELETE /v1/crm/tags/{name}` — delete tag (removes from all leads)
- Tags applied to leads: `PATCH /v1/crm/leads/{id}/tags` — `{ "add": ["hot-lead"], "remove": ["cold"] }`

---

## Notes Model

```sql
-- Flyway V10 (continued)
CREATE TABLE lead_notes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id     UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    org_id      UUID NOT NULL REFERENCES orgs(id),
    author_id   UUID NOT NULL REFERENCES users(id),
    body        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX lead_notes_lead_id_idx ON lead_notes(lead_id);
CREATE INDEX lead_notes_org_idx ON lead_notes(org_id);
```

**Note operations:**
- `POST /v1/crm/leads/{id}/notes` — add note (`{ "body": "..." }`)
- `GET /v1/crm/leads/{id}/notes` — list notes ordered by `created_at DESC`
- `PUT /v1/crm/leads/{id}/notes/{noteId}` — edit note (author only)
- `DELETE /v1/crm/leads/{id}/notes/{noteId}` — delete note (author or ADMIN)

---

## Custom Fields

Custom fields are defined at the org level and their values stored as JSONB on each lead.

```sql
-- Flyway V10 (continued)
CREATE TABLE custom_field_definitions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id       UUID NOT NULL REFERENCES orgs(id),
    key          TEXT NOT NULL,       -- machine key, e.g. "company_size"
    label        TEXT NOT NULL,       -- display label, e.g. "Company Size"
    field_type   TEXT NOT NULL,       -- TEXT, NUMBER, DATE, BOOLEAN, SELECT, MULTISELECT
    options      JSONB,               -- for SELECT/MULTISELECT: ["Small", "Medium", "Large"]
    required     BOOLEAN DEFAULT FALSE,
    position     INTEGER NOT NULL DEFAULT 0,  -- display order
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT custom_fields_key_org_unique UNIQUE (key, org_id)
);
```

**Field types:** `TEXT`, `NUMBER`, `DATE`, `BOOLEAN`, `SELECT` (single choice), `MULTISELECT` (array), `URL`

**Example custom_fields JSONB on a lead:**
```json
{
  "company_size": "51-200",
  "industry": "SaaS",
  "annual_revenue": 2500000,
  "is_decision_maker": true,
  "preferred_contact_time": "morning",
  "referral_source": "Product Hunt"
}
```

Custom field definitions are retrieved from `GET /v1/crm/custom-fields` and rendered dynamically in the contact profile form.

---

## Contact Timeline

The contact timeline shows a chronological history of all interactions with a lead:

```sql
-- Flyway V10 (continued)
CREATE TABLE lead_timeline_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id      UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    org_id       UUID NOT NULL REFERENCES orgs(id),
    event_type   TEXT NOT NULL,   -- see enum below
    title        TEXT NOT NULL,
    description  TEXT,
    metadata     JSONB NOT NULL DEFAULT '{}',
    actor_id     UUID REFERENCES users(id),  -- nullable for automated events
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX lead_timeline_lead_idx ON lead_timeline_events(lead_id, occurred_at DESC);
```

**Timeline Event Types:**

| `event_type` | Triggered By | Description |
|-------------|-------------|-------------|
| `LEAD_CREATED` | System | Lead first entered CRM |
| `CONVERSATION_STARTED` | System | New bot conversation began |
| `CONVERSATION_ENDED` | System | Conversation closed |
| `MESSAGE_RECEIVED` | System | Lead sent a message |
| `HANDOFF_REQUESTED` | System | Bot escalated to human |
| `AGENT_REPLIED` | Agent user | Agent sent a message in conversation |
| `STATUS_CHANGED` | Agent user | Lead status updated (e.g. NEW → QUALIFIED) |
| `STAGE_CHANGED` | Agent user | Pipeline stage changed |
| `TAG_ADDED` | Agent user / System | Tag applied to lead |
| `TAG_REMOVED` | Agent user | Tag removed from lead |
| `NOTE_ADDED` | Agent user | Note written on lead profile |
| `EMAIL_SENT` | Email sequence | Automated email sent to lead |
| `EMAIL_OPENED` | Email sequence | Lead opened an email (webhook from SendGrid) |
| `EMAIL_CLICKED` | Email sequence | Lead clicked a link in email |
| `ASSIGNED` | Agent user | Lead assigned to an agent |
| `CUSTOM_EVENT` | Integration | Custom event from an integration (e.g., Stripe payment received) |

**Timeline API:** `GET /v1/crm/leads/{id}/timeline?limit=50&cursor=...` — paginated, ordered by `occurred_at DESC`

---

## Conversation Linking

Multiple conversations can be linked to a single lead (same email address across different sessions):

```
Lead (id: abc-123, email: jane@acme.com)
  ├── Conversation (id: conv-1, bot: "Support Bot", started: 2026-05-01)
  ├── Conversation (id: conv-2, bot: "Sales Bot", started: 2026-05-10)
  └── Conversation (id: conv-3, bot: "Support Bot", started: 2026-05-20)
```

When a new conversation starts, `LeadService.linkOrCreateLead()` is called:
1. If a lead with the same `email` and `org_id` exists → update `last_seen_at`, link `conversation_id` to most recent conversation
2. If no matching lead → create new lead with `source=CHAT`, `status=NEW`
3. Add `CONVERSATION_STARTED` event to timeline
4. If lead capture form data is available (name, phone, custom fields) → upsert on the lead record

---

## Lead Capture via Widget

### Lead Capture Form (Widget)

When `leadCapture` is enabled in widget config, a pre-chat form is shown before the conversation starts:

```javascript
// Widget embed with lead capture
<script data-bot-id="..." 
        data-lead-capture="true"
        data-lead-fields='["name","email","phone"]'
        data-lead-required='["email"]'
        src="https://cdn.threadly.dev/widget.js">
</script>
```

Submitted form data is POSTed to `POST /v1/widget/lead-capture`:
```json
{
  "botId": "...",
  "name": "Jane Doe",
  "email": "jane@acme.com",
  "phone": "+15551234567"
}
```

Response includes a `visitorToken` and `sessionId` to start the conversation.

### Flow-Based Lead Collection

The existing `CollectInputNodeExecutor` can be configured to save collected values into the CRM:

```json
{
  "type": "collect_input",
  "data": {
    "prompt": "What's your email address?",
    "saveToVar": "visitor_email",
    "crmField": "email",    // <- Sprint 3: new field
    "saveToCrm": true       // <- Sprint 3: new flag
  }
}
```

When `saveToCrm: true`, the `CollectInputNodeExecutor` calls `LeadService.upsertLeadField()` after capturing the input.

---

## API Endpoints — CRM

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/v1/crm/leads` | JWT | List leads with filters, pagination, search |
| `POST` | `/v1/crm/leads` | JWT | Create lead manually |
| `GET` | `/v1/crm/leads/{id}` | JWT | Get lead details + custom fields |
| `PUT` | `/v1/crm/leads/{id}` | JWT | Update lead fields |
| `DELETE` | `/v1/crm/leads/{id}` | JWT | Delete lead (GDPR erasure) |
| `PATCH` | `/v1/crm/leads/{id}/status` | JWT | Update status |
| `PATCH` | `/v1/crm/leads/{id}/stage` | JWT | Update pipeline stage |
| `PATCH` | `/v1/crm/leads/{id}/assign` | JWT | Assign to team member |
| `PATCH` | `/v1/crm/leads/{id}/tags` | JWT | Add/remove tags |
| `GET` | `/v1/crm/leads/{id}/conversations` | JWT | List conversations linked to lead |
| `GET` | `/v1/crm/leads/{id}/timeline` | JWT | Get timeline events (paginated) |
| `GET` | `/v1/crm/leads/{id}/notes` | JWT | List notes |
| `POST` | `/v1/crm/leads/{id}/notes` | JWT | Add note |
| `PUT` | `/v1/crm/leads/{id}/notes/{noteId}` | JWT | Edit note |
| `DELETE` | `/v1/crm/leads/{id}/notes/{noteId}` | JWT | Delete note |
| `POST` | `/v1/crm/leads/bulk-tag` | JWT | Apply tags to multiple leads |
| `POST` | `/v1/crm/leads/bulk-assign` | JWT | Assign multiple leads to agent |
| `GET` | `/v1/crm/leads/export` | JWT | Export leads as CSV |
| `POST` | `/v1/crm/leads/import` | JWT | Import leads from CSV |
| `GET` | `/v1/crm/tags` | JWT | List org tags |
| `POST` | `/v1/crm/tags` | JWT | Create tag |
| `DELETE` | `/v1/crm/tags/{name}` | JWT | Delete tag |
| `GET` | `/v1/crm/custom-fields` | JWT | List custom field definitions |
| `POST` | `/v1/crm/custom-fields` | JWT | Create custom field definition |
| `PUT` | `/v1/crm/custom-fields/{id}` | JWT | Update custom field definition |
| `DELETE` | `/v1/crm/custom-fields/{id}` | JWT | Delete custom field definition |
| `POST` | `/v1/widget/lead-capture` | Public | Submit pre-chat lead form |

### GET /v1/crm/leads — Query Parameters

| Param | Type | Description |
|-------|------|-------------|
| `search` | string | Full-text search on name, email, phone |
| `status` | string | Filter by `LeadStatus` value |
| `stage` | string | Filter by `PipelineStage` value |
| `assigned_to` | UUID | Filter by assigned agent |
| `tags` | string (comma-separated) | Filter leads that have ALL specified tags |
| `bot_id` | UUID | Filter by originating bot |
| `source` | string | Filter by source |
| `created_after` | ISO date | Filter by creation date |
| `created_before` | ISO date | Filter by creation date |
| `sort` | string | `created_at`, `last_seen_at`, `name`, `status` |
| `order` | string | `asc` or `desc` |
| `page` | int | Page number (0-indexed) |
| `size` | int | Page size (default 25, max 100) |

---

## CRM Pages (Frontend)

### `/crm` — Contacts List Page

Layout: Full-width table with left filter sidebar

**Columns:** Name, Email, Phone, Status badge, Pipeline Stage, Tags, Bot, Assigned To, Last Seen, Created At

**Actions per row:** View profile, Quick assign, Quick tag

**Bulk actions:** Select all → tag, assign, export, delete

**Filter sidebar:** Status, Pipeline Stage, Tags (multi-select), Bot, Assigned To, Date range

**Search bar:** Debounced full-text search across name, email, phone

**Import/Export:** "Import CSV" button → modal with column mapping; "Export CSV" button → downloads filtered dataset

### `/crm/pipeline` — Kanban Pipeline View

**Columns:** INBOX / IN_PROGRESS / QUALIFIED / PROPOSAL / CLOSED_WON / CLOSED_LOST

Each column shows count badge and sum of `custom_fields.deal_value` if set.

Cards show: Name, email, tags, assigned avatar, last seen relative time.

Drag-and-drop between columns calls `PATCH /v1/crm/leads/{id}/stage`.

### `/crm/leads/[id]` — Contact Profile Page

Layout: 2-column (profile details left, timeline right)

**Left column:**
- Avatar / initials + name + email + phone
- Status dropdown (in-place edit)
- Pipeline stage dropdown
- Assigned agent picker
- Tags (add/remove chips)
- Standard fields: email, phone, name, source, bot
- Custom fields section (rendered from org's custom field definitions)
- Notes section (list + add note textarea)

**Right column (Timeline):**
- Vertical timeline with event icons and timestamps
- Event types: conversation cards (expandable), status changes, tag events, email events, agent actions
- "View Conversation" link on conversation events → opens full transcript in `/conversations`

---

## GDPR & Data Erasure

`DELETE /v1/crm/leads/{id}` performs a full erasure:
1. Anonymizes the lead record: name → `[deleted]`, email → null, phone → null, custom_fields → `{}`
2. Sets `status = 'LOST'`
3. Adds `GDPR_ERASURE` timeline event
4. Does NOT delete the `conversations` or `messages` records (conversation transcripts are anonymized separately by `ConversationAnonymizationService`)
5. Removes lead from any active email sequences

Hard-delete available for ENTERPRISE plan via `DELETE /v1/crm/leads/{id}?hard_delete=true`.

---

## Key Files (Sprint 3)

```
threadly-core/src/main/java/dev/threadly/core/
  crm/
    LeadController.java              ← REST endpoints
    LeadService.java                 ← Business logic, upsert, link conversations
    TagController.java               ← Tag CRUD
    CustomFieldController.java       ← Custom field definition CRUD
    NoteController.java              ← Notes CRUD
    TimelineService.java             ← Timeline event recording
    LeadCaptureController.java       ← Public widget lead capture endpoint
    Lead.java                        ← @Entity
    LeadNote.java                    ← @Entity
    LeadTimelineEvent.java           ← @Entity
    LeadTag.java                     ← @Entity
    CustomFieldDefinition.java       ← @Entity
    LeadRepository.java              ← Spring Data JPA
    LeadStatus.java                  ← enum
    PipelineStage.java               ← enum

threadly-core/src/main/resources/db/migration/
  V10__crm.sql

threadly-web/app/(app)/crm/
  page.tsx                           ← Contacts list
  pipeline/page.tsx                  ← Kanban pipeline view
  leads/[id]/page.tsx               ← Contact profile + timeline
  components/
    LeadsTable.tsx
    PipelineBoard.tsx
    LeadCard.tsx
    ContactProfile.tsx
    Timeline.tsx
    TimelineEvent.tsx
    TagChip.tsx
    AddNoteForm.tsx
    CustomFieldsForm.tsx
    LeadImportModal.tsx
```
