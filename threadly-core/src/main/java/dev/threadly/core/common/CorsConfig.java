package dev.threadly.core.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

  @Value("${threadly.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
  private List<String> allowedOrigins;

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("X-Total-Count", "X-Request-Id", "Content-Disposition"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Dashboard / API origins get full CORS
    source.registerCorsConfiguration("/v1/**", config);
    source.registerCorsConfiguration("/v3/**", config);
    source.registerCorsConfiguration("/swagger-ui/**", config);
    source.registerCorsConfiguration("/actuator/**", config);

    // Widget endpoints are open to any origin (customers embed on their own domains)
    CorsConfiguration widgetConfig = new CorsConfiguration();
    widgetConfig.addAllowedOriginPattern("*");
    widgetConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    widgetConfig.setAllowedHeaders(List.of("*"));
    widgetConfig.setAllowCredentials(false);
    widgetConfig.setMaxAge(86400L);
    source.registerCorsConfiguration("/v1/widget/**", widgetConfig);
    source.registerCorsConfiguration("/widget/**", widgetConfig);

    return new CorsFilter(source);
  }
}
