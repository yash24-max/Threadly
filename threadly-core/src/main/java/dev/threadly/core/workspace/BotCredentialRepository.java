package dev.threadly.core.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotCredentialRepository extends JpaRepository<BotCredential, UUID> {

  List<BotCredential> findAllByBotIdAndOrgId(UUID botId, UUID orgId);

  Optional<BotCredential> findByIdAndBotIdAndOrgId(UUID id, UUID botId, UUID orgId);

  Optional<BotCredential> findByBotIdAndOrgIdAndName(UUID botId, UUID orgId, String name);
}
