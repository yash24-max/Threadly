package dev.threadly.core.identity;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

/**
 * JWT validation service backed by Keycloak's JWKS endpoint.
 *
 * <p>Replaces the previous local RSA key validation. Keycloak manages its own
 * signing keys and rotates them automatically; this service fetches the public
 * keys from the realm's JWKS URI and delegates validation to Nimbus.
 */
@Slf4j
@Service
public class JwtService {

  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  private JwtDecoder jwtDecoder;

  @PostConstruct
  void init() {
    jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    log.info("JWT decoder initialised with JWKS URI: {}", jwkSetUri);
  }

  /**
   * Decode and validate a Keycloak-issued access token.
   *
   * @param token raw Bearer token value (no "Bearer " prefix)
   * @return validated {@link Jwt}
   * @throws JwtException if the token is expired, invalid, or has a bad signature
   */
  public Jwt decode(String token) {
    return jwtDecoder.decode(token);
  }

  /** Convenience method for places that only need a validity check. */
  public boolean isValid(String token) {
    try {
      decode(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("JWT validation failed: {}", e.getMessage());
      return false;
    }
  }
}
