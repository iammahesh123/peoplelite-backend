package com.hrlite.service;

import com.hrlite.dtos.SalaryRevisionRequest;
import com.hrlite.dtos.SalaryRevisionResponse;
import com.hrlite.entity.SalaryRevision;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.SalaryRevisionRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRevisionService {

    private final SalaryRevisionRepository salaryRevisionRepository;

    public SalaryRevisionResponse createRevision(SalaryRevisionRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        SalaryRevision revision = SalaryRevision.builder()
                .employeeId(request.getEmployeeId())
                .previousCTC(request.getPreviousCTC())
                .newCTC(request.getNewCTC())
                .previousBasic(request.getPreviousBasic())
                .newBasic(request.getNewBasic())
                .effectiveDate(request.getEffectiveDate())
                .reason(request.getReason())
                .remarks(request.getRemarks())
                .revisedBy(principal.getUserId())
                .build();

        revision = salaryRevisionRepository.save(revision);
        return mapToResponse(revision);
    }

    public List<SalaryRevisionResponse> getRevisionsByEmployee(UUID employeeId) {
        return salaryRevisionRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<SalaryRevisionResponse> getAllRevisions() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return salaryRevisionRepository.findByTenantIdOrderByEffectiveDateDesc(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SalaryRevisionResponse mapToResponse(SalaryRevision revision) {
        return SalaryRevisionResponse.builder()
                .id(revision.getId())
                .employeeId(revision.getEmployeeId())
                .previousCTC(revision.getPreviousCTC())
                .newCTC(revision.getNewCTC())
                .previousBasic(revision.getPreviousBasic())
                .newBasic(revision.getNewBasic())
                .effectiveDate(revision.getEffectiveDate())
                .reason(revision.getReason())
                .remarks(revision.getRemarks())
                .revisedBy(revision.getRevisedBy())
                .createdAt(revision.getCreatedAt())
                .updatedAt(revision.getUpdatedAt())
                .build();
    }
}
