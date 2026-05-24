package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for integration catalog entries.
 * Represents available third-party integrations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationCatalogDto {

  /** Unique integration identifier (e.g., "slack", "hubspot", "salesforce") */
  private String id;

  /** User-friendly integration name (e.g., "Slack") */
  private String name;

  /** Detailed description of integration capabilities */
  private String description;

  /** Logo/icon URL for integration branding */
  private String icon;

  /** Available actions this integration supports (e.g., "send_message", "create_channel") */
  private List<String> actions;

  /** Whether this integration uses OAuth for authentication */
  private Boolean oauth;

  /** OAuth authentication endpoint (if oauth=true) */
  private String authUrl;

  /** API documentation URL */
  private String docsUrl;

  /** Whether this integration is available in the current plan/organization */
  private Boolean available;

  /** Required scopes for OAuth (if applicable) */
  private List<String> requiredScopes;

  /** Status: "active", "beta", "deprecated" */
  private String status;

  /** Custom configuration schema for this integration */
  private Map<String, Object> configSchema;

  /** Rate limits or usage restrictions */
  private Map<String, Object> limits;
}
