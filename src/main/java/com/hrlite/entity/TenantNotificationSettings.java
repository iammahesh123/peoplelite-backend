package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Column(name = "email_leave_requests")
    @Builder.Default
    private boolean emailLeaveRequests = true;

    @Column(name = "email_leave_approvals")
    @Builder.Default
    private boolean emailLeaveApprovals = true;

    @Column(name = "email_payroll_ready")
    @Builder.Default
    private boolean emailPayrollReady = true;

    @Column(name = "email_new_employee")
    @Builder.Default
    private boolean emailNewEmployee = true;

    @Column(name = "email_document_uploaded")
    @Builder.Default
    private boolean emailDocumentUploaded = true;

    @Column(name = "email_system_alerts")
    @Builder.Default
    private boolean emailSystemAlerts = true;

    @Column(name = "inapp_leave_requests")
    @Builder.Default
    private boolean inappLeaveRequests = true;

    @Column(name = "inapp_payroll")
    @Builder.Default
    private boolean inappPayroll = true;

    @Column(name = "inapp_onboarding")
    @Builder.Default
    private boolean inappOnboarding = true;

    @Column(name = "inapp_documents")
    @Builder.Default
    private boolean inappDocuments = true;

    @Column(name = "inapp_system")
    @Builder.Default
    private boolean inappSystem = true;

    @Column(name = "quiet_hours_enabled")
    @Builder.Default
    private boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    @Builder.Default
    private LocalTime quietHoursStart = LocalTime.of(22, 0);

    @Column(name = "quiet_hours_end")
    @Builder.Default
    private LocalTime quietHoursEnd = LocalTime.of(8, 0);

    @Column(name = "digest_frequency")
    @Builder.Default
    private String digestFrequency = "daily";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
