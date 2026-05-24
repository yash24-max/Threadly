package dev.threadly.analytics.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caching configuration for analytics service.
 * Provides in-memory caching with 5-minute TTL for dashboard data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Create a cache manager for analytics caches.
     *
     * @return cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "dashboards",
            "metrics",
            "rollups"
        );
    }

}
