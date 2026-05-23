package dev.threadly.core.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes audit entries asynchronously to the {@code audit_log} table.
 *
 * <p>Obtains org/user context from {@link TenantContext} (set by the auth filter) and the
 * current HTTP request (for IP / User-Agent headers).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;

  /**
   * Records an audit event for the given action + resource.
   *
   * @param action       e.g. BOT_CREATED, FLOW_PUBLISHED
   * @param resourceType e.g. BOT, FLOW, MEMBER
   * @param resourceId   UUID of the affected resource (nullable)
   * @param oldValue     object representing old state (serialised to JSON; nullable)
   * @param newValue     object representing new state (serialised to JSON; nullable)
   */
  @Async
  public void log(String action, String resourceType, UUID resourceId,
      Object oldValue, Object newValue) {
    try {
      UUID orgId = TenantContext.getOrgIdOrNull();
      UUID userId = TenantContext.getUserId();

      if (orgId == null) {
        // Best-effort: if no tenant context (e.g. async thread), skip silently
        log.debug("AuditService: no org context for action={} resourceType={}", action, resourceType);
        return;
      }

      String ipAddress = null;
      String userAgent = null;
      try {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        ipAddress = resolveClientIp(request);
        userAgent = request.getHeader("User-Agent");
      } catch (Exception ignored) {
        // RequestContextHolder not available in async threads — that is acceptable
      }

      AuditLog entry = AuditLog.builder()
          .orgId(orgId)
          .userId(userId)
          .action(action)
          .resourceType(resourceType)
          .resourceId(resourceId)
          .oldValue(toJson(oldValue))
          .newValue(toJson(newValue))
          .ipAddress(ipAddress)
          .userAgent(userAgent)
          .build();

      auditLogRepository.save(entry);

    } catch (Exception e) {
      log.error("AuditService: failed to write audit log for action={}", action, e);
    }
  }

  private String toJson(Object value) {
    if (value == null) return null;
    if (value instanceof String s) return s;
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("AuditService: could not serialise value to JSON: {}", e.getMessage());
      return value.toString();
    }
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
