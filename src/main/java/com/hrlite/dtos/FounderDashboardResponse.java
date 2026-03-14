package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FounderDashboardResponse {

    private KPIs kpis;
    private LeaveOverview leaveOverview;
    private PayrollOverview payrollOverview;
    private OnboardingTracker onboardingTracker;
    private List<AlertItem> alerts;
    private SelfBoardingOverview selfBoardingOverview;
    private LetterActivity letterActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIs {
        private long totalEmployees;
        private long activeEmployees;
        private long pendingLeaveRequests;
        private String payrollStatus;
        private long upcomingJoinees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveOverview {
        private List<OnLeaveItem> onLeaveToday;
        private List<UpcomingLeave> upcomingLeaves;
        private List<LeaveRequestResponse> pendingApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnLeaveItem {
        private String employeeId;
        private String employeeName;
        private String leaveType;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingLeave {
        private String employeeId;
        private String employeeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String leaveType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayrollOverview {
        private String currentCycleStatus;
        private int month;
        private int year;
        private long employeesProcessed;
        private long totalEmployees;
        private BigDecimal totalPayout;
        private String currency;
        private boolean pendingGeneration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingTracker {
        private List<NewEmployee> newEmployees;
        private long pendingOfferLetters;
        private long missingDocuments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewEmployee {
        private String employeeId;
        private String employeeName;
        private LocalDate dateOfJoining;
        private int progress;
        private int totalTasks;
        private int completedTasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelfBoardingOverview {
        private long totalInvitations;
        private long pendingInvitations;
        private long completedInvitations;
        private List<RecentInvitation> recentInvitations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentInvitation {
        private String employeeName;
        private String email;
        private String status;
        private String sentAt;
        private String completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LetterActivity {
        private long totalLettersGenerated;
        private List<RecentLetter> recentLetters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentLetter {
        private String employeeName;
        private String letterType;
        private String templateStyle;
        private String generatedAt;
        private String emailedTo;
    }
}
