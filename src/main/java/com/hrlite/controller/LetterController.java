package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.LetterGenerationLog;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.LetterGenerationLogRepository;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/letters")
@RequiredArgsConstructor
@Slf4j
public class LetterController {

    private final LetterGenerationLogRepository logRepository;
    private final EmailService emailService;

    @PostMapping("/log")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LetterGenerationLog>> logGeneration(@RequestBody Map<String, String> body) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LetterGenerationLog logEntry = LetterGenerationLog.builder()
                .tenantId(principal.getTenantId())
                .employeeId(body.get("employeeId") != null ? UUID.fromString(body.get("employeeId")) : null)
                .employeeName(body.getOrDefault("employeeName", ""))
                .letterType(body.getOrDefault("letterType", ""))
                .templateStyle(body.getOrDefault("templateStyle", ""))
                .generatedBy(principal.getUserId())
                .generatedAt(LocalDateTime.now())
                .build();

        logEntry = logRepository.save(logEntry);
        return ResponseEntity.ok(ApiResponse.success("Letter generation logged", logEntry));
    }

    @PostMapping("/email")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<String>> emailLetter(@RequestBody Map<String, String> body) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String emailTo = body.get("emailTo");
        String subject = body.getOrDefault("subject", "Your Letter Document");
        String htmlBody = body.getOrDefault("htmlBody", "");

        if (emailTo == null || emailTo.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email address is required"));
        }

        emailService.sendEmail(emailTo, subject, htmlBody);

        // Also log this
        LetterGenerationLog logEntry = LetterGenerationLog.builder()
                .tenantId(principal.getTenantId())
                .employeeId(body.get("employeeId") != null ? UUID.fromString(body.get("employeeId")) : null)
                .employeeName(body.getOrDefault("employeeName", ""))
                .letterType(body.getOrDefault("letterType", ""))
                .templateStyle(body.getOrDefault("templateStyle", ""))
                .generatedBy(principal.getUserId())
                .generatedAt(LocalDateTime.now())
                .emailedTo(emailTo)
                .emailedAt(LocalDateTime.now())
                .build();
        logRepository.save(logEntry);

        return ResponseEntity.ok(ApiResponse.success("Letter emailed successfully", "sent"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<LetterGenerationLog>>> getHistory() {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<LetterGenerationLog> logs = logRepository.findByTenantIdOrderByGeneratedAtDesc(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Letter history retrieved", logs));
    }
}
