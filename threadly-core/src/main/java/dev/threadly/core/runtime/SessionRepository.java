package dev.threadly.core.runtime;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionRepository extends JpaRepository<Session, UUID> {

  @Query("SELECT s FROM Session s WHERE s.bot.id = :botId AND s.visitorId = :visitorId")
  Optional<Session> findByBotIdAndVisitorId(UUID botId, String visitorId);
}
