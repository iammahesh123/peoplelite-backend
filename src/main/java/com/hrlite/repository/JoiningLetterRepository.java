package com.hrlite.repository;

import com.hrlite.entity.JoiningLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JoiningLetterRepository extends JpaRepository<JoiningLetter, UUID> {

    List<JoiningLetter> findByTenantIdOrderByGeneratedAtDesc(UUID tenantId);

    List<JoiningLetter> findByEmployeeIdAndTenantId(UUID employeeId, UUID tenantId);

    Optional<JoiningLetter> findByIdAndTenantId(UUID id, UUID tenantId);
}
