package com.hrlite.repository;

import com.hrlite.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {

    List<EmployeeDocument> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    Optional<EmployeeDocument> findByIdAndTenantId(UUID id, UUID tenantId);
}
