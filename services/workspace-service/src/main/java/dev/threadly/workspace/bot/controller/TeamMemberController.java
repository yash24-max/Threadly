package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.TeamMemberDto;
import dev.threadly.workspace.bot.service.TeamMemberService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for team member management.
 *
 * <p>Base path: /api/v1/bots/{botId}/team-members
 *
 * <p>Manages team access and role-based permissions.
 */
@RestController
@RequestMapping("/api/v1/bots/{botId}/team-members")
@RequiredArgsConstructor
@Slf4j
public class TeamMemberController {

  private final TeamMemberService teamMemberService;

  /**
   * Add a new member to the team.
   *
   * <p>POST /api/v1/bots/{botId}/team-members
   *
   * <p>Requires OWNER role.
   *
   * <p>Request body:
   * {
   *   "user_id": "user-123",
   *   "role": "EDITOR"
   * }
   *
   * @param botId bot ID
   * @param request add member request
   * @param principal authenticated user (must be OWNER)
   * @return added member DTO with 201 status
   */
  @PostMapping
  public ResponseEntity<TeamMemberDto> addMember(
      @PathVariable String botId,
      @Valid @RequestBody AddMemberRequest request,
      Principal principal) {
    log.info("Adding member '{}' to bot: {}", request.getUserId(), botId);

    String requesterId = principal.getName();

    TeamMemberDto member = teamMemberService.addMember(
        botId, request.getUserId(), request.getRole(), requesterId);

    return ResponseEntity.status(HttpStatus.CREATED).body(member);
  }

  /**
   * List all team members of a bot.
   *
   * <p>GET /api/v1/bots/{botId}/team-members
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return paginated list of team members
   */
  @GetMapping
  public ResponseEntity<Page<TeamMemberDto>> listMembers(
      @PathVariable String botId,
      Pageable pageable,
      Principal principal) {
    log.debug("Listing members for bot: {}", botId);

    Page<TeamMemberDto> members = teamMemberService.listMembers(botId, pageable);

    return ResponseEntity.ok(members);
  }

  /**
   * Get a specific team member.
   *
   * <p>GET /api/v1/bots/{botId}/team-members/{memberId}
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @param principal authenticated user
   * @return team member DTO
   */
  @GetMapping("/{memberId}")
  public ResponseEntity<TeamMemberDto> getMember(
      @PathVariable String botId,
      @PathVariable String memberId,
      Principal principal) {
    log.debug("Fetching member '{}' for bot: {}", memberId, botId);

    TeamMemberDto member = teamMemberService.getMember(botId, memberId);

    return ResponseEntity.ok(member);
  }

  /**
   * Update a team member's role.
   *
   * <p>PATCH /api/v1/bots/{botId}/team-members/{memberId}
   *
   * <p>Requires OWNER role.
   *
   * <p>Request body:
   * {
   *   "role": "VIEWER"
   * }
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @param request update request
   * @param principal authenticated user (must be OWNER)
   * @return updated member DTO
   */
  @PatchMapping("/{memberId}")
  public ResponseEntity<TeamMemberDto> updateMemberRole(
      @PathVariable String botId,
      @PathVariable String memberId,
      @Valid @RequestBody UpdateMemberRequest request,
      Principal principal) {
    log.info("Updating member '{}' role to: {}", memberId, request.getRole());

    String requesterId = principal.getName();

    TeamMemberDto member = teamMemberService.updateMemberRole(
        botId, memberId, request.getRole(), requesterId);

    return ResponseEntity.ok(member);
  }

  /**
   * Remove a team member.
   *
   * <p>DELETE /api/v1/bots/{botId}/team-members/{memberId}
   *
   * <p>Requires OWNER role. Cannot remove the last OWNER.
   *
   * @param botId bot ID
   * @param memberId team member ID
   * @param principal authenticated user (must be OWNER)
   * @return 204 No Content
   */
  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable String botId,
      @PathVariable String memberId,
      Principal principal) {
    log.info("Removing member '{}' from bot: {}", memberId, botId);

    String requesterId = principal.getName();

    teamMemberService.removeMember(botId, memberId, requesterId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Request DTO for adding a member.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AddMemberRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("user_id")
    private String userId;

    @com.fasterxml.jackson.annotation.JsonProperty("role")
    private String role;
  }

  /**
   * Request DTO for updating a member's role.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UpdateMemberRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("role")
    private String role;
  }
}
