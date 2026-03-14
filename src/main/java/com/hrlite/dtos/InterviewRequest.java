package com.hrlite.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Job opening ID is required")
    private UUID jobOpeningId;

    private UUID interviewerId;

    @NotNull(message = "Interview date is required")
    private LocalDateTime interviewDate;

    private Integer durationMinutes;
    private String interviewType;
    private String location;
    private String meetingLink;
    private String notes;
}
