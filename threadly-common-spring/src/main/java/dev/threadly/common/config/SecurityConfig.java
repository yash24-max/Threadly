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

  @Value("${threadly.jwt.issuer:https://threadly.dev}")
  private String jwtIssuer;

  @Value("${threadly.jwt.public-key-url:http://identity-service:3001/.well-known/jwks.json}")
  private String jwksUrl;

  /**
   * Configure JWT decoder (RS256).
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
  }

  /**
   * Filter to extract tenant context from JWT claims.
   */
  @Bean
  public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)
      throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/**", "/health", "/actuator/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new TenantContextFilter(jwtDecoder()), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .cors();

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

          // Extract org_id, user_id, email from claims
          String orgIdStr = jwt.getClaimAsString("org_id");
          String userIdStr = jwt.getClaimAsString("sub"); // JWT standard: subject is user_id
          String email = jwt.getClaimAsString("email");

          if (orgIdStr != null) {
            TenantContext.setTenantId(UUID.fromString(orgIdStr));
          }
          if (userIdStr != null) {
            TenantContext.setUserId(UUID.fromString(userIdStr));
          }
          if (email != null) {
            TenantContext.setEmail(email);
          }

          log.debug("TenantContext set: orgId={}, userId={}, email={}", orgIdStr, userIdStr, email);

        } catch (JwtException e) {
          log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
          log.warn("Invalid UUID in JWT claims: {}", e.getMessage());
        }
      }

      try {
        filterChain.doFilter(request, response);
      } finally {
        TenantContext.clear();
      }
    }
  }
}
