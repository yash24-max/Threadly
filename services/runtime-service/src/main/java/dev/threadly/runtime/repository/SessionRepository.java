package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Session entity.
 * Provides database access for session management and queries.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

  /**
   * Find sessions by bot ID
   */
  @Query("SELECT s FROM Session s WHERE s.botId = ?1 AND s.state <> 'ENDED' ORDER BY s.lastMessageAt DESC")
  List<Session> findActiveSessionsByBotId(String botId);

  /**
   * Find sessions by visitor ID
   */
  @Query("SELECT s FROM Session s WHERE s.visitorId = ?1 AND s.state <> 'ENDED' ORDER BY s.lastMessageAt DESC")
  List<Session> findActiveSessionsByVisitorId(String visitorId);

  /**
   * Find sessions by flow ID and bot ID
   */
  @Query("SELECT s FROM Session s WHERE s.flowId = ?1 AND s.botId = ?2 ORDER BY s.createdAt DESC")
  List<Session> findSessionsByFlowIdAndBotId(String flowId, String botId);

  /**
   * Find paused sessions
   */
  @Query("SELECT s FROM Session s WHERE s.state = 'PAUSED' ORDER BY s.lastMessageAt ASC")
  List<Session> findPausedSessions();

  /**
   * Find sessions created after specific time
   */
  @Query("SELECT s FROM Session s WHERE s.createdAt >= ?1 ORDER BY s.createdAt DESC")
  List<Session> findSessionsCreatedAfter(LocalDateTime createdAfter);

  /**
   * Count active sessions for a bot
   */
  @Query("SELECT COUNT(s) FROM Session s WHERE s.botId = ?1 AND s.state <> 'ENDED'")
  Long countActiveSessionsByBotId(String botId);
}
