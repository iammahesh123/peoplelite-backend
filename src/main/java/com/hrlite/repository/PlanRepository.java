package com.hrlite.repository;

import com.hrlite.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<Plan> findByCode(String code);

    Optional<Plan> findByIdAndActiveTrue(UUID id);
}
