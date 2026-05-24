package dev.threadly.identity.repository;

import dev.threadly.identity.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for API Key entity database operations.
 * Provides query methods for API key lookup and management.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

  /**
   * Find all API keys for an organization.
   *
   * @param orgId the organization ID
   * @return list of API keys in the organization
   */
  List<ApiKey> findByOrgId(String orgId);

  /**
   * Find all active (non-revoked) API keys for an organization.
   *
   * @param orgId the organization ID
   * @return list of active API keys
   */
  @Query("SELECT a FROM ApiKey a WHERE a.orgId = :orgId AND a.revoked = false")
  List<ApiKey> findActiveByOrgId(@Param("orgId") String orgId);

  /**
   * Find an API key by its hash.
   *
   * @param keyHash the hash of the API key
   * @return Optional containing the API key if found
   */
  Optional<ApiKey> findByKeyHash(String keyHash);

  /**
   * Find an active API key by its hash.
   *
   * @param keyHash the hash of the API key
   * @return Optional containing the active API key if found
   */
  @Query("SELECT a FROM ApiKey a WHERE a.keyHash = :keyHash AND a.revoked = false")
  Optional<ApiKey> findActiveByKeyHash(@Param("keyHash") String keyHash);

  /**
   * Find an API key by organization and name.
   *
   * @param orgId the organization ID
   * @param name the API key name
   * @return Optional containing the API key if found
   */
  Optional<ApiKey> findByOrgIdAndName(String orgId, String name);

  /**
   * Check if an API key is valid (active and not expired).
   *
   * @param keyHash the hash of the API key
   * @return true if the key is valid
   */
  @Query("""
      SELECT COUNT(a) > 0 FROM ApiKey a
      WHERE a.keyHash = :keyHash
      AND a.revoked = false
      AND (a.expiresAt IS NULL OR a.expiresAt > CURRENT_TIMESTAMP)
      """)
  boolean isKeyValid(@Param("keyHash") String keyHash);
}
