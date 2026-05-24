package dev.threadly.workspace.common;

/**
 * Thrown when a user is authenticated but lacks permission for the requested action.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ForbiddenException insufficientPermissions(String resource) {
    return new ForbiddenException("You do not have permission to access: " + resource);
  }

  public static ForbiddenException crossTenantAccess(String resource) {
    return new ForbiddenException("Cross-tenant access is not allowed: " + resource);
  }
}
