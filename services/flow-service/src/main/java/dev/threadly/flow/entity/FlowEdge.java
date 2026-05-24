package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FlowEdge entity representing a connection between nodes in a flow.
 * Stores edge source, target, and connection handle information.
 */
@Entity
@Table(name = "flow_edge", indexes = {
    @Index(name = "idx_flow_edge_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_edge_source", columnList = "source_node_id"),
    @Index(name = "idx_flow_edge_target", columnList = "target_node_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowEdge {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false)
  private String flowId;

  @Column(name = "edge_id", nullable = false)
  private String edgeId;

  @Column(name = "source_node_id", nullable = false)
  private String sourceNodeId;

  @Column(name = "target_node_id", nullable = false)
  private String targetNodeId;

  @Column(name = "source_handle")
  private String sourceHandle;

  @Column(name = "target_handle")
  private String targetHandle;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Checks if this edge creates a cycle (source and target are the same).
   *
   * @return true if this edge would create a self-loop
   */
  public boolean isSelfLoop() {
    return sourceNodeId.equals(targetNodeId);
  }

  /**
   * Checks if this edge connects two nodes.
   *
   * @param fromNodeId the source node ID
   * @param toNodeId the target node ID
   * @return true if this edge connects the specified nodes
   */
  public boolean connects(String fromNodeId, String toNodeId) {
    return sourceNodeId.equals(fromNodeId) && targetNodeId.equals(toNodeId);
  }
}
