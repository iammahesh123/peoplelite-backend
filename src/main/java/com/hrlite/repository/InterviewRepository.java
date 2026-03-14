package com.hrlite.repository;

import com.hrlite.entity.Interview;
import com.hrlite.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByTenantIdOrderByInterviewDateDesc(UUID tenantId);

    List<Interview> findByTenantIdAndCandidateIdOrderByInterviewDateDesc(UUID tenantId, UUID candidateId);

    Optional<Interview> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, InterviewStatus status);

    List<Interview> findByTenantIdAndStatusOrderByInterviewDateAsc(UUID tenantId, InterviewStatus status);
}
