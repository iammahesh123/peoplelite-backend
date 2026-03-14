package com.hrlite.dtos;

import com.hrlite.enums.CandidateSource;
import com.hrlite.enums.CandidateStage;
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
public class CandidateResponse {

    private UUID id;
    private UUID jobOpeningId;
    private String jobTitle;
    private String name;
    private String email;
    private String phone;
    private String resumeUrl;
    private String linkedinUrl;
    private String portfolioUrl;
    private CandidateSource source;
    private CandidateStage stage;
    private String notes;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
