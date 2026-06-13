package dev.threadly.core.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BotRepository extends JpaRepository<Bot, UUID> {

  List<Bot> findAllByOrgId(UUID orgId);

  @Query("SELECT b FROM Bot b WHERE b.id = :id AND b.org.id = :orgId")
  Optional<Bot> findByIdAndOrgId(UUID id, UUID orgId);

  boolean existsByIdAndOrgId(UUID id, UUID orgId);
}
