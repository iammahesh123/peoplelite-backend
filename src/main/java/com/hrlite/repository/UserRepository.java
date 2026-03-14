package com.hrlite.repository;

import com.hrlite.enums.Role;
import com.hrlite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByEmployeeId(UUID employeeId);

    List<User> findByTenantIdAndRole(UUID tenantId, Role role);

    List<User> findByTenantIdAndActiveTrue(UUID tenantId);
}
