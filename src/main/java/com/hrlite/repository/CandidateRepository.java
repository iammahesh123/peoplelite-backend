package com.hrlite.repository;

import com.hrlite.entity.Candidate;
import com.hrlite.enums.CandidateStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    List<Candidate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Candidate> findByTenantIdAndJobOpeningIdOrderByCreatedAtDesc(UUID tenantId, UUID jobOpeningId);

    List<Candidate> findByTenantIdAndStageOrderByCreatedAtDesc(UUID tenantId, CandidateStage stage);

    Optional<Candidate> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStage(UUID tenantId, CandidateStage stage);

    long countByTenantIdAndJobOpeningId(UUID tenantId, UUID jobOpeningId);

    List<Candidate> findByTenantIdAndJobOpeningIdAndStage(UUID tenantId, UUID jobOpeningId, CandidateStage stage);

    boolean existsByEmailAndJobOpeningIdAndTenantId(String email, UUID jobOpeningId, UUID tenantId);
}
