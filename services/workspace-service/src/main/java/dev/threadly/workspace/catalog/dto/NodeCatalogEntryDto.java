package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO for a node type in the flow builder catalog.
 * Describes what a node is, how to render it, and default values.
 */
public record NodeCatalogEntryDto(
    @JsonProperty("type") String type,
    @JsonProperty("label") String label,
    @JsonProperty("description") String description,
    @JsonProperty("icon") String icon, // Lucide icon name: "MessageSquare", "GitBranch", etc.
    @JsonProperty("category") String category, // "Messaging", "Logic", "AI", "Integration", "Flow Control"
    @JsonProperty("color") String color, // Hex color for node header: "#3B82F6"
    @JsonProperty("defaultData") Map<String, Object> defaultData,
    @JsonProperty("inputs") Integer inputs, // Expected input handle count (typically 1)
    @JsonProperty("outputs") Integer outputs) // Expected output handle count (0-3)
{}
