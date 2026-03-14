package com.hrlite.repository;

import com.hrlite.entity.TenantPaymentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantPaymentSettingsRepository extends JpaRepository<TenantPaymentSettings, UUID> {

    Optional<TenantPaymentSettings> findByTenantId(UUID tenantId);
}
