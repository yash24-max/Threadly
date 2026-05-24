package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.Bot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Bot entity with multi-tenancy support.
 * All queries enforce org_id isolation and soft delete filtering.
 */
public interface BotRepository extends JpaRepository<Bot, String> {

  /**
   * Find a single bot by ID and organization ID, excluding soft-deleted bots.
   *
   * @param id bot ID
   * @param orgId organization ID
   * @return Optional containing the bot if found and not deleted
   */
  @Query("SELECT b FROM Bot b WHERE b.id = :id AND b.orgId = :orgId AND b.deletedAt IS NULL")
  Optional<Bot> findByIdAndOrgId(@Param("id") String id, @Param("orgId") String orgId);

  /**
   * List all non-deleted bots for an organization with pagination.
   *
   * @param orgId organization ID
   * @param pageable pagination parameters
   * @return paginated list of bots
   */
  @Query("SELECT b FROM Bot b WHERE b.orgId = :orgId AND b.deletedAt IS NULL")
  Page<Bot> findByOrgId(@Param("orgId") String orgId, Pageable pageable);

  /**
   * Search bots by name within an organization (case-insensitive, partial match).
   *
   * @param orgId organization ID
   * @param name search term
   * @param pageable pagination parameters
   * @return matching bots
   */
  @Query(
      "SELECT b FROM Bot b WHERE b.orgId = :orgId AND b.deletedAt IS NULL "
          + "AND LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<Bot> searchByNameAndOrgId(
      @Param("orgId") String orgId, @Param("name") String name, Pageable pageable);

  /**
   * Find bots by status for an organization.
   *
   * @param orgId organization ID
   * @param status bot status (DRAFT, PUBLISHED, ARCHIVED)
   * @param pageable pagination parameters
   * @return bots matching the status
   */
  @Query(
      "SELECT b FROM Bot b WHERE b.orgId = :orgId AND b.status = :status "
          + "AND b.deletedAt IS NULL")
  Page<Bot> findByOrgIdAndStatus(
      @Param("orgId") String orgId, @Param("status") String status, Pageable pageable);

  /**
   * Count non-deleted bots in an organization.
   *
   * @param orgId organization ID
   * @return count of active bots
   */
  @Query("SELECT COUNT(b) FROM Bot b WHERE b.orgId = :orgId AND b.deletedAt IS NULL")
  long countByOrgId(@Param("orgId") String orgId);

  /**
   * Find recently deleted bots (soft deletes).
   *
   * @param orgId organization ID
   * @param since timestamp to search from
   * @return list of bots deleted after the given timestamp
   */
  @Query(
      "SELECT b FROM Bot b WHERE b.orgId = :orgId AND b.deletedAt IS NOT NULL "
          + "AND b.deletedAt >= :since")
  List<Bot> findRecentlyDeleted(
      @Param("orgId") String orgId, @Param("since") Instant since);
}
