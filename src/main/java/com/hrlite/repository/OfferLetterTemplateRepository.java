package com.hrlite.repository;

import com.hrlite.entity.OfferLetterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferLetterTemplateRepository extends JpaRepository<OfferLetterTemplate, UUID> {

    List<OfferLetterTemplate> findByTenantIdAndActiveTrueOrderByCreatedAtDesc(UUID tenantId);

    List<OfferLetterTemplate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
