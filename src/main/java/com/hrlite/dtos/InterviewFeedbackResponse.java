package com.hrlite.dtos;

import com.hrlite.enums.FeedbackRecommendation;
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
public class InterviewFeedbackResponse {

    private UUID id;
    private UUID interviewId;
    private UUID reviewerId;
    private String reviewerName;
    private Integer rating;
    private String strengths;
    private String weaknesses;
    private String comments;
    private FeedbackRecommendation recommendation;
    private LocalDateTime createdAt;
}
