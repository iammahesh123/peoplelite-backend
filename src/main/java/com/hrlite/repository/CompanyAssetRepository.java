package com.hrlite.repository;

import com.hrlite.entity.CompanyAsset;
import com.hrlite.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyAssetRepository extends JpaRepository<CompanyAsset, UUID> {

    List<CompanyAsset> findByTenantId(UUID tenantId);

    List<CompanyAsset> findByAssignedTo(UUID employeeId);

    List<CompanyAsset> findByTenantIdAndStatus(UUID tenantId, AssetStatus status);

    long countByTenantIdAndStatus(UUID tenantId, AssetStatus status);

    List<CompanyAsset> findByTenantIdAndAssignedToIsNotNull(UUID tenantId);
}
