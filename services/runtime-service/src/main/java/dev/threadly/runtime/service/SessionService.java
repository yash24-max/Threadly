package dev.threadly.runtime.service;

import dev.threadly.runtime.exception.SessionNotFoundException;
import dev.threadly.runtime.model.ConversationMemory;
import dev.threadly.runtime.model.ExecutionState;
import dev.threadly.runtime.model.Session;
import dev.threadly.runtime.model.VisitorProfile;
import dev.threadly.runtime.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SessionService manages session lifecycle operations.
 * Handles session creation, updates, queries, and termination.
 */
@Service
@Slf4j
public class SessionService {

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private SessionVariableRepository sessionVariableRepository;

  @Autowired
  private ExecutionStateRepository executionStateRepository;

  @Autowired
  private VisitorProfileRepository visitorProfileRepository;

  @Autowired
  private ConversationMemoryRepository conversationMemoryRepository;

  @Autowired
  private ExecutionLogRepository executionLogRepository;

  /**
   * Create a new session
   */
  @Transactional
  public Session createSession(String botId, String flowId, String visitorId) {
    log.info("Creating new session for bot: {}, flow: {}, visitor: {}", botId, flowId, visitorId);

    Session session = Session.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .flowId(flowId)
        .visitorId(visitorId)
        .state(Session.SessionState.ACTIVE)
        .sessionVariablesJson("{}")
        .tokenUsageCount(0)
        .createdAt(LocalDateTime.now())
        .lastMessageAt(LocalDateTime.now())
        .build();

    session = sessionRepository.save(session);

    // Initialize execution state
    ExecutionState executionState = ExecutionState.builder()
        .id(UUID.randomUUID().toString())
        .sessionId(session.getId())
        .status(ExecutionState.ExecutionStatus.RUNNING)
        .executionStackJson("[]")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    executionStateRepository.save(executionState);

    // Initialize conversation memory
    ConversationMemory memory = ConversationMemory.builder()
        .id(UUID.randomUUID().toString())
        .sessionId(session.getId())
        .summary("")
        .recentTurnsJson("[]")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    conversationMemoryRepository.save(memory);

    log.info("Session created: {}", session.getId());
    return session;
  }

  /**
   * Get session by ID
   */
  @Transactional(readOnly = true)
  public Session getSession(String sessionId) {
    return sessionRepository.findById(sessionId)
        .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
  }

  /**
   * Update session state
   */
  @Transactional
  public void updateSessionState(String sessionId, Session.SessionState newState) {
    Session session = getSession(sessionId);
    session.setState(newState);
    session.setLastMessageAt(LocalDateTime.now());

    if (newState == Session.SessionState.ENDED) {
      session.setEndedAt(LocalDateTime.now());
    }

    sessionRepository.save(session);
    log.info("Updated session {} state to {}", sessionId, newState);
  }

  /**
   * Get all active sessions for a bot
   */
  @Transactional(readOnly = true)
  public List<Session> getActiveSessions(String botId) {
    return sessionRepository.findActiveSessionsByBotId(botId);
  }

  /**
   * Get all sessions for a visitor
   */
  @Transactional(readOnly = true)
  public List<Session> getVisitorSessions(String visitorId) {
    return sessionRepository.findActiveSessionsByVisitorId(visitorId);
  }

  /**
   * Pause session (waiting for user input)
   */
  @Transactional
  public void pauseSession(String sessionId) {
    updateSessionState(sessionId, Session.SessionState.PAUSED);
    log.info("Session paused: {}", sessionId);
  }

  /**
   * Resume paused session
   */
  @Transactional
  public void resumeSession(String sessionId) {
    updateSessionState(sessionId, Session.SessionState.ACTIVE);
    log.info("Session resumed: {}", sessionId);
  }

  /**
   * End session
   */
  @Transactional
  public void endSession(String sessionId) {
    updateSessionState(sessionId, Session.SessionState.ENDED);
    log.info("Session ended: {}", sessionId);
  }

  /**
   * Update session variables
   */
  @Transactional
  public void updateSessionVariables(String sessionId, Map<String, Object> variables) {
    Session session = getSession(sessionId);
    session.setLastMessageAt(LocalDateTime.now());
    sessionRepository.save(session);
    log.debug("Updated variables for session: {}", sessionId);
  }

  /**
   * Add to token usage
   */
  @Transactional
  public void addTokenUsage(String sessionId, Integer tokenCount) {
    Session session = getSession(sessionId);
    session.setTokenUsageCount(session.getTokenUsageCount() + tokenCount);
    sessionRepository.save(session);
    log.debug("Added {} tokens to session: {} (total: {})",
        tokenCount, sessionId, session.getTokenUsageCount());
  }

  /**
   * Store visitor profile in session
   */
  @Transactional
  public void saveVisitorProfile(String sessionId, String email, String name, String phone) {
    Optional<VisitorProfile> existing = visitorProfileRepository.findBySessionId(sessionId);

    if (existing.isPresent()) {
      VisitorProfile profile = existing.get();
      profile.setEmail(email);
      profile.setName(name);
      profile.setPhone(phone);
      visitorProfileRepository.save(profile);
    } else {
      VisitorProfile profile = VisitorProfile.builder()
          .id(UUID.randomUUID().toString())
          .sessionId(sessionId)
          .email(email)
          .name(name)
          .phone(phone)
          .build();
      visitorProfileRepository.save(profile);
    }
    log.debug("Saved visitor profile for session: {}", sessionId);
  }

  /**
   * Get visitor profile
   */
  @Transactional(readOnly = true)
  public Optional<VisitorProfile> getVisitorProfile(String sessionId) {
    return visitorProfileRepository.findBySessionId(sessionId);
  }

  /**
   * Clean up ended sessions older than specified days
   */
  @Transactional
  public void cleanupOldSessions(int daysOld) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
    List<Session> oldSessions = sessionRepository.findSessionsCreatedAfter(cutoffDate);

    for (Session session : oldSessions) {
      if (session.getState() == Session.SessionState.ENDED) {
        // Delete associated data
        sessionVariableRepository.deleteBySessionId(session.getId());
        executionStateRepository.deleteBySessionId(session.getId());
        visitorProfileRepository.deleteBySessionId(session.getId());
        conversationMemoryRepository.deleteBySessionId(session.getId());
        // executionLogRepository would need similar method

        sessionRepository.delete(session);
        log.debug("Cleaned up old session: {}", session.getId());
      }
    }
  }

  /**
   * Count active sessions for a bot
   */
  @Transactional(readOnly = true)
  public Long countActiveSessions(String botId) {
    return sessionRepository.countActiveSessionsByBotId(botId);
  }
}
