package dev.threadly.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Aggregates platform-wide statistics by querying each service schema directly.
 * In production these would be pre-computed via an analytics pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final JdbcTemplate jdbc;

    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalOrgs",  count("SELECT COUNT(*) FROM identity_service.organizations"));
            stats.put("totalUsers", count("SELECT COUNT(*) FROM identity_service.users"));
            stats.put("totalBots",  count("SELECT COUNT(*) FROM workspace_service.bots"));
            stats.put("totalFlows", count("SELECT COUNT(*) FROM flow_service.flows"));
        } catch (Exception ex) {
            log.warn("Could not aggregate stats (some schemas may be unavailable): {}", ex.getMessage());
            // Return zeros rather than failing the request
            stats.putIfAbsent("totalOrgs",  0L);
            stats.putIfAbsent("totalUsers", 0L);
            stats.putIfAbsent("totalBots",  0L);
            stats.putIfAbsent("totalFlows", 0L);
        }
        return stats;
    }

    private long count(String sql) {
        Long result = jdbc.queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }
}
