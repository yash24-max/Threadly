package dev.threadly.runtime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * VisitorProfile maintains visitor information within a session context.
 * Captures email, name, phone, and custom fields for personalization.
 */
@Entity
@Table(name = "visitor_profiles", indexes = {
    @Index(name = "idx_visitor_profile_session_id", columnList = "session_id"),
    @Index(name = "idx_visitor_profile_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorProfile {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 36)
  private String sessionId;

  @Column(length = 255)
  private String email;

  @Column(length = 255)
  private String name;

  @Column(length = 20)
  private String phone;

  @Column(columnDefinition = "TEXT")
  private String customFieldsJson;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Version
  private Long version;
}
