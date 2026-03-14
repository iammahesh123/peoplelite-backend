package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse {

    private List<AuditEntry> entries;
    private long total;
    private int page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditEntry {
        private String id;
        private String tenantId;
        private String userId;
        private String action; // CREATE, UPDATE, DELETE, APPROVE, REJECT
        private String entityType;
        private String entityId;
        private String changes; // JSON diff
        private String ipAddress;
        private LocalDateTime timestamp;
    }
}
