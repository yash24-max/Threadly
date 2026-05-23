package dev.threadly.core.common;

import java.util.UUID;

public final class TenantContext {

  private static final ThreadLocal<UUID> CURRENT_ORG = new ThreadLocal<>();
  private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();
  private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(UUID orgId, UUID userId, String role) {
    CURRENT_ORG.set(orgId);
    CURRENT_USER.set(userId);
    CURRENT_ROLE.set(role);
  }

  public static UUID getOrgId() {
    UUID orgId = CURRENT_ORG.get();
    if (orgId == null) throw new IllegalStateException("No tenant context set on this thread");
    return orgId;
  }

  /** Returns the org ID or {@code null} if no tenant context is set. Does not throw. */
  public static UUID getOrgIdOrNull() {
    return CURRENT_ORG.get();
  }

  public static UUID getUserId() {
    return CURRENT_USER.get();
  }

  public static String getRole() {
    return CURRENT_ROLE.get();
  }

  public static void clear() {
    CURRENT_ORG.remove();
    CURRENT_USER.remove();
    CURRENT_ROLE.remove();
  }
}
