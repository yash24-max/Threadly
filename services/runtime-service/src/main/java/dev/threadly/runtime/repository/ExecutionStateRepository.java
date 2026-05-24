package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.ExecutionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ExecutionState entity.
 * Provides database access for execution state management.
 */
@Repository
public interface ExecutionStateRepository extends JpaRepository<ExecutionState, String> {

  /**
   * Find execution state by session ID
   */
  @Query("SELECT es FROM ExecutionState es WHERE es.sessionId = ?1")
  Optional<ExecutionState> findBySessionId(String sessionId);

  /**
   * Delete execution state by session ID
   */
  void deleteBySessionId(String sessionId);

  /**
   * Check if execution state exists for a session
   */
  @Query("SELECT COUNT(es) > 0 FROM ExecutionState es WHERE es.sessionId = ?1")
  Boolean existsBySessionId(String sessionId);
}
