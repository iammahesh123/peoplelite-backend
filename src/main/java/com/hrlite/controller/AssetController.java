package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.enums.Feature;
import com.hrlite.service.AssetService;
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
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final FeatureGateService featureGateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<CompanyAssetResponse>>> getAllAssets() {
        featureGateService.requireFeature(Feature.ASSETS);
        List<CompanyAssetResponse> assets = assetService.getAllAssets();
        return ResponseEntity.ok(ApiResponse.success(assets));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<CompanyAssetResponse>> createAsset(
            @Valid @RequestBody CompanyAssetRequest request) {
        featureGateService.requireFeature(Feature.ASSETS);
        CompanyAssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Asset created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<CompanyAssetResponse>> updateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyAssetRequest request) {
        featureGateService.requireFeature(Feature.ASSETS);
        CompanyAssetResponse response = assetService.updateAsset(id, request);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", response));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<CompanyAssetResponse>> assignAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AssetAssignmentRequest request) {
        featureGateService.requireFeature(Feature.ASSETS);
        CompanyAssetResponse response = assetService.assignAsset(id, request);
        return ResponseEntity.ok(ApiResponse.success("Asset assigned successfully", response));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyAssetResponse>> returnAsset(@PathVariable UUID id) {
        featureGateService.requireFeature(Feature.ASSETS);
        CompanyAssetResponse response = assetService.returnAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset returned successfully", response));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<AssetStatsResponse>> getAssetStats() {
        featureGateService.requireFeature(Feature.ASSETS);
        AssetStatsResponse stats = assetService.getAssetStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<CompanyAssetResponse>>> getAssetsByEmployee(
            @PathVariable UUID employeeId) {
        featureGateService.requireFeature(Feature.ASSETS);
        List<CompanyAssetResponse> assets = assetService.getAssetsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(assets));
    }
}
