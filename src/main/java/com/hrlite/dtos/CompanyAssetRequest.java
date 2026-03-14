package com.hrlite.dtos;

import com.hrlite.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAssetRequest {

    @NotNull(message = "Asset name is required")
    private String assetName;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    private String serialNumber;

    private String notes;
}
