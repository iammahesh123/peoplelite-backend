package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {
    private long totalEmployees;
    private long activeEmployees;
    private long pendingLeaveRequests;
    private String lastPayrollMonth;
    private BigDecimal lastPayrollTotal;
    private long pendingOnboardingTasks;
}
