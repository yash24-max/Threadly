package dev.threadly.runtime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * SessionVariable represents a single variable within a session context.
 * Supports type-safe storage and retrieval of session state variables.
 */
@Entity
@Table(name = "session_variables", indexes = {
    @Index(name = "idx_session_var_session_id", columnList = "session_id"),
    @Index(name = "idx_session_var_name", columnList = "variable_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionVariable {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 36)
  private String sessionId;

  @Column(nullable = false, length = 255)
  private String variableName;

  @Column(columnDefinition = "TEXT")
  private String variableValue;

  @Column(length = 50)
  private String dataType; // STRING, NUMBER, BOOLEAN, OBJECT, ARRAY

  @UpdateTimestamp
  private LocalDateTime lastUpdated;

  @Version
  private Long version;
}
