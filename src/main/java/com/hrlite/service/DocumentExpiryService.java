package com.hrlite.service;

import com.hrlite.entity.DocumentExpiryRecord;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.DocumentExpiryRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentExpiryService {

    private final DocumentExpiryRepository documentExpiryRepository;
    private final AuditService auditService;

    public DocumentExpiryRecord create(UUID employeeId, String employeeName, String documentType,
                                        String documentName, LocalDate issueDate, LocalDate expiryDate, String notes) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DocumentExpiryRecord record = DocumentExpiryRecord.builder()
                .tenantId(principal.getTenantId())
                .employeeId(employeeId)
                .employeeName(employeeName)
                .documentType(documentType)
                .documentName(documentName)
                .issueDate(issueDate)
                .expiryDate(expiryDate)
                .notes(notes)
                .build();

        DocumentExpiryRecord saved = documentExpiryRepository.save(record);
        auditService.log("CREATE", "DocumentExpiry", saved.getId().toString(),
                "Added document expiry tracking: " + documentName + " for " + employeeName);
        return saved;
    }

    public DocumentExpiryRecord update(UUID id, String documentType, String documentName,
                                        LocalDate issueDate, LocalDate expiryDate, String status, String notes) {
        DocumentExpiryRecord record = documentExpiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Document record not found"));

        if (documentType != null) record.setDocumentType(documentType);
        if (documentName != null) record.setDocumentName(documentName);
        if (issueDate != null) record.setIssueDate(issueDate);
        if (expiryDate != null) record.setExpiryDate(expiryDate);
        if (status != null) record.setStatus(status);
        if (notes != null) record.setNotes(notes);
        record.setUpdatedAt(LocalDateTime.now());

        return documentExpiryRepository.save(record);
    }

    public void delete(UUID id) {
        documentExpiryRepository.deleteById(id);
    }

    public Page<DocumentExpiryRecord> getAll(UUID tenantId, Pageable pageable) {
        return documentExpiryRepository.findByTenantIdOrderByExpiryDateAsc(tenantId, pageable);
    }

    public List<DocumentExpiryRecord> getByEmployee(UUID tenantId, UUID employeeId) {
        return documentExpiryRepository.findByTenantIdAndEmployeeId(tenantId, employeeId);
    }

    public List<DocumentExpiryRecord> getExpiringSoon(UUID tenantId, int days) {
        return documentExpiryRepository.findExpiringSoon(tenantId, LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public long countExpired(UUID tenantId) {
        return documentExpiryRepository.countExpired(tenantId, LocalDate.now());
    }

    public long countExpiringSoon(UUID tenantId, int days) {
        return documentExpiryRepository.countExpiringSoon(tenantId, LocalDate.now(), LocalDate.now().plusDays(days));
    }
}
