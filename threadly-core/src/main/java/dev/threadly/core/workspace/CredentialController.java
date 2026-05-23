package dev.threadly.core.workspace;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@RestController
@RequestMapping("/v1/bots/{botId}/credentials")
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Encrypted external API credential store")
public class CredentialController {

  private final BotCredentialRepository credentialRepository;
  private final BotRepository botRepository;
  private final CredentialService credentialService;

  @GetMapping
  @Operation(summary = "List credentials for a bot (name and type only, never the value)")
  public List<CredentialResponse> list(@PathVariable UUID botId) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);
    return credentialRepository.findAllByBotIdAndOrgId(botId, orgId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @PostMapping
  @Operation(summary = "Store an encrypted credential")
  @Transactional
  public ResponseEntity<CredentialResponse> create(
      @PathVariable UUID botId, @Valid @RequestBody CreateCredentialRequest req) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);

    String encryptedValue = credentialService.encrypt(req.getValue());

    BotCredential credential =
        BotCredential.builder()
            .orgId(orgId)
            .botId(botId)
            .name(req.getName())
            .encryptedValue(encryptedValue)
            .type(req.getType() != null ? req.getType() : "generic")
            .build();
    credentialRepository.save(credential);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(credential));
  }

  @DeleteMapping("/{credId}")
  @Operation(summary = "Delete a credential")
  @Transactional
  public ResponseEntity<Void> delete(@PathVariable UUID botId, @PathVariable UUID credId) {
    UUID orgId = TenantContext.getOrgId();
    BotCredential credential =
        credentialRepository
            .findByIdAndBotIdAndOrgId(credId, botId, orgId)
            .orElseThrow(() -> new EntityNotFoundException("Credential not found: " + credId));
    credentialRepository.delete(credential);
    return ResponseEntity.noContent().build();
  }

  private void requireBotAccess(UUID botId, UUID orgId) {
    botRepository
        .findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));
  }

  private CredentialResponse toResponse(BotCredential c) {
    CredentialResponse r = new CredentialResponse();
    r.setId(c.getId().toString());
    r.setName(c.getName());
    r.setType(c.getType());
    r.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
    return r;
  }

  // ── DTOs ──────────────────────────────────────────────────────────────

  @Data
  public static class CredentialResponse {
    private String id;
    private String name;
    private String type;
    private String createdAt;
  }

  @Data
  public static class CreateCredentialRequest {
    @NotBlank private String name;
    @NotBlank private String value;
    private String type;
  }
}
