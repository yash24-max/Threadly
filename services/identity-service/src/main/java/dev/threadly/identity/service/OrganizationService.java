package dev.threadly.identity.service;

import dev.threadly.identity.dto.OrganizationDto;
import dev.threadly.identity.entity.Membership;
import dev.threadly.identity.entity.Organization;
import dev.threadly.identity.event.EventPublisher;
import dev.threadly.identity.exception.ResourceNotFoundException;
import dev.threadly.identity.repository.MembershipRepository;
import dev.threadly.identity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing organizations (tenants).
 * Handles organization creation, updates, member management, and member invitations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final MembershipRepository membershipRepository;
  private final EventPublisher eventPublisher;

  /**
   * Creates a new organization.
   *
   * @param name organization name
   * @param ownerId user ID of the organization owner
   * @return newly created Organization entity
   */
  @CacheEvict(value = "organizations", allEntries = true)
  public Organization createOrganization(String name, String ownerId) {
    Organization organization = Organization.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
        .ownerId(ownerId)
        .plan("FREE")
        .active(true)
        .build();

    Organization saved = organizationRepository.save(organization);

    Membership ownerMembership = Membership.builder()
        .id(UUID.randomUUID().toString())
        .userId(ownerId)
        .orgId(saved.getId())
        .role("OWNER")
        .teamIds("")
        .active(true)
        .build();

    membershipRepository.save(ownerMembership);
    log.info("Created organization: {} with owner: {}", saved.getId(), ownerId);

    eventPublisher.publishOrganizationCreated(saved.getId(), saved.getName(), ownerId, saved.getPlan());

    return saved;
  }

  /**
   * Gets an organization by ID.
   *
   * @param orgId the organization ID
   * @return Organization entity
   * @throws ResourceNotFoundException if organization not found
   */
  @Cacheable(value = "organizations", key = "#orgId", unless = "#result == null")
  @Transactional(readOnly = true)
  public Organization getOrganizationById(String orgId) {
    return organizationRepository.findById(orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization", orgId));
  }

  /**
   * Gets all organizations owned by a user.
   *
   * @param ownerId the owner's user ID
   * @return list of organizations
   */
  @Transactional(readOnly = true)
  public List<Organization> getOrganizationsByOwner(String ownerId) {
    return organizationRepository.findActiveByOwnerId(ownerId);
  }

  /**
   * Gets all organizations the user is a member of.
   *
   * @param userId the user ID
   * @return list of organizations
   */
  @Transactional(readOnly = true)
  public List<Organization> getUserOrganizations(String userId) {
    List<Membership> memberships = membershipRepository.findActiveByUserId(userId);
    return memberships.stream()
        .map(m -> getOrganizationById(m.getOrgId()))
        .collect(Collectors.toList());
  }

  /**
   * Updates organization details.
   *
   * @param orgId the organization ID
   * @param name new name (optional)
   * @param description new description (optional)
   * @param website new website URL (optional)
   * @param logoUrl new logo URL (optional)
   * @return updated Organization entity
   */
  @CacheEvict(value = "organizations", key = "#orgId")
  public Organization updateOrganization(String orgId, String name, String description,
      String website, String logoUrl) {
    Organization org = getOrganizationById(orgId);

    if (name != null && !name.isBlank()) {
      org.setName(name);
    }
    if (description != null) {
      org.setDescription(description);
    }
    if (website != null) {
      org.setWebsite(website);
    }
    if (logoUrl != null) {
      org.setLogoUrl(logoUrl);
    }

    Organization updated = organizationRepository.save(org);
    log.info("Updated organization: {}", orgId);

    return updated;
  }

  /**
   * Updates organization billing plan.
   *
   * @param orgId the organization ID
   * @param plan new plan type (FREE, PRO, ENTERPRISE)
   */
  @CacheEvict(value = "organizations", key = "#orgId")
  public void updatePlan(String orgId, String plan) {
    Organization org = getOrganizationById(orgId);
    org.setPlan(plan);
    organizationRepository.save(org);
    log.info("Updated plan for organization: {} to {}", orgId, plan);
  }

  /**
   * Deactivates an organization.
   *
   * @param orgId the organization ID
   */
  @CacheEvict(value = "organizations", key = "#orgId")
  public void deactivateOrganization(String orgId) {
    Organization org = getOrganizationById(orgId);
    org.setActive(false);
    organizationRepository.save(org);
    log.info("Deactivated organization: {}", orgId);
  }

  /**
   * Invites a user to the organization.
   * Creates a membership record for the user in the organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID to invite
   * @param role the role to assign (ADMIN, MEMBER, GUEST)
   * @return created Membership entity
   */
  public Membership inviteUserToOrg(String orgId, String userId, String role) {
    getOrganizationById(orgId);

    if (membershipRepository.findByUserIdAndOrgId(userId, orgId).isPresent()) {
      log.warn("User {} is already a member of org {}", userId, orgId);
      throw new RuntimeException("User is already a member of this organization");
    }

    Membership membership = Membership.builder()
        .id(UUID.randomUUID().toString())
        .userId(userId)
        .orgId(orgId)
        .role(role)
        .teamIds("")
        .active(true)
        .build();

    Membership saved = membershipRepository.save(membership);
    log.info("Invited user: {} to org: {} with role: {}", userId, orgId, role);

    return saved;
  }

  /**
   * Gets all active members of an organization.
   *
   * @param orgId the organization ID
   * @return list of memberships
   */
  @Transactional(readOnly = true)
  public List<Membership> getOrgMembers(String orgId) {
    return membershipRepository.findActiveByOrgId(orgId);
  }

  /**
   * Counts active members in an organization.
   *
   * @param orgId the organization ID
   * @return count of active members
   */
  @Transactional(readOnly = true)
  public long countOrgMembers(String orgId) {
    return membershipRepository.countActiveByOrgId(orgId);
  }

  /**
   * Removes a user from an organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   */
  public void removeUserFromOrg(String orgId, String userId) {
    Membership membership = membershipRepository.findByUserIdAndOrgId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Membership", userId + "_" + orgId));

    membership.setActive(false);
    membershipRepository.save(membership);
    log.info("Removed user: {} from org: {}", userId, orgId);
  }

  /**
   * Updates a user's role in an organization.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param newRole the new role
   */
  public void updateUserRole(String orgId, String userId, String newRole) {
    Membership membership = membershipRepository.findByUserIdAndOrgId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Membership", userId + "_" + orgId));

    membership.setRole(newRole);
    membershipRepository.save(membership);
    log.info("Updated role for user: {} in org: {} to {}", userId, orgId, newRole);
  }

  /**
   * Converts an Organization entity to OrganizationDto for API responses.
   *
   * @param org the Organization entity
   * @return OrganizationDto with non-sensitive information
   */
  public OrganizationDto toDto(Organization org) {
    long memberCount = countOrgMembers(org.getId());

    return OrganizationDto.builder()
        .id(org.getId())
        .name(org.getName())
        .ownerId(org.getOwnerId())
        .plan(org.getPlan())
        .description(org.getDescription())
        .website(org.getWebsite())
        .logoUrl(org.getLogoUrl())
        .active(org.getActive())
        .createdAt(org.getCreatedAt())
        .updatedAt(org.getUpdatedAt())
        .memberCount(memberCount)
        .build();
  }

  /**
   * Converts a list of Organization entities to OrganizationDto list.
   *
   * @param organizations list of Organization entities
   * @return list of OrganizationDto
   */
  public List<OrganizationDto> toDto(List<Organization> organizations) {
    return organizations.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }
}
