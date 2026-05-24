package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.ConversationMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ConversationMemory entity.
 * Provides database access for conversation memory management.
 */
@Repository
public interface ConversationMemoryRepository extends JpaRepository<ConversationMemory, String> {

  /**
   * Find conversation memory by session ID
   */
  @Query("SELECT cm FROM ConversationMemory cm WHERE cm.sessionId = ?1")
  Optional<ConversationMemory> findBySessionId(String sessionId);

  /**
   * Delete conversation memory by session ID
   */
  void deleteBySessionId(String sessionId);
}
