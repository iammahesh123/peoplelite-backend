package com.hrlite.service;

import com.hrlite.dtos.ExitChecklistItemResponse;
import com.hrlite.dtos.ExitChecklistUpdateRequest;
import com.hrlite.dtos.SeparationRequest;
import com.hrlite.dtos.SeparationResponse;
import com.hrlite.entity.*;
import com.hrlite.enums.ExitChecklistCategory;
import com.hrlite.enums.SeparationStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.ExitChecklistItemRepository;
import com.hrlite.repository.SeparationRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeparationService {

    private final SeparationRepository separationRepository;
    private final ExitChecklistItemRepository checklistItemRepository;
    private final EmployeeRepository employeeRepository;

    private static final Map<ExitChecklistCategory, List<String>> DEFAULT_CHECKLIST = Map.of(
            ExitChecklistCategory.IT_ASSETS, List.of("Return Laptop", "Return ID Card", "Return Access Card"),
            ExitChecklistCategory.HR_DOCUMENTS, List.of("Generate Experience Letter", "Collect Resignation Letter", "Update Employee Status"),
            ExitChecklistCategory.FINANCE, List.of("Process Final Settlement", "Clear Pending Reimbursements"),
            ExitChecklistCategory.ACCESS, List.of("Revoke Email Access", "Revoke System Access")
    );

    public SeparationResponse initiateSeparation(SeparationRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Separation separation = Separation.builder()
                .employeeId(request.getEmployeeId())
                .separationType(request.getSeparationType())
                .resignationDate(request.getResignationDate())
                .lastWorkingDate(request.getLastWorkingDate())
                .noticePeriodDays(request.getNoticePeriodDays())
                .reason(request.getReason())
                .status(SeparationStatus.INITIATED)
                .build();

        separation = separationRepository.save(separation);

        // Create default checklist items
        createDefaultChecklist(separation.getId(), tenantId);

        return mapToResponse(separation);
    }

    public List<SeparationResponse> getAllSeparations() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return separationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SeparationResponse getSeparationById(UUID separationId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Separation separation = separationRepository.findByIdAndTenantId(separationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Separation not found with ID: " + separationId));

        return mapToResponse(separation);
    }

    public void updateChecklistItem(UUID separationId, UUID itemId, ExitChecklistUpdateRequest request) {
        ExitChecklistItem item = checklistItemRepository.findByIdAndTenantId(itemId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found with ID: " + itemId));

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        item.setCompleted(request.isCompleted());
        item.setNotes(request.getNotes());

        if (request.isCompleted()) {
            item.setCompletedBy(principal.getUserId());
            item.setCompletedAt(LocalDateTime.now());
        }

        checklistItemRepository.save(item);
    }

    public void completeSeparation(UUID separationId) {
        Separation separation = separationRepository.findById(separationId)
                .orElseThrow(() -> new IllegalArgumentException("Separation not found with ID: " + separationId));

        separation.setStatus(SeparationStatus.COMPLETED);
        separationRepository.save(separation);
    }

    private void createDefaultChecklist(UUID separationId, UUID tenantId) {
        List<ExitChecklistItem> items = new ArrayList<>();

        DEFAULT_CHECKLIST.forEach((category, itemNames) -> {
            itemNames.forEach(itemName -> {
                ExitChecklistItem item = ExitChecklistItem.builder()
                        .separationId(separationId)
                        .itemName(itemName)
                        .category(category)
                        .completed(false)
                        .build();
                items.add(item);
            });
        });

        checklistItemRepository.saveAll(items);
    }

    private SeparationResponse mapToResponse(Separation separation) {
        List<ExitChecklistItem> items = checklistItemRepository.findBySeparationIdOrderByCategory(separation.getId());
        List<ExitChecklistItemResponse> checklistResponses = items.stream()
                .map(this::mapChecklistItemToResponse)
                .toList();

        Employee employee = employeeRepository.findById(separation.getEmployeeId())
                .orElse(null);

        return SeparationResponse.builder()
                .id(separation.getId())
                .employeeId(separation.getEmployeeId())
                .employeeName(employee != null ? employee.getFullName() : "Unknown")
                .separationType(separation.getSeparationType())
                .resignationDate(separation.getResignationDate())
                .lastWorkingDate(separation.getLastWorkingDate())
                .noticePeriodDays(separation.getNoticePeriodDays())
                .reason(separation.getReason())
                .exitInterviewNotes(separation.getExitInterviewNotes())
                .status(separation.getStatus())
                .finalSettlementAmount(separation.getFinalSettlementAmount())
                .remainingLeaves(separation.getRemainingLeaves())
                .checklistItems(checklistResponses)
                .createdAt(separation.getCreatedAt())
                .updatedAt(separation.getUpdatedAt())
                .build();
    }

    private ExitChecklistItemResponse mapChecklistItemToResponse(ExitChecklistItem item) {
        return ExitChecklistItemResponse.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .category(item.getCategory())
                .completed(item.isCompleted())
                .completedBy(item.getCompletedBy())
                .completedAt(item.getCompletedAt())
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
