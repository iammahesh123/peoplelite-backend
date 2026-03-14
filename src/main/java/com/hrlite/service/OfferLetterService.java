package com.hrlite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrlite.dtos.GenerateOfferLetterRequest;
import com.hrlite.dtos.OfferLetterResponse;
import com.hrlite.dtos.OfferLetterTemplateRequest;
import com.hrlite.dtos.OfferLetterTemplateResponse;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.TenantContext;
import com.hrlite.utils.S3StorageService;
import com.hrlite.entity.OfferLetter;
import com.hrlite.entity.OfferLetterTemplate;
import com.hrlite.repository.OfferLetterRepository;
import com.hrlite.repository.OfferLetterTemplateRepository;
import com.hrlite.utils.OfferLetterPdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferLetterService {

    private final OfferLetterTemplateRepository templateRepository;
    private final OfferLetterRepository offerLetterRepository;
    private final OfferLetterPdfGenerator pdfGenerator;
    private final S3StorageService storageService;
    private final ObjectMapper objectMapper;

    // --- Templates ---

    @Transactional(readOnly = true)
    public List<OfferLetterTemplateResponse> getTemplates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return templateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OfferLetterTemplateResponse createTemplate(OfferLetterTemplateRequest request) {
        OfferLetterTemplate template = OfferLetterTemplate.builder()
                .name(request.getName())
                .htmlContent(request.getHtmlContent())
                .variablesJson(request.getVariablesJson())
                .build();
        return toTemplateResponse(templateRepository.save(template));
    }

    // --- Offer Letters ---

    @Transactional
    public OfferLetterResponse generateOfferLetter(GenerateOfferLetterRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();

        OfferLetterTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("OfferLetterTemplate", request.getTemplateId()));

        // Add candidate info to variables
        Map<String, String> variables = request.getVariables();
        if (variables != null) {
            variables.put("candidateName", request.getCandidateName());
            if (request.getCandidateEmail() != null) {
                variables.put("candidateEmail", request.getCandidateEmail());
            }
        }

        // Generate PDF
        byte[] pdfBytes = pdfGenerator.generate(template.getHtmlContent(), variables);

        // Upload to S3
        String storageKey = storageService.upload(
                tenantId.toString(),
                "offer-letters",
                "offer-letter-" + request.getCandidateName().replaceAll("\\s+", "-") + ".pdf",
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

        OfferLetter offerLetter = OfferLetter.builder()
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .templateId(template.getId())
                .employeeId(request.getEmployeeId())
                .variablesJson(variablesJsonStr)
                .pdfStorageKey(storageKey)
                .generatedAt(LocalDateTime.now())
                .generatedBy(principal.getUserId())
                .build();

        return toOfferLetterResponse(offerLetterRepository.save(offerLetter));
    }

    @Transactional(readOnly = true)
    public List<OfferLetterResponse> getOfferLetters() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return offerLetterRepository.findByTenantIdOrderByGeneratedAtDesc(tenantId).stream()
                .map(this::toOfferLetterResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] downloadOfferLetterPdf(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        OfferLetter offerLetter = offerLetterRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OfferLetter", id));
        return storageService.download(offerLetter.getPdfStorageKey());
    }

    @Transactional(readOnly = true)
    public List<OfferLetterResponse> getOfferLettersByEmployee(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return offerLetterRepository.findByEmployeeIdAndTenantId(employeeId, tenantId).stream()
                .map(this::toOfferLetterResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] downloadOfferLetterPdfForEmployee(UUID id, UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        OfferLetter offerLetter = offerLetterRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OfferLetter", id));

        // Ensure the letter belongs to the requesting employee
        if (offerLetter.getEmployeeId() != null && !offerLetter.getEmployeeId().equals(employeeId)) {
            throw new ResourceNotFoundException("OfferLetter", id);
        }

        return storageService.download(offerLetter.getPdfStorageKey());
    }

    private OfferLetterTemplateResponse toTemplateResponse(OfferLetterTemplate t) {
        return OfferLetterTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .htmlContent(t.getHtmlContent())
                .variablesJson(t.getVariablesJson())
                .version(t.getVersion())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private OfferLetterResponse toOfferLetterResponse(OfferLetter ol) {
        String templateName = templateRepository.findById(ol.getTemplateId())
                .map(OfferLetterTemplate::getName)
                .orElse("Unknown");

        return OfferLetterResponse.builder()
                .id(ol.getId())
                .candidateName(ol.getCandidateName())
                .candidateEmail(ol.getCandidateEmail())
                .templateId(ol.getTemplateId())
                .templateName(templateName)
                .variablesJson(ol.getVariablesJson())
                .generatedAt(ol.getGeneratedAt())
                .build();
    }
}
