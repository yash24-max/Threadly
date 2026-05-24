package dev.threadly.identity.repository;

import dev.threadly.identity.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Refresh Token entity database operations.
 * Provides query methods for refresh token lookup and management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

  /**
   * Find a refresh token by its hash.
   *
   * @param tokenHash the hash of the refresh token
   * @return Optional containing the token if found
   */
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Find all refresh tokens for a user.
   *
   * @param userId the user ID
   * @return list of refresh tokens for the user
   */
  List<RefreshToken> findByUserId(String userId);

  /**
   * Find all active (non-revoked and non-expired) refresh tokens for a user.
   *
   * @param userId the user ID
   * @return list of active refresh tokens
   */
  @Query("""
      SELECT r FROM RefreshToken r
      WHERE r.userId = :userId
      AND r.revoked = false
      AND r.expiresAt > CURRENT_TIMESTAMP
      """)
  List<RefreshToken> findActiveByUserId(@Param("userId") String userId);

  /**
   * Find an active refresh token by hash and user ID.
   *
   * @param tokenHash the hash of the refresh token
   * @param userId the user ID
   * @return Optional containing the token if active
   */
  @Query("""
      SELECT r FROM RefreshToken r
      WHERE r.tokenHash = :tokenHash
      AND r.userId = :userId
      AND r.revoked = false
      AND r.expiresAt > CURRENT_TIMESTAMP
      """)
  Optional<RefreshToken> findActiveByTokenHashAndUserId(
      @Param("tokenHash") String tokenHash,
      @Param("userId") String userId);

  /**
   * Delete all expired refresh tokens.
   * Called by scheduled job to clean up expired tokens.
   *
   * @param expiresAt the cutoff timestamp
   * @return number of tokens deleted
   */
  @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :expiresAt")
  long deleteExpiredTokens(@Param("expiresAt") LocalDateTime expiresAt);

  /**
   * Revoke all active refresh tokens for a user (e.g., password change, logout all).
   *
   * @param userId the user ID
   * @return number of tokens revoked
   */
  @Query("""
      UPDATE RefreshToken r
      SET r.revoked = true
      WHERE r.userId = :userId AND r.revoked = false
      """)
  long revokeAllForUser(@Param("userId") String userId);
}
