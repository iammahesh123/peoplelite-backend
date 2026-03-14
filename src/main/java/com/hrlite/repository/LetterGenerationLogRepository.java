package com.hrlite.repository;

import com.hrlite.entity.LetterGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LetterGenerationLogRepository extends JpaRepository<LetterGenerationLog, UUID> {
    List<LetterGenerationLog> findByTenantIdOrderByGeneratedAtDesc(UUID tenantId);
    List<LetterGenerationLog> findByEmployeeIdOrderByGeneratedAtDesc(UUID employeeId);
    long countByTenantId(UUID tenantId);
}
