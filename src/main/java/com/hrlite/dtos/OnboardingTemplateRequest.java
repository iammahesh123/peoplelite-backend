package com.hrlite.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingTemplateRequest {

    @NotBlank(message = "Task name is required")
    private String taskName;

    private String description;
    private int orderIndex;
}
