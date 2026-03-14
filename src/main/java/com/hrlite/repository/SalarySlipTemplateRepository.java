package com.hrlite.repository;

import com.hrlite.entity.SalarySlipTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalarySlipTemplateRepository extends JpaRepository<SalarySlipTemplate, UUID> {

    List<SalarySlipTemplate> findByTenantIdAndActiveTrueOrderByCreatedAtDesc(UUID tenantId);

    List<SalarySlipTemplate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<SalarySlipTemplate> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<SalarySlipTemplate> findByTenantIdAndIsDefaultTrueAndActiveTrue(UUID tenantId);
}
