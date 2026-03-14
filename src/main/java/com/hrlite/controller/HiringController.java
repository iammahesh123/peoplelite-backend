package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.enums.*;
import com.hrlite.service.FeatureGateService;
import com.hrlite.service.HiringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hiring")
@RequiredArgsConstructor
public class HiringController {

    private final HiringService hiringService;
    private final FeatureGateService featureGateService;

    // ─── Job Openings ────────────────────────────────────────

    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<JobOpeningResponse>> createJob(
            @Valid @RequestBody JobOpeningRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        JobOpeningResponse response = hiringService.createJobOpening(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job opening created", response));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<JobOpeningResponse>>> getAllJobs(
            @RequestParam(required = false) String status) {
        featureGateService.requireFeature(Feature.HIRING);
        List<JobOpeningResponse> jobs;
        if (status != null) {
            jobs = hiringService.getJobOpeningsByStatus(JobStatus.valueOf(status));
        } else {
            jobs = hiringService.getAllJobOpenings();
        }
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<JobOpeningResponse>> getJob(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getJobOpeningById(id)));
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<JobOpeningResponse>> updateJob(
            @PathVariable UUID id, @Valid @RequestBody JobOpeningRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success("Job opening updated", hiringService.updateJobOpening(id, request)));
    }

    @PutMapping("/jobs/{id}/status")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(
            @PathVariable UUID id, @RequestParam JobStatus status) {
        featureGateService.requireFeature(Feature.HIRING);
        hiringService.updateJobStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Job status updated"));
    }

    // ─── Candidates ──────────────────────────────────────────

    @PostMapping("/candidates")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<CandidateResponse>> addCandidate(
            @Valid @RequestBody CandidateRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        CandidateResponse response = hiringService.addCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate added", response));
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getCandidates(
            @RequestParam(required = false) UUID jobId) {
        featureGateService.requireFeature(Feature.HIRING);
        List<CandidateResponse> candidates;
        if (jobId != null) {
            candidates = hiringService.getCandidatesByJob(jobId);
        } else {
            candidates = hiringService.getAllCandidates();
        }
        return ResponseEntity.ok(ApiResponse.success(candidates));
    }

    @GetMapping("/candidates/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidate(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getCandidateById(id)));
    }

    @PutMapping("/candidates/{id}/stage")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidateStage(
            @PathVariable UUID id, @Valid @RequestBody CandidateStageUpdateRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success("Candidate stage updated",
                hiringService.updateCandidateStage(id, request)));
    }

    // ─── Interviews ──────────────────────────────────────────

    @PostMapping("/interviews")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @Valid @RequestBody InterviewRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        InterviewResponse response = hiringService.scheduleInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled", response));
    }

    @GetMapping("/interviews")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviews(
            @RequestParam(required = false) UUID candidateId) {
        featureGateService.requireFeature(Feature.HIRING);
        List<InterviewResponse> interviews;
        if (candidateId != null) {
            interviews = hiringService.getInterviewsByCandidate(candidateId);
        } else {
            interviews = hiringService.getAllInterviews();
        }
        return ResponseEntity.ok(ApiResponse.success(interviews));
    }

    @PutMapping("/interviews/{id}/status")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateInterviewStatus(
            @PathVariable UUID id, @RequestParam InterviewStatus status) {
        featureGateService.requireFeature(Feature.HIRING);
        hiringService.updateInterviewStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Interview status updated"));
    }

    // ─── Interview Feedback ──────────────────────────────────

    @PostMapping("/feedback")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> addFeedback(
            @Valid @RequestBody InterviewFeedbackRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        InterviewFeedbackResponse response = hiringService.addFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback added", response));
    }

    @GetMapping("/feedback/{interviewId}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<InterviewFeedbackResponse>>> getFeedback(
            @PathVariable UUID interviewId) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getFeedbackByInterview(interviewId)));
    }

    // ─── Offers ──────────────────────────────────────────────

    @PostMapping("/offers")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
            @Valid @RequestBody OfferRequest request) {
        featureGateService.requireFeature(Feature.HIRING);
        OfferResponse response = hiringService.createOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offer created", response));
    }

    @GetMapping("/offers")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<OfferResponse>>> getOffers() {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getAllOffers()));
    }

    @PutMapping("/offers/{id}/status")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<OfferResponse>> updateOfferStatus(
            @PathVariable UUID id, @RequestParam OfferStatus status) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success("Offer status updated",
                hiringService.updateOfferStatus(id, status)));
    }

    // ─── Activity Timeline ───────────────────────────────────

    @GetMapping("/candidates/{id}/activities")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<CandidateActivityResponse>>> getCandidateActivities(
            @PathVariable UUID id) {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getCandidateActivities(id)));
    }

    // ─── Dashboard ───────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<HiringDashboardResponse>> getDashboard() {
        featureGateService.requireFeature(Feature.HIRING);
        return ResponseEntity.ok(ApiResponse.success(hiringService.getDashboardMetrics()));
    }
}
