package com.hrlite.repository;

import com.hrlite.entity.OfferLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferLetterRepository extends JpaRepository<OfferLetter, UUID> {

    List<OfferLetter> findByTenantIdOrderByGeneratedAtDesc(UUID tenantId);

    List<OfferLetter> findByEmployeeIdAndTenantId(UUID employeeId, UUID tenantId);

    Optional<OfferLetter> findByIdAndTenantId(UUID id, UUID tenantId);
}
