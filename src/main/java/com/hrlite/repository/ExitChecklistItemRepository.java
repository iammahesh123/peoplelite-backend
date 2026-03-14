package com.hrlite.repository;

import com.hrlite.entity.ExitChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExitChecklistItemRepository extends JpaRepository<ExitChecklistItem, UUID> {

    List<ExitChecklistItem> findBySeparationId(UUID separationId);

    List<ExitChecklistItem> findBySeparationIdOrderByCategory(UUID separationId);

    Optional<ExitChecklistItem> findByIdAndTenantId(UUID id, UUID tenantId);

    long countBySeparationIdAndCompletedTrue(UUID separationId);

    long countBySeparationId(UUID separationId);
}
