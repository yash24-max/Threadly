package dev.threadly.core.identity;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.workspace.ApiKeyRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ApiKeyRepository apiKeyRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      if (token.startsWith("tly_live_")) {
        authenticateWithApiKey(token);
      } else {
        authenticateWithJwt(token);
      }
    }
    try {
      chain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  private void authenticateWithJwt(String token) {
    try {
      Claims claims = jwtService.parseToken(token);
      UUID userId = UUID.fromString(claims.getSubject());
      UUID orgId = UUID.fromString(claims.get("org", String.class));
      String role = claims.get("role", String.class);

      TenantContext.set(orgId, userId, role);
      var auth =
          new UsernamePasswordAuthenticationToken(
              userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (Exception e) {
      log.debug("JWT parse failed: {}", e.getMessage());
    }
  }

  private void authenticateWithApiKey(String rawKey) {
    try {
      String lookupHash = sha256Hex(rawKey);
      apiKeyRepository
          .findByKeyLookupHashAndRevokedAtIsNull(lookupHash)
          .ifPresentOrElse(
              apiKey -> {
                if (passwordEncoder.matches(rawKey, apiKey.getKeyHash())) {
                  apiKey.setLastUsedAt(Instant.now());
                  apiKeyRepository.save(apiKey);
                  TenantContext.set(apiKey.getOrgId(), apiKey.getBotId(), "API_KEY");
                  var auth =
                      new UsernamePasswordAuthenticationToken(
                          apiKey.getBotId(),
                          null,
                          List.of(new SimpleGrantedAuthority("ROLE_API_KEY")));
                  SecurityContextHolder.getContext().setAuthentication(auth);
                }
              },
              () -> log.debug("API key not found or revoked"));
    } catch (Exception e) {
      log.debug("API key auth failed: {}", e.getMessage());
    }
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
