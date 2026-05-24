package dev.threadly.flow.catalog;

import dev.threadly.common.dto.NodeCatalogEntryDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for managing node catalog.
 * Provides all available node types for flow builder.
 * Cached as singleton - loaded once, invalidated rarely.
 */
@Service
public class NodeCatalogService {

  private static final Logger logger = LoggerFactory.getLogger(NodeCatalogService.class);

  /** Cache key for node catalog */
  private static final String CACHE_KEY = "node-catalog";

  /**
   * Get all available node types for the flow builder.
   *
   * @return list of all 25 node types with metadata
   */
  @Cacheable(value = "catalog", key = "'" + CACHE_KEY + "'", unless = "#result == null")
  public List<NodeCatalogEntryDto> getNodeCatalog() {
    logger.info("Loading node catalog from service (not cached)");
    List<NodeCatalogEntryDto> catalog = new ArrayList<>();

    // 1. Messaging Nodes
    catalog.add(createNode(
        "message",
        "Send Message",
        "Send a message to the customer via multiple channels",
        "MessageSquare",
        "Messaging",
        "#3B82F6",
        Map.of(
            "channel", "email",
            "template", "",
            "subject", ""
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "sms",
        "Send SMS",
        "Send an SMS text message to the customer",
        "MessageCircle",
        "Messaging",
        "#10B981",
        Map.of(
            "message", "",
            "phoneField", "phone"
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "email",
        "Send Email",
        "Send an email message with template support",
        "Mail",
        "Messaging",
        "#8B5CF6",
        Map.of(
            "to", "",
            "subject", "",
            "template", "",
            "variables", Map.of()
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "notify",
        "Send Notification",
        "Send in-app push notification",
        "Bell",
        "Messaging",
        "#F59E0B",
        Map.of(
            "title", "",
            "body", "",
            "icon", ""
        ),
        true, true, null, null
    ));

    // 2. Condition/Logic Nodes
    catalog.add(createNode(
        "if_else",
        "Conditional",
        "Branch flow based on a condition",
        "GitBranch",
        "Logic",
        "#EF4444",
        Map.of(
            "condition", "",
            "trueLabel", "Yes",
            "falseLabel", "No"
        ),
        true, true,
        List.of("start", "message", "action"),
        List.of("message", "action", "end")
    ));

    catalog.add(createNode(
        "switch",
        "Switch Case",
        "Branch to multiple paths based on expression value",
        "GitCompare",
        "Logic",
        "#DC2626",
        Map.of(
            "expression", "",
            "cases", List.of()
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "filter",
        "Filter",
        "Filter items based on condition",
        "Filter",
        "Logic",
        "#F97316",
        Map.of(
            "condition", "",
            "filterMode", "include"
        ),
        true, true, null, null
    ));

    // 3. Action Nodes
    catalog.add(createNode(
        "http_request",
        "HTTP Request",
        "Make HTTP API call to external service",
        "Globe",
        "Integration",
        "#06B6D4",
        Map.of(
            "method", "GET",
            "url", "",
            "headers", Map.of(),
            "body", ""
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "webhook",
        "Webhook Call",
        "Call external webhook with flow data",
        "Link",
        "Integration",
        "#14B8A6",
        Map.of(
            "url", "",
            "method", "POST",
            "payload", Map.of()
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "action",
        "Custom Action",
        "Execute custom business logic via action handler",
        "Zap",
        "Integration",
        "#6366F1",
        Map.of(
            "actionType", "",
            "params", Map.of()
        ),
        true, true, null, null
    ));

    // 4. Data Nodes
    catalog.add(createNode(
        "variable",
        "Set Variable",
        "Store value in flow variable",
        "Variable",
        "Data",
        "#8B5CF6",
        Map.of(
            "name", "",
            "value", "",
            "scope", "flow"
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "extract",
        "Extract Data",
        "Extract data from JSON/XML using path expression",
        "FileJson",
        "Data",
        "#7C3AED",
        Map.of(
            "source", "",
            "path", "",
            "output", ""
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "transform",
        "Transform Data",
        "Transform data using templates or expressions",
        "Layers",
        "Data",
        "#A855F7",
        Map.of(
            "transformer", "template",
            "template", "",
            "output", ""
        ),
        true, true, null, null
    ));

    // 5. Control Flow Nodes
    catalog.add(createNode(
        "delay",
        "Delay",
        "Wait before continuing flow",
        "Clock",
        "Control",
        "#EC4899",
        Map.of(
            "duration", 1000,
            "unit", "milliseconds"
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "loop",
        "Loop",
        "Iterate over array items",
        "Repeat",
        "Control",
        "#DB2777",
        Map.of(
            "array", "",
            "itemVar", "item",
            "indexVar", "index"
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "wait_event",
        "Wait for Event",
        "Pause until specific event is received",
        "Timer",
        "Control",
        "#BE185D",
        Map.of(
            "eventType", "",
            "timeout", 3600000,
            "timeoutAction", "continue"
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "parallel",
        "Parallel Execute",
        "Run multiple paths simultaneously",
        "GitMerge",
        "Control",
        "#9D174D",
        Map.of(
            "paths", List.of()
        ),
        true, true, null, null
    ));

    // 6. Flow Control
    catalog.add(createNode(
        "start",
        "Start",
        "Entry point for flow execution",
        "PlayCircle",
        "Flow",
        "#059669",
        Map.of(),
        false, true, null, List.of("message", "action", "variable", "http_request")
    ));

    catalog.add(createNode(
        "end",
        "End",
        "Terminal node - flow completes",
        "StopCircle",
        "Flow",
        "#7F1D1D",
        Map.of(
            "status", "success",
            "outputData", Map.of()
        ),
        true, false,
        List.of("message", "action", "notify", "delay", "wait_event"),
        null
    ));

    // 7. Advanced Nodes
    catalog.add(createNode(
        "ai_agent",
        "AI Agent Call",
        "Invoke AI agent for intelligent processing",
        "Brain",
        "AI",
        "#6D28D9",
        Map.of(
            "agentId", "",
            "prompt", "",
            "context", Map.of()
        ),
        true, true, null, null
    ));

    catalog.add(createNode(
        "knowledge_lookup",
        "Knowledge Base Search",
        "Query organization knowledge base",
        "BookOpen",
        "Knowledge",
        "#7E22CE",
        Map.of(
            "query", "",
            "topK", 5,
            "threshold", 0.5
        ),
        true, true, null, null
    ));

    logger.info("Loaded {} node types in catalog", catalog.size());
    return catalog;
  }

  /**
   * Helper to create a node catalog entry.
   */
  private NodeCatalogEntryDto createNode(
      String type,
      String label,
      String description,
      String icon,
      String category,
      String color,
      Map<String, Object> defaultData,
      Boolean canHaveIncoming,
      Boolean canHaveOutgoing,
      java.util.List<String> allowedParents,
      java.util.List<String> allowedChildren) {
    return NodeCatalogEntryDto.builder()
        .type(type)
        .label(label)
        .description(description)
        .icon(icon)
        .category(category)
        .color(color)
        .defaultData(new HashMap<>(defaultData))
        .canHaveIncoming(canHaveIncoming)
        .canHaveOutgoing(canHaveOutgoing)
        .allowedParents(allowedParents)
        .allowedChildren(allowedChildren)
        .build();
  }
}
