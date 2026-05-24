package dev.threadly.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for the identity service.
 * Configures password encoding and authentication mechanisms.
 */
@Configuration
public class SecurityConfig {

  /**
   * Configure BCrypt password encoder.
   * Uses strength 12 for balanced security and performance.
   *
   * @return BCryptPasswordEncoder with strength 12
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
