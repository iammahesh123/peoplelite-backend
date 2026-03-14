package com.hrlite.service;

import com.hrlite.dtos.PayrollSettingsRequest;
import com.hrlite.dtos.PayrollSettingsResponse;
import com.hrlite.entity.PayrollSettings;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.PayrollSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollSettingsService {

    private final PayrollSettingsRepository repository;

    @Transactional(readOnly = true)
    public PayrollSettingsResponse getSettings() {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollSettings settings = repository.findByTenantId(tenantId)
                .orElse(PayrollSettings.builder().build());
        return toResponse(settings);
    }

    /**
     * Returns the entity directly for internal use (e.g., PayrollService).
     * If no settings exist for the tenant, returns a default instance with everything disabled.
     */
    @Transactional(readOnly = true)
    public PayrollSettings getSettingsEntity(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .orElse(PayrollSettings.builder().build());
    }

    @Transactional
    public PayrollSettingsResponse saveSettings(PayrollSettingsRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollSettings settings = repository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    PayrollSettings newSettings = PayrollSettings.builder().build();
                    // TenantAwareEntity sets tenantId via @PrePersist
                    return newSettings;
                });

        settings.setPfEnabled(request.isPfEnabled());
        if (request.getPfRate() != null) settings.setPfRate(request.getPfRate());
        if (request.getPfCap() != null) settings.setPfCap(request.getPfCap());

        settings.setEsiEnabled(request.isEsiEnabled());
        if (request.getEsiEmployeeRate() != null) settings.setEsiEmployeeRate(request.getEsiEmployeeRate());
        if (request.getEsiEmployerRate() != null) settings.setEsiEmployerRate(request.getEsiEmployerRate());
        if (request.getEsiWageCeiling() != null) settings.setEsiWageCeiling(request.getEsiWageCeiling());

        settings.setPtEnabled(request.isPtEnabled());
        if (request.getPtAmount() != null) settings.setPtAmount(request.getPtAmount());

        settings.setTdsEnabled(request.isTdsEnabled());

        settings.setPfEmployerEnabled(request.isPfEmployerEnabled());
        if (request.getPfEmployerRate() != null) settings.setPfEmployerRate(request.getPfEmployerRate());

        settings = repository.save(settings);
        log.info("Payroll settings updated for tenant={}", tenantId);
        return toResponse(settings);
    }

    private PayrollSettingsResponse toResponse(PayrollSettings s) {
        return PayrollSettingsResponse.builder()
                .id(s.getId())
                .pfEnabled(s.isPfEnabled())
                .pfRate(s.getPfRate())
                .pfCap(s.getPfCap())
                .esiEnabled(s.isEsiEnabled())
                .esiEmployeeRate(s.getEsiEmployeeRate())
                .esiEmployerRate(s.getEsiEmployerRate())
                .esiWageCeiling(s.getEsiWageCeiling())
                .ptEnabled(s.isPtEnabled())
                .ptAmount(s.getPtAmount())
                .tdsEnabled(s.isTdsEnabled())
                .pfEmployerEnabled(s.isPfEmployerEnabled())
                .pfEmployerRate(s.getPfEmployerRate())
                .build();
    }
}
