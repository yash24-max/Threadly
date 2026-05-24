# Flow Builder Reference Guide

> Complete guide to building conversational flows in Threadly.

---

## Overview

The **Flow Builder** is the visual editor where you design your chatbot's conversation logic. You drag and drop **nodes** onto a canvas and connect them with **edges** to define how your bot should respond.

---

## Node Types

### Message Node

Send a static text message to the visitor.

**Configuration:**
- **Text:** The message content (supports emoji, markdown-style formatting)
- **Quick Replies:** Optional buttons for the visitor to choose from

**Example:**
```
"Hi! 👋 How can I help you today?"

[View Pricing] [Contact Sales] [Browse Docs]
```

**Use Case:** Welcome messages, confirmations, hand-off notifications

---

### Question Node

Ask the visitor a question and branch based on their answer.

**Configuration:**
- **Question Text:** The question to ask
- **Options:** Buttons the visitor can click
  - Each option can branch to a different node
  - Supports up to 10 options

**Example:**
```
Question: "What's your issue?"

[Pricing]  [Technical]  [Other]
   ↓           ↓            ↓
 Msg-1       Msg-2        Msg-3
```

**Use Case:** Qualifying leads, routing to different branches, gathering structured input

---

### AI Reply Node

Generate a dynamic response using AI, grounded in your knowledge base.

**Configuration:**
- **System Prompt:** Instructions for the AI (optional, defaults to helpful assistant)
- **Use Knowledge Base:** Toggle to search your documents (recommended)
- **Model:** Anthropic Claude (configurable in org settings)

**Example:**
```
System Prompt: "You are a friendly support agent. Answer questions using the knowledge base. If you don't know, offer to escalate."

Knowledge Base: ON
→ Searches your PDF documents automatically
→ Cites sources: [1], [2], etc.
```

**Use Case:** FAQ answering, product questions, customer support

---

### Collect Input Node

Ask the visitor for structured data (email, phone, custom fields).

**Configuration:**
- **Fields:**
  - Email (with validation)
  - Phone (with format check)
  - Name
  - Custom text/number fields
- **Validation:** Mark required fields
- **Success Message:** Message after input is collected

**Example:**
```
Collect Input:
  ☐ Email (required)
  ☐ Phone (optional)
  ☐ Company (required)
  ☐ Project size (optional)

Success: "Thanks! We'll follow up soon."
```

**Use Case:** Lead capture, contact forms, customer surveys

---

### Condition Node

Branch based on visitor data or session variables.

**Configuration:**
- **Variable:** What to check (visitor email, previous input, session var)
- **Operator:** equals, contains, greater than, etc.
- **Value:** What to compare against
- **Then/Else:** Where to go based on the result

**Example:**
```
IF visitor_email CONTAINS "company.com"
  THEN "You're a team member! Here's our internal docs"
ELSE "You're an external visitor. Welcome!"
```

**Use Case:** Personalized messages, role-based branching, dynamic flows

---

### Switch Node

Branch based on multiple conditions (like IF/ELSEIF/ELSE).

**Configuration:**
- Multiple conditions (up to 10)
- Fall-through to default if no conditions match

**Example:**
```
SWITCH question_answer:
  CASE "pricing" → Show pricing message
  CASE "technical" → Escalate to support
  CASE "other" → Offer to schedule demo
  DEFAULT → "How else can I help?"
```

**Use Case:** Complex branching, multiple pathways, state machines

---

### Delay Node

Pause the conversation for a specified time.

**Configuration:**
- **Duration:** Seconds, minutes, hours
- **Message (optional):** Show "thinking" indicator while waiting

**Example:**
```
"Checking your account... (waiting 5 seconds)"
```

**Use Case:** Simulation realism, rate limiting API calls, conversational pacing

---

### API Call Node

Call an external API and use the response in your flow.

**Configuration:**
- **URL:** The endpoint to call
- **Method:** GET, POST, PUT, DELETE
- **Headers:** Authorization, custom headers
- **Body:** JSON request payload (if POST/PUT)
- **Credentials:** Stored securely; not visible in flow
- **Output Variable:** Store response as `api_result`

**Example:**
```
POST https://api.crm.com/leads
Headers:
  Authorization: Bearer {{stripe_api_key}}
Body:
  {
    "email": "{{visitor_email}}",
    "name": "{{visitor_name}}"
  }

Output: api_result
→ Use {{api_result.lead_id}} in next message
```

**Use Case:** Creating leads, checking inventory, logging to your backend

---

### Send Email Node

Send an email to the visitor or your team.

**Configuration:**
- **To:** Recipient email (visitor email or static)
- **Subject:** Email subject
- **Body:** Email content (supports variables)
- **Attachments:** Optional files from knowledge base

**Example:**
```
To: {{visitor_email}}
Subject: "Your {{ product }} Demo Confirmation"
Body: "Your demo is scheduled for {{ demo_time }}..."
```

**Use Case:** Confirmations, follow-ups, lead notifications

---

### Handoff Node

Pause the AI and allow a human to take over.

**Configuration:**
- **Message to Visitor:** "Connecting you to an agent..."
- **Message to Agent:** "New conversation from {{visitor_email}}"
- **Assign to:** Queue or specific agent (optional)

**Example:**
```
"I'm connecting you to our support team. One moment..."
→ Agent sees conversation in their inbox
→ Agent can respond in real-time
→ Visitor can chat with agent
```

**Use Case:** Complex issues, sales conversations, human escalation

---

### Loop Node (ForEach)

Iterate over an array and execute nodes for each item.

**Configuration:**
- **Array Variable:** What to loop over (e.g., `items`)
- **Loop Body:** Nodes to repeat for each item
- **Current Item Variable:** `item` (accessible in loop)

**Example:**
```
Loop over {{cart_items}} (array of products):
  Send message: "{{item.name}} - ${{item.price}}"

Result: Visitor sees all products listed one by one
```

**Use Case:** Listing multiple results, batch processing, order summaries

---

### Subflow Node

Call a reusable flow from another flow.

**Configuration:**
- **Subflow:** Which subflow to call
- **Input Variables:** Pass data to subflow
- **Output Variables:** Capture results from subflow

**Example:**
```
Call Subflow: "Collect Contact Info"
  Input: visitor_email

Subflow executes (email, phone, name collection)

Output: contact_info
→ Use {{contact_info.phone}} in next message
```

**Use Case:** Reusable components, modular flows, avoiding duplication

---

### Error Handler Node

Catch errors from previous nodes and recover gracefully.

**Configuration:**
- **Upstream Node:** Which node to monitor
- **Error Type:** Specific error (e.g., API timeout, validation error)
- **Recovery Path:** What to do if error occurs

**Example:**
```
API Call Node (may fail)
  ↓
Error Handler:
  IF "timeout" → "Server busy. Try again in a moment."
  IF "invalid_data" → "Please check your input."
  ELSE → "Unexpected error. Escalating..."
```

**Use Case:** Robust error handling, graceful degradation, user experience

---

### Integration Node

Use a pre-built integration (Slack, HubSpot, Gmail, etc.).

**Configuration:**
- **Integration Type:** Choose from 20+ pre-built connectors
- **Action:** What to do (post message, create lead, send email, etc.)
- **Parameters:** Action-specific fields

**Example:**
```
Integration: Slack
Action: Post Message
Channel: #new-leads
Message: "New lead: {{visitor_name}} ({{visitor_email}})"
```

**Use Case:** Notify teams, create records, send notifications

---

### End Node

Terminate the conversation.

**Configuration:**
- **Message:** Optional goodbye message

**Example:**
```
"Thanks for chatting! Have a great day. 👋"
```

**Use Case:** Conversation completion, natural exit points

---

## Advanced Features

### Session Variables

Store data during a conversation and reuse it.

**Set a Variable:**
```
Use a "Set Variable" action (available in Message node)
var_name: "customer_tier"
value: "premium"
```

**Use a Variable:**
```
{{customer_tier}} → "premium" in your messages
{{visitor_email}} → Auto-populated from lead capture
{{api_result.plan}} → Data from API responses
```

### Knowledge Base Integration

Your AI Reply nodes automatically:
1. Search your knowledge base
2. Find relevant documents
3. Ground answers in sources
4. Cite sources: `[1]`, `[2]`, etc.

**To add documents:**
- Bot dashboard → Knowledge Base → Upload PDF/Word/TXT

### Autosave

Your flow is automatically saved as you work (no manual save needed).

### Version History

- Every publish creates a new version
- Live conversations use the published version
- Draft changes don't affect live bots
- Rollback anytime: Bot dashboard → Versions → Rollback

### Preview & Test

- **Preview:** Test your flow before publishing
- **Test Mode:** See how visitors experience it
- **Debug:** Check variable values, trace execution

---

## Best Practices

### 1. Start Simple

Begin with a basic flow:
```
Welcome → Question → AI Reply → End
```

Then add complexity as needed.

### 2. Use Clear Messages

- **Good:** "What's your email so we can follow up?"
- **Bad:** "Email?" (too vague)

### 3. Validate Input

Use Collect Input nodes with required fields:
- Email validation prevents bounces
- Phone validation ensures format

### 4. Handle Edge Cases

Add error handlers for API calls:
```
API Call (might fail)
  → Error Handler: "Let me try another way..."
```

### 5. Test Thoroughly

- Use Preview before publishing
- Test all branches (not just the happy path)
- Check variable substitution

### 6. Keep It Short

Visitors have short attention spans:
- Keep messages to 1–2 sentences
- Ask one question at a time
- Use buttons instead of long forms

### 7. Personalize When Possible

```
"Hi {{visitor_name}}! 👋 How can I help?"
vs.
"Hi! How can I help?"
```

---

## Common Patterns

### Pattern: Lead Qualification

```
Start
  ↓
Welcome: "Interested in a demo?"
  ↓
Question: "What's your company size?"
  Options: [Startup (1-10)] [Growth (11-100)] [Enterprise (100+)]
  ↓ ↓ ↓
  Msg   Msg   Special Agent Assignment
  ↓     ↓     ↓
Collect Input (email, name)
  ↓
Handoff to Sales
  ↓
End
```

### Pattern: FAQ with AI Fallback

```
Start
  ↓
Message: "What can I help with?"
  ↓
Question: "Choose a topic"
  Options: [Pricing] [Tech] [Other]
  ↓ ↓ ↓
Msg   Msg   AI Reply (for unknown Qs)
(static) (static) (grounded in KB)
  ↓ ↓ ↓
End
```

### Pattern: Data Collection & Integration

```
Start
  ↓
Collect Input (email, phone, company)
  ↓
API Call: Create lead in CRM
  ↓
Integration: Slack notification
  ↓
Message: "Thanks! We'll be in touch."
  ↓
End
```

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `N` | Add new node |
| `Del` / `Backspace` | Delete selected node |
| `Cmd-Z` / `Ctrl-Z` | Undo |
| `Cmd-C` / `Ctrl-C` | Copy node |
| `Cmd-V` / `Ctrl-V` | Paste node |
| `Cmd-S` / `Ctrl-S` | Save (auto-save happens anyway) |

---

## Troubleshooting

### Variables not showing

- Make sure you're using `{{variable_name}}` (double braces)
- Check that the variable was set earlier in the flow
- Test in Preview mode to debug

### Flow not saving

- Check your internet connection
- Look for error messages in the top-right
- Try refreshing the page

### Visitor can't see updates

- Did you click "Publish"? (Draft changes don't go live)
- Wait 1–2 minutes for the widget to update
- Refresh the website

### AI Reply not using knowledge base

- Did you upload documents to Knowledge Base?
- Make sure "Use Knowledge Base" is enabled
- Wait a few minutes for documents to be indexed
- Test with a specific term from your documents

---

## Next Steps

- **Learn Integrations:** Connect Slack, HubSpot, Gmail, etc.
- **Advanced Flows:** Build complex flows with loops and error handling
- **Analytics:** Track which questions visitors ask most
- **Customization:** Adjust bot appearance and messaging

**Happy building!** 🚀
