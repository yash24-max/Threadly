package dev.threadly.workspace.bot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents a published version of a bot. Each time a bot is published,
 * a new BotVersion entry is created to maintain version history.
 */
@Entity
@Table(name = "bot_version", indexes = {@Index(name = "idx_bot_version_bot_id", columnList = "bot_id"),
    @Index(name = "idx_bot_version_version_number", columnList = "bot_id, version_number")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotVersion {
  /**
   * Unique identifier for this version (UUID)
   */
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  /**
   * Reference to the Bot this version belongs to
   */
  @Column(name = "bot_id", length = 36, nullable = false)
  private String botId;

  /**
   * Version number (sequential, auto-incrementing)
   */
  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  /**
   * Snapshot of bot configuration as JSON at time of publish
   */
  @Column(name = "config_snapshot", columnDefinition = "LONGTEXT")
  private String configSnapshot;

  /**
   * Timestamp when this version was published
   */
  @CreationTimestamp
  @Column(name = "published_at", nullable = false)
  private Instant publishedAt;

  /**
   * User ID who published this version
   */
  @Column(name = "published_by", length = 36, nullable = false)
  private String publishedBy;

  /**
   * Release notes or changelog for this version
   */
  @Column(name = "release_notes", columnDefinition = "TEXT")
  private String releaseNotes;
}
