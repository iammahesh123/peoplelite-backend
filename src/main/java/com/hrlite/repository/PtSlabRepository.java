package com.hrlite.repository;

import com.hrlite.entity.PtSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PtSlabRepository extends JpaRepository<PtSlab, UUID> {
    List<PtSlab> findByTenantIdOrderByOrderIndexAsc(UUID tenantId);
    void deleteByTenantId(UUID tenantId);
}
