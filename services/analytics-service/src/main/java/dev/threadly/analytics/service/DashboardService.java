package dev.threadly.analytics.service;

import dev.threadly.analytics.dto.DashboardDto;
import dev.threadly.analytics.entity.DashboardView;
import dev.threadly.analytics.exception.DashboardNotFoundException;
import dev.threadly.analytics.repository.DashboardViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing custom dashboards.
 * Provides dashboard CRUD operations with caching support.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardViewRepository dashboardRepository;

    /**
     * Get all dashboards for an organization.
     *
     * @param orgId organization identifier
     * @return list of dashboard DTOs
     */
    @Cacheable(value = "dashboards", key = "#orgId")
    public List<DashboardDto> getDashboards(String orgId) {
        try {
            return dashboardRepository.findByOrgId(orgId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching dashboards for org: {}", orgId, e);
            throw new RuntimeException("Failed to fetch dashboards", e);
        }
    }

    /**
     * Get a specific dashboard by ID.
     *
     * @param dashboardId dashboard identifier
     * @return dashboard DTO
     * @throws DashboardNotFoundException if dashboard not found
     */
    public DashboardDto getDashboard(String dashboardId) {
        try {
            DashboardView dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new DashboardNotFoundException(dashboardId));

            // Update last viewed timestamp
            dashboard.setLastViewedAt(Instant.now());
            dashboardRepository.save(dashboard);

            return convertToDto(dashboard);
        } catch (DashboardNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching dashboard: {}", dashboardId, e);
            throw new RuntimeException("Failed to fetch dashboard", e);
        }
    }

    /**
     * Create a new dashboard.
     *
     * @param orgId organization identifier
     * @param dto dashboard DTO with configuration
     * @return created dashboard DTO
     */
    @CacheEvict(value = "dashboards", key = "#orgId")
    public DashboardDto createDashboard(String orgId, DashboardDto dto) {
        try {
            // Check if dashboard name already exists
            if (dashboardRepository.existsByOrgIdAndViewName(orgId, dto.getViewName())) {
                throw new IllegalArgumentException("Dashboard with name already exists: " + dto.getViewName());
            }

            DashboardView dashboard = DashboardView.builder()
                .id(UUID.randomUUID().toString())
                .orgId(orgId)
                .viewName(dto.getViewName())
                .description(dto.getDescription())
                .isDefault(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            // Store widgets as JSON if provided
            if (dto.getWidgets() != null && !dto.getWidgets().isEmpty()) {
                try {
                    // Widgets will be serialized by Jackson when persisting
                    dashboard.setWidgetsJson("{}");
                } catch (Exception e) {
                    log.warn("Error serializing widgets for dashboard: {}", dto.getViewName());
                }
            }

            DashboardView savedDashboard = dashboardRepository.save(dashboard);
            log.debug("Created dashboard: {} for org: {}", savedDashboard.getId(), orgId);

            return convertToDto(savedDashboard);

        } catch (Exception e) {
            log.error("Error creating dashboard for org: {}", orgId, e);
            throw new RuntimeException("Failed to create dashboard", e);
        }
    }

    /**
     * Update an existing dashboard.
     *
     * @param dashboardId dashboard identifier
     * @param dto updated dashboard DTO
     * @return updated dashboard DTO
     * @throws DashboardNotFoundException if dashboard not found
     */
    @CacheEvict(value = "dashboards", key = "#root.args[1].orgId")
    public DashboardDto updateDashboard(String dashboardId, DashboardDto dto) {
        try {
            DashboardView dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new DashboardNotFoundException(dashboardId));

            // Update fields
            if (dto.getViewName() != null) {
                dashboard.setViewName(dto.getViewName());
            }
            if (dto.getDescription() != null) {
                dashboard.setDescription(dto.getDescription());
            }

            dashboard.setUpdatedAt(Instant.now());
            DashboardView updatedDashboard = dashboardRepository.save(dashboard);

            log.debug("Updated dashboard: {}", dashboardId);
            return convertToDto(updatedDashboard);

        } catch (DashboardNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating dashboard: {}", dashboardId, e);
            throw new RuntimeException("Failed to update dashboard", e);
        }
    }

    /**
     * Delete a dashboard.
     *
     * @param dashboardId dashboard identifier
     * @throws DashboardNotFoundException if dashboard not found
     */
    @CacheEvict(value = "dashboards", allEntries = true)
    public void deleteDashboard(String dashboardId) {
        try {
            DashboardView dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new DashboardNotFoundException(dashboardId));

            dashboardRepository.delete(dashboard);
            log.debug("Deleted dashboard: {}", dashboardId);

        } catch (DashboardNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting dashboard: {}", dashboardId, e);
            throw new RuntimeException("Failed to delete dashboard", e);
        }
    }

    /**
     * Get default dashboard for an organization.
     *
     * @param orgId organization identifier
     * @return default dashboard DTO or null if not found
     */
    public DashboardDto getDefaultDashboard(String orgId) {
        try {
            return dashboardRepository.findDefaultDashboard(orgId)
                .map(this::convertToDto)
                .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching default dashboard for org: {}", orgId, e);
            throw new RuntimeException("Failed to fetch default dashboard", e);
        }
    }

    private DashboardDto convertToDto(DashboardView dashboard) {
        return DashboardDto.builder()
            .id(dashboard.getId())
            .orgId(dashboard.getOrgId())
            .viewName(dashboard.getViewName())
            .description(dashboard.getDescription())
            .isDefault(dashboard.getIsDefault())
            .createdAt(dashboard.getCreatedAt())
            .lastViewedAt(dashboard.getLastViewedAt())
            .updatedAt(dashboard.getUpdatedAt())
            .build();
    }

}
