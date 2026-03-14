package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.SalarySlipTemplateRequest;
import com.hrlite.dtos.SalarySlipTemplateResponse;
import com.hrlite.service.SalarySlipTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salary-slip-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class SalarySlipTemplateController {

    private final SalarySlipTemplateService salarySlipTemplateService;

    /**
     * GET /api/v1/salary-slip-templates
     * Get all active salary slip templates for the current tenant
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SalarySlipTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(salarySlipTemplateService.getTemplates()));
    }

    /**
     * GET /api/v1/salary-slip-templates/{id}
     * Get a specific salary slip template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalarySlipTemplateResponse>> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salarySlipTemplateService.getTemplate(id)));
    }

    /**
     * POST /api/v1/salary-slip-templates
     * Create a new salary slip template
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SalarySlipTemplateResponse>> createTemplate(
            @Valid @RequestBody SalarySlipTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", salarySlipTemplateService.createTemplate(request)));
    }

    /**
     * PUT /api/v1/salary-slip-templates/{id}
     * Update an existing salary slip template
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalarySlipTemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody SalarySlipTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Template updated",
                salarySlipTemplateService.updateTemplate(id, request)));
    }

    /**
     * DELETE /api/v1/salary-slip-templates/{id}
     * Delete (deactivate) a salary slip template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        salarySlipTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted"));
    }

    /**
     * GET /api/v1/salary-slip-templates/{id}/preview
     * Generate and download a preview PDF of the template with sample data
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> getTemplatePreview(@PathVariable UUID id) {
        byte[] pdf = salarySlipTemplateService.generatePreviewPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=salary-slip-preview.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
