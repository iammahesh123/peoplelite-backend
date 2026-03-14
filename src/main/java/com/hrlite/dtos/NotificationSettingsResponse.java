package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsResponse {

    private UUID id;
    private UUID tenantId;
    private boolean emailLeaveRequests;
    private boolean emailLeaveApprovals;
    private boolean emailPayrollReady;
    private boolean emailNewEmployee;
    private boolean emailDocumentUploaded;
    private boolean emailSystemAlerts;
    private boolean inappLeaveRequests;
    private boolean inappPayroll;
    private boolean inappOnboarding;
    private boolean inappDocuments;
    private boolean inappSystem;
    private boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String digestFrequency;
    private LocalDateTime updatedAt;
}
