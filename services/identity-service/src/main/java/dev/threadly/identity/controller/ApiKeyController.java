package dev.threadly.identity.controller;

import dev.threadly.identity.dto.ApiKeyDto;
import dev.threadly.identity.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for API key management endpoints.
 * Handles API key generation, listing, and revocation.
 */
@Slf4j
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

  private final ApiKeyService apiKeyService;

  /**
   * Generate a new API key for the organization.
   *
   * @param orgId the organization ID (injected from auth context)
   * @param name human-readable name for the key
   * @param expiresIn expiry time in days (0 for non-expiring)
   * @param scopes comma-separated scopes (default: "read,write")
   * @return response containing the plaintext key (only shown once)
   */
  @PostMapping
  public ResponseEntity<Map<String, String>> generateApiKey(
      @RequestHeader("X-Org-Id") String orgId,
      @RequestParam String name,
      @RequestParam(defaultValue = "0") int expiresIn,
      @RequestParam(defaultValue = "read,write") String scopes) {

    log.info("Generating API key: {} for org: {}", name, orgId);

    String keyValue = apiKeyService.generateApiKey(orgId, name, expiresIn, scopes);

    Map<String, String> response = new HashMap<>();
    response.put("key", keyValue);
    response.put("message", "Store this key securely. It will not be shown again.");

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get all active API keys for the organization.
   *
   * @param orgId the organization ID (injected from auth context)
   * @return list of ApiKeyDto (keys are not shown in list)
   */
  @GetMapping
  public ResponseEntity<List<ApiKeyDto>> listApiKeys(
      @RequestHeader("X-Org-Id") String orgId) {

    log.debug("Listing API keys for org: {}", orgId);

    List<ApiKeyDto> keys = apiKeyService.toDto(apiKeyService.getOrgApiKeys(orgId));

    return ResponseEntity.ok(keys);
  }

  /**
   * Get a specific API key by ID.
   *
   * @param keyId the API key ID
   * @return ApiKeyDto with key details (not the full key)
   */
  @GetMapping("/{keyId}")
  public ResponseEntity<ApiKeyDto> getApiKey(@PathVariable String keyId) {

    log.debug("Fetching API key: {}", keyId);

    ApiKeyDto key = apiKeyService.toDto(apiKeyService.getApiKeyById(keyId));

    return ResponseEntity.ok(key);
  }

  /**
   * Revoke an API key.
   * Revoked keys can no longer be used for authentication.
   *
   * @param keyId the API key ID to revoke
   * @return success response
   */
  @DeleteMapping("/{keyId}")
  public ResponseEntity<Void> revokeApiKey(@PathVariable String keyId) {

    log.info("Revoking API key: {}", keyId);
    apiKeyService.revokeApiKey(keyId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Rotate an API key.
   * Revokes the old key and generates a new one.
   *
   * @param keyId the API key ID to rotate
   * @param orgId the organization ID (injected from auth context)
   * @param name name for the new key
   * @param expiresIn expiry time in days (0 for non-expiring)
   * @param scopes comma-separated scopes
   * @return response containing the new plaintext key
   */
  @PostMapping("/{keyId}/rotate")
  public ResponseEntity<Map<String, String>> rotateApiKey(
      @PathVariable String keyId,
      @RequestHeader("X-Org-Id") String orgId,
      @RequestParam String name,
      @RequestParam(defaultValue = "0") int expiresIn,
      @RequestParam(defaultValue = "read,write") String scopes) {

    log.info("Rotating API key: {} for org: {}", keyId, orgId);

    String newKeyValue = apiKeyService.rotateApiKey(keyId, orgId, name, expiresIn, scopes);

    Map<String, String> response = new HashMap<>();
    response.put("key", newKeyValue);
    response.put("message", "Store this new key securely. The old key has been revoked.");

    return ResponseEntity.ok(response);
  }
}
