package com.hrlite.service;

import com.hrlite.entity.SalaryStructureTemplate;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.SalaryStructureTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryStructureTemplateService {

    private final SalaryStructureTemplateRepository repository;

    @Transactional
    public SalaryStructureTemplate create(String name, BigDecimal basicPercent,
                                           BigDecimal hraPercent, BigDecimal specialAllowancePercent,
                                           boolean isDefault) {
        SalaryStructureTemplate template = SalaryStructureTemplate.builder()
                .name(name)
                .basicPercent(basicPercent)
                .hraPercent(hraPercent)
                .specialAllowancePercent(specialAllowancePercent)
                .isDefault(isDefault)
                .build();

        // If setting as default, unset others
        if (isDefault) {
            UUID tenantId = TenantContext.getCurrentTenant();
            repository.findByTenantIdAndIsDefaultTrue(tenantId).ifPresent(existing -> {
                existing.setDefault(false);
                repository.save(existing);
            });
        }

        return repository.save(template);
    }

    @Transactional(readOnly = true)
    public List<SalaryStructureTemplate> getAll() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndActiveTrue(tenantId);
    }

    public SalaryStructureTemplate getDefault() {
        UUID tenantId = TenantContext.getCurrentTenant();

        return repository.findByTenantIdAndIsDefaultTrue(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Default Salary Template", tenantId));
    }

    /**
     * Splits a CTC amount into Basic, HRA, Special Allowance using the template percentages.
     */
    public BigDecimal[] splitCTC(SalaryStructureTemplate template, BigDecimal monthlyCTC) {
        BigDecimal basic = monthlyCTC.multiply(template.getBasicPercent())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal hra = monthlyCTC.multiply(template.getHraPercent())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal special = monthlyCTC.subtract(basic).subtract(hra);
        return new BigDecimal[]{basic, hra, special};
    }

    @Transactional
    public void delete(UUID templateId) {
        SalaryStructureTemplate t = repository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructureTemplate", templateId));
        t.setActive(false);
        repository.save(t);
    }
}
