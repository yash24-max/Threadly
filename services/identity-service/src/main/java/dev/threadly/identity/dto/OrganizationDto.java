package dev.threadly.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Organization entity.
 * Used in API responses to expose organization information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationDto {

  /**
   * Organization ID (UUID).
   */
  private String id;

  /**
   * Organization name/display name.
   */
  private String name;

  /**
   * Organization owner's user ID.
   */
  private String ownerId;

  /**
   * Billing plan type (e.g., FREE, PRO, ENTERPRISE).
   */
  private String plan;

  /**
   * Organization description or notes.
   */
  private String description;

  /**
   * Organization's website URL.
   */
  private String website;

  /**
   * Logo URL for the organization.
   */
  private String logoUrl;

  /**
   * Whether the organization account is active.
   */
  private Boolean active;

  /**
   * Timestamp when the organization was created.
   */
  private LocalDateTime createdAt;

  /**
   * Timestamp when the organization was last updated.
   */
  private LocalDateTime updatedAt;

  /**
   * Number of active members in the organization.
   */
  private Long memberCount;
}
