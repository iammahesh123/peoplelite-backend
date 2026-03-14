package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageBreakdownResponse {

    private List<TenantStorage> tenants;
    private double totalUsedGB;
    private double totalAllocatedGB;
    private double usagePercentage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantStorage {
        private String tenantId;
        private String tenantName;
        private double usedGB;
        private double allocatedGB;
        private double usagePercentage;
    }
}
