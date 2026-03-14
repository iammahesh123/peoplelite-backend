package com.hrlite.service;

import com.hrlite.entity.LetterGenerationLog;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.LetterGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LetterHistoryService {

    private final LetterGenerationLogRepository logRepository;
    private final EmailService emailService;

    @Transactional
    public LetterGenerationLog logGeneration(UUID employeeId, String employeeName,
                                             String letterType, String templateStyle, UUID generatedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();
        LetterGenerationLog log = LetterGenerationLog.builder()
                .tenantId(tenantId)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .letterType(letterType)
                .templateStyle(templateStyle)
                .generatedBy(generatedBy)
                .generatedAt(LocalDateTime.now())
                .build();
        return logRepository.save(log);
    }

    @Transactional
    public LetterGenerationLog logAndEmail(UUID employeeId, String employeeName,
                                           String letterType, String templateStyle,
                                           UUID generatedBy, String emailTo,
                                           String subject, String htmlBody) {
        UUID tenantId = TenantContext.getCurrentTenant();
        LetterGenerationLog log = LetterGenerationLog.builder()
                .tenantId(tenantId)
                .employeeId(employeeId)
                .employeeName(employeeName)
                .letterType(letterType)
                .templateStyle(templateStyle)
                .generatedBy(generatedBy)
                .generatedAt(LocalDateTime.now())
                .emailedTo(emailTo)
                .emailedAt(LocalDateTime.now())
                .build();
        log = logRepository.save(log);

        emailService.sendEmail(emailTo, subject, htmlBody);
        return log;
    }

    public List<LetterGenerationLog> getHistory() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return logRepository.findByTenantIdOrderByGeneratedAtDesc(tenantId);
    }

    public List<LetterGenerationLog> getHistoryForEmployee(UUID employeeId) {
        return logRepository.findByEmployeeIdOrderByGeneratedAtDesc(employeeId);
    }

    public long getTotalCount() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return logRepository.countByTenantId(tenantId);
    }
}
