package dev.threadly.analytics.exception;

/**
 * Exception thrown when a requested dashboard is not found.
 */
public class DashboardNotFoundException extends RuntimeException {

    public DashboardNotFoundException(String dashboardId) {
        super("Dashboard not found: " + dashboardId);
    }

    public DashboardNotFoundException(String orgId, String viewName) {
        super("Dashboard not found for org: " + orgId + ", view: " + viewName);
    }

}
