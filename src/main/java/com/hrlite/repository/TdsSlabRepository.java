package com.hrlite.repository;

import com.hrlite.entity.TdsSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TdsSlabRepository extends JpaRepository<TdsSlab, UUID> {
    List<TdsSlab> findByTenantIdOrderByOrderIndexAsc(UUID tenantId);
    List<TdsSlab> findByTenantIdAndRegimeOrderByOrderIndexAsc(UUID tenantId, String regime);
    void deleteByTenantId(UUID tenantId);
}
