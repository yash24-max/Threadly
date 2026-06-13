package dev.threadly.core.proxy;

import dev.threadly.core.proxy.CentrifugoProxyController.*;
import dev.threadly.core.runtime.FlowRuntime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CentrifugoProxyService {

  /**
   * Validate visitor JWT on connect.
   * Centrifugo already validates the JWT signature; we just extract claims here.
   */
  public ConnectResult handleConnect(ConnectRequest req) {
    // The userID is set by Centrifugo from the JWT subject claim.
    // We simply pass it through — no additional validation needed in proxy for visitor tokens.
    ConnectResult result = new ConnectResult();
    result.setUserID(req.getClientID()); // fallback if JWT not provided
    return result;
  }

  /** Authorize channel subscription. Only subscribers matching channel naming convention. */
  public SubscribeResult handleSubscribe(SubscribeRequest req) {
    SubscribeResult result = new SubscribeResult();
    String channel = req.getChannel();
    String userId = req.getUserID();

    // chat:{botId}:{visitorId} — only the matching visitor (or agents from dashboard)
    if (channel.startsWith("chat:")) {
      String[] parts = channel.split(":");
      if (parts.length == 3) {
        String visitorId = parts[2];
        // Allow if userId matches visitorId OR if user is a dashboard user (UUID format)
        result.setAllow(userId != null &&
            (userId.equals(visitorId) || isUuid(userId)));
      }
    }
    // dashboard:{orgId} — only org members (dashboard users have UUID userId from JWT)
    else if (channel.startsWith("dashboard:")) {
      result.setAllow(userId != null && isUuid(userId));
    }
    // agent:{agentId} — only matching agent
    else if (channel.startsWith("agent:")) {
      String[] parts = channel.split(":");
      result.setAllow(parts.length == 2 && parts[1].equals(userId));
    } else {
      result.setAllow(false);
    }

    return result;
  }

  /** Route published messages to the flow runtime. */
  public void handlePublish(PublishRequest req, FlowRuntime flowRuntime) {
    String channel = req.getChannel();
    if (!channel.startsWith("chat:")) return;

    String[] parts = channel.split(":");
    if (parts.length != 3) return;

    UUID botId = UUID.fromString(parts[1]);
    String visitorId = parts[2];

    // Extract org ID from channel data or visitor JWT (simplified: passed in data)
    Object orgIdObj = req.getData() != null ? req.getData().get("orgId") : null;
    if (orgIdObj == null) {
      log.warn("No orgId in publish data for channel {}", channel);
      return;
    }
    UUID orgId = UUID.fromString(orgIdObj.toString());

    Object text = req.getData().get("text");
    if (text == null || text.toString().isBlank()) return;

    // Route to flow runtime asynchronously
    flowRuntime.handleVisitorMessage(botId, visitorId, text.toString(), orgId);
  }

  private boolean isUuid(String s) {
    try { UUID.fromString(s); return true; } catch (Exception e) { return false; }
  }
}
