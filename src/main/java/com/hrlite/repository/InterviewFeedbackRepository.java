package com.hrlite.repository;

import com.hrlite.entity.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, UUID> {

    List<InterviewFeedback> findByInterviewIdOrderByCreatedAtDesc(UUID interviewId);

    List<InterviewFeedback> findByTenantIdAndInterviewId(UUID tenantId, UUID interviewId);
}
