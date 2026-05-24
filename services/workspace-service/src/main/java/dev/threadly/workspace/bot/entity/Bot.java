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
 * Represents a bot workspace entity. Each bot is scoped to an organization and tracks
 * creation/modification metadata along with soft deletion support.
 *
 * <p>Multi-tenancy: All operations must enforce org_id isolation.
 * Soft Deletes: Bots are never hard deleted; instead, deleted_at is set.
 */
@Entity
@Table(name = "bot", indexes = {@Index(name = "idx_bot_org_id", columnList = "org_id"),
    @Index(name = "idx_bot_deleted_at", columnList = "deleted_at")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot {
  /**
   * Unique identifier for the bot (UUID)
   */
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  /**
   * Organization ID this bot belongs to (multi-tenancy enforcement)
   */
  @Column(name = "org_id", length = 36, nullable = false)
  private String orgId;

  /**
   * Human-readable name of the bot
   */
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  /**
   * Detailed description of the bot's purpose
   */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /**
   * Current status of the bot (DRAFT, PUBLISHED, ARCHIVED)
   */
  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private String status = "DRAFT";

  /**
   * User ID who created this bot
   */
  @Column(name = "created_by", length = 36, nullable = false)
  private String createdBy;

  /**
   * Timestamp when bot was created
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

  /**
   * Soft delete timestamp. When null, bot is active. When set, bot is deleted.
   */
  @Column(name = "deleted_at")
  private Instant deletedAt;
}
