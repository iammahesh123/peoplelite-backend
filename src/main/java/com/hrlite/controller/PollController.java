package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.enums.Feature;
import com.hrlite.service.PollService;
import com.hrlite.service.FeatureGateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;
    private final FeatureGateService featureGateService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<PollResponse>> createPoll(
            @Valid @RequestBody PollRequest request) {
        featureGateService.requireFeature(Feature.POLLS);
        PollResponse response = pollService.createPoll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Poll created successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getActivePolls() {
        List<PollResponse> polls = pollService.getActivePolls();
        return ResponseEntity.ok(ApiResponse.success(polls));
    }

    @PostMapping("/{pollId}/respond")
    public ResponseEntity<ApiResponse<Void>> submitResponse(
            @PathVariable UUID pollId,
            @Valid @RequestBody PollResponseSubmitRequest request) {
        pollService.submitResponse(pollId, request);
        return ResponseEntity.ok(ApiResponse.success("Response submitted successfully"));
    }

    @GetMapping("/{pollId}/results")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<PollResponse>> getPollResults(@PathVariable UUID pollId) {
        PollResponse response = pollService.getPollResults(pollId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{pollId}/close")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> closePoll(@PathVariable UUID pollId) {
        pollService.closePoll(pollId);
        return ResponseEntity.ok(ApiResponse.success("Poll closed successfully"));
    }
}
