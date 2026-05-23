package dev.threadly.core.common;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Automatically applies Hibernate tenant filters on every repository call
 * so no query can leak data across org boundaries.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

  private final EntityManager entityManager;

  @Around("execution(* dev.threadly.core..*.Repository*.*(..))"
      + " || execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
  public Object applyTenantFilter(ProceedingJoinPoint pjp) throws Throwable {
    UUID orgId = TenantContext.getOrgId();
    if (orgId == null) {
      return pjp.proceed();
    }

    Session hibernateSession = entityManager.unwrap(Session.class);
    try {
      for (String filterName : new String[]{
          "orgFilter", "orgFilterKb", "orgFilterBot",
          "orgFilterApiKey", "orgFilterWebhook", "orgFilterCredential"}) {
        try {
          hibernateSession.enableFilter(filterName).setParameter("orgId", orgId);
        } catch (Exception ignored) {
          // Filter not defined on this entity — skip silently
        }
      }
      return pjp.proceed();
    } finally {
      for (String filterName : new String[]{
          "orgFilter", "orgFilterKb", "orgFilterBot",
          "orgFilterApiKey", "orgFilterWebhook", "orgFilterCredential"}) {
        try { hibernateSession.disableFilter(filterName); } catch (Exception ignored) {}
      }
    }
  }
}
