package dev.threadly.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User entity.
 * Used in API responses to expose user information without sensitive fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

  /**
   * User ID (UUID).
   */
  private String id;

  /**
   * User's email address.
   */
  private String email;

  /**
   * User's full name.
   */
  private String fullName;

  /**
   * Organization ID the user belongs to.
   */
  private String organizationId;

  /**
   * User's job title or role description.
   */
  private String jobTitle;

  /**
   * URL to user's profile picture.
   */
  private String profilePictureUrl;

  /**
   * Whether the user's email has been verified.
   */
  private Boolean emailVerified;

  /**
   * Whether the user account is active.
   */
  private Boolean active;

  /**
   * Timestamp when the user account was created.
   */
  private LocalDateTime createdAt;

  /**
   * Timestamp when the user account was last updated.
   */
  private LocalDateTime updatedAt;

  /**
   * Timestamp when the user last logged in.
   */
  private LocalDateTime lastLoginAt;
}
