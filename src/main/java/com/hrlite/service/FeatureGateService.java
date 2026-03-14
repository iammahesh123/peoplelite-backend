package com.hrlite.service;

import com.hrlite.config.PlanFeatureConfig;
import com.hrlite.entity.Tenant;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.Feature;
import com.hrlite.exception.FeatureNotAvailableException;
import com.hrlite.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeatureGateService {

    private final TenantRepository tenantRepository;

    public boolean hasFeature(Feature feature) {
        String currentPlan = getCurrentPlan();
        Set<Feature> availableFeatures = PlanFeatureConfig.getFeaturesForPlan(currentPlan);
        return availableFeatures.contains(feature);
    }

    public void requireFeature(Feature feature) {
        String currentPlan = getCurrentPlan();
        Set<Feature> availableFeatures = PlanFeatureConfig.getFeaturesForPlan(currentPlan);

        if (!availableFeatures.contains(feature)) {
            // Determine which plan has this feature
            String requiredPlan = null;
            for (String plan : PlanFeatureConfig.getAllPlans()) {
                if (PlanFeatureConfig.getFeaturesForPlan(plan).contains(feature)) {
                    requiredPlan = plan;
                }
            }
            throw new FeatureNotAvailableException(feature, requiredPlan != null ? requiredPlan : "STARTER");
        }
    }

    public int getMaxEmployees() {
        String currentPlan = getCurrentPlan();
        return PlanFeatureConfig.getMaxEmployees(currentPlan);
    }

    public String getCurrentPlan() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context found");
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));
        return tenant.getPlan();
    }

    public Set<Feature> getAvailableFeatures() {
        String currentPlan = getCurrentPlan();
        return PlanFeatureConfig.getFeaturesForPlan(currentPlan);
    }
}
