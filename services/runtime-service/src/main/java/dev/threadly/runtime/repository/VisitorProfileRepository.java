package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.VisitorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for VisitorProfile entity.
 * Provides database access for visitor profile management.
 */
@Repository
public interface VisitorProfileRepository extends JpaRepository<VisitorProfile, String> {

  /**
   * Find visitor profile by session ID
   */
  @Query("SELECT vp FROM VisitorProfile vp WHERE vp.sessionId = ?1")
  Optional<VisitorProfile> findBySessionId(String sessionId);

  /**
   * Find visitor profile by email
   */
  @Query("SELECT vp FROM VisitorProfile vp WHERE vp.email = ?1")
  Optional<VisitorProfile> findByEmail(String email);

  /**
   * Delete visitor profile by session ID
   */
  void deleteBySessionId(String sessionId);
}
