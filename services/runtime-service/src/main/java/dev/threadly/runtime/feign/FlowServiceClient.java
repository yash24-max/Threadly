package dev.threadly.runtime.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for flow-service — fetches the active published flow for a bot.
 */
@FeignClient(name = "flow-service", url = "${services.flow.url:http://localhost:3003}")
public interface FlowServiceClient {

    /**
     * GET /api/v1/flows/{botId}/version/active
     * Returns the active published flow definition for the given bot.
     */
    @GetMapping("/api/v1/flows/{botId}/version/active")
    JsonNode getActiveFlow(
            @PathVariable("botId") String botId,
            @RequestHeader("Authorization") String bearerToken);
}
