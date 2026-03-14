package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingStatusResponse {
    private UUID employeeId;
    private String employeeName;
    private long totalTasks;
    private long completedTasks;
    private int progressPercent;
    private List<OnboardingTaskResponse> tasks;
}
