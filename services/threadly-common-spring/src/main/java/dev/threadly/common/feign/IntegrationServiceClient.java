package dev.threadly.common.feign;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Integration Service (:3009).
 *
 * Endpoints:
 * - Integration marketplace
 * - OAuth callback handling
 * - Connector management
 */
@FeignClient(
    name = "integration-service",
    url = "${threadly.services.integration-service.url:http://integration-service:3009}"
)
public interface IntegrationServiceClient {

  /**
   * GET /integrations/marketplace — List all available integrations.
   */
  @GetMapping("/integrations/marketplace")
  MarketplaceResponse listMarketplace();

  /**
   * GET /integrations/{orgId} — List connected integrations for org.
   */
  @GetMapping("/integrations/{orgId}")
  ConnectedIntegrationsResponse listConnectedIntegrations(
      @PathVariable UUID orgId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /integrations/{orgId}/{type}/auth — Initiate OAuth or API key entry.
   */
  @PostMapping("/integrations/{orgId}/{type}/auth")
  AuthInitiationResponse initiateAuth(
      @PathVariable UUID orgId,
      @PathVariable String type,
      @RequestBody AuthInitiationRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /integrations/{integrationId}/test — Test connection.
   */
  @PostMapping("/integrations/{integrationId}/test")
  TestConnectionResponse testConnection(
      @PathVariable UUID integrationId,
      @RequestHeader("Authorization") String token
  );

  /**
   * DELETE /integrations/{integrationId} — Disconnect integration.
   */
  @DeleteMapping("/integrations/{integrationId}")
  void disconnectIntegration(
      @PathVariable UUID integrationId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /integrations/{integrationId}/action/{actionName} — Execute action (internal).
   */
  @PostMapping("/integrations/{integrationId}/action/{actionName}")
  ActionExecutionResponse executeAction(
      @PathVariable UUID integrationId,
      @PathVariable String actionName,
      @RequestBody java.util.Map<String, Object> params,
      @RequestHeader("Authorization") String token
  );

  // DTOs

  record MarketplaceResponse(List<IntegrationMarketplaceDTO> integrations, int total) {}

  record IntegrationMarketplaceDTO(
      String type,
      String name,
      String description,
      String category,
      String icon,
      String color,
      List<String> authMethods,
      List<String> availableActions,
      String documentationUrl
  ) {}

  record ConnectedIntegrationsResponse(
      UUID orgId,
      List<ConnectedIntegrationDTO> integrations,
      int total
  ) {}

  record ConnectedIntegrationDTO(
      UUID integrationId,
      String type,
      String displayName,
      String status,
      java.time.Instant connectedAt,
      java.time.Instant lastUsedAt
  ) {}

  record AuthInitiationRequest(
      String credentialType,
      java.util.Map<String, Object> credentials
  ) {}

  record AuthInitiationResponse(
      UUID integrationId,
      String authUrl,
      String state,
      boolean requiresCallback
  ) {}

  record TestConnectionResponse(
      boolean success,
      String message,
      java.util.Map<String, Object> details
  ) {}

  record ActionExecutionResponse(
      boolean success,
      String message,
      java.util.Map<String, Object> result
  ) {}
}
