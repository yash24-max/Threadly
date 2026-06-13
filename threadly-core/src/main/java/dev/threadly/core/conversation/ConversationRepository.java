package dev.threadly.core.conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  Page<Conversation> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);

  @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.bot.id = :botId ORDER BY c.createdAt DESC")
  Page<Conversation> findByOrgIdAndBotId(UUID orgId, UUID botId, Pageable pageable);

  Optional<Conversation> findByIdAndOrgId(UUID id, UUID orgId);

  long countByOrgIdAndStatus(UUID orgId, String status);

  @Query(value = "SELECT COALESCE(COUNT(m.id), 0) FROM messages m JOIN conversations c ON m.conversation_id = c.id WHERE c.org_id = :orgId", nativeQuery = true)
  long sumMessageCountByOrgId(@Param("orgId") UUID orgId);
}
