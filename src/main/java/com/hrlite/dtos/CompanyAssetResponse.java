package com.hrlite.dtos;

import com.hrlite.enums.AssetStatus;
import com.hrlite.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAssetResponse {

    private UUID id;
    private String assetName;
    private AssetType assetType;
    private String serialNumber;
    private UUID assignedTo;
    private LocalDate assignedDate;
    private LocalDate returnDate;
    private AssetStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
