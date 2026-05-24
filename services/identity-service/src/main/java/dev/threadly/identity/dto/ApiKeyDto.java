package dev.threadly.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for API Key entity.
 * Used in API responses to expose API key information.
 * Note: Never includes the full API key value, only prefix.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKeyDto {

  /**
   * API Key ID (UUID).
   */
  private String id;

  /**
   * Organization ID the API key belongs to.
   */
  private String organizationId;

  /**
   * Human-readable name for the API key.
   */
  private String name;

  /**
   * Prefix of the API key (for identification).
   * Full key is never exposed in responses.
   */
  private String keyPrefix;

  /**
   * Comma-separated list of scopes/permissions.
   */
  private String scopes;

  /**
   * Timestamp when the API key was last used.
   */
  private LocalDateTime lastUsedAt;

  /**
   * Timestamp when the API key will expire (NULL for non-expiring).
   */
  private LocalDateTime expiresAt;

  /**
   * Whether the API key is revoked/disabled.
   */
  private Boolean revoked;

  /**
   * Timestamp when the API key was created.
   */
  private LocalDateTime createdAt;
}
