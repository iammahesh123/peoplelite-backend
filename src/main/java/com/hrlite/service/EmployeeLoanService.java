package com.hrlite.service;

import com.hrlite.entity.EmployeeLoan;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.EmployeeLoanRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeLoanService {

    private final EmployeeLoanRepository repository;

    @Transactional
    public EmployeeLoan createLoan(UUID employeeId, String loanType, BigDecimal principalAmount,
                                   BigDecimal emiAmount, int startMonth, int startYear,
                                   String remarks, UserPrincipal principal) {

        EmployeeLoan loan = EmployeeLoan.builder()
                .employeeId(employeeId)
                .loanType(loanType)
                .principalAmount(principalAmount)
                .emiAmount(emiAmount)
                .startMonth(startMonth)
                .startYear(startYear)
                .disbursalDate(LocalDate.now())
                .remarks(remarks)
                .createdBy(principal.getUserId())
                .totalPaid(BigDecimal.ZERO)
                .remainingAmount(principalAmount)
                .active(true)
                .build();

        return repository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<EmployeeLoan> getActiveLoans(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndEmployeeIdAndActiveTrue(tenantId, employeeId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeLoan> getAllLoans() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Process EMI deduction during payroll. Returns the EMI amount deducted.
     * Updates totalPaid and remainingAmount. Closes loan if fully repaid.
     */
    @Transactional
    public BigDecimal processEmiDeduction(EmployeeLoan loan) {
        BigDecimal emi = loan.getEmiAmount();

        if (loan.getRemainingAmount() == null) {
            throw new IllegalStateException("Loan remaining amount is null for loan id: " + loan.getId());
        }

        if (loan.getTotalPaid() == null) {
            loan.setTotalPaid(BigDecimal.ZERO);
        }

        // Don't deduct more than remaining
        if (emi.compareTo(loan.getRemainingAmount()) > 0) {
            emi = loan.getRemainingAmount();
        }

        loan.setTotalPaid(loan.getTotalPaid().add(emi));
        loan.setRemainingAmount(loan.getRemainingAmount().subtract(emi));

        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setActive(false);
            loan.setRemainingAmount(BigDecimal.ZERO);
        }

        repository.save(loan);
        return emi;
    }

    @Transactional
    public EmployeeLoan closeLoan(UUID loanId) {
        EmployeeLoan loan = repository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        loan.setActive(false);
        return repository.save(loan);
    }
}