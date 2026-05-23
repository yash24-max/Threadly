package dev.threadly.core.conversation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);

  long countByConversationId(UUID conversationId);

  /** Median latency in ms for AI messages in the given org — used for p50 stat. */
  @Query(value = """
      SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY m.latency_ms)
      FROM messages m
      JOIN conversations c ON c.id = m.conversation_id
      WHERE c.org_id = :orgId AND m.role = 'ai' AND m.latency_ms IS NOT NULL
      """, nativeQuery = true)
  Double p50LatencyByOrgId(@Param("orgId") UUID orgId);
}
