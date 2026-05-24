package dev.threadly.analytics.repository;

import dev.threadly.analytics.entity.DailyRollup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyRollup persistence.
 * Handles storage and retrieval of pre-aggregated daily metrics.
 */
@Repository
public interface DailyRollupRepository extends JpaRepository<DailyRollup, String> {

    /**
     * Find daily rollup for a specific organization and date.
     *
     * @param orgId organization identifier
     * @param rollupDate the date of the rollup
     * @return optional rollup record
     */
    Optional<DailyRollup> findByOrgIdAndRollupDate(String orgId, LocalDate rollupDate);

    /**
     * Find daily rollups for a specific bot and date.
     *
     * @param botId bot identifier
     * @param rollupDate the date of the rollup
     * @return optional rollup record
     */
    Optional<DailyRollup> findByBotIdAndRollupDate(String botId, LocalDate rollupDate);

    /**
     * Find daily rollups for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of matching rollups ordered by date descending
     */
    @Query("SELECT r FROM DailyRollup r WHERE r.orgId = :orgId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate " +
           "ORDER BY r.rollupDate DESC")
    List<DailyRollup> findByOrgIdAndDateRange(
        @Param("orgId") String orgId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find daily rollups for a specific bot within a date range.
     *
     * @param botId bot identifier
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of matching rollups ordered by date descending
     */
    @Query("SELECT r FROM DailyRollup r WHERE r.botId = :botId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate " +
           "ORDER BY r.rollupDate DESC")
    List<DailyRollup> findByBotIdAndDateRange(
        @Param("botId") String botId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find daily rollups for an organization within a date range by bot.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of matching rollups ordered by date descending
     */
    @Query("SELECT r FROM DailyRollup r WHERE r.orgId = :orgId AND r.botId = :botId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate " +
           "ORDER BY r.rollupDate DESC")
    List<DailyRollup> findByOrgIdBotIdAndDateRange(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate sum of conversations for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startDate start date
     * @param endDate end date
     * @return sum of conversations
     */
    @Query("SELECT SUM(r.conversationsCount) FROM DailyRollup r WHERE r.orgId = :orgId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate")
    Long sumConversations(
        @Param("orgId") String orgId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate sum of messages for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startDate start date
     * @param endDate end date
     * @return sum of messages
     */
    @Query("SELECT SUM(r.messagesCount) FROM DailyRollup r WHERE r.orgId = :orgId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate")
    Long sumMessages(
        @Param("orgId") String orgId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate average CSAT score for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param startDate start date
     * @param endDate end date
     * @return average CSAT score
     */
    @Query("SELECT AVG(r.avgCsatScore) FROM DailyRollup r WHERE r.orgId = :orgId " +
           "AND r.rollupDate >= :startDate AND r.rollupDate <= :endDate " +
           "AND r.avgCsatScore > 0")
    Double averageCsat(
        @Param("orgId") String orgId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

}
