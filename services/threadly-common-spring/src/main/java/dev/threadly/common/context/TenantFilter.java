package dev.threadly.common.context;

import java.util.UUID;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.stereotype.Component;

/**
 * Hibernate filter definition for automatic org_id filtering.
 *
 * Usage in entity:
 * @Entity
 * @FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "orgId", type = "java.util.UUID"))
 * @Filter(name = "tenantFilter", condition = "org_id = :orgId")
 * public class Bot { ... }
 */
@Component
public class TenantFilter {

  /**
   * Enable tenant filter for current session if tenant context is set.
   * Call this in SessionFactory or Session configuration.
   */
  public static void enableTenantFilter(Session session) {
    UUID tenantId = TenantContext.getTenantIdOptional();
    if (tenantId != null) {
      Filter filter = session.enableFilter("tenantFilter");
      filter.setParameter("orgId", tenantId);
    }
  }

  /**
   * Disable all filters (useful for admin operations).
   */
  public static void disableAllFilters(Session session) {
    session.disableFilter("tenantFilter");
  }
}
