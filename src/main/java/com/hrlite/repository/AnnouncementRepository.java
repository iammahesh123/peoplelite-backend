package com.hrlite.repository;

import com.hrlite.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    Page<Announcement> findByTenantIdOrderByPinnedDescPublishedAtDesc(UUID tenantId, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE a.tenantId = :tenantId AND (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.pinned DESC, a.publishedAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);
}
