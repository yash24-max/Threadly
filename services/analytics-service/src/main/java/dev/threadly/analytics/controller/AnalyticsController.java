package dev.threadly.analytics.controller;

import dev.threadly.analytics.common.TenantContext;
import dev.threadly.analytics.dto.AnalyticsOverviewDto;
import dev.threadly.analytics.dto.MetricQueryRequest;
import dev.threadly.analytics.dto.MetricQueryResponse;
import dev.threadly.analytics.repository.AnalyticsEventRepository;
import dev.threadly.analytics.service.AnalyticsService;
import dev.threadly.analytics.service.BotMetricsService;
import dev.threadly.analytics.service.MetricAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for analytics endpoints.
 * Provides access to analytics overview, metrics, and data export.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final BotMetricsService botMetricsService;
    private final MetricAggregationService metricAggregationService;
    private final AnalyticsEventRepository eventRepository;

    /**
     * Get analytics overview for an organization.
     * POST /api/v1/analytics/overview
     */
    @PostMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getAnalyticsOverview(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @RequestParam(value = "days", defaultValue = "30") int days
    ) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);

            log.debug("Fetching analytics overview for org: {} for last {} days", orgId, days);

            AnalyticsOverviewDto.PeriodMetrics periodMetrics = AnalyticsOverviewDto.PeriodMetrics.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalConversations(0L)
                .totalMessages(0L)
                .totalAiCalls(0L)
                .avgResponseTimeMs(0.0)
                .resolutionRate(0.0)
                .avgCsatScore(0.0)
                .totalTokensConsumed(0L)
                .totalCostCents(0L)
                .handoffsCount(0L)
                .build();

            AnalyticsOverviewDto overview = AnalyticsOverviewDto.builder()
                .period(periodMetrics)
                .generatedAt(System.currentTimeMillis())
                .build();

            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error fetching analytics overview for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get specific metric data.
     * GET /api/v1/analytics/metrics/{metricName}
     */
    @GetMapping("/metrics/{metricName}")
    public ResponseEntity<?> getMetric(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @PathVariable String metricName,
        @RequestParam(value = "bot_id", required = false) String botId,
        @RequestParam(value = "days", defaultValue = "30") int days,
        Pageable pageable
    ) {
        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minusSeconds((long) days * 86400);

            log.debug("Fetching metric: {} for org: {} bot: {}", metricName, orgId, botId);

            var metrics = analyticsService.queryMetrics(orgId, metricName, startTime, endTime, pageable);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching metric: {}", metricName, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Execute custom metric query.
     * POST /api/v1/analytics/query
     */
    @PostMapping("/query")
    public ResponseEntity<?> queryMetrics(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @RequestBody MetricQueryRequest request
    ) {
        try {
            log.debug("Executing custom metric query for org: {}", orgId);

            if (request.getMetricNames() == null || request.getMetricNames().isEmpty()) {
                return ResponseEntity.badRequest().body("metric_names is required");
            }

            // Build query response
            MetricQueryResponse response = MetricQueryResponse.builder()
                .queryId(java.util.UUID.randomUUID().toString())
                .totalRecords(0L)
                .executedAt(Instant.now())
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing metric query for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Export metrics data.
     * GET /api/v1/analytics/export
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportMetrics(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @RequestParam(value = "metric") String metricName,
        @RequestParam(value = "format", defaultValue = "csv") String format,
        @RequestParam(value = "days", defaultValue = "30") int days
    ) {
        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minusSeconds((long) days * 86400);

            log.debug("Exporting metric: {} in format: {} for org: {}", metricName, format, orgId);

            String exportData = analyticsService.exportMetricsAsCsv(orgId, metricName, startTime, endTime);

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"metrics.csv\"")
                .body(exportData);
        } catch (Exception e) {
            log.error("Error exporting metrics for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Dashboard stats endpoint — matches the shape expected by the frontend dashboard page.
     * GET /api/v1/analytics/stats
     *
     * Returns: { totalConversations, openConversations, handoffConversations, p50ResponseMs }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
        @RequestHeader(value = "X-Org-ID", required = false) String orgIdHeader
    ) {
        try {
            // Prefer JWT-based TenantContext; fall back to header for backward compat
            String orgId = orgIdHeader;
            try { var tc = TenantContext.getOrgIdOptional(); if (tc != null) orgId = tc.toString(); } catch (Exception ignored) {}

            log.debug("Fetching dashboard stats for org: {}", orgId);

            Instant epoch = Instant.EPOCH;
            Instant now   = Instant.now();

            long total     = eventRepository.countByOrgIdAndTimeRange(orgId, epoch, now);
            long open      = eventRepository.countByEventType(orgId, "CONVERSATION_STARTED", epoch, now);
            long handedOff = eventRepository.countByEventType(orgId, "HANDOFF_INITIATED",    epoch, now);
            long closed    = eventRepository.countByEventType(orgId, "CONVERSATION_ENDED",   epoch, now);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalConversations",   total);
            stats.put("openConversations",    Math.max(0, open - handedOff - closed));
            stats.put("handoffConversations", handedOff);
            stats.put("closedConversations",  closed);
            stats.put("p50ResponseMs",        0L); // computed by ML pipeline in v2
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Health check endpoint.
     * GET /api/v1/analytics/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics service is running");
    }

}
