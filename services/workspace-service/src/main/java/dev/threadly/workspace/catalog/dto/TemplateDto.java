package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

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

/**
 * Flow definition structure (embedded in template).
 */
public record FlowDefinitionDto(
    @JsonProperty("nodes") List<FlowNodeDto> nodes,
    @JsonProperty("edges") List<FlowEdgeDto> edges) {}

/**
 * A single node in a flow.
 */
public record FlowNodeDto(
    @JsonProperty("id") String id,
    @JsonProperty("type") String type,
    @JsonProperty("position") Map<String, Integer> position, // { x, y }
    @JsonProperty("data") Map<String, Object> data) {}

/**
 * A connection between two nodes.
 */
public record FlowEdgeDto(
    @JsonProperty("id") String id,
    @JsonProperty("source") String source,
    @JsonProperty("target") String target,
    @JsonProperty("sourceHandle") String sourceHandle) {}
