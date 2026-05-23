package dev.threadly.core.common;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orgs/{orgId}/audit-log")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Organisation audit trail")
public class AuditLogController {

  private final AuditLogRepository auditLogRepository;

  @GetMapping
  @Operation(summary = "List audit log entries for an org (paginated, optionally filtered by resourceType)")
  public Page<AuditLog> listAuditLog(
      @PathVariable UUID orgId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String resourceType) {

    UUID contextOrgId = TenantContext.getOrgId();
    if (!contextOrgId.equals(orgId)) {
      throw new SecurityException("Access denied to org: " + orgId);
    }

    Pageable pageable = PageRequest.of(page, Math.min(size, 100));

    if (resourceType != null && !resourceType.isBlank()) {
      return auditLogRepository.findByOrgIdAndResourceTypeOrderByCreatedAtDesc(
          orgId, resourceType.toUpperCase(), pageable);
    }
    return auditLogRepository.findByOrgIdOrderByCreatedAtDesc(orgId, pageable);
  }
}
