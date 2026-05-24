package dev.threadly.identity.controller;

import dev.threadly.identity.dto.OrganizationDto;
import dev.threadly.identity.entity.Organization;
import dev.threadly.identity.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for organization management endpoints.
 * Handles organization CRUD operations and member management.
 */
@Slf4j
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  /**
   * Create a new organization.
   *
   * @param name organization name
   * @param userId the user ID of the owner (injected from auth context)
   * @return created OrganizationDto
   */
  @PostMapping
  public ResponseEntity<OrganizationDto> createOrganization(
      @RequestParam String name,
      @RequestHeader("X-User-Id") String userId) {

    log.info("Creating organization: {} with owner: {}", name, userId);

    Organization org = organizationService.createOrganization(name, userId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationService.toDto(org));
  }

  /**
   * Get an organization by ID.
   *
   * @param orgId the organization ID
   * @return OrganizationDto with organization details
   */
  @GetMapping("/{orgId}")
  public ResponseEntity<OrganizationDto> getOrganization(@PathVariable String orgId) {

    log.debug("Fetching organization: {}", orgId);

    Organization org = organizationService.getOrganizationById(orgId);

    return ResponseEntity.ok(organizationService.toDto(org));
  }

  /**
   * Get all organizations for the current user.
   *
   * @param userId the current user ID (injected from auth context)
   * @return list of OrganizationDto
   */
  @GetMapping
  public ResponseEntity<List<OrganizationDto>> getUserOrganizations(
      @RequestHeader("X-User-Id") String userId) {

    log.debug("Fetching organizations for user: {}", userId);

    List<Organization> orgs = organizationService.getUserOrganizations(userId);

    return ResponseEntity.ok(organizationService.toDto(orgs));
  }

  /**
   * Update organization details.
   *
   * @param orgId the organization ID
   * @param name new name (optional)
   * @param description new description (optional)
   * @param website new website URL (optional)
   * @param logoUrl new logo URL (optional)
   * @return updated OrganizationDto
   */
  @PatchMapping("/{orgId}")
  public ResponseEntity<OrganizationDto> updateOrganization(
      @PathVariable String orgId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String website,
      @RequestParam(required = false) String logoUrl) {

    log.info("Updating organization: {}", orgId);

    Organization updated = organizationService.updateOrganization(
        orgId,
        name,
        description,
        website,
        logoUrl
    );

    return ResponseEntity.ok(organizationService.toDto(updated));
  }

  /**
   * Update organization billing plan.
   *
   * @param orgId the organization ID
   * @param plan the new plan type (FREE, PRO, ENTERPRISE)
   * @return updated OrganizationDto
   */
  @PatchMapping("/{orgId}/plan")
  public ResponseEntity<Void> updatePlan(
      @PathVariable String orgId,
      @RequestParam String plan) {

    log.info("Updating plan for organization: {} to {}", orgId, plan);
    organizationService.updatePlan(orgId, plan);

    return ResponseEntity.noContent().build();
  }

  /**
   * Invite a user to the organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID to invite
   * @param role the role to assign (ADMIN, MEMBER, GUEST)
   * @return success response
   */
  @PostMapping("/{orgId}/members/{userId}")
  public ResponseEntity<Void> inviteUserToOrg(
      @PathVariable String orgId,
      @PathVariable String userId,
      @RequestParam String role) {

    log.info("Inviting user: {} to org: {} with role: {}", userId, orgId, role);

    organizationService.inviteUserToOrg(orgId, userId, role);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Remove a user from the organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID to remove
   * @return success response
   */
  @DeleteMapping("/{orgId}/members/{userId}")
  public ResponseEntity<Void> removeUserFromOrg(
      @PathVariable String orgId,
      @PathVariable String userId) {

    log.info("Removing user: {} from org: {}", userId, orgId);

    organizationService.removeUserFromOrg(orgId, userId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Update a user's role in the organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param newRole the new role
   * @return success response
   */
  @PatchMapping("/{orgId}/members/{userId}/role")
  public ResponseEntity<Void> updateUserRole(
      @PathVariable String orgId,
      @PathVariable String userId,
      @RequestParam String newRole) {

    log.info("Updating role for user: {} in org: {} to {}", userId, orgId, newRole);

    organizationService.updateUserRole(orgId, userId, newRole);

    return ResponseEntity.noContent().build();
  }

  /**
   * Deactivate an organization.
   *
   * @param orgId the organization ID
   * @return success response
   */
  @PostMapping("/{orgId}/deactivate")
  public ResponseEntity<Void> deactivateOrganization(@PathVariable String orgId) {

    log.info("Deactivating organization: {}", orgId);
    organizationService.deactivateOrganization(orgId);

    return ResponseEntity.noContent().build();
  }
}
