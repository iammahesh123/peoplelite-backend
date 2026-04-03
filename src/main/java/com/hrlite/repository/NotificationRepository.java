package com.hrlite.repository;

import com.hrlite.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, String type, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndSearch(@Param("userId") UUID userId,
                                             @Param("search") String search,
                                             Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndTypeAndSearch(@Param("userId") UUID userId,
                                                    @Param("type") String type,
                                                    @Param("search") String search,
                                                    Pageable pageable);

    long countByUserIdAndReadFalse(UUID userId);

    void deleteByUserId(UUID userId);
}
