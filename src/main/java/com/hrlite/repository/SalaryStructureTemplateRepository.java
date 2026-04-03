package com.hrlite.repository;

import com.hrlite.entity.SalaryStructureTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryStructureTemplateRepository extends JpaRepository<SalaryStructureTemplate, UUID> {
    List<SalaryStructureTemplate> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<SalaryStructureTemplate> findByTenantIdAndIsDefaultTrue(UUID tenantId);
}
