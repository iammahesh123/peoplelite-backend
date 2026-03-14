package com.hrlite.service;

import com.hrlite.dtos.AssetAssignmentRequest;
import com.hrlite.dtos.AssetStatsResponse;
import com.hrlite.dtos.CompanyAssetRequest;
import com.hrlite.dtos.CompanyAssetResponse;
import com.hrlite.entity.CompanyAsset;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.AssetStatus;
import com.hrlite.repository.CompanyAssetRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final CompanyAssetRepository assetRepository;

    public List<CompanyAssetResponse> getAllAssets() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return assetRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CompanyAssetResponse createAsset(CompanyAssetRequest request) {
        CompanyAsset asset = CompanyAsset.builder()
                .assetName(request.getAssetName())
                .assetType(request.getAssetType())
                .serialNumber(request.getSerialNumber())
                .notes(request.getNotes())
                .status(AssetStatus.AVAILABLE)
                .build();

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    public CompanyAssetResponse updateAsset(UUID assetId, CompanyAssetRequest request) {
        CompanyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        asset.setAssetName(request.getAssetName());
        asset.setAssetType(request.getAssetType());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setNotes(request.getNotes());

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    public CompanyAssetResponse assignAsset(UUID assetId, AssetAssignmentRequest request) {
        CompanyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        asset.setAssignedTo(request.getEmployeeId());
        asset.setAssignedDate(LocalDate.now());
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setNotes(request.getNotes());

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    public CompanyAssetResponse returnAsset(UUID assetId) {
        CompanyAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + assetId));

        asset.setAssignedTo(null);
        asset.setReturnDate(LocalDate.now());
        asset.setStatus(AssetStatus.RETURNED);

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    public List<CompanyAssetResponse> getAssetsByEmployee(UUID employeeId) {
        return assetRepository.findByAssignedTo(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AssetStatsResponse getAssetStats() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return AssetStatsResponse.builder()
                .available(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.AVAILABLE))
                .assigned(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.ASSIGNED))
                .returned(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.RETURNED))
                .damaged(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.DAMAGED))
                .lost(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.LOST))
                .total(assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.AVAILABLE) +
                       assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.ASSIGNED) +
                       assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.RETURNED) +
                       assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.DAMAGED) +
                       assetRepository.countByTenantIdAndStatus(tenantId, AssetStatus.LOST))
                .build();
    }

    private CompanyAssetResponse mapToResponse(CompanyAsset asset) {
        return CompanyAssetResponse.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType())
                .serialNumber(asset.getSerialNumber())
                .assignedTo(asset.getAssignedTo())
                .assignedDate(asset.getAssignedDate())
                .returnDate(asset.getReturnDate())
                .status(asset.getStatus())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
