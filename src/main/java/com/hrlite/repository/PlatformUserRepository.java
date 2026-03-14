package com.hrlite.repository;

import com.hrlite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformUserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT COUNT(u) FROM User u JOIN Tenant t ON u.tenantId = t.id WHERE t.active = true AND u.active = true")
    long countActiveUsers();

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role = 'FOUNDER'")
    Optional<User> findFounderByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role = 'FOUNDER' AND u.active = true")
    Optional<User> findActiveFounderByTenantId(@Param("tenantId") UUID tenantId);
}
