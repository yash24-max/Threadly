package dev.threadly.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token response.
 * Contains new access token and optionally new refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {

  /**
   * New JWT access token.
   */
  private String accessToken;

  /**
   * New refresh token (optional, may be rotated).
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
