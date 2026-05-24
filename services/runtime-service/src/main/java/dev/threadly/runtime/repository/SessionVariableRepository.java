package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.SessionVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SessionVariable entity.
 * Provides database access for session variable management.
 */
@Repository
public interface SessionVariableRepository extends JpaRepository<SessionVariable, String> {

  /**
   * Find all variables for a session
   */
  @Query("SELECT sv FROM SessionVariable sv WHERE sv.sessionId = ?1")
  List<SessionVariable> findBySessionId(String sessionId);

  /**
   * Find a specific variable by session ID and variable name
   */
  @Query("SELECT sv FROM SessionVariable sv WHERE sv.sessionId = ?1 AND sv.variableName = ?2")
  Optional<SessionVariable> findBySessionIdAndVariableName(String sessionId, String variableName);

  /**
   * Delete all variables for a session
   */
  void deleteBySessionId(String sessionId);

  /**
   * Check if a variable exists
   */
  @Query("SELECT COUNT(sv) > 0 FROM SessionVariable sv WHERE sv.sessionId = ?1 AND sv.variableName = ?2")
  Boolean existsBySessionIdAndVariableName(String sessionId, String variableName);
}
