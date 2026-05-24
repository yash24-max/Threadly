package dev.threadly.identity.repository;

import dev.threadly.identity.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Membership entity database operations.
 * Provides query methods for membership lookup and management.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, String> {

  /**
   * Find membership of a user in an organization.
   *
   * @param userId the user ID
   * @param orgId the organization ID
   * @return Optional containing the membership if found
   */
  Optional<Membership> findByUserIdAndOrgId(String userId, String orgId);

  /**
   * Find all memberships for a user.
   *
   * @param userId the user ID
   * @return list of memberships for the user
   */
  List<Membership> findByUserId(String userId);

  /**
   * Find all active memberships for a user.
   *
   * @param userId the user ID
   * @return list of active memberships
   */
  @Query("SELECT m FROM Membership m WHERE m.userId = :userId AND m.active = true")
  List<Membership> findActiveByUserId(@Param("userId") String userId);

  /**
   * Find all members in an organization.
   *
   * @param orgId the organization ID
   * @return list of all memberships in the organization
   */
  List<Membership> findByOrgId(String orgId);

  /**
   * Find all active members in an organization.
   *
   * @param orgId the organization ID
   * @return list of active memberships in the organization
   */
  @Query("SELECT m FROM Membership m WHERE m.orgId = :orgId AND m.active = true")
  List<Membership> findActiveByOrgId(@Param("orgId") String orgId);

  /**
   * Find all members in an organization with a specific role.
   *
   * @param orgId the organization ID
   * @param role the role name
   * @return list of memberships with the specified role
   */
  @Query("SELECT m FROM Membership m WHERE m.orgId = :orgId AND m.role = :role AND m.active = true")
  List<Membership> findActiveByOrgIdAndRole(@Param("orgId") String orgId, @Param("role") String role);

  /**
   * Count active members in an organization.
   *
   * @param orgId the organization ID
   * @return count of active members
   */
  @Query("SELECT COUNT(m) FROM Membership m WHERE m.orgId = :orgId AND m.active = true")
  long countActiveByOrgId(@Param("orgId") String orgId);

  /**
   * Check if a user is an active member of an organization.
   *
   * @param userId the user ID
   * @param orgId the organization ID
   * @return true if user is an active member
   */
  @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.userId = :userId AND m.orgId = :orgId AND m.active = true")
  boolean isActiveMember(@Param("userId") String userId, @Param("orgId") String orgId);
}
