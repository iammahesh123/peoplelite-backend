package com.hrlite.repository;

import com.hrlite.entity.TenantNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantNotificationSettingsRepository extends JpaRepository<TenantNotificationSettings, UUID> {

    Optional<TenantNotificationSettings> findByTenantId(UUID tenantId);
}
