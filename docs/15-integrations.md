# Threadly — Integration Library

> Sprint 3 · 20+ native connectors covering communication, CRM, productivity, e-commerce, and developer tools.
> Each integration registers as a credential-backed node executor callable from the flow canvas.

---

## Integration Framework Overview

All integrations follow the **Integration Plugin Framework** introduced in Sprint 3:

```
IntegrationPlugin (interface)
  ├── getMetadata()   → IntegrationMeta (name, category, authType, actions, icon)
  ├── authenticate()  → CredentialHandle stored in CredentialsStore (AES-GCM)
  └── execute()       → IntegrationNodeExecutor.execute(context, action, params)
```

Integrations are registered via `IntegrationRegistry` (Spring bean) and discoverable in the **Integration Marketplace** page (`/integrations`). Each integration maps to one or more **node types** in the flow builder canvas.

**Credential storage:** All OAuth tokens and API keys are stored in the existing `credentials` table (V4 migration) with AES-256-GCM encryption. OAuth refresh tokens are rotated automatically by `OAuthRefreshScheduler`.

---

## Integration Catalog

### 1. Slack

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | OAuth 2.0 (workspace install) |
| **Icon** | `slack` (Lucide: `MessageSquare` with Slack brand overlay) |
| **Credential Key** | `slack_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_message` | Send a message to a channel or DM | `channel`, `text`, optional `blocks` (Block Kit JSON) |
| `send_dm` | Send a DM to a user by email or user ID | `user_email` or `user_id`, `text` |
| `create_channel` | Create a public or private channel | `name`, `is_private` |
| `invite_to_channel` | Invite user to a channel | `channel`, `user_email` |
| `get_user_by_email` | Look up a Slack user by email | `email` |
| `upload_file` | Upload a file to a channel | `channel`, `file_content`, `filename` |

**OAuth Scopes Required:** `channels:write`, `chat:write`, `users:read`, `users:read.email`, `files:write`, `im:write`

**Node Type:** `slack_action` — appears in **Actions** category in node catalog

---

### 2. Gmail

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | OAuth 2.0 (Google) |
| **Icon** | `mail` (Lucide) |
| **Credential Key** | `gmail_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_email` | Send an email from the authenticated account | `to`, `subject`, `body_html`, optional `cc`, `bcc`, `attachments` |
| `send_template_email` | Send using a saved email sequence template | `template_id`, `to`, `variables` (JSONB) |
| `get_thread` | Retrieve an email thread by ID | `thread_id` |
| `list_labels` | List all Gmail labels | — |
| `create_draft` | Create a draft email | `to`, `subject`, `body_html` |
| `add_label` | Apply a label to a message | `message_id`, `label_name` |

**OAuth Scopes Required:** `gmail.send`, `gmail.readonly`, `gmail.modify`

**Node Type:** `gmail_action` — appears in **Actions** category

---

### 3. HubSpot

| Field | Value |
|-------|-------|
| **Category** | CRM |
| **Auth Type** | OAuth 2.0 (HubSpot app) |
| **Icon** | `users` (Lucide) |
| **Credential Key** | `hubspot_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_contact` | Create a new contact in HubSpot | `email`, `firstname`, `lastname`, optional `phone`, `company` |
| `update_contact` | Update contact properties | `contact_id` or `email`, `properties` (key-value map) |
| `get_contact` | Retrieve a contact by email or ID | `email` or `contact_id` |
| `create_deal` | Create a deal in the pipeline | `dealname`, `amount`, `stage`, `contact_id` |
| `update_deal_stage` | Move a deal to a new pipeline stage | `deal_id`, `stage` |
| `add_note` | Add a note to a contact or deal | `object_type`, `object_id`, `note_body` |
| `create_ticket` | Create a support ticket | `subject`, `content`, `pipeline`, `status`, `contact_id` |
| `enroll_in_sequence` | Enroll contact in a HubSpot sequence | `contact_id`, `sequence_id` |

**OAuth Scopes Required:** `contacts`, `crm.objects.deals.write`, `crm.objects.companies.write`, `tickets`

**Node Type:** `hubspot_action` — appears in **CRM** category

---

### 4. Notion

| Field | Value |
|-------|-------|
| **Category** | Productivity |
| **Auth Type** | OAuth 2.0 (Notion integration) |
| **Icon** | `file-text` (Lucide) |
| **Credential Key** | `notion_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_page` | Create a new page in a database | `database_id`, `properties` (JSONB matching database schema) |
| `update_page` | Update page properties | `page_id`, `properties` |
| `get_page` | Retrieve a page and its blocks | `page_id` |
| `query_database` | Query a database with filters | `database_id`, `filter` (Notion filter object), optional `sorts` |
| `append_block` | Append block content to a page | `page_id`, `blocks` (Notion block array) |
| `search` | Search across workspace | `query`, optional `filter.object` (`page` or `database`) |

**OAuth Scopes Required:** `read_content`, `update_content`, `insert_content`, `read_databases`

**Node Type:** `notion_action` — appears in **Productivity** category

---

### 5. Google Sheets

| Field | Value |
|-------|-------|
| **Category** | Productivity |
| **Auth Type** | OAuth 2.0 (Google) |
| **Icon** | `table` (Lucide) |
| **Credential Key** | `google_sheets_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `append_row` | Append a row to a sheet | `spreadsheet_id`, `sheet_name`, `values` (array) |
| `update_row` | Update a specific row by row number | `spreadsheet_id`, `sheet_name`, `row_number`, `values` |
| `get_row` | Read a row by row number | `spreadsheet_id`, `sheet_name`, `row_number` |
| `find_row` | Find first row matching a column value | `spreadsheet_id`, `sheet_name`, `column`, `value` |
| `get_sheet_data` | Read all rows from a range | `spreadsheet_id`, `range` (A1 notation) |
| `create_spreadsheet` | Create a new spreadsheet | `title`, optional `sheet_names` |

**OAuth Scopes Required:** `spreadsheets`, `drive.file`

**Node Type:** `google_sheets_action` — appears in **Productivity** category

---

### 6. Airtable

| Field | Value |
|-------|-------|
| **Category** | Productivity |
| **Auth Type** | API Key (Personal Access Token) |
| **Icon** | `grid` (Lucide) |
| **Credential Key** | `airtable_api_key` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_record` | Create a record in a table | `base_id`, `table_name`, `fields` (JSONB) |
| `update_record` | Update a record | `base_id`, `table_name`, `record_id`, `fields` |
| `get_record` | Get a record by ID | `base_id`, `table_name`, `record_id` |
| `list_records` | List records with optional filter formula | `base_id`, `table_name`, optional `filter_formula`, `max_records` |
| `delete_record` | Delete a record | `base_id`, `table_name`, `record_id` |
| `search_records` | Search records by field value | `base_id`, `table_name`, `field_name`, `value` |

**Node Type:** `airtable_action` — appears in **Productivity** category

---

### 7. Twilio

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | API Key (Account SID + Auth Token) |
| **Icon** | `phone` (Lucide) |
| **Credential Key** | `twilio_account_sid`, `twilio_auth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_sms` | Send an SMS message | `to`, `from`, `body` |
| `send_whatsapp` | Send a WhatsApp message via Twilio | `to` (whatsapp: prefix), `from`, `body` |
| `make_call` | Initiate a phone call with TwiML | `to`, `from`, `twiml_url` |
| `lookup_number` | Validate and look up a phone number | `phone_number` |
| `send_mms` | Send an MMS with media | `to`, `from`, `body`, `media_url` |

**Node Type:** `twilio_action` — appears in **Communication** category

---

### 8. SendGrid

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | API Key |
| **Icon** | `send` (Lucide) |
| **Credential Key** | `sendgrid_api_key` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_email` | Send a transactional email | `to`, `from`, `subject`, `html_content`, optional `template_id`, `dynamic_template_data` |
| `send_bulk_email` | Send personalized bulk emails | `personalizations` array, `from`, `subject` |
| `add_to_list` | Add a contact to a SendGrid list | `email`, `list_id`, optional `custom_fields` |
| `remove_from_list` | Remove a contact from a list | `email`, `list_id` |
| `get_contact` | Look up a contact by email | `email` |
| `get_stats` | Get email stats for a date range | `start_date`, `end_date`, optional `categories` |

**Note:** SendGrid is the default SMTP provider for the Email Sequence Engine. Threadly's `EmailSequenceService` uses SendGrid's transactional API for scheduled step delivery.

**Node Type:** `sendgrid_action` — appears in **Communication** category

---

### 9. Mailchimp

| Field | Value |
|-------|-------|
| **Category** | Marketing |
| **Auth Type** | OAuth 2.0 (Mailchimp) |
| **Icon** | `mail` (Lucide) |
| **Credential Key** | `mailchimp_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `add_subscriber` | Add or update a subscriber | `list_id`, `email`, `status` (`subscribed`/`pending`), optional `merge_fields` |
| `update_subscriber` | Update subscriber merge fields | `list_id`, `email`, `merge_fields` |
| `tag_subscriber` | Add tags to a subscriber | `list_id`, `email`, `tags` (array) |
| `create_campaign` | Create an email campaign | `list_id`, `subject`, `from_name`, `reply_to`, `type` |
| `send_campaign` | Send a created campaign | `campaign_id` |
| `get_list_stats` | Get audience statistics | `list_id` |

**Node Type:** `mailchimp_action` — appears in **Marketing** category

---

### 10. Shopify

| Field | Value |
|-------|-------|
| **Category** | E-Commerce |
| **Auth Type** | OAuth 2.0 (Shopify app) |
| **Icon** | `shopping-bag` (Lucide) |
| **Credential Key** | `shopify_access_token`, `shopify_store_domain` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `get_order` | Retrieve an order by ID or number | `order_id` or `order_number` |
| `get_customer` | Look up a customer by email or ID | `email` or `customer_id` |
| `create_customer` | Create a new customer | `email`, `first_name`, `last_name`, optional `phone`, `tags` |
| `update_order` | Update order tags or notes | `order_id`, `tags` or `note` |
| `get_product` | Retrieve product details | `product_id` or `handle` |
| `search_orders` | Search orders by status or customer | `status`, optional `customer_id`, `created_at_min` |
| `apply_discount` | Apply a discount code to a draft order | `customer_id`, `discount_code` |
| `cancel_order` | Cancel an order | `order_id`, optional `reason`, `refund` |

**Node Type:** `shopify_action` — appears in **E-Commerce** category

---

### 11. Discord

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | Bot Token (Discord Developer Portal) |
| **Icon** | `message-circle` (Lucide) |
| **Credential Key** | `discord_bot_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_message` | Send a message to a channel | `channel_id`, `content`, optional `embeds` |
| `send_dm` | Send a DM to a user by Discord ID | `user_id`, `content` |
| `add_role` | Assign a role to a guild member | `guild_id`, `user_id`, `role_id` |
| `remove_role` | Remove a role from a guild member | `guild_id`, `user_id`, `role_id` |
| `create_thread` | Create a thread in a channel | `channel_id`, `name`, `message` |
| `kick_member` | Remove a member from a guild | `guild_id`, `user_id`, optional `reason` |

**Node Type:** `discord_action` — appears in **Communication** category

---

### 12. GitHub

| Field | Value |
|-------|-------|
| **Category** | Developer Tools |
| **Auth Type** | OAuth 2.0 (GitHub App) or Personal Access Token |
| **Icon** | `git-branch` (Lucide) |
| **Credential Key** | `github_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_issue` | Create a GitHub issue | `owner`, `repo`, `title`, `body`, optional `labels`, `assignees` |
| `create_comment` | Add a comment to an issue or PR | `owner`, `repo`, `issue_number`, `body` |
| `close_issue` | Close an issue | `owner`, `repo`, `issue_number` |
| `get_issue` | Retrieve issue details | `owner`, `repo`, `issue_number` |
| `create_pr` | Create a pull request | `owner`, `repo`, `title`, `head`, `base`, `body` |
| `trigger_workflow` | Trigger a GitHub Actions workflow | `owner`, `repo`, `workflow_id`, `ref`, optional `inputs` |
| `get_repo_info` | Get repository metadata | `owner`, `repo` |

**Node Type:** `github_action` — appears in **Developer Tools** category

---

### 13. Linear

| Field | Value |
|-------|-------|
| **Category** | Developer Tools |
| **Auth Type** | OAuth 2.0 (Linear app) or API Key |
| **Icon** | `layers` (Lucide) |
| **Credential Key** | `linear_api_key` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_issue` | Create a Linear issue | `team_id`, `title`, `description`, optional `priority`, `label_ids`, `assignee_id` |
| `update_issue` | Update issue state or assignee | `issue_id`, optional `state_id`, `assignee_id`, `priority` |
| `get_issue` | Get an issue by ID or identifier | `issue_id` or `identifier` (e.g., `ENG-123`) |
| `create_comment` | Add a comment to an issue | `issue_id`, `body` |
| `list_issues` | List issues for a team with filters | `team_id`, optional `state_name`, `assignee_id` |
| `get_teams` | List all teams in the workspace | — |

**Node Type:** `linear_action` — appears in **Developer Tools** category

---

### 14. Jira

| Field | Value |
|-------|-------|
| **Category** | Developer Tools |
| **Auth Type** | OAuth 2.0 (Jira Cloud) or API Token + email |
| **Icon** | `check-square` (Lucide) |
| **Credential Key** | `jira_api_token`, `jira_email`, `jira_domain` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_issue` | Create a Jira issue | `project_key`, `summary`, `issue_type`, optional `description`, `priority`, `assignee_email` |
| `update_issue` | Update issue fields | `issue_key`, `fields` (JSONB) |
| `transition_issue` | Move issue to a new status | `issue_key`, `transition_name` (e.g., `In Progress`, `Done`) |
| `add_comment` | Add a comment to an issue | `issue_key`, `body` |
| `get_issue` | Retrieve issue details | `issue_key` |
| `search_issues` | JQL-based issue search | `jql`, optional `max_results` |
| `assign_issue` | Assign an issue to a user | `issue_key`, `assignee_email` |

**Node Type:** `jira_action` — appears in **Developer Tools** category

---

### 15. Stripe

| Field | Value |
|-------|-------|
| **Category** | Payments |
| **Auth Type** | API Key (Stripe Secret Key) |
| **Icon** | `credit-card` (Lucide) |
| **Credential Key** | `stripe_secret_key` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `get_customer` | Retrieve a Stripe customer | `customer_id` or `email` |
| `create_customer` | Create a new Stripe customer | `email`, optional `name`, `phone`, `metadata` |
| `get_subscription` | Get a customer's subscription | `customer_id` or `subscription_id` |
| `cancel_subscription` | Cancel a subscription | `subscription_id`, optional `cancel_at_period_end` |
| `create_invoice` | Generate an invoice for a customer | `customer_id`, `items` array |
| `retrieve_charge` | Get charge details by ID | `charge_id` |
| `list_invoices` | List invoices for a customer | `customer_id`, optional `status`, `limit` |
| `create_payment_link` | Create a Stripe payment link | `price_id`, optional `quantity` |

**Note:** Stripe is also used internally for Threadly's billing module (see `docs/16-billing.md`). The `stripe_action` node uses a separate credential from the platform billing credential.

**Node Type:** `stripe_action` — appears in **Payments** category

---

### 16. Mixpanel

| Field | Value |
|-------|-------|
| **Category** | Analytics |
| **Auth Type** | API Secret (Service Account) |
| **Icon** | `bar-chart-2` (Lucide) |
| **Credential Key** | `mixpanel_project_token`, `mixpanel_service_account_secret` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `track_event` | Track a custom event | `distinct_id`, `event_name`, optional `properties` (JSONB) |
| `set_profile` | Create or update a user profile | `distinct_id`, `properties` (JSONB) |
| `increment_property` | Increment a numeric profile property | `distinct_id`, `property_name`, `increment_by` |
| `append_to_list` | Append a value to a list property | `distinct_id`, `property_name`, `value` |
| `query_insights` | Query a Mixpanel Insights report | `report_id` |

**Node Type:** `mixpanel_action` — appears in **Analytics** category

---

### 17. Segment

| Field | Value |
|-------|-------|
| **Category** | Analytics |
| **Auth Type** | API Key (Write Key) |
| **Icon** | `pie-chart` (Lucide) |
| **Credential Key** | `segment_write_key` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `track` | Track a user event | `user_id`, `event`, optional `properties` (JSONB), `anonymous_id` |
| `identify` | Identify a user with traits | `user_id`, `traits` (JSONB) |
| `page` | Log a page view | `user_id`, `name`, optional `properties` |
| `group` | Associate a user with a group | `user_id`, `group_id`, optional `traits` |
| `alias` | Alias an anonymous ID to a user ID | `user_id`, `previous_id` |

**Node Type:** `segment_action` — appears in **Analytics** category

---

### 18. Zapier Webhook

| Field | Value |
|-------|-------|
| **Category** | Automation |
| **Auth Type** | Webhook URL (no auth) |
| **Icon** | `zap` (Lucide) |
| **Credential Key** | `zapier_webhook_url` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `trigger_zap` | POST a payload to a Zapier Catch Hook | `webhook_url`, `payload` (JSONB) |
| `trigger_with_session_vars` | Send current session variables to a Zap | `webhook_url`, optional `extra_fields` |

**Node Type:** `zapier_action` — appears in **Automation** category

---

### 19. Microsoft Teams

| Field | Value |
|-------|-------|
| **Category** | Communication |
| **Auth Type** | OAuth 2.0 (Microsoft Azure AD) |
| **Icon** | `video` (Lucide) |
| **Credential Key** | `ms_teams_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `send_message` | Send a message to a Teams channel | `team_id`, `channel_id`, `content` (HTML or plain text) |
| `send_chat_message` | Send a message to a 1:1 or group chat | `chat_id`, `content` |
| `create_meeting` | Schedule a Teams meeting | `subject`, `start_datetime`, `end_datetime`, `attendees` (email array) |
| `get_user` | Look up a Teams user by email | `email` |
| `list_channels` | List channels in a team | `team_id` |

**OAuth Scopes Required (Microsoft Graph):** `ChannelMessage.Send`, `Chat.ReadWrite`, `OnlineMeetings.ReadWrite`, `User.Read.All`

**Node Type:** `teams_action` — appears in **Communication** category

---

### 20. Salesforce

| Field | Value |
|-------|-------|
| **Category** | CRM |
| **Auth Type** | OAuth 2.0 (Salesforce Connected App) |
| **Icon** | `briefcase` (Lucide) |
| **Credential Key** | `salesforce_oauth_token`, `salesforce_instance_url` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_lead` | Create a Salesforce Lead | `first_name`, `last_name`, `email`, optional `phone`, `company`, `lead_source` |
| `create_contact` | Create a Contact | `first_name`, `last_name`, `email`, optional `account_id`, `phone` |
| `create_opportunity` | Create a new Opportunity | `name`, `stage_name`, `close_date`, `account_id`, optional `amount` |
| `update_record` | Update any Salesforce sObject | `object_type`, `record_id`, `fields` (JSONB) |
| `get_record` | Get any sObject record by ID | `object_type`, `record_id` |
| `soql_query` | Execute a SOQL query | `soql` |
| `add_task` | Create a Task related to a record | `subject`, `related_to_id`, `related_to_type`, optional `due_date`, `description` |

**Node Type:** `salesforce_action` — appears in **CRM** category

---

### 21. Intercom (Bonus)

| Field | Value |
|-------|-------|
| **Category** | CRM |
| **Auth Type** | OAuth 2.0 (Intercom app) |
| **Icon** | `message-square` (Lucide) |
| **Credential Key** | `intercom_oauth_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_contact` | Create a lead or user in Intercom | `email`, optional `name`, `phone`, `custom_attributes` |
| `update_contact` | Update contact attributes | `contact_id` or `email`, `attributes` |
| `create_conversation` | Open a new conversation | `from_user_id`, `body` |
| `send_in_app_message` | Send an in-app message to a user | `user_id`, `body` |
| `add_tag` | Tag a contact | `contact_id`, `tag_name` |
| `get_contact` | Retrieve contact by email | `email` |

**Node Type:** `intercom_action` — appears in **CRM** category

---

### 22. Pipedrive (Bonus)

| Field | Value |
|-------|-------|
| **Category** | CRM |
| **Auth Type** | API Key or OAuth 2.0 |
| **Icon** | `trending-up` (Lucide) |
| **Credential Key** | `pipedrive_api_token` |

**Supported Actions:**

| Action | Description | Required Params |
|--------|-------------|-----------------|
| `create_person` | Create a person (contact) | `name`, optional `email`, `phone`, `org_id` |
| `create_deal` | Create a deal | `title`, `person_id`, optional `stage_id`, `value`, `currency` |
| `update_deal_stage` | Move deal to another stage | `deal_id`, `stage_id` |
| `add_activity` | Log an activity (call, email, task) | `subject`, `type`, `due_date`, optional `deal_id`, `person_id` |
| `get_deal` | Retrieve deal details | `deal_id` |
| `get_person` | Retrieve person by ID or email | `person_id` or `email` |

**Node Type:** `pipedrive_action` — appears in **CRM** category

---

## Integration Node in Flow Builder

All integration actions are executed through a single generic **Integration Node** with the following config fields:

```json
{
  "type": "integration",
  "data": {
    "label": "Send Slack Message",
    "integration": "slack",
    "action": "send_message",
    "credentialId": "{{credential:slack_oauth_token}}",
    "params": {
      "channel": "#support",
      "text": "New lead: {{session.lead_name}} — {{session.lead_email}}"
    },
    "outputVar": "slack_response",
    "onError": "continue" | "stop" | "branch"
  }
}
```

**Template engine:** All param values support `{{session.varName}}` substitution via `TemplateEngine`.

**Error handling:** Integration node supports the `onError` routing property introduced in Sprint 3 for error branching.

---

## Integration Marketplace Page

Path: `/integrations`

Features:
- Category filter tabs (All, Communication, CRM, Productivity, E-Commerce, Developer Tools, Analytics, Automation, Payments)
- Search by integration name
- Integration card: logo, name, category badge, auth type, number of actions, "Connect" button
- Connection status indicator (connected / disconnected / expired)
- OAuth flow: "Connect" → popup window → OAuth consent → token stored in CredentialsStore → card shows "Connected"

---

## Database: Integration Credentials

Stored in existing `credentials` table (V4 migration):

```sql
INSERT INTO credentials (id, org_id, name, type, encrypted_value, iv, created_at)
VALUES (gen_random_uuid(), :org_id, 'slack_oauth_token', 'OAUTH_TOKEN', :encrypted, :iv, now());
```

`type` enum: `API_KEY`, `OAUTH_TOKEN`, `BASIC_AUTH`, `WEBHOOK_URL`

OAuth tokens are stored as JSON: `{ "access_token": "...", "refresh_token": "...", "expires_at": 1234567890 }`

---

## Integration Plugin Architecture

```
threadly-core/src/main/java/dev/threadly/core/
  integration/
    IntegrationPlugin.java           ← interface
    IntegrationMeta.java             ← metadata record
    IntegrationRegistry.java         ← Spring bean, plugin map
    IntegrationNodeExecutor.java     ← NodeExecutor impl
    OAuthRefreshScheduler.java       ← @Scheduled token refresh
    plugins/
      SlackIntegration.java
      GmailIntegration.java
      HubSpotIntegration.java
      NotionIntegration.java
      GoogleSheetsIntegration.java
      AirtableIntegration.java
      TwilioIntegration.java
      SendGridIntegration.java
      MailchimpIntegration.java
      ShopifyIntegration.java
      DiscordIntegration.java
      GitHubIntegration.java
      LinearIntegration.java
      JiraIntegration.java
      StripeIntegration.java
      MixpanelIntegration.java
      SegmentIntegration.java
      ZapierWebhookIntegration.java
      TeamsIntegration.java
      SalesforceIntegration.java
      IntercomIntegration.java
      PipedriveIntegration.java
```

**Flyway V7** (Sprint 3): `integration_connections` table — stores per-org connection status, token expiry, last-used timestamp.
