package dev.threadly.common.context;

import java.util.UUID;

/**
 * Thread-local holder for current organization (tenant) context.
 * Automatically extracted from JWT claims by SecurityConfig.
 */
public class TenantContext {
  private static final ThreadLocal<UUID> tenantId = new ThreadLocal<>();
  private static final ThreadLocal<UUID> userId = new ThreadLocal<>();
  private static final ThreadLocal<String> email = new ThreadLocal<>();

  /**
   * Set current tenant (org) ID.
   */
  public static void setTenantId(UUID orgId) {
    tenantId.set(orgId);
  }

  /**
   * Get current tenant (org) ID. Throws if not set.
   */
  public static UUID getTenantId() {
    UUID id = tenantId.get();
    if (id == null) {
      throw new IllegalStateException("Tenant context not set. Ensure authentication filter has run.");
    }
    return id;
  }

  /**
   * Get current tenant ID, or null if not set.
   */
  public static UUID getTenantIdOptional() {
    return tenantId.get();
  }

  /**
   * Set current user ID.
   */
  public static void setUserId(UUID userId) {
    TenantContext.userId.set(userId);
  }

  /**
   * Get current user ID. Throws if not set.
   */
  public static UUID getUserId() {
    UUID id = userId.get();
    if (id == null) {
      throw new IllegalStateException("User context not set. Ensure authentication filter has run.");
    }
    return id;
  }

  /**
   * Set current user email.
   */
  public static void setEmail(String userEmail) {
    email.set(userEmail);
  }

  /**
   * Get current user email. Throws if not set.
   */
  public static String getEmail() {
    String e = email.get();
    if (e == null) {
      throw new IllegalStateException("Email context not set. Ensure authentication filter has run.");
    }
    return e;
  }

  /**
   * Clear all context variables (called after request processing).
   */
  public static void clear() {
    tenantId.remove();
    userId.remove();
    email.remove();
  }
}
