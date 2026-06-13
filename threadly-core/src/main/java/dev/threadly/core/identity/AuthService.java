package dev.threadly.core.identity;

/**
 * Legacy auth service — replaced by Keycloak.
 *
 * All authentication operations (login, signup, token refresh, logout) are
 * now delegated to Keycloak. This stub exists to avoid breaking any
 * compile-time references while the codebase is being cleaned up.
 *
 * @deprecated Remove once all callers have been updated.
 */
@Deprecated
public final class AuthService {
  private AuthService() {}
}
