package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {

    private List<ServiceStatus> services;
    private String uptime;
    private Metrics metrics;
    private Performance performance;
    private LocalDateTime lastCheckedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatus {
        private String name;
        private String status; // UP, DOWN, DEGRADED
        private double responseTime; // in milliseconds
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private double jvmMemoryUsed;
        private double jvmMemoryMax;
        private double cpuUsage;
        private double diskUsed;
        private double diskTotal;
        private long activeConnections;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Performance {
        private double avgResponseTime;
        private double requestsPerMinute;
        private double errorRate;
        private double p95ResponseTime;
    }
}
