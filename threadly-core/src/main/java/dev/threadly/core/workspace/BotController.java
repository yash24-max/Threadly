package dev.threadly.core.workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bots")
@RequiredArgsConstructor
@Tag(name = "Bots", description = "Bot management")
public class BotController {

  private final BotService botService;

  @GetMapping
  @Operation(summary = "List all bots for the current org")
  public List<BotResponse> list() {
    return botService.listBots();
  }

  @PostMapping
  @Operation(summary = "Create a new bot")
  public ResponseEntity<BotResponse> create(@Valid @RequestBody CreateBotRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(botService.createBot(req));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a bot by ID")
  public BotResponse get(@PathVariable UUID id) {
    return botService.getBot(id);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update bot name/description/theme")
  public BotResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateBotRequest req) {
    return botService.updateBot(id, req);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a bot")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    botService.deleteBot(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/embed")
  @Operation(summary = "Get embed snippet for a bot")
  public EmbedResponse embed(@PathVariable UUID id) {
    return botService.getEmbedConfig(id);
  }

  // ── DTOs ───────────────────────────────────────────────────────────

  @Data
  public static class CreateBotRequest {
    @NotBlank @Size(max = 200) private String name;
    @Size(max = 2000) private String description;
    @Size(max = 10) private String language = "en";
  }

  @Data
  public static class UpdateBotRequest {
    @Size(max = 200) private String name;
    @Size(max = 2000) private String description;
    private String theme;
    private Boolean active;
  }

  @Data
  public static class BotResponse {
    private String id;
    private String orgId;
    private String name;
    private String description;
    private String language;
    private Object theme;
    private boolean active;
    private String createdAt;
    private String updatedAt;
  }

  @Data
  public static class EmbedResponse {
    private String botId;
    private String snippet;
    private String widgetUrl;
    private Object theme;
  }
}
