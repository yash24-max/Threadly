package dev.threadly.identity.controller;

import dev.threadly.identity.dto.*;
import dev.threadly.identity.entity.Organization;
import dev.threadly.identity.entity.User;
import dev.threadly.identity.service.AuthTokenService;
import dev.threadly.identity.service.OrganizationService;
import dev.threadly.identity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles user signup, login, token refresh, and logout operations.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final OrganizationService organizationService;
  private final AuthTokenService authTokenService;

  /**
   * Signup endpoint - creates a new user and organization.
   *
   * @param request SignupRequest with email, password, fullName, organizationName
   * @param httpRequest HTTP request for client IP and user agent
   * @return SignupResponse with user, org details and tokens
   */
  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(
      @Valid @RequestBody SignupRequest request,
      HttpServletRequest httpRequest) {

    log.info("Processing signup for email: {}", request.getEmail());

    Organization org = organizationService.createOrganization(request.getOrganizationName(), null);

    User user = userService.registerUser(
        request.getEmail(),
        request.getPassword(),
        request.getFullName(),
        org.getId()
    );

    org.setOwnerId(user.getId());
    organizationService.updateOrganization(org.getId(), org.getName(), null, null, null);

    String accessToken = authTokenService.issueAccessToken(user);
    String refreshToken = authTokenService.issueRefreshToken(
        user,
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent")
    );

    SignupResponse response = SignupResponse.builder()
        .userId(user.getId())
        .organizationId(org.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .organizationName(org.getName())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(900)
        .tokenType("Bearer")
        .build();

    log.info("User signup successful: {}", user.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Login endpoint - authenticates a user by email and password.
   *
   * @param request LoginRequest with email and password
   * @param httpRequest HTTP request for client IP and user agent
   * @return LoginResponse with user details and tokens
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {

    log.info("Processing login for email: {}", request.getEmail());

    User user = userService.authenticateUser(request.getEmail(), request.getPassword());

    String accessToken = authTokenService.issueAccessToken(user);
    String refreshToken = authTokenService.issueRefreshToken(
        user,
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent")
    );

    // Fetch org name so frontend can populate the session without an extra API call
    String orgName = "";
    try {
      var org = organizationService.getOrganizationById(user.getOrgId());
      if (org != null) orgName = org.getName();
    } catch (Exception e) {
      log.warn("Could not fetch org name for login response: {}", e.getMessage());
    }

    LoginResponse response = LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .organizationId(user.getOrgId())
        .organizationName(orgName)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(900)
        .tokenType("Bearer")
        .build();

    log.info("User login successful: {}", user.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Refresh token endpoint - obtains a new access token using refresh token.
   *
   * @param request RefreshTokenRequest with refresh token
   * @return RefreshTokenResponse with new access token
   */
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {

    log.debug("Processing token refresh");

    String newAccessToken = authTokenService.refreshAccessToken(request.getRefreshToken());

    RefreshTokenResponse response = RefreshTokenResponse.builder()
        .accessToken(newAccessToken)
        .expiresIn(900)
        .tokenType("Bearer")
        .build();

    log.debug("Token refresh successful");
    return ResponseEntity.ok(response);
  }

  /**
   * Logout endpoint - revokes refresh token.
   *
   * @param request RefreshTokenRequest with refresh token to revoke
   * @return success message
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @Valid @RequestBody RefreshTokenRequest request) {

    log.debug("Processing logout");
    authTokenService.revokeRefreshToken(request.getRefreshToken());
    log.debug("Logout successful");

    return ResponseEntity.noContent().build();
  }

  /**
   * Verify email endpoint - marks a user's email as verified.
   *
   * @param userId the user ID
   * @return success message
   */
  @PostMapping("/verify-email/{userId}")
  public ResponseEntity<Void> verifyEmail(@PathVariable String userId) {

    log.info("Verifying email for user: {}", userId);
    userService.verifyEmail(userId);

    return ResponseEntity.noContent().build();
  }
}
