package dev.threadly.common.feign;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Conversation Service (:3005).
 *
 * Endpoints:
 * - Conversation & message management
 * - Leads & CRM
 * - Handoff workflow
 */
@FeignClient(
    name = "conversation-service",
    url = "${threadly.services.conversation-service.url:http://conversation-service:3005}"
)
public interface ConversationServiceClient {

  /**
   * GET /conversations/{botId} — List conversations.
   */
  @GetMapping("/conversations/{botId}")
  ConversationsListResponse listConversations(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /conversations/{conversationId} — Get conversation transcript.
   */
  @GetMapping("/conversations/{conversationId}")
  ConversationDTO getConversation(
      @PathVariable UUID conversationId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /conversations/{conversationId}/messages — Get paginated messages.
   */
  @GetMapping("/conversations/{conversationId}/messages")
  MessagesListResponse listMessages(
      @PathVariable UUID conversationId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /conversations/{conversationId}/message — Add human reply (agent endpoint).
   */
  @PostMapping("/conversations/{conversationId}/message")
  MessageDTO addMessage(
      @PathVariable UUID conversationId,
      @RequestBody AddMessageRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /conversations/{conversationId}/close — Mark conversation as resolved.
   */
  @PostMapping("/conversations/{conversationId}/close")
  ConversationDTO closeConversation(
      @PathVariable UUID conversationId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /leads — List leads.
   */
  @GetMapping("/leads")
  LeadsListResponse listLeads(@RequestHeader("Authorization") String token);

  /**
   * POST /leads — Create lead manually.
   */
  @PostMapping("/leads")
  LeadDTO createLead(
      @RequestBody CreateLeadRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * PATCH /leads/{leadId} — Update lead.
   */
  @PatchMapping("/leads/{leadId}")
  LeadDTO updateLead(
      @PathVariable UUID leadId,
      @RequestBody UpdateLeadRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /leads/{leadId}/activities — Get lead activity timeline.
   */
  @GetMapping("/leads/{leadId}/activities")
  ActivitiesListResponse listLeadActivities(
      @PathVariable UUID leadId,
      @RequestHeader("Authorization") String token
  );

  // DTOs

  record ConversationsListResponse(List<ConversationDTO> conversations, int total) {}

  record ConversationDTO(
      UUID conversationId,
      UUID botId,
      String visitorId,
      String status,
      java.time.Instant startedAt,
      java.time.Instant endedAt,
      int messageCount,
      UUID leadId
  ) {}

  record MessagesListResponse(List<MessageDTO> messages, int total) {}

  record MessageDTO(
      UUID messageId,
      UUID conversationId,
      String senderType,
      String senderId,
      String senderName,
      String content,
      String messageType,
      java.time.Instant createdAt
  ) {}

  record AddMessageRequest(
      String content,
      String senderName
  ) {}

  record LeadsListResponse(List<LeadDTO> leads, int total) {}

  record LeadDTO(
      UUID leadId,
      UUID botId,
      String email,
      String phone,
      String name,
      String status,
      List<String> tags,
      java.util.Map<String, Object> customFields,
      java.time.Instant createdAt,
      java.time.Instant updatedAt
  ) {}

  record CreateLeadRequest(
      String email,
      String phone,
      String name,
      List<String> tags,
      java.util.Map<String, Object> customFields
  ) {}

  record UpdateLeadRequest(
      String status,
      List<String> tags,
      java.util.Map<String, Object> customFields
  ) {}

  record ActivitiesListResponse(List<LeadActivityDTO> activities, int total) {}

  record LeadActivityDTO(
      UUID activityId,
      UUID leadId,
      String activityType,
      String description,
      java.time.Instant createdAt
  ) {}
}
