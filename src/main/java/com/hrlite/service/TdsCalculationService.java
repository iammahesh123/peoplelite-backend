package com.hrlite.service;

import com.hrlite.entity.TdsSlab;
import com.hrlite.repository.TdsSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Calculates monthly TDS based on configurable tax slabs (New/Old Regime).
 * If no tenant-specific slabs exist, uses India's FY2025-26 New Regime defaults.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TdsCalculationService {

    private final TdsSlabRepository tdsSlabRepository;

    /**
     * Calculates monthly TDS for an employee based on their annual taxable income.
     * @param tenantId the tenant
     * @param annualTaxableIncome gross annual salary minus exemptions
     * @param regime "NEW" or "OLD"
     * @return monthly TDS amount
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateMonthlyTds(UUID tenantId, BigDecimal annualTaxableIncome, String regime) {
        List<TdsSlab> slabs = tdsSlabRepository.findByTenantIdAndRegimeOrderByOrderIndexAsc(tenantId, regime);

        // If no tenant-specific slabs, use New Regime FY2025-26 defaults
        if (slabs.isEmpty()) {
            return calculateDefaultNewRegimeTds(annualTaxableIncome);
        }

        BigDecimal annualTax = calculateTaxFromSlabs(annualTaxableIncome, slabs);
        // Add 4% health & education cess
        BigDecimal cess = annualTax.multiply(new BigDecimal("0.04")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAnnualTax = annualTax.add(cess);

        return totalAnnualTax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    /**
     * Default New Tax Regime slabs (India FY2025-26):
     * 0 - 4,00,000: Nil (with rebate up to 12L under new regime)
     * 4,00,001 - 8,00,000: 5%
     * 8,00,001 - 12,00,000: 10%
     * 12,00,001 - 16,00,000: 15%
     * 16,00,001 - 20,00,000: 20%
     * 20,00,001 - 24,00,000: 25%
     * Above 24,00,000: 30%
     */
    private BigDecimal calculateDefaultNewRegimeTds(BigDecimal annualIncome) {
        // Standard deduction of ₹75,000 under new regime
        BigDecimal taxableIncome = annualIncome.subtract(new BigDecimal("75000"));
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        // Rebate under section 87A: No tax if income up to ₹12,00,000
        if (taxableIncome.compareTo(new BigDecimal("1200000")) <= 0) return BigDecimal.ZERO;

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = taxableIncome;

        // Slab 1: 0 - 4L = 0%
        BigDecimal slab1 = min(remaining, new BigDecimal("400000"));
        remaining = remaining.subtract(slab1);

        // Slab 2: 4L - 8L = 5%
        BigDecimal slab2 = min(remaining, new BigDecimal("400000"));
        tax = tax.add(slab2.multiply(new BigDecimal("0.05")));
        remaining = remaining.subtract(slab2);

        // Slab 3: 8L - 12L = 10%
        BigDecimal slab3 = min(remaining, new BigDecimal("400000"));
        tax = tax.add(slab3.multiply(new BigDecimal("0.10")));
        remaining = remaining.subtract(slab3);

        // Slab 4: 12L - 16L = 15%
        BigDecimal slab4 = min(remaining, new BigDecimal("400000"));
        tax = tax.add(slab4.multiply(new BigDecimal("0.15")));
        remaining = remaining.subtract(slab4);

        // Slab 5: 16L - 20L = 20%
        BigDecimal slab5 = min(remaining, new BigDecimal("400000"));
        tax = tax.add(slab5.multiply(new BigDecimal("0.20")));
        remaining = remaining.subtract(slab5);

        // Slab 6: 20L - 24L = 25%
        BigDecimal slab6 = min(remaining, new BigDecimal("400000"));
        tax = tax.add(slab6.multiply(new BigDecimal("0.25")));
        remaining = remaining.subtract(slab6);

        // Slab 7: Above 24L = 30%
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            tax = tax.add(remaining.multiply(new BigDecimal("0.30")));
        }

        // 4% cess
        BigDecimal cess = tax.multiply(new BigDecimal("0.04")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAnnualTax = tax.add(cess);

        return totalAnnualTax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTaxFromSlabs(BigDecimal income, List<TdsSlab> slabs) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = income;

        for (TdsSlab slab : slabs) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal slabWidth;
            if (slab.getSlabTo() == null) {
                slabWidth = remaining;
            } else {
                slabWidth = slab.getSlabTo().subtract(slab.getSlabFrom());
            }

            BigDecimal taxableInSlab = min(remaining, slabWidth);
            BigDecimal rate = slab.getRatePercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            tax = tax.add(taxableInSlab.multiply(rate));
            remaining = remaining.subtract(taxableInSlab);
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public List<TdsSlab> getSlabs(UUID tenantId) {
        return tdsSlabRepository.findByTenantIdOrderByOrderIndexAsc(tenantId);
    }

    @Transactional
    public List<TdsSlab> saveSlabs(UUID tenantId, List<TdsSlab> slabs) {
        tdsSlabRepository.deleteByTenantId(tenantId);

        for (TdsSlab slab : slabs) {
            slab.setTenantId(tenantId);
        }

        return tdsSlabRepository.saveAll(slabs);
    }
    private BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
