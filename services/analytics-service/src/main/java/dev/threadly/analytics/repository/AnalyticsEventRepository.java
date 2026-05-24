package dev.threadly.analytics.repository;

import dev.threadly.analytics.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AnalyticsEvent persistence.
 * Handles storage and retrieval of raw analytics events with efficient time-series queries.
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, String> {

    /**
     * Find all events for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startTime start of time range (inclusive)
     * @param endTime end of time range (inclusive)
     * @return list of matching events
     */
    @Query("SELECT e FROM AnalyticsEvent e WHERE e.orgId = :orgId " +
           "AND e.createdAt >= :startTime AND e.createdAt <= :endTime " +
           "ORDER BY e.createdAt DESC")
    List<AnalyticsEvent> findByOrgIdAndTimeRange(
        @Param("orgId") String orgId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find events for a specific bot within a date range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching events
     */
    @Query("SELECT e FROM AnalyticsEvent e WHERE e.orgId = :orgId AND e.botId = :botId " +
           "AND e.createdAt >= :startTime AND e.createdAt <= :endTime " +
           "ORDER BY e.createdAt DESC")
    List<AnalyticsEvent> findByBotIdAndTimeRange(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find events by event type for an organization.
     *
     * @param orgId organization identifier
     * @param eventType type of event
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching events
     */
    @Query("SELECT e FROM AnalyticsEvent e WHERE e.orgId = :orgId AND e.eventType = :eventType " +
           "AND e.createdAt >= :startTime AND e.createdAt <= :endTime " +
           "ORDER BY e.createdAt DESC")
    List<AnalyticsEvent> findByEventType(
        @Param("orgId") String orgId,
        @Param("eventType") String eventType,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Find events by conversation ID.
     *
     * @param conversationId conversation identifier
     * @return list of events for the conversation
     */
    List<AnalyticsEvent> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    /**
     * Find events by session ID.
     *
     * @param sessionId session identifier
     * @return list of events for the session
     */
    List<AnalyticsEvent> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    /**
     * Count events for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startTime start of time range
     * @param endTime end of time range
     * @return count of matching events
     */
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.orgId = :orgId " +
           "AND e.createdAt >= :startTime AND e.createdAt <= :endTime")
    long countByOrgIdAndTimeRange(
        @Param("orgId") String orgId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Count events by event type for an organization.
     *
     * @param orgId organization identifier
     * @param eventType type of event
     * @param startTime start of time range
     * @param endTime end of time range
     * @return count of matching events
     */
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.orgId = :orgId " +
           "AND e.eventType = :eventType AND e.createdAt >= :startTime AND e.createdAt <= :endTime")
    long countByEventType(
        @Param("orgId") String orgId,
        @Param("eventType") String eventType,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

}
