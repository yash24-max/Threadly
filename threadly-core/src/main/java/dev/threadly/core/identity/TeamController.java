package dev.threadly.core.identity;

import dev.threadly.core.common.AuditService;
import dev.threadly.core.common.TenantContext;
import dev.threadly.core.outbox.OutboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orgs/{orgId}/members")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Team membership management")
public class TeamController {

  private static final List<String> VALID_ROLES = List.of("OWNER", "ADMIN", "MEMBER");

  private final OrgMembershipRepository membershipRepository;
  private final UserRepository userRepository;
  private final OutboxService outboxService;
  private final AuditService auditService;
  private final InviteEmailService inviteEmailService;

  @GetMapping
  @Operation(summary = "List org members with roles")
  public List<MemberResponse> listMembers(@PathVariable UUID orgId) {
    requireOrgAccess(orgId);
    return membershipRepository.findAllByOrgId(orgId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @PostMapping("/invites")
  @Operation(summary = "Invite a user to the org")
  @Transactional
  public ResponseEntity<InviteResponse> invite(
      @PathVariable UUID orgId, @Valid @RequestBody InviteRequest req) {
    requireOrgAccess(orgId);
    if (!VALID_ROLES.contains(req.getRole().toUpperCase())) {
      throw new IllegalArgumentException("Invalid role: " + req.getRole());
    }

    User invitee =
        userRepository
            .findByEmail(req.getEmail())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + req.getEmail()));

    if (membershipRepository.existsByOrgIdAndUserId(orgId, invitee.getId())) {
      throw new IllegalStateException("User is already a member of this org");
    }

    OrgMembership membership =
        OrgMembership.builder()
            .orgId(orgId)
            .userId(invitee.getId())
            .role(req.getRole().toUpperCase())
            .invitedBy(TenantContext.getUserId())
            .build();
    membershipRepository.save(membership);

    outboxService.publishDashboardEvent(
        orgId,
        "member.invited",
        Map.of(
            "inviteeId", invitee.getId().toString(),
            "inviteeEmail", invitee.getEmail(),
            "role", req.getRole().toUpperCase(),
            "invitedBy", TenantContext.getUserId().toString()));

    auditService.log("MEMBER_INVITED", "MEMBER", membership.getId(),
        null, Map.of("inviteeEmail", invitee.getEmail(), "role", membership.getRole()));

    inviteEmailService.sendInviteEmail(
        orgId, invitee.getEmail(), membership.getRole(), TenantContext.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new InviteResponse(membership.getId().toString(), invitee.getEmail(), membership.getRole()));
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "Remove a member from the org")
  @Transactional
  public ResponseEntity<Void> removeMember(@PathVariable UUID orgId, @PathVariable UUID userId) {
    requireOrgAccess(orgId);
    if (userId.equals(TenantContext.getUserId())) {
      throw new IllegalArgumentException("Cannot remove yourself from the org");
    }
    membershipRepository
        .findByOrgIdAndUserId(orgId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Membership not found for user: " + userId));
    membershipRepository.deleteByOrgIdAndUserId(orgId, userId);
    auditService.log("MEMBER_REMOVED", "MEMBER", userId, Map.of("userId", userId.toString()), null);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/role")
  @Operation(summary = "Change a member's role")
  @Transactional
  public MemberResponse changeRole(
      @PathVariable UUID orgId,
      @PathVariable UUID userId,
      @Valid @RequestBody ChangeRoleRequest req) {
    requireOrgAccess(orgId);
    if (!VALID_ROLES.contains(req.getRole().toUpperCase())) {
      throw new IllegalArgumentException("Invalid role: " + req.getRole());
    }
    OrgMembership membership =
        membershipRepository
            .findByOrgIdAndUserId(orgId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Membership not found for user: " + userId));
    String oldRole = membership.getRole();
    membership.setRole(req.getRole().toUpperCase());
    MemberResponse updated = toResponse(membershipRepository.save(membership));
    auditService.log("ROLE_CHANGED", "MEMBER", membership.getId(),
        Map.of("role", oldRole), Map.of("role", membership.getRole()));
    return updated;
  }

  private void requireOrgAccess(UUID orgId) {
    UUID currentOrg = TenantContext.getOrgId();
    if (!currentOrg.equals(orgId)) {
      throw new SecurityException("Access denied to org: " + orgId);
    }
  }

  private MemberResponse toResponse(OrgMembership m) {
    User user =
        userRepository
            .findById(m.getUserId())
            .orElse(null);
    MemberResponse r = new MemberResponse();
    r.setId(m.getId().toString());
    r.setUserId(m.getUserId().toString());
    r.setOrgId(m.getOrgId().toString());
    r.setRole(m.getRole());
    r.setCreatedAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
    if (user != null) {
      r.setEmail(user.getEmail());
      r.setName(user.getName());
      r.setAvatarUrl(user.getAvatarUrl());
    }
    return r;
  }

  // ── DTOs ──────────────────────────────────────────────────────────────

  @Data
  public static class MemberResponse {
    private String id;
    private String userId;
    private String orgId;
    private String email;
    private String name;
    private String avatarUrl;
    private String role;
    private String createdAt;
  }

  @Data
  public static class InviteRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "OWNER|ADMIN|MEMBER", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String role;
  }

  @Data
  public static class InviteResponse {
    private final String membershipId;
    private final String email;
    private final String role;
  }

  @Data
  public static class ChangeRoleRequest {
    @NotBlank
    @Pattern(regexp = "OWNER|ADMIN|MEMBER", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String role;
  }
}
