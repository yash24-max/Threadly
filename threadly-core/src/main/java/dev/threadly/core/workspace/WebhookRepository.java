package dev.threadly.core.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {

  List<Webhook> findAllByBotIdAndOrgIdAndActiveTrue(UUID botId, UUID orgId);

  List<Webhook> findAllByBotIdAndActiveTrue(UUID botId);

  Optional<Webhook> findByIdAndBotIdAndOrgId(UUID id, UUID botId, UUID orgId);
}
