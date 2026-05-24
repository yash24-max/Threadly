package dev.threadly.workspace.common;

/**
 * Thrown when a user is not authenticated.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  public static UnauthorizedException notAuthenticated() {
    return new UnauthorizedException("Authentication required");
  }
}
