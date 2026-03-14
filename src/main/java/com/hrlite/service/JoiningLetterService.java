package com.hrlite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrlite.dtos.GenerateJoiningLetterRequest;
import com.hrlite.dtos.JoiningLetterResponse;
import com.hrlite.dtos.JoiningLetterTemplateRequest;
import com.hrlite.dtos.JoiningLetterTemplateResponse;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.entity.TenantContext;
import com.hrlite.utils.S3StorageService;
import com.hrlite.entity.Employee;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.entity.JoiningLetter;
import com.hrlite.entity.JoiningLetterTemplate;
import com.hrlite.repository.JoiningLetterRepository;
import com.hrlite.repository.JoiningLetterTemplateRepository;
import com.hrlite.utils.JoiningLetterPdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoiningLetterService {

    private final JoiningLetterTemplateRepository templateRepository;
    private final JoiningLetterRepository joiningLetterRepository;
    private final EmployeeRepository employeeRepository;
    private final JoiningLetterPdfGenerator pdfGenerator;
    private final S3StorageService storageService;
    private final ObjectMapper objectMapper;

    // --- Templates ---

    @Transactional(readOnly = true)
    public List<JoiningLetterTemplateResponse> getTemplates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return templateRepository.findByTenantIdAndActiveTrueOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JoiningLetterTemplateResponse createTemplate(JoiningLetterTemplateRequest request) {
        JoiningLetterTemplate template = JoiningLetterTemplate.builder()
                .name(request.getName())
                .htmlContent(request.getHtmlContent())
                .variablesJson(request.getVariablesJson())
                .build();
        return toTemplateResponse(templateRepository.save(template));
    }

    // --- Joining Letters ---

    @Transactional
    public JoiningLetterResponse generateLetter(GenerateJoiningLetterRequest request, UUID generatedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();

        JoiningLetterTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("JoiningLetterTemplate", request.getTemplateId()));

        Employee employee = employeeRepository.findByIdAndTenantId(request.getEmployeeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));

        // Add employee info to variables
        Map<String, String> variables = request.getVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("employeeName", employee.getFullName());
        variables.put("employeeEmail", employee.getEmail());
        variables.put("designation", employee.getDesignation() != null ? employee.getDesignation() : "");
        variables.put("department", employee.getDepartment() != null ? employee.getDepartment() : "");

        // Generate PDF
        byte[] pdfBytes = pdfGenerator.generate(template.getHtmlContent(), variables);

        // Upload to S3
        String storageKey = storageService.upload(
                tenantId.toString(),
                "joining-letters",
                "joining-letter-" + employee.getFullName().replaceAll("\\s+", "-") + ".pdf",
                new ByteArrayInputStream(pdfBytes),
                pdfBytes.length,
                "application/pdf");

        // Save record
        String variablesJsonStr = null;
        try {
            variablesJsonStr = objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize variables", e);
        }

        JoiningLetter joiningLetter = JoiningLetter.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .templateId(template.getId())
                .variablesJson(variablesJsonStr)
                .pdfStorageKey(storageKey)
                .generatedAt(LocalDateTime.now())
                .generatedBy(generatedBy)
                .build();

        return toJoiningLetterResponse(joiningLetterRepository.save(joiningLetter));
    }

    @Transactional(readOnly = true)
    public List<JoiningLetterResponse> getLetters() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return joiningLetterRepository.findByTenantIdOrderByGeneratedAtDesc(tenantId).stream()
                .map(this::toJoiningLetterResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JoiningLetterResponse> getLettersByEmployee(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return joiningLetterRepository.findByEmployeeIdAndTenantId(employeeId, tenantId).stream()
                .map(this::toJoiningLetterResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] getLetterPdf(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        JoiningLetter joiningLetter = joiningLetterRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("JoiningLetter", id));
        return storageService.download(joiningLetter.getPdfStorageKey());
    }

    @Transactional(readOnly = true)
    public byte[] getLetterPdfForEmployee(UUID id, UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        JoiningLetter joiningLetter = joiningLetterRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("JoiningLetter", id));

        // Ensure the letter belongs to the requesting employee
        if (joiningLetter.getEmployeeId() == null || !joiningLetter.getEmployeeId().equals(employeeId)) {
            throw new ResourceNotFoundException("JoiningLetter", id);
        }

        return storageService.download(joiningLetter.getPdfStorageKey());
    }

    private JoiningLetterTemplateResponse toTemplateResponse(JoiningLetterTemplate t) {
        return JoiningLetterTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .htmlContent(t.getHtmlContent())
                .variablesJson(t.getVariablesJson())
                .version(t.getVersion())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private JoiningLetterResponse toJoiningLetterResponse(JoiningLetter jl) {
        String templateName = templateRepository.findById(jl.getTemplateId())
                .map(JoiningLetterTemplate::getName)
                .orElse("Unknown");

        return JoiningLetterResponse.builder()
                .id(jl.getId())
                .employeeId(jl.getEmployeeId())
                .employeeName(jl.getEmployeeName())
                .templateId(jl.getTemplateId())
                .templateName(templateName)
                .variablesJson(jl.getVariablesJson())
                .generatedAt(jl.getGeneratedAt())
                .build();
    }
}
