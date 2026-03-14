package com.hrlite.repository;

import com.hrlite.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, UUID> {

    List<OnboardingTask> findByEmployeeIdOrderByOrderIndexAsc(UUID employeeId);

    List<OnboardingTask> findByTenantIdAndCompletedFalse(UUID tenantId);

    long countByEmployeeId(UUID employeeId);

    long countByEmployeeIdAndCompletedTrue(UUID employeeId);
}
