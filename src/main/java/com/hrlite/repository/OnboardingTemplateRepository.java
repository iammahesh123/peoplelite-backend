package com.hrlite.repository;

import com.hrlite.entity.OnboardingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OnboardingTemplateRepository extends JpaRepository<OnboardingTemplate, UUID> {

    List<OnboardingTemplate> findByTenantIdAndActiveTrueOrderByOrderIndexAsc(UUID tenantId);

    List<OnboardingTemplate> findByTenantIdOrderByOrderIndexAsc(UUID tenantId);
}
