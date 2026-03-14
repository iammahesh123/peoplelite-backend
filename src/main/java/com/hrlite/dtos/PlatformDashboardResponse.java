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
public class PlatformDashboardResponse {
    private PlatformMetricsResponse metrics;
    private List<PlatformTenantResponse> recentTenants;
    private PlatformSystemHealthResponse systemHealth;
    private List<RevenueChartData> revenueChart;
    private List<TenantGrowthData> tenantGrowthChart;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueChartData {
        private String month;
        private double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantGrowthData {
        private String month;
        private long count;
    }
}
