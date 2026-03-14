package com.hrlite.entity;

import com.hrlite.enums.FeedbackRecommendation;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedback extends TenantAwareEntity {

    @Column(name = "interview_id", nullable = false)
    private UUID interviewId;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false)
    @Builder.Default
    private FeedbackRecommendation recommendation = FeedbackRecommendation.NEUTRAL;
}
