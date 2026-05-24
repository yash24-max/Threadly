package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for node catalog entries.
 * Represents available node types in flow builder.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeCatalogEntryDto {

  /** Unique node type identifier (e.g., "message", "condition", "integration") */
  private String type;

  /** User-friendly label displayed in UI (e.g., "Send Message") */
  private String label;

  /** Detailed description of what this node does */
  private String description;

  /** Icon name for UI rendering (e.g., "MessageSquare", "GitBranch") */
  private String icon;

  /** Category for grouping in node palette (e.g., "Messaging", "Logic", "Integration") */
  private String category;

  /** Hex color code for node styling (e.g., "#3B82F6") */
  private String color;

  /** Default configuration for this node type */
  private Map<String, Object> defaultData;

  /** Whether this node can have incoming connections */
  private Boolean canHaveIncoming;

  /** Whether this node can have outgoing connections */
  private Boolean canHaveOutgoing;

  /** List of allowed parent node types (null = any) */
  private java.util.List<String> allowedParents;

  /** List of allowed child node types (null = any) */
  private java.util.List<String> allowedChildren;
}
