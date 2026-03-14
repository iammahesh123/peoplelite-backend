package com.hrlite.repository;

import com.hrlite.entity.SalaryRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryRevisionRepository extends JpaRepository<SalaryRevision, UUID> {

    List<SalaryRevision> findByEmployeeIdOrderByEffectiveDateDesc(UUID employeeId);

    List<SalaryRevision> findByTenantIdOrderByEffectiveDateDesc(UUID tenantId);

    List<SalaryRevision> findByTenantId(UUID tenantId);
}
