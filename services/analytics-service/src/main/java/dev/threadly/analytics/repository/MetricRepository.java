package dev.threadly.analytics.repository;

import dev.threadly.analytics.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Metric persistence.
 * Handles storage and retrieval of computed metrics with time-series query support.
 */
@Repository
public interface MetricRepository extends JpaRepository<Metric, String> {

    /**
     * Find metrics by name for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param metricName name of the metric
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching metrics
     */
    @Query("SELECT m FROM Metric m WHERE m.orgId = :orgId AND m.metricName = :metricName " +
           "AND m.metricTimestamp >= :startTime AND m.metricTimestamp <= :endTime " +
           "ORDER BY m.metricTimestamp DESC")
    List<Metric> findByMetricName(
        @Param("orgId") String orgId,
        @Param("metricName") String metricName,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find metrics for a specific bot and metric name within a date range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName name of the metric
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching metrics
     */
    @Query("SELECT m FROM Metric m WHERE m.orgId = :orgId AND m.botId = :botId " +
           "AND m.metricName = :metricName AND m.metricTimestamp >= :startTime " +
           "AND m.metricTimestamp <= :endTime ORDER BY m.metricTimestamp DESC")
    List<Metric> findByBotAndMetricName(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("metricName") String metricName,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Get the latest metric value for a bot and metric name.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName name of the metric
     * @return the most recent metric record
     */
    @Query("SELECT m FROM Metric m WHERE m.orgId = :orgId AND m.botId = :botId " +
           "AND m.metricName = :metricName ORDER BY m.metricTimestamp DESC LIMIT 1")
    Optional<Metric> findLatestByBotAndMetricName(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("metricName") String metricName
    );

    /**
     * Find all metrics for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching metrics
     */
    @Query("SELECT m FROM Metric m WHERE m.orgId = :orgId " +
           "AND m.metricTimestamp >= :startTime AND m.metricTimestamp <= :endTime " +
           "ORDER BY m.metricTimestamp DESC")
    List<Metric> findByOrgIdAndTimeRange(
        @Param("orgId") String orgId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Calculate average metric value for a bot within a date range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName name of the metric
     * @param startTime start of time range
     * @param endTime end of time range
     * @return average value or null if no metrics
     */
    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.orgId = :orgId AND m.botId = :botId " +
           "AND m.metricName = :metricName AND m.metricTimestamp >= :startTime " +
           "AND m.metricTimestamp <= :endTime")
    Double findAverageValue(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("metricName") String metricName,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find maximum metric value for a bot within a date range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName name of the metric
     * @param startTime start of time range
     * @param endTime end of time range
     * @return maximum value or null if no metrics
     */
    @Query("SELECT MAX(m.value) FROM Metric m WHERE m.orgId = :orgId AND m.botId = :botId " +
           "AND m.metricName = :metricName AND m.metricTimestamp >= :startTime " +
           "AND m.metricTimestamp <= :endTime")
    Double findMaxValue(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("metricName") String metricName,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

}
