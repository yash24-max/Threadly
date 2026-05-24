package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FlowValidation entity storing validation status and errors for a flow.
 * Tracks whether a flow is valid according to business rules.
 */
@Entity
@Table(name = "flow_validation", indexes = {
    @Index(name = "idx_flow_validation_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_validation_valid", columnList = "is_valid")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowValidation {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false, unique = true)
  private String flowId;

  @Column(name = "is_valid", nullable = false)
  private Boolean isValid;

  @Column(name = "validation_errors_json", columnDefinition = "text")
  private String validationErrorsJson;

  @Column(name = "last_validated_at")
  private LocalDateTime lastValidatedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Marks this validation as valid with no errors.
   */
  public void markAsValid() {
    this.isValid = true;
    this.validationErrorsJson = "[]";
    this.lastValidatedAt = LocalDateTime.now();
  }

  /**
   * Marks this validation as invalid with error details.
   *
   * @param errorsJson JSON array of error objects
   */
  public void markAsInvalid(String errorsJson) {
    this.isValid = false;
    this.validationErrorsJson = errorsJson;
    this.lastValidatedAt = LocalDateTime.now();
  }

  /**
   * Checks if this validation has expired (older than 1 hour).
   *
   * @return true if validation is older than 1 hour
   */
  public boolean isExpired() {
    if (lastValidatedAt == null) {
      return true;
    }
    return LocalDateTime.now().minusHours(1).isAfter(lastValidatedAt);
  }
}
