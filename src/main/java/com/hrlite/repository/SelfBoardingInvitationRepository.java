package com.hrlite.repository;

import com.hrlite.entity.SelfBoardingInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SelfBoardingInvitationRepository extends JpaRepository<SelfBoardingInvitation, UUID> {
    Optional<SelfBoardingInvitation> findByToken(String token);
    List<SelfBoardingInvitation> findByTenantIdOrderBySentAtDesc(UUID tenantId);
    List<SelfBoardingInvitation> findByEmployeeId(UUID employeeId);
    Optional<SelfBoardingInvitation> findByEmployeeIdAndStatus(UUID employeeId, String status);
}
