package dev.threadly.identity.repository;

import dev.threadly.identity.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Organization entity database operations.
 * Provides query methods for organization lookup and listing.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {

  /**
   * Find an organization by owner ID.
   *
   * @param ownerId the owner's user ID
   * @return list of organizations owned by the user
   */
  List<Organization> findByOwnerId(String ownerId);

  /**
   * Find active organizations owned by a user.
   *
   * @param ownerId the owner's user ID
   * @return list of active organizations owned by the user
   */
  @Query("SELECT o FROM Organization o WHERE o.ownerId = :ownerId AND o.active = true")
  List<Organization> findActiveByOwnerId(@Param("ownerId") String ownerId);

  /**
   * Find all active organizations.
   *
   * @return list of all active organizations
   */
  @Query("SELECT o FROM Organization o WHERE o.active = true")
  List<Organization> findAllActive();

  /**
   * Find an organization by Stripe customer ID.
   *
   * @param stripeCustomerId the Stripe customer ID
   * @return Optional containing the organization if found
   */
  Optional<Organization> findByStripeCustomerId(String stripeCustomerId);

  /**
   * Find all organizations on a specific plan.
   *
   * @param plan the billing plan type
   * @return list of organizations on the specified plan
   */
  @Query("SELECT o FROM Organization o WHERE o.plan = :plan AND o.active = true")
  List<Organization> findByPlan(@Param("plan") String plan);

  /**
   * Check if organization exists and is active.
   *
   * @param id the organization ID
   * @return true if organization exists and is active
   */
  @Query("SELECT COUNT(o) > 0 FROM Organization o WHERE o.id = :id AND o.active = true")
  boolean existsAndIsActive(@Param("id") String id);
}
