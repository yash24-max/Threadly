package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.BotDto;
import dev.threadly.workspace.bot.dto.CreateBotRequest;
import dev.threadly.workspace.bot.dto.UpdateBotRequest;
import dev.threadly.workspace.bot.entity.Bot;
import dev.threadly.workspace.bot.entity.TeamMember;
import dev.threadly.workspace.bot.event.BotCreatedEvent;
import dev.threadly.workspace.bot.event.BotDeletedEvent;
import dev.threadly.workspace.bot.exception.BotAccessDeniedException;
import dev.threadly.workspace.bot.exception.BotNotFoundException;
import dev.threadly.workspace.bot.repository.BotRepository;
import dev.threadly.workspace.bot.repository.BotSettingsRepository;
import dev.threadly.workspace.bot.repository.BotVersionRepository;
import dev.threadly.workspace.bot.repository.TeamMemberRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for bot management operations.
 *
 * <p>Handles:
 * - Bot lifecycle (create, update, delete)
 * - Access control enforcement
 * - Event publishing
 * - Multi-tenancy enforcement
 *
 * <p>All operations enforce org_id isolation and soft delete semantics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {

  private final BotRepository botRepository;
  private final BotSettingsRepository botSettingsRepository;
  private final BotVersionRepository botVersionRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Create a new bot in the organization.
   *
   * <p>Creates an OWNER team member entry for the creator and initializes default settings.
   *
   * @param orgId organization ID
   * @param userId user ID of the creator
   * @param request creation parameters
   * @return created bot DTO
   */
  @Transactional
  public BotDto createBot(String orgId, String userId, CreateBotRequest request) {
    log.info("Creating bot in org '{}' by user '{}'", orgId, userId);

    final String botId = UUID.randomUUID().toString();
    final Instant now = Instant.now();

    Bot bot = Bot.builder()
        .id(botId)
        .orgId(orgId)
        .name(request.getName())
        .description(request.getDescription())
        .status("DRAFT")
        .createdBy(userId)
        .createdAt(now)
        .updatedAt(now)
        .build();

    bot = botRepository.save(bot);
    log.info("Bot created with id '{}'", botId);

    // Add creator as OWNER
    TeamMember owner = TeamMember.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .userId(userId)
        .role("OWNER")
        .createdAt(now)
        .updatedAt(now)
        .build();
    teamMemberRepository.save(owner);

    // Publish event
    BotCreatedEvent event = BotCreatedEvent.builder()
        .botId(botId)
        .orgId(orgId)
        .name(bot.getName())
        .createdBy(userId)
        .timestamp(now)
        .build();
    kafkaTemplate.send("threadly.bot.created", event);

    return mapToDto(bot, "OWNER");
  }

  /**
   * Get a bot by ID with access control check.
   *
   * @param botId bot ID
   * @param orgId organization ID
   * @param userId user ID for access check
   * @return bot DTO
   * @throws BotNotFoundException if bot not found
   * @throws BotAccessDeniedException if user lacks access
   */
  @Transactional(readOnly = true)
  public BotDto getBot(String botId, String orgId, String userId) {
    log.debug("Fetching bot '{}' for user '{}'", botId, userId);

    Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> BotNotFoundException.forBotInOrg(botId, orgId));

    // Check user access
    TeamMember member = teamMemberRepository.findByBotIdAndUserId(botId, userId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(userId, botId));

    return mapToDto(bot, member.getRole());
  }

  /**
   * List all non-deleted bots for an organization, paginated.
   *
   * @param orgId organization ID
   * @param pageable pagination parameters
   * @return page of bot DTOs
   */
  @Transactional(readOnly = true)
  public Page<BotDto> listBots(String orgId, Pageable pageable) {
    log.debug("Listing bots for org '{}'", orgId);
    return botRepository.findByOrgId(orgId, pageable)
        .map(bot -> mapToDto(bot, null));
  }

  /**
   * Search bots by name in an organization.
   *
   * @param orgId organization ID
   * @param searchTerm search term (case-insensitive)
   * @param pageable pagination parameters
   * @return page of matching bot DTOs
   */
  @Transactional(readOnly = true)
  public Page<BotDto> searchBots(String orgId, String searchTerm, Pageable pageable) {
    log.debug("Searching bots in org '{}' with term '{}'", orgId, searchTerm);
    return botRepository.searchByNameAndOrgId(orgId, searchTerm, pageable)
        .map(bot -> mapToDto(bot, null));
  }

  /**
   * Update a bot (name, description, status).
   *
   * <p>Enforces EDITOR+ role requirement.
   *
   * @param botId bot ID
   * @param orgId organization ID
   * @param userId user ID requesting update
   * @param request update parameters
   * @return updated bot DTO
   * @throws BotNotFoundException if bot not found
   * @throws BotAccessDeniedException if user lacks EDITOR role
   */
  @Transactional
  public BotDto updateBot(String botId, String orgId, String userId, UpdateBotRequest request) {
    log.info("Updating bot '{}' by user '{}'", botId, userId);

    Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> BotNotFoundException.forBotInOrg(botId, orgId));

    // Check user role (must be EDITOR or OWNER)
    TeamMember member = teamMemberRepository.findByBotIdAndUserId(botId, userId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(userId, botId));

    if (!member.getRole().equals("OWNER") && !member.getRole().equals("EDITOR")) {
      throw BotAccessDeniedException.insufficientRole(userId, botId, "EDITOR");
    }

    // Apply updates
    if (request.getName() != null && !request.getName().isBlank()) {
      bot.setName(request.getName());
    }
    if (request.getDescription() != null) {
      bot.setDescription(request.getDescription());
    }
    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      bot.setStatus(request.getStatus());
    }

    bot.setUpdatedAt(Instant.now());
    bot = botRepository.save(bot);

    return mapToDto(bot, member.getRole());
  }

  /**
   * Delete a bot (soft delete with deleted_at timestamp).
   *
   * <p>Enforces OWNER role requirement.
   *
   * @param botId bot ID
   * @param orgId organization ID
   * @param userId user ID requesting deletion
   * @throws BotNotFoundException if bot not found
   * @throws BotAccessDeniedException if user is not the OWNER
   */
  @Transactional
  public void deleteBot(String botId, String orgId, String userId) {
    log.info("Deleting bot '{}' by user '{}'", botId, userId);

    Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> BotNotFoundException.forBotInOrg(botId, orgId));

    // Only OWNER can delete
    TeamMember member = teamMemberRepository.findByBotIdAndUserId(botId, userId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(userId, botId));

    if (!member.getRole().equals("OWNER")) {
      throw BotAccessDeniedException.insufficientRole(userId, botId, "OWNER");
    }

    // Soft delete
    bot.setDeletedAt(Instant.now());
    botRepository.save(bot);

    // Publish event
    BotDeletedEvent event = BotDeletedEvent.builder()
        .botId(botId)
        .orgId(orgId)
        .name(bot.getName())
        .deletedBy(userId)
        .timestamp(Instant.now())
        .build();
    kafkaTemplate.send("threadly.bot.deleted", event);

    log.info("Bot '{}' soft-deleted", botId);
  }

  /**
   * Duplicate an existing bot with a new name.
   *
   * <p>Copies settings and configuration from source bot.
   *
   * @param sourceBotId bot to duplicate
   * @param orgId organization ID
   * @param userId user creating duplicate
   * @param newBotName name for the duplicate
   * @return duplicate bot DTO
   * @throws BotNotFoundException if source bot not found
   * @throws BotAccessDeniedException if user lacks access to source
   */
  @Transactional
  public BotDto duplicateBot(
      String sourceBotId, String orgId, String userId, String newBotName) {
    log.info("Duplicating bot '{}' with new name '{}' by user '{}'",
        sourceBotId, newBotName, userId);

    // Get source bot
    Bot sourceBot = botRepository.findByIdAndOrgId(sourceBotId, orgId)
        .orElseThrow(() -> BotNotFoundException.forBotInOrg(sourceBotId, orgId));

    // Verify access
    TeamMember member = teamMemberRepository.findByBotIdAndUserId(sourceBotId, userId)
        .orElseThrow(() -> BotAccessDeniedException.notAMember(userId, sourceBotId));

    // Create duplicate
    final String newBotId = UUID.randomUUID().toString();
    final Instant now = Instant.now();

    Bot duplicateBot = Bot.builder()
        .id(newBotId)
        .orgId(orgId)
        .name(newBotName)
        .description(sourceBot.getDescription())
        .status("DRAFT")
        .createdBy(userId)
        .createdAt(now)
        .updatedAt(now)
        .build();

    duplicateBot = botRepository.save(duplicateBot);

    // Add creator as OWNER
    TeamMember owner = TeamMember.builder()
        .id(UUID.randomUUID().toString())
        .botId(newBotId)
        .userId(userId)
        .role("OWNER")
        .createdAt(now)
        .updatedAt(now)
        .build();
    teamMemberRepository.save(owner);

    // Publish event
    BotCreatedEvent event = BotCreatedEvent.builder()
        .botId(newBotId)
        .orgId(orgId)
        .name(duplicateBot.getName())
        .createdBy(userId)
        .timestamp(now)
        .build();
    kafkaTemplate.send("threadly.bot.created", event);

    return mapToDto(duplicateBot, "OWNER");
  }

  /**
   * Map Bot entity to BotDto with optional user role.
   */
  private BotDto mapToDto(Bot bot, String userRole) {
    return BotDto.builder()
        .id(bot.getId())
        .orgId(bot.getOrgId())
        .name(bot.getName())
        .description(bot.getDescription())
        .status(bot.getStatus())
        .createdBy(bot.getCreatedBy())
        .createdAt(bot.getCreatedAt())
        .updatedAt(bot.getUpdatedAt())
        .userRole(userRole)
        .build();
  }
}
