package com.hrlite.repository;

import com.hrlite.entity.DocumentExpiryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentExpiryRepository extends JpaRepository<DocumentExpiryRecord, UUID> {
    Page<DocumentExpiryRecord> findByTenantIdOrderByExpiryDateAsc(UUID tenantId, Pageable pageable);

    List<DocumentExpiryRecord> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    @Query("SELECT d FROM DocumentExpiryRecord d WHERE d.tenantId = :tenantId AND d.expiryDate BETWEEN :start AND :end AND d.status = 'ACTIVE'")
    List<DocumentExpiryRecord> findExpiringSoon(@Param("tenantId") UUID tenantId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT d FROM DocumentExpiryRecord d WHERE d.expiryDate BETWEEN :start AND :end AND d.status = 'ACTIVE' AND " +
           "((d.reminderSent30 = false AND d.expiryDate BETWEEN :day23 AND :day37) OR " +
           "(d.reminderSent15 = false AND d.expiryDate BETWEEN :day8 AND :day22) OR " +
           "(d.reminderSent7 = false AND d.expiryDate BETWEEN :day1 AND :day7))")
    List<DocumentExpiryRecord> findDocumentsNeedingReminders(@Param("start") LocalDate start, @Param("end") LocalDate end,
                                                              @Param("day1") LocalDate day1, @Param("day7") LocalDate day7,
                                                              @Param("day8") LocalDate day8, @Param("day22") LocalDate day22,
                                                              @Param("day23") LocalDate day23, @Param("day37") LocalDate day37);

    @Query("SELECT COUNT(d) FROM DocumentExpiryRecord d WHERE d.tenantId = :tenantId AND d.expiryDate < :today AND d.status = 'ACTIVE'")
    long countExpired(@Param("tenantId") UUID tenantId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(d) FROM DocumentExpiryRecord d WHERE d.tenantId = :tenantId AND d.expiryDate BETWEEN :today AND :endDate AND d.status = 'ACTIVE'")
    long countExpiringSoon(@Param("tenantId") UUID tenantId, @Param("today") LocalDate today, @Param("endDate") LocalDate endDate);
}
