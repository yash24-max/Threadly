package dev.threadly.identity.repository;

import dev.threadly.identity.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Team entity database operations.
 * Provides query methods for team lookup within organizations.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

  /**
   * Find all teams in an organization.
   *
   * @param orgId the organization ID
   * @return list of teams in the organization
   */
  List<Team> findByOrgId(String orgId);

  /**
   * Find all active teams in an organization.
   *
   * @param orgId the organization ID
   * @return list of active teams in the organization
   */
  @Query("SELECT t FROM Team t WHERE t.orgId = :orgId AND t.active = true")
  List<Team> findActiveByOrgId(@Param("orgId") String orgId);

  /**
   * Find a team by organization and name.
   *
   * @param orgId the organization ID
   * @param name the team name
   * @return the team if found
   */
  Team findByOrgIdAndName(String orgId, String name);

  /**
   * Count active teams in an organization.
   *
   * @param orgId the organization ID
   * @return count of active teams
   */
  @Query("SELECT COUNT(t) FROM Team t WHERE t.orgId = :orgId AND t.active = true")
  long countActiveByOrgId(@Param("orgId") String orgId);
}
