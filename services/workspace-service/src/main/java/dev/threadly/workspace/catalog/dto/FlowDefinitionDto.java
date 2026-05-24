package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Flow definition structure (embedded in template).
 */
public record FlowDefinitionDto(
    @JsonProperty("nodes") List<FlowNodeDto> nodes,
    @JsonProperty("edges") List<FlowEdgeDto> edges) {}
