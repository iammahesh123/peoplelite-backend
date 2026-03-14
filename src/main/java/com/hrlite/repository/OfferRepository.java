package com.hrlite.repository;

import com.hrlite.entity.Offer;
import com.hrlite.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {

    List<Offer> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Offer> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Offer> findByCandidateIdAndTenantId(UUID candidateId, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, OfferStatus status);
}
