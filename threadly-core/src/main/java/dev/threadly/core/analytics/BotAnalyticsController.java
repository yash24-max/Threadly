package dev.threadly.core.analytics;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bots/{botId}/analytics")
@RequiredArgsConstructor
@Tag(name = "Bot Analytics", description = "Per-bot analytics and funnel metrics")
public class BotAnalyticsController {

  private final JdbcTemplate jdbcTemplate;

  @GetMapping("/summary")
  @Operation(summary = "Summary metrics for a specific bot")
  public BotSummary summary(@PathVariable UUID botId) {
    UUID orgId = TenantContext.getOrgId();

    long total =
        queryLong(
            "SELECT COUNT(*) FROM conversations WHERE org_id = ? AND bot_id = ?",
            orgId,
            botId);
    long open =
        queryLong(
            "SELECT COUNT(*) FROM conversations WHERE org_id = ? AND bot_id = ? AND status = 'open'",
            orgId,
            botId);
    Double avgResponseMs =
        jdbcTemplate.queryForObject(
            """
            SELECT AVG(m.latency_ms)
            FROM messages m
            JOIN conversations c ON c.id = m.conversation_id
            WHERE c.org_id = ? AND c.bot_id = ? AND m.role = 'ai' AND m.latency_ms IS NOT NULL
            """,
            Double.class,
            orgId,
            botId);

    Double satisfactionRate =
        jdbcTemplate.queryForObject(
            """
            SELECT CASE WHEN COUNT(*) = 0 THEN NULL
              ELSE 100.0 * SUM(CASE WHEN (metadata->>'rating')::int >= 4 THEN 1 ELSE 0 END) / COUNT(*)
            END
            FROM conversations
            WHERE org_id = ? AND bot_id = ? AND metadata ? 'rating'
            """,
            Double.class,
            orgId,
            botId);

    List<Map<String, Object>> topIntents =
        jdbcTemplate.queryForList(
            """
            SELECT metadata->>'intent' AS name, COUNT(*) AS count
            FROM events
            WHERE org_id = ? AND bot_id = ? AND event_type = 'intent.detected'
              AND metadata ? 'intent'
            GROUP BY name
            ORDER BY count DESC
            LIMIT 10
            """,
            orgId,
            botId);

    BotSummary summary = new BotSummary();
    summary.setTotalConversations(total);
    summary.setOpenConversations(open);
    summary.setAvgResponseTimeMs(avgResponseMs != null ? Math.round(avgResponseMs) : 0L);
    summary.setSatisfactionRate(satisfactionRate != null ? Math.round(satisfactionRate * 10) / 10.0 : null);
    summary.setTopIntents(topIntents);
    return summary;
  }

  @GetMapping("/daily")
  @Operation(summary = "Daily metrics for a bot within a date range")
  public List<Map<String, Object>> daily(
      @PathVariable UUID botId,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    UUID orgId = TenantContext.getOrgId();
    Instant fromInstant = parseInstantOrMin(from);
    Instant toInstant = parseInstantOrNow(to);

    return jdbcTemplate.queryForList(
        """
        SELECT
          d.date::text AS date,
          COALESCE(dr.conversations, 0) AS conversations,
          COALESCE(dr.messages, 0) AS messages,
          COALESCE(dr.avg_latency_ms, 0) AS avg_response_time_ms
        FROM generate_series(
          ?::date, ?::date, '1 day'::interval
        ) AS d(date)
        LEFT JOIN daily_rollups dr
          ON dr.date = d.date AND dr.org_id = ? AND dr.bot_id = ?
        ORDER BY d.date ASC
        """,
        java.sql.Timestamp.from(fromInstant),
        java.sql.Timestamp.from(toInstant),
        orgId,
        botId);
  }

  @GetMapping("/funnel")
  @Operation(summary = "Flow completion rates per node (% of sessions reaching each node)")
  public List<Map<String, Object>> funnel(@PathVariable UUID botId) {
    UUID orgId = TenantContext.getOrgId();

    return jdbcTemplate.queryForList(
        """
        WITH total_sessions AS (
          SELECT COUNT(DISTINCT id) AS total
          FROM sessions
          WHERE org_id = ? AND bot_id = ?
        ),
        node_hits AS (
          SELECT
            (metadata->>'nodeId') AS node_id,
            COUNT(DISTINCT conversation_id) AS reached
          FROM messages
          WHERE org_id = ?
            AND conversation_id IN (
              SELECT id FROM conversations WHERE org_id = ? AND bot_id = ?
            )
            AND metadata ? 'nodeId'
          GROUP BY node_id
        )
        SELECT
          nh.node_id,
          nh.reached,
          CASE WHEN ts.total = 0 THEN 0
            ELSE ROUND(100.0 * nh.reached / ts.total, 2)
          END AS completion_pct
        FROM node_hits nh
        CROSS JOIN total_sessions ts
        ORDER BY nh.reached DESC
        """,
        orgId,
        botId,
        orgId,
        orgId,
        botId);
  }

  private long queryLong(String sql, Object... args) {
    Long result = jdbcTemplate.queryForObject(sql, Long.class, args);
    return result != null ? result : 0L;
  }

  private Instant parseInstantOrMin(String s) {
    if (s == null || s.isBlank()) return Instant.parse("2020-01-01T00:00:00Z");
    try {
      return Instant.parse(s);
    } catch (DateTimeParseException e) {
      return Instant.parse("2020-01-01T00:00:00Z");
    }
  }

  private Instant parseInstantOrNow(String s) {
    if (s == null || s.isBlank()) return Instant.now();
    try {
      return Instant.parse(s);
    } catch (DateTimeParseException e) {
      return Instant.now();
    }
  }

  // ── DTOs ──────────────────────────────────────────────────────────────

  @Data
  public static class BotSummary {
    private long totalConversations;
    private long openConversations;
    private long avgResponseTimeMs;
    private Double satisfactionRate;
    private List<Map<String, Object>> topIntents;
  }
}
