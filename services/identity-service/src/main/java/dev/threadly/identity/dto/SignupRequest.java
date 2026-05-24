package dev.threadly.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user signup request.
 * Contains email, password, and organization name for new account creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

  /**
   * User's email address. Must be unique and valid format.
   */
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  /**
   * User's full name.
   */
  @NotBlank(message = "Full name is required")
  @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
  private String fullName;

  /**
   * Password for the account. Must meet complexity requirements.
   * At least 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special character.
   */
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
  private String password;

  /**
   * Name of the organization to create.
   */
  @NotBlank(message = "Organization name is required")
  @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
  private String organizationName;
}
