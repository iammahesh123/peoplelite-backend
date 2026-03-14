package com.hrlite.repository;

import com.hrlite.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {

    List<Poll> findByTenantIdAndActiveTrue(UUID tenantId);

    List<Poll> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Poll> findByTenantIdAndActiveTrueOrderByCreatedAtDesc(UUID tenantId);
}
