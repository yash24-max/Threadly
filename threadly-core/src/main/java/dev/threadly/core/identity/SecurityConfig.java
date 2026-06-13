package dev.threadly.core.identity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;

/**
 * Security configuration for threadly-core.
 *
 * JWT validation is delegated to {@link JwtAuthFilter} which decodes tokens
 * against Keycloak's JWKS endpoint configured via
 * {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public: registration (Keycloak user creation + org setup)
            .requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
            // Public: widget endpoints (called by embedded chatbot on customer sites)
            .requestMatchers(HttpMethod.POST, "/v1/widget/token", "/v1/widget/message").permitAll()
            .requestMatchers(HttpMethod.GET,  "/v1/widget/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/v1/proxy/**").permitAll()
            // Public: infra / docs
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/widget/**").permitAll()
            // Everything else requires a valid Keycloak JWT
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
