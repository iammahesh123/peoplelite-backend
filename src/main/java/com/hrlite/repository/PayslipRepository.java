package com.hrlite.repository;

import com.hrlite.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    List<Payslip> findByPayrollRunId(UUID payrollRunId);

    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(UUID employeeId);

    Optional<Payslip> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Payslip> findByTenantIdOrderByYearDescMonthDesc(UUID tenantId);
}
