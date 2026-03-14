package com.hrlite.service;

import com.hrlite.entity.AuditLog;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.AuditLogRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entityType, String entityId, String description) {
        try {
            UUID tenantId = TenantContext.getCurrentTenant();
            UUID userId = null;
            String userName = "System";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                userId = principal.getUserId();
                userName = principal.getEmail();
                if (tenantId == null) tenantId = principal.getTenantId();
            }

            AuditLog auditLog = AuditLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .userName(userName)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    public void logSync(String action, String entityType, String entityId, String description, UUID tenantId, UUID userId, String userName) {
        AuditLog auditLog = AuditLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .userName(userName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(UUID tenantId, String action, String entityType,
                                         LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findFiltered(tenantId, action, entityType, startDate, endDate, pageable);
    }
}
