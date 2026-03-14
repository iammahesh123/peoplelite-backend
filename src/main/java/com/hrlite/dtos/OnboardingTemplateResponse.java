package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingTemplateResponse {
    private UUID id;
    private String taskName;
    private String description;
    private int orderIndex;
    private boolean active;
}
