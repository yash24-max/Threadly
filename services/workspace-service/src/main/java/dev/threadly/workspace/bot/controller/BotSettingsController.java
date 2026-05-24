package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.BotSettingsDto;
import dev.threadly.workspace.bot.service.BotSettingsService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for bot settings management.
 *
 * <p>Base path: /api/v1/bots/{botId}/settings
 *
 * <p>Manages bot customization: themes, avatars, welcome messages, token budgets.
 */
@RestController
@RequestMapping("/api/v1/bots/{botId}/settings")
@RequiredArgsConstructor
@Slf4j
public class BotSettingsController {

  private final BotSettingsService botSettingsService;

  /**
   * Get bot settings.
   *
   * <p>GET /api/v1/bots/{botId}/settings
   *
   * @param botId bot ID
   * @param principal authenticated user
   * @return current settings
   */
  @GetMapping
  public ResponseEntity<BotSettingsDto> getSettings(
      @PathVariable String botId,
      Principal principal) {
    log.debug("Fetching settings for bot: {}", botId);

    BotSettingsDto settings = botSettingsService.getSettings(botId);

    return ResponseEntity.ok(settings);
  }

  /**
   * Update bot settings.
   *
   * <p>PATCH /api/v1/bots/{botId}/settings
   *
   * <p>Only provided fields are updated. All fields optional.
   *
   * @param botId bot ID
   * @param settingsDto settings to update
   * @param principal authenticated user
   * @return updated settings
   */
  @PatchMapping
  public ResponseEntity<BotSettingsDto> updateSettings(
      @PathVariable String botId,
      @Valid @RequestBody BotSettingsDto settingsDto,
      Principal principal) {
    log.info("Updating settings for bot: {}", botId);

    BotSettingsDto updated = botSettingsService.updateSettings(botId, settingsDto);

    return ResponseEntity.ok(updated);
  }

  /**
   * Apply a theme preset to the bot.
   *
   * <p>POST /api/v1/bots/{botId}/settings/theme/{themeName}
   *
   * <p>Available themes: blue, dark, light, green, purple
   *
   * @param botId bot ID
   * @param themeName theme name
   * @param principal authenticated user
   * @return updated settings with applied theme
   */
  @PostMapping("/theme/{themeName}")
  public ResponseEntity<BotSettingsDto> applyTheme(
      @PathVariable String botId,
      @PathVariable String themeName,
      Principal principal) {
    log.info("Applying theme '{}' to bot: {}", themeName, botId);

    BotSettingsDto settings = botSettingsService.applyTheme(botId, themeName);

    return ResponseEntity.ok(settings);
  }
}
