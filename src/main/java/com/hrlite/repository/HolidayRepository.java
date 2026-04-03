package com.hrlite.repository;

import com.hrlite.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByTenantIdAndYearOrderByDateAsc(UUID tenantId, int year);
    List<Holiday> findByTenantIdAndDateBetween(UUID tenantId, LocalDate startDate, LocalDate endDate);
    long countByTenantIdAndDateBetweenAndOptionalFalse(UUID tenantId, LocalDate startDate, LocalDate endDate);
}
