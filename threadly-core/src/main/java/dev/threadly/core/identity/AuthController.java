package dev.threadly.core.identity;

/**
 * Legacy auth controller — replaced by Keycloak.
 *
 * Login, signup, refresh, and logout are now handled entirely by Keycloak.
 * New user registration is handled by {@code dev.threadly.core.onboarding.OnboardingController}.
 *
 * This class is intentionally empty to preserve the package and avoid
 * breaking any compile-time references.
 */
public final class AuthController {
  private AuthController() {}
}
