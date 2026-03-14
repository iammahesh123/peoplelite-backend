package com.hrlite.repository;

import com.hrlite.entity.PayrollSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollSettingsRepository extends JpaRepository<PayrollSettings, UUID> {
    Optional<PayrollSettings> findByTenantId(UUID tenantId);
}
