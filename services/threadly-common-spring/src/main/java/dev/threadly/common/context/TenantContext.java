package dev.threadly.common.context;

import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Per-request tenant context backed by Spring's {@link RequestContextHolder}.
 *
 * <p>Replaces the previous ThreadLocal implementation so tenant data is bound
 * to the HTTP request's own attribute store. Spring's DispatcherServlet
 * automatically cleans up request attributes at end of each request — no
 * manual {@code clear()} calls needed, and no thread-leak risk.
 *
 * <p>The static API is preserved so existing service and controller code does
 * not need modification.
 */
public final class TenantContext {

    private static final String TENANT_KEY = "tc.tenantId";
    private static final String USER_KEY   = "tc.userId";
    private static final String EMAIL_KEY  = "tc.email";
    private static final String ROLE_KEY   = "tc.role";

    private TenantContext() {}

    // ── writers ─────────────────────────────────────────────────────────────

    public static void setTenantId(UUID orgId) {
        set(TENANT_KEY, orgId);
    }

    public static void setUserId(UUID userId) {
        set(USER_KEY, userId);
    }

    public static void setEmail(String email) {
        set(EMAIL_KEY, email);
    }

    public static void setRole(String role) {
        set(ROLE_KEY, role);
    }

    // ── readers ─────────────────────────────────────────────────────────────

    public static UUID getTenantId() {
        UUID id = (UUID) get(TENANT_KEY);
        if (id == null) throw new IllegalStateException(
                "Tenant context not set. Ensure authentication filter has run.");
        return id;
    }

    /** Returns {@code null} if tenant context has not been set (e.g., public endpoints). */
    public static UUID getTenantIdOptional() {
        return (UUID) get(TENANT_KEY);
    }

    public static UUID getUserId() {
        UUID id = (UUID) get(USER_KEY);
        if (id == null) throw new IllegalStateException(
                "User context not set. Ensure authentication filter has run.");
        return id;
    }

    public static String getEmail() {
        String e = (String) get(EMAIL_KEY);
        if (e == null) throw new IllegalStateException(
                "Email context not set. Ensure authentication filter has run.");
        return e;
    }

    public static String getRole() {
        return (String) get(ROLE_KEY);
    }

    // ── cleanup (optional — Spring cleans request attrs automatically) ───────

    /**
     * Explicitly removes all tenant attributes from the current request.
     * Kept for API compatibility; calling this is optional.
     */
    public static void clear() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.removeAttribute(TENANT_KEY, RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(USER_KEY,   RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(EMAIL_KEY,  RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(ROLE_KEY,   RequestAttributes.SCOPE_REQUEST);
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static void set(String key, Object value) {
        RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
        attrs.setAttribute(key, value, RequestAttributes.SCOPE_REQUEST);
    }

    private static Object get(String key) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getAttribute(key, RequestAttributes.SCOPE_REQUEST) : null;
    }
}
