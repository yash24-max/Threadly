package dev.threadly.core.workspace;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgRepository extends JpaRepository<Org, UUID> {

  Optional<Org> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
