package dev.threadly.core.identity;

import dev.threadly.core.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts and validates Keycloak-issued Bearer tokens on each request.
 *
 * Claim mapping (set by Keycloak Protocol Mappers on threadly-app client):
 *   sub    -> userId  (Keycloak standard)
 *   orgId  -> orgId   (custom user-attribute mapper)
 *   role   -> role    (custom user-attribute mapper)
 *
 * TenantContext uses Spring request-attribute store — no ThreadLocal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        Jwt jwt   = jwtService.decode(token);
        String userId = jwt.getSubject();
        String orgId  = jwt.getClaimAsString("orgId");
        String role   = jwt.getClaimAsString("role");
        if (role == null) role = "member";

        TenantContext.set(
            UUID.fromString(orgId),
            UUID.fromString(userId),
            role);

        var auth = new UsernamePasswordAuthenticationToken(
            userId, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        SecurityContextHolder.getContext().setAuthentication(auth);

      } catch (Exception e) {
        log.debug("JWT validation failed: {}", e.getMessage());
      }
    }

    chain.doFilter(request, response);
    // No TenantContext.clear() needed — Spring request attrs are auto-cleaned
  }
}
