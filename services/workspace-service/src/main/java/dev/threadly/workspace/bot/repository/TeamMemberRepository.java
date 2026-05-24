package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.TeamMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for TeamMember entity.
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {

  /**
   * Find all team members for a bot.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return paginated list of team members
   */
  Page<TeamMember> findByBotId(@Param("botId") String botId, Pageable pageable);

  /**
   * Find a team member by bot ID and user ID.
   *
   * @param botId bot ID
   * @param userId user ID
   * @return Optional containing the team member if found
   */
  Optional<TeamMember> findByBotIdAndUserId(
      @Param("botId") String botId, @Param("userId") String userId);

  /**
   * Check if a user is a team member of a bot.
   *
   * @param botId bot ID
   * @param userId user ID
   * @return true if user is a member, false otherwise
   */
  @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm WHERE tm.botId = :botId AND tm.userId = :userId")
  boolean isMember(@Param("botId") String botId, @Param("userId") String userId);

  /**
   * Find all team members with a specific role for a bot.
   *
   * @param botId bot ID
   * @param role team role (OWNER, EDITOR, VIEWER)
   * @return list of team members with this role
   */
  List<TeamMember> findByBotIdAndRole(
      @Param("botId") String botId, @Param("role") String role);

  /**
   * Count team members for a bot.
   *
   * @param botId bot ID
   * @return number of team members
   */
  long countByBotId(@Param("botId") String botId);

  /**
   * Delete all team members for a bot.
   *
   * @param botId bot ID
   * @return number of records deleted
   */
  long deleteByBotId(@Param("botId") String botId);

  /**
   * Find all bots where a user has access.
   *
   * @param userId user ID
   * @param pageable pagination parameters
   * @return paginated list of bot IDs where user is a member
   */
  @Query(
      "SELECT DISTINCT tm.botId FROM TeamMember tm WHERE tm.userId = :userId")
  Page<String> findBotIdsByUserId(@Param("userId") String userId, Pageable pageable);
}
