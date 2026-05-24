package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.BotSettingsDto;
import dev.threadly.workspace.bot.entity.BotSettings;
import dev.threadly.workspace.bot.exception.InvalidBotConfigException;
import dev.threadly.workspace.bot.repository.BotRepository;
import dev.threadly.workspace.bot.repository.BotSettingsRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing bot settings and configuration.
 *
 * <p>Handles:
 * - Theme customization (colors, avatars)
 * - Welcome messages
 * - Token budget limits
 * - Settings initialization and updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotSettingsService {

  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

  private final BotSettingsRepository botSettingsRepository;
  private final BotRepository botRepository;

  /**
   * Get or initialize bot settings.
   *
   * <p>If settings don't exist, creates defaults.
   *
   * @param botId bot ID
   * @return settings DTO
   */
  @Transactional
  public BotSettingsDto getSettings(String botId) {
    log.debug("Fetching settings for bot '{}'", botId);

    // Verify bot exists
    botRepository.findById(botId).orElseThrow(
        () -> new IllegalArgumentException("Bot not found: " + botId));

    return botSettingsRepository.findByBotId(botId)
        .map(this::mapToDto)
        .orElseGet(() -> createDefaultSettings(botId));
  }

  /**
   * Update bot settings.
   *
   * <p>Only provided fields are updated; others remain unchanged.
   *
   * @param botId bot ID
   * @param dto settings update
   * @return updated settings DTO
   */
  @Transactional
  public BotSettingsDto updateSettings(String botId, BotSettingsDto dto) {
    log.info("Updating settings for bot '{}'", botId);

    BotSettings settings = botSettingsRepository.findByBotId(botId)
        .orElseGet(() -> createDefaultSettingsEntity(botId));

    if (dto.getThemeColor() != null && !dto.getThemeColor().isBlank()) {
      validateHexColor(dto.getThemeColor());
      settings.setThemeColor(dto.getThemeColor());
    }

    if (dto.getAvatar() != null) {
      settings.setAvatar(dto.getAvatar());
    }

    if (dto.getWelcomeMessage() != null) {
      settings.setWelcomeMessage(dto.getWelcomeMessage());
    }

    if (dto.getMaxTokenBudget() != null && dto.getMaxTokenBudget() >= 0) {
      settings.setMaxTokenBudget(dto.getMaxTokenBudget());
    }

    settings.setUpdatedAt(Instant.now());
    settings = botSettingsRepository.save(settings);

    return mapToDto(settings);
  }

  /**
   * Apply a theme preset to bot settings.
   *
   * @param botId bot ID
   * @param themeName theme name (e.g., "blue", "dark", "light")
   * @return updated settings DTO
   */
  @Transactional
  public BotSettingsDto applyTheme(String botId, String themeName) {
    log.info("Applying theme '{}' to bot '{}'", themeName, botId);

    BotSettings settings = botSettingsRepository.findByBotId(botId)
        .orElseGet(() -> createDefaultSettingsEntity(botId));

    switch (themeName.toLowerCase()) {
      case "blue":
        settings.setThemeColor("#3B82F6");
        break;
      case "dark":
        settings.setThemeColor("#1F2937");
        break;
      case "light":
        settings.setThemeColor("#F3F4F6");
        break;
      case "green":
        settings.setThemeColor("#10B981");
        break;
      case "purple":
        settings.setThemeColor("#8B5CF6");
        break;
      default:
        throw new InvalidBotConfigException(
            "Unknown theme: " + themeName +
                ". Available: blue, dark, light, green, purple");
    }

    settings.setUpdatedAt(Instant.now());
    settings = botSettingsRepository.save(settings);

    return mapToDto(settings);
  }

  /**
   * Create default settings for a new bot.
   *
   * @param botId bot ID
   * @return default settings DTO
   */
  @Transactional
  private BotSettingsDto createDefaultSettings(String botId) {
    BotSettings settings = createDefaultSettingsEntity(botId);
    settings = botSettingsRepository.save(settings);
    return mapToDto(settings);
  }

  /**
   * Create default settings entity.
   */
  private BotSettings createDefaultSettingsEntity(String botId) {
    final Instant now = Instant.now();
    return BotSettings.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .themeColor("#3B82F6")
        .avatar(null)
        .welcomeMessage("Hello! How can I help you today?")
        .maxTokenBudget(0) // unlimited
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  /**
   * Validate hex color format.
   *
   * @param color hex color string
   * @throws InvalidBotConfigException if format invalid
   */
  private void validateHexColor(String color) {
    if (!HEX_COLOR_PATTERN.matcher(color).matches()) {
      throw InvalidBotConfigException.invalidThemeColor(color);
    }
  }

  /**
   * Map BotSettings entity to DTO.
   */
  private BotSettingsDto mapToDto(BotSettings settings) {
    return BotSettingsDto.builder()
        .id(settings.getId())
        .botId(settings.getBotId())
        .themeColor(settings.getThemeColor())
        .avatar(settings.getAvatar())
        .welcomeMessage(settings.getWelcomeMessage())
        .maxTokenBudget(settings.getMaxTokenBudget())
        .createdAt(settings.getCreatedAt())
        .updatedAt(settings.getUpdatedAt())
        .build();
  }
}
