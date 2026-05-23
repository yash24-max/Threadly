package dev.threadly.core.identity;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.identity.AuthController.*;
import dev.threadly.core.workspace.Org;
import dev.threadly.core.workspace.OrgRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final OrgRepository orgRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Value("${threadly.jwt.refresh-token-expiry-days:30}")
  private long refreshTokenExpiryDays;

  @Value("${threadly.jwt.access-token-expiry-minutes:15}")
  private long accessTokenExpiryMinutes;

  @Transactional
  public TokenResponse signup(SignupRequest req) {
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("An account with this email already exists.");
    }
    String slug = req.getOrgName().toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    if (orgRepository.existsBySlug(slug)) {
      slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
    Org org = orgRepository.save(Org.builder().name(req.getOrgName()).slug(slug).build());
    User user = userRepository.save(
        User.builder()
            .org(org)
            .email(req.getEmail().toLowerCase())
            .name(req.getName())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .role("admin")
            .build());
    log.info("New signup: org={} user={}", org.getId(), user.getId());
    return buildTokenResponse(user, org);
  }

  @Transactional
  public TokenResponse login(LoginRequest req) {
    User user = userRepository.findByEmail(req.getEmail().toLowerCase())
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
    if (!user.isActive()) throw new IllegalArgumentException("Account is disabled.");
    if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid email or password.");
    }
    return buildTokenResponse(user, user.getOrg());
  }

  @Transactional
  public TokenResponse refresh(String rawToken) {
    String hash = sha256(rawToken);
    RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token."));
    if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Refresh token expired or revoked.");
    }
    stored.setRevoked(true);
    refreshTokenRepository.save(stored);
    User user = stored.getUser();
    return buildTokenResponse(user, user.getOrg());
  }

  @Transactional
  public void logout(String rawToken) {
    refreshTokenRepository.findByTokenHash(sha256(rawToken))
        .ifPresent(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
  }

  public UserResponse me() {
    UUID userId = TenantContext.getUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    return toUserResponse(user, user.getOrg());
  }

  private TokenResponse buildTokenResponse(User user, Org org) {
    String access = jwtService.generateAccessToken(user.getId(), org.getId(), user.getRole());
    String rawRefresh = generateRawToken();
    RefreshToken rt = RefreshToken.builder()
        .user(user)
        .tokenHash(sha256(rawRefresh))
        .expiresAt(Instant.now().plusSeconds(refreshTokenExpiryDays * 86400L))
        .build();
    refreshTokenRepository.save(rt);
    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(access);
    resp.setRefreshToken(rawRefresh);
    resp.setExpiresIn(accessTokenExpiryMinutes * 60);
    resp.setUser(toUserResponse(user, org));
    return resp;
  }

  private UserResponse toUserResponse(User user, Org org) {
    UserResponse r = new UserResponse();
    r.setId(user.getId().toString());
    r.setOrgId(org.getId().toString());
    r.setEmail(user.getEmail());
    r.setName(user.getName());
    r.setRole(user.getRole());
    r.setAvatarUrl(user.getAvatarUrl());
    r.setOrgName(org.getName());
    r.setOrgSlug(org.getSlug());
    return r;
  }

  private static String generateRawToken() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  static String sha256(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
