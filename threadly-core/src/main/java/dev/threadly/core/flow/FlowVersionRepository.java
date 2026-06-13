package dev.threadly.core.flow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowVersionRepository extends JpaRepository<FlowVersion, UUID> {

  List<FlowVersion> findAllByFlowIdOrderByVersionNumDesc(UUID flowId);

  Optional<FlowVersion> findByFlowIdAndVersionNum(UUID flowId, int versionNum);

  int countByFlowId(UUID flowId);
}
