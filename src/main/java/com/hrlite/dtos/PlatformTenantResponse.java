package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformTenantResponse {
    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private String status;
    private int employeeCount;
    private String founderName;
    private String founderEmail;
    private LocalDateTime createdAt;
    private LocalDateTime trialEndsAt;
    private LocalDateTime lastActiveAt;
    private double mrr;
}
