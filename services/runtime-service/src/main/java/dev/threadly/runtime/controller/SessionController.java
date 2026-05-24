package dev.threadly.runtime.controller;

import dev.threadly.runtime.dto.CreateSessionRequest;
import dev.threadly.runtime.dto.SendMessageRequest;
import dev.threadly.runtime.dto.SessionDto;
import dev.threadly.runtime.exception.SessionNotFoundException;
import dev.threadly.runtime.model.Session;
import dev.threadly.runtime.service.RuntimeExecutor;
import dev.threadly.runtime.service.SessionService;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * SessionController handles session management REST endpoints
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Validated
@Slf4j
public class SessionController {

  @Autowired
  private SessionService sessionService;

  @Autowired
  private SessionVariableManager variableManager;

  @Autowired
  private RuntimeExecutor runtimeExecutor;

  /**
   * Create a new session
   * POST /api/v1/sessions
   */
  @PostMapping
  public ResponseEntity<SessionDto> createSession(@Valid @RequestBody CreateSessionRequest request) {
    log.info("Creating new session - bot: {}, flow: {}, visitor: {}",
        request.getBotId(), request.getFlowId(), request.getVisitorId());

    Session session = sessionService.createSession(
        request.getBotId(),
        request.getFlowId(),
        request.getVisitorId()
    );

    SessionDto dto = mapToDto(session);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  /**
   * Get session details
   * GET /api/v1/sessions/{sessionId}
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionDto> getSession(@PathVariable String sessionId) {
    log.debug("Retrieving session: {}", sessionId);

    try {
      Session session = sessionService.getSession(sessionId);
      SessionDto dto = mapToDto(session);
      return ResponseEntity.ok(dto);
    } catch (SessionNotFoundException e) {
      log.warn("Session not found: {}", sessionId);
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Send message to session
   * POST /api/v1/sessions/{sessionId}/message
   */
  @PostMapping("/{sessionId}/message")
  public ResponseEntity<?> sendMessage(
      @PathVariable String sessionId,
      @Valid @RequestBody SendMessageRequest request) {
    log.info("Sending message to session: {}", sessionId);

    try {
      Session session = sessionService.getSession(sessionId);

      // Store the message as a variable for processing
      // The flow will handle the message through the question node

      SessionDto dto = mapToDto(session);
      return ResponseEntity.ok(dto);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * End session
   * POST /api/v1/sessions/{sessionId}/end
   */
  @PostMapping("/{sessionId}/end")
  public ResponseEntity<?> endSession(@PathVariable String sessionId) {
    log.info("Ending session: {}", sessionId);

    try {
      sessionService.endSession(sessionId);
      Session session = sessionService.getSession(sessionId);
      SessionDto dto = mapToDto(session);
      return ResponseEntity.ok(dto);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get session state
   * GET /api/v1/sessions/{sessionId}/state
   */
  @GetMapping("/{sessionId}/state")
  public ResponseEntity<?> getSessionState(@PathVariable String sessionId) {
    log.debug("Getting session state: {}", sessionId);

    try {
      Session session = sessionService.getSession(sessionId);
      Map<String, Object> state = variableManager.getAllVariables(sessionId);

      return ResponseEntity.ok(Map.of(
          "sessionId", sessionId,
          "state", session.getState(),
          "variables", state,
          "tokenUsage", session.getTokenUsageCount()
      ));
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Pause session
   * POST /api/v1/sessions/{sessionId}/pause
   */
  @PostMapping("/{sessionId}/pause")
  public ResponseEntity<?> pauseSession(@PathVariable String sessionId) {
    log.info("Pausing session: {}", sessionId);

    try {
      sessionService.pauseSession(sessionId);
      Session session = sessionService.getSession(sessionId);
      SessionDto dto = mapToDto(session);
      return ResponseEntity.ok(dto);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Resume session
   * POST /api/v1/sessions/{sessionId}/resume
   */
  @PostMapping("/{sessionId}/resume")
  public ResponseEntity<?> resumeSession(@PathVariable String sessionId) {
    log.info("Resuming session: {}", sessionId);

    try {
      sessionService.resumeSession(sessionId);
      Session session = sessionService.getSession(sessionId);
      SessionDto dto = mapToDto(session);
      return ResponseEntity.ok(dto);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Map Session entity to DTO
   */
  private SessionDto mapToDto(Session session) {
    Map<String, Object> variables = variableManager.getAllVariables(session.getId());

    return SessionDto.builder()
        .id(session.getId())
        .botId(session.getBotId())
        .flowId(session.getFlowId())
        .visitorId(session.getVisitorId())
        .state(session.getState())
        .variables(variables)
        .tokenUsageCount(session.getTokenUsageCount())
        .createdAt(session.getCreatedAt())
        .lastMessageAt(session.getLastMessageAt())
        .endedAt(session.getEndedAt())
        .build();
  }
}
