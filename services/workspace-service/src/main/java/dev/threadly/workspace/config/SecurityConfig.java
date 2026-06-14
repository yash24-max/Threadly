package dev.threadly.workspace.config;

import dev.threadly.workspace.common.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Value("${keycloak.jwks-uri}")
    private String jwksUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsSource()))
            .addFilterBefore(tenantFilter(), AuthorizationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.decoder(jwtDecoder()).jwtAuthenticationConverter(converter())));
        return http.build();
    }

    @Bean public JwtDecoder jwtDecoder() { return NimbusJwtDecoder.withJwkSetUri(jwksUri).build(); }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtGrantedAuthoritiesConverter g = new JwtGrantedAuthoritiesConverter();
        g.setAuthoritiesClaimName("role"); g.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(g); return c;
    }

    @Bean
    public OncePerRequestFilter tenantFilter() {
        return new OncePerRequestFilter() {
            @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                    throws ServletException, IOException {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                    String orgIdStr = jwt.getClaimAsString("orgId");
                    String sub      = jwt.getSubject();
                    String role     = jwt.getClaimAsString("role");
                    if (orgIdStr != null) {
                        try { TenantContext.set(UUID.fromString(orgIdStr),
                                sub != null ? UUID.fromString(sub) : null, role);
                        } catch (Exception ignored) {}
                    }
                }
                chain.doFilter(req, res);
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration c = new CorsConfiguration(); c.setAllowCredentials(true);
        c.addAllowedOriginPattern("http://localhost:*"); c.addAllowedOriginPattern("https://*.threadly.dev");
        c.addAllowedHeader("*"); c.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c); return s;
    }
}
