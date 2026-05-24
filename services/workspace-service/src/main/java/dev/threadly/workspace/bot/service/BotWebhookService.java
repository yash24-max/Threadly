package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.BotWebhookDto;
import dev.threadly.workspace.bot.entity.BotWebhook;
import dev.threadly.workspace.bot.exception.InvalidBotConfigException;
import dev.threadly.workspace.bot.repository.BotRepository;
import dev.threadly.workspace.bot.repository.BotWebhookRepository;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for webhook management and event delivery.
 *
 * <p>Handles:
 * - Webhook registration and configuration
 * - Event subscription management
 * - Webhook activation/deactivation
 * - Event triggering
 *
 * <p>Webhooks allow external systems to receive bot lifecycle events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotWebhookService {

  private final BotWebhookRepository botWebhookRepository;
  private final BotRepository botRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Register a new webhook for a bot.
   *
   * @param botId bot ID
   * @param url webhook URL (must be HTTPS)
   * @param events comma-separated event types to subscribe to
   * @param secret optional secret for HMAC signature validation
   * @return webhook DTO
   */
  @Transactional
  public BotWebhookDto registerWebhook(
      String botId, String url, String events, String secret) {
    log.info("Registering webhook for bot '{}' at URL '{}'", botId, url);

    // Verify bot exists
    botRepository.findById(botId)
        .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));

    // Validate webhook URL
    validateWebhookUrl(url);

    // Validate events
    if (events == null || events.trim().isEmpty()) {
      throw new IllegalArgumentException("At least one event must be specified");
    }

    final Instant now = Instant.now();

    BotWebhook webhook = BotWebhook.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .url(url)
        .events(events)
        .isActive(true)
        .secret(secret)
        .createdAt(now)
        .updatedAt(now)
        .build();

    webhook = botWebhookRepository.save(webhook);

    log.info("Webhook '{}' registered for bot '{}'", webhook.getId(), botId);

    return mapToDto(webhook);
  }

  /**
   * Update a webhook's configuration.
   *
   * <p>Only provided fields are updated.
   *
   * @param webhookId webhook ID
   * @param url new URL (optional)
   * @param events new event subscriptions (optional)
   * @param secret new secret (optional)
   * @return updated webhook DTO
   */
  @Transactional
  public BotWebhookDto updateWebhook(
      String webhookId, String url, String events, String secret) {
    log.info("Updating webhook '{}'", webhookId);

    BotWebhook webhook = botWebhookRepository.findById(webhookId)
        .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));

    if (url != null && !url.isBlank()) {
      validateWebhookUrl(url);
      webhook.setUrl(url);
    }

    if (events != null && !events.isBlank()) {
      webhook.setEvents(events);
    }

    if (secret != null) {
      webhook.setSecret(secret);
    }

    webhook.setUpdatedAt(Instant.now());
    webhook = botWebhookRepository.save(webhook);

    return mapToDto(webhook);
  }

  /**
   * Activate or deactivate a webhook.
   *
   * @param webhookId webhook ID
   * @param isActive whether to activate (true) or deactivate (false)
   * @return updated webhook DTO
   */
  @Transactional
  public BotWebhookDto setWebhookActive(String webhookId, boolean isActive) {
    log.info("Setting webhook '{}' active={}", webhookId, isActive);

    BotWebhook webhook = botWebhookRepository.findById(webhookId)
        .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));

    webhook.setIsActive(isActive);
    webhook.setUpdatedAt(Instant.now());
    webhook = botWebhookRepository.save(webhook);

    return mapToDto(webhook);
  }

  /**
   * Delete a webhook.
   *
   * @param webhookId webhook ID
   */
  @Transactional
  public void deleteWebhook(String webhookId) {
    log.info("Deleting webhook '{}'", webhookId);

    BotWebhook webhook = botWebhookRepository.findById(webhookId)
        .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));

    botWebhookRepository.delete(webhook);

    log.info("Webhook '{}' deleted", webhookId);
  }

  /**
   * Get a specific webhook.
   *
   * @param webhookId webhook ID
   * @return webhook DTO
   */
  @Transactional(readOnly = true)
  public BotWebhookDto getWebhook(String webhookId) {
    log.debug("Fetching webhook '{}'", webhookId);

    BotWebhook webhook = botWebhookRepository.findById(webhookId)
        .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));

    return mapToDto(webhook);
  }

  /**
   * List all webhooks for a bot.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return page of webhooks
   */
  @Transactional(readOnly = true)
  public Page<BotWebhookDto> listWebhooks(String botId, Pageable pageable) {
    log.debug("Listing webhooks for bot '{}'", botId);

    return botWebhookRepository.findByBotId(botId, pageable)
        .map(this::mapToDto);
  }

  /**
   * Get all active webhooks subscribed to a specific event.
   *
   * @param botId bot ID
   * @param event event type
   * @return list of matching webhooks
   */
  @Transactional(readOnly = true)
  public List<BotWebhookDto> getWebhooksForEvent(String botId, String event) {
    log.debug("Finding webhooks for bot '{}' interested in event '{}'", botId, event);

    return botWebhookRepository.findByBotIdAndEvent(botId, event)
        .stream()
        .map(this::mapToDto)
        .toList();
  }

  /**
   * Trigger a webhook event (publish to event bus for delivery).
   *
   * <p>Actual HTTP delivery is handled asynchronously by a webhook delivery service
   * listening to the Kafka topic.
   *
   * @param botId bot ID
   * @param eventType event type
   * @param payload event payload as JSON
   */
  @Transactional
  public void triggerEvent(String botId, String eventType, Object payload) {
    log.info("Triggering webhook event '{}' for bot '{}'", eventType, botId);

    List<BotWebhookDto> webhooks = getWebhooksForEvent(botId, eventType);
    if (webhooks.isEmpty()) {
      log.debug("No webhooks registered for event '{}'", eventType);
      return;
    }

    // Publish event to Kafka for delivery
    kafkaTemplate.send("threadly.webhooks.deliver", payload);
  }

  /**
   * Count webhooks for a bot.
   *
   * @param botId bot ID
   * @return count of webhooks
   */
  @Transactional(readOnly = true)
  public long countWebhooks(String botId) {
    return botWebhookRepository.countByBotId(botId);
  }

  /**
   * Count active webhooks for a bot.
   *
   * @param botId bot ID
   * @return count of active webhooks
   */
  @Transactional(readOnly = true)
  public long countActiveWebhooks(String botId) {
    return botWebhookRepository.countActiveByBotId(botId);
  }

  /**
   * Validate webhook URL.
   *
   * @param urlStr URL string
   * @throws InvalidBotConfigException if URL is invalid or not HTTPS
   */
  private void validateWebhookUrl(String urlStr) {
    try {
      URL url = new URL(urlStr);
      if (!url.getProtocol().equals("https")) {
        throw InvalidBotConfigException.invalidWebhookUrl(urlStr);
      }
    } catch (MalformedURLException e) {
      throw InvalidBotConfigException.invalidWebhookUrl(urlStr);
    }
  }

  /**
   * Map BotWebhook entity to DTO.
   */
  private BotWebhookDto mapToDto(BotWebhook webhook) {
    return BotWebhookDto.builder()
        .id(webhook.getId())
        .botId(webhook.getBotId())
        .url(webhook.getUrl())
        .events(webhook.getEvents())
        .isActive(webhook.getIsActive())
        .createdAt(webhook.getCreatedAt())
        .updatedAt(webhook.getUpdatedAt())
        .lastDeliveredAt(webhook.getLastDeliveredAt())
        .build();
  }
}
