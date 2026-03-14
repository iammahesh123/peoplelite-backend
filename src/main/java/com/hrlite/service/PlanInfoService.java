package com.hrlite.service;

import com.hrlite.dtos.PlanInfoResponse;
import com.hrlite.entity.Tenant;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.Feature;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanInfoService {

    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final FeatureGateService featureGateService;

    public PlanInfoResponse getPlanInfo() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context found");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        Set<Feature> availableFeatures = featureGateService.getAvailableFeatures();
        List<String> featureNames = availableFeatures.stream()
                .map(f -> f.name().toLowerCase().replace("_", " "))
                .sorted()
                .collect(Collectors.toList());

        long currentEmployeeCount = employeeRepository.countByTenantId(tenantId);
        int maxEmployees = featureGateService.getMaxEmployees();

        return PlanInfoResponse.builder()
                .planName(tenant.getPlan())
                .maxEmployees(maxEmployees)
                .currentEmployeeCount(currentEmployeeCount)
                .availableFeatures(featureNames)
                .build();
    }
}
