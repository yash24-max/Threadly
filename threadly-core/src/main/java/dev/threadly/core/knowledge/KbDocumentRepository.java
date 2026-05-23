package dev.threadly.core.knowledge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KbDocumentRepository extends JpaRepository<KbDocument, UUID> {

  List<KbDocument> findAllByBotId(UUID botId);

  Optional<KbDocument> findByIdAndBotId(UUID id, UUID botId);

  List<KbDocument> findTop20ByStatusOrderByCreatedAtAsc(String status);
}
