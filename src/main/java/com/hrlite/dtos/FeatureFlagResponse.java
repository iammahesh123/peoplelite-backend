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
public class FeatureFlagResponse {

    private UUID id;
    private String name;
    private String description;
    private boolean enabled;
    private String scope;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
