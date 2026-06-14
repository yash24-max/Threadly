package dev.threadly.identity.controller;

import dev.threadly.identity.entity.Membership;
import dev.threadly.identity.repository.MembershipRepository;
import dev.threadly.identity.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages org team members.
 * Paths:
 *   GET  /v1/team/members        — list all members of the caller's org
 *   POST /v1/team/invite         — invite a new member by email
 *   DELETE /v1/team/members/{id} — remove a member
 */
@Slf4j
@RestController
@RequestMapping("/v1/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Org team member management")
public class TeamController {

    private final MembershipRepository membershipRepository;
    private final UserRepository       userRepository;

    @GetMapping("/members")
    @Operation(summary = "List all members in the caller's org")
    public ResponseEntity<List<Map<String, Object>>> listMembers(Authentication auth) {
        String orgId = orgId(auth);
        List<Membership> memberships = membershipRepository.findActiveByOrgId(orgId);

        List<Map<String, Object>> result = memberships.stream().map(m -> {
            var user = userRepository.findById(m.getUserId());
            Map<String, Object> member = new java.util.LinkedHashMap<>();
            member.put("id",        m.getUserId());
            member.put("role",      m.getRole());
            member.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            user.ifPresent(u -> {
                member.put("name",  u.getName() != null ? u.getName() : u.getEmail());
                member.put("email", u.getEmail());
            });
            return member;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/invite")
    @Operation(summary = "Invite a user to the org by email")
    public ResponseEntity<Void> invite(
            @Valid @RequestBody InviteRequest req,
            Authentication auth) {
        String orgId = orgId(auth);
        log.info("Team invite: email={} role={} org={}", req.getEmail(), req.getRole(), orgId);
        // In a full impl: send invite email, create pending membership.
        // For now: if user exists add directly; otherwise log for email queue.
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            boolean alreadyMember = membershipRepository
                    .findActiveByOrgId(orgId).stream()
                    .anyMatch(m -> m.getUserId().equals(user.getId()));
            if (!alreadyMember) {
                Membership m = Membership.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .userId(user.getId())
                        .orgId(orgId)
                        .role(req.getRole() != null ? req.getRole() : "agent")
                        .active(true)
                        .build();
                membershipRepository.save(m);
                log.info("Added user {} to org {}", user.getId(), orgId);
            }
        });
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/members/{id}")
    @Operation(summary = "Remove a member from the org")
    public ResponseEntity<Void> removeMember(
            @PathVariable String id,
            Authentication auth) {
        String orgId = orgId(auth);
        membershipRepository.findActiveByOrgId(orgId).stream()
                .filter(m -> m.getUserId().equals(id))
                .findFirst()
                .ifPresent(m -> {
                    m.setActive(false);
                    membershipRepository.save(m);
                    log.info("Removed member {} from org {}", id, orgId);
                });
        return ResponseEntity.noContent().build();
    }

    private String orgId(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String orgId = jwt.getClaimAsString("orgId");
            if (orgId != null) return orgId;
        }
        throw new IllegalStateException("No orgId in JWT");
    }

    @Data
    public static class InviteRequest {
        @NotBlank @Email
        private String email;
        private String role; // admin | agent
    }
}
