package com.hrlite.dtos;

import com.hrlite.enums.InterviewStatus;
import com.hrlite.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private UUID id;
    private UUID candidateId;
    private String candidateName;
    private UUID jobOpeningId;
    private String jobTitle;
    private UUID interviewerId;
    private String interviewerName;
    private LocalDateTime interviewDate;
    private Integer durationMinutes;
    private InterviewType interviewType;
    private String location;
    private String meetingLink;
    private InterviewStatus status;
    private String notes;
    private List<InterviewFeedbackResponse> feedback;
    private LocalDateTime createdAt;
}
