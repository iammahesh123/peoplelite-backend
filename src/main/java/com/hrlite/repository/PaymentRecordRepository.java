package com.hrlite.repository;

import com.hrlite.entity.PaymentRecord;
import com.hrlite.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, UUID> {

    Optional<PaymentRecord> findByRazorpayOrderId(String razorpayOrderId);

    Optional<PaymentRecord> findByRazorpayPaymentId(String razorpayPaymentId);

    List<PaymentRecord> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);

    List<PaymentRecord> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Page<PaymentRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(PaymentStatus status);
}
