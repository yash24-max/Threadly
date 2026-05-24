package dev.threadly.identity.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the identity service.
 * Enables Spring's caching abstraction with Caffeine backend.
 */
@Configuration
@EnableCaching
public class CacheConfig {
  // Configuration is handled via application.yml
  // Properties defined in spring.cache section
}
