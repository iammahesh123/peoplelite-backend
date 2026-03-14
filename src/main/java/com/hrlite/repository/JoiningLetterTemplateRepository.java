package com.hrlite.repository;

import com.hrlite.entity.JoiningLetterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JoiningLetterTemplateRepository extends JpaRepository<JoiningLetterTemplate, UUID> {

    List<JoiningLetterTemplate> findByTenantIdAndActiveTrueOrderByCreatedAtDesc(UUID tenantId);

    List<JoiningLetterTemplate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
