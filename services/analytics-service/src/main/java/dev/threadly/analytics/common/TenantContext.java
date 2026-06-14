package dev.threadly.analytics.common;

import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public final class TenantContext {
    private static final String ORG_KEY  = "tc.orgId";
    private static final String USER_KEY = "tc.userId";
    private static final String ROLE_KEY = "tc.role";
    private TenantContext() {}

    public static void set(UUID orgId, UUID userId, String role) {
        RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
        attrs.setAttribute(ORG_KEY,  orgId,  RequestAttributes.SCOPE_REQUEST);
        attrs.setAttribute(USER_KEY, userId, RequestAttributes.SCOPE_REQUEST);
        attrs.setAttribute(ROLE_KEY, role,   RequestAttributes.SCOPE_REQUEST);
    }
    public static UUID getOrgId() {
        Object val = attr(ORG_KEY);
        if (val == null) throw new IllegalStateException("No tenant context");
        return (UUID) val;
    }
    public static UUID getOrgIdOptional() { return (UUID) attr(ORG_KEY); }
    public static UUID getUserId()        { return (UUID) attr(USER_KEY); }
    public static String getRole()        { return (String) attr(ROLE_KEY); }
    public static void clear() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.removeAttribute(ORG_KEY,  RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(USER_KEY, RequestAttributes.SCOPE_REQUEST);
            attrs.removeAttribute(ROLE_KEY, RequestAttributes.SCOPE_REQUEST);
        }
    }
    private static Object attr(String key) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getAttribute(key, RequestAttributes.SCOPE_REQUEST) : null;
    }
}
