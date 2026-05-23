package dev.threadly.core.flow;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FlowRepository extends JpaRepository<Flow, UUID> {

  @Query("SELECT f FROM Flow f WHERE f.bot.id = :botId AND f.org.id = :orgId")
  Optional<Flow> findByBotIdAndOrgId(UUID botId, UUID orgId);

  @Query("SELECT f FROM Flow f WHERE f.id = :flowId AND f.bot.id = :botId AND f.org.id = :orgId")
  Optional<Flow> findByIdAndBotIdAndOrgId(UUID flowId, UUID botId, UUID orgId);
}
