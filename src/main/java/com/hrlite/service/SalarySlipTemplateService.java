package com.hrlite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.entity.TenantContext;
import com.hrlite.utils.JoiningLetterPdfGenerator;
import com.hrlite.entity.SalarySlipTemplate;
import com.hrlite.repository.SalarySlipTemplateRepository;
import com.hrlite.dtos.SalarySlipTemplateRequest;
import com.hrlite.dtos.SalarySlipTemplateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalarySlipTemplateService {

    private final SalarySlipTemplateRepository templateRepository;
    private final JoiningLetterPdfGenerator pdfGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SalarySlipTemplateResponse> getTemplates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return templateRepository.findByTenantIdAndActiveTrueOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalarySlipTemplateResponse> getAllTemplates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return templateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalarySlipTemplateResponse getTemplate(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        SalarySlipTemplate template = templateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalarySlipTemplate", id));
        return toTemplateResponse(template);
    }

    @Transactional
    public SalarySlipTemplateResponse createTemplate(SalarySlipTemplateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // If this is marked as default, unset other defaults
        if (request.isDefault()) {
            templateRepository.findByTenantIdAndIsDefaultTrueAndActiveTrue(tenantId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        templateRepository.save(existingDefault);
                    });
        }

        SalarySlipTemplate template = SalarySlipTemplate.builder()
                .name(request.getName())
                .htmlContent(request.getHtmlContent())
                .description(request.getDescription())
                .isDefault(request.isDefault())
                .active(true)
                .build();

        SalarySlipTemplate saved = templateRepository.save(template);
        return toTemplateResponse(saved);
    }

    @Transactional
    public SalarySlipTemplateResponse updateTemplate(UUID id, SalarySlipTemplateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        SalarySlipTemplate template = templateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalarySlipTemplate", id));

        template.setName(request.getName());
        template.setHtmlContent(request.getHtmlContent());
        template.setDescription(request.getDescription());

        // If this is marked as default, unset other defaults
        if (request.isDefault() && !template.isDefault()) {
            templateRepository.findByTenantIdAndIsDefaultTrueAndActiveTrue(tenantId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        templateRepository.save(existingDefault);
                    });
            template.setDefault(true);
        } else if (!request.isDefault() && template.isDefault()) {
            template.setDefault(false);
        }

        template.setUpdatedAt(LocalDateTime.now());
        SalarySlipTemplate updated = templateRepository.save(template);
        return toTemplateResponse(updated);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        SalarySlipTemplate template = templateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalarySlipTemplate", id));

        template.setActive(false);
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public byte[] generatePreviewPdf(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        SalarySlipTemplate template = templateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalarySlipTemplate", id));

        Map<String, String> sampleData = generateSampleData();
        return pdfGenerator.generate(template.getHtmlContent(), sampleData);
    }

    public byte[] generatePdfFromTemplate(UUID templateId, Map<String, String> variables) {
        UUID tenantId = TenantContext.getCurrentTenant();
        SalarySlipTemplate template = templateRepository.findByTenantIdAndId(tenantId, templateId)
                .orElseThrow(() -> new ResourceNotFoundException("SalarySlipTemplate", templateId));

        return pdfGenerator.generate(template.getHtmlContent(), variables);
    }

    private Map<String, String> generateSampleData() {
        Map<String, String> data = new HashMap<>();
        data.put("companyName", "ABC Corporation");
        data.put("employeeName", "John Doe");
        data.put("employeeCode", "EMP001");
        data.put("designation", "Senior Software Engineer");
        data.put("department", "Engineering");
        data.put("month", "March");
        data.put("year", "2026");
        data.put("basic", "50,000.00");
        data.put("hra", "10,000.00");
        data.put("specialAllowance", "5,000.00");
        data.put("grossEarnings", "65,000.00");
        data.put("pfEmployee", "1,800.00");
        data.put("professionalTax", "200.00");
        data.put("otherDeductions", "500.00");
        data.put("totalDeductions", "2,500.00");
        data.put("netPay", "62,500.00");
        data.put("workingDays", "22");
        data.put("lopDays", "0");
        data.put("deductionRemarks", "None");
        return data;
    }

    private SalarySlipTemplateResponse toTemplateResponse(SalarySlipTemplate template) {
        return SalarySlipTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .htmlContent(template.getHtmlContent())
                .description(template.getDescription())
                .isDefault(template.isDefault())
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
