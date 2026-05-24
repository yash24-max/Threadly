package dev.threadly.common.feign;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Identity Service (:3001).
 *
 * Endpoints:
 * - Authentication (signup, login, token refresh)
 * - JWT validation & introspection
 * - API key generation & validation
 * - Tenancy validation
 */
@FeignClient(
    name = "identity-service",
    url = "${threadly.services.identity-service.url:http://identity-service:3001}"
)
public interface IdentityServiceClient {

  /**
   * POST /auth/signup — Create user and organization.
   */
  @PostMapping("/auth/signup")
  SignupResponse signup(@RequestBody SignupRequest request);

  /**
   * POST /auth/login — Authenticate user (local or OAuth).
   */
  @PostMapping("/auth/login")
  LoginResponse login(@RequestBody LoginRequest request);

  /**
   * POST /auth/refresh — Rotate access and refresh tokens.
   */
  @PostMapping("/auth/refresh")
  TokenResponse refreshToken(@RequestBody RefreshTokenRequest request);

  /**
   * POST /auth/logout — Invalidate refresh token.
   */
  @PostMapping("/auth/logout")
  void logout(@RequestHeader("Authorization") String token);

  /**
   * GET /me — Get current authenticated user and org context.
   */
  @GetMapping("/me")
  CurrentUserResponse getCurrentUser(@RequestHeader("Authorization") String token);

  /**
   * GET /orgs/{orgId}/members — List org members.
   */
  @GetMapping("/orgs/{orgId}/members")
  MembersListResponse listMembers(
      @PathVariable UUID orgId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /tenancy/validate — Validate org membership (internal).
   */
  @PostMapping("/tenancy/validate")
  TenancyValidationResponse validateTenancy(@RequestBody TenancyValidationRequest request);

  /**
   * POST /apikeys — Generate new API key.
   */
  @PostMapping("/apikeys")
  ApiKeyResponse generateApiKey(
      @RequestBody ApiKeyGenerateRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * DELETE /apikeys/{keyId} — Revoke API key.
   */
  @DeleteMapping("/apikeys/{keyId}")
  void revokeApiKey(
      @PathVariable UUID keyId,
      @RequestHeader("Authorization") String token
  );

  // Request/Response DTOs

  record SignupRequest(
      String email,
      String password,
      String orgName,
      String firstName,
      String lastName
  ) {}

  record SignupResponse(
      UUID userId,
      UUID orgId,
      String accessToken,
      String refreshToken
  ) {}

  record LoginRequest(
      String email,
      String password
  ) {}

  record LoginResponse(
      UUID userId,
      UUID orgId,
      String accessToken,
      String refreshToken
  ) {}

  record RefreshTokenRequest(
      String refreshToken
  ) {}

  record TokenResponse(
      String accessToken,
      String refreshToken
  ) {}

  record CurrentUserResponse(
      UUID userId,
      String email,
      UUID orgId,
      String orgName,
      String role
  ) {}

  record MembersListResponse(
      java.util.List<MemberInfo> members,
      int total
  ) {}

  record MemberInfo(
      UUID userId,
      String email,
      String name,
      String role,
      java.time.Instant joinedAt
  ) {}

  record TenancyValidationRequest(
      UUID orgId,
      UUID userId
  ) {}

  record TenancyValidationResponse(
      boolean valid,
      String role
  ) {}

  record ApiKeyGenerateRequest(
      String name,
      java.util.List<String> scopes
  ) {}

  record ApiKeyResponse(
      UUID keyId,
      String key,
      String name,
      java.time.Instant createdAt
  ) {}
}
