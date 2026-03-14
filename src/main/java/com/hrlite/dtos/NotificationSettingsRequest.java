package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {

    private Boolean emailLeaveRequests;
    private Boolean emailLeaveApprovals;
    private Boolean emailPayrollReady;
    private Boolean emailNewEmployee;
    private Boolean emailDocumentUploaded;
    private Boolean emailSystemAlerts;
    private Boolean inappLeaveRequests;
    private Boolean inappPayroll;
    private Boolean inappOnboarding;
    private Boolean inappDocuments;
    private Boolean inappSystem;
    private Boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String digestFrequency;
}
