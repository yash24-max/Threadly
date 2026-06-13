package dev.threadly.core.workspace;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.workspace.BotController.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotService {

  private final BotRepository botRepository;
  private final OrgRepository orgRepository;

  @Value("${threadly.widget.cdn-url:http://localhost:8080}")
  private String cdnUrl;

  public List<BotResponse> listBots() {
    return botRepository.findAllByOrgId(TenantContext.getOrgId()).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public BotResponse createBot(CreateBotRequest req) {
    UUID orgId = TenantContext.getOrgId();
    Org org = orgRepository.getReferenceById(orgId);
    Bot bot = botRepository.save(
        Bot.builder()
            .org(org)
            .name(req.getName())
            .description(req.getDescription())
            .language(req.getLanguage() != null ? req.getLanguage() : "en")
            .build());
    return toResponse(bot);
  }

  public BotResponse getBot(UUID id) {
    return toResponse(findBotForCurrentOrg(id));
  }

  @Transactional
  public BotResponse updateBot(UUID id, UpdateBotRequest req) {
    Bot bot = findBotForCurrentOrg(id);
    if (req.getName() != null) bot.setName(req.getName());
    if (req.getDescription() != null) bot.setDescription(req.getDescription());
    if (req.getTheme() != null) bot.setTheme(req.getTheme());
    if (req.getActive() != null) bot.setActive(req.getActive());
    return toResponse(botRepository.save(bot));
  }

  @Transactional
  public void deleteBot(UUID id) {
    Bot bot = findBotForCurrentOrg(id);
    botRepository.delete(bot);
  }

  public EmbedResponse getEmbedConfig(UUID id) {
    Bot bot = findBotForCurrentOrg(id);
    EmbedResponse resp = new EmbedResponse();
    resp.setBotId(bot.getId().toString());
    resp.setWidgetUrl(cdnUrl + "/widget/embed.js");
    resp.setTheme(bot.getTheme());
    resp.setSnippet(
        "<script src=\"" + cdnUrl + "/widget/embed.js\" data-bot=\"" + bot.getId() + "\" async></script>");
    return resp;
  }

  private Bot findBotForCurrentOrg(UUID id) {
    return botRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + id));
  }

  private BotResponse toResponse(Bot bot) {
    BotResponse r = new BotResponse();
    r.setId(bot.getId().toString());
    r.setOrgId(bot.getOrg().getId().toString());
    r.setName(bot.getName());
    r.setDescription(bot.getDescription());
    r.setLanguage(bot.getLanguage());
    r.setTheme(bot.getTheme());
    r.setActive(bot.isActive());
    r.setCreatedAt(bot.getCreatedAt() != null ? bot.getCreatedAt().toString() : null);
    r.setUpdatedAt(bot.getUpdatedAt() != null ? bot.getUpdatedAt().toString() : null);
    return r;
  }
}
