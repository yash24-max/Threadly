package dev.threadly.core.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgMembershipRepository extends JpaRepository<OrgMembership, UUID> {

  List<OrgMembership> findAllByOrgId(UUID orgId);

  Optional<OrgMembership> findByOrgIdAndUserId(UUID orgId, UUID userId);

  boolean existsByOrgIdAndUserId(UUID orgId, UUID userId);

  void deleteByOrgIdAndUserId(UUID orgId, UUID userId);
}
