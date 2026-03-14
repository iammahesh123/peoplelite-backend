package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardResponse {

    private QuickInfo quickInfo;
    private List<LeaveBalanceResponse> leaveBalances;
    private List<LeaveRequestResponse> recentLeaves;
    private List<RecentPayslip> recentPayslips;
    private OnboardingInfo onboardingTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickInfo {
        private double totalLeaveBalance;
        private double leavesThisMonth;
        private String nextPayrollDate;
        private String latestPayslipStatus;
        private String latestPayslipMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPayslip {
        private String id;
        private int month;
        private int year;
        private BigDecimal netPay;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingInfo {
        private int totalTasks;
        private int completedTasks;
        private List<OnboardingTaskResponse> pendingTasks;
    }
}
