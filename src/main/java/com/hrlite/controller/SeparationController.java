package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.enums.Feature;
import com.hrlite.service.SeparationService;
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
@RequestMapping("/api/v1/separations")
@RequiredArgsConstructor
public class SeparationController {

    private final SeparationService separationService;
    private final FeatureGateService featureGateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<SeparationResponse>> initiateSeparation(
            @Valid @RequestBody SeparationRequest request) {
        featureGateService.requireFeature(Feature.SEPARATIONS);
        SeparationResponse response = separationService.initiateSeparation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Separation initiated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<SeparationResponse>>> getAllSeparations() {
        featureGateService.requireFeature(Feature.SEPARATIONS);
        List<SeparationResponse> separations = separationService.getAllSeparations();
        return ResponseEntity.ok(ApiResponse.success(separations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<SeparationResponse>> getSeparationById(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.SEPARATIONS);
        SeparationResponse separation = separationService.getSeparationById(id);
        return ResponseEntity.ok(ApiResponse.success(separation));
    }

    @PutMapping("/{id}/checklist/{itemId}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateChecklistItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody ExitChecklistUpdateRequest request) {
        featureGateService.requireFeature(Feature.SEPARATIONS);
        separationService.updateChecklistItem(id, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Checklist item updated successfully"));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> completeSeparation(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.SEPARATIONS);
        separationService.completeSeparation(id);
        return ResponseEntity.ok(ApiResponse.success("Separation completed"));
    }
}
