package dev.threadly.workspace.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * A single node in a flow.
 */
public record FlowNodeDto(
    @JsonProperty("id") String id,
    @JsonProperty("type") String type,
    @JsonProperty("position") Map<String, Integer> position, // { x, y }
    @JsonProperty("data") Map<String, Object> data) {}
