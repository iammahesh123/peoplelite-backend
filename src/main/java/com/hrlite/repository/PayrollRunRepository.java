package com.hrlite.repository;

import com.hrlite.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    List<PayrollRun> findByTenantIdOrderByYearDescMonthDesc(UUID tenantId);

    Optional<PayrollRun> findByTenantIdAndMonthAndYear(UUID tenantId, int month, int year);

    Optional<PayrollRun> findByIdAndTenantId(UUID id, UUID tenantId);
}
