package dev.threadly.core.workspace;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/v1/bots/{botId}/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Bot webhook configuration")
public class WebhookController {

  private static final List<String> VALID_EVENTS =
      List.of("conversation.ended", "handoff.created", "message.received");

  private final WebhookRepository webhookRepository;
  private final BotRepository botRepository;
  private final ObjectMapper objectMapper;

  @GetMapping
  @Operation(summary = "List webhooks for a bot")
  public List<WebhookResponse> list(@PathVariable UUID botId) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);
    return webhookRepository.findAllByBotIdAndOrgIdAndActiveTrue(botId, orgId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @PostMapping
  @Operation(summary = "Create a webhook")
  @Transactional
  public ResponseEntity<WebhookResponse> create(
      @PathVariable UUID botId, @Valid @RequestBody CreateWebhookRequest req) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);

    for (String event : req.getEvents()) {
      if (!VALID_EVENTS.contains(event)) {
        throw new IllegalArgumentException("Invalid event type: " + event);
      }
    }

    String eventsJson;
    try {
      eventsJson = objectMapper.writeValueAsString(req.getEvents());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid events list", e);
    }

    String secret = UUID.randomUUID().toString().replace("-", "");

    Webhook webhook =
        Webhook.builder()
            .orgId(orgId)
            .botId(botId)
            .url(req.getUrl())
            .events(eventsJson)
            .secret(secret)
            .build();
    webhookRepository.save(webhook);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(webhook));
  }

  @DeleteMapping("/{webhookId}")
  @Operation(summary = "Delete a webhook")
  @Transactional
  public ResponseEntity<Void> delete(@PathVariable UUID botId, @PathVariable UUID webhookId) {
    UUID orgId = TenantContext.getOrgId();
    Webhook webhook =
        webhookRepository
            .findByIdAndBotIdAndOrgId(webhookId, botId, orgId)
            .orElseThrow(() -> new EntityNotFoundException("Webhook not found: " + webhookId));
    webhook.setActive(false);
    webhookRepository.save(webhook);
    return ResponseEntity.noContent().build();
  }

  private void requireBotAccess(UUID botId, UUID orgId) {
    botRepository
        .findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));
  }

  private WebhookResponse toResponse(Webhook w) {
    WebhookResponse r = new WebhookResponse();
    r.setId(w.getId().toString());
    r.setBotId(w.getBotId().toString());
    r.setUrl(w.getUrl());
    r.setEvents(w.getEvents());
    r.setCreatedAt(w.getCreatedAt() != null ? w.getCreatedAt().toString() : null);
    return r;
  }

  // ── DTOs ──────────────────────────────────────────────────────────────

  @Data
  public static class WebhookResponse {
    private String id;
    private String botId;
    private String url;
    private String events;
    private String createdAt;
  }

  @Data
  public static class CreateWebhookRequest {
    @NotBlank private String url;
    @NotEmpty private List<String> events;
  }
}
