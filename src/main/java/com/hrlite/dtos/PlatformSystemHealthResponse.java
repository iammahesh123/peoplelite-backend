package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSystemHealthResponse {
    private String apiStatus;
    private double avgResponseTime;
    private double errorRate;
    private double storageUsedGB;
    private double storageLimitGB;
    private double emailDeliveryRate;
    private LocalDateTime lastCheckedAt;
}
