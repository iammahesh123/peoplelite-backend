package com.hrlite.repository;

import com.hrlite.entity.CandidateActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateActivityRepository extends JpaRepository<CandidateActivity, UUID> {

    List<CandidateActivity> findByTenantIdAndCandidateIdOrderByCreatedAtDesc(UUID tenantId, UUID candidateId);
}
