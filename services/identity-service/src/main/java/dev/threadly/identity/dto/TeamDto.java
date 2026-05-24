package dev.threadly.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Team entity.
 * Used in API responses to expose team information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamDto {

  /**
   * Team ID (UUID).
   */
  private String id;

  /**
   * Organization ID the team belongs to.
   */
  private String organizationId;

  /**
   * Team name.
   */
  private String name;

  /**
   * Team description or purpose.
   */
  private String description;

  /**
   * Whether the team is active.
   */
  private Boolean active;

  /**
   * Timestamp when the team was created.
   */
  private LocalDateTime createdAt;

  /**
   * Timestamp when the team was last updated.
   */
  private LocalDateTime updatedAt;

  /**
   * Number of active members in the team.
   */
  private Long memberCount;
}
