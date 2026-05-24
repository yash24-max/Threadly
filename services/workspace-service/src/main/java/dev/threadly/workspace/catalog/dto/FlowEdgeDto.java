package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A connection between two nodes.
 */
public record FlowEdgeDto(
    @JsonProperty("id") String id,
    @JsonProperty("source") String source,
    @JsonProperty("target") String target,
    @JsonProperty("sourceHandle") String sourceHandle) {}
