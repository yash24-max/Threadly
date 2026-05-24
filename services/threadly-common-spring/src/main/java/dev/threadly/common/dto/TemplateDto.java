package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for flow templates.
 * Represents pre-built flow definitions that users can instantiate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateDto {

  /** Unique template identifier (e.g., "customer-support-workflow") */
  private String id;

  /** User-friendly template name */
  private String name;

  /** Detailed description of the template */
  private String description;

  /** Category for organization (e.g., "Support", "Sales", "Marketing") */
  private String category;

  /** Number of times this template has been used */
  private Long usageCount;

  /** Template thumbnail or preview URL (optional) */
  private String thumbnailUrl;

  /** Complete flow definition (nodes, connections, configuration) */
  private java.util.Map<String, Object> definition;

  /** Tags for search and filtering */
  private java.util.List<String> tags;

  /** Template version for tracking updates */
  private String version;

  /** Whether this is an official Threadly template vs. organization-created */
  private Boolean isOfficial;
}
