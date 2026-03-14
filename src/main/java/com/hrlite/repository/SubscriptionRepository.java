package com.hrlite.repository;

import com.hrlite.entity.Subscription;
import com.hrlite.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findTopByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Subscription> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    long countByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.trialEndsAt < :now")
    List<Subscription> findExpiredTrials(
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.trialEndsAt BETWEEN :start AND :end")
    List<Subscription> findTrialsExpiringBetween(
            @Param("status") SubscriptionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);
}
