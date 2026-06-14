package dev.threadly.admin.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for identity-service.
 * Admin calls are made with a service-account token passed as Authorization header.
 */
@FeignClient(name = "identity-service", url = "${services.identity.url:http://localhost:3001}")
public interface IdentityServiceClient {

    @GetMapping("/v1/admin/orgs")
    JsonNode listOrganizations(@RequestHeader("Authorization") String bearerToken,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size);

    @GetMapping("/v1/admin/users")
    JsonNode listUsers(@RequestHeader("Authorization") String bearerToken,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size);

    @PutMapping("/v1/admin/users/{id}/status")
    void setUserStatus(@RequestHeader("Authorization") String bearerToken,
                       @PathVariable("id") String userId,
                       @RequestBody StatusRequest body);

    record StatusRequest(boolean enabled) {}
}
