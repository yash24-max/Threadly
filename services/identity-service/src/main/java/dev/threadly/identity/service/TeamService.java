package dev.threadly.identity.service;

import dev.threadly.identity.dto.TeamDto;
import dev.threadly.identity.entity.Membership;
import dev.threadly.identity.entity.Team;
import dev.threadly.identity.exception.ResourceNotFoundException;
import dev.threadly.identity.repository.MembershipRepository;
import dev.threadly.identity.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing teams within organizations.
 * Handles team creation, member management, and team operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

  private final TeamRepository teamRepository;
  private final MembershipRepository membershipRepository;

  /**
   * Creates a new team in an organization.
   *
   * @param orgId the organization ID
   * @param name the team name
   * @param description the team description (optional)
   * @return newly created Team entity
   */
  public Team createTeam(String orgId, String name, String description) {
    Team team = Team.builder()
        .id(UUID.randomUUID().toString())
        .orgId(orgId)
        .name(name)
        .description(description)
        .active(true)
        .build();

    Team saved = teamRepository.save(team);
    log.info("Created team: {} in org: {}", saved.getId(), orgId);

    return saved;
  }

  /**
   * Gets a team by ID.
   *
   * @param teamId the team ID
   * @return Team entity
   * @throws ResourceNotFoundException if team not found
   */
  @Transactional(readOnly = true)
  public Team getTeamById(String teamId) {
    return teamRepository.findById(teamId)
        .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
  }

  /**
   * Gets all active teams in an organization.
   *
   * @param orgId the organization ID
   * @return list of teams
   */
  @Transactional(readOnly = true)
  public List<Team> getTeamsByOrg(String orgId) {
    return teamRepository.findActiveByOrgId(orgId);
  }

  /**
   * Finds a team by organization and name.
   *
   * @param orgId the organization ID
   * @param name the team name
   * @return Team entity or null if not found
   */
  @Transactional(readOnly = true)
  public Team getTeamByName(String orgId, String name) {
    return teamRepository.findByOrgIdAndName(orgId, name);
  }

  /**
   * Updates team details.
   *
   * @param teamId the team ID
   * @param name new name (optional)
   * @param description new description (optional)
   * @return updated Team entity
   */
  public Team updateTeam(String teamId, String name, String description) {
    Team team = getTeamById(teamId);

    if (name != null && !name.isBlank()) {
      team.setName(name);
    }
    if (description != null) {
      team.setDescription(description);
    }

    Team updated = teamRepository.save(team);
    log.info("Updated team: {}", teamId);

    return updated;
  }

  /**
   * Adds a user to a team.
   * Updates the user's membership to include the team ID.
   *
   * @param teamId the team ID
   * @param userId the user ID
   * @param orgId the organization ID
   */
  public void addUserToTeam(String teamId, String userId, String orgId) {
    Membership membership = membershipRepository.findByUserIdAndOrgId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Membership", userId + "_" + orgId));

    String currentTeamIds = membership.getTeamIds() != null ? membership.getTeamIds() : "";
    List<String> teamIds = currentTeamIds.isEmpty()
        ? Arrays.asList(teamId)
        : Arrays.stream(currentTeamIds.split(","))
            .filter(id -> !id.isEmpty())
            .collect(Collectors.toList());

    if (!teamIds.contains(teamId)) {
      teamIds.add(teamId);
      membership.setTeamIds(String.join(",", teamIds));
      membershipRepository.save(membership);
      log.info("Added user: {} to team: {}", userId, teamId);
    }
  }

  /**
   * Removes a user from a team.
   *
   * @param teamId the team ID
   * @param userId the user ID
   * @param orgId the organization ID
   */
  public void removeUserFromTeam(String teamId, String userId, String orgId) {
    Membership membership = membershipRepository.findByUserIdAndOrgId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Membership", userId + "_" + orgId));

    String currentTeamIds = membership.getTeamIds() != null ? membership.getTeamIds() : "";
    if (!currentTeamIds.isEmpty()) {
      List<String> teamIds = Arrays.stream(currentTeamIds.split(","))
          .filter(id -> !id.isEmpty() && !id.equals(teamId))
          .collect(Collectors.toList());

      membership.setTeamIds(String.join(",", teamIds));
      membershipRepository.save(membership);
      log.info("Removed user: {} from team: {}", userId, teamId);
    }
  }

  /**
   * Gets all members of a team.
   * Filters memberships that contain this team ID.
   *
   * @param teamId the team ID
   * @param orgId the organization ID
   * @return list of memberships for team members
   */
  @Transactional(readOnly = true)
  public List<Membership> getTeamMembers(String teamId, String orgId) {
    List<Membership> allMembers = membershipRepository.findActiveByOrgId(orgId);

    return allMembers.stream()
        .filter(m -> m.getTeamIds() != null && m.getTeamIds().contains(teamId))
        .collect(Collectors.toList());
  }

  /**
   * Counts members in a team.
   *
   * @param teamId the team ID
   * @param orgId the organization ID
   * @return count of members
   */
  @Transactional(readOnly = true)
  public long countTeamMembers(String teamId, String orgId) {
    return getTeamMembers(teamId, orgId).size();
  }

  /**
   * Deactivates a team.
   *
   * @param teamId the team ID
   */
  public void deactivateTeam(String teamId) {
    Team team = getTeamById(teamId);
    team.setActive(false);
    teamRepository.save(team);
    log.info("Deactivated team: {}", teamId);
  }

  /**
   * Converts a Team entity to TeamDto for API responses.
   *
   * @param team the Team entity
   * @return TeamDto with non-sensitive information
   */
  public TeamDto toDto(Team team) {
    long memberCount = countTeamMembers(team.getId(), team.getOrgId());

    return TeamDto.builder()
        .id(team.getId())
        .organizationId(team.getOrgId())
        .name(team.getName())
        .description(team.getDescription())
        .active(team.getActive())
        .createdAt(team.getCreatedAt())
        .updatedAt(team.getUpdatedAt())
        .memberCount(memberCount)
        .build();
  }

  /**
   * Converts a list of Team entities to TeamDto list.
   *
   * @param teams list of Team entities
   * @return list of TeamDto
   */
  public List<TeamDto> toDto(List<Team> teams) {
    return teams.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }
}
