package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalarySlipTemplateResponse {

    private UUID id;

    private String name;

    private String htmlContent;

    private String description;

    private boolean isDefault;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
