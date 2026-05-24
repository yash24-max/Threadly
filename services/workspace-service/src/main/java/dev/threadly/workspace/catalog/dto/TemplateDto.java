package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a bot template.
 * Users can instantiate a new bot from a template.
 */
public record TemplateDto(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("category") String category, // "Support", "LeadGen", "Ecommerce", etc.
    @JsonProperty("nodeCount") Integer nodeCount,
    @JsonProperty("avatar") String avatar, // emoji or icon
    @JsonProperty("definition") FlowDefinitionDto definition,
    @JsonProperty("isCustom") Boolean isCustom, // true if org-created, false if built-in
    @JsonProperty("createdAt") String createdAt) // ISO 8601 for custom templates
{}
