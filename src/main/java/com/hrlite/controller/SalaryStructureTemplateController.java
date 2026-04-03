package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.SalaryStructureTemplateRequest;
import com.hrlite.entity.SalaryStructureTemplate;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.SalaryStructureTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salary-templates")
@RequiredArgsConstructor
public class SalaryStructureTemplateController {

    private final SalaryStructureTemplateService salaryStructureTemplateService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<SalaryStructureTemplate>> createTemplate(
            @Valid @RequestBody SalaryStructureTemplateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SalaryStructureTemplate template = salaryStructureTemplateService.create(
                request.getName(), request.getBasicPercent(), request.getHraPercent(),
                request.getSpecialAllowancePercent(), request.isDefault());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary structure template created", template));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<SalaryStructureTemplate>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(salaryStructureTemplateService.getAll()));
    }

    @GetMapping("/default")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<SalaryStructureTemplate>> getDefaultTemplate() {
        return ResponseEntity.ok(ApiResponse.success(salaryStructureTemplateService.getDefault()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        salaryStructureTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Salary structure template deleted", null));
    }
}
