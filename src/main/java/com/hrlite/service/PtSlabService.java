package com.hrlite.service;

import com.hrlite.entity.PtSlab;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.PtSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PtSlabService {

    private final PtSlabRepository repository;

    @Transactional(readOnly = true)
    public List<PtSlab> getSlabs() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdOrderByOrderIndexAsc(tenantId);
    }

    @Transactional
    public List<PtSlab> saveSlabs(List<PtSlab> slabs) {
        UUID tenantId = TenantContext.getCurrentTenant();

        validateSlabs(slabs);

        repository.deleteByTenantId(tenantId);

        for (PtSlab slab : slabs) {
            slab.setTenantId(tenantId);
        }

        return repository.saveAll(slabs);
    }

    /**
     * Calculate PT amount based on gross salary using slab rules.
     * If no slabs configured, returns BigDecimal.ZERO.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePtFromSlabs(UUID tenantId, BigDecimal grossSalary) {
        List<PtSlab> slabs = repository.findByTenantIdOrderByOrderIndexAsc(tenantId);
        if (slabs.isEmpty()) return BigDecimal.ZERO;

        for (PtSlab slab : slabs) {
            boolean aboveFrom = grossSalary.compareTo(slab.getSlabFrom()) >= 0;
            boolean belowTo = slab.getSlabTo() == null || grossSalary.compareTo(slab.getSlabTo()) <= 0;

            if (aboveFrom && belowTo) {
                return slab.getTaxAmount();
            }
        }

        return BigDecimal.ZERO;
    }

    private void validateSlabs(List<PtSlab> slabs) {
        if (slabs == null || slabs.isEmpty()) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "PT slabs cannot be empty");
        }

        slabs.sort(Comparator.comparing(PtSlab::getOrderIndex));

        BigDecimal previousTo = null;

        for (int i = 0; i < slabs.size(); i++) {
            PtSlab slab = slabs.get(i);

            if (slab.getSlabFrom() == null || slab.getTaxAmount() == null) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                        "Slab from, tax amount, and order index are required");
            }

            if (slab.getSlabFrom().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                        "Slab from cannot be negative");
            }

            if (slab.getSlabTo() != null && slab.getSlabTo().compareTo(slab.getSlabFrom()) < 0) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                        "Slab to must be greater than or equal to slab from");
            }

            if (slab.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                        "Tax amount cannot be negative");
            }

            if (previousTo != null && slab.getSlabFrom().compareTo(previousTo) <= 0) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                        "PT slabs must not overlap and must be in ascending order");
            }

            previousTo = slab.getSlabTo();
        }
    }
}