package dev.threadly.identity.repository;

import dev.threadly.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity database operations.
 * Provides query methods for user lookup by email, organization, and active status.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  /**
   * Find a user by email address.
   *
   * @param email the user's email
   * @return Optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Find all active users in an organization.
   *
   * @param orgId the organization ID
   * @return list of active users in the organization
   */
  @Query("SELECT u FROM User u WHERE u.orgId = :orgId AND u.active = true")
  List<User> findActiveByOrgId(@Param("orgId") String orgId);

  /**
   * Find all users in an organization (including inactive).
   *
   * @param orgId the organization ID
   * @return list of all users in the organization
   */
  List<User> findByOrgId(String orgId);

  /**
   * Check if a user with given email exists.
   *
   * @param email the email to check
   * @return true if user exists, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Count active users in an organization.
   *
   * @param orgId the organization ID
   * @return count of active users
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.orgId = :orgId AND u.active = true")
  long countActiveByOrgId(@Param("orgId") String orgId);
}
