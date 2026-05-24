package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for an available integration.
 * Describes third-party services that can be used in flows.
 */
public record IntegrationDto(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("category") String category, // "CRM", "Messaging", "Analytics", "Productivity", etc.
    @JsonProperty("icon") String icon, // Lucide icon name or URL
    @JsonProperty("color") String color, // Brand color hex: "#F59E0B"
    @JsonProperty("isConnected") Boolean isConnected, // Has this org connected it?
    @JsonProperty("nodeType") String nodeType, // Internal flow builder node type (e.g., "hubspot")
    @JsonProperty("authType") String authType, // "oauth", "api_key", "none"
    @JsonProperty("scopes") List<String> scopes, // OAuth scopes or API permissions
    @JsonProperty("connectUrl") String connectUrl, // URL to configure this integration
    @JsonProperty("docUrl") String docUrl) // Link to integration docs
{}
