package dev.threadly.core.identity;

import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.workspace.Org;
import dev.threadly.core.workspace.OrgRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Enqueues an {@code email.send} outbox event whenever a member is invited to an org.
 *
 * <p>Called from {@link TeamController} after the {@code member.invited} event is already
 * dispatched. This service creates a secondary outbox event that instructs the email-sending
 * infrastructure to deliver the invite notification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteEmailService {

  private final OutboxService outboxService;
  private final OrgRepository orgRepository;

  /**
   * Queues an {@code email.send} outbox event for the invited member.
   *
   * @param orgId        the organisation the user was invited to
   * @param inviteeEmail recipient e-mail address
   * @param role         assigned role
   * @param invitedByUserId UUID of the inviting user (for context)
   */
  public void sendInviteEmail(UUID orgId, String inviteeEmail, String role, UUID invitedByUserId) {
    String orgName = orgRepository.findById(orgId)
        .map(Org::getName)
        .orElse("your organisation");

    String subject = "You've been invited to join " + orgName + " on Threadly";

    Map<String, Object> emailPayload = Map.of(
        "to", inviteeEmail,
        "subject", subject,
        "templateId", "invite_member",
        "templateData", Map.of(
            "orgName", orgName,
            "role", role,
            "invitedByUserId", invitedByUserId != null ? invitedByUserId.toString() : ""
        )
    );

    log.debug("InviteEmailService: queuing email.send for {} to org {}", inviteeEmail, orgId);
    outboxService.publishDashboardEvent(orgId, "email.send", emailPayload);
  }
}
