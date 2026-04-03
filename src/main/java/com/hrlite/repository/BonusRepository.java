package com.hrlite.repository;

import com.hrlite.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, UUID> {

    List<Bonus> findByTenantIdAndMonthAndYear(UUID tenantId, int month, int year);

    @Query("SELECT b FROM Bonus b WHERE b.tenantId = :tenantId AND b.month = :month AND b.year = :year " +
            "AND (b.employeeId = :employeeId OR b.applyToAll = true)")
    List<Bonus> findBonusesForEmployee(UUID tenantId, UUID employeeId, int month, int year);

    List<Bonus> findByTenantIdOrderByYearDescMonthDesc(UUID tenantId);
}
