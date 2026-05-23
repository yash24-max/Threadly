package dev.threadly.core.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "memberships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgMembership {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  @Builder.Default
  private String role = "MEMBER";

  @Column(name = "invited_by")
  private UUID invitedBy;

  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
