package com.hrlite.entity;

import com.hrlite.enums.AssetStatus;
import com.hrlite.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "company_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAsset extends TenantAwareEntity {

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
