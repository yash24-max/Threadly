package dev.threadly.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token request.
 * Used to obtain a new access token using a valid refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

  /**
   * The refresh token issued during login/signup.
   */
  @NotBlank(message = "Refresh token is required")
  private String refreshToken;
}
