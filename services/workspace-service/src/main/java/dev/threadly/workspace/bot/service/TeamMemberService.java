package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.TeamMemberDto;
import dev.threadly.workspace.bot.entity.TeamMember;
import dev.threadly.workspace.bot.exception.BotAccessDeniedException;
import dev.threadly.workspace.bot.exception.BotNotFoundException;
import dev.threadly.workspace.bot.repository.BotRepository;
import dev.threadly.workspace.bot.repository.TeamMemberRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for team member and access control management.
 *
 * <p>Handles:
 * - Adding/removing team members
 * - Role assignment (OWNER, EDITOR, VIEWER)
 * - Access control enforcement
 * - Team listing and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService {

  private final TeamMemberRepository teamMemberRepository;
  private final BotRepository botRepository;

  /**
   * Add a new member to a bot team.
   *
   * <p>Requires OWNER role for the requester.
   *
   * @param botId bot ID
   * @param userId user to add
   * @param role role to assign (OWNER, EDITOR, VIEWER)
   * @param requesterId user making the request
   * @return added member DTO
   * @throws BotNotFoundException if bot not found
   * @throws BotAccessDeniedException if requester is not OWNER
   */
  @Transactional
  public TeamMemberDto addMember(String botId, String userId, String role, String requesterId) {
    log.info("Adding member '{}' with role '{}' to bot '{}' by user '{}'",
        userId, role, botId, requesterId);

    // Verify bot exists
    botRepository.findById(botId)
        .orElseThrow(() -> BotNotFoundException.forBotId(botId));

    // Verify requester is OWNER
    TeamMember requester = teamMemberRepository.findByBotIdAndUserId(botId, requesterId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(requesterId, botId));

    if (!requester.getRole().equals("OWNER")) {
      throw BotAccessDeniedException.insufficientRole(requesterId, botId, "OWNER");
    }

    // Check if user already member
    if (teamMemberRepository.isMember(botId, userId)) {
      throw new IllegalStateException(
          String.format("User '%s' is already a member of bot '%s'", userId, botId));
    }

    // Validate role
    validateRole(role);

    final Instant now = Instant.now();
    TeamMember member = TeamMember.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .userId(userId)
        .role(role)
        .createdAt(now)
        .updatedAt(now)
        .build();

    member = teamMemberRepository.save(member);

    log.info("Member '{}' added to bot '{}'", userId, botId);

    return mapToDto(member);
  }

  /**
   * Remove a member from a bot team.
   *
   * <p>Requires OWNER role for the requester. Cannot remove the last OWNER.
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @param requesterId user making the request
   * @throws BotAccessDeniedException if requester is not OWNER or trying to remove last OWNER
   */
  @Transactional
  public void removeMember(String botId, String memberId, String requesterId) {
    log.info("Removing member '{}' from bot '{}' by user '{}'",
        memberId, botId, requesterId);

    // Verify requester is OWNER
    TeamMember requester = teamMemberRepository.findByBotIdAndUserId(botId, requesterId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(requesterId, botId));

    if (!requester.getRole().equals("OWNER")) {
      throw BotAccessDeniedException.insufficientRole(requesterId, botId, "OWNER");
    }

    TeamMember member = teamMemberRepository.findById(memberId)
        .orElseThrow(() -> new BotNotFoundException("Team member not found: " + memberId));

    // Check if this is the last OWNER
    if (member.getRole().equals("OWNER")) {
      long ownerCount = teamMemberRepository.findByBotIdAndRole(botId, "OWNER").size();
      if (ownerCount <= 1) {
        throw BotAccessDeniedException.operationNotAllowed(
            "remove member",
            "Cannot remove the last OWNER from a bot");
      }
    }

    teamMemberRepository.delete(member);

    log.info("Member '{}' removed from bot '{}'", memberId, botId);
  }

  /**
   * Update a team member's role.
   *
   * <p>Requires OWNER role for the requester.
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @param newRole new role to assign
   * @param requesterId user making the request
   * @return updated member DTO
   * @throws BotAccessDeniedException if requester is not OWNER
   */
  @Transactional
  public TeamMemberDto updateMemberRole(
      String botId, String memberId, String newRole, String requesterId) {
    log.info("Updating member '{}' role to '{}' in bot '{}' by user '{}'",
        memberId, newRole, botId, requesterId);

    // Verify requester is OWNER
    TeamMember requester = teamMemberRepository.findByBotIdAndUserId(botId, requesterId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(requesterId, botId));

    if (!requester.getRole().equals("OWNER")) {
      throw BotAccessDeniedException.insufficientRole(requesterId, botId, "OWNER");
    }

    TeamMember member = teamMemberRepository.findById(memberId)
        .orElseThrow(() -> new BotNotFoundException("Team member not found: " + memberId));

    // Validate new role
    validateRole(newRole);

    member.setRole(newRole);
    member.setUpdatedAt(Instant.now());
    member = teamMemberRepository.save(member);

    return mapToDto(member);
  }

  /**
   * List all team members of a bot.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return page of team members
   */
  @Transactional(readOnly = true)
  public Page<TeamMemberDto> listMembers(String botId, Pageable pageable) {
    log.debug("Listing members for bot '{}'", botId);

    return teamMemberRepository.findByBotId(botId, pageable)
        .map(this::mapToDto);
  }

  /**
   * Get a specific team member.
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @return member DTO
   */
  @Transactional(readOnly = true)
  public TeamMemberDto getMember(String botId, String memberId) {
    log.debug("Fetching member '{}' for bot '{}'", memberId, botId);

    TeamMember member = teamMemberRepository.findById(memberId)
        .orElseThrow(() -> new BotNotFoundException("Team member not found: " + memberId));

    if (!member.getBotId().equals(botId)) {
      throw new IllegalArgumentException(
          String.format("Member '%s' does not belong to bot '%s'", memberId, botId));
    }

    return mapToDto(member);
  }

  /**
   * Check if a user has a specific role on a bot.
   *
   * @param botId bot ID
   * @param userId user ID
   * @param requiredRole role to check
   * @return true if user has the role or higher
   */
  @Transactional(readOnly = true)
  public boolean hasRole(String botId, String userId, String requiredRole) {
    return teamMemberRepository.findByBotIdAndUserId(botId, userId)
        .map(member -> roleHierarchy(member.getRole()) >= roleHierarchy(requiredRole))
        .orElse(false);
  }

  /**
   * Count team members on a bot.
   *
   * @param botId bot ID
   * @return count of members
   */
  @Transactional(readOnly = true)
  public long countMembers(String botId) {
    return teamMemberRepository.countByBotId(botId);
  }

  /**
   * Validate role string.
   *
   * @throws IllegalArgumentException if role invalid
   */
  private void validateRole(String role) {
    if (!role.matches("^(OWNER|EDITOR|VIEWER)$")) {
      throw new IllegalArgumentException(
          "Invalid role: " + role + ". Must be OWNER, EDITOR, or VIEWER");
    }
  }

  /**
   * Get numeric hierarchy of a role for comparison.
   * Higher number = higher privilege.
   */
  private int roleHierarchy(String role) {
    return switch (role) {
      case "OWNER" -> 3;
      case "EDITOR" -> 2;
      case "VIEWER" -> 1;
      default -> 0;
    };
  }

  /**
   * Map TeamMember entity to DTO.
   */
  private TeamMemberDto mapToDto(TeamMember member) {
    return TeamMemberDto.builder()
        .id(member.getId())
        .botId(member.getBotId())
        .userId(member.getUserId())
        .role(member.getRole())
        .createdAt(member.getCreatedAt())
        .updatedAt(member.getUpdatedAt())
        .build();
  }
}
