package dev.threadly.identity.service;

import dev.threadly.identity.entity.RefreshToken;
import dev.threadly.identity.entity.User;
import dev.threadly.identity.exception.InvalidApiKeyException;
import dev.threadly.identity.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing JWT tokens and refresh tokens.
 * Handles token generation, validation, and refresh logic.
 */
@Slf4j
@Service
public class AuthTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final SecretKey secretKey;

  @Value("${auth.jwt.access-token-expiry-seconds:900}")
  private long accessTokenExpirySeconds;

  @Value("${auth.jwt.refresh-token-expiry-seconds:2592000}")
  private long refreshTokenExpirySeconds;

  /**
   * Constructs the AuthTokenService with JWT configuration.
   *
   * @param refreshTokenRepository repository for refresh token persistence
   * @param jwtSecret the JWT secret key for signing tokens
   */
  public AuthTokenService(RefreshTokenRepository refreshTokenRepository,
      @Value("${auth.jwt.secret:defaultSecretKeyFor256BitHMACSigningAlgorithmChangeInProduction}") String jwtSecret) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Issues a new JWT access token for a user.
   *
   * @param user the user to issue token for
   * @return JWT access token string
   */
  public String issueAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", user.getEmail());
    claims.put("orgId", user.getOrgId());
    claims.put("fullName", user.getFullName());

    return createToken(claims, user.getId(), accessTokenExpirySeconds);
  }

  /**
   * Issues a new refresh token for a user.
   * Refresh tokens are longer-lived and stored in the database.
   *
   * @param user the user to issue refresh token for
   * @param ipAddress client IP address for audit trail
   * @param userAgent client user agent for audit trail
   * @return refresh token string
   */
  @Transactional
  public String issueRefreshToken(User user, String ipAddress, String userAgent) {
    String tokenValue = UUID.randomUUID().toString();
    String tokenHash = hashToken(tokenValue);

    LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("UTC"))
        .plusSeconds(refreshTokenExpirySeconds);

    RefreshToken refreshToken = RefreshToken.builder()
        .id(UUID.randomUUID().toString())
        .userId(user.getId())
        .tokenHash(tokenHash)
        .issuedFromIp(ipAddress)
        .userAgent(userAgent)
        .expiresAt(expiresAt)
        .revoked(false)
        .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("Issued refresh token for user: {}", user.getId());

    return tokenValue;
  }

  /**
   * Validates a JWT access token.
   *
   * @param token the JWT token to validate
   * @return true if valid, false otherwise
   */
  public boolean validateAccessToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Extracts claims from a JWT token.
   *
   * @param token the JWT token
   * @return claims extracted from the token
   */
  public Claims extractClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException e) {
      log.debug("Failed to extract claims from token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extracts user ID from a JWT token.
   *
   * @param token the JWT token
   * @return user ID (subject)
   */
  public String extractUserId(String token) {
    Claims claims = extractClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  /**
   * Extracts organization ID from a JWT token.
   *
   * @param token the JWT token
   * @return organization ID
   */
  public String extractOrgId(String token) {
    Claims claims = extractClaims(token);
    return claims != null ? (String) claims.get("orgId") : null;
  }

  /**
   * Validates a refresh token.
   * Checks if token exists in database, is not revoked, and not expired.
   *
   * @param refreshTokenValue the refresh token string
   * @return RefreshToken entity if valid
   * @throws InvalidApiKeyException if token is invalid
   */
  @Transactional(readOnly = true)
  public RefreshToken validateRefreshToken(String refreshTokenValue) {
    String tokenHash = hashToken(refreshTokenValue);

    return refreshTokenRepository.findByTokenHash(tokenHash)
        .filter(token -> !token.getRevoked())
        .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now(ZoneId.of("UTC"))))
        .orElseThrow(() -> new InvalidApiKeyException("Invalid or expired refresh token"));
  }

  /**
   * Refreshes an access token using a valid refresh token.
   *
   * @param refreshTokenValue the refresh token
   * @return new access token
   */
  @Transactional
  public String refreshAccessToken(String refreshTokenValue) {
    RefreshToken refreshToken = validateRefreshToken(refreshTokenValue);

    User user = new User();
    user.setId(refreshToken.getUserId());

    log.debug("Refreshed access token for user: {}", refreshToken.getUserId());
    return issueAccessToken(user);
  }

  /**
   * Revokes a refresh token (e.g., on logout).
   *
   * @param refreshTokenValue the refresh token to revoke
   */
  @Transactional
  public void revokeRefreshToken(String refreshTokenValue) {
    String tokenHash = hashToken(refreshTokenValue);

    refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
      token.setRevoked(true);
      refreshTokenRepository.save(token);
      log.debug("Revoked refresh token for user: {}", token.getUserId());
    });
  }

  /**
   * Revokes all refresh tokens for a user (e.g., password change or logout all devices).
   *
   * @param userId the user ID
   */
  @Transactional
  public void revokeAllRefreshTokens(String userId) {
    long revokedCount = refreshTokenRepository.revokeAllForUser(userId);
    log.info("Revoked {} refresh tokens for user: {}", revokedCount, userId);
  }

  /**
   * Creates a JWT token with given claims.
   *
   * @param claims token claims
   * @param subject token subject (user ID)
   * @param expirySeconds token expiry in seconds
   * @return JWT token string
   */
  private String createToken(Map<String, Object> claims, String subject, long expirySeconds) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirySeconds * 1000);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Hashes a token using SHA256 for storage.
   *
   * @param token the token to hash
   * @return hashed token
   */
  private String hashToken(String token) {
    try {
      java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(encodedhash);
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Converts byte array to hex string.
   *
   * @param hash the byte array
   * @return hex string representation
   */
  private String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
