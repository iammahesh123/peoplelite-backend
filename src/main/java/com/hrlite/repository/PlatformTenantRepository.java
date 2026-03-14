package com.hrlite.repository;

import com.hrlite.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlatformTenantRepository extends JpaRepository<Tenant, UUID> {

    @Query("SELECT t FROM Tenant t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.active = :status)")
    Page<Tenant> searchTenants(@Param("search") String search,
                               @Param("status") Boolean status,
                               Pageable pageable);

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.active = true")
    long countActiveTenants();

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.plan = 'FREE' AND t.active = true")
    long countTrialTenants();

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.active = false")
    long countSuspendedTenants();

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.createdAt >= :since")
    long countNewTenantsSince(@Param("since") LocalDateTime since);

    @Query("SELECT t FROM Tenant t ORDER BY t.createdAt DESC")
    List<Tenant> findRecentTenants(Pageable pageable);
}
