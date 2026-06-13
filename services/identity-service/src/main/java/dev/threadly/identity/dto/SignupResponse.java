package dev.threadly.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for signup response.
 * Contains user details and authentication tokens after successful registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponse {

  /**
   * Newly created user ID (UUID).
   */
  private String userId;

  /**
   * Newly created organization ID (UUID).
   */
  private String organizationId;

  /**
   * User's email address.
   */
  private String email;

  /**
   * User's full name.
   */
  private String fullName;

  /**
   * Organization name.
   */
  private String organizationName;

  /**
   * Organization slug (URL-safe version of name) for frontend routing.
   */
  private String organizationSlug;

  /**
   * JWT access token for immediate authenticated requests.
   */
  private String accessToken;

  /**
   * Refresh token for obtaining new access tokens after expiry.
   */
  private String refreshToken;

  /**
   * Access token expiry time in seconds.
   */
  private long expiresIn;

  /**
   * Token type (always "Bearer").
   */
  @Builder.Default
  private String tokenType = "Bearer";
}
