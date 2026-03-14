package com.hrlite.repository;

import com.hrlite.entity.Separation;
import com.hrlite.enums.SeparationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeparationRepository extends JpaRepository<Separation, UUID> {

    List<Separation> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Separation> findByTenantIdAndStatus(UUID tenantId, SeparationStatus status);

    Optional<Separation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Separation> findByEmployeeIdAndTenantId(UUID employeeId, UUID tenantId);

    List<Separation> findByEmployeeId(UUID employeeId);
}
