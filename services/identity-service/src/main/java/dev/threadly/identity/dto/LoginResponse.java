package dev.threadly.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response.
 * Contains authentication tokens and user details after successful login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

  /**
   * User ID (UUID).
   */
  private String userId;

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
   * Organization name — avoids extra API call on the frontend after login.
   */
  private String organizationName;

  /**
   * Organization slug (URL-safe version of name) for frontend routing.
   */
  private String organizationSlug;

  /**
   * JWT access token for authenticated requests.
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
