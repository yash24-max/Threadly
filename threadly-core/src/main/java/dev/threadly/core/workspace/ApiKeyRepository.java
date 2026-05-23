package dev.threadly.core.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

  List<ApiKey> findAllByBotIdAndOrgIdAndRevokedAtIsNull(UUID botId, UUID orgId);

  Optional<ApiKey> findByIdAndBotIdAndOrgId(UUID id, UUID botId, UUID orgId);

  Optional<ApiKey> findByKeyLookupHashAndRevokedAtIsNull(String keyLookupHash);
}
