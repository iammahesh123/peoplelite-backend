package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.DocumentExpiryRecord;
import com.hrlite.enums.Feature;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.DocumentExpiryService;
import com.hrlite.service.FeatureGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/document-expiry")
@RequiredArgsConstructor
public class DocumentExpiryController {

    private final DocumentExpiryService documentExpiryService;
    private final FeatureGateService featureGateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<DocumentExpiryRecord>> create(@RequestBody Map<String, String> body) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        DocumentExpiryRecord record = documentExpiryService.create(
                UUID.fromString(body.get("employeeId")),
                body.get("employeeName"),
                body.get("documentType"),
                body.get("documentName"),
                body.get("issueDate") != null ? LocalDate.parse(body.get("issueDate")) : null,
                LocalDate.parse(body.get("expiryDate")),
                body.get("notes")
        );
        return ResponseEntity.ok(ApiResponse.success("Document expiry record created", record));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<DocumentExpiryRecord>> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        DocumentExpiryRecord record = documentExpiryService.update(id,
                body.get("documentType"),
                body.get("documentName"),
                body.get("issueDate") != null ? LocalDate.parse(body.get("issueDate")) : null,
                body.get("expiryDate") != null ? LocalDate.parse(body.get("expiryDate")) : null,
                body.get("status"),
                body.get("notes")
        );
        return ResponseEntity.ok(ApiResponse.success("Document expiry record updated", record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        documentExpiryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document expiry record deleted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentExpiryRecord>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<DocumentExpiryRecord> records = documentExpiryService.getAll(principal.getTenantId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<DocumentExpiryRecord>>> getByEmployee(@PathVariable UUID employeeId) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DocumentExpiryRecord> records = documentExpiryService.getByEmployee(principal.getTenantId(), employeeId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<DocumentExpiryRecord>>> getExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DocumentExpiryRecord> records = documentExpiryService.getExpiringSoon(principal.getTenantId(), days);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        featureGateService.requireFeature(Feature.DOCUMENT_EXPIRY);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Long> stats = Map.of(
                "expired", documentExpiryService.countExpired(principal.getTenantId()),
                "expiringSoon30", documentExpiryService.countExpiringSoon(principal.getTenantId(), 30),
                "expiringSoon7", documentExpiryService.countExpiringSoon(principal.getTenantId(), 7)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
