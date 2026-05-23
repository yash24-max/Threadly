package dev.threadly.core.identity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  @Operation(summary = "Create org + admin user")
  public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(req));
  }

  @PostMapping("/login")
  @Operation(summary = "Login with email + password")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    return ResponseEntity.ok(authService.login(req));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rotate refresh token")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
    return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
  }

  @PostMapping("/logout")
  @Operation(summary = "Revoke refresh token")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
    authService.logout(req.getRefreshToken());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  @Operation(summary = "Current user info")
  public ResponseEntity<UserResponse> me() {
    return ResponseEntity.ok(authService.me());
  }

  // ── Request/Response DTOs ────────────────────────────────────────

  @Data
  public static class SignupRequest {
    @NotBlank @Size(max = 200) private String orgName;
    @NotBlank @Size(max = 200) private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 72) private String password;
  }

  @Data
  public static class LoginRequest {
    @NotBlank @Email private String email;
    @NotBlank private String password;
  }

  @Data
  public static class RefreshRequest {
    @NotBlank private String refreshToken;
  }

  @Data
  public static class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserResponse user;
  }

  @Data
  public static class UserResponse {
    private String id;
    private String orgId;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String orgName;
    private String orgSlug;
  }
}
