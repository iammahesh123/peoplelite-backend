package com.hrlite.service;

import com.hrlite.dtos.*;
import com.hrlite.entity.*;
import com.hrlite.enums.*;
import com.hrlite.repository.*;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HiringService {

    private final JobOpeningRepository jobOpeningRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewFeedbackRepository feedbackRepository;
    private final OfferRepository offerRepository;
    private final CandidateActivityRepository activityRepository;
    private final EmployeeRepository employeeRepository;

    // ─── Job Openings ────────────────────────────────────────

    public JobOpeningResponse createJobOpening(JobOpeningRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        JobOpening job = JobOpening.builder()
                .title(request.getTitle())
                .department(request.getDepartment())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType() != null ? request.getEmploymentType() : "FULL_TIME")
                .experienceMin(request.getExperienceMin())
                .experienceMax(request.getExperienceMax())
                .description(request.getDescription())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .status(JobStatus.OPEN)
                .build();

        job = jobOpeningRepository.save(job);
        return mapJobToResponse(job);
    }

    public List<JobOpeningResponse> getAllJobOpenings() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return jobOpeningRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapJobToResponse)
                .toList();
    }

    public List<JobOpeningResponse> getJobOpeningsByStatus(JobStatus status) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return jobOpeningRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status).stream()
                .map(this::mapJobToResponse)
                .toList();
    }

    public JobOpeningResponse getJobOpeningById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        JobOpening job = jobOpeningRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Job opening not found"));
        return mapJobToResponse(job);
    }

    public JobOpeningResponse updateJobOpening(UUID id, JobOpeningRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        JobOpening job = jobOpeningRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Job opening not found"));

        job.setTitle(request.getTitle());
        job.setDepartment(request.getDepartment());
        job.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        job.setExperienceMin(request.getExperienceMin());
        job.setExperienceMax(request.getExperienceMax());
        job.setDescription(request.getDescription());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());

        job = jobOpeningRepository.save(job);
        return mapJobToResponse(job);
    }

    public void updateJobStatus(UUID id, JobStatus status) {
        UUID tenantId = TenantContext.getCurrentTenant();
        JobOpening job = jobOpeningRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Job opening not found"));

        job.setStatus(status);
        if (status == JobStatus.CLOSED || status == JobStatus.CANCELLED) {
            job.setClosedAt(LocalDateTime.now());
        }
        jobOpeningRepository.save(job);
    }

    // ─── Candidates ──────────────────────────────────────────

    public CandidateResponse addCandidate(CandidateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Check for duplicate
        if (candidateRepository.existsByEmailAndJobOpeningIdAndTenantId(
                request.getEmail(), request.getJobOpeningId(), tenantId)) {
            throw new IllegalArgumentException("Candidate with this email already applied for this position");
        }

        CandidateSource source = CandidateSource.MANUAL;
        if (request.getSource() != null) {
            try { source = CandidateSource.valueOf(request.getSource()); } catch (Exception ignored) {}
        }

        Candidate candidate = Candidate.builder()
                .jobOpeningId(request.getJobOpeningId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .resumeUrl(request.getResumeUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .portfolioUrl(request.getPortfolioUrl())
                .source(source)
                .stage(CandidateStage.APPLIED)
                .notes(request.getNotes())
                .appliedAt(LocalDateTime.now())
                .build();

        candidate = candidateRepository.save(candidate);

        // Log activity
        logActivity(candidate.getId(), CandidateActivityType.APPLICATION_RECEIVED,
                "Application received for " + candidate.getName());

        return mapCandidateToResponse(candidate);
    }

    public List<CandidateResponse> getCandidatesByJob(UUID jobId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return candidateRepository.findByTenantIdAndJobOpeningIdOrderByCreatedAtDesc(tenantId, jobId).stream()
                .map(this::mapCandidateToResponse)
                .toList();
    }

    public List<CandidateResponse> getAllCandidates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return candidateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapCandidateToResponse)
                .toList();
    }

    public CandidateResponse getCandidateById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Candidate candidate = candidateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        return mapCandidateToResponse(candidate);
    }

    public CandidateResponse updateCandidateStage(UUID id, CandidateStageUpdateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Candidate candidate = candidateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        CandidateStage oldStage = candidate.getStage();
        candidate.setStage(request.getStage());
        if (request.getNotes() != null) {
            candidate.setNotes(request.getNotes());
        }

        candidate = candidateRepository.save(candidate);

        logActivity(candidate.getId(), CandidateActivityType.STAGE_CHANGED,
                "Stage changed from " + oldStage + " to " + request.getStage());

        return mapCandidateToResponse(candidate);
    }

    // ─── Interviews ──────────────────────────────────────────

    public InterviewResponse scheduleInterview(InterviewRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        InterviewType type = InterviewType.IN_PERSON;
        if (request.getInterviewType() != null) {
            try { type = InterviewType.valueOf(request.getInterviewType()); } catch (Exception ignored) {}
        }

        Interview interview = Interview.builder()
                .candidateId(request.getCandidateId())
                .jobOpeningId(request.getJobOpeningId())
                .interviewerId(request.getInterviewerId())
                .interviewDate(request.getInterviewDate())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .interviewType(type)
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .status(InterviewStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();

        interview = interviewRepository.save(interview);

        // Update candidate stage
        Candidate candidate = candidateRepository.findByIdAndTenantId(request.getCandidateId(), tenantId).orElse(null);
        if (candidate != null && candidate.getStage() == CandidateStage.APPLIED) {
            candidate.setStage(CandidateStage.INTERVIEW_SCHEDULED);
            candidateRepository.save(candidate);
        }

        logActivity(request.getCandidateId(), CandidateActivityType.INTERVIEW_SCHEDULED,
                "Interview scheduled for " + request.getInterviewDate());

        return mapInterviewToResponse(interview);
    }

    public List<InterviewResponse> getAllInterviews() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return interviewRepository.findByTenantIdOrderByInterviewDateDesc(tenantId).stream()
                .map(this::mapInterviewToResponse)
                .toList();
    }

    public List<InterviewResponse> getInterviewsByCandidate(UUID candidateId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return interviewRepository.findByTenantIdAndCandidateIdOrderByInterviewDateDesc(tenantId, candidateId).stream()
                .map(this::mapInterviewToResponse)
                .toList();
    }

    public void updateInterviewStatus(UUID id, InterviewStatus status) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Interview interview = interviewRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        interview.setStatus(status);
        interviewRepository.save(interview);

        if (status == InterviewStatus.COMPLETED) {
            Candidate candidate = candidateRepository.findByIdAndTenantId(interview.getCandidateId(), tenantId).orElse(null);
            if (candidate != null) {
                candidate.setStage(CandidateStage.INTERVIEW_COMPLETED);
                candidateRepository.save(candidate);
            }
            logActivity(interview.getCandidateId(), CandidateActivityType.INTERVIEW_COMPLETED,
                    "Interview completed");
        }
    }

    // ─── Interview Feedback ──────────────────────────────────

    public InterviewFeedbackResponse addFeedback(InterviewFeedbackRequest request) {
        FeedbackRecommendation rec = FeedbackRecommendation.NEUTRAL;
        if (request.getRecommendation() != null) {
            try { rec = FeedbackRecommendation.valueOf(request.getRecommendation()); } catch (Exception ignored) {}
        }

        InterviewFeedback feedback = InterviewFeedback.builder()
                .interviewId(request.getInterviewId())
                .reviewerId(request.getReviewerId())
                .rating(request.getRating())
                .strengths(request.getStrengths())
                .weaknesses(request.getWeaknesses())
                .comments(request.getComments())
                .recommendation(rec)
                .build();

        feedback = feedbackRepository.save(feedback);

        // Find the interview to get candidateId
        Interview interview = interviewRepository.findById(request.getInterviewId()).orElse(null);
        if (interview != null) {
            logActivity(interview.getCandidateId(), CandidateActivityType.FEEDBACK_ADDED,
                    "Interview feedback added with rating " + request.getRating() + "/5");
        }

        return mapFeedbackToResponse(feedback);
    }

    public List<InterviewFeedbackResponse> getFeedbackByInterview(UUID interviewId) {
        return feedbackRepository.findByInterviewIdOrderByCreatedAtDesc(interviewId).stream()
                .map(this::mapFeedbackToResponse)
                .toList();
    }

    // ─── Offers ──────────────────────────────────────────────

    public OfferResponse createOffer(OfferRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Offer offer = Offer.builder()
                .candidateId(request.getCandidateId())
                .jobOpeningId(request.getJobOpeningId())
                .offeredSalary(request.getOfferedSalary())
                .joiningDate(request.getJoiningDate())
                .designation(request.getDesignation())
                .department(request.getDepartment())
                .offerNotes(request.getOfferNotes())
                .status(OfferStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        offer = offerRepository.save(offer);

        // Update candidate stage
        Candidate candidate = candidateRepository.findByIdAndTenantId(request.getCandidateId(), tenantId).orElse(null);
        if (candidate != null) {
            candidate.setStage(CandidateStage.OFFER_SENT);
            candidateRepository.save(candidate);
        }

        logActivity(request.getCandidateId(), CandidateActivityType.OFFER_SENT,
                "Offer sent with salary " + request.getOfferedSalary());

        return mapOfferToResponse(offer);
    }

    public List<OfferResponse> getAllOffers() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return offerRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapOfferToResponse)
                .toList();
    }

    public OfferResponse updateOfferStatus(UUID id, OfferStatus status) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        offer.setStatus(status);
        offer.setRespondedAt(LocalDateTime.now());
        offer = offerRepository.save(offer);

        // Update candidate stage based on offer response
        Candidate candidate = candidateRepository.findByIdAndTenantId(offer.getCandidateId(), tenantId).orElse(null);
        if (candidate != null) {
            if (status == OfferStatus.ACCEPTED) {
                candidate.setStage(CandidateStage.HIRED);
                candidateRepository.save(candidate);
                logActivity(candidate.getId(), CandidateActivityType.CANDIDATE_HIRED, "Candidate hired");
            } else if (status == OfferStatus.REJECTED) {
                logActivity(candidate.getId(), CandidateActivityType.OFFER_REJECTED, "Offer was rejected by candidate");
            }
        }

        return mapOfferToResponse(offer);
    }

    // ─── Activity Timeline ───────────────────────────────────

    public List<CandidateActivityResponse> getCandidateActivities(UUID candidateId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return activityRepository.findByTenantIdAndCandidateIdOrderByCreatedAtDesc(tenantId, candidateId).stream()
                .map(this::mapActivityToResponse)
                .toList();
    }

    // ─── Dashboard Metrics ───────────────────────────────────

    public HiringDashboardResponse getDashboardMetrics() {
        UUID tenantId = TenantContext.getCurrentTenant();

        long totalCandidates = candidateRepository.countByTenantId(tenantId);
        long openPositions = jobOpeningRepository.countByTenantIdAndStatus(tenantId, JobStatus.OPEN);
        long closedPositions = jobOpeningRepository.countByTenantIdAndStatus(tenantId, JobStatus.CLOSED);
        long totalHired = candidateRepository.countByTenantIdAndStage(tenantId, CandidateStage.HIRED);
        long totalRejected = candidateRepository.countByTenantIdAndStage(tenantId, CandidateStage.REJECTED);
        long pendingInterviews = interviewRepository.countByTenantIdAndStatus(tenantId, InterviewStatus.SCHEDULED);
        long pendingOffers = offerRepository.countByTenantIdAndStatus(tenantId, OfferStatus.PENDING);

        // Candidates per stage
        Map<String, Integer> candidatesPerStage = new LinkedHashMap<>();
        for (CandidateStage stage : CandidateStage.values()) {
            candidatesPerStage.put(stage.name(), (int) candidateRepository.countByTenantIdAndStage(tenantId, stage));
        }

        // Hiring success rate
        double successRate = totalCandidates > 0 ? (double) totalHired / totalCandidates * 100 : 0;

        // Average days to hire (from applied to hired)
        double avgDays = 0;
        List<Candidate> hiredCandidates = candidateRepository.findByTenantIdAndStageOrderByCreatedAtDesc(tenantId, CandidateStage.HIRED);
        if (!hiredCandidates.isEmpty()) {
            long totalDays = hiredCandidates.stream()
                    .mapToLong(c -> ChronoUnit.DAYS.between(c.getAppliedAt(), c.getUpdatedAt()))
                    .sum();
            avgDays = (double) totalDays / hiredCandidates.size();
        }

        return HiringDashboardResponse.builder()
                .totalCandidates((int) totalCandidates)
                .candidatesPerStage(candidatesPerStage)
                .openPositions((int) openPositions)
                .closedPositions((int) closedPositions)
                .totalHired((int) totalHired)
                .totalRejected((int) totalRejected)
                .hiringSuccessRate(Math.round(successRate * 10.0) / 10.0)
                .avgDaysToHire(Math.round(avgDays * 10.0) / 10.0)
                .pendingInterviews((int) pendingInterviews)
                .pendingOffers((int) pendingOffers)
                .build();
    }

    // ─── Private Mappers ─────────────────────────────────────

    private JobOpeningResponse mapJobToResponse(JobOpening job) {
        UUID tenantId = TenantContext.getCurrentTenant();
        long totalCandidates = candidateRepository.countByTenantIdAndJobOpeningId(tenantId, job.getId());

        Map<String, Integer> stageStats = new LinkedHashMap<>();
        for (CandidateStage stage : CandidateStage.values()) {
            List<Candidate> atStage = candidateRepository.findByTenantIdAndJobOpeningIdAndStage(tenantId, job.getId(), stage);
            stageStats.put(stage.name(), atStage.size());
        }

        return JobOpeningResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .department(job.getDepartment())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .description(job.getDescription())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .status(job.getStatus())
                .totalCandidates((int) totalCandidates)
                .stageStats(stageStats)
                .closedAt(job.getClosedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private CandidateResponse mapCandidateToResponse(Candidate candidate) {
        JobOpening job = jobOpeningRepository.findById(candidate.getJobOpeningId()).orElse(null);

        return CandidateResponse.builder()
                .id(candidate.getId())
                .jobOpeningId(candidate.getJobOpeningId())
                .jobTitle(job != null ? job.getTitle() : "Unknown")
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .resumeUrl(candidate.getResumeUrl())
                .linkedinUrl(candidate.getLinkedinUrl())
                .portfolioUrl(candidate.getPortfolioUrl())
                .source(candidate.getSource())
                .stage(candidate.getStage())
                .notes(candidate.getNotes())
                .appliedAt(candidate.getAppliedAt())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }

    private InterviewResponse mapInterviewToResponse(Interview interview) {
        Candidate candidate = candidateRepository.findById(interview.getCandidateId()).orElse(null);
        JobOpening job = jobOpeningRepository.findById(interview.getJobOpeningId()).orElse(null);
        Employee interviewer = interview.getInterviewerId() != null
                ? employeeRepository.findById(interview.getInterviewerId()).orElse(null) : null;

        List<InterviewFeedbackResponse> feedbackList = feedbackRepository
                .findByInterviewIdOrderByCreatedAtDesc(interview.getId()).stream()
                .map(this::mapFeedbackToResponse)
                .toList();

        return InterviewResponse.builder()
                .id(interview.getId())
                .candidateId(interview.getCandidateId())
                .candidateName(candidate != null ? candidate.getName() : "Unknown")
                .jobOpeningId(interview.getJobOpeningId())
                .jobTitle(job != null ? job.getTitle() : "Unknown")
                .interviewerId(interview.getInterviewerId())
                .interviewerName(interviewer != null ? interviewer.getFullName() : null)
                .interviewDate(interview.getInterviewDate())
                .durationMinutes(interview.getDurationMinutes())
                .interviewType(interview.getInterviewType())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .status(interview.getStatus())
                .notes(interview.getNotes())
                .feedback(feedbackList)
                .createdAt(interview.getCreatedAt())
                .build();
    }

    private InterviewFeedbackResponse mapFeedbackToResponse(InterviewFeedback feedback) {
        Employee reviewer = feedback.getReviewerId() != null
                ? employeeRepository.findById(feedback.getReviewerId()).orElse(null) : null;

        return InterviewFeedbackResponse.builder()
                .id(feedback.getId())
                .interviewId(feedback.getInterviewId())
                .reviewerId(feedback.getReviewerId())
                .reviewerName(reviewer != null ? reviewer.getFullName() : null)
                .rating(feedback.getRating())
                .strengths(feedback.getStrengths())
                .weaknesses(feedback.getWeaknesses())
                .comments(feedback.getComments())
                .recommendation(feedback.getRecommendation())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private OfferResponse mapOfferToResponse(Offer offer) {
        Candidate candidate = candidateRepository.findById(offer.getCandidateId()).orElse(null);
        JobOpening job = jobOpeningRepository.findById(offer.getJobOpeningId()).orElse(null);

        return OfferResponse.builder()
                .id(offer.getId())
                .candidateId(offer.getCandidateId())
                .candidateName(candidate != null ? candidate.getName() : "Unknown")
                .jobOpeningId(offer.getJobOpeningId())
                .jobTitle(job != null ? job.getTitle() : "Unknown")
                .offeredSalary(offer.getOfferedSalary())
                .joiningDate(offer.getJoiningDate())
                .designation(offer.getDesignation())
                .department(offer.getDepartment())
                .offerNotes(offer.getOfferNotes())
                .status(offer.getStatus())
                .sentAt(offer.getSentAt())
                .respondedAt(offer.getRespondedAt())
                .createdAt(offer.getCreatedAt())
                .build();
    }

    private CandidateActivityResponse mapActivityToResponse(CandidateActivity activity) {
        Employee performer = activity.getPerformedBy() != null
                ? employeeRepository.findById(activity.getPerformedBy()).orElse(null) : null;

        return CandidateActivityResponse.builder()
                .id(activity.getId())
                .candidateId(activity.getCandidateId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .performedBy(activity.getPerformedBy())
                .performedByName(performer != null ? performer.getFullName() : null)
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private void logActivity(UUID candidateId, CandidateActivityType type, String description) {
        UUID performedBy = null;
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            performedBy = principal.getUserId();
        } catch (Exception ignored) {}

        CandidateActivity activity = CandidateActivity.builder()
                .candidateId(candidateId)
                .activityType(type)
                .description(description)
                .performedBy(performedBy)
                .build();

        activityRepository.save(activity);
    }
}
