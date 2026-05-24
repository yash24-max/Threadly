package dev.threadly.runtime.service;

import dev.threadly.runtime.exception.VariableResolutionException;
import dev.threadly.runtime.model.SessionVariable;
import dev.threadly.runtime.repository.SessionVariableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SessionVariableManager handles session variable operations.
 * Supports variable resolution, storage, and retrieval with type safety.
 */
@Service
@Slf4j
public class SessionVariableManager {

  @Autowired
  private SessionVariableRepository sessionVariableRepository;

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

  /**
   * Get all variables for a session as a map
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getAllVariables(String sessionId) {
    List<SessionVariable> variables = sessionVariableRepository.findBySessionId(sessionId);
    Map<String, Object> result = new HashMap<>();

    for (SessionVariable var : variables) {
      result.put(var.getVariableName(), deserializeValue(var.getVariableValue(), var.getDataType()));
    }

    return result;
  }

  /**
   * Get a specific variable value
   */
  @Transactional(readOnly = true)
  public Object getVariable(String sessionId, String variableName) {
    Optional<SessionVariable> optional = sessionVariableRepository
        .findBySessionIdAndVariableName(sessionId, variableName);

    if (optional.isPresent()) {
      SessionVariable var = optional.get();
      return deserializeValue(var.getVariableValue(), var.getDataType());
    }

    return null;
  }

  /**
   * Set a variable value
   */
  @Transactional
  public void setVariable(String sessionId, String variableName, Object value) {
    Optional<SessionVariable> optional = sessionVariableRepository
        .findBySessionIdAndVariableName(sessionId, variableName);

    String serializedValue = serializeValue(value);
    String dataType = getDataType(value);

    if (optional.isPresent()) {
      SessionVariable var = optional.get();
      var.setVariableValue(serializedValue);
      var.setDataType(dataType);
      var.setLastUpdated(LocalDateTime.now());
      sessionVariableRepository.save(var);
      log.debug("Updated variable '{}' in session {}", variableName, sessionId);
    } else {
      SessionVariable var = SessionVariable.builder()
          .id(UUID.randomUUID().toString())
          .sessionId(sessionId)
          .variableName(variableName)
          .variableValue(serializedValue)
          .dataType(dataType)
          .lastUpdated(LocalDateTime.now())
          .build();
      sessionVariableRepository.save(var);
      log.debug("Created variable '{}' in session {}", variableName, sessionId);
    }
  }

  /**
   * Set multiple variables at once
   */
  @Transactional
  public void setVariables(String sessionId, Map<String, Object> variables) {
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      setVariable(sessionId, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Delete a variable
   */
  @Transactional
  public void deleteVariable(String sessionId, String variableName) {
    Optional<SessionVariable> optional = sessionVariableRepository
        .findBySessionIdAndVariableName(sessionId, variableName);

    optional.ifPresent(var -> {
      sessionVariableRepository.delete(var);
      log.debug("Deleted variable '{}' from session {}", variableName, sessionId);
    });
  }

  /**
   * Delete all variables for a session
   */
  @Transactional
  public void deleteAllVariables(String sessionId) {
    sessionVariableRepository.deleteBySessionId(sessionId);
    log.debug("Deleted all variables for session {}", sessionId);
  }

  /**
   * Resolve {{variable}} references in a string
   */
  public String resolveVariables(String text, Map<String, Object> variables) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    Matcher matcher = VARIABLE_PATTERN.matcher(text);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String variableName = matcher.group(1).trim();
      Object value = variables.get(variableName);

      if (value != null) {
        String replacement = Matcher.quoteReplacement(String.valueOf(value));
        matcher.appendReplacement(result, replacement);
      } else {
        // Keep the original pattern if variable not found
        matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
        log.warn("Variable not found during resolution: {}", variableName);
      }
    }

    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Check if a variable exists
   */
  @Transactional(readOnly = true)
  public boolean hasVariable(String sessionId, String variableName) {
    return sessionVariableRepository.existsBySessionIdAndVariableName(sessionId, variableName);
  }

  /**
   * Serialize value to JSON string
   */
  private String serializeValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof Number) {
      return value.toString();
    }
    if (value instanceof Boolean) {
      return value.toString();
    }
    // For complex objects, use toString (could implement JSON serialization)
    return value.toString();
  }

  /**
   * Deserialize value from JSON string
   */
  private Object deserializeValue(String value, String dataType) {
    if (value == null) {
      return null;
    }

    if ("NUMBER".equals(dataType)) {
      try {
        if (value.contains(".")) {
          return Double.parseDouble(value);
        }
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        log.warn("Failed to parse number: {}", value);
        return value;
      }
    }

    if ("BOOLEAN".equals(dataType)) {
      return Boolean.parseBoolean(value);
    }

    // Default to string
    return value;
  }

  /**
   * Determine data type from object
   */
  private String getDataType(Object value) {
    if (value == null) {
      return "STRING";
    }
    if (value instanceof String) {
      return "STRING";
    }
    if (value instanceof Boolean) {
      return "BOOLEAN";
    }
    if (value instanceof Number) {
      return "NUMBER";
    }
    if (value instanceof java.util.List) {
      return "ARRAY";
    }
    if (value instanceof java.util.Map) {
      return "OBJECT";
    }
    return "STRING";
  }
}
