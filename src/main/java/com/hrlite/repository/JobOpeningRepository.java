package com.hrlite.repository;

import com.hrlite.entity.JobOpening;
import com.hrlite.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID> {

    List<JobOpening> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<JobOpening> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, JobStatus status);

    Optional<JobOpening> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, JobStatus status);

    long countByTenantId(UUID tenantId);
}
