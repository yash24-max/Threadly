package dev.threadly.runtime.controller;

import dev.threadly.runtime.dto.ExecutionLogDto;
import dev.threadly.runtime.exception.SessionNotFoundException;
import dev.threadly.runtime.model.ExecutionLog;
import dev.threadly.runtime.model.Session;
import dev.threadly.runtime.repository.ExecutionLogRepository;
import dev.threadly.runtime.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ExecutionController handles execution tracking and resumption endpoints
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Slf4j
public class ExecutionController {

  @Autowired
  private SessionService sessionService;

  @Autowired
  private ExecutionLogRepository executionLogRepository;

  /**
   * Get execution log for a session
   * GET /api/v1/sessions/{sessionId}/execution-log
   */
  @GetMapping("/{sessionId}/execution-log")
  public ResponseEntity<?> getExecutionLog(@PathVariable String sessionId) {
    log.debug("Retrieving execution log for session: {}", sessionId);

    try {
      sessionService.getSession(sessionId);

      List<ExecutionLog> logs = executionLogRepository.findBySessionId(sessionId);
      List<ExecutionLogDto> dtos = logs.stream()
          .map(this::mapToDto)
          .collect(Collectors.toList());

      return ResponseEntity.ok(dtos);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get execution log filtered by node
   * GET /api/v1/sessions/{sessionId}/execution-log?nodeId={nodeId}
   */
  @GetMapping("/{sessionId}/execution-log/node/{nodeId}")
  public ResponseEntity<?> getNodeExecutionLog(
      @PathVariable String sessionId,
      @PathVariable String nodeId) {
    log.debug("Retrieving execution log for session: {}, node: {}", sessionId, nodeId);

    try {
      sessionService.getSession(sessionId);

      List<ExecutionLog> logs = executionLogRepository.findBySessionIdAndNodeId(sessionId, nodeId);
      List<ExecutionLogDto> dtos = logs.stream()
          .map(this::mapToDto)
          .collect(Collectors.toList());

      return ResponseEntity.ok(dtos);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get failed executions for a session
   * GET /api/v1/sessions/{sessionId}/execution-log/failures
   */
  @GetMapping("/{sessionId}/execution-log/failures")
  public ResponseEntity<?> getFailedExecutions(@PathVariable String sessionId) {
    log.debug("Retrieving failed executions for session: {}", sessionId);

    try {
      sessionService.getSession(sessionId);

      List<ExecutionLog> logs = executionLogRepository.findFailedLogsBySessionId(sessionId);
      List<ExecutionLogDto> dtos = logs.stream()
          .map(this::mapToDto)
          .collect(Collectors.toList());

      return ResponseEntity.ok(dtos);
    } catch (SessionNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Map ExecutionLog entity to DTO
   */
  private ExecutionLogDto mapToDto(ExecutionLog log) {
    return ExecutionLogDto.builder()
        .id(log.getId())
        .sessionId(log.getSessionId())
        .nodeId(log.getNodeId())
        .nodeType(log.getNodeType())
        .inputJson(log.getInputJson())
        .outputJson(log.getOutputJson())
        .executionTimeMs(log.getExecutionTimeMs())
        .status(log.getStatus())
        .errorDetails(log.getErrorDetails())
        .createdAt(log.getCreatedAt())
        .build();
  }

  // Add missing import
  private static class Map {
    static <K, V> java.util.Map<K, V> of(K k1, V v1) {
      java.util.Map<K, V> map = new java.util.HashMap<>();
      map.put(k1, v1);
      return map;
    }

    static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2) {
      java.util.Map<K, V> map = new java.util.HashMap<>();
      map.put(k1, v1);
      map.put(k2, v2);
      return map;
    }
  }
}
