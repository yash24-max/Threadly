# Threadly — Billing & Subscription

> Sprint 3 · Stripe-powered subscription billing with free tier, Pro, Business, and Enterprise plans.
> Implementation: `threadly-core` billing module + `threadly-web` billing UI.

---

## Pricing Strategy

### Plan Overview

| Feature | Free | Pro | Business | Enterprise |
|---------|------|-----|----------|------------|
| **Price** | $0/mo | $29/mo | $99/mo | Custom |
| **Bots** | 1 | 5 | Unlimited | Unlimited |
| **Conversations/mo** | 500 | 5,000 | 50,000 | Custom |
| **Knowledge Bases** | 1 | 5 | Unlimited | Unlimited |
| **KB Storage** | 50 MB | 1 GB | 10 GB | Custom |
| **Team Members** | 1 | 5 | 25 | Unlimited |
| **Integrations** | 0 | 5 | 22 | All + custom |
| **A/B Testing** | No | Yes | Yes | Yes |
| **Analytics Export (CSV)** | No | Yes | Yes | Yes |
| **Funnel Analytics** | No | Yes | Yes | Yes |
| **Email Sequences** | No | Yes | Yes | Yes |
| **CRM Contacts** | No | Yes | Yes | Yes |
| **Bot Cloning** | No | Yes | Yes | Yes |
| **Subflows** | No | No | Yes | Yes |
| **Custom Domain** | No | No | Yes | Yes |
| **White-Label Widget** | No | No | Yes | Yes |
| **API Access** | No | No | Yes | Yes |
| **Inbound Webhooks** | No | No | Yes | Yes |
| **Priority Support** | No | No | Yes | Yes |
| **SLA** | None | None | 99.9% | 99.99% |
| **Support** | Community | Email | Priority email | Dedicated CSM |
| **SSO (SAML/OIDC)** | No | No | No | Yes |
| **Custom Integrations** | No | No | No | Yes |

---

## Plan Details

### Free Plan

**Target:** Indie developers, hobbyists, early evaluation.

**Hard limits (enforced in real time):**
- 1 bot only — `POST /v1/bots` returns HTTP 402 if bot count ≥ 1
- 500 conversations per rolling calendar month — tracked in `billing_usage` table; when exceeded, new conversations return HTTP 429 with `X-Threadly-Upgrade-Required: true`
- 1 KB with max 50 MB storage
- 1 team member (owner only)

**Feature flags disabled on Free:**
- `feature.ab_testing` = false
- `feature.analytics_export` = false
- `feature.crm` = false
- `feature.email_sequences` = false
- `feature.integrations` = false
- `feature.bot_cloning` = false

**No credit card required.** Stripe customer created on signup but no payment method collected until upgrade.

---

### Pro Plan — $29/month

**Stripe Product ID:** `prod_threadly_pro` *(placeholder — replace with live Stripe ID)*

**Stripe Price ID (monthly):** `price_threadly_pro_monthly` *(placeholder)*

**Stripe Price ID (annual, 20% discount = $278/yr):** `price_threadly_pro_annual` *(placeholder)*

**Target:** Small businesses, solopreneurs, early-stage startups.

**Hard limits:**
- 5 bots maximum
- 5,000 conversations per calendar month
- 5 KBs with max 1 GB total storage
- 5 team members

**Features unlocked:**
- A/B testing (2 variants per bot, traffic split)
- Analytics CSV export
- Funnel analytics and cohort view
- Email sequences (up to 5 active sequences)
- CRM contacts (lead management, tags, notes)
- Bot cloning (one-click duplicate)
- 5 integrations connectable (from the 22-integration library)

**Overage policy:** Conversations 5,001–6,000 billed at $0.01/conversation. Above 6,000, additional conversations blocked with upgrade prompt.

---

### Business Plan — $99/month

**Stripe Product ID:** `prod_threadly_business` *(placeholder)*

**Stripe Price ID (monthly):** `price_threadly_business_monthly` *(placeholder)*

**Stripe Price ID (annual, 20% discount = $950/yr):** `price_threadly_business_annual` *(placeholder)*

**Target:** Growing companies, customer success teams, mid-market.

**Hard limits:**
- Unlimited bots
- 50,000 conversations per calendar month
- Unlimited KBs with max 10 GB total storage
- 25 team members

**Features unlocked (all Pro +):**
- All 22 integrations
- Subflows / reusable flow blocks
- Custom domain for widget (`chat.yourdomain.com` CNAME)
- White-label widget (remove Threadly branding)
- Full REST API access with API keys
- Inbound webhook triggers (`POST /webhooks/trigger/{token}`)
- Cron / scheduled flow triggers
- Priority email support (< 4 hour response SLA)
- 99.9% uptime SLA

**Overage policy:** $0.008/conversation above 50,000, up to 60,000. Above 60,000, auto-scales to Enterprise pricing conversation.

---

### Enterprise Plan — Custom Pricing

**Target:** Large enterprises, regulated industries, agencies.

**Contact:** enterprise@threadly.dev

**Everything in Business +:**
- Unlimited team members
- Unlimited conversations (volume discount negotiated)
- Unlimited KB storage
- Custom integrations (professional services)
- SSO via SAML 2.0 or OIDC
- Dedicated CSM and Slack channel support
- 99.99% uptime SLA with financial compensation
- SOC 2 Type II audit report on request
- Data residency options (EU, US, APAC)
- On-premise deployment option
- Custom contract and invoicing

---

## Stripe Integration Architecture

### Stripe Products and Price Objects

```
Stripe Products:
  prod_threadly_pro        → Pro plan product
  prod_threadly_business   → Business plan product

Stripe Prices:
  price_threadly_pro_monthly      → $29/mo recurring
  price_threadly_pro_annual       → $278/yr recurring (saves $70)
  price_threadly_business_monthly → $99/mo recurring
  price_threadly_business_annual  → $950/yr recurring (saves $238)

Stripe Meters (for usage-based overage):
  meter_threadly_conversations    → tracks conversation count per org
```

### Stripe Webhook Events Handled

| Stripe Event | Handler | Action |
|-------------|---------|--------|
| `checkout.session.completed` | `StripeWebhookController` | Activate subscription, update org plan |
| `invoice.payment_succeeded` | `StripeWebhookController` | Reset monthly usage counters |
| `invoice.payment_failed` | `StripeWebhookController` | Set org to `PAYMENT_FAILED`, notify owner |
| `customer.subscription.updated` | `StripeWebhookController` | Update plan level in DB |
| `customer.subscription.deleted` | `StripeWebhookController` | Downgrade org to Free plan |
| `customer.subscription.trial_will_end` | `StripeWebhookController` | Send trial ending email (D-3, D-1) |

**Webhook secret:** `STRIPE_WEBHOOK_SECRET` env var — used to verify `Stripe-Signature` header with `StripeWebhookVerifier`.

### Checkout Flow

```
User clicks "Upgrade to Pro" on billing page
  → POST /v1/billing/create-checkout-session { plan: "PRO", interval: "MONTHLY" }
  → StripeService.createCheckoutSession() 
      → stripe.checkout.sessions.create({
          customer: org.stripeCustomerId,
          line_items: [{ price: "price_threadly_pro_monthly", quantity: 1 }],
          mode: "subscription",
          success_url: "https://app.threadly.dev/billing?upgraded=true",
          cancel_url: "https://app.threadly.dev/billing",
          metadata: { org_id: org.id }
        })
  → Returns { checkoutUrl: "https://checkout.stripe.com/..." }
  → Frontend redirects to Stripe Checkout
  → Stripe processes payment
  → Stripe fires checkout.session.completed webhook
  → StripeWebhookController activates subscription
```

### Customer Portal (Self-Service)

```
POST /v1/billing/create-portal-session
  → stripe.billingPortal.sessions.create({
      customer: org.stripeCustomerId,
      return_url: "https://app.threadly.dev/billing"
    })
  → Returns { portalUrl: "..." }
```

Allows customers to: update payment method, download invoices, cancel subscription, view usage.

---

## Usage Metering Strategy

### Conversation Counting

Every new conversation (when visitor first sends a message and a `Conversation` row is created) increments the org's monthly counter:

```java
// ConversationService.java
billingService.incrementConversationCount(orgId);
```

Counter stored in Redis for real-time checks:
```
key:   billing:usage:{orgId}:{YYYY-MM}
type:  INCR counter
ttl:   35 days (auto-expires after billing period)
```

Also persisted to `billing_usage` table (Flyway V8) for invoice reconciliation.

### Usage Check Before Conversation Start

```java
// FlowRuntime.java (before executing Start node)
UsageStatus status = billingService.checkConversationLimit(orgId);
if (status == UsageStatus.LIMIT_EXCEEDED && plan == FREE) {
    throw new ConversationLimitExceededException(orgId);
}
```

### Stripe Meter Reporting

At end of billing period, a scheduled job (`BillingMeterJob`) reports actual conversation counts to Stripe for overage billing:

```java
@Scheduled(cron = "0 0 1 * * ?")  // 1 AM daily
public void reportDailyUsage() {
    stripe.v2().billing().meterEvents().create(
        MeterEventCreateParams.builder()
            .setEventName("threadly_conversations")
            .setPayload(Map.of("stripe_customer_id", customerId, "value", count.toString()))
            .build()
    );
}
```

---

## Feature Flags by Plan

Feature flags are evaluated at request time by `PlanFeatureGate`:

```java
@Component
public class PlanFeatureGate {
    public boolean isEnabled(String orgId, Feature feature) {
        Plan plan = orgRepository.findPlanByOrgId(orgId);
        return feature.isAvailableOn(plan);
    }
}
```

Feature flags are enforced:
1. **API layer** — controller-level `@RequiresPlan(Plan.PRO)` annotation
2. **Frontend** — plan context from `GET /v1/billing/status` response; UI shows upgrade prompt for locked features

```java
public enum Feature {
    AB_TESTING(Plan.PRO),
    ANALYTICS_EXPORT(Plan.PRO),
    CRM(Plan.PRO),
    EMAIL_SEQUENCES(Plan.PRO),
    INTEGRATIONS(Plan.PRO),
    BOT_CLONING(Plan.PRO),
    SUBFLOWS(Plan.BUSINESS),
    CUSTOM_DOMAIN(Plan.BUSINESS),
    WHITE_LABEL(Plan.BUSINESS),
    API_ACCESS(Plan.BUSINESS),
    INBOUND_WEBHOOKS(Plan.BUSINESS),
    CRON_TRIGGERS(Plan.BUSINESS),
    SSO(Plan.ENTERPRISE);
    
    private final Plan minimumPlan;
}
```

---

## Database Schema — Billing Tables

### Flyway V8: `billing_subscriptions`

```sql
CREATE TABLE billing_subscriptions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID NOT NULL REFERENCES orgs(id),
    stripe_customer_id   TEXT NOT NULL,
    stripe_subscription_id TEXT,
    plan                 TEXT NOT NULL DEFAULT 'FREE',  -- FREE, PRO, BUSINESS, ENTERPRISE
    interval             TEXT,                          -- MONTHLY, ANNUAL
    status               TEXT NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAST_DUE, CANCELED, TRIALING
    current_period_start TIMESTAMPTZ,
    current_period_end   TIMESTAMPTZ,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    trial_end            TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX billing_subscriptions_org_idx ON billing_subscriptions(org_id);
CREATE INDEX billing_subscriptions_stripe_customer_idx ON billing_subscriptions(stripe_customer_id);
```

### Flyway V9: `billing_usage`

```sql
CREATE TABLE billing_usage (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL REFERENCES orgs(id),
    period          TEXT NOT NULL,   -- YYYY-MM format
    conversations   INTEGER NOT NULL DEFAULT 0,
    kb_storage_mb   INTEGER NOT NULL DEFAULT 0,
    ai_tokens_total BIGINT NOT NULL DEFAULT 0,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX billing_usage_org_period_idx ON billing_usage(org_id, period);
```

---

## API Endpoints — Billing

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/v1/billing/status` | JWT | Get current plan, usage, limits |
| `POST` | `/v1/billing/create-checkout-session` | JWT | Create Stripe Checkout session |
| `POST` | `/v1/billing/create-portal-session` | JWT | Create Stripe Customer Portal session |
| `GET` | `/v1/billing/invoices` | JWT | List past invoices |
| `POST` | `/v1/billing/stripe/webhook` | Stripe-Signature | Stripe webhook receiver (public, verified by signature) |

### GET /v1/billing/status — Response Schema

```json
{
  "plan": "PRO",
  "status": "ACTIVE",
  "interval": "MONTHLY",
  "currentPeriodEnd": "2026-06-24T00:00:00Z",
  "cancelAtPeriodEnd": false,
  "usage": {
    "conversations": { "used": 1243, "limit": 5000 },
    "bots": { "used": 3, "limit": 5 },
    "kbStorage_mb": { "used": 128, "limit": 1024 },
    "teamMembers": { "used": 2, "limit": 5 }
  },
  "features": {
    "abTesting": true,
    "analyticsExport": true,
    "crm": true,
    "emailSequences": true,
    "integrations": true,
    "botCloning": true,
    "subflows": false,
    "customDomain": false,
    "apiAccess": false,
    "inboundWebhooks": false
  }
}
```

---

## Billing UI Pages

### `/billing` — Subscription Management Page

Sections:
1. **Current Plan** — plan name, status badge, next billing date, cancel button
2. **Usage** — progress bars for conversations, bots, KB storage, team members
3. **Plan Cards** — Free / Pro / Business with feature comparison table; current plan highlighted; "Upgrade" CTA
4. **Invoices** — table of past invoices with download PDF links
5. **Payment Method** — masked card number, "Manage" button → Stripe Customer Portal

### Upgrade Prompt Modal

Shown when user tries to access a locked feature (A/B testing, CRM, integrations, etc.):

- Feature name and description
- Plan that unlocks it (Pro / Business)
- Benefits list (bullet points)
- "Upgrade to Pro — $29/mo" CTA button
- "Maybe later" dismiss

---

## Trial Period

- New signups receive a **14-day Pro trial** automatically
- No credit card required for trial
- Stripe customer created without payment method; subscription in `TRIALING` status
- Trial end: email notification at D-3 and D-1 (`BillingEmailService`)
- On trial expiration: downgrade to Free if no payment method added
- Trial activated via: `stripe.subscriptions.create({ trial_period_days: 14, ... })`

---

## Key Files (Sprint 3)

```
threadly-core/src/main/java/dev/threadly/core/
  billing/
    BillingController.java          ← REST endpoints
    BillingService.java             ← Plan management, checkout session
    StripeWebhookController.java    ← Stripe event handler
    StripeWebhookVerifier.java      ← Signature verification
    BillingMeterJob.java            ← @Scheduled daily usage report
    PlanFeatureGate.java            ← Feature flag evaluator
    Plan.java                       ← enum FREE, PRO, BUSINESS, ENTERPRISE
    Feature.java                    ← enum with minimum plan
    UsageService.java               ← Redis + DB usage tracking

threadly-core/src/main/resources/db/migration/
  V8__billing_subscriptions.sql
  V9__billing_usage.sql

threadly-web/app/(app)/billing/
  page.tsx                          ← Main billing page
  components/
    PlanCard.tsx                    ← Plan card with feature list
    UsageMeter.tsx                  ← Progress bar component
    InvoiceTable.tsx                ← Invoice list
    UpgradeModal.tsx                ← Feature-locked upgrade prompt
```

---

## Environment Variables (Billing)

```bash
# Stripe
STRIPE_SECRET_KEY=sk_live_...        # or sk_test_... for development
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Stripe Product/Price IDs
STRIPE_PRICE_PRO_MONTHLY=price_...
STRIPE_PRICE_PRO_ANNUAL=price_...
STRIPE_PRICE_BUSINESS_MONTHLY=price_...
STRIPE_PRICE_BUSINESS_ANNUAL=price_...
STRIPE_METER_CONVERSATIONS=mtr_...

# Local development: use Stripe CLI
# stripe listen --forward-to localhost:8080/v1/billing/stripe/webhook
```

---

## Docker Compose Additions (Sprint 3)

```yaml
# infra/docker-compose.yml additions

  # Stripe CLI (dev only — forwards Stripe webhooks to local)
  stripe-cli:
    image: stripe/stripe-cli:latest
    command: listen --api-key ${STRIPE_SECRET_KEY} --forward-to core:8080/v1/billing/stripe/webhook
    environment:
      STRIPE_API_KEY: ${STRIPE_SECRET_KEY}
    depends_on:
      - core
    profiles: ["dev"]
```
