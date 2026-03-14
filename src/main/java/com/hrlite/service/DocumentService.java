package com.hrlite.service;

import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.EmployeeDocument;
import com.hrlite.repository.EmployeeDocumentRepository;
import com.hrlite.dtos.DocumentResponse;
import com.hrlite.utils.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final S3StorageService storageService;

    @Transactional
    public DocumentResponse uploadDocument(UUID employeeId, String documentType,
                                           MultipartFile file, UserPrincipal uploader) {
        UUID tenantId = TenantContext.getCurrentTenant();

        try {
            String storageKey = storageService.upload(
                    tenantId.toString(),
                    "employees/" + employeeId,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            EmployeeDocument doc = EmployeeDocument.builder()
                    .employeeId(employeeId)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .storageKey(storageKey)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedBy(uploader.getUserId())
                    .build();

            return toResponse(documentRepository.save(doc));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getEmployeeDocuments(UUID employeeId) {
        return documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocument(UUID documentId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        EmployeeDocument doc = documentRepository.findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return storageService.download(doc.getStorageKey());
    }

    @Transactional(readOnly = true)
    public EmployeeDocument getDocumentEntity(UUID documentId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return documentRepository.findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    private DocumentResponse toResponse(EmployeeDocument doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .employeeId(doc.getEmployeeId())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .contentType(doc.getContentType())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
