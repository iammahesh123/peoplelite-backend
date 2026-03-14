package com.hrlite.dtos;

import com.hrlite.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOpeningResponse {

    private UUID id;
    private String title;
    private String department;
    private String location;
    private String employmentType;
    private Integer experienceMin;
    private Integer experienceMax;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private JobStatus status;
    private int totalCandidates;
    private Map<String, Integer> stageStats;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
