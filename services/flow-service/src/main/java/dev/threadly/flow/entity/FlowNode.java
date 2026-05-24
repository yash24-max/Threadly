package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FlowNode entity representing a node in a flow diagram.
 * Stores node metadata including position and configuration.
 */
@Entity
@Table(name = "flow_node", indexes = {
    @Index(name = "idx_flow_node_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_node_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowNode {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false)
  private String flowId;

  @Column(name = "node_id", nullable = false)
  private String nodeId;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "position_x", nullable = false)
  private Double positionX;

  @Column(name = "position_y", nullable = false)
  private Double positionY;

  @Column(name = "data_json", columnDefinition = "text")
  private String dataJson;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Updates node position coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void updatePosition(Double x, Double y) {
    this.positionX = x;
    this.positionY = y;
  }

  /**
   * Updates node data configuration.
   *
   * @param dataJson the node configuration as JSON
   */
  public void updateData(String dataJson) {
    this.dataJson = dataJson;
  }
}
