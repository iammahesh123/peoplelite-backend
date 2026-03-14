package com.hrlite.repository;

import com.hrlite.entity.TenantEmailSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantEmailSettingsRepository extends JpaRepository<TenantEmailSettings, UUID> {

    Optional<TenantEmailSettings> findByTenantId(UUID tenantId);
}
