package dev.threadly.analytics.controller;

import dev.threadly.analytics.dto.DashboardDto;
import dev.threadly.analytics.exception.DashboardNotFoundException;
import dev.threadly.analytics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for dashboard management endpoints.
 * Provides CRUD operations for custom analytics dashboards.
 */
@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get all dashboards for an organization.
     * GET /api/v1/dashboards
     */
    @GetMapping
    public ResponseEntity<List<DashboardDto>> getDashboards(
        @RequestHeader(value = "X-Org-ID") String orgId
    ) {
        try {
            log.debug("Fetching dashboards for org: {}", orgId);
            List<DashboardDto> dashboards = dashboardService.getDashboards(orgId);
            return ResponseEntity.ok(dashboards);
        } catch (Exception e) {
            log.error("Error fetching dashboards for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get a specific dashboard by ID.
     * GET /api/v1/dashboards/{dashboardId}
     */
    @GetMapping("/{dashboardId}")
    public ResponseEntity<?> getDashboard(
        @PathVariable String dashboardId
    ) {
        try {
            log.debug("Fetching dashboard: {}", dashboardId);
            DashboardDto dashboard = dashboardService.getDashboard(dashboardId);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardNotFoundException e) {
            log.warn("Dashboard not found: {}", dashboardId);
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching dashboard: {}", dashboardId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Create a new dashboard.
     * POST /api/v1/dashboards
     */
    @PostMapping
    public ResponseEntity<?> createDashboard(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @RequestBody DashboardDto dashboardDto
    ) {
        try {
            log.debug("Creating dashboard for org: {}", orgId);

            if (dashboardDto.getViewName() == null || dashboardDto.getViewName().isEmpty()) {
                return ResponseEntity.badRequest().body("view_name is required");
            }

            DashboardDto created = dashboardService.createDashboard(orgId, dashboardDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid dashboard creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating dashboard for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update an existing dashboard.
     * PUT /api/v1/dashboards/{dashboardId}
     */
    @PutMapping("/{dashboardId}")
    public ResponseEntity<?> updateDashboard(
        @PathVariable String dashboardId,
        @RequestBody DashboardDto dashboardDto
    ) {
        try {
            log.debug("Updating dashboard: {}", dashboardId);
            DashboardDto updated = dashboardService.updateDashboard(dashboardId, dashboardDto);
            return ResponseEntity.ok(updated);
        } catch (DashboardNotFoundException e) {
            log.warn("Dashboard not found: {}", dashboardId);
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating dashboard: {}", dashboardId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete a dashboard.
     * DELETE /api/v1/dashboards/{dashboardId}
     */
    @DeleteMapping("/{dashboardId}")
    public ResponseEntity<?> deleteDashboard(
        @PathVariable String dashboardId
    ) {
        try {
            log.debug("Deleting dashboard: {}", dashboardId);
            dashboardService.deleteDashboard(dashboardId);
            return ResponseEntity.noContent().build();
        } catch (DashboardNotFoundException e) {
            log.warn("Dashboard not found: {}", dashboardId);
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting dashboard: {}", dashboardId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get default dashboard for an organization.
     * GET /api/v1/dashboards/default
     */
    @GetMapping("/default/view")
    public ResponseEntity<?> getDefaultDashboard(
        @RequestHeader(value = "X-Org-ID") String orgId
    ) {
        try {
            log.debug("Fetching default dashboard for org: {}", orgId);
            DashboardDto dashboard = dashboardService.getDefaultDashboard(orgId);
            if (dashboard == null) {
                return ResponseEntity.status(404).body("No default dashboard found");
            }
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error fetching default dashboard for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

}
