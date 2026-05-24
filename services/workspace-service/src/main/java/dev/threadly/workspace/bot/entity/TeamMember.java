package dev.threadly.workspace.bot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Represents a team member's access to a bot with their assigned role.
 * Supports role-based access control: OWNER, EDITOR, VIEWER.
 */
@Entity
@Table(name = "team_member", indexes = {@Index(name = "idx_team_member_bot_id", columnList = "bot_id"),
    @Index(name = "idx_team_member_user_id", columnList = "user_id"),
    @Index(name = "idx_team_member_bot_user", columnList = "bot_id, user_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {
  /**
   * Unique identifier for this team member entry (UUID)
   */
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  /**
   * Reference to the Bot
   */
  @Column(name = "bot_id", length = 36, nullable = false)
  private String botId;

  /**
   * User ID of the team member
   */
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  /**
   * User's role on this bot: OWNER, EDITOR, VIEWER
   */
  @Column(name = "role", nullable = false, length = 32)
  private String role;

  /**
   * Timestamp when member was added
   */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**
   * Timestamp of last modification
   */
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
