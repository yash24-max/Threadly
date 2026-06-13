package dev.threadly.common.config;

import dev.threadly.common.context.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Arrays;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security configuration for JWT RS256 token validation and tenant context extraction.
 *
 * Features:
 * - JWT token validation (RS256)
 * - Automatic org_id extraction to TenantContext
 * - Bearer token parsing from Authorization header
 * - Public endpoints bypass authentication (/auth/*, /health)
 */
@Slf4j
@Configuration
public class SecurityConfig {

  /**
   * Keycloak JWKS endpoint.
   * Default points to local dev Keycloak; overridden via env var in Docker/K8s:
   *   KEYCLOAK_JWKS_URI=http://keycloak:8080/realms/threadly/protocol/openid-connect/certs
   */
  @Value("${keycloak.jwks-uri:http://localhost:8090/realms/threadly/protocol/openid-connect/certs}")
  private String jwksUrl;

  /**
   * Configure JWT decoder (RS256).
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
  }

  /**
   * CORS configuration — allows requests from the Next.js frontend and production domain.
   * Nginx gateway also adds CORS headers, but this bean covers direct service calls.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("http://localhost:*");
    config.addAllowedOriginPattern("https://*.threadly.dev");
    config.addAllowedOriginPattern("https://*.threadly.ai");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Filter to extract tenant context from JWT claims.
   */
  @Bean
  public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)
      throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/**", "/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new TenantContextFilter(jwtDecoder()), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
  }

  /**
   * Filter to extract org_id, user_id, email from JWT and set TenantContext.
   */
  public static class TenantContextFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public TenantContextFilter(JwtDecoder jwtDecoder) {
      this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
        throws jakarta.servlet.ServletException, java.io.IOException {

      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        try {
          var jwt = jwtDecoder.decode(token);

          // Keycloak JWT claim names (set by Protocol Mappers on threadly-app client):
          //   orgId  → user attribute "orgId"   (custom mapper)
          //   role   → user attribute "role"    (custom mapper)
          //   sub    → Keycloak user UUID       (standard)
          //   email  → user email               (standard OIDC)
          String orgIdStr  = jwt.getClaimAsString("orgId");
          String userIdStr = jwt.getClaimAsString("sub");
          String email     = jwt.getClaimAsString("email");
          String role      = jwt.getClaimAsString("role");

          if (orgIdStr  != null) TenantContext.setTenantId(UUID.fromString(orgIdStr));
          if (userIdStr != null) TenantContext.setUserId(UUID.fromString(userIdStr));
          if (email     != null) TenantContext.setEmail(email);
          if (role      != null) TenantContext.setRole(role);

          log.debug("TenantContext set: orgId={} userId={}", orgIdStr, userIdStr);

        } catch (JwtException e) {
          log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
          log.warn("Invalid UUID in JWT claims: {}", e.getMessage());
        }
      }

      filterChain.doFilter(request, response);
      // No TenantContext.clear() needed — Spring request attrs are auto-cleaned
    }
  }
}
