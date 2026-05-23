package dev.threadly.core.common;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  Page<AuditLog> findByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);

  Page<AuditLog> findByOrgIdAndResourceTypeOrderByCreatedAtDesc(
      UUID orgId, String resourceType, Pageable pageable);
}
