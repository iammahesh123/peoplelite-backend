package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingTaskResponse {
    private UUID id;
    private UUID employeeId;
    private String taskName;
    private String description;
    private int orderIndex;
    private boolean completed;
    private LocalDateTime completedAt;
}
