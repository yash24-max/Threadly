package dev.threadly.analytics.repository;

import dev.threadly.analytics.entity.DashboardView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DashboardView persistence.
 * Handles storage and retrieval of user-created custom dashboards.
 */
@Repository
public interface DashboardViewRepository extends JpaRepository<DashboardView, String> {

    /**
     * Find all dashboards for an organization.
     *
     * @param orgId organization identifier
     * @return list of dashboard views ordered by creation date
     */
    @Query("SELECT d FROM DashboardView d WHERE d.orgId = :orgId ORDER BY d.createdAt DESC")
    List<DashboardView> findByOrgId(@Param("orgId") String orgId);

    /**
     * Find a dashboard by name for an organization.
     *
     * @param orgId organization identifier
     * @param viewName dashboard view name
     * @return optional dashboard view
     */
    Optional<DashboardView> findByOrgIdAndViewName(String orgId, String viewName);

    /**
     * Find the default dashboard for an organization.
     *
     * @param orgId organization identifier
     * @return optional default dashboard
     */
    @Query("SELECT d FROM DashboardView d WHERE d.orgId = :orgId AND d.isDefault = true")
    Optional<DashboardView> findDefaultDashboard(@Param("orgId") String orgId);

    /**
     * Find all custom (non-default) dashboards for an organization.
     *
     * @param orgId organization identifier
     * @return list of custom dashboard views
     */
    @Query("SELECT d FROM DashboardView d WHERE d.orgId = :orgId AND d.isDefault = false " +
           "ORDER BY d.createdAt DESC")
    List<DashboardView> findCustomDashboards(@Param("orgId") String orgId);

    /**
     * Check if a dashboard with the given name exists for an organization.
     *
     * @param orgId organization identifier
     * @param viewName dashboard view name
     * @return true if dashboard exists
     */
    boolean existsByOrgIdAndViewName(String orgId, String viewName);

}
