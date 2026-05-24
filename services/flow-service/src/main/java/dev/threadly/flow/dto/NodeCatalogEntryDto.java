package dev.threadly.flow.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a node type in the flow node catalog.
 * Used by the node catalog service to describe available node types.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeCatalogEntryDto {

  private String type;
  private String label;
  private String description;
  private String icon;
  private String category;
  private String color;
  private Map<String, Object> defaultData;
  private Boolean canHaveIncoming;
  private Boolean canHaveOutgoing;
  private List<String> allowedParents;
  private List<String> allowedChildren;
}
