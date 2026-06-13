package dev.threadly.core.knowledge;

import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.Org;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "kb_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.FilterDef(name = "orgFilterKb",
    parameters = @org.hibernate.annotations.ParamDef(name = "orgId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "orgFilterKb", condition = "org_id = :orgId")
public class KbDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bot_id", nullable = false)
  private Bot bot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "org_id", nullable = false)
  private Org org;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type; // pdf | txt | url | html | docx

  @Column(name = "storage_key")
  private String storageKey;

  @Column(name = "source_url")
  private String sourceUrl;

  @Column(nullable = false)
  @Builder.Default
  private String status = "pending"; // pending | indexing | ready | error

  @Column(name = "chunk_count")
  private Integer chunkCount;

  @Column(name = "error_msg")
  private String errorMsg;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
