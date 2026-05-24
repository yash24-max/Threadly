package dev.threadly.workspace.catalog;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import dev.threadly.workspace.catalog.dto.NodeCatalogEntryDto;
import dev.threadly.workspace.catalog.dto.TemplateDto;
import dev.threadly.workspace.catalog.dto.IntegrationDto;

/**
 * Service providing the node catalog, templates, and integrations.
 * All methods are cached (24h) to avoid repeated computation.
 */
@Service
public class CatalogService {

  @Cacheable("node-catalog")
  public List<NodeCatalogEntryDto> getNodeCatalog() {
    // 25 node types organized by category
    return List.of(
        // === MESSAGING (Blue #3B82F6) ===
        new NodeCatalogEntryDto(
            "message",
            "Send Message",
            "Send text to the user",
            "MessageSquare",
            "Messaging",
            "#3B82F6",
            Map.of("content", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "question",
            "Ask Question",
            "Ask and capture user reply",
            "HelpCircle",
            "Messaging",
            "#3B82F6",
            Map.of("content", "", "variable", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "collect_input",
            "Collect Input",
            "Validate and collect user input",
            "FormInput",
            "Messaging",
            "#3B82F6",
            Map.of("variable", "", "validation", "none", "prompt", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "send_email",
            "Send Email",
            "Send email to recipient",
            "Mail",
            "Messaging",
            "#3B82F6",
            Map.of("to", "", "subject", "", "body", ""),
            1,
            1),
        // === LOGIC (Purple #8B5CF6) ===
        new NodeCatalogEntryDto(
            "condition",
            "Condition",
            "Branch on variable value",
            "GitBranch",
            "Logic",
            "#8B5CF6",
            Map.of("conditions", List.of(), "logicalOperator", "AND"),
            1,
            2),
        new NodeCatalogEntryDto(
            "switch",
            "Switch",
            "Multi-branch on variable",
            "Shuffle",
            "Logic",
            "#8B5CF6",
            Map.of("variable", "", "cases", List.of()),
            1,
            3),
        new NodeCatalogEntryDto(
            "set_variable",
            "Set Variable",
            "Set session variables",
            "Variable",
            "Logic",
            "#8B5CF6",
            Map.of("assignments", List.of()),
            1,
            1),
        new NodeCatalogEntryDto(
            "delay",
            "Delay",
            "Wait before continuing",
            "Clock",
            "Logic",
            "#8B5CF6",
            Map.of("seconds", 5),
            1,
            1),
        new NodeCatalogEntryDto(
            "loop",
            "Loop",
            "Repeat action N times",
            "Repeat2",
            "Logic",
            "#8B5CF6",
            Map.of("iterations", 5, "variable", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "subflow",
            "Subflow",
            "Call another workflow",
            "Package",
            "Logic",
            "#8B5CF6",
            Map.of("flowId", "", "parameters", Map.of()),
            1,
            1),
        // === AI (Green #10B981) ===
        new NodeCatalogEntryDto(
            "ai_reply",
            "AI Reply",
            "LLM response with RAG",
            "Sparkles",
            "AI",
            "#10B981",
            Map.of(
                "systemPrompt", "",
                "maxTokens", 1024,
                "temperature", 0.7,
                "useKnowledgeBase", true),
            1,
            1),
        new NodeCatalogEntryDto(
            "classify_intent",
            "Classify Intent",
            "Detect user intent",
            "Zap",
            "AI",
            "#10B981",
            Map.of("intents", List.of(), "model", "gpt-4"),
            1,
            3),
        new NodeCatalogEntryDto(
            "extract_entities",
            "Extract Entities",
            "Extract info from text",
            "Target",
            "AI",
            "#10B981",
            Map.of("entities", List.of(), "outputVariable", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "sentiment_analysis",
            "Sentiment Analysis",
            "Analyze emotion/sentiment",
            "Smile",
            "AI",
            "#10B981",
            Map.of("outputVariable", "sentiment", "threshold", 0.5),
            1,
            1),
        // === INTEGRATION (Amber #F59E0B) ===
        new NodeCatalogEntryDto(
            "api_call",
            "HTTP Request",
            "Call external APIs",
            "Globe",
            "Integration",
            "#F59E0B",
            Map.of("method", "GET", "url", "", "headers", List.of(), "body", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "slack",
            "Slack",
            "Send Slack message",
            "Send",
            "Integration",
            "#F59E0B",
            Map.of("channel", "", "message", "", "threadTs", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "hubspot",
            "HubSpot",
            "Create/update HubSpot contact",
            "Database",
            "Integration",
            "#F59E0B",
            Map.of("action", "create", "properties", Map.of()),
            1,
            1),
        new NodeCatalogEntryDto(
            "google_sheets",
            "Google Sheets",
            "Append row to sheet",
            "Sheet",
            "Integration",
            "#F59E0B",
            Map.of("spreadsheetId", "", "sheetName", "", "values", List.of()),
            1,
            1),
        new NodeCatalogEntryDto(
            "twilio",
            "Twilio SMS",
            "Send SMS via Twilio",
            "MessageCircle",
            "Integration",
            "#F59E0B",
            Map.of("to", "", "message", ""),
            1,
            1),
        new NodeCatalogEntryDto(
            "notion",
            "Notion",
            "Create Notion page/database entry",
            "FileText",
            "Integration",
            "#F59E0B",
            Map.of("databaseId", "", "properties", Map.of()),
            1,
            1),
        // === FLOW CONTROL (Red #EF4444) ===
        new NodeCatalogEntryDto(
            "handoff",
            "Human Handoff",
            "Transfer to human agent",
            "UserCheck",
            "Flow Control",
            "#EF4444",
            Map.of("message", "Connecting you to a human agent..."),
            1,
            1),
        new NodeCatalogEntryDto(
            "end",
            "End Flow",
            "End the conversation",
            "StopCircle",
            "Flow Control",
            "#EF4444",
            Map.of("message", ""),
            1,
            0),
        new NodeCatalogEntryDto(
            "cron_trigger",
            "Cron Trigger",
            "Schedule workflow on interval",
            "Timer",
            "Flow Control",
            "#EF4444",
            Map.of("cronExpression", "0 9 * * *", "timezone", "UTC"),
            0,
            1),
        new NodeCatalogEntryDto(
            "webhook_trigger",
            "Webhook Trigger",
            "Trigger on webhook call",
            "Webhook",
            "Flow Control",
            "#EF4444",
            Map.of("path", "", "method", "POST"),
            0,
            1),
        new NodeCatalogEntryDto(
            "error_branch",
            "Error Handler",
            "Handle errors in flow",
            "AlertTriangle",
            "Flow Control",
            "#EF4444",
            Map.of("errorTypes", List.of()),
            1,
            2));
  }

  @Cacheable("templates")
  public List<TemplateDto> getTemplates() {
    // Return hardcoded templates + any custom templates from org
    // For MVP, return the 20 core templates
    // TODO: In Phase 1, fetch org-specific templates from database
    return List.of(
        this.buildTemplate(
            "customer-support",
            "Customer Support",
            "Handle common support questions with escalation to humans",
            "Support",
            "🎧"),
        this.buildTemplate(
            "lead-qualification",
            "Lead Qualification",
            "Automatically qualify leads and score them",
            "LeadGen",
            "📊"),
        this.buildTemplate(
            "ecommerce-order-status",
            "E-commerce Order Status",
            "Track and update customers on their order status",
            "Ecommerce",
            "📦"),
        this.buildTemplate(
            "healthcare-appointment",
            "Healthcare Appointment",
            "Schedule and confirm healthcare appointments",
            "Healthcare",
            "🏥"),
        this.buildTemplate(
            "real-estate-inquiry",
            "Real Estate Inquiry",
            "Qualify real estate leads and schedule viewings",
            "RealEstate",
            "🏠"),
        this.buildTemplate(
            "job-screener",
            "Job Screener",
            "Screen job applicants with automated questions",
            "Education",
            "💼"),
        this.buildTemplate(
            "restaurant-reservation",
            "Restaurant Reservation",
            "Manage restaurant reservations and check availability",
            "Ecommerce",
            "🍽️"),
        this.buildTemplate(
            "product-recommendation",
            "Product Recommendation",
            "Recommend products based on user preferences",
            "Ecommerce",
            "🛍️"),
        this.buildTemplate(
            "employee-onboarding",
            "Employee Onboarding",
            "Streamline new employee onboarding process",
            "HR",
            "👨‍💼"),
        this.buildTemplate(
            "event-registration",
            "Event Registration",
            "Register attendees for events and send confirmations",
            "Support",
            "🎫"));
  }

  private TemplateDto buildTemplate(
      String id, String name, String description, String category, String avatar) {
    // For MVP, return stub definition. Phase 1 loads from DB.
    return new TemplateDto(id, name, description, category, 0, avatar, null, false, null);
  }

  @Cacheable("integrations")
  public List<IntegrationDto> getIntegrations() {
    return List.of(
        new IntegrationDto(
            "slack",
            "Slack",
            "Send messages to Slack channels",
            "Messaging",
            "Send",
            "#E01E5A",
            false,
            "slack",
            "oauth",
            List.of("chat:write", "channels:read"),
            "/integrations/configure/slack",
            "https://docs.threadly.dev/integrations/slack"),
        new IntegrationDto(
            "hubspot",
            "HubSpot",
            "Sync contacts and create deals",
            "CRM",
            "Database",
            "#FF5700",
            false,
            "hubspot",
            "oauth",
            List.of("crm.objects.contacts.write", "crm.objects.deals.write"),
            "/integrations/configure/hubspot",
            "https://docs.threadly.dev/integrations/hubspot"),
        new IntegrationDto(
            "google_sheets",
            "Google Sheets",
            "Append responses to spreadsheets",
            "Productivity",
            "Sheet",
            "#34A853",
            false,
            "google_sheets",
            "oauth",
            List.of("spreadsheets", "drive"),
            "/integrations/configure/google-sheets",
            "https://docs.threadly.dev/integrations/google-sheets"),
        new IntegrationDto(
            "twilio",
            "Twilio",
            "Send SMS and voice calls",
            "Messaging",
            "MessageCircle",
            "#F22F46",
            false,
            "twilio",
            "api_key",
            List.of("sms:send", "voice:calls"),
            "/integrations/configure/twilio",
            "https://docs.threadly.dev/integrations/twilio"),
        new IntegrationDto(
            "notion",
            "Notion",
            "Create Notion database entries",
            "Productivity",
            "FileText",
            "#000000",
            false,
            "notion",
            "oauth",
            List.of("content:read", "content:write"),
            "/integrations/configure/notion",
            "https://docs.threadly.dev/integrations/notion"),
        new IntegrationDto(
            "stripe",
            "Stripe",
            "Process payments and manage subscriptions",
            "Finance",
            "CreditCard",
            "#635BFF",
            false,
            "stripe",
            "api_key",
            List.of("write:charges", "read:customers"),
            "/integrations/configure/stripe",
            "https://docs.threadly.dev/integrations/stripe"),
        new IntegrationDto(
            "zapier",
            "Zapier",
            "Connect to 5000+ apps",
            "Automation",
            "Zap",
            "#FF4F00",
            false,
            "zapier",
            "api_key",
            List.of(),
            "/integrations/configure/zapier",
            "https://docs.threadly.dev/integrations/zapier"),
        new IntegrationDto(
            "make",
            "Make",
            "Automate workflows with Make",
            "Automation",
            "Workflow",
            "#7B68EE",
            false,
            "make",
            "api_key",
            List.of(),
            "/integrations/configure/make",
            "https://docs.threadly.dev/integrations/make"));
  }

  public List<IntegrationDto> searchIntegrations(String query) {
    String lowerQuery = query.toLowerCase();
    return getIntegrations().stream()
        .filter(
            i ->
                i.name().toLowerCase().contains(lowerQuery)
                    || i.description().toLowerCase().contains(lowerQuery)
                    || i.category().toLowerCase().contains(lowerQuery))
        .collect(Collectors.toList());
  }
}
