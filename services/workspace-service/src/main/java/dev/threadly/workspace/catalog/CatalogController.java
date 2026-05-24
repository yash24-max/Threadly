package dev.threadly.workspace.catalog;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dev.threadly.workspace.catalog.dto.NodeCatalogEntryDto;
import dev.threadly.workspace.catalog.dto.TemplateDto;
import dev.threadly.workspace.catalog.dto.IntegrationDto;

/**
 * REST API endpoints for bot builder catalogs.
 * Serves dynamic node types, templates, and available integrations.
 *
 * Endpoints:
 * - GET /v1/catalogs/node-types              (new endpoints)
 * - GET /v1/internal/node-catalog            (frontend compatibility)
 * - GET /v1/catalogs/templates
 * - GET /v1/templates
 * - GET /v1/catalogs/integrations
 * - GET /v1/integrations/catalog
 */
@RestController
public class CatalogController {

  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  // === NODE CATALOG ===

  /**
   * GET /v1/catalogs/node-types
   * GET /v1/internal/node-catalog
   *
   * Returns all available node types for the flow builder.
   * Cached in frontend for 24h to minimize requests.
   *
   * @return List of all 25+ node type definitions with icons, colors, defaults
   */
  @RequestMapping(
      value = {"/v1/catalogs/node-types", "/v1/internal/node-catalog"},
      method = RequestMethod.GET)
  public ResponseEntity<List<NodeCatalogEntryDto>> getNodeCatalog() {
    return ResponseEntity.ok(catalogService.getNodeCatalog());
  }

  // === TEMPLATES ===

  /**
   * GET /v1/catalogs/templates
   * GET /v1/templates
   *
   * Returns all templates available to the authenticated org.
   * Includes 20+ pre-built templates (customer-support, lead-qualification, etc.)
   * as well as any custom templates the org has created.
   *
   * @return List of templates with preview definitions and metadata
   */
  @RequestMapping(
      value = {"/v1/catalogs/templates", "/v1/templates"},
      method = RequestMethod.GET)
  public ResponseEntity<List<TemplateDto>> getTemplates() {
    return ResponseEntity.ok(catalogService.getTemplates());
  }

  /**
   * GET /v1/bots/{botId}/templates
   *
   * Returns templates for a specific bot (org-level templates).
   *
   * @param botId the bot ID
   * @return List of templates applicable to this bot
   */
  @GetMapping("/v1/bots/{botId}/templates")
  public ResponseEntity<List<TemplateDto>> getTemplatesForBot(@PathVariable String botId) {
    return ResponseEntity.ok(catalogService.getTemplates());
  }

  // === INTEGRATIONS ===

  /**
   * GET /v1/catalogs/integrations
   * GET /v1/integrations/catalog
   *
   * Returns all available integrations this bot can use in flows.
   * Includes 20+ integrations: Slack, HubSpot, Google Sheets, Twilio, Notion, etc.
   *
   * @return List of available integrations with connection status and auth requirements
   */
  @RequestMapping(
      value = {"/v1/catalogs/integrations", "/v1/integrations/catalog"},
      method = RequestMethod.GET)
  public ResponseEntity<List<IntegrationDto>> getIntegrations() {
    return ResponseEntity.ok(catalogService.getIntegrations());
  }

  /**
   * POST /v1/catalogs/integrations/search
   *
   * Search integrations by category or name (e.g., "CRM", "messaging", "Google")
   *
   * @param query search term
   * @return Filtered list of integrations
   */
  @PostMapping("/v1/catalogs/integrations/search")
  public ResponseEntity<List<IntegrationDto>> searchIntegrations(
      @RequestParam String query) {
    return ResponseEntity.ok(catalogService.searchIntegrations(query));
  }
}
