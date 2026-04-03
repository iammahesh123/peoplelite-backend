package com.hrlite.service;

import com.hrlite.entity.Reimbursement;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.ReimbursementRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReimbursementService {

    private final ReimbursementRepository repository;

    @Transactional
    public Reimbursement create(UUID employeeId, String category, BigDecimal amount,
                                String description, int month, int year,
                                UserPrincipal principal) {

        validateCreateRequest(amount, month, year);

        Reimbursement r = Reimbursement.builder()
                .employeeId(employeeId)
                .category(category)
                .amount(amount)
                .description(description)
                .receiptDate(LocalDate.now())
                .month(month)
                .year(year)
                .status("PENDING")
                // .createdBy(principal.getUserId()) // use if field exists
                .build();

        return repository.save(r);
    }

    @Transactional
    public Reimbursement approve(UUID reimbursementId, UserPrincipal principal) {
        Reimbursement r = repository.findById(reimbursementId)
                .orElseThrow(() -> new ResourceNotFoundException("Reimbursement", reimbursementId));

        if ("APPROVED".equals(r.getStatus())) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Reimbursement is already approved");
        }

        if ("REJECTED".equals(r.getStatus())) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Rejected reimbursement cannot be approved");
        }

        r.setStatus("APPROVED");
        r.setApprovedBy(principal.getUserId());
        return repository.save(r);
    }

    @Transactional
    public Reimbursement reject(UUID reimbursementId) {
        Reimbursement r = repository.findById(reimbursementId)
                .orElseThrow(() -> new ResourceNotFoundException("Reimbursement", reimbursementId));

        if ("REJECTED".equals(r.getStatus())) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Reimbursement is already rejected");
        }

        if ("APPROVED".equals(r.getStatus())) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Approved reimbursement cannot be rejected");
        }

        r.setStatus("REJECTED");
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Reimbursement> getApprovedForPayroll(UUID employeeId, int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndEmployeeIdAndMonthAndYearAndStatus(
                tenantId, employeeId, month, year, "APPROVED");
    }

    @Transactional(readOnly = true)
    public List<Reimbursement> getAll() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Reimbursement> getByMonth(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndMonthAndYear(tenantId, month, year);
    }

    private void validateCreateRequest(BigDecimal amount, int month, int year) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Reimbursement amount must be greater than zero");
        }

        if (month < 1 || month > 12) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Month must be between 1 and 12");
        }

        int currentYear = YearMonth.now().getYear();
        if (year < 2000 || year > currentYear + 1) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Invalid reimbursement year");
        }
    }
}