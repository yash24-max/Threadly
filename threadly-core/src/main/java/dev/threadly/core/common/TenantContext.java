package dev.threadly.core.common;

import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Per-request tenant context backed by Spring's RequestContextHolder.
 *
 * <p>Replaces ThreadLocal storage with Spring's request-attribute store so
 * the context is scoped to the HTTP request lifecycle and automatically
 * cleaned up by the Servlet container — no manual {@code clear()} required.
 *
 * <p>The static API is intentionally preserved so existing service code
 * does not need modification.
 */
public final class TenantContext {

    private static final String ORG_KEY  = "tc.orgId";
    private static final String USER_KEY = "tc.userId";
    private static final String ROLE_KEY = "tc.role";

    private TenantContext() {}

    // ── writers ─────────────────────────────────────────────────────────────

    public static void set(UUID orgId, UUID userId, String role) {
        RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
        attrs.setAttribute(ORG_KEY,  orgId,  RequestAttributes.SCOPE_REQUEST);
        attrs.setAttribute(USER_KEY, userId, RequestAttributes.SCOPE_REQUEST);
        attrs.setAttribute(ROLE_KEY, role,   RequestAttributes.SCOPE_REQUEST);
    }

    // ── readers ─────────────────────────────────────────────────────────────

    public static UUID getOrgId() {
        Object val = attr(ORG_KEY);
        if (val == null) throw new IllegalStateException("No tenant context — JWT filter may not have run");
        return (UUID) val;
    }

    public static UUID getUserId() {
        return (UUID) attr(USER_KEY);
    }

    public static String getRole() {
        return (String) attr(ROLE_KEY);
    }

    // ── cleanup (optional — Spring cleans request attrs automatically) ───────

    /**
     * Explicitly removes the tenant attributes from the current request.
     * Calling this is optional because Spring's DispatcherServlet clears all
     * request attributes after the request completes; kept for API compatibility.
     */
    public static void clear() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.removeAttribute(ORG_KEY,  RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(USER_KEY, RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(ROLE_KEY, RequestAttributes.SCOPE_REQUEST);
        }
    }

    // ── private helper ───────────────────────────────────────────────────────

    private static Object attr(String key) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getAttribute(key, RequestAttributes.SCOPE_REQUEST) : null;
    }
}
